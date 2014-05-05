package com.mass.concurrent.sync.zookeeper.keyfactories;

import com.google.common.base.Preconditions;
import com.mass.concurrent.sync.zookeeper.InterProcessLockKey;
import com.mass.concurrent.sync.zookeeper.SynchronizerLockKeyFactory;

/**
 * Use integers as interprocess lock keys. The key will be the human readable integer, so your zookeeper paths are easy
 * to read.
 * 
 * @author kmassaroni
 */
public class IntegerLockKeyFactory implements SynchronizerLockKeyFactory<Integer> {
    @Override
    public InterProcessLockKey toKey(final Integer key) {
        Preconditions.checkArgument(key != null, "Undefined lock key.");
        return new InterProcessLockKey(key.toString());
    }
}
