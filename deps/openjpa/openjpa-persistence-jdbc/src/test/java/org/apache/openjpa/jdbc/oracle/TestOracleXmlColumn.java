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
package org.apache.openjpa.jdbc.oracle;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.sql.DataSource;

import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.jdbc.sql.OracleDictionary;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.apache.openjpa.persistence.test.AbstractPersistenceTestCase;

public class TestOracleXmlColumn extends AbstractPersistenceTestCase {

    private static String projectStr = "project";
    private static String xmlData =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
        + "<" + projectStr + " xmlns=\"http://maven.apache.org/POM/4.0.0\" "
        + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
        + "xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 "
        + "http://maven.apache.org/maven-v4_0_0.xsd\">"
        + "</" + projectStr + ">";

    private boolean skipTest(DBDictionary dict) {
        return !(dict instanceof OracleDictionary);
    }

    public void setUp() throws SQLException {
        OpenJPAEntityManagerFactorySPI emf = createEMF();

        JDBCConfiguration conf = ((JDBCConfiguration) emf.getConfiguration());
        DBDictionary dict = conf.getDBDictionaryInstance();

        if (skipTest(dict)) {
            emf.close();
            return;
        }

        // the mapping tool doesn't handle creating XML columns that map to strings
        // build table manually
        Connection con = ((DataSource) conf.getConnectionFactory()).getConnection();
        Statement stmt = con.createStatement();
        String ddl = "DROP TABLE XmlColEntity";
        try { 
            stmt.execute(ddl);
            con.commit();
        } catch (SQLException se) {
            // assume the table did not exist.
            con.rollback();
        }

        ddl =
            "CREATE TABLE XmlColEntity (ID NUMBER NOT NULL, XMLCOLUMN " + dict.xmlTypeName
                + ", VERSION NUMBER, PRIMARY KEY (ID))";
        stmt.execute(ddl);
        String insertSql = "INSERT into XmlColEntity (ID, XMLCOLUMN, VERSION) VALUES (42, '" + xmlData + "', 1)";
        stmt.execute(insertSql);
        con.commit();

        stmt.close();
        con.close();
        emf.close();
    }

    public void testCrudXmlColumn() throws SQLException {
        // This test will fail with Oracle JDBC driver version 11.2.0.1.0.
        // It passes with 10.2.0.1.0 (maybe others).
        OpenJPAEntityManagerFactorySPI emf =
            createEMF(XmlColEntity.class,
                "openjpa.jdbc.SchemaFactory", "native", 
                "openjpa.jdbc.SynchronizeMappings",  "");

        JDBCConfiguration conf = ((JDBCConfiguration) emf.getConfiguration());
        DBDictionary dict = conf.getDBDictionaryInstance();

        if (skipTest(dict)) {
            emf.close();
            return;
        }

        EntityManager em = emf.createEntityManager();
        EntityTransaction tran = em.getTransaction();

        XmlColEntity xce = new XmlColEntity();
        xce.setId(1);
        xce.setXmlColumn(xmlData);

        tran.begin();
        em.persist(xce);
        tran.commit();
        em.close();

        em = emf.createEntityManager();
        xce = em.find(XmlColEntity.class, 1);
        assertNotNull(xce);
        assertEquals(xmlData, xmlResult(xce.getXmlColumn()));

        em.close();
        emf.close();
    }
    
    public void testExistingColumn() throws SQLException {
        // This test will fail with Oracle JDBC driver version 11.2.0.1.0.
        // It passes with 10.2.0.1.0 (maybe others).
        OpenJPAEntityManagerFactorySPI emf =
            createEMF(XmlColEntity.class,
                "openjpa.jdbc.SchemaFactory", "native", 
                "openjpa.jdbc.SynchronizeMappings",  "");

        JDBCConfiguration conf = ((JDBCConfiguration) emf.getConfiguration());
        DBDictionary dict = conf.getDBDictionaryInstance();

        if (skipTest(dict)) {
            emf.close();
            return;
        }

        EntityManager em = emf.createEntityManager();

        XmlColEntity xce = em.find(XmlColEntity.class, 42); 
        assertNotNull(xce);
        assertNotNull(xce.getXmlColumn());
        assertEquals(xmlData, xmlResult(xce.getXmlColumn()));
        em.close();
        emf.close();
    }

    private String xmlResult(String xml) {
        xml = xml.replace("\r", "").replace("\n", "").replace("/>", "></" + projectStr + ">").trim();
        return xml;
    }
}
