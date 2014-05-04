package com.mass.concurrent.sync.springaop.config;

import static com.google.common.base.Preconditions.checkArgument;
import static com.mass.concurrent.sync.springaop.config.SynchronizerLockingPolicy.STRICT;
import static com.mass.core.Preconditions.checkNotBlank;

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

    public SynchronizerConfiguration(final SynchronizerScope scope, final String zkMutexBasePath) {
        this(scope, STRICT, zkMutexBasePath);
    }

    public SynchronizerConfiguration(final SynchronizerScope scope,
            final SynchronizerLockingPolicy defaultLockingPolicy, final String zkMutexBasePath) {
        checkArgument(scope != null, "Undefined synchronizer scope.");
        checkNotBlank(zkMutexBasePath, "Undefined zookeeper mutex base path.");
        checkArgument(defaultLockingPolicy != null, "Undefined default locking policy.");
        this.scope = scope;
        this.defaultLockingPolicy = defaultLockingPolicy;
        this.zkMutexBasePath = zkMutexBasePath;
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
}
