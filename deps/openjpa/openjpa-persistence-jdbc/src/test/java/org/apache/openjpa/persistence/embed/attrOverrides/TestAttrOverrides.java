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
package org.apache.openjpa.persistence.embed.attrOverrides;

import java.math.BigDecimal;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import org.apache.openjpa.persistence.test.SQLListenerTestCase;

public class TestAttrOverrides  extends SQLListenerTestCase { 
    public int numPersons = 4;
    public int numPropertiesPerPersons = 4;
    public int eId = 1;
    public int pId = 1;

    public void setUp() throws Exception {
        super.setUp(DROP_TABLES, Address.class, Customer.class, 
            PropertyInfo.class, PropertyOwner.class, PropertyRecord.class,
            Zipcode.class, Person.class);
    }

    /**
     * This is spec 10.1.4 Example 2
     * Test AttributeOverride on embeddable fields
     */
    public void testAttrOverride1() {
        sql.clear();
        createObj1();
        findObj1();
        queryObj1();
        assertAttrOverrides("CUS_ATTROVER");
    }

    /**
     * This is spec 10.1.4. Example 3
     * Test AttributeOverrides on embeddable Map field
     */
    public void testAttrOverride2() {
        sql.clear();
        createObj2();
        findObj2();
        queryObj2();
        assertAttrOverrides("PROPREC_ATTROVER_parcels");
    }
    
    /**
     * This is spec 10.1.35. Example 3
     * Test OrderBy on embeddable field
     */
    public void testEmbeddableOrderBy() {
        sql.clear();
        createObj3();
        findObj3();
    }

