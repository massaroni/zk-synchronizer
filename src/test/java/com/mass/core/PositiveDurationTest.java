package com.mass.core;

import static org.junit.Assert.assertEquals;

import org.joda.time.Duration;
import org.junit.Test;

public class PositiveDurationTest {

    @Test
    public void testValidDuration() throws Exception {
        final PositiveDuration duration = new PositiveDuration(Duration.millis(1L));
        assertEquals(1L, duration.getDuration().getMillis());
        assertEquals(1L, duration.getMillis());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testZeroDuration() throws Exception {
        new PositiveDuration(Duration.ZERO);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeDuration() throws Exception {
        new PositiveDuration(Duration.millis(-1L));
    }

    @Test
    public void testNegativeJodaDuration() {
        final Duration d = new Duration(-1);
        assertEquals(-1L, d.getMillis());
    }

}
