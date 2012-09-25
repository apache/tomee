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
package org.apache.webbeans.newtests.interceptors.business.tests;

import junit.framework.Assert;
import org.apache.webbeans.newtests.AbstractUnitTest;
import org.apache.webbeans.newtests.interceptors.business.common.TransactionalBaseBean;
import org.apache.webbeans.newtests.interceptors.business.common.TransactionalChildBean;
import org.apache.webbeans.newtests.interceptors.common.TransactionInterceptor;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;

@SuppressWarnings("unchecked")
public class InheritedBeanInterceptorTest extends AbstractUnitTest
{
    private static final String PACKAGE_NAME = InheritedBeanInterceptorTest.class.getPackage().getName();
    
    @Test
    public void testStereoTypeBasedInterceptor()
    {
        Collection<String> beanXmls = new ArrayList<String>();
        beanXmls.add(getXmlPath(PACKAGE_NAME, "InheritedBeanInterceptorTest"));
        
        Collection<Class<?>> beanClasses = new ArrayList<Class<?>>();
        beanClasses.add(TransactionInterceptor.class);
        beanClasses.add(TransactionalBaseBean.class);
        beanClasses.add(TransactionalChildBean.class);

        startContainer(beanClasses, beanXmls);

        TransactionInterceptor.ECHO = false;
        TransactionInterceptor.count = 0;

        TransactionalChildBean child = getInstance(TransactionalChildBean.class);
        Assert.assertNotNull(child);
        Assert.assertEquals(0, TransactionInterceptor.count);

        Assert.assertEquals("21", child.doHalf());
        Assert.assertTrue(TransactionInterceptor.ECHO);
        Assert.assertEquals(1, TransactionInterceptor.count);

        TransactionInterceptor.ECHO = false;
        TransactionInterceptor.count = 0;

        Assert.assertEquals("42", child.doBase());
        Assert.assertTrue(TransactionInterceptor.ECHO);
        Assert.assertEquals(1, TransactionInterceptor.count);

        shutDownContainer();
        
    }
}
