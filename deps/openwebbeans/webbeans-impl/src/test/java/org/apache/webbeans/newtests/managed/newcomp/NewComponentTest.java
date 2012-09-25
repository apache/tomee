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
package org.apache.webbeans.newtests.managed.newcomp;

import java.util.ArrayList;
import java.util.Collection;


import org.apache.webbeans.newtests.AbstractUnitTest;
import org.apache.webbeans.test.component.CheckWithCheckPayment;
import org.apache.webbeans.test.component.IPayment;
import org.apache.webbeans.test.component.dependent.DependentComponent;
import org.apache.webbeans.test.component.dependent.DependentOwnerComponent;
import org.junit.Test;
import org.junit.Assert;


public class NewComponentTest extends AbstractUnitTest
{
    @Test
    public void testDependent()
    {
        Collection<Class<?>> beanClasses = new ArrayList<Class<?>>();
        beanClasses.add(DependentComponent.class);
        beanClasses.add(DependentOwnerComponent.class);
        beanClasses.add(NewComponent.class);

        startContainer(beanClasses, null);

        NewComponent newComponent = getInstance(NewComponent.class);
        Assert.assertNotNull(newComponent);

        Assert.assertNotNull(newComponent.owner());
        Assert.assertNotNull(newComponent.getGrandParent());
        Assert.assertNotSame(newComponent.owner(), newComponent.getGrandParent());


        shutDownContainer();
    }

    @Test
    public void testDepedent2()
    {
        Collection<Class<?>> beanClasses = new ArrayList<Class<?>>();
        beanClasses.add(CheckWithCheckPayment.class);
        beanClasses.add(IPayment.class);
        beanClasses.add(ProducerNewComponent.class);
        beanClasses.add(NewComponent.class);

        startContainer(beanClasses, null);

        IPayment payment = getInstance(IPayment.class);
        Assert.assertNotNull(payment);
        Assert.assertTrue(payment instanceof CheckWithCheckPayment);

        shutDownContainer();
    }
}
