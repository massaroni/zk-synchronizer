package com.mass.concurrent.sync.zookeeper;

import com.google.common.base.Preconditions;
import com.mass.concurrent.LockRegistry;
import com.mass.concurrent.sync.springaop.config.SynchronizerLockRegistryConfiguration;

/**
 * Produces lock registries scoped to this JVM, and have no external dependencies. This is not suitable for use in a
 * cluster.
 * 
 * @author kmassaroni
 */
class LocalLockRegistryFactory implements LockRegistryFactory {

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public LockRegistry<Object> newLockRegistry(final SynchronizerLockRegistryConfiguration definition) {
        Preconditions.checkArgument(definition != null, "Undefined lock registry definition.");
        return new LocalLockRegistry(definition.getLockKeyFactory(), definition.getTimeoutDuration());
    }

}
