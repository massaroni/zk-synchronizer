package com.mass.concurrent.sync.zookeeper;

public interface InterProcessLockKeyFactory<K> {
    public InterProcessLockKey toKey(K key);
}
