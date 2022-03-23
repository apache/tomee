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

package org.apache.openejb.resource.jdbc.managed.xa;

import org.apache.openejb.resource.jdbc.managed.local.ManagedConnection;

import java.sql.SQLException;
import javax.sql.CommonDataSource;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.TransactionSynchronizationRegistry;
import javax.transaction.xa.XAResource;

public class ManagedXAConnection extends ManagedConnection {
    public ManagedXAConnection(final CommonDataSource ds, final TransactionManager txMgr,
                               final TransactionSynchronizationRegistry txRegistry,
                               final String user, final String password) throws SQLException {
        super(ds, txMgr, txRegistry, user, password);
    }

    @Override
    public XAResource getXAResource() throws SQLException {
        if (xaResource == null) {
            newConnection();
        }
        return xaResource;
    }

    @Override
    protected void setAutoCommit(final boolean value) throws SQLException {
        // no-op
    }
}
