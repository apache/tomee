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
package org.apache.openjpa.persistence.jdbc.annotations;

import java.sql.Blob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import javax.persistence.EntityManager;

import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.persistence.OpenJPAEntityManagerSPI;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Test for serialized, clob, and lob types.
 *
 * @author Steve Kim
 */
public class TestSerializedLobs extends SingleEMFTestCase {

    private static final Date DATE = new Date();

    public void setUp() {
        setUp(AnnoTest1.class, AnnoTest2.class, Flat1.class, CLEAR_TABLES);
    }

    // Serialized fields not being read properly
    public void testSerialized() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        AnnoTest1 pc1 = new AnnoTest1(1);
        AnnoTest1 pc2 = new AnnoTest1(2);
        pc1.setSerialized("ASDASD");
        pc2.setSerialized(DATE);
        em.persist(pc1);
        em.persist(pc2);
        em.getTransaction().commit();
        em.close();

        em = emf.createEntityManager();
        pc1 = em.find(AnnoTest1.class, new Long(1));
        pc2 = em.find(AnnoTest1.class, new Long(2));
        assertEquals("ASDASD", pc1.getSerialized());
        assertEquals(DATE, pc2.getSerialized());
        em.close();
    }

    public void testBlob()
        throws Exception {
        OpenJPAEntityManagerSPI em = emf.createEntityManager();
        em.getTransaction().begin();

        AnnoTest1 pc = new AnnoTest1(1);
        pc.setBlob("Not Null".getBytes());
        em.persist(pc);
        em.getTransaction().commit();
        em.close();

        em = emf.createEntityManager();
        pc = em.find(AnnoTest1.class, new Long(1));
        assertEquals("Not Null", new String(pc.getBlob()));
        Connection conn = (Connection) em.getConnection();
        Statement stmnt = conn.createStatement();
        ResultSet rs = stmnt.executeQuery("SELECT BLOBVAL FROM ANNOTEST1 "
            + "WHERE PK = 1");
        assertTrue(rs.next());

        JDBCConfiguration conf = (JDBCConfiguration) em.getConfiguration();
        DBDictionary dict = conf.getDBDictionaryInstance();
        if (dict.useGetBytesForBlobs)
            rs.getBytes(1);
        else if (dict.useGetObjectForBlobs)
            rs.getObject(1);
        else {
            Blob blob = rs.getBlob(1);
            blob.getBytes(1L, (int) blob.length());
        }
        assertEquals("Not Null", new String(pc.getBlob()));

        try {
            rs.close();
        } catch (SQLException e) {
        }
        try {
            stmnt.close();
        } catch (SQLException e) {
        }
        try {
            conn.close();
        } catch (SQLException e) {
        }
        em.close();
    }

    public void testClob()
        throws Exception {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        AnnoTest1 pc = new AnnoTest1(1);
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < 1000; i++)
            buf.append((char) ('a' + (i % 24)));
        pc.setClob(buf.toString());
        em.persist(pc);
        em.getTransaction().commit();
        em.close();

        em = emf.createEntityManager();
        pc = em.find(AnnoTest1.class, new Long(1));
        String str = pc.getClob();
        assertEquals(1000, str.length());
        for (int i = 0; i < str.length(); i++)
            assertEquals('a' + (i % 24), str.charAt(i));
        em.close();
    }

    public void testNullableClob() throws Exception {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        AnnoTest1 pc = new AnnoTest1(1);
        em.persist(pc);
        em.getTransaction().commit();
        em.close();

        em = emf.createEntityManager();
        pc = em.find(AnnoTest1.class, new Long(1));
        String str = pc.getClob();
        assertNull(str);
        em.close();
    }

    public void testNullableBlob() throws Exception {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        AnnoTest1 pc = new AnnoTest1(1);
        em.persist(pc);
        em.getTransaction().commit();
        em.close();

        em = emf.createEntityManager();
        pc = em.find(AnnoTest1.class, new Long(1));
        byte[] bl = pc.getBlob();
        assertNull(bl);
        em.close();
    }
}
