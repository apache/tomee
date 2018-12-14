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
package org.apache.tomee.catalina;

import org.apache.openejb.core.ThreadContext;

import javax.naming.LinkRef;
import javax.naming.NamingException;
import java.util.Collection;
import java.util.TreeSet;

public class ContextValue extends LinkRef {
    public static final String MODULES_PREFIX = "openejb/modules/";

    private final Collection<String> links = new TreeSet<>();

    public ContextValue(final String linkName) {
        super(linkName);
    }

    @Override
    public synchronized String getLinkName() throws NamingException {
        if (links.size() == 1) {
            return "java:" + links.iterator().next();
        }

        // else try to get BeanContext to get linkname
        final ThreadContext tc = ThreadContext.getThreadContext();
        if (tc != null && tc.getBeanContext() != null) {
            return "java:" + linkName(tc.getBeanContext().getModuleID(), super.getLinkName());
        }

        // TODO: should we parse a stacktrace to get the module?
        throw new NamingException("more than one module binding match this name " + super.getLinkName());
    }

    public void addValue(final String link) {
        links.add(link);
    }

    public boolean hasLink(final String link) {
        return links.contains(link);
    }

    public static String linkName(final String moduleId, final String name) {
        return MODULES_PREFIX + moduleId + "/" + name;
    }
}
