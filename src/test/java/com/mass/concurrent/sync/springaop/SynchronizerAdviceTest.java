package com.mass.concurrent.sync.springaop;

import static com.mass.concurrent.sync.springaop.config.SynchronizerConfiguration.defaultTimeoutDuration;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.lang.reflect.UndeclaredThrowableException;

import org.junit.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import com.mass.core.PositiveDuration;

public class SynchronizerAdviceTest {
    @Test
    public void testAopProxy_InterfaceTarget_Proxied() throws Throwable {
        final TestService target = new TestService();
        final AspectJProxyFactory factory = new AspectJProxyFactory(target);

        final SynchronizedAdviceSpy spy = new SynchronizedAdviceSpy("test-lock-registry", "abc");

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

        final SynchronizedAdviceSpy spy = new SynchronizedAdviceSpy("test-lock-registry", "abc");

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

        final SynchronizedAdviceSpy spy = new SynchronizedAdviceSpy("test-lock-registry", "abc");

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

        final SynchronizedAdviceSpy spy = new SynchronizedAdviceSpy("test-lock-registry", "abc");

        factory.addAspect(spy.getAdviceSpy());
        final UnannotatedTestServiceInterface proxy = factory.getProxy();

        final String actual = proxy.concat("abc", "def");

        assertEquals("abcdef", actual);

        spy.verifyAdviceWasCalled();
    }

    @Test
    public void testAopProxy_UseRegistryTimeoutDuration() throws Throwable {
        final AnnotatedTestSubclass target = new AnnotatedTestSubclass();
        final AspectJProxyFactory factory = new AspectJProxyFactory(target);

        final PositiveDuration timeout1 = PositiveDuration.standardSeconds(1);
        final PositiveDuration timeout4 = PositiveDuration.standardSeconds(4);
        assertNotEquals(timeout1, timeout4);

        final SynchronizedAdviceSpy spy = new SynchronizedAdviceSpy("test-lock-registry", "abc", timeout1, timeout4,
                timeout1);

        factory.addAspect(spy.getAdviceSpy());
        final UnannotatedTestServiceInterface proxy = factory.getProxy();

        final String actual = proxy.concat("abc", "def");

        assertEquals("abcdef", actual);

        spy.verifyAdviceWasCalled();
    }

    @Test
    public void testAopProxy_UseGlobalTimeoutDuration() throws Throwable {
        final AnnotatedTestSubclass target = new AnnotatedTestSubclass();
        final AspectJProxyFactory factory = new AspectJProxyFactory(target);

        final PositiveDuration timeout1 = PositiveDuration.standardSeconds(1);
        final PositiveDuration timeout4 = PositiveDuration.standardSeconds(4);

        assertNotEquals(timeout1, defaultTimeoutDuration);
        assertNotEquals(timeout4, defaultTimeoutDuration);
        assertNotEquals(timeout1, timeout4);

        final SynchronizedAdviceSpy spy = new SynchronizedAdviceSpy("test-lock-registry", "abc", timeout4, timeout4,
                null);

        factory.addAspect(spy.getAdviceSpy());
        final UnannotatedTestServiceInterface proxy = factory.getProxy();

        final String actual = proxy.concat("abc", "def");

        assertEquals("abcdef", actual);

        spy.verifyAdviceWasCalled();
    }

    @Test(expected = UndeclaredThrowableException.class)
    public void testAopProxy_UseRegistrtyTimeoutDuration_BreakExpectations() throws Throwable {
        final AnnotatedTestSubclass target = new AnnotatedTestSubclass();
        final AspectJProxyFactory factory = new AspectJProxyFactory(target);

        final PositiveDuration timeout1 = PositiveDuration.standardSeconds(1);
        final PositiveDuration timeout4 = PositiveDuration.standardSeconds(4);

        assertNotEquals(timeout1, defaultTimeoutDuration);
        assertNotEquals(timeout4, defaultTimeoutDuration);
        assertNotEquals(timeout1, timeout4);

        final SynchronizedAdviceSpy spy = new SynchronizedAdviceSpy("test-lock-registry", "abc", timeout4, timeout4,
                timeout1);

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

        final SynchronizedAdviceSpy spy = new SynchronizedAdviceSpy("test-lock-registry", "abc");

        factory.addAspect(spy.getAdviceSpy());
        final MismatchingTestInterface proxy = factory.getProxy();

        proxy.concat("abc", "def");
    }

    public static interface TestServiceInterface {
        public String concat(@Synchronized("test-lock-registry") final String arg1, final String arg2);

        public String unsynchronizedConcat(final String arg1, final String arg2);
    }

    public static class TestService implements TestServiceInterface {
        @Override
        public String concat(@Synchronized("test-lock-registry") final String arg1, final String arg2) {
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
        public String concat(@Synchronized("test-lock-registry") final String arg1, final String arg2) {
            return arg1 + arg2;
        }
    }

    public static interface MismatchingTestInterface {
        public String concat(@Synchronized("mismatching") String arg1, String arg2);
    }

    public static class MismatchingTestSubclass implements MismatchingTestInterface {
        @Override
        public String concat(@Synchronized("test-lock-registry") final String arg1, final String arg2) {
            return arg1 + arg2;
        }
    }

}
