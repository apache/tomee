Title: Injection Of Env Entry

*Help us document this example! Source available in [svn](http://svn.apache.org/repos/asf/openejb/trunk/openejb/examples/injection-of-env-entry) or [git](https://github.com/apache/openejb/tree/trunk/openejb/examples/injection-of-env-entry). Open a [JIRA](https://issues.apache.org/jira/browse/TOMEE) with patch or pull request*

## Configuration

    package org.superbiz.injection.enventry;
    
    import javax.annotation.Resource;
    import javax.ejb.Singleton;
    import java.util.Date;
    
    /**
     * This example demostrates the use of the injection of environment entries
     * using <b>Resource</b> annotation.
     * <p/>
     * "EJB Core Contracts and Requirements" specification section 16.4.1.1.
     *
     * @version $Rev: 1090807 $ $Date: 2011-04-10 07:12:31 -0700 (Sun, 10 Apr 2011) $
     */
    //START SNIPPET: code
    @Singleton
    public class Configuration {
    
        @Resource
        private String color;
    
        @Resource
        private Shape shape;
    
        @Resource
        private Class strategy;
    
        @Resource(name = "date")
        private long date;
    
        public String getColor() {
            return color;
        }
    
        public Shape getShape() {
            return shape;
        }
    
        public Class getStrategy() {
            return strategy;
        }
    
        public Date getDate() {
            return new Date(date);
        }
    }

## Shape

    package org.superbiz.injection.enventry;
    
    /**
     * @version $Revision$ $Date$
     */
    public enum Shape {
    
        CIRCLE,
        TRIANGLE,
        SQUARE
    }

## Widget

    package org.superbiz.injection.enventry;
    
    /**
     * Exists to show that any class object can be injected and does
     * not need to be loaded directly in app code.
     *
     * @version $Revision$ $Date$
     */
    public class Widget {
    }

## ejb-jar.xml

    <ejb-jar xmlns="http://java.sun.com/xml/ns/javaee" version="3.0" metadata-complete="false">
      <enterprise-beans>
        <session>
          <ejb-name>Configuration</ejb-name>
          <env-entry>
            <env-entry-name>org.superbiz.injection.enventry.Configuration/color</env-entry-name>
            <env-entry-type>java.lang.String</env-entry-type>
            <env-entry-value>orange</env-entry-value>
          </env-entry>
          <env-entry>
            <env-entry-name>org.superbiz.injection.enventry.Configuration/shape</env-entry-name>
            <env-entry-type>org.superbiz.injection.enventry.Shape</env-entry-type>
            <env-entry-value>TRIANGLE</env-entry-value>
          </env-entry>
          <env-entry>
            <env-entry-name>org.superbiz.injection.enventry.Configuration/strategy</env-entry-name>
            <env-entry-type>java.lang.Class</env-entry-type>
            <env-entry-value>org.superbiz.injection.enventry.Widget</env-entry-value>
          </env-entry>
          <env-entry>
            <description>The name was explicitly set in the annotation so the classname prefix isn't required</description>
            <env-entry-name>date</env-entry-name>
            <env-entry-type>java.lang.Long</env-entry-type>
            <env-entry-value>123456789</env-entry-value>
          </env-entry>
        </session>
      </enterprise-beans>
    </ejb-jar>
        <!-- END SNIPPET: code -->
    

## ConfigurationTest

    package org.superbiz.injection.enventry;
    
    import junit.framework.TestCase;
    
    import javax.ejb.embeddable.EJBContainer;
    import javax.naming.Context;
    import java.util.Date;
    
    //START SNIPPET: code
    public class ConfigurationTest extends TestCase {
    
    
        public void test() throws Exception {
            final Context context = EJBContainer.createEJBContainer().getContext();
    
            final Configuration configuration = (Configuration) context.lookup("java:global/injection-of-env-entry/Configuration");
    
            assertEquals("orange", configuration.getColor());
    
            assertEquals(Shape.TRIANGLE, configuration.getShape());
    
            assertEquals(Widget.class, configuration.getStrategy());
    
            assertEquals(new Date(123456789), configuration.getDate());
        }
    }

# Running

    
    -------------------------------------------------------
     T E S T S
    -------------------------------------------------------
    Running org.superbiz.injection.enventry.ConfigurationTest
    Apache OpenEJB 4.0.0-beta-1    build: 20111002-04:06
    http://openejb.apache.org/
    INFO - openejb.home = /Users/dblevins/examples/injection-of-env-entry
    INFO - openejb.base = /Users/dblevins/examples/injection-of-env-entry
    INFO - Using 'javax.ejb.embeddable.EJBContainer=true'
    INFO - Configuring Service(id=Default Security Service, type=SecurityService, provider-id=Default Security Service)
    INFO - Configuring Service(id=Default Transaction Manager, type=TransactionManager, provider-id=Default Transaction Manager)
    INFO - Found EjbModule in classpath: /Users/dblevins/examples/injection-of-env-entry/target/classes
    INFO - Beginning load: /Users/dblevins/examples/injection-of-env-entry/target/classes
    INFO - Configuring enterprise application: /Users/dblevins/examples/injection-of-env-entry
    WARN - Method 'lookup' is not available for 'javax.annotation.Resource'. Probably using an older Runtime.
    INFO - Configuring Service(id=Default Singleton Container, type=Container, provider-id=Default Singleton Container)
    INFO - Auto-creating a container for bean Configuration: Container(type=SINGLETON, id=Default Singleton Container)
    INFO - Configuring Service(id=Default Managed Container, type=Container, provider-id=Default Managed Container)
    INFO - Auto-creating a container for bean org.superbiz.injection.enventry.ConfigurationTest: Container(type=MANAGED, id=Default Managed Container)
    INFO - Enterprise application "/Users/dblevins/examples/injection-of-env-entry" loaded.
    INFO - Assembling app: /Users/dblevins/examples/injection-of-env-entry
    INFO - Jndi(name="java:global/injection-of-env-entry/Configuration!org.superbiz.injection.enventry.Configuration")
    INFO - Jndi(name="java:global/injection-of-env-entry/Configuration")
    INFO - Jndi(name="java:global/EjbModule1355224018/org.superbiz.injection.enventry.ConfigurationTest!org.superbiz.injection.enventry.ConfigurationTest")
    INFO - Jndi(name="java:global/EjbModule1355224018/org.superbiz.injection.enventry.ConfigurationTest")
    INFO - Created Ejb(deployment-id=org.superbiz.injection.enventry.ConfigurationTest, ejb-name=org.superbiz.injection.enventry.ConfigurationTest, container=Default Managed Container)
    INFO - Created Ejb(deployment-id=Configuration, ejb-name=Configuration, container=Default Singleton Container)
    INFO - Started Ejb(deployment-id=org.superbiz.injection.enventry.ConfigurationTest, ejb-name=org.superbiz.injection.enventry.ConfigurationTest, container=Default Managed Container)
    INFO - Started Ejb(deployment-id=Configuration, ejb-name=Configuration, container=Default Singleton Container)
    INFO - Deployed Application(path=/Users/dblevins/examples/injection-of-env-entry)
    Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.664 sec
    
    Results :
    
    Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
    
