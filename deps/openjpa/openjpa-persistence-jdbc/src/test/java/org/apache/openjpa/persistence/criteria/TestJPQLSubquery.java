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

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

import javax.persistence.Query;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;
import javax.persistence.criteria.Subquery;

import org.apache.openjpa.jdbc.sql.DerbyDictionary;

/**
 * Tests type-strict version of Criteria API.
 * 
 * Most of the tests build Criteria Query and then execute the query as well as
 * a reference JPQL query supplied as a string. The test is validated by
 * asserting that the resultant SQL queries for these two alternative form of
 * executing a query are the same.
 * 
 * 
 */
public class TestJPQLSubquery extends CriteriaTest {
    
    public void testSubqueries1() {
        String jpql = "SELECT goodCustomer FROM Customer goodCustomer WHERE "
            + "goodCustomer.balanceOwed < (SELECT AVG(c.balanceOwed) "
            + " FROM Customer c)";
        String expectedSQL = "SELECT t0.id, t0.accountNum, t2.id, t2.city, t2.country, t2.county, t2.state, "
            + "t2.street, t3.userid, t3.DTYPE, t3.age, t3.compName, t3.creditRating, t3.name, t2.zipCode, "
            + "t0.balanceOwed, t0.creditRating, t0.filledOrderCount, t0.firstName, t0.lastName, t0.name, t0.status "
            + "FROM CR_CUST t0 LEFT OUTER JOIN CR_ADDR t2 ON t0.ADDRESS_ID = t2.id "
            + "LEFT OUTER JOIN CompUser t3 ON t2.id = t3.ADD_ID "
            + "WHERE (t0.balanceOwed < (SELECT AVG(t1.balanceOwed) FROM CR_CUST t1))";

        executeAndCompareSQL(jpql, expectedSQL);

        CriteriaQuery<Customer> q = cb.createQuery(Customer.class);
        Root<Customer> goodCustomer = q.from(Customer.class);
        q.select(goodCustomer);
        Subquery<Double> sq = q.subquery(Double.class);
        Root<Customer> c = sq.from(Customer.class);
        q.where(cb.lt(goodCustomer.get(Customer_.balanceOwed), sq
                .select(cb.avg(c.get(Customer_.balanceOwed)))));
        
        assertEquivalence(q, jpql);
    }

    public void testSubqueries2() {
        String jpql = "SELECT DISTINCT emp FROM Employee emp WHERE EXISTS ("
            + "SELECT spouseEmp FROM Employee spouseEmp WHERE spouseEmp ="
            + " emp.spouse)";
        String expectedSQL = "SELECT t1.empId, t1.EMP_TYPE, t2.id, t2.city, t2.country, t2.county, t2.state, "
            + "t2.street, t3.userid, t3.DTYPE, t3.age, t3.compName, t3.creditRating, t3.name, t2.zipCode, t4.deptNo, "
            + "t4.name, t5.id, t5.annualMiles, t5.name, t6.id, t7.deptNo, t7.name, t6.name, t6.salary, t1.name, "
            + "t1.rating, t1.salary, t8.empId, t8.EMP_TYPE, t8.ADDRESS_ID, t8.DEPARTMENT_DEPTNO, "
            + "t8.FREQUENTFLIERPLAN_ID, "
            + "t8.MANAGER_ID, t8.name, t8.rating, t8.salary, t8.hireDate, t1.hireDate "
            + "FROM CR_EMP t1 LEFT OUTER JOIN CR_ADDR t2 ON t1.ADDRESS_ID = t2.id "
            + "LEFT OUTER JOIN CR_DEPT t4 ON t1.DEPARTMENT_DEPTNO = t4.deptNo "
            + "LEFT OUTER JOIN FrequentFlierPlan t5 ON t1.FREQUENTFLIERPLAN_ID = t5.id "
            + "LEFT OUTER JOIN CR_MGR t6 ON t1.MANAGER_ID = t6.id "
            + "LEFT OUTER JOIN CR_EMP t8 ON t1.SPOUSE_EMPID = t8.empId "
            + "LEFT OUTER JOIN CompUser t3 ON t2.id = t3.ADD_ID "
            + "LEFT OUTER JOIN CR_DEPT t7 ON t6.DEPARTMENT_DEPTNO = t7.deptNo "
            + "WHERE (EXISTS (SELECT t0.empId FROM CR_EMP t0 WHERE (t0.empId = t1.SPOUSE_EMPID)))";

        executeAndCompareSQL(jpql, expectedSQL);

        CriteriaQuery<Employee> q = cb.createQuery(Employee.class);
        Root<Employee> emp = q.from(Employee.class);
        q.select(emp).distinct(true);
        Subquery<Employee> sq = q.subquery(Employee.class);
        Root<Employee> spouseEmp = sq.from(Employee.class);
        sq.where(cb.equal(spouseEmp, emp.get(Employee_.spouse)));
        q.where(cb.exists(sq.select(spouseEmp)));
        
        assertEquivalence(q, jpql);
    }

    public void testSubqueries3() {
        String jpql = "SELECT emp FROM Employee emp WHERE emp.salary > ALL ("
            + "SELECT m.salary FROM Manager m WHERE m.department = "
            + "emp.department)";
        String expectedSQL = "SELECT t0.empId, t0.EMP_TYPE, t2.id, t2.city, t2.country, t2.county, t2.state, "
            + "t2.street, t3.userid, t3.DTYPE, t3.age, t3.compName, t3.creditRating, t3.name, t2.zipCode, t4.deptNo, "
            + "t4.name, t5.id, t5.annualMiles, t5.name, t6.id, t7.deptNo, t7.name, t6.name, t6.salary, t0.name, "
            + "t0.rating, t0.salary, t8.empId, t8.EMP_TYPE, t8.ADDRESS_ID, t8.DEPARTMENT_DEPTNO, "
            + "t8.FREQUENTFLIERPLAN_ID, t8.MANAGER_ID, t8.name, t8.rating, t8.salary, t8.hireDate, t0.hireDate "
            + "FROM CR_EMP t0 LEFT OUTER JOIN CR_ADDR t2 ON t0.ADDRESS_ID = t2.id "
            + "LEFT OUTER JOIN CR_DEPT t4 ON t0.DEPARTMENT_DEPTNO = t4.deptNo "
            + "LEFT OUTER JOIN FrequentFlierPlan t5 ON t0.FREQUENTFLIERPLAN_ID = t5.id "
            + "LEFT OUTER JOIN CR_MGR t6 ON t0.MANAGER_ID = t6.id "
            + "LEFT OUTER JOIN CR_EMP t8 ON t0.SPOUSE_EMPID = t8.empId "
            + "LEFT OUTER JOIN CompUser t3 ON t2.id = t3.ADD_ID "
            + "LEFT OUTER JOIN CR_DEPT t7 ON t6.DEPARTMENT_DEPTNO = t7.deptNo "
            + "WHERE (t0.salary > ALL (SELECT t1.salary "
            + "FROM CR_MGR t1 WHERE (t1.DEPARTMENT_DEPTNO = t0.DEPARTMENT_DEPTNO)))";

        executeAndCompareSQL(jpql, expectedSQL);

        CriteriaQuery<Employee> q = cb.createQuery(Employee.class);
        Root<Employee> emp = q.from(Employee.class);
        q.select(emp);
        Subquery<BigDecimal> sq = q.subquery(BigDecimal.class);
        Root<Manager> m = sq.from(Manager.class);
        sq.where(cb.equal(m.get(Manager_.department), emp.get(Employee_.department)));
        sq.select(m.get(Manager_.salary));
        q.where(cb.gt(emp.get(Employee_.salary), cb.all(sq)));
        
        assertEquivalence(q, jpql);
    }

    public void testSubqueries4() {
        String jpql = "SELECT c FROM Customer c WHERE "
            + "(SELECT COUNT(o) FROM c.orders o) > 10";
        String expectedSQL = "SELECT t0.id, t0.accountNum, t3.id, t3.city, t3.country, t3.county, t3.state, "
            + "t3.street, t4.userid, t4.DTYPE, t4.age, t4.compName, t4.creditRating, t4.name, t3.zipCode, " 
            + "t0.balanceOwed, t0.creditRating, "
            + "t0.filledOrderCount, t0.firstName, t0.lastName, t0.name, t0.status "
            + "FROM CR_CUST t0 LEFT OUTER JOIN CR_ADDR t3 ON t0.ADDRESS_ID = t3.id "
            + "LEFT OUTER JOIN CompUser t4 ON t3.id = t4.ADD_ID "
            + "WHERE ((SELECT COUNT(t2.id) FROM CR_ODR t1, CR_ODR t2 WHERE (t1.id = t2.id) AND "
            + "(t0.id = t1.CUSTOMER_ID)) > ?)";

        executeAndCompareSQL(jpql, expectedSQL);

        CriteriaQuery<Customer> q = cb.createQuery(Customer.class);
        Root<Customer> c = q.from(Customer.class);
        q.select(c);
        Subquery<Long> sq = q.subquery(Long.class);
        Root<Customer> csq = sq.correlate(c);
        Join<Customer, Order> o = csq.join(Customer_.orders);
        q.where(cb.gt(sq.select(cb.count(o)), 10));

        assertEquivalence(q, jpql);
    }

    public void testSubqueries4a() {
        String jpql = "SELECT c FROM Customer c WHERE "
            + "(SELECT COUNT(o) FROM Customer c1 JOIN c1.orders o WHERE c = c1) > 10";
        String expectedSQL = "SELECT t2.id, t2.accountNum, t3.id, t3.city, t3.country, t3.county, t3.state, "
            + "t3.street, t4.userid, t4.DTYPE, t4.age, t4.compName, t4.creditRating, t4.name, t3.zipCode, " 
            + "t2.balanceOwed, t2.creditRating, t2.filledOrderCount, t2.firstName, t2.lastName, t2.name, t2.status "
            + "FROM CR_CUST t2 LEFT OUTER JOIN CR_ADDR t3 ON t2.ADDRESS_ID = t3.id "
            + "LEFT OUTER JOIN CompUser t4 ON t3.id = t4.ADD_ID "
            + "WHERE ((SELECT COUNT(t1.id) "
            + "FROM CR_CUST t0 INNER JOIN CR_ODR t1 ON t0.id = t1.CUSTOMER_ID WHERE (t2.id = t0.id)) > ?)";

        executeAndCompareSQL(jpql, expectedSQL);

        CriteriaQuery<Customer> q = cb.createQuery(Customer.class);
        Root<Customer> c = q.from(Customer.class);
        q.select(c);
        Subquery<Long> sq = q.subquery(Long.class);
        Root<Customer> c1 = sq.from(Customer.class);
        Join<Customer, Order> o = c1.join(Customer_.orders);
        sq.where(cb.equal(c, c1));
        q.where(cb.gt(sq.select(cb.count(o)), 10));

        assertEquivalence(q, jpql);
    }    

    public void testSubqueries4b() {
        String jpql = "SELECT c FROM Customer c WHERE "
            + "(SELECT COUNT(o) FROM Customer c1 JOIN c1.orders o WHERE c.id = c1.id) > 10";

        String expectedSQL = "SELECT t2.id, t2.accountNum, t3.id, t3.city, t3.country, t3.county, t3.state, "
            + "t3.street, t4.userid, t4.DTYPE, t4.age, t4.compName, t4.creditRating, t4.name, t3.zipCode, " 
            + "t2.balanceOwed, t2.creditRating, t2.filledOrderCount, t2.firstName, t2.lastName, t2.name, t2.status "
            + "FROM CR_CUST t2 LEFT OUTER JOIN CR_ADDR t3 ON t2.ADDRESS_ID = t3.id "
            + "LEFT OUTER JOIN CompUser t4 ON t3.id = t4.ADD_ID "
            + "WHERE ((SELECT COUNT(t1.id) "
            + "FROM CR_CUST t0 INNER JOIN CR_ODR t1 ON t0.id = t1.CUSTOMER_ID WHERE (t2.id = t0.id)) > ?)";

        executeAndCompareSQL(jpql, expectedSQL);

        CriteriaQuery<Customer> q = cb.createQuery(Customer.class);
        Root<Customer> c = q.from(Customer.class);
        q.select(c);
        Subquery<Long> sq = q.subquery(Long.class);
        Root<Customer> c1 = sq.from(Customer.class);
        Join<Customer, Order> o = c1.join(Customer_.orders);
        sq.where(cb.equal(c.get(Customer_.id), c1.get(Customer_.id)));
        q.where(cb.gt(sq.select(cb.count(o)), 10));

        assertEquivalence(q, jpql);
    }

