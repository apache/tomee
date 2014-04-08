/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.openejb.arquillian.tests.resenventry;

import java.lang.reflect.Field;

import javax.annotation.ManagedBean;
import javax.annotation.Resource;
import javax.enterprise.inject.spi.BeanManager;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;
import javax.transaction.UserTransaction;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.junit.Assert;

@ManagedBean
public class Purple {

    @Resource
    private Validator validator;

    @Resource
    private ValidatorFactory validatorFactory;

    @Resource
    private TransactionManager transactionManager;

    @Resource
    private TransactionSynchronizationRegistry transactionSynchronizationRegistry;

    @Resource
    private UserTransaction userTransaction;

    @Resource
    private BeanManager beanManager;

    public void test() throws IllegalAccessException {
        final Field[] fields = this.getClass().getDeclaredFields();
		
		for (Field field : fields) {
		    field.setAccessible(true);
		    Assert.assertNotNull(field.getName(), field.get(this));
		}
    }
}