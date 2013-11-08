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

import javax.persistence.Parameter;
import javax.persistence.Query;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.apache.openjpa.persistence.criteria.AbstractCriteriaTestCase.QueryDecorator;

/**
 * Tests type-strict version of Criteria API. The test scenarios are adapted
 * from TestEJBQLCondExpression in
 * org.apache.openjpa.persistence.jpql.expressions and TestEJBQLFunction in
 * org.apache.openjpa.persistence.jpql.functions.
 * 
 */

public class TestTypeSafeCondExpression extends CriteriaTest {
    
    public void testNothingUsingCriteria() {
        String query = "SELECT o FROM CompUser o";
        
        CriteriaQuery<CompUser> cq = cb.createQuery(CompUser.class);
        cq.select(cq.from(CompUser.class));
        assertEquivalence(cq, query);
    }

    public void testBetween() {
        String jpql = "SELECT o.name FROM CompUser o " 
                    + "WHERE o.age BETWEEN 19 AND 40 AND o.computerName = 'PC'";
        
        CriteriaQuery<String> cq = cb.createQuery(String.class);
        Root<CompUser> o = cq.from(CompUser.class);
        cq.where(cb.and(cb.between(o.get(CompUser_.age), 19, 40), 
                cb.equal(o.get(CompUser_.computerName), "PC")));
        cq.select(o.get(CompUser_.name));
        
        assertEquivalence(cq, jpql);
    }

    public void testNotBetween() {
        String jpql = "SELECT o.name FROM CompUser o WHERE o.age NOT BETWEEN 19 AND 40 AND o.computerName= 'PC'";

        CriteriaQuery<String> cq = cb.createQuery(String.class);
        Root<CompUser> o = cq.from(CompUser.class);
        cq.where(cb.and(cb.between(o.get(CompUser_.age), 19, 40).not(), 
                cb.equal(o.get(CompUser_.computerName), "PC")));
        cq.select(o.get(CompUser_.name));
        
        assertEquivalence(cq, jpql);
    }

    public void testInExpr() {
        String jpql = "SELECT o.name FROM CompUser o WHERE o.age IN (29, 40, 10)";
        
        CriteriaQuery<String> cq = cb.createQuery(String.class);
        Root<CompUser> o = cq.from(CompUser.class);
        cq.where(cb.in(o.get(CompUser_.age)).value(29).value(40).value(10));
        cq.select(o.get(CompUser_.name));
        
        assertEquivalence(cq, jpql);
    }

    public void testNotIn() {
        String jpql = "SELECT o.name FROM CompUser o WHERE o.age NOT IN (29, 40, 10)";

        CriteriaQuery<String> cq = cb.createQuery(String.class);
        Root<CompUser> o = cq.from(CompUser.class);
        cq.where(cb.in(o.get(CompUser_.age)).value(29).value(40).value(10)
            .not());
        cq.select(o.get(CompUser_.name));
        
        assertEquivalence(cq, jpql);
    }

    public void testLike1() {
        String jpql = "SELECT o.computerName FROM CompUser o "
                 + "WHERE o.name LIKE 'Sha%' AND o.computerName NOT IN ('PC','Laptop')";

        CriteriaQuery<String> cq = cb.createQuery(String.class);
        Root<CompUser> o = cq.from(CompUser.class);
        cq.where(cb.and(
                    cb.like(o.get(CompUser_.name),"Sha%"), 
                    cb.in(o.get(CompUser_.computerName)).value("PC").value("Laptop").not()
                ));
        
        cq.select(o.get(CompUser_.computerName));

        assertEquivalence(cq, jpql);
    }
    
    public void testLike2() {
        String jpql = "SELECT o.computerName FROM CompUser o "
            + "WHERE o.name LIKE 'Sha%o_' AND o.computerName NOT IN ('UNIX','DOS')";

        CriteriaQuery<String> cq = cb.createQuery(String.class);
        Root<CompUser> o = cq.from(CompUser.class);
        cq.where(cb.and(
                    cb.like(o.get(CompUser_.name),"Sha%o_"), 
                    cb.in(o.get(CompUser_.computerName)).value("UNIX").value("DOS").not()
                ));
        cq.select(o.get(CompUser_.computerName));
        
        assertEquivalence(cq, jpql);
    }
    
