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
package org.apache.openjpa.conf;

import java.util.*;

import javax.persistence.*;

import junit.framework.*;

import org.apache.openjpa.persistence.*;

public class TestAutoDetachProperty extends TestCase {
    private EntityManager em;
    private EntityManagerFactory emf;

    public void setUp() throws Exception {
        // Don't modify system props, as we are trying to get as close as
        // possible to testing props in persistence.xml
        HashMap props = new HashMap(System.getProperties());
        props.put("openjpa.AutoDetach", "commit,close,nontx-read");
        emf = (OpenJPAEntityManagerFactory) Persistence
                .createEntityManagerFactory("test", props);

        em = emf.createEntityManager();
    }

    public void tearDown() throws Exception {
        em.close();
        em = null;
        emf.close();
        emf = null;
    }

    public void testIsAutoDetachingOnClose() {
        assertTrue("not autodetaching on close as expected",
                isAutoDetachingOnClose());
    }

    public void testIsAutoDetachingOnCommit() {
        assertTrue("not autodetaching on commit as expected",
                isAutoDetachingOnCommit());
    }

    public void testIsAutoDetachingOnNonTxRead() {
        assertTrue("not autodetaching on nontransactional read as expected",
                isAutoDetachingOnNonTxRead());
    }

    private boolean isAutoDetachingOnClose() {
        EnumSet<AutoDetachType> autoDetachFlags =
            OpenJPAPersistence.cast(em).getAutoDetach();
        return autoDetachFlags.contains(AutoDetachType.CLOSE);
    }

    private boolean isAutoDetachingOnCommit() {
        EnumSet<AutoDetachType> autoDetachFlags =
            OpenJPAPersistence.cast(em).getAutoDetach();
        return autoDetachFlags.contains(AutoDetachType.COMMIT);
    }

    private boolean isAutoDetachingOnNonTxRead() {
        EnumSet<AutoDetachType> autoDetachFlags =
            OpenJPAPersistence.cast(em).getAutoDetach();
        return autoDetachFlags.contains(AutoDetachType.NON_TRANSACTIONAL_READ);
    }
}
