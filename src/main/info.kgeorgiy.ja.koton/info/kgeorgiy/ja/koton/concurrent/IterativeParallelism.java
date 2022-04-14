package info.kgeorgiy.ja.koton.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ListIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * @author Artem Koton
 */
public class IterativeParallelism implements ListIP {
    private final ParallelMapper parallelMapper;

    /**
     * Creates {@code IterativeParallelism} that will create {@code parallelMapper} each time it needs to parallel tasks.
     */
    public IterativeParallelism() {
        parallelMapper = null;
    }

    /**
     * Creates {@code IterativeParallelism} that will use {@code parallelMapper} for running parallel tasks.
     *
     * @param parallelMapper mapper to be used
     */
    public IterativeParallelism(ParallelMapper parallelMapper) {
        this.parallelMapper = parallelMapper;
    }

    @Override
    public String join(int threads, List<?> values) throws InterruptedException {
        return parallelize(
            threads, values,
            Collectors.mapping(Object::toString, Collectors.joining()),
            Collectors.joining()
        );
    }

    @Override
    public <T> List<T> filter(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return parallelize(
            threads, values,
            Collectors.filtering(predicate, Collectors.<T>toList()),
            Collectors.flatMapping(Collection::stream, Collectors.toList())
        );
    }

    @Override
    public <T, U> List<U> map(int threads, List<? extends T> values, Function<? super T, ? extends U> f) throws InterruptedException {
        return parallelize(
            threads, values,
            Collectors.mapping(f, Collectors.<U>toList()),
            Collectors.flatMapping(Collection::stream, Collectors.toList())
        );
    }

    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return parallelize(
            threads, values,
            Collectors.collectingAndThen(Collectors.maxBy(comparator), Optional::get),
            Collectors.maxBy(comparator)
        ).get();
    }

    @Override
    public <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return maximum(threads, values, Collections.reverseOrder(comparator));
    }

    @Override
    public <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return parallelize(
            threads, values,
            Collectors.reducing(true, predicate::test, Boolean::logicalAnd),
            Collectors.reducing(true, Boolean::logicalAnd)
        );
    }

    @Override
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return !all(threads, values, Predicate.not(predicate));
    }

    private <T, U, R> R parallelize(
        int threads,
        List<? extends T> values,
        Collector<? super T, ?, U> accumulatingCollector,
        Collector<? super U, ?, R> combiningCollector
    ) throws InterruptedException {
        if (threads == 0) {
            throw new IllegalArgumentException("At least one thread is required");
        }
        threads = Math.min(values.size(), threads);

        int blockSize = values.size() / threads;
        int remainder = values.size() % threads;

        List<List<? extends T>> subLists = new ArrayList<>(threads);

        int pos = 0;
        for (int i = 0; i < threads; ++i) {
            subLists.add(values.subList(pos, pos += blockSize + (i < remainder ? 1 : 0)));
        }

        ParallelMapper parallelMapper = Objects.requireNonNullElseGet(this.parallelMapper, () -> new ParallelMapperImpl(subLists.size()));
        R results = parallelMapper.map(list -> list.stream().collect(accumulatingCollector), subLists).stream().collect(combiningCollector);

        if (this.parallelMapper == null) {
            parallelMapper.close();
        }

        return results;
    }
}
