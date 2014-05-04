package com.mass.concurrent.sync.springaop.config;

import static com.google.common.base.Preconditions.checkArgument;
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

    public SynchronizerConfiguration(final SynchronizerScope scope, final String zkMutexBasePath) {
        checkArgument(scope != null, "Undefined synchronizer scope.");
        checkNotBlank(zkMutexBasePath, "Undefined zookeeper mutex base path.");
        this.scope = scope;
        this.zkMutexBasePath = zkMutexBasePath;
    }

    public SynchronizerScope getScope() {
        return scope;
    }

    public String getZkMutexBasePath() {
        return zkMutexBasePath;
    }
}
