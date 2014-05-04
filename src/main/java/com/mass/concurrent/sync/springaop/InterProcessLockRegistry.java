package com.mass.concurrent.sync.springaop;

import org.apache.curator.framework.CuratorFramework;

import com.google.common.base.Preconditions;
import com.mass.concurrent.sync.zookeeper.GenericInterProcessLockRegistry;
import com.mass.concurrent.sync.zookeeper.InterProcessLockKey;
import com.mass.concurrent.sync.zookeeper.InterProcessLockKeyFactory;

public class InterProcessLockRegistry<K> extends GenericInterProcessLockRegistry<K> {
    private final InterProcessLockKeyFactory<K> keyFactory;

    public InterProcessLockRegistry(final String rootZkPath, final CuratorFramework zkClient,
            final InterProcessLockKeyFactory<K> keyFactory) {
        super(rootZkPath, zkClient);
        Preconditions.checkArgument(keyFactory != null, "Undefined key factory.");
        this.keyFactory = keyFactory;
    }

    @Override
    protected InterProcessLockKey toLockKey(final K key) {
        return keyFactory.toKey(key);
    }

}
