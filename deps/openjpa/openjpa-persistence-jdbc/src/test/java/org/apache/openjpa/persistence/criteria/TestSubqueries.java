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

import java.sql.Timestamp;

import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;
import javax.persistence.criteria.Subquery;

public class TestSubqueries extends CriteriaTest {
    
    public void testExist() {
        String query = "SELECT DISTINCT o.name FROM CompUser o WHERE EXISTS"
                + " (SELECT c FROM Address c WHERE c = o.address )";

        CriteriaQuery<String> q = cb.createQuery(String.class);
        Root<CompUser> o = q.from(CompUser.class);
        Subquery<Address> sq = q.subquery(Address.class);
        Root<Address> c = sq.from(Address.class);
        sq.select(c);
        sq.where(cb.equal(c, o.get(CompUser_.address)));
        q.where(cb.exists(sq));
        q.select(o.get(CompUser_.name)).distinct(true);
        assertEquivalence(q, query);
    }

    
    public void testNotExist() {
        String query = "SELECT DISTINCT o.name FROM CompUser o WHERE NOT EXISTS"
                + " (SELECT s FROM CompUser s WHERE s.address.country = "
                + "o.address.country)";

        CriteriaQuery<String> q = cb.createQuery(String.class);
        Root<CompUser> o = q.from(CompUser.class);
        Subquery<CompUser> sq = q.subquery(CompUser.class);
        Root<CompUser> s = sq.from(CompUser.class);
        sq.select(s);
        sq.where(cb.equal(s.get(CompUser_.address).get(Address_.country), o
                .get(CompUser_.address).get(Address_.country)));
        q.where(cb.exists(sq).not());
        q.select(o.get(CompUser_.name)).distinct(true);

        assertEquivalence(q, query);
    }

    public void testAny() {
        String query = "SELECT o.name FROM CompUser o " 
                     + "WHERE o.address.zipCode = "
                     + " ANY (SELECT s.computerName " 
                     + " FROM CompUser s WHERE s.address.country IS NOT NULL)";

        CriteriaQuery<String> q = cb.createQuery(String.class);
        Root<CompUser> o = q.from(CompUser.class);
        q.select(o.get(CompUser_.name));
        Subquery<String> sq = q.subquery(String.class);
        Root<CompUser> s = sq.from(CompUser.class);
        sq.select(s.get(CompUser_.computerName));
        sq.where(cb.notEqual(s.get(CompUser_.address).get(Address_.country),
                null));
        q.where(cb.equal(o.get(CompUser_.address).get(Address_.zipCode), cb
                .any(sq)));

        assertEquivalence(q, query);
    }

    public void testSubquery01() {
        String query = "select o1.id from Order o1 where o1.id in "
                + " (select distinct o.id from LineItem i, Order o"
                + " where i.quantity > 10 and o.count > 1000 and i.id = o.id)";
        
        CriteriaQuery<Integer> q = cb.createQuery(Integer.class);
        Root<Order> o1 = q.from(Order.class);
        q.select(o1.get(Order_.id));

        Subquery<Integer> sq = q.subquery(Integer.class);
        Root<LineItem> i = sq.from(LineItem.class);
        Root<Order> o = sq.from(Order.class);
        sq.where(cb.and(cb.and(cb.gt(i.get(LineItem_.quantity), 10), cb.gt(o
                .get(Order_.count), 1000)), cb.equal(i.get(LineItem_.id), o
                .get(Order_.id))));
        sq.select(o.get(Order_.id)).distinct(true);
        q.where(cb.in(o1.get(Order_.id)).value(sq));
        
        assertEquivalence(q, query);
    }

    public void testSubquery02() {
        String query = "select o.id from Order o where o.customer.balanceOwed ="
                + " (select max(o2.customer.balanceOwed) from Order o2"
                + " where o.customer.id = o2.customer.id)";
        
        CriteriaQuery<Integer> q = cb.createQuery(Integer.class);
        Root<Order> o = q.from(Order.class);
        q.select(o.get(Order_.id));
        Subquery<Integer> sq = q.subquery(Integer.class);
        Root<Order> o2 = sq.from(Order.class);
        sq.where(cb.equal(o.get(Order_.customer).get(Customer_.id), o2.get(
                Order_.customer).get(Customer_.id)));
        q.where(cb.equal(o.get(Order_.customer).get(Customer_.balanceOwed), sq
                .select(cb.max(o2.get(Order_.customer).get(
                        Customer_.balanceOwed)))));
        
        assertEquivalence(q, query);
    }

