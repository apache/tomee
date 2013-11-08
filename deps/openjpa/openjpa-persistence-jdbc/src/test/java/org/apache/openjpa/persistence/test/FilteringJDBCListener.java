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
package org.apache.openjpa.persistence.test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.openjpa.jdbc.sql.SybaseDictionary;
import org.apache.openjpa.lib.jdbc.AbstractJDBCListener;
import org.apache.openjpa.lib.jdbc.JDBCEvent;

/**
 * JDBC listener suitable for use by the OpenJPA junit bucket. Some DBDictionaries (e.g. Sybase) generate a lot of
 * noise during connection setup - making testcases that rely on SQL count or sequences brittle.
 * 
 * This JDBC listener removes these noisy sql statements.
 */
public class FilteringJDBCListener extends AbstractJDBCListener {

    /**
     * Set of SQL statements which will be filtered out by this listener.
     */
    private Set<String> _ignoredSQL = new HashSet<String>();
    
    private List<String> _sqlStatements; 

    public FilteringJDBCListener(List<String> sql) {
        _sqlStatements = sql;
        
        // ignore connection setup SQL for Sybase
        _ignoredSQL.add(SybaseDictionary.NUMERIC_TRUNCATION_OFF_SQL);
        _ignoredSQL.add(SybaseDictionary.RIGHT_TRUNCATION_ON_SQL);
    }

    @Override
    public void beforeExecuteStatement(JDBCEvent event) {
        String sql = event.getSQL();
        if (sql != null && _sqlStatements != null && !_ignoredSQL.contains(sql)) {
            _sqlStatements.add(sql);
        }
    }
    
    public void clear() { 
        _sqlStatements.clear(); 
    }
    
    public List<String> getCopy() { 
        return new ArrayList<String>(_sqlStatements);
    }
}
