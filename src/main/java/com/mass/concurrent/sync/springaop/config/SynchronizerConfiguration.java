package com.mass.concurrent.sync.springaop.config;

import static com.google.common.base.Preconditions.checkArgument;
import static com.mass.concurrent.sync.springaop.config.SynchronizerLockingPolicy.STRICT;
import static com.mass.core.Preconditions.checkNotBlank;

import com.mass.core.PositiveDuration;

/**
 * This is an immutable value object bundling all the global properties for Synchronizer configuration. This is supposed
 * to be supplied by the user, in the spring app context.
 * 
 * @author kmassaroni
 */
public class SynchronizerConfiguration {
    private final SynchronizerScope scope;
    private final String zkMutexBasePath;
    private final SynchronizerLockingPolicy defaultLockingPolicy;
    private final PositiveDuration globalTimeoutDuration;

    public static final PositiveDuration defaultTimeoutDuration = PositiveDuration.standardSeconds(5);

    public SynchronizerConfiguration(final SynchronizerScope scope, final String zkMutexBasePath) {
        this(scope, STRICT, zkMutexBasePath);
    }

    public SynchronizerConfiguration(final SynchronizerScope scope,
            final SynchronizerLockingPolicy defaultLockingPolicy, final String zkMutexBasePath) {
        this(scope, defaultLockingPolicy, zkMutexBasePath, defaultTimeoutDuration);
    }

    public SynchronizerConfiguration(final SynchronizerScope scope,
            final SynchronizerLockingPolicy defaultLockingPolicy, final String zkMutexBasePath,
            final PositiveDuration globalTimeoutDuration) {
        checkArgument(scope != null, "Undefined synchronizer scope.");
        checkNotBlank(zkMutexBasePath, "Undefined zookeeper mutex base path.");
        checkArgument(defaultLockingPolicy != null, "Undefined default locking policy.");
        checkArgument(globalTimeoutDuration != null, "Undefined timeout duration.");
        this.scope = scope;
        this.defaultLockingPolicy = defaultLockingPolicy;
        this.zkMutexBasePath = zkMutexBasePath;
        this.globalTimeoutDuration = globalTimeoutDuration;
    }

    public SynchronizerScope getScope() {
        return scope;
    }

    public String getZkMutexBasePath() {
        return zkMutexBasePath;
    }

    public SynchronizerLockingPolicy getDefaultLockingPolicy() {
        return defaultLockingPolicy;
    }

    public PositiveDuration getGlobalTimeoutDuration() {
        return globalTimeoutDuration;
    }

    @Override
    public String toString() {
        return "SynchronizerConfiguration [scope=" + scope + ", zkMutexBasePath=" + zkMutexBasePath
                + ", defaultLockingPolicy=" + defaultLockingPolicy + ", globalTimeoutDuration=" + globalTimeoutDuration
                + "]";
    }

}
