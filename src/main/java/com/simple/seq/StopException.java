package com.simple.seq;

/**
 * .
 *
 * @author SinceNovember
 * @date 2023/4/27
 */
public final class StopException extends RuntimeException {
    public static final StopException INSTANCE = new StopException();

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
