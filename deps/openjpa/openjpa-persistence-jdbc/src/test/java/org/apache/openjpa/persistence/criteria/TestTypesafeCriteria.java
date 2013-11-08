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
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Parameter;
import javax.persistence.Query;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.MapJoin;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.criteria.SetJoin;
import javax.persistence.criteria.Subquery;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;

import org.apache.openjpa.jdbc.sql.AbstractSQLServerDictionary;
import org.apache.openjpa.jdbc.sql.OracleDictionary;
import org.apache.openjpa.persistence.test.AllowFailure;

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
public class TestTypesafeCriteria extends CriteriaTest {
    private static final String TRUE_JPQL = "SELECT p FROM Person p WHERE 1=1";
    private static final String FALSE_JPQL = "SELECT p FROM Person p WHERE 1<>1";
    
    public void testTrueLiteral() {
        CriteriaQuery<Person> q = cb.createQuery(Person.class);
        q.from(Person.class);
        assertEquivalence(q.where(cb.literal(Boolean.TRUE)), TRUE_JPQL);
    }
    
    public void testFalseLiteral() {
        CriteriaQuery<Person> q = cb.createQuery(Person.class);
        q.from(Person.class);
        assertEquivalence(q.where(cb.literal(Boolean.FALSE)), FALSE_JPQL);
    }
    
    public void testDefaultAndIsTrue() {
        CriteriaQuery<Person> q = cb.createQuery(Person.class);
        q.from(Person.class);
        assertEquivalence(q.where(cb.and()), TRUE_JPQL);
    }
    
    public void testDefaultOrIsFalse() {
        CriteriaQuery<Person> q = cb.createQuery(Person.class);
        q.from(Person.class);
        assertEquivalence(q.where(cb.or()), FALSE_JPQL);
    }

    public void testZeroDisjunctIsFalse() {
        CriteriaQuery<Person> q = cb.createQuery(Person.class);
        q.from(Person.class);
        assertEquivalence(q.where(cb.disjunction()), FALSE_JPQL);
    }
    
    public void testZeroConjunctIsTrue() {
        CriteriaQuery<Person> q = cb.createQuery(Person.class);
        q.from(Person.class);
        assertEquivalence(q.where(cb.conjunction()), TRUE_JPQL);
    }

    public void testExpressions() {
        String jpql = "SELECT o.quantity, o.totalCost*1.08, "
                + "a.zipCode FROM Customer c JOIN c.orders o JOIN c.address a "
                + "WHERE a.state = 'CA' AND a.county = 'Santa Clara'";
        CriteriaQuery<?> q = cb.createQuery();
        Root<Customer> cust = q.from(Customer.class);
        SetJoin<Customer, Order> order = cust.joinSet("orders");
        Join<Customer, Address> address = cust.join("address");
        q.where(cb.equal(address.get("state"), "CA"), cb.equal(address
            .get("county"), "Santa Clara"));
        Expression<Double> taxedCost = cb.prod(order.get(Order_.totalCost), 1.08);
        q.multiselect(order.get("quantity"), taxedCost, address.get("zipCode"));

        assertEquivalence(q, jpql);
    }
    public void testExplictRoot() {
        String jpql = "select a from Account a";
        
        CriteriaQuery<Account> c = cb.createQuery(Account.class);
        Root<Account> account = c.from(Account.class);
        c.select(account);

        assertEquivalence(c, jpql);
    }

    public void testImplicitRoot() {
        String jpql = "select a from Account a";
        
        CriteriaQuery<Account> c = cb.createQuery(Account.class);
        c.from(Account.class);

        assertEquivalence(c, jpql);
    }

    public void testEqualWithAttributeAndLiteral() {
        String jpql = "select a from Account a where a.balance=100";

        CriteriaQuery c = cb.createQuery();
        Root<Account> account = c.from(Account.class);
        c.select(account).where(cb.equal(account.get(Account_.balance), 100));

        assertEquivalence(c, jpql);
    }

    public void testEqualWithAttributeAndAttribute() {
        String jpql = "select a from Account a where a.balance=a.loan";

        CriteriaQuery<Account> c = cb.createQuery(Account.class);
        Root<Account> account = c.from(Account.class);
        c.select(account).where(
                cb.equal(account.get(Account_.balance), account
                        .get(Account_.loan)));

        assertEquivalence(c, jpql);
    }

    public void testProjection() {
        String jpql = "select a.balance,a.loan from Account a";
        CriteriaQuery<Tuple> c = cb.createTupleQuery();
        Root<Account> account = c.from(Account.class);
        c.multiselect(account.get(Account_.balance), account.get(Account_.loan));
        assertEquivalence(c, jpql);
    }

    public void testAbsExpression() {
        String jpql = "select a from Account a where abs(a.balance)=100";

        CriteriaQuery<Account> c = cb.createQuery(Account.class);
        Root<Account> account = c.from(Account.class);

        c.select(account).where(cb.equal(cb.abs(account.get(Account_.balance)), 100));
        assertEquivalence(c, jpql);
    }

    public void testAvgExpression() {
        String jpql = "select avg(a.balance) from Account a";

        CriteriaQuery<Double> c = cb.createQuery(Double.class);
        Root<Account> account = c.from(Account.class);

        c.select(cb.avg(account.get(Account_.balance)));
        assertEquivalence(c, jpql);
    }

    public void testInPredicate() {
        String jpql = "select a from Account a where a.name in ('X','Y','Z')";
        CriteriaQuery<Account> c = cb.createQuery(Account.class);
        Root<Account> account = c.from(Account.class);
        c.where(cb.in(account.get(Account_.name)).value("X").value("Y").value("Z"));
        assertEquivalence(c, jpql);
    }

    public void testInPredicateWithPath() {
        String jpql = "select a from Account a where a.owner.name in ('X','Y','Z')";
        CriteriaQuery<Account> c = cb.createQuery(Account.class);
        Root<Account> account = c.from(Account.class);
        c.where(cb.in(account.get(Account_.owner).get(Person_.name)).value("X")
                .value("Y").value("Z"));
        assertEquivalence(c, jpql);
    }

    public void testBinaryPredicate() {
        String jpql = "select a from Account a where a.balance>100 and a.balance<200";

        CriteriaQuery<Account> c = cb.createQuery(Account.class);
        Root<Account> account = c.from(Account.class);
        c.select(account).where(
                cb.and(cb.greaterThan(account.get(Account_.balance), 100), cb
                        .lessThan(account.get(Account_.balance), 200)));

        assertEquivalence(c, jpql);
    }

    public void testEqualWithAttributeAndUnaryExpression() {
        String jpql = "select a from Account a where a.balance=abs(a.balance)";

        CriteriaQuery<Account> c = cb.createQuery(Account.class);
        Root<Account> account = c.from(Account.class);
        c.select(account).where(
                cb.equal(account.get(Account_.balance), cb.abs(account
                        .get(Account_.balance))));

        assertEquivalence(c, jpql);
    }

    public void testBetweenExpression() {
        String jpql =
            "select a from Account a where a.balance between 100 and 200";

        CriteriaQuery<Account> c = cb.createQuery(Account.class);
        Root<Account> account = c.from(Account.class);
        c.select(account).where(
                cb.between(account.get(Account_.balance), 100, 200));

        assertEquivalence(c, jpql);
    }

    public void testSimplePath() {
        String jpql = "select a from Account a where a.owner.name='Pinaki'";
        CriteriaQuery<Account> c = cb.createQuery(Account.class);
        Root<Account> a = c.from(Account.class);
        c.where(cb.equal(a.get(Account_.owner).get(Person_.name), "Pinaki"));

        assertEquivalence(c, jpql);
    }

    public void testSimpleLeftJoin() {
        String jpql = "SELECT c FROM Customer c LEFT JOIN c.orders o ";
        CriteriaQuery<Customer> c = cb.createQuery(Customer.class);
        c.from(Customer.class).join(Customer_.orders, JoinType.LEFT);
        assertEquivalence(c, jpql);
    }

    public void testMultipartNavigation() {
        String jpql = "select a from A a where a.b.age=22";
        
        CriteriaQuery<A> cq = cb.createQuery(A.class);
        Root<A> a = cq.from(A.class);
        cq.where(cb.equal(a.get(A_.b).get(B_.age), 22));
        
        assertEquivalence(cq, jpql);
    }
    
    public void testMultiLevelJoins() {
        String jpql = "SELECT c FROM Customer c JOIN c.orders o " 
                    + "JOIN o.lineItems i WHERE i.product.productType = 'printer'";

        CriteriaQuery<Customer> cq = cb.createQuery(Customer.class);
        Root<Customer> c = cq.from(Customer.class);
        Join<Customer, Order> o = c.join(Customer_.orders);
        Join<Order, LineItem> i = o.join(Order_.lineItems);
        cq.select(c)
            .where(cb.equal(i.get(LineItem_.product)
            .get(Product_.productType), "printer"));

        assertEquivalence(cq, jpql);
    }

