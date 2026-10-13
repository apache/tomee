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

import org.apache.xbean.finder.filter.Filters;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NewLoaderLogicTest {
    @Test
    public void ensureExclusions() throws Exception {
        assertTrue(NewLoaderLogic.skip("openejb-core"));
        assertTrue(NewLoaderLogic.skip("openejb-core.jar"));
        assertTrue(NewLoaderLogic.skip("openejb-core-12345.jar"));
        assertTrue(NewLoaderLogic.skip("tomee-catalina-12345.jar"));
        assertFalse(NewLoaderLogic.skip("openejb-noexclude.jar"));
        assertFalse(NewLoaderLogic.skip("business-foo-1.2.3.jar"));
    }

    @Test
    public void reportWebappJarsDroppedByContainerExclusions() throws Exception {
        final File base = new File("target/NewLoaderLogicTest");
        final File lib = new File(base, "app/WEB-INF/lib");
        final URL collidingAppJar = new File(lib, "ant-reporting-web.jar").toURI().toURL();
        final URL appJar = new File(lib, "business.jar").toURI().toURL();
        final URL explicitlyExcludedAppJar = new File(lib, "internal-tools.jar").toURI().toURL();
        final URL containerJar = new File(base, "tomee/lib/ant-1.10.15.jar").toURI().toURL();
        final URL[] webUrls = {collidingAppJar, appJar, explicitlyExcludedAppJar, containerJar};

        final File exclusions = new File(base, "app/WEB-INF/exclusions.list");
        exclusions.getParentFile().mkdirs();
        Files.write(exclusions.toPath(), "internal-\n".getBytes(StandardCharsets.UTF_8));

        final List<URL> scannable = DeploymentLoader.filterWebappUrls(webUrls, null, exclusions.toURI().toURL());
        assertEquals(Collections.singletonList(appJar), scannable);

        // only the application jar dropped by the container list is reported, not the one excluded by the app itself
        assertEquals(Collections.singletonList("ant-reporting-web.jar (prefix 'ant-')"),
                new ArrayList<>(DeploymentLoader.builtinExcludedWebappJars(webUrls, scannable, Filters.prefixes("internal-"))));
    }
}