    public void testSubqueries4c() {
        String jpql = "SELECT c FROM Customer c WHERE "
            + "(SELECT COUNT(o) FROM Customer c1 JOIN c1.orders o) > 10";
        String expectedSQL = "SELECT t2.id, t2.accountNum, t3.id, t3.city, t3.country, t3.county, t3.state, "
            + "t3.street, t4.userid, t4.DTYPE, t4.age, t4.compName, t4.creditRating, t4.name, t3.zipCode, "
            + "t2.balanceOwed, t2.creditRating, t2.filledOrderCount, t2.firstName, t2.lastName, t2.name, t2.status "
            + "FROM CR_CUST t2 LEFT OUTER JOIN CR_ADDR t3 ON t2.ADDRESS_ID = t3.id "
            + "LEFT OUTER JOIN CompUser t4 ON t3.id = t4.ADD_ID "
            + "WHERE ((SELECT COUNT(t1.id) FROM CR_CUST t0 INNER JOIN CR_ODR t1 ON t0.id = t1.CUSTOMER_ID) > ?)";

        executeAndCompareSQL(jpql, expectedSQL);

        CriteriaQuery<Customer> q = cb.createQuery(Customer.class);
        Root<Customer> c = q.from(Customer.class);
        q.select(c);
        Subquery<Long> sq = q.subquery(Long.class);
        Root<Customer> c1 = sq.from(Customer.class);
        Join<Customer, Order> o = c1.join(Customer_.orders);
        q.where(cb.gt(sq.select(cb.count(o)), 10));

        assertEquivalence(q, jpql);
    }

    public void testSubqueries4d() {
        String jpql = "SELECT c FROM Customer c WHERE (SELECT COUNT(o) "
            + "FROM Customer c1 JOIN c1.orders o WHERE c.address.county = c1.address.county) > 10";

        String expectedSQL = "SELECT t2.id, t2.accountNum, t5.id, t5.city, t5.country, t5.county, "
            + "t5.state, t5.street, t6.userid, t6.DTYPE, t6.age, t6.compName, t6.creditRating, t6.name, "
            + "t5.zipCode, t2.balanceOwed, t2.creditRating, t2.filledOrderCount, t2.firstName, t2.lastName, "
            + "t2.name, t2.status "
            + "FROM CR_CUST t2 "
            + "INNER JOIN CR_ADDR t3 ON t2.ADDRESS_ID = t3.id "
            + "LEFT OUTER JOIN CR_ADDR t5 ON t2.ADDRESS_ID = t5.id "
            + "LEFT OUTER JOIN CompUser t6 ON t5.id = t6.ADD_ID "
            + "WHERE ((SELECT COUNT(t1.id) "
            + "FROM CR_CUST t0 "
            + "INNER JOIN CR_ODR t1 ON t0.id = t1.CUSTOMER_ID "
            + "INNER JOIN CR_ADDR t4 ON t0.ADDRESS_ID = t4.id "
            + "WHERE (t3.county = t4.county)) > ?)";

        executeAndCompareSQL(jpql, expectedSQL);
        
        CriteriaQuery<Customer> q = cb.createQuery(Customer.class);
        Root<Customer> c = q.from(Customer.class);
        q.select(c);
        Subquery<Long> sq = q.subquery(Long.class);
        Root<Customer> c1 = sq.from(Customer.class);
        Join<Customer, Order> o = c1.join(Customer_.orders);
        sq.where(cb.equal(c.get(Customer_.address).get(Address_.county), 
            c1.get(Customer_.address).get(Address_.county)));
        q.where(cb.gt(sq.select(cb.count(o)), 10));

        assertEquivalence(q, jpql);
    }

    public void testSubqueries4e() {
        String jpql = "SELECT c FROM Customer c WHERE EXISTS "
            + "(SELECT o.id FROM Order o WHERE o.customer = c)";
        String expectedSQL = "SELECT t1.id, t1.accountNum, t2.id, t2.city, t2.country, t2.county, t2.state, " + 
        "t2.street, t3.userid, t3.DTYPE, t3.age, t3.compName, t3.creditRating, t3.name, t2.zipCode, t1.balanceOwed, " + 
        "t1.creditRating, t1.filledOrderCount, t1.firstName, t1.lastName, t1.name, t1.status " + 
        "FROM CR_CUST t1 " + 
        "LEFT OUTER JOIN CR_ADDR t2 ON t1.ADDRESS_ID = t2.id " + 
        "LEFT OUTER JOIN CompUser t3 ON t2.id = t3.ADD_ID WHERE " + 
        "(EXISTS (SELECT t0.id FROM CR_ODR t0 WHERE (t0.CUSTOMER_ID = t1.id)))";

        executeAndCompareSQL(jpql, expectedSQL);
        
        CriteriaQuery<Customer> q = cb.createQuery(Customer.class);
        Root<Customer> c = q.from(Customer.class);
        q.select(c);
        Subquery<Integer> sq = q.subquery(Integer.class);
        Root<Order> o = sq.from(Order.class);
        sq.where(cb.equal(o.get(Order_.customer), c));
        sq.select(o.get(Order_.id));
        q.where(cb.exists(sq));

        assertEquivalence(q, jpql);

    }

    public void testSubqueries5() {
        String jpql = "SELECT o FROM Order o WHERE 10000 < ALL ("
            + "SELECT a.balance FROM o.customer c JOIN c.accounts a)";
        String expectedSQL = "SELECT t3.id, t3.cnt, t4.id, t4.accountNum, t5.id, t5.city, t5.country, t5.county, "
            + "t5.state, t5.street, t6.userid, t6.DTYPE, t6.age, t6.compName, t6.creditRating, t6.name, t5.zipCode, "
            + "t4.balanceOwed, t4.creditRating, t4.filledOrderCount, t4.firstName, t4.lastName, t4.name, t4.status, "
            + "t3.delivered, t3.name, t3.orderTs, t3.quantity, t3.totalCost " 
            + "FROM CR_ODR t3 LEFT OUTER JOIN CR_CUST t4 ON t3.CUSTOMER_ID = t4.id "
            + "LEFT OUTER JOIN CR_ADDR t5 ON t4.ADDRESS_ID = t5.id " 
            + "LEFT OUTER JOIN CompUser t6 ON t5.id = t6.ADD_ID " 
            + "WHERE (? < ALL (SELECT t2.balance FROM CR_CUST t0 INNER JOIN CR_CUST_CR_ACCT t1 ON "
            + "t0.id = t1.CUSTOMER_ID INNER JOIN CR_ACCT t2 ON t1.ACCOUNTS_ID = t2.id " 
            + "WHERE (t3.CUSTOMER_ID = t0.id)))";

        executeAndCompareSQL(jpql, expectedSQL);

        CriteriaQuery<Order> q = cb.createQuery(Order.class);
        Root<Order> o = q.from(Order.class);
        q.select(o);
        Subquery<Integer> sq = q.subquery(Integer.class);
        Root<Order> o1 = sq.correlate(o);
        Join<Order,Customer> c = o1.join(Order_.customer);
        ListJoin<Customer,Account> a = c.join(Customer_.accounts);
        sq.select(a.get(Account_.balance));
        q.where(cb.lt(cb.literal(10000), cb.all(sq)));

        assertEquivalence(q, jpql);
    }

    public void testSubqueries5a() {
        String jpql = "SELECT o FROM Order o WHERE o.name = SOME ("
            + "SELECT a.name FROM o.customer c JOIN c.accounts a)";
        String expectedSQL = "SELECT t3.id, t3.cnt, t4.id, t4.accountNum, t5.id, t5.city, t5.country, t5.county, "
            + "t5.state, t5.street, t6.userid, t6.DTYPE, t6.age, t6.compName, t6.creditRating, t6.name, t5.zipCode, "
            + "t4.balanceOwed, t4.creditRating, t4.filledOrderCount, t4.firstName, t4.lastName, t4.name, t4.status, "
            + "t3.delivered, t3.name, t3.orderTs, t3.quantity, t3.totalCost "
            + "FROM CR_ODR t3 LEFT OUTER JOIN CR_CUST t4 ON t3.CUSTOMER_ID = t4.id "
            + "LEFT OUTER JOIN CR_ADDR t5 ON t4.ADDRESS_ID = t5.id "
            + "LEFT OUTER JOIN CompUser t6 ON t5.id = t6.ADD_ID "
            + "WHERE (t3.name = ANY (SELECT t2.name "
            + "FROM CR_CUST t0 INNER JOIN CR_CUST_CR_ACCT t1 ON t0.id = t1.CUSTOMER_ID "
            + "INNER JOIN CR_ACCT t2 ON t1.ACCOUNTS_ID = t2.id WHERE (t3.CUSTOMER_ID = t0.id)))";

        executeAndCompareSQL(jpql, expectedSQL);

        CriteriaQuery<Order> q = cb.createQuery(Order.class);
        Root<Order> o = q.from(Order.class);
        q.select(o);
        Subquery<String> sq = q.subquery(String.class);
        Root<Order> o1 = sq.correlate(o);
        Join<Order,Customer> c = o1.join(Order_.customer);
        ListJoin<Customer,Account> a = c.join(Customer_.accounts);
        sq.select(a.get(Account_.name));
        q.where(cb.equal(o.get(Order_.name), cb.some(sq)));

        assertEquivalence(q, jpql);
    }

    public void testSubqueries6() {
        String jpql = "SELECT o FROM Order o JOIN o.customer c WHERE 10000 < "
            + "ALL (SELECT a.balance FROM c.accounts a)";
        String expectedSQL = "SELECT t0.id, t0.cnt, t4.id, t4.accountNum, t5.id, t5.city, t5.country, t5.county, "
            + "t5.state, t5.street, t6.userid, t6.DTYPE, t6.age, t6.compName, t6.creditRating, t6.name, t5.zipCode, "
            + "t4.balanceOwed, t4.creditRating, t4.filledOrderCount, t4.firstName, t4.lastName, t4.name, t4.status, "
            + "t0.delivered, t0.name, t0.orderTs, t0.quantity, t0.totalCost "
            + "FROM CR_ODR t0 INNER JOIN CR_CUST t1 ON t0.CUSTOMER_ID = t1.id "
            + "LEFT OUTER JOIN CR_CUST t4 ON t0.CUSTOMER_ID = t4.id "
            + "LEFT OUTER JOIN CR_ADDR t5 ON t4.ADDRESS_ID = t5.id "
            + "LEFT OUTER JOIN CompUser t6 ON t5.id = t6.ADD_ID WHERE (? < ALL ("
            + "SELECT t3.balance FROM CR_CUST_CR_ACCT t2, CR_ACCT t3 WHERE (t2.ACCOUNTS_ID = t3.id) AND "
            + "(t1.id = t2.CUSTOMER_ID)))";

        executeAndCompareSQL(jpql, expectedSQL);
        
        CriteriaQuery<Order> q = cb.createQuery(Order.class);
        Root<Order> o = q.from(Order.class);
        Join<Order,Customer> c = o.join(Order_.customer);
        q.select(o);
        Subquery<Integer> sq = q.subquery(Integer.class);
        Join<Order,Customer> c1 = sq.correlate(c);
        ListJoin<Customer,Account> a = c1.join(Customer_.accounts);
        sq.select(a.get(Account_.balance));
        q.where(cb.lt(cb.literal(10000), cb.all(sq)));

        assertEquivalence(q, jpql);
    }

    public void testSubqueries6a() {
        String jpql = "SELECT o FROM Order o JOIN o.customer c WHERE o.name = "
            + "SOME (SELECT a.name FROM c.accounts a)";
        String expectedSQL = "SELECT t0.id, t0.cnt, t4.id, t4.accountNum, t5.id, t5.city, t5.country, t5.county, "
            + "t5.state, t5.street, t6.userid, t6.DTYPE, t6.age, t6.compName, t6.creditRating, t6.name, t5.zipCode, "
            + "t4.balanceOwed, t4.creditRating, t4.filledOrderCount, t4.firstName, t4.lastName, t4.name, t4.status, "
            + "t0.delivered, t0.name, t0.orderTs, t0.quantity, t0.totalCost "
            + "FROM CR_ODR t0 INNER JOIN CR_CUST t1 ON t0.CUSTOMER_ID = t1.id "
            + "LEFT OUTER JOIN CR_CUST t4 ON t0.CUSTOMER_ID = t4.id "
            + "LEFT OUTER JOIN CR_ADDR t5 ON t4.ADDRESS_ID = t5.id "
            + "LEFT OUTER JOIN CompUser t6 ON t5.id = t6.ADD_ID WHERE (t0.name = ANY ("
            + "SELECT t3.name FROM CR_CUST_CR_ACCT t2, CR_ACCT t3 WHERE (t2.ACCOUNTS_ID = t3.id) "
            + "AND (t1.id = t2.CUSTOMER_ID)))";

        executeAndCompareSQL(jpql, expectedSQL);

        CriteriaQuery<Order> q = cb.createQuery(Order.class);
        Root<Order> o = q.from(Order.class);
        Join<Order,Customer> c = o.join(Order_.customer);
        q.select(o);
        Subquery<String> sq = q.subquery(String.class);
        Join<Order,Customer> c1 = sq.correlate(c);
        ListJoin<Customer,Account> a = c1.join(Customer_.accounts);
        sq.select(a.get(Account_.name));
        q.where(cb.equal(o.get(Order_.name), cb.some(sq)));

        assertEquivalence(q, jpql);
    }

