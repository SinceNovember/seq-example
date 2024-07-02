package com.simple.seq;

import java.util.*;
import java.util.function.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface Seq<T> extends Seq0<Consumer<T>>{

    static <T> Seq<T> empty() {
        return (Seq<T>) Empty.emptySeq;
    }

    static <T> Seq<T> flat(Seq<Optional<T>> seq) {
        return c -> seq.consume(o -> o.ifPresent(c));
    }

    static <T> Seq<T> flat(Seq<T>... seq) {
        return c -> {
            for (Seq<T> s : seq) {
                s.consume(c);
            }
        };
    }

    default void consume(Consumer<T> consumer, int n, Consumer<T> substitute) {
        if (n > 0) {
            int[] a = {n - 1};
            consume(t -> {
                if (a[0] < 0) {
                    consumer.accept(t);
                } else {
                    a[0]--;
                    substitute.accept(t);
                }
            });
        } else {
            consume(consumer);
        }
    }

    default void consumerIndexed(IndexObjConsumer<T> consumer) {
        int[] a = {0};
        consume(t -> consumer.accept(a[0]++, t));
    }

    default void consumeIndexedTillStop(IndexObjConsumer<T> consumer) {
        int[] a = {0};
        consumeTillStop(t -> consumer.accept(a[0]++, t));
    }

    static <T> ItrSeq<T> flatIterable(Iterable<Optional<T>> iterable) {
        return () -> ItrUtil.flatOptional(iterable.iterator());
    }

    static <T> ItrSeq<T> flatIterable(Iterable<T>... iterables) {
        return () -> ItrUtil.flat(Arrays.asList(iterables).iterator());
    }

    static <T> ItrSeq<T> gen(Supplier<T> supplier) {
        return () -> new Iterator<T>() {
            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public T next() {
                return supplier.get();
            }
        };
    }

    static <T> Seq<T> gen(T seed, UnaryOperator<T> operator) {
        return c -> {
            T t = seed;
            c.accept(t);
            while (true) {
                c.accept(t = operator.apply(t));
            }
        };
    }

    static <T> Seq<T> gen(T seed1, T seed2, BinaryOperator<T> operator) {
        return c -> {
            T t1 = seed1, t2 = seed2;
            c.accept(t1);
            c.accept(t2);
            while (true) {
                c.accept(t2 = operator.apply(t1, t1 = t2));
            }
        };
    }

    static ItrSeq<Matcher> match(String s, Pattern pattern) {
        return () -> new Iterator<Matcher>() {
            Matcher matcher = pattern.matcher(s);

            @Override
            public boolean hasNext() {
                return matcher.find();
            }

            @Override
            public Matcher next() {
                return matcher;
            }
        };
    }

    static <T> Consumer<T> nothing() {
        return (Consumer<T>) Empty.nothing;
    }

    static <T> Seq<T> of(Iterable<T> iterable) {
        return iterable instanceof ItrSeq ? (ItrSeq<T>) iterable : (ItrSeq<T>)iterable::iterator;
    }

    static <T> Seq<T> of(Optional<T> optional) {
        return optional::ifPresent;
    }

    static <T> Seq<T> of(T... ts) {
        return of(Arrays.asList(ts));
    }

    static <T> ItrSeq<T> repeat(int n, T t) {
        return () -> new Iterator<T>() {
            int i = n;

            @Override
            public boolean hasNext() {
                return i > 0;
            }

            @Override
            public T next() {
                i--;
                return t;
            }
        };
    }

    static <T> T stop() {
        throw StopException.INSTANCE;
    }

    static <T> ItrSeq<T> tillNull(Supplier<T> supplier) {
        return () -> new PickItr<T>() {
            @Override
            public T pick() {
                T t = supplier.get();
                return t != null ? t : Seq.stop();
            }
        };
    }

    static <T> Seq<T> unit(T t) {
        return c -> c.accept(t);
    }

    default boolean all(Predicate<T> predicate) {
        return !find(predicate.negate()).isPresent();
    }

    default boolean any(Predicate<T> predicate) {
        return find(predicate).isPresent();
    }

    default boolean anyNot(Predicate<T> predicate) {
        return any(predicate.negate());
    }

    default Seq<T> append(T t) {
        return c -> {
            consume(c);
            c.accept(t);
        };
    }

    default Seq<T> append(T... t) {
        return c -> {
            consume(c);
            for (T x : t) {
                c.accept(x);
            }
        };
    }

    default Seq<T> appendAll(Iterable<T> iterable) {
        return c -> {
            consume(c);
            iterable.forEach(c);
        };
    }

    default Seq<T> appendWith(Seq<T> seq) {
        return c -> {
            consume(c);
            seq.consume(c);
        };
    }

    default void println() {
        consume(System.out::println);
    }


    default T reduce(BinaryOperator<T> binaryOperator) {
        return reduce(Transducer.of(binaryOperator));
    }


    default <E> E reduce(E des, BiConsumer<E, T> accumulator) {
        consume(t -> accumulator.accept(des, t));
        return des;
    }

    default <E> E reduce(Reducer<T, E> reducer) {
        E des = reducer.supplier().get();
        BiConsumer<E, T> accumulator = reducer.accumulator();
        consume(t -> accumulator.accept(des, t));
        Consumer<E> finisher = reducer.finisher();
        if (finisher != null) {
            finisher.accept(des);
        }
        return des;
    }

    default <E, V> E reduce(Reducer<T, V> reducer, Function<V, E> transformer) {
        return transformer.apply(reduce(reducer));
    }

    default <E, V> E reduce(Transducer<T, V, E> transducer) {
        return transducer.transformer().apply(reduce(transducer.reducer()));
    }

    default <K> SeqMap<K, ArraySeq<T>> groupBy(Function<T, K> toKey) {
        return groupBy(toKey, Reducer.toList());
    }

    default <K> SeqMap<K, T> groupBy(Function<T, K> toKey, BinaryOperator<T> operator) {
        return groupBy(toKey, Transducer.of(operator));
    }

    default <K, E> SeqMap<K, ArraySeq<E>> groupBy(Function<T, K> toKey, Function<T, E> toValue) {
        return groupBy(toKey, Reducer.mapping(toValue));
    }

    default <K, V> SeqMap<K, V> groupBy(Function<T, K> toKey, Reducer<T, V> reducer) {
        return reduce(Reducer.groupBy(toKey, reducer));
    }

    default <K, V, E> SeqMap<K, E> groupBy(Function<T, K> toKey, Transducer<T, V, E> transducer) {
        return reduce(Reducer.groupBy(toKey, transducer));
    }



    default Optional<T> find(Predicate<T> predicate) {
        Mutable<T> m = new Mutable<>(null);
        consumeTillStop(t -> {
            if (predicate.test(t)) {
                m.set(t);
            }
        });
        return m.toOptional();
    }

    interface IntObjToInt<T> {
        int apply(int acc, T t);
    }

    interface DoubleObjToDouble<T> {
        double apply(double acc, T t);
    }

    interface LongObjToLong<T> {
        long apply(long acc, T t);
    }

    interface BooleanObjToBoolean<T> {
        boolean apply(boolean acc, T t);
    }

    interface IndexObjConsumer<T> {
        void accept(int i, T t);
    }

    interface IndexObjFunction<T, E> {
        E apply(int i, T t);
    }

    interface IndexObjPredicate<T> {
        boolean test(int i, T t);
    }


    class Empty {
        static final Seq<Object> emptySeq = c -> {};

        static final Consumer<Object> nothing = t -> {};
    }
}
