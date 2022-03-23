/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.superbiz.dynamic.mbean;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.superbiz.dynamic.mbean.simple.Simple;

import jakarta.ejb.EJB;
import jakarta.ejb.embeddable.EJBContainer;
import javax.management.Attribute;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

import static org.junit.Assert.assertEquals;

public class DynamicMBeanClientTest {

    private static ObjectName objectName;
    private static EJBContainer container;

    @EJB
    private DynamicMBeanClient localClient;
    @EJB
    private DynamicRemoteMBeanClient remoteClient;

    @BeforeClass
    public static void start() {
        container = EJBContainer.createEJBContainer();
    }

    @Before
    public void injectAndRegisterMBean() throws Exception {
        container.getContext().bind("inject", this);
        objectName = new ObjectName(DynamicMBeanClient.OBJECT_NAME);
        ManagementFactory.getPlatformMBeanServer().registerMBean(new Simple(), objectName);
    }

    @After
    public void unregisterMBean() throws Exception {
        if (objectName != null) {
            ManagementFactory.getPlatformMBeanServer().unregisterMBean(objectName);
        }
    }

    @Test
    public void localGet() throws Exception {
        assertEquals(0, localClient.getCounter());
        ManagementFactory.getPlatformMBeanServer().setAttribute(objectName, new Attribute("Counter", 5));
        assertEquals(5, localClient.getCounter());
    }

    @Test
    public void localSet() throws Exception {
        assertEquals(0, ((Integer) ManagementFactory.getPlatformMBeanServer().getAttribute(objectName, "Counter")).intValue());
        localClient.setCounter(8);
        assertEquals(8, ((Integer) ManagementFactory.getPlatformMBeanServer().getAttribute(objectName, "Counter")).intValue());
    }

    @Test
    public void localOperation() {
        assertEquals(7, localClient.length("openejb"));
    }

    @Test
    public void remoteGet() throws Exception {
        assertEquals(0, remoteClient.getCounter());
        ManagementFactory.getPlatformMBeanServer().setAttribute(objectName, new Attribute("Counter", 5));
        assertEquals(5, remoteClient.getCounter());
    }

    @Test
    public void remoteSet() throws Exception {
        assertEquals(0, ((Integer) ManagementFactory.getPlatformMBeanServer().getAttribute(objectName, "Counter")).intValue());
        remoteClient.setCounter(8);
        assertEquals(8, ((Integer) ManagementFactory.getPlatformMBeanServer().getAttribute(objectName, "Counter")).intValue());
    }

    @Test
    public void remoteOperation() {
        assertEquals(7, remoteClient.length("openejb"));
    }

    @AfterClass
    public static void close() {
        if (container != null) {
            container.close();
        }
    }
}
