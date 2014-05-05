package com.mass.concurrent.sync.keyfactories;

import com.google.common.base.Preconditions;
import com.mass.concurrent.sync.SynchronizerLockKey;
import com.mass.concurrent.sync.SynchronizerLockKeyFactory;

/**
 * Use longs as interprocess lock keys. The key will be the human readable long, so your zookeeper paths are easy to
 * read.
 * 
 * @author kmassaroni
 */
public class LongLockKeyFactory implements SynchronizerLockKeyFactory<Long> {
    @Override
    public SynchronizerLockKey toKey(final Long key) {
        Preconditions.checkArgument(key != null, "Undefined lock key.");
        return new SynchronizerLockKey(key.toString());
    }
}
