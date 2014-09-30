package com.mass.concurrent;

import java.util.concurrent.locks.ReentrantLock;

import com.mass.core.PositiveDuration;

public interface LockRegistry<K> {
    public ReentrantLock getLock(K key);

    public PositiveDuration getTimeoutDuration();
}