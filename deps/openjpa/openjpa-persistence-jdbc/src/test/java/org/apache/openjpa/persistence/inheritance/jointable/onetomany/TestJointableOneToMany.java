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
package org.apache.openjpa.persistence.inheritance.jointable.onetomany;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Test an inheritance type of 'joinable' where a OneToMany relationship is used
 * to get a parent class.
 */
public class TestJointableOneToMany extends SingleEMFTestCase {

    public void setUp() {
        setUp(UMLType.class, UMLPrimitiveType.class, UMLClass.class,
            UMLPackage.class, UMLNamed.class, CLEAR_TABLES);
        initialize();
    }

    public void initialize() {
        EntityManager em = emf.createEntityManager();
        try {
            UMLPackage aPackage =
                em.find(UMLPackage.class, "org.apache.openjpa");
            if (null == aPackage) {
                EntityTransaction tx = em.getTransaction();
                tx.begin();

                // Create a UMLPackage
                aPackage = new UMLPackage();
                aPackage.setId("org.apache.openjpa");
                aPackage.setName("org.apache.openjpa");
                aPackage.setOwnedType(new ArrayList<UMLType>());
                em.persist(aPackage);

                // Create a UMLClass and add the UMLPackage to it.
                UMLClass aClass = new UMLClass();
                aClass.setId("org.apache.openjpa.ATestClass");
                aClass.setName("TesClass");
                aClass.setOwnerPackage(aPackage);
                em.persist(aClass);

                // Add UMLClass to UMLPackage
                aPackage.getOwnedType().add(aClass);
                em.merge(aPackage);
                // TODO: temp
                // em.persist(aPackage);

                // Create a UMLPrimativeType and add UMLPackage to it.
                UMLPrimitiveType primitiveType = new UMLPrimitiveType();
                primitiveType.setId("String");
                primitiveType.setName("String");
                primitiveType.setOwnerPackage(aPackage);
                em.persist(primitiveType);

                // Add UMLPrimativeType to UMLPackage
                aPackage.getOwnedType().add(primitiveType);
                em.merge(aPackage);
                // TODO: temp
                // em.persist(aPackage);

                tx.commit();
            }
        } finally {
            em.close();
        }
    }

    public void test() {
        EntityManager em = emf.createEntityManager();
        try {
            // Verify the Class exists, and that from it we can get the Package.
            UMLClass aClass =
                em.find(UMLClass.class, "org.apache.openjpa.ATestClass");
            assertNotNull(aClass);
            assertEquals("org.apache.openjpa", aClass.getOwnerPackage()
                .getName());

            // Verify the PrimitiveType exists, and that from it we can get the
            // Package.
            UMLPrimitiveType aPrimitiveType =
                em.find(UMLPrimitiveType.class, "String");
            assertNotNull(aPrimitiveType);
            assertEquals("org.apache.openjpa", aPrimitiveType.getOwnerPackage()
                .getName());

            // Verify the Package exists.
            UMLPackage aPackage =
                em.find(UMLPackage.class, "org.apache.openjpa");
            assertNotNull(aPackage);

            // From the Package, lets get the Type.....there should be two
            // Types (i.e. a UMLClass and UMLPrimativeTYpe), but 0 is returned!
            List<UMLType> ownedType = aPackage.getOwnedType();
            assertNotNull(ownedType);
            assertEquals(2, ownedType.size());

        } finally {
            em.close();

        }
    }
}
