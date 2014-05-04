package com.mass.concurrent.sync.springaop.config;

/**
 * This is part of the Synchronizer configuration.
 * 
 * @author kmassaroni
 */
public enum SynchronizerScope {
    ZOOKEEPER, // for production cluster
    LOCAL_JVM // for a single-machine setp, or for testing
}
