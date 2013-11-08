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
package org.apache.openjpa.jdbc.meta.strats;

import java.sql.SQLException;

import org.apache.openjpa.jdbc.kernel.JDBCFetchConfiguration;
import org.apache.openjpa.jdbc.kernel.JDBCStore;
import org.apache.openjpa.jdbc.meta.ValueHandler;
import org.apache.openjpa.jdbc.meta.ValueMapping;
import org.apache.openjpa.kernel.OpenJPAStateManager;

/**
 * No-op implementations of {@link ValueHandler} interface methods.
 */
public abstract class AbstractValueHandler
    implements ValueHandler {

    public boolean isVersionable(ValueMapping vm) {
        return false;
    }

    public boolean objectValueRequiresLoad(ValueMapping vm) {
        return false;
    }

    public Object getResultArgument(ValueMapping vm) {
        return null;
    }

    public Object toDataStoreValue(ValueMapping vm, Object val,
        JDBCStore store) {
        return val;
    }

    public Object toObjectValue(ValueMapping vm, Object val) {
        return val;
    }

    public Object toObjectValue(ValueMapping vm, Object val,
        OpenJPAStateManager sm, JDBCStore store, JDBCFetchConfiguration fetch)
        throws SQLException {
        return val;
    }
}
