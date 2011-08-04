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
package org.apache.openejb.util;

import org.apache.openejb.loader.JarLocation;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.Collections;
import java.net.URI;

public class LinkResolver<E> {
    private final Map<URI, E> byFullName = new TreeMap<URI, E>();
    private final Map<String, Collection<E>> byShortName = new TreeMap<String, Collection<E>>();

    public boolean add(String modulePackageName, String name, E value) {
        return add(URI.create(modulePackageName), name, value);
    }

    public boolean add(URI moduleURI, String name, E value) {
        URI uri = resolve(moduleURI, name);

        if (byFullName.containsKey(uri)) {
            // entry already exists
            return false;
        }

        // Full name: modulePackageName#name -> value
        byFullName.put(uri, value);

        // Short name: name -> List(values)
        Collection<E> values = byShortName.get(name);
        if (values == null) {
            values = new ArrayList<E>();
            byShortName.put(name, values);
        }
        values.add(value);

        return true;
    }

    private URI resolve(URI moduleURI, String name) {
        name = name.replaceAll(" ", "%20");
        URI uri = moduleURI.resolve("#" + name);
        return uri;
    }

    public Collection<E> values() {
        return byFullName.values();
    }

    public Collection<E> values(String shortName) {
        Collection<E> es = byShortName.get(shortName);
        return es != null? es: Collections.EMPTY_LIST;
    }

    public E resolveLink(String link, String modulePackageName) {
        URI moduleURI = URI.create(modulePackageName);
        return resolveLink(link, moduleURI);
    }

    public E resolveLink(String link, URI moduleUri) {
        if (!link.contains("#")) {
            // check for a name in the current module
            E value = null;
            if (moduleUri != null && !moduleUri.toString().isEmpty()) {
                value = byFullName.get(resolve(moduleUri, link));
            }
            if (value != null) {
                return value;
            }

            // check for single value using short name
            Collection<E> values = byShortName.get(link);
            if (values == null || values.size() != 1) {
                return null;
            }
            value = values.iterator().next();
            return value;
        } else if (moduleUri != null) {
            // full (absolute) name
            URI uri = moduleUri.resolve(link);
            E value = byFullName.get(uri);
            return value;
        } else {
            // Absolute reference in a standalone module
            return null;
        }
    }

    protected E getUniqueMember() {
        if (byFullName.size() == 1) {
            return byFullName.values().iterator().next();
        } else {
            return null;
        }
    }

}
