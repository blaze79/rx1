package org.silentpom.stream;

import com.netflix.hystrix.exception.HystrixRuntimeException;
import org.silentpom.CommandHelloWorld;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by Vlad on 12.09.2017.
 */
public class ParallelAsyncServiceTest {
    private ParallelAsyncService service;
    private static final Logger LOG = LoggerFactory.getLogger(ParallelAsyncServiceTest.class);

    @BeforeMethod
    public void prepare() {
        service = new ParallelAsyncService(7);
    }

    @AfterMethod
    public void clean() {
        service.releaseBeanResources();
    }

    /**
     * работает но очень медленно, вызовы последовательны, 1238ms
     */
    @Test
    public void testNaive() {
        IntStream.range(1, 5).boxed().map(
                value -> executeCommand(value.toString())
        ).collect(Collectors.toList())
                .forEach(el ->LOG.info("List element: {}", el));
    }

    /**
     * падает - такое количество не влазит во внутрь
     */
    @Test(expectedExceptions = HystrixRuntimeException.class)
    public void testStupid() {

        try {
            IntStream.range(1, 50).boxed().map(
                    value -> executeCommandDelayed(value.toString())
            ).collect(Collectors.toList())
                    .forEach(el -> LOG.info("List element (FUTURE): {}", el.toString()));
        } catch (Exception ex) {
            LOG.error("Ooops", ex);
            throw ex;
        }
    }

    @Test
    public void testSmart() {
        service.waitStream(
                IntStream.range(1, 50).boxed().map(
                        service.parallelWarp(
                                value -> executeCommand(value.toString())
                        )
                )
        ).collect(Collectors.toList())
                .forEach(el -> LOG.info("List element: {}", el));
    }


    static String executeCommand(String str) {
        LOG.info("Direct Hystrix command created: {}", str);
        return new CommandHelloWorld(str).execute();
    }

    static Future<String> executeCommandDelayed(String str) {
        LOG.info("Direct Hystrix command created: {}", str);
        return new CommandHelloWorld(str).queue();
    }

}