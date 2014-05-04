package com.mass.core;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PreconditionsTest {
    @Test(expected = IllegalArgumentException.class)
    public void testCheckNotBlank_Empty() {
        Preconditions.checkNotBlank("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCheckNotBlank_Whitespace() {
        Preconditions.checkNotBlank(" ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCheckNotBlank_Null() {
        Preconditions.checkNotBlank(null);
    }

    public void testCheckNotBlank_Alpha() {
        Preconditions.checkNotBlank("a");
    }

    public void testCheckNotBlank_Punc() {
        Preconditions.checkNotBlank(".");
    }

    public void testCheckNotBlank_Num() {
        Preconditions.checkNotBlank("0");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCheckNotBlank_Msg_Empty() {
        try {
            Preconditions.checkNotBlank("", "testing");
        } catch (final IllegalArgumentException e) {
            assertEquals("testing", e.getMessage());
            throw e;
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCheckNotBlank_Msg_Whitespace() {
        try {
            Preconditions.checkNotBlank(" ", "testing");
        } catch (final IllegalArgumentException e) {
            assertEquals("testing", e.getMessage());
            throw e;
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCheckNotBlank_Msg_Null() {
        try {
            Preconditions.checkNotBlank(null, "testing");
        } catch (final IllegalArgumentException e) {
            assertEquals("testing", e.getMessage());
            throw e;
        }
    }

    public void testCheckNotBlank_Msg_Alpha() {
        Preconditions.checkNotBlank("a", "testing");
    }

    public void testCheckNotBlank_Msg_Punc() {
        Preconditions.checkNotBlank(".", "testing");
    }

    public void testCheckNotBlank_Msg_Num() {
        Preconditions.checkNotBlank("0", "testing");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCheckNotEmpty_Null() {
        Preconditions.checkNotEmpty(null, "error msg");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCheckNotEmpty_Empty() {
        Preconditions.checkNotEmpty("", "error msg");
    }

    public void testCheckNotEmpty_Blank() {
        Preconditions.checkNotEmpty(" ", "error msg");
    }

}
