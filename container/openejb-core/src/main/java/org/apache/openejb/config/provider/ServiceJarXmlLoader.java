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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.config.provider;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.config.sys.JaxbOpenejb;
import org.apache.openejb.config.sys.ServiceProvider;
import org.apache.openejb.config.sys.ServicesJar;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @version $Rev$ $Date$
 */
public class ServiceJarXmlLoader implements ProviderLoader {

    private final Map<ID, ServiceProvider> loaded = new LinkedHashMap<ID, ServiceProvider>();
    private final List<String> namespaces = new LinkedList<String>();

    @Override
    public ServiceProvider load(ID id) {
        id.validate();

        { // Already loaded and waiting?
            final ServiceProvider provider = loaded.remove(id);
            if (provider != null) return provider;
        }

        final String namespace = id.getNamespace();

        if (namespaces.contains(namespace)) return null;
        namespaces.add(namespace);

        parse(namespace);

        return load(id);
    }

    private void parse(String namespace) {
        try {// Load and try again

            final ServicesJar servicesJar = JaxbOpenejb.readServicesJar(namespace);
            for (ServiceProvider provider : servicesJar.getServiceProvider()) {
                final ID found = new ID(namespace, provider.getId());
                loaded.put(found, provider);
            }
        } catch (OpenEJBException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public List<ServiceProvider> load(String namespace) {

        if (!namespaces.contains(namespace)) {
            namespaces.add(namespace);

            parse(namespace);
        }

        final List<ServiceProvider> list = new ArrayList<ServiceProvider>();
        for (Map.Entry<ID, ServiceProvider> entry : loaded.entrySet()) {
            if (entry.getKey().getNamespace().equals(namespace)) {
                list.add(entry.getValue());
            }
        }
        return list;
    }
}
