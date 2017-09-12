package org.silentpom;

import com.netflix.hystrix.HystrixCommand;
import rx.Observable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by Vlad on 11.09.2017.
 */
public class RxStart {

    public static void main(String... args) throws InterruptedException {
        //stupidMethod();
        methodWindow();
        methodBuffer();
    }

    private static void methodBuffer() {
        List<Integer> source = IntStream.range(1, 50).boxed().collect(Collectors.toList());

        Observable<String> observable =
        Observable.from(source)
                .map(elem -> executeCommandDelayed(elem.toString()))
                .buffer(7)
                .concatMap(list ->  Observable.from(list).flatMap(observable1 -> observable1));
         toList(observable).forEach(s -> System.out.println(s));
    }

    private static void methodWindow() {
        List<Integer> source = IntStream.range(1, 50).boxed().collect(Collectors.toList());

        Observable<String> observable =
                Observable.from(source)
                        .map(elem -> executeCommandDelayed(elem.toString()))
                        .window(7)
                        .concatMap(window -> window.flatMap(x -> x));

        toList(observable).forEach(s -> System.out.println(s));
    }

    private static void stupidMethod() {
        List<Integer> source = IntStream.range(1, 50).boxed().collect(Collectors.toList());

        Observable<String> observable = Observable.from(source)
                .flatMap(elem -> executeCommand(elem.toString()));
        toList(observable).forEach(s -> System.out.println(s));
    }

    //Running, pool size = 10, active threads = 10, queued tasks = 0, completed tasks = 0
    private static List<String> toList(Observable<String> observable) {
        return observable.timeout(1, TimeUnit.SECONDS).toList().toBlocking().single();
    }


    private static Observable<String> executeCommand(String str) {
        return new CommandHelloWorld(str).observe();
    }

    private static Observable<String> executeCommandDelayed(String str) {
        return new CommandHelloWorld(str).toObservable();
    }
}
