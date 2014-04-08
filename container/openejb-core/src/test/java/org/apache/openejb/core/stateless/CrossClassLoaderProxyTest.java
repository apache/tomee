/**
 *
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
package org.apache.openejb.core.stateless;

import junit.framework.TestCase;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.ProxyFactoryInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.StatelessSessionContainerInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.core.ivm.EjbObjectInputStream;
import org.apache.openejb.core.ivm.IntraVmCopyMonitor;
import org.apache.openejb.core.ivm.naming.InitContextFactory;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatelessBean;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;
import javax.ejb.EJBObject;
import javax.ejb.SessionContext;
import javax.naming.InitialContext;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

/**
 * @version $Revision$ $Date$
 */
public class CrossClassLoaderProxyTest extends TestCase {

    public void testBusinessLocalInterface() throws Exception {

        InitialContext ctx = new InitialContext();

        Widget widget = (Widget) ctx.lookup("WidgetBeanLocal");

        // Do a business method...
        Stack<Lifecycle> lifecycle = widget.getLifecycle();
        assertNotNull("lifecycle", lifecycle);

        // Check the lifecycle of the bean
        List expected = Arrays.asList(Lifecycle.values());

        assertEquals(join("\n", expected), join("\n", lifecycle));
    }

    public void testBusinessRemoteInterface() throws Exception {

        InitialContext ctx = new InitialContext();

        RemoteWidget widget = (RemoteWidget) ctx.lookup("WidgetBeanRemote");

        // Do a business method...
        Stack<Lifecycle> lifecycle = widget.getLifecycle();
        assertNotNull("lifecycle", lifecycle);
        assertNotSame("is copy", lifecycle, WidgetBean.lifecycle);

        // Check the lifecycle of the bean
        List expected = Arrays.asList(Lifecycle.values());

        assertEquals(join("\n", expected), join("\n", lifecycle));
    }

    public void testRemoteInterface() throws Exception {

        InitialContext ctx = new InitialContext();
        EJBHome home = (EJBHome) ctx.lookup("WidgetBeanRemoteHome");
        assertNotNull("home", home);
        assertTrue("home should be an instance of WidgetHome", home instanceof WidgetHome);
        CrossClassLoaderProxyTestObject.widgetHome = (WidgetHome) home;
        CrossClassLoaderProxyTestObject proxyTestObject = new CrossClassLoaderProxyTestObject();
        proxyTestObject.testRemoteInterface();
    }

    public void testCrossClassLoaderRemoteInterface() throws Exception {
        HackClassLoader loader = new HackClassLoader(getClass().getClassLoader());
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(loader);
        try {
            Class testObjectClass = loader.loadClass(CrossClassLoaderProxyTestObject.class.getName());
            assertFalse(CrossClassLoaderProxyTestObject.class.equals(testObjectClass));

            Class widgetClass = (Class) testObjectClass.getField("widgetClass").get(null);
            assertEquals(Widget.class, widgetClass);

            Class widgetHomeClass = (Class) testObjectClass.getField("widgetHomeClass").get(null);
            assertFalse(WidgetHome.class.equals(widgetHomeClass));

            Class widgetRemoteClass = (Class) testObjectClass.getField("widgetRemoteClass").get(null);
            assertFalse(WidgetRemote.class.equals(widgetRemoteClass));

            Object testObject = testObjectClass.newInstance();

            InitialContext ctx = new InitialContext();
            EJBHome rawHome = (EJBHome) ctx.lookup("WidgetBeanRemoteHome");
            
            EJBHome home = (EJBHome) copy(rawHome);
            assertNotNull("home", home);
            assertEquals(widgetHomeClass.getClassLoader(), home.getClass().getClassLoader());
            assertTrue(widgetHomeClass.isAssignableFrom(home.getClass()));

            testObjectClass.getField("widgetHome").set(testObject, home);

            testObjectClass.getMethod("testRemoteInterface").invoke(testObject);
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
    }

    private static Object copy(Object source) throws Exception {
        IntraVmCopyMonitor.preCrossClassLoaderOperation();
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(128);
            ObjectOutputStream out = new ObjectOutputStream(baos);
            out.writeObject(source);
            out.close();

            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream in = new EjbObjectInputStream(bais);
            Object copy = in.readObject();
            return copy;
        } finally {
            IntraVmCopyMonitor.postCrossClassLoaderOperation();
        }
    }

    public static class HackClassLoader extends ClassLoader {
        protected HackClassLoader(ClassLoader parent) {
            super(parent);
        }

        public Class loadClass(String name) throws ClassNotFoundException {
            return loadClass(name, false);
        }

