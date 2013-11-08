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
package org.apache.openjpa.persistence.event;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.apache.openjpa.event.TransactionEvent;
import org.apache.openjpa.event.TransactionListener;
import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.jdbc.sql.OracleDictionary;
import org.apache.openjpa.jdbc.sql.SybaseDictionary;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.apache.openjpa.persistence.OpenJPAEntityManagerSPI;
import org.apache.openjpa.persistence.test.AbstractPersistenceTestCase;

public class TestBeforeCommit extends AbstractPersistenceTestCase implements TransactionListener {

    AnEntity ae = null;
    public final int PKID = 2;
    private DBDictionary dict = null;

    private static OpenJPAEntityManagerFactorySPI emf = null;

    @Override
    public void setUp() throws Exception {
        if (emf == null) {
            emf = createEMF(AnEntity.class);
        }
        dict = ((JDBCConfiguration) emf.getConfiguration()).getDBDictionaryInstance();
        EntityManager em = emf.createEntityManager();
        EntityTransaction tran = em.getTransaction();

        tran.begin();
        em.createQuery("Delete from AnEntity").executeUpdate();
        tran.commit();

        tran.begin();
        ae = new AnEntity();
        ae.setId(PKID);
        ae.setName("");
        em.persist(ae);
        tran.commit();
        em.close();
    }

    @Override
    public void tearDown() throws Exception {
        closeEMF(emf);
        emf = null;
        super.tearDown();
    }
    
    public void testQuery() {
        OpenJPAEntityManagerSPI em = (OpenJPAEntityManagerSPI) emf.createEntityManager();
        em.addTransactionListener(this);
        EntityTransaction tran = em.getTransaction();

        tran.begin();
        ae = doQuery(em);
        if (dict instanceof OracleDictionary) {
            assertNull(ae.getName());
        }
        else if (dict instanceof SybaseDictionary) {
            // Sybase converts empty strings to " " 
            assertEquals(" ", ae.getName()); 
        }
        else {
            assertEquals("", ae.getName());
        }
        assertEquals(1, ae.getVersion());
        tran.commit();

        ae = doQuery(em);
        assertEquals("Ava", ae.getName());
        assertEquals(2, ae.getVersion());

        em.clear();
        ae = em.find(AnEntity.class, PKID);
        assertEquals("Ava", ae.getName());
        assertEquals(2, ae.getVersion());

        tran.begin();
        tran.commit();
        em.clear(); 
        ae = em.find(AnEntity.class, PKID);
        assertEquals("AvaAva", ae.getName());
        assertEquals(3, ae.getVersion());

        em.close();
    }

    public void testEmptyTransaction() {
        OpenJPAEntityManagerSPI em = (OpenJPAEntityManagerSPI) emf.createEntityManager();
        em.addTransactionListener(this);
        EntityTransaction tran = em.getTransaction();
        ae = doQuery(em);
        if (dict instanceof OracleDictionary) {
            assertNull(ae.getName());
        } else if (dict instanceof SybaseDictionary) {
            // Sybase converts "" to " "
            assertEquals(" ", ae.getName());
        } else {
            assertEquals("", ae.getName());
        }
        assertEquals(1, ae.getVersion());
        em.clear();

        tran.begin();
        tran.commit(); 
        
        // when BeforeCommit was fired AE was not managed. As a result its state is out of sync with the database.
        assertEquals("Ava", ae.getName());
        ae = doQuery(em);
        if (dict instanceof OracleDictionary) {
            assertNull(ae.getName());
        } else if (dict instanceof SybaseDictionary) {
            assertEquals(" ", ae.getName());
        } else {
            assertEquals("", ae.getName());
        }
        assertEquals(1, ae.getVersion());
        em.close();
    }

    public void beforeCommit(TransactionEvent event) {
        if(StringUtils.isBlank(ae.getName())) { 
            ae.setName("Ava");
        }
        else {
            ae.setName(ae.getName() + "Ava");
        }
    }

    private AnEntity doQuery(EntityManager em) {
        Query q = em.createQuery("select a from AnEntity a where a.id = :id");
        return (AnEntity) q.setParameter("id", PKID).getSingleResult();
    }

    // Unused Interface methods
    public void afterBegin(TransactionEvent event) {
    }

    public void afterFlush(TransactionEvent event) {
    }

    public void beforeFlush(TransactionEvent event) {
    }

    public void afterCommit(TransactionEvent event) {
    }

    public void afterCommitComplete(TransactionEvent event) {
    }

    public void afterRollback(TransactionEvent event) {
    }

    public void afterRollbackComplete(TransactionEvent event) {
    }

    public void afterStateTransitions(TransactionEvent event) {
    }
}
