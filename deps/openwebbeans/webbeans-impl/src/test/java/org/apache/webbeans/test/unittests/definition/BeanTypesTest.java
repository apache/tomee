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
package org.apache.webbeans.test.unittests.definition;

import java.lang.reflect.Type;
import java.util.Set;

import javax.enterprise.inject.spi.Bean;

import junit.framework.Assert;

import org.apache.webbeans.test.TestContext;
import org.apache.webbeans.test.component.CheckWithCheckPayment;
import org.apache.webbeans.test.component.definition.BeanTypesDefinedBean;
import org.junit.Before;
import org.junit.Test;

public class BeanTypesTest extends TestContext
{
    public BeanTypesTest()
    {
        super(BeanTypesTest.class.getName());
    }
    
    @Before
    public void init()
    {
        super.init();
    }

    @Test
    public void testBeanTypes()
    {
        clear();
        
        Bean<BeanTypesDefinedBean> bean = defineManagedBean(BeanTypesDefinedBean.class);
        Set<Type> apiTypes = bean.getTypes();
        
        Assert.assertEquals(2, apiTypes.size());        
        Assert.assertTrue(apiTypes.contains(BeanTypesDefinedBean.class));
        
        Set<Bean<?>> beans = getManager().getBeans("paymentField");
        Assert.assertEquals(1, beans.size());
        
        Bean<?> pbean = beans.iterator().next();
        apiTypes = pbean.getTypes();
        
        Assert.assertEquals(2, apiTypes.size());        
        Assert.assertTrue(apiTypes.contains(CheckWithCheckPayment.class));
        
        beans = getManager().getBeans("paymentMethod");
        Assert.assertEquals(1, beans.size());
        
        pbean = beans.iterator().next();
        apiTypes = pbean.getTypes();
        
        Assert.assertEquals(2, apiTypes.size());        
        Assert.assertTrue(apiTypes.contains(CheckWithCheckPayment.class));
        
        
        
    }
}
