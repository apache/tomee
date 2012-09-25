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
package org.apache.webbeans.newtests.portable.injectiontarget.customtarget;

import java.util.Collections;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.InjectionTarget;

import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.container.BeanManagerImpl;
import org.apache.webbeans.newtests.AbstractUnitTest;
import org.junit.Assert;
import org.junit.Test;

public class CustomTargetNoConstructorTest extends AbstractUnitTest
{
    @Test
    public void testInjectionTargetWithNoConstructor()
    {
        Exception ex = null;
        try
        {
            startContainer(Collections.<Class<?>> emptyList());
            final BeanManagerImpl manager = WebBeansContext.getInstance().getBeanManagerImpl();
            AnnotatedType<CustomTarget> annotatedType = manager.createAnnotatedType(CustomTarget.class);
            InjectionTarget<CustomTarget> injectionTarget = manager.createInjectionTarget(annotatedType);
            CreationalContext<CustomTarget> context = manager.createCreationalContext(null);
            try
            {
                injectionTarget.produce(context);
            }
            catch (Exception e)
            {
                ex = e;
            }
        }
        finally
        {
            shutDownContainer();
        }
        
        Assert.assertNotNull(ex);
    }
    
    @Test
    public void testInjecDependenciesTargetWithNoConstructor()
    {
        Exception ex = null;
        final BeanManagerImpl manager;
        CreationalContext<CustomTarget> context =null;
        CustomTarget instance = null;
        try
        {
            startContainer(Collections.<Class<?>> emptyList());
            manager = WebBeansContext.getInstance().getBeanManagerImpl();
            context = manager.createCreationalContext(null);
            AnnotatedType<CustomTarget> annotatedType = manager.createAnnotatedType(CustomTarget.class);
            InjectionTarget<CustomTarget> injectionTarget = manager.createInjectionTarget(annotatedType);
            try
            {
                instance = new CustomTarget("Hiho");
                injectionTarget.inject(instance, context);
                
                Assert.assertNotNull(instance.getBeanManager());
            }
            catch (Exception e)
            {
                ex = e;
            }
        }
        finally
        {
            context.release();
            shutDownContainer();
        }
        
        Assert.assertNull(ex);
    }
    

}
