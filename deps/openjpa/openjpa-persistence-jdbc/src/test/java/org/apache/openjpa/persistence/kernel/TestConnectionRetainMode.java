/*
 * TestConnectionRetainMode.java
 *
 * Created on October 10, 2006, 1:09 PM
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

import java.util.HashMap;
import java.util.Map;




import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactory;

public class TestConnectionRetainMode extends BaseKernelTest {

    /**
     * Creates a new instance of TestConnectionRetainMode
     */
    public TestConnectionRetainMode() {
    }

    public TestConnectionRetainMode(String str) {
        super(str);
    }

    public void testOnDemand()
        throws Exception {
        doTest("on-demand");
    }

    public void testTransaction()
        throws Exception {
        doTest("transaction");
    }

    public void testPersistenceManager()
        throws Exception {
        doTest("persistence-manager");
    }

    public void doTest(String mode)
        throws Exception {
        Map props = new HashMap();
        props.put("openjpa.ConnectionRetainMode", mode);

        OpenJPAEntityManagerFactory factory = getEmf(props);
        OpenJPAEntityManager pm =
            (OpenJPAEntityManager) factory.createEntityManager();
        Object initialConnection = pm.getConnection();

        try {
            checkPM(pm, initialConnection, mode);

            pm.setOptimistic(true);
            startTx(pm);

            Object optimisticConnection = pm.getConnection();
            checkPM(pm, initialConnection, mode);
            checkTransaction(pm, optimisticConnection, mode);

            rollbackTx(pm);
            checkPM(pm, initialConnection, mode);

            pm.setOptimistic(false);
            startTx(pm);

            Object pessimisticConnection = pm.getConnection();
            checkPM(pm, initialConnection, mode);
            checkTransaction(pm, pessimisticConnection, mode);

            rollbackTx(pm);
            checkPM(pm, initialConnection, mode);
        } finally {

            rollbackTx(pm);
            endEm(pm);
        }
    }

    private void checkPM(OpenJPAEntityManager pm, Object c, String mode)
        throws Exception {
        if ("persistence-manager".equals(mode))
            assertEquals(c, pm.getConnection());
    }

    private void checkTransaction(OpenJPAEntityManager pm, Object c,
        String mode)
        throws Exception {
        if (!"on-demand".equals(mode)
            || !pm.getOptimistic())
            assertEquals(c, pm.getConnection());
    }
}
