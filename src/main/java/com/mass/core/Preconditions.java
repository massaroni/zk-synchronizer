package com.mass.core;

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

import java.util.NoSuchElementException;

/**
 * This has more methods you'd expect to find in the Guava Preconditions utility class.
 * 
 * @author kmassaroni
 */
public final class Preconditions {
    private Preconditions() {
    }

    public static void checkNotBlank(final String arg, final String msg) {
        if (isBlank(arg)) {
            throw new IllegalArgumentException(msg);
        }
    }

    public static void checkNotBlank(final String arg, final String msg, final Object... args) {
        com.google.common.base.Preconditions.checkArgument(isNotBlank(arg), msg, args);
    }

    public static void checkNotBlank(final String arg) {
        if (isBlank(arg)) {
            throw new IllegalArgumentException();
        }
    }

    public static void checkNotEmpty(final String arg, final Object errorMessage) {
        checkArgument(isNotEmpty(arg), errorMessage);
    }

    /**
     * Throw a NoSuchElementException, if the condition is false.
     * 
     * @param condition
     * @param msg
     * @param args
     */
    public static void checkHasElement(final boolean condition, final String msg, final Object... msgArgs) {
        if (!condition) {
            if (msg == null) {
                throw new NoSuchElementException();
            }

            throw new NoSuchElementException(String.format(msg, msgArgs));
        }
    }
}
