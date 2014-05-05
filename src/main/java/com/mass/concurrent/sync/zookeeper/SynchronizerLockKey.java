package com.mass.concurrent.sync.zookeeper;

import static com.mass.core.PatternPrecondition.WORD_PRECONDITION;

import com.google.common.base.Preconditions;
import com.mass.core.Word;

public final class SynchronizerLockKey {
    private final String value;

    public SynchronizerLockKey(final String value) {
        Preconditions.checkArgument(value != null, "Undefined lock key.");
        WORD_PRECONDITION.checkArgument(value, "Lock key can't contain a non-word character: '%s'", value);
        this.value = value;
    }

    public SynchronizerLockKey(final Word word) {
        Preconditions.checkArgument(word != null, "Undefined word.");
        value = word.getValue();
    }

    public String getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
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
        final SynchronizerLockKey other = (SynchronizerLockKey) obj;
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }

}
