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
package org.apache.openejb.assembler.classic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.net.URI;

public class LinkResolver<E> {
    private final Map<String, E> byFullName = new TreeMap<String, E>();
    private final Map<String, Collection<E>> byShortName = new TreeMap<String, Collection<E>>();

    public boolean add(String moduleId, String name, E value) {
        String fullName = moduleId + "#" + name;
        if (byFullName.containsKey(fullName)) {
            // entry already exists
            return false;
        }

        // Full name: moduleId#name -> value
        byFullName.put(fullName, value);

        // Short name: name -> List(values)
        Collection<E> values = byShortName.get(name);
        if (values == null) {
            values = new ArrayList<E>();
            byShortName.put(name, values);
        }
        values.add(value);

        return true;
    }

    public E resolveLink(String link, String moduleId) {
        URI moduleURI = URI.create(moduleId);
        return resolveLink(link, moduleURI);
    }

    public E resolveLink(String link, URI moduleUri) {
        if (!link.contains("#")) {
            // check for a name in the current module
            E value = byFullName.get(moduleUri.toString() + "#" + link);
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
            String fullName = moduleUri.resolve(link).toString();
            E value = byFullName.get(fullName);
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
