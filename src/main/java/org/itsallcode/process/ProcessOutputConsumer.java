package org.itsallcode.process;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

class ProcessOutputConsumer {
    private static final Logger LOG = Logger.getLogger(ProcessOutputConsumer.class.getName());
    private final Executor executor;
    private final Process process;
    private final ProcessStreamConsumer stdOutConsumer;
    private final ProcessStreamConsumer stdErrConsumer;

    ProcessOutputConsumer(final Executor executor, final Process process, final Duration streamCloseTimeout) {
        this(executor, process, new ProcessStreamConsumer("stdout", process.pid(), streamCloseTimeout),
                new ProcessStreamConsumer("stderr", process.pid(), streamCloseTimeout));
    }

    ProcessOutputConsumer(final Executor executor, final Process process,
            final ProcessStreamConsumer stdOutConsumer, final ProcessStreamConsumer stdErrConsumer) {
        this.executor = executor;
        this.process = process;
        this.stdOutConsumer = stdOutConsumer;
        this.stdErrConsumer = stdErrConsumer;
    }

    void start() {
        LOG.finest(() -> "Start reading stdout and stderr streams of process %d in background..."
                .formatted(process.pid()));
        executor.execute(() -> readStream(process.getInputStream(), stdOutConsumer));
        executor.execute(() -> readStream(process.getErrorStream(), stdErrConsumer));
    }

    private void readStream(final InputStream stream, final ProcessStreamConsumer consumer) {
        LOG.finest(() -> "Start reading from '%s' stream of process %d...".formatted(consumer.name, process.pid()));
        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                consumer.accept(line);
            }
            LOG.finest(() -> "Stream '%s' of process %d finished".formatted(consumer.name, process.pid()));
            consumer.streamFinished();
        } catch (final IOException exception) {
            LOG.log(Level.WARNING,
                    "Reading stream '%s' of process %d failed: %s".formatted(consumer.name, process.pid(),
                            exception.getMessage()),
                    exception);
            consumer.streamFinished();
        }
    }

    String getStdOut() {
        return stdOutConsumer.getContent();
    }

    String getStdErr() {
        return stdErrConsumer.getContent();
    }

    void waitForStreamsClosed() {
        stdOutConsumer.waitUntilStreamClosed();
        stdErrConsumer.waitUntilStreamClosed();
    }

    private static class ProcessStreamConsumer {
        private final CountDownLatch streamFinished = new CountDownLatch(1);
        private final StringBuilder builder = new StringBuilder();
        private final String name;
        private final long pid;
        private final Duration streamCloseTimeout;

        ProcessStreamConsumer(final String name, final long pid, final Duration streamCloseTimeout) {
            this.name = name;
            this.pid = pid;
            this.streamCloseTimeout = streamCloseTimeout;
        }

        String getContent() {
            return builder.toString();
        }

        void streamFinished() {
            streamFinished.countDown();
        }

        void accept(final String line) {
            LOG.fine(() -> "%d %s > %s".formatted(pid, name, line));
            builder.append(line).append("\n");
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
    }
}
