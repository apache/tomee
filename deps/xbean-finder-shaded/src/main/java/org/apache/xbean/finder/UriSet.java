/**
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
package org.apache.xbean.finder;

import org.apache.xbean.finder.filter.Filter;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.apache.xbean.finder.filter.Filters.invert;
import static org.apache.xbean.finder.filter.Filters.patterns;

/**
 * @version $Rev$ $Date$
 */
public class UriSet implements Iterable<URI> {

    private final Map<String, URI> URIs;

    public UriSet(URI... URIs) {
        this(Arrays.asList(URIs));
    }

    public UriSet(Collection<URI> URIs) {
        this.URIs = new HashMap<String, URI>();
        for (URI location : URIs) {
            this.URIs.put(location.toASCIIString(), location);
        }
    }

    private UriSet(Map<String, URI> URIs) {
        this.URIs = URIs;
    }

    public UriSet include(String pattern) {
        return filter(patterns(pattern));
    }

    public UriSet include(UriSet URISet) {
        Map<String, URI> URIs = new HashMap<String, URI>(this.URIs);
        URIs.putAll(URISet.URIs);
        return new UriSet(URIs);
    }


    public UriSet include(URI URI) {
        Map<String, URI> URIs = new HashMap<String, URI>(this.URIs);
        URIs.put(URI.toASCIIString(), URI);
        return new UriSet(URIs);
    }

    public UriSet exclude(UriSet URISet) {
        Map<String, URI> URIs = new HashMap<String, URI>(this.URIs);
        Map<String, URI> parentURIs = URISet.URIs;
        for (String URI : parentURIs.keySet()) {
            URIs.remove(URI);
        }
        return new UriSet(URIs);
    }

    public UriSet exclude(URI URI) {
        Map<String, URI> URIs = new HashMap<String, URI>(this.URIs);
        URIs.remove(URI.toASCIIString());
        return new UriSet(URIs);
    }

    public UriSet exclude(File file) {
        return exclude(relative(file));
    }

    public UriSet exclude(String pattern) {
        return filter(invert(patterns(pattern)));
    }

    public UriSet excludePaths(String pathString) {
        String[] paths = pathString.split(File.pathSeparator);
        UriSet URISet = this;
        for (String path : paths) {
            File file = new File(path);
            URISet = URISet.exclude(file);
        }
        return URISet;
    }

    public UriSet filter(Filter filter) {
        Map<String, URI> URIs = new HashMap<String, URI>();
        for (Map.Entry<String, URI> entry : this.URIs.entrySet()) {
            String URI = entry.getKey();
            if (filter.accept(URI)) {
                URIs.put(URI, entry.getValue());
            }
        }
        return new UriSet(URIs);
    }

    public UriSet matching(String pattern) {
        return filter(patterns(pattern));
    }

    public UriSet relative(File file) {
        String URIPath = file.toURI().toASCIIString();
        Map<String, URI> URIs = new HashMap<String, URI>();
        for (Map.Entry<String, URI> entry : this.URIs.entrySet()) {
            String URI = entry.getKey();
            if (URI.startsWith(URIPath) || URI.startsWith("jar:" + URIPath)) {
                URIs.put(URI, entry.getValue());
            }
        }
        return new UriSet(URIs);
    }

    public List<URI> getURIs() {
        return new ArrayList<URI>(URIs.values());
    }

    public int size() {
        return URIs.size();
    }

    public Iterator<URI> iterator() {
        return getURIs().iterator();
    }

    @Override
    public String toString() {
        return super.toString() + "[" + URIs.size() + "]";
    }
}
