package info.kgeorgiy.ja.koton.hello;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

public class HelloUDPNonblockingClient extends AbstractUDPClient {
    public static void main(String[] args) {
        main(new HelloUDPNonblockingClient(), args);
    }

    @Override
    public void run(InetSocketAddress address, String prefix, int threads, int requests) {
        try (
            Selector selector = Selector.open();
            Closeables<DatagramChannel> datagramChannels = new Closeables<>()
        ) {
            for (int i = 0; i < threads; ++i) {
                DatagramChannel channel = datagramChannels.add(selector.provider().openDatagramChannel());
                channel.connect(address);
                channel.configureBlocking(false);
                channel.register(selector, SelectionKey.OP_WRITE, new Attachment(i, channel.socket()));
            }

            while (!Thread.interrupted() && !selector.keys().isEmpty()) {
                if (selector.select(key -> {
                    if (!key.isValid()) {
                        return;
                    }
                    DatagramChannel channel = (DatagramChannel) key.channel();
                    Attachment attachment = (Attachment) key.attachment();
                    try {
                        if (key.isReadable()) {
                            channel.receive(attachment.receiveBuffer.clear());
                            String response = Util.extractMessage(attachment.receiveBuffer.flip());
                            if (validateResponse(response, attachment.nThread, attachment.nRequest)) {
                                System.out.println(response);
                                ++attachment.nRequest;
                            }
                            if (attachment.nRequest == requests) {
                                channel.close();
                            } else {
                                key.interestOps(SelectionKey.OP_WRITE);
                            }
                        } else if (key.isWritable()) {
                            String request = attachment.getMessage(prefix);
                            channel.send(ByteBuffer.wrap(Util.encodeMessage(request)), address);
                            key.interestOps(SelectionKey.OP_READ);
                        }
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }, TIMEOUT) == 0) {
                    selector.keys().forEach(key -> key.interestOps(SelectionKey.OP_WRITE));
                }
            }
        } catch (IOException | UncheckedIOException e) {
            throw new RuntimeException("Could not complete all requests", e);
        }
    }

    private static class Attachment {
        final int nThread;
        int nRequest;
        final ByteBuffer receiveBuffer;

        Attachment(int nThread, DatagramSocket socket) throws SocketException {
            this.nThread = nThread;
            receiveBuffer = ByteBuffer.allocateDirect(socket.getReceiveBufferSize());
        }

        String getMessage(String prefix) {
            return String.format(MESSAGE_FORMAT, prefix, nThread, nRequest);
        }
    }
}
