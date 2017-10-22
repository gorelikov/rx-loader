package org.some.thing.rx.loader;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import lombok.SneakyThrows;
import org.some.thing.rx.loader.logger.ColloredHelpFormatter;
import org.some.thing.rx.loader.logger.ColoredLogger;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class LoaderApplication {

  @SneakyThrows
  public static void main(String[] args) {

    OptionParser parser = new OptionParser();
    final OptionSpec<String> headerSpec = parser.accepts( "H", "Headers(could be delimited by ;)" )
        .withRequiredArg().withValuesSeparatedBy(";").ofType(String.class);
    final OptionSpec<Integer> threadSpec = parser.accepts("t", "threads")
        .withRequiredArg().ofType(Integer.class);
    final OptionSpec<Integer> connectionsSpec = parser.accepts("c", "connections")
        .withRequiredArg().ofType(Integer.class).defaultsTo(Integer.MAX_VALUE);
    final OptionSpec<Integer> maxRequestSpec = parser.accepts("r", "maxRequests")
        .withRequiredArg().ofType(Integer.class).defaultsTo(Integer.MAX_VALUE);
    final OptionSpec<String> addressSpec = parser.accepts("url", "requestURL")
        .withRequiredArg().ofType(String.class);
    final OptionSpec<String> durationSpec = parser.accepts("duration", "duration")
        .requiredUnless("r").withRequiredArg().ofType(String.class);
    final OptionSpec sslSpec = parser.accepts("k", "ignoreSSL");
    final OptionSpec sseSpec = parser.accepts("sse", "sseEnabled");
    parser.formatHelpWith(new ColloredHelpFormatter());

    OptionSet optionSet = null;
    try {
      optionSet = parser.parse(args);
    } catch (OptionException e) {
      ColoredLogger.log(ColoredLogger.RED, e.getMessage());
      parser.printHelpOn(System.out);
      System.exit(1);
    }

    final Map<String, String> headers = optionSet.valuesOf(headerSpec).stream()
        .map(res -> res.split(":", 2))
        .collect(Collectors.toMap(res -> res[0], res -> res[1]));

    Loader loader = new Loader(optionSet.valueOf(addressSpec), optionSet.has("k"), headers);
    loader.run(optionSet.valueOf(threadSpec), optionSet.valueOf(connectionsSpec),
        optionSet.valueOf(maxRequestSpec), Duration.parse("PT"+optionSet.valueOf(durationSpec).toUpperCase()),
        optionSet.has(sseSpec));

  }

}
