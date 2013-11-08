/*
 * TestJDBCType.java
 *
 * Created on October 3, 2006, 4:11 PM
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
package org.apache.openjpa.persistence.jdbc.meta;

import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.conf.JDBCConfigurationImpl;
import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.meta.MappingRepository;
import org.apache.openjpa.jdbc.meta.RefreshStrategyInstaller;
import org.apache.openjpa.jdbc.meta.strats.BlobValueHandler;
import org.apache.openjpa.jdbc.meta.strats.ClobValueHandler;
import org.apache.openjpa.jdbc.meta.strats.MaxEmbeddedClobFieldStrategy;
import org.apache.openjpa.jdbc.meta.strats.StringFieldStrategy;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.schema.SchemaGroup;
import org.apache.openjpa.jdbc.schema.Table;
import org.apache.openjpa.jdbc.sql.DBDictionary;


import org.apache.openjpa.jdbc.meta.JavaSQLTypes;
import org.apache.openjpa.jdbc.schema.Column;

public class TestJDBCType
        extends org.apache.openjpa.persistence.jdbc.kernel.BaseJDBCTest {
	
    /** Creates a new instance of TestJDBCType */
    public TestJDBCType(String name) 
    {
    	super(name);
    }
    /**
     * Tests that application identity classes are returned correctly.
     */
    public void testSchemaGeneration() {
        JDBCConfiguration conf = new JDBCConfigurationImpl();
        DBDictionary dict = conf.getDBDictionaryInstance();
        MappingRepository repos = conf.getMappingRepositoryInstance();
        repos.setStrategyInstaller(new RefreshStrategyInstaller(repos));
        ClassMapping mapping = repos.getMapping(Column.class, null, true);

        Class cls;
        if (dict.getPreferredType(JavaSQLTypes.CLOB) ==  JavaSQLTypes.CLOB) {
            if (dict.maxEmbeddedClobSize > 0) {
                cls = mapping.getFieldMapping("toClob").getStrategy().
                    getClass();
                assertTrue(cls.getName(),
                    MaxEmbeddedClobFieldStrategy.class.isAssignableFrom(cls));
            } else {
                cls = mapping.getFieldMapping("toClob").getHandler().
                    getClass();
                assertTrue(cls.getName(),
                    ClobValueHandler.class.isAssignableFrom(cls));
            }
        } else
            assertTrue(mapping.getFieldMapping("toClob").getStrategy()
                instanceof StringFieldStrategy);

        cls = mapping.getFieldMapping("toBlob").getHandler().getClass();
        assertTrue(cls.getName(),
            BlobValueHandler.class.isAssignableFrom(cls));

        SchemaGroup schema = repos.getSchemaGroup();
        Table table = schema.getSchemas()[0].getTables()[0];
        Column[] cols = table.getColumns();
        for (int i = 0; i < cols.length; i++) {
            if (cols[i].getName().equalsIgnoreCase("id")
                || cols[i].getName().equalsIgnoreCase("versn")
                || cols[i].getName().equalsIgnoreCase("typ"))
                continue;
            if ("longToInt".equalsIgnoreCase(cols[i].getName()))
                assertEquals(dict.getPreferredType(JavaSQLTypes.INT),
                    cols[i].getType());
            else if ("longToSQL".equalsIgnoreCase(cols[i].getName()))
                assertEquals("varchar", cols[i].getTypeName());
            else if ("toClob".equalsIgnoreCase(cols[i].getName()))
                assertEquals(dict.getPreferredType(JavaSQLTypes.CLOB),
                    cols[i].getType());
            else if ("toBlob".equalsIgnoreCase(cols[i].getName()))
                assertEquals(dict.getPreferredType(JavaSQLTypes.BLOB),
                    cols[i].getType());
            else
                fail("Unknown column:" + cols[i].getName());
        }
    }    
}
