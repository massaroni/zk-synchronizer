package com.mass.concurrent.sync.springaop;

import static com.mass.concurrent.sync.springaop.config.SynchronizerConfiguration.defaultTimeoutDuration;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.joda.time.Duration.standardMinutes;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.joda.time.Duration;
import org.junit.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import com.google.common.util.concurrent.UncheckedTimeoutException;
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
    public void testAopProxy_AnnotationTimeoutPriority() throws Throwable {
        final AnnotationTimeoutTestService target = new AnnotationTimeoutTestService();
        final AspectJProxyFactory factory = new AspectJProxyFactory(target);

        final PositiveDuration annotationTimeout = new PositiveDuration(standardMinutes(7));
        final PositiveDuration timeout1 = PositiveDuration.standardSeconds(1);
        final PositiveDuration timeout4 = PositiveDuration.standardSeconds(4);
        assertNotEquals(timeout1, timeout4);
        assertNotEquals(timeout1, defaultTimeoutDuration);
        assertNotEquals(timeout4, defaultTimeoutDuration);

        final SynchronizedAdviceSpy spy = new SynchronizedAdviceSpy("test-lock-registry", "abc", annotationTimeout,
                timeout1, timeout4);

        factory.addAspect(spy.getAdviceSpy());
        final TestServiceInterface proxy = factory.getProxy();

        final String actual = proxy.concat("abc", "def");

        assertEquals("abcdef", actual);

        spy.verifyAdviceWasCalled();
    }

    @Test
    public void testAopProxy_AnnotationTimeoutInheritance() throws Throwable {
        final InheritedAnnotationTimeoutTestService target = new InheritedAnnotationTimeoutTestService();
        final AspectJProxyFactory factory = new AspectJProxyFactory(target);

        final PositiveDuration annotationTimeout = new PositiveDuration(new Duration(3L));
        final PositiveDuration timeout1 = PositiveDuration.standardSeconds(1);
        final PositiveDuration timeout4 = PositiveDuration.standardSeconds(4);
        assertNotEquals(timeout1, timeout4);
        assertNotEquals(timeout1, defaultTimeoutDuration);
        assertNotEquals(timeout4, defaultTimeoutDuration);
        assertNotEquals(annotationTimeout, defaultTimeoutDuration);

        final SynchronizedAdviceSpy spy = new SynchronizedAdviceSpy("test-lock-registry", "abc", annotationTimeout,
                timeout1, timeout4);

        factory.addAspect(spy.getAdviceSpy());
        final AnnotationTimeoutTestServiceInterface proxy = factory.getProxy();

        final String actual = proxy.concat("abc", "def");

        assertEquals("abcdef", actual);

        spy.verifyAdviceWasCalled();
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

    @Test(expected = UncheckedTimeoutException.class)
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

    public static interface AnnotationTimeoutTestServiceInterface {
        public String concat(
                @Synchronized(value = "test-lock-registry", timeoutDuration = 3, timeoutUnits = MILLISECONDS) final String arg1,
                final String arg2);
    }

    public static class InheritedAnnotationTimeoutTestService implements AnnotationTimeoutTestServiceInterface {
        @Override
        public String concat(@Synchronized(value = "test-lock-registry") final String arg1, final String arg2) {
            return arg1 + arg2;
        }
    }

    public static class AnnotationTimeoutTestService implements TestServiceInterface {
        @Override
        public String concat(
                @Synchronized(value = "test-lock-registry", timeoutDuration = 7, timeoutUnits = MINUTES) final String arg1,
                final String arg2) {
            return arg1 + arg2;
        }

        @Override
        public String unsynchronizedConcat(final String arg1, final String arg2) {
            return "unsynchronized: " + arg1 + arg2;
        }
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
