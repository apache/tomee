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
package org.apache.openjpa.persistence.annotations;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import
    org.apache.openjpa.persistence.annotations.common.apps.annotApp.annotype.*;
import org.apache.openjpa.persistence.test.AllowFailure;
import org.apache.openjpa.persistence.OpenJPAEntityManager;

@AllowFailure(message="excluded")
public class TestAdvAnnot extends AnnotationTestCase
{
	public TestAdvAnnot(String name)
	{
		super(name, "annotationcactusapp");
	}

	public void setUp() {
		deleteAll(Schedule.class);
		deleteAll(FlightSchedule.class);

        OpenJPAEntityManager em =(OpenJPAEntityManager) currentEntityManager();
		startTx(em);

		for(int i = 0; i<5; i++)
		{
			Calendar cal = Calendar.getInstance();
			Date date = new Date(01012006l);
			Date edate = new Date(10031980l);
			String name = "AmericaWest"+i;
			String schedname = "Schedule"+i;
            FlightSchedule sched = new FlightSchedule(schedname, cal, date,
                    edate, name);

			em.persist(sched);


		}

		endTx(em);
		endEm(em);
	}

	public void testTimeTemporalAnnot()
	{
        OpenJPAEntityManager em =(OpenJPAEntityManager) currentEntityManager();

        String query = "SELECT d.startDate FROM FlightSchedule d "
            + "WHERE d.name = :name";

		Date obj = (Date) em.createQuery(query)
		              .setParameter("name", "Schedule3")
		              .getSingleResult();

		assertNotNull(obj);
		assertTrue(obj instanceof Date);

		endEm(em);
	}

	public void testCalendarTemporalAnnot()
	{
        OpenJPAEntityManager em =(OpenJPAEntityManager) currentEntityManager();
		String query = "SELECT d FROM FlightSchedule d";

		List list = em.createQuery(query).getResultList();
		assertNotNull(list);
		assertEquals(5, list.size());
		/*
        String query = "SELECT d.dob FROM FlightSchedule d "
            + "WHERE d.name = :name";

		List list = em.createQuery(query)
		              .setParameter("name", "Schedule3")
		              .getResultList();

		assertNotNull(list);
		assertTrue(list.get(0) instanceof Calendar);*/

		endEm(em);
	}

	public void testUniqueConstraintAnnot()
	{
        OpenJPAEntityManager em =(OpenJPAEntityManager) currentEntityManager();
		startTx(em);

        String query = "Update FlightSchedule f SET f.planeName = :plane "
            + "WHERE f.name = :sname";
		int upd = 0;

		try
		{
			upd = em.createQuery(query)
            .setParameter("plane", "AmericaWest3")
            .setParameter("sname", "Schedule2")
            .executeUpdate();

            fail("Violated unique constraint rule...@Unique Constraint"
                    + " annotation needs to be supported");
		}
		catch(Exception e)
		{
            //suppose to throw exception based on the unique constraint rule
		}

		assertNotNull(upd);
		assertEquals(0, upd);

		endTx(em);
		endEm(em);
	}




}
