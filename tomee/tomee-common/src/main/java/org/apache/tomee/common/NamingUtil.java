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

    private static final AtomicInteger ID = new AtomicInteger(31);
    private static final Map<String,Object> REGISTRY = new ConcurrentHashMap<String, Object>();

    // these two attributes are used to be able to cleanup quickly the registry (otherwise we need to duplicate a lot of logic)
    private static StandardContext currentContext;
    private static Map<StandardContext, Collection<String>> ID_BY_CONTEXT = new HashMap<StandardContext, Collection<String>>();

    public static String getProperty(final Reference ref, final String name) {
        final RefAddr addr = ref.get(name);
        if (addr == null) {
            return null;
        }
        final Object value = addr.getContent();
        return (String) value;
    }

    public static boolean isPropertyTrue(final Reference ref, final String name) {
        final RefAddr addr = ref.get(name);
        if (addr == null) {
            return false;
        }
        final Object value = addr.getContent();
        return Boolean.parseBoolean(String.valueOf(value));
    }

    public static void setStaticValue(final Resource resource, final Object value) {
        setStaticValue(resource, null, value);
    }

    public static void setStaticValue(final Resource resource, final String name, final Object value) {
        final String token = String.valueOf(ID.incrementAndGet());
        REGISTRY.put(token, value);
        resource.setProperty("static-token" + (name != null ? "-" + name : ""), token);
        if (currentContext != null) {
            Collection<String> ids = ID_BY_CONTEXT.get(currentContext);
            if (ids == null) {
                ids = new ArrayList<>();
                ID_BY_CONTEXT.put(currentContext, ids);
            }
            ids.add(token);
        }
    }

    @SuppressWarnings({"unchecked"})
    public static<T> T getStaticValue(final Reference ref) {
        return (T) getStaticValue(ref, null);
    }

    @SuppressWarnings({"unchecked"})
    public static <T> T getStaticValue(final Reference ref, String name) {
        name = name != null ? "-" + name : "";
        final String token = getProperty(ref, "static-token" + name);
        if (token == null) {
            return null;
        }
        final T object = (T) REGISTRY.get(token);
        return object;
    }

    public static Class<?> loadClass(final String className) {
        if (className == null) {
            return null;
        }
        try {
            final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            if (classLoader != null) {
                try {
                    final Class clazz = classLoader.loadClass(className);
                    return clazz;
                } catch(final ClassNotFoundException e) {
                    // no-op
                }
            }
            return Class.forName(className);
        } catch (final ClassNotFoundException e) {
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

    public static void setCurrentContext(final StandardContext currentContext) {
        NamingUtil.currentContext = currentContext;
    }

    public static void cleanUpContextResource(final StandardContext context) {
        final Collection<String> keys = ID_BY_CONTEXT.remove(context);
        if (keys != null) {
            for (final String k : keys) {
                REGISTRY.remove(k);
            }
        }
    }
}
