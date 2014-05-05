package com.mass.concurrent.sync.zookeeper;

import java.util.concurrent.locks.ReentrantLock;

import org.apache.curator.framework.CuratorFramework;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.mass.concurrent.sync.LockRegistry;
import com.mass.concurrent.sync.springaop.config.SynchronizerLockingPolicy;
import com.mass.core.Word;

/**
 * Get best-effort reentrant locks backed by curator zookeeper mutexes, for synchronizing keys across the whole cluster
 * in a production environment. Best-effort means that even if there's a zookeeper error, the reentrant lock will still
 * synchronize your key within the scope of the whole jvm, and it won't throw an exception.
 * 
 * @author kmassaroni
 * @param <K>
 */
class InterProcessLockRegistry<K> implements LockRegistry<K> {
    private final InterProcessReentrantLockRegistry locks;
    private final SynchronizerLockKeyFactory<K> keyFactory;

    public InterProcessLockRegistry(final String rootZkPath, final Word lockRegistryName,
            final SynchronizerLockingPolicy lockingPolicy, final CuratorFramework zkClient,
            final SynchronizerLockKeyFactory<K> keyFactory) {
        this(rootZkPath, lockRegistryName, lockingPolicy, zkClient == null ? null : new InterProcessMutexFactory(
                zkClient), keyFactory);
    }

    @VisibleForTesting
    InterProcessLockRegistry(final String rootZkPath, final Word lockRegistryName,
            final SynchronizerLockingPolicy lockingPolicy, final InterProcessMutexFactory mutexFactory,
            final SynchronizerLockKeyFactory<K> keyFactory) {
        Preconditions.checkArgument(mutexFactory != null);
        Preconditions.checkArgument(lockingPolicy != null, "Undefined locking policy.");

        switch (lockingPolicy) {
        case BEST_EFFORT:
            locks = new BestEffortInterProcessReentrantLockRegistry(rootZkPath, lockRegistryName, mutexFactory);
            break;
        case STRICT:
            locks = new StrictInterProcessReentrantLockRegistry(rootZkPath, lockRegistryName, mutexFactory);
            break;
        default:
            throw new IllegalArgumentException("Unexpected locking policy: " + lockingPolicy);
        }

        Preconditions.checkArgument(keyFactory != null, "Undefined key factory.");
        this.keyFactory = keyFactory;
    }

    @Override
    public ReentrantLock getLock(final K key) {
        final InterProcessLockKey lockKey = keyFactory.toKey(key);
        Preconditions.checkArgument(lockKey != null, "Null lock key.");
        return locks.getLock(lockKey);
    }

}
