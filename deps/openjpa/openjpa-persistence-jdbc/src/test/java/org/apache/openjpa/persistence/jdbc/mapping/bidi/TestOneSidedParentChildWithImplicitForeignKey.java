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
package org.apache.openjpa.persistence.jdbc.mapping.bidi;

import javax.persistence.EntityManager;

import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.meta.MetaDataRepository;
import org.apache.openjpa.meta.ValueStrategies;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Tests behavior of Parent-Child mapping under following conditions a) Parent
 * has many references to Child b) Child refers to Parent by Parent's identity
 * and not by object reference c) Parent's identity is assigned by the database
 * d) PostPersist callback in Parent sets the children's reference to Parent
 * 
 * The use case was originally reported in <A HREF=
 * "http://n2.nabble.com/OpenJPA---two-sided-relation-between-objects-Issue-
 * tc687050.html"> OpenJPA User Forum</A>
 * 
 * @author Pinaki Poddar
 */
public class TestOneSidedParentChildWithImplicitForeignKey extends
		SingleEMFTestCase {
	private EntityManager em;
	private static Class[] PARENT_ID_TYPES = { 
		ParentWithAppIdentity.class,      // ValueStrategies.NONE = 0
        ParentWithSequenceIdentity.class, // ValueStrategies.SEQUENCE = 2
        ParentWithAutoIdentity.class,     // ValueStrategies.AUTOASSIGN = 3
	};
	private static int[] VALUE_STRATEGIES = { 
		ValueStrategies.NONE,
		ValueStrategies.SEQUENCE, 
		ValueStrategies.AUTOASSIGN };
	
	private static long[] PARENT_IDS = new long[PARENT_ID_TYPES.length];

	private static long PARENT_ID_COUNTER = System.currentTimeMillis();
	private static long CHILD_ID_COUNTER = System.currentTimeMillis();
	private static int CHILD_COUNT = 3;

	@Override
	public void setUp() {
		super.setUp(DROP_TABLES, ParentWithAppIdentity.class,
                ParentWithSequenceIdentity.class, ParentWithAutoIdentity.class,
				Child.class);
		em = emf.createEntityManager();
		createData(CHILD_COUNT);
	}

	public void xtestStrategies() {
		MetaDataRepository repos = emf.getConfiguration()
				.getMetaDataRepositoryInstance();
		for (int i = 0; i < VALUE_STRATEGIES.length; i++) {
            ClassMetaData meta = repos.getMetaData(PARENT_ID_TYPES[i], null,
                    true);
			FieldMetaData fmd = meta.getPrimaryKeyFields()[0];
            assertEquals(fmd + " strategy is " + fmd.getValueStrategy(),
                    VALUE_STRATEGIES[i], fmd.getValueStrategy());
		}
	}

	void createData(int nChild) {
		em.getTransaction().begin();
		
		Child[] children = new Child[CHILD_COUNT];
		for (int j = 0; j < CHILD_COUNT; j++) {
			Child child = new Child();
			child.setId(CHILD_ID_COUNTER++);
			child.setName("Child" + j);
			children[j] = child;
		}
		
		for (int i = 0; i < PARENT_ID_TYPES.length; i++) {
			IParent parent = newParent(i);
			if (VALUE_STRATEGIES[i] == ValueStrategies.NONE)
				parent.setId(++PARENT_ID_COUNTER);
			for (int j = 0; j < CHILD_COUNT; j++) {
				parent.addChild(children[j]);
			}
			em.persist(parent);
			em.flush();
			PARENT_IDS[i] = parent.getId();
		}

		em.getTransaction().commit();
	}

	public void testPersist() {
		em.getTransaction().begin();

		for (int i = 0; i < PARENT_ID_TYPES.length; i++) {
			IParent parent = findParent(i);
			assertFalse(parent.getId() == 0);
			assertFalse(parent.getChildren().isEmpty());
			assertEquals(CHILD_COUNT, parent.getChildren().size());
			for (Child child : parent.getChildren()) {
                assertFalse(child.getParentIdType(VALUE_STRATEGIES[i]) == 0);
                assertTrue(child.getParentIdType(VALUE_STRATEGIES[i]) == parent
						.getId());
			}
		}
		em.getTransaction().commit();
	}

	public void testUpdate() {
		em.getTransaction().begin();

		Child newChild = new Child();
		newChild.setId(CHILD_ID_COUNTER++);
		newChild.setName("New Child");
		for (int i = 0; i < PARENT_ID_TYPES.length; i++) {
			IParent parent = findParent(i);
			parent.addChild(newChild);
			em.merge(parent);
		}
		em.flush();
		em.getTransaction().commit();
		em.clear();

		em.getTransaction().begin();
		for (int i = 0; i < PARENT_ID_TYPES.length; i++) {
			IParent parent = findParent(i);
			assertFalse(parent.getId() == 0);
			assertFalse(parent.getChildren().isEmpty());
            assertEquals(CHILD_COUNT + 1, parent.getChildren().size());
			for (Child child : parent.getChildren()) {
                assertFalse(child.getParentIdType(VALUE_STRATEGIES[i]) == 0);
                assertTrue(child.getParentIdType(VALUE_STRATEGIES[i]) == parent
						.getId());
			}
		}
		em.getTransaction().commit();
	}

	@Override
	public void tearDown() throws Exception {
	    closeEM(em);
	    super.tearDown();
	}

	public IParent newParent(int parentType) {
		try {
            IParent parent = (IParent)PARENT_ID_TYPES[parentType].newInstance();
            if (VALUE_STRATEGIES[parentType] == ValueStrategies.NONE)
				parent.setId(++PARENT_ID_COUNTER);
            parent.setName(PARENT_ID_TYPES[parentType].getSimpleName());
			return parent;
		} catch (Exception e) {
			fail(e.toString());
		}
		return null;
	}
	
	public IParent findParent(int parentType) {
		return (IParent) em.find(PARENT_ID_TYPES[parentType], 
				PARENT_IDS[parentType]);
	}
}
