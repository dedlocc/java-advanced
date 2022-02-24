package info.kgeorgiy.ja.koton.arrayset;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

class ReverseIterator<E> implements Iterator<E> {
    private final ListIterator<E> iterator;

    public ReverseIterator(List<E> list) {
        this.iterator = list.listIterator(list.size());
    }

    @Override
    public boolean hasNext() {
        return iterator.hasPrevious();
    }

    @Override
    public E next() {
        return iterator.previous();
    }
}
