package info.kgeorgiy.ja.koton.hello;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Closeables<T extends Closeable> implements Closeable, Iterable<T> {
    private final List<T> closeables = new ArrayList<>();

    public T add(T closeable) {
        closeables.add(closeable);
        return closeable;
    }

    @Override
    public void close() throws IOException {
        for (var closeable : closeables) {
            closeable.close();
        }
    }

    @Override
    public Iterator<T> iterator() {
        return closeables.iterator();
    }
}
