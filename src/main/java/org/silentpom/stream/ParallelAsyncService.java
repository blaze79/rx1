package org.silentpom.stream;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Wrapper for stream to use some time-controlled calls (hystrix) in parallel
 */
public class ParallelAsyncService {

    private static final Logger LOG = LoggerFactory.getLogger(ParallelAsyncService.class);

    private ExecutorService executorService;

    /**
     * ctor
     */
    public ParallelAsyncService(int threadSize) {
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat("parallel-async-thread-%d").build();

        executorService = Executors.newFixedThreadPool(threadSize, namedThreadFactory);
    }

    /**
     * Map user function T -> Ret to function T -> Future<Ret>. Adds task to executor service
     * @param mapper user function
     * @param <T> user function argument
     * @param <Ret> user function result
     * @return function to future
     */
    public <T, Ret> Function<T, Future<Ret>> parallelWarp(HystrixBasedMethod<T, Ret> mapper) {
        return (T t) -> {
            LOG.info("Submitting task to inner executor");
            Future<Ret> future = executorService.submit(() -> {
                LOG.info("Sending task to hystrix");
                return mapper.hystrixCommand(t);
            });
            return future;
        };
    }

    /**
     * waits all futures in stream and rethrow exception if occured
     * @param futureStream stream of futures
     * @param <T> type
     * @return stream of results
     */
    public <T> Stream<T> waitStream(Stream<Future<T>> futureStream) {
        List<Future<T>> futures = futureStream.collect(Collectors.toList());

        // wait all futures one by one.
        for (Future<T> future : futures) {
            try {
                future.get();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();

                if (cause instanceof RuntimeException) {
                    throw (RuntimeException) cause;
                }
                throw new RuntimeException(e);
            }
        }

        // all futures have completed, it is safe to call get
        return futures.stream().map(
                future -> {
                    try {
                        return future.get();
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }
        );
    }

    /**
     * destroy pool
     */
    @PreDestroy
    public void releaseBeanResources() {
        executorService.shutdown();
    }
}
