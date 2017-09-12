package org.silentpom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Vlad on 11.09.2017.
 */
public class RxStart {

    private static final Logger LOG = LoggerFactory.getLogger(RxStart.class);

    /**
     * создает список из observable, считая, что при отсутствии ошибок он будет порождать элемент не реже секунды
     * @param observable observable
     * @return список
     */
    static List<String> toList(Observable<String> observable) {
        return observable.timeout(1, TimeUnit.SECONDS).toList().toBlocking().single();
    }

    /**
     * создает горячую хистриксную команду
     * @param str  входные данные
     * @return hot observable результата
     */
    static Observable<String> executeCommand(String str) {
        LOG.info("Hot Hystrix command created: {}", str);
        return new CommandHelloWorld(str).observe();
    }

    /**
     * создает холодную хистриксную команду
     * @param str  входные данные
     * @return hot observable результата
     */
    static Observable<String> executeCommandDelayed(String str) {
        LOG.info("Cold Hystrix command created: {}", str);
        return new CommandHelloWorld(str).toObservable();
    }
}
