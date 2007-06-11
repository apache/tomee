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
package org.apache.openejb.util;

import org.apache.openejb.OpenEJBException;

import java.util.HashMap;
import java.util.Properties;

public class SafeToolkit {
    public static final Messages messages = new Messages("org.apache.openejb.util.resources");
    public static final HashMap codebases = new HashMap();

    public static SafeToolkit getToolkit(String systemLocation) {
        return new SafeToolkit(systemLocation);
    }

    private String systemLocation;

    private SafeToolkit(String systemLocation) {
        this.systemLocation = systemLocation;
    }

    private Class forName(String className) throws OpenEJBException {
        Class clazz = null;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException cnfe) {
            OpenEJBErrorHandler.classNotFound(systemLocation, className);
        }
        return clazz;
    }

    public Object newInstance(String className) throws OpenEJBException {
        return newInstance(forName(className));
    }

    public Object newInstance(Class clazz) throws OpenEJBException {
        Object instance = null;
        try {
            instance = clazz.newInstance();
        } catch (InstantiationException ie) {
            OpenEJBErrorHandler.classNotIntantiateable(systemLocation, clazz.getName());
        } catch (IllegalAccessException iae) {
            OpenEJBErrorHandler.classNotAccessible(systemLocation, clazz.getName());
        }

        catch (Throwable exception) {
            exception.printStackTrace();
            ClassLoader classLoader = clazz.getClassLoader();
            if (classLoader instanceof java.net.URLClassLoader) {
                OpenEJBErrorHandler.classNotIntantiateableFromCodebaseForUnknownReason(systemLocation, clazz.getName(), getCodebase((java.net.URLClassLoader) classLoader),
                        exception.getClass().getName(), exception.getMessage());
            } else {
                OpenEJBErrorHandler.classNotIntantiateableForUnknownReason(systemLocation, clazz.getName(), exception.getClass().getName(), exception.getMessage());
            }
        }
        return instance;

    }

    public SafeProperties getSafeProperties(Properties props) throws OpenEJBException {
        return new SafeProperties(props, systemLocation);
    }

    public static Class loadClass(String className, String codebase) throws OpenEJBException {
        return loadClass(className, codebase, true);
    }

    private static Class loadClass(String className, String codebase, boolean cache) throws OpenEJBException {

        ClassLoader cl = (cache) ? getCodebaseClassLoader(codebase) : getClassLoader(codebase);
        Class clazz = null;
        try {
            clazz = cl.loadClass(className);
        } catch (ClassNotFoundException cnfe) {
            throw new OpenEJBException(messages.format("cl0007", className, codebase), cnfe);
        }
        return clazz;
    }

    private static ClassLoader getCodebaseClassLoader(String codebase) throws OpenEJBException {
        if (codebase == null) codebase = "CLASSPATH";

        ClassLoader cl = (ClassLoader) codebases.get(codebase);
        if (cl == null) {
            synchronized (codebases) {
                cl = (ClassLoader) codebases.get(codebase);
                if (cl == null) {
                    try {
                        java.net.URL[] urlCodebase = new java.net.URL[1];
                        urlCodebase[0] = new java.net.URL("file", null, codebase);
// make sure everything works if we were not loaded by the system class loader
                        cl = new java.net.URLClassLoader(urlCodebase, SafeToolkit.class.getClassLoader());
//cl = SafeToolkit.class.getClassLoader();
                        codebases.put(codebase, cl);
                    } catch (java.net.MalformedURLException mue) {
                        throw new OpenEJBException(messages.format("cl0001", codebase, mue.getMessage()), mue);
                    } catch (SecurityException se) {
                        throw new OpenEJBException(messages.format("cl0002", codebase, se.getMessage()), se);
                    }
                }
            }
        }
        return cl;
    }

    private static ClassLoader getClassLoader(String codebase) throws OpenEJBException {
        ClassLoader cl = null;
        try {
            java.net.URL[] urlCodebase = new java.net.URL[1];
            urlCodebase[0] = new java.net.URL("file", null, codebase);

            cl = new java.net.URLClassLoader(urlCodebase, SafeToolkit.class.getClassLoader());
        } catch (java.net.MalformedURLException mue) {
            throw new OpenEJBException(messages.format("cl0001", codebase, mue.getMessage()), mue);
        } catch (SecurityException se) {
            throw new OpenEJBException(messages.format("cl0002", codebase, se.getMessage()), se);
        }
        return cl;
    }

    private static String getCodebase(java.net.URLClassLoader urlClassLoader) {
        StringBuffer codebase = new StringBuffer();
        java.net.URL urlList[] = urlClassLoader.getURLs();
        codebase.append(urlList[0].toString());
        for (int i = 1; i < urlList.length; ++i) {
            codebase.append(';');
            codebase.append(urlList[i].toString());
        }
        return codebase.toString();
    }
}