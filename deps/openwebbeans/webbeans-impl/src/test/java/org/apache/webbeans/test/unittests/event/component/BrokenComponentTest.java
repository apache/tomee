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
package org.apache.webbeans.test.unittests.event.component;

import junit.framework.Assert;

import org.apache.webbeans.test.TestContext;
import org.apache.webbeans.test.component.event.broken.BrokenObserverComponent1;
import org.apache.webbeans.test.component.event.broken.BrokenObserverComponent2;
import org.apache.webbeans.test.component.event.broken.BrokenObserverComponent3;
import org.apache.webbeans.test.component.event.broken.BrokenObserverComponent4;
import org.apache.webbeans.test.component.event.broken.BrokenObserverComponent5;
import org.apache.webbeans.test.component.event.broken.BrokenObserverComponent6;
import org.junit.Before;
import org.junit.Test;

public class BrokenComponentTest extends TestContext
{
    public BrokenComponentTest()
    {
        super(BrokenComponentTest.class.getName());
    }

    @Before
    public void init()
    {
        super.init();
    }

    @Test
    public void test1()
    {
        Exception exc = null;

        try
        {
            defineManagedBean(BrokenObserverComponent1.class);

        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            exc = e;
        }

        Assert.assertNull(exc);
    }

    @Test
    public void test2()
    {
        Exception exc = null;

        try
        {
            defineManagedBean(BrokenObserverComponent2.class);

        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            exc = e;
        }

        Assert.assertNotNull(exc);
    }

    @Test
    public void test3()
    {
        Exception exc = null;

        try
        {
            defineManagedBean(BrokenObserverComponent3.class);

        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            exc = e;
        }

        Assert.assertNotNull(exc);
    }

    @Test
    public void test4()
    {
        Exception exc = null;

        try
        {
            defineManagedBean(BrokenObserverComponent4.class);

        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            exc = e;
        }

        Assert.assertNotNull(exc);
    }

    @Test
    public void test5()
    {
        Exception exc = null;

        try
        {
            defineManagedBean(BrokenObserverComponent5.class);

        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            exc = e;
        }

        Assert.assertNotNull(exc);
    }

    @Test
    public void test6()
    {
        Exception exc = null;

        try
        {
            defineManagedBean(BrokenObserverComponent6.class);

        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            exc = e;
        }

        Assert.assertNotNull(exc);
    }

}
