package com.mass.concurrent.sync.zookeeper;

import java.util.concurrent.locks.ReentrantLock;

import org.apache.curator.framework.CuratorFramework;

import com.mass.concurrent.sync.LockRegistry;

public abstract class GenericInterProcessLockRegistry<K> implements LockRegistry<K> {
    private final BestEffortInterProcessReentrantLockRegistry locks;

    public GenericInterProcessLockRegistry(final String rootZkPath, final CuratorFramework zkClient) {
        this.locks = new BestEffortInterProcessReentrantLockRegistry(rootZkPath, zkClient);
    }

    @Override
    public ReentrantLock getLock(final K key) {
        final InterProcessLockKey lockKey = toLockKey(key);
        return locks.getLock(lockKey);
    }

    protected abstract InterProcessLockKey toLockKey(K key);
}