    public void testLike3() {
        String jpql = "SELECT o.name FROM CompUser o WHERE o.name LIKE '_J%'";

        CriteriaQuery<String> cq = cb.createQuery(String.class);
        Root<CompUser> o = cq.from(CompUser.class);
        cq.where(cb.like(o.get(CompUser_.name),"_J%"));
        cq.select(o.get(CompUser_.name));
        
        assertEquivalence(cq, jpql);
    }
    
    public void testLikeWithEscapeCharacter() {
        String query = "SELECT o.name FROM CompUser o WHERE o.name LIKE :name ESCAPE '|'";
        
        CriteriaQuery<String> cq = cb.createQuery(String.class);
        Root<CompUser> c = cq.from(CompUser.class);
        ParameterExpression<String> param = cb.parameter(String.class, "name");
        cq.where(cb.like(c.get(CompUser_.name), param, '|'));
        cq.select(c.get(CompUser_.name));
        
        assertEquivalence(new QueryDecorator() {
            public void decorate(Query q) {
                q.setParameter("name", "%|_%");
            }
        }, cq, query);
    }

    public void testNullExpression() {
        String query = "SELECT o.name FROM CompUser o "
                     + "WHERE o.age IS NOT NULL AND o.computerName = 'PC'";
        
        CriteriaQuery<String> cq = cb.createQuery(String.class);
        Root<CompUser> c = cq.from(CompUser.class);
        cq.where(cb.and(cb.notEqual(c.get(CompUser_.age), null), 
                cb.equal(c.get(CompUser_.computerName), "PC")));
        cq.select(c.get(CompUser_.name));
        
        assertEquivalence(cq, query);
    }
    
    public void testNullExpr2UsingCriteria() {
        String query =
            "SELECT o.name FROM CompUser o WHERE o.address.country IS NULL";

        CriteriaQuery<String> cq = cb.createQuery(String.class);
        Root<CompUser> c = cq.from(CompUser.class);
        cq.where(cb.equal(c.get(CompUser_.address).get(Address_.country),
            null));
        cq.select(c.get(CompUser_.name));
        
        assertEquivalence(cq, query);
    }
    
    public void testIsEmptyExprUsingCriteria() {
        String query = "SELECT o.name FROM CompUser o WHERE o.nicknames IS NOT EMPTY";

        CriteriaQuery<String> cq = cb.createQuery(String.class);
        Root<CompUser> o = cq.from(CompUser.class);
        cq.select(o.get(CompUser_.name));
        cq.where(cb.isEmpty(o.get(CompUser_.nicknames)).not());
        assertEquivalence(cq, query);
    }
    
    public void testConstructorExprUsingCriteria() {
        String query = "SELECT NEW org.apache.openjpa.persistence.criteria.MaleUser(" +
            "c.name, c.computerName, c.address, c.age, c.userid)" +
            " FROM CompUser c WHERE c.name = 'Seetha'";
        
        CriteriaQuery<MaleUser> cq = cb.createQuery(MaleUser.class);
        Root<CompUser> c = cq.from(CompUser.class);
        cq.where(cb.equal(c.get(CompUser_.name), "Seetha"));
        cq.select(cb.construct(MaleUser.class, c.get(CompUser_.name), 
            c.get(CompUser_.computerName), c.get(CompUser_.address),
            c.get(CompUser_.age), c.get(CompUser_.userid)));
        
        assertEquivalence(cq, query);
   }

    public void testConcatSubStringFunc1() {
        String query = "select " +
            "CONCAT('Ablahum', SUBSTRING(e.name, LOCATE('e', e.name), 4)) " +
            "From CompUser e WHERE e.name='Seetha'";
        CriteriaQuery<String> cq = cb.createQuery(String.class);
        Root<CompUser> e = cq.from(CompUser.class);
        cq.select(
            cb.concat("Ablahum", 
                cb.substring(
                    e.get(CompUser_.name), 
                    cb.locate(e.get(CompUser_.name), "e"), 
                    cb.literal(4)
                 )
             )
        );
        cq.where(cb.equal(e.get(CompUser_.name), "Seetha"));
        
        assertEquivalence(cq, query);
    }
    
