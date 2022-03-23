/**
 *
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
package org.apache.openejb.core.stateful;

import org.apache.openejb.OpenEJB;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.StatefulBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.AppResource;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.ejb.Remove;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import javax.naming.Context;
import javax.naming.NamingException;
import jakarta.transaction.SystemException;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(ApplicationComposer.class)
public class StatefulTransactionInCallbacksTest {
    @Module
    public EnterpriseBean bean() {
        return new StatefulBean(TransactionBean.class).localBean();
    }

    @AppResource
    private Context ctx;

    @Test
    public void create() throws NamingException {
        final TransactionBean bean = (TransactionBean) ctx.lookup("java:global/StatefulTransactionInCallbacksTest/bean/TransactionBean");
        // contruct was ok
        bean.remove();
        // destroy was ok
    }

    public static class TransactionBean {
        @PostConstruct
        @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
        public void hasTxConstruct() {
            try {
                assertTrue(OpenEJB.getTransactionManager().getTransaction() != null);
            } catch (final SystemException e) {
                fail(e.getMessage());
            }
        }

        @PreDestroy
        @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
        public void hasTxDestroy() {
            try {
                assertTrue(OpenEJB.getTransactionManager().getTransaction() != null);
            } catch (final SystemException e) {
                fail(e.getMessage());
            }
        }

        @Remove
        public void remove() {
            // no-op
        }
    }
}
