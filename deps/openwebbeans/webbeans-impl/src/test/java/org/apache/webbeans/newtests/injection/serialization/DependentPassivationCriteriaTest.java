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
package org.apache.webbeans.newtests.injection.serialization;

import org.apache.webbeans.exception.WebBeansConfigurationException;
import org.apache.webbeans.newtests.AbstractUnitTest;
import org.apache.webbeans.newtests.injection.serialization.beans.NonSerializableDependentBean;
import org.apache.webbeans.newtests.injection.serialization.beans.SerializableInjectionTargetFailA;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;

/**
 * <p>This test performs a few tests to ensure correct handling of injecting
 * &#064;Dependent scoped beans into beans of passivating scopes with
 * regard of Serialization handling.</p>
 * <p>In general a non-Serializable &#064;Dependent scoped bean must not get
 * injected into a passivating scoped bean. This is true for plain beans,
 * producer fields and also for producer methods</p>
 * <p>Injection <b>is</b> allowed if the injection point is transient or if
 * the injection target bean either provides a writeObject(ObjectOutputStream)
 * or a Externalizable#writeExternal(ObjectOutput) method.</p>
 */
public class DependentPassivationCriteriaTest extends AbstractUnitTest
{
    /**
     * This tests if the container correctly detects a deployment exception
     * according to spec 6.6.4:
     * "
     */
    @Test(expected = WebBeansConfigurationException.class)
    public void testInvalidNonSerializableDependentInjection()
    {
        Collection<String> beanXmls = new ArrayList<String>();
        Collection<Class<?>> beanClasses = new ArrayList<Class<?>>();
        beanClasses.add(NonSerializableDependentBean.class);
        beanClasses.add(SerializableInjectionTargetFailA.class);

        try
        {
            startContainer(beanClasses, beanXmls);
        }
        finally {
            shutDownContainer();
        }
    }
}