    public void testConcatSubStringFunc2() {
        String query = "select e.address From CompUser e where " +
            "e.name = 'Seetha' AND e.computerName = " +
            "CONCAT('Ablahum', SUBSTRING(e.name, LOCATE('e', e.name), 4)) ";
        CriteriaQuery<Address> cq = cb.createQuery(Address.class);
        Root<CompUser> e = cq.from(CompUser.class);
        cq.select(e.get(CompUser_.address));
        cq.where(cb.and(cb.equal(e.get(CompUser_.name), "Seetha"),
                cb.equal(e.get(CompUser_.computerName),
                cb.concat("Ablahum", 
                    cb.substring(e.get(CompUser_.name), 
                    cb.locate(e.get(CompUser_.name), "e"), cb.literal(4))))));
        
        assertEquivalence(cq, query);
    }

    public void testConcatSubStringFunc3() {
        String query = "select " +
            "CONCAT('XYZ', SUBSTRING(e.name, LOCATE('e', e.name))) " +
            "From CompUser e WHERE e.name='Ablahumeeth'";
        CriteriaQuery<?> cq = cb.createQuery();
        Root<CompUser> e = cq.from(CompUser.class);
        cq.multiselect(cb.concat("XYZ", cb.substring(e.get(CompUser_.name), 
                 cb.locate(e.get(CompUser_.name), "e"))));
        cq.where(cb.equal(e.get(CompUser_.name), "Ablahumeeth"));
        
        assertEquivalence(cq, query);
    }

    public void testConcatSubStringFunc4() {
        String query = "select e.age from CompUser e where " + 
            "e.name = 'Seetha' AND e.computerName = " +
            "CONCAT('XYZ', SUBSTRING(e.name, LOCATE('e', e.name))) ";
        CriteriaQuery<?> q = cb.createQuery();
        Root<CompUser> e = q.from(CompUser.class);
        q.multiselect(e.get(CompUser_.age));
        q.where(cb.and(cb.equal(e.get(CompUser_.name), "Seetha"),
                cb.equal(e.get(CompUser_.computerName),
                cb.concat("XYZ", cb.substring(e.get(CompUser_.name), 
                cb.locate(e.get(CompUser_.name), "e"))))));
        
        assertEquivalence(q, query);
    }

    public void testConcatFunc() {
        String query = "select " +
            "CONCAT('', '') From CompUser e WHERE e.name='Seetha'";
        CriteriaQuery<?> q = cb.createQuery();
        Root<CompUser> e = q.from(CompUser.class);
        q.multiselect(cb.concat("", cb.literal("")));
        q.where(cb.equal(e.get(CompUser_.name), "Seetha"));
        
        assertEquivalence(q, query);
    }

    public void testTrimFunc1() {
        String query = "select Trim(e.computerName) From CompUser e " +
        		"WHERE e.name='Shannon '";
        CriteriaQuery<?> q = cb.createQuery();
        Root<CompUser> e = q.from(CompUser.class);
        q.multiselect(cb.trim(e.get(CompUser_.computerName)));
        q.where(cb.equal(e.get(CompUser_.name), "Shannon "));
        
        assertEquivalence(q, query);
    }

    public void testTrimFunc2() {
        String query = "select e.computerName From CompUser e where " + 
            "Trim(e.name) = 'Shannon '";
        CriteriaQuery<?> q = cb.createQuery();
        Root<CompUser> e = q.from(CompUser.class);
        q.where(cb.equal(cb.trim(e.get(CompUser_.name)), "Shannon"));
        q.multiselect(e.get(CompUser_.computerName));
        
        assertEquivalence(q, query);
    }

