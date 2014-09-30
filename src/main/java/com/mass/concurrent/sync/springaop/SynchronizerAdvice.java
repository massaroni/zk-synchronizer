package com.mass.concurrent.sync.springaop;

import static com.mass.concurrent.sync.springaop.SynchronizedMethodUtils.toTimeoutDuration;
import static com.mass.concurrent.sync.springaop.config.SynchronizerConfiguration.defaultTimeoutDuration;
import static java.lang.String.format;
import static org.springframework.util.CollectionUtils.isEmpty;

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

    private final ImmutableMap<String, LockRegistry<Object>> lockRegistries;
    private final PositiveDuration globalTimeoutDuration;

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
            lockRegistries = null;
        } else {
            lockRegistries = buildRegistries(locks, factory);
        }
    }

    @Around("execution(* *(.., @com.mass.concurrent.sync.springaop.Synchronized (*), ..))")
    public Object synchronizeMethod(final ProceedingJoinPoint joinPoint) throws Throwable {
        Preconditions.checkState(!isEmpty(lockRegistries), "No interprocess lock registries available.");

        final MethodParameterAnnotation annotation = SynchronizedMethodUtils.getSynchronizedAnnotation(joinPoint);
        Preconditions.checkArgument(annotation != null, "Can't find @Synchronized parameter.");

        final Object[] args = joinPoint.getArgs();
        Preconditions.checkArgument(args != null, "Undefined method args.");
        Preconditions.checkArgument(args.length > annotation.getParameterIndex(),
                "Arguments array doesn't match method signature. @Synchronized parameter index out of bounds.");

        final Object lockKey = args[annotation.getParameterIndex()];

        final Synchronized sync = Synchronized.class.cast(annotation.getAnnotation());
        final String lockName = sync.value();

        final LockRegistry<Object> lockRegistry = lockRegistries.get(lockName);
        Preconditions.checkArgument(lockRegistry != null, "No interprocess lock registry named %s", lockName);

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

    private static ImmutableMap<String, LockRegistry<Object>> buildRegistries(
            final SynchronizerLockRegistryConfiguration[] locks, final LockRegistryFactory factory) {
        Preconditions.checkArgument(locks != null, "Undefined lock definitions.");
        Preconditions.checkArgument(factory != null, "Undefined lock registry factory.");

        final Map<String, LockRegistry<Object>> registries = Maps.newHashMap();

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
