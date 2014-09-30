package com.mass.concurrent.sync.zookeeper;

import static com.mass.concurrent.sync.springaop.config.SynchronizerLockingPolicy.STRICT;
import static com.mass.core.Word.word;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.junit.Test;
import org.mockito.Mockito;

import com.mass.concurrent.sync.keyfactories.IntegerLockKeyFactory;
import com.mass.core.PositiveDuration;

public class InterProcessLockRegistryTest {
    @Test
    public void testExpectedZookeeperPath_NoTrailingSlash() {
        final String expectedMutexPath = "/zk/base/path/mylocks/777";

        final InterProcessMutexFactory mockMutexFactory = mock(InterProcessMutexFactory.class);
        Mockito.when(mockMutexFactory.newMutex(Mockito.eq(expectedMutexPath)))
                .thenReturn(mock(InterProcessMutex.class));

        final InterProcessLockRegistry<Integer> registry = new InterProcessLockRegistry<Integer>("/zk/base/path",
                word("mylocks"), STRICT, mockMutexFactory, new IntegerLockKeyFactory(),
                PositiveDuration.standardSeconds(5));

        registry.getLock(777);

        verify(mockMutexFactory, times(1)).newMutex(Mockito.eq(expectedMutexPath));
    }

}
