package org.itsallcode.process;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

/**
 * Consumes stdout and stderr of a process asynchronously.
 */
class ProcessOutputConsumer<T> {
    private static final Logger LOG = Logger.getLogger(ProcessOutputConsumer.class.getName());
    private final Executor executor;
    private final Process process;
    private final List<Runnable> consumers;
    private final List<StreamCloseWaiter> streamCloseWaiter;
    private final StreamCollector<T> stdOutCollector;
    private final StreamCollector<T> stdErrCollector;

    static <T> ProcessOutputConsumer<T> create(final Executor executor, final Process process,
            final Duration streamCloseTimeout, final StreamCollector<T> stdOutCollector,
            final StreamCollector<T> stdErrCollector) {
        final long pid = process.pid();
        final StreamCloseWaiter stdOutCloseWaiter = new StreamCloseWaiter("stdOut", pid, streamCloseTimeout);
        final StreamCloseWaiter stdErrCloseWaiter = new StreamCloseWaiter("stdErr", pid, streamCloseTimeout);
        final AsyncStreamConsumer stdOutConsumer = new AsyncStreamConsumer("stdout", pid, process.getInputStream(),
                new DelegatingConsumer(List.of(stdOutCloseWaiter, stdOutCollector)));
        final AsyncStreamConsumer stdErrConsumer = new AsyncStreamConsumer("stderr", pid, process.getErrorStream(),
                new DelegatingConsumer(List.of(stdErrCloseWaiter, stdErrCollector)));
        return new ProcessOutputConsumer<>(executor, process, List.of(stdOutConsumer, stdErrConsumer),
                List.of(stdOutCloseWaiter, stdErrCloseWaiter), stdOutCollector, stdErrCollector);
    }

    ProcessOutputConsumer(final Executor executor, final Process process,
            final List<Runnable> consumers, final List<StreamCloseWaiter> streamCloseWaiter,
            final StreamCollector<T> stdOutCollector, final StreamCollector<T> stdErrCollector) {
        this.executor = executor;
        this.process = process;
        this.consumers = consumers;
        this.streamCloseWaiter = streamCloseWaiter;
        this.stdOutCollector = stdOutCollector;
        this.stdErrCollector = stdErrCollector;
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
