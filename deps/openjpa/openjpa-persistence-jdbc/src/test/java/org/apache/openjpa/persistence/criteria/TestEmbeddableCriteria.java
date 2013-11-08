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

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Parameter;
import javax.persistence.Query;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.MapJoin;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.apache.openjpa.persistence.embed.Company1;
import org.apache.openjpa.persistence.embed.Company1_;
import org.apache.openjpa.persistence.embed.Company2;
import org.apache.openjpa.persistence.embed.Company2_;
import org.apache.openjpa.persistence.embed.Department1;
import org.apache.openjpa.persistence.embed.Department1_;
import org.apache.openjpa.persistence.embed.Department2;
import org.apache.openjpa.persistence.embed.Department3;
import org.apache.openjpa.persistence.embed.Division;
import org.apache.openjpa.persistence.embed.Embed;
import org.apache.openjpa.persistence.embed.Embed_;
import org.apache.openjpa.persistence.embed.Embed_Coll_Embed;
import org.apache.openjpa.persistence.embed.Embed_Coll_Embed_;
import org.apache.openjpa.persistence.embed.Embed_Coll_Integer;
import org.apache.openjpa.persistence.embed.Embed_Coll_Integer_;
import org.apache.openjpa.persistence.embed.Embed_Embed;
import org.apache.openjpa.persistence.embed.Embed_Embed_;
import org.apache.openjpa.persistence.embed.Embed_Embed_ToMany;
import org.apache.openjpa.persistence.embed.Embed_Embed_ToMany_;
import org.apache.openjpa.persistence.embed.Embed_MappedToOne;
import org.apache.openjpa.persistence.embed.Embed_MappedToOne_;
import org.apache.openjpa.persistence.embed.Embed_ToMany;
import org.apache.openjpa.persistence.embed.Embed_ToMany_;
import org.apache.openjpa.persistence.embed.Embed_ToOne;
import org.apache.openjpa.persistence.embed.Embed_ToOne_;
import org.apache.openjpa.persistence.embed.Employee1;
import org.apache.openjpa.persistence.embed.Employee2;
import org.apache.openjpa.persistence.embed.Employee3;
import org.apache.openjpa.persistence.embed.EntityA_Coll_Embed_Embed;
import org.apache.openjpa.persistence.embed.EntityA_Coll_Embed_Embed_;
import org.apache.openjpa.persistence.embed.EntityA_Coll_Embed_ToOne;
import org.apache.openjpa.persistence.embed.EntityA_Coll_Embed_ToOne_;
import org.apache.openjpa.persistence.embed.EntityA_Coll_String;
import org.apache.openjpa.persistence.embed.EntityA_Coll_String_;
import org.apache.openjpa.persistence.embed.EntityA_Embed_Coll_Embed;
import org.apache.openjpa.persistence.embed.EntityA_Embed_Coll_Embed_;
import org.apache.openjpa.persistence.embed.EntityA_Embed_Coll_Integer;
import org.apache.openjpa.persistence.embed.EntityA_Embed_Coll_Integer_;
import org.apache.openjpa.persistence.embed.EntityA_Embed_Embed;
import org.apache.openjpa.persistence.embed.EntityA_Embed_Embed_;
import org.apache.openjpa.persistence.embed.EntityA_Embed_Embed_ToMany;
import org.apache.openjpa.persistence.embed.EntityA_Embed_Embed_ToMany_;
import org.apache.openjpa.persistence.embed.EntityA_Embed_MappedToOne;
import org.apache.openjpa.persistence.embed.EntityA_Embed_MappedToOne_;
import org.apache.openjpa.persistence.embed.EntityA_Embed_ToMany;
import org.apache.openjpa.persistence.embed.EntityA_Embed_ToMany_;
import org.apache.openjpa.persistence.embed.EntityA_Embed_ToOne;
import org.apache.openjpa.persistence.embed.EntityA_Embed_ToOne_;
import org.apache.openjpa.persistence.embed.EntityB1;
import org.apache.openjpa.persistence.embed.EntityB1_;
import org.apache.openjpa.persistence.embed.Item1;
import org.apache.openjpa.persistence.embed.Item1_;
import org.apache.openjpa.persistence.embed.Item2;
import org.apache.openjpa.persistence.embed.Item2_;
import org.apache.openjpa.persistence.embed.Item3;
import org.apache.openjpa.persistence.embed.Item3_;
import org.apache.openjpa.persistence.embed.VicePresident;
import org.apache.openjpa.persistence.embed.VicePresident_;
import org.apache.openjpa.persistence.test.AllowFailure;


public class TestEmbeddableCriteria extends EmbeddableDomainTestCase {

    private static int TEST_COUNT = 0;
    
    @Override
    public void setUp() throws Exception {
        super.setUp();
        TEST_COUNT++;
    }
    
    @Override
    public void tearDown() throws Exception {
        // Hack to cleanup EM and EMF when we're done
        if (TEST_COUNT >= 123)
            super.tearDown();
    }
    
    public void testEmbeddableQuery1() {
        String jpql = "select e from EntityA_Coll_String a, in (a.nickNames) e order by a.id";
        CriteriaQuery<String> q = cb.createQuery(String.class);
        Root<EntityA_Coll_String> a = q.from(EntityA_Coll_String.class);
        Join<EntityA_Coll_String, String> e = a.join(EntityA_Coll_String_.nickNames);
        q.select(e);
        q.orderBy(cb.asc(a.get(EntityA_Coll_String_.id)));
        
        assertEquivalence(q, jpql);
    }
    
    public void testEmbeddableQuery3() {
        String jpql = "select e from EntityA_Coll_String a, in (a.nickNames) e order by e";
        CriteriaQuery<String> q = cb.createQuery(String.class);
        Root<EntityA_Coll_String> a = q.from(EntityA_Coll_String.class);
        Join<EntityA_Coll_String, String> e = a.join(EntityA_Coll_String_.nickNames);
        q.select(e);
        q.orderBy(cb.asc(e));
        
        assertEquivalence(q, jpql);
    }
    
    public void testEmbeddableQuery4() {
        String jpql = "select a from EntityA_Coll_String a WHERE a.nickNames IS EMPTY order by a";
        CriteriaQuery<EntityA_Coll_String> q = cb.createQuery(EntityA_Coll_String.class);
        Root<EntityA_Coll_String> a = q.from(EntityA_Coll_String.class);
        q.select(a);
        q.where(cb.isEmpty(a.get(EntityA_Coll_String_.nickNames)));
        q.orderBy(cb.asc(a));
        
        assertEquivalence(q, jpql);
    }
    
    public void testEmbeddableQuery5() {
        String jpql = "select a from EntityA_Coll_String a WHERE exists (select n from EntityA_Coll_String a, " +
                " in (a.nickNames) n where n like '%1') order by a";
        CriteriaQuery<EntityA_Coll_String> q = cb.createQuery(EntityA_Coll_String.class);
        Root<EntityA_Coll_String> a = q.from(EntityA_Coll_String.class);
        q.select(a);
        Subquery<Set> sq = q.subquery(Set.class);
        Root<EntityA_Coll_String> a1 = sq.from(EntityA_Coll_String.class);
        Expression n = a1.get(EntityA_Coll_String_.nickNames);
        n.alias("n");
        sq.where(cb.like(n, "%1"));
        sq.select(n);
        q.where(cb.exists(sq));
        q.orderBy(cb.asc(a));
        
        assertEquivalence(q, jpql);
    }
    
    public void testEmbeddableQuery6() {
        String jpql = "select a from EntityA_Coll_String a";
        CriteriaQuery<EntityA_Coll_String> q = cb.createQuery(EntityA_Coll_String.class);
        Root<EntityA_Coll_String> a = q.from(EntityA_Coll_String.class);
        q.select(a);
        
        assertEquivalence(q, jpql);
    }

    public void testEmbeddableQuery7() {
        String jpql = "select a.embed from EntityA_Embed_ToOne a ";
        CriteriaQuery<Embed_ToOne> q = cb.createQuery(Embed_ToOne.class);
        Root<EntityA_Embed_ToOne> a = q.from(EntityA_Embed_ToOne.class);
        q.select(a.get(EntityA_Embed_ToOne_.embed));
        
        assertEquivalence(q, jpql);
    }
    
    public void testEmbeddableQuery9() {
        String jpql = "select e from EntityA_Embed_ToOne a " +
                " join a.embed e join e.b b where e.b.id > 0 order by a.id";
        CriteriaQuery<Embed_ToOne> q = cb.createQuery(Embed_ToOne.class);
        Root<EntityA_Embed_ToOne> a = q.from(EntityA_Embed_ToOne.class);
        Join<EntityA_Embed_ToOne, Embed_ToOne> e = a.join(EntityA_Embed_ToOne_.embed);
        Join<Embed_ToOne, EntityB1> b = e.join(Embed_ToOne_.b);
        q.where(cb.gt(b.get(EntityB1_.id), 0));
        q.select(e);
        q.orderBy(cb.asc(a.get(EntityA_Embed_ToOne_.id)));
        
        assertEquivalence(q, jpql);
    }
    
    public void testEmbeddableQuery11() {
        String jpql = "select a.embed from EntityA_Embed_ToOne a ORDER BY a.embed";
        CriteriaQuery<Embed_ToOne> q = cb.createQuery(Embed_ToOne.class);
        Root<EntityA_Embed_ToOne> a = q.from(EntityA_Embed_ToOne.class);
        q.select(a.get(EntityA_Embed_ToOne_.embed));
        q.orderBy(cb.asc(a.get(EntityA_Embed_ToOne_.embed)));
        
        assertEquivalence(q, jpql);
    }
    
    public void testEmbeddableQuery12() {
        String jpql = "select a.embed from EntityA_Embed_ToOne a WHERE a.embed.b IS NOT NULL " +
                " ORDER BY a.embed";
        CriteriaQuery<Embed_ToOne> q = cb.createQuery(Embed_ToOne.class);
        Root<EntityA_Embed_ToOne> a = q.from(EntityA_Embed_ToOne.class);
        q.select(a.get(EntityA_Embed_ToOne_.embed));
        q.where(cb.isNotNull(a.get(EntityA_Embed_ToOne_.embed).get(Embed_ToOne_.b)));
        q.orderBy(cb.asc(a.get(EntityA_Embed_ToOne_.embed)));
        
        assertEquivalence(q, jpql);
    }
    
    public void testEmbeddableQuery13() {
        String jpql = "select a.embed from EntityA_Embed_ToOne a WHERE exists " +
                " (select a from EntityA_Embed_ToOne a where a.embed.b IS NOT NULL) " +
                " ORDER BY a.embed";
        CriteriaQuery<Embed_ToOne> q = cb.createQuery(Embed_ToOne.class);
        Root<EntityA_Embed_ToOne> a = q.from(EntityA_Embed_ToOne.class);
        q.select(a.get(EntityA_Embed_ToOne_.embed));
        Subquery<EntityA_Embed_ToOne> sq = q.subquery(EntityA_Embed_ToOne.class);
        Root<EntityA_Embed_ToOne> a1 = sq.from(EntityA_Embed_ToOne.class);
        Expression n = a1.get(EntityA_Embed_ToOne_.embed).get(Embed_ToOne_.b);
        sq.where(cb.isNotNull(n));
        sq.select(a1);
        q.where(cb.exists(sq));
        q.orderBy(cb.asc(a.get(EntityA_Embed_ToOne_.embed)));
        
        assertEquivalence(q, jpql);
    }

    public void testEmbeddableQuery14() {
        String jpql = "select a.embed from EntityA_Embed_MappedToOne a ";
        CriteriaQuery<Embed_MappedToOne> q = cb.createQuery(Embed_MappedToOne.class);
        Root<EntityA_Embed_MappedToOne> a = q.from(EntityA_Embed_MappedToOne.class);
        q.select(a.get(EntityA_Embed_MappedToOne_.embed));
        
        assertEquivalence(q, jpql);
    }
    
    public void testEmbeddableQuery15() {
        String jpql = "select e from EntityA_Embed_MappedToOne a " +
                " join a.embed e join e.bm bm where e.bm.id > 0 order by a.id";
        CriteriaQuery<Embed_MappedToOne> q = cb.createQuery(Embed_MappedToOne.class);
        Root<EntityA_Embed_MappedToOne> a = q.from(EntityA_Embed_MappedToOne.class);
        Join<EntityA_Embed_MappedToOne,Embed_MappedToOne> e = a.join(EntityA_Embed_MappedToOne_.embed);
        Join<Embed_MappedToOne,EntityB1> bm = e.join(Embed_MappedToOne_.bm);
        q.where(cb.gt(e.get(Embed_MappedToOne_.bm).get(EntityB1_.id), 0));
        q.orderBy(cb.asc(a.get(EntityA_Embed_MappedToOne_.id)));
        q.select(e);
        
        assertEquivalence(q, jpql);
    }
    
    public void testEmbeddableQuery16() {
        String jpql = "select a.embed as e from EntityA_Embed_MappedToOne a ORDER BY e";
        CriteriaQuery<Embed_MappedToOne> q = cb.createQuery(Embed_MappedToOne.class);
        Root<EntityA_Embed_MappedToOne> a = q.from(EntityA_Embed_MappedToOne.class);
        Expression<Embed_MappedToOne> e = a.get(EntityA_Embed_MappedToOne_.embed);
        e.alias("e");
        q.select(e);
        q.orderBy(cb.asc(e));
        
        assertEquivalence(q, jpql);
    }
    
