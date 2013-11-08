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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.sql.DataSource;

import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.schema.ForeignKey;
import org.apache.openjpa.jdbc.schema.Table;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Test that makes sure the number of foreign keys are same on a table after
 * calling loadNameFromDB method of ForeignKey class.
 * 
 */
public class TestForeignKeyCountViolation extends SingleEMFTestCase {

    private JDBCConfiguration _conf;
    
    public void setUp() {
        super.setUp(EntityF.class, EntityG.class);
        Map props = new HashMap(System.getProperties());
        _conf = (JDBCConfiguration) emf.getConfiguration();
    }
    
    public void testFKCount() throws SQLException {
        EntityManager em = emf.createEntityManager();
        Table tableG = getMapping(EntityG.class).getTable();
        tableG.addForeignKey();
        int b4Count = tableG.getForeignKeys().length;
        
        EntityF f = new EntityF();
        f.setId(1);

        List<EntityG> listG = new ArrayList<EntityG>();
        EntityG g1 = new EntityG();
        g1.setId(1);
        listG.add(g1);
        g1.setEntityF(f);

        EntityG g2 = new EntityG();
        g2.setId(2);
        listG.add(g2);
        g2.setEntityF(f);

        EntityG g3 = new EntityG();
        g3.setId(3);
        listG.add(g3);
        g3.setEntityF(f);

        EntityG g4 = new EntityG();
        g4.setId(4);
        listG.add(g4);
        g4.setEntityF(f);

        f.setListG(listG);
        em.getTransaction().begin();
        em.persist(f);
        em.persist(g1);
        em.persist(g2);
        em.persist(g3);
        em.persist(g4);
        em.getTransaction().commit();
        
        ForeignKey fks[] = tableG.getForeignKeys();
        
        DataSource ds = (DataSource) _conf.getConnectionFactory();
        Connection c = ds.getConnection(_conf.getConnectionUserName(),
            _conf.getConnectionPassword());
        
        for (int i=0; i< fks.length; i++) {
            fks[i].loadNameFromDB(
                _conf.getDBDictionaryInstance(), c);
        }
        
        assertEquals(b4Count, tableG.getForeignKeys().length);
        em.close();
    }
    
    public void testFKNamefromDB()throws SQLException {
        
        EntityManager em = emf.createEntityManager();
        Table tableG = getMapping(EntityG.class).getTable();
        tableG.addForeignKey();
        
        EntityF f = new EntityF();
        f.setId(1);

        List<EntityG> listG = new ArrayList<EntityG>();
        EntityG g1 = new EntityG();
        g1.setId(1);
        listG.add(g1);
        g1.setEntityF(f);

        EntityG g2 = new EntityG();
        g2.setId(2);
        listG.add(g2);
        g2.setEntityF(f);

        EntityG g3 = new EntityG();
        g3.setId(3);
        listG.add(g3);
        g3.setEntityF(f);

        EntityG g4 = new EntityG();
        g4.setId(4);
        listG.add(g4);
        g4.setEntityF(f);

        f.setListG(listG);
        em.getTransaction().begin();
        em.persist(f);
        em.persist(g1);
        em.persist(g2);
        em.persist(g3);
        em.persist(g4);
        em.getTransaction().commit();
            
        DataSource ds = (DataSource) _conf.getConnectionFactory();
        Connection c = ds.getConnection(_conf.getConnectionUserName(),
            _conf.getConnectionPassword());
        
        ForeignKey fkfromDB[] = _conf.getDBDictionaryInstance().getImportedKeys(
            c.getMetaData(), c.getCatalog() , tableG.getSchemaName()
            , tableG.getName(), c);
        
        
        ArrayList<String> fkListfromDB = new ArrayList<String>();
        ArrayList<String> fkListfromTable = new ArrayList<String>();
        
        for (int i=0; i< fkfromDB.length; i++) {
            fkListfromDB.add(fkfromDB[i].getName());
        }
        
        ForeignKey fks[] = tableG.getForeignKeys();    
        for (int i=0; i< fks.length; i++) {
            String fkNamefromDB =fks[i].loadNameFromDB(
                _conf.getDBDictionaryInstance(), c);
            if( fkNamefromDB != null)
                fkListfromTable.add(fkNamefromDB);
        }
        
        assertEquals(fkListfromDB.toArray().length,
            fkListfromTable.toArray().length);
        
        Collections.sort(fkListfromTable);
        Collections.sort(fkListfromDB);
        
        for(int i=0; i< fkListfromDB.size(); i++)
        {
            assertEquals(fkListfromDB.get(i),fkListfromTable.get(i));
        }
        
        em.close();
    }

}
