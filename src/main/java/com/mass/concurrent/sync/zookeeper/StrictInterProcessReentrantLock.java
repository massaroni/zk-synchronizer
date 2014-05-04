package com.mass.concurrent.sync.zookeeper;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.joda.time.DateTimeUtils;

import com.google.common.base.Preconditions;

/**
 * This is an adapter that provides a plain java concurrent ReentrantLock interface for an underlying Curator
 * InterProcessMutex, so that you can swap out intra-process locks with inter-process locks, without changing the client
 * code. This will throw exceptions if it can't get the interprocess zookeeper lock.
 * 
 * @author kmassaroni
 */
public class StrictInterProcessReentrantLock extends ReentrantLock {
    private static final long serialVersionUID = 5812223349797413401L;

    private final InterProcessMutex mutex;

    public StrictInterProcessReentrantLock(final InterProcessMutex mutex) {
        super();
        Preconditions.checkArgument(mutex != null);
        this.mutex = mutex;
    }

    @Override
    public void lock() {
        super.lock();

        try {
            mutex.acquire();
        } catch (final Exception t) {
            super.unlock();
            throw new RuntimeException("Can't get interprocess lock.", t);
        }
    }

    @Override
    public void unlock() {
        super.unlock();

        try {
            mutex.release();
        } catch (final Exception e) {
            throw new RuntimeException("Can't release interprocess lock.", e);
        }
    }

    @Override
    public boolean tryLock() {
        try {
            return tryLock(0, TimeUnit.MILLISECONDS);
        } catch (final InterruptedException e) {
            return false;
        }
    }

    @Override
    public boolean tryLock(final long timeout, final TimeUnit unit) throws InterruptedException {
        final boolean hasTimeout = unit != null && timeout > -1;

        final long startTime = DateTimeUtils.currentTimeMillis();

        final boolean jvmLockAcquired = super.tryLock(timeout, unit);

        if (!jvmLockAcquired) {
            return false;
        }

        try {
            final boolean acquired;

            if (hasTimeout) {
                final long totalTime = unit.toMillis(timeout);
                final long now = DateTimeUtils.currentTimeMillis();
                final long elapsed = now - startTime;
                final long zkTimeout = totalTime - elapsed;
                acquired = mutex.acquire(zkTimeout, unit);
            } else {
                acquired = mutex.acquire(-1, null);
            }

            if (!acquired) {
                super.unlock();
                return false;
            }

            return true;
        } catch (final Exception t) {
            super.unlock();
            throw new RuntimeException("Can't get interprocess lock.", t);
        }
    };

    @Override
    public void lockInterruptibly() throws InterruptedException {
        super.lockInterruptibly();

        try {
            mutex.acquire();
        } catch (final Exception t) {
            super.unlock();
            throw new RuntimeException("Can't get interprocess lock.", t);
        }
    }
}
