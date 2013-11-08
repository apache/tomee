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

import javax.persistence.EntityManager;

import org.apache.openjpa.persistence.merge.model.Toy;
import org.apache.openjpa.persistence.merge.model.ToyBox;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

public class TestInverseManager extends SingleEMFTestCase {
    public void setUp() {
        setUp(CLEAR_TABLES, Toy.class, ToyBox.class, "openjpa.InverseManager", "true(ManageLRS=true)");
    }

    public void testPersist() {
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        Toy t = new Toy(1);
        ToyBox tb = new ToyBox(1);

        tb.setToyList(Arrays.asList(new Toy[] { t }));
        em.persist(tb);

        em.getTransaction().commit();
        em.close();
    }
}
