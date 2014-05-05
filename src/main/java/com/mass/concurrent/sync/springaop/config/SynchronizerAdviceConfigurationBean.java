package com.mass.concurrent.sync.springaop.config;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.mass.concurrent.sync.springaop.SynchronizerAdvice;
import com.mass.concurrent.sync.zookeeper.LockRegistries;
import com.mass.concurrent.sync.zookeeper.LockRegistryFactory;

/**
 * This bean factory makes it easier to configure a Synchronizer spring application context, because it conditionally
 * queries the app context for the CuratorFramwork required for zookeeper locks. That way, you don't have to provide one
 * at all, if you just want to use local-jvm locks.
 * 
 * @author kmassaroni
 */
@Configuration
public class SynchronizerAdviceConfigurationBean implements ApplicationContextAware {
    private ApplicationContext context;

    @Autowired
    private SynchronizerConfiguration configuration;

    @Autowired
    private SynchronizerLockRegistryConfiguration[] lockDefinitions;

    private final Supplier<SynchronizerAdvice> adviceSupplier = Suppliers.memoize(new Supplier<SynchronizerAdvice>() {
        @Override
        public SynchronizerAdvice get() {
            checkArgument(configuration != null, "Can't build advice: Undefined synchronizer configuration.");
            checkArgument(context != null, "Can't build advice: Undefined application context.");

            final LockRegistryFactory factory = registryFactory();
            final SynchronizerAdvice advice = new SynchronizerAdvice(lockDefinitions, factory);
            return advice;
        }
    });

    private LockRegistryFactory registryFactory() {
        final SynchronizerScope scope = configuration.getScope();
        final SynchronizerLockingPolicy defaultLockingPolicy = configuration.getDefaultLockingPolicy();
        final String zkBasePath = configuration.getZkMutexBasePath();

        switch (scope) {
        case LOCAL_JVM:
            return LockRegistries.newLocalLockRegistryFactory();
        case ZOOKEEPER:
            final CuratorFramework zkClient = context.getBean(CuratorFramework.class);
            checkState(zkClient != null,
                    "No CuratorFramework in the application context, required by Synchronizer for zookeeper inter-process locking.");
            return LockRegistries.newInterProcessLockRegistryFactory(zkClient, defaultLockingPolicy, zkBasePath);
        default:
            throw new IllegalStateException("Unexpected SynchronizerScope: " + scope);
        }
    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        Preconditions.checkArgument(applicationContext != null, "Undefined application context.");
        context = applicationContext;
    }

    @Bean
    public SynchronizerAdvice synchronizer() throws Exception {
        return adviceSupplier.get();
    }

}
