package com.mass.core.concurrent.zookeeper;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.junit.Test;

import com.mass.core.concurrent.zookeeper.BestEffortInterProcessReentrantLock;
import com.mass.core.concurrent.zookeeper.BestEffortInterProcessReentrantLock.InterProcessLockFailObserver;

public class BestEffortInterProcessReentrantLockTest {

    /**
     * Lock the lock, make sure it knows that it's locked, and make sure that it holds that lock against another thread
     * calling tryLock, all without a zookeeper mutex.
     */
    @Test(timeout = 1000)
    public void testLock_WithoutZk() throws Exception {
        final InterProcessLockFailObserver mockFailObserver = mock(InterProcessLockFailObserver.class);
        final BestEffortInterProcessReentrantLock lock = new BestEffortInterProcessReentrantLock(null, mockFailObserver);

        lock.lock();
        assertTrue(lock.isHeldByCurrentThread());

        final CountDownLatch latch = new CountDownLatch(1);

        final Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                assertFalse(lock.tryLock());
                latch.countDown();
            }
        });

        t.start();
        latch.await();

        verify(mockFailObserver, never()).onInterProcessLockFail(any(Throwable.class));
    }

    /**
     * Lock the lock, make sure it knows that it's locked, and make sure that it holds that lock against another thread
     * calling tryLock, with a failing zookeeper mutex.
     */
    @Test(timeout = 1000)
    public void testLock_WithBadZk() throws Exception {
        final InterProcessLockFailObserver mockFailObserver = mock(InterProcessLockFailObserver.class);
        final InterProcessMutex mockMutex = mock(InterProcessMutex.class);

        doThrow(RuntimeException.class).when(mockMutex).acquire();
        doThrow(RuntimeException.class).when(mockMutex).acquire(anyLong(), any(TimeUnit.class));
        doThrow(RuntimeException.class).when(mockMutex).release();

        final BestEffortInterProcessReentrantLock lock = new BestEffortInterProcessReentrantLock(mockMutex,
                mockFailObserver);

        lock.lock();
        assertTrue(lock.isHeldByCurrentThread());

        final CountDownLatch latch = new CountDownLatch(1);

        final Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                assertFalse(lock.tryLock());
                latch.countDown();
            }
        });

        t.start();
        latch.await();

        verify(mockMutex, times(1)).acquire();
        verify(mockMutex, times(0)).acquire(anyLong(), any(TimeUnit.class));
        verify(mockFailObserver, times(1)).onInterProcessLockFail(any(Throwable.class));
    }

    /**
     * Lock the lock, make sure it knows that it's locked, and make sure that it holds that lock against another thread
     * calling tryLock, all without a zookeeper mutex.
     */
    @Test(timeout = 1000)
    public void testTryLock_WithoutZk() throws Exception {
        final InterProcessLockFailObserver mockFailObserver = mock(InterProcessLockFailObserver.class);
        final BestEffortInterProcessReentrantLock lock = new BestEffortInterProcessReentrantLock(null, mockFailObserver);

        assertTrue(lock.tryLock(1, TimeUnit.SECONDS));
        assertTrue(lock.isHeldByCurrentThread());

        final CountDownLatch latch = new CountDownLatch(1);

        final Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                assertFalse(lock.tryLock());
                latch.countDown();
            }
        });

        t.start();
        latch.await();

        verify(mockFailObserver, never()).onInterProcessLockFail(any(Throwable.class));
    }

    /**
     * Lock the lock, make sure it knows that it's locked, and make sure that it holds that lock against another thread
     * calling tryLock, with a failing zookeeper mutex.
     */
    @Test(timeout = 1000)
    public void testTryLock_WithBadZk() throws Exception {
        final InterProcessLockFailObserver mockFailObserver = mock(InterProcessLockFailObserver.class);
        final InterProcessMutex mockMutex = mock(InterProcessMutex.class);

        doThrow(RuntimeException.class).when(mockMutex).acquire();
        doThrow(RuntimeException.class).when(mockMutex).acquire(anyLong(), any(TimeUnit.class));
        doThrow(RuntimeException.class).when(mockMutex).release();

        final BestEffortInterProcessReentrantLock lock = new BestEffortInterProcessReentrantLock(mockMutex,
                mockFailObserver);

        assertTrue(lock.tryLock(1, TimeUnit.SECONDS));
        assertTrue(lock.isHeldByCurrentThread());

        final CountDownLatch latch = new CountDownLatch(1);

        final Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                assertFalse(lock.tryLock());
                latch.countDown();
            }
        });

        t.start();
        latch.await();

        verify(mockMutex, never()).acquire();
        verify(mockMutex, times(1)).acquire(anyLong(), any(TimeUnit.class));
        verify(mockFailObserver, times(1)).onInterProcessLockFail(any(Throwable.class));
    }

    /**
     * Lock the lock, make sure it knows that it's locked, and make sure that it holds that lock against another thread
     * calling tryLock, with a failing zookeeper mutex.
     */
    @Test(timeout = 1000)
    public void testLockInterruptibly_WithBadZk() throws Exception {
        final InterProcessLockFailObserver mockFailObserver = mock(InterProcessLockFailObserver.class);
        final InterProcessMutex mockMutex = mock(InterProcessMutex.class);

        doThrow(RuntimeException.class).when(mockMutex).acquire();
        doThrow(RuntimeException.class).when(mockMutex).acquire(anyLong(), any(TimeUnit.class));
        doThrow(RuntimeException.class).when(mockMutex).release();

        final BestEffortInterProcessReentrantLock lock = new BestEffortInterProcessReentrantLock(mockMutex,
                mockFailObserver);

        lock.lockInterruptibly();
        assertTrue(lock.isHeldByCurrentThread());

        final CountDownLatch latch = new CountDownLatch(1);

        final Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                assertFalse(lock.tryLock());
                latch.countDown();
            }
        });

        t.start();
        latch.await();

        verify(mockMutex, times(1)).acquire();
        verify(mockMutex, never()).acquire(anyLong(), any(TimeUnit.class));
        verify(mockFailObserver, times(1)).onInterProcessLockFail(any(Throwable.class));
    }

    /**
     * If it can't acquire the zookeeper mutex, then it can't acquire the lock.
     * 
     * @throws Exception
     */
    @Test(timeout = 1000)
    public void testTryLock_CantAcquireZkMutex() throws Exception {
        final InterProcessLockFailObserver mockFailObserver = mock(InterProcessLockFailObserver.class);
        final InterProcessMutex mockMutex = mock(InterProcessMutex.class);

        when(mockMutex.acquire(anyLong(), any(TimeUnit.class))).thenReturn(false);

        final BestEffortInterProcessReentrantLock lock = new BestEffortInterProcessReentrantLock(mockMutex,
                mockFailObserver);

        assertFalse(lock.tryLock(50, TimeUnit.MILLISECONDS));
        assertFalse(lock.isHeldByCurrentThread());

        verify(mockMutex, never()).acquire();
        verify(mockMutex, times(1)).acquire(anyLong(), any(TimeUnit.class));
        verify(mockFailObserver, never()).onInterProcessLockFail(any(Throwable.class));
    }

}
