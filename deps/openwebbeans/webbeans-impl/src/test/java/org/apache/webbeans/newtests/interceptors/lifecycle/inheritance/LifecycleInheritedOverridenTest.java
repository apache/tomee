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
package org.apache.webbeans.newtests.interceptors.lifecycle.inheritance;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import junit.framework.Assert;

import org.apache.webbeans.newtests.AbstractUnitTest;
import org.junit.Before;
import org.junit.Test;

public class LifecycleInheritedOverridenTest extends AbstractUnitTest
{
    @Before
    public void before()
    {
        SubClassBean.POST_CONSTRUCT = false;
        SubClassBean.PRE_DESTOY = false;
        
        SuperClassBean.POST_CONSTRUCT = false;
        SuperClassBean.PRE_DESTOY = false;        
    }
    
    @Test
    public void testInheritedOverridenMethods()
    {
        Collection<Class<?>> classes = new ArrayList<Class<?>>();
        classes.add(SubClassBean.class);
        classes.add(SubClassInheritedBean.class);
        
        startContainer(classes);
        
        BeanManager manager = getBeanManager();
        Bean<?> subClassBean = manager.getBeans(SubClassInheritedBean.class, new Annotation[0]).iterator().next();
        
        Object subClassInstance = manager.getReference(subClassBean, SubClassInheritedBean.class, manager.createCreationalContext(subClassBean));
        
        Assert.assertTrue(subClassInstance instanceof SubClassInheritedBean);
        SubClassInheritedBean beanInstance = (SubClassInheritedBean)subClassInstance;
        beanInstance.business();
                
        Assert.assertFalse(SubClassInheritedBean.POST_CONSTRUCT);
        Assert.assertFalse(SubClassInheritedBean.POST_CONSTRUCT);
        
        Assert.assertTrue(SubClassBean.POST_CONSTRUCT);
        Assert.assertTrue(SubClassBean.POST_CONSTRUCT);
        
        Assert.assertFalse(SuperClassBean.POST_CONSTRUCT);
        Assert.assertFalse(SuperClassBean.POST_CONSTRUCT);
        
        shutDownContainer();
        
        Assert.assertFalse(SubClassInheritedBean.PRE_DESTOY);
        Assert.assertFalse(SubClassInheritedBean.PRE_DESTOY);
        
        Assert.assertTrue(SubClassBean.PRE_DESTOY);
        Assert.assertTrue(SubClassBean.PRE_DESTOY);
        
        Assert.assertFalse(SuperClassBean.PRE_DESTOY);
        Assert.assertFalse(SuperClassBean.PRE_DESTOY);
        
    }

}
