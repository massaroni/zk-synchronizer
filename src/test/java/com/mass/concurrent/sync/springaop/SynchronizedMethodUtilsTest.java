package com.mass.concurrent.sync.springaop;

import static org.joda.time.Duration.standardMinutes;
import static org.junit.Assert.assertEquals;

import java.lang.annotation.Annotation;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import com.mass.core.PositiveDuration;

public class SynchronizedMethodUtilsTest {

    @Test
    public void testToTimeoutDuration() throws Exception {
        final Synchronized annotation = new Synchronized() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return Synchronized.class;
            }

            @Override
            public String value() {
                return "foo";
            }

            @Override
            public TimeUnit timeoutUnits() {
                return TimeUnit.MINUTES;
            }

            @Override
            public long timeoutDuration() {
                return 7;
            }
        };

        final PositiveDuration actual = SynchronizedMethodUtils.toTimeoutDuration(annotation);

        assertEquals(standardMinutes(7), actual.getDuration());
    }

    @Test
    public void testFindSynchronizedAnnotation_ProxiedGenericParameter() throws Throwable {
        final TestService target = new TestService();
        final AspectJProxyFactory factory = new AspectJProxyFactory(target);

        final SynchronizedAdviceSpy spy = new SynchronizedAdviceSpy("test-lock-registry", "abc");

        factory.addAspect(spy.getAdviceSpy());
        final TestService proxy = factory.getProxy();

        final String actual = proxy.concat("abc", "def");

        assertEquals("abcdef", actual);

        spy.verifyAdviceWasCalled();
    }

    public static abstract class AbstractTestService<T> {
        public abstract String concat(@Synchronized("test-lock-registry") final T arg1, final String arg2);
    }

    public static class TestService extends AbstractTestService<String> {
        @Override
        public String concat(@Synchronized("test-lock-registry") final String arg1, final String arg2) {
            return arg1 + arg2;
        }
    }

}
