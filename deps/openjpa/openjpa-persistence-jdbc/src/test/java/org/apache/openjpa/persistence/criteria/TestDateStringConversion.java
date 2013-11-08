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
package org.apache.openjpa.persistence.criteria;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;

import javax.persistence.Query;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.apache.openjpa.kernel.Filters;

/**
 * Test JDBC Escape Syntax date/time strings in query.
 * 
 * 
 * @author Pinaki Poddar
 *
 */
public class TestDateStringConversion extends CriteriaTest {
    private static final String OPEN_BRACKET  = "{";
    private static final String CLOSE_BRACKET = "}";
    private static final String SINGLE_QUOTE  = "'";
    
    void createData(String name) {
        long now = System.currentTimeMillis();
        long tomorrow = now + 24*60*60*1000+1;
        
        Date date = new Date(tomorrow);
        DependentId id = new DependentId();
        id.setEffDate(date);
        id.setName(name);
        Dependent pc = new Dependent();
        pc.setId(id);
        pc.setEndDate(date);
        em.getTransaction().begin();
        em.persist(pc);
        em.getTransaction().commit();
    }
    
    /**
     * The persistent property Dependent.endDate being tested is declared as Date but mapped with
     * @Temporal(TemporalType.TIMESTAMP). 
     * 
     * When executing JPQL, the query string directly uses a JDBC Escape Syntax that is passed
     * to JDBC Driver and hence that string should encode a Timestamp.
     * 
     * Criteria Query, on the other hand, must conform to the API signature and hence the expression
     * that takes the JDBC Escape Syntax must be cast to Date.class to match the declared type of
     * Dependent.endDate.
     * 
     */
    public void testDateString() {
        createData("testDateString");
        long now = System.currentTimeMillis();
        
        String dateString = createJDBCEscapeString(new Date(now));
        String tsString = createJDBCEscapeString(new Timestamp(now));
        
        // add Timestamp string because Dependent.endDate is mapped to TemporalType.TimeStamp
        String jpql = "select d from Dependent d where d.endDate >= " + tsString + " ORDER BY d.endDate";
        
        
        CriteriaQuery<Dependent> c = cb.createQuery(Dependent.class);
        Root<Dependent> d = c.from(Dependent.class);
        // the literal is cast to Date.class because Dependent.endDate is declared as Date
        c.where(cb.greaterThanOrEqualTo(d.get(Dependent_.endDate), cb.literal(dateString).as(Date.class)));
        c.orderBy(cb.asc(d.get(Dependent_.endDate)));
        
        // can not compare SQL because JPQL and Criteria handles JDBC escape syntax in 
        // a different way.
        assertSameResult(c, jpql);
    }
    
    /**
     * A similar query but the JDBC Escape String is passed as an parameter.
     * Parameter value binding is relaxed to accept the string as a value
     * of temporal type instance.
     * Hence does not require, as in the the earlier case, to use different 
     * escape string. 
     */
    public void testDateStringAsParameter() {
        createData("testDateStringAsParameter");
        long now = System.currentTimeMillis();
        
        Date earlier = new Date(now - 1000);
        
        final String dateString = createJDBCEscapeString(earlier);
        String jpql = "select d from Dependent d where d.endDate >= :dateString ORDER BY d.endDate";
        CriteriaQuery<Dependent> c = cb.createQuery(Dependent.class);
        Root<Dependent> d = c.from(Dependent.class);
        
        c.where(cb.greaterThanOrEqualTo(d.get(Dependent_.endDate), cb.parameter(String.class, "dateString")
                .as(Date.class)));
        c.orderBy(cb.asc(d.get(Dependent_.endDate)));
        
        assertEquivalence(new QueryDecorator() {
            public void decorate(Query q) {
                q.setParameter("dateString", dateString);
            }
        }, c, jpql);

    }
    
    String createJDBCEscapeString(Object time) {
        String key = "";
        if (time instanceof Date)
            key = "d ";
        else if (time instanceof Time)
            key = "t ";
        else if (time instanceof Timestamp)
            key = "ts ";
        else {
            fail("Wrong object " + time + " of " + time.getClass() + " for JDBC conversion");
        }
        return OPEN_BRACKET + key + SINGLE_QUOTE + time.toString() + SINGLE_QUOTE + CLOSE_BRACKET;
    }
    
    public void testJDBCEscapeSyntaxTimestamp() {
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        String s = "{ts '" + ts.toString() + "'}";
        assertTrue(Filters.isJDBCTemporalSyntax(s));
        assertNotNull(Filters.parseJDBCTemporalSyntax(s));
        Object converted = Filters.convert(s, Timestamp.class);
        assertTrue(converted instanceof Timestamp);
        assertEquals("Original=" + s + " Converted " + converted, ts.toString(), converted.toString());
    }
    
    public void testJDBCEscapeSyntaxTime() {
        Time t = new Time(System.currentTimeMillis());
        String s = "{t '" + t.toString() + "'}";
        assertTrue(Filters.isJDBCTemporalSyntax(s));
        assertNotNull(Filters.parseJDBCTemporalSyntax(s));
        Object converted = Filters.convert(s, Time.class);
        assertTrue(converted instanceof Time);
        assertEquals("Original=" + s + " Converted " + converted, t.toString(), converted.toString());
    }
    
    public void testJDBCEscapeSyntaxDate() {
        Date d = new Date(System.currentTimeMillis());
        String s = "{d '" + d.toString() + "'}";
        assertTrue(Filters.isJDBCTemporalSyntax(s));
        assertNotNull(Filters.parseJDBCTemporalSyntax(s));
        Object converted = Filters.convert(s, Date.class);
        assertTrue(converted instanceof Date);
        assertEquals("Original=" + s + " Converted " + converted, d.toString(), converted.toString());
    }
    
    void assertSameResult(CriteriaQuery<Dependent> c, String jpql) {
        List<Dependent> jResult = em.createQuery(jpql).getResultList();
        List<Dependent> cResult = em.createQuery(c).getResultList();
        assertFalse(jResult.isEmpty());
        assertEquals(cResult.size(), jResult.size());
        for (int i = 0; i < jResult.size(); i++) {
            assertEquals(jResult.get(i).getEndDate(), cResult.get(i).getEndDate());
        }
    }
}
