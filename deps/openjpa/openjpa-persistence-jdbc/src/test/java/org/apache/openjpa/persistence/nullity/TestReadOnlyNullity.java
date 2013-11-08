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
package org.apache.openjpa.persistence.nullity;

import javax.persistence.EntityManager;

import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.sql.AbstractDB2Dictionary;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;

/**
 * Testcase that verifies that null checking is omitted for "read only" fields.
 * An example use case is a Date field that is set by the database (although in
 * that case the field would be updateable=false and insertable=false).  
 */
public class TestReadOnlyNullity extends AbstractNullityTestCase {

    private boolean skip = false;

    public void setUp() {
        OpenJPAEntityManagerFactorySPI emf = createEMF(new Object[] {});
        if (((JDBCConfiguration) emf.getConfiguration())
            .getDBDictionaryInstance() instanceof AbstractDB2Dictionary) {

            setUp(CLEAR_TABLES, TimestampedEntity.class);
        } else {
            skip = true;
        }
        closeEMF(emf);
    }

    /**
     * Test that a non-insertable field may be set to null. This test is skipped
     * for non-db2 databases. 
     */
    public void testNonInsertableBlobDoesNotFail() {
        if (!skip) {
            TimestampedEntity pc = new TimestampedEntity();
            pc.setNonInsertableNonNullableDate(null);
            assertCommitSucceeds(pc, NEW);
        }
        // else no-op
    }

    /**
     * Test that a non-updatable field may be set to null. This test is skipped
     * for non-db2 databases. 
     */
    public void testNonUpdatableBlobDoesNotFail() {
        if (!skip) {
            TimestampedEntity pc = new TimestampedEntity();
            EntityManager em = emf.createEntityManager();
            em.getTransaction().begin();
            em.persist(pc);
            em.getTransaction().commit();
            em.clear();

            pc.setNonUpdatableNonNullableDate(null);
            assertCommitSucceeds(pc, !NEW);
        }
        // else no-op
    }
}