    public void testJoinsNotPresentInWhereClause() {
        String jpql = "SELECT c FROM Customer c LEFT JOIN c.orders o WHERE c.status = 1";
        
        CriteriaQuery<Customer> c = cb.createQuery(Customer.class);
        Root<Customer> cust = c.from(Customer.class);
        Join<Customer, Order> order = cust
        .join(Customer_.orders, JoinType.LEFT);
        c.where(cb.equal(cust.get(Customer_.status), 1)).select(cust);

        assertEquivalence(c, jpql);
    }

    public void testFetchJoins() {
        String jpql = "SELECT d FROM Department d LEFT JOIN FETCH d.employees WHERE d.deptNo = 1";
        
        CriteriaQuery<Department> q = cb.createQuery(Department.class);
        Root<Department> d = q.from(Department.class);
        d.fetch(Department_.employees, JoinType.LEFT);
        q.where(cb.equal(d.get(Department_.deptNo), 1)).select(d);

        assertEquivalence(q, jpql);
    }

    public void testJoinedPathInProjection() {
        String jpql = "SELECT p.vendor FROM Employee e JOIN e.contactInfo c JOIN c.phones p "
                    + "WHERE c.address.zipCode = '95054'";

        CriteriaQuery<String> cq = cb.createQuery(String.class);
        Root<Employee> e = cq.from(Employee.class);
        Join<Contact, Phone> p = e.join(Employee_.contactInfo).join(Contact_.phones);
        cq.where(cb.equal(e.get(Employee_.contactInfo).get(Contact_.address)
                .get(Address_.zipCode), "95054"));
        cq.select(p.get(Phone_.vendor));

        assertEquivalence(cq, jpql);
    }

    public void testKeyExpression() {
        String jpql = "select i.name, VALUE(p) from Item i join i.photos p where KEY(p) like 'egret'";

        CriteriaQuery<Tuple> q = cb.createTupleQuery();
        Root<Item> item = q.from(Item.class);
        MapJoin<Item, String, Photo> photo = item.join(Item_.photos);
        q.multiselect(item.get(Item_.name), photo).where(cb.like(photo.key(), "%egret%"));

        assertEquivalence(q, jpql);
    }

    public void testIndexExpression() {
        String jpql = "SELECT t FROM CreditCard c JOIN c.transactionHistory t "
            + "WHERE c.customer.accountNum = 321987 AND INDEX(t) BETWEEN 0 AND 9";
        
        CriteriaQuery<TransactionHistory> q = cb.createQuery(TransactionHistory.class);
        Root<CreditCard> c = q.from(CreditCard.class);
        ListJoin<CreditCard, TransactionHistory> t = c
        .join(CreditCard_.transactionHistory);
        q.select(t).where(cb.and(
            cb.equal(c.get(CreditCard_.customer).get(Customer_.accountNum),
                321987), 
            cb.between(t.index(), 0, 9)));

        assertEquivalence(q, jpql);
    }

    public void testIsEmptyExpression() {
        String jpql = "SELECT o FROM Order o WHERE o.lineItems IS EMPTY";
        
        CriteriaQuery<Order> q = cb.createQuery(Order.class);
        Root<Order> order = q.from(Order.class);
        q.where(cb.isEmpty(order.get(Order_.lineItems))).select(order);

        assertEquivalence(q, jpql);
    }

    public void testExpressionInProjection() {
        String jpql = "SELECT o.quantity, o.totalCost*1.08, "
            + "a.zipCode FROM Customer c JOIN c.orders o JOIN c.address a "
            + "WHERE a.state = 'CA' AND a.county = 'Santa Clara'";
        
        CriteriaQuery<Tuple> cq = cb.createTupleQuery();
        Root<Customer> c = cq.from(Customer.class);
        Join<Customer, Order> o = c.join(Customer_.orders);
        Join<Customer, Address> a = c.join(Customer_.address);
        cq.where(cb.and(
            cb.equal(a.get(Address_.state), "CA"), 
            cb.equal(a.get(Address_.county), "Santa Clara")));
        cq.multiselect(o.get(Order_.quantity), cb.prod(o
                .get(Order_.totalCost), 1.08), a.get(Address_.zipCode));

        assertEquivalence(cq, jpql);
    }

    public void testTypeExpression() {
        String jpql = "SELECT TYPE(e) FROM Employee e WHERE TYPE(e) <> Exempt";
        
        CriteriaQuery<Object> q = cb.createQuery();
        Root<Employee> emp = q.from(Employee.class);
        q.multiselect(emp.type()).where(cb.notEqual(emp.type(), Exempt.class));

        assertEquivalence(q, jpql);
    }

    public void testIndexExpressionAndLietral() {
        String jpql = "SELECT w.name FROM Course c JOIN c.studentWaitList w "
                    + "WHERE c.name = 'Calculus' AND INDEX(w) = 0";
        
        CriteriaQuery<String> q = cb.createQuery(String.class);
        Root<Course> course = q.from(Course.class);
        ListJoin<Course, Student> w = course.join(Course_.studentWaitList);
        q.where(cb.and(
            cb.equal(course.get(Course_.name), "Calculus"),
            cb.equal(w.index(), 0))).select(w.get(Student_.name));

        assertEquivalence(q, jpql);
    }

    public void testAggregateInProjection() {
        String jpql = "SELECT SUM(i.price) FROM Order o JOIN o.lineItems i " + 
            "JOIN o.customer c WHERE c.lastName = 'Smith' AND " + 
            "c.firstName = 'John'";
        
        CriteriaQuery<Double> q = cb.createQuery(Double.class);
        Root<Order> o = q.from(Order.class);
        Join<Order, LineItem> i = o.join(Order_.lineItems);
        Join<Order, Customer> c = o.join(Order_.customer);
        q.where(cb.and(
            cb.equal(c.get(Customer_.lastName), "Smith"), 
            cb.equal(c.get(Customer_.firstName), "John")));
        q.select(cb.sum(i.get(LineItem_.price)));

        assertEquivalence(q, jpql);
    }

    public void testSizeExpression() {
        String jpql = "SELECT SIZE(d.employees) FROM Department d "
            + "WHERE d.name = 'Sales'";
        
        CriteriaQuery<Integer> q = cb.createQuery(Integer.class);
        Root<Department> d = q.from(Department.class);
        q.where(cb.equal(d.get(Department_.name), "Sales"));
        q.select(cb.size(d.get(Department_.employees)));

        assertEquivalence(q, jpql);
    }

    public void testCaseExpression() {
        String jpql = "SELECT e.name, CASE " 
            + "WHEN e.rating = 1 THEN e.salary * 1.1 "
                + "WHEN e.rating = 2 THEN e.salary * 1.2 "
                + "ELSE e.salary * 1.01 END "
            + "FROM Employee e WHERE e.department.name = 'Engineering'";
        CriteriaQuery<?> q = cb.createQuery();
        Root<Employee> e = q.from(Employee.class);
        q.where(cb.equal(e.get(Employee_.department).get(Department_.name), "Engineering"));
        q.multiselect(e.get(Employee_.name), 
                cb.selectCase()
                  .when(cb.equal(e.get(Employee_.rating), 1), cb.prod(e.get(Employee_.salary), 1.1))
                  .when(cb.equal(e.get(Employee_.rating), 2), cb.prod(e.get(Employee_.salary), 1.2))
                  .otherwise(cb.prod(e.get(Employee_.salary), 1.01)));

        assertEquivalence(q, jpql);
    }

    public void testExpression1() {
        String jpql = "SELECT o.quantity, o.totalCost*1.08, "
            + "a.zipCode FROM Customer c JOIN c.orders o JOIN c.address a "
            + "WHERE a.state = 'CA' AND a.county = 'Santa Clara'";
        
        CriteriaQuery<Tuple> q = cb.createTupleQuery();
        Root<Customer> cust = q.from(Customer.class);
        Join<Customer, Order> order = cust.join(Customer_.orders);
        Join<Customer, Address> address = cust.join(Customer_.address);
        q.where(cb.and(
            cb.equal(address.get(Address_.state), "CA"), 
            cb.equal(address.get(Address_.county), "Santa Clara")));
        q.multiselect(order.get(Order_.quantity), cb.prod(order
                .get(Order_.totalCost), 1.08), address.get(Address_.zipCode));

        assertEquivalence(q, jpql);
    }

