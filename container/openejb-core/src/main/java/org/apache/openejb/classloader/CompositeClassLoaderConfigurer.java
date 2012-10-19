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
package org.apache.openejb.classloader;

import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class CompositeClassLoaderConfigurer implements ClassLoaderConfigurer {
    private final ClassLoaderConfigurer[] composites;
    private final URL[] urls;

    public CompositeClassLoaderConfigurer(final ClassLoaderConfigurer[] configurers) {
        composites = configurers;

        final Set<URL> urlSet = new HashSet<URL>();
        for (ClassLoaderConfigurer configurer : configurers) {
            urlSet.addAll(Arrays.asList(configurer.additionalURLs()));
        }
        urls = urlSet.toArray(new URL[urlSet.size()]);
    }

    @Override
    public URL[] additionalURLs() {
        return urls;
    }

    @Override
    public boolean accept(final URL url) {
        for (ClassLoaderConfigurer configurer : composites) {
            if (!configurer.accept(url)) {
                return false;
            }
        }
        return true;
    }
}
