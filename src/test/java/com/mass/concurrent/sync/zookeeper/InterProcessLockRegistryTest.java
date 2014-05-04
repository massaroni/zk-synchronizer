package com.mass.concurrent.sync.zookeeper;

import static com.mass.core.Word.word;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.mockito.Mockito;

import com.mass.concurrent.sync.zookeeper.keyfactories.IntegerLockKeyFactory;

public class InterProcessLockRegistryTest {
    @Test
    public void testExpectedZookeeperPath_NoTrailingSlash() {
        final InterProcessMutexFactory mockMutexFactory = mock(InterProcessMutexFactory.class);

        final InterProcessLockRegistry<Integer> registry = new InterProcessLockRegistry<Integer>("/zk/base/path",
                word("mylocks"), mockMutexFactory, new IntegerLockKeyFactory());

        registry.getLock(777);

        verify(mockMutexFactory, times(1)).newMutex(Mockito.eq("/zk/base/path/mylocks/777"));
    }
}
