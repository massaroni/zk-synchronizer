package com.mass.concurrent.sync.springaop.config;

import org.apache.commons.lang.StringUtils;

import com.google.common.base.Preconditions;
import com.mass.concurrent.sync.zookeeper.InterProcessLockKeyFactory;
import com.mass.core.Word;

/**
 * This is a user-provided per-lock configuration bean that you need in your spring application context. The name of
 * this definition corresponds to the lock name in the @Synchronized("myLockName") annotation.
 * 
 * @author kmassaroni
 */
public class InterProcessLockDefinition {
    private final Word name;
    private final InterProcessLockKeyFactory<?> lockKeyFactory;

    /**
     * @param name
     *            - corresponds to the name in the synchronized annotation: @Synchronized("myLockName")
     * @param lockKeyFactory
     *            - converts your proprietary lock-key model into a lock key that we can use with zookeeper. this is
     *            required even if you're not using zookeeper.
     */
    public InterProcessLockDefinition(final String name, final InterProcessLockKeyFactory<?> lockKeyFactory) {
        Preconditions.checkArgument(StringUtils.isNotBlank(name), "Undefined lock name.");

        this.name = new Word(name);
        this.lockKeyFactory = lockKeyFactory;
    }

    public Word getName() {
        return name;
    }

    public InterProcessLockKeyFactory<?> getLockKeyFactory() {
        return lockKeyFactory;
    }

    @Override
    public String toString() {
        return "InterProcessLockDefinition [name=" + name + ", lockKeyFactory=" + lockKeyFactory + "]";
    }
}
