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
package org.apache.openejb.config.event;

import org.apache.openejb.observer.Event;

import java.net.URL;
import java.util.Arrays;

@Event
public class BeforeDeploymentEvent {
    private final URL[] urls;
    private final ClassLoader parentClassLoader;

    public BeforeDeploymentEvent(final URL[] files) {
        this(files, null);
    }

    public BeforeDeploymentEvent(final URL[] files, final ClassLoader parent) {
        urls = files;
        parentClassLoader = parent;
    }

    public URL[] getUrls() {
        return urls;
    }

    public ClassLoader getParentClassLoader() {
        if (parentClassLoader != null) {
            return parentClassLoader;
        }
        return getClass().getClassLoader();
    }

    @Override
    public String toString() {
        return "BeforeDeploymentEvent{" +
                "urls=" + Arrays.asList(urls) +
            '}';
    }
}
