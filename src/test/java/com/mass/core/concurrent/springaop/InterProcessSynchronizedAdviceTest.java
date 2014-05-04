package com.mass.core.concurrent.springaop;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import com.mass.core.concurrent.springaop.MismatchingSynchronizedAnnotationsException;
import com.mass.core.concurrent.springaop.Synchronized;

public class InterProcessSynchronizedAdviceTest {
    @Test
    public void testAopProxy_InterfaceTarget_Proxied() throws Throwable {
        final TestService target = new TestService();
        final AspectJProxyFactory factory = new AspectJProxyFactory(target);

        final SynchronizedAdviceSpy spy = new SynchronizedAdviceSpy("test lock registry", "abc");

        factory.addAspect(spy.getAdviceSpy());
        final TestServiceInterface proxy = factory.getProxy();

        final String actual = proxy.concat("abc", "def");

        assertEquals("abcdef", actual);

        spy.verifyAdviceWasCalled();
    }

    @Test
    public void testAopProxy_InterfaceTarget_NotProxied() throws Throwable {
        final TestService target = new TestService();
        final AspectJProxyFactory factory = new AspectJProxyFactory(target);

        final SynchronizedAdviceSpy spy = new SynchronizedAdviceSpy("test lock registry", "abc");

        factory.addAspect(spy.getAdviceSpy());
        final TestServiceInterface proxy = factory.getProxy();

        final String actual = proxy.unsynchronizedConcat("abc", "def");

        assertEquals("unsynchronized: abcdef", actual);

        spy.verifyAdviceWasNotCalled();
    }

    @Test
    public void testAopProxy_RespectInterfaceAnnotationsOnUnannotatedSubclass() throws Throwable {
        final UnAnnotatedTestService target = new UnAnnotatedTestService();
        final AspectJProxyFactory factory = new AspectJProxyFactory(target);

        final SynchronizedAdviceSpy spy = new SynchronizedAdviceSpy("test lock registry", "abc");

        factory.addAspect(spy.getAdviceSpy());
        final TestServiceInterface proxy = factory.getProxy();

        final String actual = proxy.concat("abc", "def");

        assertEquals("abcdef", actual);

        spy.verifyAdviceWasCalled();
    }

    @Test
    public void testAopProxy_RespectSubclassAnnotationOnUnannotatedInterface() throws Throwable {
        final AnnotatedTestSubclass target = new AnnotatedTestSubclass();
        final AspectJProxyFactory factory = new AspectJProxyFactory(target);

        final SynchronizedAdviceSpy spy = new SynchronizedAdviceSpy("test lock registry", "abc");

        factory.addAspect(spy.getAdviceSpy());
        final UnannotatedTestServiceInterface proxy = factory.getProxy();

        final String actual = proxy.concat("abc", "def");

        assertEquals("abcdef", actual);

        spy.verifyAdviceWasCalled();
    }

    @Test(expected = MismatchingSynchronizedAnnotationsException.class)
    public void testMismatchingInterfaceAndSubclassAnnotations_LockName() throws Throwable {
        final MismatchingTestSubclass target = new MismatchingTestSubclass();
        final AspectJProxyFactory factory = new AspectJProxyFactory(target);

        final SynchronizedAdviceSpy spy = new SynchronizedAdviceSpy("test lock registry", "abc");

        factory.addAspect(spy.getAdviceSpy());
        final MismatchingTestInterface proxy = factory.getProxy();

        proxy.concat("abc", "def");
    }

    public static interface TestServiceInterface {
        public String concat(@Synchronized("test lock registry") final String arg1, final String arg2);

        public String unsynchronizedConcat(final String arg1, final String arg2);
    }

    public static class TestService implements TestServiceInterface {
        @Override
        public String concat(@Synchronized("test lock registry") final String arg1, final String arg2) {
            return arg1 + arg2;
        }

        @Override
        public String unsynchronizedConcat(final String arg1, final String arg2) {
            return "unsynchronized: " + arg1 + arg2;
        }
    }

    public static class UnAnnotatedTestService implements TestServiceInterface {
        @Override
        public String concat(final String arg1, final String arg2) {
            return arg1 + arg2;
        }

        @Override
        public String unsynchronizedConcat(final String arg1, final String arg2) {
            return "unsynchronized: " + arg1 + arg2;
        }
    }

    public static interface UnannotatedTestServiceInterface {
        public String concat(String arg1, String arg2);
    }

    public static class AnnotatedTestSubclass implements UnannotatedTestServiceInterface {
        @Override
        public String concat(@Synchronized("test lock registry") final String arg1, final String arg2) {
            return arg1 + arg2;
        }
    }

    public static interface MismatchingTestInterface {
        public String concat(@Synchronized("mismatching") String arg1, String arg2);
    }

    public static class MismatchingTestSubclass implements MismatchingTestInterface {
        @Override
        public String concat(@Synchronized("test lock registry") final String arg1, final String arg2) {
            return arg1 + arg2;
        }
    }

}
