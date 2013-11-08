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
package org.apache.openjpa.persistence.derivedid;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.openjpa.persistence.test.SQLListenerTestCase;

public class TestDerivedIdentity  extends SQLListenerTestCase {

    public void setUp() {
        setUp(EBigDecimalID.class, EDBigDecimalID.class,
            EBigIntegerID.class, EDBigIntegerID.class,
            EDateID.class, EDDateID.class,
            ESQLDateID.class, EDSQLDateID.class,
            CLEAR_TABLES);
        populate();
    }

    public void populate() {
        EntityManager em = emf.createEntityManager();
        
        for (int i = 0; i < 2; i++) {
            long time = (long) (System.currentTimeMillis() / 1000)+i*1317;
            BigDecimal did = new BigDecimal(time);            
            EBigDecimalID e1 = new EBigDecimalID(did);
            EDBigDecimalID e2 = new EDBigDecimalID(e1);
            em.persist(e1);
            em.persist(e2);

            int time2 = (int) (System.currentTimeMillis() / 1000)+i*7131;
            BigInteger iid = new BigInteger(Integer.toString(time2));
            EBigIntegerID e3 = new EBigIntegerID(iid);
            EDBigIntegerID e4 = new EDBigIntegerID(e3);
            em.persist(e3);
            em.persist(e4);

            Date id = new Date(time);
            EDateID e5 = new EDateID(id);
            EDDateID e6 = new EDDateID(e5);
            em.persist(e5);
            em.persist(e6);
            
            if (i == 0) {
                java.sql.Date sid = new java.sql.Date(time);
                ESQLDateID e7 = new ESQLDateID(sid);
                EDSQLDateID e8 = new EDSQLDateID(e7);
                em.persist(e7);
                em.persist(e8);
            }
        }
        
        em.getTransaction().begin();
        em.getTransaction().commit();
        em.close();
    }

    public void testDerivedIdentity() {
        EntityManager em = emf.createEntityManager();
        Query query = null;
        String str[] = {
            "select e from EDDateID e",
            "select e from EDBigDecimalID e",
            "select e from EDBigIntegerID e",
            "select e from EDSQLDateID e",
            "select e from EDDateID e join fetch e.rid",
            "select e from EDBigDecimalID e join fetch e.rid",
            "select e from EDBigIntegerID e join fetch e.rid",
            "select e from EDSQLDateID e join fetch e.rid",
        };
        for (int i = 0; i < str.length; i++) {
            query = em.createQuery(str[i]);
            List rs = query.getResultList();
            assertTrue(rs.size() > 0);
            for (int j = 0; j < rs.size(); j++) {
                Object e = rs.get(j);
                String name = null;
                Object oid = null;
                if (e instanceof EDDateID) {
                    name = ((EDDateID)e).getName();
                    oid = ((EDDateID)e).getRid().getId();
                } else if (e instanceof EDBigDecimalID) {
                    name = ((EDBigDecimalID)e).getName();
                    oid = ((EDBigDecimalID)e).getRid().getId();
                } else if (e instanceof EDBigIntegerID) {
                    name = ((EDBigIntegerID)e).getName();
                    oid = ((EDBigIntegerID)e).getRid().getId();
                } else if (e instanceof EDSQLDateID) {
                    name = ((EDSQLDateID)e).getName();
                    oid = ((EDSQLDateID)e).getRid().getId();
                }
                //System.out.println(name);
                //System.out.println(oid.toString());
                assertTrue(name.startsWith("Rel"));
            }
        }
        
        em.close();
    }
}

