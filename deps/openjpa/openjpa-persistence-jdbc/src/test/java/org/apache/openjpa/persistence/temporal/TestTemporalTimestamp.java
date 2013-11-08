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
package org.apache.openjpa.persistence.temporal;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;

import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.test.SQLListenerTestCase;

public class TestTemporalTimestamp extends SQLListenerTestCase {
    private OpenJPAEntityManager em;
    
    
    public void setUp() {
        setSupportedDatabases(
            org.apache.openjpa.jdbc.sql.DerbyDictionary.class,
            org.apache.openjpa.jdbc.sql.DB2Dictionary.class);
        if (isTestsDisabled()) {
            return;
        }
        
        setUp(CLEAR_TABLES, TemporalEntity.class, "openjpa.jdbc.DBDictionary", "RoundTimeToMillisec=false");
        assertNotNull(emf);
        
        loadDB();
    }
    
    public void testNoRounding() {
        em = emf.createEntityManager();
        final List<TemporalEntity> temporalEntityList = findAll(em);
        assertNotNull(temporalEntityList);
        assertNotEquals(temporalEntityList.size(), 0);
        for (final TemporalEntity temporalEntity : temporalEntityList) {
            Date testDate = temporalEntity.getTestDate();
            assertEquals(testDate.getDay(), 5);
            assertEquals(testDate.getMonth(), 11);
            assertEquals(testDate.getDate(), 31);
            assertEquals(testDate.getHours(), 23);
            assertEquals(testDate.getMinutes(), 59);
            assertEquals(testDate.getSeconds(), 59);
            assertEquals(testDate.getYear(), 8099);
        }
        em.close();
    }
    
    public void testRounding() {
        JDBCConfiguration conf = (JDBCConfiguration) emf.getConfiguration();
        DBDictionary dict = conf.getDBDictionaryInstance();
        // set value back to default
        dict.roundTimeToMillisec = true;
        
        em = emf.createEntityManager();
        final List<TemporalEntity> temporalEntityList = findAll(em);
        assertNotNull(temporalEntityList);
        assertNotEquals(temporalEntityList.size(), 0);
        for (final TemporalEntity temporalEntity : temporalEntityList) {
            Date testDate = temporalEntity.getTestDate();
            assertEquals(testDate.getDay(), 6);
            assertEquals(testDate.getMonth(), 0);
            assertEquals(testDate.getDate(), 1);
            assertEquals(testDate.getHours(), 0);
            assertEquals(testDate.getMinutes(), 0);
            assertEquals(testDate.getSeconds(), 0);
            assertEquals(testDate.getYear(), 8100);
        }
        em.close();
    }
    
    public List<TemporalEntity> findAll(EntityManager em) {
        final CriteriaQuery<TemporalEntity> critQuery = em.getCriteriaBuilder().createQuery(TemporalEntity.class);
        critQuery.from(TemporalEntity.class);
        final TypedQuery<TemporalEntity> typedQuery = em.createQuery(critQuery);
        return typedQuery.getResultList();
    }

    private void loadDB() {
        em = emf.createEntityManager();
        em.getTransaction().begin();
        String sql = "INSERT INTO TemporalEntity (Id, testDate) VALUES (1, '9999-12-31 23:59:59.9999')";
        em.createNativeQuery(sql).executeUpdate();
        sql = "INSERT INTO TemporalEntity (Id, testDate) VALUES (2, '9999-12-31 23:59:59.9996')";
        em.createNativeQuery(sql).executeUpdate();
        em.getTransaction().commit();
        em.close();
    }

}
