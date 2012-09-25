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
import org.apache.webbeans.newtests.injection.circular.beans.CircularApplicationScopedBean;
import org.apache.webbeans.newtests.injection.circular.beans.CircularDependentScopedBean;
import org.junit.Test;

public class CircularInjectionTest extends AbstractUnitTest
{
    public CircularInjectionTest()
    {
        
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testOneNormalOneDependent()
    {
        Collection<String> beanXmls = new ArrayList<String>();
        
        Collection<Class<?>> beanClasses = new ArrayList<Class<?>>();
        beanClasses.add(CircularDependentScopedBean.class);
        beanClasses.add(CircularApplicationScopedBean.class);
        
        startContainer(beanClasses, beanXmls);

        CircularApplicationScopedBean beanInstance = getInstance(CircularApplicationScopedBean.class);
        beanInstance.callDependent();
        
        Assert.assertTrue(CircularDependentScopedBean.success);
        Assert.assertTrue(CircularApplicationScopedBean.success);
        
        shutDownContainer();
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testOneDependentOneNormal()
    {
        Collection<String> beanXmls = new ArrayList<String>();
        
        Collection<Class<?>> beanClasses = new ArrayList<Class<?>>();
        beanClasses.add(CircularDependentScopedBean.class);
        beanClasses.add(CircularApplicationScopedBean.class);
        
        startContainer(beanClasses, beanXmls);

        CircularDependentScopedBean reference = getInstance(CircularDependentScopedBean.class);
        Assert.assertTrue(reference instanceof CircularDependentScopedBean);

        reference.callAppScoped();
                
        Assert.assertTrue(CircularDependentScopedBean.success);
        Assert.assertTrue(CircularApplicationScopedBean.success);
        
        shutDownContainer();
    }
}
