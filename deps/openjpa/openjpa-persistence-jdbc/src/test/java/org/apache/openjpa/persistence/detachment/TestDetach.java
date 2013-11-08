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
package org.apache.openjpa.persistence.detachment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.openjpa.conf.Compatibility;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.MetaDataRepository;
import org.apache.openjpa.meta.ValueMetaData;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.detachment.model.DMCustomer;
import org.apache.openjpa.persistence.detachment.model.DMCustomerInventory;
import org.apache.openjpa.persistence.detachment.model.DMItem;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Tests detachment behavior according to JPA 2.0 Specification. The primary
 * changes in detachment behavior from the existing OpenJPA behavior are
 * i. detach(x) does not flush if x is dirty
 * ii. detach(x) removes x from persistence context
 * iii. detach(x) propagates via CascadeType.DETACH. It is not clear how that
 * impacts the detach graph. So currently, detach graph is same as 'loaded'.   
 * 
 * The test uses a 'domain model' with following cascade relation
 * 
 *            ALL                 PERSIST, MERGE, REFRESH
 * Customer --------> Inventory   ----------------------> Item
 *          <-------
 *            MERGE
 *            
 * @author Pinaki Poddar
 *
 */
public class TestDetach extends SingleEMFTestCase {
    OpenJPAEntityManager em;
    DMCustomer root;
    
    public void setUp() {
        super.setUp(DMCustomer.class, DMCustomerInventory.class, DMItem.class,
            CLEAR_TABLES);
        
        Compatibility compat = 
            emf.getConfiguration().getCompatibilityInstance();
        compat.setCopyOnDetach(false);
        compat.setFlushBeforeDetach(false);
        
        em = emf.createEntityManager();
        root = createData();
    }
    
    public void testDetachCascadeIsSet() {
        MetaDataRepository repos = emf.getConfiguration()
                                      .getMetaDataRepositoryInstance();
        ClassMetaData meta = repos.getCachedMetaData(DMCustomer.class);
        assertEquals(ValueMetaData.CASCADE_NONE,
                meta.getField("firstName").getCascadeDetach());
        assertEquals(ValueMetaData.CASCADE_IMMEDIATE, meta.getField(
                "customerInventories").getElement().getCascadeDetach());
        
        meta = repos.getCachedMetaData(DMCustomerInventory.class);
        assertEquals(ValueMetaData.CASCADE_NONE,
                meta.getField("customer").getCascadeDetach());
        assertEquals(ValueMetaData.CASCADE_NONE,
                meta.getField("item").getCascadeDetach());
        
    }
    
    public void testDetachRemovesEntityAndCascadedRelationFromContext() {
        em.getTransaction().begin();
        
        DMCustomer pc = em.find(DMCustomer.class, root.getId());
        List<DMCustomerInventory> inventories = pc.getCustomerInventories();
        DMItem item = inventories.get(0).getItem();
        
        assertNotDetached(pc);
        for (DMCustomerInventory i : inventories) assertNotDetached(i);
        assertNotDetached(item);   
        
        em.detach(pc);
        
        assertDetached(pc);
        for (DMCustomerInventory i : inventories) assertDetached(i);
        
        em.getTransaction().rollback();
        
        assertNotNull(pc.getFirstName());
    }
    
    public void testDetachingDirtyEntityDoesNotImplicitlyFlush() {
        em.getTransaction().begin();
        DMCustomer pc = em.find(DMCustomer.class, root.getId());
        String original = pc.getLastName();
        pc.setLastName("Changed That Should not be Saved");
 
        em.detach(pc);
        em.getTransaction().commit();
        
        DMCustomer pc2 = em.find(DMCustomer.class, root.getId());
        assertNotNull(pc2);
        assertEquals(original, pc2.getLastName());
    }
    
    public void testDetachingNewEntityIsIgnored() {
        em.getTransaction().begin();
        DMCustomer pc = em.find(DMCustomer.class, root.getId());
        List<DMCustomerInventory> inventories = pc.getCustomerInventories();
        
        DMCustomer newPC = new DMCustomer();
        newPC.setCustomerInventories(inventories);
        for (DMCustomerInventory inventory : inventories)
            inventory.setCustomer(newPC);
        
        em.detach(newPC);
        for (DMCustomerInventory inventory : inventories) {
            assertNotDetached(inventory);
        }
        em.getTransaction().rollback();
    }
    
    public void testDetachingDetachedEntityIsIgnored() {
        em.getTransaction().begin();
        DMCustomer pc = em.find(DMCustomer.class, root.getId());
        List<DMCustomerInventory> inventories = pc.getCustomerInventories();
        
        em.detach(pc);
        DMCustomer detached = pc;
        assertDetached(detached);
        for (DMCustomerInventory inventory : inventories) {
            assertDetached(inventory);
        }
        
        List<DMCustomerInventory> newInventories =
            new ArrayList<DMCustomerInventory>();
        newInventories.addAll(inventories);
        DMCustomerInventory newInventory = new DMCustomerInventory();
        newInventory.setCustomer(detached);
        newInventories.add(newInventory);
        detached.setCustomerInventories(newInventories);
        em.persist(newInventory);
        assertNotDetached(newInventory);
        
        em.detach(detached);
        assertDetached(detached);
        assertEquals(inventories.size()+1, newInventories.size());
        for (DMCustomerInventory inventory : newInventories) {
            if (inventory == newInventory)
                assertNotDetached(inventory);
            else
                assertDetached(inventory);
        }
        em.getTransaction().rollback();
    }
    
    
    public void testFlushingBeforeDetachingSavesChange() {
        
    }
    
    public void testManagedEntityContinuesToReferDetachedEntities() {
        em.getTransaction().begin();
        
        DMCustomer pc = em.find(DMCustomer.class, root.getId());
        List<DMCustomerInventory> inventories = pc.getCustomerInventories();
        DMItem item = inventories.get(1).getItem();
        
        em.detach(inventories.get(0));
        
        DMCustomerInventory attached0 = inventories.get(0);
        DMCustomerInventory attached1 = inventories.get(1);
        
        assertSame(pc.getCustomerInventories().get(0), attached0);
        assertSame(pc.getCustomerInventories().get(1), attached1);
        
        em.getTransaction().rollback();
    }
    
    DMCustomer createData() {
        DMItem item1 = new DMItem();
        DMItem item2 = new DMItem();
        item1.setName("item-1"); item1.setPrice(100.0);
        item2.setName("item-2"); item2.setPrice(200.0);
        
        DMCustomerInventory inventory1 = new DMCustomerInventory();
        DMCustomerInventory inventory2 = new DMCustomerInventory();
        inventory1.setItem(item1); inventory1.setQuantity(10);
        inventory2.setItem(item2); inventory2.setQuantity(20);
        DMCustomer customer = new DMCustomer();
        customer.setFirstName("Detached"); customer.setLastName("Customer");
        customer.setCustomerInventories(Arrays.asList(
            new DMCustomerInventory[]{inventory1,inventory2}));
        inventory1.setCustomer(customer);
        inventory2.setCustomer(customer);
        
        em.getTransaction().begin();
        em.persist(customer);
        em.getTransaction().commit();
        em.clear();
        
        return customer;
    }
    
    void assertDetached(Object pc) {
        assertTrue(pc + " should be detached", em.isDetached(pc));
        assertFalse(pc + " should not be in cache", em.contains(pc));
    }
    
    void assertNotDetached(Object pc) {
        assertFalse(pc + " should not be detached", em.isDetached(pc));
        assertTrue(pc + " should be in cache", em.contains(pc));
    }
}