    public void testExpression3() {
        String jpql = "SELECT w.name FROM Course c JOIN c.studentWaitList w "
            + "WHERE c.name = 'Calculus' AND INDEX(w) = 0";
        
        CriteriaQuery<String> q = cb.createQuery(String.class);
        Root<Course> course = q.from(Course.class);
        ListJoin<Course, Student> w = course.join(Course_.studentWaitList);
        q.where(cb.and(
            cb.equal(course.get(Course_.name), "Calculus"),
            cb.equal(w.index(), 0))).select(w.get(Student_.name));

        assertEquivalence(q, jpql);
    }

    public void testGeneralCaseExpression() {
        String jpql = "SELECT e.name, CASE "
            + "WHEN e.rating = 1 THEN e.salary * 1.1 "
            + "WHEN e.rating = 2 THEN e.salary * 1.2 ELSE e.salary * "
            + "1.01 END "
            + "FROM Employee e WHERE e.department.name = 'Engineering'";
        CriteriaQuery<?> q = cb.createQuery();
        Root<Employee> e = q.from(Employee.class);
        q.where(cb.equal(e.get(Employee_.department).get(Department_.name), "Engineering"));
        q.multiselect(e.get(Employee_.name), 
                cb.selectCase()
                  .when(cb.equal(e.get(Employee_.rating), 1), cb.prod(e.get(Employee_.salary), 1.1))
                  .when(cb.equal(e.get(Employee_.rating), 2), cb.prod(e.get(Employee_.salary), 1.2))
                  .otherwise(cb.prod(e.get(Employee_.salary), 1.01)));

        assertEquivalence(q, jpql);
    }

    public void testSimpleCaseExpression1() {
        String jpql = "SELECT e.name, CASE e.rating "
            + "WHEN 1 THEN e.salary * 1.1 "
            + "WHEN 2 THEN e.salary * 1.2 ELSE e.salary * 1.01 END "
            + "FROM Employee e WHERE e.department.name = 'Engineering'";
        CriteriaQuery<Tuple> q = cb.createTupleQuery();
        Root<Employee> e = q.from(Employee.class);
        q.where(cb.equal(e.get(Employee_.department).get(Department_.name), "Engineering"));
        Expression<Long> salary = e.get(Employee_.salary); 
        Expression<Integer> rating  = e.get(Employee_.rating);
        q.multiselect(e.get(Employee_.name),
                      cb.selectCase(rating).
                              when(1, cb.prod(salary, 1.1))
                             .when(2, cb.prod(salary, 1.2))
                             .otherwise(cb.prod(salary, 1.01)));

        assertEquivalence(q, jpql);
    }

    public void testSimpleCaseExpression2() {
        String jpql = "SELECT e.name, CASE e.rating WHEN 1 THEN 10 "
            + "WHEN 2 THEN 20 ELSE 30 END "
            + "FROM Employee e WHERE e.department.name = 'Engineering'";
        CriteriaQuery<Tuple> q = cb.createTupleQuery();
        Root<Employee> e = q.from(Employee.class);
        Expression<Integer> rating  = e.get(Employee_.rating);
        q.where(cb.equal(e.get(Employee_.department).get(Department_.name), "Engineering"));
        q.multiselect(e.get(Employee_.name), 
                cb.selectCase(rating)
                  .when(1, 10)
                  .when(2, 20)
                  .otherwise(30));
        assertEquivalence(q, jpql);
    }

    public void testLiterals() {
        String jpql = "SELECT p FROM Person p where 'Joe' MEMBER OF p.nickNames";
        CriteriaQuery<Person> q = cb.createQuery(Person.class);
        Root<Person> p = q.from(Person.class);
        q.select(p).where(cb.isMember(cb.literal("Joe"), p.get(Person_.nickNames)));

        assertEquivalence(q, jpql);
    }
    
    public void testParameters1() {
        String jpql = "SELECT c FROM Customer c Where c.status = :stat";
        
        CriteriaQuery<Customer> q = cb.createQuery(Customer.class);
        Root<Customer> c = q.from(Customer.class);
        Parameter<Integer> param = cb.parameter(Integer.class, "stat");
        q.select(c).where(cb.equal(c.get(Customer_.status), param));

        assertEquivalence(new QueryDecorator() {
            public void decorate(Query q) {
                q.setParameter("stat", 1);
            }
        }, q, jpql);
    }
    
    public void testParameters2() {
        String jpql = "SELECT c FROM Customer c Where c.status = :stat AND c.name = :name";
        
        CriteriaQuery<Customer> q = cb.createQuery(Customer.class);
        Root<Customer> c = q.from(Customer.class);
        Parameter<Integer> param1 = cb.parameter(Integer.class, "stat");
        Parameter<String> param2 = cb.parameter(String.class, "name");
        q.select(c).where(cb.and(cb.equal(c.get(Customer_.status), param1), 
                cb.equal(c.get(Customer_.name), param2)));

        assertEquivalence(new QueryDecorator() {
            public void decorate(Query q) {
                q.setParameter("stat", 1);
                q.setParameter("name", "test");
            }
        }, q, jpql);
    }
    
    public void testParameters3() {
        String jpql = "SELECT c FROM Customer c Where c.status = :stat";

        CriteriaQuery<Customer> q = cb.createQuery(Customer.class);
        Root<Customer> c = q.from(Customer.class);
        Parameter<Integer> param = cb.parameter(Integer.class, "stat");
        q.select(c).where(cb.equal(c.get(Customer_.status), param));
        
        assertEquivalence(new QueryDecorator() {
            public void decorate(Query q) {
                q.setParameter("stat", 1);
            }
        }, q, jpql);
    }
    
    public void testParameters4() {
        String jpql = "SELECT c FROM Customer c Where c.status = :stat AND c.name = :name";
        
        CriteriaQuery<Customer> q = cb.createQuery(Customer.class);
        Root<Customer> c = q.from(Customer.class);
        Parameter<Integer> param1 = cb.parameter(Integer.class, "stat");
        Parameter<String> param2 = cb.parameter(String.class, "name");
        q.select(c).where(cb.and(cb.equal(c.get(Customer_.status), param1), 
                cb.equal(c.get(Customer_.name), param2)));
        assertEquivalence(new QueryDecorator() {
            public void decorate(Query q) {
                q.setParameter("stat", 1);
                q.setParameter("name", "test");
            }
        }, q, jpql);
    }
    
    public void testParameters5() {
        String jpql = "SELECT c FROM Customer c Where c.status IN :coll";
        
        CriteriaQuery<Customer> q = cb.createQuery(Customer.class);
        Root<Customer> c = q.from(Customer.class);
        ParameterExpression<List> param1 = cb.parameter(List.class, "coll");
        q.where(c.get(Customer_.status).in(param1));
        q.select(c);
        final List vals = new ArrayList();
        vals.add(1);
        vals.add(2);
        assertEquivalence(new QueryDecorator() {
            public void decorate(Query q) {
                q.setParameter("coll", vals);
            }
        }, q, jpql);
    }
    
    public void testSelectList1() {
        String jpql = "SELECT v.location.street, KEY(i).title, VALUE(i) FROM "
            + "VideoStore v JOIN v.videoInventory i WHERE v.location.zipCode = "
            + "'94301' AND VALUE(i) > 0";
        
        CriteriaQuery<Tuple> q = cb.createTupleQuery();
        Root<VideoStore> v = q.from(VideoStore.class);
        MapJoin<VideoStore, Movie, Integer> i = v.join(VideoStore_.videoInventory);
        q.where(cb.and(
        cb.equal(v.get(VideoStore_.location).get(Address_.zipCode), "94301"), 
        cb.gt(i.value(), 0)));
        q.multiselect(v.get(VideoStore_.location).get(Address_.street), 
                i.key().get(Movie_.title), 
                i.value());

        assertEquivalence(q, jpql);
    }

    public void testNewConstruct() {
        String jpql = "SELECT NEW CustomerDetails(c.id, c.status) FROM Customer c";
        
        CriteriaQuery<CustomerDetails> q = cb.createQuery(CustomerDetails.class);

        Root<Customer> c = q.from(Customer.class);
        q.select(cb.construct(CustomerDetails.class, c.get(Customer_.id), c.get(Customer_.status)));
        assertEquivalence(q, jpql);
    }
    
    public void testConstructorInProjection() {
        String jpql = "SELECT NEW CustomerDetails(c.id, c.status, o.quantity) "
                    + "FROM Customer c JOIN c.orders o WHERE o.quantity > 100";
        
        CriteriaQuery<CustomerDetails> q = cb.createQuery(CustomerDetails.class);
        Root<Customer> c = q.from(Customer.class);
        SetJoin<Customer, Order> o = c.join(Customer_.orders);
        q.where(cb.gt(o.get(Order_.quantity), 100));
        q.select(cb.construct(CustomerDetails.class, 
                            c.get(Customer_.id), 
                            c.get(Customer_.status), 
                            o.get(Order_.quantity)));

        assertEquivalence(q, jpql);
    }

