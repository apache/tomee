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
package org.apache.openjpa.persistence.kernel;

import javax.persistence.EntityManager;


import org.apache.openjpa.persistence.kernel.common.apps.Entity1;
import org.apache.openjpa.persistence.common.utils.AbstractTestCase;

public class Test2EJBConcurrency extends AbstractTestCase {

    private Object _id = null;

    public Test2EJBConcurrency(String name) {
        super(name, "kernelcactusapp");
    }

    public void setUp() throws Exception {
        deleteAll(Entity1.class);

        EntityManager em = currentEntityManager();
        startTx(em);

        Entity1 b = new Entity1(3, "STRING", 10);
        em.persist(b);
        em.flush();

        endTx(em);
        endEm(em);
    }

    /**
     * Test optimistic concurrency.
     */
    public void testOptConcurrency1()
        throws Exception {
        EntityManager em1 = currentEntityManager();
        startTx(em1);

        EntityManager em2 = currentEntityManager();
        startTx(em2);

        Entity1 b1 = (Entity1) em1.find(Entity1.class, 3);
        b1.setStringField("STRING2");
        endTx(em1);
        assertEquals("b1.getstringField is not STRING2 as exp.", "STRING2",
            b1.getStringField());

        Entity1 b2 = (Entity1) em2.find(Entity1.class, 3);
        assertEquals("b2.getstringField is not STRING2 as exp.", "STRING2",
            b2.getStringField());
        b2.setStringField("STRING3");
        endTx(em2);
        assertEquals("b2.getstringField is not STRING3 as exp.", "STRING3",
            b2.getStringField());

        startTx(em1);
        b1 = (Entity1) em1.find(Entity1.class, 3);
        em1.refresh(b1);
        assertEquals("b1.getstringField is not STRING3 as exp.", "STRING2",
            b1.getStringField());
        b1.setStringField("STRING4");
        endTx(em1);

        b2 = (Entity1) em2.find(Entity1.class, 3);
        assertEquals("b2.getstringField is not STRING3 as exp.", "STRING3",
            b2.getStringField());

        endEm(em1);
        endEm(em2);
    }

    /**
     *	Test optimistic concurrency.
     */
//	public void testOptConcurrency2 ()
//		throws Exception
//	{
//		EntityManager em1 = currentEntityManager();		
//		startTx(em1);
//
//		EntityManager em2 = currentEntityManager();		
//		startTx(em2);
//
//		Entity1 b1 = (Entity1) em1.find (Entity1.class, 3);
//		Entity1 b2 = (Entity1) em2.find (Entity1.class, 3);
//
//      assertEquals("b1.getstringField is not STRING as exp.", "STRING",
//          b1.getStringField());
//      assertEquals("b2.getstringField is not STRING as exp.", "STRING",
//          b2.getStringField());
//
//		b1.setStringField("STRING2");
//		endTx(em1);
//      assertEquals("b1.getstringField is not STRING as exp.", "STRING2",
//          b1.getStringField());
//
//      assertEquals("b2.getstringField is not STRING as exp.", "STRING",
//          b2.getStringField());
//		b2.setStringField("STRING3");
//
//		try
//		{
//			endTx(em2);
//			fail ("OL Violation");
//		}
//		catch (Exception ole)
//		{
//			// expected
//		}
//		
//		rollbackTx(em2);
//		
//
//    	b2 = (Entity1) em2.find (Entity1.class, 3);
//      assertEquals ("b2.getstringField is not STRING2 as exp.", "STRING2",
//          b2.getStringField ());
//    	
//		endEm(em1);
//		endEm(em2);
//	}
}

