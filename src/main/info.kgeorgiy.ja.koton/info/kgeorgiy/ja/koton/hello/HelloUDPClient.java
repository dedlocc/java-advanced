package info.kgeorgiy.ja.koton.hello;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

public class HelloUDPClient extends AbstractUDPClient {
    public static void main(String[] args) {
        main(new HelloUDPClient(), args);
    }

    @Override
    public void run(InetSocketAddress address, String prefix, int threads, int requests) {
        ExecutorService threadPool = Executors.newFixedThreadPool(threads);

        try {
            for (var feature : threadPool.invokeAll(IntStream.range(0, threads).mapToObj(nThread -> (Callable<?>) () -> {
                try (var socket = new DatagramSocket()) {
                    socket.connect(address);
                    socket.setSoTimeout(TIMEOUT);
                    for (int nRequest = 0; nRequest < requests; ++nRequest) {
                        String request = String.format(MESSAGE_FORMAT, prefix, nThread, nRequest);
                        DatagramPacket outPacket = Util.createPacket(request);
                        DatagramPacket inPacket = Util.createPacket(socket.getReceiveBufferSize());
                        while (true) {
                            socket.send(outPacket);
                            try {
                                socket.receive(inPacket);
                                String response = Util.extractMessage(inPacket);
                                if (validateResponse(response, nThread, nRequest)) {
                                    System.out.println(response);
                                    break;
                                }
                            } catch (SocketTimeoutException ignored) {
                            }
                        }
                    }
                }
                return null;
            }).toList())) {
                feature.get();
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Could not complete all requests", e);
        } finally {
            Util.closeThreadPool(threadPool);
        }
    }
}
