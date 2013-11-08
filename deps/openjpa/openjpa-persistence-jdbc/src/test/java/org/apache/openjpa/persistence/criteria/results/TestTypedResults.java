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

package org.apache.openjpa.persistence.criteria.results;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Root;

import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.jdbc.sql.PostgresDictionary;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

public class TestTypedResults extends SingleEMFTestCase {

    private static final int N_ORDERS = 15;
    private static final int N_ITEMS_PER_ORDER = 3;

    // use short data format
    private static final String[] ORDER_DATES =
        { "3/12/2008 1:00 PM", "10/01/2008 1:51 AM", "12/12/2008 10:01 AM", "5/21/2009 3:23 PM" };

    DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, Locale.US);

    public void setUp() throws Exception {
        setUp(CLEAR_TABLES, Order.class, Item.class, Shop.class, Producer.class,
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
        em.getTransaction().commit();
        em.close();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Verify that a query using a date field works the same with JPQL, JPQL (typed), Criteria (typed), and via a
     * NativeQuery
     * 
     * @throws Exception
     */
    public void testTypedJPQLQuery() {
        EntityManager em = emf.createEntityManager();

        Query jpqlQuery = em.createQuery("Select o from Order o where o.filled = true");
        // Don't suppress warnings.
        List<Order> jpqlResults = jpqlQuery.getResultList();
        assertEquals(N_ORDERS / 2, jpqlResults.size());

        TypedQuery<Order> jpqlTypedQuery = em.createQuery("Select o from Order o where o.filled = true", Order.class);
        List<Order> jpqlTypedResults = jpqlTypedQuery.getResultList();
        assertEquals(N_ORDERS / 2, jpqlTypedResults.size());

        // create the same query and get typed results.
        CriteriaBuilder qb = em.getCriteriaBuilder();
        CriteriaQuery<Order> cq = qb.createQuery(Order.class);
        Root<Order> order = cq.from(Order.class);
        cq.select(order).where(qb.equal(order.get(Order_.filled), Boolean.TRUE));

        TypedQuery<Order> typedCriteriaQuery = em.createQuery(cq);
        List<Order> typedCriteriaResults = typedCriteriaQuery.getResultList();
        assertEquals(N_ORDERS / 2, typedCriteriaResults.size());

        
        DBDictionary dict = ((JDBCConfiguration)emf.getConfiguration()).getDBDictionaryInstance();
        String sql = "SELECT * FROM CRIT_RES_ORD o WHERE (o.filled = 1)";
        if (dict instanceof PostgresDictionary)
            sql = "SELECT * FROM CRIT_RES_ORD o WHERE (o.filled = true)";
        Query nativeQ = em.createNativeQuery(sql, Order.class);
        // Don't suppress warnings.
        List<Order> typedNativeResults = nativeQ.getResultList();
        assertEquals(N_ORDERS / 2, typedNativeResults.size());

        for (Order o : jpqlResults) {
            assertTrue(jpqlTypedResults.contains(o));
            assertTrue(typedCriteriaResults.contains(o));
            assertTrue(typedNativeResults.contains(o));
        }
        em.close();
    }

    /**
     * Verify that a query using a date field works the same with JPQL, JPQL (typed), Criteria (typed), and via a
     * NativeQuery
     * 
     * @throws Exception
     */
    public void testDateQuery() throws Exception {
        EntityManager em = emf.createEntityManager();
        Date maxDate = df.parse(ORDER_DATES[2]);
        
        Query jpqlQuery = em.createQuery("Select o from Order o where o.date < :maxDate");
        jpqlQuery.setParameter("maxDate", maxDate);
        List<Order> jpqlResults = jpqlQuery.getResultList();
        assertEquals(N_ORDERS / 2, jpqlResults.size());

        TypedQuery<Order> typedJpqlQuery = em.createQuery("Select o from Order o where o.date < :maxDate", Order.class);
        typedJpqlQuery.setParameter("maxDate", maxDate);
        List<Order> typedJpqlResults = typedJpqlQuery.getResultList();
        assertEquals(N_ORDERS / 2, typedJpqlResults.size());

        CriteriaBuilder qb = em.getCriteriaBuilder();
        CriteriaQuery<Order> criteriaQuery = qb.createQuery(Order.class);
        Root<Order> order = criteriaQuery.from(Order.class);
        criteriaQuery.select(order).where(qb.lessThan(order.get(Order_.date), qb.parameter(Date.class, "maxDate")));
        TypedQuery<Order> tq = em.createQuery(criteriaQuery);
        tq.setParameter("maxDate", maxDate);
        List<Order> criteriaResults = tq.getResultList();
        assertEquals(N_ORDERS / 2, criteriaResults.size());

        Query nativeQuery = em.createNativeQuery("Select * from CRIT_RES_ORD o WHERE (o.cdate < ?1)", Order.class);
        nativeQuery.setParameter(1, maxDate);
        List<Order> nativeResults = nativeQuery.getResultList();
        assertEquals(N_ORDERS / 2, nativeResults.size());

        for (Order o : jpqlResults) {
            assertTrue(typedJpqlResults.contains(o));
            assertTrue(criteriaResults.contains(o));
            assertTrue(nativeResults.contains(o));
        }
        em.close();
    }

    /**
     * Testcase to verify that selecting multiple results in a variety of ways returns the same results. Results are
     * returned via a normal Object [] (JPQL), Tuple (Criteria), and a custom tuple (Criteria.construct)
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

        // Get results using Tuple
        CriteriaBuilder qb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> criteriaQuery = qb.createTupleQuery();
        Root<Order> order = criteriaQuery.from(Order.class);
        Join<Item, Producer> producer = order.join(Order_.items).join(Item_.producer);
        criteriaQuery.select(qb.tuple(order, producer));
        criteriaQuery.where(qb.equal(order.get(Order_.filled), Boolean.TRUE));
        TypedQuery<Tuple> eq = em.createQuery(criteriaQuery);
        List<Tuple> criteriaResults = eq.getResultList();

        // Get results using custom class
        CriteriaQuery<OrderProducer> constructQuery = qb.createQuery(OrderProducer.class);
        Root<Order> order2 = constructQuery.from(Order.class);
        Join<Item, Producer> producer2 = order.join(Order_.items).join(Item_.producer);
        constructQuery.select(qb.construct(OrderProducer.class, order2, producer2));
        constructQuery.where(qb.equal(order2.get(Order_.filled), Boolean.TRUE));
        TypedQuery<OrderProducer> typedQuery = em.createQuery(constructQuery);
        List<OrderProducer> constructResults = typedQuery.getResultList();

        assertEquals(N_ORDERS / 2 * N_ITEMS_PER_ORDER, jpqlResults.size());
        assertEquals(N_ORDERS / 2 * N_ITEMS_PER_ORDER, criteriaResults.size());
        assertEquals(N_ORDERS / 2 * N_ITEMS_PER_ORDER, constructResults.size());

        for (Object[] os : jpqlResults) {
            assertEquals(2, os.length);
            assertTrue(os[0] instanceof Order);
            assertTrue(os[1] instanceof Producer);
        }

        // cheap way to ensure that we have the same contents.
        // if needed an orderBy clause can be added to make this more robust.
        Object[] jpqlTuple;
        Tuple criteriaTuple;
        OrderProducer constructTuple;
        for (int i = 0; i < jpqlResults.size(); i++) {
            jpqlTuple = jpqlResults.get(i);
            criteriaTuple = criteriaResults.get(i);
            constructTuple = constructResults.get(i);
            assertEquals(jpqlTuple[0], criteriaTuple.get(0));
            assertEquals(jpqlTuple[1], criteriaTuple.get(1));
            assertEquals(jpqlTuple[0], constructTuple.getOrder());
            assertEquals(jpqlTuple[1], constructTuple.getProducer());
        }
        em.close();
    }
}
