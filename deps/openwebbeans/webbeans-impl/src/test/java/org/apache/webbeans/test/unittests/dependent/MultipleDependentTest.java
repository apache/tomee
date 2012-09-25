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
package org.apache.webbeans.test.unittests.dependent;

import junit.framework.Assert;

import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.test.TestContext;
import org.apache.webbeans.test.component.dependent.DependentComponent;
import org.apache.webbeans.test.component.dependent.MultipleDependentComponent;
import org.junit.Before;
import org.junit.Test;

public class MultipleDependentTest extends TestContext
{
    public MultipleDependentTest()
    {
        super(MultipleDependentTest.class.getName());
    }
    
    
    @Before
    public void init()
    {
        initDependentContext();
    }
    
    @Test
    public void testMultipleDependent()
    {
        clear();
        WebBeansContext.getInstance().getContextFactory().initRequestContext(null);

        defineManagedBean(DependentComponent.class);
        defineManagedBean(MultipleDependentComponent.class);
        
        MultipleDependentComponent bean = (MultipleDependentComponent)getManager().getInstance(getComponents().get(1));
        
        Assert.assertNotNull(bean.get1());
        Assert.assertNotNull(bean.get2());
        
        Assert.assertNotSame(bean.get1(), bean.get2());
    }

}
