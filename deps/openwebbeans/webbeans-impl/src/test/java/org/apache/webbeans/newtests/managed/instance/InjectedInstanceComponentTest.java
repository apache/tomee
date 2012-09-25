/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.webbeans.newtests.managed.instance;

import junit.framework.Assert;
import org.apache.webbeans.newtests.AbstractUnitTest;
import org.apache.webbeans.newtests.managed.instance.beans.DependentBean;
import org.apache.webbeans.newtests.managed.instance.beans.InstanceForDependentBean;
import org.apache.webbeans.newtests.managed.instance.beans.InstanceInjectedComponent;
import org.apache.webbeans.test.component.CheckWithCheckPayment;
import org.apache.webbeans.test.component.CheckWithMoneyPayment;
import org.apache.webbeans.test.component.IPayment;
import org.apache.webbeans.test.component.PaymentProcessorComponent;
import org.junit.Test;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.util.TypeLiteral;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;


public class InjectedInstanceComponentTest extends AbstractUnitTest
{

        @Test
    public void testInstanceInjectedComponent()
    {
        Collection<Class<?>> beanClasses = new ArrayList<Class<?>>();
        beanClasses.add(PaymentProcessorComponent.class);
        beanClasses.add(InstanceInjectedComponent.class);
        beanClasses.add(CheckWithCheckPayment.class);
        beanClasses.add(CheckWithMoneyPayment.class);
        beanClasses.add(IPayment.class);

        startContainer(beanClasses, null);

        InstanceInjectedComponent instance = getInstance(InstanceInjectedComponent.class);

        org.junit.Assert.assertNotNull(instance);
        org.junit.Assert.assertNotNull(instance.getInstance());
        org.junit.Assert.assertNotNull(instance.getPaymentComponent());

        Instance<PaymentProcessorComponent> ins = instance.getInstance();

        boolean ambigious = ins.isAmbiguous();

        Assert.assertFalse(ambigious);

        boolean unsatisfied = ins.isUnsatisfied();

        Assert.assertFalse(unsatisfied);

        shutDownContainer();
    }

    @Test
    public void testInstanceDestroyal()
    {
        Collection<Class<?>> beanClasses = new ArrayList<Class<?>>();

        beanClasses.add(InstanceForDependentBean.class);
        beanClasses.add(DependentBean.class);
        startContainer(beanClasses, null);

        InstanceForDependentBean holder = getInstance(InstanceForDependentBean.class);
        Assert.assertNotNull(holder);

        Assert.assertEquals(42, holder.getMeaningOfLife());

        DependentBean.properlyDestroyed = false;

        shutDownContainer();

        Assert.assertTrue(DependentBean.properlyDestroyed);
    }

    @Test
    public void testManualInstanceResolving()
    {
        Collection<Class<?>> beanClasses = new ArrayList<Class<?>>();

        beanClasses.add(PaymentProcessorComponent.class);
        beanClasses.add(CheckWithCheckPayment.class);
        beanClasses.add(CheckWithMoneyPayment.class);
        beanClasses.add(IPayment.class);

        startContainer(beanClasses, null);

        Set<Bean<?>> beans =
                getBeanManager().getBeans(new TypeLiteral<Instance<PaymentProcessorComponent>>() {}.getType());
        Bean<Instance<PaymentProcessorComponent>> bean =
                (Bean<Instance<PaymentProcessorComponent>>) getBeanManager().resolve(beans);
        CreationalContext<Instance<PaymentProcessorComponent>> creationalContext =
                getBeanManager().createCreationalContext(bean);
        Instance<PaymentProcessorComponent> provider = bean.create(creationalContext);
        Assert.assertNotNull(provider);

        // please note that the provider will NOT create a PaymentProcessorComponent
        // because the Bean doesn't know this information! This is compatible with Weld
        // which also doesn't handle this case. This is due to the fact that the spec
        // defines that there is only one InstanceBean.

        shutDownContainer();
    }

}
