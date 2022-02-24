package info.kgeorgiy.ja.koton.arrayset;

import java.util.*;

public class ArraySet<E> extends AbstractSet<E> implements NavigableSet<E> {
    private final List<E> list;
    private final Comparator<? super E> comparator;

    public ArraySet() {
        list = Collections.emptyList();
        comparator = null;
    }

    public ArraySet(Collection<? extends E> c, Comparator<? super E> comparator) {
        var treeSet = new TreeSet<E>(comparator);
        treeSet.addAll(c);
        this.list = new ArrayList<>(treeSet);
        this.comparator = comparator;
    }

    public ArraySet(Collection<? extends E> c) {
        this(c, null);
    }

    protected ArraySet(ArraySet<E> subset, List<E> list) {
        this.list = list;
        this.comparator = subset.comparator;
    }

    protected ArraySet(ArraySet<E> subset) {
        this(subset, subset.list);
    }

    protected ArraySet(ArraySet<E> subset, int head, int tail) {
        this(subset, head <= tail ? subset.list.subList(head, tail) : Collections.emptyList());
    }

    //

    @Override
    public boolean contains(Object o) {
        @SuppressWarnings("unchecked")
        var e = (E) o;
        return Collections.binarySearch(list, e, comparator) >= 0;
    }

    @Override
    public E lower(E e) {
        return getOrNull(binarySearch(e, false, true));
    }

    @Override
    public E floor(E e) {
        return getOrNull(binarySearch(e, true, true));
    }

    @Override
    public E ceiling(E e) {
        return getOrNull(binarySearch(e, true, false));
    }

    @Override
    public E higher(E e) {
        return getOrNull(binarySearch(e, false, false));
    }

    @Override
    public E pollFirst() {
        throw new UnsupportedOperationException();
    }

    @Override
    public E pollLast() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<E> iterator() {
        return new ForwardIterator<>(list);
    }

    @Override
    public NavigableSet<E> descendingSet() {
        return new ReversedArraySet<>(this);
    }

    @Override
    public Iterator<E> descendingIterator() {
        return new ReverseIterator<>(list);
    }

    @Override
    public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
        if (getComparator().compare(fromElement, toElement) > 0) {
            throw new IllegalArgumentException("Invalid range: fromElement > toElement");
        }
        return createSubSet(normalizeIndex(fromElement, fromInclusive), normalizeIndex(toElement, !toInclusive));
    }

    @Override
    public NavigableSet<E> headSet(E toElement, boolean inclusive) {
        return createSubSet(0, normalizeIndex(toElement, !inclusive));
    }

    @Override
    public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
        return createSubSet(normalizeIndex(fromElement, inclusive), size());
    }

    @Override
    public Comparator<? super E> comparator() {
        return comparator;
    }

    @Override
    public SortedSet<E> subSet(E fromElement, E toElement) {
        return subSet(fromElement, true, toElement, false);
    }

    @Override
    public SortedSet<E> headSet(E toElement) {
        return headSet(toElement, false);
    }

    @Override
    public SortedSet<E> tailSet(E fromElement) {
        return tailSet(fromElement, true);
    }

    @Override
    public E first() {
        assertNotEmpty();
        return list.get(0);
    }

    @Override
    public E last() {
        assertNotEmpty();
        return list.get(size() - 1);
    }

    @Override
    public int size() {
        return list.size();
    }

    //

    private E getOrNull(int index) {
        return index >= 0 && index < size() ? list.get(index) : null;
    }

    private void assertNotEmpty() {
        if (isEmpty()) {
            throw new NoSuchElementException("ArraySet is empty");
        }
    }

    private Comparator<? super E> getComparator() {
        return comparator == null ? (e1, e2) -> {
            @SuppressWarnings("unchecked")
            var comparable = (Comparable<? super E>) e1;
            return comparable.compareTo(e2);
        } : comparator;
    }

    protected ArraySet<E> createSubSet(int head, int tail) {
        return new ArraySet<>(this, head, tail);
    }

    protected int binarySearch(E e, boolean inclusive, boolean lower) {
        int bs = Collections.binarySearch(list, e, comparator);
        return bs >= 0 ? bs + (inclusive ? 0 : lower ? -1 : 1) : -(bs + (lower ? 2 : 1));
    }

    protected int normalizeIndex(E e, boolean inclusive) {
        return binarySearch(e, inclusive, false);
    }
}
