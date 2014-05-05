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

import com.mass.concurrent.LockRegistry;
import com.mass.concurrent.sync.SynchronizerLockKey;
import com.mass.concurrent.sync.SynchronizerLockKeyFactory;
import com.mass.concurrent.sync.springaop.config.SynchronizerLockRegistryConfiguration;
import com.mass.concurrent.sync.zookeeper.LockRegistryFactory;

/**
 * This is for a one-shot disposable use case in unit tests.
 * 
 * @author kmassaroni
 */
public class SynchronizedAdviceSpy {
    private final ReentrantLock mockLock;
    private final SynchronizerAdvice adviceSpy;

    public SynchronizedAdviceSpy(final String lockName, final Object expectedLockKey) {
        final SynchronizerLockRegistryConfiguration lockDefinition = new SynchronizerLockRegistryConfiguration(
                lockName, new NoOpLockKeyFactory());

        mockLock = mock(ReentrantLock.class);
        try {
            when(mockLock.tryLock(eq(5L), eq(TimeUnit.SECONDS))).thenReturn(true);
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        }

        @SuppressWarnings("unchecked")
        final LockRegistry<Object> mockLockRegistry = mock(LockRegistry.class);
        when(mockLockRegistry.getLock(eq(expectedLockKey))).thenReturn(mockLock);

        final LockRegistryFactory mockRegistryFactory = mock(LockRegistryFactory.class);
        when(mockRegistryFactory.newLockRegistry(eq(lockDefinition))).thenReturn(mockLockRegistry);

        final SynchronizerLockRegistryConfiguration[] lockDefinitions = { lockDefinition };

        adviceSpy = Mockito.spy(new SynchronizerAdvice(lockDefinitions, mockRegistryFactory));
    }

    public SynchronizerAdvice getAdviceSpy() {
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

    private static class NoOpLockKeyFactory implements SynchronizerLockKeyFactory<Object> {
        @Override
        public SynchronizerLockKey toKey(final Object key) {
            return null;
        }
    }
}