package com.mass.concurrent.sync.zookeeper;

public interface SynchronizerLockKeyFactory<K> {
    public SynchronizerLockKey toKey(K key);
}
