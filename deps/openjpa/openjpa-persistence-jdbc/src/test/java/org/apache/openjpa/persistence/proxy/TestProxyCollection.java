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
package org.apache.openjpa.persistence.proxy;

import java.util.LinkedHashSet;

import javax.persistence.EntityManager;

import org.apache.openjpa.persistence.test.SingleEMFTestCase;
import org.apache.openjpa.util.ChangeTracker;
import org.apache.openjpa.util.ProxyCollection;

/**
 * Tests proxying and change tracking of collection fields for modification in
 * detached state.
 * 
 * Originally reported in
 * <A HREF="https://issues.apache.org/jira/browse/OPENJPA-628">OPENJPA-628</A>
 * 
 * @author Pinaki Poddar
 *
 */
public class TestProxyCollection extends SingleEMFTestCase {
	public void setUp() {
		super.setUp(CLEAR_TABLES, TreeNode.class, ConcreteEntity.class, AbstractEntity.class);
	}
	/**
	 * Tests that a uniform tree is created with expected fan outs at each 
	 * level. This is not a persistent operation, just in-memory. 
	 */
	public void testCreateTree() {
		TreeNode root = new TreeNode();
		root.setName("0");
		int[] fanOuts = {1,2,3};
		root.createTree(fanOuts);
		assertArrayEquals(fanOuts, root.getFanOuts());
	}
	
	/**
     * Tests that a uniform tree can be modified with different fan outs at each
	 * level. This is not a persistent operation, just in-memory. 
	 */
	public void testModifyTree() {
		int[] fanOuts = {1,2,2,4};
		int[] newFanOuts = {1,3,1,2};
		TreeNode root = new TreeNode();
		root.createTree(fanOuts);
		assertArrayEquals(fanOuts, root.getFanOuts());
		
		root.modify(newFanOuts);
		assertArrayEquals(newFanOuts, root.getFanOuts());
	}
	
	/**
     * Tests that a uniform tree is persisted and later fetched back with same
	 * number of children at every level.
	 */
	public void testPersistTree() {
		int[] fanOuts = {2,3,4};
		verify(create(fanOuts), fanOuts);
	}
	
	public void testAddNodeAtLeaf() {
		int[] original = {1,2,3};
		int[] modifier = {1,2,4}; // add new child at Level 2
		createModifyAndMerge(original, modifier);
	}
	
	public void testAddNewLevel() {
		int[] original = {1,2,3};
		int[] modifier = {1,2,3,2}; // add 2 new children at new Level 
		createModifyAndMerge(original, modifier);
	}
	
	public void testAddAndRemove() {
		int[] original = {2,3,4};
        int[] modifier = {4,3,2}; // add 1 at Level 1 + remove 1 at Level 3
		createModifyAndMerge(original, modifier);
	}
	
	public void testAddAtAllLevel() {
		int[] original = {2,3,4};
		int[] modifier = {3,4,5}; // add 1 at each Level 
		createModifyAndMerge(original, modifier);
	}
	
	public void testRemoveAtAllLevel() {
		int[] original = {2,3,4};
		int[] modifier = {1,2,3}; // remove 1 from each Level 
		createModifyAndMerge(original, modifier);
	}
	
    public void testCreateCorrectType() {
        ConcreteEntity ce = new ConcreteEntity();
        ce.addItem(ce);

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        em.persist(ce);
        em.getTransaction().commit();
        em.clear();

        ce = em.find(ConcreteEntity.class, ce.getId());
        assertNotNull(ce);
        Class<?> proxyCls = ce.getItems().getClass();
        assertTrue(proxyCls + " is not assignableFrom " + LinkedHashSet.class,
            LinkedHashSet.class.isAssignableFrom(proxyCls));
    }
	/**
	 * Create a uniform tree with original fanout.
	 * Persist.
	 * Verify in a separate persistence context that the tree is stored.
	 * Modify the tree by adding or deleting nodes according to the given 
	 * modified fanouts outside a transaction.
	 * Merge the changes.
	 * Verify that the changes are merged by fetching the modified version.
	 * 
	 * @param original
	 * @param modified
	 */
	void createModifyAndMerge(int[] original, int[] modifier) {
		TreeNode root = create(original);
		
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		TreeNode modified = em.find(TreeNode.class, root.getId());
		modified.modify(modifier);
		em.merge(modified);
		em.getTransaction().commit();

		em.clear();

		assertProxyCollection(root.getNodes(), false);

		verify(root, modifier);
	}
	
	/**
	 * Create a uniform tree with given fan out.
	 * Persist.
     * Verify that the tree is stored by fetching it in a separate persistence
	 * context.
	 */
	TreeNode create(int[] original) {
		TreeNode root = new TreeNode();
		root.createTree(original);
		
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		em.persist(root);
		em.getTransaction().commit();
		em.clear();
		
		return root;
	}
	
	void verify(TreeNode node, int[] fanOuts) {
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		TreeNode test = em.find(TreeNode.class, node.getId());
		assertNotNull(test);
		assertArrayEquals(fanOuts, test.getFanOuts());
	}

    /** 
     * Asserts the given arrays have exactly same elements at the same index.
     */
	void assertArrayEquals(int[] a, int[] b) {
		assertEquals(a.length, b.length);
		for (int i = 0; i<a.length; i++)
			assertEquals(a[i], b[i]);
	}

	/**
     * Asserts that the given object is a proxy collection and whether it is
	 * tracking changes.
	 */
	void assertProxyCollection(Object o, boolean tracking) {
		assertTrue(o instanceof ProxyCollection);
		ChangeTracker tracker = ((ProxyCollection)o).getChangeTracker();
		if (tracking) {
			assertNotNull(tracker);
			assertTrue(tracker.isTracking());
		} else {
			assertFalse(tracker.isTracking());
		}
	}

	/**
	 * Asserts that the given object is NOT a proxy collection.
	 */
	void assertNotProxyCollection(Object o) {
		assertFalse(o instanceof ProxyCollection);
	}
}

