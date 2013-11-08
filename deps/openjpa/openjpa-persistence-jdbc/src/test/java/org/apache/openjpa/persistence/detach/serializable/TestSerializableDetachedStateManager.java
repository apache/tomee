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
package org.apache.openjpa.persistence.detach.serializable;

import java.sql.Date;

import org.apache.openjpa.persistence.OpenJPAEntityManagerSPI;
import org.apache.openjpa.persistence.test.AbstractPersistenceTestCase;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

public class TestSerializableDetachedStateManager extends SingleEMFTestCase {
    public void setUp() {
        setUp(CLEAR_TABLES, SerializableDetachedStateManager.class, DROP_TABLES, "openjpa.DetachState",
            "fgs(DetachedStateField=true)");
    }
    
    public void testRoundTrip() throws Exception {
        SerializableDetachedStateManager c = new SerializableDetachedStateManager();
        c.zDate = new Date(System.currentTimeMillis());
        OpenJPAEntityManagerSPI em = emf.createEntityManager();
        
        em.getTransaction().begin();
        em.persist(c);
        em.getTransaction().commit();
        em.close();
        AbstractPersistenceTestCase.roundtrip(c);
    }
}
