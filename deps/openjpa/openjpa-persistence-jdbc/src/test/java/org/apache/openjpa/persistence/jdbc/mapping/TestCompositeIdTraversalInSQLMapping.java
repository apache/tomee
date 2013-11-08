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
package org.apache.openjpa.persistence.jdbc.mapping;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.openjpa.persistence.jdbc.common.apps.mappingApp.*;
import org.apache.openjpa.persistence.common.utils.*;


public class TestCompositeIdTraversalInSQLMapping extends AbstractTestCase {
	
	
	public TestCompositeIdTraversalInSQLMapping(String name)
	{
		super(name, "jdbccactusapp");
	}

	
	public void setUp()
	{
		deleteAll(OwnerOfEntityWithCompositeId.class);
		deleteAll(EntityWithCompositeId.class);
	}

    private void persist(Integer uniqueId, String uniqueName, String relName) {
        OwnerOfEntityWithCompositeId owner = new OwnerOfEntityWithCompositeId();
		EntityWithCompositeId relative = new EntityWithCompositeId();
		owner.setName(uniqueName);
		relative.setId(uniqueId);
		relative.setName(relName);
		relative.setValue("foo");
		owner.setRelation(relative);
		
		EntityManager em = currentEntityManager();
		startTx(em);
		em.persist(owner);
		endTx(em);
		endEm(em);
	}
	public void testTraversalWhenSQLSelectsBothEndOfTheRelation ()
	{
		EntityManager em = currentEntityManager();
        Integer uid  = new Integer((int) (System.currentTimeMillis()%100000));
		String uName = "P"+uid;
		String rName = "R"+uName;
		
		persist(uid, uName, rName);
		
        String sql = "SELECT a.NAME as OWNER_NAME, a.RELATION_ID as REL_ID," +
		             "a.RELATION_NAME as REL_NAME, b.ID as C_ID," +
                     "b.NAME as C_NAME, b.VALUE as C_VALUE " +
		             "FROM OWNER_OF_COMPOSITE_ID a, COMPOSITE_ID b " +
                     "WHERE a.RELATION_ID=b.ID AND a.RELATION_NAME=b.NAME " +
		             "AND a.NAME='" + uName + "'";
		
		Query query = em.createNativeQuery(sql, 
			"SQLSelectsBothEndOfTheRelation");
		
		List result = query.getResultList();
		
		assertEquals(1,result.size());
		Object object = result.get(0);
		assertEquals(Object[].class, object.getClass());
		Object[] array = (Object[])object;
		assertEquals(2,array.length);
        assertEquals(OwnerOfEntityWithCompositeId.class,array[0].getClass());
		assertEquals(EntityWithCompositeId.class,array[1].getClass());
		
        OwnerOfEntityWithCompositeId owner = (OwnerOfEntityWithCompositeId)
			array[0];
		
        EntityWithCompositeId relative = (EntityWithCompositeId)array[1];
		
		assertEquals(uName, owner.getName());
		assertEquals(owner.getRelation(), relative);
		assertEquals(relative.getId(),uid);
		assertEquals(relative.getName(), rName);
		assertEquals("foo", relative.getValue());
		endEm(em);
	}

