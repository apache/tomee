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
package org.apache.openjpa.persistence.event;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


import org.apache.openjpa.persistence.event.common.apps.RuntimeTest1;
import org.apache.openjpa.persistence.test.AllowFailure;
import org.apache.openjpa.persistence.common.utils.AbstractTestCase;

import org.apache.openjpa.event.AbstractRemoteCommitProvider;
import org.apache.openjpa.event.RemoteCommitEvent;
import org.apache.openjpa.event.RemoteCommitListener;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactory;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.apache.openjpa.util.Id;

@AllowFailure(message="surefire excluded")
public class TestFakeRemoteEvents extends AbstractTestCase {

    /*
      * The most recently set provider, and a lock to control access to it. This
      * is rather hacky.
      */
    private static Object currentProviderLock = new Object();

    private static RemoteCommitProviderTestImpl currentProvider;

    public TestFakeRemoteEvents(String s) {
        super(s, "eventcactusapp");
    }

    public void setUp() {
        deleteAll(RuntimeTest1.class);
    }

    public void testListener() {

        Map propsMap = new HashMap();
        propsMap.put("openjpa.RemoteCommitProvider", "sjvm");
        propsMap.put("openjpa.DataCache", "true");
        OpenJPAEntityManagerFactory factory = getEmf(propsMap);

        RemoteCommitListenerTestImpl transactionListener =
            new RemoteCommitListenerTestImpl();
        ((OpenJPAEntityManagerFactorySPI) factory).getConfiguration()
            .getRemoteCommitEventManager().addListener(
            transactionListener);

        OpenJPAEntityManager pm = (OpenJPAEntityManager) factory
            .createEntityManager();

        // get an object id
        RuntimeTest1 t1 = new RuntimeTest1("foo", 5);
        startTx(pm);
        pm.persist(t1);
        Object oid = pm.getObjectId(t1);
        rollbackTx(pm);

        // simulate an add
        Set s = new HashSet();
        s.add(oid);
        ((OpenJPAEntityManagerFactorySPI) factory).getConfiguration()
            .getRemoteCommitEventManager().fireEvent(
            new RemoteCommitEvent(RemoteCommitEvent.PAYLOAD_OIDS_WITH_ADDS,
                s, null, null, null));

        boolean pass = false;
        for (Iterator iter = transactionListener.added.iterator(); iter
            .hasNext();) {
            if (iter.next().equals(oid)) {
                pass = true;
                break;
            }
        }
        assertTrue(pass);
        assertTrue(transactionListener.updated.size() == 0);
        assertTrue(transactionListener.deleted.size() == 0);

        // simulate modifications
        ((OpenJPAEntityManagerFactorySPI) factory).getConfiguration()
            .getRemoteCommitEventManager().fireEvent(
            new RemoteCommitEvent(RemoteCommitEvent.PAYLOAD_OIDS_WITH_ADDS,
                null, null, s, null));

        pass = false;
        for (Iterator iter = transactionListener.updated.iterator(); iter
            .hasNext();) {
            if (iter.next().equals(oid)) {
                pass = true;
                break;
            }
        }
        assertTrue(pass);
        assertTrue(transactionListener.added.size() == 0);
        assertTrue(transactionListener.deleted.size() == 0);

        // simulate a delete
        ((OpenJPAEntityManagerFactorySPI) factory).getConfiguration()
            .getRemoteCommitEventManager().fireEvent(
            new RemoteCommitEvent(RemoteCommitEvent.PAYLOAD_OIDS_WITH_ADDS,
                null, null, null, s));

        pass = false;
        for (Iterator iter = transactionListener.deleted.iterator(); iter
            .hasNext();) {
            if (iter.next().equals(oid)) {
                pass = true;
                break;
            }
        }
        assertTrue(pass);
        assertTrue(transactionListener.added.size() == 0);
        assertTrue(transactionListener.updated.size() == 0);
    }

    public void testProvider() {
        RemoteCommitProviderTestImpl provider;
        OpenJPAEntityManager pm;
        synchronized (currentProviderLock) {
            Map propsMap = new HashMap();
            propsMap.put("openjpa.RemoteCommitProvider",
                RemoteCommitProviderTestImpl.class.getName()
                    + "(TransmitPersistedObjectIds=true)");
            propsMap.put("openjpa.DataCache", "true");
            OpenJPAEntityManagerFactory factory = getEmf(propsMap);

            pm = (OpenJPAEntityManager) factory.createEntityManager();
            provider = currentProvider;
        }

        // get an object id
        RuntimeTest1 t1 = new RuntimeTest1("foo", 5);
        startTx(pm);
        pm.persist(t1);
        Object oid = pm.getObjectId(t1);
        endTx(pm);

        boolean pass = false;
        for (Iterator iter = provider.added.iterator(); iter.hasNext();) {
            Object added = iter.next();
            if (equals(added, oid)) {
                pass = true;
                break;
            }
        }
        assertTrue(pass);
        assertTrue(provider.updated.size() == 0);
        assertTrue(provider.deleted.size() == 0);
    }

    boolean equals(Object added, Object oid) {
        if (added.equals(oid))
            return true;
        if (added instanceof Id)
            return ((Id) added).getIdObject().equals(oid);
        return false;
    }

    private static class RemoteCommitListenerTestImpl implements
        RemoteCommitListener {

        transient Collection added;

        transient Collection updated;

        transient Collection deleted;

        public void afterCommit(RemoteCommitEvent event) {
            this.added = event.getPersistedObjectIds();
            this.updated = event.getUpdatedObjectIds();
            this.deleted = event.getDeletedObjectIds();
        }

        public void close() {
        }
    }

    public static class RemoteCommitProviderTestImpl extends
        AbstractRemoteCommitProvider {

        Collection added;

        Collection updated;

        Collection deleted;

        public RemoteCommitProviderTestImpl() {
            synchronized (currentProviderLock) {
                currentProvider = this;
            }
        }

        // ---------- RemoteCommitProvider implementation ----------

        public void broadcast(RemoteCommitEvent event) {
            this.added = event.getPersistedObjectIds();
            this.updated = event.getUpdatedObjectIds();
            this.deleted = event.getDeletedObjectIds();
        }

        public void close() {
        }
    }
}
