package org.itsallcode.process;

import java.io.IOException;

class StringCollector implements StreamCollector<String> {

    private final StringBuilder builder = new StringBuilder();

    @Override
    public void accept(final String line) {
        builder.append(line).append("\n");
    }

    @Override
    public void streamFinished() {
        // Nothing to do
    }

    @Override
    public void streamReadingFailed(final IOException exception) {
        // Nothing to do
    }

    @Override
    public String getResult() {
        return builder.toString();
    }
}
