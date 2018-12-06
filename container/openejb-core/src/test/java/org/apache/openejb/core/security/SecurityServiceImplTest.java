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
package org.apache.openejb.core.security;

import org.apache.openejb.util.ArrayEnumeration;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class SecurityServiceImplTest {
    @Test
    public void plusInPath() throws Exception {
        final AtomicReference<String> path = new AtomicReference<>();
        final ClassLoader jaasLoader = new URLClassLoader(new URL[0]) {
            @Override
            public Enumeration<URL> getResources(final String name) throws IOException {
                return new ArrayEnumeration(Collections.singletonList(new URL("file:/tmp/jaas/folder+with+plus/login.config")));
            }
        };
        Thread.currentThread().setContextClassLoader(jaasLoader);
        try {
            final Method mtd = SecurityServiceImpl.class.getDeclaredMethod("installJaas");
            mtd.setAccessible(true);
            mtd.invoke(null);
            final String config = System.getProperty("java.security.auth.login.config");
            assertEquals("file:/tmp/jaas/folder+with+plus/login.config", config);
        } finally {
            Thread.currentThread().setContextClassLoader(jaasLoader.getParent());
        }
    }
}
