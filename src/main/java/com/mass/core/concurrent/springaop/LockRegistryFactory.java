package com.mass.core.concurrent.springaop;

import com.mass.core.concurrent.LockRegistry;

interface LockRegistryFactory {
    public LockRegistry<Object> newLockRegistry(final InterProcessLockDefinition definition);
}
