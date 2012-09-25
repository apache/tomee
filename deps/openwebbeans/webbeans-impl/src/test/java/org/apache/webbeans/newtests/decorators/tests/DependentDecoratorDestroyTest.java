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
package org.apache.webbeans.newtests.decorators.tests;

import java.util.ArrayList;
import java.util.Collection;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.util.AnnotationLiteral;

import junit.framework.Assert;

import org.apache.webbeans.newtests.AbstractUnitTest;
import org.apache.webbeans.newtests.decorators.dependent.DependentDecorator;
import org.apache.webbeans.newtests.decorators.dependent.IDestroy;
import org.apache.webbeans.newtests.decorators.dependent.MyDestory;
import org.apache.webbeans.test.component.event.normal.TransactionalInterceptor;
import org.junit.Test;

public class DependentDecoratorDestroyTest extends AbstractUnitTest
{
    public static final String PACKAGE_NAME = DependentDecoratorDestroyTest.class.getPackage().getName();

    @Test
    @SuppressWarnings("unchecked")    
    public void testDecoratorStack()
    {
        Collection<Class<?>> classes = new ArrayList<Class<?>>();
        classes.add(DependentDecorator.class);
        classes.add(TransactionalInterceptor.class);
        classes.add(MyDestory.class);

        Collection<String> xmls = new ArrayList<String>();
        xmls.add(getXmlPath(PACKAGE_NAME, "DependentDecoratorDestroy"));

        startContainer(classes, xmls);
        
        @SuppressWarnings("serial")
        Bean<IDestroy> bean = (Bean<IDestroy>)getBeanManager().getBeans(IDestroy.class, new AnnotationLiteral<Default>(){}).iterator().next();
        CreationalContext<IDestroy> creationalContext = getBeanManager().createCreationalContext(bean);
        Object instance = getBeanManager().getReference(bean, IDestroy.class, creationalContext);
        IDestroy outputProvider = (IDestroy) instance;
        
        Assert.assertTrue(outputProvider != null);
        outputProvider.destroy();
        Assert.assertTrue(MyDestory.destroyed);
        
        bean.destroy(outputProvider,creationalContext);
        
        Assert.assertTrue(DependentDecorator.dispose);

    }

}