    public void testSubqueries6b() {
        String jpql = "SELECT o FROM Order o JOIN o.customer c WHERE o.name = "
            + "SOME (SELECT a.county FROM c.address a)";
        String expectedSQL = "SELECT t0.id, t0.cnt, t3.id, t3.accountNum, t4.id, t4.city, t4.country, t4.county, "
            + "t4.state, t4.street, t5.userid, t5.DTYPE, t5.age, t5.compName, t5.creditRating, t5.name, t4.zipCode, "
            + "t3.balanceOwed, t3.creditRating, t3.filledOrderCount, t3.firstName, t3.lastName, t3.name, t3.status, "
            + "t0.delivered, t0.name, t0.orderTs, t0.quantity, t0.totalCost "
            + "FROM CR_ODR t0 INNER JOIN CR_CUST t1 ON t0.CUSTOMER_ID = t1.id "
            + "LEFT OUTER JOIN CR_CUST t3 ON t0.CUSTOMER_ID = t3.id "
            + "LEFT OUTER JOIN CR_ADDR t4 ON t3.ADDRESS_ID = t4.id "
            + "LEFT OUTER JOIN CompUser t5 ON t4.id = t5.ADD_ID WHERE (t0.name = ANY "
            + "(SELECT t2.county FROM CR_ADDR t2 WHERE (t1.ADDRESS_ID = t2.id)))";
        
        executeAndCompareSQL(jpql, expectedSQL);
        
        CriteriaQuery<Order> q = cb.createQuery(Order.class);
        Root<Order> o = q.from(Order.class);
        Join<Order,Customer> c = o.join(Order_.customer);
        q.select(o);
        Subquery<String> sq = q.subquery(String.class);
        Join<Order,Customer> c1 = sq.correlate(c);
        Join<Customer,Address> a = c1.join(Customer_.address);
        sq.select(a.get(Address_.county));
        q.where(cb.equal(o.get(Order_.name), cb.some(sq)));

        assertEquivalence(q, jpql);
    }

    public void testSubqueries6c() {
        String jpql = "SELECT o FROM Order o JOIN o.customer c WHERE 10000 < "
            + "ALL (SELECT u.age FROM c.address a JOIN a.user u)";

        String expectedSQL = "SELECT t0.id, t0.cnt, t4.id, t4.accountNum, t5.id, t5.city, t5.country, t5.county, "
            + "t5.state, t5.street, t6.userid, t6.DTYPE, t6.age, t6.compName, t6.creditRating, t6.name, t5.zipCode, "
            + "t4.balanceOwed, t4.creditRating, t4.filledOrderCount, t4.firstName, t4.lastName, t4.name, t4.status, "
            + "t0.delivered, t0.name, t0.orderTs, t0.quantity, t0.totalCost "
            + "FROM CR_ODR t0 INNER JOIN CR_CUST t1 ON t0.CUSTOMER_ID = t1.id "
            + "LEFT OUTER JOIN CR_CUST t4 ON t0.CUSTOMER_ID = t4.id "
            + "LEFT OUTER JOIN CR_ADDR t5 ON t4.ADDRESS_ID = t5.id "
            + "LEFT OUTER JOIN CompUser t6 ON t5.id = t6.ADD_ID WHERE (? < ALL ("
            + "SELECT t3.age FROM CR_ADDR t2 INNER JOIN CompUser t3 ON t2.id = t3.ADD_ID "
            + "WHERE (t1.ADDRESS_ID = t2.id)))";

        executeAndCompareSQL(jpql, expectedSQL);

        CriteriaQuery<Order> q = cb.createQuery(Order.class);
        Root<Order> o = q.from(Order.class);
        Join<Order,Customer> c = o.join(Order_.customer);
        q.select(o);
        Subquery<Integer> sq = q.subquery(Integer.class);
        Join<Order,Customer> c1 = sq.correlate(c);
        Join<Customer,Address> a = c1.join(Customer_.address);
        Join<Address, CompUser> u = a.join(Address_.user);
        sq.select(u.get(CompUser_.age));
        q.where(cb.lt(cb.literal(10000), cb.all(sq)));

        assertEquivalence(q, jpql);
    }

    public void testSubqueries6d() {
        String jpql = "SELECT o FROM Order o JOIN o.customer c JOIN c.address a WHERE 10000 < "
            + "ALL (SELECT u.age FROM c.address a1 JOIN a1.user u WHERE a.city = a1.city)";
        String expectedSQL = "SELECT t2.id, t2.cnt, t5.id, t5.accountNum, t6.id, t6.city, t6.country, t6.county, "
            + "t6.state, t6.street, t7.userid, t7.DTYPE, t7.age, t7.compName, t7.creditRating, t7.name, t6.zipCode, "
            + "t5.balanceOwed, t5.creditRating, t5.filledOrderCount, t5.firstName, t5.lastName, t5.name, t5.status, "
            + "t2.delivered, t2.name, t2.orderTs, t2.quantity, t2.totalCost "
            + "FROM CR_ODR t2 INNER JOIN CR_CUST t3 ON t2.CUSTOMER_ID = t3.id "
            + "LEFT OUTER JOIN CR_CUST t5 ON t2.CUSTOMER_ID = t5.id "
            + "INNER JOIN CR_ADDR t4 ON t3.ADDRESS_ID = t4.id "
            + "LEFT OUTER JOIN CR_ADDR t6 ON t5.ADDRESS_ID = t6.id "
            + "LEFT OUTER JOIN CompUser t7 ON t6.id = t7.ADD_ID WHERE (? < ALL ("
            + "SELECT t1.age FROM CR_ADDR t0 INNER JOIN CompUser t1 ON t0.id = t1.ADD_ID WHERE (t4.city = t0.city AND "
            + "t3.ADDRESS_ID = t0.id)) AND 1 = 1)";

        executeAndCompareSQL(jpql, expectedSQL);

        CriteriaQuery<Order> q = cb.createQuery(Order.class);
        Root<Order> o = q.from(Order.class);
        Join<Order,Customer> c = o.join(Order_.customer);
        Join<Customer,Address> a = c.join(Customer_.address);
        q.select(o);
        Subquery<Integer> sq = q.subquery(Integer.class);
        Join<Order,Customer> c1 = sq.correlate(c);
        Join<Customer,Address> a1 = c1.join(Customer_.address);
        Join<Address, CompUser> u = a1.join(Address_.user);
        sq.select(u.get(CompUser_.age));
        sq.where(cb.equal(a.get(Address_.city), a1.get(Address_.city)));
        q.where(cb.lt(cb.literal(10000), cb.all(sq)));

        assertEquivalence(q, jpql);
    }

    public void testSubqueries6e() {
        String jpql = "SELECT o FROM Order o JOIN o.customer c JOIN c.address a WHERE 10000 < "
            + "ALL (SELECT u.age FROM a.user u)";

        String expectedSQL = "SELECT t0.id, t0.cnt, t5.id, t5.accountNum, t6.id, t6.city, t6.country, t6.county, "
            + "t6.state, t6.street, t7.userid, t7.DTYPE, t7.age, t7.compName, t7.creditRating, t7.name, t6.zipCode, "
            + "t5.balanceOwed, t5.creditRating, t5.filledOrderCount, t5.firstName, t5.lastName, t5.name, t5.status, "
            + "t0.delivered, t0.name, t0.orderTs, t0.quantity, t0.totalCost "
            + "FROM CR_ODR t0 INNER JOIN CR_CUST t1 ON t0.CUSTOMER_ID = t1.id "
            + "LEFT OUTER JOIN CR_CUST t5 ON t0.CUSTOMER_ID = t5.id "
            + "INNER JOIN CR_ADDR t2 ON t1.ADDRESS_ID = t2.id "
            + "LEFT OUTER JOIN CR_ADDR t6 ON t5.ADDRESS_ID = t6.id "
            + "LEFT OUTER JOIN CompUser t7 ON t6.id = t7.ADD_ID "
            + "WHERE (? < ALL (SELECT t4.age FROM CompUser t3, CompUser t4 "
            + "WHERE (t3.userid = t4.userid) AND (t2.id = t3.ADD_ID)) AND 1 = 1)";

        executeAndCompareSQL(jpql, expectedSQL);

        CriteriaQuery<Order> q = cb.createQuery(Order.class);
        Root<Order> o = q.from(Order.class);
        Join<Order,Customer> c = o.join(Order_.customer);
        Join<Customer,Address> a = c.join(Customer_.address);
        q.select(o);
        Subquery<Integer> sq = q.subquery(Integer.class);
        Join<Customer,Address> a1 = sq.correlate(a);
        Join<Address, CompUser> u = a1.join(Address_.user);
        sq.select(u.get(CompUser_.age));
        q.where(cb.lt(cb.literal(10000), cb.all(sq)));

        assertEquivalence(q, jpql);
    }

    public void testSubqueries6f() {
        String jpql = "SELECT o FROM Order o JOIN o.customer c WHERE 10000 < "
            + "ALL (SELECT u.age FROM c.address.user u)";

        String expectedSQL = "SELECT t0.id, t0.cnt, t5.id, t5.accountNum, t6.id, t6.city, t6.country, t6.county, "
            + "t6.state, t6.street, t7.userid, t7.DTYPE, t7.age, t7.compName, t7.creditRating, t7.name, t6.zipCode, "
            + "t5.balanceOwed, t5.creditRating, t5.filledOrderCount, t5.firstName, t5.lastName, t5.name, t5.status, "
            + "t0.delivered, t0.name, t0.orderTs, t0.quantity, t0.totalCost "
            + "FROM CR_ODR t0 INNER JOIN CR_CUST t1 ON t0.CUSTOMER_ID = t1.id "
            + "LEFT OUTER JOIN CR_CUST t5 ON t0.CUSTOMER_ID = t5.id "
            + "LEFT OUTER JOIN CR_ADDR t6 ON t5.ADDRESS_ID = t6.id "
            + "LEFT OUTER JOIN CompUser t7 ON t6.id = t7.ADD_ID "
            + "WHERE (? < ALL (SELECT t4.age "
            + "FROM CR_ADDR t2 INNER JOIN CompUser t3 ON t2.id = t3.ADD_ID, CompUser t4 "
            + "WHERE (t3.userid = t4.userid) AND (t1.ADDRESS_ID = t2.id)))";

        executeAndCompareSQL(jpql, expectedSQL);
    }

    public void testSubqueries6g() {
        String jpql = "SELECT o FROM Order o JOIN o.customer c WHERE 10000 < "
            + "ALL (SELECT u.age FROM c.address a JOIN a.user u)";

        String expectedSQL = "SELECT t0.id, t0.cnt, t4.id, t4.accountNum, t5.id, t5.city, t5.country, t5.county, "
            + "t5.state, t5.street, t6.userid, t6.DTYPE, t6.age, t6.compName, t6.creditRating, t6.name, t5.zipCode, "
            + "t4.balanceOwed, t4.creditRating, t4.filledOrderCount, t4.firstName, t4.lastName, t4.name, t4.status, "
            + "t0.delivered, t0.name, t0.orderTs, t0.quantity, t0.totalCost "
            + "FROM CR_ODR t0 "
            + "INNER JOIN CR_CUST t1 ON t0.CUSTOMER_ID = t1.id "
            + "LEFT OUTER JOIN CR_CUST t4 ON t0.CUSTOMER_ID = t4.id "
            + "LEFT OUTER JOIN CR_ADDR t5 ON t4.ADDRESS_ID = t5.id "
            + "LEFT OUTER JOIN CompUser t6 ON t5.id = t6.ADD_ID WHERE (? < ALL ("
            + "SELECT t3.age "
            + "FROM CR_ADDR t2 "
            + "INNER JOIN CompUser t3 ON t2.id = t3.ADD_ID WHERE (t1.ADDRESS_ID = t2.id)))";

        executeAndCompareSQL(jpql, expectedSQL);

        CriteriaQuery<Order> q = cb.createQuery(Order.class);
        Root<Order> o = q.from(Order.class);
        Join<Order,Customer> c = o.join(Order_.customer);
        q.select(o);
        Subquery<Integer> sq = q.subquery(Integer.class);
        Join<Order,Customer> c1 = sq.correlate(c);
        Join<Address, CompUser> u = c1.join(Customer_.address).join(Address_.user);
        sq.select(u.get(CompUser_.age));
        q.where(cb.lt(cb.literal(10000), cb.all(sq)));

        assertEquivalence(q, jpql);
    }

