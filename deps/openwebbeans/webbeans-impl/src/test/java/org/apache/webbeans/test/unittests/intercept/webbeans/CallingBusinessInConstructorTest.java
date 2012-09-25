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
package org.apache.webbeans.test.unittests.intercept.webbeans;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.spi.Bean;

import junit.framework.Assert;

import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.context.ContextFactory;
import org.apache.webbeans.context.RequestContext;
import org.apache.webbeans.test.TestContext;
import org.apache.webbeans.test.component.intercept.webbeans.CallBusinessInConstructorBean;
import org.apache.webbeans.test.component.intercept.webbeans.SecureInterceptor;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class CallingBusinessInConstructorTest extends TestContext
{
    public CallingBusinessInConstructorTest()
    {
        super(CallingBusinessInConstructorTest.class.getName());
    }
    
    @Before
    public void init()
    {
        super.init();        
    }
    
    @After
    public void after()
    {
        SecureInterceptor.CALL = false;
    }
    
    @Test
    public void testCallBusinessInConstructor()
    {
        ContextFactory contextFactory = WebBeansContext.getInstance().getContextFactory();
        contextFactory.initRequestContext(null);

        clear();      
        
        SecureInterceptor.CALL = false;
        initializeInterceptorType(SecureInterceptor.class);
        
        defineInterceptor(SecureInterceptor.class);
        Bean<CallBusinessInConstructorBean> bean = defineManagedBean(CallBusinessInConstructorBean.class);
        CallBusinessInConstructorBean instance = (CallBusinessInConstructorBean) getInstanceByName("callBusinessInConstructorBean");
        
        Assert.assertNotNull(instance);
        
        Assert.assertTrue(SecureInterceptor.CALL);

        contextFactory.destroyRequestContext(null);

        SecureInterceptor.CALL = false;

        contextFactory.initRequestContext(null);

        RequestContext ctx = (RequestContext) contextFactory.getStandardContext(RequestScoped.class);
                
        Assert.assertNull(ctx.get(bean));
                
        instance = (CallBusinessInConstructorBean) getInstanceByName("callBusinessInConstructorBean");
        
        Assert.assertNotNull(instance);
        
        Assert.assertTrue(SecureInterceptor.CALL);

        contextFactory.destroyRequestContext(null);

    }

}
