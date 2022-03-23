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
package org.apache.openejb.junit;

import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.testing.*;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import jakarta.inject.Inject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ContainerApplicationRuleTest {
    private final ApplicationRule applicationRule = new ApplicationRule(new App());

    @Rule
    public final TestRule rule = RuleChain.outerRule(new ContainerRule(new Container())).around(applicationRule);

    @Test
    public void run() {
        assertTrue(SystemInstance.isInitialized());
        assertNotNull(SystemInstance.get().getComponent(Assembler.class));
        assertNotNull(SystemInstance.get().getComponent(ConfigurationFactory.class));
        assertNotNull(applicationRule.getInstance(App.class));
        assertEquals("ok", applicationRule.getInstance(App.class).bean.val());
    }

    @ContainerProperties(@ContainerProperties.Property(name = "test-ContainerApplicationRuleTest", value = "success"))
    public static class Container {
    }
    @org.apache.openejb.testing.Classes(cdi = true, innerClassesAsBean = true)
    public static class App {
        @Inject
        private App.ABean bean;

        public static class ABean {
            String val() {
                return "ok";
            }
        }
    }
}
