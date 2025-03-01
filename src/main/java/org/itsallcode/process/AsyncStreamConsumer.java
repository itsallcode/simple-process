package org.itsallcode.process;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

class AsyncStreamConsumer implements Runnable {
    private static final Logger LOG = Logger.getLogger(AsyncStreamConsumer.class.getName());
    private final String name;
    private final long pid;
    private final ProcessStreamConsumer consumer;
    private final InputStream stream;

    AsyncStreamConsumer(final String name, final long pid, final InputStream stream,
            final ProcessStreamConsumer consumer) {
        this.name = name;
        this.pid = pid;
        this.stream = stream;
        this.consumer = consumer;
    }

    @Override
    public void run() {
        LOG.finest(() -> "Start reading from '%s' stream of process %d...".formatted(name, pid));
        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                consumer.accept(line);
            }
            LOG.finest(() -> "Stream '%s' of process %d finished".formatted(name, pid));
            consumer.streamFinished();
        } catch (final IOException exception) {
            final Level logLevel = "Stream closed".equals(exception.getMessage()) ? Level.FINEST : Level.WARNING;
            LOG.log(logLevel,
                    "Reading stream '%s' of process %d failed: %s".formatted(name, pid,
                            exception.getMessage()),
                    exception);
            consumer.streamReadingFailed(exception);
        }
    }
}
