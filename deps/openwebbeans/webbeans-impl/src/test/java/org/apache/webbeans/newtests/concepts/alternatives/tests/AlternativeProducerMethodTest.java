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

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import javax.enterprise.util.AnnotationLiteral;

import junit.framework.Assert;

import org.apache.webbeans.newtests.AbstractUnitTest;
import org.apache.webbeans.newtests.concepts.alternatives.common.AlternativeOnClassAndProducerMethodBean;
import org.apache.webbeans.newtests.concepts.alternatives.common.AlternativeOnClassOnlyBean;
import org.apache.webbeans.newtests.concepts.alternatives.common.DefaultBeanProducer;
import org.apache.webbeans.newtests.concepts.alternatives.common.DefaultBeanProducerWithoutDisposes;
import org.apache.webbeans.newtests.concepts.alternatives.common.IProducedBean;
import org.apache.webbeans.newtests.concepts.alternatives.common.QualifierProducerBased;
import org.junit.Test;
import org.junit.Ignore;

public class AlternativeProducerMethodTest extends AbstractUnitTest {

    private static final String PACKAGE_NAME = AlternativeProducerMethodTest.class.getPackage().getName();

    @Test
    public void testNotEnabledAlternativeOnClassOnlyBean()
    {
        Collection<Class<?>> beanClasses = new ArrayList<Class<?>>();
        beanClasses.add(DefaultBeanProducer.class);

        // available but not enabled in beans.xml
        beanClasses.add(AlternativeOnClassOnlyBean.class);
        
        startContainer(beanClasses, null);

        Annotation[] anns = new Annotation[1];
        anns[0] = new AnnotationLiteral<QualifierProducerBased>()
        {
        };

        IProducedBean model = getInstance(IProducedBean.class, anns);
        Assert.assertNotNull(model);
        Assert.assertEquals("default", model.getProducerType());

        shutDownContainer();
    }

    @Test
    @Ignore("need to clarify this with the EG")
    //X TODO It's not yet clear how paragraph 5.1.1 is to be interpreted
    public void testAlternativeOnClassOnlyBean()
    {
        Collection<String> beanXmls = new ArrayList<String>();
        beanXmls.add(getXmlPath(PACKAGE_NAME, "AlternativeOnClassOnly"));

        Collection<Class<?>> beanClasses = new ArrayList<Class<?>>();
        beanClasses.add(DefaultBeanProducer.class);
        beanClasses.add(AlternativeOnClassOnlyBean.class);

        startContainer(beanClasses, null);

        Annotation[] anns = new Annotation[1];
        anns[0] = new AnnotationLiteral<QualifierProducerBased>()
        {
        };

        IProducedBean model = getInstance(IProducedBean.class, anns);
        Assert.assertNotNull(model);
        Assert.assertEquals("AlternativeOnClassOnlyBean", model.getProducerType());

        shutDownContainer();
    }


    @Test
    public void testAlternativeOnClassAndProducerMethodBean()
    {
        Collection<String> beanXmls = new ArrayList<String>();
        beanXmls.add(getXmlPath(PACKAGE_NAME, "AlternativeOnClassAndProducerMethod"));

        Collection<Class<?>> beanClasses = new ArrayList<Class<?>>();
        beanClasses.add(IProducedBean.class);
        beanClasses.add(DefaultBeanProducerWithoutDisposes.class);

        // available but not enabled in beans.xml
        beanClasses.add(AlternativeOnClassAndProducerMethodBean.class);

        startContainer(beanClasses, beanXmls);

        IProducedBean producedBean = getInstance(IProducedBean.class, new AnnotationLiteral<QualifierProducerBased>(){});
        Assert.assertNotNull(producedBean);
        Assert.assertEquals("AlternativeOnClassAndProducerMethodBean", producedBean.getProducerType());

        shutDownContainer();
    }


    //X TODO add tests for disposal methods

}
