package com.mass.core;

import static com.mass.core.PatternPrecondition.WORD_PRECONDITION;

import org.junit.Test;

public class PatternPreconditionsTest {
    @Test
    public void testWordPrecondition_Digits() {
        final String arg = "100001839854877";
        WORD_PRECONDITION.checkArgument(arg);
        WORD_PRECONDITION.checkArgument(arg, "test");
        WORD_PRECONDITION.checkArgument(arg, "testing %s", 123);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWordPrecondition_1_DigitsAndWhitespace() {
        final String arg = "100001839854877 ";
        WORD_PRECONDITION.checkArgument(arg);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWordPrecondition_2_DigitsAndWhitespace() {
        final String arg = "100001839854877 ";
        WORD_PRECONDITION.checkArgument(arg, "test");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWordPrecondition_3_DigitsAndWhitespace() {
        final String arg = "100001839854877 ";
        WORD_PRECONDITION.checkArgument(arg, "testing %s", 123);
    }

    @Test
    public void testWordPrecondition_AllChars() {
        final String arg = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_1234567890-";
        WORD_PRECONDITION.checkArgument(arg);
        WORD_PRECONDITION.checkArgument(arg, "test");
        WORD_PRECONDITION.checkArgument(arg, "testing %s", 123);
    }

    @Test
    public void testWordPrecondition_FromLong() {
        WORD_PRECONDITION.checkArgument(Long.toString(100001839854877L));
        WORD_PRECONDITION.checkArgument(Long.toString(100001839854877L), "testing");
        WORD_PRECONDITION.checkArgument(Long.toString(100001839854877L), "testing %s", 123);
    }

}