    public void testMultipleConstructorInProjection() {
        String jpql = "SELECT NEW CustomerDetails(c.id, c.status), " 
                    + "NEW CustomerFullName(c.firstName, c.lastName) "
                    + "FROM Customer c";
        
        CriteriaQuery<?> q = cb.createQuery();
        Root<Customer> c = q.from(Customer.class);
        q.multiselect(cb.construct(CustomerDetails.class, 
                             c.get(Customer_.id), 
                             c.get(Customer_.status)),
                cb.construct(CustomerFullName.class, 
                             c.get(Customer_.firstName), 
                             c.get(Customer_.lastName))
        );
        em.createQuery(q).getResultList();
        
        // assertEquivalence(q, jpql);
    }
    
    
    public void testSubqueries1() {
        String jpql = "SELECT goodCustomer FROM Customer goodCustomer WHERE "
            + "goodCustomer.balanceOwed < (SELECT AVG(c.balanceOwed) " 
            + " FROM "
            + "Customer c)";
        CriteriaQuery<Customer> q = cb.createQuery(Customer.class);
        Root<Customer> goodCustomer = q.from(Customer.class);
        Subquery<Double> sq = q.subquery(Double.class);
        Root<Customer> c = sq.from(Customer.class);
        q.where(cb.lt(goodCustomer.get(Customer_.balanceOwed), sq.select(cb
                .avg(c.get(Customer_.balanceOwed)))));
        q.select(goodCustomer);

        assertEquivalence(q, jpql);
    }

    public void testSubqueries2() {
        String jpql = "SELECT DISTINCT emp FROM Employee emp WHERE EXISTS ("
            + "SELECT spouseEmp FROM Employee spouseEmp WHERE spouseEmp ="
            + " emp.spouse)";
        CriteriaQuery<Employee> q = cb.createQuery(Employee.class);
        Root<Employee> emp = q.from(Employee.class);
        Subquery<Employee> sq = q.subquery(Employee.class);
        Root<Employee> spouseEmp = sq.from(Employee.class);
        sq.select(spouseEmp);
        sq.where(cb.equal(spouseEmp, emp.get(Employee_.spouse)));
        q.where(cb.exists(sq));
        q.select(emp).distinct(true);

        assertEquivalence(q, jpql);
    }

    public void testSubqueries3() {
        String jpql = "SELECT emp FROM Employee emp WHERE emp.salary > ALL ("
            + "SELECT m.salary FROM Manager m WHERE m.department = "
            + "emp.department)";
        CriteriaQuery<Employee> q = cb.createQuery(Employee.class);
        Root<Employee> emp = q.from(Employee.class);
        q.select(emp);
        Subquery<BigDecimal> sq = q.subquery(BigDecimal.class);
        Root<Manager> m = sq.from(Manager.class);
        sq.select(m.get(Manager_.salary));
        sq.where(cb.equal(m.get(Manager_.department), emp
                .get(Employee_.department)));
        q.where(cb.gt(emp.get(Employee_.salary), cb.all(sq)));

        assertEquivalence(q, jpql);
    }

    public void testSubqueries4() {
        String jpql = "SELECT c FROM Customer c WHERE "
            + "(SELECT COUNT(o) FROM c.orders o) > 10";
        CriteriaQuery<Customer> q = cb.createQuery(Customer.class);
        Root<Customer> c1 = q.from(Customer.class);
        q.select(c1);
        Subquery<Long> sq3 = q.subquery(Long.class);
        Root<Customer> c2 = sq3.correlate(c1);
        Join<Customer, Order> o = c2.join(Customer_.orders);
        q.where(cb.gt(sq3.select(cb.count(o)), 10));

        assertEquivalence(q, jpql);
    }

    public void testSubqueries5() {
        String jpql = "SELECT o FROM Order o WHERE 10000 < ALL ("
            + "SELECT a.balance FROM o.customer c JOIN c.accounts a)";
        CriteriaQuery<Order> q = cb.createQuery(Order.class);
        Root<Order> o = q.from(Order.class);
        q.select(o);
        Subquery<Integer> sq = q.subquery(Integer.class);
        Root<Order> osq = sq.correlate(o);
        Join<Order, Customer> c = osq.join(Order_.customer);
        Join<Customer, Account> a = c.join(Customer_.accounts);
        sq.select(a.get(Account_.balance));
        q.where(cb.lt(cb.literal(10000), cb.all(sq)));

        assertEquivalence(q, jpql);
    }

    public void testSubqueries6() {
        String jpql = "SELECT o FROM Order o JOIN o.customer c WHERE 10000 < "
            + "ALL (SELECT a.balance FROM c.accounts a)";
        CriteriaQuery<Order> q = cb.createQuery(Order.class);
        Root<Order> o = q.from(Order.class);
        q.select(o);
        Join<Order, Customer> c = o.join(Order_.customer);
        Subquery<Integer> sq = q.subquery(Integer.class);
        Join<Order, Customer> csq = sq.correlate(c);
        Join<Customer, Account> a = csq.join(Customer_.accounts);
        sq.select(a.get(Account_.balance));
        q.where(cb.lt(cb.literal(10000), cb.all(sq)));

        assertEquivalence(q, jpql);
    }

    public void testGroupByAndHaving() {
        String jpql = "SELECT c.status, AVG(c.filledOrderCount), COUNT(c) FROM "
            + "Customer c GROUP BY c.status HAVING c.status IN (1, 2)";
        CriteriaQuery<Tuple> q = cb.createTupleQuery();
        Root<Customer> c = q.from(Customer.class);
        q.groupBy(c.get(Customer_.status));
        q.having(cb.in(c.get(Customer_.status)).value(1).value(2));
        q.multiselect(c.get(Customer_.status), cb.avg(c
                .get(Customer_.filledOrderCount)), cb.count(c));

        assertEquivalence(q, jpql);
    }

    public void testOrdering1() {
        String jpql = "SELECT o FROM Customer c JOIN c.orders o "
            + "JOIN c.address a WHERE a.state = 'CA' ORDER BY o.quantity DESC, "
            + "o.totalCost";
        CriteriaQuery<Order> q = cb.createQuery(Order.class);
        Root<Customer> c = q.from(Customer.class);
        Join<Customer, Order> o = c.join(Customer_.orders);
        Join<Customer, Address> a = c.join(Customer_.address);
        q.where(cb.equal(a.get(Address_.state), "CA"));
        q.orderBy(cb.desc(o.get(Order_.quantity)), cb.asc(o
                .get(Order_.totalCost)));
        q.select(o);

        assertEquivalence(q, jpql);
    }

    public void testOrdering2() {
        String jpql = "SELECT o.quantity, a.zipCode FROM Customer c "
            + "JOIN c.orders o JOIN c.address a WHERE a.state = 'CA' "
            + "ORDER BY o.quantity, a.zipCode";
        CriteriaQuery<Tuple> q = cb.createTupleQuery();
        Root<Customer> c = q.from(Customer.class);
        Join<Customer, Order> o = c.join(Customer_.orders);
        Join<Customer, Address> a = c.join(Customer_.address);
        q.where(cb.equal(a.get(Address_.state), "CA"));
        q.orderBy(cb.asc(o.get(Order_.quantity)), cb.asc(a
                .get(Address_.zipCode)));
        q.multiselect(o.get(Order_.quantity), a.get(Address_.zipCode));

        assertEquivalence(q, jpql);
    }

    public void testOrdering3() {
        String jpql = "SELECT o.quantity, o.totalCost * 1.08 AS taxedCost, "
            + "a.zipCode FROM Customer c JOIN c.orders o JOIN c.address a "
            + "WHERE a.state = 'CA' AND a.county = 'Santa Clara' "
            + "ORDER BY o.quantity, taxedCost, a.zipCode";
        CriteriaQuery<Tuple> q = cb.createTupleQuery();
        Root<Customer> c = q.from(Customer.class);
        Join<Customer, Order> o = c.join(Customer_.orders);
        Join<Customer, Address> a = c.join(Customer_.address);
        Expression<Double> taxedCost = (Expression<Double>)cb.prod(o.get(Order_.totalCost), 1.08).alias("taxedCost");
        q.where(cb.equal(a.get(Address_.state), "CA"), 
                cb.equal(a.get(Address_.county), "Santa Clara"));
        q.orderBy(cb.asc(o.get(Order_.quantity)), 
                cb.asc(taxedCost),
                cb.asc(a.get(Address_.zipCode)));
        q.multiselect(o.get(Order_.quantity), taxedCost, a.get(Address_.zipCode));

        assertEquivalence(q, jpql);
    }
    
    public void testOrdering4() {
        String jpql = "SELECT c FROM Customer c ORDER BY c.name DESC, c.status";
        CriteriaQuery<Customer> q = cb.createQuery(Customer.class);
        Root<Customer> c = q.from(Customer.class);
        q.orderBy(cb.desc(c.get(Customer_.name)), 
                  cb.asc(c.get(Customer_.status)));
        q.select(c);

        assertEquivalence(q, jpql);
    }

