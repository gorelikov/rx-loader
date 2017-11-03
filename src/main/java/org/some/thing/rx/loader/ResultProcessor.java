package org.some.thing.rx.loader;

import lombok.RequiredArgsConstructor;
import org.some.thing.rx.loader.data.LongStatistics;
import org.some.thing.rx.loader.data.Timing;
import org.some.thing.rx.loader.logger.ColoredLogger;
import org.some.thing.rx.loader.logger.LongStatisticsColoredFormatter;

import java.util.LongSummaryStatistics;
import java.util.Map;

@RequiredArgsConstructor
public class ResultProcessor {
    private final boolean debugOutput;

    public void processResult(Map<Integer, Timing> result, long seconds) {
        LongStatistics firstAnswerStat = result.values().stream()
                .filter(res -> !res.isFailed() && res.isMeasured())
                .map(Timing::getTimings)
                .map(LongSummaryStatistics::getMin)
                .collect(
                        LongStatistics::new,
                        LongStatistics::accept,
                        LongStatistics::combine
                );

        final Long errors = result.values().stream()
                .filter(Timing::isFailed)
                .count();

        LongStatistics answerDistanceStat = result.values().stream()
                .filter(res -> !res.isFailed() && res.isMeasured())
                .flatMap(res -> res.getDistances().stream())
                .filter(res -> res > 0.0d)//yeah, i know, i would think about that, but for now it just could not be real
                .collect(
                        LongStatistics::new,
                        LongStatistics::accept,
                        LongStatistics::combine
                );

        LongStatistics answerStat = result.values().stream()
                .filter(res -> !res.isFailed() && res.isFinished() && res.isMeasured())
                .map(Timing::getTimings)
                .map(LongSummaryStatistics::getMax)
                .collect(
                        LongStatistics::new,
                        LongStatistics::accept,
                        LongStatistics::combine
                );

        LongStatistics eventStat = result.values().stream()
                .filter(res -> !res.isFailed() && res.isMeasured())
                .map(Timing::eventCount)
                .collect(
                        LongStatistics::new,
                        LongStatistics::accept,
                        LongStatistics::combine
                );

        ColoredLogger.log(LongStatisticsColoredFormatter.header());
        ColoredLogger.log(LongStatisticsColoredFormatter.toString("First", firstAnswerStat));
        ColoredLogger.log(LongStatisticsColoredFormatter.toString("Distance", answerDistanceStat));
        ColoredLogger.log(LongStatisticsColoredFormatter.toString("Total", answerStat));
        ColoredLogger.log(ColoredLogger.GREEN_BOLD, "--------------------------------------------------------------------");
        ColoredLogger.log(LongStatisticsColoredFormatter.toString("Event count", eventStat));
        ColoredLogger.log(ColoredLogger.GREEN_BOLD, "Total requests sent: " + result.size());
        ColoredLogger.log(ColoredLogger.GREEN_BOLD, "Total requests finished: " + answerStat.getCount());
        ColoredLogger.log(ColoredLogger.GREEN_BOLD, "Requests per second:\t" + (double)answerStat.getCount() / seconds);
        ColoredLogger.log(ColoredLogger.RED_UNDERLINED, "Total errors: " + errors);
        if(debugOutput) {
            result.values().stream().filter(res -> res.isFailed()).forEach(res -> System.out.println(res.getException()));
        }
    }
}
