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
package org.apache.openejb;

import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.PersistenceUnitLinkResolver;
import org.apache.openejb.jee.Application;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.apache.openejb.loader.Files;
import org.junit.Test;

import java.io.File;
import java.net.URI;

import static org.junit.Assert.assertNull;

public class PersistenceUnitLinkResolverTest {
    @Test
    public void resolve() {
        final AppModule appModule = new AppModule(Thread.currentThread().getContextClassLoader(), "target/classes/foo", new Application(), false);

        Files.mkdir(new File("target/classes/foo/bar"));

        final PersistenceUnitLinkResolver resolver = new PersistenceUnitLinkResolver(appModule);
        resolver.add(URI.create("file:/fake/1"), "foo", new PersistenceUnit());
        resolver.add(URI.create("file:/fake/2"), "foo", new PersistenceUnit());

        assertNull(resolver.resolveLink("foo", URI.create("bar"))); // can't resolve but doesn't fail
    }
}
