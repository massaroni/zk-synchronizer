package com.mass.concurrent.sync.springaop;

import com.google.common.base.Preconditions;
import com.mass.concurrent.sync.LockRegistry;
import com.mass.concurrent.sync.zookeeper.LocalLockRegistry;

/**
 * Produces lock registries scoped to this JVM, and have no external dependencies. This is not suitable for use in a
 * cluster.
 * 
 * @author kmassaroni
 */
public class LocalLockRegistryFactory implements LockRegistryFactory {

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public LockRegistry<Object> newLockRegistry(final InterProcessLockDefinition definition) {
        Preconditions.checkArgument(definition != null, "Undefined lock registry definition.");
        return new LocalLockRegistry(definition.getLockKeyFactory());
    }

}
