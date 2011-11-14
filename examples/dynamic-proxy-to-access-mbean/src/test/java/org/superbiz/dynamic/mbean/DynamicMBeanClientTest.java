package org.superbiz.dynamic.mbean;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.superbiz.dynamic.mbean.simple.Simple;

import javax.ejb.EJB;
import javax.ejb.embeddable.EJBContainer;
import javax.management.Attribute;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

import static junit.framework.Assert.assertEquals;

/**
 * @author rmannibucau
 */
public class DynamicMBeanClientTest {
    private static ObjectName objectName;
    private static EJBContainer container;

    @EJB private DynamicMBeanClient client;

    @BeforeClass public static void start() {
        container = EJBContainer.createEJBContainer();
    }

    @Before public void injectAndRegisterMBean() throws Exception {
        container.getContext().bind("inject", this);
        objectName = new ObjectName(DynamicMBeanClient.OBJECT_NAME);
        ManagementFactory.getPlatformMBeanServer().registerMBean(new Simple(), objectName);
    }

    @After public void unregisterMBean() throws Exception {
        if (objectName != null) {
            ManagementFactory.getPlatformMBeanServer().unregisterMBean(objectName);
        }
    }

    @Test public void get() throws Exception {
        assertEquals(0, client.getCounter());
        ManagementFactory.getPlatformMBeanServer().setAttribute(objectName, new Attribute("Counter", 5));
        assertEquals(5, client.getCounter());
    }

    @Test public void set() throws Exception {
        assertEquals(0, ((Integer) ManagementFactory.getPlatformMBeanServer().getAttribute(objectName, "Counter")).intValue());
        client.setCounter(8);
        assertEquals(8, ((Integer) ManagementFactory.getPlatformMBeanServer().getAttribute(objectName, "Counter")).intValue());
    }

    @Test public void operation() {
        assertEquals(7, client.length("openejb"));
    }

    @AfterClass public static void close() {
        if (container != null) {
            container.close();
        }
    }
}
