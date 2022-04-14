package info.kgeorgiy.ja.koton.concurrent;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * @author Artem Koton
 */
public class ParallelMapperImpl implements ParallelMapper {
    private final List<Thread> threadPool;
    private final Queue<Runnable> tasks = new ArrayDeque<>();

    /**
     * Starts {@code threads} threads to be used for mapping.
     *
     * @param threads number of threads
     */
    public ParallelMapperImpl(int threads) {
        if (threads == 0) {
            throw new IllegalArgumentException("At least one thread is required");
        }

        threadPool = Stream.generate(() -> {
            Thread thread = new Thread(this::run);
            thread.start();
            return thread;
        }).limit(threads).toList();
    }

    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> args) throws InterruptedException {
        List<R> results = new ArrayList<>(Collections.nCopies(args.size(), null));
        CountToZero tasksRemaining = new CountToZero(args.size());

        for (int i = 0; i < args.size(); ++i) {
            int argIndex = i;
            tasks.add(() -> {
                results.set(argIndex, f.apply(args.get(argIndex)));
                tasksRemaining.decrement();
            });
            synchronized (this) {
                notifyAll();
            }
        }

        tasksRemaining.waitForZero();

        return results;
    }

    @Override
    public void close() {
        threadPool.forEach(Thread::interrupt);
    }

    private void run() {
        try {
            while (true) {
                nextTask().run();
            }
        } catch (InterruptedException ignored) {}
    }

    private synchronized Runnable nextTask() throws InterruptedException {
        while (tasks.isEmpty()) {
            wait();
        }

        return tasks.remove();
    }

    private static class CountToZero {
        private int value;

        public CountToZero(int initialValue) {
            value = initialValue;
        }

        public synchronized void decrement() {
            if (--value == 0) {
                notify();
            }
        }

        public synchronized void waitForZero() throws InterruptedException {
            while (value > 0) {
                wait();
            }
        }
    }
}
