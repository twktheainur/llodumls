package io.github.twktheainur.utils;

import java.util.concurrent.atomic.AtomicInteger;

public final class ProgressCounter {
    private ProgressCounter() {
    }
    private static final AtomicInteger atomicInteger = new AtomicInteger(0);


    public static synchronized int increment(){
        return atomicInteger.incrementAndGet();
    }
}
