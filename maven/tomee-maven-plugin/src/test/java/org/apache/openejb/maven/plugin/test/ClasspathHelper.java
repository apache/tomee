/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.maven.plugin.test;

import java.io.File;

public class ClasspathHelper {

    /**
     * @param jarName e.g. "log4j-api"
     * @return the version of the dependency with the given name
     */
    public static String getJarVersion(String jarName) {
        final String jarPrefix = jarName + "-";
        for (final String entry : System.getProperty("java.class.path", "").split(File.pathSeparator)) {
            if (entry.contains(jarPrefix) && entry.endsWith(".jar")) {
                final String name = new File(entry).getName();
                return name.substring(jarPrefix.length(), name.length() - ".jar".length());
            }
        }
        throw new IllegalStateException(jarName + " jar not found on classpath");
    }
}
