/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openjpa.enhance;

import javax.persistence.EntityManager;

import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * A simple query fails with unenhanced (or runtime enhanced classes)
 * as originally reported in 
 * <A HREF="https://issues.apache.org/jira/browse/OPENJPA-659">OPENJPA-659</A>.
 * The original issue reports the failure in a Spring-Tomcat-Weaver settings
 * with embedded instances but even the following test shows the same failure 
 * in a simpler settings.
 *  
 * @author Pinaki Poddar
 *
 */
public class TestSimpleUnenhancedQuery extends SingleEMFTestCase {
	public void setUp() throws Exception {
		setUp(CLEAR_TABLES, UnenhancedPObject.class,"openjpa.RuntimeUnenhancedClasses", "supported");
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		em.persist(new UnenhancedPObject());
		em.getTransaction().commit();
	}
	
	public void testExtentQuery() {
		EntityManager em = emf.createEntityManager();
		assertFalse(em.createQuery("SELECT p FROM UnenhancedPObject p")
				.getResultList().isEmpty());
	}
}
