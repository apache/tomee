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
package org.apache.openjpa.persistence.query;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import junit.framework.Assert;

import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.jdbc.sql.HSQLDictionary;
import org.apache.openjpa.jdbc.sql.PostgresDictionary;
import org.apache.openjpa.jdbc.sql.SQLServerDictionary;
import org.apache.openjpa.jdbc.sql.SybaseDictionary;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Test JDBC escape syntax for date, time, and timestamp literals
 */
public class TestJDBCEscapeDate extends SingleEMFTestCase {

    public void setUp() {
        setUp(Employee.class, DROP_TABLES);
    }

    public void testJDBCEscape() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tran = em.getTransaction();
        Employee e = new Employee();
        e.setEmpId(1);
        e.setName("name1");
        e.setHireDate(new Date());
        e.setHireTime(new Date());
        e.setHireTimestamp(new Date());
        em.persist(e);
        tran.begin();
        em.flush();
        tran.commit();
        em.clear();

        String[] jpql;
        DBDictionary dict = ((JDBCConfiguration)emf.getConfiguration()).getDBDictionaryInstance();
        if ((dict instanceof SQLServerDictionary) || (dict instanceof HSQLDictionary)) {
            jpql = new String[] {
                // some changes to the jpql strings had to be made for MSSQL and HSQLDB
                "select a from Employee a where a.hireDate >= {d '2009-08-25'}",
                "select a from Employee a where a.hireDate >= {d '2009-08-05'}",    // requires yyyy-mm-dd
                // "select a from Employee a where a.hireTime >= {t '00:00:00'}",   // fails ?
                "select a from Employee a where a.hireTimestamp >= {ts '2009-08-25 00:00:00'}",
                "select a from Employee a where a.hireTimestamp >= {ts '2009-08-25 00:00:00.'}",
                "select a from Employee a where a.hireTimestamp >= {ts '2009-08-25 00:00:00.1'}",
                "select a from Employee a where a.hireTimestamp >= {ts '2009-08-25 00:00:00.12'}",
                "select a from Employee a where a.hireTimestamp >= {ts '2009-08-25 00:00:00.123'}",
                // "select a from Employee a where a.hireTimestamp >= {ts '2009-08-25 00:00:00.1234'}", // more than 3
                // "select a from Employee a where a.hireTimestamp >= {ts '2009-08-25 00:00:00.12345'}", // fails
                // "select a from Employee a where a.hireTimestamp >= {ts '2009-08-25 00:00:00.123456'}",
                "select {t '00:00:00'}, a.empId from Employee a",
            };
        } else if ((dict instanceof SybaseDictionary)) {
            jpql = new String[] {
                "select a from Employee a where a.hireDate >= {d '2009-08-25'}",
                "select a from Employee a where a.hireDate >= {d '2009-8-5'}",    
                "select a from Employee a where a.hireTime >= {t '00:00:00'}",  
                "select a from Employee a where a.hireTimestamp >= {ts '2009-08-25 00:00:00'}",
                "select a from Employee a where a.hireTimestamp >= {ts '2009-08-25 00:00:00.0'}",
                "select a from Employee a where a.hireTimestamp >= {ts '2009-08-25 00:00:00.1'}",
                "select a from Employee a where a.hireTimestamp >= {ts '2009-08-25 00:00:00.12'}",
                "select a from Employee a where a.hireTimestamp >= {ts '2009-08-25 00:00:00.123'}",
                "select a from Employee a where a.hireTimestamp >= {ts '2009-08-25 00:00:00.1234'}",
                "select a from Employee a where a.hireTimestamp >= {ts '2009-08-25 00:00:00.12345'}", 
                "select a from Employee a where a.hireTimestamp >= {ts '2009-08-25 00:00:00.123456'}",
                "select {t '00:00:00'}, a.empId from Employee a",
            };
        } else if (dict instanceof PostgresDictionary) {
            jpql = new String[] {
                "select a from Employee a where a.hireDate >= {d '2009-08-25'}",
                "select a from Employee a where a.hireDate >= {d '2009-8-5'}",
                "select a from Employee a where a.hireTime >= {t '00:00:00'}",
                "select a from Employee a where a.hireTimestamp >= {ts '2009-08-25 00:00:00'}",
                //"select a from Employee a where a.hireTimestamp >= {ts '2009-08-25 00:00:00.'}",
                "select a from Employee a where a.hireTimestamp >= {ts '2009-08-25 00:00:00.1'}",
                "select a from Employee a where a.hireTimestamp >= {ts '2009-08-25 00:00:00.12'}",
                "select a from Employee a where a.hireTimestamp >= {ts '2009-08-25 00:00:00.123'}",
                "select a from Employee a where a.hireTimestamp >= {ts '2009-08-25 00:00:00.1234'}",
                "select a from Employee a where a.hireTimestamp >= {ts '2009-08-25 00:00:00.12345'}",
                "select a from Employee a where a.hireTimestamp >= {ts '2009-08-25 00:00:00.123456'}",
                "select {t '00:00:00'}, a.empId from Employee a",
            };
        } else {
            jpql = new String[] {
                    "select a from Employee a where a.hireDate >= {d '2009-08-25'}",
                    "select a from Employee a where a.hireDate >= {d '2009-8-5'}",
                    "select a from Employee a where a.hireTime >= {t '00:00:00'}",
                    "select a from Employee a where a.hireTimestamp >= {ts '2009-08-25 00:00:00'}",
                    "select a from Employee a where a.hireTimestamp >= {ts '2009-08-25 00:00:00.'}",
                    "select a from Employee a where a.hireTimestamp >= {ts '2009-08-25 00:00:00.1'}",
                    "select a from Employee a where a.hireTimestamp >= {ts '2009-08-25 00:00:00.12'}",
                    "select a from Employee a where a.hireTimestamp >= {ts '2009-08-25 00:00:00.123'}",
                    "select a from Employee a where a.hireTimestamp >= {ts '2009-08-25 00:00:00.1234'}",
                    "select a from Employee a where a.hireTimestamp >= {ts '2009-08-25 00:00:00.12345'}",
                    "select a from Employee a where a.hireTimestamp >= {ts '2009-08-25 00:00:00.123456'}",
                    "select {t '00:00:00'}, a.empId from Employee a",
                };
        }
        

