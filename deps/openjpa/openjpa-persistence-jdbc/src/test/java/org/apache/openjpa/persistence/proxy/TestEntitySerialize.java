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
package org.apache.openjpa.persistence.proxy;

import java.util.UUID;

import javax.persistence.EntityManager;

import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.apache.openjpa.persistence.OpenJPAPersistence;
import org.apache.openjpa.persistence.proxy.entities.Address;
import org.apache.openjpa.persistence.proxy.entities.Annuity;
import org.apache.openjpa.persistence.proxy.entities.AnnuityHolder;
import org.apache.openjpa.persistence.proxy.entities.AnnuityPersistebleObject;
import org.apache.openjpa.persistence.proxy.entities.Contact;
import org.apache.openjpa.persistence.proxy.entities.EquityAnnuity;
import org.apache.openjpa.persistence.proxy.entities.FixedAnnuity;
import org.apache.openjpa.persistence.proxy.entities.Payor;
import org.apache.openjpa.persistence.proxy.entities.Payout;
import org.apache.openjpa.persistence.proxy.entities.Person;
import org.apache.openjpa.persistence.proxy.entities.Rider;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Test for showing OPENJPA-1900
 */
public class TestEntitySerialize extends SingleEMFTestCase {

    public void setUp() {
        setUp(DROP_TABLES, Address.class, Annuity.class, AnnuityHolder.class, AnnuityPersistebleObject.class,
            Contact.class, EquityAnnuity.class, FixedAnnuity.class, Payor.class, Payout.class, Person.class,
            Rider.class);
    }

    public void testSerialization() throws Exception {
        OpenJPAEntityManagerFactorySPI emf =
            (OpenJPAEntityManagerFactorySPI) OpenJPAPersistence.createEntityManagerFactory("Annuity1Compat",
                "org/apache/openjpa/persistence/proxy/persistence1.xml");
        assertNotNull(emf);

        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Annuity ann = createAnnuity(em);

            // Make sure that we can detach an Entity via serialization that is currently associated
            // with a persistence context
            assertNotNull(roundtrip(ann));
            em.getTransaction().commit();
        } finally {
            closeEM(em);
        }
    }

    private Annuity createAnnuity(EntityManager em) {
        FixedAnnuity fixedAnn = new FixedAnnuity();
        ((FixedAnnuity) fixedAnn).setRate(10.0);
        fixedAnn.setId(getId());
        fixedAnn.setAmount(500.00);
        fixedAnn.setAccountNumber("123456");
        em.persist(fixedAnn);

        EquityAnnuity equityAnn = new EquityAnnuity();
        equityAnn.setId(getId());
        equityAnn.setAmount(500.00);
        equityAnn.setAccountNumber("123456");
        equityAnn.setFundNames("Something nothing wrong");
        equityAnn.setIndexRate(10.99);
        equityAnn.setLastPaidAmt(100.00);
        equityAnn.setPreviousAnnuity(fixedAnn);
        em.persist(equityAnn);

        return equityAnn;
    }

    private String getId() {
        UUID uid = UUID.randomUUID();
        return uid.toString();
    }
}
