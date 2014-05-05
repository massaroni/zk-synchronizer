package com.mass.concurrent.sync.zookeeper;

import static com.google.common.base.Preconditions.checkArgument;
import static com.mass.core.Preconditions.checkNotBlank;

import org.apache.curator.framework.CuratorFramework;

import com.google.common.base.Preconditions;
import com.mass.concurrent.sync.SynchronizerLockKeyFactory;
import com.mass.concurrent.sync.springaop.config.SynchronizerLockRegistryConfiguration;
import com.mass.concurrent.sync.springaop.config.SynchronizerLockingPolicy;

class InterProcessLockRegistryFactory implements LockRegistryFactory {
    private final CuratorFramework zkClient;
    private final String zkBasePath;
    private final SynchronizerLockingPolicy defaultLockingPolicy;

    public InterProcessLockRegistryFactory(final CuratorFramework zkClient,
            final SynchronizerLockingPolicy defaultLockingPolicy, final String zkBasePath) {
        checkArgument(zkClient != null, "Undefined zookeeper client.");
        checkNotBlank(zkBasePath, "Blank zookeeper mutex base path.");
        checkArgument(defaultLockingPolicy != null, "Undefined default locking policy.");

        this.zkClient = zkClient;
        this.defaultLockingPolicy = defaultLockingPolicy;
        this.zkBasePath = zkBasePath;
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public InterProcessLockRegistry<Object> newLockRegistry(final SynchronizerLockRegistryConfiguration definition) {
        Preconditions.checkArgument(definition != null, "Undefined interprocess lock registry definition.");
        final SynchronizerLockKeyFactory keyFactory = definition.getLockKeyFactory();
        final SynchronizerLockingPolicy overridePolicy = definition.getLockingPolicy();
        final SynchronizerLockingPolicy lockingPolicy = overridePolicy != null ? overridePolicy : defaultLockingPolicy;
        return new InterProcessLockRegistry(zkBasePath, definition.getName(), lockingPolicy, zkClient, keyFactory);
    }
}
