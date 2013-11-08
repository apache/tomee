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
package org.apache.openjpa.persistence.criteria.results;

import java.lang.reflect.Array;
import java.util.List;

import javax.persistence.Tuple;
import javax.persistence.criteria.CompoundSelection;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;

import org.apache.openjpa.persistence.criteria.CriteriaTest;
import org.apache.openjpa.persistence.criteria.Person;
import org.apache.openjpa.persistence.criteria.Person_;


/**
 * Test variations of {@link CriteriaQuery#multiselect(java.util.List)} arguments.
 * 
 * @author Pinaki Poddar
 *
 */

public class TestMultiselect extends CriteriaTest {
    private static boolean initialized = false;
    
    @Override
    public void setUp() throws Exception {
        super.setUp();
        if (!initialized) {
            clearData();
            createData();
            initialized = true;
        }
    }
    
    void clearData() {
        em.getTransaction().begin();
        em.createQuery("DELETE FROM Foo f").executeUpdate();
        em.createQuery("DELETE FROM Bar b").executeUpdate();
        em.createQuery("DELETE FROM Person p").executeUpdate();
        em.getTransaction().commit();
    }
    
    void createData() {
        em.getTransaction().begin();
        Person p = new Person("Test Result Shape");
        em.persist(p);
        
        Foo foo = new Foo(100L, "Test Foo");
        Bar bar = new Bar(200L, "Test Bar");
        foo.setBar(bar);
        em.persist(foo);
        em.persist(bar);
        em.getTransaction().commit();
    }
    
    /**
    * If the type of the criteria query is CriteriaQuery<Tuple>
    * (i.e., a criteria query object created by either the 
    * createTupleQuery method or by passing a Tuple class argument 
    * to the createQuery method), a Tuple object corresponding to 
    * the elements of the list passed to the multiselect method 
    * will be instantiated and returned for each row that results 
    * from the query execution.
    * */
    
    public void testTupleQuery() {
        CriteriaQuery<Tuple> q = cb.createTupleQuery();
        Root<Person> p = q.from(Person.class); 
        q.multiselect(p.get(Person_.name), p.get(Person_.id));
        
        assertResult(q, Tuple.class, String.class, Integer.class);
    }
    
    public void testTupleQueryExplicit() {
        CriteriaQuery<Tuple> q = cb.createQuery(Tuple.class);
        Root<Person> p = q.from(Person.class); 
        q.multiselect(p.get(Person_.name), p.get(Person_.id));
        
        assertResult(q, Tuple.class, String.class, Integer.class);
    }
  
    //=======================================================================
    
    /**
     * If the type of the criteria query is CriteriaQuery<X> for
     * some user-defined class X (i.e., a criteria query object
     * created by passing a X class argument to the createQuery 
     * method), then the elements of the list passed to the
     * multiselect method will be passed to the X constructor and 
     * an instance of type X will be returned for each row.  
     */
    public void testUserResultQueryWithExplictProjectionOfConstructorArguments() {
        CriteriaQuery<Person> q = cb.createQuery(Person.class);
        Root<Person> p = q.from(Person.class); 
        q.multiselect(p.get(Person_.name));
        
        assertResult(q, Person.class);
    }
    
    public void testUserResultQueryWithImplicitProjection() {
        CriteriaQuery<Person> q = cb.createQuery(Person.class);
        q.from(Person.class); 
        
        assertResult(q, Person.class);
    }

    public void testUserResultQueryWithMismatchProjectionOfConstructorArguments() {
        CriteriaQuery<Person> q = cb.createQuery(Person.class);
        Root<Person> p = q.from(Person.class); 
        q.multiselect(p.get(Person_.name), p.get(Person_.id));
        
        fail("Person has no constrcutor with (name,id)", q);
    }
    
