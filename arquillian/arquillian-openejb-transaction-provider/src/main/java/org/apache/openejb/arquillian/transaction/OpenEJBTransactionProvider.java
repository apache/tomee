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
package org.apache.openejb.arquillian.transaction;

import org.apache.openejb.OpenEJB;
import org.apache.openejb.OpenEJBRuntimeException;
import org.jboss.arquillian.transaction.spi.provider.TransactionProvider;
import org.jboss.arquillian.transaction.spi.test.TransactionalTest;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.transaction.TransactionManager;

public class OpenEJBTransactionProvider implements TransactionProvider {
    @Override
    public void beginTransaction(final TransactionalTest test) {
        try {
            lookup(test.getManager()).begin();
        } catch (final Exception e) {
            throw new OpenEJBRuntimeException(e);
        }
    }

    @Override
    public void commitTransaction(final TransactionalTest test) {
        try {
            lookup(test.getManager()).commit();
        } catch (final Exception e) {
            throw new OpenEJBRuntimeException(e);
        }
    }

    @Override
    public void rollbackTransaction(final TransactionalTest test) {
        try {
            lookup(test.getManager()).rollback();
        } catch (final Exception e) {
            throw new OpenEJBRuntimeException(e);
        }
    }

    private static TransactionManager lookup(final String manager) throws NamingException {
        if (builtIn(manager)) {
            return OpenEJB.getTransactionManager();
        }
        return (TransactionManager) new InitialContext().lookup(manager);
    }

    private static boolean builtIn(final String manager) {
        return manager == null || manager.isEmpty()
                    || "openejb".equalsIgnoreCase(manager)
                    || "tomee".equalsIgnoreCase(manager);
    }
}
