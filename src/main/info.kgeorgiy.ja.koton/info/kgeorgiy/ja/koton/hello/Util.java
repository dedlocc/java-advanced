package info.kgeorgiy.ja.koton.hello;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

final class Util {
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    private static final int DEFAULT_POOL_TIMEOUT = 60;

    static DatagramPacket createPacket(byte[] buf) {
        return new DatagramPacket(buf, buf.length);
    }

    static DatagramPacket createPacket(int bufferSize) {
        return createPacket(new byte[bufferSize]);
    }

    static DatagramPacket createPacket(DatagramSocket socket) throws SocketException {
        return createPacket(socket.getReceiveBufferSize());
    }

    static DatagramPacket createPacket(String message) {
        return createPacket(message.getBytes(DEFAULT_CHARSET));
    }

    static String extractMessage(DatagramPacket packet) {
        return new String(packet.getData(), packet.getOffset(), packet.getLength(), DEFAULT_CHARSET);
    }

    static boolean closeThreadPool(ExecutorService pool, int timeout) {
        pool.shutdownNow();

        try {
            return pool.awaitTermination(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            return false;
        }
    }

    static boolean closeThreadPool(ExecutorService pool) {
        return closeThreadPool(pool, DEFAULT_POOL_TIMEOUT);
    }
}
