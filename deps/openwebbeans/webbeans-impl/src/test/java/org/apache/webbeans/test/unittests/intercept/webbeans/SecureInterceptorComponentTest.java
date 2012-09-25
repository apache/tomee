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

import junit.framework.Assert;

import org.apache.webbeans.component.AbstractOwbBean;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.test.TestContext;
import org.apache.webbeans.test.component.intercept.webbeans.SecureComponent;
import org.apache.webbeans.test.component.intercept.webbeans.SecureInterceptor;
import org.junit.Before;
import org.junit.Test;

public class SecureInterceptorComponentTest extends TestContext
{
    public SecureInterceptorComponentTest()
    {
        super(SecureInterceptorComponentTest.class.getName());
    }

    @Before
    public void init()
    {
        initDefaultStereoTypes();
        initializeInterceptorType(SecureInterceptor.class);
    }

    @Test
    public void testSecureInterceptor()
    {
        defineInterceptor(SecureInterceptor.class);
        AbstractOwbBean<SecureComponent> component = defineManagedBean(SecureComponent.class);

        WebBeansContext.getInstance().getContextFactory().initRequestContext(null);

        SecureComponent secureComponent = getManager().getInstance(component);

        Assert.assertNotNull(secureComponent);

        boolean value = secureComponent.checkout();

        Assert.assertTrue(SecureInterceptor.CALL);
        Assert.assertTrue(value);

    }

}
