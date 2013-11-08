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
package org.apache.openjpa.jdbc.meta;

import java.sql.SQLException;

import org.apache.openjpa.jdbc.kernel.JDBCFetchConfiguration;
import org.apache.openjpa.jdbc.kernel.JDBCStore;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.schema.ColumnIO;
import org.apache.openjpa.jdbc.sql.Result;
import org.apache.openjpa.kernel.OpenJPAStateManager;

/**
 * Interface for field strategies that can managed fields of
 * embedded-element, embedded-key, and embedded-value objects. Fields of
 * directly embedded objects do not have to implement this interface.
 *
 * @author Abe White
 * @since 0.4.0
 */
public interface Embeddable {

    public static final Object UNSUPPORTED = new Object();

    /**
     * Return the columns used by this strategy.
     */
    public Column[] getColumns();

    /**
     * Return column I/O information for this mapping.
     */
    public ColumnIO getColumnIO();

    /**
     * Return the arguments needed to extract datastore values via
     * {@link Result#getObject} for each column.
     */
    public Object[] getResultArguments();

    /**
     * Convert the given Java value to its datastore equivalent. If this
     * mapping occupies multiple columns, return an object array with one
     * element per column. For relation id columns, return the state manager
     * the column depends on.
     */
    public Object toEmbeddedDataStoreValue(Object val, JDBCStore store);

    /**
     * Convert the given datastore value to its Java equivalent.
     * If {@link #getColumns} returns multiple columns, the given datastore
     * value will be an object array of the corresponding length. This method
     * must only be supported by mappings of embedded id objects. In other
     * cases {@link #loadEmbedded} will be used instead. Return
     * {@link #UNSUPPORTED} if this mapping cannot support this method.
     */
    public Object toEmbeddedObjectValue(Object val);

    /**
     * Load this strategy's field by transforming the given datastore value.
     * If {@link #getColumns} returns multiple columns, the given datastore
     * value will be an object array of the corresponding length. The value
     * does not have to be loaded immediately; it may be stored as impl data.
     */
    public void loadEmbedded(OpenJPAStateManager sm, JDBCStore store,
        JDBCFetchConfiguration fetch, Object val)
        throws SQLException;
}
