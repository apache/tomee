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
import static junit.framework.Assert.assertNotSame;

/**
 * @author Romain Manni-Bucau
 */
public class GuessHowManyMBeanTest {
    private static final String OBJECT_NAME = "openejb.user.mbeans:group=org.superbiz.mbean,application=mbean-auto-registration,name=GuessHowManyMBean";

    @Test public void play() throws Exception {
        Properties properties = new Properties();
        properties.setProperty("openejb.user.mbeans.list", GuessHowManyMBean.class.getName());
        EJBContainer.createEJBContainer(properties);

        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        ObjectName objectName = new ObjectName(OBJECT_NAME);
        MBeanInfo infos = server.getMBeanInfo(objectName);
        assertEquals(0, server.getAttribute(objectName, "value"));
        server.setAttribute(objectName, new Attribute("value", 3));
        assertEquals(3, server.getAttribute(objectName, "value"));
        assertEquals("winner", server.invoke(objectName, "tryValue", new Object[]{3}, null));
        assertEquals("not the correct value, please have another try", server.invoke(objectName, "tryValue", new Object[]{2}, null));
    }
}
