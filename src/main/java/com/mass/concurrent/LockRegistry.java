package com.mass.concurrent;

import java.util.concurrent.locks.ReentrantLock;

import com.mass.core.PositiveDuration;

public interface LockRegistry<K> {
    public ReentrantLock getLock(K key);

    /**
     * Get the lock-registry level timeout duration. This overrides the global default timeout duration, defined in the
     * SynchronizerConfiguration bean, and this is overridden by the annotation-level timeout duration.
     * 
     * @return null if this lock registry has no explicitly defined timeout duration
     */
    public PositiveDuration getTimeoutDuration();
}