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
package org.apache.webbeans.test.unittests.inject;

import java.util.List;

import junit.framework.Assert;

import org.apache.webbeans.component.AbstractOwbBean;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.context.ContextFactory;
import org.apache.webbeans.test.TestContext;
import org.apache.webbeans.test.component.service.InjectedComponent;
import org.apache.webbeans.test.component.service.ServiceImpl1;
import org.junit.Before;
import org.junit.Test;

public class InjectedComponentTest extends TestContext
{

    public InjectedComponentTest()
    {
        super(InjectedComponentTest.class.getSimpleName());
    }

    @Before
    public void init()
    {
        super.init();
    }

    @Test
    public void testTypedComponent() throws Throwable
    {
        clear();

        defineManagedBean(InjectedComponent.class);
        defineManagedBean(ServiceImpl1.class);
        List<AbstractOwbBean<?>> comps = getComponents();

        ContextFactory contextFactory = WebBeansContext.getInstance().getContextFactory();
        contextFactory.initRequestContext(null);
        contextFactory.initApplicationContext(null);

        Assert.assertEquals(2, comps.size());

        Object object = getManager().getInstance(comps.get(0));

        Assert.assertTrue(object instanceof InjectedComponent);

        contextFactory.destroyApplicationContext(null);
        contextFactory.destroyRequestContext(null);
    }

}
