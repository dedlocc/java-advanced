package info.kgeorgiy.ja.koton.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractUDPClient implements HelloClient {
    protected static final String MESSAGE_FORMAT = "%s%d_%d";
    protected static final int TIMEOUT = 200;
    protected static final Pattern RESPONSE_PATTERN = Pattern.compile("\\D*(\\d+)\\D*(\\d+)\\D*");

    protected static void main(HelloClient client, String[] args) {
        if (args == null || args.length != 5 || Arrays.stream(args).anyMatch(Objects::isNull)) {
            System.err.printf("Usage: %s <port> <threads>%n", client.getClass().getSimpleName());
            return;
        }

        try {
            int i = 0;
            client.run(
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
        run(new InetSocketAddress(host, port), prefix, threads, requests);
    }

    protected abstract void run(InetSocketAddress address, String prefix, int threads, int requests);

    protected static boolean validateResponse(String response, int nThread, int nRequest) {
        Matcher matcher = RESPONSE_PATTERN.matcher(response);
        return matcher.matches() && matcher.group(1).equals(Integer.toString(nThread)) && matcher.group(2).equals(Integer.toString(nRequest));
    }
}
