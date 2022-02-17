package info.kgeorgiy.ja.koton.walk;

import info.kgeorgiy.ja.koton.walk.visitor.HashFileVisitor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.function.BiFunction;

public class Walker {
    public final BiFunction<HashWriter, MessageDigest, HashFileVisitor> visitorFactory;

    public Walker(BiFunction<HashWriter, MessageDigest, HashFileVisitor> visitorFactory) {
        this.visitorFactory = visitorFactory;
    }

    public void walk(String[] args) {
        if (args == null || args.length < 2 || args[0] == null || args[1] == null) {
            System.err.println("Usage: java Walk <input file> <output file>");
            return;
        }

        MessageDigest sha1;
        try {
            sha1 = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            System.err.println("SHA-1 is not available");
            return;
        }

        try (
            BufferedReader reader = Files.newBufferedReader(Path.of(args[0]));
            BufferedWriter writer = Files.newBufferedWriter(Path.of(args[1]), StandardOpenOption.CREATE)
        ) {
            HashWriter hashWriter = new HashWriter(writer);
            HashFileVisitor visitor = visitorFactory.apply(hashWriter, sha1);

            String file;
            while ((file = reader.readLine()) != null) {
                try {
                    Files.walkFileTree(Path.of(file), visitor);
                } catch (InvalidPathException e) {
                    System.err.println(e.getMessage());
                    hashWriter.writeNull(file);
                }
            }
        } catch (InvalidPathException | IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
