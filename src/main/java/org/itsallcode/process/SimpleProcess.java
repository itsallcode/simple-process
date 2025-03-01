package org.itsallcode.process;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Provides control over native processes.
 * 
 * @param <T> type of stdout and stderr, e.g. {@link String}.
 */
public class SimpleProcess<T> {
    private static final Logger LOG = Logger.getLogger(SimpleProcess.class.getName());
    private final Process process;
    private final String command;
    private final ProcessOutputConsumer<T> consumer;

    SimpleProcess(final Process process, final ProcessOutputConsumer<T> consumer, final String command) {
        this.process = process;
        this.consumer = consumer;
        this.command = command;
    }

    /**
     * Wait until the process has terminated.
     * 
     * @return exit code
     * @see Process#waitFor()
     */
    public int waitForTermination() {
        final int exitCode = waitForProcess();
        consumer.waitForStreamsClosed();
        return exitCode;
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

    /**
     * Wait until the process terminates successfully with exit code {@code 0}.
     * 
     * @throws IllegalStateException if exit code is not equal to {@code 0}.
     * @see #waitForTermination()
     */
    public void waitForSuccessfulTermination() {
        waitForTermination(0);
    }

    /**
     * Wait until the process terminates successfully with the given exit code.
     * 
     * @param expectedExitCode expected exit code
     * @throws IllegalStateException if exit code is not equal to the given expected
     *                               exit code.
     * @see #waitForTermination(int)
     */
    public void waitForTermination(final int expectedExitCode) {
        final int exitCode = waitForTermination();
        if (exitCode != expectedExitCode) {
            throw new IllegalStateException(
                    "Expected process %d (command '%s') to terminate with exit code %d but was %d"
                            .formatted(process.pid(), command, expectedExitCode, exitCode));
        }
    }

    /**
     * Wait until the process terminates with the given timeout.
     * 
     * @param timeout maximum time to wait for the termination
     * @throws IllegalStateException if process does not exit within the given
     *                               timeout.
     * @see Process#waitFor(long, TimeUnit)
     */
    public void waitForTermination(final Duration timeout) {
        waitForProcess(timeout);
        LOG.finest(() -> "Process %d (command '%s') terminated with exit code %d".formatted(process.pid(), command,
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

    /**
     * Get the standard output of the process.
     * 
     * @return standard output
     */
    T getStdOut() {
        return consumer.getStdOut();
    }

    /**
     * Get the standard error of the process.
     * 
     * @return standard error
     */
    T getStdErr() {
        return consumer.getStdErr();
    }

    /**
     * Check wether the process is alive.
     * 
     * @return {@code  true} if the process has not yet terminated
     * @see Process#isAlive()
     */
    boolean isAlive() {
        return process.isAlive();
    }

    /**
     * Get the exit value of the process.
     * 
     * @return exit value
     * @see Process#exitValue()
     */
    int exitValue() {
        return process.exitValue();
    }

    /**
     * Kill the process.
     * 
     * @See Process#destroy()
     */
    void destroy() {
        process.destroy();
    }

    /**
     * Kill the process forcibly.
     * 
     * @see Process#destroyForcibly()
     */
    public void destroyForcibly() {
        process.destroyForcibly();
    }
}
