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
package org.apache.openjpa.lib.jdbc;

/**
 * A listener for all {@link JDBCEvent}s that occur.
 *
 * @author Marc Prud'hommeaux
 * @author Abe White
 * @see AbstractJDBCListener
 */
public interface JDBCListener {

    /**
     * @see JDBCEvent#BEFORE_PREPARE_STATEMENT
     */
    public void beforePrepareStatement(JDBCEvent event);

    /**
     * @see JDBCEvent#AFTER_PREPARE_STATEMENT
     */
    public void afterPrepareStatement(JDBCEvent event);

    /**
     * @see JDBCEvent#BEFORE_CREATE_STATEMENT
     */
    public void beforeCreateStatement(JDBCEvent event);

    /**
     * @see JDBCEvent#AFTER_CREATE_STATEMENT
     */
    public void afterCreateStatement(JDBCEvent event);

    /**
     * @see JDBCEvent#BEFORE_EXECUTE_STATEMENT
     */
    public void beforeExecuteStatement(JDBCEvent event);

    /**
     * @see JDBCEvent#AFTER_EXECUTE_STATEMENT
     */
    public void afterExecuteStatement(JDBCEvent event);

    /**
     * @see JDBCEvent#BEFORE_COMMIT
     */
    public void beforeCommit(JDBCEvent event);

    /**
     * @see JDBCEvent#AFTER_COMMIT
     */
    public void afterCommit(JDBCEvent event);

    /**
     * @see JDBCEvent#BEFORE_ROLLBACK
     */
    public void beforeRollback(JDBCEvent event);

    /**
     * @see JDBCEvent#AFTER_ROLLBACK
     */
    public void afterRollback(JDBCEvent event);

    /**
     * @see JDBCEvent#AFTER_CONNECT
     */
    public void afterConnect(JDBCEvent event);

    /**
     * @see JDBCEvent#BEFORE_CLOSE
     */
    public void beforeClose(JDBCEvent event);
}

