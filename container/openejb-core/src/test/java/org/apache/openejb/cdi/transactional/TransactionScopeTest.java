/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.cdi.transactional;

import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.inject.Inject;
import jakarta.transaction.TransactionScoped;
import jakarta.transaction.Transactional;
import java.io.Serializable;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

@RunWith(ApplicationComposer.class)
public class TransactionScopeTest {
    @Module
    @Classes(cdi = true, innerClassesAsBean = true)
    public WebApp jar() {
        return new WebApp();
    }

    @Inject
    private MyTransactionalBean bean;

    @Test
    public void scope() {
        bean.withTransaction();
        final String firstId1 = bean.id;

        bean.withTransaction();
        final String secondId1 = bean.id;

        assertThat("bean should change between scenarios", firstId1, is(not(secondId1)));
    }

    public static class MyTransactionalBean {
        @Inject
        private MyTransactionScopedBean bean;

        private static String id; // static cause after the tx the bean doesn't exist anymore

        @Transactional
        public void withTransaction() {
            id = bean.getId();
        }
    }

    @TransactionScoped
    public static class MyTransactionScopedBean implements Serializable {
        public String getId() {
            return this + "";
        }
    }
}
