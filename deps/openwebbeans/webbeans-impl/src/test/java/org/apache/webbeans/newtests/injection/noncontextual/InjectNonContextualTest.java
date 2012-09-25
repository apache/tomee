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
package org.apache.webbeans.newtests.injection.noncontextual;

import junit.framework.Assert;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.newtests.AbstractUnitTest;
import org.junit.Test;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionTarget;
import java.util.ArrayList;
import java.util.Collection;

public class InjectNonContextualTest extends AbstractUnitTest
{
    @SuppressWarnings("unchecked")
    public <T> void inject(T instance)
    {
        BeanManager mgr = WebBeansContext.getInstance().getBeanManagerImpl();
        AnnotatedType<T> annotatedType = mgr.createAnnotatedType((Class<T>) instance.getClass());
        InjectionTarget<T> injectionTarget = mgr.createInjectionTarget(annotatedType);
        CreationalContext<T> context = mgr.createCreationalContext(null);
        injectionTarget.inject(instance, context);
    }


    @Test
    public void testInjectingNonContextualBean()
    {
        Collection<String> beanXmls = new ArrayList<String>();

        Collection<Class<?>> beanClasses = new ArrayList<Class<?>>();
        beanClasses.add(ContextualBean.class);
        startContainer(beanClasses, beanXmls);
        try
        {
            final NonContextualBean bean = new NonContextualBean();
            inject(bean);
            Assert.assertNotNull(bean.getContextual());
        }
        finally
        {
            shutDownContainer();
        }
    }

}