    public void testEmbeddableQuery17() {
        String jpql = "select a.embed from EntityA_Embed_MappedToOne a WHERE a.embed.bm IS NOT NULL";
        CriteriaQuery<Embed_MappedToOne> q = cb.createQuery(Embed_MappedToOne.class);
        Root<EntityA_Embed_MappedToOne> a = q.from(EntityA_Embed_MappedToOne.class);
        q.where(cb.isNotNull(a.get(EntityA_Embed_MappedToOne_.embed).get(Embed_MappedToOne_.bm)));
        q.select(a.get(EntityA_Embed_MappedToOne_.embed));
        
        assertEquivalence(q, jpql);
    }
    
    public void testEmbeddableQuery18() {
        String jpql = "select a.embed from EntityA_Embed_MappedToOne a " +
                " WHERE exists (select a from EntityA_Embed_MappedToOne a " +
                " where a.embed.bm IS NOT NULL)";
        CriteriaQuery<Embed_MappedToOne> q = cb.createQuery(Embed_MappedToOne.class);
        Root<EntityA_Embed_MappedToOne> a = q.from(EntityA_Embed_MappedToOne.class);
        Subquery<EntityA_Embed_MappedToOne> sq = q.subquery(EntityA_Embed_MappedToOne.class);
        Root<EntityA_Embed_MappedToOne> a1 = sq.from(EntityA_Embed_MappedToOne.class);
        sq.select(a1);
        sq.where(cb.isNotNull(a1.get(EntityA_Embed_MappedToOne_.embed).get(Embed_MappedToOne_.bm)));
        q.where(cb.exists(sq));
        q.select(a.get(EntityA_Embed_MappedToOne_.embed));
        
        assertEquivalence(q, jpql);
    }
    
    //jpql does not support comparison of embeddable
    public void testEmbeddableQuery19() {
        String jpql = "select a.embed from EntityA_Embed_MappedToOne a " +
                " WHERE exists (select e from EntityA_Embed_MappedToOne a" +
                " join a.embed e where e.bm IS NOT NULL)";
        CriteriaQuery<Embed_MappedToOne> q = cb.createQuery(Embed_MappedToOne.class);
        Root<EntityA_Embed_MappedToOne> a = q.from(EntityA_Embed_MappedToOne.class);
        Subquery<Embed_MappedToOne> sq = q.subquery(Embed_MappedToOne.class);
        Root<EntityA_Embed_MappedToOne> a1 = sq.from(EntityA_Embed_MappedToOne.class);
        Join<EntityA_Embed_MappedToOne, Embed_MappedToOne> e = a1.join(EntityA_Embed_MappedToOne_.embed);
        sq.select(e);
        sq.where(cb.isNotNull(e.get(Embed_MappedToOne_.bm)));
        q.where(cb.exists(sq));
        q.select(a.get(EntityA_Embed_MappedToOne_.embed));
        
        executeExpectFail(jpql);
        executeExpectFail(q, jpql);
    }
    
    //no support for comparison of embeddable 
    public void testEmbeddableQuery20() {
        String jpql = "select a.embed from EntityA_Embed_MappedToOne a " +
                " WHERE exists (select a.embed from EntityA_Embed_MappedToOne a " +
                " where a.embed.bm IS NOT NULL)";
        CriteriaQuery<Embed_MappedToOne> q = cb.createQuery(Embed_MappedToOne.class);
        Root<EntityA_Embed_MappedToOne> a = q.from(EntityA_Embed_MappedToOne.class);
        Subquery<Embed_MappedToOne> sq = q.subquery(Embed_MappedToOne.class);
        Root<EntityA_Embed_MappedToOne> a1 = sq.from(EntityA_Embed_MappedToOne.class);
        sq.select(a1.get(EntityA_Embed_MappedToOne_.embed));
        sq.where(cb.isNotNull(a1.get(EntityA_Embed_MappedToOne_.embed).get(Embed_MappedToOne_.bm)));
        q.where(cb.exists(sq));
        q.select(a.get(EntityA_Embed_MappedToOne_.embed));
        
        executeExpectFail(q, jpql);
        executeExpectFail(jpql);
    }
    
    public void testEmbeddableQuery21() {
        String jpql = "select a from EntityA_Embed_MappedToOne a";
        CriteriaQuery<EntityA_Embed_MappedToOne> q = cb.createQuery(EntityA_Embed_MappedToOne.class);
        Root<EntityA_Embed_MappedToOne> a = q.from(EntityA_Embed_MappedToOne.class);
        q.select(a);
        
        assertEquivalence(q, jpql);
    }

    public void testEmbeddableQuery22() {
        String jpql = "select e, e.b from EntityA_Coll_Embed_ToOne a, in (a.embed1s) e order by e.name1";
        CriteriaQuery<?> q = cb.createQuery();
        Root<EntityA_Coll_Embed_ToOne> a = q.from(EntityA_Coll_Embed_ToOne.class);
        Join<EntityA_Coll_Embed_ToOne, Embed_ToOne> e = a.join(EntityA_Coll_Embed_ToOne_.embed1s);
        q.orderBy(cb.asc(e.get(Embed_ToOne_.name1)));
        q.multiselect(e, e.get(Embed_ToOne_.b));
        
        assertEquivalence(q, jpql);
    }
    
    public void testEmbeddableQuery23() {
        String jpql = "select e, a.id from EntityA_Coll_Embed_ToOne a, in (a.embed1s) e where e.b.id > 0 " +
            "order by a.id";
        CriteriaQuery<?> q = cb.createQuery();
        Root<EntityA_Coll_Embed_ToOne> a = q.from(EntityA_Coll_Embed_ToOne.class);
        Join<EntityA_Coll_Embed_ToOne, Embed_ToOne> e = a.join(EntityA_Coll_Embed_ToOne_.embed1s);
        q.where(cb.gt(e.get(Embed_ToOne_.b).get(EntityB1_.id), 0));
        q.orderBy(cb.asc(a.get(EntityA_Coll_Embed_ToOne_.id)));
        q.multiselect(e, a.get(EntityA_Coll_Embed_ToOne_.id));
        
        assertEquivalence(q, jpql);
    }
    
    public void testEmbeddableQuery24() {
        String jpql = "select e, e.b.id from EntityA_Coll_Embed_ToOne a " +
                " , in (a.embed1s) e where e.name1 like '%1' order by e.name3";
        CriteriaQuery<?> q = cb.createQuery();
        Root<EntityA_Coll_Embed_ToOne> a = q.from(EntityA_Coll_Embed_ToOne.class);
        Join<EntityA_Coll_Embed_ToOne, Embed_ToOne> e = a.join(EntityA_Coll_Embed_ToOne_.embed1s);
        q.where(cb.like(e.get(Embed_ToOne_.name1), "%1"));
        q.orderBy(cb.asc(e.get(Embed_ToOne_.name3)));
        q.multiselect(e, e.get(Embed_ToOne_.b).get(EntityB1_.id));
        
        assertEquivalence(q, jpql);
    }
    
    public void testEmbeddableQuery25() {
        String jpql = "select e, e.b.id  from EntityA_Coll_Embed_ToOne a " +
                " , in (a.embed1s) e where e.name1 like '%1' order by e";
        CriteriaQuery<?> q = cb.createQuery();
        Root<EntityA_Coll_Embed_ToOne> a = q.from(EntityA_Coll_Embed_ToOne.class);
        Join<EntityA_Coll_Embed_ToOne, Embed_ToOne> e = a.join(EntityA_Coll_Embed_ToOne_.embed1s);
        q.where(cb.like(e.get(Embed_ToOne_.name1), "%1"));
        q.orderBy(cb.asc(e));
        q.multiselect(e, e.get(Embed_ToOne_.b).get(EntityB1_.id));
        
        assertEquivalence(q, jpql);
    }
    
    public void testEmbeddableQuery26() {
        String jpql = "select e, e.b.id  from EntityA_Coll_Embed_ToOne a, " +
                "in (a.embed1s) e where e.name1 like '%1' and a.embed1s IS NOT EMPTY and " +
                " e.b IS NOT NULL order by e";
        CriteriaQuery<?> q = cb.createQuery();
        Root<EntityA_Coll_Embed_ToOne> a = q.from(EntityA_Coll_Embed_ToOne.class);
        Join<EntityA_Coll_Embed_ToOne, Embed_ToOne> e = a.join(EntityA_Coll_Embed_ToOne_.embed1s);
        Predicate p1 = cb.like(e.get(Embed_ToOne_.name1), "%1");
        Predicate p2 = cb.isEmpty(a.get(EntityA_Coll_Embed_ToOne_.embed1s)).not();
        Predicate p3 = cb.isNotNull(e.get(Embed_ToOne_.b));
        q.where(cb.and(cb.and(p1, p2), p3));
        q.orderBy(cb.asc(e));
        q.multiselect(e, e.get(Embed_ToOne_.b).get(EntityB1_.id));
        
        assertEquivalence(q, jpql);
    }
    
    // can not compare embeddable. Expect to fail
    public void testEmbeddableQuery27() {
        String jpql = "select e, e.b.id from EntityA_Coll_Embed_ToOne a " +
                " , in (a.embed1s) e where exists (select a.embed1s from " +
                " EntityA_Coll_Embed_ToOne a) and exists (select e.b from a.embed1s e) " +
                " order by e";
        CriteriaQuery<Integer> q = cb.createQuery(Integer.class);
        Root<EntityA_Coll_Embed_ToOne> a = q.from(EntityA_Coll_Embed_ToOne.class);
        Join<EntityA_Coll_Embed_ToOne, Embed_ToOne> e = a.join(EntityA_Coll_Embed_ToOne_.embed1s);
        Subquery<Set> sq1 = q.subquery(Set.class);
        Root<EntityA_Coll_Embed_ToOne> a1 = sq1.from(EntityA_Coll_Embed_ToOne.class);
        Expression e1 = a1.get(EntityA_Coll_Embed_ToOne_.embed1s);
        sq1.select(e1);

        Subquery<EntityB1> sq2 = q.subquery(EntityB1.class);
        Root<EntityA_Coll_Embed_ToOne> a2 = sq2.correlate(a);
        Join<EntityA_Coll_Embed_ToOne, Embed_ToOne> e2 = a2.join(EntityA_Coll_Embed_ToOne_.embed1s);
        sq2.select(e2.get(Embed_ToOne_.b));
        
        Predicate p1 = cb.exists(sq1);
        Predicate p2 = cb.exists(sq2);
        q.where(cb.and(p1, p2));
        q.orderBy(cb.asc(e));
        q.multiselect(e, e.get(Embed_ToOne_.b).get(EntityB1_.id));
        
        executeExpectFail(q, jpql);
        executeExpectFail(jpql);
    }

    public void testEmbeddableQuery28() {
        String jpql = "select a.embed from EntityA_Embed_ToMany a";
        CriteriaQuery<Embed_ToMany> q = cb.createQuery(Embed_ToMany.class);
        Root<EntityA_Embed_ToMany> a = q.from(EntityA_Embed_ToMany.class);
        q.select(a.get(EntityA_Embed_ToMany_.embed));
        assertEquivalence(q, jpql);
    }
    
    public void testEmbeddableQuery29() {
        String jpql = "select e from EntityA_Embed_ToMany a join a.embed e";
        CriteriaQuery<Embed_ToMany> q = cb.createQuery(Embed_ToMany.class);
        Root<EntityA_Embed_ToMany> a = q.from(EntityA_Embed_ToMany.class);
        Join<EntityA_Embed_ToMany,Embed_ToMany> e = a.join(EntityA_Embed_ToMany_.embed);
        q.select(e);
        assertEquivalence(q, jpql);
    }
    
    public void testEmbeddableQuery30() {
        String jpql = "select b from EntityA_Embed_ToMany a join a.embed.bs b";
        CriteriaQuery<EntityB1> q = cb.createQuery(EntityB1.class);
        Root<EntityA_Embed_ToMany> a = q.from(EntityA_Embed_ToMany.class);
        Join<Embed_ToMany, EntityB1> b = a.join(EntityA_Embed_ToMany_.embed).join(Embed_ToMany_.bs);
        q.select(b);
        assertEquivalence(q, jpql);
    }
    
    public void testEmbeddableQuery31() {
        String jpql = "select e from EntityA_Embed_ToMany a join a.embed e where e.name1 like '%1'";
        CriteriaQuery<Embed_ToMany> q = cb.createQuery(Embed_ToMany.class);
        Root<EntityA_Embed_ToMany> a = q.from(EntityA_Embed_ToMany.class);
        Join<EntityA_Embed_ToMany,Embed_ToMany> e = a.join(EntityA_Embed_ToMany_.embed);
        q.select(e);
        q.where(cb.like(e.get(Embed_ToMany_.name1), "%1"));
        assertEquivalence(q, jpql);
    }
    
    public void testEmbeddableQuery32() {
        String jpql = "select a.embed from EntityA_Embed_ToMany a ORDER BY a.embed";
        CriteriaQuery<Embed_ToMany> q = cb.createQuery(Embed_ToMany.class);
        Root<EntityA_Embed_ToMany> a = q.from(EntityA_Embed_ToMany.class);
        q.select(a.get(EntityA_Embed_ToMany_.embed));
        q.orderBy(cb.asc(a.get(EntityA_Embed_ToMany_.embed)));
        assertEquivalence(q, jpql);
    }
    
    public void testEmbeddableQuery33() {
        String jpql = "select e from EntityA_Embed_ToMany a join a.embed e ORDER BY e";
        CriteriaQuery<Embed_ToMany> q = cb.createQuery(Embed_ToMany.class);
        Root<EntityA_Embed_ToMany> a = q.from(EntityA_Embed_ToMany.class);
        Join<EntityA_Embed_ToMany,Embed_ToMany> e = a.join(EntityA_Embed_ToMany_.embed);
        e.alias("e");
        q.select(e);
        q.orderBy(cb.asc(e));
        assertEquivalence(q, jpql);
    }
    
