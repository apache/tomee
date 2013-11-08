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

package org.apache.openjpa.persistence.query.results;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.openjpa.persistence.criteria.Person;
import org.apache.openjpa.persistence.criteria.results.Bar;
import org.apache.openjpa.persistence.criteria.results.Foo;
import org.apache.openjpa.persistence.criteria.results.FooBar;
import org.apache.openjpa.persistence.criteria.results.Item;
import org.apache.openjpa.persistence.criteria.results.Order;
import org.apache.openjpa.persistence.criteria.results.Producer;
import org.apache.openjpa.persistence.criteria.results.Shop;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

public class TestJPQLMultiSelectTypedResults extends SingleEMFTestCase {

    private static final int N_ORDERS = 15;
    private static final int N_ITEMS_PER_ORDER = 3;

    // use short data format
    private static final String[] ORDER_DATES =
        { "3/12/2008 1:00 PM", "10/01/2008 1:51 AM", "12/12/2008 10:01 AM", "5/21/2009 3:23 PM" };

    DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, Locale.US);

    public void setUp() throws Exception {
        setUp(CLEAR_TABLES, Order.class, Item.class, Shop.class, Producer.class,
              Person.class, Foo.class, Bar.class,
             "openjpa.DynamicEnhancementAgent", "false");
        populate();
    }

    public void populate() throws ParseException {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        Shop s = new Shop();
        Order order;
        Item item;
        Producer p;

        s.setId(1);
        s.setName("eBay.com");
        s.setOrders(new HashSet<Order>());

        for (int i = 1; i <= N_ORDERS; i++) {
            order = new Order();
            order.setId(i);
            order.setDate(df.parse(ORDER_DATES[i % ORDER_DATES.length]));
            order.setFilled(i % 2 == 0 ? true : false);
            order.setShop(s);
            order.setItems(new HashSet<Item>());
            s.getOrders().add(order);
            for (int j = 1; j <= N_ITEMS_PER_ORDER; j++) {
                item = new Item();
                item.setOrder(order);
                order.getItems().add(item);
                p = new Producer();
                p.setName("filler");
                p.setItem(item);
                item.setProduct(p);
            }
        }
        em.persist(s);
        Person person = new Person("Test Result Shape");
        em.persist(person);
        
        Foo foo = new Foo(100L, "Test Foo");
        Bar bar = new Bar(200L, "Test Bar");
        foo.setBar(bar);
        em.persist(foo);
        em.persist(bar);
        em.getTransaction().commit();
        em.close();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testMultipleConstructor() {
        String query = "SELECT NEW Foo(f.fid,f.fint), b, NEW FooBar(f.fid, b.bid) " +
                    "from Foo f JOIN f.bar b WHERE f.bar=b";
        EntityManager em = emf.createEntityManager();
        Query jpqlQuery = em.createQuery(query);
        List<Object[]> result = jpqlQuery.getResultList();

        assertFalse(result.isEmpty());
        for (Object[] row : result) {
            assertEquals(3, row.length);
            assertTrue("0-th element " + row[0].getClass() + " is not Foo", row[0] instanceof Foo);
            assertTrue("1-st element " + row[1].getClass() + " is not Bar", row[1] instanceof Bar);
            assertTrue("2-nd element " + row[2].getClass() + " is not FooBar", row[2] instanceof FooBar);
        }
        
    }
    public void testMultipleConstructorMixWithMultiSelect() {
        String query = "SELECT NEW Person(p.name), p.id, NEW Person(p.name), p.name FROM Person p ORDER BY p.name";
        EntityManager em = emf.createEntityManager();
        Query jpqlQuery = em.createQuery(query);
        List<Object[]> result = jpqlQuery.getResultList();
        assertTrue(!result.isEmpty());
        for (Object[] row : result) {
            assertEquals(4, row.length);
            
            assertEquals(Person.class,  row[0].getClass());
            assertEquals(Integer.class, row[1].getClass());
            assertEquals(Person.class,  row[2].getClass());
            assertEquals(String.class,  row[3].getClass());
            
            assertEquals(((Person)row[0]).getName(), ((Person)row[2]).getName());
            assertEquals(((Person)row[0]).getName(), row[3]);
        }
    }
    /**
     * Testcase to verify that selecting multiple results in a variety of ways returns the same results. Results are
     * returned via a normal Object [] (JPQL).
     * 
     * @throws Exception
     */
    public void testMultiSelect() throws Exception {
        // get results from traditional JPQL
        EntityManager em = emf.createEntityManager();
        Query jpqlQuery =
            em.createQuery("SELECT o, p from Order o JOIN o.items i JOIN i.producer p WHERE o.filled = true");
        // don't suppress warnings.
        List<Object[]> jpqlResults = jpqlQuery.getResultList();

        assertEquals(N_ORDERS / 2 * N_ITEMS_PER_ORDER, jpqlResults.size());

        for (Object[] os : jpqlResults) {
            assertEquals(2, os.length);
            assertTrue(os[0] instanceof Order);
            assertTrue(os[1] instanceof Producer);
        }

        em.close();
    }
}
