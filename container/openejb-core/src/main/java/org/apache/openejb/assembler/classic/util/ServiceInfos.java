/*
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
package org.apache.openejb.assembler.classic.util;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.ServiceInfo;
import org.apache.openejb.config.sys.MapFactory;
import org.apache.openejb.loader.SystemInstance;
import org.apache.xbean.recipe.ObjectRecipe;
import org.apache.xbean.recipe.Option;
import org.apache.xbean.recipe.UnsetPropertiesRecipe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

// mainly an utility class when using services as config. Null is returned if the action is not possible
// (instead of throwing an exception)
public final class ServiceInfos {
    private ServiceInfos() {
         // no-op
    }

    public static Object resolve(final Collection<ServiceInfo> services, final String id) {
        if (id == null) {
            return null;
        }

        try {
            return build(services, find(services, id));
        } catch (OpenEJBException e) {
            throw new OpenEJBRuntimeException(e);
        }
    }

    public static ServiceInfo find(final Collection<ServiceInfo> services, final String id) {
        for (ServiceInfo s : services) {
            if (id.equals(s.id)) {
                return s;
            }
        }
        return null;
    }

    public static ServiceInfo findByClass(final Collection<ServiceInfo> services, final String clazz) {
        for (ServiceInfo s : services) {
            if (clazz.equals(s.className)) {
                return s;
            }
        }
        return null;
    }

    public static List<Object> resolve(final Collection<ServiceInfo> serviceInfos, final String[] ids) {
        if (ids == null || ids.length == 0) {
            return null;
        }

        final List<Object> instances = new ArrayList<Object>();
        for (String id : ids) {
            Object instance = resolve(serviceInfos, id);
            if (instance == null) {  // maybe id == classname
                try {
                    instance = Thread.currentThread().getContextClassLoader().loadClass(id).newInstance();
                } catch (Exception e) {
                    // ignore
                }
            }

            if (instance != null) {
                instances.add(instance);
            }
        }
        return instances;
    }

    public static Properties serviceProperties(final Collection<ServiceInfo> serviceInfos, final String id) {
        if (id == null) {
            return null;
        }

        final ServiceInfo info = find(serviceInfos, id);
        if (info == null) {
            return null;
        }
        return info.properties;
    }

    private static Object build(final Collection<ServiceInfo> services, final ServiceInfo info) throws OpenEJBException {
        if (info == null) {
            return null;
        }

        final ObjectRecipe serviceRecipe = Assembler.prepareRecipe(info);
        return build(services, info, serviceRecipe);
    }

    public static Object build(final Collection<ServiceInfo> services, final ServiceInfo info, final ObjectRecipe serviceRecipe) {
        if (!info.properties.containsKey("properties")) {
            info.properties.put("properties", new UnsetPropertiesRecipe());
        }

        // we can't ask to have a setter for existing code
        serviceRecipe.allow(Option.FIELD_INJECTION);
        serviceRecipe.allow(Option.PRIVATE_PROPERTIES);

        if (MapFactory.class.getName().equals(info.className)) {
            serviceRecipe.setProperty("prop", info.properties);
        } else {
            for (Map.Entry<Object, Object> entry : info.properties.entrySet()) { // manage links
                final Object value = entry.getValue();
                if (value instanceof String && value.toString().startsWith("$")) {
                    serviceRecipe.setProperty(entry.getKey().toString(), resolve(services, value.toString().substring(1)));
                } else {
                    serviceRecipe.setProperty(entry.getKey().toString(), entry.getValue());
                }
            }
        }

        final Object service = serviceRecipe.create();

        SystemInstance.get().addObserver(service);
        Assembler.logUnusedProperties(serviceRecipe, info);

        return service;
    }
}
