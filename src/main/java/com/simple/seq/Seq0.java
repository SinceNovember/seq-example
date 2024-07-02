package com.simple.seq;

import java.util.function.Consumer;

public interface Seq0<C> {

    void consume(C consumer);

    default void consumeTillStop(C consumer) {
        try {
            consume(consumer);
        } catch (StopException ignore) {

        }
    }
}
