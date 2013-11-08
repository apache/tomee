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
package org.apache.openjpa.persistence.detach;

import org.apache.openjpa.conf.Compatibility;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.test.SQLListenerTestCase;

public class TestDetachReloadProp extends SQLListenerTestCase {
    IntVersionEntity intVer;
    TimestampVersionEntity tsVer;
    NoVersionEntity noVer;
    int id;
    Compatibility compat;
    
    public void setUp() {
        setUp(org.apache.openjpa.persistence.detach.IntVersionEntity.class, 
            org.apache.openjpa.persistence.detach.TimestampVersionEntity.class,
            org.apache.openjpa.persistence.detach.NoVersionEntity.class);
        compat = emf.getConfiguration().getCompatibilityInstance();
        id++;
        create(id);
        persist();
    }
    
    private void create(int id) {
        intVer = new IntVersionEntity(id);
        intVer.setName("xxx");
        tsVer = new TimestampVersionEntity(id);
        tsVer.setName("yyy");
        intVer.setE2(tsVer);
        noVer = new NoVersionEntity(id);
        noVer.setName("zzz");
    }
    
    private void persist() {
        OpenJPAEntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(noVer);
        em.persist(intVer); // also persists referenced tsVer
        em.getTransaction().commit();
        em.close();
        em = null;
    }
    
    public void testReloadTrue() {
        compat.setReloadOnDetach(true);
        detachProcessing();
    }
    
    public void testReloadFalse() {
        compat.setReloadOnDetach(false);
        detachProcessing();
    }
    
    private void detachProcessing() {
        // Detach individual entities explicitly
        OpenJPAEntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        intVer = em.find(IntVersionEntity.class, id);
        tsVer = em.find(TimestampVersionEntity.class, id);
        noVer = em.find(NoVersionEntity.class, id);
        em.detach(intVer);
        em.detach(tsVer);
        em.detach(noVer);
        em.getTransaction().commit();
        em.close();
        
        // Detach all internal implicitly with close()
        em = emf.createEntityManager();
        em.getTransaction().begin();
        intVer = em.find(IntVersionEntity.class, id);
        tsVer = em.find(TimestampVersionEntity.class, id);
        noVer = em.find(NoVersionEntity.class, id);
        em.getTransaction().commit();
        em.close();
        
    }
}