    // redundant t3
    // compare to 6e, t3 should be in main query for LEFT OUTER JOIN
    public void testSubqueries6h() {
        String jpql = "SELECT o FROM Order o JOIN o.customer.address a WHERE 10000 < "
            + "ALL (SELECT u.age FROM a.user u)";
        String expectedSQL = "SELECT t0.id, t0.cnt, t1.id, t1.accountNum, t6.id, t6.city, t6.country, t6.county, "
            + "t6.state, t6.street, t7.userid, t7.DTYPE, t7.age, t7.compName, t7.creditRating, t7.name, t6.zipCode, "
            + "t1.balanceOwed, t1.creditRating, t1.filledOrderCount, t1.firstName, t1.lastName, t1.name, t1.status, "
            + "t0.delivered, t0.name, t0.orderTs, t0.quantity, t0.totalCost "
            + "FROM CR_ODR t0 INNER JOIN CR_CUST t1 ON t0.CUSTOMER_ID = t1.id "
            + "INNER JOIN CR_ADDR t2 ON t1.ADDRESS_ID = t2.id "
            + "LEFT OUTER JOIN CR_ADDR t6 ON t1.ADDRESS_ID = t6.id "
            + "LEFT OUTER JOIN CompUser t7 ON t6.id = t7.ADD_ID WHERE (? < ALL ("
            + "SELECT t5.age FROM CR_CUST t3, CompUser t4, CompUser t5 "
            + "WHERE (t4.userid = t5.userid) AND (t0.CUSTOMER_ID = t3.id) AND (t2.id = t4.ADD_ID)))";

        executeAndCompareSQL(jpql, expectedSQL);
    }

    public void testExist1() {
        String jpql = "SELECT DISTINCT c.name FROM CompUser c WHERE EXISTS"
            + " (SELECT a FROM Address a WHERE a = c.address )";

        String expectedSQL = "SELECT DISTINCT t1.name FROM CompUser t1 WHERE (EXISTS ("
            + "SELECT t0.id FROM CR_ADDR t0 WHERE (t0.id = t1.ADD_ID)))";

        executeAndCompareSQL(jpql, expectedSQL);
 
        CriteriaQuery<String> q = cb.createQuery(String.class);
        Root<CompUser> c = q.from(CompUser.class);
        q.select(c.get(CompUser_.name)).distinct(true);
        Subquery<Address> sq = q.subquery(Address.class);
        Root<Address> a = sq.from(Address.class);
        sq.select(a);
        sq.where(cb.equal(a, c.get(CompUser_.address)));
        q.where(cb.exists(sq));

        assertEquivalence(q, jpql);
    }

    public void testExist1a() {
        String jpql = "SELECT DISTINCT o.name FROM CompUser o WHERE EXISTS"
            + " (SELECT s FROM CompUser s WHERE s.address.country = "
            + "o.address.country)";

        String expectedSQL = "SELECT DISTINCT t2.name " + "FROM CompUser t2 "
        + "INNER JOIN CR_ADDR t3 ON t2.ADD_ID = t3.id "
        + "WHERE (EXISTS (" + "SELECT t0.userid " + "FROM CompUser t0 "
        + "INNER JOIN CR_ADDR t1 ON t0.ADD_ID = t1.id "
        + "WHERE (t1.country = t3.country)))";

        executeAndCompareSQL(jpql, expectedSQL);
        
        CriteriaQuery<String> q = cb.createQuery(String.class);
        Root<CompUser> o = q.from(CompUser.class);
        q.select(o.get(CompUser_.name)).distinct(true);
        Subquery<CompUser> sq = q.subquery(CompUser.class);
        Root<CompUser> s = sq.from(CompUser.class);
        sq.select(s);
        sq.where(cb.equal(s.get(CompUser_.address).get(Address_.country), 
            o.get(CompUser_.address).get(Address_.country)));
        q.where(cb.exists(sq));

        assertEquivalence(q, jpql);
    }
    
    public void testExist1b() {
        String jpql = "select c from Customer c left join c.orders o where exists"
            + " (select o2 from c.orders o2 where o2 = o)";

        String expectedSQL = "SELECT t0.id, t0.accountNum, t4.id, t4.city, t4.country, t4.county, "
            + "t4.state, t4.street, t5.userid, t5.DTYPE, t5.age, t5.compName, t5.creditRating, t5.name, "
            + "t4.zipCode, t0.balanceOwed, t0.creditRating, t0.filledOrderCount, t0.firstName, t0.lastName, "
            + "t0.name, t0.status "
            + "FROM CR_CUST t0 "
            + "LEFT OUTER JOIN CR_ODR t1 ON t0.id = t1.CUSTOMER_ID "
            + "LEFT OUTER JOIN CR_ADDR t4 ON t0.ADDRESS_ID = t4.id "
            + "LEFT OUTER JOIN CompUser t5 ON t4.id = t5.ADD_ID WHERE (EXISTS (" 
            + "SELECT t3.id FROM CR_ODR t2, CR_ODR t3 WHERE (t1.id = t3.id AND t2.id = t3.id) "
            + "AND (t0.id = t2.CUSTOMER_ID)))";

        executeAndCompareSQL(jpql, expectedSQL);

        CriteriaQuery<Customer> q = cb.createQuery(Customer.class);
        Root<Customer> c = q.from(Customer.class);
        Join<Customer, Order> o = c.join(Customer_.orders, JoinType.LEFT);
        q.select(c);
        Subquery<Order> sq = q.subquery(Order.class);
        Root<Customer> c1 = sq.correlate(c);
        Join<Customer,Order> o2 = c1.join(Customer_.orders);
        sq.select(o2);
        sq.where(cb.equal(o2, o));
        q.where(cb.exists(sq));

        assertEquivalence(q, jpql);
    }

    public void testNotExist1() {
        String jpql = "SELECT DISTINCT c.name FROM CompUser c WHERE NOT EXISTS"
            + " (SELECT a FROM Address a WHERE a = c.address )";

        String expectedSQL = "SELECT DISTINCT t1.name FROM CompUser t1 WHERE (NOT (EXISTS ("
            + "SELECT t0.id FROM CR_ADDR t0 WHERE (t0.id = t1.ADD_ID))))";

        executeAndCompareSQL(jpql, expectedSQL);

        CriteriaQuery<String> q = cb.createQuery(String.class);
        Root<CompUser> c = q.from(CompUser.class);
        q.select(c.get(CompUser_.name)).distinct(true);
        Subquery<Address> sq = q.subquery(Address.class);
        Root<Address> a = sq.from(Address.class);
        sq.select(a);
        sq.where(cb.equal(a, c.get(CompUser_.address)));
        q.where(cb.exists(sq).not());

        assertEquivalence(q, jpql);
    }

    public void testNotExist1a() {
        String jpql = "select c from Customer c left join c.orders o where not exists"
            + " (select o2 from c.orders o2 where o2 = o)";

        String expectedSQL = "SELECT t0.id, t0.accountNum, t4.id, t4.city, t4.country, t4.county, "
            + "t4.state, t4.street, t5.userid, t5.DTYPE, t5.age, t5.compName, t5.creditRating, t5.name, "
            + "t4.zipCode, t0.balanceOwed, t0.creditRating, t0.filledOrderCount, t0.firstName, t0.lastName, "
            + "t0.name, t0.status "
            + "FROM CR_CUST t0 "
            + "LEFT OUTER JOIN CR_ODR t1 ON t0.id = t1.CUSTOMER_ID "
            + "LEFT OUTER JOIN CR_ADDR t4 ON t0.ADDRESS_ID = t4.id "
            + "LEFT OUTER JOIN CompUser t5 ON t4.id = t5.ADD_ID WHERE (NOT (EXISTS ("
            + "SELECT t3.id FROM CR_ODR t2, CR_ODR t3 WHERE (t1.id = t3.id AND t2.id = t3.id) "
            + "AND (t0.id = t2.CUSTOMER_ID))))";

        executeAndCompareSQL(jpql, expectedSQL);

        CriteriaQuery<Customer> q = cb.createQuery(Customer.class);
        Root<Customer> c = q.from(Customer.class);
        SetJoin<Customer,Order> o = c.join(Customer_.orders, JoinType.LEFT);
        q.select(c);
        Subquery<Order> sq = q.subquery(Order.class);
        Root<Customer> c1 = sq.correlate(c);
        SetJoin<Customer,Order> o2 = c1.join(Customer_.orders);
        sq.select(o2);
        sq.where(cb.equal(o2, o));
        q.where(cb.exists(sq).not());

        assertEquivalence(q, jpql);
    }

    public void testAny() {
        String jpql = "SELECT o.name FROM CompUser o "
            + "WHERE o.address.zipCode = ANY (SELECT s.computerName "
            + " FROM CompUser s WHERE s.address.country IS NOT NULL)";

        String expectedSQL = "SELECT t0.name "
            + "FROM CompUser t0 "
            + "INNER JOIN CR_ADDR t1 ON t0.ADD_ID = t1.id "
            + "WHERE (t1.zipCode = ANY ("
            + "SELECT t2.compName "
            + "FROM CompUser t2 "
            + "INNER JOIN CR_ADDR t3 ON t2.ADD_ID = t3.id WHERE (t3.country IS NOT NULL)))";

        executeAndCompareSQL(jpql, expectedSQL);

        CriteriaQuery<String> q = cb.createQuery(String.class);
        Root<CompUser> o = q.from(CompUser.class);
        q.select(o.get(CompUser_.name));
        Subquery<String> sq = q.subquery(String.class);
        Root<CompUser> s = sq.from(CompUser.class);
        sq.select(s.get(CompUser_.computerName));
        sq.where(cb.isNotNull(s.get(CompUser_.address).get(Address_.country)));
        q.where(cb.equal(o.get(CompUser_.address).get(Address_.zipCode), cb.any(sq)));

        assertEquivalence(q, jpql);
    }

    // redundant t1
    public void testSubquery01() {
        String jpql = "select o1.id from Order o1 where o1.id in "
            + " (select distinct o.id from LineItem i, Order o"
            + " where i.quantity > 10 and o.count > 1000 and i.id = o.id)";

        String crossJoin = (getDictionary() instanceof DerbyDictionary) 
             ? "JOIN CR_ODR t2 ON (1 = 1)"
             : "CROSS JOIN CR_ODR t2";
        String expectedSQL = "SELECT t0.id FROM CR_ODR t0 WHERE (t0.id IN ("
            + "SELECT DISTINCT t2.id "
            + "FROM CR_ODR t1 " + crossJoin + ", CR_LI t3 WHERE (" 
            + "t3.quantity > ? AND t2.cnt > ? AND t3.id = t2.id)))";

        executeAndCompareSQL(jpql, expectedSQL);

        CriteriaQuery<Integer> q = cb.createQuery(Integer.class);
        Root<Order> o1 = q.from(Order.class);
        q.select(o1.get(Order_.id));
        
        Subquery<Integer> sq = q.subquery(Integer.class);
        Root<LineItem> i = sq.from(LineItem.class);
        Root<Order> o = sq.from(Order.class);
        sq.select(o.get(Order_.id)).distinct(true);
        sq.where(cb.gt(i.get(LineItem_.quantity), 10), 
                 cb.gt(o.get(Order_.count), 1000),
                 cb.equal(i.get(LineItem_.id), o.get(Order_.id)));        
        q.where(o1.get(Order_.id).in(sq));

        assertEquivalence(q, jpql);
    }

    public void testSubquery02() {
        String jpql = "select o.id from Order o where o.customer.balanceOwed ="
            + " (select max(o2.customer.balanceOwed) from Order o2"
            + " where o.customer.id = o2.customer.id)";

        String expectedSQL = "SELECT t0.id FROM CR_ODR t0 "
            + "INNER JOIN CR_CUST t1 ON t0.CUSTOMER_ID = t1.id WHERE (t1.balanceOwed = ("
            + "SELECT MAX(t3.balanceOwed) FROM CR_ODR t2 "
            + "INNER JOIN CR_CUST t3 ON t2.CUSTOMER_ID = t3.id WHERE (t0.CUSTOMER_ID = t2.CUSTOMER_ID)))";

        executeAndCompareSQL(jpql, expectedSQL);

        CriteriaQuery<Integer> q = cb.createQuery(Integer.class);
        Root<Order> o = q.from(Order.class);
        q.select(o.get(Order_.id));
        
        Subquery<Integer> sq = q.subquery(Integer.class);
        Root<Order> o2 = sq.from(Order.class);
        sq.select(cb.max(o2.get(Order_.customer).get(Customer_.balanceOwed)));
        sq.where(cb.equal(o.get(Order_.customer).get(Customer_.id), 
                o2.get(Order_.customer).get(Customer_.id)));
        q.where(cb.equal(o.get(Order_.customer).get(Customer_.balanceOwed), sq));

        assertEquivalence(q, jpql);
    }

