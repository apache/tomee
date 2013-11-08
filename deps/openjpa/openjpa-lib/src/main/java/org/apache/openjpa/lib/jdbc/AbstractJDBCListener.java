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
 * An abstract implementation of the {@link JDBCListener}
 * listener. It allows simple implementation of fine-grained event handling.
 *
 * @author Marc Prud'hommeaux
 */
public class AbstractJDBCListener implements JDBCListener {

    /**
     * Catch-all for unhandled events. This method is called by all other
     * event methods if you do not override them. Does nothing by default.
     */
    protected void eventOccurred(JDBCEvent event) {
    }

    public void beforePrepareStatement(JDBCEvent event) {
        eventOccurred(event);
    }

    public void afterPrepareStatement(JDBCEvent event) {
        eventOccurred(event);
    }

    public void beforeCreateStatement(JDBCEvent event) {
        eventOccurred(event);
    }

    public void afterCreateStatement(JDBCEvent event) {
        eventOccurred(event);
    }

    public void beforeExecuteStatement(JDBCEvent event) {
        eventOccurred(event);
    }

    public void afterExecuteStatement(JDBCEvent event) {
        eventOccurred(event);
    }

    public void beforeCommit(JDBCEvent event) {
        eventOccurred(event);
    }

    public void afterCommit(JDBCEvent event) {
        eventOccurred(event);
    }

    public void beforeRollback(JDBCEvent event) {
        eventOccurred(event);
    }

    public void afterRollback(JDBCEvent event) {
        eventOccurred(event);
    }

    public void beforeReturn(JDBCEvent event) {
        eventOccurred(event);
    }

    public void afterReturn(JDBCEvent event) {
        eventOccurred(event);
    }

    public void afterConnect(JDBCEvent event) {
        eventOccurred(event);
    }

    public void beforeClose(JDBCEvent event) {
        eventOccurred(event);
    }
}

