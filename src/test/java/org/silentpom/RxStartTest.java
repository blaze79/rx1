package org.silentpom;

import com.netflix.hystrix.exception.HystrixRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import rx.Observable;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.silentpom.RxStart.*;

/**
 * Created by Vlad on 11.09.2017.
 */
public class RxStartTest {

    private static final Logger LOG = LoggerFactory.getLogger(RxStartTest.class);

    /**
     * наивный тест, все работает
     */
    @Test
    public void testNaive() {
        List<Integer> source = IntStream.range(1, 7).boxed().collect(Collectors.toList());

        Observable<String> observable = Observable.from(source)
                .flatMap(elem -> executeCommand(elem.toString()));

        toList(observable).forEach(el ->LOG.info("List element: {}", el));
    }

    /**
     * увеличиваем размер списка и внезапно падаем
     * @throws Exception HystrixRuntimeException от переполнения пула
     */
    @Test(expectedExceptions = HystrixRuntimeException.class)
    public void testStupid() throws Exception {
        try {
            List<Integer> source = IntStream.range(1, 50).boxed().collect(Collectors.toList());

            Observable<String> observable = Observable.from(source)
                    .flatMap(elem -> executeCommandDelayed(elem.toString()));

            toList(observable).forEach(el -> LOG.info("List element: {}", el));
        } catch (Exception ex) {
            LOG.error("Ooops", ex);
            throw ex;
        }
    }

    /**
     * разбиваем поток на ленивые команды, формируем пачки по 7 штук, которые последовательно перебираются,
     * но сами элементы пачки обрабатываются параллельно
     */
    @Test
    public void testWindow() {
        List<Integer> source = IntStream.range(1, 50).boxed().collect(Collectors.toList());

        Observable<String> observable =  Observable.from(source)
                        .map(elem -> executeCommandDelayed(elem.toString()))
                        .window(7)
                        .concatMap(window -> window.flatMap(x -> x));

        toList(observable).forEach(el ->LOG.info("List element: {}", el));
    }
}