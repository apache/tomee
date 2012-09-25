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
package org.apache.webbeans.test.unittests.intercept;

import java.util.List;

import javax.enterprise.inject.spi.BeanManager;

import junit.framework.Assert;

import org.apache.webbeans.component.AbstractOwbBean;
import org.apache.webbeans.component.ManagedBean;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.context.ContextFactory;
import org.apache.webbeans.intercept.InterceptorData;
import org.apache.webbeans.test.TestContext;
import org.apache.webbeans.test.component.CheckWithCheckPayment;
import org.apache.webbeans.test.component.PreDestroyComponent;
import org.junit.Before;
import org.junit.Test;

public class PreDestroyComponentTest extends TestContext
{
    BeanManager container = null;

    public PreDestroyComponentTest()
    {
        super(PreDestroyComponentTest.class.getSimpleName());
    }

    @Before
    public void init()
    {
        this.container = WebBeansContext.getInstance().getBeanManagerImpl();
        super.init();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testTypedComponent() throws Throwable
    {
        clear();

        defineManagedBean(CheckWithCheckPayment.class);
        defineManagedBean(PreDestroyComponent.class);
        List<AbstractOwbBean<?>> comps = getComponents();

        ContextFactory contextFactory = WebBeansContext.getInstance().getContextFactory();
        contextFactory.initRequestContext(null);

        Assert.assertEquals(2, comps.size());

        CheckWithCheckPayment object = (CheckWithCheckPayment)getManager().getInstance(comps.get(0));
        PreDestroyComponent object2 = (PreDestroyComponent)getManager().getInstance(comps.get(1));
        
        object2.getP();

        Assert.assertTrue(object instanceof CheckWithCheckPayment);
        Assert.assertTrue(object2 instanceof PreDestroyComponent);

        PreDestroyComponent pcc = object2;
        CheckWithCheckPayment payment = (CheckWithCheckPayment) pcc.getP();
        payment.setValue(true);

        ManagedBean<PreDestroyComponent> s = (ManagedBean<PreDestroyComponent>) comps.get(1);
        List<InterceptorData> stack = s.getInterceptorStack();

        Assert.assertEquals(2, stack.size());

        Assert.assertNotNull(pcc.getP());
        Assert.assertSame(object.getValue(), payment.getValue());

        Assert.assertFalse(PreDestroyComponent.isDestroyed());

        contextFactory.destroyRequestContext(null);

        Assert.assertTrue(PreDestroyComponent.isDestroyed());
    }

}
