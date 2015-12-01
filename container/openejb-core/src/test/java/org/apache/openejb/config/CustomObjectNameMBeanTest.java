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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.config;

import org.apache.openejb.api.jmx.MBean;
import org.apache.openejb.api.jmx.ManagedAttribute;
import org.apache.openejb.testing.ApplicationComposers;
import org.apache.openejb.testing.Classes;
import org.junit.Test;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

import static org.junit.Assert.assertEquals;

@Classes(innerClassesAsBean = true)
public class CustomObjectNameMBeanTest {
    @Test
    public void run() throws Exception {
        final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        mbeanState(server, false);
        new ApplicationComposers(this).evaluate(this, new Runnable() {
            @Override
            public void run() {
                try {
                    mbeanState(server, true);
                    assertEquals(1, server.getAttribute(new ObjectName("openejb.user.mbeans:application=openejb,group=org.apache.openejb.config,name=DefaultName"), "value"));
                    assertEquals(2, server.getAttribute(new ObjectName("foo:type=bar,custom=yes"), "value"));
                } catch (final Exception e) {
                    throw new IllegalStateException(e);
                }
            }
        });
        mbeanState(server, false);
    }

    private void mbeanState(final MBeanServer server, final boolean registered) throws MalformedObjectNameException {
        assertEquals(registered, server.isRegistered(new ObjectName("openejb.user.mbeans:application=openejb,group=org.apache.openejb.config,name=DefaultName")));
        assertEquals(registered, server.isRegistered(new ObjectName("foo:type=bar,custom=yes")));
    }

    @MBean
    public static class DefaultName {
        @ManagedAttribute
        public int getValue() {
            return 1;
        }
    }

    @MBean(objectName = "foo:type=bar,custom=yes")
    public static class CustomName {
        @ManagedAttribute
        public int getValue() {
            return 2;
        }
    }
}