    public void testLowerFunc1() {
        String query = "select LOWER(e.name) From CompUser e WHERE " +
        		"e.computerName='UNIX'";
        CriteriaQuery<?> q = cb.createQuery();
        Root<CompUser> e = q.from(CompUser.class);
        q.multiselect(cb.lower(e.get(CompUser_.name)));
        q.where(cb.equal(e.get(CompUser_.computerName), "UNIX"));
        
        assertEquivalence(q, query);
    }

    public void testLowerFunc2() {
        String query = "select e.age From CompUser e where LOWER(e.name)" +
        		" ='ugo'";
        CriteriaQuery<Integer> q = cb.createQuery(Integer.class);
        Root<CompUser> e = q.from(CompUser.class);
        q.where(cb.equal(cb.lower(e.get(CompUser_.name)), "ugo"));
        q.select(e.get(CompUser_.age));
        
        assertEquivalence(q, query);
    }

    public void testUpperFunc1() {
        String query = "select UPPER(e.name) From CompUser e WHERE " +
        		"e.computerName='PC'";
        CriteriaQuery<String> q = cb.createQuery(String.class);
        Root<CompUser> e = q.from(CompUser.class);
        q.select(cb.upper(e.get(CompUser_.name)));
        q.where(cb.equal(e.get(CompUser_.computerName), "PC"));
        
        assertEquivalence(q, query);
    }

    public void testUpperFunc2() {
        String query = "select e.age from CompUser e where " +
            "UPPER(e.name)='UGO'";
        CriteriaQuery<Integer> q = cb.createQuery(Integer.class);
        Root<CompUser> e = q.from(CompUser.class);
        q.where(cb.equal(cb.upper(e.get(CompUser_.name)), "UGO"));
        q.select(e.get(CompUser_.age));
        
        assertEquivalence(q, query);
    }

    public void testLengthFunc() {
        String query = "SELECT o.name FROM CompUser o " + 
            "WHERE LENGTH(o.address.country) = 3";
        
        CriteriaQuery<String> q = cb.createQuery(String.class);
        Root<CompUser> e = q.from(CompUser.class);
        q.where(cb.equal(cb.length(e.get(CompUser_.address).
            get(Address_.country)), 3));
        q.select(e.get(CompUser_.name));
        
        assertEquivalence(q, query);
    }

    public void testArithmFunc1() {
        String query = "select ABS(e.age) From CompUser e WHERE e.name='Seetha'";
        
        CriteriaQuery<Integer> q = cb.createQuery(Integer.class);
        Root<CompUser> e = q.from(CompUser.class);
        q.select(cb.abs(e.get(CompUser_.age)));
        q.where(cb.equal(e.get(CompUser_.name), "Seetha"));
        
        assertEquivalence(q, query);
    }
    
    public void testArithmFunc2() {
        String query = "select SQRT(e.age) From CompUser e WHERE e.name='Seetha'";
        
        CriteriaQuery<Double> q = cb.createQuery(Double.class);
        Root<CompUser> e = q.from(CompUser.class);
        q.select(cb.sqrt(e.get(CompUser_.age)));
        q.where(cb.equal(e.get(CompUser_.name), "Seetha"));
        
        assertEquivalence(q, query);
    }
    
    public void testArithmFunc3() {
        String query = "select MOD(e.age, 4) From CompUser e WHERE e.name='Seetha'";
        
        CriteriaQuery<Integer> q = cb.createQuery(Integer.class);
        Root<CompUser> e = q.from(CompUser.class);
        q.select(cb.mod(e.get(CompUser_.age), 4));
        q.where(cb.equal(e.get(CompUser_.name), "Seetha"));
        
        assertEquivalence(q, query);
    }
    
    public void testArithmFunc4() {
        String query = "SELECT e.name FROM CompUser e WHERE SIZE(e.nicknames) = 6";
        
        CriteriaQuery<String> q = cb.createQuery(String.class);
        Root<CompUser> e = q.from(CompUser.class);
        q.where(cb.equal(cb.size(e.get(CompUser_.nicknames)), 6));
        q.select(e.get(CompUser_.name));
        
        assertEquivalence(q, query);
    }

