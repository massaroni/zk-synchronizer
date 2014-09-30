package com.mass.concurrent.sync.springaop;

import static org.joda.time.Duration.standardMinutes;
import static org.junit.Assert.assertEquals;

import java.lang.annotation.Annotation;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.mass.core.PositiveDuration;

public class SynchronizedMethodUtilsTest {

    @Test
    public void testToTimeoutDuration() throws Exception {
        final Synchronized annotation = new Synchronized() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return Synchronized.class;
            }

            @Override
            public String value() {
                return "foo";
            }

            @Override
            public TimeUnit timeoutUnits() {
                return TimeUnit.MINUTES;
            }

            @Override
            public long timeoutDuration() {
                return 7;
            }
        };

        final PositiveDuration actual = SynchronizedMethodUtils.toTimeoutDuration(annotation);

        assertEquals(standardMinutes(7), actual.getDuration());
    }

}
