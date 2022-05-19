package info.kgeorgiy.ja.koton.hello;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HelloUDPServer extends AbstractUDPServer {
    public static void main(String[] args) {
        try (var server = new HelloUDPServer()) {
            main(server, args);
        }
    }

    private DatagramSocket socket;
    protected ExecutorService threadPool;

    @Override
    public void start(int port, int threads) {
        int bufferSize;
        try {
            socket = new DatagramSocket(port);
            bufferSize = socket.getReceiveBufferSize();
        } catch (SocketException e) {
            close();
            throw new RuntimeException("Couldn't open UDP socket", e);
        }

        threadPool = Executors.newFixedThreadPool(threads);

        for (int i = 0; i < threads; ++i) {
            threadPool.submit(() -> {
                DatagramPacket inPacket = Util.createPacket(bufferSize);
                while (!Thread.interrupted() && !socket.isClosed()) {
                    try {
                        socket.receive(inPacket);
                        DatagramPacket outPacket = Util.createPacket(generateResponse(Util.extractMessage(inPacket)));
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
