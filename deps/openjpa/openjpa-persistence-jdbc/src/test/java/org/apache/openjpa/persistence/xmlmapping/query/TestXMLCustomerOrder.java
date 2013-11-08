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
package org.apache.openjpa.persistence.xmlmapping.query;

import java.sql.Connection;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import junit.textui.TestRunner;

import org.apache.commons.lang.StringUtils;
import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.apache.openjpa.persistence.OpenJPAEntityManagerSPI;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;
import org.apache.openjpa.persistence.xmlmapping.entities.Customer;
import org.apache.openjpa.persistence.xmlmapping.entities.EAddress;
import org.apache.openjpa.persistence.xmlmapping.entities.Order;
import org.apache.openjpa.persistence.xmlmapping.entities.Customer.CreditRating;
import org.apache.openjpa.persistence.xmlmapping.xmlbindings.myaddress.Address;
import org.apache.openjpa.persistence.xmlmapping.xmlbindings.myaddress.CANAddress;
import org.apache.openjpa.persistence.xmlmapping.xmlbindings.myaddress.ObjectFactory;
import org.apache.openjpa.persistence.xmlmapping.xmlbindings.myaddress.USAAddress;

/**
 * Test query with predicates on persistent field mapped to XML column.
 * Samples of platform specific sqls are under resources in
 * TestXMLCustomerOrder.[dbname] files.
 * 
 * DB2 requires some special options on the database creation to allow for
 * native XML support:
 * create database test using codeset utf-8 territory us
 * 
 * @author Catalina Wei
 * @author Milosz Tylenda
 * @since 1.0.0
 */
