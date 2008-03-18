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

import static org.apache.openejb.assembler.classic.MethodTransactionBuilder.normalize;
import junit.framework.TestCase;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.jee.ContainerTransaction;
import org.apache.openejb.jee.TransAttribute;
import org.apache.openejb.core.CoreDeploymentInfo;
import static org.apache.openejb.assembler.classic.MethodInfoUtil.*;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.loader.SystemInstance;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.TransactionAttribute;
import static javax.ejb.TransactionAttributeType.*;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.List;

/**
 * @version $Rev$ $Date$
 */
public class TransactionAttributesTest extends TestCase {
    private Map<Method, MethodAttributeInfo> attributes;

    public void test() throws Exception {
        Assembler assembler = new Assembler();
        ConfigurationFactory config = new ConfigurationFactory();

        assembler.createProxyFactory(config.configureService(ProxyFactoryInfo.class));
        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new StatelessBean(Color.class));
        ejbJar.addEnterpriseBean(new StatelessBean(Red.class));
        ejbJar.addEnterpriseBean(new StatelessBean(Crimson.class));
        ejbJar.addEnterpriseBean(new StatelessBean(Scarlet.class));
        List<ContainerTransaction> declared = ejbJar.getAssemblyDescriptor().getContainerTransaction();

        declared.add(new ContainerTransaction(TransAttribute.REQUIRED, "*", "*", "*"));
        declared.add(new ContainerTransaction(TransAttribute.SUPPORTS, "*", "Crimson", "*"));
        declared.add(new ContainerTransaction(TransAttribute.SUPPORTS, Color.class.getName(), "Scarlet", "*"));
        declared.add(new ContainerTransaction(TransAttribute.NEVER, Red.class.getName(), "Scarlet", "red"));
        declared.add(new ContainerTransaction(TransAttribute.REQUIRED, "Scarlet", Scarlet.class.getMethod("scarlet")));

        EjbJarInfo ejbJarInfo = config.configureApplication(ejbJar);
        assembler.createApplication(ejbJarInfo);

        loadAttributes(ejbJarInfo, "Color");

        assertAttribute("Never", Color.class.getMethod("color"));
        assertAttribute("RequiresNew", Color.class.getMethod("color", Object.class));
        assertAttribute("Mandatory", Color.class.getMethod("color", String.class));
        assertAttribute("Mandatory", Color.class.getMethod("color", Boolean.class));
        assertAttribute("Mandatory", Color.class.getMethod("color", Integer.class));

        loadAttributes(ejbJarInfo, "Red");

        assertAttribute("Never", Red.class.getMethod("color"));
        assertAttribute("Required", Red.class.getMethod("color", Object.class));
        assertAttribute("Mandatory", Red.class.getMethod("color", String.class));
        assertAttribute("Mandatory", Red.class.getMethod("color", Boolean.class));
        assertAttribute("Mandatory", Red.class.getMethod("color", Integer.class));
        assertAttribute("RequiresNew", Red.class.getMethod("red"));
        assertAttribute("Required", Red.class.getMethod("red", Object.class));
        assertAttribute("Required", Red.class.getMethod("red", String.class));

        loadAttributes(ejbJarInfo, "Crimson");

        assertAttribute("Supports", Crimson.class.getMethod("color"));
        assertAttribute("Supports", Crimson.class.getMethod("color", Object.class));
        assertAttribute("Supports", Crimson.class.getMethod("color", String.class));
        assertAttribute("Supports", Crimson.class.getMethod("color", Boolean.class));
        assertAttribute("Supports", Crimson.class.getMethod("color", Integer.class));
        assertAttribute("RequiresNew", Crimson.class.getMethod("red"));
        assertAttribute("Supports", Crimson.class.getMethod("red", Object.class));
        assertAttribute("Supports", Crimson.class.getMethod("red", String.class));
        assertAttribute("RequiresNew", Crimson.class.getMethod("crimson"));
        assertAttribute("Supports", Crimson.class.getMethod("crimson", String.class));

        loadAttributes(ejbJarInfo, "Scarlet");

        assertAttribute("Never", Scarlet.class.getMethod("color"));
        assertAttribute("Required", Scarlet.class.getMethod("color", Object.class));
        assertAttribute("Supports", Scarlet.class.getMethod("color", String.class));
        assertAttribute("Supports", Scarlet.class.getMethod("color", Boolean.class));
        assertAttribute("Supports", Scarlet.class.getMethod("color", Integer.class));
        assertAttribute("RequiresNew", Scarlet.class.getMethod("red"));
        assertAttribute("Never", Scarlet.class.getMethod("red", Object.class));
        assertAttribute("Never", Scarlet.class.getMethod("red", String.class));
        assertAttribute("Required", Scarlet.class.getMethod("scarlet"));
        assertAttribute("NotSupported", Scarlet.class.getMethod("scarlet", String.class));

    }

    private void loadAttributes(EjbJarInfo ejbJarInfo, String deploymentId) {
        ContainerSystem system = SystemInstance.get().getComponent(ContainerSystem.class);
        CoreDeploymentInfo deploymentInfo = (CoreDeploymentInfo) system.getDeploymentInfo(deploymentId);
        List<MethodTransactionInfo> infos = normalize(ejbJarInfo.methodTransactions);
        attributes = resolveAttributes(infos, deploymentInfo);
    }

    private void assertAttribute(String attribute, Method method) {
        MethodTransactionInfo info = (MethodTransactionInfo) attributes.get(method);
        assertEquals(method.toString(), attribute, info.transAttribute);
    }

    @Local
    public static interface ColorLocal {
    }

    @Remote
    public static interface ColorRemote {
    }

    @TransactionAttribute(MANDATORY)
    public static class Color implements ColorLocal, ColorRemote {


        @TransactionAttribute(NEVER)
        public void color(){}


        @TransactionAttribute(REQUIRES_NEW)
        public void color(Object o){}

        public void color(String s){}
        public void color(Boolean b){}
        public void color(Integer i){}
    }


    public static class Red extends Color {

        public void color(Object o){super.color(o);}

        @TransactionAttribute(REQUIRES_NEW)
        public void red(){}

        public void red(Object o){}
        public void red(String s){}

    }

    @TransactionAttribute(NOT_SUPPORTED)
    public static class Crimson extends Red {


        public void color(){}
        public void color(String s){}

        @TransactionAttribute(REQUIRES_NEW)
        public void crimson(){}

        public void crimson(String s){}
    }

    @TransactionAttribute(NOT_SUPPORTED)
    public static class Scarlet extends Red {

        @TransactionAttribute(REQUIRES_NEW)
        public void scarlet(){}

        public void scarlet(String s){}
    }

}
