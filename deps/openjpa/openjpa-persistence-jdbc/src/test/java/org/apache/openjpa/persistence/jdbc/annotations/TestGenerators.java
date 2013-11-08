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
package org.apache.openjpa.persistence.jdbc.annotations;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.enhance.ClassRedefiner;
import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Test for generators
 *
 * @author Steve Kim
 */
public class TestGenerators extends SingleEMFTestCase {
    Log _log;
    public void setUp()
        throws Exception {
        setUp(Generator.class, CLEAR_TABLES);
        _log = emf.getConfiguration().getLog(OpenJPAConfiguration.LOG_RUNTIME);
    }

    public void testGet() {
        if (!PersistenceCapable.class.isAssignableFrom(Generator.class)
            && !ClassRedefiner.canRedefineClasses(_log))
            fail("This test requires a higher level of enhancement than"
                + " is available in the current environment.");

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        Generator g = new Generator();
        g.setPk(5);
        em.persist(g);
        assertPks(g);
        em.getTransaction().commit();
        em.close();
        assertNew();
    }

    public void testFlush() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        Generator g = new Generator();
        g.setPk(5);
        em.persist(g);
        em.flush();
        assertPks(g);
        em.getTransaction().commit();
        em.close();
        assertNew();
    }

    public void testCommit() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        Generator g = new Generator();
        g.setPk(5);
        em.persist(g);
        em.getTransaction().commit();
        em.close();
        assertNew();
    }

    private void assertNew() {
        EntityManager em = emf.createEntityManager();
        Query q = em.createQuery("select g from Generator g where "
            + "g.stringField = 'foo'");
        for (Object o : q.getResultList())
            assertPks((Generator) o);
        em.close();
    }

    private void assertPks(Generator g) {
        assertNotEquals(0, g.getPk());
        assertNotNull(g.getPk2());
        assertNotEquals(new Integer(0), g);
        assertNotEquals(0, g.getPk3());
    }
}
