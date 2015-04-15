/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.superbiz.resource.jmx;

import org.apache.ziplock.maven.Mvn;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.superbiz.resource.jmx.resources.HelloMBean;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.management.Attribute;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

@RunWith(Arquillian.class)
public class JMXTest {

    @EJB
    private TestEjb ejb;

    @Deployment
    public static EnterpriseArchive createDeployment() {

        final JavaArchive ejbJar = new Mvn.Builder()
                .name("jmx-ejb.jar")
                .build(JavaArchive.class)
                .addClass(JMXTest.class)
                .addClass(TestEjb.class);

        final EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class, "jmx.ear")
                .addAsModule(ejbJar);

        return ear;
    }

    @Test
    public void test() throws Exception {
        final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        final ObjectName objectName = new ObjectName("superbiz.test:name=Hello");

        Assert.assertNotNull(ejb);
        
        Assert.assertEquals(0, mbs.getAttribute(objectName, "Count"));
        Assert.assertEquals(0, ejb.getCount());
        
        mbs.invoke(objectName, "increment", new Object[0], new String[0]);
        Assert.assertEquals(1, mbs.getAttribute(objectName, "Count"));
        Assert.assertEquals(1, ejb.getCount());
        
        ejb.increment();
        Assert.assertEquals(2, mbs.getAttribute(objectName, "Count"));
        Assert.assertEquals(2, ejb.getCount());

        Attribute attribute = new Attribute("Count", 12345);
        mbs.setAttribute(objectName, attribute);
        Assert.assertEquals(12345, mbs.getAttribute(objectName, "Count"));
        Assert.assertEquals(12345, ejb.getCount());
        
        ejb.setCount(23456);
        Assert.assertEquals(23456, mbs.getAttribute(objectName, "Count"));
        Assert.assertEquals(23456, ejb.getCount());

        Assert.assertEquals("Hello, world", mbs.invoke(objectName, "greet", new Object[] { "world" }, new String[] { String.class.getName() }));
        Assert.assertEquals("Hello, world", ejb.greet("world"));
    }

    @Singleton
    @Lock(LockType.READ)
    public static class TestEjb {

        @Resource(name="jmx/Hello")
        private HelloMBean helloMBean;

        public String greet(String name) {
            return helloMBean.greet(name);
        }

        public void setCount(int count) {
            helloMBean.setCount(count);
        }

        public void increment() {
            helloMBean.increment();
        }

        public int getCount() {
            return helloMBean.getCount();
        }
    }
}
