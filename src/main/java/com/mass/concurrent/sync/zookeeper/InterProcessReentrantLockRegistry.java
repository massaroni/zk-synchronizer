package com.mass.concurrent.sync.zookeeper;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mass.concurrent.sync.zookeeper.BestEffortInterProcessReentrantLock.InterProcessLockFailObserver;
import com.mass.core.Word;

abstract class InterProcessReentrantLockRegistry {
    private final Cache<InterProcessLockKey, ReentrantLock> locks = CacheBuilder.newBuilder().softValues().build();
    private final String rootZkPath;
    private final InterProcessMutexFactory mutexFactory;

    private final Log log = LogFactory.getLog(InterProcessReentrantLockRegistry.class);
    private final InterProcessLockFailObserver observer = new InterProcessLockFailObserver() {
        @Override
        public void onInterProcessLockFail(final Throwable failure) {
            log.error("Inter process locking failed.", failure);
        }
    };

    public InterProcessReentrantLockRegistry(final String rootZkPath, final Word lockRegistryName,
            final CuratorFramework zkClient) {
        this(rootZkPath, lockRegistryName, zkClient == null ? null : new InterProcessMutexFactory(zkClient));
    }

    @VisibleForTesting
    InterProcessReentrantLockRegistry(final String rootZkPath, final Word lockRegistryName,
            final InterProcessMutexFactory mutexFactory) {
        this.rootZkPath = toZkDirPath(rootZkPath, lockRegistryName);
        this.mutexFactory = mutexFactory;
    }

    private static String toZkDirPath(final String path, final Word lockRegistryName) {
        com.mass.core.Preconditions.checkNotBlank(path, "Undefined dir path for zookeeper mutexes base dir.");

        if (path.endsWith("/")) {
            return path;
        }

        return path + '/' + lockRegistryName.getValue() + '/';
    }

    public ReentrantLock getLock(final InterProcessLockKey key) {
        Preconditions.checkArgument(key != null, "Undefined key.");
        final LockFactory factory = new LockFactory(key);

        try {
            return locks.get(key, factory);
        } catch (final ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract ReentrantLock newLock(InterProcessMutex mutex, InterProcessLockFailObserver observer);

    private class LockFactory implements Callable<ReentrantLock> {
        private final InterProcessLockKey id;

        public LockFactory(final InterProcessLockKey id) {
            this.id = id;
        }

        @Override
        public ReentrantLock call() throws Exception {
            if (mutexFactory == null) {
                return new ReentrantLock(true);
            }

            final String path = rootZkPath + id.getValue();
            final InterProcessMutex mutex = mutexFactory.newMutex(path);
            final ReentrantLock lock = newLock(mutex, observer);
            Preconditions.checkState(lock != null, "Can't build a new lock.");

            return lock;
        }
    }

}