    public void testGroupByHavingClause() {
        String query = "SELECT c.name FROM CompUser c GROUP BY c.name HAVING c.name LIKE 'S%'";

        CriteriaQuery<String> q = cb.createQuery(String.class);
        Root<CompUser> e = q.from(CompUser.class);
        q.groupBy(e.get(CompUser_.name));
        q.having(cb.like(e.get(CompUser_.name), "S%"));
        q.select(e.get(CompUser_.name));
        
        assertEquivalence(q, query);
    }

    public void testOrderByClause() {
        String query = "SELECT c.name FROM CompUser c WHERE c.name LIKE 'S%' ORDER BY c.name";

        CriteriaQuery<String> q = cb.createQuery(String.class);
        Root<CompUser> e = q.from(CompUser.class);
        q.where(cb.like(e.get(CompUser_.name), "S%"));
        q.select(e.get(CompUser_.name));
        q.orderBy(cb.asc(e.get(CompUser_.name)));
        
        assertEquivalence(q, query);
    }

    public void testAVGAggregFunc() {
        //To be Tested: AVG, COUNT, MAX, MIN, SUM
        String query = "SELECT AVG(e.age) FROM CompUser e";
        
        CriteriaQuery<Double> q = cb.createQuery(Double.class);
        Root<CompUser> e = q.from(CompUser.class);
        q.select(cb.avg(e.get(CompUser_.age)));
        
        assertEquivalence(q, query);
    }

    public void testCOUNTAggregFunc() {
        String query = "SELECT COUNT(c.name) FROM CompUser c";

        CriteriaQuery<Long> q = cb.createQuery(Long.class);
        Root<CompUser> e = q.from(CompUser.class);
        q.select(cb.count(e.get(CompUser_.name)));
        
        assertEquivalence(q, query);
    }

    public void testMAXAggregFunc() {
        String query = "SELECT DISTINCT MAX(c.age) FROM CompUser c";

        CriteriaQuery<Integer> q = cb.createQuery(Integer.class);
        Root<CompUser> e = q.from(CompUser.class);
        q.select(cb.max(e.get(CompUser_.age))).distinct(true);
        
        assertEquivalence(q, query);
    }

    public void testMINAggregFunc() {
        String query = "SELECT DISTINCT MIN(c.age) FROM CompUser c";

        CriteriaQuery<Integer> q = cb.createQuery(Integer.class);
        Root<CompUser> e = q.from(CompUser.class);
        q.select(cb.min(e.get(CompUser_.age))).distinct(true);
        
        assertEquivalence(q, query);
    }

    public void testSUMAggregFunc() {
        String query = "SELECT SUM(c.age) FROM CompUser c";

        CriteriaQuery<Integer> q = cb.createQuery(Integer.class);
        Root<CompUser> e = q.from(CompUser.class);
        q.select(cb.sum(e.get(CompUser_.age)));
        
        assertEquivalence(q, query);
    }

    public void testTypeExpression1() {
        String jpql = "SELECT e FROM CompUser e where TYPE(e) in (:a, :b) ORDER By e.name";
        
        CriteriaQuery<CompUser> cq = cb.createQuery(CompUser.class);
        Root<CompUser> e = cq.from(CompUser.class);
        cq.select(e);
        Parameter<Class> param1 = cb.parameter(Class.class, "a");
        Parameter<Class> param2 = cb.parameter(Class.class, "b");
        cq.where(e.type().in(param1, param2));
        cq.orderBy(cb.asc(e.get(CompUser_.name)));
        
        assertEquivalence(new QueryDecorator() {
            public void decorate(Query q) {
                q.setParameter("a", MaleUser.class);
                q.setParameter("b", FemaleUser.class);
            }
        }, cq, jpql);
    }

    public void testTypeExpression2() {
        String query = "SELECT TYPE(e) FROM CompUser e where TYPE(e) <> :t";
        
        CriteriaQuery<Tuple> q = cb.createTupleQuery();
        Root<CompUser> e = q.from(CompUser.class);
        Parameter<Class> param1 = cb.parameter(Class.class, "t");
        Expression<Class<? extends CompUser>> etype = e.type();
        // how to specify the following
        q.multiselect(e.type());
        q.where(cb.equal(e.type(), param1).not());
        
        assertEquivalence(new QueryDecorator() {
            public void decorate(Query q) {
                q.setParameter("t", MaleUser.class);
            }
        }, q, query);
    }

