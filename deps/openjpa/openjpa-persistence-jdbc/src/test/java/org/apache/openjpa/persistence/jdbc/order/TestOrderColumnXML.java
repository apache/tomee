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
package org.apache.openjpa.persistence.jdbc.order;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.meta.FieldMapping;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.lib.meta.MetaDataSerializer;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.MetaDataRepository;
import org.apache.openjpa.persistence.ArgumentException;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.apache.openjpa.persistence.OpenJPAEntityManagerSPI;
import org.apache.openjpa.persistence.OpenJPAPersistence;
import org.apache.openjpa.persistence.jdbc.XMLPersistenceMappingParser;
import org.apache.openjpa.persistence.jdbc.XMLPersistenceMappingSerializer;
import org.apache.openjpa.persistence.test.AbstractCachedEMFTestCase;

public class TestOrderColumnXML extends AbstractCachedEMFTestCase {       

    /*
     * Validates the use of the nullable attribute on OrderColumn through
     * an entity defined in orm.xml
     */
    public void testOrderColumnNullableFalse() {
        
        OpenJPAEntityManagerFactorySPI emf1 = 
            (OpenJPAEntityManagerFactorySPI)OpenJPAPersistence.
            createEntityManagerFactory("BaseNoNullTest",
            "org/apache/openjpa/persistence/jdbc/order/" +
            "order-persistence-4.xml");

        OpenJPAConfiguration conf = emf1.getConfiguration();
        MetaDataRepository repos = conf.getMetaDataRepositoryInstance();

        // Force entity resolution
        repos.getMetaData(BaseTestEntity2.class, null, true);
        
        OpenJPAEntityManagerSPI em = emf1.createEntityManager();

        validateOrderColumnNullable(emf1, BaseTestEntity2.class, 
            "one2Melems", false);

        validateOrderColumnNullable(emf1, BaseTestEntity2.class, 
                "collelems", false);

        validateOrderColumnNullable(emf1, BaseTestEntity2.class, 
                "m2melems", false);

        em.close();
        try {
            if (emf1 != null)
                cleanupEMF(emf1);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }        
    }
    
