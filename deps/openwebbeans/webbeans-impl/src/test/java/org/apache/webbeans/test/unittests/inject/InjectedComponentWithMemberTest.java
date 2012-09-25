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

import javax.enterprise.inject.spi.BeanManager;

import junit.framework.Assert;

import org.apache.webbeans.component.AbstractOwbBean;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.context.ContextFactory;
import org.apache.webbeans.test.TestContext;
import org.apache.webbeans.test.component.BindingComponent;
import org.apache.webbeans.test.component.NonBindingComponent;
import org.junit.Before;
import org.junit.Test;

public class InjectedComponentWithMemberTest extends TestContext
{
    BeanManager container = null;

    public InjectedComponentWithMemberTest()
    {
        super(InjectedComponentWithMemberTest.class.getSimpleName());
    }

    @Before
    public void init()
    {
        super.init();
        this.container = WebBeansContext.getInstance().getBeanManagerImpl();
    }

    @Test
    public void testTypedComponent() throws Throwable
    {
        clear();
        defineManagedBean(BindingComponent.class);
        defineManagedBean(NonBindingComponent.class);
        List<AbstractOwbBean<?>> comps = getComponents();

        Object session = getSession();
        ContextFactory contextFactory = WebBeansContext.getInstance().getContextFactory();
        contextFactory.initSessionContext(session);

        Assert.assertEquals(2, comps.size());

        getManager().getInstance(comps.get(0));
        Object object = getManager().getInstance(comps.get(1));

        Assert.assertTrue(object instanceof NonBindingComponent);

        NonBindingComponent comp = (NonBindingComponent) object;
        BindingComponent bc = comp.getComponent();

        Assert.assertTrue(bc != null);

        contextFactory.destroySessionContext(session);
    }

}
