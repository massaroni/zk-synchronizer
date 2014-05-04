package com.mass.concurrent.sync;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.ReentrantLock;

import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * This is a thread safe registry of reentrant locks. This maps a key to a lock instance. Each lock is
 * lazy-instantiated, and remains cached in this registry until it goes out of scope in all threads, so that it's
 * impossible to return two different reentrant locks for the same key, and yet this registry can automatically respond
 * to jvm memory pressure, so that you don't have to manage its contents yourself. This class has no static state, so
 * you can get two different lock instances for the same key only if you get them from two different instances of this
 * registry.
 * 
 * @author kmassaroni
 * @param <K>
 */
public class GenericLockRegistry<K> implements LockRegistry<K> {
    private final Cache<K, ReentrantLock> locks = CacheBuilder.newBuilder().softValues().build();
    private final LockFactory factory = new LockFactory();

    @Override
    public ReentrantLock getLock(final K key) {
        Preconditions.checkArgument(key != null, "Undefined key.");
        try {
            return locks.get(key, factory);
        } catch (final ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private static class LockFactory implements Callable<ReentrantLock> {
        @Override
        public ReentrantLock call() throws Exception {
            return new ReentrantLock();
        }
    }
}
