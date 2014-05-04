package com.mass.concurrent.sync.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;

import com.google.common.base.Preconditions;

class InterProcessMutexFactory {
    private final CuratorFramework zkClient;

    public InterProcessMutexFactory(final CuratorFramework zkClient) {
        Preconditions.checkArgument(zkClient != null, "Undefined zookeeper client.");
        this.zkClient = zkClient;
    }

    public InterProcessMutex newMutex(final String zookeeperPath) {
        return new InterProcessMutex(zkClient, zookeeperPath);
    }
}
