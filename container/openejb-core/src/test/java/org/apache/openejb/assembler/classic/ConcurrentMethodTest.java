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
package org.apache.openejb.assembler.classic;

import junit.framework.TestCase;
import org.apache.openejb.BeanContext;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.junit.AfterClass;

import jakarta.ejb.AccessTimeout;
import jakarta.ejb.Local;
import jakarta.ejb.Lock;
import jakarta.ejb.Remote;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static jakarta.ejb.LockType.READ;
import static jakarta.ejb.LockType.WRITE;

/**
 * @version $Rev$ $Date$
 */
public class ConcurrentMethodTest extends TestCase {
    private Map<Method, MethodAttributeInfo> lockAttributes;
    private Map<Method, MethodAttributeInfo> accessTimeoutAttributes;

    @AfterClass
    public static void afterClass() throws Exception {
        OpenEJB.destroy();
    }

    @Override
    protected void tearDown() throws Exception {
        OpenEJB.destroy();
    }

    public void test() throws Exception {
        final Assembler assembler = new Assembler();
        final ConfigurationFactory config = new ConfigurationFactory();

        assembler.createProxyFactory(config.configureService(ProxyFactoryInfo.class));
        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        final EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new SingletonBean(Color.class));

        final EjbJarInfo ejbJarInfo = config.configureApplication(ejbJar);
        assembler.createApplication(ejbJarInfo);

        loadAttributes(ejbJarInfo, "Color");

        assertAccessTimeoutAttribute(1, TimeUnit.SECONDS, Color.class.getMethod("color", Object.class));
        assertLockAttribute("Read", Color.class.getMethod("color", Object.class));

        assertAccessTimeoutAttribute(1, TimeUnit.SECONDS, Color.class.getMethod("color", String.class));
        assertLockAttribute("Write", Color.class.getMethod("color", String.class));

        assertAccessTimeoutAttribute(2, TimeUnit.HOURS, Color.class.getMethod("color", Boolean.class));
        assertLockAttribute("Read", Color.class.getMethod("color", Boolean.class));
    }

    private void loadAttributes(final EjbJarInfo ejbJarInfo, final String deploymentId) {
        final ContainerSystem system = SystemInstance.get().getComponent(ContainerSystem.class);
        final BeanContext beanContext = system.getBeanContext(deploymentId);
        final List<MethodConcurrencyInfo> lockInfos = new ArrayList<>();
        final List<MethodConcurrencyInfo> accessTimeoutInfos = new ArrayList<>();
        MethodConcurrencyBuilder.normalize(ejbJarInfo.methodConcurrency, lockInfos, accessTimeoutInfos);
        accessTimeoutAttributes = MethodInfoUtil.resolveAttributes(accessTimeoutInfos, beanContext);
        lockAttributes = MethodInfoUtil.resolveAttributes(lockInfos, beanContext);
    }

    private void assertAccessTimeoutAttribute(final long time, final TimeUnit unit, final Method method) {
        final MethodConcurrencyInfo info = (MethodConcurrencyInfo) accessTimeoutAttributes.get(method);
        assertTrue("Null timeout for " + method, info != null && info.accessTimeout != null);
        assertEquals("Timeout time for " + method, time, info.accessTimeout.time);
        assertEquals("Timeout unit for " + method, unit, TimeUnit.valueOf(info.accessTimeout.unit));
    }

    private void assertLockAttribute(final String attribute, final Method method) {
        final MethodConcurrencyInfo info = (MethodConcurrencyInfo) lockAttributes.get(method);
        assertEquals(method.toString(), attribute, info.concurrencyAttribute);
    }

    @Local
    public static interface ColorLocal {
    }

    @Remote
    public static interface ColorRemote {
    }

    @AccessTimeout(value = 1, unit = TimeUnit.SECONDS)
    @Lock(READ)
    public static class Color implements ColorLocal, ColorRemote {

        public void color(final Object o) {
        }

        @Lock(WRITE)
        public void color(final String s) {
        }

        @AccessTimeout(value = 2, unit = TimeUnit.HOURS)
        public void color(final Boolean s) {
        }
    }

}
