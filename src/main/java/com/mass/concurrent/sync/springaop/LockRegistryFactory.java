package com.mass.concurrent.sync.springaop;

import com.mass.concurrent.sync.LockRegistry;
import com.mass.concurrent.sync.springaop.config.InterProcessLockDefinition;

interface LockRegistryFactory {
    public LockRegistry<Object> newLockRegistry(final InterProcessLockDefinition definition);
}