    public void testSubquery03() {
        String query = "select o from Order o where o.customer.balanceOwed ="
                + " (select max(o2.customer.balanceOwed) from Order o2"
                + " where o.customer.id = o2.customer.id)";
        
        CriteriaQuery<Order> q = cb.createQuery(Order.class);
        Root<Order> o = q.from(Order.class);
        q.select(o);
        Subquery<Integer> sq = q.subquery(Integer.class);
        Root<Order> o2 = sq.from(Order.class);
        sq.where(cb.equal(o.get(Order_.customer).get(Customer_.id), o2.get(
                Order_.customer).get(Customer_.id)));
        q.where(cb.equal(o.get(Order_.customer).get(Customer_.balanceOwed), sq
                .select(cb.max(o2.get(Order_.customer).get(
                        Customer_.balanceOwed)))));
        
        assertEquivalence(q, query);
    }

    public void testSubquery04() {
        String query = "select o.id from Order o where o.quantity >"
                + " (select count(i) from o.lineItems i)";
        
        CriteriaQuery<Integer> q = cb.createQuery(Integer.class);
        Root<Order> o = q.from(Order.class);
        q.select(o.get(Order_.id));
        Subquery<Long> sq = q.subquery(Long.class);
        Root<Order> osq = sq.correlate(o);
        Join<Order, LineItem> i = osq.join(Order_.lineItems);
        q.where(cb.gt(o.get(Order_.quantity), sq.select(cb.count(i))));
        assertEquivalence(q, query);
    }

    public void testSubquery05() {
        String query = "select o.id from Order o where o.quantity >"
                + " (select count(o.quantity) from Order o)";
        
        CriteriaQuery<Integer> q = cb.createQuery(Integer.class);
        Root<Order> o = q.from(Order.class);
        q.select(o.get(Order_.id));
        Subquery<Long> sq = q.subquery(Long.class);
        Root<Order> o2 = sq.from(Order.class);
        q.where(cb.gt(o.get(Order_.quantity), sq.select(cb.count(o2
                .get(Order_.quantity)))));
        
        assertEquivalence(q, query);
    }

    
    public void testSubquery06() {
        String query = "select o.id from Order o where o.quantity >"
                + " (select count(o.id) from Order o)";

        CriteriaQuery<Integer> q = cb.createQuery(Integer.class);
        Root<Order> o = q.from(Order.class);
        q.select(o.get(Order_.id));
        Subquery<Long> sq = q.subquery(Long.class);
        Root<Order> o2 = sq.from(Order.class);
        q.where(cb.gt(o.get(Order_.quantity), sq.select(cb.count(o2
                .get(Order_.id)))));
        
        assertEquivalence(q, query);
    }

    
    public void testSubquery07() {
        String query = "select o.id from Order o where o.quantity >"
                + " (select avg(o.quantity) from Order o)";

        CriteriaQuery<Integer> q = cb.createQuery(Integer.class);
        Root<Order> o = q.from(Order.class);
        q.select(o.get(Order_.id));
        Subquery<Double> sq = q.subquery(Double.class);
        Root<Order> o2 = sq.from(Order.class);
        q.where(cb.gt(o.get(Order_.quantity), sq.select(cb.avg(o2
                .get(Order_.quantity)))));
        
        assertEquivalence(q, query);
    }

    public void testSubquery08() {
        String query = "select c.name from Customer c "
                + "where exists(select o from c.orders o where o.id = 1) "
                + "or exists(select o from c.orders o where o.id = 2)";
        
        CriteriaQuery<String> q = cb.createQuery(String.class);
        Root<Customer> c = q.from(Customer.class);
        q.select(c.get(Customer_.name));
        Subquery<Order> sq1 = q.subquery(Order.class);
        Root<Customer> c1 = sq1.correlate(c);
        SetJoin<Customer, Order> o1 = c1.join(Customer_.orders);
        sq1.where(cb.equal(o1.get(Order_.id), 1)).select(o1);

        Subquery<Order> sq2 = q.subquery(Order.class);
        Root<Customer> c2 = sq2.correlate(c);
        SetJoin<Customer, Order> o2 = c2.join(Customer_.orders);
        sq2.where(cb.equal(o2.get(Order_.id), 2)).select(o2);

        q.where(cb.or(cb.exists(sq1), cb.exists(sq2)));
        
        assertEquivalence(q, query);
    }

    
    public void testSubquery09() {
        String query = "select c.name from Customer c, in(c.orders) o "
                + "where o.quantity between "
                + "(select max(o.quantity) from Order o) and "
                + "(select avg(o.quantity) from Order o) ";
        
        CriteriaQuery<String> q = cb.createQuery(String.class);
        Root<Customer> c = q.from(Customer.class);
        q.select(c.get(Customer_.name));

        Subquery<Integer> sq1 = q.subquery(Integer.class);
        Root<Order> o1 = sq1.from(Order.class);
        sq1.select(cb.max(o1.get(Order_.quantity)));

        Subquery<Double> sq2 = q.subquery(Double.class);
        Root<Order> o2 = sq2.from(Order.class);
        sq2.select(cb.avg(o2.get(Order_.quantity)));

        SetJoin<Customer, Order> o = c.join(Customer_.orders);
        q.where(cb.between(o.get(Order_.quantity), sq1, sq2.as(Integer.class)));
        
        assertEquivalence(q, query);
    }

