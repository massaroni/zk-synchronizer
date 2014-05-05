package com.mass.concurrent.sync.zookeeper;

import java.util.concurrent.locks.ReentrantLock;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;

import com.mass.concurrent.sync.zookeeper.BestEffortInterProcessReentrantLock.InterProcessLockFailObserver;
import com.mass.core.Word;

/**
 * Throws an exception if it can't get a curator mutex, and the reentrant locks throw an exception if their underlying
 * curator mutexes malfunction, or if they time out waiting on their curator mutex.
 * 
 * @author kmassaroni
 */
class StrictInterProcessReentrantLockRegistry extends InterProcessReentrantLockRegistry {

    public StrictInterProcessReentrantLockRegistry(final String rootZkPath, final Word lockRegistryName,
            final CuratorFramework zkClient) {
        super(rootZkPath, lockRegistryName, zkClient);
    }

    public StrictInterProcessReentrantLockRegistry(final String rootZkPath, final Word lockRegistryName,
            final InterProcessMutexFactory mutexFactory) {
        super(rootZkPath, lockRegistryName, mutexFactory);
    }

    @Override
    protected ReentrantLock newLock(final InterProcessMutex mutex, final InterProcessLockFailObserver observer) {
        return new StrictInterProcessReentrantLock(mutex);
    }

}
