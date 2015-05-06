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
package org.apache.openejb.config;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;

import static java.util.Collections.emptyEnumeration;
import static java.util.Collections.enumeration;
import static java.util.Collections.singleton;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class DeploymentsResolverTest {
    @Test
    public void avoidDuplicatedJars() throws MalformedURLException {
        final File metaInf = new File("target/DeploymentsResolverTest/avoidDuplicatedJars/META-INF");
        metaInf.getParentFile().mkdirs();

        final URL url = metaInf.getParentFile().toURI().toURL();
        final ClassLoader loader = new URLClassLoader(new URL[] {url}, new ClassLoader() {
            @Override
            protected Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
                throw new ClassNotFoundException();
            }

            @Override
            public Enumeration<URL> getResources(final String name) throws IOException {
                return emptyEnumeration();
            }
        }) {
            @Override
            public Enumeration<URL> getResources(final String name) throws IOException {
                if ("META-INF".equals(name)) {
                    return emptyEnumeration();
                }
                return enumeration(singleton(new URL("jar:file:/tmp/app.jar!/")));
            }

            @Override
            public URL[] getURLs() {
                try {
                    return new URL[] { new URL("file:/tmp/app.jar") };
                } catch (final MalformedURLException e) {
                    fail();
                    throw new IllegalStateException(e);
                }
            }
        };
        assertEquals(1, new DeploymentsResolver.ClasspathSearcher().loadUrls(loader).getUrls().size());
    }
}
