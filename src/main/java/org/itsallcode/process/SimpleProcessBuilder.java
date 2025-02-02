package org.itsallcode.process;

import static java.util.stream.Collectors.joining;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Builder for {@link SimpleProcess}. Create a new instance with
 * {@link SimpleProcess#builder()}.
 */
public class SimpleProcessBuilder {
    private final ProcessBuilder processBuilder;
    private Duration streamCloseTimeout = Duration.ofSeconds(1);
    private Executor executor = null;

    SimpleProcessBuilder() {
        this.processBuilder = new ProcessBuilder();
    }

    /**
     * Set program and arguments.
     * 
     * @param command program and arguments
     * @return {@code this} for fluent programming
     * @see ProcessBuilder#command(String...)
     */
    public SimpleProcessBuilder command(final String... command) {
        this.processBuilder.command(command);
        return this;
    }

    /**
     * Set program and arguments.
     * 
     * @param command program and arguments
     * @return {@code this} for fluent programming
     * @see ProcessBuilder#command(List)
     */
    public SimpleProcessBuilder command(final List<String> command) {
        this.processBuilder.command(command);
        return this;
    }

    /**
     * Set working directory.
     * 
     * @param workingDir working directory
     * @return {@code this} for fluent programming
     * @see ProcessBuilder#directory(java.io.File)
     */
    public SimpleProcessBuilder workingDir(final Path workingDir) {
        this.processBuilder.directory(workingDir.toFile());
        return this;
    }

    /**
     * Redirect the error stream to the output stream if this is {@code true}.
     * 
     * @param redirectErrorStream the new property value, default: {@code false}
     * @return {@code this} for fluent programming
     * @see ProcessBuilder#redirectErrorStream(boolean)
     */
    public SimpleProcessBuilder redirectErrorStream(final boolean redirectErrorStream) {
        this.processBuilder.redirectErrorStream(redirectErrorStream);
        return this;
    }

    /**
     * Set the timeout for closing the asynchronous stream readers.
     * 
     * @param streamCloseTimeout timeout
     * @return {@code this} for fluent programming
     */
    public SimpleProcessBuilder setStreamCloseTimeout(final Duration streamCloseTimeout) {
        this.streamCloseTimeout = streamCloseTimeout;
        return this;
    }

    /**
     * Set a custom executor for asynchronous stream readers.
     * 
     * @param executor executor
     * @return {@code this} for fluent programming
     */
    public SimpleProcessBuilder streamConsumerExecutor(final Executor executor) {
        this.executor = executor;
        return this;
    }

    /**
     * Start the new process.
     * 
     * @return a new process
     * @see ProcessBuilder#start()
     */
    public SimpleProcess<String> start() {
        final Process process = startProcess();
        final ProcessOutputConsumer<String> consumer = ProcessOutputConsumer.create(getExecutor(process), process,
                streamCloseTimeout, new StringCollector(), new StringCollector());
        consumer.start();
        return new SimpleProcess<>(process, consumer, getCommand());
    }

    private Process startProcess() {
        try {
            return processBuilder.start();
        } catch (final IOException exception) {
            throw new UncheckedIOException(
                    "Failed to start process %s in working dir %s: %s".formatted(processBuilder.command(),
                            processBuilder.directory(), exception.getMessage()),
                    exception);
        }
    }

    private Executor getExecutor(final Process process) {
        if (this.executor != null) {
            return executor;
        }
        return createThreadExecutor(process.pid());
    }

    private static Executor createThreadExecutor(final long pid) {
        return runnable -> {
            final Thread thread = new Thread(runnable);
            thread.setName("SimpleProcess-" + pid);
            thread.setUncaughtExceptionHandler(new LoggingExceptionHandler());
            thread.start();
        };
    }

    private String getCommand() {
        return processBuilder.command().stream().collect(joining(" "));
    }

    private static class LoggingExceptionHandler implements UncaughtExceptionHandler {
        private static final Logger LOG = Logger.getLogger(LoggingExceptionHandler.class.getName());

        @Override
        public void uncaughtException(final Thread thread, final Throwable exception) {
            LOG.log(Level.WARNING,
                    "Exception occurred in thread '%s': %s".formatted(thread.getName(), exception.toString()),
                    exception);
        }
    }
}
