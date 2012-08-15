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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.arquillian.transaction;

import org.apache.openejb.OpenEJB;
import org.apache.openejb.OpenEJBRuntimeException;
import org.jboss.arquillian.transaction.spi.provider.TransactionProvider;
import org.jboss.arquillian.transaction.spi.test.TransactionalTest;

public class OpenEJBTransactionProvider implements TransactionProvider {
    public static final String CONFIG_PATH = "arquillian-transaction-configuration.properties";

    @Override
    public void beginTransaction(final TransactionalTest test) {
        if (accept(test.getManager())) {
            try {
                OpenEJB.getTransactionManager().begin();
            } catch (Exception e) {
                throw new OpenEJBRuntimeException(e);
            }
        }
    }

    @Override
    public void commitTransaction(final TransactionalTest test) {
        if (accept(test.getManager())) {
            try {
                OpenEJB.getTransactionManager().commit();
            } catch (Exception e) {
                throw new OpenEJBRuntimeException(e);
            }
        }
    }

    @Override
    public void rollbackTransaction(final TransactionalTest test) {
        if (accept(test.getManager())) {
            try {
                OpenEJB.getTransactionManager().rollback();
            } catch (Exception e) {
                throw new OpenEJBRuntimeException(e);
            }
        }
    }

    private boolean accept(final String manager) {
        return manager == null || manager.isEmpty()
                    || "openejb".equalsIgnoreCase(manager)
                    || "tomee".equalsIgnoreCase(manager);
    }
}
