package com.mass.concurrent.sync.springaop.config;

import com.google.common.base.Preconditions;

/**
 * This is an immutable value object bundling all the global properties for Synchronizer configuration. This is supposed
 * to be supplied by the user, in the spring app context.
 * 
 * @author kmassaroni
 */
public class SynchronizerConfiguration {
    private final SynchronizerScope scope;

    public SynchronizerConfiguration(final SynchronizerScope scope) {
        Preconditions.checkArgument(scope != null, "Undefined synchronizer scope.");
        this.scope = scope;
    }

    public SynchronizerScope getScope() {
        return scope;
    }
}