    public void testOrdering5() {
        String jpql = "SELECT c.firstName, c.lastName, c.balanceOwed FROM Customer c ORDER BY c.name DESC, c.status";
        
        CriteriaQuery<Tuple> q = cb.createTupleQuery();
        Root<Customer> c = q.from(Customer.class);
        q.orderBy(cb.desc(c.get(Customer_.name)), cb.asc(c
                .get(Customer_.status)));
        q.multiselect(c.get(Customer_.firstName), c.get(Customer_.lastName), c
                .get(Customer_.balanceOwed));

        assertEquivalence(q, jpql);
    }
    
    /**
     * 0-arg function works only if there is other projection items to determine the table to select from. 
     */
    @AllowFailure(message="runs only on databases with CURRENT_USER() function e.g. MySQL but not Derby")
    public void testFunctionWithNoArgument() {
        String jpql = "SELECT c.balanceOwed FROM Customer c";
        String sql = "SELECT CURRENT_USER(), t0.balanceOwed FROM CR_CUST t0";
        
        CriteriaQuery<Tuple> q = cb.createTupleQuery();
        Root<Customer> c = q.from(Customer.class);
        q.multiselect(cb.function("CURRENT_USER", String.class, (Expression<?>[])null), c.get(Customer_.balanceOwed));
        
        executeAndCompareSQL(q, sql);
//        assertEquivalence(q, jpql);
    }

    public void testFunctionWithOneArgument() {
        String jpql = "SELECT MAX(c.balanceOwed) FROM Customer c";
        
        CriteriaQuery<Tuple> q = cb.createTupleQuery();
        Root<Customer> c = q.from(Customer.class);
        q.multiselect(cb.function("MAX", Integer.class, c.get(Customer_.balanceOwed)));
        
        assertEquivalence(q, jpql);

    }
    
    public void testFunctionWithTwoArgument() {
        String jpql = "SELECT MOD(c.balanceOwed,10) FROM Customer c";
        
        if (getDictionary().supportsModOperator) {
            // @AllowFailure
            // TODO - Skip executing this until OPENJPA-16xx is fixed, as CriteriaBuilder always
            // generates JPQL with MOD(,) instead of using "%" for Microsoft SQL Server
            getEntityManagerFactory().getConfiguration().getLog("test").warn(
                "SKIPPING testFunctionWithTwoArgument() for SQLServer");
            return;
        }
        
        CriteriaQuery<Tuple> q = cb.createTupleQuery();
        Root<Customer> c = q.from(Customer.class);
        q.multiselect(cb.function("MOD", Integer.class, c.get(Customer_.balanceOwed), cb.literal(10)));
        
        assertEquivalence(q, jpql);

    }
    
    public void testFunctionWithFunctionArgumentInOrderBy() {
        String jpql = "SELECT MOD(c.balanceOwed,10) FROM Customer c WHERE LENGTH(c.name)>3 ORDER BY LENGTH(c.name)";
        String sql = "SELECT MOD(t0.balanceOwed, ?), LENGTH(t0.name) FROM CR_CUST t0 WHERE (LENGTH(t0.name) > ?) " +
                     "ORDER BY LENGTH(t0.name) ASC";
        
        if (getDictionary().supportsModOperator) {
            // @AllowFailure
            // TODO - Skip executing this until OPENJPA-16xx is fixed, as CriteriaBuilder always
            // generates JPQL with MOD(,) instead of using "%" for Microsoft SQL Server
            getEntityManagerFactory().getConfiguration().getLog("test").warn(
            "SKIPPING testFunctionWithFunctionArgumentInOrderBy() for SQLServer");
            return;
        }

        CriteriaQuery<Tuple> q = cb.createTupleQuery();
        Root<Customer> c = q.from(Customer.class);
        Expression<Integer> nameLength = cb.function("LENGTH", Integer.class, c.get(Customer_.name));
        q.multiselect(cb.function("MOD", Integer.class, c.get(Customer_.balanceOwed), cb.literal(10)));
        q.where(cb.greaterThan(nameLength, 3));
        q.orderBy(cb.asc(nameLength));
        
        executeAndCompareSQL(q, sql);

    }

    public void testKeys1() {
        String sql = "SELECT t0.name, t2.id, t2.label FROM CR_ITEM t0 "
            + "INNER JOIN CR_ITEM_photos t1 ON t0.id = t1.ITEM_ID "
            + "INNER JOIN CR_PHT t2 ON t1.VALUE_ID = t2.id WHERE " + 
            "((t1.KEY0 = ? OR t1.KEY0 = ? OR t1.KEY0 = ? OR t1.KEY0 = ? OR t1.KEY0 = ?) "
            + "AND 0 < (SELECT COUNT(*) FROM CR_ITEM_photos WHERE CR_ITEM_photos.ITEM_ID = t0.id))";
        
        CriteriaQuery<Customer> q = cb.createQuery(Customer.class);
        Root<Item> item = q.from(Item.class);
        MapJoin<Item, String, Photo> photo = item.join(Item_.photos);
        q.multiselect(item.get(Item_.name), photo);
        Map<String, Photo> photo1 = new HashMap<String, Photo>();
        for (int i = 0; i < 5; i++) {
            Photo p1 = new Photo();
            p1.setLabel("label" + i);
            photo1.put("photo" + i, p1);
        }
        q.where(photo.key().in(cb.keys(photo1))); 
        executeAndCompareSQL(q, sql);
    }

    public void testKeys2() {
        String sql = "SELECT t0.name, t2.id, t2.label FROM CR_ITEM t0 "
            + "INNER JOIN CR_ITEM_photos t1 ON t0.id = t1.ITEM_ID "
            + "INNER JOIN CR_PHT t2 ON t1.VALUE_ID = t2.id WHERE " + 
            "(t1.KEY0 IN (?, ?, ?, ?, ?))";

        CriteriaQuery<Customer> q = cb.createQuery(Customer.class);
        Root<Item> item = q.from(Item.class);
        MapJoin<Item, String, Photo> photo = item.join(Item_.photos);
        q.multiselect(item.get(Item_.name), photo);
        Map<String, Photo> photo1 = new HashMap<String, Photo>();
        for (int i = 0; i < 5; i++) {
            Photo p1 = new Photo();
            p1.setLabel("label" + i);
            photo1.put("photo" + i, p1);
        }
        q.where(cb.isMember(photo.key(), cb.keys(photo1))); 
        executeAndCompareSQL(q, sql);
    }

    public void testKeys3() {
        String sql = "SELECT t0.name, t2.id, t2.label FROM CR_ITEM t0 "
            + "INNER JOIN CR_ITEM_photos t1 ON t0.id = t1.ITEM_ID "
            + "INNER JOIN CR_PHT t2 ON t1.VALUE_ID = t2.id WHERE (1 <> 1)";

        CriteriaQuery<Customer> q = cb.createQuery(Customer.class);
        Root<Item> item = q.from(Item.class);
        MapJoin<Item, String, Photo> photo = item.join(Item_.photos);
        q.multiselect(item.get(Item_.name), photo);
        Map<String, Photo> photo1 = new HashMap<String, Photo>();
        for (int i = 0; i < 5; i++) {
            Photo p1 = new Photo();
            p1.setLabel("label" + i);
            photo1.put("photo" + i, p1);
        }
        q.where(cb.isEmpty(cb.keys(photo1))); 
        executeAndCompareSQL(q, sql);
    }

    public void testKeys4() {
        String sql = "SELECT t0.name, t2.id, t2.label FROM CR_ITEM t0 "
            + "INNER JOIN CR_ITEM_photos t1 ON t0.id = t1.ITEM_ID "
            + "INNER JOIN CR_PHT t2 ON t1.VALUE_ID = t2.id WHERE (5 = 5)";

        CriteriaQuery<Customer> q = cb.createQuery(Customer.class);
        Root<Item> item = q.from(Item.class);
        MapJoin<Item, String, Photo> photo = item.join(Item_.photos);
        q.multiselect(item.get(Item_.name), photo);
        Map<String, Photo> photo1 = new HashMap<String, Photo>();
        for (int i = 0; i < 5; i++) {
            Photo p1 = new Photo();
            p1.setLabel("label" + i);
            photo1.put("photo" + i, p1);
        }
        q.where(cb.equal(cb.size(cb.keys(photo1)), 5)); 
        executeAndCompareSQL(q, sql);
    }

