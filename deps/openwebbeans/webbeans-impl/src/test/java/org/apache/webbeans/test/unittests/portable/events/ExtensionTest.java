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
package org.apache.webbeans.test.unittests.portable.events;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;

import javax.enterprise.inject.spi.Bean;

import junit.framework.Assert;

import org.apache.webbeans.annotation.DefaultLiteral;
import org.apache.webbeans.newtests.AbstractUnitTest;
import org.apache.webbeans.portable.events.discovery.BeforeShutdownImpl;
import org.apache.webbeans.test.component.library.BookShop;
import org.apache.webbeans.test.component.portable.events.MyExtension;
import org.apache.webbeans.test.component.producer.primitive.PrimitiveProducer;
import org.junit.Test;

/**
 * This test checks if an extension gets loaded correctly and
 * if all specified events get fired.
 */
public class ExtensionTest extends AbstractUnitTest
{
    public ExtensionTest()
    {
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testExtensionServices()
    {
        MyExtension.reset();

        Collection<Class<?>> classes = new ArrayList<Class<?>>();
        classes.add(BookShop.class);
        classes.add(PrimitiveProducer.class);
        
        addExtension(new MyExtension());
        
        startContainer(classes);

        getWebBeansContext().getContextFactory().initApplicationContext(null);

        Bean<MyExtension> extension = (Bean<MyExtension>) getBeanManager().getBeans(MyExtension.class, new DefaultLiteral()).iterator().next();
        
        MyExtension ext = (MyExtension) getBeanManager().getReference(extension, MyExtension.class, getBeanManager().createCreationalContext(extension));
        
        System.out.println(ext.toString());
        
        Assert.assertNotNull(MyExtension.processAnnotatedTypeEvent);
        Assert.assertNotNull(MyExtension.processBean);
        Assert.assertNotNull(MyExtension.processObserverMethod);
        Assert.assertNotNull(MyExtension.lastAfterBeanDiscovery);
        Assert.assertNotNull(MyExtension.lastBeforeBeanDiscovery);
        Assert.assertNotNull(MyExtension.afterDeploymentValidation);
        Assert.assertNotNull(MyExtension.processInjectionTarget);
        Assert.assertNotNull(MyExtension.processProducer);
        
        //Fire shut down
        getBeanManager().fireEvent(new BeforeShutdownImpl(), new Annotation[0]);

        getWebBeansContext().getContextFactory().destroyApplicationContext(null);

        Assert.assertNotNull(MyExtension.beforeShutdownEvent);
    }
}
