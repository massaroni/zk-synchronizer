package com.mass.concurrent.sync.springaop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * Mark method parameters used as interprocess lock keys. Timeout durations in this parameter override all other timeout
 * configurations, including the lock registry and global timeout configurations.
 * 
 * @author kmassaroni
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER })
public @interface Synchronized {

    /**
     * The name of the lock registry to use.
     */
    String value();

    /**
     * Timeout duration takes effect when it's set to a positive value.
     */
    long timeoutDuration() default -1;

    TimeUnit timeoutUnits() default TimeUnit.DAYS;
}
