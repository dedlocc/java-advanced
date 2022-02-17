package info.kgeorgiy.ja.koton.walk;

import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;
import java.math.BigInteger;

public class HashWriter implements Closeable {
    private final Writer writer;

    public HashWriter(Writer writer) {
        this.writer = writer;
    }

    private void write(String file, BigInteger hash) throws IOException {
        writer.write(String.format("%040x %s%n", hash, file));
    }

    public void write(String file, byte[] hash) throws IOException {
        write(file, new BigInteger(1, hash));
    }

    public void writeNull(String file) throws IOException {
        write(file, BigInteger.ZERO);
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}
