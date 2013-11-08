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
package org.apache.openjpa.enhance;

import org.apache.openjpa.persistence.test.SingleEMTestCase;

public class TestUnenhancedCompoundPK
    extends SingleEMTestCase {

    public void setUp() {
        setUp(UnenhancedCompoundPKFieldAccess.class,
            UnenhancedCompoundPKPropertyAccess.class, CLEAR_TABLES);
    }

    public void testCompoundPKFieldAccessUserDefined() {
        UnenhancedCompoundPKFieldAccess un
            = new UnenhancedCompoundPKFieldAccess(17, 31);
        UnenhancedCompoundPKFieldAccess.PK oid
            = new UnenhancedCompoundPKFieldAccess.PK(17, 31);
        compoundPKHelper(un, oid, true);
    }

    public void testCompoundPKFieldAccessOpenJPADefined() {
        UnenhancedCompoundPKFieldAccess un
            = new UnenhancedCompoundPKFieldAccess(17, 31);
        UnenhancedCompoundPKFieldAccess.PK oid
            = new UnenhancedCompoundPKFieldAccess.PK(17, 31);
        compoundPKHelper(un, oid, false);
    }

    public void testCompoundPKPropertyAccessUserDefined() {
        UnenhancedCompoundPKPropertyAccess un
            = new UnenhancedCompoundPKPropertyAccess(17, 31);
        UnenhancedCompoundPKPropertyAccess.PK oid
            = new UnenhancedCompoundPKPropertyAccess.PK(17, 31);
        compoundPKHelper(un, oid, true);
    }

    public void testCompoundPKPropertyAccessOpenJPADefined() {
        UnenhancedCompoundPKPropertyAccess un
            = new UnenhancedCompoundPKPropertyAccess(17, 31);
        UnenhancedCompoundPKPropertyAccess.PK oid
            = new UnenhancedCompoundPKPropertyAccess.PK(17, 31);
        compoundPKHelper(un, oid, false);
    }

    private void compoundPKHelper(Object o, Object oid, boolean userDefined) {
        em.getTransaction().begin();
        em.persist(o);
        em.getTransaction().commit();

        if (!userDefined) {
            em.close();
            em = emf.createEntityManager();
        }

        em.find(o.getClass(), oid);
    }
}
