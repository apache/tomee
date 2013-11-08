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


import org.apache.openjpa.persistence.kernel.common.apps.Inner;
import org.apache.openjpa.persistence.common.utils.AbstractTestCase;

public class TestEJBNullValues extends AbstractTestCase {

    public TestEJBNullValues(String name) {
        super(name, "kernelcactusapp");
    }

    public void setUp() throws Exception {
        super.setUp(Inner.class);
    }

    public void testException() {
        EntityManager pm = currentEntityManager();
        startTx(pm);
        pm.persist(new Inner());
        try {
            endTx(pm);
            fail("Null value allowed");
        }
        catch (Exception jfe) {
            // Exception expected...Null value not allowed...
        }
        endEm(pm);
    }
}
