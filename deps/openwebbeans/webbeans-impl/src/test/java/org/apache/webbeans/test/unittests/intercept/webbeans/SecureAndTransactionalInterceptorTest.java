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

import javax.enterprise.inject.spi.Bean;

import junit.framework.Assert;

import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.context.ContextFactory;
import org.apache.webbeans.test.TestContext;
import org.apache.webbeans.test.component.intercept.webbeans.SecureAndTransactionalComponent;
import org.apache.webbeans.test.component.intercept.webbeans.SecureAndTransactionalInterceptor;
import org.junit.Before;
import org.junit.Test;

public class SecureAndTransactionalInterceptorTest extends TestContext
{
    public SecureAndTransactionalInterceptorTest()
    {
        super(SecureAndTransactionalInterceptorTest.class.getName());
    }
    
    @Before
    public void init()
    {
        initDefaultStereoTypes();
        initializeInterceptorType(SecureAndTransactionalInterceptor.class);
    }

    @Test
    public void testSecureAndTransactionalInterceptor()
    {
        ContextFactory contextFactory = WebBeansContext.getInstance().getContextFactory();
        contextFactory.initSessionContext(null);
        defineInterceptor(SecureAndTransactionalInterceptor.class);
        
        Bean<SecureAndTransactionalComponent> bean = defineManagedBean(SecureAndTransactionalComponent.class);
        SecureAndTransactionalComponent payment = getManager().getInstance(bean);
        
        Assert.assertFalse(SecureAndTransactionalComponent.getCALL());
        
        payment.pay();
        
        Assert.assertTrue(SecureAndTransactionalComponent.getCALL());


        contextFactory.destroySessionContext(null);
    }
}
