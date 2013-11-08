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
import javax.persistence.Query;

import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.jdbc.sql.MariaDBDictionary;
import org.apache.openjpa.jdbc.sql.MySQLDictionary;
import org.apache.openjpa.persistence.query.Customer.CreditRating;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Test JPQL subquery
 */
public class TestSubquery
    extends SingleEMFTestCase {

    public void setUp() {
        setUp(Customer.class, Customer.CustomerKey.class, Order.class,
            OrderItem.class, Magazine.class, Publisher.class, Employee.class,
            Dependent.class, DependentId.class, Account.class, DROP_TABLES);
    }

    static String[]  querys = new String[] {
        "select c from Customer c where EXISTS" +
            " (select o from  in(c.orders)  o)" , 
        "select c from Customer c where EXISTS" +
            " (select o from c.orders o)" , 
        "select c from Customer c where NOT EXISTS" +
            " (select o from in (c.orders) o)" , 
        "select c from Customer c where NOT EXISTS" +
            " (select o from  c.orders o)" , 
        "select o1.oid from Order o1 where o1.oid in " +
            " (select distinct o.oid from OrderItem i, Order o" +
            " where i.quantity > 10 and o.amount > 1000 and i.lid = o.oid)" ,
        "select o.oid from Order o where o.customer.name =" +
            " (select max(o2.customer.name) from Order o2" +
            " where o.customer.cid.id = o2.customer.cid.id)",
        "select o from Order o where o.customer.name =" +
            " (select max(o2.customer.name) from Order o2" +
            " where o.customer.cid.id = o2.customer.cid.id)",
        "select o.oid from Order o where o.amount >" +
            " (select count(i) from o.lineitems i)",
        "select o.oid from Order o where o.amount >" +
            " (select count(o.amount) from Order o)",
        "select o.oid from Order o where o.amount >" +
            " (select count(o.oid) from Order o)",
        "select o.oid from Order o where o.amount >" +
            " (select avg(o.amount) from Order o)",
        "select c.name from Customer c where exists" +
            " (select o from c.orders o where o.oid = 1) or exists" +
            " (select o from c.orders o where o.oid = 2)",
        "select c.name from Customer c, in(c.orders) o where o.amount " +
            "between" +
            " (select max(o.amount) from Order o) and" +
            " (select avg(o.amount) from Order o) ",
        "select o.oid from Order o where o.amount >" +
            " (select sum(o2.amount) from Customer c, in(c.orders) o2) ",   
        "select o.oid from Order o where o.amount between" +
            " (select avg(o2.amount) from Customer c, in(c.orders) o2)" +
            " and (select min(o2.amount) from Customer c, in(c.orders) o2)",
        "select o.oid from Customer c, in(c.orders)o where o.amount >" +
            " (select sum(o2.amount) from c.orders o2)",
        "select o1.oid, c.name from Order o1, Customer c where o1.amount = " +
            " any(select o2.amount from in(c.orders) o2)",
        "SELECT p, m "+
            "FROM Publisher p "+
            "LEFT OUTER JOIN p.magazineCollection m "+
            "WHERE m.id = (SELECT MAX(m2.id) "+
            "FROM Magazine m2 "+
            "WHERE m2.idPublisher.id = p.id "+
            "AND m2.datePublished = "+
            "(SELECT MAX(m3.datePublished) "+
            "FROM Magazine m3 "+
            "WHERE m3.idPublisher.id = p.id)) ", 
        "select o from Order o where o.amount > " +
            " (select count(o) from Order o)",
        "select o from Order o where o.amount > " +
            "(select count(o2) from Order o2)",
        "select c from Customer c left join c.orders o where not exists"
         + " (select o2 from c.orders o2 where o2 = o)",
    };

    static String[]  querys_jpa20 = new String[] {        
        "select o.oid from Order o where o.delivered =" +
            " (select " +
            "   CASE WHEN o2.amount > 10 THEN true" + 
            "     WHEN o2.amount = 10 THEN false " +
            "     ELSE false " +
            "     END " +
            " from Order o2" +
            " where o.customer.cid.id = o2.customer.cid.id)",
 
        "select o1.oid from Order o1 where o1.amount > " +
            " (select o.amount*0.8 from OrderItem i, Order o" +
            " where i.quantity > 10 and o.amount > 1000 and i.lid = o.oid)",
            
        "select o.oid from Order o where o.customer.name =" +
            " (select substring(o2.customer.name, 3) from Order o2" +
            " where o.customer.cid.id = o2.customer.cid.id)",
            
        "select o.oid from Order o where o.orderTs >" +
            " (select CURRENT_TIMESTAMP from o.lineitems i)",
            
        "select o.oid from Order o where o.amount >" +
            " (select SQRT(o.amount) from Order o where o.delivered = true)",
            
        "select o.oid from Order o where o.customer.name in" +
            " (select CONCAT(o.customer.name, 'XX') from Order o" +
            " where o.amount > 10)",  
            
        "select c from Customer c where c.creditRating =" +
            " (select " +
            "   CASE WHEN o2.amount > 10 THEN " + 
            "org.apache.openjpa.persistence.query.Customer$CreditRating.POOR" +
            "     WHEN o2.amount = 10 THEN " + 
            "org.apache.openjpa.persistence.query.Customer$CreditRating." +
            "GOOD " +
            "     ELSE " + 
            "org.apache.openjpa.persistence.query." +
            "Customer$CreditRating.EXCELLENT " +
            "     END " +
            " from Order o2" +
            " where c.cid.id = o2.customer.cid.id)",

        "select c from Customer c " + 
            "where c.creditRating = (select COALESCE (c1.creditRating, " + 
            "org.apache.openjpa.persistence.query." +
            "Customer$CreditRating.POOR) " +
            "from Customer c1 where c1.name = 'Famzy') order by c.name DESC", 
            
        "select c from Customer c " + 
            "where c.creditRating = (select NULLIF (c1.creditRating, " + 
            "org.apache.openjpa.persistence.query." +
            "Customer$CreditRating.POOR) " +
            "from Customer c1 where c1.name = 'Famzy') order by c.name DESC",
    };

    static String[] updates = new String[] {
        "update Order o set o.amount = 1000 where o.customer.name = " +
            " (select max(o2.customer.name) from Order o2 " + 
            " where o.customer.cid.id = o2.customer.cid.id)",  
    };

    static String[]  querys2 = new String[] {
            // 0
        "select o1.oid, c.name from Order o1, Customer c" +
            " where o1.customer.name = " + 
            " any(select o2.customer.name from in(c.orders) o2)",
            // 1
        "select o1.oid, c.name from Order o1, Customer c" +
            " where o1.amount = " +
            " any(select o2.amount from in(c.orders) o2)",
            // 2
        "select DISTINCT c.name FROM Customer c JOIN c.orders o " +
            "WHERE EXISTS (SELECT o FROM o.lineitems l where l.quantity > 2 ) ",
            // 3
        "select DISTINCT c.name FROM Customer c, IN(c.orders) co " +
            "WHERE co.amount > ALL " +
            "(Select o.amount FROM Order o, in(o.lineitems) l WHERE l.quantity > 2)", 
            // 4
        "select distinct c.name FROM Customer C, IN(C.orders) co " +
            "WHERE co.amount < ALL " +
            "(Select o.amount FROM Order o, IN(o.lineitems) l WHERE l.quantity > 2)", 
            //5
        "select c.name FROM Customer c, IN(c.orders) co " +
            "WHERE co.amount <= ALL " +
            "(Select o.amount FROM Order o, IN(o.lineitems) l WHERE l.quantity > 2)",
            // 6
        "select DISTINCT c.name FROM Customer c, IN(c.orders) co " +
            "WHERE co.amount > ANY " +
            "(Select o.amount FROM Order o, IN(o.lineitems) l WHERE l.quantity = 2)",
            // 7
        "select DISTINCT c.name FROM Customer c " +
            "WHERE EXISTS (SELECT o FROM c.orders o where o.amount " +
            "BETWEEN 1000 AND 1200)",
            // 8
        "select DISTINCT c.name FROM Customer c " +
            "WHERE EXISTS (SELECT o FROM c.orders o where o.amount > 1000 )",
            // 9
        "SELECT o.oid from Order o WHERE " +
            "EXISTS (SELECT c.name From o.customer c WHERE c.name LIKE '%los') ",
            // 10
        "select Distinct c.name FROM Customer c, IN(c.orders) co " +
            "WHERE co.amount >= SOME" +
            "(Select o.amount FROM Order o, IN(o.lineitems) l WHERE l.quantity = 2)",
            // 11
        "select c FROM Customer c WHERE EXISTS" +
            " (SELECT o FROM c.orders o where o.amount > 1000)",
            // 12
        "select c FROM Customer c WHERE EXISTS" +
            " (SELECT o FROM c.orders o)",
            // 13
        "SELECT c FROM Customer c WHERE "
            + "(SELECT COUNT(o) FROM c.orders o) > 10",
            // 14
        "SELECT o FROM Order o JOIN o.customer c WHERE c.name = "
            + "SOME (SELECT a.name FROM c.accounts a)",

        };

    public void testSubquery2() {
        EntityManager em = emf.createEntityManager();
        for (int i = 0; i < querys2.length; i++) {
            String q = querys2[i];
            System.err.println(">>> JPQL JPA2 :[ " + i + "]" +q);
            List rs = em.createQuery(q).getResultList();
            assertEquals(0, rs.size());
        }
        em.close();
    }


    public void testSubquery() {
        JDBCConfiguration conf = (JDBCConfiguration) emf.getConfiguration();
        DBDictionary dict = conf.getDBDictionaryInstance();
        
        EntityManager em = emf.createEntityManager();
        for (int i = 0; i < querys_jpa20.length; i++) {
            String q = querys_jpa20[i];
            System.err.println(">>> JPQL JPA2 :[ " + i + "]" +q);
            List rs = em.createQuery(q).getResultList();
            assertEquals(0, rs.size());
        }
        for (int i = 0; i < querys.length; i++) {
            String q = querys[i];
            System.err.println(">>> JPQL: [ " + i + "]"+q);
            List rs = em.createQuery(q).getResultList();
            assertEquals(0, rs.size());
        }

        // MySQL throws exception for the jpql in the updates:
        // "You can't specify target table 'xxx' for update in FROM clause". The MySQL manual mentions 
        // this at the bottom of the UPDATE documentation(http://dev.mysql.com/doc/refman/5.0/en/update.html): 
        // Currently, you cannot update a table and select from the same table in a subquery.
        
        if (dict instanceof MySQLDictionary || dict instanceof MariaDBDictionary)
            return;
        
        em.getTransaction().begin();
        for (int i = 0; i < updates.length; i++) {
            int updateCount = em.createQuery(updates[i]).executeUpdate();
            assertEquals(0, updateCount);
        }

        em.getTransaction().rollback();
        em.close();
    }
    
    /**
     * Verify a sub query can contain MAX and additional date comparisons 
     * without losing the correct alias information. This sort of query 
     * originally caused problems for DBDictionaries which used DATABASE 
     * syntax.
     */
    public void testSubSelectMaxDateRange() {        
        String query =
            "SELECT e,d from Employee e, Dependent d "
                + "WHERE e.empId = :empid "
                + "AND d.id.empid = (SELECT MAX (e2.empId) FROM Employee e2) "
                + "AND d.id.effDate > :minDate "
                + "AND d.id.effDate < :maxDate ";
        EntityManager em = emf.createEntityManager();
        Query q = em.createQuery(query);
        q.setParameter("empid", (long) 101);
        q.setParameter("minDate", new Date(100));
        q.setParameter("maxDate", new Date(100000));
        q.getResultList();
        em.close();
    }

    public void testUpdateWithCorrelatedSubquery() {
        String update = "update Customer c set c.creditRating = ?1 where EXISTS" +
           " (select o from  in(c.orders)  o)";
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        CreditRating creditRating = CreditRating.GOOD;
        int updateCount = em.createQuery(update).
            setParameter(1, creditRating).executeUpdate();
        em.getTransaction().rollback();
        em.close();
    }
}
