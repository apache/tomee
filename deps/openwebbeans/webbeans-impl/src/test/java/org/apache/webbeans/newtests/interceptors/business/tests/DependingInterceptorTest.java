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
import org.apache.webbeans.newtests.interceptors.beans.ApplicationScopedBean;
import org.apache.webbeans.newtests.interceptors.beans.RequestScopedBean;
import org.apache.webbeans.newtests.interceptors.common.TransactionInterceptor;
import org.junit.Test;

@SuppressWarnings("unchecked")
public class DependingInterceptorTest extends AbstractUnitTest
{
    private static final String PACKAGE_NAME = DependingInterceptorTest.class.getPackage().getName();
    
    @Test
    public void testDependingBeanInterceptor()
    {
        Collection<String> beanXmls = new ArrayList<String>();
        beanXmls.add(getXmlPath(PACKAGE_NAME, "DependingInterceptorTest"));
        
        Collection<Class<?>> beanClasses = new ArrayList<Class<?>>();
        beanClasses.add(TransactionInterceptor.class);
        beanClasses.add(ApplicationScopedBean.class);
        beanClasses.add(RequestScopedBean.class);
        
        TransactionInterceptor.count = 0;
        
        startContainer(beanClasses, beanXmls);       
        
        Set<Bean<?>> beans = getBeanManager().getBeans(RequestScopedBean.class);
        Assert.assertNotNull(beans);        
        Bean<RequestScopedBean> bean = (Bean<RequestScopedBean>)beans.iterator().next();                
        
        CreationalContext<RequestScopedBean> ctx = getBeanManager().createCreationalContext(bean);
        
        Object reference1 = getBeanManager().getReference(bean, RequestScopedBean.class, ctx);
        Assert.assertNotNull(reference1);
        
        Assert.assertTrue(reference1 instanceof RequestScopedBean);

        RequestScopedBean beanInstance1 = (RequestScopedBean)reference1;
        
        TransactionInterceptor.count = 0;
        
        beanInstance1.getMyService().getJ();

        RequestScopedBean realInstance1 = beanInstance1.getInstance();
        
        Assert.assertTrue(TransactionInterceptor.ECHO);
        Assert.assertEquals(1, TransactionInterceptor.count);

        TransactionInterceptor.ECHO = false;

        WebBeansContext webBeansContext = WebBeansContext.getInstance();
        webBeansContext.getContextFactory().destroyRequestContext(null);
        webBeansContext.getContextFactory().initRequestContext(null);

        CreationalContext<RequestScopedBean> ctx2 = getBeanManager().createCreationalContext(bean);
        Object reference2 = getBeanManager().getReference(bean, RequestScopedBean.class, ctx2);
        Assert.assertNotNull(reference2);
        
        Assert.assertTrue(reference2 instanceof RequestScopedBean);
        
        RequestScopedBean beanInstance2 = (RequestScopedBean)reference2;
        beanInstance2.getMyService().getJ();
        RequestScopedBean realInstance2 = beanInstance2.getInstance();

        Assert.assertTrue(TransactionInterceptor.ECHO);

        Assert.assertEquals(2, TransactionInterceptor.count);
        
        Assert.assertNotSame(realInstance1, realInstance2);
        Assert.assertEquals(realInstance1.getMyService().getJ(), realInstance2.getMyService().getJ());
        
        shutDownContainer();
        
    }
    
    @Test
    public void testInterceptorCreation() throws Exception 
    {
            Collection<String> beanXmls = new ArrayList<String>();
            beanXmls.add(getXmlPath(PACKAGE_NAME, "DependingInterceptorTest"));
            
            Collection<Class<?>> beanClasses = new ArrayList<Class<?>>();
            beanClasses.add(TransactionInterceptor.class);
            beanClasses.add(ApplicationScopedBean.class);
            
            TransactionInterceptor.count = 0;
            TransactionInterceptor.interceptorCount = 0;
            
            startContainer(beanClasses, beanXmls);

            Set<Bean<?>> beans = getBeanManager().getBeans(ApplicationScopedBean.class);
            Assert.assertNotNull(beans);        
            Bean<ApplicationScopedBean> bean = (Bean<ApplicationScopedBean>)beans.iterator().next();                
            
            CreationalContext<ApplicationScopedBean> ctx = getBeanManager().createCreationalContext(bean);
            
            Object reference1 = getBeanManager().getReference(bean, ApplicationScopedBean.class, ctx);
            Assert.assertNotNull(reference1);
            
            ApplicationScopedBean app = (ApplicationScopedBean) reference1;

            app.getJ();
            Assert.assertEquals(1, TransactionInterceptor.interceptorCount);
            Assert.assertEquals(1, TransactionInterceptor.count);

            app.getJ();
            Assert.assertEquals(1, TransactionInterceptor.interceptorCount);
            Assert.assertEquals(2, TransactionInterceptor.count);

            app.getJ();
            Assert.assertEquals(1, TransactionInterceptor.interceptorCount);
            Assert.assertEquals(3, TransactionInterceptor.count);
}
}
