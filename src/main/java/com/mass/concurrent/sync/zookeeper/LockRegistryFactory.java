package com.mass.concurrent.sync.zookeeper;

import com.mass.concurrent.LockRegistry;
import com.mass.concurrent.sync.springaop.config.SynchronizerLockRegistryConfiguration;

public interface LockRegistryFactory {
    public LockRegistry<Object> newLockRegistry(final SynchronizerLockRegistryConfiguration definition);
}
