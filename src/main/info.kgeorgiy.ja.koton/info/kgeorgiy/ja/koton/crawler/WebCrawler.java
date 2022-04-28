package info.kgeorgiy.ja.koton.crawler;

import info.kgeorgiy.ja.koton.implementor.Implementor;
import info.kgeorgiy.java.advanced.crawler.*;
import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

public class WebCrawler implements Crawler {
    private final Downloader downloader;
    private final ExecutorService downloaders;
    private final ExecutorService extractors;
    private final int maxPerHost;
    private final Map<String, PerHostDownloader> perHostDownloaders;

    public static void main(String[] args) {
        if (args == null || args.length < 1 || args.length > 5 || Arrays.stream(args).anyMatch(Objects::isNull)) {
            System.err.println("Usage: WebCrawler url [depth [downloads [extractors [perHost]]]]");
            return;
        }

        Result result;
        try (var crawler = new WebCrawler(
            new CachingDownloader(),
            getIntArgOrDefault(args, 2, 1),
            getIntArgOrDefault(args, 3, 1),
            getIntArgOrDefault(args, 4, Integer.MAX_VALUE)
        )) {
            result = crawler.download(args[0], getIntArgOrDefault(args, 1, 1));
        } catch (IOException e) {
            System.err.println("Couldn't create caching downloader: " + e.getMessage());
            return;
        } catch (NumberFormatException e) {
            System.err.println("Couldn't parse integer: " + e.getMessage());
            return;
        }

        System.out.println("Downloaded:");
        for (var url : result.getDownloaded()) {
            System.out.println("- " + url);
        }

        if (!result.getErrors().isEmpty()) {
            System.out.println("Errors:");
            for (var entry : result.getErrors().entrySet()) {
                System.out.println("- " + entry.getKey());
                System.out.println("  " + entry.getValue().getMessage());
            }
        }
    }

    private static int getIntArgOrDefault(String[] args, int index, int defaultValue) {
        return args.length > index ? Integer.parseInt(args[index]) : defaultValue;
    }

    public WebCrawler(Downloader downloader, int downloaders, int extractors, int maxPerHost) {

        this.downloader = downloader;
        this.downloaders = Executors.newFixedThreadPool(downloaders);
        this.extractors = Executors.newFixedThreadPool(extractors);
        this.maxPerHost = maxPerHost;
        this.perHostDownloaders = new ConcurrentHashMap<>();
    }

    @Override
    public Result download(String url, int depth) {
        Set<String> downloaded = ConcurrentHashMap.newKeySet();
        Map<String, IOException> errors = new ConcurrentHashMap<>();
        Set<String> cache = ConcurrentHashMap.newKeySet();

        IntStream.rangeClosed(1, depth).boxed().<List<String>>reduce(new ArrayList<>(List.of(url)), (q, i) -> q.stream().filter(cache::add).map(curUrl -> {
            var task = new Task(curUrl, new FutureTask<>(() -> {
                Document document;
                try {
                    document = downloader.download(curUrl);
                } finally {
                    perHostDownloaders.get(URLUtils.getHost(curUrl)).release();
                }
                downloaded.add(curUrl);
                return i == depth ? CompletableFuture.completedFuture(Collections.emptyList()) : extractors.submit(document::extractLinks);
            }));

            try {
                if (!perHostDownloaders.containsKey(task.getHost())) {
                    perHostDownloaders.put(task.getHost(), new PerHostDownloader());
                }
                perHostDownloaders.get(task.getHost()).run(task.future);
                return task;
            } catch (MalformedURLException e) {
                errors.put(curUrl, e);
                return null;
            }
        }).filter(Objects::nonNull).toList().stream().flatMap(task -> {
            try {
                return awaitFuture(awaitFuture(task.future)).stream();
            }
            catch (ExecutionException e) {
                errors.put(task.url, (IOException) e.getCause());
            }
            return Stream.empty();
        }).toList(), (q1, q2) -> {
            throw new UnsupportedOperationException();
        });

        return new Result(List.copyOf(downloaded), errors);
    }

    private record Task(String url, RunnableFuture<Future<List<String>>> future) {
        String getHost() throws MalformedURLException {
            return URLUtils.getHost(url);
        }
    }

    private static <T> T awaitFuture(Future<T> future) throws ExecutionException {
        do {
            try {
                return future.get();
            } catch (InterruptedException ignored) {
            }
        } while (!future.isDone());
        return null;
    }

    private class PerHostDownloader {
        private int downloading;
        private final Queue<Runnable> queue = new ArrayDeque<>();

        public synchronized void run(Runnable task) {
            if (downloading < maxPerHost) {
                ++downloading;
                downloaders.submit(task);
            } else {
                queue.add(task);
            }
        }

        public synchronized void release() {
            if (queue.isEmpty()) {
                --downloading;
            } else {
                downloaders.submit(queue.remove());
            }
        }
    }

    @Override
    public void close() {
        while (!downloaders.isShutdown() || !extractors.isShutdown()) {
            downloaders.shutdownNow();
            extractors.shutdownNow();
        }
    }
}
