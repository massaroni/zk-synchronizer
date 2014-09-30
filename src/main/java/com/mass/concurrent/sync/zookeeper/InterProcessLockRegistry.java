package com.mass.concurrent.sync.zookeeper;

import java.util.concurrent.locks.ReentrantLock;

import org.apache.curator.framework.CuratorFramework;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.mass.concurrent.LockRegistry;
import com.mass.concurrent.sync.SynchronizerLockKey;
import com.mass.concurrent.sync.SynchronizerLockKeyFactory;
import com.mass.concurrent.sync.springaop.config.SynchronizerLockingPolicy;
import com.mass.core.PositiveDuration;
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
    private final PositiveDuration timeoutDuration;

    public InterProcessLockRegistry(final String rootZkPath, final Word lockRegistryName,
            final SynchronizerLockingPolicy lockingPolicy, final CuratorFramework zkClient,
            final SynchronizerLockKeyFactory<K> keyFactory, final PositiveDuration timeoutDuration) {
        this(rootZkPath, lockRegistryName, lockingPolicy, zkClient == null ? null : new InterProcessMutexFactory(
                zkClient), keyFactory, timeoutDuration);
    }

    @VisibleForTesting
    InterProcessLockRegistry(final String rootZkPath, final Word lockRegistryName,
            final SynchronizerLockingPolicy lockingPolicy, final InterProcessMutexFactory mutexFactory,
            final SynchronizerLockKeyFactory<K> keyFactory, final PositiveDuration timeoutDuration) {
        Preconditions.checkArgument(mutexFactory != null);
        Preconditions.checkArgument(lockingPolicy != null, "Undefined locking policy.");
        Preconditions.checkArgument(keyFactory != null, "Undefined key factory.");

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

        this.keyFactory = keyFactory;
        this.timeoutDuration = timeoutDuration;
    }

    @Override
    public PositiveDuration getTimeoutDuration() {
        return timeoutDuration;
    }

    @Override
    public ReentrantLock getLock(final K key) {
        final SynchronizerLockKey lockKey = keyFactory.toKey(key);
        Preconditions.checkArgument(lockKey != null, "Null lock key.");
        return locks.getLock(lockKey);
    }

}
