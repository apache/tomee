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

import static javax.persistence.metamodel.Type.PersistenceType.EMBEDDABLE;
import static javax.persistence.metamodel.Type.PersistenceType.ENTITY;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.Parameter;
import javax.persistence.Query;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.MapJoin;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;
import javax.persistence.criteria.Subquery;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.SetAttribute;

import org.apache.openjpa.persistence.test.AllowFailure;

public class TestMetaModelTypesafeCriteria extends CriteriaTest {
    protected EntityType<Account> account_ = null;
    protected EmbeddableType<Address> address_ = null;
    protected EmbeddableType<Contact> contact_ = null;
    protected EntityType<Course> course_ = null;
    protected EntityType<CreditCard> creditCard_ = null;
    protected EntityType<Customer> customer_ = null;
    protected EntityType<Department> department_ = null;
    protected EntityType<Employee> employee_ = null;
    protected EntityType<Exempt> exempt_ = null;
    protected EntityType<Item> item_ = null;
    protected EntityType<LineItem> lineItem_ = null;
    protected EntityType<Manager> manager_ = null;
    protected EntityType<Movie> movie_ = null;
    protected EntityType<Order> order_ = null;
    protected EntityType<Person> person_ = null;
    protected EntityType<Phone> phone_ = null;
    protected EntityType<Photo> photo_ = null;
    protected EntityType<Product> product_ = null;
    protected EntityType<Semester> semester_ = null;
    protected EntityType<Student> student_ = null;
    protected EntityType<TransactionHistory> transactionHistory_ = null;
    protected EntityType<VideoStore> videoStore_ = null;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        Metamodel mm = em.getMetamodel();
        account_ = mm.entity(Account.class);
        address_ = mm.embeddable(Address.class);
        assertNotNull(address_);
        contact_ = mm.embeddable(Contact.class);
        course_ = mm.entity(Course.class);
        creditCard_ = mm.entity(CreditCard.class);
        customer_ = mm.entity(Customer.class);
        department_ = mm.entity(Department.class);
        employee_ = mm.entity(Employee.class);
        exempt_ = mm.entity(Exempt.class);
        item_ = mm.entity(Item.class);
        lineItem_ = mm.entity(LineItem.class);
        manager_ = mm.entity(Manager.class);
        movie_ = mm.entity(Movie.class);
        order_ = mm.entity(Order.class);
        person_ = mm.entity(Person.class);
        phone_ = mm.entity(Phone.class);
        photo_ = mm.entity(Photo.class);
        product_ = mm.entity(Product.class);
        semester_ = mm.entity(Semester.class);
        student_ = mm.entity(Student.class);
        transactionHistory_ = mm.entity(TransactionHistory.class);
        videoStore_ = mm.entity(VideoStore.class);
    }

    public void testEntityEmbeddableTest() {
        Metamodel mm = em.getMetamodel();

        assertEquals(mm.managedType(Account.class).getPersistenceType(), ENTITY);
        assertEquals(mm.managedType(Address.class).getPersistenceType(), EMBEDDABLE);

        assertNotNull(mm.entity(Account.class));
        assertNotNull(mm.embeddable(Address.class));

        try {
            mm.entity(Address.class);
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException iaex) {
        }
        try {
            mm.embeddable(Account.class);
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException iaex) {
        }

        int numEntity = 0;
        int numEmbeddables = 0;
        for (Class<?> clz : getDomainClasses()) {
            if (clz.getAnnotation(Embeddable.class) != null) {
                ++numEmbeddables;
            } else if (clz.getAnnotation(Entity.class) != null) {
                ++numEntity;
            }
        }
        Set<EmbeddableType<?>> embs = mm.getEmbeddables();
        assertEquals(embs.size(), numEmbeddables);
        Set<EntityType<?>> ents = mm.getEntities();
        assertEquals(ents.size(), numEntity);
        Set<ManagedType<?>> metaTypes = mm.getManagedTypes();
        assertEquals(metaTypes.size(), numEntity + numEmbeddables);
    }

    public void testStringEqualExpression() {
        String jpql = "select c from Customer c " 
                    + "where c.name='Autowest Toyota'";
        
        CriteriaQuery q = cb.createQuery();
        Root<Customer> customer = q.from(Customer.class);
        q.select(customer)
         .where(cb.equal(
                customer.get(customer_.getSingularAttribute("name", String.class)), 
                "Autowest Toyota"));

        assertEquivalence(q, jpql);
    }

    public void testSetAndListJoins() {
        String jpql = "SELECT c.name FROM Customer c " 
                    + "JOIN c.orders o JOIN o.lineItems i " 
                    + "WHERE i.product.productType = 'printer'";
        
        CriteriaQuery q = cb.createQuery();
        Root<Customer> c = q.from(Customer.class);
        SetJoin<Customer, Order> o = c.join(customer_.getSet("orders",
                Order.class));
        ListJoin<Order, LineItem> i = o.join(order_.getList("lineItems",
                LineItem.class));
        q.select(c.get(Customer_.name)).where(
                cb.equal(i.get(lineItem_.getSingularAttribute("product", Product.class))
                    .get(product_.getSingularAttribute("productType", String.class)),
                    "printer"));

        assertEquivalence(q, jpql);
    }
    
    public void testLeftSetJoin() {
        String jpql = "SELECT c FROM Customer c "
                    + "LEFT JOIN c.orders o "
                    + "WHERE c.status = 1";
        
        CriteriaQuery<Customer> q = cb.createQuery(Customer.class);
        Root<Customer> c = q.from(Customer.class);
        SetJoin<Customer, Order> o = c.join(customer_.getSet("orders",
                Order.class), JoinType.LEFT);
        q.where(cb.equal(
                c.get(customer_.getSingularAttribute("status", Integer.class)), 
                1));

        assertEquivalence(q, jpql);
    }

    public void testFetchJoins() {
        String jpql = "SELECT d FROM Department d LEFT JOIN FETCH d.employees "
                + "WHERE d.deptNo = 1";
        CriteriaQuery q = cb.createQuery();
        Root<Department> d = q.from(Department.class);
        d.fetch(department_.getSet("employees", Employee.class), JoinType.LEFT);
        q.where(
                cb.equal(d.get(department_
                        .getSingularAttribute("deptNo", Integer.class)), 1)).select(d);

        assertEquivalence(q, jpql);
    }

    @AllowFailure(message="This is regression.")
    public void testPathNavigation() {
        String jpql = "SELECT p.vendor FROM Employee e "
                    + "JOIN e.contactInfo.phones p  "
                    + "WHERE e.contactInfo.address.zipCode = '95054'";
        
        
        CriteriaQuery<String> q = cb.createQuery(String.class);
        Root<Employee> e = q.from(Employee.class);
        Join<Contact, Phone> p = e.join(employee_.getSingularAttribute("contactInfo", Contact.class))
                                  .join(contact_.getList("phones", Phone.class));
        q.where(cb.equal(e.get(employee_.getSingularAttribute("contactInfo", Contact.class))
                          .get(contact_.getSingularAttribute("address", Address.class))
                          .get(address_.getSingularAttribute("zipCode", String.class)), 
                         "95054"));
        q.select(p.get(phone_.getSingularAttribute("vendor", String.class)));

        assertEquivalence(q, jpql);
    }
    
    public void testKeyPathNavigation() {
        String jpql = "SELECT i.name, p FROM Item i JOIN i.photos p WHERE KEY(p) LIKE '%egret%'";

        CriteriaQuery<Tuple> q = cb.createTupleQuery();
        Root<Item> i = q.from(Item.class);
        MapJoin<Item, String, Photo> p = i.join(item_.getMap("photos", String.class, Photo.class));
        q.multiselect(i.get(item_.getSingularAttribute("name", String.class)), p)
                .where(cb.like(p.key(), "%egret%"));

        assertEquivalence(q, jpql);
    }

    public void testIndexExpression() {
        String jpql = "SELECT t FROM CreditCard c JOIN c.transactionHistory t "
                + "WHERE c.customer.accountNum = 321987 AND INDEX(t) BETWEEN 0 "
                + "AND 9";
        
        CriteriaQuery<TransactionHistory> cq = cb.createQuery(TransactionHistory.class);
        Root<CreditCard> c = cq.from(CreditCard.class);
        ListJoin<CreditCard, TransactionHistory> t = c.join(creditCard_.getList("transactionHistory", 
                TransactionHistory.class));
        Predicate p1 = cb.equal(
                c.get(creditCard_.getSingularAttribute("customer", Customer.class))
                 .get(customer_.getSingularAttribute("accountNum", long.class)), 321987);
        Predicate p2 = cb.between(t.index(), 0, 9);
        cq.select(t).where(p1,p2);

        assertEquivalence(cq, jpql);
    }
    
    public void testIsEmptyExpressionOnJoin() {
        String jpql = "SELECT o FROM Order o WHERE o.lineItems IS EMPTY"; 
        CriteriaQuery<Order> q = cb.createQuery(Order.class); 
        Root<Order> o = q.from(Order.class);
        Expression<LineItem> lineItems = o.get("lineItems").as(LineItem.class);
        q.where(cb.isEmpty(lineItems.as(List.class))); 
        q.select(o);
        assertEquivalence(q, jpql);
    }

    public void testFunctionalExpressionInProjection() {
        String jpql = "SELECT o.quantity, o.totalCost*1.08 AS taxedCost, "
                + "a.zipCode FROM Customer c JOIN c.orders o JOIN c.address a "
                + "WHERE a.state = 'CA' AND a.county = 'Santa Clara'";
        
        CriteriaQuery<Tuple> q = cb.createTupleQuery();
        Root<Customer> c = q.from(Customer.class);
        Join<Customer, Order> o = c.join(customer_.getSet("orders", Order.class));
        Join<Customer, Address> a = c.join(customer_.getSingularAttribute("address", Address.class));
        Expression<Double> taxedCost = (Expression<Double>)cb.prod(o.get(order_.getSingularAttribute("totalCost", 
                Double.class)), 1.08).alias("taxedCost");
        q.where(cb.equal(a.get(address_.getSingularAttribute("state", String.class)), "CA"), 
                cb.equal(a.get(address_.getSingularAttribute("county", String.class)), "Santa Clara"));
        q.multiselect(o.get(order_.getSingularAttribute("quantity", Integer.class)),
                taxedCost,
                a.get(address_.getSingularAttribute("zipCode", String.class)));

        assertEquivalence(q, jpql);
    }
    
    public void testTypeExpression() {
        String jpql = "SELECT TYPE(e) FROM Employee e WHERE TYPE(e) <> Exempt";
        CriteriaQuery q = cb.createQuery();
        Root<Employee> emp = q.from(Employee.class);
        q.select(emp.type()).where(cb.notEqual(emp.type(), Exempt.class));

        assertEquivalence(q, jpql);
    }
    
    public void testJoinAndIndexExpression() {
       String jpql = "SELECT w.name FROM Course c JOIN c.studentWaitList w "
                + "WHERE c.name = 'Calculus' AND INDEX(w) = 0";
        
       CriteriaQuery<String> q = cb.createQuery(String.class);
        Root<Course> course = q.from(Course.class);
        ListJoin<Course, Student> w = course.join(course_.getList("studentWaitList", Student.class));
        q.where(cb.equal(course.get(course_.getSingularAttribute("name", String.class)), "Calculus"), 
                cb.equal(w.index(), 0))
          .select(w.get(student_.getSingularAttribute("name", String.class)));

        assertEquivalence(q, jpql);
    }
    
    public void testAggregateExpressionInProjection() {
        String jpql = "SELECT SUM(i.price) " 
             + "FROM Order o JOIN o.lineItems i JOIN o.customer c "
             + "WHERE c.lastName = 'Smith' AND c.firstName = 'John'";
        CriteriaQuery q = cb.createQuery();
        Root<Order> o = q.from(Order.class);
        Join<Order, LineItem> i = o.join(order_.getList("lineItems",
                LineItem.class));
        Join<Order, Customer> c = o.join(order_.getSingularAttribute("customer",
                Customer.class));
        q.where(cb.equal(c.get(customer_.getSingularAttribute("lastName", String.class)), "Smith"), 
                cb.equal(c.get(customer_.getSingularAttribute("firstName", String.class)), "John"));
        q.select(cb.sum(i.get(lineItem_.getSingularAttribute("price", Double.class))));

        assertEquivalence(q, jpql);
    }
    
    public void testSizeExpressionInProjection() {
        String jpql = "SELECT SIZE(d.employees) FROM Department d " 
         + "WHERE d.name = 'Sales'"; 
        
        CriteriaQuery q = cb.createQuery(); 
        Root<Department> d = q.from(Department.class);
        q.where(cb.equal(
                d.get(department_.getSingularAttribute("name", String.class)), 
                "Sales"));
        SetAttribute<Department, Employee> employees = 
            department_.getDeclaredSet("employees", Employee.class);
        q.select(cb.size(d.get(employees)));
        
        assertEquivalence(q, jpql);
        
    }
    
    public void testCaseExpression() {
        String jpql = "SELECT e.name, "
             + "CASE WHEN e.rating = 1 THEN e.salary * 1.1 "
             + "WHEN e.rating = 2 THEN e.salary * 1.2 ELSE e.salary * 1.01 END "
             + "FROM Employee e WHERE e.department.name = 'Engineering'";
        
        CriteriaQuery<?> q = cb.createQuery();
        Root<Employee> e = q.from(Employee.class);
        q.where(cb.equal(e.get(
                        employee_.getSingularAttribute("department", Department.class))
                        .get(department_.getSingularAttribute("name", String.class)),
                        "Engineering"));
        q.multiselect(e.get(employee_.getSingularAttribute("name", String.class)), 
                cb.selectCase().when(
                        cb.equal(e.get(employee_.getSingularAttribute("rating", Integer.class)), 1),
                        cb.prod(e.get(employee_.getSingularAttribute("salary",  Long.class)), 1.1)).when(
                        cb.equal(e.get(employee_.getSingularAttribute("rating", Integer.class)), 2),
                        cb.prod(e.get(employee_.getSingularAttribute("salary",  Long.class)), 1.2)).otherwise(
                        cb.prod(e.get(employee_.getSingularAttribute("salary",  Long.class)), 1.01)));

        assertEquivalence(q, jpql);
    }

    public void testMemberOfExpression() {
      String jpql = "SELECT p FROM Person p where 'Joe' MEMBER OF p.nickNames";
     
      CriteriaQuery<?> q = cb.createQuery(); 
      Root<Person> p = q.from(Person.class);
      q.multiselect(p).where(cb.isMember(cb.literal("Joe"), 
             p.get(person_.getDeclaredSet("nickNames", String.class))));
     
       assertEquivalence(q, jpql); 
     }

    public void testParameters() {
        String jpql = "SELECT c FROM Customer c Where c.status = :stat";
        CriteriaQuery<?> q = cb.createQuery();
        Root<Customer> c = q.from(Customer.class);
        Parameter<Integer> param = cb.parameter(Integer.class, "stat");
        q.multiselect(c).where(cb.equal(
            c.get(customer_.getSingularAttribute("status", Integer.class)), param));

        assertEquivalence(new QueryDecorator() {
            public void decorate(Query q) {
                q.setParameter("stat", 1);
            }
        }, q, jpql);
    }

    public void testKeyExpressionInSelectList() {
        String jpql = "SELECT v.location.street, KEY(i).title, VALUE(i) "
                + "FROM VideoStore v JOIN v.videoInventory i "
                + "WHERE v.location.zipCode = '94301' AND VALUE(i) > 0";
        
        CriteriaQuery<?> q = cb.createQuery();
        Root<VideoStore> v = q.from(VideoStore.class);
        MapJoin<VideoStore, Movie, Integer> i = v.join(videoStore_.getMap("videoInventory", Movie.class,Integer.class));
        q.where(cb.equal(v.get(videoStore_.getSingularAttribute("location", Address.class))
                          .get(address_.getSingularAttribute("zipCode", String.class)), "94301"), 
                          cb.gt(i.value(), 0));
        q.multiselect(v.get(videoStore_.getSingularAttribute("location", Address.class))
                .get(address_.getSingularAttribute("street", String.class)), 
                i.key().get(movie_.getSingularAttribute("title", String.class)), 
                i.value());

        assertEquivalence(q, jpql);
    }
    
    public void testConstructorInSelectList() {
        String jpql = "SELECT NEW CustomerDetails(c.id, c.status, o.quantity) "
                    + "FROM Customer c JOIN c.orders o WHERE o.quantity > 100";
        
        CriteriaQuery<CustomerDetails> q = cb.createQuery(CustomerDetails.class);
        Root<Customer> c = q.from(Customer.class);
        SetJoin<Customer, Order> o = c.join(
                customer_.getSet("orders", Order.class));
        q.where(cb.gt(o.get(order_.getSingularAttribute("quantity", Integer.class)),
                100));
        q.select(cb.construct(CustomerDetails.class, 
                c.get(customer_.getSingularAttribute("id", Long.class)), 
                c.get(customer_.getSingularAttribute("status", Integer.class)), 
                o.get(order_.getSingularAttribute("quantity",  Integer.class))));

        assertEquivalence(q, jpql);
    }

    public void testUncorrelatedSubqueryWithAggregateProjection() {
        String jpql = "SELECT goodCustomer FROM Customer goodCustomer WHERE "
                + "goodCustomer.balanceOwed < (SELECT AVG(c.balanceOwed) FROM "
                + "Customer c)";
        CriteriaQuery<?> q = cb.createQuery();
        Root<Customer> goodCustomer = q.from(Customer.class);
        Subquery<Double> sq = q.subquery(Double.class);
        Root<Customer> c = sq.from(Customer.class);
        q.where(cb.lt(goodCustomer.get(customer_.getSingularAttribute("balanceOwed",
                Integer.class)), sq.select(cb.avg(c.get(customer_.getSingularAttribute(
                "balanceOwed", Integer.class))))));
        q.multiselect(goodCustomer);

        assertEquivalence(q, jpql);
    }
    
    public void testSubqueryWithExistsClause() {
        String jpql = "SELECT DISTINCT emp FROM Employee emp WHERE EXISTS ("
                + "SELECT spouseEmp FROM Employee spouseEmp WHERE spouseEmp = "
                + "emp.spouse)";
        CriteriaQuery<Employee> q = cb.createQuery(Employee.class);
        Root<Employee> emp = q.from(Employee.class);
        Subquery<Employee> sq = q.subquery(Employee.class);
        Root<Employee> spouseEmp = sq.from(Employee.class);
        sq.select(spouseEmp);
        sq.where(cb.equal(spouseEmp, emp.get(employee_.getSingularAttribute("spouse", Employee.class))));
        q.where(cb.exists(sq));
        q.distinct(true);

        assertEquivalence(q, jpql);
    }

    public void testSubqueryWithAllClause() {
        String jpql = "SELECT emp FROM Employee emp WHERE emp.salary > ALL ("
                + "SELECT m.salary FROM Manager m WHERE m.department ="
                + " emp.department)";
        
        CriteriaQuery<Employee> q = cb.createQuery(Employee.class);
        Root<Employee> emp = q.from(Employee.class);
        
        Subquery<BigDecimal> sq = q.subquery(BigDecimal.class);
        Root<Manager> m = sq.from(Manager.class);
        sq.select(m.get(manager_.getSingularAttribute("salary", BigDecimal.class)));
        sq.where(cb.equal(m.get(manager_.getSingularAttribute("department",
                Department.class)), emp.get(employee_.getSingularAttribute(
                "department", Department.class))));
        q.where(cb.gt(emp.get(employee_.getSingularAttribute("salary", Long.class)), cb
                .all(sq)));

        assertEquivalence(q, jpql);
    }
    
    public void testCorrelatedSubqueryWithCount() {
        String jpql = "SELECT c FROM Customer c WHERE "
                + "(SELECT COUNT(o) FROM c.orders o) > 10";
        CriteriaQuery<Customer> q = cb.createQuery(Customer.class);
        Root<Customer> c1 = q.from(Customer.class);
        Subquery<Long> sq3 = q.subquery(Long.class);
        Root<Customer> c2 = sq3.correlate(c1);
        Join<Customer, Order> o = c2.join(customer_.getSet("orders",
                Order.class));
        q.where(cb.gt(sq3.select(cb.count(o)), 10));

        assertEquivalence(q, jpql);
    }
    
    public void testCorrelatedSubqueryWithJoin() {
        String jpql = "SELECT o FROM Order o WHERE 10000 < ALL ("
                + "SELECT a.balance FROM o.customer c JOIN c.accounts a)";
        
        CriteriaQuery<Order> q = cb.createQuery(Order.class);
        Root<Order> o = q.from(Order.class);
        
        Subquery<Integer> sq = q.subquery(Integer.class);
        Root<Order> o2 = sq.correlate(o);
        Join<Order, Customer> c = o2.join(order_.getSingularAttribute("customer",
                Customer.class));
        Join<Customer, Account> a = c.join(customer_.getList("accounts",
                Account.class));
        sq.select(a.get(account_.getSingularAttribute("balance", Integer.class)));
        q.where(cb.lt(cb.literal(10000), cb.all(sq)));

        assertEquivalence(q, jpql);
    }
    
    public void testCorrelatedSubqueryWithAllClause() {
        String jpql = "SELECT o FROM Order o JOIN o.customer c "
                    + "WHERE 10000 < ALL (SELECT a.balance FROM c.accounts a)";
        
        CriteriaQuery<Order> q = cb.createQuery(Order.class);
        Root<Order> o = q.from(Order.class);
        
        Join<Order, Customer> c = o.join(Order_.customer);
        Subquery<Integer> sq = q.subquery(Integer.class);
        Join<Order, Customer> csq = sq.correlate(c);
        Join<Customer, Account> a = csq.join(customer_.getList("accounts",
                Account.class));
        sq.select(a.get(account_.getSingularAttribute("balance", Integer.class)));
        q.where(cb.lt(cb.literal(10000), cb.all(sq)));

        assertEquivalence(q, jpql);
    }

    public void testGroupByAndHaving() {
        String jpql = "SELECT c.status, AVG(c.filledOrderCount), COUNT(c) FROM "
                + "Customer c GROUP BY c.status HAVING c.status IN (1, 2)";
        
        CriteriaQuery<?> q = cb.createQuery();
        Root<Customer> c = q.from(Customer.class);
        q.groupBy(c.get(customer_.getSingularAttribute("status", Integer.class)));
        q.having(cb.in(c.get(customer_.getSingularAttribute("status", Integer.class)))
                .value(1).value(2));
        q.multiselect(c.get(customer_.getSingularAttribute("status", Integer.class)), cb
                .avg(c.get(customer_.getSingularAttribute("filledOrderCount",
                        Integer.class))), cb.count(c));

        assertEquivalence(q, jpql);
    }

    public void testOrderingByExpressionNotIncludedInSelection() {
        String jpql = "SELECT o FROM Customer c " 
                    + "JOIN c.orders o JOIN c.address a "
                    + "WHERE a.state = 'CA' "
                    + "ORDER BY o.quantity DESC, o.totalCost";
        
        CriteriaQuery<?> q = cb.createQuery();
        Root<Customer> c = q.from(Customer.class);
        SetJoin<Customer, Order> o = c.join(customer_.getSet("orders", 
                Order.class));
        Join<Customer, Address> a = c.join(customer_.getSingularAttribute("address",
                Address.class));
        q.where(cb.equal(
                a.get(address_.getSingularAttribute("state", String.class)),
                "CA"));
        q.orderBy(
          cb.desc(o.get(order_.getSingularAttribute("quantity", Integer.class))),
          cb.asc(o.get(order_.getSingularAttribute("totalCost", Double.class))));
        q.multiselect(o);

        assertEquivalence(q, jpql);
    }
    
    public void testOrderingByExpressionIncludedInSelection() {
        String jpql = "SELECT o.quantity, a.zipCode FROM Customer c "
                    + "JOIN c.orders o JOIN c.address a " 
                    + "WHERE a.state = 'CA' "
                    + "ORDER BY o.quantity, a.zipCode";
        
        CriteriaQuery<?> q = cb.createQuery();
        Root<Customer> c = q.from(Customer.class);
        Join<Customer, Order> o = c.join(customer_.getSet("orders",
                Order.class));
        Join<Customer, Address> a = c.join(customer_.getSingularAttribute("address",
                Address.class));
        q.where(cb.equal(
                a.get(address_.getSingularAttribute("state", String.class)),
                "CA"));
        q.orderBy(cb
                .asc(o.get(order_.getSingularAttribute("quantity", Integer.class))),
                cb.asc(a.get(address_.getSingularAttribute("zipCode", String.class))));
        q.multiselect(o.get(order_.getSingularAttribute("quantity", Integer.class)), 
                a.get(address_.getSingularAttribute("zipCode", String.class)));

        assertEquivalence(q, jpql);
    }
    
    public void testOrderingWithNumericalExpressionInSelection() {
        String jpql = "SELECT o.quantity, o.totalCost * 1.08 AS taxedCost, a.zipCode "
                + "FROM Customer c JOIN c.orders o JOIN c.address a "
                + "WHERE a.state = 'CA' AND a.county = 'Santa Clara' "
                + "ORDER BY o.quantity, taxedCost, a.zipCode";
        
        CriteriaQuery<?> q = cb.createQuery();
        Root<Customer> c = q.from(Customer.class);
        Join<Customer, Order> o = c.join(customer_.getSet("orders",  Order.class));
        Join<Customer, Address> a = c.join(customer_.getSingularAttribute("address", Address.class));
        Expression<Double> taxedCost = (Expression<Double>)cb.prod(o.get(order_.getSingularAttribute("totalCost", 
                Double.class)), 1.08).alias("taxedCost");
        q.where(cb.equal(a.get(address_.getSingularAttribute("state", String.class)), "CA"), 
                cb.equal(a.get(address_.getSingularAttribute("county", String.class)), "Santa Clara"));
        q.orderBy(cb.asc(o.get(order_.getSingularAttribute("quantity", Integer.class))),
                  cb.asc(taxedCost), 
                  cb.asc(a.get(address_.getSingularAttribute("zipCode", String.class))));
        q.multiselect(o.get(order_.getSingularAttribute("quantity", Integer.class)), 
                      taxedCost, 
                      a.get(address_.getSingularAttribute("zipCode", String.class)));
        assertEquivalence(q, jpql);
    }
}
