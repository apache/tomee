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
package org.apache.openjpa.persistence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.openjpa.persistence.test.SQLListenerTestCase;
import javax.persistence.*;

public class TestXMLPersistenceMetaDataParser extends SQLListenerTestCase {

    public void setUp() {
        super.setUp(CLEAR_TABLES, Security1.class, Country1.class,
            Security.class, Country.class);
    }

    protected String getPersistenceUnitName() {
        return "test-persistence-xml-orm";
    }

    public void testManyToOneLazyFetch() {

        EntityManager em = emf.createEntityManager();

        // initialize objects
        long aI_sid = 148007244;
        long aUS_sid = 1;

        Security1 aI_security = new Security1(aI_sid, new Embed("XYZ"));
        Country1 aUS_country = new Country1(aUS_sid, "USA");
        aI_security.setCountry1(aUS_country);

        Security aI_securityAnn = new Security(aI_sid, new Embed("XYZ"));
        Country aUS_countryAnn = new Country(aUS_sid, "USA");
        aI_securityAnn.setCountry(aUS_countryAnn);

        em.getTransaction().begin();
        em.persist(aI_security);
        em.persist(aUS_country);
        em.getTransaction().commit();
        em.clear();

        ArrayList<String> XMLsql = new ArrayList<String>();
        ArrayList<String> Annsql = new ArrayList<String>();

        super.sql = new ArrayList<String>();
        aUS_country = em.find(Country1.class, aUS_sid);
        Iterator itr = super.sql.iterator();
        while (itr.hasNext()) {
            XMLsql.add((String) itr.next());
        }
        super.sql.clear();

        super.sql = new ArrayList<String>();
        aUS_countryAnn = em.find(Country.class, aUS_sid);
        itr = super.sql.iterator();
        while (itr.hasNext()) {
            Annsql.add((String) itr.next());
        }
        super.sql.clear();
        compareselectSQLs(Annsql, XMLsql);
        Annsql.clear();
        XMLsql.clear();

        super.sql = new ArrayList<String>();
        aI_security = em.find(Security1.class, aI_sid);
        itr = super.sql.iterator();
        while (itr.hasNext()) {
            XMLsql.add((String) itr.next());
        }
        super.sql.clear();

        super.sql = new ArrayList<String>();
        aI_securityAnn = em.find(Security.class, aI_sid);
        itr = super.sql.iterator();
        while (itr.hasNext()) {
            Annsql.add((String) itr.next());
        }
        super.sql.clear();
        compareselectSQLs(Annsql, XMLsql);
        Annsql.clear();
        XMLsql.clear();

        super.sql = new ArrayList<String>();
        Country1 aUS_country1 = aI_security.getCountry1();
        itr = super.sql.iterator();
        while (itr.hasNext()) {
            XMLsql.add((String) itr.next());
        }
        super.sql.clear();

        super.sql = new ArrayList<String>();
        Country aUS_country2 = aI_securityAnn.getCountry();
        itr = super.sql.iterator();
        while (itr.hasNext()) {
            Annsql.add((String) itr.next());
        }
        super.sql.clear();
        compareselectSQLs(Annsql, XMLsql);
        Annsql.clear();
        XMLsql.clear();

        // Close
        em.close();

    }
    
    public void testManyToOneEagerFetch() {
        // initialize objects
        EntityManager em = emf.createEntityManager();

        long aI_sid = 148007245;
        long aUS_sid = 2;

        Security1 aI_security = new Security1(aI_sid, new Embed("XYZ"));
        Country1 aUS_country = new Country1(aUS_sid, "USA");
        aI_security.setCountry1(aUS_country);
        aI_security.setCountryEager(aUS_country);
        
        Security aI_securityAnn = new Security(aI_sid, new Embed("XYZ"));
        Country aUS_countryAnn = new Country(aUS_sid, "USA");
        aI_securityAnn.setCountry(aUS_countryAnn);
        aI_securityAnn.setCountryEager(aUS_countryAnn);

        em.getTransaction().begin();
        em.persist(aI_security);
        em.persist(aUS_country);
        em.getTransaction().commit();
        em.clear();

        aI_security = em.find(Security1.class, aI_sid);
        em.clear();
        Country1 countryEager = aI_security.getCountryEager(); 
        assertNotNull(countryEager);
        
        aI_securityAnn = em.find(Security.class, aI_sid);
        em.clear();
        Country countryEagerAnn = aI_securityAnn.getCountryEager(); 
        assertNotNull(countryEagerAnn);
        
        // Close
        em.close();
        
    }
    

    private void printArrayList(ArrayList aList) {
        Iterator itr = aList.iterator();
        while (itr.hasNext()) {
            System.out.println(itr.next());
        }
    }

    /*
     * This method is not a genralized method that can compare any select
     * statement. It is customized only for this testcase.
     */
    private void compareselectSQLs(List<String> a, List<String> b) {
        assertEquals(a.size(), b.size());
        for (int i = 0; i < a.size(); i++) {
            // assertEquals(a.get(i), b.get(i));
            String aStr = a.get(i);
            String bStr = b.get(i);
            String[] aArr =
                (aStr.substring(aStr.indexOf("SELECT ") + 7, aStr
                    .indexOf(" FROM "))).split(",");
            String[] bArr =
                (bStr.substring(bStr.indexOf("SELECT ") + 7, bStr
                    .indexOf(" FROM "))).split(",");
            Arrays.sort(aArr);
            Arrays.sort(bArr);
            assertTrue(Arrays.equals(aArr, bArr));
            assertEquals(aStr.substring(aStr.indexOf("FROM ") + 5), bStr
                .substring(aStr.indexOf("FROM ") + 5));
        }
    }

}
