index-group=Unrevised
type=page
status=published
title=Mbean Auto Registration
~~~~~~

This example shows how to automatically create and register mbeans using TomEE features.

# Dependencies

To be able to use it you need to import the mbean api (annotations):

    <dependency>
      <groupId>org.apache.openejb</groupId>
      <artifactId>mbean-annotation-api</artifactId>
      <version>4.5.0</version>
      <scope>provided</scope>
    </dependency>

# The MBean

The mbean implements a simple game where the goal is to guess a number.

It allows the user to change the value too.

    package org.superbiz.mbean;

    import javax.management.Description;
    import javax.management.MBean;
    import javax.management.ManagedAttribute;
    import javax.management.ManagedOperation;

    @MBean
    @Description("play with me to guess a number")
    public class GuessHowManyMBean {
        private int value = 0;

        @ManagedAttribute
        @Description("you are cheating!")
        public int getValue() {
            return value;
        }

        @ManagedAttribute
        public void setValue(int value) {
            this.value = value;
        }

        @ManagedOperation
        public String tryValue(int userValue) {
            if (userValue == value) {
                return "winner";
            }
            return "not the correct value, please have another try";
        }
    }

To register a MBean you simply have to specify a property either in system.properties,
or in intial context properties.

    Properties properties = new Properties();
    properties.setProperty("openejb.user.mbeans.list", GuessHowManyMBean.class.getName());
    EJBContainer.createEJBContainer(properties);

# Accessing the MBean

Then simply get the platform server and you can play with parameters and operations:

    package org.superbiz.mbean;

    import org.junit.Test;

    import javax.ejb.embeddable.EJBContainer;
    import javax.management.Attribute;
    import javax.management.MBeanInfo;
    import javax.management.MBeanServer;
    import javax.management.ObjectName;
    import java.lang.management.ManagementFactory;
    import java.util.Properties;

    import static junit.framework.Assert.assertEquals;

    public class GuessHowManyMBeanTest {
        private static final String OBJECT_NAME = "openejb.user.mbeans:group=org.superbiz.mbean,application=mbean-auto-registration,name=GuessHowManyMBean";

        @Test
        public void play() throws Exception {
            Properties properties = new Properties();
            properties.setProperty("openejb.user.mbeans.list", GuessHowManyMBean.class.getName());
            EJBContainer container = EJBContainer.createEJBContainer(properties);

            MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            ObjectName objectName = new ObjectName(OBJECT_NAME);
            MBeanInfo infos = server.getMBeanInfo(objectName);
            assertEquals(0, server.getAttribute(objectName, "value"));
            server.setAttribute(objectName, new Attribute("value", 3));
            assertEquals(3, server.getAttribute(objectName, "value"));
            assertEquals("winner", server.invoke(objectName, "tryValue", new Object[]{3}, null));
            assertEquals("not the correct value, please have another try", server.invoke(objectName, "tryValue", new Object[]{2}, null));

            container.close();
        }
    }

#### Note

If OpenEJB can't find any module it can't start. So to force him to start even if the example has only the mbean
as java class, we added a `beans.xml` file to turn our project into a Java EE module.

# Running

    
    -------------------------------------------------------
     T E S T S
    -------------------------------------------------------
    Running org.superbiz.mbean.GuessHowManyMBeanTest
    Apache OpenEJB 4.0.0-beta-1    build: 20111002-04:06
    http://tomee.apache.org/
    INFO - openejb.home = /Users/dblevins/examples/mbean-auto-registration
    INFO - openejb.base = /Users/dblevins/examples/mbean-auto-registration
    INFO - Using 'javax.ejb.embeddable.EJBContainer=true'
    INFO - Configuring Service(id=Default Security Service, type=SecurityService, provider-id=Default Security Service)
    INFO - Configuring Service(id=Default Transaction Manager, type=TransactionManager, provider-id=Default Transaction Manager)
    INFO - Found EjbModule in classpath: /Users/dblevins/examples/mbean-auto-registration/target/classes
    INFO - Beginning load: /Users/dblevins/examples/mbean-auto-registration/target/classes
    INFO - Configuring enterprise application: /Users/dblevins/examples/mbean-auto-registration
    INFO - MBean openejb.user.mbeans:application=,group=org.superbiz.mbean,name=GuessHowManyMBean registered.
    INFO - MBean openejb.user.mbeans:application=mbean-auto-registration,group=org.superbiz.mbean,name=GuessHowManyMBean registered.
    INFO - MBean openejb.user.mbeans:application=EjbModule1847652919,group=org.superbiz.mbean,name=GuessHowManyMBean registered.
    INFO - Configuring Service(id=Default Managed Container, type=Container, provider-id=Default Managed Container)
    INFO - Auto-creating a container for bean mbean-auto-registration.Comp: Container(type=MANAGED, id=Default Managed Container)
    INFO - Enterprise application "/Users/dblevins/examples/mbean-auto-registration" loaded.
    INFO - Assembling app: /Users/dblevins/examples/mbean-auto-registration
    INFO - Jndi(name="java:global/mbean-auto-registration/mbean-auto-registration.Comp!org.apache.openejb.BeanContext$Comp")
    INFO - Jndi(name="java:global/mbean-auto-registration/mbean-auto-registration.Comp")
    INFO - Jndi(name="java:global/EjbModule1847652919/org.superbiz.mbean.GuessHowManyMBeanTest!org.superbiz.mbean.GuessHowManyMBeanTest")
    INFO - Jndi(name="java:global/EjbModule1847652919/org.superbiz.mbean.GuessHowManyMBeanTest")
    INFO - Created Ejb(deployment-id=mbean-auto-registration.Comp, ejb-name=mbean-auto-registration.Comp, container=Default Managed Container)
    INFO - Created Ejb(deployment-id=org.superbiz.mbean.GuessHowManyMBeanTest, ejb-name=org.superbiz.mbean.GuessHowManyMBeanTest, container=Default Managed Container)
    INFO - Started Ejb(deployment-id=mbean-auto-registration.Comp, ejb-name=mbean-auto-registration.Comp, container=Default Managed Container)
    INFO - Started Ejb(deployment-id=org.superbiz.mbean.GuessHowManyMBeanTest, ejb-name=org.superbiz.mbean.GuessHowManyMBeanTest, container=Default Managed Container)
    INFO - Deployed Application(path=/Users/dblevins/examples/mbean-auto-registration)
    INFO - Undeploying app: /Users/dblevins/examples/mbean-auto-registration
    Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.063 sec
    
    Results :
    
    Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
    
