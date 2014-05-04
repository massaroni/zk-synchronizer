package com.mass.concurrent.sync.zookeeper;

import org.junit.Test;

import com.mass.concurrent.sync.zookeeper.InterProcessLockKey;

public class InterProcessLockKeyTest {

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyKey() {
        new InterProcessLockKey("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullKey() {
        new InterProcessLockKey(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWhitespaceKey() {
        new InterProcessLockKey("x x");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidKey() {
        new InterProcessLockKey("a-Z_1-0");
    }

    public void testNumberKey() {
        new InterProcessLockKey("100001839854877");
    }

    public void testNumberKeyFromLong() {
        final long number = 100001839854877L;
        final String serializedNumber = Long.toString(number);
        new InterProcessLockKey(serializedNumber);
    }

}
