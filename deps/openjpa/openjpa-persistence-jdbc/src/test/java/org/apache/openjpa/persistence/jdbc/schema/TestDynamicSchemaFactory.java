/*
 * TestDynamicSchemaFactory.java
 *
 * Created on October 6, 2006, 1:34 PM
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

import java.sql.Types;

import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.conf.JDBCConfigurationImpl;
import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.meta.Discriminator;
import org.apache.openjpa.jdbc.meta.MappingRepository;
import org.apache.openjpa.jdbc.meta.Version;
import org.apache.openjpa.jdbc.schema.DynamicSchemaFactory;
import org.apache.openjpa.jdbc.schema.Schemas;
import org.apache.openjpa.jdbc.schema.Table;

import org.apache.openjpa.persistence.jdbc.common.apps.*;


public class TestDynamicSchemaFactory
        extends org.apache.openjpa.persistence.jdbc.kernel.BaseJDBCTest {
    
    private ClassMapping _mapping = null;    
    
    /** Creates a new instance of TestDynamicSchemaFactory */
    public TestDynamicSchemaFactory() {
    }
    public TestDynamicSchemaFactory(String test) {
        super(test);
    }

    public void setUp()
        throws Exception {
        JDBCConfiguration conf = new JDBCConfigurationImpl();
        conf.setSchemaFactory(DynamicSchemaFactory.class.getName());
        MappingRepository repos = conf.newMappingRepositoryInstance();
        _mapping = repos.getMapping(MappingTest1.class, null, true);
    }

    public void testClassMapping() {
        Table table = _mapping.getTable();
        assertEquals("MAPPINGTEST1", table.getName().toUpperCase());
        assertEquals(1, table.getPrimaryKey().getColumns().length);
        int type = table.getPrimaryKey().getColumns()[0].getType();
        assertEquals(Schemas.getJDBCName(type), Types.INTEGER, type);
    }

    public void testIndicators() {
        Version vers = _mapping.getVersion();
        assertNotNull(vers);
        assertEquals("MAPPINGTEST1", vers.getColumns()[0].getTable().
            getName().toUpperCase());
        assertEquals(Types.INTEGER, vers.getColumns()[0].getType());

        Discriminator cls = _mapping.getDiscriminator();
        assertNotNull(cls);
        assertEquals("MAPPINGTEST1", cls.getColumns()[0].getTable().
            getName().toUpperCase());
        assertEquals(Types.VARCHAR, cls.getColumns()[0].getType());
    }

    public static void main(String[] args) {
       // main(TestDynamicSchemaFactory.class);
    }
    
}
