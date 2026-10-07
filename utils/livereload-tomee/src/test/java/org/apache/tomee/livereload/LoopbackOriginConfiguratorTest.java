/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.apache.tomee.livereload;

import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LoopbackOriginConfiguratorTest {
    private final LoopbackOriginConfigurator configurator = new LoopbackOriginConfigurator();

    @After
    public void reset() {
        Instances.get().setAllowAnyOrigin(false);
    }

    @Test
    public void acceptsMissingOrigin() {
        assertTrue(configurator.checkOrigin(null));
        assertTrue(configurator.checkOrigin(""));
    }

    @Test
    public void acceptsLoopbackOrigins() {
        assertTrue(configurator.checkOrigin("http://localhost:8080"));
        assertTrue(configurator.checkOrigin("https://LOCALHOST"));
        assertTrue(configurator.checkOrigin("http://127.0.0.1:8080"));
        assertTrue(configurator.checkOrigin("http://[::1]:8080"));
    }

    @Test
    public void acceptsBrowserExtensions() {
        assertTrue(configurator.checkOrigin("chrome-extension://abcdefghijklmnop"));
        assertTrue(configurator.checkOrigin("moz-extension://0f8cf3a6-6c8e-4b2a-8e5d-9f4a4b2f1c3d"));
    }

    @Test
    public void rejectsOtherOrigins() {
        assertFalse(configurator.checkOrigin("http://example.com"));
        assertFalse(configurator.checkOrigin("https://example.com:35729"));
        assertFalse(configurator.checkOrigin("http://localhost.example.com"));
        assertFalse(configurator.checkOrigin("http://192.168.1.10:8080"));
        assertFalse(configurator.checkOrigin("null"));
        assertFalse(configurator.checkOrigin("file://localhost"));
        assertFalse(configurator.checkOrigin("not a uri ::"));
        assertFalse(configurator.checkOrigin("http://"));
    }

    @Test
    public void acceptsAnyOriginWhenAllowed() {
        Instances.get().setAllowAnyOrigin(true);
        assertTrue(configurator.checkOrigin("http://example.com"));
        assertTrue(configurator.checkOrigin("http://192.168.1.10:8080"));
    }
}
