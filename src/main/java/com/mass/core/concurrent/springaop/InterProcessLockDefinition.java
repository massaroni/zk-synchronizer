package com.mass.core.concurrent.springaop;

import org.apache.commons.lang.StringUtils;

import com.google.common.base.Preconditions;
import com.mass.core.concurrent.zookeeper.InterProcessLockKeyFactory;

public class InterProcessLockDefinition {
    private final String name;
    private final String zookeeperPath;
    private final InterProcessLockKeyFactory<?> lockKeyFactory;

    public InterProcessLockDefinition(final String name, final String zookeeperPath,
            final InterProcessLockKeyFactory<?> lockKeyFactory) {
        Preconditions.checkArgument(StringUtils.isNotBlank(name), "Undefined lock name.");
        Preconditions.checkArgument(StringUtils.isNotBlank(zookeeperPath), "Undefined lock name.");

        this.name = name;
        this.zookeeperPath = zookeeperPath;
        this.lockKeyFactory = lockKeyFactory;
    }

    public String getName() {
        return name;
    }

    public String getZookeeperPath() {
        return zookeeperPath;
    }

    public InterProcessLockKeyFactory<?> getLockKeyFactory() {
        return lockKeyFactory;
    }

    @Override
    public String toString() {
        return "InterProcessLockDefinition [name=" + name + ", zookeeperPath=" + zookeeperPath + ", lockKeyFactory="
                + lockKeyFactory + "]";
    }
}
