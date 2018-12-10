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
package org.apache.openejb.loader;

import java.net.URLClassLoader;

import static java.lang.ClassLoader.getSystemClassLoader;

public class ClassPathFactory {

    @SuppressWarnings("checkstyle:needbraces")
    public static ClassPath createClassPath(final String name) {
        if (name.equalsIgnoreCase("tomcat")) return new TomcatClassPath();
        if (name.equalsIgnoreCase("tomcat-common")) return new TomcatClassPath();
        if (name.equalsIgnoreCase("tomcat-system")) return new TomcatClassPath();
        if (name.equalsIgnoreCase("tomcat-webapp")) return new WebAppClassPath();
        if (name.equalsIgnoreCase("bootstrap") && isSystemSupported()) return new SystemClassPath();
        if (name.equalsIgnoreCase("system") && isSystemSupported()) return new SystemClassPath();
        if (name.equalsIgnoreCase("thread")) return new ContextClassPath();
        if (name.equalsIgnoreCase("context")) return new ContextClassPath();
        return new ContextClassPath();
    }

    private static boolean isSystemSupported() {
        return URLClassLoader.class.isInstance(getSystemClassLoader());
    }
}