public class TestXMLCustomerOrder
    extends SingleEMFTestCase {
    
    private static final int ORDER_1_OID = 10;
    private static final double ORDER_1_AMOUNT = 850;
    private static final boolean ORDER_1_DELIVERED = false;

    private static final int ORDER_2_OID = 20;
    private static final double ORDER_2_AMOUNT = 1000;
    private static final boolean ORDER_2_DELIVERED = false;

    private static boolean firstTestExecuted;

    public void setUp() {
        Object clearOrDropTables = (firstTestExecuted) ? CLEAR_TABLES : DROP_TABLES;
        firstTestExecuted = true;
        setUp(Customer.class, Customer.CustomerKey.class, Order.class,
            EAddress.class, clearOrDropTables);  // test create table DDL for XML column but only once to save time.

        // skip test if dictionary has no support for XML column type
        setTestsDisabled(!dictionarySupportsXMLColumn());
        if (isTestsDisabled()) {
            getLog().trace("TestXMLCustomerOrder() - Skipping all tests - No XML Column support");
            return;
        }
        
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        persistEntities(em);
        em.getTransaction().commit();
        em.close();
    }

    @SuppressWarnings("unchecked")
    public void testXMLFieldProjection() {
        EntityManager em = emf.createEntityManager();
        List<Address> addrs = em.createQuery(
            "select o.shipAddress from Order o order by o.oid")
            .getResultList();
        
        assertEquals(2, addrs.size());
        
        Address addressFromDb = addrs.get(0);
        Address address = createUSAAddress("Harry's Auto");
        assertEquals(address.toString(), addressFromDb.toString());
        
        addressFromDb = addrs.get(1);
        address = createCANAddress("A&J Auto");
        assertEquals(address.toString(), addressFromDb.toString());
        
        em.close();
    }
    
    @SuppressWarnings("unchecked")
    public void testXMLFieldInEntity() {
        EntityManager em = emf.createEntityManager();
        List<Order> orders = em.createQuery(
            "select o from Order o order by o.oid")
            .getResultList();
        
        assertEquals(2, orders.size());
        
        Order orderFromDb = orders.get(0);
        Address addressFromDb = orderFromDb.getShipAddress();
        Address address = createUSAAddress("Harry's Auto");
        assertEquals(address.toString(), addressFromDb.toString());
        
        orderFromDb = orders.get(1);
        addressFromDb = orderFromDb.getShipAddress();
        address = createCANAddress("A&J Auto");
        assertEquals(address.toString(), addressFromDb.toString());
        
        em.close();
    }

    @SuppressWarnings("unchecked")
    public void testXMLStringToXMLStringComparison() {
        EntityManager em = emf.createEntityManager();
        List<Object[]> orders = em.createQuery(
            "select o, o2 from Order o, Order o2 where o.shipAddress.city " +
            "= o2.shipAddress.city order by o.oid")
            .getResultList();
        
        assertEquals(2, orders.size());

        Object[] ordersFromDb = orders.get(0);
        Order order1 = (Order) ordersFromDb[0];
        Order order2 = (Order) ordersFromDb[1];
        assertEquals(ORDER_1_OID, order1.getOid());
        assertOrdersEqual(order1, order2);

        ordersFromDb = orders.get(1);
        order1 = (Order) ordersFromDb[0];
        order2 = (Order) ordersFromDb[1];
        assertEquals(ORDER_2_OID, order1.getOid());
        assertOrdersEqual(order1, order2);
        
        em.close();
    }

    @SuppressWarnings("unchecked")
    public void testXMLStringToEmbeddedStringComparison() {
        EntityManager em = emf.createEntityManager();
        List<Order> orders = em.createQuery(
            "select o from Order o, Customer c where o.shipAddress.city " +
            "= c.address.city")
            .getResultList();
        
        assertEquals(1, orders.size());
        Order order = orders.get(0);
        assertEquals(ORDER_1_OID, order.getOid());
        
        em.close();
    }

    @SuppressWarnings("unchecked")
    public void testXMLStringToConstantStringComparison() {
        EntityManager em = emf.createEntityManager();
        List<Order> orders = em.createQuery(
            "select o from Order o where o.shipAddress.city = 'San Jose'")
            .getResultList();
        
        assertEquals(1, orders.size());
        Order order = orders.get(0);
        assertEquals(ORDER_1_OID, order.getOid());
        
        em.close();
    }

    @SuppressWarnings("unchecked")
    public void testXMLStringToParameterStringComparison() {
        EntityManager em = emf.createEntityManager();
        Query query = em.createQuery(
            "select o from Order o where o.shipAddress.city = :city");
        query.setParameter("city", "San Jose");
        List<Order> orders = query.getResultList();

        assertEquals(1, orders.size());
        Order order = orders.get(0);
        assertEquals(ORDER_1_OID, order.getOid());
        
        em.close();
    }

    @SuppressWarnings("unchecked")
    public void testParameterStringToXMLStringComparison() {
        EntityManager em = emf.createEntityManager();
        Query query = em.createQuery(
            "select o from Order o where :city = o.shipAddress.city");
        query.setParameter("city", "San Jose");
        List<Order> orders = query.getResultList();

        assertEquals(1, orders.size());
        Order order = orders.get(0);
        assertEquals(ORDER_1_OID, order.getOid());
        
        em.close();
    }

    public void testUpdate() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        Order order = em.find(Order.class, ORDER_1_OID);
        USAAddress address = (USAAddress) order.getShipAddress();
        address.setCity("Cupertino");
        address.setZIP(95014);

        em.getTransaction().commit();
        em.close();

        em = emf.createEntityManager();
        em.getTransaction().begin();

        order = em.find(Order.class, ORDER_1_OID);
        address = (USAAddress) order.getShipAddress();
        assertEquals("Cupertino", address.getCity());
        assertEquals(95014, address.getZIP());
        
        em.getTransaction().commit();
        em.close();
    }

    public void testNullify() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        Order order = em.find(Order.class, ORDER_1_OID);
        order.setShipAddress(null);

        em.getTransaction().commit();
        em.close();

        em = emf.createEntityManager();
        em.getTransaction().begin();

        order = em.find(Order.class, ORDER_1_OID);
        Address address = order.getShipAddress();
        assertNull(address);
        
        em.getTransaction().commit();
        em.close();
    }

    public void testXMLStringToConstantIntComparison() {
        EntityManager em = emf.createEntityManager();
        try {
            em.createQuery(
                "select o from Order o where o.shipAddress.city = 95141")
                .getResultList();
        } catch (IllegalArgumentException iae) {
            return;
        } finally {
            em.close();
        }
        fail("createQuery should throw IllegalArgumentException.");
    }

    public void testXMLListToConstantStringComparison() {
        EntityManager em = emf.createEntityManager();
        try {
            em.createQuery(
                "select o from Order o where o.shipAddress.street "
                    + "= '555 Bailey'").getResultList();
        } catch (IllegalArgumentException iae) {
            return;
        } finally {
            em.close();
        }
        fail("createQuery should throw IllegalArgumentException.");
    }

    public void testSubclassPropertyInXMLFieldComparison() {
        EntityManager em = emf.createEntityManager();
        try {
            em.createQuery(
                "select o from Order o where o.shipAddress.zip = 95141")
                .getResultList();
        } catch (IllegalArgumentException iae) {
            return;
        } finally {
            em.close();
        }
        fail("createQuery should throw IllegalArgumentException.");
    }

    public static void main(String[] args) {
        TestRunner.run(TestXMLCustomerOrder.class);
    }

    private void persistEntities(EntityManager em) {
        Customer c2 = new Customer();
        c2.setCid( new Customer.CustomerKey("USA", 2) );
        c2.setName("A&J Auto");
        c2.setRating( CreditRating.GOOD );
        c2.setAddress(new EAddress("2480 Campbell Ave", "Campbell", "CA"
                , "95123"));
        em.persist(c2);

        Customer c1 = new Customer();
        c1.setCid( new Customer.CustomerKey("USA", 1) );
        c1.setName("Harry's Auto");
        c1.setRating( CreditRating.GOOD );
        c1.setAddress( new EAddress("12500 Monterey", "San Jose", "CA",
                "95141"));
        em.persist(c1);

        Order o1 = new Order(ORDER_1_OID, ORDER_1_AMOUNT, ORDER_1_DELIVERED,
                c1);
        o1.setShipAddress(createUSAAddress(c1.getName()));
        em.persist(o1);

        Order o2 = new Order(ORDER_2_OID, ORDER_2_AMOUNT, ORDER_2_DELIVERED,
                c1);
        o2.setShipAddress(createCANAddress(c2.getName()));
        em.persist(o2);
    }

    /**
     * Check whether DBDictionary supports XML column.
     * This is done by forcing the execution of
     * {@link DBDictionary#connectedConfiguration(Connection)}.
     * This is where some dictionaries actually determine whether XML column is
     * supported.
     * @return true if {@link DBDictionary} supports XML column
     */
    private boolean dictionarySupportsXMLColumn() {
        OpenJPAEntityManagerFactorySPI emf = createEMF();
        OpenJPAEntityManagerSPI em = emf.createEntityManager();
        DBDictionary dict = ((JDBCConfiguration) em.getConfiguration())
            .getDBDictionaryInstance();
        closeEMF(emf);
        return dict.supportsXMLColumn;
    }
    
    private USAAddress createUSAAddress(String name) {
        USAAddress address = new ObjectFactory().createUSAAddress();
        address.setName(name);
        // Use a 4000-byte value so the entire XML string is longer than 4000 bytes - ensure Oracle handles this.
        address.getStreet().add(StringUtils.repeat("12500 Mont", 400));
        address.setCity("San Jose");
        address.setState("CA");
        address.setZIP(95141);
        return address;
    }

    private CANAddress createCANAddress(String name) {
        CANAddress address = new ObjectFactory().createCANAddress();
        address.setName(name);
        address.getStreet().add("123 Warden Road");
        address.setCity("Markham");
        address.setPostalCode("L6G 1C7");
        address.setProvince("ON");
        return address;
    }
    
    private void assertOrdersEqual(Order o1, Order o2) {
        assertEquals(o1.getOid(), o2.getOid());
        assertEquals(o1.getAmount(), o2.getAmount());
        assertEquals(o1.isDelivered(), o2.isDelivered());
    }

}
