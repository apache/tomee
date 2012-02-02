/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.tomee.loader;

import org.apache.tomcat.JarScannerCallback;
import org.apache.tomcat.util.scan.StandardJarScanner;

import javax.servlet.ServletContext;
import java.util.Set;

public class TomEEJarScanner extends StandardJarScanner {
    @Override
    public void scan(ServletContext context, ClassLoader classLoader, JarScannerCallback callback, Set<String> jarsToIgnore) {
        if ("FragmentJarScannerCallback".equals(callback.getClass().getSimpleName())) {
            EmbeddedJarScanner embeddedJarScanner = new EmbeddedJarScanner();
            embeddedJarScanner.scan(context, classLoader, callback, jarsToIgnore);
        } else {
            super.scan(context, classLoader, callback, jarsToIgnore);
        }

//        String openejbWar = System.getProperty("tomee.war");
//
//        if (openejbWar == null) {
//            EmbeddedJarScanner embeddedJarScanner = new EmbeddedJarScanner();
//            embeddedJarScanner.scan(context, classLoader, callback, jarsToIgnore);
//            return;
//        }
//
//        Set<String> newIgnores = new HashSet<String>();
//        if (jarsToIgnore != null) {
//            newIgnores.addAll(jarsToIgnore);
//        }
//
//        if (openejbWar != null && "FragmentJarScannerCallback".equals(callback.getClass().getSimpleName())) {
//            File openejbApp = new File(openejbWar);
//            File libFolder = new File(openejbApp, "lib");
//            for (File f : libFolder.listFiles()) {
//                if (f.getName().toLowerCase().endsWith(".jar")) {
//                    newIgnores.add(f.getName());
//                }
//            }
//        }
//
//        super.scan(context, classLoader, callback, newIgnores);
    }
}
