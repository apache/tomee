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
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.apache.openjpa.persistence.StoreCache;
import org.apache.openjpa.persistence.test.AbstractCachedEMFTestCase;

public class TestCacheExclusions extends AbstractCachedEMFTestCase {

    private OpenJPAEntityManagerFactorySPI emf = null;

    private static String[] ITEM_NAMES =
        { "Cup", "pen", "pencil", "phone", "laptop", "keyboard", "mouse" };
    
    private static final String _tSep = ";";

    Item[] items = new Item[ITEM_NAMES.length];
    Order o1, o2;
    Purchase p;

    public void populate() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        int n = 0;
        for (String s : ITEM_NAMES) {
            items[n] = new Item();
            items[n].setName(s);
            items[n].setId(n);
            em.persist(items[n++]);
        }
        p = new Purchase();
        p.setOrders(new ArrayList<Order>());
        o1 = new Order();
        o1.setItem(em.find(Item.class, 1));
        o1.setQuantity(2);
        o1.setPurchase(p);
        p.getOrders().add(o1);

        o2 = new Order();
        o2.setItem(em.find(Item.class, 4));
        o2.setQuantity(23);
        o2.setPurchase(p);
        p.getOrders().add(o2);

        em.persist(p);
        em.getTransaction().commit();
        em.close();
    }

    public void tearDown() throws Exception {
        if (emf != null) {
            EntityManager em = emf.createEntityManager();
            em.getTransaction().begin();

            for (ClassMapping mapping : ((ClassMapping[]) emf
                .getConfiguration().getMetaDataRepositoryInstance()
                .getMetaDatas())) {
                Query q =
                    em.createNativeQuery("DROP TABLE "
                        + mapping.getTable().getName());
                q.executeUpdate();
            }
            em.getTransaction().commit();
            em.close();
            
            emf.close();
        }
        emf = null;
        super.tearDown();
    }

    public void testCacheAll() {
        getEntityManagerFactoryCacheSettings(null, null);
        populate();
        StoreCache cache = emf.getStoreCache();
        assertCacheContents(cache, true, true, true);
    }

    public void testCacheItems() {
        getEntityManagerFactoryCacheSettings(new Class[] { Item.class }, null);
        populate();
        StoreCache cache = emf.getStoreCache();
        assertCacheContents(cache, false, false, true);
    }

    public void testCacheItemsAndPurchases() {
        getEntityManagerFactoryCacheSettings(new Class[] { Item.class,
            Purchase.class }, null);
        populate();
        StoreCache cache = emf.getStoreCache();
        assertCacheContents(cache, true, false, true);
    }

    public void testCacheItemsAndOrders() {
        getEntityManagerFactoryCacheSettings(new Class[] { Item.class,
            Order.class }, null);
        populate();
        StoreCache cache = emf.getStoreCache();
        assertCacheContents(cache, false, true, true);
    }

    public void testCachePurchasesAndOrders() {
        getEntityManagerFactoryCacheSettings(new Class[] { Purchase.class,
            Order.class }, null);
        populate();
        StoreCache cache = emf.getStoreCache();
        assertCacheContents(cache, true, true, false);
    }

    public void testExcludePurchases() {
        getEntityManagerFactoryCacheSettings(null,
            new Class[] { Purchase.class });
        populate();
        StoreCache cache = emf.getStoreCache();
        assertCacheContents(cache, false, true, true);
    }

    public void testExcludeOrders() {
        getEntityManagerFactoryCacheSettings(null, new Class[] { Order.class });
        populate();
        StoreCache cache = emf.getStoreCache();
        assertCacheContents(cache, true, false, true);
    }

    public void testExcludeItems() {
        getEntityManagerFactoryCacheSettings(null, new Class[] { Item.class });
        populate();
        StoreCache cache = emf.getStoreCache();
        assertCacheContents(cache, true, true, false);
    }

    public void testExcludeOrdersAndPurchases() {
        getEntityManagerFactoryCacheSettings(null, new Class[] { Order.class,
            Purchase.class });
        populate();
        StoreCache cache = emf.getStoreCache();
        assertCacheContents(cache, false, false, true);
    }

    public void testIncludePurchaseItemExcludePurchase() {
        try{
        getEntityManagerFactoryCacheSettings(new Class[] { Purchase.class,
            Item.class }, new Class[] { Purchase.class });
        populate();
            fail("Shouldn't be able to create an EMF with an entity in both Types and ExcludedTypes");
        StoreCache cache = emf.getStoreCache();
        assertCacheContents(cache, false, false, true);
        }catch(Exception e){
            //expected
        }
    }

    public OpenJPAEntityManagerFactorySPI getEntityManagerFactoryCacheSettings(
        Class<?>[] includedTypes, Class<?>[] excludedTypes) {
        StringBuilder includes = new StringBuilder();
        if (includedTypes != null && includedTypes.length > 0) {
            includes.append("Types=");
            for (Class<?> c : includedTypes) {
                includes.append(c.getName());
                includes.append(_tSep);
            }
            includes.setLength(includes.length() - 1); // remove last semicolon
        }
        StringBuilder excludes = new StringBuilder();
        if (excludedTypes != null && excludedTypes.length > 0) {
            excludes.append("ExcludedTypes=");
            for (Class<?> c : excludedTypes) {
                excludes.append(c.getName());
                excludes.append(_tSep);
            }
            excludes.setLength(excludes.length() - 1); // remove last semicolon
        }
        StringBuilder dataCacheSettings = new StringBuilder();
        boolean hasIncludeOrExclude = includes.length() > 0 || excludes.length() > 0;
        dataCacheSettings.append("true" + (hasIncludeOrExclude ? "(" : ""));
        if (hasIncludeOrExclude) {
            dataCacheSettings.append(includes);
            if (includes.length() > 0 && excludes.length() > 0) 
                dataCacheSettings.append(",");
            dataCacheSettings.append(excludes);
            dataCacheSettings.append(")");
        }
        Map<String, String> props = new HashMap<String, String>();
        props.put("openjpa.DataCache", dataCacheSettings.toString());
        props.put("openjpa.RemoteCommitProvider", "sjvm");
        props.put("openjpa.MetaDataFactory", "jpa(Types="
            + Item.class.getName() + _tSep + Purchase.class.getName() + _tSep
            + Order.class.getName() + ")");
        emf =
            (OpenJPAEntityManagerFactorySPI) javax.persistence.Persistence
                .createEntityManagerFactory("test", props);
        return emf;
    }

    public void assertCacheContents(StoreCache cache, boolean expectPurchase,
        boolean expectOrders, boolean expectItems) {
        assertEquals("Expected purchases to " + (expectPurchase ? "" : "not ")
            + "exist in the cache", expectPurchase, cache.contains(
            Purchase.class, p.getId()));
        assertEquals("Expected Orders to " + (expectOrders ? "" : "not ")
            + "exist in the cache", expectOrders, cache.contains(Order.class,
            o1.getId()));
        assertEquals("Expected Orders to " + (expectOrders ? "" : "not ")
            + "exist in the cache", expectOrders, cache.contains(Order.class,
            o2.getId()));
        for (int i = 0; i < ITEM_NAMES.length; i++) {
            assertEquals("Expected Items to " + (expectItems ? "" : "not ")
                + "exist in the cache", expectItems, cache.contains(Item.class,
                items[i].getId()));
        }
    }
}
