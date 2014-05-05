package com.mass.concurrent.sync.zookeeper;

public interface SynchronizerLockKeyFactory<K> {
    public InterProcessLockKey toKey(K key);
}
