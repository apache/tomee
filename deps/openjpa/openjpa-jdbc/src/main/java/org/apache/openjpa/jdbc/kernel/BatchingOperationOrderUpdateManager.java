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
import java.util.Collection;

import org.apache.openjpa.jdbc.sql.RowManager;
import org.apache.openjpa.jdbc.sql.SQLExceptions;
import org.apache.openjpa.util.OpenJPAException;

/**
 * <P>Batch update manager that writes the SQL in object-level operation order. 
 * This update manager initiates a BatchPreparedStatementManagerImpl which 
 * will utilize the JDBC addBatch() and executeBatch() APIs to batch the 
 * statements for performance improvement.</P>
 * <P>This is the plug-in class for UpdateManager to support statement 
 * batching for ordering. You can plug-in this statement batch implementation 
 * through the following property: 
 * <PRE>
 * < property name="openjpa.jdbc.UpdateManager" 
 *   value="org.apache.openjpa.jdbc.kernel.BatchingOperationOrderUpdateManager"
 *    />   
 * </PRE></P>
 * @author Teresa Kan
 */

public class BatchingOperationOrderUpdateManager extends
    OperationOrderUpdateManager {

    protected PreparedStatementManager newPreparedStatementManager(
        JDBCStore store, Connection conn) {
        int batchLimit = dict.getBatchLimit();
        return new BatchingPreparedStatementManagerImpl(store, conn,
            batchLimit);
    }
    
    /*
     * Override this method to flush any remaining batched row in the
     * PreparedStatementManager.
     */
    protected Collection flush(RowManager rowMgr,
        PreparedStatementManager psMgr, Collection exceps) {
        exceps = super.flush(rowMgr, psMgr, exceps);
        BatchingPreparedStatementManagerImpl bPsMgr = 
            (BatchingPreparedStatementManagerImpl) psMgr;
        try {
            bPsMgr.flushBatch();
        } catch (SQLException se) {
            exceps = addException(exceps, SQLExceptions.getStore(se, dict));
        } catch (OpenJPAException ke) {
            exceps = addException(exceps, ke);
        }

        return exceps;
    }
}
