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
package org.apache.openjpa.persistence.relations;

import javax.persistence.EntityManager;

import org.apache.openjpa.persistence.test.SingleEMFTestCase;

public class TestChainEntities extends SingleEMFTestCase {

	/*
	 * Set this magical number to 3 or less to avoid the error.
	 */
	private static final int MAGICAL_NUMBER = 50;
	
	long aid;

	public void setUp () {
		setUp (DROP_TABLES,
		    ChainEntityA.class, ChainEntityB.class, ChainEntityC.class);
		// Create A
		ChainEntityA a = new ChainEntityA ();
		a.setName ("TEST_A");
		// Persist A
		EntityManager em = emf.createEntityManager ();
		em.getTransaction ().begin ();
		em.persist (a);
		aid = a.getId ();
		em.getTransaction ().commit ();
		em.close ();
	}

	public void testChainEntities () {
		chainUpdate ();
	}
	
	protected void chainUpdate () {
		// Get A
		ChainEntityA a = getA ();
		// Create B
		ChainEntityB b = new ChainEntityB ();
		b.setName ("Test_B_");
		ChainEntityC c;
		/*
         * Create and add C to B. Increasing the number of iterations (number of
		 * ChainEntityC) increases the probability to get the unique key
		 * constraint violation error.
		 */
		for (int i = 1; i <= MAGICAL_NUMBER; i++) {
			c = new ChainEntityC ();
			c.setName ("Test_C_" + i);
			b.addChainEntityC (c);
		}
		a.addChildEntityB (b);
		// dump (a);  // debug
		// Merge A
		EntityManager em = emf.createEntityManager ();
		em.getTransaction ().begin ();
		a = em.merge (a);
		/*
         * workaround: Uncommenting following line is a workaround. If we
         * retrive Ids of ChainEntityC objects after merge but before commit we
		 * don't get the error.
		 */
		//dump (a);
		em.getTransaction ().commit ();
		em.close ();
		// dump (getA ());  // debug
	}

	/**
	 * Get created ChainEntityA using aid field.
	 * 
	 * @return
	 */
	protected ChainEntityA getA () {
		EntityManager em = emf.createEntityManager ();
		ChainEntityA a = em.find (ChainEntityA.class, aid);
		em.close ();
		return a;
	}

	/**
	 * Print the object graph of given ChainEntityA to System.out
	 * 
	 * @param testA
	 */
	protected void dump (ChainEntityA testA) {
		System.out.println ("-------");
        System.out.println (testA.getName () + "[" + testA.getId () + "]");
        for (ChainEntityB testB : testA.getChildren ()) {
            System.out.println (testB.getName () + "[" + testB.getId () + "]");
            for (ChainEntityC testC : testB.getChainEntityCSet ()) {
                System.out.println (testC.getName () + "[" + testC.getId ()
                        + "]");
            }
        }
		System.out.println ("-------");
	}

}
