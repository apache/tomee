/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.monitoring;

import org.apache.openejb.core.singleton.SingletonContainer;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Module;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(ApplicationComposer.class)
public class JMXContainerTest {
    @BeforeClass
    public static void init() {
        System.setProperty(LocalMBeanServer.OPENEJB_JMX_ACTIVE, "true");
    }

    @AfterClass
    public static void reset() {
        System.clearProperty(LocalMBeanServer.OPENEJB_JMX_ACTIVE);
    }

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

