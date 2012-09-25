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
package org.apache.webbeans.test.unittests.exception.initializer;

import org.apache.webbeans.test.TestContext;
import org.apache.webbeans.test.component.exception.initializer.BrokenInitializer1;
import org.apache.webbeans.test.component.exception.initializer.BrokenInitializer2;
import org.apache.webbeans.test.component.exception.initializer.BrokenInitializer3;
import org.apache.webbeans.test.component.exception.initializer.BrokenInitializer4;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class BrokenInitializerTest extends TestContext
{
    public BrokenInitializerTest()
    {
        super(BrokenInitializerTest.class.getName());
    }
    
    @Before
    public void init()
    {
    }
    
    @Test
    public void broken1()
    {
        Exception e = null;
        
        try
        {
            defineManagedBean(BrokenInitializer1.class);
            
        }catch(Exception e1)
        {
            System.out.println(e1.getMessage());
            e = e1;
        }
        
        Assert.assertNotNull(e);
    }
    
    @Test
    public void broken2()
    {
        Exception e = null;
        
        try
        {
            defineManagedBean(BrokenInitializer2.class);
            
        }catch(Exception e1)
        {
            System.out.println(e1.getMessage());
            e = e1;
        }
        
        Assert.assertNotNull(e);
    }

    @Test
    public void broken3()
    {
        Exception e = null;
        
        try
        {
            defineManagedBean(BrokenInitializer3.class);
            
        }catch(Exception e1)
        {
            System.out.println(e1.getMessage());
            e = e1;
        }
        
        Assert.assertNotNull(e);
    }

    @Test
    public void broken4()
    {
        Exception e = null;
        
        try
        {
            defineManagedBean(BrokenInitializer4.class);
            
        }catch(Exception e1)
        {
            System.out.println(e1.getMessage());
            e = e1;
        }
        
        Assert.assertNotNull(e);
    }

  }
