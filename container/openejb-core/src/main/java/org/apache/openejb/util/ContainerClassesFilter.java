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

import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.classloader.URLClassLoaderFirst;
import org.apache.xbean.finder.filter.Filter;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

// a bit more brutal than URLClassLoaderFirst.shouldSkip
public class ContainerClassesFilter implements Filter {
    private static final String[] EMPTY_ARRAY = new String[0];

    private final String[] forced;
    private final String[] skipped;
    private final Filter delegateAccept;
    private final Filter delegateSkip;

    public ContainerClassesFilter(final Properties configuration) {
        final String forcedStr = configuration == null ? null : configuration.getProperty("openejb.container.additional.exclude", null);
        final String skippedStr = configuration == null ? null : configuration.getProperty("openejb.container.additional.include", null);
        forced = forcedStr == null ? EMPTY_ARRAY : forcedStr.split(" *, *");
        skipped = skippedStr == null ? EMPTY_ARRAY : skippedStr.split(" *, *");

        final Set<String> excluded = new HashSet<>();
        excluded.add("javax");
        excluded.add("org.apache.myfaces");
        excluded.add("org.apache.cxf");
        excluded.add("org.apache.oro");
        excluded.add("org.apache.ws");
        excluded.add("org.apache.jcp");
        excluded.add("org.apache.openejb");
        excluded.add("org.apache.tomee");
        excluded.add("org.apache.tomcat");
        excluded.add("org.apache.juli");
        excluded.add("org.apache.johnzon");
        excluded.add("org.apache.activemq");
        excluded.add("org.apache.neethi");
        excluded.add("org.apache.xml");
        excluded.add("org.apache.velocity");
        excluded.add("org.apache.wss4j");
        excluded.add("org.apache.commons.logging");
        excluded.add("org.metatype.sxc");
        excluded.add("org.openejb");
        excluded.add("org.slf4j");
        excluded.add("org.fusesource.hawtbuf");
        excluded.add("org.objectweb.howl");
        excluded.add("org.joda.time");
        excluded.add("org.codehaus.stax2");
        excluded.add("org.jvnet.mimepull");
        excluded.add("org.jasypt");
        excluded.add("org.hamcrest");
        excluded.add("org.swizzle");
        excluded.add("com.ctc.wstx");
        excluded.add("com.ibm.wsdl");
        excluded.add("net.sf.ehcache");
        excluded.add("junit");
        excluded.add("org.junit");
        excluded.add("serp");

        final Set<String> included = new HashSet<>();
        included.add("org.apache.myfaces.cdi");
        // included.add("org.apache.myfaces.application.cdi");
        included.add("org.apache.myfaces.flow.cdi");

        delegateSkip = new OptimizedExclusionFilter(excluded);
        delegateAccept = new OptimizedExclusionFilter(included);
    }

    public ContainerClassesFilter() {
        this(SystemInstance.get().getProperties());
    }

    @Override
    public boolean accept(final String name) {
        if (forced != null && startsWith(forced, name)) {
            return true;
        }
        if (skipped != null && startsWith(skipped, name)) {
            return false;
        }
        return delegateAccept.accept(name) || (!delegateSkip.accept(name) && !URLClassLoaderFirst.shouldSkip(name));
    }

    private static boolean startsWith(final String[] array, String name) {
        for (final String prefix : array) {
            if (name.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    public static class OptimizedExclusionFilter implements Filter {
        private final Set<String> included;

        public OptimizedExclusionFilter(final Set<String> exclusions) {
            included = exclusions;
        }

        @Override
        public boolean accept(final String name) {
            int dot = name.indexOf('.');
            while (dot > 0) {
                if (included.contains(name.substring(0, dot))) {
                    return true;
                }
                dot = name.indexOf('.', dot + 1);
            }
            return false;
        }
    }
}