        protected synchronized Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
            // see if we've already loaded it
            Class c = findLoadedClass(name);
            if (c != null) {
                return c;
            }

            if (!name.equals("org.apache.openejb.core.stateless.CrossClassLoaderProxyTest$WidgetHome") &&
                    !name.equals("org.apache.openejb.core.stateless.CrossClassLoaderProxyTest$WidgetRemote") &&
                    !name.equals("org.apache.openejb.core.stateless.CrossClassLoaderProxyTestObject")) {
                return super.loadClass(name, resolve);
            }

            String resourceName = name.replace('.', '/') + ".class";
            InputStream in = getResourceAsStream(resourceName);
            if (in == null) {
                throw new ClassNotFoundException(name);
            }

            // 80% of class files are smaller then 6k
            ByteArrayOutputStream bout = new ByteArrayOutputStream(8 * 1024);

            // copy the input stream into a byte array
            byte[] bytes = new byte[0];
            try {
                byte[] buf = new byte[4 * 1024];
                for (int count = -1; (count = in.read(buf)) >= 0;) {
                    bout.write(buf, 0, count);
                }
                bytes = bout.toByteArray();
            } catch (IOException e) {
                throw new ClassNotFoundException(name, e);
            }

            // define the package
            int packageEndIndex = name.lastIndexOf('.');
            if (packageEndIndex != -1) {
                String packageName = name.substring(0, packageEndIndex);
                if (getPackage(packageName) == null) {
                    definePackage(packageName, null, null, null, null, null, null, null);
                }
            }

            // define the class
            try {
                return defineClass(name, bytes, 0, bytes.length);
            } catch (SecurityException e) {
                // possible prohibited package: defer to the parent
                return super.loadClass(name, resolve);
            }
        }

        public String toString() {
            return "HackClassLoader";
        }
    }

    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, InitContextFactory.class.getName());

        ConfigurationFactory config = new ConfigurationFactory();
        Assembler assembler = new Assembler();

        assembler.createProxyFactory(config.configureService(ProxyFactoryInfo.class));
        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        // containers
        StatelessSessionContainerInfo statelessContainerInfo = config.configureService(StatelessSessionContainerInfo.class);
        statelessContainerInfo.properties.setProperty("TimeOut", "10");
        statelessContainerInfo.properties.setProperty("MaxSize", "0");
        statelessContainerInfo.properties.setProperty("StrictPooling", "false");
        assembler.createContainer(statelessContainerInfo);

        // Setup the descriptor information

        StatelessBean bean = new StatelessBean(WidgetBean.class);
        bean.addBusinessLocal(Widget.class.getName());
        bean.addBusinessRemote(RemoteWidget.class.getName());
        bean.setHomeAndRemote(WidgetHome.class, WidgetRemote.class);
        bean.addPostConstruct("init");
        bean.addPreDestroy("destroy");

        EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(bean);

        assembler.createApplication(config.configureApplication(ejbJar));

        WidgetBean.lifecycle.clear();
    }

    private static String join(String delimeter, List items) {
        StringBuffer sb = new StringBuffer();
        for (Object item : items) {
            sb.append(item.toString()).append(delimeter);
        }
        return sb.toString();
    }

    public static interface Widget {
        Stack<Lifecycle> getLifecycle();
    }

    public static interface RemoteWidget extends Widget {

    }

    public static interface WidgetRemote extends EJBObject {
        Stack<Lifecycle> getLifecycle();
    }

    public static interface WidgetHome extends EJBHome {
        WidgetRemote create() throws CreateException, RemoteException;
    }

    public static enum Lifecycle {
        CONSTRUCTOR, POST_CONSTRUCT, BUSINESS_METHOD, PRE_DESTROY
    }

    public static class WidgetBean implements Widget, RemoteWidget {

        public static Stack<Lifecycle> lifecycle = new Stack<Lifecycle>();

        public WidgetBean() {
            WidgetBean.lifecycle.push(Lifecycle.CONSTRUCTOR);
        }

        public void setSessionContext(SessionContext sessionContext) {
            //lifecycle.push(Lifecycle.INJECTION);
        }

        public Stack<Lifecycle> getLifecycle() {
            WidgetBean.lifecycle.push(Lifecycle.BUSINESS_METHOD);
            return WidgetBean.lifecycle;
        }

        public void init() {
            WidgetBean.lifecycle.push(Lifecycle.POST_CONSTRUCT);
        }

        public void destroy() {
            WidgetBean.lifecycle.push(Lifecycle.PRE_DESTROY);
        }
    }
}
