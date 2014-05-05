package com.mass.concurrent.sync.springaop;

import static java.lang.Thread.sleep;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.ImmutableList;

public abstract class SpringIntegrationTest {
    @Autowired
    private SynchronizedAlarms springAlarms;

    @Test
    public void testSpringAlarmsServiceIsProxied() {
        assertFalse(springAlarms.getClass() == SynchronizedAlarms.class);
    }

    @Test
    public void testExampleSpringAutowiring() throws Exception {
        bombard(springAlarms);
    }

    @Test(expected = ExecutionException.class)
    public void testFailsIfUnsynchronized() throws Exception {
        final SynchronizedAlarms unsynchronized = new SynchronizedAlarms();
        bombard(unsynchronized);
    }

    @Test
    public void testTheTestClasses() {
        final Alarm alarm = new Alarm();
        final AlarmObserver observer = new AlarmObserver();
        alarm.addObserver(observer);
        final AlarmObserver anotherObserver = new AlarmObserver();

        try {
            alarm.addObserver(anotherObserver);
        } catch (final AssertionError ex) {
            assertEquals("Duplicate caller id tripped the alarm.", ex.getMessage());
            return;
        }

        fail();
    }

    private static void bombard(final SynchronizedAlarms alarms) throws InterruptedException, ExecutionException {
        final ExecutorService exec = Executors.newFixedThreadPool(30);
        final List<Future<Object>> futures = exec.invokeAll(ImmutableList.of(
                // run 40 alarm callers in 30 threads
                new AlarmCaller(alarms, "a"), new AlarmCaller(alarms, "b"), new AlarmCaller(alarms, "a"),
                new AlarmCaller(alarms, "b"), new AlarmCaller(alarms, "a"), new AlarmCaller(alarms, "b"),
                new AlarmCaller(alarms, "a"), new AlarmCaller(alarms, "b"), new AlarmCaller(alarms, "a"),
                new AlarmCaller(alarms, "b"), new AlarmCaller(alarms, "a"), new AlarmCaller(alarms, "b"),
                new AlarmCaller(alarms, "a"), new AlarmCaller(alarms, "b"), new AlarmCaller(alarms, "a"),
                new AlarmCaller(alarms, "b"), new AlarmCaller(alarms, "a"), new AlarmCaller(alarms, "b"),
                new AlarmCaller(alarms, "a"), new AlarmCaller(alarms, "b"), new AlarmCaller(alarms, "a"),
                new AlarmCaller(alarms, "b"), new AlarmCaller(alarms, "a"), new AlarmCaller(alarms, "b"),
                new AlarmCaller(alarms, "a"), new AlarmCaller(alarms, "b"), new AlarmCaller(alarms, "a"),
                new AlarmCaller(alarms, "b"), new AlarmCaller(alarms, "a"), new AlarmCaller(alarms, "b"),
                new AlarmCaller(alarms, "a"), new AlarmCaller(alarms, "b"), new AlarmCaller(alarms, "a"),
                new AlarmCaller(alarms, "b"), new AlarmCaller(alarms, "a"), new AlarmCaller(alarms, "b"),
                new AlarmCaller(alarms, "a"), new AlarmCaller(alarms, "b"), new AlarmCaller(alarms, "a"),
                new AlarmCaller(alarms, "b")));

        int done = 0;
        for (final Future<Object> future : futures) {
            assertNotNull(future.get());
            done++;
        }

        assertEquals(40, done);
    }

    private static class Alarm extends Observable {
        @Override
        public synchronized void addObserver(final Observer o) {
            setChanged();
            notifyObservers();
            super.addObserver(o);
        }

        @Override
        public synchronized boolean hasChanged() {
            return true;
        }
    }

    private static class AlarmObserver implements Observer {
        @Override
        public void update(final Observable o, final Object arg) {
            fail("Duplicate caller id tripped the alarm.");
        }
    }

    public static class SynchronizedAlarms {
        private final Alarm alarmA = new Alarm();
        private final Alarm alarmB = new Alarm();

        /**
         * Throws an exception if multiple threads for the same caller id enter the critical section at the same time.
         * 
         * @param callerId
         * @throws InterruptedException
         */
        public void sleepOnAlarm(final @Synchronized("callers") String callerId) throws InterruptedException {
            final Alarm alarm = getAlarm(callerId);
            final AlarmObserver observer = new AlarmObserver();

            alarm.addObserver(observer);
            sleep(10); // give the jvm plenty of time to run other threads
            alarm.deleteObserver(observer);
        }

        private Alarm getAlarm(final String callerId) {
            if ("a".equals(callerId)) {
                return alarmA;
            } else if ("b".equals(callerId)) {
                return alarmB;
            } else {
                throw new IllegalArgumentException("Unexpected caller id: " + callerId);
            }
        }
    }

    public static class AlarmCaller implements Callable<Object> {
        private final SynchronizedAlarms alarms;
        private final String callerId;

        public AlarmCaller(final SynchronizedAlarms alarms, final String callerId) {
            this.alarms = alarms;
            this.callerId = callerId;
        }

        @Override
        public Object call() throws Exception {
            alarms.sleepOnAlarm(callerId);
            return new Object();
        }
    }
}
