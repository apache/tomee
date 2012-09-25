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
package org.apache.webbeans.newtests.interceptors.inheritance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;

import junit.framework.Assert;

import org.apache.webbeans.newtests.AbstractUnitTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class InheritedInterceptorTest extends AbstractUnitTest
{
    private static final String PACKAGE_NAME = InheritedInterceptorTest.class.getPackage().getName();
    String beansXMLOrdering = "[InterceptorSimple, InterceptorInherited, InterceptorMethod, InterceptorIncludedByIndirect, InterceptorInheritedIncludedByIndirect]";
    String childBeansXMLOrdering = "[InterceptorInherited, InterceptorMethod, InterceptorIncludedByIndirect, InterceptorInheritedIncludedByIndirect]";
    String methodOnlyXMLOrdering = "[InterceptorMethod]";
    
    Set<Bean<?>> beans = null;
    
    @Before
    public void setUp() { 
        Collection<String> beanXmls = new ArrayList<String>();
        
        beanXmls.add(getXmlPath(PACKAGE_NAME, "InheritedInterceptorTest"));

        Collection<Class<?>> beanClasses = new ArrayList<Class<?>>();

        beanClasses.add(Deck.class);
        beanClasses.add(DeckChild.class);

        beanClasses.add(DeckStereotyped.class);
        beanClasses.add(DeckStereotypedChild.class);

        beanClasses.add(DeckStereotypedNotInherited.class);
        beanClasses.add(DeckStereotypedNotInheritedChild.class);
        beanClasses.add(DeckStereotypedGrandchild.class);

        beanClasses.add(InterceptorSimple.class);
        beanClasses.add(InterceptorInherited.class);
        beanClasses.add(InterceptorIncludedByIndirect.class);
        beanClasses.add(InterceptorInheritedIncludedByIndirect.class);
        beanClasses.add(InterceptorMethod.class);

        startContainer(beanClasses, beanXmls);
        
        this.beans = getBeanManager().getBeans(DeckType.class);
        Assert.assertNotNull("BeanManager.getBeans() returned null", beans);
    }
    
    @After
    public void tearDown() { 
        shutDownContainer();
    }
    
    @Test
    public void testNormalScopeNoNewInstance()
    {
        do_testNormalScopeNoNewInstance();
    }
    
    @Test
    public void testLoopNormalScopeNoNewInstance()
    {
        for (int i = 0; i<100; i++) 
        {
            do_testNormalScopeNoNewInstance();
        }
    }
     
    public void do_testNormalScopeNoNewInstance() 
    { 
        for (Bean<?> bean : beans)
        {
            CreationalContext<?> cc = getBeanManager().createCreationalContext(null);

            DeckType d1 = (DeckType) getBeanManager().getReference(bean, DeckType.class, cc);
            d1.shuffle();
            // Can't reproduce in TC with this call
            // cc.release();
        }
    }    
   
    @Test
    public void testStereotype()
    {
        
        for (Bean<?> bean : beans)
        {
            DeckType d = (DeckType) getBeanManager().getReference(bean, DeckType.class, getBeanManager().createCreationalContext(bean));
            d.shuffle(); // intercepted method

            List<String> interceptors = d.getInterceptors();
            System.out.println(d.toString());

            if (d.getName().equals("DeckStereotyped") || d.getName().equals("DeckStereotypedChild") || d.getName().equals("DeckStereotypedGrandchild") || d.getName().equals("DeckStereotypedNotInherited"))
            {
                Assert.assertEquals(d.getName(), beansXMLOrdering, interceptors.toString());
            }
            else if (d.getName().equals("DeckStereotypedNotInheritedChild"))
            {
                Assert.assertEquals(d.getName(), methodOnlyXMLOrdering, interceptors.toString());
            }
        }
    }
   
    @Test
    public void testInterceptorInheritance()
    {  
        for (Bean<?> bean : beans)
        {
            DeckType d = (DeckType) getBeanManager().getReference(bean, DeckType.class, getBeanManager().createCreationalContext(bean));
            d.shuffle(); // intercepted method

            List<String> interceptors = d.getInterceptors();
            System.out.println(d.toString());

            if (d.getName().equals("Deck"))
            {
                Assert.assertEquals(d.getName(), beansXMLOrdering, interceptors.toString());
            }
            else if (d.getName().equals("DeckChild"))
            {
                Assert.assertEquals(d.getName(), childBeansXMLOrdering, interceptors.toString());
            }
        }
    }
}
