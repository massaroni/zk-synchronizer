package com.mass.concurrent.sync.zookeeper;

import java.util.concurrent.locks.ReentrantLock;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;

import com.mass.concurrent.sync.zookeeper.BestEffortInterProcessReentrantLock.InterProcessLockFailObserver;
import com.mass.core.Word;

/**
 * This is designed so that zookeeper is not a single point of failure. If there's no zookeeper client available, then
 * this produces regular java reentrant locks. If there's a connectivity problem, or some other zookeeper configuration
 * problem, then these locks will log exceptions, but they won't throw them. These locks guarantee the java concurrent
 * reentrant lock contract, and make a best-effort attempt to provide the curator interprocess mutex contract.
 * 
 * @author kmassaroni
 */
public class BestEffortInterProcessReentrantLockRegistry extends InterProcessReentrantLockRegistry {

    public BestEffortInterProcessReentrantLockRegistry(final String rootZkPath, final Word lockRegistryName,
            final CuratorFramework zkClient) {
        super(rootZkPath, lockRegistryName, zkClient);
    }

    public BestEffortInterProcessReentrantLockRegistry(final String rootZkPath, final Word lockRegistryName,
            final InterProcessMutexFactory mutexFactory) {
        super(rootZkPath, lockRegistryName, mutexFactory);
    }

    @Override
    protected ReentrantLock newLock(final InterProcessMutex mutex, final InterProcessLockFailObserver observer) {
        return new BestEffortInterProcessReentrantLock(mutex, observer);
    }

}
