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


import org.apache.openjpa.persistence.kernel.common.apps.TransactionalClassPC;
import org.apache.openjpa.persistence.common.utils.AbstractTestCase;

public class TestEJBTransactionalClass extends AbstractTestCase {

    public TestEJBTransactionalClass(String name) {
        super(name, "kernelcactusapp");
    }

    public void testTransactional() {
        TransactionalClassPC pc = new TransactionalClassPC();
        pc.setIntField(1);

        EntityManager pm = currentEntityManager();
        startTx(pm);
        //pm.makeTransactional (pc);
        pc.setIntField(2);
        endTx(pm);

        assertEquals(2, pc.getIntField());

        startTx(pm);
        pc.setIntField(3);
        pm.getTransaction().rollback();

        assertEquals(3, pc.getIntField());
        endEm(pm);
    }
}
