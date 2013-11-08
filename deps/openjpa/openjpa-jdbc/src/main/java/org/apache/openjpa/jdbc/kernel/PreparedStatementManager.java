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

import java.util.Collection;

import org.apache.openjpa.jdbc.sql.RowImpl;

/**
 * Manages prepared statement execution.
 *
 * @author Abe White
 */
public interface PreparedStatementManager {

    /**
     * Return the exceptions encountered during all flushes.
     */
    public Collection getExceptions();

    /**
     * Flush the given row.
     */
    public void flush(RowImpl row);

    /**
     * This method must be called after the last row has been
     * flushed, to flush any remaining statements.
     */
    public void flush();
}
