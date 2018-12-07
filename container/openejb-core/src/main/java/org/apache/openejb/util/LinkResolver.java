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

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;


public class LinkResolver<E> {
    private final Map<URI, E> byFullName = new TreeMap<>();
    private final Map<String, Collection<E>> byShortName = new TreeMap<>();

    public boolean add(final String modulePackageName, final String name, final E value) {
        return add(URLs.uri(modulePackageName), name, value);
    }

    public boolean add(final URI moduleURI, final String name, final E value) {
        final URI uri = resolve(moduleURI, name);

        if (byFullName.containsKey(uri)) {
            // entry already exists
            return false;
        }

        // Full name: modulePackageName#name -> value
        byFullName.put(uri, value);

        // Short name: name -> List(values)
        Collection<E> values = byShortName.get(name);
        if (values == null) {
            values = new ArrayList<>();
            byShortName.put(name, values);
        }
        values.add(value);

        return true;
    }

    private URI resolve(final URI moduleURI, String name) {
        name = name.replace(" ", "%20").replace("#", "%23");
        return moduleURI.resolve("#" + name);
    }

    public Collection<E> values() {
        return byFullName.values();
    }

    public Collection<E> values(final String shortName) {
        final Collection<E> es = byShortName.get(shortName);
        return es != null ? es : Collections.EMPTY_LIST;
    }

    public E resolveLink(final String link, final String modulePackageName) {
        final URI moduleURI = URLs.uri(modulePackageName);
        return resolveLink(link, moduleURI);
    }

    public E resolveLink(final String link, final URI moduleUri) {
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
            if (values == null) {
                return null;
            }
            if (values.size() > 1) {
                values = tryToResolveForEar(values, moduleUri, link);
            }
            if (values.size() != 1) {
                return null;
            }
            value = values.iterator().next();
            return value;
        } else if (moduleUri != null) {
            // full (absolute) name
            final URI uri = moduleUri.resolve(link);
            return byFullName.get(uri);
        } else {
            // Absolute reference in a standalone module
            return null;
        }
    }

    // mainly to let children add some check here
    protected Collection<E> tryToResolveForEar(final Collection<E> values, final URI moduleUri, final String link) {
        return values;
    }

    protected E getUniqueMember() {
        if (byFullName.size() == 1) {
            return byFullName.values().iterator().next();
        } else {
            return null;
        }
    }
}
