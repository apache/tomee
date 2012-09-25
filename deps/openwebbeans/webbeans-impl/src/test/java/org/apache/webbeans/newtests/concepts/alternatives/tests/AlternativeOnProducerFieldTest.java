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
package org.apache.webbeans.newtests.concepts.alternatives.tests;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.util.AnnotationLiteral;

import junit.framework.Assert;

import org.apache.webbeans.newtests.AbstractUnitTest;
import org.apache.webbeans.newtests.concepts.alternatives.common.Pen;
import org.apache.webbeans.newtests.concepts.alternatives.common.Pencil;
import org.apache.webbeans.newtests.concepts.alternatives.common.PencilProducerBean;
import org.junit.Test;

public class AlternativeOnProducerFieldTest extends AbstractUnitTest
{
   private static final String PACKAGE_NAME = AlternativeOnProducerFieldTest.class.getPackage().getName(); 
    
    public AlternativeOnProducerFieldTest()
    {
        
    }
    
    @Test
    public void testProducerFieldAlternativeNotEnabled()
    {

        Collection<Class<?>> beanClasses = new ArrayList<Class<?>>();
        beanClasses.add(PencilProducerBean.class);
        beanClasses.add(Pencil.class);
        
        startContainer(beanClasses, null);

        Set<Bean<?>> beans = getBeanManager().getBeans(Pencil.class, new AnnotationLiteral<Pen>(){});
        Assert.assertEquals(0, beans.size());

        Assert.assertNull(getBeanManager().resolve(beans));

        shutDownContainer();
    }

    @Test
    public void testProducerFieldAlternativeEnabled()
    {
        Collection<String> beanXmls = new ArrayList<String>();
        beanXmls.add(getXmlPath(PACKAGE_NAME, "AlternativeOnProducerFieldTest"));

        Collection<Class<?>> beanClasses = new ArrayList<Class<?>>();
        beanClasses.add(PencilProducerBean.class);
        beanClasses.add(Pencil.class);

        startContainer(beanClasses, beanXmls);

        Set<Bean<?>> beans = getBeanManager().getBeans(Pencil.class, new AnnotationLiteral<Pen>(){});
        Assert.assertEquals(1, beans.size());
        Pencil pencil = getInstance(Pencil.class, new AnnotationLiteral<Pen>(){});
        Assert.assertNotNull(pencil);
        Assert.assertEquals(42, pencil.getNr());

        beans = getBeanManager().getBeans(PencilProducerBean.class);
        Assert.assertEquals(1, beans.size());

        shutDownContainer();
    }

}
