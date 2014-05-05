package com.mass.concurrent;

import java.util.concurrent.locks.ReentrantLock;

public interface LockRegistry<K> {
    public ReentrantLock getLock(K key);
}