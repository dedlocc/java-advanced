package info.kgeorgiy.ja.koton.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.util.Arrays;
import java.util.Objects;

public abstract class AbstractUDPServer implements HelloServer {
    protected static void main(HelloServer server, String[] args) {
        if (args == null || args.length != 2 || Arrays.stream(args).anyMatch(Objects::isNull)) {
            System.err.printf("Usage: %s <port> <threads>%n", server.getClass().getSimpleName());
            return;
        }

        try {
            server.start(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
        } catch (NumberFormatException e) {
            System.err.println(e.getMessage());
        }
    }

    protected String generateResponse(String request) {
        return "Hello, " + request;
    }
}