    public void testTypeExpression3() {
        String query = "SELECT e, FemaleUser, a FROM Address a, FemaleUser e where e.address IS NOT NULL";
        
        CriteriaQuery<Tuple> q = cb.createTupleQuery();
        Root<Address> a = q.from(Address.class);
        Root<FemaleUser> e = q.from(FemaleUser.class);
        q.multiselect(e, cb.literal(FemaleUser.class), a);
        q.where(e.get(FemaleUser_.address).isNotNull());
        
        assertEquivalence(q, query);
    }

    public void testTypeExpression4() {
        String query = "SELECT e FROM CompUser e where TYPE(e) = MaleUser";
        
        CriteriaQuery<CompUser> cq = cb.createQuery(CompUser.class);
        Root<CompUser> e = cq.from(CompUser.class);
        cq.select(e);
        cq.where(cb.equal(e.type(), cb.literal(MaleUser.class)));
        
        assertEquivalence(cq, query);
    }

    public void testTypeExpression5() {
        String query = "SELECT e FROM CompUser e where TYPE(e) in (MaleUser)";
        
        CriteriaQuery<CompUser> cq = cb.createQuery(CompUser.class);
        Root<CompUser> e = cq.from(CompUser.class);
        cq.where(cb.in(e.type()).value(MaleUser.class));
        
        assertEquivalence(cq, query);
    }

    public void testTypeExpression6() {
        String query = "SELECT e FROM CompUser e where TYPE(e) not in " +
            "(MaleUser, FemaleUser)";
        
        CriteriaQuery<CompUser> cq = cb.createQuery(CompUser.class);
        Root<CompUser> e = cq.from(CompUser.class);
        cq.where(cb.in(e.type()).value(MaleUser.class).value(FemaleUser.class)
                .not());
        
        assertEquivalence(cq, query);
    }

    
    public void testTypeExpression7() {
        String query = "SELECT TYPE(a.b) FROM A a";
        CriteriaQuery<Tuple> q = cb.createTupleQuery();
        Root<A> a = q.from(A.class);
        q.multiselect(a.get(A_.b).type());
        
        assertEquivalence(q, query);
    }

    public void testTypeExpression8() {
        String query = "SELECT MaleUser FROM A a";
        
        CriteriaQuery<Class> q = cb.createQuery(Class.class);
        Root<A> a = q.from(A.class);
        q.multiselect(cb.literal(MaleUser.class));
        
        assertEquivalence(q, query);
    }

    public void testTypeExpression9() {
        String query = "SELECT "
                + " CASE TYPE(e) WHEN FemaleUser THEN 'Female' "
                + " ELSE 'Male' END FROM CompUser e";
        CriteriaQuery<Object> q = cb.createQuery();
        Root<CompUser> e = q.from(CompUser.class);
        q.select(cb.selectCase(e.type())
                    .when(FemaleUser.class, "Female")
                    .otherwise("Male"));
        
        assertEquivalence(q, query);
    }

    public void testCoalesceExpressions() {
         String query = "SELECT e.name, COALESCE (e.address.country, 'Unknown') FROM CompUser e ORDER BY e.name DESC";

        CriteriaQuery<Tuple> q = cb.createTupleQuery();
        Root<CompUser> e = q.from(CompUser.class);
        q.multiselect(e.get(CompUser_.name), 
                      cb.coalesce().value(e.get(CompUser_.address).get(Address_.country)).value("Unknown"));
        q.orderBy(cb.desc(e.get(CompUser_.name)));
        
        assertEquivalence(q, query);
    }

    public void testNullIfExpressions() {
        String query = "SELECT e.name, NULLIF (e.address.country, 'USA') FROM CompUser e ORDER BY e.name DESC";
        
        CriteriaQuery<Tuple> q = cb.createTupleQuery();
        Root<CompUser> e = q.from(CompUser.class);
        q.multiselect(e.get(CompUser_.name), 
                cb.nullif(e.get(CompUser_.address).get(Address_.country), "USA"));
        q.orderBy(cb.desc(e.get(CompUser_.name)));
        
        assertEquivalence(q, query);
    }

