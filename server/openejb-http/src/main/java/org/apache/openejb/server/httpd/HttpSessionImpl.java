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
package org.apache.openejb.server.httpd;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSessionContext;
import org.apache.openejb.client.ArrayEnumeration;

import java.util.*;

public class HttpSessionImpl implements HttpSession {
    private String sessionId = UUID.randomUUID().toString();
    private Map<String, Object> attributes = new HashMap<String, Object>();

    @Override
    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    @Override
    public void removeValue(String s) {
        Iterator<String> it = attributes.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            if (attributes.get(key).equals(s)) {
                attributes.remove(key);
            }
        }
    }

    @Override
    public void invalidate() {
        attributes.clear();
    }

    @Override
    public boolean isNew() {
        return false;
    }

    @Override
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    @Override
    public Object getValue(String s) {
        return attributes.get(s);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return new ArrayEnumeration(new ArrayList(attributes.keySet()));
    }

    @Override
    public String[] getValueNames() {
        return attributes.keySet().toArray(new String[attributes.size()]);
    }

    @Override
    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    @Override
    public void putValue(String s, Object o) {
        setAttribute(s, o);
    }

    @Override
    public long getCreationTime() {
        return -1;
    }

    @Override
    public String getId() {
        return sessionId;
    }

    @Override
    public long getLastAccessedTime() {
        return -1;
    }

    @Override
    public ServletContext getServletContext() {
        return null;
    }

    @Override
    public void setMaxInactiveInterval(int i) {
        // no-op
    }

    @Override
    public int getMaxInactiveInterval() {
        return -1;
    }

    @Override
    public HttpSessionContext getSessionContext() {
        return null;
    }
}
