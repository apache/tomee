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
package org.apache.webbeans.newtests.injection.typeliteral;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.util.TypeLiteral;


import org.apache.webbeans.newtests.AbstractUnitTest;
import org.junit.Test;

public class TypeLiteralTest extends AbstractUnitTest
{
    @SuppressWarnings("serial")
    @Test
    public void testDependentProducerMethodInjectionPoint()
    {
        Collection<String> beanXmls = new ArrayList<String>();
        
        Collection<Class<?>> beanClasses = new ArrayList<Class<?>>();
        beanClasses.add(Pencil.class);
        beanClasses.add(XXPencil.class);
        beanClasses.add(YYPencil.class);
        
        startContainer(beanClasses, beanXmls);    
                
        Set<Bean<?>> bean = getBeanManager().getBeans(new TypeLiteral<Pencil<Ypencil>>(){}.getType(), new Annotation[0]);
        System.out.println(bean.size());
        
        shutDownContainer();

    }
}