    public void testSubquery03() {
        String jpql = "select o from Order o where o.customer.balanceOwed ="
            + " (select max(o2.customer.balanceOwed) from Order o2"
            + " where o.customer.id = o2.customer.id)";

        String expectedSQL = "SELECT t0.id, t0.cnt, t1.id, t1.accountNum, t4.id, t4.city, t4.country, "
            + "t4.county, t4.state, t4.street, t5.userid, t5.DTYPE, t5.age, t5.compName, t5.creditRating, t5.name, "
            + "t4.zipCode, t1.balanceOwed, t1.creditRating, t1.filledOrderCount, t1.firstName, t1.lastName, "
            + "t1.name, t1.status, t0.delivered, t0.name, t0.orderTs, t0.quantity, t0.totalCost "
            + "FROM CR_ODR t0 "
            + "INNER JOIN CR_CUST t1 ON t0.CUSTOMER_ID = t1.id "
            + "LEFT OUTER JOIN CR_ADDR t4 ON t1.ADDRESS_ID = t4.id "
            + "LEFT OUTER JOIN CompUser t5 ON t4.id = t5.ADD_ID WHERE (t1.balanceOwed = ("
            + "SELECT MAX(t3.balanceOwed) FROM CR_ODR t2 "
            + "INNER JOIN CR_CUST t3 ON t2.CUSTOMER_ID = t3.id WHERE (t0.CUSTOMER_ID = t2.CUSTOMER_ID)))";

        executeAndCompareSQL(jpql, expectedSQL);

        CriteriaQuery<Order> q = cb.createQuery(Order.class);
        Root<Order> o = q.from(Order.class);
        q.select(o);
        
        Subquery<Integer> sq = q.subquery(Integer.class);
        Root<Order> o2 = sq.from(Order.class);
        sq.select(cb.max(o2.get(Order_.customer).get(Customer_.balanceOwed)));
        sq.where(cb.equal(o.get(Order_.customer).get(Customer_.id), 
                o2.get(Order_.customer).get(Customer_.id)));
        q.where(cb.equal(o.get(Order_.customer).get(Customer_.balanceOwed), sq));

        assertEquivalence(q, jpql);
    }

    public void testSubquery04() {
        String jpql = "select o.id from Order o where o.quantity >"
            + " (select count(i) from o.lineItems i)";

        String expectedSQL = "SELECT t0.id FROM CR_ODR t0 WHERE (t0.quantity > ("
            + "SELECT COUNT(t2.id) FROM CR_LI t1, CR_LI t2 "
            + "WHERE (t1.id = t2.id) AND (t0.id = t1.ORDER_ID)))";

        executeAndCompareSQL(jpql, expectedSQL);

        CriteriaQuery<Integer> q = cb.createQuery(Integer.class);
        Root<Order> o = q.from(Order.class);
        q.select(o.get(Order_.id));
        
        Subquery<Long> sq = q.subquery(Long.class);
        Root<Order> o2 = sq.correlate(o);
        ListJoin<Order, LineItem> i = o2.join(Order_.lineItems);
        sq.select(cb.count(i));
        q.where(cb.gt(o.get(Order_.quantity), sq));

        assertEquivalence(q, jpql);
    }

    public void testSubquery05() {
        String jpql = "select o.id from Order o where o.quantity >"
            + " (select count(o.quantity) from Order o)";

        String expectedSQL = "SELECT t0.id FROM CR_ODR t0 WHERE (t0.quantity > ("
            + "SELECT COUNT(t1.quantity) FROM CR_ODR t1))";
        executeAndCompareSQL(jpql, expectedSQL);

        CriteriaQuery<Integer> q = cb.createQuery(Integer.class);
        Root<Order> o = q.from(Order.class);
        q.select(o.get(Order_.id));
        
        Subquery<Long> sq = q.subquery(Long.class);
        Root<Order> o2 = sq.from(Order.class);
        sq.select(cb.count(o2.get(Order_.quantity)));
        q.where(cb.gt(o.get(Order_.quantity), sq));

        assertEquivalence(q, jpql);
    }

    public void testSubquery06() {
        String jpql = "select o.id from Order o where o.quantity >"
            + " (select count(o.id) from Order o)";

        String expectedSQL = "SELECT t0.id FROM CR_ODR t0 WHERE (t0.quantity > ("
            + "SELECT COUNT(t1.id) FROM CR_ODR t1))";

        executeAndCompareSQL(jpql, expectedSQL);

        CriteriaQuery<Integer> q = cb.createQuery(Integer.class);
        Root<Order> o = q.from(Order.class);
        q.select(o.get(Order_.id));
        
        Subquery<Long> sq = q.subquery(Long.class);
        Root<Order> o2 = sq.from(Order.class);
        sq.select(cb.count(o2.get(Order_.id)));
        q.where(cb.gt(o.get(Order_.quantity), sq));

        assertEquivalence(q, jpql);
    }

    public void testSubquery07() {
        String jpql = "select o.id from Order o where o.quantity >"
            + " (select avg(o.quantity) from Order o)";

        String expectedSQL = "SELECT t0.id FROM CR_ODR t0 WHERE (t0.quantity > ("
            + "SELECT AVG(t1.quantity) FROM CR_ODR t1))";
        executeAndCompareSQL(jpql, expectedSQL);

        CriteriaQuery<Integer> q = cb.createQuery(Integer.class);
        Root<Order> o = q.from(Order.class);
        q.select(o.get(Order_.id));
        
        Subquery<Double> sq = q.subquery(Double.class);
        Root<Order> o2 = sq.from(Order.class);
        sq.select(cb.avg(o2.get(Order_.quantity)));
        q.where(cb.gt(o.get(Order_.quantity), sq));

        assertEquivalence(q, jpql);
    }

    public void testSubquery08() {
        String jpql = "select c.name from Customer c "
            + "where exists (select o from c.orders o where o.id = 1) "
            + "or exists (select o from c.orders o where o.id = 2)";

        String expectedSQL = "SELECT t0.name FROM CR_CUST t0 WHERE (EXISTS ("
            + "SELECT t2.id FROM CR_ODR t1, CR_ODR t2 "
            + "WHERE (t2.id = ? AND t1.id = t2.id) AND (t0.id = t1.CUSTOMER_ID)) OR EXISTS ("
            + "SELECT t4.id FROM CR_ODR t3, CR_ODR t4 "
            + "WHERE (t4.id = ? AND t3.id = t4.id) AND (t0.id = t3.CUSTOMER_ID)))";
        executeAndCompareSQL(jpql, expectedSQL);

        CriteriaQuery<String> q = cb.createQuery(String.class);
        Root<Customer> c = q.from(Customer.class);
        q.select(c.get(Customer_.name));
        
        Subquery<Order> sq1 = q.subquery(Order.class);
        Root<Customer> c1 = sq1.correlate(c);
        SetJoin<Customer,Order> o1 = c1.join(Customer_.orders);
        sq1.select(o1);
        sq1.where(cb.equal(o1.get(Order_.id), 1));
        
        Subquery<Order> sq2 = q.subquery(Order.class);
        Root<Customer> c2 = sq2.correlate(c);
        SetJoin<Customer,Order> o2 = c2.join(Customer_.orders);
        sq2.select(o2);
        sq2.where(cb.equal(o2.get(Order_.id), 2));
        
        q.where(cb.or(cb.exists(sq1), cb.exists(sq2)));

        assertEquivalence(q, jpql);
    }

    public void testSubquery09() {
        String jpql = "select c.name from Customer c, in(c.orders) o "
            + "where o.quantity between "
            + "(select max(o.quantity) from Order o) and "
            + "(select avg(o.quantity) from Order o) ";

        String expectedSQL = "SELECT t0.name "
            + "FROM CR_CUST t0 INNER JOIN CR_ODR t1 ON t0.id = t1.CUSTOMER_ID WHERE ("
            + "t1.quantity >= (SELECT MAX(t2.quantity) FROM CR_ODR t2) AND "
            + "t1.quantity <= (SELECT AVG(t3.quantity) FROM CR_ODR t3))";
        executeAndCompareSQL(jpql, expectedSQL);
        
        CriteriaQuery<String> q = cb.createQuery(String.class);
        Root<Customer> c = q.from(Customer.class);
        SetJoin<Customer,Order> o = c.join(Customer_.orders);
        q.select(c.get(Customer_.name));
        
        Subquery<Integer> sq1 = q.subquery(Integer.class);
        Root<Order> o1 = sq1.from(Order.class);
        sq1.select(cb.max(o1.get(Order_.quantity)));
        
        Subquery<Double> sq2 = q.subquery(Double.class);
        Root<Order> o2 = sq2.from(Order.class);
        sq2.select(cb.avg(o2.get(Order_.quantity)));
        
        q.where(cb.between(o.get(Order_.quantity), sq1, sq2.as(Integer.class)));
        
        assertEquivalence(q, jpql);
    }

    public void testSubquery10() {
        String jpql = "select o.id from Order o where o.quantity >"
            + " (select sum(o2.quantity) from Customer c, "
            + "in(c.orders) o2) ";

        String expectedSQL = "SELECT t2.id FROM CR_ODR t2 WHERE (t2.quantity > ("
            + "SELECT SUM(t1.quantity) FROM CR_CUST t0 INNER JOIN CR_ODR t1 ON t0.id = t1.CUSTOMER_ID))";
        executeAndCompareSQL(jpql, expectedSQL);

        CriteriaQuery<Integer> q = cb.createQuery(Integer.class);
        Root<Order> o = q.from(Order.class);
        q.select(o.get(Order_.id));
        
        Subquery<Integer> sq = q.subquery(Integer.class);
        Root<Customer> c = sq.from(Customer.class);
        Join<Customer,Order> o2 = c.join(Customer_.orders);
        sq.select(cb.sum(o2.get(Order_.quantity)));
        
        q.where(cb.gt(o.get(Order_.quantity), sq));

        assertEquivalence(q, jpql);
    }

    public void testSubquery11() {
        String jpql = "select o.id from Order o where o.quantity between"
            + " (select avg(o2.quantity) from Customer c, in(c.orders) o2)"
            + " and (select min(o2.quantity) from Customer c, in(c.orders)"
            + " o2)";

        String expectedSQL = "SELECT t4.id FROM CR_ODR t4 WHERE (t4.quantity >= ("
            + "SELECT AVG(t1.quantity) "
            + "FROM CR_CUST t0 INNER JOIN CR_ODR t1 ON t0.id = t1.CUSTOMER_ID) AND "
            + "t4.quantity <= ("
            + "SELECT MIN(t3.quantity) "
            + "FROM CR_CUST t2 INNER JOIN CR_ODR t3 ON t2.id = t3.CUSTOMER_ID))";
        executeAndCompareSQL(jpql, expectedSQL);

        CriteriaQuery<Integer> q = cb.createQuery(Integer.class);
        Root<Order> o = q.from(Order.class);
        q.select(o.get(Order_.id));

        Subquery<Double> sq1 = q.subquery(Double.class);
        Root<Customer> c = sq1.from(Customer.class);
        SetJoin<Customer, Order> o2 = c.join(Customer_.orders);
        sq1.select(cb.avg(o2.get(Order_.quantity)));

        Subquery<Integer> sq2 = q.subquery(Integer.class);
        Root<Customer> c2 = sq2.from(Customer.class);
        SetJoin<Customer, Order> o3 = c2.join(Customer_.orders);
        sq2.select(cb.min(o3.get(Order_.quantity)));

        q.where(cb.between(o.get(Order_.quantity), sq1.as(Integer.class), sq2));

        assertEquivalence(q, jpql);
    }

    public void testSubquery12() {
        String jpql = "select o.id from Customer c, in(c.orders)o "
            + "where o.quantity > (select sum(o2.quantity)"
            + " from c.orders o2)";

        String expectedSQL = "SELECT t1.id FROM CR_CUST t0 "
            + "INNER JOIN CR_ODR t1 ON t0.id = t1.CUSTOMER_ID WHERE (t1.quantity > ("
            + "SELECT SUM(t3.quantity) FROM CR_ODR t2, CR_ODR t3 "
            + "WHERE (t2.id = t3.id) AND (t0.id = t2.CUSTOMER_ID)))";
        executeAndCompareSQL(jpql, expectedSQL);

        CriteriaQuery<Integer> q = cb.createQuery(Integer.class);
        Root<Customer> c = q.from(Customer.class);
        SetJoin<Customer,Order> o = c.join(Customer_.orders);
        q.select(o.get(Order_.id));
        
        Subquery<Integer> sq = q.subquery(Integer.class);
        Root<Customer> c1 = sq.correlate(c);
        Join<Customer,Order> o2 = c1.join(Customer_.orders);
        sq.select(cb.sum(o2.get(Order_.quantity)));
        
        q.where(cb.gt(o.get(Order_.quantity), sq));

        assertEquivalence(q, jpql);
    }

