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

import org.apache.openejb.api.configuration.PersistenceUnitDefinition;
import org.apache.openejb.testing.ContainerProperties;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class ContainerAndApplicationRulesTest {
    private final ContainerRule instanceContainer = new ContainerRule(new Container());
    private final ApplicationRule instanceServer = new ApplicationRule(new App());

    @Rule
    public final TestRule rule = RuleChain.outerRule(instanceContainer).around(instanceServer);

    @Test
    public void test() {
        assertNotNull(instanceServer.getInstance(App.class).v);
        assertNull(instanceContainer.getInstance(Container.class).ignored);
    }

    @ContainerProperties(@ContainerProperties.Property(name = "openejb.conf.file", value = ContainerProperties.Property.IGNORED))
    @org.apache.openejb.testing.Classes(cdi = true, value = Ignored.class) // @Classes invalid for a container
    public static class Container {
        @Inject
        private Provider<Ignored> ignored;
    }

    @PersistenceUnitDefinition
    @org.apache.openejb.testing.Classes(context = "App1", cdi = true, value = Valuable.class)
    public static class App {
        @Inject
        private Valuable v;
    }

    public static class Ignored {
    }

    public static class Valuable {
    }
}
