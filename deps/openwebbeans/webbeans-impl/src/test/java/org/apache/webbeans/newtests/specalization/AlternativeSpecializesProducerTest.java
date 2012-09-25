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
package org.apache.webbeans.newtests.specalization;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.util.AnnotationLiteral;

import junit.framework.Assert;

import org.apache.webbeans.newtests.AbstractUnitTest;
import org.junit.Test;

public class AlternativeSpecializesProducerTest extends AbstractUnitTest
{

    private static final String PACKAGE_NAME = AlternativeSpecializesProducerTest.class.getPackage().getName();

    @Test
    @SuppressWarnings("unchecked")
    public void testAlternativeSpecializeBean()
    {
        Collection<String> beanXmls = new ArrayList<String>();
        beanXmls.add(getXmlPath(PACKAGE_NAME, "AlternativeSpecializesProducer"));
        
        Collection<Class<?>> beanClasses = new ArrayList<Class<?>>();
        beanClasses.add(Pen.class);
        beanClasses.add(DefaultPenProducer.class);
        beanClasses.add(AdvancedPenProducer.class);
        beanClasses.add(PremiumPenProducer.class);

        startContainer(beanClasses, beanXmls);        

        Annotation[] anns = new Annotation[1];
        anns[0] = new AnnotationLiteral<QualifierSpecialized>()
        {
        };


        Set<Bean<?>> beans = getBeanManager().getBeans(IPen.class, anns);
        Assert.assertTrue(beans.size() == 1);
        Bean<IPen> bean = (Bean<IPen>)beans.iterator().next();
        CreationalContext<IPen> cc = getBeanManager().createCreationalContext(bean);
        IPen pen = (IPen) getBeanManager().getReference(bean, IPen.class, cc);
        Assert.assertTrue(pen.getID().contains("premium"));
        
        shutDownContainer();
    }

}
