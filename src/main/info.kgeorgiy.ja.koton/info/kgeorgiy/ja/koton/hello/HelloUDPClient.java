package info.kgeorgiy.ja.koton.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.net.*;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class HelloUDPClient implements HelloClient {
    private static final String MESSAGE_FORMAT = "%s%d_%d";
    private static final int SOCKET_TIMEOUT = 500;
    private static final Pattern RESPONSE_PATTERN = Pattern.compile("\\D*(\\d+)\\D*(\\d+)\\D*");

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
                    socket.connect(address);
                    socket.setSoTimeout(SOCKET_TIMEOUT);
                    for (int nRequest = 0; nRequest < requests; ++nRequest) {
                        String request = String.format(MESSAGE_FORMAT, prefix, nThread, nRequest);
                        DatagramPacket outPacket = Util.createPacket(request);
                        DatagramPacket inPacket = Util.createPacket(socket);
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

    private static boolean validateResponse(String response, int nThread, int nRequest) {
        Matcher matcher = RESPONSE_PATTERN.matcher(response);
        return matcher.matches() && matcher.group(1).equals(Integer.toString(nThread)) && matcher.group(2).equals(Integer.toString(nRequest));
    }
}
