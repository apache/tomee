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
package org.apache.openjpa.persistence.annotations;

import java.sql.*;
import java.util.Date;

import org.apache.openjpa.persistence.OpenJPAEntityManager;

import
    org.apache.openjpa.persistence.annotations.common.apps.annotApp.annotype.*;


/**
 * Test for serialized, clob, and lob types.
 *
 * @author Steve Kim
 */
public class TestSerializedLobs extends AnnotationTestCase
{

	public TestSerializedLobs(String name)
	{
		super(name, "annotationcactusapp");
	}

    private static final Date DATE = new Date();

    public void setUp() {
        deleteAll(AnnoTest1.class);
    }

    // Serialized fields not being read properly
    public void testSerialized() {
        OpenJPAEntityManager em =(OpenJPAEntityManager) currentEntityManager();
        startTx(em);
        AnnoTest1 pc1 = new AnnoTest1(1);
        AnnoTest1 pc2 = new AnnoTest1(2);
        pc1.setSerialized("ASDASD");
        pc2.setSerialized(DATE);
        em.persist(pc1);
        em.persist(pc2);
        endTx(em);
        endEm(em);

        em =(OpenJPAEntityManager) currentEntityManager();
        pc1 = em.find(AnnoTest1.class, em.getObjectId(pc1));
        pc2 = em.find(AnnoTest1.class, em.getObjectId(pc2));
        assertEquals("ASDASD", pc1.getSerialized());
        assertEquals(DATE, pc2.getSerialized());
        endEm(em);
    }

    public void testBlob()
        throws Exception {
        OpenJPAEntityManager em = (OpenJPAEntityManager) currentEntityManager();
        startTx(em);

        AnnoTest1 pc = new AnnoTest1(1);
        pc.setBlob("Not Null".getBytes());
        em.persist(pc);
        endTx(em);
        endEm(em);

        em = (OpenJPAEntityManager) currentEntityManager();
        pc = em.find(AnnoTest1.class, em.getObjectId(pc));
        assertEquals("Not Null", new String(pc.getBlob()));
        Connection conn = (Connection) em.getConnection();
        Statement stmnt = conn.createStatement();
        ResultSet rs = stmnt.executeQuery("SELECT BLOBVAL FROM ANNOTEST1 "
            + "WHERE PK = 1");
        assertTrue(rs.next());

       /** JDBCConfiguration conf = (JDBCConfiguration) em.getConfiguration();
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
        }**/
        endEm(em);
    }

    public void testClob()
        throws Exception {
        OpenJPAEntityManager em =(OpenJPAEntityManager) currentEntityManager();
        startTx(em);

        AnnoTest1 pc = new AnnoTest1(1);
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < 1000; i++)
            buf.append((char) ('a' + (i % 24)));
        pc.setClob(buf.toString());
        em.persist(pc);
        endTx(em);
        endEm(em);

        em =(OpenJPAEntityManager) currentEntityManager();
        pc = em.find(AnnoTest1.class,em.getObjectId(pc));
        String str = pc.getClob();
        assertEquals(1000, str.length());
        for (int i = 0; i < str.length(); i++)
            assertEquals('a' + (i % 24), str.charAt(i));
        endEm(em);
    }
}