    public void testSubquery13() {
        String jpql = "select o1.id, c.name from Order o1, Customer c"
            + " where o1.quantity = "
            + " any(select o2.quantity from in(c.orders) o2)";

        String crossJoin = getDictionary() instanceof DerbyDictionary
            ? "JOIN CR_CUST t1 ON (1 = 1)"
            : "CROSS JOIN CR_CUST t1";
        String expectedSQL = "SELECT t0.id, t1.name " + 
        "FROM CR_ODR t0 " + crossJoin + " WHERE (t0.quantity = ANY (" + 
        "SELECT t3.quantity FROM CR_ODR t2, CR_ODR t3 WHERE (t2.id = t3.id) AND (t1.id = t2.CUSTOMER_ID)))"; 

        executeAndCompareSQL(jpql, expectedSQL);

        CriteriaQuery<?> q = cb.createQuery();
        Root<Order> o1 = q.from(Order.class);
        Root<Customer> c = q.from(Customer.class);
        q.multiselect(o1.get(Order_.id), c.get(Customer_.name));
        
        Subquery<Integer> sq = q.subquery(Integer.class);
        Root<Customer> c1 = sq.correlate(c);
        Join<Customer,Order> o2 = c1.join(Customer_.orders);
        sq.select(o2.get(Order_.quantity));
        
        q.where(cb.equal(o1.get(Order_.quantity), cb.any(sq)));

        assertEquivalence(q, jpql);
    }

    public void testSubquery14() {
        String jpql = "SELECT p, m FROM Publisher p "
            + "LEFT OUTER JOIN p.magazineCollection m "
            + "WHERE m.id = (SELECT MAX(m2.id) FROM Magazine m2 "
            + "WHERE m2.idPublisher.id = p.id AND m2.id = "
            + "(SELECT MAX(m3.id) FROM Magazine m3 "
            + "WHERE m3.idPublisher.id = p.id)) ";

        String expectedSQL = "SELECT t0.id, t1.id, t1.date_published, t1.id_publisher, t1.name "
            + "FROM CR_PSH t0 LEFT OUTER JOIN CR_MG t1 ON t0.id = t1.id_publisher WHERE (t1.id = ("
            + "SELECT MAX(t2.id) FROM CR_MG t2 WHERE ("
            + "t2.id_publisher = t0.id AND "
            + "t2.id = (SELECT MAX(t3.id) FROM CR_MG t3 WHERE (t3.id_publisher = t0.id)))))";
        executeAndCompareSQL(jpql, expectedSQL);

        CriteriaQuery<?> q = cb.createQuery();
        Root<Publisher> p = q.from(Publisher.class);
        SetJoin<Publisher, Magazine> m = p.join(Publisher_.magazineCollection, JoinType.LEFT);
        q.multiselect(p, m);
        
        Subquery<Integer> sq = q.subquery(Integer.class);
        Root<Magazine> m2 = sq.from(Magazine.class);
        sq.select(cb.max(m2.get(Magazine_.id)));

        Subquery<Integer> sq2 = sq.subquery(Integer.class);
        Root<Magazine> m3 = sq2.from(Magazine.class);
        sq2.select(cb.max(m3.get(Magazine_.id)));
        sq2.where(cb.equal(m3.get(Magazine_.idPublisher).get(Publisher_.id), p.get(Publisher_.id)));
        
        sq.where(
            cb.equal(m2.get(Magazine_.idPublisher).get(Publisher_.id), p.get(Publisher_.id)),
            cb.equal(m2.get(Magazine_.id), sq2));
        
        q.where(cb.equal(m.get(Magazine_.id), sq));

        assertEquivalence(q, jpql);
    }

    public void testSubquery15() {
        String jpql = "select o.id from Order o where o.delivered =(select "
            + "   CASE WHEN o2.quantity > 10 THEN true"
            + "     WHEN o2.quantity = 10 THEN false "
            + "     ELSE false END from Order o2"
            + " where o.customer.id = o2.customer.id)";

        String expectedSQL = "SELECT t0.id FROM CR_ODR t0 WHERE (t0.delivered = ("
            + "SELECT  CASE  WHEN t1.quantity > ? THEN 1 WHEN t1.quantity = ? THEN 0 ELSE 0 END  "
            + "FROM CR_ODR t1 WHERE (t0.CUSTOMER_ID = t1.CUSTOMER_ID)))";
        executeAndCompareSQL(jpql, expectedSQL);

        CriteriaQuery<Integer> q = cb.createQuery(Integer.class);
        Root<Order> o = q.from(Order.class);
        q.select(o.get(Order_.id));

        Subquery<Object> sq = q.subquery(Object.class);
        Root<Order> o2 = sq.from(Order.class);
        sq.where(cb.equal(o.get(Order_.customer).get(Customer_.id), o2.get(
                Order_.customer).get(Customer_.id)));
        sq.select(
            cb.selectCase().when(cb.gt(o2.get(Order_.quantity), 10), true)
                .when(cb.equal(o2.get(Order_.quantity), 10), false)
                .otherwise(false)
        );

        q.where(cb.equal(o.get(Order_.delivered), sq));
        

        assertEquivalence(q, jpql);
    }

    public void testSubquery16() {
        String jpql = "select o1.id from Order o1 where o1.quantity > "
            + " (select o.quantity*2 from LineItem i, Order o"
            + " where i.quantity > 10 and o.quantity > 1000 and i.id = "
            + "o.id)";
        String crossJoin = (getDictionary() instanceof DerbyDictionary) 
        ? "JOIN CR_ODR t2 ON (1 = 1)"
        : "CROSS JOIN CR_ODR t2";
        String expectedSQL = "SELECT t0.id FROM CR_ODR t0 WHERE (t0.quantity > ("
            + "SELECT (t2.quantity * ?) FROM CR_ODR t1 " + crossJoin + ", CR_LI t3 WHERE ("
            + "t3.quantity > ? AND t2.quantity > ? AND t3.id = t2.id)))";
        executeAndCompareSQL(jpql, expectedSQL);

        CriteriaQuery<Integer> q = cb.createQuery(Integer.class);
        Root<Order> o1 = q.from(Order.class);
        q.select(o1.get(Order_.id));

        Subquery<Integer> sq = q.subquery(Integer.class);
        Root<LineItem> i = sq.from(LineItem.class);
        Root<Order> o = sq.from(Order.class);
        sq.where(cb.and(cb.and(cb.gt(i.get(LineItem_.quantity), 10), cb.gt(o
                .get(Order_.quantity), 1000)), cb.equal(i.get(LineItem_.id), o
                .get(Order_.id))));

        q.where(cb.gt(o1.get(Order_.quantity), sq.select(cb.prod(o
                .get(Order_.quantity), 2))));

        assertEquivalence(q, jpql);
    }

    public void testSubquery17() {
        String jpql = "select o.id from Order o where o.customer.name ="
            + " (select substring(o2.customer.name, 3) from Order o2"
            + " where o.customer.id = o2.customer.id)";
        
        String useCast = (getDictionary() instanceof DerbyDictionary) 
        ? "SUBSTR(CAST((t3.name) AS VARCHAR(1000)), 3) "
        : "SUBSTRING(t3.name, 3) ";
        String expectedSQL = "SELECT t0.id FROM CR_ODR t0 "
            + "INNER JOIN CR_CUST t1 ON t0.CUSTOMER_ID = t1.id WHERE (t1.name = ("
            + "SELECT " + useCast
            + "FROM CR_ODR t2 INNER JOIN CR_CUST t3 ON t2.CUSTOMER_ID = t3.id "
            + "WHERE (t0.CUSTOMER_ID = t2.CUSTOMER_ID)))";
        executeAndCompareSQL(jpql, expectedSQL);

        CriteriaQuery<Integer> q = cb.createQuery(Integer.class);
        Root<Order> o = q.from(Order.class);
        q.select(o.get(Order_.id));

        Subquery<String> sq = q.subquery(String.class);
        Root<Order> o2 = sq.from(Order.class);
        sq.where(cb.equal(o.get(Order_.customer).get(Customer_.id), o2.get(
                Order_.customer).get(Customer_.id)));

        q.where(cb.equal(o.get(Order_.customer).get(Customer_.name), sq
                .select(cb.substring(o2.get(Order_.customer)
                        .get(Customer_.name), 3))));

        assertEquivalence(q, jpql);
    }

    public void testSubquery18() {
        String jpql = "select o.id from Order o where o.orderTs >"
            + " (select CURRENT_TIMESTAMP from o.lineItems i)";

        String expectedSQL = "SELECT t0.id FROM CR_ODR t0 WHERE (t0.orderTs > ("
            + "SELECT CURRENT_TIMESTAMP FROM CR_LI t1, CR_LI t2 WHERE (t1.id = t2.id) AND "
            + "(t0.id = t1.ORDER_ID)))";
        executeAndCompareSQL(jpql, expectedSQL);

        CriteriaQuery<Integer> q = cb.createQuery(Integer.class);
        Root<Order> o = q.from(Order.class);
        q.select(o.get(Order_.id));

        Subquery<Timestamp> sq = q.subquery(Timestamp.class);
        Root<Order> o2 = sq.correlate(o);
        ListJoin<Order, LineItem> i = o2.join(Order_.lineItems);

        q.where(cb.greaterThan(o.get(Order_.orderTs).as(Long.class), sq.select(cb.currentTimestamp()).as(Long.class)));
        
        assertEquivalence(q, jpql);
    }

    public void testSubquery19() {
        String jpql = "select o.id from Order o where o.quantity >"
            + " (select SQRT(o.quantity) from Order o where o.delivered"
            + " = true)";

        String expectedSQL = "SELECT t0.id FROM CR_ODR t0 WHERE (t0.quantity > ("
            + "SELECT SQRT(t1.quantity) FROM CR_ODR t1 WHERE (t1.delivered = ?)))";
        executeAndCompareSQL(jpql, expectedSQL);

        CriteriaQuery<Integer> q = cb.createQuery(Integer.class);
        Root<Order> o = q.from(Order.class);
        q.select(o.get(Order_.id));

        Subquery<Double> sq = q.subquery(Double.class);
        Root<Order> o2 = sq.from(Order.class);
        sq.where(cb.equal(o2.get(Order_.delivered), true));

        q.where(cb.gt(o.get(Order_.quantity), sq.select(cb.sqrt(o2
                .get(Order_.quantity)))));
        assertEquivalence(q, jpql);
    }

    public void testSubquery20() {
        String jpql = "select o.id from Order o where o.customer.name in"
            + " (select CONCAT(o.customer.name, 'XX') from Order o"
            + " where o.quantity > 10)";
        String useCast = getDictionary() instanceof DerbyDictionary
           ? "(CAST(t1.name AS VARCHAR(1000)) || CAST(? AS VARCHAR(1000))) "
           : "CONCAT(t1.name,?) ";
        String expectedSQL = "SELECT t2.id FROM CR_ODR t2 "
            + "INNER JOIN CR_CUST t3 ON t2.CUSTOMER_ID = t3.id WHERE (t3.name IN ("
            + "SELECT " + useCast 
            + "FROM CR_ODR t0 INNER JOIN CR_CUST t1 ON t0.CUSTOMER_ID = t1.id WHERE (t0.quantity > ?)))";

        executeAndCompareSQL(jpql, expectedSQL);
        CriteriaQuery<Integer> q = cb.createQuery(Integer.class);
        Root<Order> o = q.from(Order.class);
        q.select(o.get(Order_.id));

        Subquery<String> sq = q.subquery(String.class);
        Root<Order> o2 = sq.from(Order.class);
        sq.where(cb.gt(o2.get(Order_.quantity), 10));

        q.where(cb.in(o.get(Order_.customer).get(Customer_.name)).value(
                sq.select(cb.concat(
                        o2.get(Order_.customer).get(Customer_.name), "XX"))));
        assertEquivalence(q, jpql);
    }

