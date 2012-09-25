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
package org.apache.webbeans.test.unittests.inject.broken;

import org.apache.webbeans.test.TestContext;
import org.apache.webbeans.test.component.inject.broken.InstanceInjectedBrokenComponent1;
import org.apache.webbeans.test.component.inject.broken.InstanceInjectedBrokenComponent2;
import org.apache.webbeans.test.component.inject.broken.InstanceInjectedBrokenComponent3;
import org.apache.webbeans.test.component.inject.broken.InstanceInjectedBrokenComponent4;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class InjectedInstanceBrokenComponentTest extends TestContext
{
    public InjectedInstanceBrokenComponentTest()
    {
        super(InjectedInstanceBrokenComponentTest.class.getName());
    }

    @Before
    public void init()
    {
        super.init();     
        
    }
    
    @Test
    public void testInjectedInstanceBrokenComponent1()
    {
        Exception e = null;
        
        try
        {
            defineManagedBean(InstanceInjectedBrokenComponent1.class);
            
        }catch(Exception e1)
        {
            e = e1;
            System.out.println(e.getMessage());
        }
        
        Assert.assertNotNull(e);
    }
    
    @Test
    public void testInjectedInstanceBrokenComponent2()
    {
        Exception e = null;
        
        try
        {
            defineManagedBean(InstanceInjectedBrokenComponent2.class);
            
        }catch(Exception e1)
        {
            e = e1;
            System.out.println(e.getMessage());
        }
        
        Assert.assertNull(e);
    }

    @Test
    public void testInjectedInstanceBrokenComponent3()
    {
        Exception e = null;
        
        try
        {
            defineManagedBean(InstanceInjectedBrokenComponent3.class);
            
        }catch(Exception e1)
        {
            e = e1;
            System.out.println(e.getMessage());
        }
        
        Assert.assertNotNull(e);
    }

    @Test
    public void testInjectedInstanceBrokenComponent4()
    {
        Exception e = null;
        
        try
        {
            defineManagedBean(InstanceInjectedBrokenComponent4.class);
            
        }
        catch(Exception e1)
        {
            e = e1;
            System.out.println(e.getMessage());
        }
        
        Assert.assertNotNull(e);
    }

}
