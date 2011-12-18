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

public class DynamicMBeanClientTest {
    private static ObjectName objectName;
    private static EJBContainer container;

    @EJB private DynamicMBeanClient localClient;
    @EJB private DynamicRemoteMBeanClient remoteClient;

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

    @Test public void localGet() throws Exception {
        assertEquals(0, localClient.getCounter());
        ManagementFactory.getPlatformMBeanServer().setAttribute(objectName, new Attribute("Counter", 5));
        assertEquals(5, localClient.getCounter());
    }

    @Test public void localSet() throws Exception {
        assertEquals(0, ((Integer) ManagementFactory.getPlatformMBeanServer().getAttribute(objectName, "Counter")).intValue());
        localClient.setCounter(8);
        assertEquals(8, ((Integer) ManagementFactory.getPlatformMBeanServer().getAttribute(objectName, "Counter")).intValue());
    }

    @Test public void localOperation() {
        assertEquals(7, localClient.length("openejb"));
    }

    @Test public void remoteGet() throws Exception {
        assertEquals(0, remoteClient.getCounter());
        ManagementFactory.getPlatformMBeanServer().setAttribute(objectName, new Attribute("Counter", 5));
        assertEquals(5, remoteClient.getCounter());
    }

    @Test public void remoteSet() throws Exception {
        assertEquals(0, ((Integer) ManagementFactory.getPlatformMBeanServer().getAttribute(objectName, "Counter")).intValue());
        remoteClient.setCounter(8);
        assertEquals(8, ((Integer) ManagementFactory.getPlatformMBeanServer().getAttribute(objectName, "Counter")).intValue());
    }

    @Test public void remoteOperation() {
        assertEquals(7, remoteClient.length("openejb"));
    }

    @AfterClass public static void close() {
        if (container != null) {
            container.close();
        }
    }
}
