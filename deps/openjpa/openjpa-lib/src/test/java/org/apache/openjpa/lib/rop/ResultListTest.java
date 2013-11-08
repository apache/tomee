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
package org.apache.openjpa.lib.rop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import org.apache.openjpa.lib.test.AbstractTestCase;

/**
 * Tests for {@link ResultList} implementations.
 *
 * @author Abe White
 */
public abstract class ResultListTest extends AbstractTestCase {

    private ResultList[] _lists = null;

    protected boolean subListSupported = false;
    
    public ResultListTest(String test) {
        super(test);
    }
    
    public ResultListTest(String test, boolean supportSubList) {
        super(test);
        subListSupported = supportSubList;
    }

    /**
     * Return a result list to use with the given provider.
     */
    protected abstract ResultList getResultList(ResultObjectProvider provider);

    /**
     * Override to customize the result object provider being used. You
     * can return multiple providers to test with each.
     */
    protected ResultObjectProvider[] getResultObjectProviders(List list) {
        return new ResultObjectProvider[]{
            new ListResultObjectProvider(list)
        };
    }

    public void setUp() {
        List results = new ArrayList(100);
        for (int i = 0; i < 100; i++)
            results.add(String.valueOf(i));
        ResultObjectProvider[] rops = getResultObjectProviders(results);
        _lists = new ResultList[rops.length];
        for (int i = 0; i < _lists.length; i++)
            _lists[i] = getResultList(rops[i]);
    }

    public void testIterator() {
        for (int i = 0; i < _lists.length; i++) {
            Iterator itr = _lists[i].iterator();
            int count = 0;
            for (; itr.hasNext(); count++)
                assertEquals(String.valueOf(count), itr.next());
            assertEquals(100, count);
            try {
                itr.next();
                fail("After last.");
            } catch (IndexOutOfBoundsException ioob) {
            } catch (NoSuchElementException nse) {
            }
        }
    }

    public void testIteratorModification() {
        for (int i = 0; i < _lists.length; i++) {
            try {
                Iterator itr = _lists[i].iterator();
                itr.next();
                itr.remove();
                fail("Allowed modification.");
            } catch (Exception e) {
            }
        }
    }

    public void testListIteratorForward() {
        for (int i = 0; i < _lists.length; i++) {
            ListIterator itr = _lists[i].listIterator();
            int count = 0;
            for (; itr.hasNext(); count++) {
                assertEquals(count, itr.nextIndex());
                assertEquals(String.valueOf(count), itr.next());
            }
            assertEquals(100, count);
            try {
                itr.next();
                fail("After last.");
            } catch (IndexOutOfBoundsException ioob) {
            } catch (NoSuchElementException nse) {
            }
        }
    }

    public void testListIteratorIndex() {
        for (int i = 0; i < _lists.length; i++) {
            ListIterator itr = _lists[i].listIterator(50);
            int count = 50;
            for (; itr.hasNext(); count++) {
                assertEquals(count, itr.nextIndex());
                assertEquals(String.valueOf(count), itr.next());
            }
            assertEquals(100, count);
            try {
                itr.next();
                fail("After last.");
            } catch (IndexOutOfBoundsException ioob) {
            } catch (NoSuchElementException nse) {
            }
        }
    }

    public void testListIteratorReverse() {
        for (int i = 0; i < _lists.length; i++) {
            ListIterator itr = _lists[i].listIterator(100);
            int count = 99;
            for (; itr.hasPrevious(); count--) {
                assertEquals(count, itr.previousIndex());
                assertEquals(String.valueOf(count), itr.previous());
            }
            assertEquals(-1, count);
            try {
                itr.previous();
                fail("Before first.");
            } catch (IndexOutOfBoundsException ioob) {
            } catch (NoSuchElementException nse) {
            }
        }
    }

    public void testListIteratorModification() {
        for (int i = 0; i < _lists.length; i++) {
            try {
                ListIterator itr = _lists[i].listIterator();
                itr.next();
                itr.set("foo");
                fail("Allowed modification.");
            } catch (Exception e) {
            }
        }
    }

