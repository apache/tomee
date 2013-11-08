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
package org.apache.openjpa.persistence.graph;

import java.sql.Date;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.meta.FieldMapping;
import org.apache.openjpa.jdbc.meta.FieldStrategy;
import org.apache.openjpa.jdbc.meta.MappingRepository;
import org.apache.openjpa.jdbc.meta.ValueHandler;
import org.apache.openjpa.jdbc.meta.strats.HandlerFieldStrategy;
import org.apache.openjpa.jdbc.meta.strats.HandlerHandlerMapTableFieldStrategy;
import org.apache.openjpa.jdbc.meta.strats.UntypedPCValueHandler;
import org.apache.openjpa.kernel.QueryHints;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.meta.MetaDataRepository;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Tests basic create and query on generic persistent graph. The test creates a
 * graph of People and Cities. Then different queries on the graph are verified.
 * 
 * @author Pinaki Poddar
 * 
 */
public class TestPersistentGraph extends SingleEMFTestCase {
    private static enum Emotion {
        LOVES, HATES, KNOWS
    };

    // Identity of People is their SSN
    private static final long[] SSN = { 123456781, 123456782, 123456783, 123456784, 123456785 };
    private static final String[] PERSON_NAMES = { "P1", "P2", "P3", "P4", "P5" };
    private static final String[] CITY_NAMES = { "San Francisco", "Paris", "Rome" };

    private static final String ATTR_SINCE = "since";
    private static final Date SINCE = new Date(90, 1, 27);

    private static final String ATTR_EMOTION = "feels";
    private static final Emotion[][] EMOTIONS = {
                            /*    P1             P2             P3             P4             P5       */
    /* P1 */new Emotion[] {          null, Emotion.LOVES, Emotion.HATES,          null, Emotion.KNOWS },
    /* P2 */new Emotion[] { Emotion.LOVES,          null, Emotion.LOVES,          null, Emotion.LOVES },
    /* P3 */new Emotion[] { Emotion.HATES, Emotion.LOVES,          null,          null, Emotion.KNOWS },
    /* P4 */new Emotion[] { Emotion.LOVES, Emotion.HATES, Emotion.KNOWS, Emotion.LOVES, Emotion.LOVES },
    /* P5 */new Emotion[] {          null, Emotion.LOVES, Emotion.KNOWS, Emotion.KNOWS,          null }, 
    };

    private static final String ATTR_DISTANCE = "distance";
    private static final int[][] ATTR_DISTANCE_VALUE = {
                       /* C1 C2 C3 */
    /* C1 */new int[] { 0, 200, 400 },
    /* C2 */new int[] { 200, 0, 500 },
    /* C3 */new int[] { 400, 500, 0 }
    };

    private EntityManager em;
    private PersistentGraph<Object> graph;
    
    public void setUp() throws Exception {
        super.setUp(CLEAR_TABLES, PersistentGraph.class, RelationGraph.class,
                PersistentRelation.class, People.class, City.class);
        em = emf.createEntityManager();
        graph = createData();
        em.clear();
    }
    
    /**
     * Verifies that fields are mapped with expected strategy or value handlers.
     */
    public void testMapping() {
        assertStrategy(PersistentRelation.class, "source", HandlerFieldStrategy.class, UntypedPCValueHandler.class);
        assertStrategy(PersistentRelation.class, "target", HandlerFieldStrategy.class, UntypedPCValueHandler.class);
        assertStrategy(PersistentRelation.class, "attrs", HandlerHandlerMapTableFieldStrategy.class, null);
    }

    private void printMapping(FieldMapping fm) {
        System.err.println("Field :" + fm.getName());
        System.err.println("Type :" + fm.getTypeCode() + " " + fm.getType());
        System.err.println("Type (declared):" + fm.getDeclaredTypeCode() + " " + fm.getDeclaredType());
        System.err.println("Type Override :" + fm.getTypeOverride());
        System.err.println("Key type  :" + fm.getKey().getType());
        System.err.println("Key declared type  :" + fm.getKey().getDeclaredType());
        System.err.println("Element type  :" + fm.getElement().getType());
        System.err.println("Element declared type  :" + fm.getElement().getDeclaredType());
    }

