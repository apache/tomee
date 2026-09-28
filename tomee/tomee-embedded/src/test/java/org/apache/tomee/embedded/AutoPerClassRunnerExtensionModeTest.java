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
package org.apache.tomee.embedded;

import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.testing.Application;
import org.apache.openejb.testing.RandomPort;
import org.apache.tomee.embedded.junit.jupiter.RunWithTomEEEmbedded;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

// the container follows the lifecycle of the test instance, so it is started once for the test class
@RunWithTomEEEmbedded
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AutoPerClassRunnerExtensionModeTest {
    // an application instance is created for each container start
    private static final Set<Object> APPLICATIONS = Collections.newSetFromMap(new IdentityHashMap<>());

    @Application
    private RunnerExtensionModeApp app;

    @RandomPort("http")
    private int port;

    @Test
    public void first() {
        assertStarted();
    }

    @Test
    public void second() {
        assertStarted();
    }

    @Test
    public void third() {
        assertStarted();
    }

    @AfterAll
    public static void containers() {
        assertEquals(1, APPLICATIONS.size());
    }

    private void assertStarted() {
        assertNotNull(SystemInstance.get().getComponent(Assembler.class));
        assertNotNull(app);
        assertNotEquals(0, app.getPort());
        assertEquals(app.getPort(), port);
        APPLICATIONS.add(app);
    }
}
