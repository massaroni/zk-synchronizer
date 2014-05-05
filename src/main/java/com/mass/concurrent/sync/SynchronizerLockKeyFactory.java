package com.mass.concurrent.sync;

public interface SynchronizerLockKeyFactory<K> {
    public SynchronizerLockKey toKey(K key);
}
