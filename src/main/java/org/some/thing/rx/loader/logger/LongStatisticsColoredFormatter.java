package org.some.thing.rx.loader.logger;

import org.some.thing.rx.loader.data.LongStatistics;

public class LongStatisticsColoredFormatter {
  private static final Integer FIELD_WIDTH = 15;

  public static String header() {
    return String.format(
        "%s%-" + FIELD_WIDTH + "s%s%-" + FIELD_WIDTH+ "s%s%-" + FIELD_WIDTH + "s%s%-" + FIELD_WIDTH +"s%s%-" + FIELD_WIDTH + "s%s",
        ColoredLogger.BLUE_BOLD,
        "Event name",
        ColoredLogger.YELLOW_BOLD,
        "min(ms)",
        ColoredLogger.BLUE_BOLD,
        "max(ms)",
        ColoredLogger.YELLOW_BOLD,
        "avg(ms)",
        ColoredLogger.BLUE_BOLD,
        "stdev(ms)",
        ColoredLogger.RESET
    );
  }
  public static String toString(String eventName, LongStatistics statistics) {
    return String.format(
        "%s%-" + FIELD_WIDTH + "s%s%-" + FIELD_WIDTH+ "d%s%-" + FIELD_WIDTH + "d%s%-" + FIELD_WIDTH +"s%s%-" + FIELD_WIDTH + "s%s",
        ColoredLogger.BLUE_BOLD,
        eventName,
        ColoredLogger.YELLOW_BOLD,
        statistics.getMin() == Long.MAX_VALUE ? 0 : statistics.getMin(),
        ColoredLogger.BLUE_BOLD,
        statistics.getMax() == Long.MIN_VALUE ? 0 : statistics.getMax(),
        ColoredLogger.YELLOW_BOLD,
        String.format("%.2f", statistics.getAverage()),
        ColoredLogger.BLUE_BOLD,
        String.format("%.2f", statistics.getStandardDeviation()),
        ColoredLogger.RESET
    );
  }
}
