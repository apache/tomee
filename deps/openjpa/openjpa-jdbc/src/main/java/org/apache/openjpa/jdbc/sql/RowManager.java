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
package org.apache.openjpa.jdbc.sql;

import java.sql.SQLException;

import org.apache.openjpa.jdbc.schema.Table;
import org.apache.openjpa.kernel.OpenJPAStateManager;

/**
 * Manages rows during an insert/update/delete process. Row managers
 * do not have to be threadsafe.
 *
 * @author Abe White
 */
public interface RowManager {

    /**
     * Return the row for the given table and owner object, or null if
     * <code>create</code> is false and the row has not already been created.
     * The action must be one of {@link Row#ACTION_INSERT},
     * {@link Row#ACTION_UPDATE}, {@link Row#ACTION_DELETE}.
     */
    public Row getRow(Table table, int action, OpenJPAStateManager sm,
        boolean create);

    /**
     * Return a row for the given secondary table.
     * The action must be one of {@link Row#ACTION_INSERT},
     * {@link Row#ACTION_UPDATE}, {@link Row#ACTION_DELETE}.
     * Note that secondary rows are not considered when creating the foreign
     * key dependency graph, with can cause constraint violations when using
     * the <code>UPDATE</code> action. Only use this action if the secondary
     * row does not have restrict-action foreign keys. Otherwise use both
     * a delete and then an insert to perform the update.
     */
    public Row getSecondaryRow(Table table, int action);

    /**
     * Flush the secondary row; after flushing the row is available for reuse.
     * It will retain all previously set values.
     */
    public void flushSecondaryRow(Row row)
        throws SQLException;

    /**
     * Return a logical row representing an update that should be made to
     * all rows of the given table. The action must be one of
     * {@link Row#ACTION_UPDATE}, {@link Row#ACTION_DELETE}.
     */
    public Row getAllRows(Table table, int action);

    /**
     * Flush the logical row.
     */
    public void flushAllRows(Row row)
        throws SQLException;
}
