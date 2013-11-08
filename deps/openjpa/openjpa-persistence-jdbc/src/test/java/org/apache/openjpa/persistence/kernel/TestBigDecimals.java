/*
 * TestBigDecimals.java
 *
 * Created on October 9, 2006, 6:07 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

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



import org.apache.openjpa.persistence.kernel.common.apps.AllFieldTypesTest;
import junit.framework.AssertionFailedError;

import org.apache.openjpa.persistence.OpenJPAEntityManager;

public class TestBigDecimals extends BaseKernelTest {

    /**
     * Creates a new instance of TestBigDecimals
     */
    public TestBigDecimals() {
    }

    public TestBigDecimals(String name) {
        super(name);
    }

    public void testBigDecimalDataIntegrity()
        throws Exception {
        try {
            BigDecimal bd = new BigDecimal(Math.random() * 10000000 + "");
            bd = bd.setScale(100);
            for (int i = 0; i < 50; i++) {
                bd = bd.movePointLeft(1);
                bigDecimalTest(bd);
            }
        }
        catch (AssertionFailedError e) {
            bug(3, e, "Precision loss for BigDecimals");
        }
    }
//    FixMe aokeke: Passes but takes a long time --commenting for resource sake
//    public void testBigBigDecimals()
//        throws Exception {
//        try {
//            BigDecimal bd = new BigDecimal("1234567890."
//                + "12345678901234567890123456789012345678901234567890"
//                + "12345678901234567890123456789012345678901234567890");
//
//            bigDecimalTest(bd);
//        } catch (AssertionFailedError e) {
//            bug(3, e, "Precision loss for BigDecimals");
//        }
//    }

    public void bigDecimalTest(final BigDecimal bd) {
        OpenJPAEntityManager pm = null, pm2 = null;

        try {
            pm = getPM();
            startTx(pm);
            AllFieldTypesTest aftt = new AllFieldTypesTest();
            aftt.setTestBigDecimal(bd);
            pm.persist(aftt);
            endTx(pm);
            Object id = pm.getObjectId(aftt);
            pm.evict(aftt);

            pm2 = getPM();
            startTx(pm);
            AllFieldTypesTest aftt2 = (AllFieldTypesTest) pm2.getObjectId(id);

            // why wouldn't they be two different objects?
            assertTrue("identitcal field values",
                bd != aftt2.getTestBigDecimal());

            // this should always succeed
            assertEquals(bd,
                aftt2.getTestBigDecimal().setScale(bd.scale()));

            // this will fail if we are losing scale
            assertEquals(bd, aftt2.getTestBigDecimal());

            rollbackTx(pm);
        } catch (Throwable afe) {
            bug(3, afe, "floating point precision loss");
        } finally {
            if (pm != null) {
                //if (pm.getTransaction().isActive())
                //rollbackTx(pm,());
                endEm(pm);
            }

            if (pm2 != null) {
                //if (pm2.getTransaction().isActive())
                //rollbackTx(pm2,());
                //pm2.close();
                endEm(pm2);
            }
        }
    }
}