    public void testSimpleCaseExpression1() {
        String query = "SELECT e.name, e.age+1, "
                + "CASE e.address.country WHEN 'USA' THEN 'us' "
                + " ELSE 'non-us' END, e.address.country "
                + " FROM CompUser e";
        
        CriteriaQuery<Tuple> q = cb.createTupleQuery();
        Root<CompUser> e = q.from(CompUser.class);
        Expression<Integer> cage = cb.sum(e.get(CompUser_.age), 1);
        Expression d2 = cb.selectCase(
                e.get(CompUser_.address).get(Address_.country)).when("USA",
                "us").otherwise("non-us");
        q.multiselect(e.get(CompUser_.name), cage, d2, e.get(CompUser_.address).get(
                Address_.country));
        
        assertEquivalence(q, query);
    }

    public void testSimpleCaseExpression2() {
        String query = "SELECT e.name, e.age+1, "
                + "CASE e.address.country WHEN 'USA'"
                + " THEN 'United-States' "
                + " ELSE e.address.country  END," + " e.address.country "
                + " FROM CompUser e";
        CriteriaQuery<Tuple> q = cb.createTupleQuery();
        Root<CompUser> e = q.from(CompUser.class);
        Expression cage = cb.sum(e.get(CompUser_.age), 1);
        Expression d2 = cb.selectCase(
                e.get(CompUser_.address).get(Address_.country)).when("USA",
                "United-States").otherwise(
                e.get(CompUser_.address).get(Address_.country));
        q.multiselect(e.get(CompUser_.name), cage, d2, e.get(CompUser_.address).get(
                Address_.country));
        
        assertEquivalence(q, query);
    }

    public void testSimpleCaseExpression3() {
        String query = "SELECT e.name, "
                + " CASE TYPE(e) WHEN FemaleUser THEN 'Female' "
                + " ELSE 'Male' END"
                + " FROM CompUser e WHERE e.name like 'S%' "
                + " ORDER BY e.name DESC";
        CriteriaQuery<Tuple> q = cb.createTupleQuery();
        Root<CompUser> e = q.from(CompUser.class);
        q.multiselect(e.get(CompUser_.name), 
            cb.selectCase(e.type()).when(FemaleUser.class, "Female")
            .otherwise("Male"));
        q.where(cb.like(e.get(CompUser_.name), "S%"));
        q.orderBy(cb.desc(e.get(CompUser_.name)));
        
        assertEquivalence(q, query);
    }

    public void testSimpleCaseExpression4() {
        String query = "SELECT e.name, CASE e.address.country WHEN 'USA'"
                + " THEN true ELSE false END,"
                + " e.address.country FROM CompUser e";
        CriteriaQuery<Tuple> q = cb.createTupleQuery();
        Root<CompUser> e = q.from(CompUser.class);
        Expression b = cb.selectCase(
                e.get(CompUser_.address).get(Address_.country)).when("USA",
                true).otherwise(false);
        q.multiselect(e.get(CompUser_.name), b, e.get(CompUser_.address).get(
                Address_.country));
        
        assertEquivalence(q, query);
    }

    public void testGeneralCaseExpression1() {
        String query = "SELECT e.name, e.age, "
                + " CASE WHEN e.age > 30 THEN e.age - 1 "
                + " WHEN e.age < 15 THEN e.age + 1 ELSE e.age + 0 "
                + " END FROM CompUser e";
        CriteriaQuery<Tuple> q = cb.createTupleQuery();
        Root<CompUser> e = q.from(CompUser.class);
        Expression cage = cb.selectCase().when(cb.gt(e.get(CompUser_.age), 30),
                cb.diff(e.get(CompUser_.age), 1)).when(
                cb.lt(e.get(CompUser_.age), 15),
                cb.sum(e.get(CompUser_.age), 1)).otherwise(
                cb.sum(e.get(CompUser_.age), 0));
        q.multiselect(e.get(CompUser_.name), e.get(CompUser_.age), cage);

        assertEquivalence(q, query);
    }

