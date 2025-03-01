package org.itsallcode.process;

interface StreamCollector<T> extends ProcessStreamConsumer {
    T getResult();
}
