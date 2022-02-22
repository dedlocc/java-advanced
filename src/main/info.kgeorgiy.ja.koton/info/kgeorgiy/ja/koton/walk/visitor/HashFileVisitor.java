package info.kgeorgiy.ja.koton.walk.visitor;

import info.kgeorgiy.ja.koton.walk.HashWriter;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.DigestInputStream;
import java.security.MessageDigest;

public class HashFileVisitor extends SimpleFileVisitor<Path> {
    protected final HashWriter hashWriter;
    private final MessageDigest digest;

    public HashFileVisitor(HashWriter hashWriter, MessageDigest digest) {
        this.hashWriter = hashWriter;
        this.digest = digest;
    }

    @Override
    public FileVisitResult visitFile(Path filePath, BasicFileAttributes attrs) throws IOException {
        try (
            InputStream inputStream = new FileInputStream(filePath.toFile());
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            DigestInputStream digestStream = new DigestInputStream(bufferedInputStream, digest)
        ) {
            //noinspection StatementWithEmptyBody
            while (digestStream.read() >= 0) {}
            hashWriter.write(filePath.toString(), digest.digest());
        } catch (IOException e) {
            return visitFileFailed(filePath, e);
        }

        return super.visitFile(filePath, attrs);
    }

    @Override
    public FileVisitResult visitFileFailed(Path filePath, IOException e) throws IOException {
        System.err.println(e.getMessage());
        hashWriter.writeNull(filePath.toString());
        return FileVisitResult.CONTINUE;
    }
}