    public void testEmbeddableQuery34() {
        String jpql = "select b from EntityA_Embed_ToMany a join a.embed.bs b ORDER BY b";
        CriteriaQuery<EntityB1> q = cb.createQuery(EntityB1.class);
        Root<EntityA_Embed_ToMany> a = q.from(EntityA_Embed_ToMany.class);
        ListJoin<Embed_ToMany, EntityB1> b = a.join(EntityA_Embed_ToMany_.embed).join(Embed_ToMany_.bs);
        b.alias("b");
        q.select(b);
        q.orderBy(cb.asc(b));
        assertEquivalence(q, jpql);
    }
    
    public void testEmbeddableQuery35() {
        String jpql = "select e from EntityA_Embed_ToMany a join a.embed e WHERE e.bs IS NOT EMPTY ORDER BY e";
        CriteriaQuery<Embed_ToMany> q = cb.createQuery(Embed_ToMany.class);
        Root<EntityA_Embed_ToMany> a = q.from(EntityA_Embed_ToMany.class);
        Join<EntityA_Embed_ToMany, Embed_ToMany> e = a.join(EntityA_Embed_ToMany_.embed);
        e.alias("e");
        q.select(e);
        q.where(cb.isEmpty(e.get(Embed_ToMany_.bs)).not());
        q.orderBy(cb.asc(e));
        assertEquivalence(q, jpql);
    }
    
    public void testEmbeddableQuery36() {
        String jpql = "select a from EntityA_Embed_ToMany a WHERE exists (select a from EntityA_Embed_ToMany a" +
                " where a.embed.bs IS NOT EMPTY) ORDER BY a";
        CriteriaQuery<EntityA_Embed_ToMany> q = cb.createQuery(EntityA_Embed_ToMany.class);
        Root<EntityA_Embed_ToMany> a = q.from(EntityA_Embed_ToMany.class);
        q.select(a);

        Subquery<EntityA_Embed_ToMany> sq = q.subquery(EntityA_Embed_ToMany.class);
        Root<EntityA_Embed_ToMany> a1 = sq.from(EntityA_Embed_ToMany.class);
        sq.select(a1);
        sq.where(cb.isEmpty(a1.get(EntityA_Embed_ToMany_.embed).get(Embed_ToMany_.bs)).not());
        q.where(cb.exists(sq));
        q.orderBy(cb.asc(a));
        assertEquivalence(q, jpql);
    }
    
    public void testEmbeddableQuery38() {
        String jpql = "select a from EntityA_Embed_ToMany a";
        CriteriaQuery<EntityA_Embed_ToMany> q = cb.createQuery(EntityA_Embed_ToMany.class);
        Root<EntityA_Embed_ToMany> a = q.from(EntityA_Embed_ToMany.class);
        q.select(a);
        assertEquivalence(q, jpql);
    }

    public void testEmbeddableQuery39() {
        String jpql = "select a.embed from EntityA_Embed_Embed_ToMany a";
        CriteriaQuery<Embed_Embed_ToMany> q = cb.createQuery(Embed_Embed_ToMany.class);
        Root<EntityA_Embed_Embed_ToMany> a = q.from(EntityA_Embed_Embed_ToMany.class);
        q.select(a.get(EntityA_Embed_Embed_ToMany_.embed));
        assertEquivalence(q, jpql);
    }
    
    public void testEmbeddableQuery40() {
        String jpql = "select a.embed from EntityA_Embed_Embed_ToMany a where a.embed.embed.name1 like '%1' ";
        CriteriaQuery<Embed_Embed_ToMany> q = cb.createQuery(Embed_Embed_ToMany.class);
        Root<EntityA_Embed_Embed_ToMany> a = q.from(EntityA_Embed_Embed_ToMany.class);
        q.select(a.get(EntityA_Embed_Embed_ToMany_.embed));
        q.where(cb.like(a.get(EntityA_Embed_Embed_ToMany_.embed).get(Embed_Embed_ToMany_.embed).
            get(Embed_ToMany_.name1), "%1"));
        assertEquivalence(q, jpql);
    }
    
    public void testEmbeddableQuery41() {
        String jpql = "select a.embed.embed from EntityA_Embed_Embed_ToMany a";
        CriteriaQuery<Embed_ToMany> q = cb.createQuery(Embed_ToMany.class);
        Root<EntityA_Embed_Embed_ToMany> a = q.from(EntityA_Embed_Embed_ToMany.class);
        q.select(a.get(EntityA_Embed_Embed_ToMany_.embed).get(Embed_Embed_ToMany_.embed));
        assertEquivalence(q, jpql);
    }
    
    public void testEmbeddableQuery42() {
        String jpql = "select b from EntityA_Embed_Embed_ToMany a join a.embed.embed.bs b";
        CriteriaQuery<EntityB1> q = cb.createQuery(EntityB1.class);
        Root<EntityA_Embed_Embed_ToMany> a = q.from(EntityA_Embed_Embed_ToMany.class);
        Join<Embed_ToMany, EntityB1> b = a.join(EntityA_Embed_Embed_ToMany_.embed).join(Embed_Embed_ToMany_.embed).
            join(Embed_ToMany_.bs);
        b.alias("b");
        q.select(b);
        assertEquivalence(q, jpql);
    }
    
    public void testEmbeddableQuery43() {
        String jpql = "select a.embed.embed from EntityA_Embed_Embed_ToMany a where a.embed.embed.name1 like '%1'";
        CriteriaQuery<Embed_ToMany> q = cb.createQuery(Embed_ToMany.class);
        Root<EntityA_Embed_Embed_ToMany> a = q.from(EntityA_Embed_Embed_ToMany.class);
        q.select(a.get(EntityA_Embed_Embed_ToMany_.embed).get(Embed_Embed_ToMany_.embed));
        q.where(cb.like(a.get(EntityA_Embed_Embed_ToMany_.embed).get(Embed_Embed_ToMany_.embed).
            get(Embed_ToMany_.name1), "%1"));
        assertEquivalence(q, jpql);
    }
    
    public void testEmbeddableQuery44() {
        String jpql = "select e2 from EntityA_Embed_Embed_ToMany a left join a.embed e1 left join e1.embed e2";
        CriteriaQuery<Embed_ToMany> q = cb.createQuery(Embed_ToMany.class);
        Root<EntityA_Embed_Embed_ToMany> a = q.from(EntityA_Embed_Embed_ToMany.class);
        Join<EntityA_Embed_Embed_ToMany, Embed_Embed_ToMany> e1 = a.join(EntityA_Embed_Embed_ToMany_.embed, 
            JoinType.LEFT);
        Join<Embed_Embed_ToMany, Embed_ToMany> e2 = e1.join(Embed_Embed_ToMany_.embed, JoinType.LEFT);
        q.select(e2);
        assertEquivalence(q, jpql);
    }
    
    public void testEmbeddableQuery45() {
        String jpql = "select e2 from EntityA_Embed_Embed_ToMany a join a.embed e1 join e1.embed e2";
        CriteriaQuery<Embed_ToMany> q = cb.createQuery(Embed_ToMany.class);
        Root<EntityA_Embed_Embed_ToMany> a = q.from(EntityA_Embed_Embed_ToMany.class);
        Join<EntityA_Embed_Embed_ToMany, Embed_Embed_ToMany> e1 = a.join(EntityA_Embed_Embed_ToMany_.embed);
        Join<Embed_Embed_ToMany, Embed_ToMany> e2 = e1.join(Embed_Embed_ToMany_.embed);
        q.select(e2);
        assertEquivalence(q, jpql);
    }
    
    public void testEmbeddableQuery46() {
        String jpql = "select a.embed as e from EntityA_Embed_Embed_ToMany a ORDER BY e";
        CriteriaQuery<Embed_Embed_ToMany> q = cb.createQuery(Embed_Embed_ToMany.class);
        Root<EntityA_Embed_Embed_ToMany> a = q.from(EntityA_Embed_Embed_ToMany.class);
        Expression<Embed_Embed_ToMany> e = a.get(EntityA_Embed_Embed_ToMany_.embed);
        e.alias("e");
        q.select(e);
        q.orderBy(cb.asc(e));
        assertEquivalence(q, jpql);
    }
    
    public void testEmbeddableQuery47() {
        String jpql = "select a.embed.embed as e from EntityA_Embed_Embed_ToMany a " + 
            "where a.embed.embed.name1 like '%1' ORDER BY e";
        CriteriaQuery<Embed_ToMany> q = cb.createQuery(Embed_ToMany.class);
        Root<EntityA_Embed_Embed_ToMany> a = q.from(EntityA_Embed_Embed_ToMany.class);
        Expression<Embed_ToMany> e = a.get(EntityA_Embed_Embed_ToMany_.embed).get(Embed_Embed_ToMany_.embed);
        e.alias("e");
        q.select(e);
        q.where(cb.like(a.get(EntityA_Embed_Embed_ToMany_.embed).get(Embed_Embed_ToMany_.embed).
            get(Embed_ToMany_.name1), "%1"));
        q.orderBy(cb.asc(e));
        assertEquivalence(q, jpql);
    }
    
    public void testEmbeddableQuery48() {
        String jpql = "select a.embed from EntityA_Embed_Embed_ToMany a " +
                " where a.embed.embed.bs IS NOT EMPTY";
        CriteriaQuery<Embed_Embed_ToMany> q = cb.createQuery(Embed_Embed_ToMany.class);
        Root<EntityA_Embed_Embed_ToMany> a = q.from(EntityA_Embed_Embed_ToMany.class);
        Expression<Embed_Embed_ToMany> e = a.get(EntityA_Embed_Embed_ToMany_.embed);
        q.select(e);
        q.where(cb.isEmpty(a.get(EntityA_Embed_Embed_ToMany_.embed).get(Embed_Embed_ToMany_.embed).
            get(Embed_ToMany_.bs)).not());
        q.orderBy(cb.asc(e));
        assertEquivalence(q, jpql);
    }
    
    public void testEmbeddableQuery49() {
        String jpql = "select a.embed from EntityA_Embed_Embed_ToMany a " +
                " where exists (select a.embed.embed.bs from EntityA_Embed_Embed_ToMany a)";
        CriteriaQuery<Embed_Embed_ToMany> q = cb.createQuery(Embed_Embed_ToMany.class);
        Root<EntityA_Embed_Embed_ToMany> a = q.from(EntityA_Embed_Embed_ToMany.class);
        Expression<Embed_Embed_ToMany> e = a.get(EntityA_Embed_Embed_ToMany_.embed);
        q.select(e);
        Subquery<List> sq = q.subquery(List.class);
        Root<EntityA_Embed_Embed_ToMany> a1 = sq.from(EntityA_Embed_Embed_ToMany.class);
        Expression bs = a1.get(EntityA_Embed_Embed_ToMany_.embed).get(Embed_Embed_ToMany_.embed).
            get(Embed_ToMany_.bs);
        sq.select(bs);
        q.where(cb.exists(sq));
        assertEquivalence(q, jpql);
    }
    
    public void testEmbeddableQuery50() {
        String jpql = "select b from EntityA_Embed_Embed_ToMany a join a.embed.embed.bs b";
        CriteriaQuery<EntityB1> q = cb.createQuery(EntityB1.class);
        Root<EntityA_Embed_Embed_ToMany> a = q.from(EntityA_Embed_Embed_ToMany.class);
        Join<Embed_ToMany, EntityB1> b = a.join(EntityA_Embed_Embed_ToMany_.embed).
            join(Embed_Embed_ToMany_.embed).join(Embed_ToMany_.bs);
        b.alias("b");
        q.select(b);
        assertEquivalence(q, jpql);
    }
    
    public void testEmbeddableQuery51() {
        String jpql = "select a from EntityA_Embed_Embed_ToMany a";
        CriteriaQuery<EntityA_Embed_Embed_ToMany> q = cb.createQuery(EntityA_Embed_Embed_ToMany.class);
        Root<EntityA_Embed_Embed_ToMany> a = q.from(EntityA_Embed_Embed_ToMany.class);
        q.select(a);
        assertEquivalence(q, jpql);
    }

    public void testEmbeddableQuery52() {
        String jpql = "select e, a.id from EntityA_Embed_Coll_Integer a, in (a.embed.otherIntVals) e order by e";
        CriteriaQuery<?> q = cb.createQuery();
        Root<EntityA_Embed_Coll_Integer> a = q.from(EntityA_Embed_Coll_Integer.class);
        Join<Embed_Coll_Integer, Integer> e = a.join(EntityA_Embed_Coll_Integer_.embed).
            join(Embed_Coll_Integer_.otherIntVals);
        e.alias("e");
        q.multiselect(e, a.get(EntityA_Embed_Coll_Integer_.id));
        q.orderBy(cb.asc(e));
        assertEquivalence(q, jpql);
    }
    
    public void testEmbeddableQuery54() {
        String jpql = "select e, a.id from EntityA_Embed_Coll_Integer a, in (a.embed.otherIntVals) e order by a.id";
        CriteriaQuery<?> q = cb.createQuery();
        Root<EntityA_Embed_Coll_Integer> a = q.from(EntityA_Embed_Coll_Integer.class);
        Join<Embed_Coll_Integer, Integer> e = a.join(EntityA_Embed_Coll_Integer_.embed).
            join(Embed_Coll_Integer_.otherIntVals);
        e.alias("e");
        q.multiselect(e, a.get(EntityA_Embed_Coll_Integer_.id));
        q.orderBy(cb.asc(a.get(EntityA_Embed_Coll_Integer_.id)));
        assertEquivalence(q, jpql);
    }
    
