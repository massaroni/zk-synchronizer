package com.mass.core.concurrent.springaop;

import org.apache.curator.framework.CuratorFramework;

import com.google.common.base.Preconditions;
import com.mass.core.concurrent.zookeeper.InterProcessLockKeyFactory;

class InterProcessLockRegistryFactory implements LockRegistryFactory {
    private final CuratorFramework zkClient;

    public InterProcessLockRegistryFactory(final CuratorFramework zkClient) {
        Preconditions.checkArgument(zkClient != null, "Undefined zookeeper client.");
        this.zkClient = zkClient;
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public InterProcessLockRegistry<Object> newLockRegistry(final InterProcessLockDefinition definition) {
        Preconditions.checkArgument(definition != null, "Undefined interprocess lock registry definition.");
        final String zkPath = definition.getZookeeperPath();
        final InterProcessLockKeyFactory keyFactory = definition.getLockKeyFactory();
        return new InterProcessLockRegistry(zkPath, zkClient, keyFactory);
    }
}
