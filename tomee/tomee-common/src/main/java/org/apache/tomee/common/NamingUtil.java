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
package org.apache.tomee.common;

import org.apache.catalina.core.StandardContext;
import org.apache.naming.EjbRef;

import javax.naming.RefAddr;
import javax.naming.Reference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class NamingUtil {
    public static final String NAME = "name";
    public static final String DEPLOYMENT_ID = "deploymentid";
    public static final String EXTERNAL = "external";
    public static final String LOCAL = "local";
    public static final String LOCALBEAN = "localbean";
    public static final String REMOTE = EjbRef.REMOTE;
    public static final String JNDI_NAME = "jndiname";
    public static final String JNDI_PROVIDER_ID = "jndiproviderid";
    public static final String UNIT = "unit";
    public static final String EXTENDED = "extended";
    public static final String PROPERTIES = "properties";
    public static final String RESOURCE_ID = "resourceid";
    public static final String COMPONENT_TYPE = "componenttype";
    public static final String WS_ID = "wsid";
    public static final String WS_CLASS = "wsclass";
    public static final String WS_QNAME = "wsqname";
    public static final String WS_PORT_QNAME = "wsportqname";
    public static final String WSDL_URL = "wsdlurl";

    private static final AtomicInteger id = new AtomicInteger(31);
    private static final Map<String,Object> registry = new ConcurrentHashMap<String, Object>();

    // these two attributes are used to be able to cleanup quickly the registry (otherwise we need to duplicate a lot of logic)
    private static StandardContext currentContext = null;
    private static Map<StandardContext, Collection<String>> ID_BY_CONTEXT = new HashMap<StandardContext, Collection<String>>();

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

    public static void setStaticValue(Resource resource, Object value) {
        setStaticValue(resource, null, value);
    }

    public static void setStaticValue(Resource resource, String name, Object value) {
        name = name != null ? "-" + name : "";
        String token = "" + id.incrementAndGet();
        registry.put(token, value);
        resource.setProperty("static-token" + name, token);
        if (currentContext != null) {
            Collection<String> ids = ID_BY_CONTEXT.get(currentContext);
            if (ids == null) {
                ids = new ArrayList<String>();
                ID_BY_CONTEXT.put(currentContext, ids);
            }
            ids.add(token);
        }
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

    /**
     * This interface exists because the class org.apache.catalina.deploy.ContextResource
     * is not available in the common classloader in tomcat 55
     */
    public interface Resource {
        void setProperty(String name, Object value);
    }

    public static void setCurrentContext(StandardContext currentContext) {
        NamingUtil.currentContext = currentContext;
    }

    public static void cleanUpContextResource(final StandardContext context) {
        final Collection<String> keys = ID_BY_CONTEXT.remove(context);
        if (keys != null) {
            for (String k : keys) {
                registry.remove(k);
            }
        }
    }
}