    public void testEmbeddableQuery55() {
        String jpql = "select e, a.embed.intVal1 from EntityA_Embed_Coll_Integer a " +
                " , in (a.embed.otherIntVals) e order by a.id";
        CriteriaQuery<?> q = cb.createQuery();
        Root<EntityA_Embed_Coll_Integer> a = q.from(EntityA_Embed_Coll_Integer.class);
        Join<Embed_Coll_Integer, Integer> e = a.join(EntityA_Embed_Coll_Integer_.embed).
            join(Embed_Coll_Integer_.otherIntVals);
        e.alias("e");
        q.multiselect(e, a.get(EntityA_Embed_Coll_Integer_.embed).get(Embed_Coll_Integer_.intVal1));
        q.orderBy(cb.asc(a.get(EntityA_Embed_Coll_Integer_.id)));
        assertEquivalence(q, jpql);
    }
    
    public void testEmbeddableQuery56() {
        String jpql = "select e, a.embed.intVal2 from EntityA_Embed_Coll_Integer a " +
                " , in (a.embed.otherIntVals) e order by e";
        CriteriaQuery<?> q = cb.createQuery();
        Root<EntityA_Embed_Coll_Integer> a = q.from(EntityA_Embed_Coll_Integer.class);
        Join<Embed_Coll_Integer, Integer> e = a.join(EntityA_Embed_Coll_Integer_.embed).
            join(Embed_Coll_Integer_.otherIntVals);
        e.alias("e");
        q.multiselect(e, a.get(EntityA_Embed_Coll_Integer_.embed).get(Embed_Coll_Integer_.intVal2));
        q.orderBy(cb.asc(e));
        assertEquivalence(q, jpql);
    }
    
    public void testEmbeddableQuery57() {
        String jpql1 = "select e, a.embed.intVal2 from EntityA_Embed_Coll_Integer a " +
            " , in (a.embed.otherIntVals) e WHERE a.embed.otherIntVals IS NOT EMPTY order by e";
        
        String jpql = "select e1, a.embed.intVal2 from EntityA_Embed_Coll_Integer a " +
            "JOIN a.embed e JOIN e.otherIntVals e1 WHERE a.embed.otherIntVals IS NOT EMPTY order by e1";
        CriteriaQuery<?> q = cb.createQuery();
        Root<EntityA_Embed_Coll_Integer> a = q.from(EntityA_Embed_Coll_Integer.class);
        Join<Embed_Coll_Integer, Integer> e = a.join(EntityA_Embed_Coll_Integer_.embed).
            join(Embed_Coll_Integer_.otherIntVals);
        e.alias("e");
        q.multiselect(e, a.get(EntityA_Embed_Coll_Integer_.embed).get(Embed_Coll_Integer_.intVal2));
        q.where(cb.isEmpty(a.get(EntityA_Embed_Coll_Integer_.embed).get(Embed_Coll_Integer_.otherIntVals)).not());
        q.orderBy(cb.asc(e));
        assertEquivalence(q, jpql);
    }
    
    public void testEmbeddableQuery58() {
        String jpql = "select e, a0.intVal2 from EntityA_Embed_Coll_Integer a " +
                "JOIN a.embed a0 JOIN a0.otherIntVals e WHERE exists (select a from " +
                " EntityA_Embed_Coll_Integer a JOIN a.embed a0 JOIN a0.otherIntVals e " +
                " where e > 0) order by e";
        CriteriaQuery<?> q = cb.createQuery();
        Root<EntityA_Embed_Coll_Integer> a = q.from(EntityA_Embed_Coll_Integer.class);
        Join<Embed_Coll_Integer, Integer> e = a.join(EntityA_Embed_Coll_Integer_.embed).
            join(Embed_Coll_Integer_.otherIntVals);
        e.alias("e");
        q.multiselect(e, a.get(EntityA_Embed_Coll_Integer_.embed).get(Embed_Coll_Integer_.intVal2));
        Subquery<EntityA_Embed_Coll_Integer> sq = q.subquery(EntityA_Embed_Coll_Integer.class);
        Root<EntityA_Embed_Coll_Integer> a1 = sq.from(EntityA_Embed_Coll_Integer.class);
        Join<Embed_Coll_Integer, Integer> e1 = a1.join(EntityA_Embed_Coll_Integer_.embed).
            join(Embed_Coll_Integer_.otherIntVals);
        sq.where(cb.gt(e1, 0));
        q.where(cb.exists(sq));
        q.orderBy(cb.asc(e));
        assertEquivalence(q, jpql);
    }

    public void testEmbeddableQuery59() {
        String jpql = "select a from EntityA_Embed_Coll_Integer a";
        CriteriaQuery<EntityA_Embed_Coll_Integer> q = cb.createQuery(EntityA_Embed_Coll_Integer.class);
        Root<EntityA_Embed_Coll_Integer> a = q.from(EntityA_Embed_Coll_Integer.class);
        q.select(a);
        assertEquivalence(q, jpql);
    }

    public void testEmbeddableQuery60() {
        String jpql = "select a.embed from EntityA_Embed_Embed a";
        CriteriaQuery<Embed_Embed> q = cb.createQuery(Embed_Embed.class);
        Root<EntityA_Embed_Embed> a = q.from(EntityA_Embed_Embed.class);
        q.select(a.get(EntityA_Embed_Embed_.embed));
        assertEquivalence(q, jpql);
    }
    
    public void testEmbeddableQuery61() {
        String jpql = "select a.embed.embed from EntityA_Embed_Embed a";
        CriteriaQuery<Embed> q = cb.createQuery(Embed.class);
        Root<EntityA_Embed_Embed> a = q.from(EntityA_Embed_Embed.class);
        q.select(a.get(EntityA_Embed_Embed_.embed).get(Embed_Embed_.embed));
        assertEquivalence(q, jpql);
    }
    
    public void testEmbeddableQuery62() {
        String jpql = "select a.embed as e from EntityA_Embed_Embed a ORDER BY e";
        CriteriaQuery<Embed_Embed> q = cb.createQuery(Embed_Embed.class);
        Root<EntityA_Embed_Embed> a = q.from(EntityA_Embed_Embed.class);
        Expression<Embed_Embed> e = a.get(EntityA_Embed_Embed_.embed);
        e.alias("e");
        q.select(e);
        q.orderBy(cb.asc(e));
        assertEquivalence(q, jpql);
    }
    
    //comparison of embed is not support
    @AllowFailure(message="")
    public void testEmbeddableQuery63() {
        String jpql = "select a.embed from EntityA_Embed_Embed a WHERE a.embed.embed IS NOT NULL";
        CriteriaQuery<Embed_Embed> q = cb.createQuery(Embed_Embed.class);
        Root<EntityA_Embed_Embed> a = q.from(EntityA_Embed_Embed.class);
        q.select(a.get(EntityA_Embed_Embed_.embed));
        q.where(cb.isNotNull(a.get(EntityA_Embed_Embed_.embed).get(Embed_Embed_.embed)));
        executeExpectFail(q, jpql);
        executeExpectFail(jpql);
    }
    
    public void testEmbeddableQuery64() {
        String jpql = "select a.embed from EntityA_Embed_Embed a WHERE exists " +
                " (select a.embed.embed from EntityA_Embed_Embed a where a.embed IS NOT NULL) ";
        CriteriaQuery<Embed_Embed> q = cb.createQuery(Embed_Embed.class);
        Root<EntityA_Embed_Embed> a = q.from(EntityA_Embed_Embed.class);
        q.select(a.get(EntityA_Embed_Embed_.embed));
        Subquery<Embed> sq = q.subquery(Embed.class);
        Root<EntityA_Embed_Embed> a1 = sq.from(EntityA_Embed_Embed.class);
        sq.where(cb.isNotNull(a1.get(EntityA_Embed_Embed_.embed)));
        sq.select(a1.get(EntityA_Embed_Embed_.embed).get(Embed_Embed_.embed));
        q.where(cb.exists(sq));
        executeExpectFail(q, jpql);
        executeExpectFail(jpql);    }
    
    public void testEmbeddableQuery65() {
        String jpql = "select a from EntityA_Embed_Embed a";
        CriteriaQuery<EntityA_Embed_Embed> q = cb.createQuery(EntityA_Embed_Embed.class);
        Root<EntityA_Embed_Embed> a = q.from(EntityA_Embed_Embed.class);
        q.select(a);
        assertEquivalence(q, jpql);
    }

    public void testEmbeddableQuery66() {
        String jpql = "select e, e.intVal1, e.embed.intVal2 from " +
                " EntityA_Coll_Embed_Embed a, in (a.embeds) e order by e.intVal3";
        CriteriaQuery<?> q = cb.createQuery();
        Root<EntityA_Coll_Embed_Embed> a = q.from(EntityA_Coll_Embed_Embed.class);
        ListJoin<EntityA_Coll_Embed_Embed, Embed_Embed> e = a.join(EntityA_Coll_Embed_Embed_.embeds);
        q.orderBy(cb.asc(e.get(Embed_Embed_.intVal3)));
        q.multiselect(e, e.get(Embed_Embed_.intVal2), e.get(Embed_Embed_.embed).get(Embed_.intVal2));
        assertEquivalence(q, jpql);
    }
    
    public void testEmbeddableQuery68() {
        String jpql = "select e, a.id from EntityA_Coll_Embed_Embed a, in (a.embeds) e order by a.id";
        CriteriaQuery<?> q = cb.createQuery();
        Root<EntityA_Coll_Embed_Embed> a = q.from(EntityA_Coll_Embed_Embed.class);
        ListJoin<EntityA_Coll_Embed_Embed, Embed_Embed> e = a.join(EntityA_Coll_Embed_Embed_.embeds);
        q.orderBy(cb.asc(a.get(EntityA_Coll_Embed_Embed_.id)));
        q.multiselect(e, a.get(EntityA_Coll_Embed_Embed_.id));
        assertEquivalence(q, jpql);
    }
    
    public void testEmbeddableQuery69() {
        String jpql = "select e, a.id from EntityA_Coll_Embed_Embed a, in (a.embeds) e order by e desc";
        CriteriaQuery<?> q = cb.createQuery();
        Root<EntityA_Coll_Embed_Embed> a = q.from(EntityA_Coll_Embed_Embed.class);
        ListJoin<EntityA_Coll_Embed_Embed, Embed_Embed> e = a.join(EntityA_Coll_Embed_Embed_.embeds);
        e.alias("e");
        q.orderBy(cb.desc(e));
        q.multiselect(e, a.get(EntityA_Coll_Embed_Embed_.id));
        assertEquivalence(q, jpql);
    }
    
    public void testEmbeddableQuery70() {
        String jpql = "select e, e.intVal1, e.embed.intVal2 from " +
                " EntityA_Coll_Embed_Embed a, in (a.embeds) e WHERE a.embeds IS NOT EMPTY " +
                " order by e.intVal3";
        CriteriaQuery<?> q = cb.createQuery();
        Root<EntityA_Coll_Embed_Embed> a = q.from(EntityA_Coll_Embed_Embed.class);
        ListJoin<EntityA_Coll_Embed_Embed, Embed_Embed> e = a.join(EntityA_Coll_Embed_Embed_.embeds);
        q.orderBy(cb.asc(e.get(Embed_Embed_.intVal3)));
        q.multiselect(e, e.get(Embed_Embed_.intVal1), e.get(Embed_Embed_.embed).get(Embed_.intVal2));
        q.where(cb.isEmpty(a.get(EntityA_Coll_Embed_Embed_.embeds)).not());
        assertEquivalence(q, jpql);
    }
    
    // comparison of embeddable is not supported
    public void testEmbeddableQuery71() {
        String jpql = "select e, e.intVal1, e.embed.intVal2 from " +
                " EntityA_Coll_Embed_Embed a, in (a.embeds) e WHERE exists (select a.embeds " +
                " from EntityA_Coll_Embed_Embed a) order by e.intVal3";
            
        CriteriaQuery<?> q = cb.createQuery();
        Root<EntityA_Coll_Embed_Embed> a = q.from(EntityA_Coll_Embed_Embed.class);
        ListJoin<EntityA_Coll_Embed_Embed, Embed_Embed> e = a.join(EntityA_Coll_Embed_Embed_.embeds);
        q.orderBy(cb.asc(e.get(Embed_Embed_.intVal3)));
        q.multiselect(e, e.get(Embed_Embed_.intVal1), e.get(Embed_Embed_.embed).get(Embed_.intVal2));
        Subquery<List> sq = q.subquery(List.class);
        Root<EntityA_Coll_Embed_Embed> a1 = sq.from(EntityA_Coll_Embed_Embed.class);
        Expression e2 = a1.get(EntityA_Coll_Embed_Embed_.embeds);
        sq.select(e2);
        q.where(cb.exists(sq));
        executeExpectFail(q, jpql);
        executeExpectFail(jpql);
    }
    
    public void testEmbeddableQuery72() {
        String jpql = "select e, e.intVal1, e.embed.intVal2 from " +
                " EntityA_Coll_Embed_Embed a, in (a.embeds) e WHERE e.intVal1 < ANY (select e2.intVal2 " +
                " from EntityA_Coll_Embed_Embed a1, in (a1.embeds) e2) " +
                " order by e.intVal3";
        CriteriaQuery<?> q = cb.createQuery();
        Root<EntityA_Coll_Embed_Embed> a = q.from(EntityA_Coll_Embed_Embed.class);
        ListJoin<EntityA_Coll_Embed_Embed, Embed_Embed> e = a.join(EntityA_Coll_Embed_Embed_.embeds);
        q.orderBy(cb.asc(e.get(Embed_Embed_.intVal3)));
        q.multiselect(e, e.get(Embed_Embed_.intVal1), e.get(Embed_Embed_.embed).get(Embed_.intVal2));
        Subquery<Integer> sq = q.subquery(Integer.class);
        Root<EntityA_Coll_Embed_Embed> a1 = sq.from(EntityA_Coll_Embed_Embed.class);
        Join<EntityA_Coll_Embed_Embed, Embed_Embed> e2 = a1.join(EntityA_Coll_Embed_Embed_.embeds);
        sq.select(e2.get(Embed_Embed_.intVal2));
        q.where(cb.lt(e.get(Embed_Embed_.intVal1), cb.any(sq)));
        assertEquivalence(q, jpql);
    }
    
