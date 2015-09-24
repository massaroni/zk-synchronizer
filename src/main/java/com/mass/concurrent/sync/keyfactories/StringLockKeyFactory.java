package com.mass.concurrent.sync.keyfactories;

import com.mass.codec.Base64;
import com.mass.concurrent.sync.SynchronizerLockKey;
import com.mass.concurrent.sync.SynchronizerLockKeyFactory;
import com.mass.core.Preconditions;

/**
 * Use strings as keys to interprocess locks. Lock keys are Base64'd, because not all strings can be valid zookeeper
 * paths.
 * 
 * @author kmassaroni
 */
public class StringLockKeyFactory implements SynchronizerLockKeyFactory<String> {
    @Override
    public SynchronizerLockKey toKey(final String key) {
        Preconditions.checkNotEmpty(key, "Empty interprocess lock key.");
        final String zkSafe = Base64.encodeURLSafe(key);
        return new SynchronizerLockKey(zkSafe);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return obj != null && obj.getClass().equals(getClass());
    }
}
