package com.simple.test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.StringJoiner;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface Seq<T> {

    void consume(Consumer<T> consumer);

    default <E> Seq<E> map(Function<T, E> function) {
        return c -> consume(t -> {
            c.accept(function.apply(t));
        });
    }

    default <E> Seq<E> flatMap(Function<T, Seq<E>> function) {
        return c -> consume(t -> function.apply(t).consume(c));
    }

    default Seq<T> filter(Predicate<T> predicate) {
        return c -> consume(t -> {
            if (predicate.test(t)) {
                c.accept(t);
            }
        });
    }

    default Seq<T> take(int n) {
        return c -> {
            int[] i = {n};
            consumeTillStop(t -> {
                if (i[0]-- > 0) {
                    c.accept(t);
                } else {
                    stop();
                }
            });
        };
    }

    default Seq<T> drop(int n) {
        return c -> {
            int[] a = {n - 1};
            consume(t -> {
                if (a[0] < 0) {
                    c.accept(t);
                } else {
                    a[0]--;
                }
            });
        };
    }

    default Seq<T> onEach(Consumer<T> consumer) {
        return c -> consume(consumer.andThen(c));
    }

    default <T> T stop() {
        throw StopException.INSTANCE;
    }

    /**
     * 将迭代器的每个数与待消费的变量进行组合
     *
     * @param iterable iterable
     * @param function 函数
     * @return
     */
    default <E, R> Seq<R> zip(Iterable<E> iterable, BiFunction<T, E, R> function) {
        return c -> {
            Iterator<E> iterator = iterable.iterator();
            consumeTillStop(t -> {
                if (iterator.hasNext()) {
                    System.out.println("t");
                    c.accept(function.apply(t, iterator.next()));
                } else {
                    stop();
                }
            });
        };
    }

    default void consumeTillStop(Consumer<T> consumer) {
        try {
            consume(consumer);
        } catch (StopException ignore) {
        }
    }


    /********************收集方法*****************/

    default String join(String sep) {
        StringJoiner joiner = new StringJoiner(sep);
        consume(t -> joiner.add(t.toString()));
        return joiner.toString();
    }

    default List<T> toList() {
        List<T> list = new ArrayList<>();
        consume(list::add);
        return list;
    }

    default Stream<T> stream() {
        Iterator<T> iterator = new Iterator<T>() {
            @Override
            public boolean hasNext() {
                throw new NoSuchElementException();
            }

            @Override
            public T next() {
                throw new NoSuchElementException();
            }

            @Override
            public void forEachRemaining(Consumer<? super T> action) {
                consume(action::accept);
            }
        };
        return StreamSupport.stream(
            Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED),
            false);
    }
}
