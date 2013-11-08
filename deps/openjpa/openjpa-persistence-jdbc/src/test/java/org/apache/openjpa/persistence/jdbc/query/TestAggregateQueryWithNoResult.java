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
package org.apache.openjpa.persistence.jdbc.query;

import javax.persistence.EntityManager;

import org.apache.openjpa.persistence.jdbc.query.domain.Chess;
import org.apache.openjpa.persistence.jdbc.query.domain.Game;
import org.apache.openjpa.persistence.jdbc.query.domain.IndoorGame;
import org.apache.openjpa.persistence.jdbc.query.domain.Scrabble;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Tests Aggregate Query that has no record.
 * 
 * SUM() and COUNT() always return Long.
 * AVG(), MAX(), MIN() preserves the type of aggregated field.
 * 
 * @author Pinaki Poddar
 * 
 */
public class TestAggregateQueryWithNoResult extends SingleEMFTestCase {
    EntityManager em;
	public void setUp() {
        super.setUp(CLEAR_TABLES,
                "openjpa.Compatibility", "ReturnNullOnAggregateResult=false",  //OPENJPA-1794
                Game.class, IndoorGame.class, Scrabble.class,
				Chess.class);
        em = emf.createEntityManager();
        assertTrue(em.createQuery("select p from Scrabble p").getResultList().isEmpty());
	}


    public void testSumWithNoResult() {
        String jpql = "SELECT SUM(g.nTile) FROM Scrabble g";

        Long result = (Long)em.createQuery(jpql).getSingleResult();
        
        assertNotNull(result);
        assertEquals(result, new Long(0));
    }
    
    public void testAvgWithNoResult() {
        String jpql = "SELECT AVG(g.nTile) FROM Scrabble g";

        Integer result = (Integer)em.createQuery(jpql).getSingleResult();
        
        assertNotNull(result);
        assertEquals(result, new Integer(0));
    }
    
    public void testCountWithNoResult() {
        String jpql = "SELECT COUNT(g.nTile) FROM Scrabble g";

        Long result = (Long)em.createQuery(jpql).getSingleResult();
        
        assertNotNull(result);
        assertEquals(result, new Long(0));
    }
    
    public void testMaxWithNoResult() {
        String jpql = "SELECT MAX(g.nTile) FROM Scrabble g";

        Integer result = (Integer)em.createQuery(jpql).getSingleResult();
       
        assertNotNull(result);
        assertEquals(result, new Integer(0));
    }
    
    public void testMinWithNoResult() {
        String jpql = "SELECT MIN(g.nTile) FROM Scrabble g";

        Integer result = (Integer)em.createQuery(jpql).getSingleResult();
        
        assertNotNull(result);
        assertEquals(result, new Integer(0));
    }
}
