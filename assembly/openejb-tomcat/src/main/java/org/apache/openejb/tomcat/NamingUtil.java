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
package org.apache.openejb.tomcat;

import org.apache.naming.EjbRef;
import org.apache.catalina.deploy.ContextResource;

import javax.naming.Reference;
import javax.naming.RefAddr;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Map;
import java.util.HashMap;

public class NamingUtil {
    public static final String NAME = "name";
    public static final String DEPLOYMENT_ID = "deploymentid";
    public static final String EXTERNAL = "external";
    public static final String LOCAL = "local";
    public static final String REMOTE = EjbRef.REMOTE;
    public static final String JNDI_NAME = "jndiname";
    public static final String JNDI_PROVIDER_ID = "jndiproviderid";
    public static final String UNIT = "unit";
    public static final String EXTENDED = "extended";
    public static final String PROPERTIES = "properties";
    public static final String RESOURCE_ID = "resourceid";
    public static final String COMPONENT_TYPE = "componenttype";
    public static final String WEB_SERVICE_CLASS = "webserviceclass";
    public static final String WEB_SERVICE_QNAME = "webserviceqname";
    public static final String WSDL_URL = "wsdlurl";
    public static final String WSDL_REPO_URI = "wsdlrepouri";

    private static final AtomicInteger id = new AtomicInteger(31);
    private static final Map<String,Object> registry = new HashMap<String,Object>();

    public static String getProperty(Reference ref, String name) {
        RefAddr addr = ref.get(name);
        if (addr == null) return null;
        Object value = addr.getContent();
        return (String) value;
    }

    public static boolean isPropertyTrue(Reference ref, String name) {
        RefAddr addr = ref.get(name);
        if (addr == null) return false;
        Object value = addr.getContent();
        return Boolean.parseBoolean("" + value);
    }

    public static void setStaticValue(ContextResource resource, Object value) {
        setStaticValue(resource, null, value);
    }

    public static void setStaticValue(ContextResource resource, String name, Object value) {
        name = name != null ? "-" + name : "";
        String token = "" + id.incrementAndGet();
        registry.put(token, value);
        resource.setProperty("static-token" + name, token);
    }

    @SuppressWarnings({"unchecked"})
    public static<T> T getStaticValue(Reference ref) {
        return (T) getStaticValue(ref, null);
    }

    @SuppressWarnings({"unchecked"})
    public static <T> T getStaticValue(Reference ref, String name) {
        name = name != null ? "-" + name : "";
        String token = getProperty(ref, "static-token" + name);
        if (token == null) {
            return null;
        }
        T object = (T) registry.get(token);
        return object;
    }

    public static Class<?> loadClass(String className) {
        if (className == null) return null;
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            if (classLoader != null) {
                try {
                    Class clazz = classLoader.loadClass(className);
                    return clazz;
                } catch(ClassNotFoundException e) {
                }
            }
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}
