index-group=Unrevised
type=page
status=published
title=Custom resources in an EAR archive
~~~~~~

TomEE allows you to define your own resources within your application, and declare them in `META-INF/resources.xml`. This allows you do inject these resource into any managed component within your application.

In addition to this, you can also define a `create` method on either the resource itself, or on a POJO that acts as a factory. This example demonstrates using the `create` method to additionally register the resource as a JMX MBean, as well as make it available for injection.

## Resource

Custom resources can be defined using very simple Java classes. In this particular instance, as the application also wants to register this resource as an MBean, the resource class needs to follow the MBean specification.

	public class Hello implements HelloMBean {

	    private AtomicInteger count = new AtomicInteger(0);

	    @Override
	    public String greet(String name) {
	        if (name == null) {
	            throw new NullPointerException("Name cannot be null");
	        }

	        return "Hello, " + name;
	    }

	    @Override
	    public int getCount() {
	        return count.get();
	    }

	    @Override
	    public void setCount(int value) {
	        count.set(value);
	    }

	    @Override
	    public void increment() {
	        count.incrementAndGet();
	    }
	}
	
	public interface HelloMBean {

	    public String greet(final String name);

	    public int getCount();

	    public void setCount(int count);

	    public void increment();

	}

## Create method

To avoid adding the logic to register the resource as an MBean in every resource, the application provides a single class with a create() method that takes care of this logic for us.

	public class JMXBeanCreator {

	    private static Logger LOGGER = Logger.getLogger(JMXBeanCreator.class.getName());
	    private Properties properties;

	    public Object create() throws MBeanRegistrationException {
	        // instantiate the bean

	        final String code = properties.getProperty("code");
	        final String name = properties.getProperty("name");

	        requireNotNull(code);
	        requireNotNull(name);

	        try {
	            final Class<?> cls = Class.forName(code, true, Thread.currentThread().getContextClassLoader());
	            final Object instance = cls.newInstance();

	            final Field[] fields = cls.getDeclaredFields();
	            for (final Field field : fields) {

	                final String property = properties.getProperty(field.getName());
	                if (property == null) {
	                    continue;
	                }

	                try {
	                    field.setAccessible(true);
	                    field.set(instance, Converter.convert(property, field.getType(), field.getName()));
	                } catch (Exception e) {
	                    LOGGER.info(String.format("Unable to set value %s on field %s", property, field.getName()));
	                }
	            }

	            final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
	            final ObjectName objectName = new ObjectName(name);
	            mbs.registerMBean(instance, objectName);

	            return instance;

	        } catch (final ClassNotFoundException e) {
	            LOGGER.severe("Unable to find class " + code);
	            throw new MBeanRegistrationException(e);
	        } catch (final InstantiationException e) {
	            LOGGER.severe("Unable to create instance of class " + code);
	            throw new MBeanRegistrationException(e);
	        } catch (final IllegalAccessException e) {
	            LOGGER.severe("Illegal access: " + code);
	            throw new MBeanRegistrationException(e);
	        } catch (final MalformedObjectNameException e) {
	            LOGGER.severe("Malformed MBean name: " + name);
	            throw new MBeanRegistrationException(e);
	        } catch (final InstanceAlreadyExistsException e) {
	            LOGGER.severe("Instance already exists: " + name);
	            throw new MBeanRegistrationException(e);
	        } catch (final NotCompliantMBeanException e) {
	            LOGGER.severe("Class is not a valid MBean: " + code);
	            throw new MBeanRegistrationException(e);
	        } catch (final javax.management.MBeanRegistrationException e) {
	            LOGGER.severe("Error registering " + name + ", " + code);
	            throw new MBeanRegistrationException(e);
	        }
	    }

	    private void requireNotNull(final String object) throws MBeanRegistrationException {
	        if (object == null) {
	            throw new MBeanRegistrationException("code property not specified, stopping");
	        }
	    }

	    public Properties getProperties() {
	        return properties;
	    }

	    public void setProperties(final Properties properties) {
	        this.properties = properties;
	    }
	}
    

Note that this class uses the properties defined in the <Resource> configuration (below), combined with reflection, to instantiate the resource, and set its attributes. The code above requires two properties `code` and `name` in order to know what class to create, and the JMX name to register it under.

## Resource

The resource can be defined in `META-INF/resources.xml` as follows:

	<Resources>
	  <Resource id="Hello" class-name="org.superbiz.resource.jmx.factory.JMXBeanCreator" factory-name="create">
	    code org.superbiz.resource.jmx.resources.Hello
	    name superbiz.test:name=Hello
	    count 12345
	  </Resource>
	</Resources>

Note that the class-name attribute refers to the factory class, and not the resource. Once the resource has been created and bound in TomEE's JNDI tree the factory is no longer used.

## Using @Resource for injection

The test case for this example demonstrates injection into an EJB as one way of accessing the resource, and also accessing the resource via JMX.

	@RunWith(Arquillian.class)
	public class JMXTest {

	    @EJB
	    private TestEjb ejb;

	    @Deployment
	    public static EnterpriseArchive createDeployment() {

	        final JavaArchive ejbJar = new Mvn.Builder()
	                .name("jmx-ejb.jar")
	                .build(JavaArchive.class)
	                .addClass(JMXTest.class)
	                .addClass(TestEjb.class);

	        final EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class, "jmx.ear")
	                .addAsModule(ejbJar);

	        return ear;
	    }

	    @Test
	    public void test() throws Exception {
	        final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
	        final ObjectName objectName = new ObjectName("superbiz.test:name=Hello");

	        Assert.assertNotNull(ejb);
        
	        Assert.assertEquals(0, mbs.getAttribute(objectName, "Count"));
	        Assert.assertEquals(0, ejb.getCount());
        
	        mbs.invoke(objectName, "increment", new Object[0], new String[0]);
	        Assert.assertEquals(1, mbs.getAttribute(objectName, "Count"));
	        Assert.assertEquals(1, ejb.getCount());
        
	        ejb.increment();
	        Assert.assertEquals(2, mbs.getAttribute(objectName, "Count"));
	        Assert.assertEquals(2, ejb.getCount());

	        Attribute attribute = new Attribute("Count", 12345);
	        mbs.setAttribute(objectName, attribute);
	        Assert.assertEquals(12345, mbs.getAttribute(objectName, "Count"));
	        Assert.assertEquals(12345, ejb.getCount());
        
	        ejb.setCount(23456);
	        Assert.assertEquals(23456, mbs.getAttribute(objectName, "Count"));
	        Assert.assertEquals(23456, ejb.getCount());

	        Assert.assertEquals("Hello, world", mbs.invoke(objectName, "greet", new Object[] { "world" }, new String[] { String.class.getName() }));
	        Assert.assertEquals("Hello, world", ejb.greet("world"));
	    }

	    @Singleton
	    @Lock(LockType.READ)
	    public static class TestEjb {

	        @Resource(name="jmx/Hello")
	        private HelloMBean helloMBean;

	        public String greet(String name) {
	            return helloMBean.greet(name);
	        }

	        public void setCount(int count) {
	            helloMBean.setCount(count);
	        }

	        public void increment() {
	            helloMBean.increment();
	        }

	        public int getCount() {
	            return helloMBean.getCount();
	        }
	    }
	}

