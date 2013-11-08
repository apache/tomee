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

import javax.persistence.EntityManager;

import org.apache.openjpa.persistence.detachment.model.DMCustomer;
import org.apache.openjpa.persistence.detachment.model.DMCustomerInventory;
import org.apache.openjpa.persistence.detachment.model.DMItem;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

public class TestDetachedEntityCascadePersist extends SingleEMFTestCase {

    public void setUp() {		
        setUp(
            CLEAR_TABLES,
            DMCustomer.class,
            DMItem.class,
            DMCustomerInventory.class
        );
    }

    public void testDetachedEntityCascadePersist() {
        // Persist an item for finding later 
        EntityManager em = emf.createEntityManager();
        DMItem item = new DMItem(); 
        item.setName("openjpa");
        item.setPrice(0.0);
        em.getTransaction().begin();        
        em.persist(item);
        // Persist a customer for finding later
        DMCustomer customer = new DMCustomer();
        customer.setFirstName("Open");
        customer.setLastName("JPA");
        em.persist(customer);
        em.getTransaction().commit();
        em.close();

        em = emf.createEntityManager();
        DMItem itemDetached = em.find(DMItem.class, item.getId());
        em.close();        
        em = emf.createEntityManager();
        DMCustomer customer2 = em.find(DMCustomer.class, customer.getId());
        DMCustomerInventory customerInventory = new DMCustomerInventory();
        customerInventory.setCustomer(customer2);
        customerInventory.setItem(itemDetached);
        customerInventory.setQuantity(20);
        customer2.getCustomerInventories().add(customerInventory);
        em.getTransaction().begin();
        em.merge(customer2);        
        // At this point, itemDetached is still detached.
        // The following commit causes a persist on CustomerInventory,
        // which leads to a cascade-persist on the detached item.
        // This cascade-persist on a detached item should be ignored, 
        // instead of a EntityExistsException being thrown
        em.getTransaction().commit();
    }
}
