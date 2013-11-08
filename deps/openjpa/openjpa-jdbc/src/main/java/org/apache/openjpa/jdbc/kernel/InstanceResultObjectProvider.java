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

import java.sql.SQLException;

import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.sql.Result;
import org.apache.openjpa.jdbc.sql.Select;
import org.apache.openjpa.jdbc.sql.SelectExecutor;
import org.apache.openjpa.util.ProxyCalendar;

/**
 * Object provider implementation wrapped around a {@link Select}.
 *
 * @author Abe White
 * @nojavadoc
 */
public class InstanceResultObjectProvider
    extends SelectResultObjectProvider {

    private final ClassMapping _mapping;

    /**
     * Constructor.
     *
     * @param sel the select to execute
     * @param mapping the mapping for the base class of the result objects
     * @param store the store to delegate loading to
     * @param fetch the fetch configuration, or null for default
     */
    public InstanceResultObjectProvider(SelectExecutor sel,
        ClassMapping mapping, JDBCStore store, JDBCFetchConfiguration fetch) {
        super(sel, store, fetch);
        _mapping = mapping;
    }

    public Object getResultObject()
        throws SQLException {
        Result res = getResult();
        ClassMapping mapping = res.getBaseMapping();
        if (mapping == null)
            mapping = _mapping;
        Object ret = res.load(mapping, getStore(), getFetchConfiguration());
        if (ret != null && ret instanceof ProxyCalendar) {
            ret = ((ProxyCalendar) ret).copy(ret);
        }
        return ret;
    }
}
