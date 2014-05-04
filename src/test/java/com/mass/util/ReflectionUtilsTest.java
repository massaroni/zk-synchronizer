package com.mass.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

import org.aspectj.lang.JoinPoint.StaticPart;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.mass.lang.MethodParameterAnnotation;
import com.mass.util.ReflectionUtils;

public class ReflectionUtilsTest {
    @Test
    public void testTryGetMethodName_ProceedingJoinPoint() {
        final Signature testSig = mock(Signature.class);
        when(testSig.getDeclaringType()).thenReturn(Implementor.class);
        when(testSig.getName()).thenReturn("someServiceMethod");

        final StaticPart testStaticPart = mock(StaticPart.class);
        when(testStaticPart.getSignature()).thenReturn(testSig);

        final ProceedingJoinPoint testJoinPoint = mock(ProceedingJoinPoint.class);
        when(testJoinPoint.getStaticPart()).thenReturn(testStaticPart);
        when(testJoinPoint.getSignature()).thenReturn(testSig);

        assertTrue(ReflectionUtils.hasParameterOfType(String.class, testJoinPoint));
        assertFalse(ReflectionUtils.hasParameterOfType(Long.class, testJoinPoint));
    }

    @Test
    public void testTryGetMethodName_Method() throws Exception {
        final Method someServiceMethod = Implementor.class.getMethod("someServiceMethod", String.class);

        assertNotNull("Can't get the test fixture.", someServiceMethod);
        assertTrue(ReflectionUtils.hasParameterOfType(String.class, someServiceMethod));
        assertFalse(ReflectionUtils.hasParameterOfType(Long.class, someServiceMethod));
    }

    @Test
    public void testTryGetMethodName_Signature() {
        final Signature testSig = mock(Signature.class);
        when(testSig.getDeclaringType()).thenReturn(Implementor.class);
        when(testSig.getName()).thenReturn("someServiceMethod");

        assertTrue(ReflectionUtils.hasParameterOfType(String.class, testSig));
        assertFalse(ReflectionUtils.hasParameterOfType(Long.class, testSig));
    }

    public static class InterfaceParentClass {
        /**
         * Mock thrift service interface.
         * 
         * @author kmassaroni
         */
        public static interface Iface {
            public int someServiceMethod(final String str);
        }
    }

    public static class Implementor implements InterfaceParentClass.Iface {
        private int callCounter = 0;

        @Override
        public int someServiceMethod(final String str) {
            return ++callCounter;
        }

        public int getCallCount() {
            return callCounter;
        }
    }

    @Test
    public void testFindParameterAnnotation() throws Exception {
        final Method concatMethod = TestClass.class.getMethod("concat", String.class, String.class);
        final Annotation[][] paramAnnotations = concatMethod.getParameterAnnotations();

        final List<MethodParameterAnnotation> annotations = ReflectionUtils.toParameterAnnotations(paramAnnotations);
        final MethodParameterAnnotation annotation = Iterables.getOnlyElement(annotations);

        assertEquals(0, annotation.getParameterIndex());
        assertEquals(TestMethodParameterAnnotation.class, annotation.getAnnotation().annotationType());
    }

    @Test
    public void testFindParameterAnnotationIndex() throws Exception {
        final Method concatMethod = TestClass.class.getMethod("concat", String.class, String.class);
        final Annotation[][] paramAnnotations = concatMethod.getParameterAnnotations();
        final MethodParameterAnnotation testAnnotation = new MethodParameterAnnotation(0, paramAnnotations[0][0]);

        final Multimap<Class<? extends Annotation>, MethodParameterAnnotation> actualIndex = ReflectionUtils
                .toParameterAnnotationsIndex(ImmutableList.of(testAnnotation));

        assertEquals(1, actualIndex.size());
        final MethodParameterAnnotation actualAnnotation = Iterables.getOnlyElement(actualIndex
                .get(TestMethodParameterAnnotation.class));

        assertEquals(testAnnotation, actualAnnotation);
    }

    public static class TestClass {
        public String concat(@TestMethodParameterAnnotation final String arg1, final String arg2) {
            return arg1 + arg2;
        }
    }
}