    public void testMultipleConstructorWithAliasRepeatedAndInOrderingClause() {
        // SELECT NEW(p.name), p.id, NEW(p.name), p.name FROM Person p ORDER BY p.name
        CriteriaQuery<Tuple> q = cb.createTupleQuery();
        Root<Person> p = q.from(Person.class); 
        q.multiselect(
            cb.construct(Person.class, p.get(Person_.name)),
            p.get(Person_.id),
            cb.construct(Person.class, p.get(Person_.name)),
            p.get(Person_.name))
            .orderBy(cb.asc(p.get(Person_.name)));
        
        List<Tuple> tuples = em.createQuery(q).getResultList();
        assertTrue(!tuples.isEmpty());
        for (Tuple row : tuples) {
            assertEquals(4, row.getElements().size());
            
            assertEquals(Person.class,  row.get(0).getClass());
            assertEquals(Integer.class, row.get(1).getClass());
            assertEquals(Person.class,  row.get(2).getClass());
            assertEquals(String.class,  row.get(3).getClass());
            
            assertEquals(((Person)row.get(0)).getName(), ((Person)row.get(2)).getName());
            assertEquals(((Person)row.get(0)).getName(), row.get(3));
        }
    }
    
    // ======================================================================
    /**
     * If the type of the criteria query is CriteriaQuery<X[]> for
     * some class X, an instance of type X[] will be returned for 
     * each row.   The elements of the array will correspond to the 
     * elements of the list passed to the multiselect method.  
     */
    
    public void testUserClassArray() {
        CriteriaQuery<Person[]> q = cb.createQuery(Person[].class);
        Root<Person> p = q.from(Person.class); 
        q.multiselect(p,p);
        
        assertResult(q, Person[].class, Person.class, Person.class);
    }
    
    public void testBasicClassArray() {
        CriteriaQuery<String[]> q = cb.createQuery(String[].class);
        Root<Person> p = q.from(Person.class); 
        q.multiselect(p.get(Person_.name), p.get(Person_.name));
        
        assertResult(q, String[].class);
    }
    
    public void testTupleArray() {
        CriteriaQuery<Tuple[]> q = cb.createQuery(Tuple[].class);
        Root<Person> p = q.from(Person.class); 
        q.multiselect(p.get(Person_.name), p.get(Person_.id), p.get(Person_.name));
        
        assertResult(q, Tuple[].class, String.class, Integer.class, String.class);
    }
// =================================================================    
    /**
     * If the type of the criteria query is CriteriaQuery<Object>
     * or if the criteria query was created without specifying a 
     * type, and the list passed to the multiselect method contains 
     * only a single element, an instance of type Object will be 
     * returned for each row.
     */
    public void testSingleObject() {
        CriteriaQuery<Object> q = cb.createQuery(Object.class);
        Root<Person> p = q.from(Person.class);
        q.multiselect(p);
        
        assertResult(q, Object.class);
    }
    
    public void testSingleObjectViaConstructor() {
        CriteriaQuery<Object> q = cb.createQuery(Object.class);
        Root<Person> p = q.from(Person.class);
        q.multiselect(cb.construct(Person.class, p.get(Person_.name)));
        
        assertResult(q, Object.class);
    }
    
    public void testSingleObjectAsProperty() {
        CriteriaQuery<Object> q = cb.createQuery(Object.class);
        Root<Person> p = q.from(Person.class);
        q.multiselect(p.get(Person_.name));
        
        assertResult(q, Object.class);
    }
    
    public void testSingleObjectImplicit() {
        CriteriaQuery<?> q = cb.createQuery();
        Root<Person> p = q.from(Person.class);
        q.multiselect(p);
        
        assertResult(q, Object.class);
    }
    
    public void testSingleObjectViaConstructorImplicit() {
        CriteriaQuery<?> q = cb.createQuery();
        Root<Person> p = q.from(Person.class);
        q.multiselect(cb.construct(Person.class, p.get(Person_.name)));
        
        assertResult(q, Object.class);
    }
    
    public void testSingleObjectAsPropertyImplicit() {
        CriteriaQuery<?> q = cb.createQuery();
        Root<Person> p = q.from(Person.class);
        q.multiselect(p.get(Person_.name));
        
        assertResult(q, Object.class);
    }

// ================================================================================
    /**
     * If the type of the criteria query is CriteriaQuery<Object>
     * or if the criteria query was created without specifying a 
     * type, and the list passed to the multiselect method contains 
     * more than one element, an instance of type Object[] will be 
     * instantiated and returned for each row.  The elements of the 
     * array will correspond to the elements of the list passed to
     * the multiselect method.
     */
    public void testSingleObjectMultipleProjections() {
        CriteriaQuery<Object> q = cb.createQuery(Object.class);
        Root<Person> p = q.from(Person.class);
        q.multiselect(p.get(Person_.name), p.get(Person_.id));
        
        assertResult(q, Object[].class, String.class, Integer.class);
    }
    
