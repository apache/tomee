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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.server.httpd;

import org.apache.webbeans.web.lifecycle.test.MockServletContext;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EmbeddedServletContext extends MockServletContext {
    private final Map<String, Object> attributes = new ConcurrentHashMap<>();

    @Override
    public int getMajorVersion() {
        return 3;
    }

    @Override
    public int getEffectiveMajorVersion() {
        return 3;
    }

    @Override
    public String getVirtualServerName() {
        return "openejb";
    }

    @Override
    public void setAttribute(final String name, final Object object) {
        attributes.put(name, object);
    }

    @Override
    public Object getAttribute(final String name) {
        return attributes.get(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(attributes.keySet());
    }
}