    FieldMapping getFieldMapping(Class<?> pcClass, String field) {
        MappingRepository repos = (MappingRepository) emf.getConfiguration()
                .getMetaDataRepositoryInstance();
        ClassMapping cmd = repos.getMapping(pcClass, null, true);
        assertNotNull("No metadata found for " + pcClass, cmd);
        FieldMapping fmd = cmd.getFieldMapping(field);
        assertNotNull("No metadata found for " + pcClass.getName() + "." + field + " Fields are "
                + Arrays.toString(cmd.getFieldNames()), fmd);
        return fmd;
    }

    /**
     * Asserts that the given field of the given class has been mapped with the
     * given strategy or value handler.
     */
    void assertStrategy(Class<?> pcClass, String field, Class<? extends FieldStrategy> strategy,
            Class<? extends ValueHandler> handler) {

        FieldMapping fmd = getFieldMapping(pcClass, field);
        FieldStrategy actualStrategy = ((FieldMapping) fmd).getStrategy();
        assertEquals(strategy, actualStrategy.getClass());
        ValueHandler actualHandler = fmd.getHandler();
        if (handler == null) {
            if (actualHandler != null) {
                printMapping(fmd);
                fail("Expected no value handler for " + pcClass.getName() + "." + field + 
                        " but found " + actualHandler);
            }
        } else {
            if (actualHandler == null) {
                printMapping(fmd);
                fail("Expected a value handler for " + pcClass.getName() + "." + field + " but found null");
            }
            if (!handler.getClass().equals(actualHandler.getClass())) {
                printMapping(fmd);
                assertEquals(handler, fmd.getHandler().getClass());
            }
        }
    }

    FieldStrategy getStrategy(Class<?> cls, String field) {
        MetaDataRepository repos = emf.getConfiguration().getMetaDataRepositoryInstance();
        ClassMetaData cmd = repos.getMetaData(cls, null, true);
        assertNotNull("No metadat found for " + cls, cmd);
        FieldMetaData fmd = cmd.getField(field);
        assertNotNull("No metadata found for " + cls.getName() + "." + field + " Fields are "
                + Arrays.toString(cmd.getFieldNames()), fmd);
        FieldStrategy strategy = ((FieldMapping) fmd).getStrategy();
        System.err.println(cls.getName() + "." + field + ":" + strategy.getClass().getSimpleName());
        return strategy;
    }

    /**
     * Tests that the nodes retrieved from the database meets the same
     * assertions of the created graph.
     */
    public void testCreateGraph() {
        em.getTransaction().begin();
        assertFalse(em.contains(graph));
        graph = em.find(PersistentGraph.class, graph.getId());
        assertNotNull(graph);
        People[] people = new People[SSN.length];
        for (int i = 0; i < SSN.length; i++) {
            People p = em.find(People.class, SSN[i]);
            assertNotNull(p);
            people[i] = p;
        }
        City[] cities = new City[CITY_NAMES.length];
        for (int i = 0; i < CITY_NAMES.length; i++) {
            City c = em.find(City.class, CITY_NAMES[i]);
            assertNotNull(c);
            cities[i] = c;
        }
        assertDataEquals(graph, people, cities);

        em.getTransaction().rollback();
    }

    /**
     * Tests that relation can be queried and their references are set
     * correctly.
     */
    public void testQueryRelation() {
        String jpql = "select r from PersistentRelation r";
        List<PersistentRelation> relations = em.createQuery(jpql, PersistentRelation.class).getResultList();
        for (Relation<?, ?> r : relations) {
            Object source = r.getSource();
            Object target = r.getTarget();
            if (source instanceof People) {
                int i = indexOf((People) source);
                if (target instanceof People) {
                    int j = indexOf((People) target);
                    assertNotNull(EMOTIONS[i][j]);
                    assertEquals(EMOTIONS[i][j].toString(), r.getAttribute(ATTR_EMOTION));
                } else if (target instanceof City) {
                    int j = indexOf((City) target);
                    assertEquals(i % CITY_NAMES.length, j);
                    assertTrue(r.getAttributes().isEmpty());
                } else if (target != null){
                    fail("Unexpected relation " + r);
                }
            } else if (source instanceof City) {
                int i = indexOf((City) source);
                if (target instanceof City) {
                    int j = indexOf((City) target);
                    assertEquals(""+ATTR_DISTANCE_VALUE[i][j], r.getAttribute(ATTR_DISTANCE));
                } else if (target != null) {
                    fail("Unexpected relation " + r);
                }
            }
        }
    }

