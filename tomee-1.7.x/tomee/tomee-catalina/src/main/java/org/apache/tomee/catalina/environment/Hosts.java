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
package org.apache.tomee.catalina.environment;

import org.apache.catalina.Host;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class Hosts implements Iterable<Host> {
    private final Map<String, Host> hosts = new TreeMap<String, Host>();
    private String defaultHost = "localhost";

    public void add(final Host host) {
        hosts.put(host.getName(), host);
    }

    public Map<String, Host> all() {
        return hosts;
    }

    public Host get(final String name) {
        return hosts.get(name);
    }

    public void setDefault(final String aDefault) {
        this.defaultHost = aDefault;
    }

    public String getDefaultHost() {
        return defaultHost;
    }

    public Host getDefault() {
        return hosts.get(defaultHost);
    }

    @Override
    public Iterator<Host> iterator() {
        return new ArrayList<Host>(hosts.values()).iterator();
    }
}
