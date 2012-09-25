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
package org.apache.webbeans.test.unittests.inject.generic;

import org.apache.webbeans.component.AbstractOwbBean;
import org.apache.webbeans.test.TestContext;
import org.apache.webbeans.test.component.inject.generic.GenericComponent;
import org.apache.webbeans.test.component.inject.generic.GenericComponentInjector;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GenericBeanTest extends TestContext
{
    public GenericBeanTest()
    {
        super(GenericBeanTest.class.getName());
    }

    @Before
    public void init()
    {
        super.init();
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testGenericBeanInjection()
    {
        defineManagedBean(GenericComponent.class);
        AbstractOwbBean<GenericComponentInjector> bean2 = defineManagedBean(GenericComponentInjector.class);
        
        GenericComponentInjector<?> instance = getManager().getInstance(bean2);
        Assert.assertNotNull(instance.getInjection1());
        Assert.assertNotNull(instance.getInjection2());
        Assert.assertNotNull(instance.getInjection3());
        Assert.assertNotNull(instance.getInjection4());
        
        
        
    }
}
