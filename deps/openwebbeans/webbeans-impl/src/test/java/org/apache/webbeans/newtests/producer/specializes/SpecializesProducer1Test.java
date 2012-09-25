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
package org.apache.webbeans.newtests.producer.specializes;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.util.AnnotationLiteral;

import org.apache.webbeans.newtests.AbstractUnitTest;
import org.apache.webbeans.test.annotation.binding.Binding1;
import org.apache.webbeans.test.annotation.binding.Binding2;
import org.apache.webbeans.test.component.producer.specializes.SpecializesProducer1;
import org.apache.webbeans.test.component.producer.specializes.superclazz.SpecializesProducer1SuperClazz;
import org.junit.Assert;
import org.junit.Test;

public class SpecializesProducer1Test extends AbstractUnitTest
{

    @Test
    public void testSpecializedProducer1()
    {
        Collection<String> beanXmls = new ArrayList<String>();
        Collection<Class<?>> beanClasses = new ArrayList<Class<?>>();

        beanClasses.add(SpecializesProducer1SuperClazz.class);
        beanClasses.add(SpecializesProducer1.class);
        
        startContainer(beanClasses, beanXmls);        
        
        Annotation binding1 = new AnnotationLiteral<Binding1>()
        {
        };
        Annotation binding2 = new AnnotationLiteral<Binding2>()
        {
        };

        Set beans = getBeanManager().getBeans(int.class, new Annotation[] { binding1, binding2 });
        System.out.print("Size of the bean set is " + beans.size());
        Assert.assertTrue(beans.size() == 1);
        Bean<Integer> bean = (Bean<Integer>)beans.iterator().next();
        CreationalContext<Integer> cc = getBeanManager().createCreationalContext(bean);
        Integer number = (Integer) getBeanManager().getReference(bean, int.class, cc);
        
        Assert.assertEquals(10000, number.intValue());
        
        shutDownContainer();       
        
    }
}
