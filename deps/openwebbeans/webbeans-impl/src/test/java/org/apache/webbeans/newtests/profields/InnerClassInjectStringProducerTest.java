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
package org.apache.webbeans.newtests.profields;

import java.util.ArrayList;
import java.util.Collection;

import javax.enterprise.inject.spi.Bean;

import junit.framework.Assert;

import org.apache.webbeans.newtests.AbstractUnitTest;
import org.apache.webbeans.newtests.profields.beans.stringproducer.StringProducerBean;
import org.apache.webbeans.newtests.profields.innerClass.InnerClassInjectStringProducer;
import org.apache.webbeans.newtests.profields.innerClass.InnerClassInjectStringProducer.Xsimple;
import org.junit.Test;

public class InnerClassInjectStringProducerTest extends AbstractUnitTest
{
    public InnerClassInjectStringProducerTest()
    {
        
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testInnerClassProducerInjection()
    {
        Collection<String> beanXmls = new ArrayList<String>();
        
        Collection<Class<?>> beanClasses = new ArrayList<Class<?>>();
        beanClasses.add(StringProducerBean.class);
        beanClasses.add(InnerClassInjectStringProducer.class);
        beanClasses.add(Xsimple.class);
        
        startContainer(beanClasses, beanXmls);   
        
        Bean<Xsimple> bean = (Bean<Xsimple>) getBeanManager().getBeans("Xsimple").iterator().next();
        Xsimple simple = (Xsimple) getBeanManager().getReference(bean, Xsimple.class, getBeanManager().createCreationalContext(bean));
        
        Assert.assertNotNull(simple.getInner());
        
        shutDownContainer();
    }

}
