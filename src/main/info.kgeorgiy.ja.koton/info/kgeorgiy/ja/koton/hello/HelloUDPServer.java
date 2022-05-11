package info.kgeorgiy.ja.koton.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HelloUDPServer implements HelloServer {
    private DatagramSocket socket;
    private ExecutorService threadPool;

    public static void main(String[] args) {
        if (args == null || args.length != 2 || Arrays.stream(args).anyMatch(Objects::isNull)) {
            System.err.println("Usage: HelloUDPServer <port> <threads>");
            return;
        }

        try (var server = new HelloUDPServer()) {
            server.start(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
        } catch (NumberFormatException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public void start(int port, int threads) {
        int bufferSize;
        try {
            socket = new DatagramSocket(port);
            bufferSize = socket.getReceiveBufferSize();
        } catch (SocketException e) {
            throw new RuntimeException("Couldn't open UDP socket", e);
        }

        threadPool = Executors.newFixedThreadPool(threads);

        for (int i = 0; i < threads; ++i) {
            threadPool.submit(() -> {
                DatagramPacket inPacket = Util.createPacket(bufferSize);
                while (!Thread.interrupted() && !socket.isClosed()) {
                    try {
                        socket.receive(inPacket);
                        DatagramPacket outPacket = Util.createPacket("Hello, " + Util.extractMessage(inPacket));
                        outPacket.setSocketAddress(inPacket.getSocketAddress());
                        socket.send(outPacket);
                    } catch (IOException ignored) {
                    }
                }
            });
        }
    }

    @Override
    public void close() {
        if (socket != null) {
            socket.close();
        }
        if (threadPool != null) {
            Util.closeThreadPool(threadPool);
        }
    }
}
