package info.kgeorgiy.ja.koton.arrayset;

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

class ForwardIterator<E> implements Iterator<E> {
    private final Iterator<E> iterator;

    public ForwardIterator(List<E> list) {
        this.iterator = list.iterator();
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public E next() {
        return iterator.next();
    }

    @Override
    public void forEachRemaining(Consumer<? super E> action) {
        iterator.forEachRemaining(action);
    }
}
