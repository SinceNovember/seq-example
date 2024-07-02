package com.simple.seq;

import java.util.*;
import java.util.function.*;

public interface Reducer<T, V> {
    Supplier<V> supplier();
    BiConsumer<V, T> accumulator();
    Consumer<V> finisher();

    static <T> Transducer<T, ?, Double> average(ToDoubleFunction<T> function) {
        return average(function, null);
    }

    static <T> Transducer<T, ?, Double> average(ToDoubleFunction<T> function, ToDoubleFunction<T> weightFunction) {
        BiConsumer<double[], T> biConsumer;
        if (weightFunction != null) {
            biConsumer = (a, t) -> {
                double v = function.applyAsDouble(t);
                double w = weightFunction.applyAsDouble(t);
                a[0] += v * w;
                a[1] += w;
            };
        } else {
            biConsumer = (a, t) -> {
                a[0] += function.applyAsDouble(t);
                a[1] += 1;
            };
        }
        return Transducer.of(() -> new double[2], biConsumer, a -> a[1] != 0 ? a[0] / a[1] : 0);
    }

    static <T, C extends Collection<T>> Reducer<T, C> collect(Supplier<C> des) {
        return of(des, Collection::add);
    }

    static <T> Transducer<T, ?, Integer> count() {
        return Transducer.of(() -> new int[1], (a, t) -> a[0]++, a -> a[0]);
    }

    static <T> Transducer<T, ?, Integer> count(Predicate<T> predicate) {
        return Transducer.of(() -> new int[1], (a, t) -> {
            if (predicate.test(t)) {
                a[0]++;
            }
        }, a -> a[0]);
    }

    static <T> Transducer<T, ?, Integer> countNot(Predicate<T> predicate) {
        return count(predicate.negate());
    }

    static <T> Reducer<T, ArraySeq<T>> filtering(Predicate<T> predicate) {
        return filtering(predicate, toList());
    }

    static <T, V> Reducer<T, V> filtering(Predicate<T> predicate, Reducer<T, V> reducer) {
        BiConsumer<V, T> accumulator = reducer.accumulator();
        return of(reducer.supplier(), (v, t) -> {
            if (predicate.test(t)) {
                accumulator.accept(v, t);
            }
        }, reducer.finisher());
    }

    static <T, K, V> Reducer<T, SeqMap<K, V>> groupBy(Function<T, K> toKey, Reducer<T, V> reducer) {
        Supplier<V> supplier = reducer.supplier();
        BiConsumer<V, T> accumulator = reducer.accumulator();
        Consumer<V> finisher = reducer.finisher();
        return of(SeqMap::hash, (m, t) ->
                        accumulator.accept(m.computeIfAbsent(toKey.apply(t), k -> supplier.get()), t),
                finisher == null ? null : m -> m.justValues().consume(finisher));
    }

    static <T, K, V, E> Transducer<T, ?, SeqMap<K, E>> groupBy(Function<T, K> toKey, Transducer<T, V, E> transducer) {
        return Transducer.of(groupBy(toKey, transducer.reducer()), m -> m.replaceValue(transducer.transformer()));
    }

    static <T, E> Reducer<T, ArraySeq<E>> mapping(Function<T, E> mapper) {
        return mapping(mapper, toList());
    }

    static <T, E, V> Reducer<T, V> mapping(Function<T, E> mapper, Reducer<E, V> reducer) {
        BiConsumer<V, E> accumulator = reducer.accumulator();
        return of(reducer.supplier(), (v, t) -> {
            E e = mapper.apply(t);
            accumulator.accept(v, e);
        }, reducer.finisher());
    }

    static <T> Reducer<T, ArraySeq<T>> toList() {
        return of(ArraySeq::new, ArraySeq::add);
    }

    static <T> Reducer<T, ArraySeq<T>> toList(int initialCapacity) {
        return of(() -> new ArraySeq<>(initialCapacity), ArraySeq::add);
    }

    static <T, K, V> Reducer<T, SeqMap<K, V>> toMap(Function<T, K> toKey, Function<T, V> toValue) {
        return of(SeqMap::hash, (m, t) -> m.put(toKey.apply(t), toValue.apply(t)));
    }

    static <T, K, V> Reducer<T, SeqMap<K, V>> toMap(Supplier<Map<K, V>> mapSupplier, Function<T, K> toKey, Function<T, V> toValue) {
        return of(() -> SeqMap.of(mapSupplier.get()), (m, t) -> m.put(toKey.apply(t), toValue.apply(t)));
    }

    static <T, K> Reducer<T, SeqMap<K, T>> toMapBy(Function<T, K> toKey) {
        return toMapBy(LinkedHashMap::new, toKey);
    }

    static <T, K> Reducer<T, SeqMap<K, T>> toMapBy(Supplier<Map<K, T>> mapSupplier, Function<T, K> toKey) {
        return of(() -> SeqMap.of(mapSupplier.get()), (m, t) -> m.put(toKey.apply(t), t));
    }

    static <T, V> Reducer<T, SeqMap<T, V>> toMapWith(Function<T, V> toValue) {
        return toMapWith(LinkedHashMap::new, toValue);
    }

    static <T, V> Reducer<T, SeqMap<T, V>> toMapWith(Supplier<Map<T, V>> mapSupplier, Function<T, V> toValue) {
        return of(() -> SeqMap.of(mapSupplier.get()), (m, t) -> m.put(t, toValue.apply(t)));
    }

    static <T> Reducer<T, SeqSet<T>> toSet() {
        return of(LinkedSeqSet::new, Set::add);
    }

    static <T> Reducer<T, SeqSet<T>> toSet(int initialCapacity) {
        return of(() -> new LinkedSeqSet<>(initialCapacity), Set::add);
    }

    default Reducer<T, V> then(Consumer<V> action) {
        Consumer<V> finisher = finisher();
        return of(supplier(), accumulator(), finisher == null ? action : finisher.andThen(action));
    }


    static <T, V> Reducer<T, V> of(Supplier<V> supplier, BiConsumer<V, T> accumulator) {
        return of(supplier, accumulator, null);
    }

    static <T, V> Reducer<T, V> of(Supplier<V> supplier, BiConsumer<V, T> accumulator, Consumer<V> finisher) {
        return new Reducer<T, V>() {
            @Override
            public Supplier<V> supplier() {
                return supplier;
            }

            @Override
            public BiConsumer<V, T> accumulator() {
                return accumulator;
            }

            @Override
            public Consumer<V> finisher() {
                return finisher;
            }
        };
    }


}
