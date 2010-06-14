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
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.jee.ConcurrentLockType;
import org.apache.openejb.jee.ContainerConcurrency;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;

import javax.ejb.Local;
import javax.ejb.Lock;
import javax.ejb.Remote;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static javax.ejb.LockType.READ;
import static javax.ejb.LockType.WRITE;

/**
 * @version $Rev$ $Date$
 */
public class ConcurrentLockTypeTest extends TestCase {
    private Map<Method, MethodAttributeInfo> attributes;

    public void test() throws Exception {
        Assembler assembler = new Assembler();
        ConfigurationFactory config = new ConfigurationFactory();

        assembler.createProxyFactory(config.configureService(ProxyFactoryInfo.class));
        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new SingletonBean(Color.class));
        ejbJar.addEnterpriseBean(new SingletonBean(Red.class));
        ejbJar.addEnterpriseBean(new SingletonBean(Crimson.class));
        ejbJar.addEnterpriseBean(new SingletonBean(Scarlet.class));
        List<ContainerConcurrency> declared = ejbJar.getAssemblyDescriptor().getContainerConcurrency();

        declared.add(new ContainerConcurrency(ConcurrentLockType.WRITE, "*", "*", "*"));
        declared.add(new ContainerConcurrency(ConcurrentLockType.READ, "*", "Crimson", "*"));
        declared.add(new ContainerConcurrency(ConcurrentLockType.READ, Color.class.getName(), "Scarlet", "*"));
        declared.add(new ContainerConcurrency(ConcurrentLockType.READ, Red.class.getName(), "Scarlet", "red"));
        declared.add(new ContainerConcurrency(ConcurrentLockType.WRITE, "Scarlet", Scarlet.class.getMethod("scarlet")));

        EjbJarInfo ejbJarInfo = config.configureApplication(ejbJar);
        assembler.createApplication(ejbJarInfo);

        loadAttributes(ejbJarInfo, "Color");

        assertAttribute("Read", Color.class.getMethod("color"));
        assertAttribute("Write", Color.class.getMethod("color", Object.class));
        assertAttribute("Write", Color.class.getMethod("color", String.class));
        assertAttribute("Write", Color.class.getMethod("color", Boolean.class));
        assertAttribute("Write", Color.class.getMethod("color", Integer.class));

        loadAttributes(ejbJarInfo, "Red");

        assertAttribute("Read", Red.class.getMethod("color"));
        assertAttribute("Write", Red.class.getMethod("color", Object.class));
        assertAttribute("Write", Red.class.getMethod("color", String.class));
        assertAttribute("Write", Red.class.getMethod("color", Boolean.class));
        assertAttribute("Write", Red.class.getMethod("color", Integer.class));
        assertAttribute("Write", Red.class.getMethod("red"));
        assertAttribute("Write", Red.class.getMethod("red", Object.class));
        assertAttribute("Write", Red.class.getMethod("red", String.class));

        loadAttributes(ejbJarInfo, "Crimson");

        assertAttribute("Read", Crimson.class.getMethod("color"));
        assertAttribute("Read", Crimson.class.getMethod("color", Object.class));
        assertAttribute("Read", Crimson.class.getMethod("color", String.class));
        assertAttribute("Read", Crimson.class.getMethod("color", Boolean.class));
        assertAttribute("Read", Crimson.class.getMethod("color", Integer.class));
        assertAttribute("Write", Crimson.class.getMethod("red"));
        assertAttribute("Read", Crimson.class.getMethod("red", Object.class));
        assertAttribute("Read", Crimson.class.getMethod("red", String.class));
        assertAttribute("Write", Crimson.class.getMethod("crimson"));
        assertAttribute("Read", Crimson.class.getMethod("crimson", String.class));

        loadAttributes(ejbJarInfo, "Scarlet");

        assertAttribute("Read", Scarlet.class.getMethod("color"));
        assertAttribute("Write", Scarlet.class.getMethod("color", Object.class));
        assertAttribute("Read", Scarlet.class.getMethod("color", String.class));
        assertAttribute("Read", Scarlet.class.getMethod("color", Boolean.class));
        assertAttribute("Read", Scarlet.class.getMethod("color", Integer.class));
        assertAttribute("Write", Scarlet.class.getMethod("red"));
        assertAttribute("Read", Scarlet.class.getMethod("red", Object.class));
        assertAttribute("Read", Scarlet.class.getMethod("red", String.class));
        assertAttribute("Write", Scarlet.class.getMethod("scarlet"));
        assertAttribute("Read", Scarlet.class.getMethod("scarlet", String.class));

    }

    private void loadAttributes(EjbJarInfo ejbJarInfo, String deploymentId) {
        ContainerSystem system = SystemInstance.get().getComponent(ContainerSystem.class);
        CoreDeploymentInfo deploymentInfo = (CoreDeploymentInfo) system.getDeploymentInfo(deploymentId);
        List<MethodConcurrencyInfo> infos = MethodConcurrencyBuilder.normalize(ejbJarInfo.methodConcurrency);
        attributes = MethodInfoUtil.resolveAttributes(infos, deploymentInfo);
    }

    private void assertAttribute(String attribute, Method method) {
        MethodConcurrencyInfo info = (MethodConcurrencyInfo) attributes.get(method);
        assertEquals(method.toString(), attribute, info.concurrencyAttribute);
    }

    @Local
    public static interface ColorLocal {
    }

    @Remote
    public static interface ColorRemote {
    }

    @Lock(WRITE)
    public static class Color implements ColorLocal, ColorRemote {


        @Lock(READ)
        public void color() {
        }


        @Lock(WRITE)
        public void color(Object o) {
        }

        public void color(String s) {
        }

        public void color(Boolean b) {
        }

        public void color(Integer i) {
        }
    }


    public static class Red extends Color {

        public void color(Object o) {
            super.color(o);
        }

        @Lock(WRITE)
        public void red() {
        }

        public void red(Object o) {
        }

        public void red(String s) {
        }

    }

    @Lock(READ)
    public static class Crimson extends Red {


        public void color() {
        }

        public void color(String s) {
        }

        @Lock(WRITE)
        public void crimson() {
        }

        public void crimson(String s) {
        }
    }

    @Lock(READ)
    public static class Scarlet extends Red {

        @Lock(WRITE)
        public void scarlet() {
        }

        public void scarlet(String s) {
        }
    }

}
