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

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import javax.persistence.EntityManager;


import org.apache.openjpa.persistence.kernel.common.apps.RuntimeTest1;
import org.apache.openjpa.persistence.kernel.common.apps.RuntimeTest2;
import org.apache.openjpa.persistence.kernel.common.apps.RuntimeTest3;
import org.apache.openjpa.persistence.common.utils.AbstractTestCase;

public class TestEJBLocales extends AbstractTestCase {

    public TestEJBLocales(String name) {
        super(name, "kernelcactusapp");
    }

    public void setUp() throws Exception {
        super.setUp(RuntimeTest1.class, RuntimeTest2.class, RuntimeTest3.class);
    }

    public void testLocales() {
        EntityManager pm = currentEntityManager();
        startTx(pm);
        RuntimeTest1 t1 = new RuntimeTest1(1);
        t1.setLocaleField(new Locale(Locale.FRANCE.getCountry(),
            Locale.FRENCH.getLanguage()));
        pm.persist(t1);
        pm.persist(new RuntimeTest1(2));
        endTx(pm);
        endEm(pm);

        pm = currentEntityManager();
        List c = findAll(RuntimeTest1.class, pm);
        assertEquals(2, c.size());

        boolean foundNull = false;
        boolean foundFrance = false;
        Locale locale;

        for (Iterator iter = c.iterator(); iter.hasNext();) {
            t1 = (RuntimeTest1) iter.next();
            locale = t1.getLocaleField();
            if (locale == null)
                foundNull = true;
            else if (
                (locale.getCountry().equals(Locale.FRANCE.getCountry())) &&
                    (locale.getLanguage().equals(Locale.FRANCE.getLanguage())))
                foundFrance = true;
        }

        assertTrue(foundNull);
        assertTrue(foundFrance);
        endEm(pm);
    }

    public List findAll(Class c, EntityManager em) {
        List l = em.createQuery("Select object(o) from RuntimeTest1 o")
            .getResultList();
        return l;
    }
}
