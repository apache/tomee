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
package org.apache.webbeans.test.unittests.typedliteral;

import java.lang.annotation.Annotation;
import java.util.List;

import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.util.AnnotationLiteral;
import javax.enterprise.util.TypeLiteral;

import junit.framework.Assert;

import org.apache.webbeans.component.AbstractOwbBean;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.context.ContextFactory;
import org.apache.webbeans.test.TestContext;
import org.apache.webbeans.test.component.ITypeLiteralComponent;
import org.apache.webbeans.test.component.InjectedTypeLiteralComponent;
import org.apache.webbeans.test.component.TypeLiteralComponent;
import org.junit.Before;
import org.junit.Test;

public class TypedLiteralComponentTest extends TestContext
{
    public TypedLiteralComponentTest()
    {
        super(TypedLiteralComponentTest.class.getSimpleName());
    }

    @Before
    public void init()
    {
        super.init();
    }

    @Test
    public void testTypedComponent() throws Throwable
    {
        clear();

        defineManagedBean(TypeLiteralComponent.class);
        defineManagedBean(InjectedTypeLiteralComponent.class);
        List<AbstractOwbBean<?>> comps = getComponents();

        ContextFactory contextFactory = WebBeansContext.getInstance().getContextFactory();
        contextFactory.initRequestContext(null);

        Assert.assertEquals(2, comps.size());

        TypeLiteralComponent userComponent = (TypeLiteralComponent) getManager().getInstance(comps.get(0));
        InjectedTypeLiteralComponent tc = (InjectedTypeLiteralComponent) getManager().getInstance(comps.get(1));

        Assert.assertNotNull(tc.getComponent());
        Assert.assertNotNull(userComponent);

        Assert.assertTrue(tc.getComponent() instanceof TypeLiteralComponent);

        contextFactory.destroyRequestContext(null);
    }

    @Test
    public void testTypedLiteralComponent() throws Throwable
    {
        clear();

        defineManagedBean(TypeLiteralComponent.class);
        List<AbstractOwbBean<?>> comps = getComponents();

        ContextFactory contextFactory = WebBeansContext.getInstance().getContextFactory();
        contextFactory.initRequestContext(null);

        Assert.assertEquals(1, comps.size());

        TypeLiteral<ITypeLiteralComponent<List<String>>> tl = new TypeLiteral<ITypeLiteralComponent<List<String>>>()
        {
        };

        Annotation[] anns = new Annotation[1];
        anns[0] = new AnnotationLiteral<Default>()
        {

        };

        Bean<?> s = WebBeansContext.getInstance().getBeanManagerImpl().getBeans(tl.getType(), anns).iterator().next();
        Assert.assertNotNull(s);

        contextFactory.destroyRequestContext(null);
    }

}
