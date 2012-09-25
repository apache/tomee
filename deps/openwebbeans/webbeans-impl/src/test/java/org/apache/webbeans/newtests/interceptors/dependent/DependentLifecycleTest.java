/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.webbeans.newtests.interceptors.dependent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;

import junit.framework.Assert;

import org.apache.webbeans.newtests.AbstractUnitTest;
import org.junit.Test;

public class DependentLifecycleTest extends AbstractUnitTest
{

    @Test
    @SuppressWarnings("unchecked")
    public void testLifecycle()
    {
        Collection<String> beanXmls = new ArrayList<String>();
        
        Collection<Class<?>> beanClasses = new ArrayList<Class<?>>();
        beanClasses.add(DependentLifecycleBean.class);
        
        startContainer(beanClasses, beanXmls);        
        
        Set<Bean<?>> beans = getBeanManager().getBeans("org.apache.webbeans.newtests.interceptors.dependent.DependentLifecycleBean");
        Assert.assertNotNull(beans);        
        Bean<DependentLifecycleBean> bean = (Bean<DependentLifecycleBean>)beans.iterator().next();
        
        CreationalContext<DependentLifecycleBean> ctx = getBeanManager().createCreationalContext(bean);

        Object reference = getBeanManager().getReference(bean, DependentLifecycleBean.class, ctx);
        Assert.assertNotNull(reference);
        
        Assert.assertTrue(reference instanceof DependentLifecycleBean);

        Assert.assertEquals(1, DependentLifecycleBean.value);
        Assert.assertTrue(DependentSuperBean.SC);
        Assert.assertTrue(MyExtraSuper.SC);
                        
        ctx.release();
        
        shutDownContainer();
        
        Assert.assertEquals(0, DependentLifecycleBean.value);
        Assert.assertFalse(DependentSuperBean.SC);
        Assert.assertFalse(MyExtraSuper.SC);
        
    }
}
