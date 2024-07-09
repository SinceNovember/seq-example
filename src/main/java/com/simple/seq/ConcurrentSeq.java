package com.simple.seq;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedDeque;

public class ConcurrentSeq<T> extends ConcurrentLinkedDeque<T> implements SeqQueue<T> {
    public ConcurrentSeq() {
    }

    public ConcurrentSeq(Collection<? extends T> c) {
        super(c);
    }

}
