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
package org.apache.openjpa.persistence.detachment;

import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.persistence.OpenJPAEntityManagerSPI;
import org.apache.openjpa.persistence.detachment.model.NoDetachedStateEntityFieldAccess;
import org.apache.openjpa.persistence.detachment.model.NoDetachedStateEntityPropertyAccess;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

public class TestDetachNoStateField extends SingleEMFTestCase {

    @Override
    protected void setUp(Object... props) {
        super.setUp(DROP_TABLES, "openjpa.DetachState", "loaded(DetachedStateField=false)",
            NoDetachedStateEntityPropertyAccess.class, NoDetachedStateEntityFieldAccess.class);
        loadDB();
    }

    /**
     * This testcase was added for OPENJPA-1400.
     */
    public void testIsDetchedNoStateManagerZeroVersionField() {
        OpenJPAEntityManagerSPI em = emf.createEntityManager();
        NoDetachedStateEntityPropertyAccess property = em.find(NoDetachedStateEntityPropertyAccess.class, 1);
        NoDetachedStateEntityFieldAccess field = em.find(NoDetachedStateEntityFieldAccess.class, 1);
        em.close();

        PersistenceCapable pcProperty = (PersistenceCapable) property;
        PersistenceCapable pcField = (PersistenceCapable) field;

        assertTrue(pcProperty.pcIsDetached());
        assertTrue(pcField.pcIsDetached());
    }

    /**
     * This testcase was added for OPENJPA-1400.
     */
    public void testPersistRelationshipToDetchedEntityZeroVersion() {
        OpenJPAEntityManagerSPI em = emf.createEntityManager();
        NoDetachedStateEntityPropertyAccess property = em.find(NoDetachedStateEntityPropertyAccess.class, 1);
        em.close();

        // Make sure we don't hit any exceptions when persisting a relationship to a detached
        // entity.
        em = emf.createEntityManager();
        em.getTransaction().begin();
        NoDetachedStateEntityFieldAccess field = em.find(NoDetachedStateEntityFieldAccess.class, 1);
        field.setRelationship(property);
        em.getTransaction().commit();
        em.close();

        // Make sure that the relationship was persisted
        em = emf.createEntityManager();
        field = em.find(NoDetachedStateEntityFieldAccess.class, 1);
        property = field.getRelationship();
        assertNotNull(property);
        assertEquals(1, property.getId());

    }

    /**
     * This testcase was added for OPENJPA-1482.
     */
    public void testSetVersionPropertyAccess() {
        NoDetachedStateEntityPropertyAccess entity = new NoDetachedStateEntityPropertyAccess();
        entity.setVersion(1);
    }
    
    void loadDB() {
        OpenJPAEntityManagerSPI em = emf.createEntityManager();
        em.getTransaction().begin();
        em.createNativeQuery("INSERT INTO PropertyAccessNoDetachedState (ID,VERSION) VALUES (1,0)")
            .executeUpdate();
        em.createNativeQuery("INSERT INTO FieldAccessNoDetachedState (ID,VERSION) VALUES (1,0)")
            .executeUpdate();
        em.getTransaction().commit();
        em.close();
    }
}
