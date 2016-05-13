/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

/**
 * @version $Rev$ $Date$
 */
public class EjbObjectInputStream extends ObjectInputStream {
    private static final AtomicReference<BlacklistClassResolver> RESOLVER_ATOMIC_REFERENCE =
        new AtomicReference<>(new BlacklistClassResolver());

    public static void reloadResolverConfig() {
        RESOLVER_ATOMIC_REFERENCE.set(new BlacklistClassResolver());
    }

    public EjbObjectInputStream(final InputStream in) throws IOException {
        super(in);
    }

    @Override
    protected Class<?> resolveClass(final ObjectStreamClass classDesc) throws IOException, ClassNotFoundException {
        final String n = RESOLVER_ATOMIC_REFERENCE.get().check(classDesc.getName());
        final ClassLoader classloader = getClassloader();
        try {
            return Class.forName(n, false, classloader);
        } catch (ClassNotFoundException e) {

            if (n.equals("boolean")) {
                return boolean.class;
            }
            if (n.equals("byte")) {
                return byte.class;
            }
            if (n.equals("char")) {
                return char.class;
            }
            if (n.equals("short")) {
                return short.class;
            }
            if (n.equals("int")) {
                return int.class;
            }
            if (n.equals("long")) {
                return long.class;
            }
            if (n.equals("float")) {
                return float.class;
            }
            if (n.equals("double")) {
                return double.class;
            }

            //Last try - Let runtime try and find it.
            return Class.forName(n, false, null);
        }
    }

    @Override
    protected Class resolveProxyClass(final String[] interfaces) throws IOException, ClassNotFoundException {
        final Class[] cinterfaces = new Class[interfaces.length];
        for (int i = 0; i < interfaces.length; i++) {
            cinterfaces[i] = getClassloader().loadClass(interfaces[i]);
        }

        try {
            return Proxy.getProxyClass(getClassloader(), cinterfaces);
        } catch (IllegalArgumentException e) {
            throw new ClassNotFoundException(null, e);
        }
    }

    ClassLoader getClassloader() {
        return Thread.currentThread().getContextClassLoader();
    }

    public static class BlacklistClassResolver {
        private final String[] blacklist;
        private final String[] whitelist;

        public static final Pattern PRIMITIVE_ARRAY = Pattern.compile("^\\[+[BCDFIJSVZ]$");

        protected BlacklistClassResolver() {
            this(toArray(System.getProperty(
                "tomee.serialization.class.blacklist",
                "org.codehaus.groovy.runtime.,org.apache.commons.collections.functors.,org.apache.xalan,java.lang.Process")),
                toArray(System.getProperty("tomee.serialization.class.whitelist")));
        }

        protected BlacklistClassResolver(final String[] blacklist, final String[] whitelist) {
            this.whitelist = whitelist;
            this.blacklist = blacklist;
        }

        protected boolean isBlacklisted(final String name) {
            // allow primitive arrays
            if (PRIMITIVE_ARRAY.matcher(name).matches()) {
                return false;
            }

            if (name != null && name.startsWith("[L") && name.endsWith(";")) {
                return isBlacklisted(name.substring(2, name.length() - 1));
            }
            return (whitelist != null && !contains(whitelist, name)) || contains(blacklist, name);
        }

        public final String check(final String name) {
            if (isBlacklisted(name)) {
                throw new SecurityException(name + " is not whitelisted as deserialisable, prevented before loading it, " +
                    "customize tomee.serialization.class.blacklist and tomee.serialization.class.whitelist to add it to not fail there. " +
                    "-Dtomee.serialization.class.blacklist=- -Dtomee.serialization.class.whitelist=" + name +
                    " for instance (or in conf/system.properties).");
            }
            return name;
        }

        private static String[] toArray(final String property) {
            return property == null ? null : property.split(" *, *");
        }

        private static boolean contains(final String[] list, String name) {
            if (list != null) {
                for (final String white : list) {
                    if ("*".equals(white) || name.startsWith(white)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }
}
