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

import java.util.Arrays;

import javax.persistence.EntityTransaction;

import org.apache.openjpa.persistence.test.SingleEMTestCase;

public class TestRelationOrphanRemoval extends SingleEMTestCase {

    public void setUp() {
        setUp(Parent.class, Child.class, GrandChild.class, CLEAR_TABLES
             );
    }

    public void testRelationOrphanRemoval() {
        EntityTransaction tx = em.getTransaction();

        int origId = 10;
        int id = origId;

        Parent parent = new Parent();
        parent.setId(id++);
        Child child = new Child();
        child.setId(id++);
        GrandChild grandChild = new GrandChild();
        grandChild.setId(id++);
        parent.setChilds(Arrays.asList(child));
        child.setParent(parent);
        child.setGrandChilds(Arrays.asList(grandChild));
        grandChild.setChild(child);

        tx.begin();
        em.persist(parent);
        tx.commit();

        tx.begin();
        parent = em.find(Parent.class, new Integer(origId));
        child = parent.getChilds().iterator().next();
        grandChild = child.getGrandChilds().iterator().next();
        child.setName("Test");

        grandChild.setChild(null);
        child.setGrandChilds(null);
        child.setParent(null);
        parent.setChilds(null);
        tx.commit();
    }
}