    public void testSubquery10() {
        String query = "select o.id from Order o where o.quantity >"
                + " (select sum(o2.quantity) from Customer c, " 
                + "in(c.orders) o2) ";
        
        CriteriaQuery<Integer> q = cb.createQuery(Integer.class);
        Root<Order> o = q.from(Order.class);
        q.select(o.get(Order_.id));

        Subquery<Integer> sq = q.subquery(Integer.class);
        Root<Customer> c = sq.from(Customer.class);
        SetJoin<Customer, Order> o2 = c.join(Customer_.orders);
        sq.select(cb.sum(o2.get(Order_.quantity)));

        q.where(cb.gt(o.get(Order_.quantity), sq));
        
        assertEquivalence(q, query);
    }

    
    public void testSubquery11() {
        String query = "select o.id from Order o where o.quantity between"
                + " (select avg(o2.quantity) from Customer c, in(c.orders) o2)"
                + " and (select min(o2.quantity) from Customer c, in(c.orders)"
                + " o2)";

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
        assertEquivalence(q, query);
    }

    
    public void testSubquery12() {
        String query = "select o.id from Customer c, in(c.orders)o "
                + "where o.quantity > (select sum(o2.quantity)"
                + " from c.orders o2)";

        CriteriaQuery<Integer> q = cb.createQuery(Integer.class);
        Root<Customer> c = q.from(Customer.class);
        SetJoin<Customer, Order> o = c.join(Customer_.orders);
        q.select(o.get(Order_.id));

        Subquery<Integer> sq = q.subquery(Integer.class);
        Root<Customer> sqc = sq.correlate(c);
        SetJoin<Customer, Order> o2 = sqc.join(Customer_.orders);
        sq.select(cb.sum(o2.get(Order_.quantity)));
        q.where(cb.gt(o.get(Order_.quantity), sq));
        
        assertEquivalence(q, query);
    }

    public void testSubquery13() {
        String query = "select o1.id, c.name from Order o1, Customer c"
                + " where o1.quantity = "
                + " any(select o2.quantity from in(c.orders) o2)";
        
        CriteriaQuery<Tuple> q = cb.createTupleQuery();
        Root<Order> o1 = q.from(Order.class);
        Root<Customer> c = q.from(Customer.class);
        q.multiselect(o1.get(Order_.id), c.get(Customer_.name));

        Subquery<Integer> sq = q.subquery(Integer.class);
        Root<Customer> sqc = sq.correlate(c);
        SetJoin<Customer, Order> o2 = sqc.join(Customer_.orders);
        sq.select(o2.get(Order_.quantity));

        q.where(cb.equal(o1.get(Order_.quantity), cb.any(sq)));
        
        assertEquivalence(q, query);
    }

    
    public void testSubquery14() {
        String query = "SELECT p, m FROM Publisher p "
            + "LEFT OUTER JOIN p.magazineCollection m "
            + "WHERE m.id = (SELECT MAX(m2.id) FROM Magazine m2 "
            + "WHERE m2.idPublisher.id = p.id AND m2.id = "
            + "(SELECT MAX(m3.id) FROM Magazine m3 "
            + "WHERE m3.idPublisher.id = p.id)) ";

        CriteriaQuery<Tuple> q = cb.createTupleQuery();
        Root<Publisher> p = q.from(Publisher.class);
        Join<Publisher, Magazine> m = p.join(Publisher_.magazineCollection,
            JoinType.LEFT);
        q.multiselect(p, m);

        Subquery<Integer> sq = q.subquery(Integer.class);
        Root<Magazine> m2 = sq.from(Magazine.class);
        q.where(cb.equal(
                m.get(Magazine_.id), 
                sq.select(cb.max(m2.get(Magazine_.id)))));

        Subquery<Integer> sq2 = q.subquery(Integer.class);
        Root<Magazine> m3 = sq2.from(Magazine.class);
        
        sq2.where(cb.equal(
                m3.get(Magazine_.idPublisher).get(Publisher_.id), 
                p.get(Publisher_.id)));
        
        sq.where(cb.and(cb.equal(
                   m2.get(Magazine_.idPublisher).get(Publisher_.id), 
                   p.get(Publisher_.id)), 
                cb.equal(
                   m2.get(Magazine_.id), 
                   sq2.select(cb.max(m3.get(Magazine_.id))))));
        
        assertEquivalence(q, query);
    }

