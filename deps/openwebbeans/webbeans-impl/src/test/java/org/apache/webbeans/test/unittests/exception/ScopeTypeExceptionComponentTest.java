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
package org.apache.webbeans.test.unittests.exception;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.spi.Bean;

import junit.framework.Assert;

import org.apache.webbeans.exception.WebBeansConfigurationException;
import org.apache.webbeans.test.TestContext;
import org.apache.webbeans.test.component.exception.stero.ComponentDefaultScopeWithDifferentScopeSteros;
import org.apache.webbeans.test.component.exception.stero.ComponentDefaultScopeWithNonScopeStero;
import org.apache.webbeans.test.component.exception.stero.ComponentNonDefaultScopeWithDifferentScopeSteros;
import org.apache.webbeans.test.component.exception.stero.ComponentWithDefaultScopeStero;
import org.apache.webbeans.test.component.exception.stero.ComponentWithDifferentScopeSteros;
import org.apache.webbeans.test.component.exception.stero.ComponentWithNonScopeStero;
import org.apache.webbeans.test.component.exception.stero.ComponentWithSameScopeSteros;
import org.apache.webbeans.test.component.exception.stero.ComponentWithoutScopeStero;
import org.junit.Before;
import org.junit.Test;

public class ScopeTypeExceptionComponentTest extends TestContext
{

    public ScopeTypeExceptionComponentTest()
    {
        super(ScopeTypeExceptionComponentTest.class.getName());
    }

    @Before
    public void init()
    {
        super.init();
    }

    @Test
    public void testComponentWithNonScopeStero()
    {
        clear();
        defineManagedBean(ComponentWithNonScopeStero.class);
        Bean<?> bean = getComponents().get(0);

        Assert.assertEquals(Dependent.class, bean.getScope());
    }

    @Test
    public void testComponentDefaultScopeWithNonScopeStero()
    {
        clear();
        defineManagedBean(ComponentDefaultScopeWithNonScopeStero.class);
        Bean<?> bean = getComponents().get(0);

        Assert.assertEquals(SessionScoped.class, bean.getScope());
    }

    @Test
    public void testComponentWithDefaultScopeStero()
    {
        clear();
        defineManagedBean(ComponentWithDefaultScopeStero.class);
        Bean<?> bean = getComponents().get(0);

        Assert.assertEquals(RequestScoped.class, bean.getScope());
    }

    @Test
    public void testComponentWithDifferentScopeSteros()
    {
        clear();
        try
        {
            defineManagedBean(ComponentWithDifferentScopeSteros.class);
        }
        catch (WebBeansConfigurationException e)
        {
            System.out.println("got expected exception: " + e.getMessage());
            return; // all ok!
        }
        Assert.fail("expecting an exception!");
    }

    @Test
    public void testComponentWithoutScopeStero()
    {
        clear();
        defineManagedBean(ComponentWithoutScopeStero.class);
        Bean<?> bean = getComponents().get(0);

        Assert.assertEquals(Dependent.class, bean.getScope());
    }

    @Test
    public void testComponentWithSameScopeSteros()
    {
        clear();
        defineManagedBean(ComponentWithSameScopeSteros.class);
        Bean<?> bean = getComponents().get(0);

        Assert.assertEquals(SessionScoped.class, bean.getScope());
    }

    @Test
    public void testComponentDefaultScopeWithDifferentScopeSteros()
    {
        clear();
        defineManagedBean(ComponentDefaultScopeWithDifferentScopeSteros.class);
        Bean<?> bean = getComponents().get(0);

        Assert.assertEquals(SessionScoped.class, bean.getScope());
    }

    @Test
    public void testComponentNonDefaultScopeWithDifferentScopeSteros()
    {
        clear();
        try
        {
            defineManagedBean(ComponentNonDefaultScopeWithDifferentScopeSteros.class);
        }
        catch (WebBeansConfigurationException e)
        {
            System.out.println("got expected exception: " + e.getMessage());
            return; // all ok!
        }
        Assert.fail("expecting an exception!");
    }

}
