package com.mass.concurrent.sync.springaop;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.aspectj.lang.ProceedingJoinPoint;
import org.mockito.Mockito;

import com.mass.concurrent.sync.springaop.config.InterProcessLockDefinition;
import com.mass.concurrent.sync.zookeeper.InterProcessLockKey;
import com.mass.concurrent.sync.zookeeper.InterProcessLockKeyFactory;
import com.mass.concurrent.sync.zookeeper.InterProcessLockRegistry;

/**
 * This is for a one-shot disposable use case in unit tests.
 * 
 * @author kmassaroni
 */
public class SynchronizedAdviceSpy {
    private final ReentrantLock mockLock;
    private final InterProcessSynchronizedAdvice adviceSpy;

    public SynchronizedAdviceSpy(final String lockName, final Object expectedLockKey) {
        final InterProcessLockDefinition lockDefinition = new InterProcessLockDefinition(lockName,
                new NoOpLockKeyFactory());

        mockLock = mock(ReentrantLock.class);
        try {
            when(mockLock.tryLock(eq(5L), eq(TimeUnit.SECONDS))).thenReturn(true);
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        }

        @SuppressWarnings("unchecked")
        final InterProcessLockRegistry<Object> mockLockRegistry = mock(InterProcessLockRegistry.class);
        when(mockLockRegistry.getLock(eq(expectedLockKey))).thenReturn(mockLock);

        final LockRegistryFactory mockRegistryFactory = mock(LockRegistryFactory.class);
        when(mockRegistryFactory.newLockRegistry(eq(lockDefinition))).thenReturn(mockLockRegistry);

        final InterProcessLockDefinition[] lockDefinitions = { lockDefinition };

        adviceSpy = Mockito.spy(new InterProcessSynchronizedAdvice(lockDefinitions, mockRegistryFactory));
    }

    public InterProcessSynchronizedAdvice getAdviceSpy() {
        return adviceSpy;
    }

    public void verifyAdviceWasCalled() throws Throwable {
        verify(adviceSpy, times(1)).synchronizeMethod(any(ProceedingJoinPoint.class));
        verify(mockLock, times(1)).tryLock(eq(5L), eq(TimeUnit.SECONDS));
        verify(mockLock, times(1)).unlock();
    }

    public void verifyAdviceWasNotCalled() throws Throwable {
        verify(adviceSpy, never()).synchronizeMethod(any(ProceedingJoinPoint.class));
        verify(mockLock, never()).tryLock(anyLong(), any(TimeUnit.class));
        verify(mockLock, never()).unlock();
    }

    private static class NoOpLockKeyFactory implements InterProcessLockKeyFactory<Object> {
        @Override
        public InterProcessLockKey toKey(final Object key) {
            return null;
        }
    }
}