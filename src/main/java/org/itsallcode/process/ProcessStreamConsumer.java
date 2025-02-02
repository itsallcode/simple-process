package org.itsallcode.process;

import java.io.IOException;

interface ProcessStreamConsumer {

    void accept(String line);

    void streamFinished();

    void streamReadingFailed(IOException exception);
}
