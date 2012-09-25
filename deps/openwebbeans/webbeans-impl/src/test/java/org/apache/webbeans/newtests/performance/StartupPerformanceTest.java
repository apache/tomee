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
package org.apache.webbeans.newtests.performance;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.webbeans.newtests.AbstractUnitTest;
import org.apache.webbeans.newtests.contexts.session.common.PersonalDataBean;
import org.apache.webbeans.newtests.injection.circular.beans.CircularApplicationScopedBean;
import org.apache.webbeans.newtests.injection.circular.beans.CircularDependentScopedBean;
import org.apache.webbeans.newtests.managed.instance.beans.InstanceInjectedComponent;
import org.apache.webbeans.newtests.profields.beans.classproducer.MyProductBean;
import org.apache.webbeans.newtests.profields.beans.classproducer.MyProductProducer;
import org.apache.webbeans.newtests.profields.beans.classproducer.ProductInjectedBean;
import org.apache.webbeans.newtests.promethods.beans.PersonProducerBean;
import org.apache.webbeans.newtests.promethods.common.Person;
import org.apache.webbeans.newtests.specalization.AdvancedPenProducer;
import org.apache.webbeans.newtests.specalization.DefaultPenProducer;
import org.apache.webbeans.newtests.specalization.Pen;
import org.apache.webbeans.newtests.specalization.PremiumPenProducer;
import org.apache.webbeans.test.component.CheckWithCheckPayment;
import org.apache.webbeans.test.component.CheckWithMoneyPayment;
import org.apache.webbeans.test.component.IPayment;
import org.apache.webbeans.test.component.PaymentProcessorComponent;
import org.junit.Test;

/**
 * Small unit test to help testing the startup performance.
 * We just stuff lots of classes into it and check how it performs.
 */
public class StartupPerformanceTest extends AbstractUnitTest
{
    @Test
    public void testPerformance()
    {
        Collection<String> beanXmls = new ArrayList<String>();

        Collection<Class<?>> beanClasses = new ArrayList<Class<?>>();
        beanClasses.add(PaymentProcessorComponent.class);
        beanClasses.add(InstanceInjectedComponent.class);
        beanClasses.add(CheckWithCheckPayment.class);
        beanClasses.add(CheckWithMoneyPayment.class);
        beanClasses.add(IPayment.class);
        beanClasses.add(ProductInjectedBean.class);
        beanClasses.add(MyProductProducer.class);
        beanClasses.add(MyProductBean.class);
        beanClasses.add(Person.class);
        beanClasses.add(PersonProducerBean.class);
        beanClasses.add(Pen.class);
        beanClasses.add(DefaultPenProducer.class);
        beanClasses.add(AdvancedPenProducer.class);
        beanClasses.add(PremiumPenProducer.class);
        beanClasses.add(PersonalDataBean.class);
        beanClasses.add(CircularDependentScopedBean.class);
        beanClasses.add(CircularApplicationScopedBean.class);

        startContainer(beanClasses, beanXmls);

        // do nothing ...

        shutDownContainer();
    }
}
