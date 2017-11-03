package org.some.thing.rx.loader;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Level;
import org.some.thing.rx.loader.logger.ColoredHelpFormatter;
import org.some.thing.rx.loader.logger.ColoredLogger;

import java.time.Duration;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class LoaderApplication {

    @SneakyThrows
    public static void main(String[] args) {
        Configurator.defaultConfig()
                .formatPattern("{message}")
                .activate();

        OptionParser parser = new OptionParser();
        parser.formatHelpWith(new ColoredHelpFormatter());

        final OptionSpec<String> headerSpec = parser.accepts("H", "Headers(could be delimited by ;)")
                .withRequiredArg().withValuesSeparatedBy(";").ofType(String.class);
        final OptionSpec<Integer> threadSpec = parser.accepts("t", "threads")
                .withRequiredArg().required().ofType(Integer.class);
        final OptionSpec<Integer> connectionsSpec = parser.accepts("c", "connections")
                .withRequiredArg().ofType(Integer.class).defaultsTo(Integer.MAX_VALUE);
        final OptionSpec<Integer> maxRequestSpec = parser.accepts("r", "maxRequests")
                .withRequiredArg().ofType(Integer.class).defaultsTo(Integer.MAX_VALUE);
        final OptionSpec<String> addressSpec = parser.accepts("url", "requestURL")
                .withRequiredArg().required().ofType(String.class);
        final OptionSpec<String> durationSpec = parser.accepts("duration", "duration")
                .requiredUnless("r").withRequiredArg().ofType(String.class);
        final OptionSpec sslSpec = parser.accepts("k", "ignoreSSL");
        final OptionSpec sseSpec = parser.accepts("sse", "sseEnabled");
        final OptionSpec debugSpec = parser.accepts("debugEnabled", "debugEnabled");

        OptionSet optionSet = null;
        try {
            optionSet = parser.parse(args);
        } catch (OptionException e) {
            ColoredLogger.log(ColoredLogger.RED, e.getMessage());
            parser.printHelpOn(System.out);
            System.exit(1);
        }

        Configurator.currentConfig()
                .level(optionSet.has(debugSpec) ? Level.DEBUG : Level.INFO)
                .activate();

        final Map<String, String> headers = parseHeaders(headerSpec, optionSet);

        Loader loader = new Loader(optionSet.valueOf(addressSpec), optionSet.has(sslSpec),
                headers, optionSet.has(debugSpec), optionSet.has(sseSpec));

        loader.run(optionSet.valueOf(threadSpec), optionSet.valueOf(connectionsSpec),
                optionSet.valueOf(maxRequestSpec), Duration.parse("PT" + optionSet.valueOf(durationSpec).toUpperCase()));

    }

    private static Map<String, String> parseHeaders(OptionSpec<String> headerSpec, OptionSet optionSet) {
        return optionSet.valuesOf(headerSpec).stream()
                .map(res -> res.split(":", 2))
                .collect(Collectors.toMap(res -> res[0], res -> res[1]));
    }

}
