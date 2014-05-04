package com.mass.core.concurrent.zookeeper;

import static com.mass.core.PatternPrecondition.WORD_PRECONDITION;

import com.google.common.base.Preconditions;

public final class InterProcessLockKey {
    private final String value;

    public InterProcessLockKey(final String value) {
        Preconditions.checkArgument(value != null, "Undefined lock key.");
        WORD_PRECONDITION.checkArgument(value, "Lock key can't contain a non-word character: '%s'", value);
        this.value = value;
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
        final InterProcessLockKey other = (InterProcessLockKey) obj;
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
