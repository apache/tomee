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

package org.apache.openjpa.jdbc.meta.strats;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.datacache.DataCachePCData;
import org.apache.openjpa.jdbc.sql.DB2Dictionary;
import org.apache.openjpa.jdbc.sql.MySQLDictionary;
import org.apache.openjpa.jdbc.sql.OracleDictionary;
import org.apache.openjpa.jdbc.sql.SQLServerDictionary;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.persistence.JPAFacadeHelper;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * This abstract class defines all the tests for LOB streaming.
 *
 * @author Ignacio Andreu
 * @since 1.1.0
 */

public abstract class AbstractLobTest extends SingleEMFTestCase {

    protected static boolean firstTestExecuted;

    protected List<Class<?>> supportedDatabases =
        new ArrayList<Class<?>>
            (Arrays.asList(MySQLDictionary.class, OracleDictionary.class, SQLServerDictionary.class,
                    DB2Dictionary.class));
        
    public void setUp() throws Exception {
        setSupportedDatabases(supportedDatabases.toArray(new Class<?>[] {}));
        if (isTestsDisabled()) {
            return;
        }

        // Test CREATE TABLE but only once to save time.
        Object clearOrDropTables = (firstTestExecuted) ? CLEAR_TABLES : DROP_TABLES;
        firstTestExecuted = true;
        super.setUp(getLobEntityClass(), clearOrDropTables,
            "openjpa.DataCache", "true",
            "openjpa.RemoteCommitProvider", "sjvm",
            "openjpa.ConnectionRetainMode", "transaction");
    }

