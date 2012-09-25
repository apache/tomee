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
package org.apache.webbeans.newtests.promethods;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;

import junit.framework.Assert;

import org.apache.webbeans.component.ProducerMethodBean;
import org.apache.webbeans.newtests.AbstractUnitTest;
import org.apache.webbeans.newtests.promethods.beans.PersonProducerBean;
import org.apache.webbeans.newtests.promethods.common.Person;
import org.junit.Test;

public class PersonProducerTest extends AbstractUnitTest
{
    public PersonProducerTest()
    {
        
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testPersonProducer()
    {
        Collection<String> beanXmls = new ArrayList<String>();
        
        Collection<Class<?>> beanClasses = new ArrayList<Class<?>>();
        beanClasses.add(Person.class);
        beanClasses.add(PersonProducerBean.class);
        
        startContainer(beanClasses, beanXmls);      
        
        Set<Bean<?>> beans = getBeanManager().getBeans("personProducer");
        Assert.assertNotNull(beans);        
        Bean<Person> bean = (Bean<Person>)beans.iterator().next();
        
        Assert.assertTrue(bean instanceof ProducerMethodBean);
        
        CreationalContext<Person> ctx = getBeanManager().createCreationalContext(bean);
        
        Object reference = getBeanManager().getReference(bean, Person.class, ctx);
        Assert.assertNotNull(reference);
        
        Assert.assertTrue(reference instanceof Person);
        
        shutDownContainer();
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testNullPersonProducer()
    {
        Collection<String> beanXmls = new ArrayList<String>();
        
        Collection<Class<?>> beanClasses = new ArrayList<Class<?>>();
        beanClasses.add(Person.class);
        beanClasses.add(PersonProducerBean.class);
        
        startContainer(beanClasses, beanXmls);      
        
        Set<Bean<?>> beans = getBeanManager().getBeans("nullInjectedPersonProducer");
        Assert.assertNotNull(beans);        
        Bean<Person> bean = (Bean<Person>)beans.iterator().next();
        
        Assert.assertTrue(bean instanceof ProducerMethodBean);
        
        CreationalContext<Person> ctx = getBeanManager().createCreationalContext(bean);
        
        Object reference = getBeanManager().getReference(bean, Person.class, ctx);
        Assert.assertNull(reference);
        
        shutDownContainer();
    }
}