    public void testSingleObjectMultipleProjectionsAndConstructor() {
        CriteriaQuery<Object> q = cb.createQuery(Object.class);
        Root<Person> p = q.from(Person.class);
        q.multiselect(cb.construct(Person.class, p.get(Person_.name)), p.get(Person_.id), p.get(Person_.name));
        
        assertResult(q, Object[].class, Person.class, Integer.class, String.class);
    }
    
    /**
     * An element of the list passed to the multiselect method 
     * must not be a tuple- or array-valued compound selection item. 
     * 
     */
    // This test is retired because we are now supporting arbitrary nesting 
    public void xtestTupleCanNotBeNested() {
        CriteriaQuery<Tuple> q = cb.createTupleQuery();
        Root<Person> p = q.from(Person.class);
        
        CompoundSelection<Tuple> tuple1 = cb.tuple(p.get(Person_.name), p.get(Person_.id));
        CompoundSelection<Tuple> tuple2 = cb.tuple(p.get(Person_.id), p.get(Person_.name));
        
        try {
            cb.tuple(tuple1, tuple2);
            fail("Expected exception while nesting tuples");
        } catch (IllegalArgumentException e) {
            
        }
    }
    
    public void testMultiConstructor() {
        // SELECT NEW Foo(f.fid,f.fint), b, NEW FooBar(f.fid, b.bid) from Foo f JOIN f.bar b WHERE f.b=b
        CriteriaQuery<Object[]> q = cb.createQuery(Object[].class);
        Root<Foo> f = q.from(Foo.class);
        Join<Foo, Bar> b = f.join(Foo_.bar);
        q.multiselect(cb.construct(Foo.class, f.get(Foo_.fid), f.get(Foo_.fstring)), 
                      b, 
                      cb.construct(FooBar.class, f.get(Foo_.fid), b.get(Bar_.bid)));
        q.where(cb.equal(f.get(Foo_.fid), 100L));
        
        List<Object[]> result = em.createQuery(q).getResultList();
        assertFalse(result.isEmpty());
        for (Object[] row : result) {
            assertEquals(3, row.length);
            assertTrue("0-th element " + row[0].getClass() + " is not Foo", row[0] instanceof Foo);
            assertTrue("1-st element " + row[1].getClass() + " is not Bar", row[1] instanceof Bar);
            assertTrue("2-nd element " + row[2].getClass() + " is not FooBar", row[2] instanceof FooBar);
        }
    }
    
    public void testSelectSingleTermWithMultiselectObjectArray() {
        CriteriaQuery<Object[]> q = cb.createQuery(Object[].class);
        Root<Foo> f = q.from(Foo.class);
        q.multiselect(f);
        
        assertResult(q, Object[].class, Foo.class);
    }
    
    public void testSelectSingleTermWithMultiselectObject() {
        CriteriaQuery<Object> q = cb.createQuery(Object.class);
        Root<Foo> f = q.from(Foo.class);
        q.multiselect(f);
        
        assertResult(q, Foo.class);
    }
    
    public void testSelectSingleTermWithMultiselectTuple() {
        CriteriaQuery<Tuple> q = cb.createQuery(Tuple.class);
        Root<Foo> f = q.from(Foo.class);
        q.multiselect(f);
        
        assertResult(q, Tuple.class, Foo.class);
    }
    
    public void testSelectSingleTermWithMultiselectTupleArray() {
        CriteriaQuery<Tuple[]> q = cb.createQuery(Tuple[].class);
        Root<Foo> f = q.from(Foo.class);
        q.multiselect(f);
        
        assertResult(q, Tuple[].class, Foo.class);
    }
    
    public void testSanity() {
        CriteriaQuery<Foo> q = cb.createQuery(Foo.class);
        Root<Foo> f = q.from(Foo.class);
        
        assertResult(q, Foo.class);
    }
    
    public void testSanity2() {
        CriteriaQuery<Foo> q = cb.createQuery(Foo.class);
        Root<Foo> f = q.from(Foo.class);
        q.select(f);
        assertResult(q, Foo.class);
    }
    