    public void testKeys5() {
        String sql = "SELECT t0.name, t2.id, t2.label FROM CR_ITEM t0 "
            + "INNER JOIN CR_ITEM_photos t1 ON t0.id = t1.ITEM_ID "
            + "INNER JOIN CR_PHT t2 ON t1.VALUE_ID = t2.id WHERE " + 
            "(NOT (t1.KEY0 = ? OR t1.KEY0 = ? OR t1.KEY0 = ? OR t1.KEY0 = ? OR t1.KEY0 = ?) "
            + "AND 0 < (SELECT COUNT(*) FROM CR_ITEM_photos WHERE CR_ITEM_photos.ITEM_ID = t0.id))";
        
        CriteriaQuery<Customer> q = cb.createQuery(Customer.class);
        Root<Item> item = q.from(Item.class);
        MapJoin<Item, String, Photo> photo = item.join(Item_.photos);
        q.multiselect(item.get(Item_.name), photo);
        Map<String, Photo> photo1 = new HashMap<String, Photo>();
        for (int i = 0; i < 5; i++) {
            Photo p1 = new Photo();
            p1.setLabel("label" + i);
            photo1.put("photo" + i, p1);
        }
        q.where(photo.key().in(cb.keys(photo1)).not()); 
        executeAndCompareSQL(q, sql);
    }

    public void testKeys6() {
        String sql = "SELECT t0.name, t2.id, t2.label FROM CR_ITEM t0 "
            + "INNER JOIN CR_ITEM_photos t1 ON t0.id = t1.ITEM_ID "
            + "INNER JOIN CR_PHT t2 ON t1.VALUE_ID = t2.id WHERE " + 
            "(NOT (t1.KEY0 IN (?, ?, ?, ?, ?)))";

        CriteriaQuery<Customer> q = cb.createQuery(Customer.class);
        Root<Item> item = q.from(Item.class);
        MapJoin<Item, String, Photo> photo = item.join(Item_.photos);
        q.multiselect(item.get(Item_.name), photo);
        Map<String, Photo> photo1 = new HashMap<String, Photo>();
        for (int i = 0; i < 5; i++) {
            Photo p1 = new Photo();
            p1.setLabel("label" + i);
            photo1.put("photo" + i, p1);
        }
        q.where(cb.isNotMember(photo.key(), cb.keys(photo1))); 
        executeAndCompareSQL(q, sql);
    }

    public void testKeys7() {
        String sql = "SELECT t0.name, t2.id, t2.label FROM CR_ITEM t0 "
            + "INNER JOIN CR_ITEM_photos t1 ON t0.id = t1.ITEM_ID "
            + "INNER JOIN CR_PHT t2 ON t1.VALUE_ID = t2.id WHERE (NOT (1 <> 1))";

        CriteriaQuery<Customer> q = cb.createQuery(Customer.class);
        Root<Item> item = q.from(Item.class);
        MapJoin<Item, String, Photo> photo = item.join(Item_.photos);
        q.multiselect(item.get(Item_.name), photo);
        Map<String, Photo> photo1 = new HashMap<String, Photo>();
        for (int i = 0; i < 5; i++) {
            Photo p1 = new Photo();
            p1.setLabel("label" + i);
            photo1.put("photo" + i, p1);
        }
        q.where(cb.isNotEmpty(cb.keys(photo1))); 
        executeAndCompareSQL(q, sql);
    }

    public void testKeys8() {
        String sql = "SELECT t0.name, t2.id, t2.label FROM CR_ITEM t0 "
            + "INNER JOIN CR_ITEM_photos t1 ON t0.id = t1.ITEM_ID "
            + "INNER JOIN CR_PHT t2 ON t1.VALUE_ID = t2.id WHERE (5 = 4)";
        CriteriaQuery<Customer> q = cb.createQuery(Customer.class);
        Root<Item> item = q.from(Item.class);
        MapJoin<Item, String, Photo> photo = item.join(Item_.photos);
        q.multiselect(item.get(Item_.name), photo);
        Map<String, Photo> photo1 = new HashMap<String, Photo>();
        for (int i = 0; i < 5; i++) {
            Photo p1 = new Photo();
            p1.setLabel("label" + i);
            photo1.put("photo" + i, p1);
        }
        q.where(cb.equal(cb.size(cb.keys(photo1)), 4)); 
        executeAndCompareSQL(q, sql);
    }

    public void testValues1() {
        String sql = "SELECT t0.name, t2.id, t2.label FROM CR_ITEM t0 "
            + "INNER JOIN CR_ITEM_photos t1 ON t0.id = t1.ITEM_ID "
            + "INNER JOIN CR_PHT t2 ON t1.VALUE_ID = t2.id WHERE " +
            "((t1.VALUE_ID = ? OR t1.VALUE_ID = ? OR t1.VALUE_ID = ? OR t1.VALUE_ID = ? OR t1.VALUE_ID = ?) "
            + "AND 0 < (SELECT COUNT(*) FROM CR_ITEM_photos WHERE CR_ITEM_photos.ITEM_ID = t0.id))";

        CriteriaQuery<Customer> q = cb.createQuery(Customer.class);
        Root<Item> item = q.from(Item.class);
        MapJoin<Item, String, Photo> photo = item.join(Item_.photos);
        q.multiselect(item.get(Item_.name), photo);
        Map<String, Photo> photo1 = new HashMap<String, Photo>();
        for (int i = 0; i < 5; i++) {
            Photo p1 = new Photo();
            p1.setLabel("label" + i);
            photo1.put("photo" + i, p1);
        }
        q.where(photo.value().in(cb.values(photo1))); 
        executeAndCompareSQL(q, sql);
    }

    public void testValues2() {
        String sql = "SELECT t0.name, t2.id, t2.label FROM CR_ITEM t0 "
            + "INNER JOIN CR_ITEM_photos t1 ON t0.id = t1.ITEM_ID "
            + "INNER JOIN CR_PHT t2 ON t1.VALUE_ID = t2.id WHERE (t1.VALUE_ID IN (?, ?, ?, ?, ?))";

        CriteriaQuery<Customer> q = cb.createQuery(Customer.class);
        Root<Item> item = q.from(Item.class);
        MapJoin<Item, String, Photo> photo = item.join(Item_.photos);
        q.multiselect(item.get(Item_.name), photo);
        Map<String, Photo> photo1 = new HashMap<String, Photo>();
        for (int i = 0; i < 5; i++) {
            Photo p1 = new Photo();
            p1.setLabel("label" + i);
            photo1.put("photo" + i, p1);
        }
        q.where(cb.isMember(photo.value(), cb.values(photo1))); 
        executeAndCompareSQL(q, sql);
    }

    public void testValues3() {
        String sql = "SELECT t0.name, t2.id, t2.label FROM CR_ITEM t0 "
            + "INNER JOIN CR_ITEM_photos t1 ON t0.id = t1.ITEM_ID "
            + "INNER JOIN CR_PHT t2 ON t1.VALUE_ID = t2.id WHERE (1 <> 1)";

        CriteriaQuery<Customer> q = cb.createQuery(Customer.class);
        Root<Item> item = q.from(Item.class);
        MapJoin<Item, String, Photo> photo = item.join(Item_.photos);
        q.multiselect(item.get(Item_.name), photo);
        Map<String, Photo> photo1 = new HashMap<String, Photo>();
        for (int i = 0; i < 5; i++) {
            Photo p1 = new Photo();
            p1.setLabel("label" + i);
            photo1.put("photo" + i, p1);
        }
        q.where(cb.isEmpty(cb.values(photo1))); 
        executeAndCompareSQL(q, sql);
    }

    public void testValue4() {
        String sql = "SELECT t0.name, t2.id, t2.label FROM CR_ITEM t0 "
            + "INNER JOIN CR_ITEM_photos t1 ON t0.id = t1.ITEM_ID "
            + "INNER JOIN CR_PHT t2 ON t1.VALUE_ID = t2.id WHERE (5 = 5)";

        CriteriaQuery<Customer> q = cb.createQuery(Customer.class);
        Root<Item> item = q.from(Item.class);
        MapJoin<Item, String, Photo> photo = item.join(Item_.photos);
        q.multiselect(item.get(Item_.name), photo);
        Map<String, Photo> photo1 = new HashMap<String, Photo>();
        for (int i = 0; i < 5; i++) {
            Photo p1 = new Photo();
            p1.setLabel("label" + i);
            photo1.put("photo" + i, p1);
        }
        q.where(cb.equal(cb.size(cb.values(photo1)), 5)); 
        executeAndCompareSQL(q, sql);
    }

