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
import jakarta.ejb.Remote;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @version $Rev$ $Date$
 */
public class AccessTimeoutTest extends TestCase {
    private Map<Method, MethodAttributeInfo> attributes;

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
        //TODO alternative to hack in CidBuilder to initialize if missing
//        SystemInstance.get().setComponent(ThreadSingletonService.class, new ThreadSingletonServiceImpl(getClass().getClassLoader()));

        final EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new SingletonBean(Color.class));
        ejbJar.addEnterpriseBean(new SingletonBean(Red.class));
        ejbJar.addEnterpriseBean(new SingletonBean(Crimson.class));
        ejbJar.addEnterpriseBean(new SingletonBean(Scarlet.class));

        final EjbJarInfo ejbJarInfo = config.configureApplication(ejbJar);
        assembler.createApplication(ejbJarInfo);

        loadAttributes(ejbJarInfo, "Color");

        assertAttribute(2, TimeUnit.SECONDS, Color.class.getMethod("color"));
        assertAttribute(3, TimeUnit.SECONDS, Color.class.getMethod("color", Object.class));
        assertAttribute(1, TimeUnit.SECONDS, Color.class.getMethod("color", String.class));
        assertAttribute(1, TimeUnit.SECONDS, Color.class.getMethod("color", Boolean.class));
        assertAttribute(1, TimeUnit.SECONDS, Color.class.getMethod("color", Integer.class));

        loadAttributes(ejbJarInfo, "Red");

        assertAttribute(2, TimeUnit.SECONDS, Red.class.getMethod("color"));
        assertNullAttribute(Red.class.getMethod("color", Object.class));
        assertAttribute(1, TimeUnit.SECONDS, Red.class.getMethod("color", String.class));
        assertAttribute(1, TimeUnit.SECONDS, Red.class.getMethod("color", Boolean.class));
        assertAttribute(1, TimeUnit.SECONDS, Red.class.getMethod("color", Integer.class));

        assertAttribute(1, TimeUnit.MINUTES, Red.class.getMethod("red"));
        assertNullAttribute(Red.class.getMethod("red", Object.class));
        assertNullAttribute(Red.class.getMethod("red", String.class));

        loadAttributes(ejbJarInfo, "Crimson");

        assertAttribute(1, TimeUnit.HOURS, Crimson.class.getMethod("color"));
        assertNullAttribute(Crimson.class.getMethod("color", Object.class));
        assertAttribute(1, TimeUnit.HOURS, Crimson.class.getMethod("color", String.class));
        assertAttribute(1, TimeUnit.SECONDS, Crimson.class.getMethod("color", Boolean.class));
        assertAttribute(1, TimeUnit.SECONDS, Crimson.class.getMethod("color", Integer.class));

        assertAttribute(1, TimeUnit.MINUTES, Crimson.class.getMethod("red"));
        assertNullAttribute(Crimson.class.getMethod("red", Object.class));
        assertNullAttribute(Crimson.class.getMethod("red", String.class));

        assertAttribute(2, TimeUnit.HOURS, Crimson.class.getMethod("crimson"));
        assertAttribute(1, TimeUnit.HOURS, Crimson.class.getMethod("crimson", String.class));

        loadAttributes(ejbJarInfo, "Scarlet");

        assertAttribute(2, TimeUnit.SECONDS, Scarlet.class.getMethod("color"));
        assertNullAttribute(Scarlet.class.getMethod("color", Object.class));
        assertAttribute(1, TimeUnit.SECONDS, Scarlet.class.getMethod("color", String.class));
        assertAttribute(1, TimeUnit.SECONDS, Scarlet.class.getMethod("color", Boolean.class));
        assertAttribute(1, TimeUnit.SECONDS, Scarlet.class.getMethod("color", Integer.class));

        assertAttribute(1, TimeUnit.MINUTES, Scarlet.class.getMethod("red"));
        assertNullAttribute(Scarlet.class.getMethod("red", Object.class));
        assertNullAttribute(Scarlet.class.getMethod("red", String.class));

        assertAttribute(2, TimeUnit.DAYS, Scarlet.class.getMethod("scarlet"));
        assertAttribute(1, TimeUnit.DAYS, Scarlet.class.getMethod("scarlet", String.class));
    }

    private void loadAttributes(final EjbJarInfo ejbJarInfo, final String deploymentId) {
        final ContainerSystem system = SystemInstance.get().getComponent(ContainerSystem.class);
        final BeanContext beanContext = system.getBeanContext(deploymentId);
        final List<MethodConcurrencyInfo> lockInfos = new ArrayList<>();
        final List<MethodConcurrencyInfo> accessTimeoutInfos = new ArrayList<>();
        MethodConcurrencyBuilder.normalize(ejbJarInfo.methodConcurrency, lockInfos, accessTimeoutInfos);
        attributes = MethodInfoUtil.resolveAttributes(accessTimeoutInfos, beanContext);
    }

    private void assertAttribute(final long time, final TimeUnit unit, final Method method) {
        final MethodConcurrencyInfo info = (MethodConcurrencyInfo) attributes.get(method);
        assertTrue("Null timeout for " + method, info != null && info.accessTimeout != null);
        assertEquals("Timeout time for " + method, time, info.accessTimeout.time);
        assertEquals("Timeout unit for " + method, unit, TimeUnit.valueOf(info.accessTimeout.unit));
    }

    private void assertNullAttribute(final Method method) {
        final MethodConcurrencyInfo info = (MethodConcurrencyInfo) attributes.get(method);
        assertTrue("Non-null timeout for " + method, info == null || info.accessTimeout == null);
    }

    @Local
    public static interface ColorLocal {
    }

    @Remote
    public static interface ColorRemote {
    }

    @AccessTimeout(value = 1, unit = TimeUnit.SECONDS)
    public static class Color implements ColorLocal, ColorRemote {


        @AccessTimeout(value = 2, unit = TimeUnit.SECONDS)
        public void color() {
        }


        @AccessTimeout(value = 3, unit = TimeUnit.SECONDS)
        public void color(final Object o) {
        }

        public void color(final String s) {
        }

        public void color(final Boolean b) {
        }

        public void color(final Integer i) {
        }
    }


    public static class Red extends Color {

        public void color(final Object o) {
            super.color(o);
        }

        @AccessTimeout(value = 1, unit = TimeUnit.MINUTES)
        public void red() {
        }

        public void red(final Object o) {
        }

        public void red(final String s) {
        }

    }

    @AccessTimeout(value = 1, unit = TimeUnit.HOURS)
    public static class Crimson extends Red {


        public void color() {
        }

        public void color(final String s) {
        }

        @AccessTimeout(value = 2, unit = TimeUnit.HOURS)
        public void crimson() {
        }

        public void crimson(final String s) {
        }
    }

    @AccessTimeout(value = 1, unit = TimeUnit.DAYS)
    public static class Scarlet extends Red {

        @AccessTimeout(value = 2, unit = TimeUnit.DAYS)
        public void scarlet() {
        }

        public void scarlet(final String s) {
        }
    }

}
