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
package org.apache.openjpa.lib.util.concurrent;

import java.io.IOException;
import java.util.Set;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.openjpa.lib.test.AbstractTestCase;

public class TestNullSafeConcurrentHashMap extends AbstractTestCase {

    private NullSafeConcurrentHashMap newMap() {
        return new NullSafeConcurrentHashMap();
    }

    public void testRemoveRandomIsNotTotallyDeterministic() {
        removeHelper(false);
    }

    public void testRandomIteratorIsNotTotallyDeterministic() {
        removeHelper(true);
    }

    private void removeHelper(boolean iter) {
        Map<String,Integer> removedCounts = new HashMap();
        for (int i = 0; i < 1000; i++) {
            NullSafeConcurrentHashMap m = new NullSafeConcurrentHashMap();
            m.put("a", "A");
            m.put("b", "B");
            m.put("c", "C");
            m.put("d", "D");
            m.put("e", "E");
            m.put("f", "F");
            m.put("g", "G");

            String removed;
            if (iter) {
                removed = (String) m.removeRandom().getKey();
            } else {
                removed = (String) ((Entry) m.randomEntryIterator().next())
                    .getKey();
                m.remove(removed);
            }

            Integer count = removedCounts.get(removed);
            if (count == null)
                removedCounts.put(removed, 1);
            else
                removedCounts.put(removed, count.intValue() + 1);
        }

        // assume that over 1000 runs, every element should be removed at
        // least once, and no element should be removed more than 30% of
        // the time
        assertEquals(7, removedCounts.size());
        for (Entry<String,Integer> entry : removedCounts.entrySet()) {
            if (entry.getValue() == 0)
                fail("element " + entry.getKey() + " was never removed");
            if (entry.getValue() > 500)
                fail("element " + entry.getKey() + " was removed "
                    + entry.getValue() + " times; this is greater than the "
                    + "threshold of 500.");
        }
    }

    public void testNullKeys() throws ClassNotFoundException, IOException {
        helper(null, "value 0", "value 1", "value 2");
    }

    private void helper(Object key, Object value0,
        Object value1, Object value2)
        throws IOException, ClassNotFoundException {

        NullSafeConcurrentHashMap m = newMap();

        // initial put
        m.put(key, value0);

        // get etc.
        assertEquals(value0, m.get(key));
        assertTrue(m.containsKey(key));
        assertTrue(m.containsValue(value0));

        // keySet
        Set keys = m.keySet();
        assertTrue(keys.contains(key));
        assertEquals(1, keys.size());
        assertEquals(key, keys.iterator().next());

        // entrySet
        Set entries = m.entrySet();
        Entry e = (Entry) entries.iterator().next();
        assertEquals(key, e.getKey());
        assertEquals(value0, e.getValue());

        // values
        Collection values = m.values();
        assertEquals(1, values.size());
        assertEquals(value0, values.iterator().next());

        // serializability
        assertEquals(m, roundtrip(m, true));

        // put
        assertEquals(value0, m.put(key, value1));

        // remove
        assertEquals(value1, m.put(key, value1));
        assertEquals(value1, m.remove(key));
        m.put(key, value1);

        // ConcurrentMap stuff
        assertFalse(m.remove("invalid key", value0));
        assertTrue(m.remove(key, value1));
        assertNull(m.putIfAbsent(key, value0)); // null == prev unset

        // value0 might be null; can't disambiguate from above in OpenJPA
        // interpretation
        assertEquals(value0, m.putIfAbsent(key, "invalid value"));

        // replace
        assertEquals(value0, m.replace(key, value1));
        assertTrue(m.replace(key, value1, value2));

        // putAll. Note that ConcurrentHashMap happens to delegate to put()
        // from within putAll() calls. This test should help ensure that we
        // find out if that changes.
        m = newMap();
        Map putAllArg = new HashMap();
        putAllArg.put(key, value0);
        putAllArg.put("another key", value1);
        m.putAll(putAllArg);
        assertEquals(value0, m.get(key));
        assertEquals(value1, m.get("another key"));
    }

    public void testNullValues() throws ClassNotFoundException, IOException {
        nullValsHelper("foo");
    }

    private void nullValsHelper(Object key)
        throws IOException, ClassNotFoundException {
        helper(key, null, null, null);
        helper(key, "bar", "baz", "quux");

        helper(key, "bar", "baz", null);
        helper(key, null, "baz", "quux");
        helper(key, "bar", null, "quux");

        helper(key, "bar", null, null);
        helper(key, null, "baz", null);
        helper(key, null, null, "quux");
    }

    public void testNullKeysAndValues()
        throws ClassNotFoundException, IOException {
        nullValsHelper(null);
    }
}
