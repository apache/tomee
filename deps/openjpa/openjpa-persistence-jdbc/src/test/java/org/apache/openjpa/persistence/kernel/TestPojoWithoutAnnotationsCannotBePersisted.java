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
package org.apache.openjpa.persistence.kernel;

import javax.persistence.EntityManager;

import org.apache.openjpa.persistence.common.utils.AbstractTestCase;
import org.apache.openjpa.persistence.kernel.common.apps.UnAnnotPojo;

/**
 * Ensures that pojo that is not persistent capable cannot be persisted.
 * @author Afam Okeke
 */
public class TestPojoWithoutAnnotationsCannotBePersisted
    extends AbstractTestCase {

    UnAnnotPojo pojo = null;

    public TestPojoWithoutAnnotationsCannotBePersisted(String name) {
        super(name, "kernelcactusapp");
    }

    public void setUp() throws Exception {
        super.setUp(new Object[] {});
        pojo = new UnAnnotPojo();
        pojo.setName("failure");
        pojo.setNum(0);
    }

    /*
      * Try to persist pojo with no metadata
      */
    public void testPersistingUnAnnotatedObject() {
        EntityManager em = currentEntityManager();
        startTx(em);

        try {
            em.persist(pojo);
            fail("...Should not persist object without proper metadata...");
        }
        catch (Exception e) {
            //expected
        }

        endTx(em);
        endEm(em);
    }
}
