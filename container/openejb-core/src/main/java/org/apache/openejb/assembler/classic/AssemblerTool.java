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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.assembler.classic;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.util.Messages;
import org.apache.openejb.util.SafeToolkit;
import org.apache.openejb.util.proxy.ProxyFactory;

import javax.transaction.TransactionManager;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Properties;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class AssemblerTool {

    public static final Map<String, Class> serviceInterfaces = new HashMap<String, Class>();
    static {
        serviceInterfaces.put("ProxyFactory", ProxyFactory.class);
        serviceInterfaces.put("SecurityService", org.apache.openejb.spi.SecurityService.class);
        serviceInterfaces.put("TransactionManager", TransactionManager.class);
        serviceInterfaces.put("ConnectionManager", javax.resource.spi.ConnectionManager.class);
        serviceInterfaces.put("Connector", javax.resource.spi.ManagedConnectionFactory.class);
        serviceInterfaces.put("Resource", javax.resource.spi.ResourceAdapter.class);
        serviceInterfaces.put("Container", org.apache.openejb.Container.class);
    }

    protected static final Messages messages = new Messages("org.apache.openejb.util.resources");
    protected static final SafeToolkit toolkit = SafeToolkit.getToolkit("AssemblerTool");

    protected Properties props = new Properties();

    static {
        System.setProperty("noBanner", "true");
    }

    protected static void resolveMethods(List<Method> methods, Class intrface, MethodInfo mi)
            throws SecurityException {
        /*TODO: Add better exception handling. There is a lot of complex code here, I'm sure something could go wrong the user
         might want to know about.
         At the very least, log a warning or two.
         */

        // todo local permissions cause exception
        if (intrface == null) return;

        if (mi.methodName.equals("*")) {
            Method[] mthds = intrface.getMethods();
            for (int i = 0; i < mthds.length; i++)
                methods.add(mthds[i]);
        } else if (mi.methodParams != null) {// there are paramters specified
            try {
                List<Class> params = new ArrayList<Class>();
                ClassLoader cl = intrface.getClassLoader();
                for (String methodParam : mi.methodParams) {
                    try {
                        params.add(getClassForParam(methodParam, cl));
                    } catch (ClassNotFoundException cnfe) {

                    }
                }
                Method m = intrface.getMethod(mi.methodName, params.toArray(new Class[params.size()]));
                methods.add(m);
            } catch (NoSuchMethodException nsme) {
                /*
                Do nothing.  Exceptions are not only possible they are expected to be a part of normall processing.
                Normally exception handling should not be a part of business logic, but server start up doesn't need to be
                as peformant as server runtime, so its allowed.
                */
            }
        } else {// no paramters specified so may be several methods
            Method[] ms = intrface.getMethods();
            for (int i = 0; i < ms.length; i++) {
                if (ms[i].getName().equals(mi.methodName))
                    methods.add(ms[i]);
            }
        }

    }

    protected static void checkImplementation(Class intrfce, Class factory, String serviceType, String serviceName) throws OpenEJBException {
        if (!intrfce.isAssignableFrom(factory)) {
            throw new OpenEJBException(messages.format("init.0100", serviceType, serviceName, factory.getName(), intrfce.getName()));
        }
    }

    private static java.lang.Class getClassForParam(java.lang.String className, ClassLoader cl) throws ClassNotFoundException {
        if (cl == null) {
            cl = ClassLoader.getSystemClassLoader();
        }

        if (className.equals("int")) {
            return java.lang.Integer.TYPE;
        } else if (className.equals("double")) {
            return java.lang.Double.TYPE;
        } else if (className.equals("long")) {
            return java.lang.Long.TYPE;
        } else if (className.equals("boolean")) {
            return java.lang.Boolean.TYPE;
        } else if (className.equals("float")) {
            return java.lang.Float.TYPE;
        } else if (className.equals("char")) {
            return java.lang.Character.TYPE;
        } else if (className.equals("short")) {
            return java.lang.Short.TYPE;
        } else if (className.equals("byte")) {
            return java.lang.Byte.TYPE;
        } else
            return cl.loadClass(className);

    }
}