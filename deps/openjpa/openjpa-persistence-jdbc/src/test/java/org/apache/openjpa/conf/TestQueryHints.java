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
package org.apache.openjpa.conf;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.openjpa.jdbc.sql.MariaDBDictionary;
import org.apache.openjpa.jdbc.sql.MySQLDictionary;
import org.apache.openjpa.jdbc.sql.OracleDictionary;
import org.apache.openjpa.kernel.QueryHints;
import org.apache.openjpa.persistence.OpenJPAPersistence;
import org.apache.openjpa.persistence.OpenJPAQuery;
import org.apache.openjpa.persistence.jdbc.IsolationLevel;
import org.apache.openjpa.persistence.jdbc.JDBCFetchPlan;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Tests JPA 2.0 API methods {@link Query#getSupportedHints()} and 
 * {@link Query#getHints()}. 
 * 
 * @author Pinaki Poddar
 *
 */
public class TestQueryHints extends SingleEMFTestCase {
    EntityManager em;
    OpenJPAQuery<?> query;
    
    public void setUp() {
       super.setUp((Object[])null);
       em = emf.createEntityManager();
       String sql = "select * from Person";
       query = OpenJPAPersistence.cast(em.createNativeQuery(sql));
    }
    
    public void testSupportedHintsContainProductDerivationHints() {
        assertSupportedHint(OracleDictionary.SELECT_HINT, true);
        assertSupportedHint(MySQLDictionary.SELECT_HINT, true);
        assertSupportedHint(MariaDBDictionary.SELECT_HINT, true);
    }
    
    public void testSupportedHintsContainFetchPlanHints() {
        assertSupportedHint("openjpa.FetchPlan.LockTimeout", true);
    }

    public void testSupportedHintsIgnoresSomeFetchPlanBeanStyleProperty() {
        assertSupportedHint("openjpa.FetchPlan.QueryResultCache", false);
    }
    
    public void testSupportedHintsContainQueryProperty() {
        assertSupportedHint("openjpa.Subclasses", true);
    }
    
    public void testSupportedHintsContainKernelQueryHints() {
        assertSupportedHint(QueryHints.HINT_IGNORE_PREPARED_QUERY, true);
    }
    
    public void testSupportedHintsContainJPAQueryHints() {
        assertSupportedHint("javax.persistence.query.timeout", true);
    }
    
    public void testUnrecognizedKeyIsIgnored() {
        String unrecognizedKey = "acme.org.hint.SomeThingUnknown";
        query.setHint(unrecognizedKey, "xyz");
        assertFalse(query.getHints().containsKey(unrecognizedKey));
        assertNull(query.getFetchPlan().getDelegate().getHint(unrecognizedKey));
     }
    
    public void testRecognizedKeyIsNotRecordedButAvailable() {
        String recognizedKey = "openjpa.some.derivation.hint";
        query.setHint(recognizedKey, "abc");
        assertFalse(query.getHints().containsKey(recognizedKey));
        assertEquals("abc", query.getFetchPlan().getDelegate().getHint(
                recognizedKey));
    }

    public void testSupportedKeyIsRecordedAndAvailable() {
        String supportedKey = "openjpa.FetchPlan.FetchBatchSize";
        query.setHint(supportedKey, 42);
        assertTrue(query.getHints().containsKey(supportedKey));
        assertEquals(42, query.getFetchPlan().getFetchBatchSize());
    }
    
    public void testSupportedKeyWrongValue() {
        String supportedKey = "openjpa.FetchPlan.FetchBatchSize";
        short goodValue = (short)42;
        float badValue = 57.9f;
        query.setHint(supportedKey, goodValue);
        assertTrue(query.getHints().containsKey(supportedKey));
        assertEquals(goodValue, query.getFetchPlan().getFetchBatchSize());
        
        try {
            query.setHint(supportedKey, badValue);
            fail("Expected to fail to set " + supportedKey + " hint to "
                    + badValue);
        } catch (IllegalArgumentException e) {
            // good
        }
    }
    
    public void testSupportedKeyIntegerValueConversion() {
        String supportedKey = "openjpa.hint.OptimizeResultCount";
        String goodValue = "57";
        int badValue = -3;
        query.setHint(supportedKey, goodValue);
        assertTrue(query.getHints().containsKey(supportedKey));
        assertEquals(57, query.getFetchPlan().getDelegate().getHint(
                supportedKey));
        
        try {
            query.setHint(supportedKey, badValue);
            fail("Expected to fail to set " + supportedKey + " hint to "
                    + badValue);
        } catch (IllegalArgumentException e) {
            // good
        }
    }

    public void testSupportedKeyBooleanValueConversion() {
        String supportedKey = QueryHints.HINT_IGNORE_PREPARED_QUERY;
        String goodValue = "true";
        query.setHint(supportedKey, goodValue);
        assertTrue(query.getHints().containsKey(supportedKey));
        assertEquals(true, query.getFetchPlan().getDelegate().getHint(
                supportedKey));
        
        goodValue = "false";
        query.setHint(supportedKey, goodValue);
        assertTrue(query.getHints().containsKey(supportedKey));
        assertEquals(false, query.getFetchPlan().getDelegate().getHint(
                supportedKey));
    }
    
    public void testJPAHintSetsFetchPlan() {
        query.setHint("javax.persistence.lock.timeout", 5671);
        query.setHint("javax.persistence.query.timeout", 7500);
        assertEquals(5671, query.getFetchPlan().getLockTimeout());
        assertEquals(7500, query.getFetchPlan().getQueryTimeout());
    }

    public void testInvalidLockTimeoutHint() {
        try {
            query.setHint("javax.persistence.lock.timeout", -5671);
            fail("Expected setHint to fail with an IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }
    
    public void testInvalidQueryTimeoutHint() {
        try {
            query.setHint("javax.persistence.query.timeout", -7500);
            fail("Expected setHint to fail with an IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }
    
    /**
     * Verifies a valid fetchplan isolation level hint can be set and retrieved.
     */
    public void testFetchPlanIsolation() {
        query.setHint("openjpa.FetchPlan.Isolation", "SERIALIZABLE");
        assertTrue(query.getHints().containsKey("openjpa.FetchPlan.Isolation"));
        assertEquals(IsolationLevel.SERIALIZABLE, ((JDBCFetchPlan)query.getFetchPlan()).getIsolation());
    }

    /**
     * Verifies an invalid fetchplan isolation level hint is ignored.
     */
    public void testInvalidFetchPlanIsolation() {
        query.setHint("openjpa.FetchPlan.TransactionIsolation", "SERIALIZABLE");
        assertFalse(query.getHints().containsKey("openjpa.FetchPlan.TransactionIsolation"));
        assertNotEquals(IsolationLevel.SERIALIZABLE, ((JDBCFetchPlan)query.getFetchPlan()).getIsolation());        
    }

    void assertSupportedHint(String hint, boolean contains) {
        if (contains)
            assertTrue("Expected supported hint [" + hint + "]",
                query.getSupportedHints().contains(hint));
        else
            assertFalse("Unexpected supported hint [" + hint + "]",
                query.getSupportedHints().contains(hint));
    }
    
    
}
