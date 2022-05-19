package info.kgeorgiy.ja.koton.hello;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HelloUDPNonblockingServer extends AbstractUDPServer {
    protected static final int TIMEOUT = 200;

    public static void main(String[] args) {
        try (var server = new HelloUDPNonblockingServer()) {
            main(server, args);
        }
    }

    private Selector selector;
    private DatagramChannel channel;
    private ExecutorService workers;
    private Thread thread;

    @Override
    public void start(int port, int threads) {
        try {
            selector = Selector.open();
            channel = selector.provider().openDatagramChannel();
            channel.bind(new InetSocketAddress(port));
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_READ, new Attachment(channel.socket().getReceiveBufferSize()));
            workers = Executors.newFixedThreadPool(threads);
        } catch (IOException e) {
            close();
            throw new RuntimeException("Could not start server", e);
        }

        thread = new Thread(() -> {
            while (!Thread.interrupted()) {
                try {
                    selector.select(this::handle, TIMEOUT);
                } catch (IOException e) {
                    throw new RuntimeException("An error occurred during server execution", e);
                }
            }
        });
        thread.start();
    }

    private void handle(SelectionKey key) {
        if (!key.isValid()) {
            return;
        }

        Attachment attachment = (Attachment) key.attachment();

        try {
            if (key.isWritable()) {
                if (attachment.responseQueue.isEmpty()) {
                    key.interestOps(SelectionKey.OP_READ);
                } else {
                    Response response = attachment.responseQueue.remove();
                    channel.send(ByteBuffer.wrap(Util.encodeMessage(response.message)), response.address);
                    key.interestOps(SelectionKey.OP_WRITE | SelectionKey.OP_READ);
                }
            } else if (key.isReadable()) {
                ByteBuffer buffer = attachment.receiveBuffer;
                SocketAddress address = channel.receive(buffer.clear());
                String request = Util.extractMessage(buffer.flip());
                workers.submit(() -> {
                    attachment.responseQueue.add(new Response(generateResponse(request), address));
                    key.interestOps(SelectionKey.OP_WRITE | SelectionKey.OP_READ);
                    selector.wakeup();
                });
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void close() {
        if (thread != null) {
            thread.interrupt();
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        if (workers != null) {
            Util.closeThreadPool(workers);
        }
        try {
            if (channel != null) {
                channel.close();
            }
            if (selector != null) {
                selector.close();
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not stop server", e);
        }
    }

    private record Response(String message, SocketAddress address) {
    }

    private static class Attachment {
        final ByteBuffer receiveBuffer;
        Queue<Response> responseQueue = new ConcurrentLinkedQueue<>();

        Attachment(int bufferSize) {
            receiveBuffer = ByteBuffer.allocateDirect(bufferSize);
        }
    }
}
