/*
 * TestLazySchemaFactory.java
 *
 * Created on October 6, 2006, 1:47 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

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
package org.apache.openjpa.persistence.jdbc.schema;

import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.schema.LazySchemaFactory;
import org.apache.openjpa.jdbc.schema.SchemaGroup;
import org.apache.openjpa.jdbc.schema.Table;

import org.apache.openjpa.persistence.common.utils.AbstractTestCase;


public class TestLazySchemaFactory
        extends org.apache.openjpa.persistence.jdbc.kernel.BaseJDBCTest {
       
    private SchemaGroup _group = null;
    private boolean _fks = true;
    
    /** Creates a new instance of TestLazySchemaFactory */
    public TestLazySchemaFactory() {
    }

    public TestLazySchemaFactory(String test) {
        super(test);
    }

    public void setUp()
        throws Exception {
        // see if the dict supports foreign keys; mysql can support them, but
        // can't detect keys added through alter table commands, which is what
        // kodo uses
        JDBCConfiguration conf = (JDBCConfiguration) getConfiguration();
        _fks = conf.getDBDictionaryInstance().supportsForeignKeys
            && getCurrentPlatform() != AbstractTestCase.Platform.MYSQL
            && getCurrentPlatform() != AbstractTestCase.Platform.MARIADB;

        LazySchemaFactory factory = new LazySchemaFactory();
        factory.setConfiguration(conf);
        factory.setPrimaryKeys(true);
        factory.setIndexes(true);
        factory.setForeignKeys(true);
        _group = factory;
    }

    public void testLazySchemaFactory() {
        // should read tables 1 and 2...
        Table table = _group.findTable("T1");
        assertNotNull(table);
        Table table2 = _group.findTable("t1");
        assertTrue(table == table2);

        int expectedColumns = 2;

        // Sybase has an extra "UNQ_INDEX" column.
        if (getCurrentPlatform() == AbstractTestCase.Platform.SYBASE)
            expectedColumns++;

        assertEquals(expectedColumns, table.getColumns().length);
        if (_fks)
            assertEquals(1, table.getForeignKeys().length);

        table2 = _group.findTable("T2");
        assertNotNull(table2);
        if (_fks)
            assertTrue(table.getForeignKeys()[0].getPrimaryKeyColumns()[0].
                getTable() == table2);

        assertNull(table.getSchema().getTable("T3"));

        // should read table 3 only...
        Table table3 = _group.findTable("T3");
        assertNotNull(table3);
        assertTrue(table.getSchema().getTable("T3") == table3);
        if (_fks)
            assertTrue(table3.getForeignKeys()[0].getPrimaryKeyColumns()[0].
                getTable() == table);

        assertNull(table3.getSchema().getTable("T4"));
    }

    public static void main(String[] args) {
        //main(TestLazySchemaFactory.class);
    }
    
}
