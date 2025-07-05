package org.itsallcode.process;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

class StreamLogger implements ProcessStreamConsumer {

    private static final Logger LOG = Logger.getLogger(StreamLogger.class.getName());
    private final Level logLevel;
    private final long pid;
    private final String streamName;

    StreamLogger(long pid, String streamName, Level logLevel) {
        this.pid = pid;
        this.streamName = streamName;
        this.logLevel = logLevel;
    }

    @Override
    public void accept(String line) {
        LOG.log(logLevel, () -> "%d:%s> %s".formatted(pid, streamName, line));
    }

    @Override
    public void streamFinished() {
        // Ignore
    }

    @Override
    public void streamReadingFailed(IOException exception) {
        // Ignore
    }
}
