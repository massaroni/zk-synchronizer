package com.mass.concurrent.sync.springaop;

import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * This reads one config property: onlinenow.update.vendor=[inmemory|kafka]
 * 
 * @author kmassaroni
 */
@Configuration
@PropertySource("classpath:play2prep-server-java.properties")
public class SynchronizedInterProcessLockSpringConfiguration {

    @Value("${synchronized.mutexes.vendor}")
    private String mutexVendor;

    @Autowired
    private CuratorFramework zkClient;

    @Bean
    public LockRegistryFactory lockRegistryFactory() {
        if ("zookeeper".equals(mutexVendor)) {
            return new InterProcessLockRegistryFactory(zkClient);
        } else {
            return new LocalLockRegistryFactory();
        }
    }
}
