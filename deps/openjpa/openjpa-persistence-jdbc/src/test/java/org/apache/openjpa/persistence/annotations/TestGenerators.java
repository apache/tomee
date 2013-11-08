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
package org.apache.openjpa.persistence.annotations;

import javax.persistence.*;

import
    org.apache.openjpa.persistence.annotations.common.apps.annotApp.annotype.*;

import org.apache.openjpa.persistence.OpenJPAEntityManager;

/**
 * Test for generators
 *
 * @author Steve Kim
 */
public class TestGenerators extends AnnotationTestCase
{

	public TestGenerators(String name)
	{
		super(name, "annotationcactusapp");
	}

    public void setUp()
        throws Exception {
        deleteAll(Generator.class);
    }

    public void testGet() {
        OpenJPAEntityManager em =(OpenJPAEntityManager) currentEntityManager();
        startTx(em);
        Generator g = new Generator();
        g.setPk(5);
        em.persist(g);
        assertPks(g);
        endTx(em);
        endEm(em);
        assertNew();
    }

    public void testFlush() {
        OpenJPAEntityManager em =(OpenJPAEntityManager) currentEntityManager();
        startTx(em);
        Generator g = new Generator();
        g.setPk(5);
        em.persist(g);
        em.flush();
        assertPks(g);
        endTx(em);
        endEm(em);
        assertNew();
    }

    public void testCommit() {
        OpenJPAEntityManager em =(OpenJPAEntityManager) currentEntityManager();
        startTx(em);
        Generator g = new Generator();
        g.setPk(5);
        em.persist(g);
        endTx(em);
        endEm(em);
        assertNew();
    }

    private void assertNew() {
        OpenJPAEntityManager em =(OpenJPAEntityManager) currentEntityManager();
        Query q = em.createQuery("select g from Generator g where "
            + "g.stringField = 'foo'");
        for (Object o : q.getResultList())
            assertPks((Generator) o);
        endEm(em);
    }

    private void assertPks(Generator g) {
        assertNotEquals(0, g.getPk());
        assertNotNull(g.getPk2());
       // assertNotEquals(new Integer(0), g);
        assertNotEquals(0, g.getPk3());
    }

    private boolean assertNotEquals(long n, long num)
    {
    	return(n != num);
    }
}
