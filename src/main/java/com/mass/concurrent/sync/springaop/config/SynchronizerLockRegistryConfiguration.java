package com.mass.concurrent.sync.springaop.config;

import org.apache.commons.lang.StringUtils;
import org.joda.time.Seconds;

import com.google.common.base.Preconditions;
import com.mass.concurrent.sync.SynchronizerLockKeyFactory;
import com.mass.core.PositiveDuration;
import com.mass.core.Word;

/**
 * This is a user-provided per-lock configuration bean that you need in your spring application context. The name of
 * this definition corresponds to the lock name in the @Synchronized("myLockName") annotation.
 * 
 * @author kmassaroni
 */
public class SynchronizerLockRegistryConfiguration {
    private final Word name;
    private final SynchronizerLockKeyFactory<?> lockKeyFactory;
    private final SynchronizerLockingPolicy policyOverride;
    private final PositiveDuration timeoutDuration;

    /**
     * @param name
     *            - corresponds to the name in the synchronized annotation: @Synchronized("myLockName")
     * @param lockKeyFactory
     *            - converts your proprietary lock-key model into a lock key that we can use with zookeeper. this is
     *            required even if you're not using zookeeper.
     */
    public SynchronizerLockRegistryConfiguration(final String name, final SynchronizerLockKeyFactory<?> lockKeyFactory) {
        this(name, null, lockKeyFactory);
    }

    /**
     * @param name
     *            - corresponds to the name in the synchronized annotation: @Synchronized("myLockName")
     * @param lockKeyFactory
     *            - converts your proprietary lock-key model into a lock key that we can use with zookeeper. this is
     *            required even if you're not using zookeeper.
     * @param policyOverride
     *            - (optional) overrides the default locking policy, for this lock registry.
     */
    public SynchronizerLockRegistryConfiguration(final String name, final SynchronizerLockingPolicy policyOverride,
            final SynchronizerLockKeyFactory<?> lockKeyFactory) {
        this(name, policyOverride, lockKeyFactory, (PositiveDuration) null);
    }

    public SynchronizerLockRegistryConfiguration(final String name, final SynchronizerLockingPolicy policyOverride,
            final SynchronizerLockKeyFactory<?> lockKeyFactory, final Seconds timeoutDuration) {
        this(name, policyOverride, lockKeyFactory, PositiveDuration.seconds(timeoutDuration));
    }

    /**
     * @param name
     *            - corresponds to the name in the synchronized annotation: @Synchronized("myLockName")
     * @param lockKeyFactory
     *            - converts your proprietary lock-key model into a lock key that we can use with zookeeper. this is
     *            required even if you're not using zookeeper.
     * @param policyOverride
     *            - (optional) overrides the default locking policy, for this lock registry.
     * @param timeoutDuration
     *            - (optional) (nullable) a thread will give up and throw a timeout exception if it can't get the lock
     *            in this time window.
     */
    public SynchronizerLockRegistryConfiguration(final String name, final SynchronizerLockingPolicy policyOverride,
            final SynchronizerLockKeyFactory<?> lockKeyFactory, final PositiveDuration timeoutDuration) {
        Preconditions.checkArgument(StringUtils.isNotBlank(name), "Undefined lock name.");
        Preconditions.checkArgument(lockKeyFactory != null, "Undefined lock key factory.");

        this.name = new Word(name);
        this.policyOverride = policyOverride;
        this.lockKeyFactory = lockKeyFactory;
        this.timeoutDuration = timeoutDuration;
    }

    public Word getName() {
        return name;
    }

    public SynchronizerLockKeyFactory<?> getLockKeyFactory() {
        return lockKeyFactory;
    }

    public SynchronizerLockingPolicy getLockingPolicy() {
        return policyOverride;
    }

    /**
     * Get the lock-registry level timeout duration. This overrides the global default timeout duration, defined in the
     * SynchronizerConfiguration bean.
     * 
     * @return null if this lock registry has no explicitly defined timeout duration
     */
    public PositiveDuration getTimeoutDuration() {
        return timeoutDuration;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (lockKeyFactory == null ? 0 : lockKeyFactory.hashCode());
        result = prime * result + (name == null ? 0 : name.hashCode());
        result = prime * result + (policyOverride == null ? 0 : policyOverride.hashCode());
        result = prime * result + (timeoutDuration == null ? 0 : timeoutDuration.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SynchronizerLockRegistryConfiguration other = (SynchronizerLockRegistryConfiguration) obj;
        if (lockKeyFactory == null) {
            if (other.lockKeyFactory != null) {
                return false;
            }
        } else if (!lockKeyFactory.equals(other.lockKeyFactory)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (policyOverride != other.policyOverride) {
            return false;
        }
        if (timeoutDuration == null) {
            if (other.timeoutDuration != null) {
                return false;
            }
        } else if (!timeoutDuration.equals(other.timeoutDuration)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "SynchronizerLockRegistryConfiguration [name=" + name + ", lockKeyFactory=" + lockKeyFactory
                + ", policyOverride=" + policyOverride + "]";
    }
}
