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

import java.util.ArrayList;
import java.util.Collection;

import org.apache.webbeans.newtests.AbstractUnitTest;
import org.apache.webbeans.newtests.injection.serialization.beans.NonSerializableDependentBean;
import org.apache.webbeans.newtests.injection.serialization.beans.ProducerWithNonSerializableParameterBean;
import org.apache.webbeans.newtests.injection.serialization.beans.SerializableBean;
import org.junit.Assert;
import org.junit.Test;

/**
 * <p>
 * Test non serializable parameters in producer methods and &#064;Inject methods.
 * </p>
 */
public class NonSerializableParametersTest extends AbstractUnitTest
{
    /**
     * This tests if the container correctly allows non serializable parameters
     * for producer methods according to CDI-1.1 spec 6.6.4.
     */
    @Test
    public void testInvalidNonSerializableDependentInjection()
    {
        Collection<String> beanXmls = new ArrayList<String>();
        Collection<Class<?>> beanClasses = new ArrayList<Class<?>>();
        beanClasses.add(ProducerWithNonSerializableParameterBean.class);
        beanClasses.add(SerializableBean.class);
        beanClasses.add(NonSerializableDependentBean.class);

        try
        {
            startContainer(beanClasses, beanXmls);
            SerializableBean sb = getInstance(SerializableBean.class);
            Assert.assertNotNull(sb);
        }
        finally {
            shutDownContainer();
        }
    }
}
