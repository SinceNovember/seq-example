package com.simple.seq;

import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public interface Transducer<T, V, E> {
    Reducer<T, V> reducer();
    Function<V, E> transformer();

    static <T, V, E> Transducer<T, V, E> of(Collector<T, V, E> collector) {
        return of(Reducer.of(collector.supplier(), collector.accumulator()), collector.finisher());
    }

    static <T> Transducer<T, ?, T> of(BinaryOperator<T> binaryOperator) {
        return of(() -> new Mutable<T>(null), (m, t) -> {
            if (m.isSet) {
                m.it = binaryOperator.apply(m.it, t);
            } else {
                m.set(t);
            }
        }, Mutable::get);
    }

    static <T, V, E> Transducer<T, V, E> of(Supplier<V> supplier, BiConsumer<V, T> accumulator, Function<V, E> transformer) {
        return of(Reducer.of(supplier, accumulator), transformer);
    }

    static <T, V, E> Transducer<T, V, E> of(Reducer<T, V> reducer, Function<V, E> transform) {
        return new Transducer<T, V, E>() {
            @Override
            public Reducer<T, V> reducer() {
                return reducer;
            }

            @Override
            public Function<V, E> transformer() {
                return transform;
            }
        };
    }
}
