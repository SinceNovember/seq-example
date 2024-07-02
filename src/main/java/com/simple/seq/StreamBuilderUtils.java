package com.simple.seq;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * .
 *
 * @author SinceNovember
 * @date 2023/4/28
 */
public class StreamBuilderUtils {

    public static <T> Stream<T> stream(Seq<T> seq) {
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
                seq.consume(action::accept);
            }
        };
        return StreamSupport.stream(
            Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED),
            false);
    }

}