The name `<appname>/<resource-id>` attribute is used on the `@Resource` annotation to perform the injection. No further configuration is needed to inject the resource.

# Additional properties

In addition to the `code` and `name` properties that the code above uses to instantiate the resource, TomEE itself provides some
properties to provide more control over the creation of resources.

Resources are typically discovered, created, and bound to JNDI very early on in the deployment process, as other components depend on them. This may lead to problems where the final classpath for the application has not yet been determined, and therefore TomEE is unable to load your custom resource. 

The following properties can be used to change this behavior.

* Lazy

This is a boolean value, which when true, creates a proxy that defers the actual instantiation of the resource until the first time it is looked up from JNDI. This can be useful if the resource requires the application classpath, or to improve startup time by not fully initializing resources that might not be used.

* UseAppClassLoader 

This boolean value forces a lazily instantiated resource to use the application classloader, instead of the classloader available when the resources were first processed.

* InitializeAfterDeployment

This boolean setting forces a resource created with the Lazy property to be instantiated once the application has started, as opposed to waiting for it to be looked up. Use this flag if you require the resource to be loaded, irrespective of whether it is injected into a managed component or manually looked up.

By default, all of these settings are `false`, unless TomEE encounters a custom application resource that cannot be instantiated until the application has started. In this case, it will set these three flags to `true`, unless the `Lazy` flag has been explicitly set.

# PostConstruct / PreDestroy

As an alternative to using a factory method, you can use @PostConstruct and @PreDestroy methods within your resource class (note that you cannot use this within a factory class) to manage any additional creation or cleanup activities. TomEE will automatically call these methods when the application is started and destroyed. Using @PostConstruct will effectively force a lazily loaded resource to be instantiated when the application is starting - in the same way that the `InitializeAfterDeployment` property does.

    public class Alternative implements AlternativeMBean {
    
        private static Logger LOGGER = Logger.getLogger(Alternative.class.getName());
        private Properties properties;
    
        @PostConstruct
        public void postConstruct() throws MBeanRegistrationException {
            // initialize the bean
    
            final String code = properties.getProperty("code");
            final String name = properties.getProperty("name");
    
            requireNotNull(code);
            requireNotNull(name);
    
            try {
                final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
                final ObjectName objectName = new ObjectName(name);
                mbs.registerMBean(this, objectName);
            } catch (final MalformedObjectNameException e) {
                LOGGER.severe("Malformed MBean name: " + name);
                throw new MBeanRegistrationException(e);
            } catch (final InstanceAlreadyExistsException e) {
                LOGGER.severe("Instance already exists: " + name);
                throw new MBeanRegistrationException(e);
            } catch (final NotCompliantMBeanException e) {
                LOGGER.severe("Class is not a valid MBean: " + code);
                throw new MBeanRegistrationException(e);
            } catch (final javax.management.MBeanRegistrationException e) {
                LOGGER.severe("Error registering " + name + ", " + code);
                throw new MBeanRegistrationException(e);
            }
        }
    
        @PreDestroy
        public void preDestroy() throws MBeanRegistrationException {
            final String name = properties.getProperty("name");
            requireNotNull(name);
    
            try {
                final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
                final ObjectName objectName = new ObjectName(name);
                mbs.unregisterMBean(objectName);
            } catch (final MalformedObjectNameException e) {
                LOGGER.severe("Malformed MBean name: " + name);
                throw new MBeanRegistrationException(e);
            } catch (final javax.management.MBeanRegistrationException e) {
                LOGGER.severe("Error unregistering " + name);
                throw new MBeanRegistrationException(e);
            } catch (InstanceNotFoundException e) {
                LOGGER.severe("Error unregistering " + name);
                throw new MBeanRegistrationException(e);
            }
        }
    
        private void requireNotNull(final String object) throws MBeanRegistrationException {
            if (object == null) {
                throw new MBeanRegistrationException("code property not specified, stopping");
            }
        }
    
        public Properties getProperties() {
            return properties;
        }
    
        public void setProperties(final Properties properties) {
            this.properties = properties;
        }
    
        private int count = 0;
    
        @Override
        public String greet(String name) {
            if (name == null) {
                throw new NullPointerException("Name cannot be null");
            }
    
            return "Hello, " + name;
        }
    
        @Override
        public int getCount() {
            return count;
        }
    
        @Override
        public void setCount(int value) {
            count = value;
        }
    
        @Override
        public void increment() {
            count++;
        }
    }


# Running

Running the example can be done from maven with a simple 'mvn clean install' command run from the 'resources-jmx-example' directory.