    public void createObj1() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tran = em.getTransaction();
        for (int i = 0; i < numPersons; i++)
            createCustomer(em, eId++);
        tran.begin();
        em.flush();
        tran.commit();
        em.close();
    }

    public Customer createCustomer(EntityManager em, int id) {
        Customer p = new Customer();
        p.setId(id);
        Address addr = new Address();
        addr.setCity("city_" + id);
        addr.setState("state_" + id);
        addr.setStreet("street_" + id);
        p.setAddress(addr);
        p.setName("name_" + id);
        em.persist(p);
        return p;
    }

    public void findObj1() {
        EntityManager em = emf.createEntityManager();
        Customer p = em.find(Customer.class, 1);
        assertEquals(p.getId(), new Integer(1));
        assertEquals(p.getAddress().getCity(), "city_1");
        assertEquals(p.getAddress().getStreet(), "street_1");
        assertEquals(p.getAddress().getState(), "state_1");
        assertEquals(p.getName(), "name_1");
        em.close();
    }

    public void queryObj1() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tran = em.getTransaction();
        tran.begin();
        String jpql = "select p from Customer p";
        Query q = em.createQuery(jpql);
        List<Customer> ps = q.getResultList();
        assertEquals(ps.size(), numPersons);
        tran.commit();
        em.close();
    }

    public void createObj2() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tran = em.getTransaction();
        for (int i = 0; i < numPersons; i++)
            createPropertyRecord(em, pId++);
        tran.begin();
        em.flush();
        tran.commit();
        em.close();
    }

    public PropertyRecord createPropertyRecord(EntityManager em, int id) {
        PropertyRecord p = new PropertyRecord();
        PropertyOwner owner = new PropertyOwner();
        owner.setSsn("ssn_" + id);
        Address addr = new Address();
        addr.setCity("city_" + id);
        addr.setState("state_" + id);
        addr.setStreet("street_" + id);
        Zipcode zipcode = new Zipcode();
        zipcode.setZip("zip_" + id);
        zipcode.setPlusFour("+4_" + id);
        addr.setZipcode(zipcode);
        owner.setAddress(addr);
        p.setOwner(owner);
        for (int i = 0; i < numPropertiesPerPersons; i++) {
            PropertyInfo info = new PropertyInfo();
            info.setParcelNumber(id*10 + i);
            info.setSize(id*10 + i);
            info.setTax(new BigDecimal(id*10 + i));
            Address paddr = new Address();
            paddr.setCity("pcity_" + id + "_" + i);
            paddr.setState("pstate_" + id + "_" + i);
            paddr.setStreet("pstreet_" + id + "_" + i);
            Zipcode pzipcode = new Zipcode();
            pzipcode.setZip("pzip_" + id + "_" + i);
            pzipcode.setPlusFour("p+4_" + id + "_" + i);
            paddr.setZipcode(zipcode);
            p.addParcel(paddr, info);
        }
        em.persist(p);
        return p;
    }

    public void findObj2() {
        EntityManager em = emf.createEntityManager();
        PropertyOwner owner = new PropertyOwner();
        owner.setSsn("ssn_1");
        Address addr = new Address();
        addr.setCity("city_1");
        addr.setState("state_1");
        addr.setStreet("street_1");
        Zipcode zipcode = new Zipcode();
        zipcode.setZip("zip_1");
        zipcode.setPlusFour("+4_1");
        addr.setZipcode(zipcode);
        owner.setAddress(addr);
        PropertyRecord p = em.find(PropertyRecord.class, owner);
        assertEquals(p.getOwner().getSsn(), "ssn_1");
        assertEquals(p.getOwner().getAddress().getCity(), "city_1");
        assertEquals(p.getOwner().getAddress().getStreet(), "street_1");
        assertEquals(p.getOwner().getAddress().getState(), "state_1");
        assertEquals(p.getOwner().getAddress().getZipcode().getZip(), "zip_1");
        assertEquals(p.getOwner().getAddress().getZipcode().getPlusFour(),
                "+4_1");

        assertEquals(p.getParcels().size(), numPropertiesPerPersons);
        em.close();
    }

    public void queryObj2() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tran = em.getTransaction();
        tran.begin();
        String jpql = "select p from PropertyRecord p";
        Query q = em.createQuery(jpql);
        List<PropertyRecord> ps = q.getResultList();
        assertEquals(ps.size(), numPersons);
        tran.commit();
        em.close();
    }
    
    public void assertAttrOverrides(String tableName) {
        boolean found = false;
        for (String sqlStr : sql) {
            if (sqlStr.indexOf("CREATE TABLE " + tableName + " ") != -1) {
                if (tableName.equals("CUS_ATTROVER")) {
                    found = true;
                    if (sqlStr.indexOf("ADDR_STATE") == -1 ||
                        sqlStr.indexOf("ADDR_ZIP") == -1 ||
                        sqlStr.indexOf("ADDR_PLUSFOUR") == -1 )
                        fail();
                } else if (tableName.equals("PROPREC_ATTROVER_parcels")) {
                    found = true;
                    if (sqlStr.indexOf("STREET_NAME") == -1 ||
                        sqlStr.indexOf("SQUARE_FEET") == -1 ||
                        sqlStr.indexOf("ASSESSMENT") == -1 )
                        fail();
                }
                break;
            }
        }
        if (!found)
            fail();
    }
    
    public void createObj3() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tran = em.getTransaction();
        for (int i = 0; i < numPersons; i++)
            createPerson(em, eId++);
        tran.begin();
        em.flush();
        tran.commit();
        em.close();
    }

    public Person createPerson(EntityManager em, int id) {
        Person p = new Person();
        p.setSsn("ssn" + id);
        p.setName("name_" + id);
    
        for (int i = 4; i > 0; i--) {
            Address addr = new Address();
            addr.setCity("city_" + id + "_" + i );
            addr.setState("state_" + id + "_"  + i);
            addr.setStreet("street_" + id + "_"  + i);
            Zipcode zipCode = new Zipcode();
            zipCode.setZip("zip_" + id + "_" + i);
            zipCode.setPlusFour("plusFour_" + id + "_" + i);
            addr.setZipcode(zipCode);
            p.addResidence(addr);
            p.addNickName("nickName_ + " + i);
        }
        em.persist(p);
        return p;
    }

    public void findObj3() {
        EntityManager em = emf.createEntityManager();
        Person p = em.find(Person.class, "ssn1");
        List<Address> residences = p.getResidences();
        assertEquals(4, residences.size());
        int i = 1;
        for (Address a : residences) {
            String zip = a.getZipcode().getZip();
            String plusFour = a.getZipcode().getPlusFour();
            String expZip = "zip_1_";
            String expPlusFour = "plusFour_1_";
            expZip = expZip + i;
            expPlusFour = expPlusFour + i;
            assertEquals(expZip, zip);
            assertEquals(expPlusFour, plusFour);
            i++;
        }
        
        List<String> nickNames = p.getNickNames();
        assertEquals(4, nickNames.size());
        i = 4;
        for (String s : nickNames) {
            String expNickName = "nickName_ + " + i;
            assertEquals(expNickName, s);
            i--;
        }
        em.close();
   }
}

