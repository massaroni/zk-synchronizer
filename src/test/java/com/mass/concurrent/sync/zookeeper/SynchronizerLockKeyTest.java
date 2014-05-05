package com.mass.concurrent.sync.zookeeper;

import org.junit.Test;

import com.mass.concurrent.sync.SynchronizerLockKey;
import com.mass.core.Word;

public class SynchronizerLockKeyTest {

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyKey() {
        new SynchronizerLockKey("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullStringKey() {
        new SynchronizerLockKey((String) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullWordKey() {
        new SynchronizerLockKey((Word) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWhitespaceKey() {
        new SynchronizerLockKey("x x");
    }

    public void testValidKey() {
        new SynchronizerLockKey("a-Z_1-0");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMultiplePathParts() {
        new SynchronizerLockKey("a/b");
    }

    public void testNumberKey() {
        new SynchronizerLockKey("100001839854877");
    }

    public void testNumberKeyFromLong() {
        final long number = 100001839854877L;
        final String serializedNumber = Long.toString(number);
        new SynchronizerLockKey(serializedNumber);
    }

}