When run you should see output similar to the following.

	-------------------------------------------------------
	 T E S T S
	-------------------------------------------------------
	Running org.superbiz.resource.jmx.JMXTest
	Apr 15, 2015 12:40:09 PM org.jboss.arquillian.container.impl.MapObject populate
	WARNING: Configuration contain properties not supported by the backing object org.apache.tomee.arquillian.remote.RemoteTomEEConfiguration
	Unused property entries: {openejbVersion=${tomee.version}, tomcatVersion=}
	Supported property names: [additionalLibs, httpPort, httpsPort, stopCommand, portRange, conf, debug, exportConfAsSystemProperty, type, unpackWars, version, serverXml, preloadClasses, dir, deployerProperties, stopPort, singleDumpByArchiveName, appWorkingDir, host, cleanOnStartUp, quickSession, ajpPort, artifactId, properties, singleDeploymentByArchiveName, groupId, stopHost, lib, catalina_opts, debugPort, webContextToUseWithEars, simpleLog, removeUnusedWebapps, keepServerXmlAsThis, classifier, bin]
	Apr 15, 2015 12:40:09 PM org.apache.openejb.arquillian.common.Setup findHome
	INFO: Unable to find home in: /Users/jgallimore/tmp/tomee-1.7.x/examples/resources-jmx-example/resources-jmx-ejb/target/apache-tomee-remote
	Apr 15, 2015 12:40:09 PM org.apache.openejb.arquillian.common.MavenCache getArtifact
	INFO: Downloading org.apache.openejb:apache-tomee:7.0.0-SNAPSHOT:zip:plus please wait...
	Apr 15, 2015 12:40:10 PM org.apache.openejb.arquillian.common.Zips unzip
	INFO: Extracting '/Users/jgallimore/.m2/repository/org/apache/openejb/apache-tomee/7.0.0-SNAPSHOT/apache-tomee-7.0.0-SNAPSHOT-plus.zip' to '/Users/jgallimore/tmp/tomee-1.7.x/examples/resources-jmx-example/resources-jmx-ejb/target/apache-tomee-remote'
	Apr 15, 2015 12:40:12 PM org.apache.tomee.arquillian.remote.RemoteTomEEContainer configure
	INFO: Downloaded container to: /Users/jgallimore/tmp/tomee-1.7.x/examples/resources-jmx-example/resources-jmx-ejb/target/apache-tomee-remote/apache-tomee-plus-7.0.0-SNAPSHOT
	Started server process on port: 61309
	objc[20102]: Class JavaLaunchHelper is implemented in both /Library/Java/JavaVirtualMachines/jdk1.7.0_71.jdk/Contents/Home/jre/bin/java and /Library/Java/JavaVirtualMachines/jdk1.7.0_71.jdk/Contents/Home/jre/lib/libinstrument.dylib. One of the two will be used. Which one is undefined.
	Apr 15, 2015 12:40:14 PM org.apache.catalina.startup.VersionLoggerListener log
	INFO: Server version:        Apache Tomcat (TomEE)/7.0.61 (7.0.0-SNAPSHOT)
	Apr 15, 2015 12:40:14 PM org.apache.catalina.startup.VersionLoggerListener log
	INFO: Server built:          Mar 27 2015 12:03:56 UTC
	Apr 15, 2015 12:40:14 PM org.apache.catalina.startup.VersionLoggerListener log
	INFO: Server number:         7.0.61.0
	Apr 15, 2015 12:40:14 PM org.apache.catalina.startup.VersionLoggerListener log
	INFO: OS Name:               Mac OS X
	Apr 15, 2015 12:40:14 PM org.apache.catalina.startup.VersionLoggerListener log
	INFO: OS Version:            10.9.5
	Apr 15, 2015 12:40:14 PM org.apache.catalina.startup.VersionLoggerListener log
	INFO: Architecture:          x86_64
	Apr 15, 2015 12:40:14 PM org.apache.catalina.startup.VersionLoggerListener log
	INFO: Java Home:             /Library/Java/JavaVirtualMachines/jdk1.7.0_71.jdk/Contents/Home/jre
	Apr 15, 2015 12:40:14 PM org.apache.catalina.startup.VersionLoggerListener log
	INFO: JVM Version:           1.7.0_71-b14
	Apr 15, 2015 12:40:14 PM org.apache.catalina.startup.VersionLoggerListener log
	INFO: JVM Vendor:            Oracle Corporation
	Apr 15, 2015 12:40:14 PM org.apache.catalina.startup.VersionLoggerListener log
	INFO: CATALINA_BASE:         /Users/jgallimore/tmp/tomee-1.7.x/examples/resources-jmx-example/resources-jmx-ejb/target/apache-tomee-remote/apache-tomee-plus-7.0.0-SNAPSHOT
	Apr 15, 2015 12:40:14 PM org.apache.catalina.startup.VersionLoggerListener log
	INFO: CATALINA_HOME:         /Users/jgallimore/tmp/tomee-1.7.x/examples/resources-jmx-example/resources-jmx-ejb/target/apache-tomee-remote/apache-tomee-plus-7.0.0-SNAPSHOT
	Apr 15, 2015 12:40:14 PM org.apache.catalina.startup.VersionLoggerListener log
	INFO: Command line argument: -XX:+HeapDumpOnOutOfMemoryError
	Apr 15, 2015 12:40:14 PM org.apache.catalina.startup.VersionLoggerListener log
	INFO: Command line argument: -XX:PermSize=64m
	Apr 15, 2015 12:40:14 PM org.apache.catalina.startup.VersionLoggerListener log
	INFO: Command line argument: -XX:MaxPermSize=256m
	Apr 15, 2015 12:40:14 PM org.apache.catalina.startup.VersionLoggerListener log
	INFO: Command line argument: -Xmx512m
	Apr 15, 2015 12:40:14 PM org.apache.catalina.startup.VersionLoggerListener log
	INFO: Command line argument: -Xms256m
	Apr 15, 2015 12:40:14 PM org.apache.catalina.startup.VersionLoggerListener log
	INFO: Command line argument: -XX:ReservedCodeCacheSize=64m
	Apr 15, 2015 12:40:14 PM org.apache.catalina.startup.VersionLoggerListener log
	INFO: Command line argument: -Dtomee.httpPort=61309
	Apr 15, 2015 12:40:14 PM org.apache.catalina.startup.VersionLoggerListener log
	INFO: Command line argument: -Dorg.apache.catalina.STRICT_SERVLET_COMPLIANCE=false
	Apr 15, 2015 12:40:14 PM org.apache.catalina.startup.VersionLoggerListener log
	INFO: Command line argument: -Dorg.apache.openejb.servlet.filters=org.apache.openejb.arquillian.common.ArquillianFilterRunner=/ArquillianServletRunner
	Apr 15, 2015 12:40:14 PM org.apache.catalina.startup.VersionLoggerListener log
	INFO: Command line argument: -Djava.util.logging.config.file=/Users/jgallimore/tmp/tomee-1.7.x/examples/resources-jmx-example/resources-jmx-ejb/target/apache-tomee-remote/apache-tomee-plus-7.0.0-SNAPSHOT/conf/logging.properties
	Apr 15, 2015 12:40:14 PM org.apache.catalina.startup.VersionLoggerListener log
	INFO: Command line argument: -javaagent:/Users/jgallimore/tmp/tomee-1.7.x/examples/resources-jmx-example/resources-jmx-ejb/target/apache-tomee-remote/apache-tomee-plus-7.0.0-SNAPSHOT/lib/openejb-javaagent.jar
	Apr 15, 2015 12:40:14 PM org.apache.catalina.startup.VersionLoggerListener log
	INFO: Command line argument: -Djava.util.logging.manager=org.apache.juli.ClassLoaderLogManager
	Apr 15, 2015 12:40:14 PM org.apache.catalina.startup.VersionLoggerListener log
	INFO: Command line argument: -Djava.io.tmpdir=/Users/jgallimore/tmp/tomee-1.7.x/examples/resources-jmx-example/resources-jmx-ejb/target/apache-tomee-remote/apache-tomee-plus-7.0.0-SNAPSHOT/temp
	Apr 15, 2015 12:40:14 PM org.apache.catalina.startup.VersionLoggerListener log
	INFO: Command line argument: -Djava.endorsed.dirs=/Users/jgallimore/tmp/tomee-1.7.x/examples/resources-jmx-example/resources-jmx-ejb/target/apache-tomee-remote/apache-tomee-plus-7.0.0-SNAPSHOT/endorsed
	Apr 15, 2015 12:40:14 PM org.apache.catalina.startup.VersionLoggerListener log
	INFO: Command line argument: -Dcatalina.base=/Users/jgallimore/tmp/tomee-1.7.x/examples/resources-jmx-example/resources-jmx-ejb/target/apache-tomee-remote/apache-tomee-plus-7.0.0-SNAPSHOT
	Apr 15, 2015 12:40:14 PM org.apache.catalina.startup.VersionLoggerListener log
	INFO: Command line argument: -Dcatalina.home=/Users/jgallimore/tmp/tomee-1.7.x/examples/resources-jmx-example/resources-jmx-ejb/target/apache-tomee-remote/apache-tomee-plus-7.0.0-SNAPSHOT
	Apr 15, 2015 12:40:14 PM org.apache.catalina.startup.VersionLoggerListener log
	INFO: Command line argument: -Dcatalina.ext.dirs=/Users/jgallimore/tmp/tomee-1.7.x/examples/resources-jmx-example/resources-jmx-ejb/target/apache-tomee-remote/apache-tomee-plus-7.0.0-SNAPSHOT/lib
	Apr 15, 2015 12:40:14 PM org.apache.catalina.startup.VersionLoggerListener log
	INFO: Command line argument: -Dorg.apache.tomcat.util.http.ServerCookie.ALLOW_HTTP_SEPARATORS_IN_V0=true
	Apr 15, 2015 12:40:14 PM org.apache.catalina.startup.VersionLoggerListener log
	INFO: Command line argument: -ea
	Apr 15, 2015 12:40:14 PM org.apache.catalina.core.AprLifecycleListener lifecycleEvent
	INFO: The APR based Apache Tomcat Native library which allows optimal performance in production environments was not found on the java.library.path: /Users/jgallimore/Library/Java/Extensions:/Library/Java/Extensions:/Network/Library/Java/Extensions:/System/Library/Java/Extensions:/usr/lib/java:.
	Apr 15, 2015 12:40:14 PM org.apache.coyote.AbstractProtocol init
	INFO: Initializing ProtocolHandler ["http-bio-61309"]
	Apr 15, 2015 12:40:14 PM org.apache.coyote.AbstractProtocol init
	INFO: Initializing ProtocolHandler ["ajp-bio-8009"]
	Apr 15, 2015 12:40:16 PM org.apache.openejb.util.OptionsLog info
	INFO: Using 'openejb.jdbc.datasource-creator=org.apache.tomee.jdbc.TomEEDataSourceCreator'
	Apr 15, 2015 12:40:16 PM org.apache.openejb.OpenEJB$Instance <init>
	INFO: ********************************************************************************
	Apr 15, 2015 12:40:16 PM org.apache.openejb.OpenEJB$Instance <init>
	INFO: OpenEJB http://tomee.apache.org/
	Apr 15, 2015 12:40:16 PM org.apache.openejb.OpenEJB$Instance <init>
	INFO: Startup: Wed Apr 15 12:40:16 BST 2015
	Apr 15, 2015 12:40:16 PM org.apache.openejb.OpenEJB$Instance <init>
	INFO: Copyright 1999-2013 (C) Apache OpenEJB Project, All Rights Reserved.
	Apr 15, 2015 12:40:16 PM org.apache.openejb.OpenEJB$Instance <init>
	INFO: Version: 7.0.0-SNAPSHOT
	Apr 15, 2015 12:40:16 PM org.apache.openejb.OpenEJB$Instance <init>
	INFO: Build date: 20150415
	Apr 15, 2015 12:40:16 PM org.apache.openejb.OpenEJB$Instance <init>
	INFO: Build time: 11:37
	Apr 15, 2015 12:40:16 PM org.apache.openejb.OpenEJB$Instance <init>
	INFO: ********************************************************************************
	Apr 15, 2015 12:40:16 PM org.apache.openejb.OpenEJB$Instance <init>
	INFO: openejb.home = /Users/jgallimore/tmp/tomee-1.7.x/examples/resources-jmx-example/resources-jmx-ejb/target/apache-tomee-remote/apache-tomee-plus-7.0.0-SNAPSHOT
	Apr 15, 2015 12:40:16 PM org.apache.openejb.OpenEJB$Instance <init>
	INFO: openejb.base = /Users/jgallimore/tmp/tomee-1.7.x/examples/resources-jmx-example/resources-jmx-ejb/target/apache-tomee-remote/apache-tomee-plus-7.0.0-SNAPSHOT
	Apr 15, 2015 12:40:16 PM org.apache.openejb.cdi.CdiBuilder initializeOWB
	INFO: Created new singletonService org.apache.openejb.cdi.ThreadSingletonServiceImpl@4a00b74b
	Apr 15, 2015 12:40:16 PM org.apache.openejb.cdi.CdiBuilder initializeOWB
	INFO: Succeeded in installing singleton service
	Apr 15, 2015 12:40:17 PM org.apache.openejb.config.ConfigurationFactory init
	INFO: openejb configuration file is '/Users/jgallimore/tmp/tomee-1.7.x/examples/resources-jmx-example/resources-jmx-ejb/target/apache-tomee-remote/apache-tomee-plus-7.0.0-SNAPSHOT/conf/tomee.xml'
	Apr 15, 2015 12:40:17 PM org.apache.openejb.config.ConfigurationFactory configureService
	INFO: Configuring Service(id=Tomcat Security Service, type=SecurityService, provider-id=Tomcat Security Service)
	Apr 15, 2015 12:40:17 PM org.apache.openejb.config.ConfigurationFactory configureService
	INFO: Configuring Service(id=Default Transaction Manager, type=TransactionManager, provider-id=Default Transaction Manager)
	Apr 15, 2015 12:40:17 PM org.apache.openejb.util.OptionsLog info
	INFO: Using 'openejb.system.apps=true'
	Apr 15, 2015 12:40:17 PM org.apache.openejb.config.ConfigurationFactory configureApplication
	INFO: Configuring enterprise application: openejb
	Apr 15, 2015 12:40:17 PM org.apache.openejb.config.InitEjbDeployments deploy
	INFO: Using openejb.deploymentId.format '{ejbName}'
	Apr 15, 2015 12:40:17 PM org.apache.openejb.config.InitEjbDeployments deploy
	INFO: Auto-deploying ejb openejb/Deployer: EjbDeployment(deployment-id=openejb/Deployer)
	Apr 15, 2015 12:40:17 PM org.apache.openejb.config.InitEjbDeployments deploy
	INFO: Auto-deploying ejb openejb/ConfigurationInfo: EjbDeployment(deployment-id=openejb/ConfigurationInfo)
	Apr 15, 2015 12:40:18 PM org.apache.openejb.config.InitEjbDeployments deploy
	INFO: Auto-deploying ejb MEJB: EjbDeployment(deployment-id=MEJB)
	Apr 15, 2015 12:40:18 PM org.apache.openejb.config.ConfigurationFactory configureService
	INFO: Configuring Service(id=Default Stateless Container, type=Container, provider-id=Default Stateless Container)
	Apr 15, 2015 12:40:18 PM org.apache.openejb.config.AutoConfig createContainer
	INFO: Auto-creating a container for bean openejb/Deployer: Container(type=STATELESS, id=Default Stateless Container)
	Apr 15, 2015 12:40:18 PM org.apache.openejb.config.AppInfoBuilder build
	INFO: Enterprise application "openejb" loaded.
	Apr 15, 2015 12:40:18 PM org.apache.openejb.assembler.classic.Assembler createRecipe
	INFO: Creating TransactionManager(id=Default Transaction Manager)
	Apr 15, 2015 12:40:18 PM org.apache.openejb.assembler.classic.Assembler createRecipe
	INFO: Creating SecurityService(id=Tomcat Security Service)
	Apr 15, 2015 12:40:18 PM org.apache.openejb.assembler.classic.Assembler createRecipe
	INFO: Creating Container(id=Default Stateless Container)
	Apr 15, 2015 12:40:18 PM org.apache.openejb.assembler.classic.Assembler createAppClassLoader
	INFO: Not creating another application classloader for openejb
	Apr 15, 2015 12:40:18 PM org.apache.openejb.assembler.classic.Assembler createApplication
	INFO: Assembling app: openejb
	Apr 15, 2015 12:40:18 PM org.apache.openejb.util.OptionsLog info
	INFO: Using 'openejb.jndiname.format={deploymentId}{interfaceType.openejbLegacyName}'
	Apr 15, 2015 12:40:18 PM org.apache.openejb.assembler.classic.JndiBuilder bind
	INFO: Jndi(name=openejb/DeployerBusinessRemote) --> Ejb(deployment-id=openejb/Deployer)
	Apr 15, 2015 12:40:18 PM org.apache.openejb.assembler.classic.JndiBuilder bind
	INFO: Jndi(name=global/openejb/openejb/Deployer!org.apache.openejb.assembler.Deployer) --> Ejb(deployment-id=openejb/Deployer)
	Apr 15, 2015 12:40:18 PM org.apache.openejb.assembler.classic.JndiBuilder bind
	INFO: Jndi(name=global/openejb/openejb/Deployer) --> Ejb(deployment-id=openejb/Deployer)
	Apr 15, 2015 12:40:18 PM org.apache.openejb.assembler.classic.JndiBuilder bind
	INFO: Jndi(name=openejb/ConfigurationInfoBusinessRemote) --> Ejb(deployment-id=openejb/ConfigurationInfo)
	Apr 15, 2015 12:40:18 PM org.apache.openejb.assembler.classic.JndiBuilder bind
	INFO: Jndi(name=global/openejb/openejb/ConfigurationInfo!org.apache.openejb.assembler.classic.cmd.ConfigurationInfo) --> Ejb(deployment-id=openejb/ConfigurationInfo)
	Apr 15, 2015 12:40:18 PM org.apache.openejb.assembler.classic.JndiBuilder bind
	INFO: Jndi(name=global/openejb/openejb/ConfigurationInfo) --> Ejb(deployment-id=openejb/ConfigurationInfo)
	Apr 15, 2015 12:40:19 PM org.apache.openejb.assembler.classic.JndiBuilder bind
	INFO: Jndi(name=MEJB) --> Ejb(deployment-id=MEJB)
	Apr 15, 2015 12:40:19 PM org.apache.openejb.assembler.classic.JndiBuilder bind
	INFO: Jndi(name=global/openejb/MEJB!javax.management.j2ee.ManagementHome) --> Ejb(deployment-id=MEJB)
	Apr 15, 2015 12:40:19 PM org.apache.openejb.assembler.classic.JndiBuilder bind
	INFO: Jndi(name=global/openejb/MEJB) --> Ejb(deployment-id=MEJB)
	Apr 15, 2015 12:40:19 PM org.apache.openejb.assembler.classic.Assembler startEjbs
	INFO: Created Ejb(deployment-id=openejb/Deployer, ejb-name=openejb/Deployer, container=Default Stateless Container)
	Apr 15, 2015 12:40:19 PM org.apache.openejb.assembler.classic.Assembler startEjbs
	INFO: Created Ejb(deployment-id=MEJB, ejb-name=MEJB, container=Default Stateless Container)
	Apr 15, 2015 12:40:19 PM org.apache.openejb.assembler.classic.Assembler startEjbs
	INFO: Created Ejb(deployment-id=openejb/ConfigurationInfo, ejb-name=openejb/ConfigurationInfo, container=Default Stateless Container)
	Apr 15, 2015 12:40:19 PM org.apache.openejb.assembler.classic.Assembler startEjbs
	INFO: Started Ejb(deployment-id=openejb/Deployer, ejb-name=openejb/Deployer, container=Default Stateless Container)
	Apr 15, 2015 12:40:19 PM org.apache.openejb.assembler.classic.Assembler startEjbs
	INFO: Started Ejb(deployment-id=MEJB, ejb-name=MEJB, container=Default Stateless Container)
	Apr 15, 2015 12:40:19 PM org.apache.openejb.assembler.classic.Assembler startEjbs
	INFO: Started Ejb(deployment-id=openejb/ConfigurationInfo, ejb-name=openejb/ConfigurationInfo, container=Default Stateless Container)
	Apr 15, 2015 12:40:19 PM org.apache.openejb.assembler.classic.Assembler deployMBean
	INFO: Deployed MBean(openejb.user.mbeans:application=openejb,group=org.apache.openejb.assembler.monitoring,name=JMXDeployer)
	Apr 15, 2015 12:40:19 PM org.apache.openejb.assembler.classic.Assembler createApplication
	INFO: Deployed Application(path=openejb)
	Apr 15, 2015 12:40:20 PM org.apache.openejb.server.ServiceManager initServer
	INFO: Creating ServerService(id=cxf)
	Apr 15, 2015 12:40:20 PM org.apache.openejb.server.ServiceManager initServer
	INFO: Creating ServerService(id=cxf-rs)
	Apr 15, 2015 12:40:20 PM org.apache.openejb.server.SimpleServiceManager start
	INFO:   ** Bound Services **
	Apr 15, 2015 12:40:20 PM org.apache.openejb.server.SimpleServiceManager printRow
	INFO:   NAME                 IP              PORT  
	Apr 15, 2015 12:40:20 PM org.apache.openejb.server.SimpleServiceManager start
	INFO: -------
	Apr 15, 2015 12:40:20 PM org.apache.openejb.server.SimpleServiceManager start
	INFO: Ready!
	Apr 15, 2015 12:40:20 PM org.apache.catalina.startup.Catalina load
	INFO: Initialization processed in 7621 ms
	Apr 15, 2015 12:40:20 PM org.apache.tomee.catalina.OpenEJBNamingContextListener bindResource
	INFO: Importing a Tomcat Resource with id 'UserDatabase' of type 'org.apache.catalina.UserDatabase'.
	Apr 15, 2015 12:40:20 PM org.apache.openejb.assembler.classic.Assembler createRecipe
	INFO: Creating Resource(id=UserDatabase)
	Apr 15, 2015 12:40:20 PM org.apache.catalina.core.StandardService startInternal
	INFO: Starting service Catalina
	Apr 15, 2015 12:40:20 PM org.apache.catalina.core.StandardEngine startInternal
	INFO: Starting Servlet Engine: Apache Tomcat (TomEE)/7.0.61 (7.0.0-SNAPSHOT)
	Apr 15, 2015 12:40:21 PM org.apache.coyote.AbstractProtocol start
	INFO: Starting ProtocolHandler ["http-bio-61309"]
	Apr 15, 2015 12:40:21 PM org.apache.coyote.AbstractProtocol start
	INFO: Starting ProtocolHandler ["ajp-bio-8009"]
	Apr 15, 2015 12:40:21 PM org.apache.catalina.startup.Catalina start
	INFO: Server startup in 247 ms
	Apr 15, 2015 12:40:21 PM org.apache.openejb.client.EventLogger log
	INFO: RemoteInitialContextCreated{providerUri=http://localhost:61309/tomee/ejb}
	Apr 15, 2015 12:40:21 PM org.apache.openejb.util.JarExtractor extract
	INFO: Extracting jar: /Users/jgallimore/tmp/tomee-1.7.x/examples/resources-jmx-example/resources-jmx-ejb/target/arquillian-test-working-dir/0/jmx.ear
	Apr 15, 2015 12:40:21 PM org.apache.openejb.util.JarExtractor extract
	INFO: Extracted path: /Users/jgallimore/tmp/tomee-1.7.x/examples/resources-jmx-example/resources-jmx-ejb/target/arquillian-test-working-dir/0/jmx
	Apr 15, 2015 12:40:21 PM org.apache.openejb.util.JarExtractor extract
	INFO: Extracting jar: /Users/jgallimore/tmp/tomee-1.7.x/examples/resources-jmx-example/resources-jmx-ejb/target/arquillian-test-working-dir/0/jmx/arquillian-protocol.war
	Apr 15, 2015 12:40:21 PM org.apache.openejb.util.JarExtractor extract
	INFO: Extracted path: /Users/jgallimore/tmp/tomee-1.7.x/examples/resources-jmx-example/resources-jmx-ejb/target/arquillian-test-working-dir/0/jmx/arquillian-protocol
	Apr 15, 2015 12:40:21 PM org.apache.openejb.util.OptionsLog info
	INFO: Using 'openejb.deployments.classpath.filter.systemapps=false'
	Apr 15, 2015 12:40:23 PM org.apache.openejb.util.OptionsLog info
	INFO: Using 'openejb.default.deployment-module=org.apache.openejb.config.WebModule'
	Apr 15, 2015 12:40:23 PM org.apache.openejb.util.OptionsLog info
	INFO: Using 'openejb.default.deployment-module=org.apache.openejb.config.WebModule'
	Apr 15, 2015 12:40:23 PM org.apache.openejb.config.DeploymentsResolver processUrls
	INFO: Found EjbModule in classpath: /Users/jgallimore/tmp/tomee-1.7.x/examples/resources-jmx-example/resources-jmx-ejb/target/arquillian-test-working-dir/0/jmx/jmx-ejb.jar
	Apr 15, 2015 12:40:23 PM org.apache.openejb.util.OptionsLog info
	INFO: Using 'openejb.default.deployment-module=org.apache.openejb.config.WebModule'
	Apr 15, 2015 12:40:23 PM org.apache.openejb.util.OptionsLog info
	INFO: Using 'openejb.default.deployment-module=org.apache.openejb.config.WebModule'
	Apr 15, 2015 12:40:23 PM org.apache.openejb.util.OptionsLog info
	INFO: Using 'openejb.default.deployment-module=org.apache.openejb.config.WebModule'
	Apr 15, 2015 12:40:23 PM org.apache.openejb.config.DeploymentsResolver loadFromClasspath
	INFO: Searched 6 classpath urls in 1605 milliseconds.  Average 267 milliseconds per url.
	Apr 15, 2015 12:40:23 PM org.apache.openejb.config.ConfigurationFactory configureApplication
	INFO: Configuring enterprise application: /Users/jgallimore/tmp/tomee-1.7.x/examples/resources-jmx-example/resources-jmx-ejb/target/arquillian-test-working-dir/0/jmx
	Apr 15, 2015 12:40:24 PM org.apache.openejb.config.InitEjbDeployments deploy
	INFO: Auto-deploying ejb TestEjb: EjbDeployment(deployment-id=TestEjb)
	Apr 15, 2015 12:40:24 PM org.apache.openejb.config.ConfigurationFactory configureService
	INFO: Configuring Service(id=jmx/Hello, type=Resource, provider-id=jmx/Hello)
	Apr 15, 2015 12:40:24 PM org.apache.openejb.config.ConfigurationFactory configureService
	INFO: Configuring Service(id=Default Managed Container, type=Container, provider-id=Default Managed Container)
	Apr 15, 2015 12:40:24 PM org.apache.openejb.config.AutoConfig createContainer
	INFO: Auto-creating a container for bean jmx-ejb.Comp1256115069: Container(type=MANAGED, id=Default Managed Container)
	Apr 15, 2015 12:40:24 PM org.apache.openejb.assembler.classic.Assembler createRecipe
	INFO: Creating Container(id=Default Managed Container)
	Apr 15, 2015 12:40:24 PM org.apache.openejb.core.managed.SimplePassivater init
	INFO: Using directory /Users/jgallimore/tmp/tomee-1.7.x/examples/resources-jmx-example/resources-jmx-ejb/target/apache-tomee-remote/apache-tomee-plus-7.0.0-SNAPSHOT/temp for stateful session passivation
	Apr 15, 2015 12:40:24 PM org.apache.openejb.config.AutoConfig processResourceRef
	INFO: Auto-linking resource-ref 'java:comp/env/jmx/Hello' in bean jmx-ejb.Comp1256115069 to Resource(id=jmx/Hello)
	Apr 15, 2015 12:40:24 PM org.apache.openejb.config.AutoConfig processResourceRef
	INFO: Auto-linking resource-ref 'openejb/Resource/jmx/Hello' in bean jmx-ejb.Comp1256115069 to Resource(id=Hello)
	Apr 15, 2015 12:40:24 PM org.apache.openejb.config.AutoConfig processResourceRef
	INFO: Auto-linking resource-ref 'openejb/Resource/Hello' in bean jmx-ejb.Comp1256115069 to Resource(id=Hello)
	Apr 15, 2015 12:40:24 PM org.apache.openejb.config.ConfigurationFactory configureService
	INFO: Configuring Service(id=Default Singleton Container, type=Container, provider-id=Default Singleton Container)
	Apr 15, 2015 12:40:24 PM org.apache.openejb.config.AutoConfig createContainer
	INFO: Auto-creating a container for bean TestEjb: Container(type=SINGLETON, id=Default Singleton Container)
	Apr 15, 2015 12:40:24 PM org.apache.openejb.assembler.classic.Assembler createRecipe
	INFO: Creating Container(id=Default Singleton Container)
	Apr 15, 2015 12:40:24 PM org.apache.openejb.config.AutoConfig processResourceRef
	INFO: Auto-linking resource-ref 'java:comp/env/jmx/Hello' in bean TestEjb to Resource(id=jmx/Hello)
	Apr 15, 2015 12:40:24 PM org.apache.openejb.config.AutoConfig processResourceRef
	INFO: Auto-linking resource-ref 'openejb/Resource/jmx/Hello' in bean TestEjb to Resource(id=Hello)
	Apr 15, 2015 12:40:24 PM org.apache.openejb.config.AutoConfig processResourceRef
	INFO: Auto-linking resource-ref 'openejb/Resource/Hello' in bean TestEjb to Resource(id=Hello)
	Apr 15, 2015 12:40:24 PM org.apache.openejb.config.AutoConfig processResourceRef
	INFO: Auto-linking resource-ref 'openejb/Resource/jmx/Hello' in bean jmx_org.superbiz.resource.jmx.JMXTest to Resource(id=Hello)
	Apr 15, 2015 12:40:24 PM org.apache.openejb.config.AutoConfig processResourceRef
	INFO: Auto-linking resource-ref 'openejb/Resource/Hello' in bean jmx_org.superbiz.resource.jmx.JMXTest to Resource(id=Hello)
	Apr 15, 2015 12:40:24 PM org.apache.openejb.config.AppInfoBuilder build
	INFO: Enterprise application "/Users/jgallimore/tmp/tomee-1.7.x/examples/resources-jmx-example/resources-jmx-ejb/target/arquillian-test-working-dir/0/jmx" loaded.
	Apr 15, 2015 12:40:24 PM org.apache.openejb.assembler.classic.Assembler createAppClassLoader
	INFO: Creating dedicated application classloader for jmx
	Apr 15, 2015 12:40:24 PM org.apache.openejb.assembler.classic.Assembler createApplication
	INFO: Assembling app: /Users/jgallimore/tmp/tomee-1.7.x/examples/resources-jmx-example/resources-jmx-ejb/target/arquillian-test-working-dir/0/jmx
	Apr 15, 2015 12:40:24 PM org.apache.openejb.assembler.classic.JndiBuilder bind
	INFO: Jndi(name=TestEjbLocalBean) --> Ejb(deployment-id=TestEjb)
	Apr 15, 2015 12:40:24 PM org.apache.openejb.assembler.classic.JndiBuilder bind
	INFO: Jndi(name=global/jmx/jmx-ejb/TestEjb!org.superbiz.resource.jmx.JMXTest$TestEjb) --> Ejb(deployment-id=TestEjb)
	Apr 15, 2015 12:40:24 PM org.apache.openejb.assembler.classic.JndiBuilder bind
	INFO: Jndi(name=global/jmx/jmx-ejb/TestEjb) --> Ejb(deployment-id=TestEjb)
	Apr 15, 2015 12:40:24 PM org.apache.openejb.cdi.CdiBuilder initSingleton
	INFO: Existing thread singleton service in SystemInstance(): org.apache.openejb.cdi.ThreadSingletonServiceImpl@4a00b74b
	Apr 15, 2015 12:40:24 PM org.apache.openejb.cdi.OpenEJBLifecycle startApplication
	INFO: OpenWebBeans Container is starting...
	Apr 15, 2015 12:40:24 PM org.apache.webbeans.plugins.PluginLoader startUp
	INFO: Adding OpenWebBeansPlugin : [CdiPlugin]
	Apr 15, 2015 12:40:24 PM org.apache.webbeans.plugins.PluginLoader startUp
	Apr 15, 2015 12:40:25 PM org.apache.webbeans.config.BeansDeployer validateInjectionPoints
	INFO: All injection points were validated successfully.
	Apr 15, 2015 12:40:25 PM org.apache.openejb.cdi.OpenEJBLifecycle startApplication
	INFO: OpenWebBeans Container has started, it took 186 ms.
	Apr 15, 2015 12:40:25 PM org.apache.openejb.assembler.classic.Assembler startEjbs
	INFO: Created Ejb(deployment-id=TestEjb, ejb-name=TestEjb, container=Default Singleton Container)
	Apr 15, 2015 12:40:25 PM org.apache.openejb.assembler.classic.Assembler startEjbs
	INFO: Started Ejb(deployment-id=TestEjb, ejb-name=TestEjb, container=Default Singleton Container)
	Apr 15, 2015 12:40:25 PM org.apache.tomee.catalina.TomcatWebAppBuilder deployWebApps
	INFO: using default host: localhost
	Apr 15, 2015 12:40:25 PM org.apache.tomee.catalina.TomcatWebAppBuilder init
	INFO: ------------------------- localhost -> /arquillian-protocol
	Apr 15, 2015 12:40:25 PM org.apache.openejb.util.OptionsLog info
	INFO: Using 'openejb.session.manager=org.apache.tomee.catalina.session.QuickSessionManager'
	Apr 15, 2015 12:40:25 PM org.apache.openejb.cdi.CdiBuilder initSingleton
	INFO: Existing thread singleton service in SystemInstance(): org.apache.openejb.cdi.ThreadSingletonServiceImpl@4a00b74b
	Apr 15, 2015 12:40:25 PM org.apache.openejb.cdi.OpenEJBLifecycle startApplication
	INFO: OpenWebBeans Container is starting...
	Apr 15, 2015 12:40:25 PM org.apache.webbeans.plugins.PluginLoader startUp
	INFO: Adding OpenWebBeansPlugin : [CdiPlugin]
	Apr 15, 2015 12:40:25 PM org.apache.webbeans.plugins.PluginLoader startUp
	Apr 15, 2015 12:40:25 PM org.apache.webbeans.config.BeansDeployer validateInjectionPoints
	INFO: All injection points were validated successfully.
	Apr 15, 2015 12:40:25 PM org.apache.openejb.cdi.OpenEJBLifecycle startApplication
	INFO: OpenWebBeans Container has started, it took 17 ms.
	Apr 15, 2015 12:40:25 PM org.apache.openejb.assembler.classic.Assembler createRecipe
	INFO: Creating Resource(id=jmx/Hello, aliases=Hello)
	Apr 15, 2015 12:40:25 PM org.superbiz.resource.jmx.factory.JMXBeanCreator create
	INFO: Unable to set value 12345 on field count
	Apr 15, 2015 12:40:25 PM org.apache.openejb.assembler.classic.Assembler logUnusedProperties
	WARNING: Property "code" not supported by "jmx/Hello"
	Apr 15, 2015 12:40:25 PM org.apache.openejb.assembler.classic.Assembler logUnusedProperties
	WARNING: Property "name" not supported by "jmx/Hello"
	Apr 15, 2015 12:40:25 PM org.apache.openejb.assembler.classic.Assembler logUnusedProperties
	WARNING: Property "count" not supported by "jmx/Hello"
	Apr 15, 2015 12:40:25 PM org.apache.openejb.assembler.classic.Assembler createApplication
	INFO: Deployed Application(path=/Users/jgallimore/tmp/tomee-1.7.x/examples/resources-jmx-example/resources-jmx-ejb/target/arquillian-test-working-dir/0/jmx)
	Apr 15, 2015 12:40:26 PM org.apache.openejb.client.EventLogger log
	INFO: RemoteInitialContextCreated{providerUri=http://localhost:61309/tomee/ejb}
	Apr 15, 2015 12:40:26 PM org.apache.openejb.assembler.classic.Assembler destroyApplication
	INFO: Undeploying app: /Users/jgallimore/tmp/tomee-1.7.x/examples/resources-jmx-example/resources-jmx-ejb/target/arquillian-test-working-dir/0/jmx
	Apr 15, 2015 12:40:27 PM org.apache.openejb.arquillian.common.TomEEContainer undeploy
	INFO: cleaning /Users/jgallimore/tmp/tomee-1.7.x/examples/resources-jmx-example/resources-jmx-ejb/target/arquillian-test-working-dir/0/jmx.ear
	Apr 15, 2015 12:40:27 PM org.apache.openejb.arquillian.common.TomEEContainer undeploy
	INFO: cleaning /Users/jgallimore/tmp/tomee-1.7.x/examples/resources-jmx-example/resources-jmx-ejb/target/arquillian-test-working-dir/0/jmx
	Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 18.464 sec
	Apr 15, 2015 12:40:27 PM org.apache.catalina.core.StandardServer await
	INFO: A valid shutdown command was received via the shutdown port. Stopping the Server instance.
	Apr 15, 2015 12:40:27 PM org.apache.coyote.AbstractProtocol pause
	INFO: Pausing ProtocolHandler ["http-bio-61309"]
	Apr 15, 2015 12:40:27 PM org.apache.coyote.AbstractProtocol pause
	INFO: Pausing ProtocolHandler ["ajp-bio-8009"]
	Apr 15, 2015 12:40:27 PM org.apache.catalina.core.StandardService stopInternal
	INFO: Stopping service Catalina
	Apr 15, 2015 12:40:27 PM org.apache.coyote.AbstractProtocol stop
	INFO: Stopping ProtocolHandler ["http-bio-61309"]
	Apr 15, 2015 12:40:27 PM org.apache.coyote.AbstractProtocol stop
	INFO: Stopping ProtocolHandler ["ajp-bio-8009"]
	Apr 15, 2015 12:40:27 PM org.apache.openejb.server.SimpleServiceManager stop
	INFO: Stopping server services
	Apr 15, 2015 12:40:27 PM org.apache.openejb.assembler.classic.Assembler destroyApplication
	INFO: Undeploying app: openejb
	Apr 15, 2015 12:40:27 PM org.apache.coyote.AbstractProtocol destroy
	INFO: Destroying ProtocolHandler ["http-bio-61309"]
	Apr 15, 2015 12:40:27 PM org.apache.coyote.AbstractProtocol destroy
	INFO: Destroying ProtocolHandler ["ajp-bio-8009"]

	Results :

	Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
    

Note the following lines showing the creation of the resource.

	Apr 15, 2015 12:40:24 PM org.apache.openejb.config.ConfigurationFactory configureService
	INFO: Configuring Service(id=jmx/Hello, type=Resource, provider-id=jmx/Hello)
	
