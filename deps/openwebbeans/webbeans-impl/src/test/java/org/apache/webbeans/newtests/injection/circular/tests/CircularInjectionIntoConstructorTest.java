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
package org.apache.webbeans.newtests.injection.circular.tests;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;

import junit.framework.Assert;

import org.apache.webbeans.newtests.AbstractUnitTest;
import org.apache.webbeans.newtests.injection.circular.beans.CircularConstructorOrProducerMethodParameterBean;
import org.apache.webbeans.newtests.injection.circular.beans.CircularNormalInConstructor;
import org.junit.Test;

public class CircularInjectionIntoConstructorTest extends AbstractUnitTest
{
    public CircularInjectionIntoConstructorTest()
    {
        
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testOneNormalOneDependent()
    {
        Collection<String> beanXmls = new ArrayList<String>();
        
        Collection<Class<?>> beanClasses = new ArrayList<Class<?>>();
        beanClasses.add(CircularNormalInConstructor.class);
        beanClasses.add(CircularConstructorOrProducerMethodParameterBean.class);
        
        startContainer(beanClasses, beanXmls);        
        
        Set<Bean<?>> beans = getBeanManager().getBeans("org.apache.webbeans.newtests.injection.circular.beans.CircularNormalInConstructor");
        Assert.assertNotNull(beans);        
        
        Bean<CircularNormalInConstructor> bean = (Bean<CircularNormalInConstructor>)beans.iterator().next();        
        CreationalContext<CircularNormalInConstructor> ctx = getBeanManager().createCreationalContext(bean);
        
        Object reference = getBeanManager().getReference(bean, CircularNormalInConstructor.class, ctx);
        
        Assert.assertTrue(reference instanceof CircularNormalInConstructor);
        
        CircularNormalInConstructor beanInstance = (CircularNormalInConstructor)reference;
        beanInstance.sayHello();
        
        beans = getBeanManager().getBeans("org.apache.webbeans.newtests.injection.circular.beans.CircularConstructorOrProducerMethodParameterBean");
        Assert.assertNotNull(beans);        
        Bean<CircularConstructorOrProducerMethodParameterBean> bean2 = (Bean<CircularConstructorOrProducerMethodParameterBean>)beans.iterator().next();
                
        CreationalContext<CircularConstructorOrProducerMethodParameterBean> ctx2 = getBeanManager().createCreationalContext(bean2);
        
        reference = getBeanManager().getReference(bean2, CircularConstructorOrProducerMethodParameterBean.class, ctx2);
        
        Assert.assertTrue(reference instanceof CircularConstructorOrProducerMethodParameterBean);
        
        CircularConstructorOrProducerMethodParameterBean beanInstance2 = (CircularConstructorOrProducerMethodParameterBean)reference;
        Assert.assertTrue(beanInstance2.getSAYHELLO());
        
        reference = getBeanManager().getReference(bean, CircularNormalInConstructor.class, ctx);
        
        Assert.assertTrue(reference instanceof CircularNormalInConstructor);
        
        beanInstance = (CircularNormalInConstructor)reference;
        
        Assert.assertTrue(beanInstance.getSAYHELLO());
        
        shutDownContainer();
    }
 
}