    /*
     * Validates the use of the columnDefinition attribute on OrderColumn.
     */
    public void testOrderColumnColumnDefinition() {

        OpenJPAEntityManagerFactorySPI emf1 = 
            (OpenJPAEntityManagerFactorySPI)OpenJPAPersistence.
            createEntityManagerFactory("ColDefTest",
            "org/apache/openjpa/persistence/jdbc/order/" +
            "order-persistence-2.xml");

        // Create the EM.  This will spark the mapping tool.
        OpenJPAEntityManagerSPI em = emf1.createEntityManager();        
        // 
        // Create a collection using a custom column definition
        validateOrderColumnDef(emf1, ColDefTestEntity.class, 
            "one2Mcoldef", "INTEGER");

        validateOrderColumnDef(emf1, ColDefTestEntity.class, 
            "collcoldef", "INTEGER");

        validateOrderColumnDef(emf1, ColDefTestEntity.class, 
            "m2mcoldef", "INTEGER");

        // Add and query some values
        ColDefTestEntity cdent = new ColDefTestEntity();
        
        ColDefTestElement cdel1 = new ColDefTestElement("Element1");
        ColDefTestElement cdel2 = new ColDefTestElement("Element2");
        ColDefTestElement cdel3 = new ColDefTestElement("Element3");
        
        List<ColDefTestElement> one2Mcoldef = 
            new ArrayList<ColDefTestElement>();
        one2Mcoldef.add(cdel3);
        one2Mcoldef.add(cdel2);
        one2Mcoldef.add(cdel1);
        cdent.setOne2Mcoldef(one2Mcoldef);

        Set<ColDefTestElement> collcoldef = 
            new LinkedHashSet<ColDefTestElement>();
        collcoldef.add(cdel1);
        collcoldef.add(cdel2);
        collcoldef.add(cdel3);
        cdent.setCollcoldef(collcoldef);
        
        List<ColDefTestElement> m2mcoldef = new ArrayList<ColDefTestElement>();
        m2mcoldef.add(cdel2);
        m2mcoldef.add(cdel1);
        m2mcoldef.add(cdel3);
        cdent.setM2mcoldef(m2mcoldef);
        
        em.getTransaction().begin();
        em.persist(cdent);
        em.getTransaction().commit();
        
        em.close();
        try {
            if (emf1 != null)
                cleanupEMF(emf1);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    /**
     * Validates the use of the table attribute defined in XML
     */
    public void testOrderColumnTableXML() {   
        
        OpenJPAEntityManagerFactorySPI emf1 = 
            (OpenJPAEntityManagerFactorySPI)OpenJPAPersistence.
            createEntityManagerFactory("TableTest", 
            "org/apache/openjpa/persistence/jdbc/order/" +
            "order-persistence-5.xml");
        
        OpenJPAEntityManagerSPI em = emf1.createEntityManager();
        
        validateOrderColumnTable(emf1, BaseTestEntity3.class, "one2Melems", 
            "xml_o2m_table", "one2MOrder"); 
                    
        validateOrderColumnTable(emf1, BaseTestEntity3.class, "m2melems", 
             "xml_m2m_table", "m2morder"); 

        validateOrderColumnTable(emf1, BaseTestEntity3.class, "collelems", 
             "xml_coll_table", "collelems_ORDER"); 
        
        em.close();
        try {
            if (emf1 != null)
                cleanupEMF(emf1);
        } catch (Exception e) {
            fail(e.getMessage());
        }        
    }

    
    /*
     * Validates OrderBy and OrderColumn should not be specified together per 
     * the JPA 2.0 spec.
     */
    public void testOrderColumnOrderBy() {
        
        OpenJPAEntityManagerFactorySPI emf1 = null;
        OpenJPAEntityManagerSPI em = null;
        try {
            emf1 = (OpenJPAEntityManagerFactorySPI)OpenJPAPersistence.
                createEntityManagerFactory("ObOcTest", 
                    "org/apache/openjpa/persistence/jdbc/order/" +
                    "order-persistence-3.xml");
        
            em = emf1.createEntityManager();
            
            ObOcEntity ent = new ObOcEntity();
            List<Integer> intList = new ArrayList<Integer>();
            intList.add(new Integer(10));
            intList.add(new Integer(20));
            ent.setIntList(intList);
            
            em.getTransaction().begin();
            em.persist(intList);
            em.getTransaction().commit();

            em.close();
            em = null;
            fail("An exception should have been thrown.");
        } catch (Exception e) {
            assertException(e, ArgumentException.class);
        } finally {
            if (em != null)
                em.close();
        }
        try {
            if (emf1 != null)
                cleanupEMF(emf1);
        } catch (Exception e) {
            fail(e.getMessage());
        }        
    }
    
    public void testOrderColumnMetaDataSerialization() 
        throws Exception {

        OpenJPAEntityManagerFactorySPI emf1 = 
            (OpenJPAEntityManagerFactorySPI)OpenJPAPersistence.
            createEntityManagerFactory("BaseTest", 
            "org/apache/openjpa/persistence/jdbc/order/" +
            "order-persistence.xml");

        OpenJPAConfiguration conf = emf1.getConfiguration();
        MetaDataRepository repos = conf.newMetaDataRepositoryInstance();

        // Force entity resolution
        repos.getMetaData(BaseTestEntity1.class, null, true);

        XMLPersistenceMappingSerializer ser =
            new XMLPersistenceMappingSerializer((JDBCConfiguration)conf);
        ser.addAll(repos);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ser.serialize(new OutputStreamWriter(out), MetaDataSerializer.PRETTY);
        byte[] bytes = out.toByteArray();
        
        XMLPersistenceMappingParser parser =
            new XMLPersistenceMappingParser((JDBCConfiguration)conf);
        parser.parse(new InputStreamReader
            (new ByteArrayInputStream(bytes)), "bytes");
        MetaDataRepository mdr2 = parser.getRepository();

        ClassMetaData _entityMeta2 = 
            mdr2.getMetaData(BaseTestEntity1.class, null, true);

        // Assert metadata is populated correctly
        FieldMapping fm = (FieldMapping)_entityMeta2.getField("one2Melems");
        Column oc = fm.getOrderColumn();
        assertNotNull(oc);
        assertEquals(oc.getName(),"one2MOrder");

        fm = (FieldMapping)_entityMeta2.getField("m2melems");
        oc = fm.getOrderColumn();
        assertNotNull(oc);
        assertEquals(oc.getName(),"m2morder");

        fm = (FieldMapping)_entityMeta2.getField("collelems");
        oc = fm.getOrderColumn();
        assertNotNull(oc);
        assertEquals(oc.getName(),"collelems_ORDER");

        try {
            if (emf1 != null)
                cleanupEMF(emf1);
        } catch (Exception e) {
            fail(e.getMessage());
        }        
    }
               
    private Column getOrderColumn(OpenJPAEntityManagerFactorySPI emf1, 
        Class clazz, String fieldName) {
        JDBCConfiguration conf = (JDBCConfiguration) emf1.getConfiguration();
        ClassMapping cls = conf.getMappingRepositoryInstance().
            getMapping(clazz, null, true);
        FieldMapping fm = cls.getFieldMapping(fieldName);
        Column oc = fm.getOrderColumn();
        assertNotNull(oc);
        return oc;
    }

    private void validateOrderColumnTable(
            OpenJPAEntityManagerFactorySPI emf1, 
            Class clazz, String fieldName, String tableName, 
            String columnName) {        
            Column oc = getOrderColumn(emf1, clazz, fieldName);
            // Verify the oc has the correct table name
            assertTrue(oc.getTableName().equalsIgnoreCase(tableName));
            // Verify the table exists in the db
            assertTrue(tableAndColumnExists(emf1, null, tableName, null, 
                columnName));
    }

    private void validateOrderColumnDef(
            OpenJPAEntityManagerFactorySPI emf1, Class clazz, String fieldName, 
            String type) {        
            Column oc = getOrderColumn(emf1, clazz, fieldName);
            assertEquals(type, oc.getTypeName());
    }

    private void validateOrderColumnNullable(
            OpenJPAEntityManagerFactorySPI emf1, Class clazz, String fieldName, 
            boolean nullable) {
            Column oc = getOrderColumn(emf1, clazz, fieldName);
            assertEquals(nullable, !oc.isNotNull());
    }


    /**
     * Method to verify a table was created for the given name and schema
     */
    private boolean tableAndColumnExists(OpenJPAEntityManagerFactorySPI emf1, 
            OpenJPAEntityManagerSPI em, String tableName, String schemaName,
            String columnName) {
        JDBCConfiguration conf = (JDBCConfiguration) emf1.getConfiguration();
        DBDictionary dict = conf.getDBDictionaryInstance();
        OpenJPAEntityManagerSPI em1 = em;
                
        // If no em supplied, create one
        if (em1 == null) {
            em1 = emf1.createEntityManager();
        }
        Connection conn = (Connection)em1.getConnection();
        try {
            DatabaseMetaData dbmd = conn.getMetaData();
            // (meta, catalog, schemaName, tableName, conn)
            Column[] cols = dict.getColumns(dbmd, null, null, 
                    tableName, columnName, conn);
            if (cols != null && cols.length == 1) {
                Column col = cols[0];
                String colName = col.getName();
                if (col.getTableName().equalsIgnoreCase(tableName) &&
                    (schemaName == null || 
                    col.getSchemaName().equalsIgnoreCase(schemaName)) &&
                    colName.equalsIgnoreCase(columnName))
                    return true;
            }
        } catch (Throwable e) {
            fail("Unable to get column information.");
        } finally {
            if (em == null) {
                em1.close();
            }
        }
        return false;
    }
    /**
     * Closes a specific entity manager factory and cleans up 
     * associated tables.
     */
    private void cleanupEMF(OpenJPAEntityManagerFactorySPI emf1) 
      throws Exception {

        if (emf1 == null)
            return;

        try {
            clear(emf1);
        } catch (Exception e) {
            // if a test failed, swallow any exceptions that happen
            // during tear-down, as these just mask the original problem.
            if (testResult.wasSuccessful())
                throw e;
        } finally {
            closeEMF(emf1);
        }
    }    
 }
