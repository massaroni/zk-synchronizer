package com.mass.concurrent.sync.zookeeper;

import org.apache.curator.framework.CuratorFramework;

import com.mass.concurrent.sync.springaop.config.SynchronizerLockingPolicy;

public final class LockRegistries {
    private LockRegistries() {
    }

    public static LockRegistryFactory newLocalLockRegistryFactory() {
        return new LocalLockRegistryFactory();
    }

    public static LockRegistryFactory newInterProcessLockRegistryFactory(final CuratorFramework zkClient,
            final SynchronizerLockingPolicy defaultLockingPolicy, final String zkBasePath) {
        return new InterProcessLockRegistryFactory(zkClient, defaultLockingPolicy, zkBasePath);
    }
}
