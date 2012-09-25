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
package org.apache.webbeans.test.unittests.typedliteral;

import java.util.List;
import java.util.Map;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.util.TypeLiteral;

import junit.framework.Assert;

import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.test.TestContext;
import org.apache.webbeans.test.component.literals.InstanceTypeLiteralBean;
import org.apache.webbeans.test.component.literals.InstanceTypeLiteralBean.IntegerOrder;
import org.apache.webbeans.test.component.literals.InstanceTypeLiteralBean.StringOrder;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("unchecked")
public class TypeLiteralTest extends TestContext
{
    public TypeLiteralTest()
    {
        super(TypeLiteralTest.class.getName());
    }
    
    @Before
    public void init()
    {
        super.init();
    }

    public static class Literal1 extends TypeLiteral<Map<String, String>>
    {
        
    }
    
    public TypeLiteral<List<Integer>> literal2 = new TypeLiteral<List<Integer>>(){};
    
    @Test
    public void testLiterals()
    {
        Literal1 literal1 = new Literal1();
        literal1.getRawType().equals(Map.class);
        
        literal2.getRawType().equals(List.class);
    }
    
    @Test
    public void testTypeLiteralInInstance()
    {
        clear();

        WebBeansContext webBeansContext = WebBeansContext.getInstance();
        webBeansContext.getBeanManagerImpl().addInternalBean(webBeansContext.getWebBeansUtil().getInstanceBean());
        
        defineManagedBean(StringOrder.class);
        defineManagedBean(IntegerOrder.class);
        Bean<InstanceTypeLiteralBean> bean =  defineManagedBean(InstanceTypeLiteralBean.class);
        
        Object object = getManager().getReference(bean, InstanceTypeLiteralBean.class,getManager().createCreationalContext(bean) );        
        Assert.assertTrue(object instanceof InstanceTypeLiteralBean);
        InstanceTypeLiteralBean beaninstance = (InstanceTypeLiteralBean)object;
        Object produce = beaninstance.produce(0);
        Assert.assertTrue(produce instanceof Instance);
        
        Instance<IntegerOrder> order = (Instance<IntegerOrder>)produce;
        Assert.assertTrue(order.get() instanceof IntegerOrder);
        
        produce = beaninstance.produce(1);
        Assert.assertTrue(produce instanceof Instance);

        Instance<StringOrder> order2 = (Instance<StringOrder>)produce;
        Assert.assertTrue(order2.get() instanceof StringOrder);
    }
}
