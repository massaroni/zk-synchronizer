package com.mass.concurrent.sync.springaop;

import static com.mass.concurrent.sync.springaop.SynchronizedMethodUtils.toTimeoutDuration;
import static com.mass.concurrent.sync.springaop.config.SynchronizerConfiguration.defaultTimeoutDuration;
import static java.lang.String.format;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.UncheckedTimeoutException;
import com.mass.concurrent.LockRegistry;
import com.mass.concurrent.sync.keyfactories.StringLockKeyFactory;
import com.mass.concurrent.sync.springaop.config.SynchronizerConfiguration;
import com.mass.concurrent.sync.springaop.config.SynchronizerLockRegistryConfiguration;
import com.mass.concurrent.sync.zookeeper.LockRegistryFactory;
import com.mass.core.PositiveDuration;
import com.mass.lang.MethodParameterAnnotation;

/**
 * Proxy method calls with a @Synchronized parameter. This wraps their method calls in a best-effort interprocess lock.
 * This synchronizes methods that share resources across process boundaries.
 * 
 * @author kmassaroni
 */
@Aspect
@Order(Integer.MIN_VALUE)
public class SynchronizerAdvice {
    private static final Log log = LogFactory.getLog(SynchronizerAdvice.class);
    private static String METHOD_KEYLESS_LOCK_REGISTRY_NAME = "SYNCHRONIZER_KEYLESS_LOCKS";

    private final ImmutableMap<String, LockRegistry<Object>> lockRegistries;
    private final PositiveDuration globalTimeoutDuration;
    private final LockRegistry<Object> keylessLocks;

    public SynchronizerAdvice(final SynchronizerLockRegistryConfiguration[] locks, final LockRegistryFactory factory) {
        this(locks, factory, null);
    }

    @Autowired
    public SynchronizerAdvice(final SynchronizerLockRegistryConfiguration[] locks, final LockRegistryFactory factory,
            final SynchronizerConfiguration globalConfig) {
        Preconditions.checkArgument(factory != null, "Undefined lock registry factory.");

        globalTimeoutDuration = globalConfig == null ? defaultTimeoutDuration : globalConfig.getGlobalTimeoutDuration();
        Preconditions.checkArgument(globalTimeoutDuration != null, "Undefined global timeout duration.");

        log.info("new SynchronizerAdvice");

        if (locks == null) {
            lockRegistries = buildRegistries(new SynchronizerLockRegistryConfiguration[] {}, factory);
        } else {
            lockRegistries = buildRegistries(locks, factory);
        }

        keylessLocks = lockRegistries.get(METHOD_KEYLESS_LOCK_REGISTRY_NAME);
        Preconditions.checkState(keylessLocks != null, "Can't setup keyless lock registry.");
    }

    @Around("execution(@com.mass.concurrent.sync.springaop.Synchronized * *(..))")
    public Object synchronizeMethod(final ProceedingJoinPoint joinPoint) throws Throwable {
        final Annotation annotation = SynchronizedMethodUtils.getMethodLevelSynchronizedAnnotation(joinPoint);
        Preconditions.checkArgument(annotation != null, "Can't find @Synchronized annotation in %s", joinPoint);
        final Synchronized sync = Synchronized.class.cast(annotation);
        final String lockName = sync.value();

        final ReentrantLock lock = keylessLocks.getLock(lockName);
        Preconditions.checkState(lock != null, "Can't get interprocess lock for keyless registry %s", lockName);

        final PositiveDuration timeoutDuration = getTimeoutDuration(sync, keylessLocks);
        Preconditions.checkArgument(timeoutDuration != null, "Undefined timeout duration for keyless lock %s.",
                lockName);

        if (log.isTraceEnabled()) {
            log.trace("Locking keyless " + lockName);
        }

        if (!lock.tryLock(timeoutDuration.getMillis(), TimeUnit.MILLISECONDS)) {
            final String msg = format("Timed out getting interprocess synchronizer lock for keyless lock %s", lockName);
            throw new UncheckedTimeoutException(msg);
        }

        try {
            return joinPoint.proceed();
        } finally {
            lock.unlock();
        }
    }