    public void testValues5() {
        String sql = "SELECT t0.name, t2.id, t2.label FROM CR_ITEM t0 "
            + "INNER JOIN CR_ITEM_photos t1 ON t0.id = t1.ITEM_ID "
            + "INNER JOIN CR_PHT t2 ON t1.VALUE_ID = t2.id WHERE "
            + "(0 = (SELECT COUNT(*) FROM CR_ITEM_photos t3 WHERE "
            + "(t3.VALUE_ID = ? OR t3.VALUE_ID = ? OR t3.VALUE_ID = ? OR t3.VALUE_ID = ? OR t3.VALUE_ID = ?) "
            + "AND t0.id = t1.ITEM_ID) "
            + "AND 0 < (SELECT COUNT(*) FROM CR_ITEM_photos WHERE CR_ITEM_photos.ITEM_ID = t0.id))";
        
        CriteriaQuery<Customer> q = cb.createQuery(Customer.class);
        Root<Item> item = q.from(Item.class);
        MapJoin<Item, String, Photo> photo = item.join(Item_.photos);
        q.multiselect(item.get(Item_.name), photo);
        Map<String, Photo> photo1 = new HashMap<String, Photo>();
        for (int i = 0; i < 5; i++) {
            Photo p1 = new Photo();
            p1.setLabel("label" + i);
            photo1.put("photo" + i, p1);
        }
        q.where(photo.value().in(cb.values(photo1)).not()); 
        executeAndCompareSQL(q, sql);
    }

    public void testValues6() {
        String sql = "SELECT t0.name, t2.id, t2.label FROM CR_ITEM t0 "
            + "INNER JOIN CR_ITEM_photos t1 ON t0.id = t1.ITEM_ID "
            + "INNER JOIN CR_PHT t2 ON t1.VALUE_ID = t2.id WHERE (NOT (t1.VALUE_ID IN (?, ?, ?, ?, ?)))";

        CriteriaQuery<Customer> q = cb.createQuery(Customer.class);
        Root<Item> item = q.from(Item.class);
        MapJoin<Item, String, Photo> photo = item.join(Item_.photos);
        q.multiselect(item.get(Item_.name), photo);
        Map<String, Photo> photo1 = new HashMap<String, Photo>();
        for (int i = 0; i < 5; i++) {
            Photo p1 = new Photo();
            p1.setLabel("label" + i);
            photo1.put("photo" + i, p1);
        }
        q.where(cb.isNotMember(photo.value(), cb.values(photo1))); 
        executeAndCompareSQL(q, sql);
    }

    public void testValues7() {
        String sql = "SELECT t0.name, t2.id, t2.label FROM CR_ITEM t0 "
            + "INNER JOIN CR_ITEM_photos t1 ON t0.id = t1.ITEM_ID "
            + "INNER JOIN CR_PHT t2 ON t1.VALUE_ID = t2.id WHERE (NOT (1 <> 1))";

        CriteriaQuery<Customer> q = cb.createQuery(Customer.class);
        Root<Item> item = q.from(Item.class);
        MapJoin<Item, String, Photo> photo = item.join(Item_.photos);
        q.multiselect(item.get(Item_.name), photo);
        Map<String, Photo> photo1 = new HashMap<String, Photo>();
        for (int i = 0; i < 5; i++) {
            Photo p1 = new Photo();
            p1.setLabel("label" + i);
            photo1.put("photo" + i, p1);
        }
        q.where(cb.isNotEmpty(cb.values(photo1))); 
        executeAndCompareSQL(q, sql);
    }

    public void testValue8() {
        String sql = "SELECT t0.name, t2.id, t2.label FROM CR_ITEM t0 "
            + "INNER JOIN CR_ITEM_photos t1 ON t0.id = t1.ITEM_ID "
            + "INNER JOIN CR_PHT t2 ON t1.VALUE_ID = t2.id WHERE (5 = 4)";

        CriteriaQuery<Customer> q = cb.createQuery(Customer.class);
        Root<Item> item = q.from(Item.class);
        MapJoin<Item, String, Photo> photo = item.join(Item_.photos);
        q.multiselect(item.get(Item_.name), photo);
        Map<String, Photo> photo1 = new HashMap<String, Photo>();
        for (int i = 0; i < 5; i++) {
            Photo p1 = new Photo();
            p1.setLabel("label" + i);
            photo1.put("photo" + i, p1);
        }
        q.where(cb.equal(cb.size(cb.values(photo1)), 4)); 
        executeAndCompareSQL(q, sql);
    }
    
    /**
     * The syntax for joining the key of a Map attribute is different in JPQL.
     * Hence instead of comparing target SQL we compare the result.
     */
    public void testJoinKey() {
        em.getTransaction().begin();
        em.createQuery("DELETE FROM Student s").executeUpdate();
        em.createQuery("DELETE FROM Course s").executeUpdate();
        em.createQuery("DELETE FROM Semester s").executeUpdate();
        em.getTransaction().commit();
        
        em.getTransaction().begin();
        Student s1 = new Student(); s1.setName("S1");
        Student s2 = new Student(); s2.setName("S2");
        Student s3 = new Student(); s3.setName("S3");
        Student s4 = new Student(); s4.setName("S4");
        Semester sm1 = new Semester(); sm1.setName("Summer");
        Semester sm2 = new Semester(); sm2.setName("Fall");
        Course c1 = new Course(); c1.setName("C1");
        Course c2 = new Course(); c2.setName("C2");

        s1.addToEnrollment(c1, sm1); s1.addToEnrollment(c2, sm2);
        s2.addToEnrollment(c2, sm1); s2.addToEnrollment(c1, sm2);
                                     s3.addToEnrollment(c1, sm2); 
        s4.addToEnrollment(c2, sm1);
        
        em.persist(s1); em.persist(s2); em.persist(s3); em.persist(s4);
        em.persist(c1); em.persist(c2);
        em.persist(sm1); em.persist(sm2);
        em.getTransaction().commit();
        
        String jpql = "select s from Student s JOIN s.enrollment e where KEY(e).name=:name";
        List<Student> jResult = em.createQuery(jpql).setParameter("name", "C1").getResultList();
        
        CriteriaQuery<Student> q = cb.createQuery(Student.class);
        Root<Student> s = q.from(Student.class);
        Join<Map<Course,Semester>,Course> c = ((Joins.Map)s.join(Student_.enrollment)).joinKey();
        q.where(cb.equal(c.get(Course_.name), cb.parameter(String.class, "name")));
        
        List<Student> cResult = em.createQuery(q).setParameter("name","C1").getResultList();
        
        assertFalse(jResult.isEmpty());
        assertEquals(cResult.size(), jResult.size());
        for (int i = 0; i < jResult.size(); i++) {
            assertEquals(jResult.get(i).getName(), cResult.get(i).getName());
        }
    }
    
    public void testAliasInOrderByClause() {
        String jpql = "SELECT AVG(a.balance) AS x FROM Account a ORDER BY x";

        OpenJPACriteriaQuery<Double> c = cb.createQuery(Double.class);
        Root<Account> account = c.from(Account.class);
        Expression<Double> original = cb.avg(account.get(Account_.balance));
        Expression<Double> aliased = (Expression<Double>)original.alias("x");
        c.orderBy(cb.asc(aliased));
        assertSame(original, aliased);
        assertEquals("x", aliased.getAlias());
        c.select(aliased);
        assertEquivalence(c, jpql);
        assertEquals(jpql, c.toCQL());
    }
    
    public void testRealiasNotAllowed() {
        OpenJPACriteriaQuery<Double> c = cb.createQuery(Double.class);
        Root<Account> account = c.from(Account.class);
        Selection<Double> term = cb.avg(account.get(Account_.balance));
        term.alias("firsttime");
        try {
            term.alias("secondtime");
            fail("Expected to fail on re-aliasing");
        } catch (IllegalStateException e) {
            // good
        }
    }
    
    public void testInvalidAliasNotAllowed() {
        OpenJPACriteriaQuery<Double> c = cb.createQuery(Double.class);
        Root<Account> account = c.from(Account.class);
        Selection<Double> term = cb.avg(account.get(Account_.balance));
        try {
            term.alias("from");
            fail("Expected to fail on reserved word as alias");
        } catch (IllegalArgumentException e) {
            // good
            assertNull(term.getAlias());
        }
        try {
            term.alias(" with a space");
            fail("Expected to fail on invalid alias");
        } catch (IllegalArgumentException e) {
            // good
            assertNull(term.getAlias());
        }
        try {
            term.alias(" with?known_symbol");
            fail("Expected to fail on invalid alias");
        } catch (IllegalArgumentException e) {
            // good
            assertNull(term.getAlias());
        }
    }
    
    public void testInvalidParameterName() {
        try {
            cb.parameter(Integer.class, "from");
            fail("Expected to fail on reserved word as alias");
        } catch (IllegalArgumentException e) {
        }
        try {
            cb.parameter(Integer.class, ":name");
            fail("Expected to fail on invalid alias");
        } catch (IllegalArgumentException e) {
        }
        try {
            cb.parameter(Integer.class, "?3");
            fail("Expected to fail on invalid alias");
        } catch (IllegalArgumentException e) {
        }
    }
    
