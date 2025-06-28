
/**
 * Simplified usage of Java's {@link java.lang.Process} API.
 * <p>
 * Create a new process builder using
 * {@link org.itsallcode.process.SimpleProcessBuilder#create()} and start the
 * process with
 * {@link org.itsallcode.process.SimpleProcessBuilder#start()}.
 */

module simple.process {
    exports org.itsallcode.process;

    requires java.logging;
}
