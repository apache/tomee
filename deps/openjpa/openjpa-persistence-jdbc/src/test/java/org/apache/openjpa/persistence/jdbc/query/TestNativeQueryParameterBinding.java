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
import javax.persistence.Query;

import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Tests that Native queries use only 1-based positional parameters and 
 * disallows named parameters.
 * 
 * Originally reported in 
 * <A HRE="http://issues.apache.org/jira/browse/OPENJPA-112>OPENJPA-112</A>
 *  
 * @author Pinaki Poddar
 *
 */
public class TestNativeQueryParameterBinding extends SingleEMFTestCase {
	private static Class<? extends Exception> NO_ERROR = null;
	
	@Override
	public void setUp() throws Exception {
		super.setUp(CLEAR_TABLES);
	}
	
	public void testNamedParameterInNativeQueryIsNotValid() {
		String sql = "SELECT * FROM Application WHERE id=:id";
		verifyParams(sql, IllegalArgumentException.class, "id", 10);
	}
	
	public void testPositionalParameterInNativeQueryIsValid() {
		String sql = "SELECT * FROM Application WHERE id=?1";
		verifyParams(sql, NO_ERROR, 1, 10);
	}
	
	public void testZeroPositionalParameterInNativeQueryIsNotValid() {
		String sql = "SELECT * FROM Application WHERE id=?1";
		verifyParams(sql, IllegalArgumentException.class, 0, 10);
	}
	
	public void testNativeQueryDeclaredParameters() {
        String sql = "SELECT * FROM Application WHERE id=?1 AND name=?2";
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        Query query = em.createNativeQuery(sql);
        assertTrue(query.getParameters().isEmpty());
        em.getTransaction().commit();
        em.close();
	}
	
	void verifyParams(String jpql, Class<? extends Exception> error,
        Object... params) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        Query query = em.createNativeQuery(jpql);
        for (int i = 0; params != null && i < params.length; i = +2) {
            try {
                if (params[i] instanceof Number) {
                    query.setParameter(((Number) params[i]).intValue(),
                        params[i + 1]);
                } else {
                    query.setParameter(params[i].toString(), params[i + 1]);
                }
				if (error != null)
					fail("Expected " + error.getName());
			} catch (Exception e) {
				if (!error.isAssignableFrom(e.getClass())) {
                    // let the test harness handle the exception.
                    throw new RuntimeException("An unexpected exception " +
                            "occurred see the initCause for details", e);
				} 
			}		
		}
        em.getTransaction().commit();
        em.close();
	}
}
