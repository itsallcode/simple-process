package org.itsallcode.process;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

class StreamCloseWaiter implements ProcessStreamConsumer {
    private static final Logger LOG = Logger.getLogger(StreamCloseWaiter.class.getName());
    private final CountDownLatch streamFinished = new CountDownLatch(1);
    private final Duration streamCloseTimeout;
    private final String name;
    private final long pid;

    StreamCloseWaiter(final String name, final long pid, final Duration streamCloseTimeout) {
        this.name = name;
        this.pid = pid;
        this.streamCloseTimeout = streamCloseTimeout;
    }

    void waitUntilStreamClosed() {
        LOG.finest(
                () -> "Waiting %s for stream '%s' of process %d to close".formatted(streamCloseTimeout, name, pid));
        if (!await(streamCloseTimeout)) {
            throw new IllegalStateException(
                    "Stream '%s' of process %d not closed within timeout of %s".formatted(name, pid,
                            streamCloseTimeout));
        } else {
            LOG.finest(() -> "Stream '%s' of process %d closed".formatted(name, pid));
        }
    }

    private boolean await(final Duration timeout) {
        try {
            return streamFinished.await(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (final InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "Interrupted while waiting for stream '%s' of process %d to be closed: %s"
                            .formatted(name, pid, exception.getMessage()),
                    exception);
        }
    }

    @Override
    public void accept(final String line) {
        // ignore
    }

    @Override
    public void streamFinished() {
        streamFinished.countDown();
    }

    @Override
    public void streamReadingFailed(final IOException exception) {
        streamFinished.countDown();
    }
}
