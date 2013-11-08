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
package org.apache.openjpa.jdbc.kernel;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;

import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.util.UserException;

/**
 * {@link SavepointManager} implementation that uses JDBC 3 savepoints
 * to store state. This plugin has the side effect of triggering
 * a flush on the {@link Broker}.
 * 
 * <b>Note that this plugin requires a database and JDBC driver which
 * supports JDBC 3 savepoints</b>
 *
 * @author Steve Kim
 * @since 0.3.4
 */
public class JDBC3SavepointManager
    extends AbstractJDBCSavepointManager {

    private static final Localizer _loc = Localizer.forPackage
        (JDBC3SavepointManager.class);

    protected void rollbackDataStore(ConnectionSavepoint savepoint) {
        try {
            Connection conn = savepoint.getConnection();
            conn.rollback((Savepoint) savepoint.getDataStoreSavepoint());
        } catch (SQLException sqe) {
            throw new UserException(_loc.get("error-rollback",
                savepoint.getName()), sqe);
        }
    }

    protected void setDataStore(ConnectionSavepoint savepoint) {
        try {
            Connection conn = savepoint.getConnection();
            savepoint.setDataStoreSavepoint(conn.setSavepoint
                (savepoint.getName()));
        } catch (SQLException sqe) {
            throw new UserException(_loc.get("error-save",
                savepoint.getName()), sqe);
        }
    }
}
