package com.mass.concurrent.sync.zookeeper.keyfactories;

import com.google.common.base.Preconditions;
import com.mass.concurrent.sync.zookeeper.InterProcessLockKey;
import com.mass.concurrent.sync.zookeeper.SynchronizerLockKeyFactory;

/**
 * Use longs as interprocess lock keys. The key will be the human readable long, so your zookeeper paths are easy to
 * read.
 * 
 * @author kmassaroni
 */
public class LongLockKeyFactory implements SynchronizerLockKeyFactory<Long> {
    @Override
    public InterProcessLockKey toKey(final Long key) {
        Preconditions.checkArgument(key != null, "Undefined lock key.");
        return new InterProcessLockKey(key.toString());
    }
}
