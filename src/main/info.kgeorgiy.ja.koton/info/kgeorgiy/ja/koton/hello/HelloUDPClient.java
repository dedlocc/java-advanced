package info.kgeorgiy.ja.koton.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.net.*;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.stream.IntStream;

public class HelloUDPClient implements HelloClient {
    private static final String MESSAGE_FORMAT = "%s%d_%d";
    private static final int SOCKET_TIMEOUT = 500;

    public static void main(String[] args) {
        if (args == null || args.length != 5 || Arrays.stream(args).anyMatch(Objects::isNull)) {
            System.err.println("Usage: HelloUDPClient <port> <threads>");
            return;
        }

        try {
            int i = 0;
            new HelloUDPClient().run(
                args[i++],
                Integer.parseInt(args[i++]),
                args[i++],
                Integer.parseInt(args[i++]),
                Integer.parseInt(args[i++])
            );
        } catch (NumberFormatException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public void run(String host, int port, String prefix, int threads, int requests) {
        ExecutorService threadPool = Executors.newFixedThreadPool(threads);
        InetSocketAddress address = new InetSocketAddress(host, port);

        try {
            for (var feature : threadPool.invokeAll(IntStream.range(0, threads).mapToObj(nThread -> (Callable<?>) () -> {
                try (var socket = new DatagramSocket()) {
                    socket.setReuseAddress(true);
                    socket.connect(address);
                    socket.setSoTimeout(SOCKET_TIMEOUT);
                    for (int nRequest = 0; nRequest < requests; ++nRequest) {
                        String message = String.format(MESSAGE_FORMAT, prefix, nThread, nRequest);
                        DatagramPacket outPacket = Util.createPacket(message);
                        DatagramPacket inPacket = Util.createPacket(socket);
                        while (true) {
                            socket.send(outPacket);
                            try {
                                socket.receive(inPacket);
                                String response = Util.extractMessage(inPacket);
                                if (response.contains(message)) {
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
