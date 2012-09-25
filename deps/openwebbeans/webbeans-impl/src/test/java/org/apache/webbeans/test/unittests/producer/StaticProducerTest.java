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
package org.apache.webbeans.test.unittests.producer;

import java.lang.annotation.Annotation;

import org.apache.webbeans.component.ProducerMethodBean;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.test.TestContext;
import org.apache.webbeans.test.component.producer.StaticProducer1;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class StaticProducerTest extends TestContext
{

    public StaticProducerTest()
    {
        super(StaticProducerTest.class.getName());
    }

    @Before
    public void init()
    {
        super.init();
    }

    @Test
    public void testStaticProducer1()
    {
        clear();

        WebBeansContext.getInstance().getContextFactory().initRequestContext(null);

        defineManagedBean(StaticProducer1.class);

        ProducerMethodBean<?> pc = (ProducerMethodBean<?>) getManager().resolveByName("weight").iterator().next();

        Object obj = getManager().getInstance(pc);

        Assert.assertTrue(obj instanceof Integer);
        Assert.assertEquals(79, obj);

        pc = (ProducerMethodBean<?>) getManager().resolveByType(int.class, new Annotation[] {}).iterator().next();

        obj = getManager().getInstance(pc);

        Assert.assertTrue(obj instanceof Integer);
        Assert.assertEquals(79, obj);

    }
}
