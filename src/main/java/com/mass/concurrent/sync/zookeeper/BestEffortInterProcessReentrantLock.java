package com.mass.concurrent.sync.zookeeper;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.joda.time.DateTimeUtils;

/**
 * This is an adapter that provides a plain java concurrent ReentrantLock interface for an underlying Curator
 * InterProcessMutex, so that you can swap out intra-process locks with inter-process locks, without changing the client
 * code. This makes a best-effort attempt to acquire the interprocess lock, but if it can't, then it won't throw an
 * exception. This lock guarantees the in-process ReentrantLock contract, and makes a best-effort attempt at the
 * interprocess mutex contract. This will quietly send exceptions with interprocess locking to an observer. If the
 * observer throws an exception, then it will cause this lock to throw an exception.
 * 
 * @author kmassaroni
 */
class BestEffortInterProcessReentrantLock extends ReentrantLock {
    private static final long serialVersionUID = -7639919128834742605L;

    private final InterProcessMutex mutex;
    private final InterProcessLockFailObserver failObserver;

    public BestEffortInterProcessReentrantLock(final InterProcessMutex mutex,
            final InterProcessLockFailObserver failObserver) {
        super();
        this.mutex = mutex;
        this.failObserver = failObserver;
    }

    public interface InterProcessLockFailObserver {
        public void onInterProcessLockFail(Throwable failure);
    }

    private void onInterProcessLockFail(final Throwable failure) {
        if (failObserver != null) {
            failObserver.onInterProcessLockFail(failure);
        }
    }

    @Override
    public void lock() {
        super.lock();

        if (mutex == null) {
            return;
        }

        try {
            mutex.acquire();
        } catch (final Exception t) {
            onInterProcessLockFail(new RuntimeException("Can't get interprocess lock.", t));
        }
    }

    @Override
    public void unlock() {
        super.unlock();

        if (mutex == null) {
            return;
        }

        try {
            mutex.release();
        } catch (final Exception e) {
            onInterProcessLockFail(new RuntimeException("Can't release interprocess lock.", e));
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

        if (mutex == null) {
            return true;
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
            onInterProcessLockFail(new RuntimeException("Can't get interprocess lock.", t));
            return true;
        }
    };

    @Override
    public void lockInterruptibly() throws InterruptedException {
        super.lockInterruptibly();

        if (mutex == null) {
            return;
        }

        try {
            mutex.acquire();
        } catch (final Exception t) {
            onInterProcessLockFail(new RuntimeException("Can't get interprocess lock.", t));
        }
    }
}
