package com.mass.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;

public class HasParameterAnnotationPredicate implements Predicate<Method> {
    private final Class<? extends Annotation> annotationType;

    public HasParameterAnnotationPredicate(final Class<? extends Annotation> annotationType) {
        Preconditions.checkArgument(annotationType != null, "Undefined annotation type.");
        this.annotationType = annotationType;
    }

    @Override
    public boolean apply(final Method method) {
        if (method == null) {
            return false;
        }

        final Annotation[][] annotations = method.getParameterAnnotations();

        if (annotations == null) {
            return false;
        }

        for (final Annotation[] paramAnnotations : annotations) {
            if (hasAnnotation(paramAnnotations)) {
                return true;
            }
        }

        return false;
    }

    private boolean hasAnnotation(final Annotation[] annotations) {
        if (annotations == null) {
            return false;
        }

        for (final Annotation annotation : annotations) {
            if (annotation == null) {
                continue;
            }

            if (annotationType.equals(annotation.annotationType())) {
                return true;
            }
        }

        return false;
    }
}
