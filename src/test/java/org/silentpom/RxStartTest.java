package org.silentpom;

import org.testng.annotations.Test;
import rx.Observable;


import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.testng.Assert.*;

/**
 * Created by Vlad on 11.09.2017.
 */
public class RxStartTest {

    @Test
    public void testName() throws Exception {
        List<Integer> source = IntStream.range(1, 50).boxed().collect(Collectors.toList());

        Observable.from(source).filter(
                elem -> elem % 2 > 0
        ).map(
                i -> i.toString()
        ).map(
                str -> "Some " + str + " string object"
        ).subscribe(
                elem -> {
                    System.out.println(elem);
                },
                er -> {
                    er.printStackTrace(System.out);
                },
                () -> {
                    System.out.println("Compleeeeeeete!");
                }
        );
    }

    @Test
    public void testFlatMap() throws Exception {
        List<Integer> source = IntStream.range(1, 50).boxed().collect(Collectors.toList());

        Observable.from(source).filter(
                elem -> elem % 2 > 0
        ).map(
                i -> i.toString()
        ).flatMap(
                str -> Observable.just("Some " + str + " string object")
        ).subscribe(
                elem -> {
                    System.out.println(elem);
                },
                er -> {
                    er.printStackTrace(System.out);
                },
                () -> {
                    System.out.println("Compleeeeeeete!");
                }
        );
    }

    @Test
    public void testFlatMapDelay() throws Exception {
        List<Integer> source = IntStream.range(1, 50).boxed().collect(Collectors.toList());

        Observable.from(source).filter(
                elem -> elem % 2 > 0
        ).map(
                i -> i.toString()
        ).flatMap(
                str -> Observable.timer(500, TimeUnit.MILLISECONDS).map(zero -> "Some async " + str + " string object")
        ).subscribe(
                        elem -> {
                            System.out.println(elem);
                        },
                        er -> {
                            er.printStackTrace(System.out);
                        },
                        () -> {
                            System.out.println("Compleeeeeeete!");
                        }
                );

        TimeUnit.MILLISECONDS.sleep(1000);
    }

    @Test
    public void testFlatMapDelayList() throws Exception {
        List<Integer> source = IntStream.range(1, 50).boxed().collect(Collectors.toList());

        Observable.from(source).filter(
                elem -> elem % 2 > 0
        ).map(
                i -> i.toString()
        ).flatMap(
                str -> Observable.timer(500, TimeUnit.MILLISECONDS).map(zero -> "Some async " + str + " string object")
        ).timeout(
                5000,
                TimeUnit.MILLISECONDS
        ).toList()
                .toBlocking()
                .single().forEach(str -> {
            System.out.println("element of list:" + str);
        });

    }
}