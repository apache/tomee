package org.apache.openejb.monitoring;

import java.lang.management.ManagementFactory;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.apache.openejb.core.singleton.SingletonContainer;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.junit.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(ApplicationComposer.class)
public class JMXContainerTest {
    @Test
    public void checkContainerIsRegistered() throws Exception {
        final ObjectName on = new ObjectName("openejb.management:ObjectType=containers,DataSource=Default Singleton Container");
        final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        assertTrue(server.isRegistered(on));
        assertEquals(server.getAttribute(on, "className").toString(), SingletonContainer.class.getName());
    }

    @Module
    public SingletonBean persistence() {
        return new SingletonBean(ABean.class);
    }

    public static class ABean {

    }
}

