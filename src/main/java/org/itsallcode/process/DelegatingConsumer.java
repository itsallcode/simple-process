package org.itsallcode.process;

import java.io.IOException;
import java.util.List;

class DelegatingConsumer implements ProcessStreamConsumer {

    private final List<ProcessStreamConsumer> delegates;

    DelegatingConsumer(final List<ProcessStreamConsumer> delegates) {
        this.delegates = delegates;
    }

    @Override
    public void accept(final String line) {
        delegates.forEach(delegate -> delegate.accept(line));
    }

    @Override
    public void streamFinished() {
        delegates.forEach(ProcessStreamConsumer::streamFinished);
    }

    @Override
    public void streamReadingFailed(final IOException exception) {
        delegates.forEach(delegate -> delegate.streamReadingFailed(exception));
    }
}
