package org.itsallcode.process;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

class SimpleProcess {
    private static final Logger LOG = Logger.getLogger(SimpleProcess.class.getName());

    private final Process process;
    private final String command;
    private final ProcessOutputConsumer consumer;

    SimpleProcess(final Process process, final ProcessOutputConsumer consumer, final String command) {
        this.process = process;
        this.consumer = consumer;
        this.command = command;
    }

    public static SimpleProcessBuilder builder() {
        return new SimpleProcessBuilder();
    }

    public int waitForTermination() {
        final int exitCode = waitForProcess();
        consumer.waitForStreamsClosed();
        return exitCode;
    }

    public void waitForSuccessfulTermination() {
        waitForTermination(0);
    }

    public void waitForTermination(final int expectedExitCode) {
        final int exitCode = waitForTermination();
        if (exitCode != expectedExitCode) {
            throw new IllegalStateException(
                    "Expected process %d (command '%s') to terminate with exit code %d but was %d"
                            .formatted(process.pid(), command, expectedExitCode, exitCode));
        }
    }

    private int waitForProcess() {
        try {
            LOG.finest(() -> "Waiting for process %d (command '%s') to terminate...".formatted(
                    process.pid(), command));
            return process.waitFor();
        } catch (final InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "Interrupted while waiting for process %d (command '%s') to finish".formatted(process.pid(),
                            command),
                    exception);
        }
    }

    public void waitForTermination(final Duration timeout) {
        waitForProcess(timeout);
        LOG.fine(() -> "Process %d (command '%s') terminated with exit code %d".formatted(process.pid(), command,
                exitValue()));
        consumer.waitForStreamsClosed();
    }

    private void waitForProcess(final Duration timeout) {
        try {
            LOG.finest(() -> "Waiting %s for process %d (command '%s') to terminate...".formatted(timeout,
                    process.pid(), command));
            if (!process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
                throw new IllegalStateException(
                        "Timeout while waiting %s for process %d (command '%s')".formatted(timeout, process.pid(),
                                command));
            }
        } catch (final InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "Interrupted while waiting %s for process %d (command '%s') to finish".formatted(timeout,
                            process.pid(), command),
                    exception);
        }
    }

    String getStdOut() {
        return consumer.getStdOut();
    }

    String getStdErr() {
        return consumer.getStdErr();
    }

    boolean isAlive() {
        return process.isAlive();
    }

    int exitValue() {
        return process.exitValue();
    }

    void destroy() {
        process.destroy();
    }

    void destroyForcibly() {
        process.destroyForcibly();
    }
}
