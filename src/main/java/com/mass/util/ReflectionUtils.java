package com.mass.util;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

import org.aspectj.lang.JoinPoint.StaticPart;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.Advised;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

public final class ReflectionUtils {
    private ReflectionUtils() {
    }

    /**
     * Get the annotation on this class, or the first ancestor class that declares it, including interfaces.
     * 
     * @param c
     * @param annotationClass
     */
    public static <A extends Annotation> A getAnnotation(final Class<?> c, final Class<A> annotationClass) {
        Preconditions.checkArgument(c != null, "Undefined annotated class.");
        Preconditions.checkArgument(annotationClass != null, "Undefined annotation class.");

        final A annotation = c.getAnnotation(annotationClass);

        if (annotation != null) {
            return annotation;
        }

        final Class<?> superclass = c.getSuperclass();
        if (superclass != null) {
            final A superAnnotation = getAnnotation(superclass, annotationClass);
            if (superAnnotation != null) {
                return superAnnotation;
            }
        }

        final Class<?>[] interfaces = c.getInterfaces();

        if (interfaces == null) {
            return null;
        }

        for (final Class<?> iface : interfaces) {
            final A ifaceAnnotation = getAnnotation(iface, annotationClass);
            if (ifaceAnnotation != null) {
                return ifaceAnnotation;
            }
        }

        return null;
    }