	public void testTraversalWhenSQLSelectsOnlyOneEndOfTheRelation ()
	{
		EntityManager em = currentEntityManager();
        Integer uid  = new Integer((int) (System.currentTimeMillis()%100000));
		String uName = "P"+uid;
		String rName = "R"+uName;
		
		persist (uid, uName, rName);
        String sql = "SELECT a.NAME as OWNER_NAME, a.RELATION_ID as REL_ID," +
		             "a.RELATION_NAME as REL_NAME "+
		             "FROM OWNER_OF_COMPOSITE_ID a " +
		             "WHERE  a.NAME='" + uName + "'";
		
		Query query = em.createNativeQuery(sql, 
			"SQLSelectsOnlyOneEndOfTheRelation");
		
		List result = query.getResultList();
		
		assertEquals(1,result.size());
		Object object = result.get(0);
        assertEquals(OwnerOfEntityWithCompositeId.class,object.getClass());
		
        OwnerOfEntityWithCompositeId owner = (OwnerOfEntityWithCompositeId)
			object;
		
		EntityWithCompositeId relative = owner.getRelation();
		
		assertEquals(uName, owner.getName());
		assertEquals(relative.getId(),uid);
		assertEquals(relative.getName(), rName);
		assertEquals("foo", relative.getValue());
		endEm(em);
	}
	public void testTraversalWhenSQLSelectsUnrelatedInstances ()
	{
		EntityManager em = currentEntityManager();
        Integer uid1  = new Integer((int) (System.currentTimeMillis()%100000));
		Integer uid2  = new Integer(uid1.intValue()+1);
		String uName1 = "P"+uid1;
		String rName1 = "R"+uName1;
		String uName2 = "P"+uid2;
		String rName2 = "R"+uName2;
		
		persist(uid1, uName1, rName1);
		persist(uid2, uName2, rName2);
		
        String sql = "SELECT a.NAME as OWNER_NAME, a.RELATION_ID AS REL_ID," +
        "a.RELATION_NAME AS REL_NAME, " +
        "b.ID AS C_ID, b.NAME AS C_NAME, b.VALUE AS C_VALUE "+
        "FROM OWNER_OF_COMPOSITE_ID a, COMPOSITE_ID b " +
        "WHERE b.NAME='"+ rName2 +"' " +
        "AND a.NAME='" + uName1 + "'";
		
		Query query = em.createNativeQuery(sql, 
				"SQLSelectsUnrelatedInstances");
		
		List result = query.getResultList();
		
		assertEquals(1,result.size());
		Object object = result.get(0);
		assertEquals(Object[].class, object.getClass());
		Object[] array = (Object[])object;
		assertEquals(2,array.length);
        assertEquals(OwnerOfEntityWithCompositeId.class,array[0].getClass());
		assertEquals(EntityWithCompositeId.class,array[1].getClass());
		
        OwnerOfEntityWithCompositeId owner1 = (OwnerOfEntityWithCompositeId)
			array[0];
		EntityWithCompositeId relative1 = owner1.getRelation();
		
        EntityWithCompositeId relative2 = (EntityWithCompositeId)array[1];
		
		assertEquals(uName1, owner1.getName());
		assertEquals(uid1, relative1.getId());
		assertEquals(rName1, relative1.getName());
		assertEquals(uid2, relative2.getId());
		assertEquals(rName2, relative2.getName());
		endEm(em); 
	}
	
	public void testRecursiveTraversal () 
	{
        Integer rootId  = new Integer((int)(System.currentTimeMillis()%100000));
		
		int depth = 3;
		persistChainedRelative(rootId, depth);
		
		String sql = createSelfJoinSQL("RECURSIVE_ENTITY",
				depth,rootId);
		
		EntityManager em = currentEntityManager();
        Query query = em.createNativeQuery(sql, "SQLSelectsChainedRelation");

		List result = query.getResultList();
		
		assertEquals(1, result.size());
		assertEquals(RecursiveEntityWithCompositeId.class,
				result.get(0).getClass());
		
        RecursiveEntityWithCompositeId root = (RecursiveEntityWithCompositeId)
                result.get(0);
		assertEquals(rootId,root.getId());
		assertEquals("P"+rootId,root.getName());
		for (int i=1; i<depth;i++){
            RecursiveEntityWithCompositeId relative = root.getRelation();
			Integer expecetedId = root.getId()+1;
			assertEquals (expecetedId,relative.getId());
			assertEquals ("P"+expecetedId, relative.getName());
			root = relative;
		}
		 
	}
	
	void persistChainedRelative(Integer rootId, int depth)
	{
		RecursiveEntityWithCompositeId root = 
			new RecursiveEntityWithCompositeId();
		root.setId(rootId);
		root.setName("P"+rootId);
		RecursiveEntityWithCompositeId head = root;
		for (int i=1; i<=depth; i++)
		{
			RecursiveEntityWithCompositeId relation = 
				new RecursiveEntityWithCompositeId();
			relation.setId(rootId+i);
			relation.setName("P"+(rootId+i));
			head.setRelation(relation);
			head = relation;
		}
		EntityManager em = currentEntityManager();
		startTx(em);
		em.persist(root);
		endTx(em);
		endEm(em);
	}
	
	String createSelfJoinSQL(String table, int depth, int id)
	{
		
		StringBuffer sql = new StringBuffer("SELECT ");
		for (int i=0; i<depth; i++)
			sql.append("t"+i+".ID AS T"+i+"_ID, ")
			   .append("t"+i+".NAME AS T"+i+"_NAME, ")
			   .append("t"+i+".RELATION_ID AS T"+i+"_REL_ID, ")
			   .append("t"+i+".RELATION_NAME AS T"+i+"_REL_NAME")
			   .append((i==(depth-1))?" " : ", ");
		
		sql.append(" FROM ");
		for (int i=0; i<depth; i++)
			sql.append(table + " t"+i)
			   .append((i==(depth-1))?" " : ", ");
		
		sql.append(" WHERE ");
		for (int i=0; i<(depth-1); i++)
			sql.append("t"+i+".RELATION_ID=t"+(i+1)+".ID AND ")
               .append("t"+i+".RELATION_NAME=t"+(i+1)+".NAME AND " );
		
		sql.append("t0.ID="+id);
		
		return sql.toString();
	}
}
