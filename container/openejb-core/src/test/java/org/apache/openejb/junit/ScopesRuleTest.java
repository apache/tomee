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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.junit;

import org.apache.openejb.testing.ContainerProperties;
import org.apache.webbeans.config.WebBeansContext;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import jakarta.enterprise.context.ContextNotActiveException;
import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;

import static org.junit.Assert.assertTrue;

@ContainerProperties({
        @ContainerProperties.Property(name = "openejb.testing.start-cdi-contexts", value = "false")
})
@org.apache.openejb.testing.Classes(cdi = true, innerClassesAsBean = true)
public class ScopesRuleTest {
    @Rule
    public final TestRule rule = RuleChain.outerRule(new ApplicationComposerRule(this))
            .around(new ScopesRule());

    public static class Foo {
        public void touch() {
            // ok
        }
    }

    @Inject
    private BeanManager beanManager;

    @Test(expected = ContextNotActiveException.class)
    public void scopeDoesNotExist() {
        beanManager.getContext(SessionScoped.class);
    }

    @Test
    @CdiScopes(SessionScoped.class)
    public void scopeExists() {
        assertTrue(WebBeansContext.currentInstance().getContextsService().getCurrentContext(SessionScoped.class).isActive());
    }
}