    public void testSubquery15() {
        String query = "select o.id from Order o where o.delivered =(select "
                + "   CASE WHEN o2.quantity > 10 THEN true"
                + "     WHEN o2.quantity = 10 THEN false "
                + "     ELSE false END from Order o2"
                + " where o.customer.id = o2.customer.id)";

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
        
        assertEquivalence(q, query);
    }

    
    public void testSubquery16() {
        String query = "select o1.id from Order o1 where o1.quantity > "
                + " (select o.quantity*2 from LineItem i, Order o"
                + " where i.quantity > 10 and o.quantity > 1000 and i.id = " +
                        "o.id)";

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

        assertEquivalence(q, query);
    }

    
    public void testSubquery17() {
        String query = "select o.id from Order o where o.customer.name ="
                + " (select substring(o2.customer.name, 3) from Order o2"
                + " where o.customer.id = o2.customer.id)";

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

        assertEquivalence(q, query);
    }

    public void testSubquery18() {
        String query = "select o.id from Order o where o.orderTs >"
                + " (select CURRENT_TIMESTAMP from o.lineItems i)";

        CriteriaQuery<Integer> q = cb.createQuery(Integer.class);
        Root<Order> o = q.from(Order.class);
        q.select(o.get(Order_.id));

        Subquery<Timestamp> sq = q.subquery(Timestamp.class);
        Root<Order> o2 = sq.correlate(o);
        ListJoin<Order, LineItem> i = o2.join(Order_.lineItems);

        q.where(cb.gt(o.get(Order_.orderTs).as(Long.class), sq.select(cb.currentTimestamp()).as(Long.class)));
        
        assertEquivalence(q, query);
    }

    
    public void testSubquery19() {
        String query = "select o.id from Order o where o.quantity >"
                + " (select SQRT(o.quantity) from Order o where o.delivered" +
                        " = true)";
        CriteriaQuery<Integer> q = cb.createQuery(Integer.class);
        Root<Order> o = q.from(Order.class);
        q.select(o.get(Order_.id));

        Subquery<Double> sq = q.subquery(Double.class);
        Root<Order> o2 = sq.from(Order.class);
        sq.where(cb.equal(o2.get(Order_.delivered), true));

        q.where(cb.gt(o.get(Order_.quantity), sq.select(cb.sqrt(o2
                .get(Order_.quantity)))));
        assertEquivalence(q, query);
    }

    
    public void testSubquery20() {
        String query = "select o.id from Order o where o.customer.name in"
                + " (select CONCAT(o.customer.name, 'XX') from Order o"
                + " where o.quantity > 10)";
        CriteriaQuery<Integer> q = cb.createQuery(Integer.class);
        Root<Order> o = q.from(Order.class);
        q.select(o.get(Order_.id));

        Subquery<String> sq = q.subquery(String.class);
        Root<Order> o2 = sq.from(Order.class);
        sq.where(cb.gt(o2.get(Order_.quantity), 10));

        q.where(cb.in(o.get(Order_.customer).get(Customer_.name)).value(
                sq.select(cb.concat(
                        o2.get(Order_.customer).get(Customer_.name), "XX"))));
        assertEquivalence(q, query);
    }

    public void testSubquery21() {
        String query = "select c from Customer c where c.creditRating ="
                + " (select "
                + "   CASE WHEN o2.quantity > 10 THEN "
       + "org.apache.openjpa.persistence.criteria.Customer$CreditRating.POOR " 
                + "WHEN o2.quantity = 10 THEN "
       + "org.apache.openjpa.persistence.criteria.Customer$CreditRating.GOOD " 
                + "     ELSE "
   + "org.apache.openjpa.persistence.criteria.Customer$CreditRating.EXCELLENT " 
                + "     END from Order o2"
                + " where c.id = o2.customer.id)";
        
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
        assertEquivalence(q, query);
    }

    public void testSubquery22() {
        String query = "select c from Customer c "
                + "where c.creditRating = (select COALESCE (c1.creditRating, "
                + "org.apache.openjpa.persistence.criteria.Customer$" +
                        "CreditRating.POOR) "
                + "from Customer c1 where c1.name = 'Famzy') order by c.name " +
                        "DESC";
        
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
        assertEquivalence(q, query);
    }

    
    public void testSubquery23() {
        String query =
            "select c from Customer c "
                + "where c.creditRating = (select NULLIF (c1.creditRating, "
                + "org.apache.openjpa.persistence.criteria."
                + "Customer$CreditRating.POOR) "
                + "from Customer c1 where c1.name = 'Famzy') "
                + "order by c.name DESC";
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
        assertEquivalence(q, query);
    }
}
