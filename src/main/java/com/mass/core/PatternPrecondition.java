package com.mass.core;

import java.util.regex.Pattern;

import com.google.common.base.Preconditions;

/**
 * Guava style precondition checker for regex patterns.
 * 
 * @author kmassaroni
 */
public class PatternPrecondition {
    private final Pattern pattern;

    /**
     * This concept of a "word" is url-safe, linux path safe, and zookeeper znode name safe.
     */
    public static final PatternPrecondition WORD_PRECONDITION = new PatternPrecondition("[\\w\\-]+");

    public PatternPrecondition(final Pattern pattern) {
        Preconditions.checkArgument(pattern != null, "Undefined precondition pattern.");
        this.pattern = pattern;
    }

    public Pattern getPattern() {
        return pattern;
    }

    /**
     * @param regexPattern
     *            - java regex pattern
     */
    public PatternPrecondition(final String regexPattern) {
        pattern = Pattern.compile(regexPattern);
    }

    public void checkArgument(final String arg, final String msg) {
        Preconditions.checkArgument(pattern.matcher(arg).matches(), msg);
    }

    public void checkArgument(final String arg, final String msg, final Object... args) {
        if (!pattern.matcher(arg).matches()) {
            if (msg == null) {
                throw new IllegalArgumentException();
            }
            final String errorMsg = String.format(msg, args);
            throw new IllegalArgumentException(errorMsg);
        }
    }

    public void checkArgument(final String arg) {
        Preconditions.checkArgument(pattern.matcher(arg).matches());
    }

}