    public void testEmbeddableQuery73() {
        String jpql = "select e, e.intVal1, e.embed.intVal2 from " +
                " EntityA_Coll_Embed_Embed a, in (a.embeds) e WHERE e.intVal1 < ALL (select e2.intVal2 " +
                " from EntityA_Coll_Embed_Embed a1, in (a1.embeds) e2) " +
                " order by e.intVal3";
        CriteriaQuery<?> q = cb.createQuery();
        Root<EntityA_Coll_Embed_Embed> a = q.from(EntityA_Coll_Embed_Embed.class);
        ListJoin<EntityA_Coll_Embed_Embed, Embed_Embed> e = a.join(EntityA_Coll_Embed_Embed_.embeds);
        q.orderBy(cb.asc(e.get(Embed_Embed_.intVal3)));
        q.multiselect(e, e.get(Embed_Embed_.intVal1), e.get(Embed_Embed_.embed).get(Embed_.intVal2));
        Subquery<Integer> sq = q.subquery(Integer.class);
        Root<EntityA_Coll_Embed_Embed> a1 = sq.from(EntityA_Coll_Embed_Embed.class);
        Join<EntityA_Coll_Embed_Embed, Embed_Embed> e2 = a1.join(EntityA_Coll_Embed_Embed_.embeds);
        sq.select(e2.get(Embed_Embed_.intVal2));
        q.where(cb.lt(e.get(Embed_Embed_.intVal1), cb.all(sq)));
        assertEquivalence(q, jpql);
    }
    
    public void testEmbeddableQuery74() {
        String jpql = "select e, e.intVal1, e.embed.intVal2 from " +
                " EntityA_Coll_Embed_Embed a, in (a.embeds) e WHERE e.intVal1 <= SOME " +
                " (select e2.intVal2 from EntityA_Coll_Embed_Embed a1, in (a1.embeds) e2) " +
                " order by e.intVal3";
        CriteriaQuery<?> q = cb.createQuery();
        Root<EntityA_Coll_Embed_Embed> a = q.from(EntityA_Coll_Embed_Embed.class);
        ListJoin<EntityA_Coll_Embed_Embed, Embed_Embed> e = a.join(EntityA_Coll_Embed_Embed_.embeds);
        q.orderBy(cb.asc(e.get(Embed_Embed_.intVal3)));
        q.multiselect(e, e.get(Embed_Embed_.intVal1), e.get(Embed_Embed_.embed).get(Embed_.intVal2));
        Subquery<Integer> sq = q.subquery(Integer.class);
        Root<EntityA_Coll_Embed_Embed> a1 = sq.from(EntityA_Coll_Embed_Embed.class);
        Join<EntityA_Coll_Embed_Embed, Embed_Embed> e2 = a1.join(EntityA_Coll_Embed_Embed_.embeds);
        sq.select(e2.get(Embed_Embed_.intVal2));
        q.where(cb.le(e.get(Embed_Embed_.intVal1), cb.some(sq)));
        assertEquivalence(q, jpql);
    }
    
    public void testEmbeddableQuery75() {
        String jpql = "select e, e.intVal1, e.embed.intVal2 from " +
                " EntityA_Coll_Embed_Embed a, in (a.embeds) e WHERE e.intVal1 > ALL (select e2.intVal2 " +
                " from EntityA_Coll_Embed_Embed a1, in (a1.embeds) e2) order by e.intVal3";
        CriteriaQuery<?> q = cb.createQuery();
        Root<EntityA_Coll_Embed_Embed> a = q.from(EntityA_Coll_Embed_Embed.class);
        ListJoin<EntityA_Coll_Embed_Embed, Embed_Embed> e = a.join(EntityA_Coll_Embed_Embed_.embeds);
        q.orderBy(cb.asc(e.get(Embed_Embed_.intVal3)));
        q.multiselect(e, e.get(Embed_Embed_.intVal1), e.get(Embed_Embed_.embed).get(Embed_.intVal2));
        Subquery<Integer> sq = q.subquery(Integer.class);
        Root<EntityA_Coll_Embed_Embed> a1 = sq.from(EntityA_Coll_Embed_Embed.class);
        Join<EntityA_Coll_Embed_Embed, Embed_Embed> e2 = a1.join(EntityA_Coll_Embed_Embed_.embeds);
        sq.select(e2.get(Embed_Embed_.intVal2));
        q.where(cb.gt(e.get(Embed_Embed_.intVal1), cb.all(sq)));
        assertEquivalence(q, jpql);
    }

    public void testEmbeddableQuery76() {
        String jpql = "select e, e.intVal1, e.embed.intVal2 from EntityA_Coll_Embed_Embed a " +
                " , in (a.embeds) e WHERE e.intVal1 < ANY (select e.intVal2 " +
                " from EntityA_Coll_Embed_Embed a, in (a.embeds) e) order by e.intVal3";

        String expectedSQL = "SELECT t1.IntVal1x, t1.IntVal2x, t1.IntVal3x, t1.intVal1, t1.intVal2, t1.intVal3 " + 
        "FROM TBL1A t0 INNER JOIN TBL1A_embeds t1 ON t0.id = t1.ENTITYA_COLL_EMBED_EMBED_ID WHERE " + 
        "(t1.intVal1 < ANY (" + 
        "SELECT t3.intVal2 FROM TBL1A t2 " +
        "INNER JOIN TBL1A_embeds t3 ON t2.id = t3.ENTITYA_COLL_EMBED_EMBED_ID)) " + 
        "ORDER BY t1.intVal3 ASC";

        CriteriaQuery<?> q = cb.createQuery();
        Root<EntityA_Coll_Embed_Embed> a = q.from(EntityA_Coll_Embed_Embed.class);
        ListJoin<EntityA_Coll_Embed_Embed, Embed_Embed> e = a.join(EntityA_Coll_Embed_Embed_.embeds);
        q.orderBy(cb.asc(e.get(Embed_Embed_.intVal3)));
        q.multiselect(e, e.get(Embed_Embed_.intVal1), e.get(Embed_Embed_.embed).get(Embed_.intVal2));
        Subquery<Integer> sq = q.subquery(Integer.class);
        Root<EntityA_Coll_Embed_Embed> a1 = sq.from(EntityA_Coll_Embed_Embed.class);
        Join<EntityA_Coll_Embed_Embed, Embed_Embed> e2 = a1.join(EntityA_Coll_Embed_Embed_.embeds);
        sq.select(e2.get(Embed_Embed_.intVal2));
        q.where(cb.lt(e.get(Embed_Embed_.intVal1), cb.any(sq)));
        
        assertEquivalence(q, jpql, expectedSQL);
    }

    public void testEmbeddableQuery77() {
        String jpql = "select e, e.intVal1, e.embed.intVal2 from EntityA_Coll_Embed_Embed a " +
                " , in (a.embeds) e WHERE e.intVal1 < ALL (select e.intVal2 " +
                " from EntityA_Coll_Embed_Embed a, in (a.embeds) e) order by e.intVal3";

        String expectedSQL = "SELECT t1.IntVal1x, t1.IntVal2x, t1.IntVal3x, t1.intVal1, t1.intVal2, t1.intVal3 " + 
        "FROM TBL1A t0 INNER JOIN TBL1A_embeds t1 ON t0.id = t1.ENTITYA_COLL_EMBED_EMBED_ID " +
        "WHERE (t1.intVal1 < ALL (" + 
        "SELECT t3.intVal2 FROM TBL1A t2 INNER JOIN TBL1A_embeds t3 " +
        "ON t2.id = t3.ENTITYA_COLL_EMBED_EMBED_ID)) " + 
        "ORDER BY t1.intVal3 ASC";

        CriteriaQuery<?> q = cb.createQuery();
        Root<EntityA_Coll_Embed_Embed> a = q.from(EntityA_Coll_Embed_Embed.class);
        ListJoin<EntityA_Coll_Embed_Embed, Embed_Embed> e = a.join(EntityA_Coll_Embed_Embed_.embeds);
        q.orderBy(cb.asc(e.get(Embed_Embed_.intVal3)));
        q.multiselect(e, e.get(Embed_Embed_.intVal1), e.get(Embed_Embed_.embed).get(Embed_.intVal2));
        Subquery<Integer> sq = q.subquery(Integer.class);
        Root<EntityA_Coll_Embed_Embed> a1 = sq.from(EntityA_Coll_Embed_Embed.class);
        Join<EntityA_Coll_Embed_Embed, Embed_Embed> e2 = a1.join(EntityA_Coll_Embed_Embed_.embeds);
        sq.select(e2.get(Embed_Embed_.intVal2));
        q.where(cb.lt(e.get(Embed_Embed_.intVal1), cb.all(sq)));
        assertEquivalence(q, jpql, expectedSQL);
    }
    
    public void testEmbeddableQuery78() {
        String jpql = "select e, e.intVal1, e.embed.intVal2 from " +
                " EntityA_Coll_Embed_Embed a, in (a.embeds) e WHERE e.intVal1 <= SOME " +
                " (select e.intVal2 from EntityA_Coll_Embed_Embed a, in (a.embeds) e) " +
                " order by e.intVal3";
        String expectedSQL = "SELECT t1.IntVal1x, t1.IntVal2x, t1.IntVal3x, t1.intVal1, t1.intVal2, t1.intVal3 " + 
        "FROM TBL1A t0 INNER JOIN TBL1A_embeds t1 ON t0.id = t1.ENTITYA_COLL_EMBED_EMBED_ID " +
        "WHERE (t1.intVal1 <= ANY (" + 
        "SELECT t3.intVal2 FROM TBL1A t2 INNER JOIN TBL1A_embeds t3 ON t2.id = t3.ENTITYA_COLL_EMBED_EMBED_ID)) " + 
        "ORDER BY t1.intVal3 ASC";
        CriteriaQuery<?> q = cb.createQuery();
        Root<EntityA_Coll_Embed_Embed> a = q.from(EntityA_Coll_Embed_Embed.class);
        ListJoin<EntityA_Coll_Embed_Embed, Embed_Embed> e = a.join(EntityA_Coll_Embed_Embed_.embeds);
        q.orderBy(cb.asc(e.get(Embed_Embed_.intVal3)));
        q.multiselect(e, e.get(Embed_Embed_.intVal1), e.get(Embed_Embed_.embed).get(Embed_.intVal2));
        Subquery<Integer> sq = q.subquery(Integer.class);
        Root<EntityA_Coll_Embed_Embed> a1 = sq.from(EntityA_Coll_Embed_Embed.class);
        Join<EntityA_Coll_Embed_Embed, Embed_Embed> e2 = a1.join(EntityA_Coll_Embed_Embed_.embeds);
        sq.select(e2.get(Embed_Embed_.intVal2));
        q.where(cb.le(e.get(Embed_Embed_.intVal1), cb.some(sq)));
        assertEquivalence(q, jpql, expectedSQL);
    }
    
    public void testEmbeddableQuery79() {
        String jpql = "select e, e.intVal1, e.embed.intVal2 from EntityA_Coll_Embed_Embed a " +
                " , in (a.embeds) e WHERE e.intVal1 > ALL (select e.intVal2 " +
                " from EntityA_Coll_Embed_Embed a, in (a.embeds) e) order by e.intVal3";
        String expectedSQL = "SELECT t1.IntVal1x, t1.IntVal2x, t1.IntVal3x, t1.intVal1, t1.intVal2, t1.intVal3 " + 
            "FROM TBL1A t0 INNER JOIN TBL1A_embeds t1 ON t0.id = t1.ENTITYA_COLL_EMBED_EMBED_ID " +
            "WHERE (t1.intVal1 > ALL (" + 
            "SELECT t3.intVal2 FROM TBL1A t2 INNER JOIN TBL1A_embeds t3 ON t2.id = t3.ENTITYA_COLL_EMBED_EMBED_ID)) " + 
            "ORDER BY t1.intVal3 ASC";
        CriteriaQuery<?> q = cb.createQuery();
        Root<EntityA_Coll_Embed_Embed> a = q.from(EntityA_Coll_Embed_Embed.class);
        ListJoin<EntityA_Coll_Embed_Embed, Embed_Embed> e = a.join(EntityA_Coll_Embed_Embed_.embeds);
        q.orderBy(cb.asc(e.get(Embed_Embed_.intVal3)));
        q.multiselect(e, e.get(Embed_Embed_.intVal1), e.get(Embed_Embed_.embed).get(Embed_.intVal2));
        Subquery<Integer> sq = q.subquery(Integer.class);
        Root<EntityA_Coll_Embed_Embed> a1 = sq.from(EntityA_Coll_Embed_Embed.class);
        Join<EntityA_Coll_Embed_Embed, Embed_Embed> e2 = a1.join(EntityA_Coll_Embed_Embed_.embeds);
        sq.select(e2.get(Embed_Embed_.intVal2));
        q.where(cb.gt(e.get(Embed_Embed_.intVal1), cb.all(sq)));
        assertEquivalence(q, jpql, expectedSQL);
    }
    
