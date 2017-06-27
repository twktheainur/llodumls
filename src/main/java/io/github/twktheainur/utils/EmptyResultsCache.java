package io.github.twktheainur.utils;


import redis.clients.jedis.JedisCommands;

public final class EmptyResultsCache {
    private EmptyResultsCache() {
    }
    private static final String EMPTY_RESULT_PREFIX="EMPTY_RESULT_";

    public static boolean isEmpty(final String key, final JedisCommands jedis){
        return jedis.exists(EMPTY_RESULT_PREFIX+key);
    }

    public static void markEmpty(final String key, final JedisCommands jedis){
        jedis.set(EMPTY_RESULT_PREFIX+key, "true");
    }
}
