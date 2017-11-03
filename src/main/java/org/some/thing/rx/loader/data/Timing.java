package org.some.thing.rx.loader.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Wither;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@ToString
@NoArgsConstructor
@Wither
@AllArgsConstructor
public class Timing {
    private Integer id;
    private Long initialTime;
    private List<Long> times;
    @Getter
    private boolean finished = false;
    @Getter
    private boolean failed = false;
    @Getter
    private boolean measured = false;
    private LongSummaryStatistics cachedStatistics;
    private List<Long> cachedDistance;
    @Getter
    private Throwable exception;

    public void accept(TimingEvent event) {
        if (times == null)
            this.times = new ArrayList<>(100);
        this.id = event.getId();
        if (event.isInitial())
            this.initialTime = event.getTime();
        else {
            this.measured = true;
            this.finished |= event.isFinished();
            this.failed |= event.isFailed();
            this.times.add(event.getTime());
        }
        if (event.isFailed())
            this.exception = event.getException();
    }

    public int eventCount() {
        return times.size() - 1;//exclude last onComplete event
    }

    public Timing combine(Timing other) {
        Timing result = this.withFinished(this.finished | other.finished);
        result.finished = this.finished | other.finished;
        result.times.addAll(other.times);
        result.exception = other.exception;
        return result;
    }

    public LongSummaryStatistics getTimings() {
        if (cachedStatistics != null)
            return cachedStatistics;
        return cachedStatistics = times.stream().filter(Objects::nonNull).mapToLong(res -> res - initialTime).summaryStatistics();
    }

    public List<Long> getDistances() {
        if (cachedDistance != null)
            return cachedDistance;

        if (times == null || times.size() < 2)
            return Collections.emptyList();

        times.sort(Long::compareTo);

        return cachedDistance = IntStream.range(0, times.size() - 3) //skip last element cause it just timing of connection closing and other stuff
                .mapToLong(i -> times.get(i + 1) - times.get(i))
                .boxed()
                .collect(Collectors.toList());
    }
}
