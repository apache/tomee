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
package org.apache.openjpa.persistence.query;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.openjpa.persistence.FetchPlan;
import org.apache.openjpa.persistence.QueryImpl;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

public class TestOutOfBoundsEx extends SingleEMFTestCase {
	private EntityManager em = null;
	private Lookup lookup;
	
	public void setUp() throws Exception {
		super.setUp(Lookup.class, Case.class, Role.class, ScheduledAssignment.class, ScheduleDay.class, 
        DROP_TABLES);
		em = emf.createEntityManager();
		insertLookups();
	}
	
	public void testOutOfBounds() throws Exception {
		Calendar cal = Calendar.getInstance();
		final Date date = cal.getTime();
		ScheduleDay sd = insertScheduleDay(date);
		
		Role role1 = insertJob();
		Role role2 = insertJob();
		Case kase1 = insertCase(sd);
		Case kase2 = insertCase(sd);
		insertScheduledAssignmentInCase(role1, kase2);
		
		// simulate new web transaction on different em
		em.close();
		em = emf.createEntityManager();
		
		Query query = em.createQuery("select o from Case as o" +
				" where o.scheduleDay = :sd");
		query.setParameter("sd", sd);
		FetchPlan fetchPlan = ((QueryImpl) query).getFetchPlan();
		fetchPlan.addField(Case.class, "scheduledAssignments");
		
		//Without the changes of OJ1424, this next call would cause an 
		//ArrayIndexOutOfBoundsException.
		List<Case> allCases = query.getResultList();
	}

	public void insertLookups() {
		lookup = new Lookup();
		lookup.setName("XYZ");
		lookup.setId(1);
		save(lookup);
	}

	public void save(Object obj) {
		em.getTransaction().begin();
		em.persist(obj);
		em.getTransaction().commit();
	}
	
	public Role insertJob() {
		Role role = new Role();
		role.setLookup(lookup);
		save(role);
		return role;
	}

	public Case insertCase(ScheduleDay sd) throws Exception {
		Case kase = new Case();
		kase.setScheduleDay(sd);
		save(kase);
		return kase;
	}

	public void insertScheduledAssignmentInCase(Role job, Case kase) {
		ScheduledAssignment sa = new ScheduledAssignment();
		sa.setRole(job);
		sa.setCase(kase);
		sa.setScheduleDay(kase.getScheduleDay());
		save(sa);
	}

	public ScheduleDay insertScheduleDay(Date date) {
		ScheduleDay sd = new ScheduleDay();
		sd.setDate(date);
		save(sd);
		return sd;
	}	
}
