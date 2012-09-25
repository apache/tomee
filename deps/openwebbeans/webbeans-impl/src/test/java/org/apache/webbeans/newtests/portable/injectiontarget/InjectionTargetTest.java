/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.webbeans.newtests.portable.injectiontarget;

import java.util.ArrayList;
import java.util.Collection;

import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.InjectionTarget;

import junit.framework.Assert;

import org.apache.webbeans.newtests.AbstractUnitTest;
import org.junit.Test;

public class InjectionTargetTest extends AbstractUnitTest
{
    public static class MyContextual<T> implements Contextual<T>
    {

        @Override
        public T create(CreationalContext<T> context)
        {
            return null;
        }

        @Override
        public void destroy(T instance, CreationalContext<T> context)
        {
            
        }
        
    }
    
    @Test
    public void testInjectionTarget()
    {
        Collection<Class<?>> classes = new ArrayList<Class<?>>();
        startContainer(classes);
        
        InjectionTarget<PersonModel> model = getBeanManager().createInjectionTarget(getBeanManager().createAnnotatedType(PersonModel.class));
        PersonModel person = model.produce(getBeanManager().createCreationalContext(new InjectionTargetTest.MyContextual<PersonModel>()));
        Assert.assertNotNull(person);
        
        shutDownContainer();
        
    }

}
