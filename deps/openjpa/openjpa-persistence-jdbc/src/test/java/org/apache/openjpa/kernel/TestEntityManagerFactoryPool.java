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
package org.apache.openjpa.kernel;

import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.openjpa.persistence.test.SingleEMFTestCase;

public class TestEntityManagerFactoryPool
    extends SingleEMFTestCase {

    public void setUp() {
        setUp("openjpa.EntityManagerFactoryPool", Boolean.TRUE);

        emf.createEntityManager().close();
    }

    public void testBrokerFactoryPoolHit() {
        Map m = new HashMap();
        // also tests string values for the property
        m.put("openjpa.EntityManagerFactoryPool", "True");
        EntityManagerFactory emf1 = Persistence.createEntityManagerFactory(
            "test", m);
        assertSame(this.emf, emf1);
        clear(emf1);
        closeEMF(emf1);
    }

    public void testBrokerFactoryPoolMiss() {
        Map m = new HashMap();
        m.put("openjpa.EntityManagerFactoryPool", Boolean.TRUE);
        EntityManagerFactory emf1 = Persistence.createEntityManagerFactory(
            "second-persistence-unit", m);
        assertNotSame(this.emf, emf1);
        clear(emf1);
        closeEMF(emf1);
    }
}