    public void testDeeplyNestedShape() {
        CriteriaQuery<Tuple> q = cb.createQuery(Tuple.class);
        Root<Foo> foo = q.from(Foo.class);
        q.multiselect(cb.construct(Foo.class, foo.get(Foo_.flong), foo.get(Foo_.fstring)), 
                 cb.tuple(foo, cb.array(foo.get(Foo_.fint), cb.tuple(foo.get(Foo_.fstring)))));
        List<Tuple> result = em.createQuery(q).getResultList();
        assertFalse(result.isEmpty());
        Tuple tuple = result.get(0);
        
        assertEquals(Foo.class,   tuple.get(0).getClass());
        assertTrue(Tuple.class.isAssignableFrom(tuple.get(1).getClass()));
        Tuple tuple2 = (Tuple)tuple.get(1);
        assertEquals(Foo.class,   tuple2.get(0).getClass());
        assertEquals(Object[].class, tuple2.get(1).getClass());
        Object[] level3 = (Object[])tuple2.get(1);
        assertEquals(Integer.class, level3[0].getClass());
        assertTrue(Tuple.class.isAssignableFrom(level3[1].getClass()));
        Tuple tuple4 = (Tuple)level3[1];
        assertEquals(String.class, tuple4.get(0).getClass());
    }
    
    public void testConstructorFailsFast() {
        CriteriaQuery<Tuple> q = cb.createQuery(Tuple.class);
        Root<Foo> foo = q.from(Foo.class);
        try {
            q.multiselect(cb.construct(Foo.class, foo.get(Foo_.flong)));
            fail("Expected IllegalArgumentException becuase Foo(long) is not a valid constructor");
        } catch (IllegalArgumentException e) {
            // good -- but print the error message to check it is informative enough
            System.err.println(e);
        }
        try {
            q.multiselect(cb.construct(Foo.class));
            fail("Expected IllegalArgumentException becuase Foo() is not a valid constructor");
        } catch (IllegalArgumentException e) {
            // good -- but print the error message to check it is informative enough
            System.err.println(e);
        }
        
    }
    
// =============== assertions by result types ========================
    
    void assertResult(CriteriaQuery<?> q, Class<?> resultClass) {
        assertResult(q, resultClass, (Class<?>[])null);
    }
    /**
     * Assert the query result elements by their types 
     */
    void assertResult(CriteriaQuery<?> q, Class<?> resultClass, Class<?>... arrayElementClasses) {
        List<?> result = em.createQuery(q).getResultList();
        assertFalse(result.isEmpty());
        for (Object row : result) {
            assertTrue(toClass(row) + " does not match actual result " + toString(resultClass), 
                resultClass.isInstance(row));
            if (resultClass.isArray() && arrayElementClasses != null) {
                for (int i = 0; i < arrayElementClasses.length; i++) {
                    Object element = Array.get(row, i);
                    if (Tuple.class.isInstance(element)) {
                        assertEquals(arrayElementClasses[i], Tuple.class.cast(element).get(0).getClass()); 
                    } else {
                    assertTrue(i + "-th array element " + toString(arrayElementClasses[i]) + 
                       " does not match actual result " + toClass(element), arrayElementClasses[i].isInstance(element));
                    }
                }
            }
            if (resultClass == Tuple.class && arrayElementClasses != null) {
                Tuple tuple = (Tuple)row;
                for (int i = 0; i < arrayElementClasses.length; i++) {
                    Object element = tuple.get(i);
                    assertTrue(i + "-th tuple element " + toString(arrayElementClasses[i]) + 
                       " does not match actual result " + toClass(element), arrayElementClasses[i].isInstance(element));
                }
            }
        }
    }
    
    
    void fail(String msg, CriteriaQuery<?> q) {
        try {
            em.createQuery(q).getResultList();
            fail("Expected to fail " + msg);
        } catch (Exception e) {
            // this is an expected exception
        }
    }
    
    String toClass(Object o) {
       return toString(o.getClass());
    }
    
    String toString(Class<?> cls) {
        return cls.isArray() ? toString(cls.getComponentType())+"[]" : cls.toString();
    }
}
