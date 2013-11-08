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
import org.apache.openjpa.jdbc.sql.SelectExecutor;

/**
 * A Select-oriented Result Object Provider whose Select has been executed
 * outside its own scope.
 *  
 * @author Pinaki Poddar
 *
 */
public class PreparedResultObjectProvider extends InstanceResultObjectProvider {
    /**
     * Constructor.
     *
     * @param sel the select to execute
     * @param store the store to delegate loading to
     * @param fetch the fetch configuration, or null for the default
     * @param res the result of the given select
     */
    public PreparedResultObjectProvider(SelectExecutor sel,
        ClassMapping mapping, JDBCStore store, JDBCFetchConfiguration fetch, 
        Result res) {
        super(sel, mapping, store, fetch);
        _res = res;
    }
    
    public void open() throws SQLException {
        // do nothing
    }
}
