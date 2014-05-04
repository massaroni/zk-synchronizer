package com.mass.core.concurrent.zookeeper;

public interface InterProcessLockKeyFactory<K> {
    public InterProcessLockKey toKey(K key);
}
