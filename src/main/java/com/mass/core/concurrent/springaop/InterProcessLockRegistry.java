package com.mass.core.concurrent.springaop;

import org.apache.curator.framework.CuratorFramework;

import com.google.common.base.Preconditions;
import com.mass.core.concurrent.zookeeper.GenericInterProcessLockRegistry;
import com.mass.core.concurrent.zookeeper.InterProcessLockKey;
import com.mass.core.concurrent.zookeeper.InterProcessLockKeyFactory;

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
