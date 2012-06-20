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

import java.util.Properties;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.observer.ObserverManager;
import org.apache.openejb.observer.Observes;
import org.apache.openejb.observer.event.ConfigurationReadEvent;
import org.apache.openejb.observer.event.DestroyingEvent;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HooksTest {
    @BeforeClass
    public static void init() {
        SystemInstance.get().getProperties().putAll(new Properties() {{
            setProperty("lifecycle", "new://ServerObservers?name=" + HookLifecycle.class.getName());
        }});
    }

    @AfterClass
    public static void reset() {
        SystemInstance.reset();
    }

    @Test
    public void check() throws OpenEJBException {
        final ConfigurationFactory cf = new ConfigurationFactory();
        cf.getOpenEjbConfiguration(); // load observers
        assertTrue(HookLifecycle.start);
        assertFalse(HookLifecycle.stop);
        SystemInstance.get().getComponent(ObserverManager.class).fireEvent(new DestroyingEvent());
        assertTrue(HookLifecycle.stop);
    }

    public static class HookLifecycle {
        private static boolean start = false;
        private static boolean stop = false;

        public void start(@Observes ConfigurationReadEvent notUsed) {
            start = true;
        }

        public void stop(@Observes DestroyingEvent notUsed) {
            stop = true;
        }
    }
}
