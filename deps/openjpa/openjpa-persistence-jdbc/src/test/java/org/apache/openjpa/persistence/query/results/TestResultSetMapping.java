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

package org.apache.openjpa.persistence.query.results;

import java.math.BigDecimal;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import org.apache.openjpa.persistence.criteria.results.ShipRate;
import org.apache.openjpa.persistence.test.SQLListenerTestCase;

public class TestResultSetMapping extends SQLListenerTestCase{
    
    public void setUp() {
        setUp(CLEAR_TABLES, ShipRate.class);        
    }

    public void testQuery () {
        EntityManager em = emf.createEntityManager();         
        try 
        {
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            ShipRate rate = new ShipRate(1000, new BigDecimal(20.5));
            em.persist(rate);
            tx.commit();
            em.close();
            
            em = emf.createEntityManager(); 
            //Query query = em.createNativeQuery(sqlStatement, "ShipRateMapping");
            Query query = em.createNamedQuery("selectShipRateQuery");
            
            List<ShipRate> results = (List<ShipRate>)query.getResultList();    
            assertEquals(1, results.size());
        } 
        catch(RuntimeException x)
        {
            x.printStackTrace();
            throw x;
        }
        finally 
        {
            em.close();
        }
    }
}
