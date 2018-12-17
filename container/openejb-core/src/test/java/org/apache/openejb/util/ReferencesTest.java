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

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.apache.openejb.util.References.sort;

/**
 * @version $Rev$ $Date$
 */
public class ReferencesTest extends TestCase {

    private final BeanVisitor visitor = new BeanVisitor();
    private List<Bean> beans;

    public void testEmptyList() {
        beans = new ArrayList<>();
        assertEquals(0, sort(beans, visitor).size());
    }

    public void test() {

        beans = new ArrayList<>();

        final Bean a = bean("a");
        final Bean b = bean("b", "a");
        final Bean c = bean("c", "b");

        final List<Bean> actual = sort(beans, visitor);
        assertEquals(expected(a, b, c), actual);
    }

    public void testSimple() {

        beans = new ArrayList<>();

        final Bean c = bean("c", "b", "a");
        final Bean b = bean("b", "a");
        final Bean a = bean("a");

        final List<Bean> actual = sort(beans, visitor);

        assertEquals(expected(a, b, c), actual);
    }

    public void testOrder() {

        beans = new ArrayList<>();

        final Bean c = bean("c", "b", "a");
        final Bean b = bean("b", "a");
        final Bean a = bean("a");

        final Bean f = bean("f", "e", "d");
        final Bean e = bean("e", "d");
        final Bean d = bean("d");

        final List<Bean> actual = sort(beans, visitor);

        //assertEquals(expected(d, a, e, b, f, c), actual);
        assertEquals(expected(a, b, c, d, e, f), actual);
    }

    public void testOrder2() {

        beans = new ArrayList<>();

        final Bean c = bean("c", "b", "a");
        final Bean a = bean("a");
        final Bean b = bean("b", "a", "d");

        final Bean f = bean("f", "e", "d", "c", "b", "a");
        final Bean d = bean("d", "a");
        final Bean e = bean("e", "d", "b", "a");

        final List<Bean> actual = sort(beans, visitor);

        assertEquals(expected(a, d, b, c, e, f), actual);
    }


    public void testCircuit() {

        beans = new ArrayList<>();

        final Bean a = bean("a", "c");
        final Bean b = bean("b", "a");
        final Bean c = bean("c", "b");
        try {
            sort(beans, visitor);
            fail("Ciruit should have been detected");
        } catch (final CircularReferencesException e) {
            final List<List> circuits = e.getCircuits();
            assertEquals(1, circuits.size());
            assertEquals(expected(a, c, b, a), circuits.get(0));
        }
    }


    public void testCircuit2() {

        beans = new ArrayList<>();

        final Bean a = bean("a", "c");
        final Bean b = bean("b", "a");
        final Bean c = bean("c", "b");

        final Bean d = bean("d", "f");
        final Bean e = bean("e", "d");
        final Bean f = bean("f", "e");

        try {
            sort(beans, visitor);
            fail("Ciruit should have been detected");
        } catch (final CircularReferencesException cre) {
            final List<List> circuits = cre.getCircuits();
            assertEquals(2, circuits.size());
            assertEquals(expected(a, c, b, a), circuits.get(0));
            assertEquals(expected(d, f, e, d), circuits.get(1));
        }
    }

    public void testCircuit3() {

        beans = new ArrayList<>();

        final Bean a = bean("a", "a", "b", "c");
        final Bean b = bean("b", "a", "b", "c");
        final Bean c = bean("c", "a", "b", "c");

        try {
            sort(beans, visitor);
            fail("Ciruit should have been detected");
        } catch (final CircularReferencesException cre) {
            final List<List> circuits = cre.getCircuits();
            assertEquals(7, circuits.size());
            final Iterator<List> actual = circuits.listIterator();

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

        beans = new ArrayList<>();

        final Bean a = bean("a");
        final Bean b = bean("b", "a");
        final Bean c = bean("c", "b", "z");

        List<Bean> actual = null;
        try {
            actual = sort(beans, visitor);
            fail("An IllegalArgumentException should have been thrown");
        } catch (final IllegalArgumentException e) {
            // pass
        }
    }

    // proof that this sort is not a stable sort -- i.e. disrupts order unnecessarily
    // this test needs to pass
    public void testNoReferences() {

        beans = new ArrayList<>();

        final Bean b = bean("b");
        final Bean a = bean("a");
        final Bean d = bean("d");
        final Bean c = bean("c");
        final Bean f = bean("f");
        final Bean e = bean("e");

        final List<Bean> actual = sort(beans, visitor);

        assertEquals(expected(b, a, d, c, f, e), actual);
    }

    // items are already in the right order
    // this should pass but doesn't
    public void testOrderedReferences() {

        beans = new ArrayList<>();

        final Bean b = bean("b");
        final Bean a = bean("a");
        final Bean d = bean("d", "a", "b");
        final Bean c = bean("c");
        final Bean e = bean("e", "f");
        final Bean f = bean("f");

        final List<Bean> actual = sort(beans, visitor);

        assertEquals(expected(b, a, d, c, f, e), actual);
    }

    private List<Bean> expected(final Bean... beans) {
        return Arrays.asList(beans);
    }

    private Bean bean(final String name, final String... refs) {
        final Bean bean = new Bean(name, refs);
        beans.add(bean);
        return bean;
    }


    public static class Bean {
        private final String name;
        private final Set<String> refs;

        public Bean(final String name, final String... refs) {
            this.name = name;
            this.refs = new LinkedHashSet<>(refs.length);
            this.refs.addAll(Arrays.asList(refs));
        }

        public String toString() {
            return name;
        }
    }

    public static class BeanVisitor implements References.Visitor<Bean> {
        public String getName(final Bean t) {
            return t.name;
        }

        public Set<String> getReferences(final Bean t) {
            return t.refs;
        }
    }
}
