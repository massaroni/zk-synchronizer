package com.mass.concurrent.sync.keyfactories;

import com.google.common.base.Preconditions;
import com.mass.concurrent.sync.SynchronizerLockKey;
import com.mass.concurrent.sync.SynchronizerLockKeyFactory;
import com.mass.core.Word;

/**
 * Word values are already zookeeper-path safe, so we can use their raw value as the lock key, without base-64ing it
 * like we do in the StringLockKeyFactory.
 * 
 * @author kmassaroni
 */
public class WordLockKeyFactory implements SynchronizerLockKeyFactory<Word> {
    @Override
    public SynchronizerLockKey toKey(final Word key) {
        Preconditions.checkArgument(key != null, "Undefined lock key.");
        return new SynchronizerLockKey(key);
    }
}
