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
package org.apache.webbeans.newtests.interceptors.business.tests;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;

import junit.framework.Assert;

import org.apache.webbeans.newtests.AbstractUnitTest;
import org.apache.webbeans.newtests.interceptors.business.common.WithInheritedStereoTypeInterceptorBean;
import org.apache.webbeans.newtests.interceptors.common.SecureInterceptor;
import org.apache.webbeans.newtests.interceptors.common.TransactionInterceptor;
import org.junit.Test;

public class WithInheritedStereoTypeInterceptorTest extends AbstractUnitTest
{
    private static final String PACKAGE_NAME = WithStereoTypeInterceptorTest.class.getPackage().getName();
    
    public WithInheritedStereoTypeInterceptorTest()
    {
        
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testInheritedStereoTypeWihtInterceptorBinding()
    {
        Collection<String> beanXmls = new ArrayList<String>();
        beanXmls.add(getXmlPath(PACKAGE_NAME, "WithInheritedStereoTypeInterceptorTest"));
        
        Collection<Class<?>> beanClasses = new ArrayList<Class<?>>();
        beanClasses.add(TransactionInterceptor.class);
        beanClasses.add(SecureInterceptor.class);
        beanClasses.add(WithInheritedStereoTypeInterceptorBean.class);
        
        startContainer(beanClasses, beanXmls);       
        
        Set<Bean<?>> beans = getBeanManager().getBeans("org.apache.webbeans.newtests.interceptors.business.common.WithInheritedStereoTypeInterceptorBean");
        Assert.assertNotNull(beans);        
        Bean<WithInheritedStereoTypeInterceptorBean> bean = (Bean<WithInheritedStereoTypeInterceptorBean>)beans.iterator().next();
        
        CreationalContext<WithInheritedStereoTypeInterceptorBean> ctx = getBeanManager().createCreationalContext(bean);
        
        Object reference = getBeanManager().getReference(bean, WithInheritedStereoTypeInterceptorBean.class, ctx);
        Assert.assertNotNull(reference);
        
        Assert.assertTrue(reference instanceof WithInheritedStereoTypeInterceptorBean);
        
        WithInheritedStereoTypeInterceptorBean beanInstance = (WithInheritedStereoTypeInterceptorBean)reference;
        
        beanInstance.businessMethod();
        
        Assert.assertTrue(TransactionInterceptor.ECHO);
        Assert.assertTrue(SecureInterceptor.ECHO);
        
        shutDownContainer();
        
    }

}
