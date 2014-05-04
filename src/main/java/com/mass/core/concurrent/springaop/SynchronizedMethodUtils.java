package com.mass.core.concurrent.springaop;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;

import org.apache.commons.collections.CollectionUtils;
import org.aspectj.lang.ProceedingJoinPoint;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.mass.util.MethodParameterAnnotation;
import com.mass.util.ReflectionUtils;

public final class SynchronizedMethodUtils {
    private SynchronizedMethodUtils() {
    }

    public static MethodParameterAnnotation getSynchronizedAnnotation(final ProceedingJoinPoint joinPoint) {
        final Method ifaceMethod = ReflectionUtils.getSignatureMethod(joinPoint);
        final Method targetMethod = ReflectionUtils.getTargetMethod(joinPoint);

        final MethodParameterAnnotation ifaceAnnotation = getSynchronizedAnnotation(ifaceMethod);

        if (ifaceMethod.equals(targetMethod)) {
            Preconditions.checkState(ifaceAnnotation != null, "No @Synchronized annotation on any parameter: %s",
                    ifaceMethod.toGenericString());
            return ifaceAnnotation;
        }

        final MethodParameterAnnotation targetAnnotation = getSynchronizedAnnotation(targetMethod);

        if (targetAnnotation == null) {
            Preconditions.checkState(ifaceAnnotation != null, "No @Synchronized annotation on any parameter: %s",
                    ifaceMethod.toGenericString());
            return ifaceAnnotation;
        }

        if (ifaceAnnotation == null) {
            Preconditions.checkState(targetAnnotation != null, "No @Synchronized annotation on any parameter: %s",
                    targetMethod.toGenericString());
            return targetAnnotation;
        }

        if (targetAnnotation.equals(ifaceAnnotation)) {
            return ifaceAnnotation;
        }

        final String msg = String.format(
                "Mismatching @Synchronized annotations on interface and implementation methods: %s %s",
                ifaceMethod.toGenericString(), targetMethod.toGenericString());
        throw new MismatchingSynchronizedAnnotationsException(msg);
    }

    private static MethodParameterAnnotation getSynchronizedAnnotation(final Method method) {
        final Multimap<Class<? extends Annotation>, MethodParameterAnnotation> annotations = ReflectionUtils
                .getMethodParameterAnnotations(method);
        final Collection<MethodParameterAnnotation> syncAnnotations = annotations.get(Synchronized.class);

        if (CollectionUtils.isEmpty(syncAnnotations)) {
            return null;
        }

        Preconditions.checkArgument(syncAnnotations.size() == 1,
                "Expected exactly one parameter with a @Synchronized annotation, but found %s, on method %s.",
                syncAnnotations.size(), method.toGenericString());

        return Iterables.getOnlyElement(syncAnnotations);
    }
}