    public void testEmbeddableQuery80() {
        String jpql = "select e, e.intVal1, e.embed.intVal2 from EntityA_Coll_Embed_Embed a " +
                " , in (a.embeds) e WHERE e.intVal1 < ANY (select e2.intVal2 " +
                " from in(a.embeds) e2) order by e.intVal3";
        String expectedSQL = "SELECT t1.IntVal1x, t1.IntVal2x, t1.IntVal3x, t1.intVal1, t1.intVal2, t1.intVal3 " + 
        "FROM TBL1A t0 INNER JOIN TBL1A_embeds t1 ON t0.id = t1.ENTITYA_COLL_EMBED_EMBED_ID WHERE " + 
        "(t1.intVal1 < ANY (SELECT t2.intVal2 FROM TBL1A_embeds t2 " + 
        "WHERE (t0.id = t2.ENTITYA_COLL_EMBED_EMBED_ID))) ORDER BY t1.intVal3 ASC";

        CriteriaQuery<?> q = cb.createQuery();
        Root<EntityA_Coll_Embed_Embed> a = q.from(EntityA_Coll_Embed_Embed.class);
        ListJoin<EntityA_Coll_Embed_Embed, Embed_Embed> e = a.join(EntityA_Coll_Embed_Embed_.embeds);
        q.orderBy(cb.asc(e.get(Embed_Embed_.intVal3)));
        q.multiselect(e, e.get(Embed_Embed_.intVal1), e.get(Embed_Embed_.embed).get(Embed_.intVal2));
        Subquery<Integer> sq = q.subquery(Integer.class);
        Root<EntityA_Coll_Embed_Embed> a1 = sq.correlate(a);
        Join<EntityA_Coll_Embed_Embed, Embed_Embed> e2 = a1.join(EntityA_Coll_Embed_Embed_.embeds);
        sq.select(e2.get(Embed_Embed_.intVal2));
        q.where(cb.lt(e.get(Embed_Embed_.intVal1), cb.any(sq)));
        assertEquivalence(q, jpql, expectedSQL);
    }
    
    public void testEmbeddableQuery81() {
        String jpql = "select e, e.intVal1, e.embed.intVal2 from EntityA_Coll_Embed_Embed a " +
                " , in (a.embeds) e WHERE e.intVal1 < ALL (select e2.intVal2 " +
                " from a.embeds e2) order by e.intVal3";
        String expectedSQL = "SELECT t1.IntVal1x, t1.IntVal2x, t1.IntVal3x, t1.intVal1, t1.intVal2, t1.intVal3 " + 
        "FROM TBL1A t0 INNER JOIN TBL1A_embeds t1 ON t0.id = t1.ENTITYA_COLL_EMBED_EMBED_ID WHERE " + 
        "(t1.intVal1 < ALL (SELECT t2.intVal2 FROM TBL1A_embeds t2 " + 
        "WHERE (t0.id = t2.ENTITYA_COLL_EMBED_EMBED_ID))) " + 
        "ORDER BY t1.intVal3 ASC";

        CriteriaQuery<?> q = cb.createQuery();
        Root<EntityA_Coll_Embed_Embed> a = q.from(EntityA_Coll_Embed_Embed.class);
        ListJoin<EntityA_Coll_Embed_Embed, Embed_Embed> e = a.join(EntityA_Coll_Embed_Embed_.embeds);
        q.orderBy(cb.asc(e.get(Embed_Embed_.intVal3)));
        q.multiselect(e, e.get(Embed_Embed_.intVal1), e.get(Embed_Embed_.embed).get(Embed_.intVal2));
        Subquery<Integer> sq = q.subquery(Integer.class);
        Root<EntityA_Coll_Embed_Embed> a1 = sq.correlate(a);
        Join<EntityA_Coll_Embed_Embed, Embed_Embed> e2 = a1.join(EntityA_Coll_Embed_Embed_.embeds);
        sq.select(e2.get(Embed_Embed_.intVal2));
        q.where(cb.lt(e.get(Embed_Embed_.intVal1), cb.all(sq)));
        assertEquivalence(q, jpql, expectedSQL);
    }
    
    public void testEmbeddableQuery82() {
        String jpql = "select e, e.intVal1, e.embed.intVal2 from EntityA_Coll_Embed_Embed a " +
                " , in (a.embeds) e WHERE e.intVal1 <= SOME (select e2.intVal2 " +
                " from in(a.embeds) e2) order by e.intVal3";
        String expectedSQL = "SELECT t1.IntVal1x, t1.IntVal2x, t1.IntVal3x, t1.intVal1, t1.intVal2, t1.intVal3 " + 
        "FROM TBL1A t0 INNER JOIN TBL1A_embeds t1 ON t0.id = t1.ENTITYA_COLL_EMBED_EMBED_ID WHERE " + 
        "(t1.intVal1 <= ANY (SELECT t2.intVal2 FROM TBL1A_embeds t2 " + 
        "WHERE (t0.id = t2.ENTITYA_COLL_EMBED_EMBED_ID))) " + 
        "ORDER BY t1.intVal3 ASC";

        CriteriaQuery<?> q = cb.createQuery();
        Root<EntityA_Coll_Embed_Embed> a = q.from(EntityA_Coll_Embed_Embed.class);
        ListJoin<EntityA_Coll_Embed_Embed, Embed_Embed> e = a.join(EntityA_Coll_Embed_Embed_.embeds);
        q.orderBy(cb.asc(e.get(Embed_Embed_.intVal3)));
        q.multiselect(e, e.get(Embed_Embed_.intVal1), e.get(Embed_Embed_.embed).get(Embed_.intVal2));
        Subquery<Integer> sq = q.subquery(Integer.class);
        Root<EntityA_Coll_Embed_Embed> a1 = sq.correlate(a);
        Join<EntityA_Coll_Embed_Embed, Embed_Embed> e2 = a1.join(EntityA_Coll_Embed_Embed_.embeds);
        sq.select(e2.get(Embed_Embed_.intVal2));
        q.where(cb.le(e.get(Embed_Embed_.intVal1), cb.some(sq)));
        assertEquivalence(q, jpql, expectedSQL);
    }
    
    public void testEmbeddableQuery83() {
        String jpql = "select e, e.intVal1, e.embed.intVal2 from EntityA_Coll_Embed_Embed a " +
                " , in (a.embeds) e WHERE e.intVal1 > ALL (select e2.intVal2 " +
                " from a.embeds e2) order by e.intVal3";
        String expectedSQL = "SELECT t1.IntVal1x, t1.IntVal2x, t1.IntVal3x, t1.intVal1, t1.intVal2, t1.intVal3 " + 
        "FROM TBL1A t0 INNER JOIN TBL1A_embeds t1 ON t0.id = t1.ENTITYA_COLL_EMBED_EMBED_ID WHERE " + 
        "(t1.intVal1 > ALL (SELECT t2.intVal2 FROM TBL1A_embeds t2 " + 
        "WHERE (t0.id = t2.ENTITYA_COLL_EMBED_EMBED_ID))) " + 
        "ORDER BY t1.intVal3 ASC";

        CriteriaQuery<?> q = cb.createQuery();
        Root<EntityA_Coll_Embed_Embed> a = q.from(EntityA_Coll_Embed_Embed.class);
        ListJoin<EntityA_Coll_Embed_Embed, Embed_Embed> e = a.join(EntityA_Coll_Embed_Embed_.embeds);
        q.orderBy(cb.asc(e.get(Embed_Embed_.intVal3)));
        q.multiselect(e, e.get(Embed_Embed_.intVal1), e.get(Embed_Embed_.embed).get(Embed_.intVal2));
        Subquery<Integer> sq = q.subquery(Integer.class);
        Root<EntityA_Coll_Embed_Embed> a1 = sq.correlate(a);
        Join<EntityA_Coll_Embed_Embed, Embed_Embed> e2 = a1.join(EntityA_Coll_Embed_Embed_.embeds);
        sq.select(e2.get(Embed_Embed_.intVal2));
        q.where(cb.gt(e.get(Embed_Embed_.intVal1), cb.all(sq)));
        assertEquivalence(q, jpql, expectedSQL);
    }
    
    @AllowFailure(message="JPQL parse error")
    public void testEmbeddableQuery84() {
        String jpql = "select e, e.intVal1, e.embed.intVal2 from EntityA_Coll_Embed_Embed a, " +
                " in (a.embeds) e WHERE :embed2 MEMBER OF a.embeds order by e.intVal3";
        CriteriaQuery<Tuple> q = cb.createTupleQuery();
        Root<EntityA_Coll_Embed_Embed> a = q.from(EntityA_Coll_Embed_Embed.class);
        ListJoin<EntityA_Coll_Embed_Embed, Embed_Embed> e = a.join(EntityA_Coll_Embed_Embed_.embeds);
        q.orderBy(cb.asc(e.get(Embed_Embed_.intVal3)));
        q.multiselect(e, e.get(Embed_Embed_.intVal1), e.get(Embed_Embed_.embed).get(Embed_.intVal2));
        ParameterExpression<Embed_Embed> param1 = cb.parameter(Embed_Embed.class, "embed2");
        q.where(cb.isMember(param1, a.get(EntityA_Coll_Embed_Embed_.embeds)));
        assertEquivalence(new QueryDecorator() {
            public void decorate(Query q) {
                q.setParameter("embed2", new Embed_Embed());
            }
        }, q, jpql);
    }
    
    @AllowFailure(message="JPQL parse error")
    public void testEmbeddableQuery85() {
        String jpql = "select e, e.intVal1, e.embed.intVal2 from EntityA_Coll_Embed_Embed a " +
                " left join a.embeds e WHERE :embed2 MEMBER OF a.embeds order by e.intVal3";
        CriteriaQuery<?> q = cb.createQuery();
        Root<EntityA_Coll_Embed_Embed> a = q.from(EntityA_Coll_Embed_Embed.class);
        ListJoin<EntityA_Coll_Embed_Embed, Embed_Embed> e = a.join(EntityA_Coll_Embed_Embed_.embeds, JoinType.LEFT);
        q.orderBy(cb.asc(e.get(Embed_Embed_.intVal3)));
        q.multiselect(e, e.get(Embed_Embed_.intVal1), e.get(Embed_Embed_.embed).get(Embed_.intVal2));
        ParameterExpression<Embed_Embed> param1 = cb.parameter(Embed_Embed.class, "embed2");
        q.where(cb.isMember(param1, a.get(EntityA_Coll_Embed_Embed_.embeds)));
        
        assertEquivalence(new QueryDecorator() {
            public void decorate(Query q) {
                q.setParameter("embed2", new Embed_Embed());
            }
        }, q, jpql);
    }
    
    @AllowFailure(message="JPQL parse error")
    public void testEmbeddableQuery86() {
        String jpql = "select e, e.intVal1, e.embed.intVal2 from EntityA_Coll_Embed_Embed a " +
                " , in (a.embeds) e WHERE ?1 = e order by e.intVal3";
        CriteriaQuery<?> q = cb.createQuery();
        Root<EntityA_Coll_Embed_Embed> a = q.from(EntityA_Coll_Embed_Embed.class);
        ListJoin<EntityA_Coll_Embed_Embed, Embed_Embed> e = a.join(EntityA_Coll_Embed_Embed_.embeds);
        q.orderBy(cb.asc(e.get(Embed_Embed_.intVal3)));
        q.multiselect(e, e.get(Embed_Embed_.intVal1), e.get(Embed_Embed_.embed).get(Embed_.intVal2));
        Parameter<Embed_Embed> param1 = cb.parameter(Embed_Embed.class);
        //q.where(cb.equal(param1, e));
        assertEquivalence(new QueryDecorator() {
            public void decorate(Query q) {
                q.setParameter("embed2", new Embed_Embed());
            }
        }, q, jpql);
    }

    public void testEmbeddableQuery87() {
        String jpql = "select a from EntityA_Coll_Embed_Embed a";
        CriteriaQuery<EntityA_Coll_Embed_Embed> q = cb.createQuery(EntityA_Coll_Embed_Embed.class);
        Root<EntityA_Coll_Embed_Embed> a = q.from(EntityA_Coll_Embed_Embed.class);
        q.select(a);
        assertEquivalence(q, jpql);
    }

    public void testEmbeddableQuery88() {
        String jpql = "select e, e.intVal1, e.intVal2 from EntityA_Embed_Coll_Embed a " +
                " , in (a.embed.embeds) e order by e";
        CriteriaQuery<?> q = cb.createQuery();
        Root<EntityA_Embed_Coll_Embed> a = q.from(EntityA_Embed_Coll_Embed.class);
        ListJoin<Embed_Coll_Embed, Embed> e = a.join(EntityA_Embed_Coll_Embed_.embed).join(Embed_Coll_Embed_.embeds);
        q.orderBy(cb.asc(e));
        q.multiselect(e, e.get(Embed_.intVal1), e.get(Embed_.intVal2));
        assertEquivalence(q, jpql);
    }
    
    public void testEmbeddableQuery89() {
        String jpql = "select e, e.intVal1 from EntityA_Embed_Coll_Embed a, in (a.embed.embeds) e order by e.intVal3";
        CriteriaQuery<?> q = cb.createQuery();
        Root<EntityA_Embed_Coll_Embed> a = q.from(EntityA_Embed_Coll_Embed.class);
        ListJoin<Embed_Coll_Embed, Embed> e = a.join(EntityA_Embed_Coll_Embed_.embed).join(Embed_Coll_Embed_.embeds);
        q.orderBy(cb.asc(e.get(Embed_.intVal3)));
        q.multiselect(e, e.get(Embed_.intVal1));
        assertEquivalence(q, jpql);
    }
    