    public void testGeneralCaseExpression2() {
        String query = "SELECT e.name, e.age+1, "
                + "CASE WHEN e.address.country = 'USA' "
                + " THEN 'United-States' "
                + " ELSE 'Non United-States'  END,"
                + " e.address.country "
                + " FROM CompUser e";
        CriteriaQuery<Tuple> q = cb.createTupleQuery();
        Root<CompUser> e = q.from(CompUser.class);
        Expression d2 = cb.selectCase()
                .when(
                        cb.equal(
                                e.get(CompUser_.address).get(Address_.country),
                                "USA"), "United-States").otherwise(
                        "Non United-States");
        Expression cage = cb.sum(e.get(CompUser_.age), 1);
        q.multiselect(e.get(CompUser_.name), cage, d2, e.get(CompUser_.address).get(
                Address_.country));

        assertEquivalence(q, query);
    }

    public void testGeneralCaseExpression3() {
        String query = " select e.name, "
            + "CASE WHEN e.age = 11 THEN "
            + "org.apache.openjpa.persistence.criteria.CompUser$" 
            + "CreditRating.POOR"
            + " WHEN e.age = 35 THEN "
            + "org.apache.openjpa.persistence.criteria.CompUser$" 
            + "CreditRating.GOOD"
            + " ELSE "
            + "org.apache.openjpa.persistence.criteria.CompUser$" 
            + "CreditRating.EXCELLENT"
            + " END FROM CompUser e ORDER BY e.age";
        CriteriaQuery<Tuple> q = cb.createTupleQuery();
        Root<CompUser> e = q.from(CompUser.class);
        q.multiselect(e.get(CompUser_.name), cb.selectCase().when(
                cb.equal(e.get(CompUser_.age), 11), CompUser.CreditRating.POOR)
                .when(cb.equal(e.get(CompUser_.age), 35),
                        CompUser.CreditRating.GOOD).otherwise(
                        CompUser.CreditRating.EXCELLENT));

        q.orderBy(cb.asc(e.get(CompUser_.age)));

        assertEquivalence(q, query);
    }

    // not sure how to write CriteriaQuery for
    // Subquery.select(SimpleCase/GeneralCase)
    
    public void testGeneralCaseExpression4() {
        String query = "select e.name, e.creditRating from CompUser e "
            + "where e.creditRating = "
            + "(select "
            + "CASE WHEN e1.age = 11 THEN "
            + "org.apache.openjpa.persistence.criteria.CompUser$" 
            + "CreditRating.POOR"
            + " WHEN e1.age = 35 THEN "
            + "org.apache.openjpa.persistence.criteria.CompUser$" 
            + "CreditRating.GOOD"
            + " ELSE "
            + "org.apache.openjpa.persistence.criteria.CompUser$" 
            + "CreditRating.EXCELLENT"
            + " END from CompUser e1"
            + " where e.userid = e1.userid) ORDER BY e.age";
        CriteriaQuery<Tuple> q = cb.createTupleQuery();
        Root<CompUser> e = q.from(CompUser.class);
        q.multiselect(e.get(CompUser_.name), e.get(CompUser_.creditRating));
        q.orderBy(cb.asc(e.get(CompUser_.age)));
        Subquery<Object> sq = q.subquery(Object.class);
        Root<CompUser> e1 = sq.from(CompUser.class);
        sq.where(cb.equal(e.get(CompUser_.userid), e1.get(CompUser_.userid)));

        q.where(cb.equal(e.get(CompUser_.creditRating),
           sq.select(
                cb.selectCase()
                  .when(cb.equal(e1.get(CompUser_.age), 11), CompUser.CreditRating.POOR)
                  .when(cb.equal(e1.get(CompUser_.age), 35), CompUser.CreditRating.GOOD)
                  .otherwise(CompUser.CreditRating.EXCELLENT))));

        q.orderBy(cb.asc(e.get(CompUser_.age)));
        
        assertEquivalence(q, query);
    }
}
