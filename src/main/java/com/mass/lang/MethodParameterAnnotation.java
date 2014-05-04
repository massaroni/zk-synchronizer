package com.mass.lang;

import java.lang.annotation.Annotation;

import com.google.common.base.Preconditions;

public class MethodParameterAnnotation {
    private final int parameterIndex;
    private final Annotation annotation;

    public MethodParameterAnnotation(final int parameterIndex, final Annotation annotation) {
        Preconditions.checkArgument(parameterIndex >= 0);
        Preconditions.checkArgument(annotation != null, "Undefined annotation.");

        this.parameterIndex = parameterIndex;
        this.annotation = annotation;
    }

    public int getParameterIndex() {
        return parameterIndex;
    }

    public Annotation getAnnotation() {
        return annotation;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((annotation == null) ? 0 : annotation.hashCode());
        result = prime * result + parameterIndex;
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
        final MethodParameterAnnotation other = (MethodParameterAnnotation) obj;
        if (annotation == null) {
            if (other.annotation != null) {
                return false;
            }
        } else if (!annotation.equals(other.annotation)) {
            return false;
        }
        if (parameterIndex != other.parameterIndex) {
            return false;
        }
        return true;
    }

}
