package com.simple.seq;

/**
 * @author wolray
 */
public interface Function3<A, B, C, T> {
    T apply(A a, B b, C c);
}
