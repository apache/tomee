/*
 * TestOpenResultsCommit.java
 *
 * Created on October 12, 2006, 2:43 PM
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
import java.util.Iterator;
import java.util.Map;
import java.util.EnumSet;

import org.apache.openjpa.persistence.kernel.common.apps.RuntimeTest1;
import org.apache.openjpa.persistence.common.utils.AbstractTestCase;

import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactory;

public class TestOpenResultsCommit extends BaseKernelTest {

    /**
     * Creates a new instance of TestOpenResultsCommit
     */
    public TestOpenResultsCommit() {
    }

    public TestOpenResultsCommit(String testName) {
        super(testName);
    }

    public void setUp()
        throws Exception {
        super.setUp();

        deleteAll(RuntimeTest1.class);

        OpenJPAEntityManager pm = getPM();
        startTx(pm);
        for (int i = 0; i < 50; i++)
            pm.persist(new RuntimeTest1("open results #" + i, i));
        endTx(pm);
        endEm(pm);
    }

    public void testCommitWithModeTransaction() {
        try {
            testCommitWithOpenResults("transaction");
        } catch (Exception e) {
            bug(EnumSet.of(AbstractTestCase.Platform.POINTBASE,
                AbstractTestCase.Platform.INFORMIX,
                AbstractTestCase.Platform.EMPRESS), 718, e,
                "Cannot keep results open across commit");
        }
    }

    public void testCommitWithModeOpenJPAEntityManager() {
        try {
            testCommitWithOpenResults("persistence-manager");
        } catch (Exception e) {
            bug(EnumSet.of(AbstractTestCase.Platform.POINTBASE,
                AbstractTestCase.Platform.INFORMIX,
                AbstractTestCase.Platform.EMPRESS), 718, e,
                "Cannot keep results open across commit");
        }
    }

    public void testCommitWithModeOnDemand() {
        try {
            testCommitWithOpenResults("on-demand");
        } catch (Exception e) {
            bug(EnumSet.of(AbstractTestCase.Platform.POINTBASE,
                AbstractTestCase.Platform.INFORMIX,
                AbstractTestCase.Platform.EMPRESS), 718, e,
                "Cannot keep results open across commit");
        }
    }

    private void testCommitWithOpenResults(String crm) {
        Map props = new HashMap();
        props.put("openjpa.DefaultFetchThreshold", 1 + "");
        props.put("openjpa.ConnectionRetainMode", crm);

        OpenJPAEntityManagerFactory pmf =
            (OpenJPAEntityManagerFactory) getEmf(props);
        OpenJPAEntityManager pm = pmf.createEntityManager();

        startTx(pm);
        Iterator results =
            pm.createExtent(RuntimeTest1.class, false).iterator();
        results.next();
        ((RuntimeTest1) results.next()).setStringField("changed name");
        endTx(pm);
        while (results.hasNext())
            results.next();
        endEm(pm);
    }
}
