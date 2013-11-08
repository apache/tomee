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
package org.apache.openjpa.persistence.datacache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;


import org.apache.openjpa.persistence.datacache.common.apps.AttachA;
import org.apache.openjpa.persistence.datacache.common.apps.AttachB;
import org.apache.openjpa.persistence.datacache.common.apps.AttachC;
import org.apache.openjpa.persistence.datacache.common.apps.AttachD;
import org.apache.openjpa.persistence.datacache.common.apps.AttachE;
import org.apache.openjpa.persistence.common.utils.AbstractTestCase;
import org.apache.openjpa.persistence.OpenJPAEntityManager;

/**
 * Tests load on the cache. To run:
 * <p/>
 * java -Dkodo.properties=hsql.properties -Dcachetest.threads=30
 * -Dcachetest.iterations=1000 kodo.datacache.CacheLoadTest
 *
 * @author <a href="mailto:marc@solarmetric.com">Marc Prud'hommeaux</a>
 */
public class CacheLoadTest
    extends AbstractTestCase {

    private EntityManagerFactory emf;
    protected int threadCount =
        Integer.getInteger("cachetest.threads", 30).intValue();
    protected int interationCount =
        Integer.getInteger("cachetest.iterations", 1000).intValue();

    @Override
    public void setUp() throws Exception {
        super.setUp();
        Map propsMap = new HashMap();
        propsMap.put("openjpa.DataCache", "true");
        propsMap.put("openjpa.RemoteCommitProvider", "sjvm");
        propsMap.put("openjpa.QueryCache", "true");
        emf = getEmf(propsMap);
    }

    @Override
    public void tearDown() throws Exception {
        closeEMF(emf);
        emf = null;
        super.tearDown();
    }
    
    public void testCacheLoad()
        throws Exception {
        mttest(Integer.getInteger("cachetest.threads", 30).intValue(),
            Integer.getInteger("cachetest.iterations", 1000).intValue());

        switch ((int) (Math.random() * 4)) {
            case 0:
                insert();
                break;
            case 1:
                query();
                break;
            case 2:
                delete();
                break;
            case 3:
                read();
                break;
        }
    }

    private int rnd(int num) {
        return randomInt().intValue() % num;
    }

    private void insert()
        throws Exception {

        EntityManager em = emf.createEntityManager();
        startTx(em);

        for (int i = 0; i < (rnd(100)); i++) {
            em.persist(randomizeBean(rndclass().newInstance()));
        }

        endTx(em);
        endEm(em);
    }

    private void query() {

        OpenJPAEntityManager em =
            (OpenJPAEntityManager) emf.createEntityManager();

        String[] filters = new String[]{
            "select from " + AttachA.class.getName() + " where aint > 0",
            "select from " + AttachA.class.getName() + " where aint < 0",
            "select from " + AttachB.class.getName() + " where aint > 0",
            "select from " + AttachB.class.getName() + " where aint < 0",
            "select from " + AttachC.class.getName() + " where aint > 0",
            "select from " + AttachC.class.getName() + " where aint < 0",
        };

        for (int i = 0; i < rnd(50); i++) {
            try {
                new ArrayList((Collection) em
                    .createQuery("org.apache.openjpa.kernel.jpql.JPQL",
                        filters[randomInt().intValue() % filters.length]).
                    getResultList());
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }

        endEm(em);
    }

    private void delete() {

        try {
            OpenJPAEntityManager em =
                (OpenJPAEntityManager) emf.createEntityManager();
            startTx(em);

            for (Iterator i = em.createExtent(rndclass(), Math.random() > 0.5f).
                iterator(); i.hasNext();) {
                Object o = i.next();
                if (Math.random() > 0.6f)
                    em.remove(o);
            }

            endTx(em);
            endEm(em);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void read() {

        OpenJPAEntityManager em =
            (OpenJPAEntityManager) emf.createEntityManager();

        for (Iterator i = em.createExtent(rndclass(), Math.random() > 0.5f).
            iterator(); i.hasNext();) {
            Object o = i.next();
        }

        endEm(em);
    }

    private Class rndclass() {
        Class[] c = new Class[]{
            AttachA.class,
            AttachB.class,
            AttachC.class,
            AttachD.class,
            AttachE.class,
        };

        return c[(int) (Math.random() * c.length)];
    }
}

