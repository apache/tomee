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
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.sql.Row;
import org.apache.openjpa.jdbc.sql.RowImpl;
import org.apache.openjpa.jdbc.sql.SQLExceptions;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.lib.jdbc.ReportingSQLException;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.util.OptimisticException;

/**
 * Batch prepared statement manager implementation. This prepared statement
 * manager will utilize the JDBC addBatch() and exceuteBatch() to batch the SQL
 * statements together to improve the execution performance.
 * 
 * @author Teresa Kan
 */

public class BatchingPreparedStatementManagerImpl extends
        PreparedStatementManagerImpl {

    private final static Localizer _loc = Localizer
            .forPackage(BatchingPreparedStatementManagerImpl.class);

    private String _batchedSql = null;
    private List<RowImpl> _batchedRows = new ArrayList<RowImpl>();
    private int _batchLimit;
    private boolean _disableBatch = false;

    /**
     * Constructor. Supply connection.
     */
    public BatchingPreparedStatementManagerImpl(JDBCStore store,
        Connection conn, int batchLimit) {
        super(store, conn);
        _batchLimit = batchLimit;
        if (_log.isTraceEnabled())
            _log.trace(_loc.get("batch_limit", String.valueOf(_batchLimit)));
    }

    /**
     * Flush the given row immediately or deferred the flush in batch.
     */
    protected void flushAndUpdate(RowImpl row) throws SQLException {
        if (isBatchDisabled(row)) {
            // if there were some statements batched before, then
            // we need to flush them out first before processing the
            // current non batch process.
            flushBatch();

            super.flushAndUpdate(row);
        } else {
            // process the SQL statement, either execute it immediately or
            // batch it for later execution.
            batchOrExecuteRow(row);
        }
    }

    protected void batchOrExecuteRow(RowImpl row) throws SQLException {
        String sql = row.getSQL(_dict);
        if (_batchedSql == null) {
            // brand new SQL
            _batchedSql = sql;
        } else if (!sql.equals(_batchedSql)) {
            // SQL statements changed.
            switch (_batchedRows.size()) {
            case 0:
                break;
            case 1:
                // single entry in cache, direct SQL execution. 
                try {
                    super.flushAndUpdate((RowImpl) _batchedRows.get(0));
                } finally {
                    _batchedSql = null;
                    _batchedRows.clear();
                }
                break;
            default:
                // flush all entries in cache in batch.
                flushBatch();
            }
            _batchedSql = sql;
        }
        _batchedRows.add(row);
    }

    /*
     * Compute if batching is disabled, based on values of batch limit
     * and database characteristics.
     */
    private boolean isBatchDisabled(RowImpl row) {
        boolean rtnVal = true;
        int limit = getBatchLimit();
        if ((limit < 0 || limit > 1) && !isBatchDisabled()) {
            OpenJPAStateManager sm = row.getPrimaryKey();
            ClassMapping cmd = null;
            if (sm != null)
                cmd = (ClassMapping) sm.getMetaData();
            Column[] autoAssign = null;
            if (row.getAction() == Row.ACTION_INSERT)
                autoAssign = row.getTable().getAutoAssignedColumns();
            // validate batch capability
            rtnVal = _dict
                .validateBatchProcess(row, autoAssign, sm, cmd);
            setBatchDisabled(rtnVal);
        }
        return rtnVal;
    }
    
    /**
     * flush all cached up statements to be executed as a single or batched
     * prepared statements.
     */
    protected void flushBatch() throws SQLException {
        List<RowImpl> batchedRows = getBatchedRows();
        String batchedSql = getBatchedSql();
        if (batchedRows == null)
            return;

        int batchSize = batchedRows.size();
        if (batchedSql != null &&  batchSize > 0) {
            PreparedStatement ps = null;
            try {
                RowImpl onerow = null;
                ps = prepareStatement(batchedSql);
                if (batchSize == 1) {
                    // execute a single row.
                    onerow = batchedRows.get(0);
                    flushSingleRow(onerow, ps);
                } else {
                    // cache has more than one rows, execute as batch.
                    int count = 0;
                    int batchedRowsBaseIndex = 0;
                    Iterator<RowImpl> itr = batchedRows.iterator();
                    while (itr.hasNext()) {
                        onerow = itr.next();
                        if (_batchLimit == 1) {
                            flushSingleRow(onerow, ps);
                        } else {
                            if (count < _batchLimit || _batchLimit == -1) {
                                if (ps != null)
                                    onerow.flush(ps, _dict, _store);
                                addBatch(ps, onerow, count);
                                count++;
                            } else {
                                // reach the batchLimit, execute the batch
                                int[] rtn = executeBatch(ps);
                                checkUpdateCount(rtn, batchedRowsBaseIndex, ps);

                                batchedRowsBaseIndex += _batchLimit;

                                if (ps != null)
                                    onerow.flush(ps, _dict, _store);
                                addBatch(ps, onerow, count);
                                // reset the count to 1 for new batch
                                count = 1;
                            }
                        }
                    }
                    // end of the loop, execute the batch
                    int[] rtn = executeBatch(ps);
                    checkUpdateCount(rtn, batchedRowsBaseIndex, ps);
                }
            } catch (SQLException se) {
                //If we look at PreparedStatementManagerImpl.flushAndUpdate (which is the 'non-batch' code path
                //similar to this path, or I should say, the path which is taken instead of this path when
                //we aren't using batching), we see that the catch block doesn't do a 'se.getNextException'.
                //When we do a 'getNextException', the 'next exception' doesn't contain the same message as se.
                //That is, 'next exception' contains a subset msg which is contained in se.
                SQLException sqex = se.getNextException();
                if (sqex == null){
                    sqex = se;
                }
                
                if (se instanceof ReportingSQLException){
                  int index = ((ReportingSQLException) se).getIndexOfFirstFailedObject();

                  //if we have only batched one statement, the index should be 0.  As can be seen above,
                  //if 'batchSize == 1' a different path is taken (the 'single row' path), and if that row
                  //fails, we know that the index is 0 since there is only one row.
                  if (batchSize == 1){
                      index = 0;
                  }
                  
                  //index should not be less than 0 in this path, but if for some reason it is, lets
                  //resort to the 'old way' and simply pass the 'ps' as the failed object.
                  if (index < 0){ 
                      throw SQLExceptions.getStore(se, ps, _dict);
                  }
                  else{
                      if(_batchedRows.size() == 0) { 
                          if(_log.isTraceEnabled()) { 
                              _log.trace("No batched rows found. The failed object may not be reliable"); 
                          }
                          throw SQLExceptions.getStore(se, ps, _dict);
                      }
                      throw SQLExceptions.getStore(se, (_batchedRows.get(index)).getFailedObject(), _dict);
                  }                    
                }
                else{
                	//per comments above, use 'sqex' rather than 'se'. 
                    throw SQLExceptions.getStore(sqex, ps, _dict);
                }
            } finally {
                _batchedSql = null;
                batchedRows.clear();
                if (ps != null) {
                    ps.clearParameters();
                    try {
                        ps.close();
                    } catch (SQLException sqex) {
                        throw SQLExceptions.getStore(sqex, ps, _dict);
                    }
                }
            }
        }
    }

    /*
     * Execute an update of a single row.
     */
    private void flushSingleRow(RowImpl row, PreparedStatement ps)
        throws SQLException {
        if (ps != null)
            row.flush(ps, _dict, _store);
        int count = executeUpdate(ps, row.getSQL(_dict), row);
        if (count != 1) {
            logSQLWarnings(ps);
            Object failed = row.getFailedObject();
            if (failed != null)
                _exceptions.add(new OptimisticException(failed));
            else if (row.getAction() == Row.ACTION_INSERT)
                throw new SQLException(_loc.get("update-failed-no-failed-obj",
                    String.valueOf(count), row.getSQL(_dict)).getMessage());
        }
    }

    /*
     * Process executeBatch function array of return counts.
     */
    private void checkUpdateCount(int[] count, int batchedRowsBaseIndex,
        PreparedStatement ps)
        throws SQLException {
        // value in int[] count  returned from executeBatch: 
        //               Update          Delete        Insert
        // ===============================================================
        //               OK / Error      OK / Error    OK / Error
        // DB2LUW         1 / 0           1 / 0         1 / SQLException
        // DB2/ZOS        1 / 0           1 / 0        -2 / SQLException
        // Oracle        -2 / -2         -2 / -2       -2 / SQLException
        int cnt = 0;
        int updateSuccessCnt = _dict.getBatchUpdateCount(ps);
        Object failed = null;
        List<RowImpl> batchedRows = getBatchedRows();
        for (int i = 0; i < count.length; i++) {
            cnt = count[i];
            RowImpl row = (RowImpl) batchedRows.get(batchedRowsBaseIndex + i);
            failed = row.getFailedObject();
            switch (cnt) {
            case Statement.EXECUTE_FAILED: // -3
                if (failed != null || row.getAction() == Row.ACTION_UPDATE)
                    _exceptions.add(new OptimisticException(failed));
                else if (row.getAction() == Row.ACTION_INSERT)
                    throw new SQLException(_loc.get(
                        "update-failed-no-failed-obj",
                        String.valueOf(count[i]), 
                        row.getSQL(_dict)).getMessage());
                break;
            case Statement.SUCCESS_NO_INFO: // -2
                if (_dict.reportsSuccessNoInfoOnBatchUpdates &&
                    updateSuccessCnt != count.length) {
                    // Oracle batching specifics:
                    // treat update/delete of SUCCESS_NO_INFO as failed case
                    // because:
                    // 1. transaction should be rolled back.
                    // 2. if DataCache is enabled, objects in
                    //    cache should be removed.
                    if (failed != null)
                        _exceptions.add(new OptimisticException(failed));
                    else if (row.getAction() == Row.ACTION_INSERT)
                        throw new SQLException(_loc.get(
                            "update-failed-no-failed-obj",
                            String.valueOf(count[i]), 
                            row.getSQL(_dict)).getMessage());
                }
                if (_log.isTraceEnabled())
                    _log.trace(_loc.get("batch_update_info",
                        String.valueOf(cnt), 
                        row.getSQL(_dict)).getMessage());
                break;
            case 0: // no row is inserted, treats it as failed
                // case
                logSQLWarnings(ps);
                if (failed != null)
                    _exceptions.add(new OptimisticException(failed));
                else if (row.getAction() == Row.ACTION_INSERT)
                    throw new SQLException(_loc.get(
                        "update-failed-no-failed-obj",
                        String.valueOf(count[i]), 
                        row.getSQL(_dict)).getMessage());
            }
        }
    }

    public boolean isBatchDisabled() {
        return _disableBatch;
    }

    public void setBatchDisabled(boolean disableBatch) {
        _disableBatch = disableBatch;
    }

    public int getBatchLimit() {
        return _batchLimit;
    }

    public void setBatchLimit(int batchLimit) {
        _batchLimit = batchLimit;
    }

    public List<RowImpl> getBatchedRows() {
        return _batchedRows;
    }

    public String getBatchedSql() {
        return _batchedSql;
    }

    protected void addBatch(PreparedStatement ps, RowImpl row, 
            int count) throws SQLException {
        ps.addBatch();
    }

    protected int[] executeBatch(PreparedStatement ps) 
    throws SQLException {
        return ps.executeBatch();
    }
}