    public void testEmbeddableQuery90() {
        String jpql = "select e, a.id from EntityA_Embed_Coll_Embed a, in (a.embed.embeds) e order by a.id";
        CriteriaQuery<?> q = cb.createQuery();
        Root<EntityA_Embed_Coll_Embed> a = q.from(EntityA_Embed_Coll_Embed.class);
        ListJoin<Embed_Coll_Embed, Embed> e = a.join(EntityA_Embed_Coll_Embed_.embed).join(Embed_Coll_Embed_.embeds);
        q.orderBy(cb.asc(a.get(EntityA_Embed_Coll_Embed_.id)));
        q.multiselect(e, e.get(Embed_.intVal1), a.get(EntityA_Embed_Coll_Embed_.id));
        assertEquivalence(q, jpql);
    }
    
    public void testEmbeddableQuery91() {
        String jpql = "select e, e.intVal1, e.intVal2 from EntityA_Embed_Coll_Embed a " +
                " , in (a.embed.embeds) e order by e.intVal3";
        CriteriaQuery<?> q = cb.createQuery();
        Root<EntityA_Embed_Coll_Embed> a = q.from(EntityA_Embed_Coll_Embed.class);
        ListJoin<Embed_Coll_Embed, Embed> e = a.join(EntityA_Embed_Coll_Embed_.embed).join(Embed_Coll_Embed_.embeds);
        q.orderBy(cb.asc(e.get(Embed_.intVal3)));
        q.multiselect(e, e.get(Embed_.intVal1), e.get(Embed_.intVal2));
        assertEquivalence(q, jpql);
    }
    
    public void testEmbeddableQuery92() {
        String jpql1 = "select e, e.intVal1, e.intVal2 from EntityA_Embed_Coll_Embed a " +
            " , in (a.embed.embeds) e where a.embed.embeds IS NOT EMPTY order by e";
        String jpql = "select e1, e1.intVal1, e1.intVal2 from EntityA_Embed_Coll_Embed a " +
            "JOIN a.embed e JOIN e.embeds e1 where e.embeds IS NOT EMPTY order by e1";
        CriteriaQuery<?> q = cb.createQuery();
        Root<EntityA_Embed_Coll_Embed> a = q.from(EntityA_Embed_Coll_Embed.class);
        ListJoin<Embed_Coll_Embed, Embed> e = a.join(EntityA_Embed_Coll_Embed_.embed).join(Embed_Coll_Embed_.embeds);
        q.orderBy(cb.asc(e));
        q.multiselect(e, e.get(Embed_.intVal1), e.get(Embed_.intVal2));
        q.where(cb.isEmpty(a.get(EntityA_Embed_Coll_Embed_.embed).get(Embed_Coll_Embed_.embeds)).not());
        assertEquivalence(q, jpql);
    }

    public void testEmbeddableQuery93() {
        String jpql1 = "select e, e.intVal1, e.intVal2 from EntityA_Embed_Coll_Embed a " +
            " , in (a.embed.embeds) e where exists (select e.intVal1 " +
            " from EntityA_Embed_Coll_Embed a, in (a.embed.embeds) e " +
            " where e.intVal2 = 105) order by e";
        
        String jpql = "select e, e.intVal1, e.intVal2 from EntityA_Embed_Coll_Embed a " +
            " JOIN a.embed ae JOIN ae.embeds e where exists (select e.intVal1 " +
            " from EntityA_Embed_Coll_Embed a JOIN a.embed ae JOIN ae.embeds e " +
            " where e.intVal2 = 105) order by e";

        CriteriaQuery<?> q = cb.createQuery();
        Root<EntityA_Embed_Coll_Embed> a = q.from(EntityA_Embed_Coll_Embed.class);
        ListJoin<Embed_Coll_Embed, Embed> e = a.join(EntityA_Embed_Coll_Embed_.embed).join(Embed_Coll_Embed_.embeds);
        q.orderBy(cb.asc(e));
        q.multiselect(e, e.get(Embed_.intVal1), e.get(Embed_.intVal2));
        Subquery<Integer> sq = q.subquery(Integer.class);
        Root<EntityA_Embed_Coll_Embed> a1 = sq.from(EntityA_Embed_Coll_Embed.class);
        ListJoin<Embed_Coll_Embed, Embed> e1 = a1.join(EntityA_Embed_Coll_Embed_.embed).join(Embed_Coll_Embed_.embeds);
        sq.where(cb.equal(e1.get(Embed_.intVal2), 105));
        sq.select(e1.get(Embed_.intVal1));
        q.where(cb.exists(sq));
        assertEquivalence(q, jpql);
    }
    
    public void testEmbeddableQuery94() {
        String jpql1 = "select e, a from EntityA_Embed_Coll_Embed a, in (a.embed.embeds) e " +
            " where e.intVal1 = SOME (select e2.intVal1 from EntityA_Embed_Coll_Embed a2 " +
            " , in (a2.embed.embeds) e2) order by e";
        String jpql = "select e, a1 from EntityA_Embed_Coll_Embed a1 JOIN a1.embed a0 JOIN a0.embeds e " +
            " where e.intVal1 = SOME (select e2.intVal1 from EntityA_Embed_Coll_Embed a2 " +
            "JOIN a2.embed e0 JOIN e0.embeds e2) order by e";
        CriteriaQuery<?> q = cb.createQuery();
        Root<EntityA_Embed_Coll_Embed> a = q.from(EntityA_Embed_Coll_Embed.class);
        ListJoin<Embed_Coll_Embed, Embed> e = a.join(EntityA_Embed_Coll_Embed_.embed).join(Embed_Coll_Embed_.embeds);
        q.orderBy(cb.asc(e));
        q.multiselect(e, a);
        Subquery<Integer> sq = q.subquery(Integer.class);
        Root<EntityA_Embed_Coll_Embed> a2 = sq.from(EntityA_Embed_Coll_Embed.class);
        Join<EntityA_Embed_Coll_Embed, Embed_Coll_Embed> e0 = a2.join(EntityA_Embed_Coll_Embed_.embed);
        ListJoin<Embed_Coll_Embed, Embed> e2 = e0.join(Embed_Coll_Embed_.embeds);
        sq.select(e2.get(Embed_.intVal1));
        q.where(cb.equal(e.get(Embed_.intVal1), cb.some(sq)));
        assertEquivalence(q, jpql);
    }
    
    public void testEmbeddableQuery95() {
        String jpql = "select e, a from EntityA_Embed_Coll_Embed a, in (a.embed.embeds) e " +
                " where e = :p order by e";
        CriteriaQuery<?> q = cb.createQuery();
        Root<EntityA_Embed_Coll_Embed> a = q.from(EntityA_Embed_Coll_Embed.class);
        ListJoin<Embed_Coll_Embed, Embed> e = a.join(EntityA_Embed_Coll_Embed_.embed).join(Embed_Coll_Embed_.embeds);
        q.orderBy(cb.asc(e));
        q.multiselect(e, a);
        Parameter<Embed> param1 = cb.parameter(Embed.class, "p");
        q.where(cb.equal(e, param1));
        executeExpectFail(jpql, new String[] {"p1"}, new Object[] {new Embed()});
        executeExpectFail(q, jpql, new String[] {"p1"}, new Object[] {new Embed()});
    }
    
    public void testEmbeddableQuery96() {
        String jpql = "select a from EntityA_Embed_Coll_Embed a where a.embed = :p order by a";
        CriteriaQuery<EntityA_Embed_Coll_Embed> q = cb.createQuery(EntityA_Embed_Coll_Embed.class);
        Root<EntityA_Embed_Coll_Embed> a = q.from(EntityA_Embed_Coll_Embed.class);
        q.orderBy(cb.asc(a));
        q.select(a);
        Parameter<Embed> param1 = cb.parameter(Embed.class, "p");
        q.where(cb.equal(a.get(EntityA_Embed_Coll_Embed_.embed), param1));
        executeExpectFail(jpql, new String[] {"p1"}, new Object[] {new Embed()});
        executeExpectFail(q, jpql, new String[] {"p1"}, new Object[] {new Embed()});
    }
    
    public void testEmbeddableQuery97() {
        String jpql = "select a from EntityA_Embed_Coll_Embed a";
        CriteriaQuery<EntityA_Embed_Coll_Embed> q = cb.createQuery(EntityA_Embed_Coll_Embed.class);
        Root<EntityA_Embed_Coll_Embed> a = q.from(EntityA_Embed_Coll_Embed.class);
        q.select(a);
        assertEquivalence(q, jpql);
    }
    
    public void testEmbeddableQuery98() {
        String jpql = "select d from Department1 d";
        CriteriaQuery<Department1> q = cb.createQuery(Department1.class);
        Root<Department1> d = q.from(Department1.class);
        q.select(d);
        assertEquivalence(q, jpql);
    }
    
    public void testEmbeddableQuery99() {
        String jpql = "select d from Department2 d";
        CriteriaQuery<Department2> q = cb.createQuery(Department2.class);
        Root<Department2> d = q.from(Department2.class);
        q.select(d);
        assertEquivalence(q, jpql);
    }
    
    public void testEmbeddableQuery100() {
        String jpql = "select d from Department3 d";
        CriteriaQuery<Department3> q = cb.createQuery(Department3.class);
        Root<Department3> d = q.from(Department3.class);
        q.select(d);
        assertEquivalence(q, jpql);
    }
    
    public void testEmbeddableQuery101() {
        String jpql = "select d from Department1 d join d.empMap e where KEY(e) > 1 order by d";
        CriteriaQuery<Department1> q = cb.createQuery(Department1.class);
        Root<Department1> d = q.from(Department1.class);
        MapJoin<Department1, Integer, Employee1> empMap = d.join(Department1_.empMap);
        q.select(d);
        q.where(cb.gt(empMap.key(), 1));
        q.orderBy(cb.asc(d));
        assertEquivalence(q, jpql);
    }
    
    public void testEmbeddableQuery102() {
        String jpql = "select d from Department1 d join d.empMap e where d.deptId = KEY(e) order by d";
        CriteriaQuery<Department1> q = cb.createQuery(Department1.class);
        Root<Department1> d = q.from(Department1.class);
        MapJoin<Department1, Integer, Employee1> empMap = d.join(Department1_.empMap);
        q.select(d);
        q.where(cb.equal(d.get(Department1_.deptId), empMap.key()));
        q.orderBy(cb.asc(d));
        assertEquivalence(q, jpql);
    }
    
    public void testEmbeddableQuery103() {
        String jpql = "select d from Department1 d where d.deptId < ANY " +
                " (select KEY(e) from in(d.empMap) e) order by d";
        CriteriaQuery<Department1> q = cb.createQuery(Department1.class);
        Root<Department1> d = q.from(Department1.class);
        Subquery<Integer> sq = q.subquery(Integer.class);
        Root<Department1> d1 = sq.correlate(d);
        MapJoin<Department1, Integer, Employee1> empMap = d1.join(Department1_.empMap);
        sq.select(empMap.key());
        q.select(d);
        q.where(cb.lt(d.get(Department1_.deptId), cb.any(sq)));
        q.orderBy(cb.asc(d));
        assertEquivalence(q, jpql);
    }
    
    public void testEmbeddableQuery103a() {
        String jpql = "select KEY(e) from Department1 d, in(d.empMap) e order by d";
        CriteriaQuery<Integer> q = cb.createQuery(Integer.class);
        Root<Department1> d = q.from(Department1.class);
        MapJoin<Department1, Integer, Employee1> empMap = d.join(Department1_.empMap);
        q.select(empMap.key());
        q.orderBy(cb.asc(d));
        assertEquivalence(q, jpql);
    }
    
    public void testEmbeddableQuery103b() {
        String jpql = "select ENTRY(e) from Department1 d, in(d.empMap) e order by d";
        CriteriaQuery<Map.Entry> q = cb.createQuery(Map.Entry.class);
        Root<Department1> d = q.from(Department1.class);
        MapJoin<Department1, Integer, Employee1> empMap = d.join(Department1_.empMap);
        q.select(empMap.entry());
        q.orderBy(cb.asc(d));
        assertEquivalence(q, jpql);
    }

    public void testEmbeddableQuery104() {
        String jpql = "select d from Department1 d where d.deptId < SOME " +
                " (select KEY(e) from Department1 d1, in(d1.empMap) e) order by d";
        CriteriaQuery<Department1> q = cb.createQuery(Department1.class);
        Root<Department1> d = q.from(Department1.class);
        Subquery<Integer> sq = q.subquery(Integer.class);
        Root<Department1> d1 = sq.from(Department1.class);
        MapJoin<Department1, Integer, Employee1> empMap = d1.join(Department1_.empMap);
        sq.select(empMap.key());
        q.select(d);
        q.where(cb.lt(d.get(Department1_.deptId), cb.some(sq)));
        q.orderBy(cb.asc(d));
        assertEquivalence(q, jpql);
    }
    
    public void testEmbeddableQuery105() {
        String jpql = "select e from Employee1 e";
        CriteriaQuery<Employee1> q = cb.createQuery(Employee1.class);
        Root<Employee1> e = q.from(Employee1.class);
        q.select(e);
        assertEquivalence(q, jpql);
    }

    public void testEmbeddableQuery106() {
        String jpql = "select e from Employee2 e";
        CriteriaQuery<Employee2> q = cb.createQuery(Employee2.class);
        Root<Employee2> e = q.from(Employee2.class);
        q.select(e);
        assertEquivalence(q, jpql);
    }
    
    public void testEmbeddableQuery107() {
        String jpql = "select e from Employee3 e";
        CriteriaQuery<Employee3> q = cb.createQuery(Employee3.class);
        Root<Employee3> e = q.from(Employee3.class);
        q.select(e);
        assertEquivalence(q, jpql);
    }
    
