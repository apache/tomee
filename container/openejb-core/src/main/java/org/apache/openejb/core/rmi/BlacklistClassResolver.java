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
package org.apache.openejb.core.rmi;

import java.io.ObjectStreamClass;

public class BlacklistClassResolver {
    public static final BlacklistClassResolver DEFAULT = new BlacklistClassResolver(
        toArray(System.getProperty(
            "tomee.serialization.class.blacklist",
            "org.codehaus.groovy.runtime.,org.apache.commons.collections.functors.,org.apache.xalan,java.lang.Process")),
        toArray(System.getProperty("tomee.serialization.class.whitelist")));

    private final String[] blacklist;
    private final String[] whitelist;

    protected BlacklistClassResolver(final String[] blacklist, final String[] whitelist) {
        this.whitelist = whitelist;
        this.blacklist = blacklist;
    }

    protected boolean isBlacklisted(final String name) {
        return (whitelist != null && !contains(whitelist, name)) || contains(blacklist, name);
    }

    public final ObjectStreamClass check(final ObjectStreamClass classDesc) {
        check(classDesc.getName());
        return classDesc;
    }

    public final String check(final String name) {
        if (isBlacklisted(name)) {
            throw new SecurityException(name + " is not whitelisted as deserialisable, prevented before loading.");
        }
        return name;
    }

    private static String[] toArray(final String property) {
        return property == null ? null : property.split(" *, *");
    }

    private static boolean contains(final String[] list, final String name) {
        if (list != null) {
            for (final String white : list) {
                if (name.startsWith(white)) {
                    return true;
                }
            }
        }
        return false;
    }
}
