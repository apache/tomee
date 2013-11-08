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

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

import javax.persistence.EntityManager;

import junit.textui.TestRunner;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Simple test case to test the default values associated with the @Basic 
 * annotation.
 *
 * @author Kevin Sutter
 */
public class TestBasicAnnotation
    extends SingleEMFTestCase {

    public void setUp() {
        setUp(CLEAR_TABLES, AllFieldTypes.class);
    }

    public void testEagerFetchType() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        AllFieldTypes aft = new AllFieldTypes();
        
        // Initialize a sampling of the types
        aft.setBigDecimalField(new BigDecimal(1));
        aft.setBooleanField(false);
        aft.setByteLob(new byte[1]);
        aft.setCalendarField(Calendar.getInstance());
        aft.setDateField(new Date());
        aft.setEnumField(AllFieldTypes.EnumType.Value1);
        aft.setSerializableField(new Date());
        aft.setSqlTimestampField(new java.sql.Timestamp(
                System.currentTimeMillis()));
        aft.setStringField("aft");
        aft.setWByteLob(new Byte[1]);
        aft.setWDoubleField(new Double(1));
        
        em.persist(aft);
        em.getTransaction().commit();
        em.clear();
        
        AllFieldTypes aftQuery = (AllFieldTypes)em.createQuery
            ("select x from AllFieldTypes x where x.stringField = 'aft'").
            getSingleResult();
        em.clear();  // ensure detached
        assertFalse(em.contains(aftQuery));
        
        // assert that the sampling of fields are not null
        assertNotNull(aftQuery.getBigDecimalField());
        assertNotNull(aftQuery.getBooleanField());
        assertNotNull(aftQuery.getByteLob());
        assertNotNull(aftQuery.getCalendarField());
        assertNotNull(aftQuery.getDateField());
        assertNotNull(aftQuery.getEnumField());
        assertNotNull(aftQuery.getSerializableField());
        assertNotNull(aftQuery.getSqlTimestampField());
        assertNotNull(aftQuery.getStringField());
        assertNotNull(aftQuery.getWByteLob());
        assertNotNull(aftQuery.getWDoubleField());
        
        em.close();
    }

    public static void main(String[] args) {
        TestRunner.run(TestBasicAnnotation.class);
    }
}

