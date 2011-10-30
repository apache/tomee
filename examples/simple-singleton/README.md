Title: Simple Singleton

*Help us document this example! Source available in [svn](http://svn.apache.org/repos/asf/openejb/trunk/openejb/examples/simple-singleton) or [git](https://github.com/apache/openejb/tree/trunk/openejb/examples/simple-singleton). Open a [JIRA](https://issues.apache.org/jira/browse/TOMEE) with patch or pull request*

## ComponentRegistry

    package org.superbiz.registry;
    
    //START SNIPPET: code
    
    import javax.ejb.Lock;
    import javax.ejb.Singleton;
    import java.util.ArrayList;
    import java.util.Collection;
    import java.util.HashMap;
    import java.util.Map;
    
    import static javax.ejb.LockType.READ;
    import static javax.ejb.LockType.WRITE;
    
    @Singleton
    @Lock(READ)
    public class ComponentRegistry {
    
        private final Map<Class, Object> components = new HashMap<Class, Object>();
    
        public <T> T getComponent(Class<T> type) {
            return (T) components.get(type);
        }
    
        public Collection<?> getComponents() {
            return new ArrayList(components.values());
        }
    
        @Lock(WRITE)
        public <T> T setComponent(Class<T> type, T value) {
            return (T) components.put(type, value);
        }
    
        @Lock(WRITE)
        public <T> T removeComponent(Class<T> type) {
            return (T) components.remove(type);
        }
    }

## PropertyRegistry

    package org.superbiz.registry;
    
    //START SNIPPET: code
    
    import javax.annotation.PostConstruct;
    import javax.annotation.PreDestroy;
    import javax.ejb.ConcurrencyManagement;
    import javax.ejb.Singleton;
    import javax.ejb.Startup;
    import java.util.Properties;
    
    import static javax.ejb.ConcurrencyManagementType.BEAN;
    
    @Singleton
    @Startup
    @ConcurrencyManagement(BEAN)
    public class PropertyRegistry {
    
        // Note the java.util.Properties object is a thread-safe
        // collections that uses synchronization.  If it didn't
        // you would have to use some form of synchronization
        // to ensure the PropertyRegistryBean is thread-safe.
        private final Properties properties = new Properties();
    
        // The @Startup method ensures that this method is
        // called when the application starts up.
        @PostConstruct
        public void applicationStartup() {
            properties.putAll(System.getProperties());
        }
    
        @PreDestroy
        public void applicationShutdown() {
            properties.clear();
        }
    
        public String getProperty(String key) {
            return properties.getProperty(key);
        }
    
        public String setProperty(String key, String value) {
            return (String) properties.setProperty(key, value);
        }
    
        public String removeProperty(String key) {
            return (String) properties.remove(key);
        }
    }

## ComponentRegistryTest

    package org.superbiz.registry;
    
    import junit.framework.TestCase;
    
    import javax.ejb.embeddable.EJBContainer;
    import javax.naming.Context;
    import java.net.URI;
    import java.util.Date;
    
    //START SNIPPET: code
    public class ComponentRegistryTest extends TestCase {
    
        public void test() throws Exception {
    
            final Context context = EJBContainer.createEJBContainer().getContext();
    
            // Both references below will point to the exact same instance
            ComponentRegistry one = (ComponentRegistry) context.lookup("java:global/simple-singleton/ComponentRegistry");
    
            ComponentRegistry two = (ComponentRegistry) context.lookup("java:global/simple-singleton/ComponentRegistry");
    
    
            // Let's prove both references point to the same instance
    
    
            // Set a URL into 'one' and retrieve it from 'two'
    
            URI expectedUri = new URI("foo://bar/baz");
    
            one.setComponent(URI.class, expectedUri);
    
            URI actualUri = two.getComponent(URI.class);
    
            assertSame(expectedUri, actualUri);
    
    
            // Set a Date into 'two' and retrieve it from 'one'
    
            Date expectedDate = new Date();
    
            two.setComponent(Date.class, expectedDate);
    
            Date actualDate = one.getComponent(Date.class);
    
            assertSame(expectedDate, actualDate);
        }
    }

## PropertiesRegistryTest

    package org.superbiz.registry;
    
    import junit.framework.TestCase;
    
    import javax.ejb.embeddable.EJBContainer;
    import javax.naming.Context;
    
    public class PropertiesRegistryTest extends TestCase {
    
        public void test() throws Exception {
    
            Context context = EJBContainer.createEJBContainer().getContext();
    
            PropertyRegistry one = (PropertyRegistry) context.lookup("java:global/simple-singleton/PropertyRegistry");
    
            PropertyRegistry two = (PropertyRegistry) context.lookup("java:global/simple-singleton/PropertyRegistry");
    
    
            one.setProperty("url", "http://superbiz.org");
    
            String url = two.getProperty("url");
    
            assertEquals("http://superbiz.org", url);
    
    
            two.setProperty("version", "1.0.5");
    
            String version = one.getProperty("version");
    
            assertEquals("1.0.5", version);
        }
    }

# Running

    
    -------------------------------------------------------
     T E S T S
    -------------------------------------------------------
    Running org.superbiz.registry.ComponentRegistryTest
    Apache OpenEJB 4.0.0-beta-1    build: 20111002-04:06
    http://openejb.apache.org/
    INFO - openejb.home = /Users/dblevins/examples/simple-singleton
    INFO - openejb.base = /Users/dblevins/examples/simple-singleton
    INFO - Using 'javax.ejb.embeddable.EJBContainer=true'
    INFO - Configuring Service(id=Default Security Service, type=SecurityService, provider-id=Default Security Service)
    INFO - Configuring Service(id=Default Transaction Manager, type=TransactionManager, provider-id=Default Transaction Manager)
    INFO - Found EjbModule in classpath: /Users/dblevins/examples/simple-singleton/target/classes
    INFO - Beginning load: /Users/dblevins/examples/simple-singleton/target/classes
    INFO - Configuring enterprise application: /Users/dblevins/examples/simple-singleton
    INFO - Configuring Service(id=Default Singleton Container, type=Container, provider-id=Default Singleton Container)
    INFO - Auto-creating a container for bean PropertyRegistry: Container(type=SINGLETON, id=Default Singleton Container)
    INFO - Configuring Service(id=Default Managed Container, type=Container, provider-id=Default Managed Container)
    INFO - Auto-creating a container for bean org.superbiz.registry.ComponentRegistryTest: Container(type=MANAGED, id=Default Managed Container)
    INFO - Enterprise application "/Users/dblevins/examples/simple-singleton" loaded.
    INFO - Assembling app: /Users/dblevins/examples/simple-singleton
    INFO - Jndi(name="java:global/simple-singleton/PropertyRegistry!org.superbiz.registry.PropertyRegistry")
    INFO - Jndi(name="java:global/simple-singleton/PropertyRegistry")
    INFO - Jndi(name="java:global/simple-singleton/ComponentRegistry!org.superbiz.registry.ComponentRegistry")
    INFO - Jndi(name="java:global/simple-singleton/ComponentRegistry")
    INFO - Jndi(name="java:global/EjbModule453799164/org.superbiz.registry.ComponentRegistryTest!org.superbiz.registry.ComponentRegistryTest")
    INFO - Jndi(name="java:global/EjbModule453799164/org.superbiz.registry.ComponentRegistryTest")
    INFO - Created Ejb(deployment-id=org.superbiz.registry.ComponentRegistryTest, ejb-name=org.superbiz.registry.ComponentRegistryTest, container=Default Managed Container)
    INFO - Created Ejb(deployment-id=PropertyRegistry, ejb-name=PropertyRegistry, container=Default Singleton Container)
    INFO - Created Ejb(deployment-id=ComponentRegistry, ejb-name=ComponentRegistry, container=Default Singleton Container)
    INFO - Started Ejb(deployment-id=org.superbiz.registry.ComponentRegistryTest, ejb-name=org.superbiz.registry.ComponentRegistryTest, container=Default Managed Container)
    INFO - Started Ejb(deployment-id=PropertyRegistry, ejb-name=PropertyRegistry, container=Default Singleton Container)
    INFO - Started Ejb(deployment-id=ComponentRegistry, ejb-name=ComponentRegistry, container=Default Singleton Container)
    INFO - Deployed Application(path=/Users/dblevins/examples/simple-singleton)
    Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.075 sec
    Running org.superbiz.registry.PropertiesRegistryTest
    INFO - EJBContainer already initialized.  Call ejbContainer.close() to allow reinitialization
    Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.004 sec
    
    Results :
    
    Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
    
