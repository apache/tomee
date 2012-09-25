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
package org.apache.webbeans.test.unittests.disposal;

import java.util.List;

import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.context.ContextFactory;
import org.apache.webbeans.test.TestContext;
import org.apache.webbeans.test.component.disposal.Disposal1;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DisposalTest extends TestContext
{

    public DisposalTest()
    {
        super(DisposalTest.class.getName());
    }

    @Before
    public void init()
    {
        super.init();
    }

    @Test
    public void testDisposal1()
    {
        clear();

        ContextFactory contextFactory = WebBeansContext.getInstance().getContextFactory();
        contextFactory.initRequestContext(null);
        contextFactory.initSessionContext(null);

        defineManagedBean(Disposal1.class);

        @SuppressWarnings("unchecked")
        List<Integer> list = (List<Integer>) getInstanceByName("createBinding1");
        Assert.assertNotNull(list);
        Assert.assertTrue(list.size() == 1);
        contextFactory.destroyRequestContext(null);

        Assert.assertTrue(Disposal1.getDISPOSCALL());

    }
}