    public void testEmbeddableQuery108() {
        String jpql = "select i from Item1 i";
        CriteriaQuery<Item1> q = cb.createQuery(Item1.class);
        Root<Item1> i = q.from(Item1.class);
        q.select(i);
        assertEquivalence(q, jpql);
    }
        
    public void testEmbeddableQuery109() {
        String jpql = "select i from Item2 i";
        CriteriaQuery<Item2> q = cb.createQuery(Item2.class);
        Root<Item2> i = q.from(Item2.class);
        q.select(i);
        assertEquivalence(q, jpql);
    }

    public void testEmbeddableQuery110() {
        String jpql = "select i from Item3 i";
        CriteriaQuery<Item3> q = cb.createQuery(Item3.class);
        Root<Item3> i = q.from(Item3.class);
        q.select(i);
        assertEquivalence(q, jpql);
    }

    public void testEmbeddableQuery111() {
        String jpql = "select i from Item1 i where :image = any(select KEY(e) from i.images e) order by i";
        CriteriaQuery<Item1> q = cb.createQuery(Item1.class);
        Root<Item1> i = q.from(Item1.class);
        Subquery<String> sq = q.subquery(String.class);
        Root<Item1> i1 = sq.correlate(i);
        MapJoin<Item1, String, String> e = i1.join(Item1_.images);
        sq.select(e.key());
        q.select(i);
        ParameterExpression<String> param1 = cb.parameter(String.class, "image");
        q.where(cb.equal(param1, cb.any(sq)));
        q.orderBy(cb.asc(i));
        assertEquivalence(new QueryDecorator() {
            public void decorate(Query q) {
                q.setParameter("image", "my photo");
            }
        }, q, jpql);
    }
    
    public void testEmbeddableQuery112() {
        String jpql = "select i from Item1 i where :image = any (select KEY(e) from Item1 i, in(i.images) e) " +
                " order by i";
        CriteriaQuery<Item1> q = cb.createQuery(Item1.class);
        Root<Item1> i = q.from(Item1.class);
        Subquery<String> sq = q.subquery(String.class);
        Root<Item1> i1 = sq.from(Item1.class);
        MapJoin<Item1, String, String> e = i1.join(Item1_.images);
        sq.select(e.key());
        q.select(i);
        ParameterExpression<String> param1 = cb.parameter(String.class, "image");
        q.where(cb.equal(param1, cb.any(sq)));
        q.orderBy(cb.asc(i));
        assertEquivalence(new QueryDecorator() {
            public void decorate(Query q) {
                q.setParameter("image", "my photo");
            }
        }, q, jpql);
    }
    
    public void testEmbeddableQuery113() {
        String jpql = "select i from Item1 i where exists (select e from Item1 i, in(i.images) e" +
                " where :image = KEY(e)) order by i";
        CriteriaQuery<Item1> q = cb.createQuery(Item1.class);
        Root<Item1> i = q.from(Item1.class);
        Subquery<String> sq = q.subquery(String.class);
        Root<Item1> i1 = sq.from(Item1.class);
        MapJoin<Item1, String, String> e = i1.join(Item1_.images);
        sq.select(e);
        q.select(i);
        ParameterExpression<String> param1 = cb.parameter(String.class, "image");
        sq.where(cb.equal(param1, e.key()));
        q.where(cb.exists(sq));
        q.orderBy(cb.asc(i));
        assertEquivalence(new QueryDecorator() {
            public void decorate(Query q) {
                q.setParameter("image", "my photo");
            }
        }, q, jpql);
    }
     
    public void testEmbeddableQuery114() {
        String jpql = "select i from Item2 i where :image = any (select KEY(e) from Item2 i, in(i.images) e) " +
                " order by i";
        CriteriaQuery<Item2> q = cb.createQuery(Item2.class);
        Root<Item2> i = q.from(Item2.class);
        Subquery<String> sq = q.subquery(String.class);
        Root<Item2> i1 = sq.from(Item2.class);
        MapJoin<Item2, String, String> e = i1.join(Item2_.images);
        sq.select(e.key());
        q.select(i);
        ParameterExpression<String> param1 = cb.parameter(String.class, "image");
        q.where(cb.equal(param1, cb.any(sq)));
        q.orderBy(cb.asc(i));
        assertEquivalence(new QueryDecorator() {
            public void decorate(Query q) {
                q.setParameter("image", "my photo");
            }
        }, q, jpql);
    }
    
    public void testEmbeddableQuery115() {
        String jpql = "select i from Item2 i where exists (select e from Item2 i, in(i.images) e" +
                "   where :image = KEY(e)) order by i";
        CriteriaQuery<Item2> q = cb.createQuery(Item2.class);
        Root<Item2> i = q.from(Item2.class);
        Subquery<String> sq = q.subquery(String.class);
        Root<Item2> i1 = sq.from(Item2.class);
        MapJoin<Item2, String, String> e = i1.join(Item2_.images);
        sq.select(e);
        q.select(i);
        ParameterExpression<String> param1 = cb.parameter(String.class, "image");
        sq.where(cb.equal(param1, e.key()));
        q.where(cb.exists(sq));
        q.orderBy(cb.asc(i));
        assertEquivalence(new QueryDecorator() {
            public void decorate(Query q) {
                q.setParameter("image", "my photo");
            }
        }, q, jpql);
    }
    
    public void testEmbeddableQuery116() {
        String jpql = "select i from Item3 i where :image = any (select KEY(e) from Item3 i, in(i.images) e) " +
                "order by i";
        CriteriaQuery<Item3> q = cb.createQuery(Item3.class);
        Root<Item3> i = q.from(Item3.class);
        Subquery<String> sq = q.subquery(String.class);
        Root<Item3> i1 = sq.from(Item3.class);
        MapJoin<Item3, String, String> e = i1.join(Item3_.images);
        sq.select(e.key());
        q.select(i);
        ParameterExpression<String> param1 = cb.parameter(String.class, "image");
        q.where(cb.equal(param1, cb.any(sq)));
        q.orderBy(cb.asc(i));
        assertEquivalence(new QueryDecorator() {
            public void decorate(Query q) {
                q.setParameter("image", "my photo");
            }
        }, q, jpql);
    }
    
    public void testEmbeddableQuery117() {
        String jpql = "select i from Item3 i where exists (select e from Item3 i, in(i.images) e" +
                " where :image = KEY(e)) order by i";
        CriteriaQuery<Item3> q = cb.createQuery(Item3.class);
        Root<Item3> i = q.from(Item3.class);
        Subquery<String> sq = q.subquery(String.class);
        Root<Item3> i1 = sq.from(Item3.class);
        MapJoin<Item3, String, String> e = i1.join(Item3_.images);
        sq.select(e);
        q.select(i);
        ParameterExpression<String> param1 = cb.parameter(String.class, "image");
        sq.where(cb.equal(param1, e.key()));
        q.where(cb.exists(sq));
        q.orderBy(cb.asc(i));
        assertEquivalence(new QueryDecorator() {
            public void decorate(Query q) {
                q.setParameter("image", "my photo");
            }
        }, q, jpql);
    }
    
    public void testEmbeddableQuery118() {
        String jpql = "select c from Company1 c";
        CriteriaQuery<Company1> q = cb.createQuery(Company1.class);
        Root<Company1> c = q.from(Company1.class);
        q.select(c);
        assertEquivalence(q, jpql);
    }
    
    public void testEmbeddableQuery119() {
        String jpql = "select c from Company2 c";
        CriteriaQuery<Company2> q = cb.createQuery(Company2.class);
        Root<Company2> c = q.from(Company2.class);
        q.select(c);
        assertEquivalence(q, jpql);
    }
    
    public void testEmbeddableQuery121() {
        String jpql = "select c from Company1 c where :division = " +
                " (select KEY(d) from Company1 c, in(c.organization) d where d.id = 1) order by c ";
        CriteriaQuery<Company1> q = cb.createQuery(Company1.class);
        Root<Company1> c = q.from(Company1.class);
        Subquery<Division> sq = q.subquery(Division.class);
        Root<Company1> c1 = sq.from(Company1.class);
        MapJoin<Company1,Division,VicePresident> d = c1.join(Company1_.organization);
        sq.select(d.key());
        q.select(c);
        ParameterExpression<Division> param1 = cb.parameter(Division.class, "division");
        sq.where(cb.equal(d.value().get(VicePresident_.id), 1));
        q.where(cb.equal(param1, sq));
        q.orderBy(cb.asc(c));
        assertEquivalence(new QueryDecorator() {
            public void decorate(Query q) {
                q.setParameter("division", new Division());
            }
        }, q, jpql);
    }
    
    public void testEmbeddableQuery122() {
        String jpql = "select c from Company1 c where exists (select KEY(d) from in(c.organization) d" +
                "  where KEY(d) = :division) order by c ";
        CriteriaQuery<Company1> q = cb.createQuery(Company1.class);
        Root<Company1> c = q.from(Company1.class);
        Subquery<Division> sq = q.subquery(Division.class);
        Root<Company1> c1 = sq.correlate(c);
        MapJoin<Company1,Division,VicePresident> d = c1.join(Company1_.organization);
        q.select(c);
        Parameter<Division> param1 = cb.parameter(Division.class, "division");
        sq.where(cb.equal(d.key(), param1));
        sq.select(d.key());
        q.where(cb.exists(sq));
        q.orderBy(cb.asc(c));
        assertEquivalence(new QueryDecorator() {
            public void decorate(Query q) {
                q.setParameter("division", new Division());
            }
        }, q, jpql);
    }
    
    public void testEmbeddableQuery123() {
        String jpql = "select c from Company1 c where exists (select KEY(d) from c.organization d" +
                "  where KEY(d) = :division) order by c ";  
        CriteriaQuery<Company1> q = cb.createQuery(Company1.class);
        Root<Company1> c = q.from(Company1.class);
        Subquery<Division> sq = q.subquery(Division.class);
        Root<Company1> c1 = sq.correlate(c);
        MapJoin<Company1,Division,VicePresident> d = c1.join(Company1_.organization);
        sq.select(d.key());
        q.select(c);
        Parameter<Division> param1 = cb.parameter(Division.class, "division");
        sq.where(cb.equal(d.key(), param1));
        q.where(cb.exists(sq));
        q.orderBy(cb.asc(c));
        assertEquivalence(new QueryDecorator() {
            public void decorate(Query q) {
                q.setParameter("division", new Division());
            }
        }, q, jpql);
    }
    
    public void testEmbeddableQuery124() {
        String jpql = "select c from Company2 c where :division = (select KEY(d) from Company2 c, in(c.organization) d" 
                    + " where d.id = 3) order by c ";
        CriteriaQuery<Company2> q = cb.createQuery(Company2.class);
        Root<Company2> c = q.from(Company2.class);
        Subquery<Division> sq = q.subquery(Division.class);
        Root<Company2> c1 = sq.from(Company2.class);
        MapJoin<Company2,Division,VicePresident> d = c1.join(Company2_.organization);
        sq.select(d.key());
        q.select(c);
        ParameterExpression<Division> param1 = cb.parameter(Division.class, "division");
        sq.where(cb.equal(d.value().get(VicePresident_.id), 3));
        q.where(cb.equal(param1, sq));
        q.orderBy(cb.asc(c));
        assertEquivalence(new QueryDecorator() {
            public void decorate(Query q) {
                q.setParameter("division", new Division());
            }
        }, q, jpql);
    }
    
    public void testEmbeddableQuery125() {
        String jpql = "select c from Company2 c where exists (select KEY(d) from in(c.organization) d" +
                "  where KEY(d) = :k) order by c ";
        CriteriaQuery<Company2> q = cb.createQuery(Company2.class);
        Root<Company2> c = q.from(Company2.class);
        Subquery<Division> sq = q.subquery(Division.class);
        Root<Company2> c1 = sq.correlate(c);
        MapJoin<Company2,Division,VicePresident> d = c1.join(Company2_.organization);
        q.select(c);
        Parameter<Division> param1 = cb.parameter(Division.class, "k");
        sq.where(cb.equal(d.key(), param1));
        sq.select(d.key());
        q.where(cb.exists(sq));
        q.orderBy(cb.asc(c));
        assertEquivalence(new QueryDecorator() {
            public void decorate(Query q) {
                q.setParameter("k", new Division());
            }
        }, q, jpql);
    }
    
    public void testEmbeddableQuery126() {
        String jpql = "select c from Company2 c where exists (select VALUE(d) from c.organization d "
            + "where KEY(d) = :k) order by c ";  
        CriteriaQuery<Company2> q = cb.createQuery(Company2.class);
        Root<Company2> c = q.from(Company2.class);
        Subquery<VicePresident> sq = q.subquery(VicePresident.class);
        Root<Company2> c1 = sq.correlate(c);
        MapJoin<Company2,Division,VicePresident> d = c1.join(Company2_.organization);
        q.select(c);
        Parameter<Division> param1 = cb.parameter(Division.class, "k");
        sq.where(cb.equal(d.key(), param1));
        sq.select(d.value());
        q.where(cb.exists(sq));
        q.orderBy(cb.asc(c));
        assertEquivalence(new QueryDecorator() {
            public void decorate(Query q) {
                q.setParameter("k", new Division());
            }
        }, q, jpql);
    }

    public void testEmbeddableQuery127() {
        String jpql = "select d from Division d";
        CriteriaQuery<Division> q = cb.createQuery(Division.class);
        Root<Division> d = q.from(Division.class);
        q.select(d);
        assertEquivalence(q, jpql);
    }

    public void testEmbeddableQuery128() {
        String jpql = "select vp from VicePresident vp";
        CriteriaQuery<VicePresident> q = cb.createQuery(VicePresident.class);
        Root<VicePresident> vp = q.from(VicePresident.class);
        q.select(vp);
        assertEquivalence(q, jpql);
    }
}
