package com.mass.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.mass.concurrent.sync.springaop.Synchronized;

public class HasParameterAnnotationPredicateTest {

    @Test
    public void testHasAnnotation() throws Exception {
        final HasParameterAnnotationPredicate predicate = new HasParameterAnnotationPredicate(Synchronized.class);
        assertTrue(predicate.apply(TestClass.class.getMethod("foo", String.class)));
    }

    @Test
    public void testHasNoAnnotation() throws Exception {
        final HasParameterAnnotationPredicate predicate = new HasParameterAnnotationPredicate(Synchronized.class);
        assertFalse(predicate.apply(TestClass.class.getMethod("bar", String.class)));
    }

    private static class TestClass {
        public void foo(final @Synchronized("foo") String s) {
        }

        public void bar(final String s) {
        }
    }
}
