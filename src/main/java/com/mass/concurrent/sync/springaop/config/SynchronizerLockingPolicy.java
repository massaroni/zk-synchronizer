package com.mass.concurrent.sync.springaop.config;

public enum SynchronizerLockingPolicy {
    /**
     * deny access to critical section, if synchronizer can't obtain a cluster-scoped lock
     */
    STRICT,
    /**
     * fall back to a jvm-scoped lock, if synchronizer can't obtain a cluster-scoped lock
     */
    BEST_EFFORT
}
