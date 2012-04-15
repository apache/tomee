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
import org.apache.xbean.finder.util.SingleLinkedList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @version $Rev$ $Date$
 */
public class SingleLinkedListTest extends TestCase {
    private SingleLinkedList<String> list;
    private List<String> expected;

    @Override
    protected void setUp() throws Exception {
        list = new SingleLinkedList<String>();
        list.add("one");
        list.add("two");
        list.add("three");
        list.add("four");
        list.add("five");

        expected = Arrays.asList("five", "four", "three", "two", "one");
    }

    public void testIterator() throws Exception {
        ArrayList<String> arrayList = new ArrayList<String>();
        for (String s : list) {
            arrayList.add(s);
        }

        assertEquals(expected, arrayList);
    }

    public void testArrayListConstructor() throws Exception {
        ArrayList<String> arrayList = new ArrayList<String>(list);

        assertEquals(expected, arrayList);
    }

    public void testLinkedListConstructor() throws Exception {
        LinkedList<String> linkedList = new LinkedList<String>(list);

        assertEquals(expected, linkedList);
    }

    public void testToArrayWithWrongSize() {
        final String[] strings = list.toArray(new String[0]);

        assertEquals(expected, Arrays.asList(strings));
    }

    public void testToArrayWithRightSize() {
        final String[] strings = list.toArray(new String[5]);

        assertEquals(expected, Arrays.asList(strings));
    }

    public void testToArray() {
        final Object[] strings = list.toArray();

        assertEquals(expected, Arrays.asList(strings));
    }

    public void testContains() {
        assertTrue(list.contains("five"));
        assertFalse(list.contains("foo"));
    }

    public void testContainsNull() {
        assertFalse(list.contains(null));
    }

    public void testGet() {
        int i = 0;
        assertEquals("one", list.get(i++));
        assertEquals("two", list.get(i++));
        assertEquals("three", list.get(i++));
        assertEquals("four", list.get(i++));
        assertEquals("five", list.get(i++));
    }

    public void testGetInvalid() {

        try {
            list.get(-1);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // pass
        }


        try {
            list.get(5);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // pass
        }
    }


    public void testSet() {
        int i = 0;
        assertEquals("one", list.set(i++, "uno"));
        assertEquals("two", list.set(i++, "dos"));
        assertEquals("three", list.set(i++, "tres"));
        assertEquals("four", list.set(i++, "quatro"));
        assertEquals("five", list.set(i++, "cinco"));

        i = 0;
        assertEquals("uno", list.get(i++));
        assertEquals("dos", list.get(i++));
        assertEquals("tres", list.get(i++));
        assertEquals("quatro", list.get(i++));
        assertEquals("cinco", list.get(i++));
    }

    public void testSetInvalid() {

        try {
            list.set(-1, null);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // pass
        }


        try {
            list.set(5, null);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // pass
        }
    }
    
    public void testIsEmpty() {
        SingleLinkedList<String> temp = new SingleLinkedList<String>();
        assertTrue(temp.isEmpty());
        assertEquals(0, temp.size());
        temp.add("one"); 
        assertFalse(temp.isEmpty());
        assertEquals(1, temp.size());
        temp.clear(); 
        assertTrue(temp.isEmpty());
        assertEquals(0, temp.size());
    }

}
