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
package org.apache.openjpa.persistence.fields;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.sql.DB2Dictionary;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.apache.openjpa.persistence.test.SingleEMTestCase;

/*
 * This tests a particular kind of byte [] that is not a BLOB for DB2.  Don't need to
 * do anything except return if we are using a different DB.
 */
public class TestByteArray extends SingleEMTestCase {
    boolean runTest=false;
    
    public void setUp()  {
        super.setUp();
        OpenJPAEntityManagerFactorySPI ojpaEmf = (OpenJPAEntityManagerFactorySPI) emf;
        JDBCConfiguration conf = (JDBCConfiguration) ojpaEmf.getConfiguration();
        if (conf.getDBDictionaryInstance() instanceof DB2Dictionary) {
            runTest = true;
            super.setUp(ByteArrayHolder.class, CLEAR_TABLES);
        }
    }
    
    @SuppressWarnings("unchecked")
    public void testByteArray() {
        if (! runTest) { 
            // skip if not DB2 (from setup)
            return;
        }

        EntityManager em = emf.createEntityManager();
        EntityManager em2 = emf.createEntityManager();

        byte[] ba =
            new byte[] { (byte) 0xa0, (byte) 0x1b, (byte) 0x01, (byte) 0x1f,
                (byte) 0x38, (byte) 0xcf, (byte) 0x67, (byte) 0x35,
                (byte) 0x55, (byte) 0x43, (byte) 0xd9, (byte) 0xf6,
                (byte) 0x71, (byte) 0x5e, (byte) 0x00, (byte) 0x00 };
        ByteArrayHolder holder = new ByteArrayHolder();
        holder.setTkiid(ba);
        try {
            em.getTransaction().begin();
            em.persist(holder);
            em.getTransaction().commit();
        } catch(Throwable t) {
            t.printStackTrace();
            fail("Error: Task insert failed");
        }

        // verify that the get works
        Query q = em2.createQuery("select e from ByteArrayHolder e");
        List<ByteArrayHolder> elist = q.getResultList();
        for (ByteArrayHolder e : elist) {
            String baFromH = new String(e.getTkiid());
            assertEquals(new String (ba), baFromH);
        }
        
        // verify that it's still in the original EntityManager 
        holder = em.find(ByteArrayHolder.class, holder.getTaskId());
        String baFromH = new String(holder.getTkiid());
        assertEquals(new String (ba), baFromH);
        closeEM(em);
        closeEM(em2);
    }
}
