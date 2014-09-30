package com.mass.concurrent.sync.springaop;

import static com.mass.concurrent.sync.springaop.config.SynchronizerConfiguration.defaultTimeoutDuration;
import static com.mass.concurrent.sync.springaop.config.SynchronizerLockingPolicy.STRICT;
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

import com.google.common.base.Preconditions;
import com.mass.concurrent.LockRegistry;
import com.mass.concurrent.sync.SynchronizerLockKey;
import com.mass.concurrent.sync.SynchronizerLockKeyFactory;
import com.mass.concurrent.sync.springaop.config.SynchronizerConfiguration;
import com.mass.concurrent.sync.springaop.config.SynchronizerLockRegistryConfiguration;
import com.mass.concurrent.sync.zookeeper.LockRegistryFactory;
import com.mass.core.PositiveDuration;

/**
 * This is for a one-shot disposable use case in unit tests.
 * 
 * @author kmassaroni
 */
public class SynchronizedAdviceSpy {
    private final ReentrantLock mockLock;
    private final SynchronizerAdvice adviceSpy;
    private final PositiveDuration expectedTimeoutDuration;

    public SynchronizedAdviceSpy(final String lockName, final Object expectedLockKey) {
        this(lockName, expectedLockKey, defaultTimeoutDuration, defaultTimeoutDuration, null);
    }

    public SynchronizedAdviceSpy(final String lockName, final Object expectedLockKey,
            final PositiveDuration expectedTimeout, final PositiveDuration globalTimeout,
            final PositiveDuration registryTimeout) {
        Preconditions.checkArgument(expectedTimeout != null, "Undefined expected timeout duration.");
        Preconditions.checkArgument(globalTimeout != null, "Undefined global timeout duration.");

        expectedTimeoutDuration = expectedTimeout;

        final SynchronizerLockRegistryConfiguration lockDefinition = new SynchronizerLockRegistryConfiguration(
                lockName, STRICT, new NoOpLockKeyFactory(), registryTimeout);

        mockLock = mock(ReentrantLock.class);
        try {
            when(mockLock.tryLock(eq(expectedTimeout.getMillis()), eq(TimeUnit.MILLISECONDS))).thenReturn(true);
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        }

        @SuppressWarnings("unchecked")
        final LockRegistry<Object> mockLockRegistry = mock(LockRegistry.class);
        when(mockLockRegistry.getLock(eq(expectedLockKey))).thenReturn(mockLock);
        when(mockLockRegistry.getTimeoutDuration()).thenReturn(registryTimeout);

        final LockRegistryFactory mockRegistryFactory = mock(LockRegistryFactory.class);
        when(mockRegistryFactory.newLockRegistry(eq(lockDefinition))).thenReturn(mockLockRegistry);

        final SynchronizerLockRegistryConfiguration[] lockDefinitions = { lockDefinition };

        final SynchronizerConfiguration mockGlobalConfig = mock(SynchronizerConfiguration.class);
        when(mockGlobalConfig.getGlobalTimeoutDuration()).thenReturn(globalTimeout);

        adviceSpy = Mockito.spy(new SynchronizerAdvice(lockDefinitions, mockRegistryFactory, mockGlobalConfig));
    }

    public SynchronizerAdvice getAdviceSpy() {
        return adviceSpy;
    }

    public void verifyAdviceWasCalled() throws Throwable {
        verify(adviceSpy, times(1)).synchronizeMethod(any(ProceedingJoinPoint.class));
        verify(mockLock, times(1)).tryLock(eq(expectedTimeoutDuration.getMillis()), eq(TimeUnit.MILLISECONDS));
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