    /**
     * Tests that a relation can be queried predicated on its source vertex.
     */
    public void testQueryRelationOnSourceParameter() {
        People p1 = em.find(People.class, SSN[0]);
        String jpql = "select r from PersistentRelation r where r.source = :node";
        List<PersistentRelation> result = em.createQuery(jpql, PersistentRelation.class)
                                  .setParameter("node", p1)
                                  .getResultList();
        assertFalse("Result of [" + jpql + "] on source = " + p1 + " should not be empty", result.isEmpty());
    }

    /**
     * Tests that a relation can be queried predicated on its attribute key.
     */
    public void testQueryRelationOnSingleAttributeKey() {
        String jpql = "select r from PersistentRelation r join r.attrs a where key(a) = :key";
        List<PersistentRelation> result = em.createQuery(jpql, PersistentRelation.class)
                                  .setParameter("key", ATTR_EMOTION)
                                  .getResultList();

        assertFalse("Result of [" + jpql + "] on key = " + ATTR_EMOTION + " should not be empty", result.isEmpty());
    }

    /**
     * Tests that a relation can be queried predicated on a single attribute
     * key-value pair.
     */
    public void testQueryRelationOnSingleAttributeKeyValue() {
        String jpql = "select r from PersistentRelation r join r.attrs a where key(a) = :key and value(a) = :value";
        String value = EMOTIONS[0][2].toString();
        List<PersistentRelation> result = em.createQuery(jpql, PersistentRelation.class)
                                  .setParameter("key", ATTR_EMOTION)
                                  .setParameter("value", value)
                                  .getResultList();

        assertFalse("Result of [" + jpql + "] on key-value (" + ATTR_EMOTION + "," + value + ") should not be empty", 
                result.isEmpty());
    }

    /**
     * Tests that a relation can be queried predicated on a multiple attribute
     * key-value pair. This requires multiple joins. Single join will produce
     * wrong result.
     */
    public void testQueryRelationOnMultipleAttributeKeyValuePairs() {
        String jpql = "select r from PersistentRelation r join r.attrs a1 join r.attrs a2 "
                    + "where key(a1) = :key1 and value(a1) = :value1 " 
                    + "and key(a2) = :key2 and value(a2) = :value2";
        String value = EMOTIONS[0][2].toString();
        List<PersistentRelation> result = em.createQuery(jpql, PersistentRelation.class)
                                  .setParameter("key1", ATTR_EMOTION)
                                  .setParameter("value1", value)
                                  .setParameter("key2", ATTR_SINCE)
                                  .setParameter("value2", SINCE.toString())
                                  .getResultList();

        assertFalse("Result of [" + jpql + "] on key-value = (" + ATTR_EMOTION + "," + value 
                + ") and key-value=("  + ATTR_SINCE + "," + SINCE + ") should not be empty", 
                result.isEmpty());

        String wrongJPQL = "select r from PersistentRelation r join r.attrs a "  
                         + "where key(a) = :key1 and value(a) = :value1 "
                         + "and key(a) = :key2 and value(a) = :value2";
        List<PersistentRelation> result2 = em.createQuery(wrongJPQL, PersistentRelation.class)
                                   .setParameter("key1", ATTR_EMOTION)
                                   .setParameter("value1", value)
                                   .setParameter("key2", ATTR_SINCE)
                                   .setParameter("value2", SINCE.toString())
                                   .getResultList();

        assertTrue("Result of [" + jpql + "] on key-value = (" + ATTR_EMOTION + "," + value 
                + ") and key-value=("+ ATTR_SINCE + "," + SINCE + ") should be empty", 
                result2.isEmpty());
    }

    public void testAddRemoveAttribute() {
        em.getTransaction().begin();
        People p1 = em.find(People.class, SSN[0]);
        String jpql = "select r from PersistentRelation r where r.source = :node";
        List<PersistentRelation> r = em.createQuery(jpql, PersistentRelation.class)
                            .setHint(QueryHints.HINT_IGNORE_PREPARED_QUERY, true)
                            .setParameter("node", p1)
                            .getResultList();
        assertFalse(r.isEmpty());
        r.get(0).addAttribute("new-key", "new-value");
        em.getTransaction().commit();
        em.clear();
        
        em.getTransaction().begin();
        jpql = "select r from PersistentRelation r join r.attrs a where key(a) = :key";
        Relation newR = em.createQuery(jpql, PersistentRelation.class)
                          .setParameter("key", "new-key")
                          .getSingleResult();
        assertNotNull(newR);
        assertEquals("new-value", newR.getAttribute("new-key"));
        newR.removeAttribute("new-key");
        em.getTransaction().commit();
        
        em.getTransaction().begin();
        jpql = "select r from PersistentRelation r join r.attrs a where key(a) = :key";
        try {
            newR = em.createQuery(jpql, PersistentRelation.class)
                          .setParameter("key", "new-key")
                          .getSingleResult();
            fail(jpql + " with new-key expected no result");
        } catch (NoResultException nre) {
            // this is what is expected
        } finally {
            em.getTransaction().rollback();
        }
    }
    
