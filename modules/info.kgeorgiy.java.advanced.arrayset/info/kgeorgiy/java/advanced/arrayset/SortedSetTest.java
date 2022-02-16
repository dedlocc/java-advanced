package info.kgeorgiy.java.advanced.arrayset;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;

/**
 * Tests for easy version
 * of <a href="https://www.kgeorgiy.info/courses/java-advanced/homeworks.html#homework-arrayset">ArraySet</a> homework
 * for <a href="https://www.kgeorgiy.info/courses/java-advanced/">Java Advanced</a> course.
 *
 * @author Georgiy Korneev (kgeorgiy@kgeorgiy.info)
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SortedSetTest extends BaseSetTest {
    public SortedSetTest() {
    }

    @Test
    public void test01_constructors() {
        final Class<?> token = loadClass();
        Assert.assertTrue(token.getName() + " should implement SortedSet interface", SortedSet.class.isAssignableFrom(token));

        checkConstructor("default constructor", token);
        checkConstructor("constructor out of Collection", token, Collection.class);
        checkConstructor("constructor out of Collection and Comparator", token, Collection.class, Comparator.class);
    }

    @Test
    public void test02_empty() {
        final SortedSet<Integer> set = set();
        Assert.assertEquals("Empty set size should be zero", 0, set.size());
        Assert.assertTrue("Empty set should be empty", set.isEmpty());
        Assert.assertEquals("toArray for empty set should return empty array", 0, (Object) set.toArray().length);
    }

    @Test
    public void test03_externalOrder() {
        test(BaseSetTest::assertEq);
    }

    @Test
    public void test04_contains() {
        testGet("contains(%s)", SortedSet::contains);
    }

    @Test
    public void test05_containsAll() {
        test(pair -> Assert.assertTrue("set should contains() all elements " + " " + pair.context, pair.tested.containsAll(pair.elements)));
        testGet("containsAll(List.of(%s))", (set, element) -> set.containsAll(List.of(element, element)));
    }

    @Test
    public void test06_comparator() {
        testGet("comparator()", SortedSet::comparator);
    }

    @Test
    public void test07_headSet() {
        testGet("headSet(%s)", SortedSet::headSet);
    }

    @Test
    public void test08_subSet() {
        testPairs((pair, from, to) -> {
            if (pair.comparator.compare(from, to) <= 0) {
                pair.testGet("subSet(" + from + ", " + to + ")", set -> set.subSet(from, to));
            }
        });
    }

    @Test
    public void test09_first() {
        testGet("first()", SortedSet::first);
    }
}
