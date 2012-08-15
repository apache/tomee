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

import org.apache.openejb.config.sys.ServiceProvider;
import org.apache.openejb.util.SuperProperties;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A few principles guide this new implementation of ServiceProvider tracking
 *
 * 1. Never expose the raw datastructure underneath so providers
 * must be registered via the "front door" and can't be slipped
 * in without proper parent provider resolution.
 *
 * 2. How to locate providers from disk or parse xml
 * is completely abstracted from this implementation.
 *
 *
 * @version $Rev$ $Date$
 */
public class ProviderManager {

    private final List<String> namespaces = new LinkedList<String>();
    private final Map<ID, ServiceProvider> providers = new LinkedHashMap<ID, ServiceProvider>();
    private final ProviderLoader loader;

    public ProviderManager(ProviderLoader loader) {
        this.loader = loader;
    }

    public ServiceProvider get(String namespace, String name) {
        final ID id = new ID(namespace, name);
        return getProvider(id, new LinkedHashSet<ID>());
    }

    public List<ServiceProvider> getAll() {
        return new ArrayList<ServiceProvider>(providers.values());
    }

    public void register(String namespace, ServiceProvider provider) {
        if (provider == null) throw new IllegalArgumentException("provider cannot be null");

        final ID id = new ID(namespace, provider.getId());

        register(id, provider, new LinkedHashSet<ID>());
    }

    public List<ServiceProvider> load(String namespace) {
        if (namespace == null) throw new IllegalArgumentException("namespace cannot be null");

        namespace = namespace.toLowerCase();

        if (!namespaces.contains(namespace)) {
            namespaces.add(namespace);

            { // load
                final ArrayList<ServiceProvider> list = new ArrayList<ServiceProvider>(loader.load(namespace));
                for (ServiceProvider provider : list) {
                    register(namespace, provider);
                }
            }
        }

        final List<ServiceProvider> providers = new ArrayList<ServiceProvider>();
        for (Map.Entry<ID, ServiceProvider> entry : this.providers.entrySet()) {
            if (entry.getKey().getNamespace().equals(namespace)) {
                providers.add(entry.getValue());
            }
        }

        return providers;
    }

    private void register(ID id, ServiceProvider provider, Set<ID> seen) {
        if (providers.containsKey(id)) return;

        if (provider.getParent() != null) {

            final ID parentId = ID.parse(provider.getParent(), id);

            // Pass in a stack on this call to prevent circular reference
            final ServiceProvider parent = getProvider(parentId, seen);

            if (parent == null) {
                throw new NoSuchParentProviderException(provider, parentId);
            }

            inherit(provider, parent);
        }

        validate(id, provider);

        providers.put(id, provider);
    }

    private void inherit(ServiceProvider child, ServiceProvider parent) {

        if (n(child.getClassName())) child.setClassName(parent.getClassName());
        if (n(child.getConstructor())) child.setConstructor(parent.getConstructor());
        if (n(child.getFactoryName())) child.setFactoryName(parent.getFactoryName());
        if (n(child.getDescription())) child.setDescription(parent.getDescription());
        if (n(child.getDisplayName())) child.setDisplayName(parent.getDisplayName());
        if (n(child.getService())) child.setService(parent.getService());

        { // types
            final Set<String> types = new HashSet<String>();
            types.addAll(parent.getTypes());
            types.addAll(child.getTypes());

            child.getTypes().clear();
            child.getTypes().addAll(types);
        }

        { // properties
            final SuperProperties properties = new SuperProperties();
            properties.putAll(parent.getProperties());
            properties.putAll(child.getProperties());

            child.getProperties().clear();
            child.getProperties().putAll(properties);
        }
    }

    private boolean n(String s) {
        return s == null || s.length() == 0;
    }

    private ServiceProvider getProvider(ID id, final Set<ID> seen) {
        if (seen.contains(id)) throw new ProviderCircularReferenceException(seen);
        seen.add(id);

        { // Already loaded?
            final ServiceProvider provider = providers.get(id);
            if (provider != null) return provider;
        }


        { // Can we load it?
            final ServiceProvider provider = loader.load(id);
            if (provider != null) {
                register(id, provider, seen);
                return provider;
            }
        }

        return null;
    }

    private void validate(ID id, ServiceProvider provider) {
        id.validate();

        if (provider.getService() == null) {
            throw new InvalidProviderDeclarationException("'service' attribute cannot be null", id, provider);
        }
        // TODO - validate provider
    }

    public ProviderLoader getLoader() {
        return loader;
    }


}
