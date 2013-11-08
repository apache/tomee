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

import javax.persistence.* ;

import org.apache.openjpa.jdbc.conf.* ;
import org.apache.openjpa.jdbc.meta.* ;
import org.apache.openjpa.jdbc.meta.strats.* ;

import
    org.apache.openjpa.persistence.annotations.common.apps.annotApp.annotype.*;
import org.apache.openjpa.persistence.test.AllowFailure;

import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAEntityManagerSPI;
import org.apache.openjpa.persistence.OpenJPAPersistence;

/*
   Test for opt-lock

   @author Steve Kim
  */
@AllowFailure(message="excluded")
public class TestVersion extends AnnotationTestCase
{
	private Object oid;

	private Object oid1;

	private Object oid2;

	public TestVersion(String name)
	{
		super(name, "annotationcactusapp");
	}

	public void setUp()
	{
		new AnnoTest1();
		new AnnoTest2();
		new AnnoTest3();

		deleteAll(AnnoTest1.class);
		deleteAll(AnnoTest2.class);

		OpenJPAEntityManager em = currentEntityManager();
		startTx(em);
		AnnoTest1 test1 = new AnnoTest1();
		test1.setPk(new Long(5));
		test1.setBasic(50);
		test1.setTransient(500);
		em.persist(test1);

		AnnoTest2 test2 = new AnnoTest2();
		test2.setPk1(5);
		test2.setPk2("bar");
		test2.setBasic("50");
		em.persist(test2);

		AnnoTest3 test3 = new AnnoTest3();
		test3.setPk(new Long(3));
		test3.setBasic2(50);
		em.persist(test3);
		oid = em.getObjectId(test1);
		oid1 = em.getObjectId(test2);
		oid2 = em.getObjectId(test3);

		endTx(em);
		endEm(em);
	}

/*
 * Fix Me aokeke -- Testcases causes deadlock during runtime CR307216 is used to
 * track this issue.
 */
 public void testVersionNumeric()
	{
		OpenJPAEntityManager em1 = currentEntityManager();
		startTx(em1);
		EntityManager em2 = getEmf().createEntityManager();

		AnnoTest1 pc1 = em1.find(AnnoTest1.class, oid);
		AnnoTest1 pc2 = em2.find(AnnoTest1.class, oid);
		assertEquals(1, pc1.getVersion());
		assertEquals(1, pc2.getVersion());
		assertEquals(0, pc1.getTransient());
		pc1.setBasic(75);

		endTx(em1);
		endEm(em1);

		em2.getTransaction().begin();
		pc2.setBasic(75);
		em1 = (OpenJPAEntityManager) currentEntityManager();
		pc1 = em1.find(AnnoTest1.class, oid);
		assertEquals(2, pc1.getVersion());
		endEm(em1);
		try
		{
			em2.getTransaction().commit();
			fail("Optimistic fail");
		}
		catch (RuntimeException re)
		{}
		catch (Exception e)
		{}
		finally
		{
			em2.close();
		}
	}

	public void testVersionTimestamp()
	{
		OpenJPAEntityManager em1 = currentEntityManager();
		startTx(em1);
		OpenJPAEntityManager em2 = getEmf().createEntityManager();

		AnnoTest2 pc1 = em1.find(AnnoTest2.class, oid1);
		AnnoTest2 pc2 = em2.find(AnnoTest2.class, oid1);
		assertNotNull(pc1.getVersion());
		assertEquals(pc1.getVersion(), pc2.getVersion());
		pc1.setBasic("75");

		endTx(em1);
		endEm(em1);

		em2.getTransaction().begin();
		pc2.setBasic("75");

		em1 = (OpenJPAEntityManager) currentEntityManager();
		pc1 = em1.find(AnnoTest2.class, oid1);
		assertTrue(pc1.getVersion().compareTo(pc2.getVersion()) > 0);
		endEm(em1);
		try
		{
			em2.getTransaction().commit();
			fail("Optimistic fail");
		}
		catch (RuntimeException re)
		{}
		catch (Exception e)
		{}
		finally
		{
			em2.close();
		}
	}

	public void testVersionSubclass()
	{
		OpenJPAEntityManager em1 = currentEntityManager();
		startTx(em1);
		OpenJPAEntityManager em2 = getEmf().createEntityManager();

		AnnoTest3 pc1 = em1.find(AnnoTest3.class, oid2);
		AnnoTest3 pc2 = em2.find(AnnoTest3.class, oid2);
		assertEquals(1, pc1.getVersion());
		assertEquals(1, pc2.getVersion());
		pc1.setBasic2(75);

		endTx(em1);
		endEm(em1);


		em2.getTransaction().begin();
		pc2.setBasic2(75);


		em1 = (OpenJPAEntityManager) currentEntityManager();
		pc1 = em1.find(AnnoTest3.class, oid2);
		assertEquals(2, pc1.getVersion());
		endEm(em1);
		try
		{
			em2.getTransaction().commit();
			fail("Optimistic fail");
		}
		catch (RuntimeException re)
		{}
		catch (Exception e)
		{}
		finally
		{
			em2.close();
		}
	}

	public void testVersionNoChange()
	{
		OpenJPAEntityManager em = currentEntityManager();
		startTx(em);

		AnnoTest1 pc = em.find(AnnoTest1.class, oid);
		assertEquals(1, pc.getVersion());
		assertEquals(0, pc.getTransient());
		pc.setTransient(750);
		endTx(em);
		endEm(em);

		em = (OpenJPAEntityManager) currentEntityManager();
		pc = em.find(AnnoTest1.class, oid);
		assertEquals(1, pc.getVersion());
		assertEquals(0, pc.getTransient());
		endEm(em);
	}

	   public void testNoDefaultVersionWithoutFieldOrColumn()
	   {
           OpenJPAEntityManager pm =
               (OpenJPAEntityManager) currentEntityManager();
           ClassMapping cls =
                   ((JDBCConfigurationImpl) ((OpenJPAEntityManagerSPI)
                   OpenJPAPersistence.cast(pm)).getConfiguration())
                   .getMappingRepositoryInstance().getMapping(EmbedOwner.class,
                           null, true);
           assertEquals(NoneVersionStrategy.getInstance(),
                   cls.getVersion().getStrategy()); assertEquals(0,
                           cls.getVersion().getColumns().length);
			endEm(pm);
	   }

	   public void testVersionWithField()
	   {
           OpenJPAEntityManager pm =
               (OpenJPAEntityManager) currentEntityManager();
           ClassMapping cls = ((JDBCConfigurationImpl)((OpenJPAEntityManagerSPI)
                   OpenJPAPersistence.cast(pm)).getConfiguration())
                   .getMappingRepositoryInstance().getMapping(AnnoTest1.class,
                           null, true);
		   assertTrue(NoneVersionStrategy.getInstance() !=
			   cls.getVersion().getStrategy()); assertEquals(1,
                       cls.getVersion().getColumns().length);
			endEm(pm);
	   }
}
