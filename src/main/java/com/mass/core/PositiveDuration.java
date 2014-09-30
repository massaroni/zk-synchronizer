package com.mass.core;

import static com.google.common.base.Preconditions.checkArgument;

import org.joda.time.Duration;
import org.joda.time.Seconds;

/**
 * Provides type safety and a guarantee that its duration component represents a positive, non-zero duration.
 * 
 * @author kmassaroni
 */
public class PositiveDuration {
    private final Duration duration;

    public PositiveDuration(final Duration duration) {
        checkArgument(duration != null, "Undefined duration.");
        checkArgument(duration.getMillis() > 0L, "Expected positive non zero duration, but was %s", duration);
        this.duration = duration;
    }

    public Duration getDuration() {
        return duration;
    }

    public long getMillis() {
        return duration.getMillis();
    }

    public static PositiveDuration seconds(final Seconds seconds) {
        checkArgument(seconds != null, "Undefined seconds duration.");
        return new PositiveDuration(seconds.toStandardDuration());
    }

    public static PositiveDuration standardSeconds(final long seconds) {
        return new PositiveDuration(Duration.standardSeconds(seconds));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (duration == null ? 0 : duration.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PositiveDuration other = (PositiveDuration) obj;
        if (duration == null) {
            if (other.duration != null) {
                return false;
            }
        } else if (!duration.equals(other.duration)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "PositiveDuration [duration=" + duration + "]";
    }

}
