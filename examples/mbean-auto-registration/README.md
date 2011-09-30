# MBean auto registration

[Download as zip](mbean-auto-registration.zip)

This example shows how to automatically create and register mbeans using OpenEJB features.

# Dependencies

To be able to use it you need to import the mbean api (annotations):

    <dependency>
      <groupId>org.apache.openejb</groupId>
      <artifactId>mbean-annotation-api</artifactId>
      <version>4.0.0-beta-1</version>
      <scope>provided</scope>
    </dependency>

# The MBean

The mbean implements a simple game where the goal is to guess a number.

It allows the user to change the value too.

# The registering

To register a MBean using OpenEJB you simply have to specify a property eaiher in system.properties,
or in intial context properties.

The example MBean is called org.superbiz.mbean.GuessHowManyMBean so we simply add it to the properties
given to the initial context:

    Properties properties = new Properties();
    properties.setProperty("openejb.user.mbeans.list", GuessHowManyMBean.class.getName());
    EJBContainer.createEJBContainer(properties);

# The implementation

To implement the [org.superbiz.mbean.GuessHowManyMBean](src/main/java/org/superbiz/mbean/GuessHowManyMBean.java.html)
MBean you simply annotate the POJO with specific annotations:

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

# Tests

Then simply get the platform server and you can play with parameters and operations:

    MBeanServer server = ManagementFactory.getPlatformMBeanServer();
    ObjectName objectName = new ObjectName(OBJECT_NAME);
    MBeanInfo infos = server.getMBeanInfo(objectName);
    assertEquals(0, server.getAttribute(objectName, "value"));
    server.setAttribute(objectName, new Attribute("value", 3));
    assertEquals(3, server.getAttribute(objectName, "value"));
    assertEquals("winner", server.invoke(objectName, "tryValue", new Object[]{3}, null));
    assertEquals("not the correct value, please have another try", server.invoke(objectName, "tryValue", new Object[]{2}, null));

# Note

If OpenEJB can't find any module it can't start. So to force him to start even if the example has only the mbean
as java class, we added a beans.xml file to let OpenEJB find a module.
