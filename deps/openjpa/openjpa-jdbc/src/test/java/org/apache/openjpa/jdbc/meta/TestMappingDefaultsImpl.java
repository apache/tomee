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

import org.apache.openjpa.jdbc.schema.Table;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.conf.JDBCConfigurationImpl;

import junit.framework.TestCase;

public class TestMappingDefaultsImpl extends TestCase {

    public void setUp() {
    }

    /**
     * For databases that accept only short column names, test avoidance of 
     * duplicate column names when populating the table with long column names.
     */
    public void testPopulateWithLongColumnNames() {
        MappingDefaultsImpl mapping = new MappingDefaultsImpl();
        JDBCConfiguration conf = new JDBCConfigurationImpl(false, false);
        conf.setDBDictionary("oracle");
        mapping.setConfiguration(conf);
        Table table = new Table("testtable", null);
        Column[] cols = new Column[3];
        cols[0] = new
            Column("longnamelongnamelongnamelongnamelongnamelongname1", null);
        cols[1] = new
            Column("longnamelongnamelongnamelongnamelongnamelongname2", null);
        cols[2] = new
            Column("longnamelongnamelongnamelongnamelongnamelongname3", null);
        MappingRepository mr = new MappingRepository();
        mr.setConfiguration(conf);
        Version version = new Version(new ClassMapping(String.class,mr));
        mapping.populateColumns(version, table, cols);
        assertFalse("column names are conflicted : " + cols[0].getName(),
                cols[0].getName().equals(cols[1].getName()));
        assertFalse("column names are conflicted : " + cols[0].getName(),
                cols[0].getName().equals(cols[2].getName()));
        assertFalse("column names are conflicted : " + cols[1].getName(),
                cols[1].getName().equals(cols[2].getName()));
    }
}