    public void insert(LobEntity le) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(le);
        em.getTransaction().commit();
        em.close();
    }

    public void testInsert() {
        insert(newLobEntity(createLobData(), 1));
    }

    public void testInsertAndSelect() throws IOException {
        String s = createLobData();
        insert(newLobEntity(s, 1));
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        Query query = em.createQuery(getSelectQuery());
        LobEntity entity = (LobEntity) query.getSingleResult();
        assertNotNull(entity.getStream());
        assertEquals(s, getStreamContentAsString(entity.getStream()));
        em.getTransaction().commit();
        em.close();
    }

    public void testInsertNull() {
        insert(newLobEntity(null, 1));
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        LobEntity le = (LobEntity) em.find(getLobEntityClass(), 1);
        assertNull(le.getStream());
        em.getTransaction().commit();
        em.close();
    }

    public void testUpdate() throws IOException {
        insert(newLobEntity(createLobData(), 1));
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        LobEntity entity = (LobEntity) em.find(getLobEntityClass(), 1);
        String string = createLobData2();
        changeStream(entity, string);
        em.getTransaction().commit();
        em.close();
        em = emf.createEntityManager();
        em.getTransaction().begin();
        entity = (LobEntity) em.find(getLobEntityClass(), 1);
        assertEquals(string, getStreamContentAsString(entity.getStream()));
        em.getTransaction().commit();
        em.close();
    }

    public void testUpdateWithNull() {
        insert(newLobEntity(createLobData(), 1));
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        LobEntity entity = (LobEntity) em.find(getLobEntityClass(), 1);
        entity.setStream(null);
        em.getTransaction().commit();
        em.close();
        em = emf.createEntityManager();
        em.getTransaction().begin();
        entity = (LobEntity) em.find(getLobEntityClass(), 1);
        assertNull(entity.getStream());
        em.getTransaction().commit();
        em.close();
    }
    
    public void testUpdateANullObjectWithoutNull() throws IOException {
        insert(newLobEntity(null, 1));
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        LobEntity entity = (LobEntity) em.find(getLobEntityClass(), 1);
        String string = createLobData2();
        changeStream(entity, string);
        em.getTransaction().commit();
        em.close();
        em = emf.createEntityManager();
        em.getTransaction().begin();
        entity = (LobEntity) em.find(getLobEntityClass(), 1);
        assertEquals(string, getStreamContentAsString(entity.getStream()));
        em.getTransaction().commit();
        em.close();
    }
    
    public void testDelete() {
        insert(newLobEntity(createLobData(), 1));
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        LobEntity entity = (LobEntity) em.find(getLobEntityClass(), 1);
        em.remove(entity);
        em.getTransaction().commit();
        em.close();
        em = emf.createEntityManager();
        em.getTransaction().begin();
        Query q = em.createQuery(getSelectQuery());
        assertEquals(0, q.getResultList().size());
        em.getTransaction().commit();
        em.close();
    }
    
    public void testLifeCycleInsertFlushModify() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        LobEntity le = newLobEntity(createLobData(), 1);
        em.persist(le);
        em.flush();
        changeStream(le, createLobData2());
        em.getTransaction().commit();
        em.close();
    }

    public void testLifeCycleLoadFlushModifyFlush() {
        insert(newLobEntity(createLobData(), 1));
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        LobEntity entity = (LobEntity) em.find(getLobEntityClass(), 1);
        em.flush();
        changeStream(entity, createLobData2());
        em.flush();
        em.getTransaction().commit();
        em.close();
    }

    public void testReadingMultipleTimesWithASingleConnection()
        throws IOException {
        insert(newLobEntity(createLobData(), 1));
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        LobEntity le = (LobEntity) em.find(getLobEntityClass(), 1);
        String string = createLobData2();
        changeStream(le, string);
        em.getTransaction().commit();
        em.close();
        em = emf.createEntityManager();
        em.getTransaction().begin();
        le = (LobEntity) em.find(getLobEntityClass(), 1);
        assertNotNull(le.getStream());
        LobEntity entity = newLobEntity(createLobData(), 2);
        em.persist(entity);
        assertEquals(string, getStreamContentAsString(le.getStream()));
        em.getTransaction().commit();
        em.close();
    }

    public void testDataCache() {
        OpenJPAEntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        LobEntity le = newLobEntity(createLobData(), 1);
        em.persist(le);
        em.getTransaction().commit();
        OpenJPAConfiguration conf = emf.getConfiguration();
        Object o = em.getObjectId(le);
        ClassMetaData meta = JPAFacadeHelper.getMetaData(le);
        Object objectId = JPAFacadeHelper.toOpenJPAObjectId(meta, o);
        DataCachePCData pcd =
            conf.getDataCacheManagerInstance()
                .getSystemDataCache().get(objectId);
        assertFalse(pcd.isLoaded(meta.getField("stream").getIndex()));
        em.close();
    }

    public void testSetResetAndFlush() throws IOException {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        LobEntity le = newLobEntity(createLobData(), 1);
        em.persist(le);
        String string = createLobData2();
        changeStream(le, string);
        em.flush();
        em.getTransaction().commit();
        em.close();
        em = emf.createEntityManager();
        em.getTransaction().begin();
        LobEntity entity = (LobEntity) em.find(getLobEntityClass(), 1);
        assertEquals(string, getStreamContentAsString(entity.getStream()));
        em.getTransaction().commit();
        em.close();
    }

    public void testSetFlushAndReset() throws IOException {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        LobEntity le = newLobEntity(createLobData(), 1);
        em.persist(le);
        em.flush();
        String string = createLobData2();
        changeStream(le, string);
        LobEntity entity = (LobEntity) em.find(getLobEntityClass(), 1);
        assertEquals(string, getStreamContentAsString(entity.getStream()));
        em.getTransaction().commit();
        em.close();
    }

    protected String createLobData() {
        return StringUtils.repeat("ooOOOOoo, ", 3000);
    }

    protected String createLobData2() {
        return StringUtils.repeat("iiIIIIii, ", 1000);
    }

    protected abstract Class getLobEntityClass();

    protected abstract String getStreamContentAsString(Object o)
        throws IOException;

    protected abstract LobEntity newLobEntity(String s, int id);

    protected abstract LobEntity newLobEntityForLoadContent(String s, int id);

    protected abstract String getSelectQuery();

    protected abstract void changeStream(LobEntity le, String s);
}
