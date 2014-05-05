package com.mass.concurrent.sync.keyfactories;

import com.google.common.base.Preconditions;
import com.mass.codec.Base64;
import com.mass.concurrent.sync.SynchronizerLockKey;
import com.mass.concurrent.sync.SynchronizerLockKeyFactory;

/**
 * Use any object as an interprocess lock. Lock keys are derived from the Base64'd toString() of the object, because not
 * all strings can be valid zookeeper paths. Null objects are not allowed.
 * 
 * @author kmassaroni
 */
public class ToStringLockKeyFactory implements SynchronizerLockKeyFactory<Object> {
    @Override
    public SynchronizerLockKey toKey(final Object key) {
        Preconditions.checkArgument(key != null, "Undefined lock key.");
        final String str = key.toString();
        final String zkSafe = Base64.encodeURLSafe(str);
        return new SynchronizerLockKey(zkSafe);
    }
}
