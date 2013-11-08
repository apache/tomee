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
package org.apache.openjpa.event;

import java.util.HashMap;
import java.util.Map;
import java.util.Collections;
import javax.persistence.Persistence;

import org.apache.openjpa.persistence.test.AbstractCachedEMFTestCase;
import org.apache.openjpa.persistence.OpenJPAPersistence;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.apache.openjpa.persistence.JPAFacadeHelper;

public class TestSJVMRemoteCommitProvider
extends AbstractCachedEMFTestCase {

    private OpenJPAEntityManagerFactorySPI emf1;
    private OpenJPAEntityManagerFactorySPI emf2;
    private ListenerImpl listen1;
    private ListenerImpl listen2;

    public void setUp() {
        Map sjvm1 = new HashMap();
        sjvm1.put("openjpa.RemoteCommitProvider", "sjvm");
        // set this to differentiate emf1 from the other emf below
        sjvm1.put("openjpa.DetachState", "true");
        emf1 = (OpenJPAEntityManagerFactorySPI) OpenJPAPersistence.cast(
            Persistence.createEntityManagerFactory("test", sjvm1));
        emf1.getConfiguration().getRemoteCommitEventManager().addListener(
            listen1 = new ListenerImpl());

        Map sjvm2 = new HashMap();
        sjvm2.put("openjpa.RemoteCommitProvider", "sjvm");
        sjvm2.put("openjpa.DetachState", "false"); // differentiate from above
        emf2 = (OpenJPAEntityManagerFactorySPI) OpenJPAPersistence.cast(
            Persistence.createEntityManagerFactory("test", sjvm2));
        emf2.getConfiguration().getRemoteCommitEventManager().addListener(
            listen2 = new ListenerImpl());
    }

    public void tearDown() throws Exception {
        super.tearDown();
        clear(emf1);
        closeEMF(emf1);
        clear(emf2);
        closeEMF(emf2);
    }
        
    public void testSJVMRemoteCommitProvider() {
        assertNotSame(JPAFacadeHelper.toBrokerFactory(emf1),
            JPAFacadeHelper.toBrokerFactory(emf2));
        
        emf1.getConfiguration().getRemoteCommitEventManager()
            .getRemoteCommitProvider().broadcast(
                new RemoteCommitEvent(RemoteCommitEvent.PAYLOAD_OIDS,
                    Collections.EMPTY_SET,
                    Collections.EMPTY_SET,
                    Collections.EMPTY_SET,
                    Collections.EMPTY_SET));

        assertEquals(0, listen1.eventCount);
        assertEquals(1, listen2.eventCount);
    }

    private class ListenerImpl
        implements RemoteCommitListener {

        private int eventCount;

        public void afterCommit(RemoteCommitEvent event) {
            eventCount++;
        }

        public void close() {
        }
    }
}