    boolean isPopulated() {
        return em.createQuery("select count(p) from People p", Long.class).getSingleResult() > 0;
    }

    /**
     * Creates a typical graph of People and Cities. The tests are sensitive to
     * the actual values and relations set in in this method.
     */
    PersistentGraph<Object> createData() {
        PersistentGraph<Object> graph = new RelationGraph<Object>();
        
        em.getTransaction().begin();

        People[] people = new People[SSN.length];
        for (int i = 0; i < SSN.length; i++) {
            People p = new People();
            graph.add(p);
            p.setSsn(SSN[i]);
            p.setName(PERSON_NAMES[i]);
            people[i] = p;
        }
        City[] cities = new City[CITY_NAMES.length];
        for (int i = 0; i < CITY_NAMES.length; i++) {
            City c = new City();
            graph.add(c);
            c.setName(CITY_NAMES[i]);
            cities[i] = c;
        }
        for (int i = 0; i < people.length; i++) {
            for (int j = 0; j < people.length; j++) {
                if (EMOTIONS[i][j] != null) {
                    Relation<People, People> r = graph.link(people[i], people[j])
                                 .addAttribute(ATTR_EMOTION, EMOTIONS[i][j]);
                    if (i == 0 && j == 2) {
                        r.addAttribute(ATTR_SINCE, SINCE);
                    }
                }
            }
        }
        for (int i = 0; i < cities.length; i++) {
            for (int j = 0; j < cities.length; j++) {
                graph.link(cities[i], cities[j]).addAttribute(ATTR_DISTANCE, ATTR_DISTANCE_VALUE[i][j]);
            }
        }

        for (int i = 0; i < people.length; i++) {
            graph.link(people[i], cities[i % CITY_NAMES.length]);
        }
        em.persist(graph);
        em.getTransaction().commit();
        
        return graph;
    }

    void assertDataEquals(Graph<Object> graph, People[] people, City[] cities) {
        assertEquals(SSN.length, people.length);
        assertEquals(CITY_NAMES.length, cities.length);
        
        for (int i = 0; i < people.length; i++) {
            People p = people[i];
            assertEquals(SSN[i], p.getSsn());
            assertEquals(PERSON_NAMES[i], p.getName());
        }
        for (int i = 0; i < cities.length; i++) {
            City c = cities[i];
            assertEquals(CITY_NAMES[i], c.getName());
        }
        for (int i = 0; i < people.length; i++) {
            People p1 = people[i];
            for (int j = 0; j < people.length; j++) {
                People p2 = people[j];
                Relation<People, People> r = graph.getRelation(p1,p2);
                if (EMOTIONS[i][j] != null) {
                    assertNotNull(r);
                    assertEquals(EMOTIONS[i][j].toString(), r.getAttribute(ATTR_EMOTION));
                } else {
                    assertNull(r);
                }
            }
        }
        for (int i = 0; i < cities.length; i++) {
            City c1 = cities[i];
            for (int j = 0; j < cities.length; j++) {
                City c2 = cities[j];
                Relation<City, City> r12 = graph.getRelation(c1,c2);
                assertNotNull(r12);
                assertEquals(""+ATTR_DISTANCE_VALUE[i][j], r12.getAttribute(ATTR_DISTANCE));
            }
        }

        for (int i = 0; i < people.length; i++) {
            People p = people[i];
            for (int j = 0; j < cities.length; j++) {
                City c = cities[j];
                Relation<People, City> r = graph.getRelation(p,c);
                if (i % CITY_NAMES.length == j) {
                    assertNotNull(r);
                } else {
                    assertNull(r);
                }
            }

        }
    }

    int indexOf(People p) {
        for (int i = 0; i < SSN.length; i++) {
            if (SSN[i] == p.getSsn())
                return i;
        }
        return -1;
    }

    int indexOf(City c) {
        for (int i = 0; i < CITY_NAMES.length; i++) {
            if (CITY_NAMES[i].equals(c.getName()))
                return i;
        }
        return -1;
    }
}
