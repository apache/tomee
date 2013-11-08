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
package org.apache.openjpa.persistence.simple;

import junit.textui.TestRunner;
import org.apache.openjpa.persistence.test.SingleEMTestCase;
import java.math.BigDecimal;

/**
 * Test case to ensure that the proper JPA clear semantics are processed.
 *
 * @author Kevin Sutter
 */
public class TestEntityManagerClear
    extends SingleEMTestCase {

    public void setUp() {
        setUp(AllFieldTypes.class, Item.class,CLEAR_TABLES);
    }

    public void testDetach() {
        // Create EntityManager and Start a transaction (1)
        begin();

        // Insert a new object and flush
        AllFieldTypes testObject1 = new AllFieldTypes();
        testObject1.setStringField("my test object1");
        persist(testObject1);
        em.flush();
        assertTrue("testObject1 not found in pc", em.contains(testObject1));

        // Insert another object and persist
        AllFieldTypes testObject2 = new AllFieldTypes();
        testObject1.setStringField("my test object2");
        persist(testObject2);
        assertTrue("testObject2 not found in pc", em.contains(testObject2));
        
        // Rollback to clear the PC
        rollback();
        
        assertFalse("testObject1 found in pc", em.contains(testObject1));
        assertFalse("testObject2 found in pc", em.contains(testObject2));
        
    }
    
    public void testClear() {
        // Create EntityManager and Start a transaction (1)
        begin();

        // Insert a new object and flush
        AllFieldTypes testObject1 = new AllFieldTypes();
        testObject1.setStringField("my test object1");
        persist(testObject1);
        em.flush();

        // Clear the PC for new object 2
        AllFieldTypes testObject2 = new AllFieldTypes();
        testObject1.setStringField("my test object2");
        persist(testObject2);
        em.clear();

        // Commit the transaction (only object 1 should be in database)
        commit();

        // Start a new transaction
        begin();

        // Attempt retrieve of Object1 from previous PC (should exist)
        assertEquals(1, query("select x from AllFieldTypes x "
            + "where x.stringField = 'my test object1'").
                getResultList().size());

        // Attempt retrieve of Object2 from previous PC (should not exist)
        assertEquals(0, query("select x from AllFieldTypes x "
            + "where x.stringField = 'my test object2'").
                getResultList().size());

        // Rollback the transaction and close everything
        rollback();
    }

    public void testNewClearMerge() {
        // Create EntityManager and Start a transaction (1)
        begin();

        // Insert a new object then clear persistent context
        AllFieldTypes testObject1 = new AllFieldTypes();
        testObject1.setStringField("my test object1");
        persist(testObject1);
        //Object1 is not flushed to DB but only detached by clear().
        em.clear();

        assertEquals(0, query("select x from AllFieldTypes x "
                + "where x.stringField = 'my test object1'").
                    getResultList().size());
        em.merge(testObject1);
        commit();

        //Start a new transaction
        begin();

        // Expect Object1 is persisted after merge and commit.
        assertEquals(1, query("select x from AllFieldTypes x "
            + "where x.stringField = 'my test object1'").
                getResultList().size());

        // Rollback the transaction and close everything
        rollback();
    }

    public void testUpdateClearMerge() {
        // Create EntityManager and Start a transaction (1)
        begin();

        // Insert a new object
        Item i = new Item();
        i.setItemName("cup");
        persist(i);
        commit();
        int id = i.getItemId();

        begin();
        Item i2 = em.find(Item.class, id);
        i2.setItemName("fancy cup");
        //Updated item is not flushed to DB but only detached by clear().
        em.clear();
        assertEquals(0, query("select x from Item x "
                + "where x.itemName = 'fancy cup'").
                    getResultList().size());
        em.merge(i2);
        commit();

        //Start a new transaction
        begin();

        //should be changed by previous commit
        assertEquals(1, query("select x from Item x "
            + "where x.itemName = 'fancy cup'").
                getResultList().size());

        // Rollback the transaction and close everything
        rollback();
    }

    /**
     * Test modify entity then clear context which cause unflushed modified
     * entity detached. Do more modification on detached entity, merge back and
     * commit. Expect both changes before clear and after clear are persisted.
     *
     */
    public void testUpdateClearUpdateMerge() {
        // Create EntityManager and Start a transaction (1)
        begin();

        // Insert a new object
        Item i = new Item();
        i.setItemName("cup");
        i.setItemPrice(new BigDecimal(100.00));
        persist(i);
        commit();
        int id = i.getItemId();

        begin();
        Item i2 = em.find(Item.class, id);
        i2.setItemName("fancy cup");
        //Updated item is not flushed to DB but only detached by clear().
        em.clear();
        assertEquals(0, query("select x from Item x "
                + "where x.itemName = 'fancy cup'").
                    getResultList().size());
        i2.setItemPrice(new BigDecimal(120.00));
        em.merge(i2);
        commit();

        //Start a new transaction
        begin();

        //should be changed by previous commit
        assertEquals(1, query("select x from Item x "
            + "where x.itemName = 'fancy cup' and x.itemPrice = 120.00").
                getResultList().size());

        // Rollback the transaction and close everything
        rollback();
    }

    public void testUpdateFlushClearUpdateMerge() {
        // Create EntityManager and Start a transaction (1)
        begin();

        // Insert a new object
        Item i = new Item();
        i.setItemName("cup");
        i.setItemPrice(new BigDecimal(100.00));
        persist(i);
        commit();
        int id = i.getItemId();

        begin();
        Item i2 = em.find(Item.class, id);
        i2.setItemName("fancy cup");
        em.flush();
        em.clear();
        //it is updated because it is flushed before clear();
        assertEquals(1, query("select x from Item x "
                + "where x.itemName = 'fancy cup'").
                    getResultList().size());
        i2.setItemPrice(new BigDecimal(120.00));
        i2.setItemName("red cup");
        em.merge(i2);
        commit();

        //Start a new transaction
        begin();

        //should be changed by previous commit
        assertEquals(1, query("select x from Item x "
            + "where x.itemName = 'red cup' and x.itemPrice = 120.00").
                getResultList().size());

        // Rollback the transaction and close everything
        rollback();
    }
    public static void main(String[] args) {
        TestRunner.run(TestEntityManagerClear.class);
    }
}

