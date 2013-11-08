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

import java.util.HashMap;
import java.util.Map;


import org.apache.openjpa.persistence.event.common.apps.RuntimeTest1;
import org.apache.openjpa.persistence.test.AllowFailure;
import org.apache.openjpa.persistence.common.utils.AbstractTestCase;
import org.apache.openjpa.persistence.common.utils.BufferedLogFactory;

import org.apache.openjpa.event.OrphanedKeyAction;
import org.apache.openjpa.persistence.EntityNotFoundException;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactory;
import org.apache.openjpa.persistence.OpenJPAEntityManagerSPI;
import org.apache.openjpa.util.OpenJPAException;

/**
 * <p>Test the {@link OrphanedKeyAction} plugin.</p>
 *
 * @author Abe White
 */
@AllowFailure(message="surefire excluded")
public class TestOrphanedKeyAction
    extends AbstractTestCase {

    private Object _oid = null;

    public TestOrphanedKeyAction(String s) {
        super(s, "eventcactusapp");
    }

    public void setUp() {
        deleteAll(RuntimeTest1.class);

        RuntimeTest1 pc = new RuntimeTest1();
        pc.setIntField(1);
        RuntimeTest1 pc2 = new RuntimeTest1();
        pc2.setIntField(2);
        pc.setSelfOneOne(pc2);

        OpenJPAEntityManager pm =
            (OpenJPAEntityManager) currentEntityManager();
        startTx(pm);
        pm.persist(pc);
        endTx(pm);
        _oid = pm.getObjectId(pc);

        startTx(pm);
        pm.remove(pc2);
        endTx(pm);
        endEm(pm);
    }

    public void testLog() {
        Map map = getOrphanedKeyConfiguration("log(Level=FATAL)");

        OpenJPAEntityManagerFactory pmf = getEmf(map);
        OpenJPAEntityManager pm = pmf.createEntityManager();

        BufferedLogFactory logFactory = (BufferedLogFactory)
            ((OpenJPAEntityManagerSPI) pm).getConfiguration().getLogFactory();
        logFactory.clear();

        RuntimeTest1 pc = (RuntimeTest1) pm.find(RuntimeTest1.class, _oid);
        logFactory.assertNoLogMessage("*orphan*");
        assertNull(pc.getSelfOneOne());
        logFactory.assertLogMessage("*orphan*");
        endEm(pm);
        pmf.close();
    }

    public void testException() {
        Map map = getOrphanedKeyConfiguration("exception");
        OpenJPAEntityManagerFactory pmf = getEmf(map);

        OpenJPAEntityManager pm = pmf.createEntityManager();

        BufferedLogFactory logFactory = (BufferedLogFactory)
            ((OpenJPAEntityManagerSPI) pm).getConfiguration().getLogFactory();
        logFactory.clear();

        RuntimeTest1 pc = (RuntimeTest1) pm.find(RuntimeTest1.class, _oid);
        try {
            pc.getSelfOneOne();
            fail("Did not throw expected exception for orphaned key.");
        } catch (EntityNotFoundException onfe) {
            logFactory.assertNoLogMessage("*orphan*");
        } catch (OpenJPAException ke) {
            bug(1138, ke, "Wrong exception type.");
        }
        endEm(pm);
        pmf.close();
    }

    public void testNone() {
        Map map = getOrphanedKeyConfiguration("none");
        OpenJPAEntityManagerFactory pmf = getEmf(map);
        OpenJPAEntityManager pm = pmf.createEntityManager();

        BufferedLogFactory logFactory = (BufferedLogFactory)
            ((OpenJPAEntityManagerSPI) pm).getConfiguration().getLogFactory();
        logFactory.clear();

        RuntimeTest1 pc = (RuntimeTest1) pm.find(RuntimeTest1.class, _oid);
        assertNull(pc.getSelfOneOne());
        logFactory.assertNoLogMessage("*orphan*");
        endEm(pm);
        pmf.close();
    }

    Map getOrphanedKeyConfiguration(String orphanKeyAction) {
        Map map = new HashMap();
        map.put("openjpa.Log", BufferedLogFactory.class.getName());
        if (orphanKeyAction != null)
            map.put("openjpa.OrphanedKeyAction", orphanKeyAction);
        return map;
    }
}
