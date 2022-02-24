package info.kgeorgiy.ja.koton.arrayset;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NavigableSet;

class ReversedArraySet<E> extends ArraySet<E> {
    protected ReversedArraySet(ArraySet<E> subset) {
        super(subset);
    }

    public ReversedArraySet(ArraySet<E> subset, int head, int tail) {
        super(subset, head, tail);
    }

    @Override
    public E first() {
        return super.last();
    }

    @Override
    public E last() {
        return super.first();
    }

    @Override
    public Iterator<E> iterator() {
        return super.descendingIterator();
    }

    @Override
    public Iterator<E> descendingIterator() {
        return super.iterator();
    }

    @Override
    public NavigableSet<E> descendingSet() {
        return new ArraySet<>(this);
    }

    @Override
    public Comparator<? super E> comparator() {
        return super.comparator() == null ? null : super.comparator().reversed();
    }

    @Override
    public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
        return super.subSet(toElement, toInclusive, fromElement, fromInclusive).descendingSet();
    }

    @Override
    public NavigableSet<E> headSet(E toElement, boolean inclusive) {
        return super.tailSet(toElement, inclusive).descendingSet();
    }

    @Override
    public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
        return super.headSet(fromElement, inclusive).descendingSet();
    }

    @Override
    protected ArraySet<E> createSubSet(int head, int tail) {
        return new ReversedArraySet<>(this, head, tail);
    }

    @Override
    protected int binarySearch(E e, boolean inclusive, boolean lower) {
        return super.binarySearch(e, inclusive, !lower);
    }

    @Override
    protected int normalizeIndex(E e, boolean inclusive) {
        return binarySearch(e, inclusive, true);
    }
}
