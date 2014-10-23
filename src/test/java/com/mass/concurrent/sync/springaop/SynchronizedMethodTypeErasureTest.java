package com.mass.concurrent.sync.springaop;

import static org.joda.time.Duration.standardMinutes;
import static org.junit.Assert.assertEquals;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import com.mass.core.PositiveDuration;

/**
 * Test that Synchronizer can find the @Synchronzied annotation on the join point's target method, despite type erasure.
 * 
 * @author kmassaroni
 */
public class SynchronizedMethodTypeErasureTest {

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

            @Override
            public String key() {
                return "";
            }
        };

        final PositiveDuration actual = SynchronizedMethodUtils.toTimeoutDuration(annotation);

        assertEquals(standardMinutes(7), actual.getDuration());
    }

    @Test
    public void testFindSynchronizedAnnotation_ProxiedGenericParameter() throws Throwable {
        final TestService<String> target = new TestServiceImpl();
        final AspectJProxyFactory factory = new AspectJProxyFactory(target);

        final SynchronizedAdviceSpy spy = new SynchronizedAdviceSpy("test-lock-registry", "abc");

        factory.addAspect(spy.getAdviceSpy());
        final TestService<String> proxy = factory.getProxy();

        final String actual = proxy.concat("abc", "def");

        assertEquals("in subclass abcdef", actual);

        spy.verifyAdviceWasCalled();
    }

    public static interface TestService<T> {
        public String concat(final T syncArg, final String arg2);
    }

    public static abstract class AbstractTestService<T> implements TestService<T> {
        @Override
        public String concat(@Synchronized("test-lock-registry") final T syncArg, final String arg2) {
            return "in superclass " + syncArg + arg2;
        }
    }

    public static class TestServiceImpl extends AbstractTestService<String> implements TestService<String> {
        @Override
        public String concat(@Synchronized("test-lock-registry") final String arg1, final String arg2) {
            return "in subclass " + arg1 + arg2;
        }
    }

    @Test
    public void testShallowLockKeyExpression() throws Exception {
        final Method shallowLock = DerivedKeyTestService.class.getMethod("shallow", ComplexKey.class, String.class);
        final Synchronized annotation = (Synchronized) shallowLock.getParameterAnnotations()[0][0];

        final ComplexKey complexKey = new ComplexKey("shallow key", new NestedComplexKey("deep key"));
        final String actualKey = (String) SynchronizedMethodUtils.getLockKey(complexKey, annotation);
        assertEquals("shallow key", actualKey);
    }

    @Test
    public void testDeepLockKeyExpression() throws Exception {
        final Method shallowLock = DerivedKeyTestService.class.getMethod("deep", ComplexKey.class, String.class);
        final Synchronized annotation = (Synchronized) shallowLock.getParameterAnnotations()[0][0];

        final ComplexKey complexKey = new ComplexKey("shallow key", new NestedComplexKey("deep key"));
        final String actualKey = (String) SynchronizedMethodUtils.getLockKey(complexKey, annotation);
        assertEquals("deep key", actualKey);
    }

    public static class NestedComplexKey {
        private final String nestedValue;

        public NestedComplexKey(final String nestedValue) {
            this.nestedValue = nestedValue;
        }

        public String getNestedValue() {
            return nestedValue;
        }
    }

    public static class ComplexKey {
        private final String lockValue;
        private final NestedComplexKey nestedLock;

        public ComplexKey(final String lockValue, final NestedComplexKey nestedLock) {
            this.lockValue = lockValue;
            this.nestedLock = nestedLock;
        }

        public String getLockValue() {
            return lockValue;
        }

        public NestedComplexKey getNestedLock() {
            return nestedLock;
        }
    }

    public static class DerivedKeyTestService {
        public String shallow(@Synchronized(value = "echo-lock", key = "lockValue") final ComplexKey lock,
                final String echo) {
            return echo;
        }

        public String deep(@Synchronized(value = "echo-lock", key = "nestedLock.nestedValue") final ComplexKey lock,
                final String echo) {
            return echo;
        }
    }

}