    public void testGroupByOnMaxResult() {
        String jpql = "SELECT c.address.country, count(c) from Customer c GROUP BY c.address.country " +
                      "HAVING COUNT(c.address.country)>3";
        
        CriteriaQuery<Object[]> c = cb.createQuery(Object[].class);
        Root<Customer> customer = c.from(Customer.class);
        Path<String> country = customer.get(Customer_.address).get(Address_.country);
        c.multiselect(country, cb.count(customer))
         .groupBy(country)
         .having(cb.gt(cb.count(country), 3));
        
        assertEquivalence(new QueryDecorator(){
            public void decorate(Query q) {
                q.setMaxResults(20);
            }
        }, c, jpql);
    }

    public void testEmptyAnd() {
        CriteriaQuery<Order> c = cb.createQuery(Order.class);
        Root<Order> order = c.from(Order.class);
        c.where(cb.and(cb.not(cb.equal(order.get(Order_.customer).get(Customer_.name), "Robert E. Bissett")),
                cb.isTrue(cb.conjunction())));
        em.createQuery(c).getResultList();
    }
    
    public void testEmptyOr() {
        CriteriaQuery<Order> c = cb.createQuery(Order.class);
        Root<Order> order = c.from(Order.class);
        c.where(cb.and(cb.not(cb.equal(order.get(Order_.customer).get(Customer_.name), "Robert E. Bissett")),
                cb.isTrue(cb.disjunction())));
        em.createQuery(c).getResultList();
    }
    
    public void testDefaultProjectionWithUntypedResult() {
        CriteriaQuery cquery = cb.createQuery(); 
        Root<Customer> customer = cquery.from(Customer.class);

        //Get Metamodel from Root
        EntityType<Customer> Customer_ = customer.getModel();

        cquery.where(cb.equal(
                customer.get(Customer_.getSingularAttribute("name", String.class)), 
                cb.nullLiteral(String.class)));

        Query q = em.createQuery(cquery);
    }
    
    public void testCountDistinct() {
        String jpql = "select COUNT(DISTINCT a.name) from Account a";
        
        CriteriaQuery<Long> c = cb.createQuery(Long.class);
        Root<Account> a = c.from(Account.class);
        c.select(cb.countDistinct(a.get(Account_.name)));
        
        assertEquivalence(c, jpql);
    }
    
    public void testCountDistinctOnJoin() {
        String jpql = "select COUNT(DISTINCT a.b.age) from A a";
        
        CriteriaQuery<Long> c = cb.createQuery(Long.class);
        Root<A> a = c.from(A.class);
        c.select(cb.countDistinct(a.get(A_.b).get(B_.age)));
        
        assertEquivalence(c, jpql);
    }

    
    public void testSizeReturnsInteger() {
        String jpql = "select SIZE(c.accounts) from Customer c";
        CriteriaQuery<Integer> c = cb.createQuery(Integer.class);
        Root<Customer> customer = c.from(Customer.class);
        c.select(cb.size(customer.get(Customer_.accounts)));
        
        assertEquivalence(c, jpql);
        
    }
    
    public void testDisjunctionAsFalse() {
        Metamodel mm = em.getMetamodel();

        CriteriaQuery<Order> cquery = cb.createQuery(Order.class);
        Root<Order> order = cquery.from(Order.class);
        
       EntityType<Order> Order_ = order.getModel();
       EntityType<Customer> Customer_ = mm.entity(Customer.class);
       cquery.where(cb.and(cb.equal(
         order.get(Order_.getSingularAttribute("customer", Customer.class))
                  .get(Customer_.getSingularAttribute("name", String.class)), "Robert E. Bissett"),
         cb.isFalse(cb.disjunction())));

       cquery.distinct(true);

       Query q = em.createQuery(cquery);

       List result = q.getResultList();        
    }

    public void testCurrentTimeReturnsSQLTypes() {
        if (getDictionary() instanceof OracleDictionary) {
            // Oracle does not have CURRENT_TIME function, nor does it support DB generated identity
            return;
        }
        em.getTransaction().begin();
        Product pc = new Product();
        em.persist(pc);
        em.getTransaction().commit();
        
        int pid = pc.getPid();
        
        CriteriaQuery<Time> cquery = cb.createQuery(Time.class);
        Root<Product> product = cquery.from(Product.class);
        cquery.select(cb.currentTime());
        cquery.where(cb.equal(product.get(Product_.pid), pid));

        TypedQuery<Time> tq = em.createQuery(cquery);
        Object result = tq.getSingleResult();
        assertTrue(result.getClass() + " not instance of Time", result instanceof Time);  
        
    }    

    public void testCurrentDateReturnsSQLTypes() {
        em.getTransaction().begin();
        Order pc = new Order();
        em.persist(pc);
        em.getTransaction().commit();
        
        int oid = pc.getId();
        
        CriteriaQuery<Date> cquery = cb.createQuery(Date.class);
        Root<Order> order = cquery.from(Order.class);
        cquery.select(cb.currentDate());
        cquery.where(cb.equal(order.get(Order_.id), oid));

        TypedQuery<Date> tq = em.createQuery(cquery);
        Object result = tq.getSingleResult();
        assertTrue(result.getClass() + " not instance of Date", result instanceof Date);  

    }

    public void testCurrentTimestampReturnsSQLTypes() {
        em.getTransaction().begin();
        Order pc = new Order();
        em.persist(pc);
        em.getTransaction().commit();

        int oid = pc.getId();

        CriteriaQuery<Timestamp> cquery = cb.createQuery(Timestamp.class);
        Root<Order> order = cquery.from(Order.class);
        cquery.select(cb.currentTimestamp());
        cquery.where(cb.equal(order.get(Order_.id), oid));

        TypedQuery<Timestamp> tq = em.createQuery(cquery);
        Object result = tq.getSingleResult();
        assertTrue(result.getClass() + " not instance of Timestamp", result instanceof Timestamp);  
        
    }
    
//    public void testInMemoryAccessPath() {
//        em.getTransaction().begin();
//        // must have new/dirty managed instances to exercise the code path
//        em.persist(new Customer());
//        CriteriaQuery<Customer> cquery = cb.createQuery(Customer.class);
//        Root<Customer> customer = cquery.from(Customer.class);
//        Fetch<Customer, Account> c = customer.fetch("accounts", JoinType.LEFT);
//        cquery.where(cb.like(customer.<String>get("firstName"), "a%")).select(customer).distinct(true);
//        TypedQuery<Customer> tquery = em.createQuery(cquery);
//        tquery.setMaxResults(3);
//        List<Customer> result = tquery.getResultList();
//
//    }
    
    public void testLiteralInProjection() {
        String jpql = "select 'a' from Customer c where c.id=10";
        
        CriteriaQuery<String> cq = cb.createQuery(String.class);
        Root<Customer> c = cq.from(Customer.class);
        cq.select(cb.toString(cb.literal('a')));
        cq.where(cb.equal(c.get(Customer_.id), 10));
        assertEquivalence(cq, jpql);
    }
    
    public void testBigDecimalConversion() {
        String jpql = "select c.accountNum*10.32597 from Customer c where c.id=10";        
        long accountNumber = 1234516279;
        
        if (getDictionary() instanceof AbstractSQLServerDictionary) {
            // @AllowFailure
            // TODO - Skipping for MSSQL & Sybase, as the calculation result has a precision larger than 38
            // params=(BigDecimal) 10.3259699999999998709654391859658062458038330078125
            getEntityManagerFactory().getConfiguration().getLog("test").warn(
                "SKIPPING testBigDecimalConversion() for SQLServer & Sybase");
            return;
        }
        
        em.getTransaction().begin();
        Customer customer = new Customer();
        customer.setAccountNum(accountNumber);
        em.persist(customer);
        em.getTransaction().commit();
        
        long cid = customer.getId();
        
        CriteriaQuery<BigDecimal> cq = cb.createQuery(BigDecimal.class);
        Root<Customer> c = cq.from(Customer.class);
        cq.select(cb.toBigDecimal(cb.prod(c.get(Customer_.accountNum), new BigDecimal(10.32597))));
        cq.where(cb.equal(c.get(Customer_.id), cid));
        //assertEquivalence(cq, jpql);
        
        List<BigDecimal> result = em.createQuery(cq).getResultList();
        assertFalse(result.isEmpty());
        assertTrue(result.get(0) instanceof BigDecimal);
    }
    
    public void testIdClass() {
        String jpql = "select p from EntityWithIdClass p";
        
    	CriteriaQuery<EntityWithIdClass> cq = cb.createQuery(EntityWithIdClass.class);
    	Root<EntityWithIdClass> c = cq.from(EntityWithIdClass.class);
    	em.createQuery(cq).getResultList();
    	
        assertEquivalence(cq, jpql);
    }
}