    @Around("execution(* *(.., @com.mass.concurrent.sync.springaop.Synchronized (*), ..))")
    public Object synchronizeMethodArg(final ProceedingJoinPoint joinPoint) throws Throwable {
        Preconditions.checkState(lockRegistries.size() > 1, "No interprocess lock registries available.");

        final MethodParameterAnnotation annotation = SynchronizedMethodUtils.getSynchronizedAnnotation(joinPoint);
        Preconditions.checkArgument(annotation != null, "Can't find @Synchronized parameter.");

        final Object[] args = joinPoint.getArgs();
        Preconditions.checkArgument(args != null, "Undefined method args.");
        Preconditions.checkArgument(args.length > annotation.getParameterIndex(),
                "Arguments array doesn't match method signature. @Synchronized parameter index out of bounds.");

        final Object lockKeyArg = args[annotation.getParameterIndex()];

        final Synchronized sync = Synchronized.class.cast(annotation.getAnnotation());
        final String lockName = sync.value();

        final LockRegistry<Object> lockRegistry = lockRegistries.get(lockName);
        Preconditions.checkArgument(lockRegistry != null, "No interprocess lock registry named %s", lockName);

        final Object lockKey = SynchronizedMethodUtils.getLockKey(lockKeyArg, sync);

        final ReentrantLock lock = lockRegistry.getLock(lockKey);
        Preconditions.checkState(lock != null, "Can't get interprocess lock for registry %s, for key %s", lockName,
                lockKey);

        final PositiveDuration timeoutDuration = getTimeoutDuration(sync, lockRegistry);
        Preconditions.checkArgument(timeoutDuration != null, "Undefined timeout duration for registry %s.", lockName);

        if (log.isTraceEnabled()) {
            log.trace("Locking " + lockKey);
        }

        if (!lock.tryLock(timeoutDuration.getMillis(), TimeUnit.MILLISECONDS)) {
            final String msg = format("Timed out getting interprocess synchronizer lock for registry %s, for key %s",
                    lockName, lockKey);
            throw new UncheckedTimeoutException(msg);
        }

        try {
            return joinPoint.proceed();
        } finally {
            lock.unlock();
        }
    }

    private PositiveDuration getTimeoutDuration(final Synchronized annotation, final LockRegistry<?> registry) {
        final PositiveDuration annotationTimeout = toTimeoutDuration(annotation);

        if (annotationTimeout != null) {
            return annotationTimeout;
        }

        final PositiveDuration registryTimeout = registry.getTimeoutDuration();
        return registryTimeout != null ? registryTimeout : globalTimeoutDuration;
    }

    static SynchronizerLockRegistryConfiguration keylessLocksConfiguration() {
        return new SynchronizerLockRegistryConfiguration(METHOD_KEYLESS_LOCK_REGISTRY_NAME, new StringLockKeyFactory());
    }

    private static LockRegistry<Object> newKeylessLockRegistry(final LockRegistryFactory factory) {
        final SynchronizerLockRegistryConfiguration keylessRegistryConfig = keylessLocksConfiguration();
        return factory.newLockRegistry(keylessRegistryConfig);
    }

    private static ImmutableMap<String, LockRegistry<Object>> buildRegistries(
            final SynchronizerLockRegistryConfiguration[] locks, final LockRegistryFactory factory) {
        Preconditions.checkArgument(locks != null, "Undefined lock definitions.");
        Preconditions.checkArgument(factory != null, "Undefined lock registry factory.");

        final Map<String, LockRegistry<Object>> registries = Maps.newHashMap();

        final LockRegistry<Object> keylessLocks = newKeylessLockRegistry(factory);
        Preconditions.checkState(keylessLocks != null, "Can't make keyless lock registry.");
        registries.put(METHOD_KEYLESS_LOCK_REGISTRY_NAME, keylessLocks);

        for (final SynchronizerLockRegistryConfiguration lockDefinition : locks) {
            final String name = lockDefinition.getName().getValue();
            Preconditions.checkArgument(!registries.containsKey(name),
                    "%s is already registered as an interprocess lock registry.", name);
            final LockRegistry<Object> registry = factory.newLockRegistry(lockDefinition);
            registries.put(name, registry);
        }

        return ImmutableMap.copyOf(registries);
    }
}
