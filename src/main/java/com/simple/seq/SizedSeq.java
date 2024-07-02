package com.simple.seq;

import java.util.Collections;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.simple.seq.Reducer.toList;

public interface SizedSeq<T> extends ItrSeq<T> {
    int size();

    boolean isEmpty();

    default SizedSeq<T> cache() {
        return this;
    }

    default void consume(Consumer<T> consumer, int n, Consumer<T> substitute) {
        if (n > size()) {
            consume(substitute);
        } else {
            ItrSeq.super.consume(consumer, n, substitute);
        }
    }

    default int count() {
        return size();
    }

    @Override
    default ItrSeq<T> drop(int n) {
        return n >= size() ? Collections::emptyIterator : ItrSeq.super.drop(n);
    }

    @Override
    default <E> SizedSeq<E> map(Function<T, E> function) {
        return new SizedSeq<E>() {

            @Override
            public Iterator<E> iterator() {
                return ItrUtil.map(SizedSeq.this.iterator(), function);
            }
            @Override
            public int size() {
                return SizedSeq.this.size();
            }

            @Override
            public boolean isEmpty() {
                return SizedSeq.this.isEmpty();
            }

            @Override
            public SizedSeq<E> cache() {
                return toList();
            }

        }
    }
}
