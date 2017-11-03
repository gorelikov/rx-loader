package org.some.thing.rx.loader;

import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.client.HttpClientRequest;
import io.reactivex.netty.protocol.http.client.HttpClientResponse;
import org.some.thing.rx.loader.data.Timing;
import org.some.thing.rx.loader.data.TimingEvent;
import org.some.thing.rx.loader.logger.ColoredLogger;
import rx.Observable;
import rx.Scheduler;
import rx.schedulers.Schedulers;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class Loader {
    private final HttpClientRequest<ByteBuf, ByteBuf> clientObs;
    private final String address;
    private final boolean sse;
    private final ResultProcessor resultProcessor;

    public Loader(String address, boolean ignoreSSl, Map<String, String> headers, boolean debugEnabled, boolean sse) {
        this.clientObs = ClientFactory.create(address, ignoreSSl, headers);
        this.address = address;
        this.resultProcessor = new ResultProcessor(debugEnabled);
        this.sse = sse;
    }

    public void run(int threads, int connections,
                    int requests, Duration duration) {
        final Integer maxConnections = connections < 1 ? Integer.MAX_VALUE : connections;
        final Integer requestCount = requests < 1 ? Integer.MAX_VALUE : requests;

        Scheduler scheduler;
        if (threads < 1) {
            scheduler = Schedulers.from(Executors.newFixedThreadPool(threads));
        } else {
            scheduler = Schedulers.computation();
        }

        Observable<TimingEvent> timingEventObservable = Observable.range(0, requestCount)
                .flatMap(id -> measureEvents(clientObs, scheduler, id), maxConnections);

        if (requestCount == Integer.MAX_VALUE)
            timingEventObservable = timingEventObservable.takeUntil(Observable.timer(duration.getSeconds(), TimeUnit.SECONDS));

        ColoredLogger.log(ColoredLogger.GREEN_BOLD, String.format("Running %ds test @ %s\n %d threads and %d connections",
                duration.getSeconds(),
                address,
                threads,
                connections
        ));

        timingEventObservable.toList()
                .map(this::aggregate)
                .toBlocking()
                .subscribe(res -> resultProcessor.processResult(res, duration.getSeconds()));
    }

    private Observable<TimingEvent> measureEvents(HttpClientRequest<ByteBuf, ByteBuf> clientObs, Scheduler scheduler, Integer id) {
        final TimingEvent initial = TimingEvent.builder().id(id).initial(true).time(System.currentTimeMillis()).build();
        return clientObs.subscribeOn(scheduler)
                .flatMap(this::mapToTiming)
                .map(clientRes -> TimingEvent.builder().id(id).time(System.currentTimeMillis()).build())
                .startWith(initial)
                .concatWith(Observable.just(1)
                        .map(just -> TimingEvent.builder().id(id).finished(true).time(System.currentTimeMillis()).build()))
                .onErrorReturn(err -> TimingEvent.builder().id(id).failed(true).time(System.currentTimeMillis()).exception(err).build());
    }

    private Observable<Long> mapToTiming(HttpClientResponse<ByteBuf> clientRes) {
        if (sse || (clientRes.getHeader("Content-type", "text/plain").contains("event")))
            return clientRes.getContentAsServerSentEvents()
                    .map(body -> System.currentTimeMillis());
        else
            return clientRes.getContent()
                    .map(body -> System.currentTimeMillis())
                    .takeLast(1);
    }

    private Map<Integer, Timing> aggregate(List<TimingEvent> list) {
        return list.stream()
                .collect(Collectors.groupingBy(
                        TimingEvent::getId,
                        Collector.of(
                                Timing::new,
                                Timing::accept,
                                Timing::combine
                        ))
                );
    }
}
