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
package org.apache.openjpa.persistence.merge;

import java.io.IOException;
import java.util.ArrayList;

import javax.persistence.EntityManager;

import org.apache.openjpa.persistence.test.SQLListenerTestCase;

/**
 * Test the merge of a new object that has an Entity as
 * part of its Id
 */
public class TestMergeNew extends SQLListenerTestCase {
    public void setUp() {
        setUp(CLEAR_TABLES, Parent.class,
            Child.class, GrandChild.class);
        assertNotNull(emf);
        populate();
    }
    
    public void testMergeNewParent() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();    
        ParentPK pk = new ParentPK(1);
        pk.setKey1("K1");
        Parent parent = em.find(Parent.class, pk);
        
        Child child = new Child();                                               
        child.setChildKey(1);
        child.setParent(parent);                                                 
        parent.getChilds().add(child);                                           
        
        GrandChild grandChild = new GrandChild();
        grandChild.setGrandChildKey(1);
        grandChild.setChild(child);                                              
        child.getGrandChilds().add(grandChild);
    
        Parent newParent = em.merge(parent);
        assertNotNull(newParent);
        
        // verify key fields
        assertEquals(newParent.getKey1(), "K1");
        assertEquals(newParent.getKey2(), new Integer(1));
        
        // verify Child field
        ArrayList<Child> childs = (ArrayList<Child>)newParent.getChilds();
        assertNotNull(childs);
        assertEquals(childs.size(), 1);
        Child newChild = childs.get(0);
        assertNotSame(child, newChild);
        Parent childParent = newChild.getParent();
        assertEquals(childParent, newParent);
        assertEquals(newChild.getChildKey(), new Integer(1));
        
        // verify GrandChild field
        ArrayList<GrandChild> grandChilds = (ArrayList<GrandChild>)newChild.getGrandChilds();
        assertNotNull(grandChilds);
        assertEquals(grandChilds.size(), 1);
        GrandChild newGrandChild = grandChilds.get(0);
        assertNotSame(newGrandChild, grandChild);
        Child grandChildChild = newGrandChild.getChild();
        assertEquals(grandChildChild, newChild);
        
        em.getTransaction().commit();
        em.close();
    }

    public void testMergeParentRoundTrip()throws ClassNotFoundException, IOException {    	
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();    
        ParentPK pk = new ParentPK(1);
        pk.setKey1("K1");
        Parent parent = em.find(Parent.class, pk);
        
        //Simulate an EJB Call to get the Parent from the server:
        Parent p2 = (Parent) roundtrip(parent);
        
        Child child = new Child();                                               
        child.setChildKey(1);
        child.setParent(p2);                                                 
       	p2.getChilds().add(child); 
       	
        GrandChild grandChild = new GrandChild();
        grandChild.setChild(child);
        grandChild.setGrandChildKey(1);
       	child.getGrandChilds().add(grandChild);
       	
       	//Simulate an EJB Call to send the Parent back to the server:
       	Parent p3 = (Parent) roundtrip(p2);
       	
        em = emf.createEntityManager();
        em.getTransaction().begin();    
        
       	Parent newParent = em.merge(p3);
       	
       	em.getTransaction().commit();
       	assertNotNull(newParent);
       	
       	// verify key fields
       	assertEquals(newParent.getKey1(), "K1");
       	assertEquals(newParent.getKey2(), new Integer(1));
       	
       	// verify Child field
       	ArrayList<Child> childs = (ArrayList<Child>)newParent.getChilds();
       	assertNotNull(childs);
       	assertEquals(childs.size(), 1);
       	Child newChild = childs.get(0);
       	assertNotSame(child, newChild);
       	Parent childParent = newChild.getParent();
       	assertNotNull(childParent);
       	assertEquals(newChild.getChildKey(), new Integer(1));
       	
       	// verify GrandChild field
       	ArrayList<GrandChild> grandChilds = (ArrayList<GrandChild>)newChild.getGrandChilds();
       	assertNotNull(grandChilds);
       	assertEquals(grandChilds.size(), 1);
       	GrandChild newGrandChild = grandChilds.get(0);
       	assertNotSame(newGrandChild, grandChild);
       	Child grandChildChild = newGrandChild.getChild();
       	assertNotNull(grandChildChild);
       	em.close();
    }
    
    private void populate() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        Parent p = new Parent();
        p.setKey1("K1");
        p.setKey2(1);
        em.persist(p);
        em.getTransaction().commit();
        em.close();
    }
    
}