    public void testMultipleIterations() {
        testListIteratorIndex();
        testListIteratorForward();
        testListIteratorReverse();
    }

    public void testContains() {
        for (int i = 0; i < _lists.length; i++) {
            assertTrue(_lists[i].contains("0"));
            assertTrue(_lists[i].contains("50"));
            assertTrue(_lists[i].contains("99"));
            assertFalse(_lists[i].contains("-1"));
            assertFalse(_lists[i].contains("100"));
            assertFalse(_lists[i].contains(null));
            assertTrue(_lists[i].containsAll(Arrays.asList(new String[]
                { "0", "50", "99" })));
            assertFalse(_lists[i].containsAll(Arrays.asList(new String[]
                { "0", "-1", "99" })));
        }
    }

    public void testModification() {
        for (int i = 0; i < _lists.length; i++) {
            try {
                _lists[i].add("foo");
                fail("Allowed modification.");
            } catch (UnsupportedOperationException uoe) {
            }
            try {
                _lists[i].remove("1");
                fail("Allowed modification.");
            } catch (UnsupportedOperationException uoe) {
            }
            try {
                _lists[i].set(0, "foo");
                fail("Allowed modification.");
            } catch (UnsupportedOperationException uoe) {
            }
        }
    }

    public void testGetBegin() {
        for (int i = 0; i < _lists.length; i++) {
            for (int j = 0; j < 10; j++)
                assertEquals(String.valueOf(j), _lists[i].get(j));
            try {
                _lists[i].get(-1);
                fail("Before begin.");
            } catch (IndexOutOfBoundsException ioob) {
            } catch (NoSuchElementException nse) {
            }
        }
    }

    public void testGetMiddle() {
        for (int i = 0; i < _lists.length; i++)
            for (int j = 50; j < 60; j++)
                assertEquals(String.valueOf(j), _lists[i].get(j));
    }

    public void testGetEnd() {
        for (int i = 0; i < _lists.length; i++) {
            for (int j = 90; j < 100; j++)
                assertEquals(String.valueOf(j), _lists[i].get(j));
            try {
                _lists[i].get(100);
                fail("Past end.");
            } catch (IndexOutOfBoundsException ioob) {
            } catch (NoSuchElementException nse) {
            }
        }
    }

    public void testGetReverse() {
        for (int i = 0; i < _lists.length; i++)
            for (int j = 99; j > -1; j--)
                assertEquals(String.valueOf(j), _lists[i].get(j));
    }

    public void testMultipleGet() {
        testGetMiddle();
        testGetBegin();
        testGetEnd();

        // take list size and traverse list to cache values if not already
        for (int i = 0; i < _lists.length; i++)
            _lists[i].size();
        testListIteratorForward();

        testGetMiddle();
        testGetBegin();
        testGetEnd();
    }

    public void testSize() {
        for (int i = 0; i < _lists.length; i++)
            assertTrue(_lists[i].size() == 100
                || _lists[i].size() == Integer.MAX_VALUE);
    }

    public void testEmpty() {
        ResultObjectProvider[] rops = getResultObjectProviders
            (Collections.EMPTY_LIST);
        for (int i = 0; i < rops.length; i++) {
            ResultList list = getResultList(rops[i]);
            assertEquals(0, list.size());
            assertTrue(list.isEmpty());
        }
    }

    public void testSubList() {
        ResultObjectProvider[] rops = getResultObjectProviders
            (Collections.EMPTY_LIST);
        for (int i = 0; i < rops.length; i++) {
            ResultList list = getResultList(rops[i]);
            try {
                List subList = list.subList(0, 0);
                if (subListSupported == false)
                    fail("Should not support subList.");
            } catch (UnsupportedOperationException e) {
                if (subListSupported == true)
                    fail("Should support subList.");
            }
        }
    }
}
