package com.mass.concurrent.sync.springaop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark method parameters used as interprocess lock keys.
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

}
