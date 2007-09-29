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
        String token = "" + id.incrementAndGet();
        registry.put(token, value);
        resource.setProperty("static-token", token);
    }

    public static Object getStaticValue(Reference ref) {
        String token = getProperty(ref, "static-token");
        if (token == null) {
            return null;
        }
        Object object = registry.get(token);
        return object;
    }
}
