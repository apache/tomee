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

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;

import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.schema.SchemaTool;
import org.apache.openjpa.persistence.OpenJPAPersistence;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.apache.openjpa.persistence.simple.AllFieldTypes;

import junit.framework.TestCase;


public class TestMappingToolAutoDelete
    extends TestCase {

    private JDBCConfiguration _conf;
    private OpenJPAEntityManagerFactorySPI emf;

    public void setUp() {
        Map props = new HashMap(System.getProperties());
        props.put("openjpa.MetaDataFactory",
            "jpa(Types=" + AllFieldTypes.class.getName() + ")");
        emf = (OpenJPAEntityManagerFactorySPI) OpenJPAPersistence.cast( 
            Persistence.createEntityManagerFactory("test", props));
        _conf = (JDBCConfiguration) emf.getConfiguration();
        
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(new AllFieldTypes());
        em.getTransaction().commit();
        em.close();
    }
    
    public void tearDown() {
        emf.close();
    }

    public void testMappingToolAutoDelete() 
        throws IOException, SQLException {
        MappingTool.Flags flags = new MappingTool.Flags();
        
        // indirect validation that comma-separated schema actions work
        flags.schemaAction = SchemaTool.ACTION_ADD + "," 
            + SchemaTool.ACTION_DELETE_TABLE_CONTENTS;
        
        MappingTool.run(_conf, new String[0], flags, null);

        EntityManager em = emf.createEntityManager();
        assertEquals(Long.valueOf(0), 
            em.createQuery("select count(o) from AllFieldTypes o")
                .getSingleResult());
        em.close();
    }
}
