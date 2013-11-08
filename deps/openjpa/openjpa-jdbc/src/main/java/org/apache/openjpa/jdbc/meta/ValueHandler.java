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

import java.io.Serializable;
import java.sql.SQLException;

import org.apache.openjpa.jdbc.kernel.JDBCFetchConfiguration;
import org.apache.openjpa.jdbc.kernel.JDBCStore;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.schema.ColumnIO;
import org.apache.openjpa.jdbc.sql.Result;
import org.apache.openjpa.kernel.OpenJPAStateManager;

/**
 * Maps a value to a relational schema. Value handler are stateless.
 *
 * @author Abe White
 * @since 0.4.0
 */
public interface ValueHandler
    extends Serializable {

    /**
     * Map the given value and return all mapped columns, or simply return an
     * array of unmapped default columns. The columns should have at least
     * their <code>Name</code> and <code>JavaType</code> properties set.
     *
     * @param name use as a base to form column name(s); the column names
     * of unmapped columns will automatically be made to fit
     * database limitations
     * @param io I/O information about mapped columns; you do not
     * have to set this information if returning templates
     * @param adapt whether to adapt the mapping or schema
     */
    public Column[] map(ValueMapping vm, String name, ColumnIO io,
        boolean adapt);

    /**
     * Return whether the values managed by this handler can be used in
     * state image versioning.
     */
    public boolean isVersionable(ValueMapping vm);

    /**
     * Return whether this handler potentially must load extra data to extract
     * the object value from its datastore representation.
     */
    public boolean objectValueRequiresLoad(ValueMapping vm);

    /**
     * Return the argument to pass to the result set when loading data
     * via {@link Result#getObject}, or null if none. If this value
     * occupies multiple columns, return an array with one element per
     * column. You may return null if all array elements would be null.
     */
    public Object getResultArgument(ValueMapping vm);

    /**
     * Translate the given value to its datastore equivalent. If this value
     * occupies multiple columns, return an object array with one element
     * per column. For relation id columns, return the state manager
     * the column depends on.
     */
    public Object toDataStoreValue(ValueMapping vm, Object val,
        JDBCStore store);

    /**
     * Translate the given datastore value into its Java equivalent. If
     * the value occupies multiple columns, the given object will be an object
     * array with one entry per column. This method is only called if
     * {@link #objectValueRequiresLoad} returns false.
     */
    public Object toObjectValue(ValueMapping vm, Object val);

    /**
     * Translate the given datastore value into its Java equivalent. If
     * the value occupies multiple columns, the given object will be an object
     * array with one entry per column. This method is only called if
     * {@link #objectValueRequiresLoad} returns true.
     *
     * @param sm the state manager that owns the value; may be null if
     * loading a projection
     */
    public Object toObjectValue(ValueMapping vm, Object val,
        OpenJPAStateManager sm, JDBCStore store, JDBCFetchConfiguration fetch)
        throws SQLException;
}
