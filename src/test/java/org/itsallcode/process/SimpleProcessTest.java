package org.itsallcode.process;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SimpleProcessTest {

    private static final Duration MIN_TIMEOUT = Duration.ofMillis(3);

    @Test
    void startingFails() {
        final SimpleProcessBuilder builder = SimpleProcess.builder().command("no-such-process");
        assertThatThrownBy(builder::start).isInstanceOf(UncheckedIOException.class).hasMessage(
                "Failed to start process [no-such-process] in working dir null: Cannot run program \"no-such-process\": error=2, No such file or directory");
    }

    @Test
    void workingDirNull() {
        final SimpleProcess<String> process = SimpleProcess.builder().command("pwd").start();
        process.waitForSuccessfulTermination();

        assertThat(process.getStdOut()).isEqualTo("%s%n".formatted(Path.of(".").toAbsolutePath().normalize()));
    }

    @Test
    void workingDirDefined(@TempDir final Path tempDir) {
        final SimpleProcess<String> process = SimpleProcess.builder().command("pwd").workingDir(tempDir).start();
        process.waitForSuccessfulTermination();

        assertThat(process.getStdOut()).isEqualTo("%s%n".formatted(tempDir));
    }

    @Test
    void processWritesToStdOut() {
        final SimpleProcess<String> process = SimpleProcess.builder().command("echo", "hello world").start();
        process.waitForSuccessfulTermination();

        assertThat(process.getStdOut()).isEqualTo("hello world\n");
        assertThat(process.getStdErr()).isEmpty();
    }

    @Test
    void processWritesMultipleLinesToStdOut() {
        final SimpleProcess<String> process = SimpleProcess.builder()
                .command("sh", "-c", "echo 'line 1' && echo 'line 2'")
                .start();
        process.waitForSuccessfulTermination();

        assertThat(process.getStdOut()).isEqualTo("line 1\nline 2\n");
    }

    @Test
    void processWritesToStdOutWithoutTrailingNewline() {
        final SimpleProcess<String> process = SimpleProcess.builder().command("echo", "-n", "hello world").start();
        process.waitForSuccessfulTermination();

        assertThat(process.getStdOut()).isEqualTo("hello world\n");
        assertThat(process.getStdErr()).isEmpty();
    }

    @Test
    void processWritesToStdErr() {
        final SimpleProcess<String> process = SimpleProcess.builder().command("sh", "-c", "echo 'hello world' >&2")
                .start();
        process.waitForTermination(MIN_TIMEOUT);

        assertThat(process.getStdOut()).isEmpty();
        assertThat(process.getStdErr()).isEqualTo("hello world\n");
    }

    @Test
    void processWritesToStdOutAndStdErr() {
        final SimpleProcess<String> process = SimpleProcess.builder().redirectErrorStream(false)
                .command("sh", "-c",
                        "echo '1: std err' >&2 && echo '2: std out' && echo '3: std err' >&2 && echo '4: std out'")
                .start();
        process.waitForTermination(MIN_TIMEOUT);

        assertThat(process.getStdOut()).isEqualTo("2: std out\n4: std out\n");
        assertThat(process.getStdErr()).isEqualTo("1: std err\n3: std err\n");
    }

    @Test
    void redirectErrorStream() {
        final SimpleProcess<String> process = SimpleProcess.builder().redirectErrorStream(true)
                .command("sh", "-c",
                        "echo '1: std err' >&2 && echo '2: std out' && echo '3: std err' >&2 && echo '4: std out'")
                .start();
        process.waitForTermination(MIN_TIMEOUT);

        assertThat(process.getStdOut()).isEqualTo("1: std err\n2: std out\n3: std err\n4: std out\n");
        assertThat(process.getStdErr()).isEmpty();
    }

    @Test
    void processExitNonZero() {
        final SimpleProcess<String> process = SimpleProcess.builder().command("false").start();
        process.waitForTermination(MIN_TIMEOUT);

        assertThat(process.exitValue()).isOne();
        assertThat(process.isAlive()).isFalse();
    }

    @Test
    void processExitZero() {
        final SimpleProcess<String> process = SimpleProcess.builder().command("true").start();
        process.waitForTermination(MIN_TIMEOUT);

        assertThat(process.exitValue()).isZero();
        assertThat(process.isAlive()).isFalse();
    }

    @Test
    void waitForTerminationWithTimeout() {
        final SimpleProcess<String> process = SimpleProcess.builder().command("echo", "hello world").start();
        process.waitForTermination(MIN_TIMEOUT);

        assertThat(process.exitValue()).isZero();
        assertThat(process.isAlive()).isFalse();
    }

    @Test
    void waitForTerminationWithoutTimeout() {
        final SimpleProcess<String> process = SimpleProcess.builder().command("echo", "hello world").start();
        assertThat(process.waitForTermination()).isZero();

        assertThat(process.exitValue()).isZero();
        assertThat(process.isAlive()).isFalse();
    }

    @Test
    void waitForTerminationDoesNotDestroyProcess() {
        final SimpleProcess<String> process = SimpleProcess.builder().command("sleep", "1").start();
        final Duration timeout = Duration.ofMillis(10);
        assertThatThrownBy(() -> process.waitForTermination(timeout))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageStartingWith("Timeout while waiting PT0.01S for process");
        assertThat(process.isAlive()).isTrue();
    }

    @Test
    void destroyTerminatesProcess() {
        final SimpleProcess<String> process = SimpleProcess.builder().command("sleep", "1").start();
        assertThat(process.isAlive()).isTrue();
        process.destroy();
        process.waitForTermination(Duration.ofMillis(10));
        assertThat(process.isAlive()).isFalse();
        assertThat(process.exitValue()).isEqualTo(143);
    }

    @Test
    void destroyTerminatesProcessWaitForTermination() {
        final SimpleProcess<String> process = SimpleProcess.builder().command("sleep", "1").start();
        assertThat(process.isAlive()).isTrue();
        process.destroy();
        assertThat(process.waitForTermination()).isEqualTo(143);
    }

    @Test
    void destroyForcibly() {
        final SimpleProcess<String> process = SimpleProcess.builder().command("sleep", "1").start();
        assertThat(process.isAlive()).isTrue();
        process.destroyForcibly();
        process.waitForTermination(Duration.ofMillis(10));
        assertThat(process.isAlive()).isFalse();
        assertThat(process.exitValue()).isEqualTo(137);
    }

    @Test
    void destroyForciblyWaitForTermination() {
        final SimpleProcess<String> process = SimpleProcess.builder().command("sleep", "1").start();
        assertThat(process.isAlive()).isTrue();
        process.destroyForcibly();
        assertThat(process.waitForTermination()).isEqualTo(137);
    }
}
