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

import java.math.BigDecimal;
import javax.persistence.EntityManager;


import org.apache.openjpa.persistence.kernel.common.apps.AllFieldsTypeTest;
import org.apache.openjpa.persistence.common.utils.AbstractTestCase;

public class TestEJBState extends AbstractTestCase {

    public TestEJBState(String name) {
        super(name, "kernelcactusapp");
    }

    public void setUp() throws Exception {
        super.setUp(AllFieldsTypeTest.class);
    }

    public void testBigDecimalsLoseTrailingZeros() {
        EntityManager pm1 = currentEntityManager();
        startTx(pm1);
        AllFieldsTypeTest aftt = new AllFieldsTypeTest();
        aftt.setId(1);
        aftt.setTestBigDecimal(new BigDecimal("5.760000"));
        pm1.persist(aftt);

        endTx(pm1);

        EntityManager pm2 = currentEntityManager();
        startTx(pm2);

        Object retrieved = pm2.find(AllFieldsTypeTest.class, 1);
        assertEquals(aftt, retrieved);
        endTx(pm2);
        endEm(pm2);
        endEm(pm1);
    }
}
