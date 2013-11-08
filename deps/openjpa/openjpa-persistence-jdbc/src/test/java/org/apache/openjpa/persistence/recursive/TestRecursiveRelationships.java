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
package org.apache.openjpa.persistence.recursive;

import javax.persistence.EntityManager;

import org.apache.openjpa.persistence.test.SingleEMFTestCase;

public class TestRecursiveRelationships extends SingleEMFTestCase {
    private int _l1Nodes = 3;
    private int _l2Nodes = 3;

    public void setUp() {
        setUp(DROP_TABLES, Node.class);
    }

    public void testRecursiveNodes() {
        EntityManager em = emf.createEntityManager();

        // set up initial tree
        em.getTransaction().begin();
        Node root = new Node();
        for (int i = 0; i < _l1Nodes; i++) {
            Node n1 = new Node();
            root.getNodes().add(n1);
            em.persist(n1);
        }
        em.persist(root);
        em.getTransaction().commit();

        // clear PC
        em.refresh(root);
        int rootId = root.getId();
        em.clear();
        em.close();
        em = emf.createEntityManager();

        // add new nodes
        em.getTransaction().begin();
        root = em.getReference(Node.class, rootId);
        assertNotNull(root);
        assertNotNull(root.getNodes());
        for (Node n : root.getNodes()) {
            for (int j = 0; j < _l2Nodes; j++) {
                Node n2 = new Node();
                n.getNodes().add(n.getNodes().size(), n2);
                em.persist(n2);
            }
        }
        em.getTransaction().commit();
        em.clear();
        em.close();
        em = emf.createEntityManager();

        // ensure count is correct.
        root = em.getReference(Node.class, rootId);
        assertNotNull(root);
        assertNotNull(root.getNodes());
        assertEquals(_l1Nodes, root.getNodes().size());
        for (Node n : root.getNodes()) {
            assertEquals(_l2Nodes, n.getNodes().size());
        }
        em.close();
    }
}
