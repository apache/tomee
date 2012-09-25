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
package org.apache.webbeans.newtests.portable.injectiontarget.supportInjections;

import java.util.ArrayList;
import java.util.Collection;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.InjectionTarget;

import junit.framework.Assert;

import org.apache.webbeans.newtests.AbstractUnitTest;
import org.junit.Test;

public class SupportInjectionTest extends AbstractUnitTest
{
    class SupportCreational implements CreationalContext<SupportInjectionBean>
    {

        @Override
        public void push(SupportInjectionBean incompleteInstance)
        {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void release()
        {
            // TODO Auto-generated method stub
            
        }
        
    }
    
    @Test
    public void testInjectionTarget()
    {
        Collection<Class<?>> classes = new ArrayList<Class<?>>();
        classes.add(Chair.class);
        classes.add(Table.class);
        startContainer(classes);
        
        InjectionTarget<SupportInjectionBean> model = getBeanManager().createInjectionTarget(getBeanManager().createAnnotatedType(SupportInjectionBean.class));
        SupportCreational cc = new SupportCreational();
        SupportInjectionBean instance = model.produce(cc);
        
        model.inject(instance, cc);
        
        Assert.assertNotNull(instance.getChair());
        Assert.assertNotNull(instance.getTable());
        Assert.assertNotNull(instance.getPerson());
        
        model.postConstruct(instance);
        Assert.assertTrue(SupportInjectionBean.POST_COSTRUCT);
        
        model.preDestroy(instance);
        Assert.assertTrue(SupportInjectionBean.PRE_DESTROY);
        
        shutDownContainer();
        
    }

}
