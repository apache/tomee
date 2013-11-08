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
package org.apache.openjpa.kernel;

import java.util.Collections;
import javax.persistence.EntityManager;

import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.enhance.UnenhancedFieldAccess;
import org.apache.openjpa.enhance.ManagedClassSubclasser;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.persistence.JPAFacadeHelper;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.apache.openjpa.persistence.query.SimpleEntity;
import org.apache.openjpa.persistence.test.AbstractCachedEMFTestCase;
import org.apache.openjpa.persistence.test.AllowFailure;

@AllowFailure(message="excluded")
public class TestDynamicClassRegistration
    extends AbstractCachedEMFTestCase {

    private OpenJPAEntityManagerFactorySPI emf1;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        emf1 = createNamedEMF("empty-pu");
    }

    public void tearDown() throws Exception {
        super.tearDown();
        clear(emf1);
        closeEMF(emf1);
    }

    public void testEnhancedDynamicClassRegistration()
        throws ClassNotFoundException {
        assertTrue(
            PersistenceCapable.class.isAssignableFrom(SimpleEntity.class));

        // trigger class initialization. We could just do 'new SimpleEntity()'.
        Class.forName(SimpleEntity.class.getName(), true,
            getClass().getClassLoader());

        ClassMetaData meta =
            JPAFacadeHelper.getMetaData(emf1, SimpleEntity.class);
        assertNotNull(meta);
        EntityManager em = emf1.createEntityManager();
        javax.persistence.Query q = em.createQuery("select o from simple o");
        em.close();
    }

    public void testUnenhancedDynamicClassRegistration() {
        assertFalse(PersistenceCapable.class.isAssignableFrom(
            UnenhancedFieldAccess.class));

        // trigger class initialization
        ManagedClassSubclasser.prepareUnenhancedClasses(
            emf1.getConfiguration(),
            Collections.singleton(UnenhancedFieldAccess.class),
            null);

        ClassMetaData meta =
            JPAFacadeHelper.getMetaData(emf1, UnenhancedFieldAccess.class);
        assertNotNull(meta);
        EntityManager em = emf1.createEntityManager();
        javax.persistence.Query q = em.createQuery(
            "select o from UnenhancedFieldAccess o");
        em.close();
    }
}
