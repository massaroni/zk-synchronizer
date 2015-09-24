package com.mass.concurrent.sync.springaop;

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.lang.StringUtils.isBlank;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ObjectUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.joda.time.Duration;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.mass.core.PositiveDuration;
import com.mass.lang.MethodParameterAnnotation;
import com.mass.util.ReflectionUtils;

public final class SynchronizedMethodUtils {
    private SynchronizedMethodUtils() {
    }

    public static Object getLockKey(final Object synchronizedArgument, final Synchronized annotation) {
        Preconditions.checkArgument(synchronizedArgument != null, "Undefined lock key argument.");
        Preconditions.checkArgument(annotation != null);

        final String keyExpression = annotation.key();
        if (isBlank(keyExpression)) {
            return synchronizedArgument;
        }

        final ExpressionParser parser = new SpelExpressionParser();
        final Expression exp = parser.parseExpression(keyExpression);
        final EvaluationContext context = new StandardEvaluationContext(synchronizedArgument);

        return exp.getValue(context);
    }

    public static Annotation getMethodLevelSynchronizedAnnotation(final ProceedingJoinPoint joinPoint) {
        final Method ifaceMethod = ReflectionUtils.getSignatureMethod(joinPoint);
        final Method targetMethod = ReflectionUtils.getTargetMethod(joinPoint);

        if (targetMethod != null) {
            final Annotation annotation = getMethodLevelSynchronizedAnnotation(targetMethod);
            if (annotation != null) {
                return annotation;
            }
        }

        Preconditions.checkArgument(ifaceMethod != null, "Can't read methods in join point: %s", joinPoint);
        return getMethodLevelSynchronizedAnnotation(ifaceMethod);
    }

    public static Annotation getMethodLevelSynchronizedAnnotation(final Method method) {
        return method.getAnnotation(Synchronized.class);
    }

    public static MethodParameterAnnotation getSynchronizedAnnotation(final ProceedingJoinPoint joinPoint) {
        final Method ifaceMethod = ReflectionUtils.getSynchronizedSignatureMethod(joinPoint);
        final Method targetMethod = ReflectionUtils.getSynchronizedTargetMethod(joinPoint);

        if (ifaceMethod == null) {
            Preconditions.checkArgument(targetMethod != null,
                    "No @Synchronized annotation on interface or override method.");
            return getSynchronizedAnnotation(targetMethod);
        }

        if (targetMethod == null) {
            Preconditions.checkArgument(ifaceMethod != null,
                    "No @Synchronized annotation on interface or override method.");
            return getSynchronizedAnnotation(ifaceMethod);
        }

        final MethodParameterAnnotation ifaceAnnotation = getSynchronizedAnnotation(ifaceMethod);
        Preconditions.checkState(ifaceAnnotation != null, "Can't find @Synchronized annotation on iface method: %s",
                ifaceMethod);

        if (ifaceMethod.equals(targetMethod)) {
            return ifaceAnnotation;
        }

        final MethodParameterAnnotation targetAnnotation = getSynchronizedAnnotation(targetMethod);
        Preconditions.checkState(targetAnnotation != null, "Can't find @Synchronized annotation on target method: %s",
                targetMethod);

        if (!equivalentSynchronizedAnnotations(targetAnnotation, ifaceAnnotation)) {
            final String msg = String.format(
                    "Mismatching @Synchronized annotations on interface and implementation methods: %s %s",
                    ifaceMethod.toGenericString(), targetMethod.toGenericString());
            throw new MismatchingSynchronizedAnnotationsException(msg);
        }

        if (hasTimeoutConfig(targetAnnotation)) {
            return targetAnnotation;
        }

        return ifaceAnnotation;
    }

    private static boolean hasTimeoutConfig(final MethodParameterAnnotation annotation) {
        return hasTimeoutConfig(annotation.getAnnotation());
    }

    private static boolean hasTimeoutConfig(final Annotation annotation) {
        Preconditions.checkArgument(annotation != null, "Undefined @Synchronized annotation.");
        Preconditions.checkArgument(annotation instanceof Synchronized,
                "Expected @Synchronized annotation, but was %s", annotation);
        return hasTimeoutConfig(Synchronized.class.cast(annotation));
    }

    public static boolean hasTimeoutConfig(final Synchronized annotation) {
        return annotation.timeoutDuration() > 0;
    }

    /**
     * Ignores timeout configuration.
     * 
     * @param lhs
     * @param rhs
     * @return
     */
    private static boolean equivalentSynchronizedAnnotations(final MethodParameterAnnotation lhs,
            final MethodParameterAnnotation rhs) {
        Preconditions.checkArgument(lhs != null);
        Preconditions.checkArgument(rhs != null);

        if (lhs.getParameterIndex() != rhs.getParameterIndex()) {
            return false;
        }

        final Annotation lhsAnnotation = lhs.getAnnotation();
        final Annotation rhsAnnotation = rhs.getAnnotation();

        Preconditions.checkArgument(lhsAnnotation instanceof Synchronized,
                "Expected lhs Synchronized annotation, but was %s", lhsAnnotation);
        Preconditions.checkArgument(rhsAnnotation instanceof Synchronized,
                "Expected Synchronized annotation, but was %s", rhsAnnotation);

        final Synchronized lhsSynchronized = Synchronized.class.cast(lhsAnnotation);
        final Synchronized rhsSynchronized = Synchronized.class.cast(rhsAnnotation);

        return ObjectUtils.equals(lhsSynchronized.value(), rhsSynchronized.value());
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

    public static PositiveDuration toTimeoutDuration(final Synchronized annotation) {
        if (annotation == null) {
            return null;
        }

        final long timeout = annotation.timeoutDuration();

        if (timeout == -1) {
            return null;
        }

        checkArgument(timeout > 0, "Timeout duration out of range: %s, %s", timeout, annotation);

        final long timeoutMillis = annotation.timeoutUnits().toMillis(timeout);
        return new PositiveDuration(Duration.millis(timeoutMillis));
    }

}
