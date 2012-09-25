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
package org.apache.webbeans.newtests.portable;

import junit.framework.Assert;
import org.apache.webbeans.newtests.AbstractUnitTest;
import org.junit.Test;

import javax.enterprise.inject.spi.BeanManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Various tests for the BeanManager
 */
public class BeanManagerTest extends AbstractUnitTest
{
    public BeanManagerTest()
    {
    }

    
    /**
     * This test adds a scope and tests if the lifecycle works
     */
    @Test
    public void testResolve()
    {
        Collection<Class<?>> classes = new ArrayList<Class<?>>();
        startContainer(classes);

        BeanManager bm = getInstance(BeanManager.class);
        Assert.assertNotNull(bm);

        // this must not throw a NPE
        Assert.assertNull(bm.resolve(null));

        Assert.assertNull(bm.resolve(Collections.EMPTY_SET));

        shutDownContainer();
    }
}
