package com.mass.concurrent.sync.springaop;

import com.mass.concurrent.sync.LockRegistry;

interface LockRegistryFactory {
    public LockRegistry<Object> newLockRegistry(final InterProcessLockDefinition definition);
}
