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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.annotations.common.apps.annotApp.annotype.
        PartyId;
import org.apache.openjpa.persistence.annotations.common.apps.annotApp.annotype.
        Site;
import org.apache.openjpa.persistence.annotations.common.apps.annotApp.annotype.
        Site1;
import org.apache.openjpa.persistence.annotations.common.apps.annotApp.annotype.
        Store;
import org.apache.openjpa.persistence.annotations.common.apps.annotApp.annotype.
        Store1;

public class TestMappedSuperClass extends AnnotationTestCase {

    public static Long pkey = new Long(1500);
    public static String pstr = "TestParty";
    public static int ikey = 1501;
    public static PartyId pid = new PartyId(pstr, ikey);
    
    public TestMappedSuperClass(String name) {
        super(name, "annotationMappedSuperclassApp");
    }

    public void setUp() {
        deleteAll(Store.class);
        deleteAll(Site.class);
        deleteAll(Site1.class);
        deleteAll(Store1.class);
    }

    public void testMappedSuperClassSameKeys() {
        createSite();
        createStore();
    }

    public void testMappedSuperClassIdClass() {
        createSite1();
        createStore1();
    }

    private void createSite() {
        OpenJPAEntityManager em =(OpenJPAEntityManager) currentEntityManager();
        boolean persistSuccess = true;
        try{
            startTx(em);
    
            Site s = new Site();
            s.setPartyId(pkey);
            s.setSiteName("San Jose");
            s.setSiteDescription("San Jose site");
            s.setStatus("2");
            s.setArchiveStatus("2");
            s.setCreateDate(new Date());
    
            em.persist(s);
    
            endTx(em);
        }catch(Exception e) {
            persistSuccess = false;
        }finally{
            assertTrue(persistSuccess);
        }
        endEm(em);
    }

    private void createStore() {
        OpenJPAEntityManager em =(OpenJPAEntityManager) currentEntityManager();
        boolean persistSuccess = true;
        try{
            startTx(em);
    
            Site site = em.find(Site.class, pkey);
    
            Store store = new Store();
            store.setPartyId(pkey);
            store.setStoreDescription("storeDescription");
            store.setStoreName("storeName");
            store.setStatus("1");
            store.setArchiveStatus("1");
            store.setCreateDate(new Date());
            store.setSiteId(site.getPartyId());
            store.setSite(site);
    
            List<Store> stores = new ArrayList<Store>();
            stores.add(store);
            site.setStores(stores);
    
            em.persist(store);
            endTx(em);
        }catch(Exception e) {
            persistSuccess = false;
        }finally {
            assertTrue(persistSuccess);
        }
        endEm(em);
    }

    private void createSite1() {
        OpenJPAEntityManager em =(OpenJPAEntityManager) currentEntityManager();
        boolean persistSuccess = true;
        try{
            startTx(em);
    
            Site1 s = new Site1();
            s.setId(ikey);
            s.setPartyName(pstr);
            s.setSiteName("San Jose");
            s.setSiteDescription("San Jose site");
            s.setStatus("2");
            s.setArchiveStatus("2");
            s.setCreateDate(new Date());
    
            em.persist(s);
    
            endTx(em);
        }catch(Exception e) {
            persistSuccess = false;
        }finally {
            assertTrue(persistSuccess);
        }
        endEm(em);
    }

    private void createStore1() {
        OpenJPAEntityManager em =(OpenJPAEntityManager) currentEntityManager();
        boolean persistSuccess = true;
        try{
            startTx(em);
    
            Site1 site = em.find(Site1.class, pid);
    
            Store1 store = new Store1();
            store.setId(ikey);
            store.setPartyName(pstr);
            store.setStoreDescription("storeDescription");
            store.setStoreName("storeName");
            store.setStatus("1");
            store.setArchiveStatus("1");
            store.setCreateDate(new Date());
            store.setSite(site);
    
            List<Store1> stores = new ArrayList<Store1>();
            stores.add(store);
            site.setStores(stores);
    
            em.persist(store);
            endTx(em);
    }catch(Exception e) {
        persistSuccess = false;
    }finally {
        assertTrue(persistSuccess);
    }
        endEm(em);
    }
}