    public void testSubquery21() {
        String jpql = "select c from Customer c where c.creditRating ="
            + " (select CASE WHEN o2.quantity > 10 THEN "
            + "            Customer$CreditRating.POOR "
            + "        WHEN o2.quantity = 10 THEN "
            + "            Customer$CreditRating.GOOD "
            + "        ELSE "
            + "            Customer$CreditRating.EXCELLENT "
            + "        END from Order o2 "
            + "   where c.id = o2.customer.id)";

        String expectedSQL = "SELECT t0.id, t0.accountNum, t2.id, t2.city, t2.country, t2.county, t2.state, " +
        "t2.street, t3.userid, t3.DTYPE, t3.age, t3.compName, t3.creditRating, t3.name, t2.zipCode, " + 
        "t0.balanceOwed, t0.creditRating, t0.filledOrderCount, t0.firstName, t0.lastName, t0.name, " + 
        "t0.status " + 
        "FROM CR_CUST t0 " + 
        "LEFT OUTER JOIN CR_ADDR t2 ON t0.ADDRESS_ID = t2.id " + 
        "LEFT OUTER JOIN CompUser t3 ON t2.id = t3.ADD_ID " + 
        "WHERE (t0.creditRating = (" + 
        "SELECT  CASE  WHEN t1.quantity > ? THEN 0 WHEN t1.quantity = ? THEN 1 ELSE 2 END  " + 
        "FROM CR_ODR t1 WHERE (t0.id = t1.CUSTOMER_ID)))";
        executeAndCompareSQL(jpql, expectedSQL);

        CriteriaQuery<Customer> q = cb.createQuery(Customer.class);
        Root<Customer> c = q.from(Customer.class);
        q.select(c);

        Subquery<Object> sq = q.subquery(Object.class);
        Root<Order> o2 = sq.from(Order.class);
        sq.where(cb.equal(
            c.get(Customer_.id), 
            o2.get(Order_.customer).get(Customer_.id)));
        Expression<Object> generalCase = cb.selectCase()
                .when(cb.gt(o2.get(Order_.quantity), 10), 
                        Customer.CreditRating.POOR)
                .when(cb.equal(o2.get(Order_.quantity), 10), 
                        Customer.CreditRating.GOOD)
                .otherwise(Customer.CreditRating.EXCELLENT);
        
        sq.select(generalCase);
        q.where(cb.equal(c.get(Customer_.creditRating), sq));
        assertEquivalence(q, jpql);
    }

    public void testSubquery22() {
        String jpql = "select c from Customer c "
            + "where c.creditRating = (select COALESCE (c1.creditRating, "
            + "org.apache.openjpa.persistence.criteria."
            + "Customer$CreditRating.POOR) "
            + "from Customer c1 where c1.name = 'Famzy') order by c.name "
            + "DESC";

        String expectedSQL = "SELECT t0.id, t0.accountNum, t2.id, t2.city, t2.country, t2.county, t2.state, " + 
        "t2.street, t3.userid, t3.DTYPE, t3.age, t3.compName, t3.creditRating, t3.name, t2.zipCode, " + 
        "t0.balanceOwed, t0.creditRating, t0.filledOrderCount, t0.firstName, t0.lastName, t0.name, " + 
        "t0.status " + 
        "FROM CR_CUST t0 " + 
        "LEFT OUTER JOIN CR_ADDR t2 ON t0.ADDRESS_ID = t2.id " + 
        "LEFT OUTER JOIN CompUser t3 ON t2.id = t3.ADD_ID WHERE (t0.creditRating = (" + 
        "SELECT  COALESCE(t1.creditRating,0) FROM CR_CUST t1 WHERE (t1.name = ?))) " + 
        "ORDER BY t0.name DESC";
        executeAndCompareSQL(jpql, expectedSQL);

        CriteriaQuery<Customer> q = cb.createQuery(Customer.class);
        Root<Customer> c = q.from(Customer.class);
        q.select(c);
        q.orderBy(cb.desc(c.get(Customer_.name)));        

        Subquery<Customer.CreditRating> sq =
            q.subquery(Customer.CreditRating.class);
        Root<Customer> c1 = sq.from(Customer.class);
        sq.where(cb.equal(c1.get(Customer_.name), "Famzy"));
        
        Expression<Customer.CreditRating> coalesce = cb.coalesce(
                c1.get(Customer_.creditRating), 
                Customer.CreditRating.POOR);
        sq.select(coalesce);
        q.where(cb.equal(c.get(Customer_.creditRating),sq));
        
        assertEquivalence(q, jpql);
    }

    public void testSubquery23() {
        String jpql = "select c from Customer c "
            + "where c.creditRating = (select NULLIF (c1.creditRating, "
            + "org.apache.openjpa.persistence.criteria."
            + "Customer$CreditRating.POOR) "
            + "from Customer c1 where c1.name = 'Famzy') "
            + "order by c.name DESC";

        String expectedSQL = "SELECT t0.id, t0.accountNum, t2.id, t2.city, t2.country, t2.county, " + 
        "t2.state, t2.street, t3.userid, t3.DTYPE, t3.age, t3.compName, t3.creditRating, t3.name, " + 
        "t2.zipCode, t0.balanceOwed, t0.creditRating, t0.filledOrderCount, t0.firstName, t0.lastName, " + 
        "t0.name, t0.status " + 
        "FROM CR_CUST t0 " + 
        "LEFT OUTER JOIN CR_ADDR t2 ON t0.ADDRESS_ID = t2.id " + 
        "LEFT OUTER JOIN CompUser t3 ON t2.id = t3.ADD_ID WHERE (t0.creditRating = (" + 
        "SELECT  NULLIF(t1.creditRating,0) FROM CR_CUST t1 WHERE (t1.name = ?))) " + 
        "ORDER BY t0.name DESC";
        executeAndCompareSQL(jpql, expectedSQL);
        CriteriaQuery<Customer> q = cb.createQuery(Customer.class);
        Root<Customer> c = q.from(Customer.class);
        q.select(c);
        q.orderBy(cb.desc(c.get(Customer_.name)));        

        Subquery<Customer.CreditRating> sq =
            q.subquery(Customer.CreditRating.class);
        Root<Customer> c1 = sq.from(Customer.class);
        sq.where(cb.equal(c1.get(Customer_.name), "Famzy"));
        
        q.where(cb.equal(c.get(Customer_.creditRating),
            sq.select(cb.nullif(c1.get(Customer_.creditRating),
                Customer.CreditRating.POOR))));    
        assertEquivalence(q, jpql);

    }

    public void testSubquery24() {
        String jpql = "select o from Order o where o.count > (select count(o) from Order o)";

        String expectedSQL = "SELECT t0.id, t0.cnt, t2.id, t2.accountNum, t3.id, t3.city, t3.country, " + 
        "t3.county, t3.state, t3.street, t4.userid, t4.DTYPE, t4.age, t4.compName, t4.creditRating, " + 
        "t4.name, t3.zipCode, t2.balanceOwed, t2.creditRating, t2.filledOrderCount, t2.firstName, " + 
        "t2.lastName, t2.name, t2.status, t0.delivered, t0.name, t0.orderTs, t0.quantity, " + 
        "t0.totalCost " + 
        "FROM CR_ODR t0 " + 
        "LEFT OUTER JOIN CR_CUST t2 ON t0.CUSTOMER_ID = t2.id " + 
        "LEFT OUTER JOIN CR_ADDR t3 ON t2.ADDRESS_ID = t3.id " + 
        "LEFT OUTER JOIN CompUser t4 ON t3.id = t4.ADD_ID WHERE (t0.cnt > (" + 
        "SELECT COUNT(t1.id) FROM CR_ODR t1))";
        executeAndCompareSQL(jpql, expectedSQL);

        CriteriaQuery<Order> q = cb.createQuery(Order.class);
        Root<Order> o = q.from(Order.class);
        q.select(o);

        Subquery<Long> sq = q.subquery(Long.class);
        Root<Order> o2 = sq.from(Order.class);
        sq.select(cb.count(o2));

        q.where(cb.gt(o.get(Order_.count), sq));
        assertEquivalence(q, jpql);
    }

    public void testSubquery25() {
        String jpql = "select o from Order o where o.count > (select count(o2) from Order o2)";

        String expectedSQL = "SELECT t0.id, t0.cnt, t2.id, t2.accountNum, t3.id, t3.city, t3.country, " + 
        "t3.county, t3.state, t3.street, t4.userid, t4.DTYPE, t4.age, t4.compName, t4.creditRating, " + 
        "t4.name, t3.zipCode, t2.balanceOwed, t2.creditRating, t2.filledOrderCount, t2.firstName, " + 
        "t2.lastName, t2.name, t2.status, t0.delivered, t0.name, t0.orderTs, t0.quantity, " + 
        "t0.totalCost " + 
        "FROM CR_ODR t0 " + 
        "LEFT OUTER JOIN CR_CUST t2 ON t0.CUSTOMER_ID = t2.id " + 
        "LEFT OUTER JOIN CR_ADDR t3 ON t2.ADDRESS_ID = t3.id " + 
        "LEFT OUTER JOIN CompUser t4 ON t3.id = t4.ADD_ID WHERE (t0.cnt > (" + 
        "SELECT COUNT(t1.id) FROM CR_ODR t1))";
        executeAndCompareSQL(jpql, expectedSQL);

        CriteriaQuery<Order> q = cb.createQuery(Order.class);
        Root<Order> o = q.from(Order.class);
        q.select(o);

        Subquery<Long> sq = q.subquery(Long.class);
        Root<Order> o2 = sq.from(Order.class);
        sq.select(cb.count(o2));

        q.where(cb.gt(o.get(Order_.count), sq));
        assertEquivalence(q, jpql);
    }

    
    public void testSubSelectMaxDateRange() {
        String jpql = "SELECT e,d from Employee e, Dependent d "
            + "WHERE e.empId = :empid "
            + "AND d.id.empid = (SELECT MAX (e2.empId) FROM Employee e2) "
            + "AND d.id.effDate > :minDate "
            + "AND d.id.effDate < :maxDate ";

        Query jQ = em.createQuery(jpql);
        jQ.setParameter("empid", (long) 101);
        jQ.setParameter("minDate", new Date(100));
        jQ.setParameter("maxDate", new Date(100000));

        CriteriaQuery<?> q = cb.createQuery();
        Root<Employee> e = q.from(Employee.class);
        Root<Dependent> d = q.from(Dependent.class);
        q.multiselect(e, d);
        ParameterExpression<Integer> empid = cb.parameter(Integer.class, "empid");
        ParameterExpression<Date> minDate = cb.parameter(Date.class, "minDate");
        ParameterExpression<Date> maxDate = cb.parameter(Date.class, "maxDate");

        Subquery<Integer> sq = q.subquery(Integer.class);
        Root<Employee> e2 = sq.from(Employee.class);
        sq.select(cb.max(e2.get(Employee_.empId)));
        Predicate p1 = cb.equal(e.get(Employee_.empId), empid); 
        Predicate p2 = cb.equal(d.get(Dependent_.id).get(DependentId_.empid), sq);
        Predicate p3 = cb.greaterThan(d.get(Dependent_.id).get(DependentId_.effDate), minDate);
        Predicate p4 = cb.lessThan(d.get(Dependent_.id).get(DependentId_.effDate), maxDate);
        
        q.where(cb.and(cb.and(cb.and(p1, p2), p3), p4));
        assertEquivalence(new QueryDecorator() {
            public void decorate(Query q) {
                q.setParameter("empid",   101L);
                q.setParameter("minDate", new Date(100));
                q.setParameter("maxDate", new Date(100000));
            }
        }, q, jpql);
    }
    
    public void testCorrelatedNestedSubquery1() {
        String jpql = "Select Object (c) From Customer c Where Not Exists ("
                + "   Select a.id From Account As a Where "
                + "        a.customer = c  And "
                + "        exists (select o.id from Order o where o.customer = c and o.count = 1))";

        String expectedSQL = "SELECT t1.id, t1.accountNum, t3.id, t3.city, t3.country, t3.county, t3.state, "
            + "t3.street, t4.userid, t4.DTYPE, t4.age, t4.compName, t4.creditRating, t4.name, t3.zipCode, "
            + "t1.balanceOwed, t1.creditRating, t1.filledOrderCount, t1.firstName, t1.lastName, t1.name, "
            + "t1.status "
            + "FROM CR_CUST t1 LEFT OUTER JOIN CR_ADDR t3 ON t1.ADDRESS_ID = t3.id "
            + "LEFT OUTER JOIN CompUser t4 ON t3.id = t4.ADD_ID WHERE (NOT (EXISTS ("
            + "SELECT t0.id FROM CR_ACCT t0 WHERE (t0.CUSTOMER_ID = t1.id AND EXISTS ("
            + "SELECT t2.id FROM CR_ODR t2 WHERE (t2.CUSTOMER_ID = t1.id AND t2.cnt = ?))))))";
        
        executeAndCompareSQL(jpql, expectedSQL);

        CriteriaQuery<Customer> q = cb.createQuery(Customer.class);
        Root<Customer> c = q.from(Customer.class);
        q.select(c);

        Subquery<Long> sq = q.subquery(Long.class);
        Root<Account> a = sq.from(Account.class);
        sq.select(a.get(Account_.id));
        
        Subquery<Integer> sq1 = sq.subquery(Integer.class);
        Root<Order> o = sq1.from(Order.class);
        sq1.select(o.get(Order_.id));
        sq1.where(cb.and(cb.equal(o.get(Order_.customer), c), cb.equal(o.get(Order_.count), 1)));
        
        
        sq.where(cb.and(cb.equal(a.get(Account_.customer), c), cb.exists(sq1)));

        q.where(cb.exists(sq).not());
        assertEquivalence(q, jpql);
    }
    
