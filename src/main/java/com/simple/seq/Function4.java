package com.simple.seq;

/**
 * @author wolray
 */
public interface Function4<A, B, C, D, T> {
    T apply(A a, B b, C c, D d);
}
