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

import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.newtests.AbstractUnitTest;
import org.apache.webbeans.newtests.interceptors.business.common.WithInheritedBindingTypeBean;
import org.apache.webbeans.newtests.interceptors.common.SecureInterceptor;
import org.apache.webbeans.newtests.interceptors.common.TransactionInterceptor;
import org.junit.Test;

@SuppressWarnings("unchecked")
public class WithInheritedBindingTypeTest extends AbstractUnitTest
{
    private static final String PACKAGE_NAME = WithInheritedBindingTypeTest.class.getPackage().getName();
    
    @Test
    public void testStereoTypeBasedInterceptor()
    {
        Collection<String> beanXmls = new ArrayList<String>();
        beanXmls.add(getXmlPath(PACKAGE_NAME, "WithInheritedBindingTypeTest"));
        
        Collection<Class<?>> beanClasses = new ArrayList<Class<?>>();
        beanClasses.add(TransactionInterceptor.class);
        beanClasses.add(SecureInterceptor.class);
        beanClasses.add(WithInheritedBindingTypeBean.class);
        
        TransactionInterceptor.count = 0;
        
        startContainer(beanClasses, beanXmls);       
        
        Set<Bean<?>> beans = getBeanManager().getBeans("org.apache.webbeans.newtests.interceptors.business.common.WithInheritedBindingTypeBean");
        Assert.assertNotNull(beans);        
        Bean<WithInheritedBindingTypeBean> bean = (Bean<WithInheritedBindingTypeBean>)beans.iterator().next();                
        
        CreationalContext<WithInheritedBindingTypeBean> ctx = getBeanManager().createCreationalContext(bean);
        
        Object reference = getBeanManager().getReference(bean, WithInheritedBindingTypeBean.class, ctx);
        Assert.assertNotNull(reference);
        
        Assert.assertTrue(reference instanceof WithInheritedBindingTypeBean);
        
        WithInheritedBindingTypeBean beanInstance = (WithInheritedBindingTypeBean)reference;
        
        beanInstance.business();
        
        
        Assert.assertTrue(TransactionInterceptor.ECHO);
        Assert.assertTrue(SecureInterceptor.ECHO);

        WebBeansContext webBeansContext = WebBeansContext.getInstance();
        webBeansContext.getContextFactory().destroyRequestContext(null);
        webBeansContext.getContextFactory().initRequestContext(null);

        reference = getBeanManager().getReference(bean, WithInheritedBindingTypeBean.class, ctx);
        beanInstance = (WithInheritedBindingTypeBean)reference;
        beanInstance.business();
        
        Assert.assertEquals(2, TransactionInterceptor.count);
        
        shutDownContainer();
        
    }
}
