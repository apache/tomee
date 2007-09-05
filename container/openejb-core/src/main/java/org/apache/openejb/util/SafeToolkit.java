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