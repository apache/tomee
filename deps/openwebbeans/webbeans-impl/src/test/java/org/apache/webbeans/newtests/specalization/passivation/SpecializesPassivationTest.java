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
package org.apache.webbeans.newtests.specalization.passivation;

import org.apache.webbeans.newtests.AbstractUnitTest;
import org.apache.webbeans.newtests.specalization.Pen;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;

public class SpecializesPassivationTest extends AbstractUnitTest
{

    private static final String PACKAGE_NAME = SpecializesPassivationTest.class.getPackage().getName();

    @Test
    @SuppressWarnings("unchecked")
    public void testSpecializePassivatingBean()
    {
        Collection<Class<?>> beanClasses = new ArrayList<Class<?>>();
        beanClasses.add(Pen.class);
        beanClasses.add(DefaultXyBean.class);
        beanClasses.add(SpecializedXyBean.class);

        startContainer(beanClasses, null);
        shutDownContainer();
    }

}
