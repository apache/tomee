package org.apache.webbeans.newtests.portable;

import java.util.ArrayList;
import java.util.Collection;

import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.util.AnnotationLiteral;

import junit.framework.Assert;

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
import org.apache.webbeans.newtests.AbstractUnitTest;
import org.apache.webbeans.newtests.portable.addannotated.extension.AddAdditionalAnnotatedTypeExtension;
import org.apache.webbeans.newtests.portable.events.extensions.AddBeanExtension.MyBean;
import org.junit.Test;

public class BeforeBeanDiscoveryTest extends AbstractUnitTest
{

    @Test
    public void testAddAdditionalAnnotatedType()
    {
        Collection<String> beanXmls = new ArrayList<String>();

        Collection<Class<?>> beanClasses = new ArrayList<Class<?>>();

        addExtension(new AddAdditionalAnnotatedTypeExtension());

        startContainer(beanClasses, beanXmls);

        Bean<?> bean = getBeanManager().getBeans(MyBean.class, new AnnotationLiteral<Default>()
        {
        }).iterator().next();

        // Bean should not be null, as we added it as an additional annotated
        // type during before bean discovery in the extension
        Assert.assertNotNull(bean);

        shutDownContainer();
    }

    @Test
    public void testAddAdditionalAnnotatedTypeWithPresentClass()
    {
        Collection<String> beanXmls = new ArrayList<String>();

        Collection<Class<?>> beanClasses = new ArrayList<Class<?>>();
        beanClasses.add(MyBean.class);

        addExtension(new AddAdditionalAnnotatedTypeExtension());

        startContainer(beanClasses, beanXmls);

        Bean<?> bean = getBeanManager().getBeans(MyBean.class, new AnnotationLiteral<Default>()
        {
        }).iterator().next();

        // Bean should not be null, as we added it as an additional annotated
        // type during before bean discovery in the extension
        Assert.assertNotNull(bean);

        shutDownContainer();
    }
}
