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

import javax.ejb.EJBMetaData;
import javax.ejb.Handle;
import javax.ejb.HomeHandle;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

/**
 * @version $Revision$ $Date$
 */
public class CrossClassLoaderProxyTestObject extends TestCase {
    public static final Class widgetClass = CrossClassLoaderProxyTest.Widget.class;
    public static final Class widgetHomeClass = CrossClassLoaderProxyTest.WidgetHome.class;
    public static final Class widgetRemoteClass =  CrossClassLoaderProxyTest.WidgetRemote.class;
    public static CrossClassLoaderProxyTest.WidgetHome widgetHome;

    public void testRemoteInterface() throws Exception {
        assertNotNull("widgetHome", widgetHome);
//        assertTrue("home should be an instance of WidgetHome", home instanceof CrossClassLoaderProxyTest.WidgetHome);
//        CrossClassLoaderProxyTest.WidgetHome widgetHome = (CrossClassLoaderProxyTest.WidgetHome)home;

        Object object = widgetHome.create();
        assertNotNull("widgetHome.create()", object);

        assertTrue("object should be an instance of WidgetRemote", object instanceof CrossClassLoaderProxyTest.WidgetRemote);
        CrossClassLoaderProxyTest.WidgetRemote widget = (CrossClassLoaderProxyTest.WidgetRemote)object;

        // Do a business method...
        Stack<CrossClassLoaderProxyTest.Lifecycle> lifecycle = widget.getLifecycle();
        assertNotNull("lifecycle",lifecycle);
        assertNotSame("is copy", lifecycle, CrossClassLoaderProxyTest.WidgetBean.lifecycle);

        // Check the lifecycle of the bean
        List expected = Arrays.asList(CrossClassLoaderProxyTest.Lifecycle.values());

        assertEquals(join("\n", expected) , join("\n", lifecycle));

        // verify home ejb meta data
        EJBMetaData metaData = widgetHome.getEJBMetaData();
        assertTrue("metaData.getEJBHome() should be an instance of WidgetHome", metaData.getEJBHome() instanceof CrossClassLoaderProxyTest.WidgetHome);
        assertEquals(CrossClassLoaderProxyTest.WidgetHome.class, metaData.getHomeInterfaceClass());
        assertEquals(CrossClassLoaderProxyTest.WidgetRemote.class, metaData.getRemoteInterfaceClass());

        // verify home handle
        HomeHandle homeHandle = widgetHome.getHomeHandle();
        assertTrue("homeHandle.getEJBHome() should be an instance of WidgetHome", homeHandle.getEJBHome() instanceof CrossClassLoaderProxyTest.WidgetHome);

        // verify ejb object getHome
        assertTrue("widget.getEJBHome() should be an instance of WidgetHome", widget.getEJBHome() instanceof CrossClassLoaderProxyTest.WidgetHome);

        // verify ejb object handle
        Handle objectHandle = widget.getHandle();
        assertTrue("objectHandle.getEJBObject() should be an instance of WidgetHome", objectHandle.getEJBObject() instanceof CrossClassLoaderProxyTest.WidgetRemote);
    }

//    public void testCrossClassLoaderRemoteInterface() throws Exception {
//        CrossClassLoaderProxyTest.HackClassLoader loader = new CrossClassLoaderProxyTest.HackClassLoader(getClass().getClassLoader());
//        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
//        Thread.currentThread().setContextClassLoader(loader);
//        try {
//            Class widgetClass = loader.loadClass(CrossClassLoaderProxyTest.Widget.class.getName());
//            assertEquals(CrossClassLoaderProxyTest.Widget.class, widgetClass);
//
//            Class widgetHomeClass = loader.loadClass(CrossClassLoaderProxyTest.WidgetHome.class.getName());
//            assertFalse(CrossClassLoaderProxyTest.WidgetHome.class.equals(widgetHomeClass));
//
//            Class widgetRemoteClass = loader.loadClass(CrossClassLoaderProxyTest.WidgetRemote.class.getName());
//            assertFalse(CrossClassLoaderProxyTest.WidgetRemote.class.equals(widgetRemoteClass));
//
//            CoreDeploymentInfo coreDeploymentInfo = (CoreDeploymentInfo) deploymentInfo;
//            EJBHome home = (EJBHome) copy(coreDeploymentInfo.getEJBHome());
//            assertNotNull("home", home);
//            assertEquals(widgetHomeClass.getClassLoader(), home.getClass().getClassLoader());
//            assertTrue(widgetHomeClass.isAssignableFrom(home.getClass()));
//
//            Object object = widgetHomeClass.getMethod("create").invoke(home);
//            assertNotNull("widgetHome.create()", home);
//
//            assertTrue("object should be an instance of WidgetRemote", object instanceof CrossClassLoaderProxyTest.WidgetRemote);
//            CrossClassLoaderProxyTest.WidgetRemote widget = (CrossClassLoaderProxyTest.WidgetRemote)object;
//
//            // Do a business method...
//            Stack<CrossClassLoaderProxyTest.Lifecycle> lifecycle = widget.getLifecycle();
//            assertNotNull("lifecycle",lifecycle);
//            assertNotSame("is copy", lifecycle, CrossClassLoaderProxyTest.WidgetBean.lifecycle);
//
//            // Check the lifecycle of the bean
//            List expected = Arrays.asList(CrossClassLoaderProxyTest.Lifecycle.values());
//
//            assertEquals(join("\n", expected) , join("\n", lifecycle));
//
//            // verify home ejb meta data
//            EJBMetaData metaData = home.getEJBMetaData();
//            assertTrue("metaData.getEJBHome() should be an instance of WidgetHome", metaData.getEJBHome() instanceof CrossClassLoaderProxyTest.WidgetHome);
//            assertEquals(CrossClassLoaderProxyTest.WidgetHome.class, metaData.getHomeInterfaceClass());
//            assertEquals(CrossClassLoaderProxyTest.WidgetRemote.class, metaData.getRemoteInterfaceClass());
//
//            // verify home handle
//            HomeHandle homeHandle = home.getHomeHandle();
//            assertTrue("homeHandle.getEJBHome() should be an instance of WidgetHome", homeHandle.getEJBHome() instanceof CrossClassLoaderProxyTest.WidgetHome);
//
//            // verify ejb object getHome
//            assertTrue("widget.getEJBHome() should be an instance of WidgetHome", widget.getEJBHome() instanceof CrossClassLoaderProxyTest.WidgetHome);
//
//            // verify ejb object handle
//            Handle objectHandle = widget.getHandle();
//            assertTrue("objectHandle.getEJBObject() should be an instance of WidgetHome", objectHandle.getEJBObject() instanceof CrossClassLoaderProxyTest.WidgetRemote);
//        } finally {
//            Thread.currentThread().setContextClassLoader(oldClassLoader);
//        }
//    }

    private static String join(String delimeter, List items) {
        StringBuffer sb = new StringBuffer();
        for (Object item : items) {
            sb.append(item.toString()).append(delimeter);
        }
        return sb.toString();
    }
}