        for (int i = 0; i < jpql.length; i++) {
            Query q = em.createQuery(jpql[i]);
            List results = q.getResultList();
            Assert.assertEquals("For jpql["+i+"]", 1, results.size());
        }
        
        // Test support in HAVING clause.
        String[] havingJpql = {
            "select a from Employee a group by a.hireTime having a.hireTime >= {t '00:00:00'}",
            "select a from Employee a group by a.hireDate having a.hireDate >= {d '2009-08-25'}",
            "select a from Employee a group by a.hireTimestamp having a.hireTimestamp >= {d '2009-08-25'}"
        };

        for (int j = 0; j < havingJpql.length; j++) {
            Query q = em.createQuery(havingJpql[j]);
            List results = q.getResultList();
            Assert.assertEquals("For havingJpql["+j+"]", 1, results.size());
        }
        em.getTransaction().begin();
        String update;
        if ((dict instanceof SQLServerDictionary) || (dict instanceof HSQLDictionary)) {
            // more than 3 digits after 00:00:00. fails on MSSQL and HSQLDB
            update = "update Employee a set a.hireTimestamp = {ts '2009-08-25 00:00:00.123'} where a.empId = 1";
        } else {
            update = "update Employee a set a.hireTimestamp = {ts '2009-08-25 00:00:00.123456'} where a.empId = 1";
        }
        Query q = em.createQuery(update);
        int updateCnt = q.executeUpdate();
        em.getTransaction().commit();
        Assert.assertEquals(1, updateCnt);
        em.close();
    }
}
