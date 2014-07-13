/*
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.management.ManagementFactory;

import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.openejb.api.jmx.ManagedOperation;
import org.junit.Test;

public class TestDynamicMBeanWrapper {
    @Test // just to ensure MBeanRegistrationSupport doesn't break anything
    public void normalMBeanCanStillBeRegistered() throws Exception {
        final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        final DynamicMBeanWrapper wrapper = new DynamicMBeanWrapper(new MyNotLifecycleAwareMBean());
        final ObjectName on = new ObjectName("org.superbiz.foo:type=dummy2");
        try {
            server.registerMBean(wrapper, on);
            assertTrue(server.isRegistered(on));
            assertEquals("ok", server.invoke(on, "value", new Object[0], null));
        } finally {
            server.unregisterMBean(on);
        }
    }

    @Test
    public void mbeanRegistrationSupport() throws Exception {
        final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        final DynamicMBeanWrapper wrapper = new DynamicMBeanWrapper(new MyMBean());
        assertFalse(MyMBean.postDeregister);
        assertFalse(MyMBean.preDeregister);
        assertFalse(MyMBean.preRegister);
        assertFalse(MyMBean.postRegister);
        final ObjectName on = new ObjectName("org.superbiz.foo:type=dummy");
        try {
            server.registerMBean(wrapper, on);
            assertTrue(server.isRegistered(on));
        } finally {
            server.unregisterMBean(on);
        }
        assertTrue(MyMBean.postDeregister);
        assertTrue(MyMBean.preDeregister);
        assertTrue(MyMBean.preRegister);
        assertTrue(MyMBean.postRegister);
    }

    public static class MyNotLifecycleAwareMBean {
        @ManagedOperation
        public String value() {
            return "ok";
        }
    }

    public static class MyMBean implements MBeanRegistration {
        private static boolean preRegister = false;
        private static boolean postRegister = false;
        private static boolean preDeregister = false;
        private static boolean postDeregister = false;

        @Override
        public ObjectName preRegister(final MBeanServer server, final ObjectName name) throws Exception {
            preRegister = true;
            return name;
        }

        @Override
        public void postRegister(final Boolean registrationDone) {
            postRegister = true;
        }

        @Override
        public void preDeregister() throws Exception {
            preDeregister = true;
        }

        @Override
        public void postDeregister() {
            postDeregister = true;
        }
    }
}
