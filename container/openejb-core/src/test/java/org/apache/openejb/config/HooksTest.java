/**
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

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.event.AssemblerCreated;
import org.apache.openejb.assembler.classic.event.AssemblerDestroyed;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.observer.Observes;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HooksTest {
    @BeforeClass
    public static void init() {
        SystemInstance.get().getProperties().putAll(new Properties() {{
            setProperty("lifecycle", "new://Service?class=" + HookLifecycle.class.getName());
        }});
    }

    @AfterClass
    public static void reset() {
        SystemInstance.reset();
    }

    @Test
    public void check() throws OpenEJBException {

        SystemInstance.get().addObserver(new HookLifecycle());

        assertFalse(HookLifecycle.start);
        assertFalse(HookLifecycle.stop);

        final Assembler assembler = new Assembler();


        assertTrue(HookLifecycle.start);
        assertFalse(HookLifecycle.stop);

        assembler.destroy();


        assertTrue(HookLifecycle.start);
        assertTrue(HookLifecycle.stop);
    }

    public static class HookLifecycle {
        private static boolean start = false;
        private static boolean stop = false;

        public void start(@Observes AssemblerCreated notUsed) {
            start = true;
        }

        public void stop(@Observes AssemblerDestroyed notUsed) {
            stop = true;
        }
    }
}
