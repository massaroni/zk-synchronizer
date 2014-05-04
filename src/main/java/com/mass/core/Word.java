package com.mass.core;

import static com.mass.core.PatternPrecondition.WORD_PRECONDITION;
import static com.mass.core.Preconditions.checkNotBlank;

/**
 * Guarantees that its string value is a valid word. This concept of a "word" is url-safe, linux path safe, and
 * zookeeper znode name safe.
 * 
 * @author kmassaroni
 */
public final class Word {
    private final String value;

    public Word(final String value) {
        checkNotBlank(value, "Blank word value.");
        WORD_PRECONDITION.checkArgument(value, "Not a valid word: %s", value);
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * Factory method.
     * 
     * @param word
     * @return
     */
    public static Word word(final String word) {
        return new Word(word);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (value == null ? 0 : value.hashCode());
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
        final Word other = (Word) obj;
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return value;
    }
}
