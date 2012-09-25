/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.webbeans.test.unittests.scopes;

import java.util.List;

import junit.framework.Assert;

import org.apache.webbeans.component.AbstractOwbBean;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.context.ContextFactory;
import org.apache.webbeans.test.TestContext;
import org.apache.webbeans.test.component.CheckWithCheckPayment;
import org.apache.webbeans.test.component.producer.ScopeAdaptorComponent;
import org.apache.webbeans.test.component.producer.ScopeAdaptorInjectorComponent;
import org.junit.Before;
import org.junit.Test;

public class ScopeAdapterTest extends TestContext
{
    public ScopeAdapterTest()
    {
        super(ScopeAdapterTest.class.getName());
    }

    @Before
    public void init()
    {
        super.init();

    }

    @Test
    public void testDependent()
    {
        clear();

        defineManagedBean(CheckWithCheckPayment.class);
        defineManagedBean(ScopeAdaptorComponent.class);
        defineManagedBean(ScopeAdaptorInjectorComponent.class);

        Object session = getSession();
        ContextFactory contextFactory = WebBeansContext.getInstance().getContextFactory();
        contextFactory.initRequestContext(null);
        contextFactory.initSessionContext(session);
        contextFactory.initApplicationContext(null);

        List<AbstractOwbBean<?>> comps = getComponents();

        Assert.assertEquals(4, getDeployedComponents());

        getManager().getInstance(comps.get(0));
        getManager().getInstance(comps.get(1));
        getInstanceByName("scope");
        getManager().getInstance(comps.get(2));

        contextFactory.destroyApplicationContext(null);
        contextFactory.destroySessionContext(session);
        contextFactory.destroyRequestContext(null);

    }

}
