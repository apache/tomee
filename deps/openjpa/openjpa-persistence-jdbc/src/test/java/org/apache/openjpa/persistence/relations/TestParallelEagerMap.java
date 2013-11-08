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

import java.util.HashMap;
import java.util.Map;

import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

public class TestParallelEagerMap extends SingleEMFTestCase {

    public void setUp() {
        super.setUp(CLEAR_TABLES, MapKeyParent.class, MapKeyChild.class,
            "openjpa.jdbc.EagerFetchMode", "parallel");
    }

    public void testParentNotNull() {

        MapKeyParent[] parents = { new MapKeyParent(), new MapKeyParent() };
        for (MapKeyParent parent : parents) {
            Map<String, MapKeyChild> children =
                new HashMap<String, MapKeyChild>();
            for (String key : new String[] { "childA" }) {
                MapKeyChild child = new MapKeyChild();
                child.setParent(parent);
                child.setMapKey(key);
                children.put(key, child);
            }
            parent.setChildren(children);
        }

        OpenJPAEntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        for (MapKeyParent parent : parents) {
            em.persist(parent);
        }
        em.getTransaction().commit();
        em.close();

        em = emf.createEntityManager();
        MapKeyParent parent2 =
            (MapKeyParent) em.createQuery(
                "SELECT p FROM MapKeyParent p WHERE p.id=" + parents[0].getId()
                    + " OR p.id=" + parents[1].getId()).getResultList().get(1);
        em.close();

        for (MapKeyChild child : parent2.getChildren().values()) {
            assertNotNull("Parent should not be null", child.getParent());
        }
    }
}
