package info.kgeorgiy.ja.koton.walk.visitor;

import info.kgeorgiy.ja.koton.walk.HashWriter;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.DigestInputStream;
import java.security.MessageDigest;

public class NoDirsHashFileVisitor extends HashFileVisitor {
    public NoDirsHashFileVisitor(HashWriter hashWriter, MessageDigest digest) {
        super(hashWriter, digest);
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        hashWriter.writeNull(dir.toString());
        return FileVisitResult.SKIP_SUBTREE;
    }
}
