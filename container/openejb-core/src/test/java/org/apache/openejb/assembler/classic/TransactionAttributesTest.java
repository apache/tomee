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
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.jee.ContainerTransaction;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.InterceptorBinding;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.jee.TransAttribute;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.junit.AfterClass;

import jakarta.ejb.EJBTransactionRequiredException;
import jakarta.ejb.Local;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Remote;
import jakarta.ejb.TransactionAttribute;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.InvocationContext;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static jakarta.ejb.TransactionAttributeType.MANDATORY;
import static jakarta.ejb.TransactionAttributeType.NEVER;
import static jakarta.ejb.TransactionAttributeType.NOT_SUPPORTED;
import static jakarta.ejb.TransactionAttributeType.REQUIRES_NEW;
import static org.apache.openejb.assembler.classic.MethodInfoUtil.resolveAttributes;
import static org.apache.openejb.assembler.classic.MethodTransactionBuilder.normalize;

/**
 * @version $Rev$ $Date$
 */
public class TransactionAttributesTest extends TestCase {
    private Map<Method, MethodAttributeInfo> attributes;
    private Object bean;

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
        ejbJar.addEnterpriseBean(new StatelessBean(Color.class));
        ejbJar.addEnterpriseBean(new StatelessBean(Red.class));
        ejbJar.addEnterpriseBean(new StatelessBean(Crimson.class));
        ejbJar.addEnterpriseBean(new StatelessBean(Scarlet.class));
        final List<ContainerTransaction> declared = ejbJar.getAssemblyDescriptor().getContainerTransaction();

        declared.add(new ContainerTransaction(TransAttribute.REQUIRED, "*", "*", "*"));
        declared.add(new ContainerTransaction(TransAttribute.SUPPORTS, "*", "Crimson", "*"));
        declared.add(new ContainerTransaction(TransAttribute.SUPPORTS, Color.class.getName(), "Scarlet", "*"));
        declared.add(new ContainerTransaction(TransAttribute.NEVER, Red.class.getName(), "Scarlet", "red"));
        declared.add(new ContainerTransaction(TransAttribute.REQUIRED, "Scarlet", Scarlet.class.getMethod("scarlet")));

        ejbJar.getAssemblyDescriptor().addInterceptorBinding(new InterceptorBinding("*", AttributeInterceptor.class.getName()));

        final EjbJarInfo ejbJarInfo = config.configureApplication(ejbJar);
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

    private void loadAttributes(final EjbJarInfo ejbJarInfo, final String deploymentId) {
        final ContainerSystem system = SystemInstance.get().getComponent(ContainerSystem.class);
        final BeanContext beanContext = system.getBeanContext(deploymentId);
        final List<MethodTransactionInfo> infos = normalize(ejbJarInfo.methodTransactions);
        attributes = resolveAttributes(infos, beanContext);
        bean = system.getBeanContext(deploymentId).getBusinessLocalBeanHome().create();
    }

    private void assertAttribute(final String attribute, final Method method) throws Exception {
        final MethodTransactionInfo info = (MethodTransactionInfo) attributes.get(method);
        assertEquals(method.toString(), attribute, info.transAttribute);

        try {
            final Object[] args = new Object[method.getParameterTypes().length];
            final Object result = method.invoke(bean, args);
            assertEquals(attribute, result);
        } catch (final InvocationTargetException e) {
            assertEquals(attribute, "Mandatory");
            assertTrue(e.getTargetException() instanceof EJBTransactionRequiredException);
        }
    }

    public static class AttributeInterceptor {

        @AroundInvoke
        public Object invoke(final InvocationContext context) throws Exception {
            return ThreadContext.getThreadContext().getTransactionPolicy().getClass().getSimpleName().replace("Tx", "");
        }
    }

    @Local
    public static interface ColorLocal {
    }

    @Remote
    public static interface ColorRemote {
    }

    @LocalBean
    @TransactionAttribute(MANDATORY)
    public static class Color implements ColorLocal, ColorRemote {


        @TransactionAttribute(NEVER)
        public String color() {
            return null;
        }


        @TransactionAttribute(REQUIRES_NEW)
        public String color(final Object o) {
            return null;
        }

        public String color(final String s) {
            return null;
        }

        public String color(final Boolean b) {
            return null;
        }

        public String color(final Integer i) {
            return null;
        }
    }


    public static class Red extends Color {

        public String color(final Object o) {
            return super.color(o);
        }

        @TransactionAttribute(REQUIRES_NEW)
        public String red() {
            return null;
        }

        public String red(final Object o) {
            return null;
        }

        public String red(final String s) {
            return null;
        }

    }

    @TransactionAttribute(NOT_SUPPORTED)
    public static class Crimson extends Red {


        public String color() {
            return null;
        }

        public String color(final String s) {
            return null;
        }

        @TransactionAttribute(REQUIRES_NEW)
        public String crimson() {
            return null;
        }

        public String crimson(final String s) {
            return null;
        }
    }

    @TransactionAttribute(NOT_SUPPORTED)
    public static class Scarlet extends Red {

        @TransactionAttribute(REQUIRES_NEW)
        public String scarlet() {
            return null;
        }

        public String scarlet(final String s) {
            return null;
        }
    }

}
