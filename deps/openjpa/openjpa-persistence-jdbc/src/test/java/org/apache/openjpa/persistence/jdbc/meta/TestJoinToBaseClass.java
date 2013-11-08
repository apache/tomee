/*
 * TestJoinToBaseClass.java
 *
 * Created on October 3, 2006, 4:19 PM
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
import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.meta.FieldMapping;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.schema.ForeignKey;
import org.apache.openjpa.jdbc.schema.Table;

import org.apache.openjpa.persistence.jdbc.common.apps.*;


import org.apache.openjpa.persistence.OpenJPAEntityManagerFactory;
import org.apache.openjpa.persistence.OpenJPAEntityManager;


public class TestJoinToBaseClass
        extends org.apache.openjpa.persistence.jdbc.kernel.BaseJDBCTest {
    
    
    
    /** Creates a new instance of TestJoinToBaseClass */
    public TestJoinToBaseClass(String name) 
    {
    	super(name);
    }
    
    public void testMapping() {
        ClassMapping mapping = ((JDBCConfiguration) getConfiguration()).
            getMappingRepositoryInstance().
            getMapping(MappingTest5.class, null, true);

        Table supTable = mapping.getPCSuperclassMapping().getTable();
        assertTrue(mapping.getTable() != supTable);
        FieldMapping field = mapping.getFieldMapping("vertRel");
        ForeignKey fk = field.getForeignKey();
        assertEquals(mapping.getTable(), fk.getTable());
        assertEquals(supTable, fk.getPrimaryKeyTable());
        Column[] cols = field.getColumns();
        assertEquals(2, cols.length);
        assertEquals("V1", cols[0].getName());
        assertEquals("V2", cols[1].getName());
    }

    public void testConstraintAnalysis() {
        //FIXME jthomas
        //PersistenceManagerFactory factory = getPMFactory(new String[]{
          //  "openjpa.jdbc.SchemaFactory", "native(ForeignKeys=true)",
        //});
        OpenJPAEntityManagerFactory factory=null;
        OpenJPAEntityManager pm = factory.createEntityManager();
        startTx(pm);
        
       deleteAll( MappingTest1.class,pm);
       deleteAll( MappingTest2.class,pm);
        endTx(pm);
        pm.close();

        pm = factory.createEntityManager();
        startTx(pm);
        for (int i = 0; i < 10; i++) {
            MappingTest5 pc1 = new MappingTest5();
            pc1.setPk1(i);
            pc1.setPk2(i + 1);
            MappingTest5 pc2 = new MappingTest5();
            pc2.setPk1(i + 10);
            pc2.setPk2(i + 11);
            pc1.setVertRel(pc2);
            pc2.setVertRel(pc1);
            pm.persist(pc1);
        }
        endTx(pm);
        pm.close();

        assertSizes(20, MappingTest5.class);

        pm = factory.createEntityManager();
        startTx(pm);
        deleteAll(MappingTest2.class,pm);
        endTx(pm);
        pm.close();
    }

    private void assertSizes(int size, Class cls) {
        assertSize(size, currentEntityManager().createExtent(cls, true).list());
    }
}