    /**
     * Recursively search for a class level annotation, and be aware of Spring aop proxies.
     * 
     * @param c
     * @param annotationClass
     * @return
     */
    public static <A extends Annotation> A getAnnotationOnProxy(final Object bean, final Class<A> annotationClass) {
        Preconditions.checkArgument(bean != null, "Undefined bean.");
        Preconditions.checkArgument(!Class.class.isAssignableFrom(bean.getClass()), "Expected a bean, got a class.");
        Preconditions.checkArgument(annotationClass != null, "Undefined annotation class.");

        final Class<?> beanClass = bean.getClass();
        final A annotation = getAnnotation(beanClass, annotationClass);

        if (annotation != null) {
            return annotation;
        }

        if (!Advised.class.isAssignableFrom(beanClass)) {
            return null;
        }

        final Advised proxy = Advised.class.cast(bean);
        final TargetSource source = proxy.getTargetSource();

        if (source == null) {
            return null;
        }

        Object target;
        try {
            target = source.getTarget();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        if (target != null) {
            return getAnnotationOnProxy(target, annotationClass);
        }

        return null;
    }

    public static Signature tryGetStaticSignature(final ProceedingJoinPoint joinPoint) {
        if (joinPoint == null) {
            return null;
        }

        final StaticPart stat = joinPoint.getStaticPart();
        if (stat == null) {
            return null;
        }

        return stat.getSignature();
    }

    public static String tryGetMethodName(final ProceedingJoinPoint joinPoint) {
        final Signature sig = tryGetStaticSignature(joinPoint);
        if (sig == null) {
            return null;
        }

        return sig.getName();
    }

    public static Class<?> tryGetDeclaringClass(final ProceedingJoinPoint joinPoint) {
        final Signature sig = tryGetStaticSignature(joinPoint);
        if (sig == null) {
            return null;
        }

        return sig.getDeclaringType();
    }

    public static boolean hasParameterOfType(final Class<?> paramType, final ProceedingJoinPoint joinPoint) {
        Preconditions.checkArgument(paramType != null, "Undefined parameter class.");
        Preconditions.checkArgument(joinPoint != null, "Undefined join point.");

        final Signature sig = tryGetStaticSignature(joinPoint);
        return hasParameterOfType(paramType, sig);
    }

    public static boolean hasParameterOfType(final Class<?> paramType, final Signature signature) {
        Preconditions.checkArgument(signature != null, "Undefined signature.");

        final Class<?> declaringClass = signature.getDeclaringType();
        final Method[] methods = declaringClass.getMethods();
        Preconditions.checkNotNull(methods);

        final String methodName = signature.getName();
        int methodsChecked = 0;

        for (final Method method : methods) {
            if (method.getName().equals(methodName)) {
                methodsChecked++;
                if (hasParameterOfType(paramType, method)) {
                    return true;
                }
            }
        }

        Preconditions
                .checkArgument(methodsChecked > 0, "Can't find method %s on class %s.", methodName, declaringClass);
        return false;
    }

    public static List<Method> getMethodsForSignature(final ProceedingJoinPoint joinPoint) {
        final Signature signature = tryGetStaticSignature(joinPoint);

        final int joinPointArgCount = joinPoint.getArgs().length;

        final Class<?> declaringClass = signature.getDeclaringType();
        final Method[] methods = declaringClass.getMethods();
        Preconditions.checkNotNull(methods);
        final String methodName = signature.getName();

        final List<Method> matches = Lists.newArrayList();

        for (final Method method : methods) {
            if (method.getName().equals(methodName) && method.getParameterTypes().length == joinPointArgCount) {
                matches.add(method);
            }
        }

        return matches;
    }

    public static boolean hasParameterOfType(final Class<?> targetParamClass, final Method method) {
        Preconditions.checkArgument(targetParamClass != null, "Undefined parameter class.");
        Preconditions.checkArgument(method != null, "Undefined method.");

        final Class<?>[] params = method.getParameterTypes();

        if (params == null) {
            return false;
        }

        for (final Class<?> paramClass : params) {
            if (paramClass == null) {
                continue;
            }

            if (paramClass == targetParamClass) {
                return true;
            }
        }

        return false;
    }

    /**
     * Get the interface Method for this join point, or the concrete Method itself, if it doesn't implement an
     * interface.
     * 
     * @param joinPoint
     * @return
     */
    public static Method getSignatureMethod(final ProceedingJoinPoint joinPoint) {
        final List<Method> methods = getMethodsForSignature(joinPoint);

        Preconditions.checkArgument(isNotEmpty(methods), "No methods matching join point.");
        Preconditions.checkArgument(methods.size() == 1,
                "Too many methods matching join point signature. Can't find target method.");

        final Method method = methods.get(0);
        Preconditions.checkArgument(method != null, "Missing signature method.");

        return method;
    }

    /**
     * Get the concrete Method of the proxied object.
     * 
     * @param joinPoint
     * @return
     */
    public static Method getTargetMethod(final ProceedingJoinPoint joinPoint) {
        Preconditions.checkArgument(joinPoint != null, "Undefined join point.");

        final Object proxyTarget = joinPoint.getTarget();
        Preconditions.checkArgument(proxyTarget != null, "Undefined proxy target in join point.");
        final Class<?> targetClass = proxyTarget.getClass();

        final Method ifaceMethod = getSignatureMethod(joinPoint);

        if (targetClass.equals(ifaceMethod.getDeclaringClass())) {
            return ifaceMethod;
        }

        try {
            final Method targetMethod = targetClass.getMethod(ifaceMethod.getName(), ifaceMethod.getParameterTypes());
            Preconditions.checkNotNull(targetMethod);
            return targetMethod;
        } catch (final SecurityException e) {
            throw new RuntimeException(e);
        } catch (final NoSuchMethodException e) {
            throw new RuntimeException("Can't find method for join point.", e);
        }
    }

    public static Multimap<Class<? extends Annotation>, MethodParameterAnnotation> getMethodParameterAnnotations(
            final Method method) {
        Preconditions.checkArgument(method != null, "Undefined method.");
        final List<MethodParameterAnnotation> annotations = toParameterAnnotations(method.getParameterAnnotations());
        return toParameterAnnotationsIndex(annotations);
    }

    public static List<MethodParameterAnnotation> toParameterAnnotations(final Annotation[][] annotations) {
        Preconditions.checkArgument(annotations != null, "Undefined annotations array.");

        final List<MethodParameterAnnotation> list = Lists.newArrayList();

        for (int p = 0; p < annotations.length; p++) {
            final Annotation[] paramAnnotations = annotations[p];

            if (paramAnnotations == null) {
                continue;
            }

            for (int a = 0; a < paramAnnotations.length; a++) {
                final Annotation annotation = paramAnnotations[a];

                if (annotation != null) {
                    list.add(new MethodParameterAnnotation(p, annotation));
                }
            }
        }

        return list;
    }

    public static Multimap<Class<? extends Annotation>, MethodParameterAnnotation> toParameterAnnotationsIndex(
            final Iterable<MethodParameterAnnotation> annotations) {
        return Multimaps.index(annotations, new Function<MethodParameterAnnotation, Class<? extends Annotation>>() {
            @Override
            public Class<? extends Annotation> apply(final MethodParameterAnnotation mpa) {
                return mpa.getAnnotation().annotationType();
            }
        });
    }
}
