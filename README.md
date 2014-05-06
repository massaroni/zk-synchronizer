zk-synchronizer
===============

Java library for annotation-based distributed locking, with Zookeeper and the Curator Framework, in a Spring managed application.

Your use case:

1. you have multiple machines that want to synchronize access to the same pool of shared resources
2. you have a dynamic pool of shared resources, each resource has a unique key, and you want to allow concurrent access to different resources. "Don't make me wait for resource B just because someone else has a lock on resource A."
3. you may have many pools of resources
4. you may have your own models representing resource keys

## Installing

**This is a beta release, despite the version number.**

### Maven (POM)

Add this repository in your `pom.xml` file:

```xml
<repositories>
    <repository>
        <id>mass-public-synchronizer-releases</id>
        <url>https://raw.github.com/massaroni/zk-synchronizer/maven-repo/mvn/releases</url>
    </repository>
</repositories>
```

### Maven (Gradle)

```groovy
repositories {
    maven {
        url "https://raw.github.com/massaroni/zk-synchronizer/maven-repo/mvn/releases"
    }
}
```

### Requirements

Spring AOP, and a Zookeeper server

(there is a local-jvm-only mode that you can use without a zookeeper connection)

## Just Annotate Your Java Method

You can sprinkle these @Synchronized annotations on parameters of public methods in spring-managed classes. This advice supports one @Synchronized annotation, on public methods. 

```java
@Service
public class ServiceWithCriticalSection {
	public void accessSomeSharedResource(@Synchronized("myPoolOfResources") String resourceId) {
	 ...
	}
	
	public void anotherMethodTouchesMyResourcePool(int someInt, @Synchronized("myPoolOfResources") String resourceId, String someString) {
	 ...
	}
	
	public void accessSomeOtherKindOfSharedResource(@Synchronized("anotherPoolOfResources") int resourceId) {
	 ...
	}
}

@Service
public class SomeUnrelatedService {
	public void alsoWantsAccessToTheSameResources(@Synchronized("myPoolOfResources") String resourceId, int someInt) {
	 ...
	}
}

```

## Example Spring Configuration

Pure-XML Style, with all the available configuration, including optional properties.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:context="http://www.springframework.org/schema/context"
	xmlns:aop="http://www.springframework.org/schema/aop"
	xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
						http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context.xsd
						http://www.springframework.org/schema/aop http://www.springframework.org/schema/aop/spring-aop-3.0.xsd">

    <context:annotation-config />
    <aop:aspectj-autoproxy />

	<!-- boilerplate synchronizer configuration. this line injects synchronizer into your app context. -->
    <bean class="com.mass.concurrent.sync.springaop.config.SynchronizerAdviceConfigurationBean" />
    <!-- or you can component-scan this package: -->
    <-- <context:component-scan base-package="com.mass.concurrent.sync.springaop.config" /> -->

	<!-- these are the services I want to synchronize -->
    <bean class="com.me.myservices.ServiceWithCriticalSection" />
    <bean class="com.me.myservices.SomeUnrelatedService" />

	<!-- custom, user-provided synchronizer configuration -->
	
	<!-- you need one lock definition for each lock registry named in a synchronizer annotation, like this: @Synchronized("myLockRegistry") --> 
    <bean class="com.mass.concurrent.sync.springaop.config.SynchronizerLockRegistryConfiguration">
    	<constructor-arg name="name" value="myPoolOfResources" />
    	<constructor-arg name="lockKeyFactory">
    		<!-- you can use a prepackaged lock key factory, or make one for your own model -->
    		<!-- this lock key factory must accept the type of method parameter you're annotating -->
    		<bean class="com.mass.concurrent.sync.keyfactories.StringLockKeyFactory" />
    	</constructor-arg>
    </bean>
    
    <!-- here's another lock registry, for another pool of resources --> 
    <bean class="com.mass.concurrent.sync.springaop.config.SynchronizerLockRegistryConfiguration">
    	<constructor-arg name="name" value="anotherPoolOfResources" />
    	
    	<!-- optionally, you can override the default locking policy, for individual registries -->
    	<constructor-arg name="policyOverride" value="BEST_EFFORT" />
    	
    	<constructor-arg name="lockKeyFactory">
    		<!-- you can use a prepackaged lock key factory, or make one for your own model -->
    		<!-- this lock key factory must accept the type of method parameter you're annotating -->
    		<bean class="com.mass.concurrent.sync.keyfactories.IntegerLockKeyFactory" />
    	</constructor-arg>
    </bean>
    
    <!-- this is the global Synchronizer configuration bean, and you need exactly one per app context -->
	<bean class="com.mass.concurrent.sync.springaop.config.SynchronizerConfiguration" >
		<!-- this says we should use Zookeeper based locks for everything (see the SynchronizerScope class) -->
		<!-- ZOOKEEPER = zk mutexes -->
		<!-- LOCAL_JVM = plain java.util.concurrent locks (not going to synchronize your cluster) -->
		<constructor-arg name="scope" value="ZOOKEEPER" />
		
		<!-- (optional) default lock policy that applies to all lock registries (see the SynchronizerLockingPolicy class)-->
		<!-- STRICT = throw errors and deny access to critical sections if you lose your connection to zookeeper -->
		<!-- BEST_EFFORT = fail over to a jvm-scoped lock, if there's a zookeeper error -->
		<constructor-arg name="defaultLockingPolicy" value="STRICT" />
		
		<!-- all lock registries will have their own subdirectory, rooted under this base path -->
		<constructor-arg name="zkMutexBasePath" value="/zkpath/mutexes" />
	</bean>
	<!-- end custom, user-provided synchronizer configuration -->
        
</beans>

```