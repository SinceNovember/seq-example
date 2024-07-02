package com.simple.seq;

import javax.swing.text.html.Option;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.*;

public interface ItrSeq<T> extends Iterable<T>, Seq<T> {

    default ItrSeq<T> asIterable() {
        return this;
    }

    @Override
    default void consume(Consumer<T> consumer) {
        forEach(consumer);
    }

    default ItrSeq<T> drop(int n) {
        return () -> ItrUtil.drop(iterator(), n);
    }

    default ItrSeq<T> dropWhile(Predicate<T> predicate) {
        return () -> ItrUtil.dropWhile(iterator(), predicate);
    }

    default ItrSeq<T> filter(Predicate<T> predicate) {
        return predicate == null ? this : () -> ItrUtil.filter(iterator(), predicate);
    }

    default <E> ItrSeq<E> filterInstance(Class<E> cls) {
        return () -> new PickItr<E>() {
            Iterator<T> iterator = iterator();

            @Override
            public E pick() {
                while (iterator.hasNext()) {
                    T t = iterator.next();
                    if (cls.isInstance(t)) {
                        return cls.cast(t);
                    }
                }
                return Seq.stop();
            }
        };
    }

    default Optional<T> find(Predicate<T> predicate) {
        for (T t : this) {
            if (predicate.test(t)) {
                return Optional.of(t);
            }
        }
        return Optional.empty();
    }

    default T first() {
        for (T t : this) {
            return t;
        }
        return null;
    }

    default <E> ItrSeq<E> flatIterable(Function<T, Iterable<E>> function) {
        return () -> ItrUtil.flat(iterator(), function);

    }

    default <E> ItrSeq<E> flatOptional(Function<T, Optional<E>> function) {
        return () -> ItrUtil.flatOptional(ItrUtil.map(iterator(), function));
    }

    default <E> E fold(E init, BiFunction<E, T, E> function) {
        E acc = init;
        for (T t : this) {
            acc = function.apply(acc, t);
        }
        return acc;
    }

    default T last() {
        T res = null;
        for (T t : this) {
            res = t;
        }
        return res;
    }

    default <E> ItrSeq<E> map(Function<T, E> function) {
        return () -> ItrUtil.map(iterator(), function);
    }

    default <E> ItrSeq<E> map(Function<T, E> function, int n, Function<T, E> substitute) {
        return n <= 0 ? map(function) : () -> ItrUtil.map(iterator(), function, n, substitute);
    }

    default <E> ItrSeq<E> mapIndexed(IndexObjFunction<T, E> function) {
        return () -> ItrUtil.mapIndexed(iterator(), function);
    }

    default <E> ItrSeq<E> mapMaybe(Function<T, E> function) {
        return () -> new PickItr<E>() {
            final Iterator<T> iterator = iterator();

            @Override
            public E pick() {
                while (iterator.hasNext()) {
                    T t = iterator.next();
                    if (t != null) {
                        return function.apply(t);
                    }
                }
                return Seq.stop();
            }
        };
    }

    default <E> ItrSeq<E> mapNotNull(Function<T, E> function) {
        return () -> new PickItr<E>() {
            Iterator<T> iterator = iterator();

            @Override
            public E pick() {
                while (iterator.hasNext()) {
                    E e = function.apply(iterator.next());
                    if (e != null) {
                        return e;
                    }
                }
                return Seq.stop();
            }
        };
    }

    default ItrSeq<T> onEach(Consumer<T> consumer) {
        return map(t -> {
            consumer.accept(t);
            return t;
        });
    }

    default ItrSeq<T> onEach(int n, Consumer<T> consumer) {
        return map(t -> t, n, t -> {
            consumer.accept(t);
            return t;
        });
    }

    default <E> ItrSeq<E> runningFold(E init, BiFunction<E, T, E> function) {
        return () -> new MapItr<>(iterator()) {
            E acc = init;

            @Override
            public E apply(T t) {

                return acc = function.apply(acc, t);
            }
        };
    }

    default ItrSeq<T> take(int n) {
        return () -> ItrUtil.take(iterator(), n);
    }

    default <E> ItrSeq<T> takeWhile(Function<T, E> function, BiPredicate<E, E> testPrevCurr) {
        return () -> ItrUtil.takeWhile(iterator(), function, testPrevCurr);
    }

    default ItrSeq<T> takeWhile(Predicate<T> predicate) {
        return () -> ItrUtil.takeWhile(iterator(), predicate);
    }

    default ItrSeq<T> zip(T t) {
        return () -> ItrUtil.zip(iterator(), t);
    }



}
