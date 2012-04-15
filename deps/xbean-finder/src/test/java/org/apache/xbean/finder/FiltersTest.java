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
package org.apache.xbean.finder;

import junit.framework.TestCase;
import org.apache.xbean.finder.filter.ClassFilter;
import org.apache.xbean.finder.filter.ExcludeIncludeFilter;
import org.apache.xbean.finder.filter.Filter;
import org.apache.xbean.finder.filter.FilterList;
import org.apache.xbean.finder.filter.Filters;
import org.apache.xbean.finder.filter.IncludeExcludeFilter;

/**
 * @version $Rev$ $Date$
 */
public class FiltersTest extends TestCase {
    public void setUp() throws Exception {
        super.setUp();
    }

    public void testPackages() throws Exception {
        Filter filter = Filters.packages("org.foo", "org.bar");

        assertTrue(filter.accept("org.foo.Red"));
        assertTrue(filter.accept("org.bar.Orange"));

        assertFalse(filter.accept("org.fooo.Orange"));
        assertFalse(filter.accept("org.barr.Orange"));
        assertFalse(filter.accept("org"));
        assertFalse(filter.accept(""));
    }

    public void testClasses() throws Exception {
        Filter filter = Filters.classes("org.foo.Red", "org.foo.Blue");

        assertTrue(filter.accept("org.foo.Red"));
        assertTrue(filter.accept("org.foo.Blue"));

        assertFalse(filter.accept("org.foo.Orange"));
        assertFalse(filter.accept("org.foo.Redd"));
        assertFalse(filter.accept(""));
    }

    public void testPatterns() throws Exception {
        Filter filter = Filters.patterns("org\\.foo\\..*", ".*\\.Blue");

        assertTrue(filter.accept("org.foo.Red"));
        assertTrue(filter.accept("org.foo.Blue"));
        assertTrue(filter.accept("org.bar.Blue"));

        assertFalse(filter.accept("com.foo.Orange"));
        assertFalse(filter.accept("net.foo.Redd"));
        assertFalse(filter.accept(""));
    }

    public void testOptimize() throws Exception {

        ClassFilter foo = new ClassFilter("foo");
        ClassFilter foo2 = new ClassFilter("foo");
        ClassFilter foo3 = new ClassFilter("foo");

        FilterList filter = new FilterList(
                new FilterList(
                        new FilterList(
                                new FilterList(
                                        foo,
                                        new FilterList(
                                                foo,
                                                new FilterList(
                                                        new FilterList(foo, foo2, foo3)
                                                )

                                        )
                                )

                        )
                )
        );

        assertSame(foo, Filters.optimize(filter));
    }

    public void testIncludeExclude() {
        Filter filter = new IncludeExcludeFilter(Filters.packages("org.foo", "org.bar"), Filters.packages("org.foo.util"));

        assertTrue(filter.accept("org.foo.Red"));
        assertTrue(filter.accept("org.bar.Red"));

        assertFalse(filter.accept("com.bar.Red"));
        assertFalse(filter.accept("org.foo.util.Blue"));
    }

    public void testExcludeInclude() {
        Filter filter = new ExcludeIncludeFilter(Filters.packages("org.foo.util"), Filters.packages("org.foo", "org.bar"));

        assertFalse(filter.accept("org.foo.Red"));
        assertFalse(filter.accept("org.bar.Red"));

        assertTrue(filter.accept("com.bar.Red"));
        assertTrue(filter.accept("org.foo.util.Blue"));
    }

}
