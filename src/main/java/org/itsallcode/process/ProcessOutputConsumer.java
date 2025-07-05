package org.itsallcode.process;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Consumes stdout and stderr of a process asynchronously.
 */
final class ProcessOutputConsumer<T> {
    private static final String STD_ERR_NAME = "stdErr";
    private static final String STD_OUT_NAME = "stdOut";
    private static final Logger LOG = Logger.getLogger(ProcessOutputConsumer.class.getName());
    private final Executor executor;
    private final Process process;
    private final List<Runnable> consumers;
    private final List<StreamCloseWaiter> streamCloseWaiter;
    private final StreamCollector<T> stdOutCollector;
    private final StreamCollector<T> stdErrCollector;

    private ProcessOutputConsumer(final Executor executor, final Process process,
            final List<Runnable> consumers, final List<StreamCloseWaiter> streamCloseWaiter,
            final StreamCollector<T> stdOutCollector, final StreamCollector<T> stdErrCollector) {
        this.executor = executor;
        this.process = process;
        this.consumers = Collections.unmodifiableList(consumers);
        this.streamCloseWaiter = Collections.unmodifiableList(streamCloseWaiter);
        this.stdOutCollector = stdOutCollector;
        this.stdErrCollector = stdErrCollector;
    }

    static <T> ProcessOutputConsumer<T> create(final Executor executor, final Process process,
            final Duration streamCloseTimeout, Level logLevel, final StreamCollector<T> stdOutCollector,
            final StreamCollector<T> stdErrCollector) {
        final long pid = process.pid();
        final StreamCloseWaiter stdOutCloseWaiter = new StreamCloseWaiter(STD_OUT_NAME, pid, streamCloseTimeout);
        final StreamCloseWaiter stdErrCloseWaiter = new StreamCloseWaiter(STD_ERR_NAME, pid, streamCloseTimeout);
        final AsyncStreamConsumer stdOutConsumer = new AsyncStreamConsumer(STD_OUT_NAME, pid, process.getInputStream(),
                new DelegatingConsumer(
                        List.of(stdOutCloseWaiter, stdOutCollector, new StreamLogger(pid, STD_OUT_NAME, logLevel))));
        final AsyncStreamConsumer stdErrConsumer = new AsyncStreamConsumer(STD_ERR_NAME, pid, process.getErrorStream(),
                new DelegatingConsumer(
                        List.of(stdErrCloseWaiter, stdErrCollector, new StreamLogger(pid, STD_ERR_NAME, logLevel))));
        return new ProcessOutputConsumer<>(executor, process, List.of(stdOutConsumer, stdErrConsumer),
                List.of(stdOutCloseWaiter, stdErrCloseWaiter), stdOutCollector, stdErrCollector);
    }

    void start() {
        LOG.finest(() -> "Start reading stdout and stderr streams of process %d in background..."
                .formatted(process.pid()));
        consumers.forEach(executor::execute);
    }

    T getStdOut() {
        return stdOutCollector.getResult();
    }

    T getStdErr() {
        return stdErrCollector.getResult();
    }

    void waitForStreamsClosed() {
        streamCloseWaiter.forEach(StreamCloseWaiter::waitUntilStreamClosed);
    }
}
