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

import java.util.List;

import javax.persistence.EntityManager;

import org.apache.openjpa.persistence.jdbc.query.domain.IndoorGame;
import org.apache.openjpa.persistence.jdbc.query.domain.Scrabble;
import org.apache.openjpa.persistence.jdbc.query.domain.Chess;
import org.apache.openjpa.persistence.jdbc.query.domain.Game;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Tests GROUP BY in sub query does not get parsed by owning query.
 * 
 * Further details can be found in <A
 * HREF="https://issues.apache.org/jira/browse/OPENJPA-28">OPENJPA-28</A>
 * 
 * @author Pinaki Poddar
 * 
 */
public class TestGroupByQuery extends SingleEMFTestCase {
	public void setUp() {
        super.setUp(DROP_TABLES, Game.class, IndoorGame.class, Scrabble.class,
				Chess.class);
		try {
			createData();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	void createData() throws Exception {
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
        Class[] classes = { Game.class, IndoorGame.class, Scrabble.class,
				Chess.class };
		for (Class cls : classes) {
			for (int i = 1; i <= 4; i++) {
				Game p = (Game) cls.newInstance();
				p.setName(cls.getSimpleName() + "-" + i);
				em.persist(p);
			}
		}
		em.getTransaction().commit();
	}

	public void testGroupBy() {
		String jpql = "SELECT g.name, g.nTile FROM Scrabble g WHERE "
                + "(g.name = ANY(SELECT g1.name FROM Scrabble g1 "
				+ "GROUP BY g1.name )) ORDER BY g.name";
		EntityManager em = emf.createEntityManager();

        List<IndoorGame> employees = em.createQuery(jpql).getResultList();

	}
}
