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
package org.superbiz.mbean;

import org.apache.openejb.monitoring.LocalMBeanServer;
import org.junit.Test;

import jakarta.ejb.embeddable.EJBContainer;
import javax.management.Attribute;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class GuessHowManyMBeanTest {

    private static final String OBJECT_NAME = "openejb.user.mbeans:group=org.superbiz.mbean,application=mbean-auto-registration,name=GuessHowManyMBean";

    @Test
    public void play() throws Exception {
        Properties properties = new Properties();
        properties.setProperty(LocalMBeanServer.OPENEJB_JMX_ACTIVE, Boolean.TRUE.toString());
        EJBContainer container = EJBContainer.createEJBContainer(properties);

        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        ObjectName objectName = new ObjectName(OBJECT_NAME);
        assertEquals(0, server.getAttribute(objectName, "value"));
        server.setAttribute(objectName, new Attribute("value", 3));
        assertEquals(3, server.getAttribute(objectName, "value"));
        assertEquals("winner", server.invoke(objectName, "tryValue", new Object[]{3}, null));
        assertEquals("not the correct value, please have another try", server.invoke(objectName, "tryValue", new Object[]{2}, null));

        container.close();
    }
}