    public void testCorrelatedNestedSubquery1a() {
        String jpql = "Select Object (o) From Product o Where Not Exists ("
            + "   Select a.id From Account As a Where "
            + "        a.product = o  And "
            + "        exists (select r.id from Request r where r.account = a and r.status = 1))";

        String expectedSQL = "SELECT t1.pid, t1.version, t1.productType FROM CR_PRD t1 WHERE (NOT (EXISTS ("
            + "SELECT t0.id FROM CR_ACCT t0 WHERE (t0.PRODUCT_PID = t1.pid AND EXISTS ("
            + "SELECT t2.id FROM Request t2 WHERE (t2.ACCOUNT_ID = t0.id AND t2.status = ?))))))";
        
        executeAndCompareSQL(jpql, expectedSQL);

        CriteriaQuery<Product> q = cb.createQuery(Product.class);
        Root<Product> o = q.from(Product.class);
        q.select(o);

        Subquery<Long> sq = q.subquery(Long.class);
        Root<Account> a = sq.from(Account.class);
        sq.select(a.get(Account_.id));
        
        Subquery<Integer> sq1 = sq.subquery(Integer.class);
        Root<Request> r = sq1.from(Request.class);
        sq1.select(r.get(Request_.id));
        sq1.where(cb.and(cb.equal(r.get(Request_.account), a), cb.equal(r.get(Request_.status), 1)));
        
        
        sq.where(cb.and(cb.equal(a.get(Account_.product), o), cb.exists(sq1)));

        q.where(cb.exists(sq).not());
        assertEquivalence(q, jpql);
    }
    
    public void testPluralCorrelatedJoin1() {
        String jpql = "SELECT o.quantity, o.totalCost*1.08, "
            + "a.zipCode FROM Customer c JOIN c.orders o JOIN c.address a "
            + "WHERE a.state = " 
            + "(SELECT o.name from Customer c1 JOIN c1.orders o1 where o.quantity = o1.quantity)";
        
        String expectedSQL = "SELECT t2.quantity, (t2.totalCost * ?), t1.zipCode "
            + "FROM CR_CUST t0 INNER JOIN CR_ODR t2 ON t0.id = t2.CUSTOMER_ID "
            + "INNER JOIN CR_ADDR t1 ON t0.ADDRESS_ID = t1.id "
            + "WHERE (t1.state = "
            + "(SELECT t2.name "
            + "FROM CR_CUST t3 INNER JOIN CR_ODR t4 ON t3.id = t4.CUSTOMER_ID "
            + "WHERE (t2.quantity = t4.quantity)) AND 1 = 1)";
        executeAndCompareSQL(jpql, expectedSQL);

        CriteriaQuery<?> q = cb.createQuery();
        Root<Customer> cust = q.from(Customer.class);
        SetJoin<Customer, Order> order = cust.joinSet("orders");
        Join<Customer, Address> address = cust.join("address");
        
        Subquery<String> sq = q.subquery(String.class);
        Root<Customer> cust1 = sq.from(Customer.class);
        SetJoin<Customer, Order> order1 = cust1.joinSet("orders");
        SetJoin<Customer, Order> corrJoin = sq.correlate(order);
        sq.where(cb.equal(corrJoin.get(Order_.quantity), order1.get(Order_.quantity)));
        q.where(cb.equal(address.get("state"), sq.select(corrJoin.get(Order_.name))));
        Expression<Double> taxedCost = cb.prod(order.get(Order_.totalCost), 1.08);
        q.multiselect(order.get("quantity"), taxedCost, address.get("zipCode"));

        assertEquivalence(q, jpql);
    }

    public void testPluralCorrelatedJoin2() {
        String jpql = "SELECT d.name FROM Department d JOIN d.employees e JOIN e.manager m "
           + "WHERE m.salary = (SELECT e1.salary from Employee e1 JOIN e1.manager m1 "
           + "where m.name = m1.name)";
        String expectedSQL = "SELECT t0.name FROM CR_DEPT t0 "
            + "INNER JOIN CR_DEPT_CR_EMP t1 ON t0.deptNo = t1.DEPARTMENT_DEPTNO "
            + "INNER JOIN CR_EMP t2 ON t1.EMPLOYEES_EMPID = t2.empId "
            + "INNER JOIN CR_EMP t6 ON t1.EMPLOYEES_EMPID = t6.empId "
            + "INNER JOIN CR_MGR t3 ON t2.MANAGER_ID = t3.id "
            + "INNER JOIN CR_MGR t7 ON t6.MANAGER_ID = t7.id "
            + "WHERE (t3.salary = (SELECT t4.salary FROM CR_EMP t4 "
            + "INNER JOIN CR_MGR t5 ON t4.MANAGER_ID = t5.id "
            + "WHERE (t7.name = t5.name)) AND 1 = 1)";
        executeAndCompareSQL(jpql, expectedSQL);

        CriteriaQuery<String> q = cb.createQuery(String.class);
        Root<Department> d = q.from(Department.class);
        SetJoin<Department, Employee> e = d.joinSet("employees");
        Join<Employee, Manager> m = e.join("manager");
        Subquery<Long> sq = q.subquery(Long.class);
        Root<Employee> e1 = sq.from(Employee.class);
        Join<Employee, Manager> m1 = e1.join("manager");
        Join<Employee, Manager> corrJoin = sq.correlate(m);
        sq.where(cb.equal(corrJoin.get(Manager_.name), m1.get(Manager_.name)));
        q.where(cb.equal(m.get(Manager_.salary), sq.select(e1.get(Employee_.salary))));
        q.select(d.get(Department_.name));
        
        assertEquivalence(q, jpql);
    }

    public void testPluralCorrelatedJoin3() {
        String jpql = "SELECT o FROM Order o JOIN o.customer c JOIN c.accounts a WHERE 10000 < "
            + "ANY (SELECT a1.balance FROM Account a1 WHERE a.owner = a1.owner)";
        String expectedSQL = "SELECT t0.id, t0.cnt, t6.id, t6.accountNum, t7.id, t7.city, "
            + "t7.country, t7.county, t7.state, t7.street, t8.userid, t8.DTYPE, t8.age, "
            + "t8.compName, t8.creditRating, t8.name, t7.zipCode, t6.balanceOwed, "
            + "t6.creditRating, t6.filledOrderCount, t6.firstName, t6.lastName, t6.name, "
            + "t6.status, t0.delivered, t0.name, t0.orderTs, t0.quantity, t0.totalCost "
            + "FROM CR_ODR t0 "
            + "INNER JOIN CR_CUST t1 ON t0.CUSTOMER_ID = t1.id "
            + "LEFT OUTER JOIN CR_CUST t6 ON t0.CUSTOMER_ID = t6.id "
            + "INNER JOIN CR_CUST_CR_ACCT t2 ON t1.id = t2.CUSTOMER_ID "
            + "LEFT OUTER JOIN CR_ADDR t7 ON t6.ADDRESS_ID = t7.id "
            + "INNER JOIN CR_ACCT t3 ON t2.ACCOUNTS_ID = t3.id "
            + "INNER JOIN CR_ACCT t4 ON t2.ACCOUNTS_ID = t4.id "
            + "LEFT OUTER JOIN CompUser t8 ON t7.id = t8.ADD_ID WHERE (? < "
            + "ANY (SELECT t5.balance FROM CR_ACCT t5 "
            + "WHERE (t4.OWNER_ID = t5.OWNER_ID)) AND 1 = 1)";
        executeAndCompareSQL(jpql, expectedSQL);
        
        CriteriaQuery<Order> q = cb.createQuery(Order.class);
        Root<Order> o = q.from(Order.class);
        Join<Order,Customer> c = o.join(Order_.customer);
        ListJoin<Customer,Account> a = c.joinList("accounts");
        q.select(o);
        Subquery<Integer> sq = q.subquery(Integer.class);
        Root<Account> a1 = sq.from(Account.class);
        ListJoin<Customer,Account> corrJoin = sq.correlate(a);
        sq.select(a1.get(Account_.balance));
        sq.where(cb.equal(corrJoin.get(Account_.owner), a1.get(Account_.owner)));
        q.where(cb.lt(cb.literal(10000), cb.any(sq)));

        assertEquivalence(q, jpql);
    }    

    public void testPluralCorrelatedJoin4() {
        String jpql = 
        "SELECT o.quantity FROM Order o JOIN o.customer c JOIN c.accounts a JOIN a.owner owner WHERE 10000 < "
        + "ANY (SELECT a1.balance FROM Account a1 JOIN a1.owner owner1 WHERE owner.name = owner1.name)";
        String expectedSQL = "SELECT t0.quantity FROM CR_ODR t0 "
            + "INNER JOIN CR_CUST t1 ON t0.CUSTOMER_ID = t1.id "
            + "INNER JOIN CR_CUST_CR_ACCT t2 ON t1.id = t2.CUSTOMER_ID "
            + "INNER JOIN CR_ACCT t3 ON t2.ACCOUNTS_ID = t3.id "
            + "INNER JOIN CR_ACCT t7 ON t2.ACCOUNTS_ID = t7.id "
            + "INNER JOIN CR_PSN t4 ON t3.OWNER_ID = t4.id "
            + "INNER JOIN CR_PSN t8 ON t7.OWNER_ID = t8.id WHERE (? < "
            + "ANY (SELECT t5.balance "
            + "FROM CR_ACCT t5 INNER JOIN CR_PSN t6 ON t5.OWNER_ID = t6.id "
            + "WHERE (t8.name = t6.name)) AND 1 = 1)";
        executeAndCompareSQL(jpql, expectedSQL);
        
        CriteriaQuery<Integer> q = cb.createQuery(Integer.class);
        Root<Order> o = q.from(Order.class);
        Join<Order,Customer> c = o.join(Order_.customer);
        ListJoin<Customer,Account> a = c.joinList("accounts");
        Join<Account,Person> owner = a.join(Account_.owner);
        
        Subquery<Integer> sq = q.subquery(Integer.class);
        Root<Account> a1 = sq.from(Account.class);
        Join<Account,Person> owner1 = a1.join(Account_.owner);
        Join<Account,Person> corrJoin = sq.correlate(owner);
        sq.where(cb.equal(corrJoin.get(Person_.name), owner1.get(Person_.name)));
        q.where(cb.lt(cb.literal(10000), cb.any(sq.select(a1.get(Account_.balance)))));
        q.select(o.get(Order_.quantity));
        assertEquivalence(q, jpql);
    }    

    public void testPluralCorrelatedJoin5() {
        String jpql = "SELECT o.quantity FROM Order o JOIN o.customer c JOIN c.accounts a WHERE c.name = "
            + "ANY (SELECT owner.name FROM a.owner owner WHERE owner.id = 1)";
        String expectedSQL = "SELECT t0.quantity FROM CR_ODR t0 "
            + "INNER JOIN CR_CUST t1 ON t0.CUSTOMER_ID = t1.id "
            + "INNER JOIN CR_CUST_CR_ACCT t2 ON t1.id = t2.CUSTOMER_ID "
            + "INNER JOIN CR_ACCT t3 ON t2.ACCOUNTS_ID = t3.id "
            + "INNER JOIN CR_ACCT t4 ON t2.ACCOUNTS_ID = t4.id "
            + "WHERE (t1.name = "
            + "ANY (SELECT t5.name FROM CR_PSN t5 WHERE (t5.id = ? AND t4.OWNER_ID = t5.id)) "
            + "AND 1 = 1)";
        executeAndCompareSQL(jpql, expectedSQL);
        
        CriteriaQuery<Integer> q = cb.createQuery(Integer.class);
        Root<Order> o = q.from(Order.class);
        Join<Order,Customer> c = o.join(Order_.customer);
        ListJoin<Customer,Account> a = c.joinList("accounts");
         
        Subquery<String> sq = q.subquery(String.class);
        ListJoin<Customer,Account> a1 = sq.correlate(a);
        Join<Account,Person> owner = a1.join(Account_.owner);
        sq.where(cb.equal(owner.get(Person_.id), 1));
        q.where(cb.equal(c.get(Customer_.name), cb.any(sq.select(owner.get(Person_.name)))));
        q.select(o.get(Order_.quantity));
        assertEquivalence(q, jpql);
    }    

}
