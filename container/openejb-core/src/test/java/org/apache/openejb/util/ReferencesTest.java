/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.util;

import static org.apache.openejb.util.References.sort;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

/**
 * @version $Rev$ $Date$
 */
public class ReferencesTest extends TestCase {

    private BeanVisitor visitor = new BeanVisitor();
    private List<Bean> beans;

    public void test() {

        beans = new ArrayList<Bean>();

        Bean a = bean("a");
        Bean b = bean("b", "a");
        Bean c = bean("c", "b");

        List<Bean> actual = sort(beans, visitor);

        assertEquals(expected(a, b, c), actual);
    }

    public void testSimple() {

        beans = new ArrayList<Bean>();

        Bean c = bean("c", "b", "a");
        Bean b = bean("b", "a");
        Bean a = bean("a");

        List<Bean> actual = sort(beans, visitor);

        assertEquals(expected(a, b, c), actual);
    }

    public void testOrder() {

        beans = new ArrayList<Bean>();

        Bean c = bean("c", "b", "a");
        Bean b = bean("b", "a");
        Bean a = bean("a");

        Bean f = bean("f", "e", "d");
        Bean e = bean("e", "d");
        Bean d = bean("d");

        List<Bean> actual = sort(beans, visitor);

        //assertEquals(expected(d, a, e, b, f, c), actual);
        assertEquals(expected(a, b, c, d, e, f), actual);
    }

    public void testOrder2() {

        beans = new ArrayList<Bean>();

        Bean c = bean("c", "b", "a");
        Bean a = bean("a");
        Bean b = bean("b", "a", "d");

        Bean f = bean("f", "e", "d", "c", "b", "a");
        Bean d = bean("d", "a");
        Bean e = bean("e", "d", "b", "a");

        List<Bean> actual = sort(beans, visitor);

        assertEquals(expected(a, d, b, c, e, f), actual);
    }


    public void testCircuit() {

        beans = new ArrayList<Bean>();

        Bean a = bean("a", "c");
        Bean b = bean("b", "a");
        Bean c = bean("c", "b");
        try {
            sort(beans, visitor);
            fail("Ciruit should have been detected");
        } catch (CircularReferencesException e) {
            List<List> circuits = e.getCircuits();
            assertEquals(1, circuits.size());
            assertEquals(expected(a, c, b, a), circuits.get(0));
        }
    }


    public void testCircuit2() {

        beans = new ArrayList<Bean>();

        Bean a = bean("a", "c");
        Bean b = bean("b", "a");
        Bean c = bean("c", "b");

        Bean d = bean("d", "f");
        Bean e = bean("e", "d");
        Bean f = bean("f", "e");

        try {
            sort(beans, visitor);
            fail("Ciruit should have been detected");
        } catch (CircularReferencesException cre) {
            List<List> circuits = cre.getCircuits();
            assertEquals(2, circuits.size());
            assertEquals(expected(a, c, b, a), circuits.get(0));
            assertEquals(expected(d, f, e, d), circuits.get(1));
        }
    }

    public void testCircuit3() {

        beans = new ArrayList<Bean>();

        Bean a = bean("a", "a", "b", "c");
        Bean b = bean("b", "a", "b", "c");
        Bean c = bean("c", "a", "b", "c");

        try {
            sort(beans, visitor);
            fail("Ciruit should have been detected");
        } catch (CircularReferencesException cre) {
            List<List> circuits = cre.getCircuits();
            assertEquals(7, circuits.size());
            Iterator<List> actual = circuits.listIterator();

            assertEquals(expected(a, a), actual.next());
            assertEquals(expected(b, b), actual.next());
            assertEquals(expected(c, c), actual.next());
            assertEquals(expected(a, b, a), actual.next());
            assertEquals(expected(a, c, a), actual.next());
            assertEquals(expected(b, c, b), actual.next());
            assertEquals(expected(a, b, c, a), actual.next());
        }
    }


    public void testNonSuchObject() {

        beans = new ArrayList<Bean>();

        Bean a = bean("a");
        Bean b = bean("b", "a");
        Bean c = bean("c", "b", "z");

        List<Bean> actual = null;
        try {
            actual = sort(beans, visitor);
            fail("An IllegalArgumentException should have been thrown");
        } catch (IllegalArgumentException e) {
            // pass
        }
    }

    // proof that this sort is not a stable sort -- i.e. disrupts order unnecessarily
    // this test needs to pass
    public void testNoReferences() {

        beans = new ArrayList<Bean>();

        Bean b = bean("b");
        Bean a = bean("a");
        Bean d = bean("d");
        Bean c = bean("c");
        Bean f = bean("f");
        Bean e = bean("e");

        List<Bean> actual = sort(beans, visitor);

        assertEquals(expected(b, a, d, c, f, e), actual);
    }

    // items are already in the right order
    // this should pass but doesn't
    public void testOrderedReferences() {

        beans = new ArrayList<Bean>();

        Bean b = bean("b");
        Bean a = bean("a");
        Bean d = bean("d","a", "b");
        Bean c = bean("c");
        Bean e = bean("e", "f");
        Bean f = bean("f");

        List<Bean> actual = sort(beans, visitor);

        assertEquals(expected(b, a, d, c, f, e), actual);
    }

    private List<Bean> expected(Bean... beans){
        return Arrays.asList(beans);
    }

    private Bean bean(String name, String... refs) {
        Bean bean = new Bean(name, refs);
        beans.add(bean);
        return bean;
    }


    public static class Bean {
        private final String name;
        private final Set<String> refs;

        public Bean(String name, String... refs) {
            this.name = name;
            this.refs = new LinkedHashSet<String>(refs.length);
            for (String s : refs) {
                this.refs.add(s);
            }
        }

        public String toString() {
            return name;
        }
    }

    public static class BeanVisitor implements References.Visitor<Bean> {
        public String getName(Bean t) {
            return t.name;
        }

        public Set<String> getReferences(Bean t) {
            return t.refs;
        }
    }
}
