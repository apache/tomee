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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.openjpa.lib.util.ReferenceMap;
import org.apache.openjpa.lib.test.AbstractTestCase;

/**
 * Tests the methods of {@link ConcurrentMap}.
 *
 * @author Abe White
 */
public class TestConcurrentMap extends AbstractTestCase {

    private static final int ENTRIES = 333;
    private static final int SLEEP = 3;

    private ConcurrentMap[] _maps = new ConcurrentMap[]{
        new SizedConcurrentHashMap(ENTRIES, .75f, 16), 
        new ConcurrentReferenceHashMap(ReferenceMap.HARD, ReferenceMap.HARD), };

    public void setUp() throws Exception {
        super.setUp();
        for (int i = 0; i < ENTRIES; i++) {
            for (int j = 0; j < _maps.length; j++) {
                int key = j * ENTRIES + i;
                _maps[j].put(new Integer(key), "v" + key);
            }
        }
        for (int i = 0; i < _maps.length; i++)
            assertEquals(ENTRIES, _maps[i].size());
    }

    public void testRemoveRandom() {
        Set keys = new TreeSet();
        for (int i = 0; i < ENTRIES; i++)
            for (int j = 0; j < _maps.length; j++)
                assertTrue(removeRandom(_maps[j], keys));
        postRemoveTest(keys);
    }

    private static boolean removeRandom(ConcurrentMap map, Set keys) {
        Map.Entry rem = map.removeRandom();
        return rem != null && rem.getValue().equals("v" + rem.getKey())
            && keys.add(rem.getKey());
    }

    private void postRemoveTest(Set keys) {
        for (int i = 0; i < _maps.length; i++) {
            assertTrue(_maps[i].isEmpty());
            assertTrue(!_maps[i].containsKey(new Integer(ENTRIES * i + i)));
        }
        assertEquals(keys.toString(), ENTRIES * _maps.length, keys.size());
    }

    public synchronized void testRemoveRandomThreaded()
        throws InterruptedException {
        Set keys = Collections.synchronizedSet(new TreeSet());
        RemoveRandomRunnable[] runs =
            new RemoveRandomRunnable[ENTRIES * _maps.length];
        for (int i = 0; i < ENTRIES; i++)
            for (int j = 0; j < _maps.length; j++)
                runs[j * ENTRIES + i] = new RemoveRandomRunnable
                    (_maps[j], keys);
        for (int i = 0; i < runs.length; i++)
            new Thread(runs[i]).start();
        Thread.currentThread().sleep(SLEEP * ENTRIES * _maps.length);
        for (int i = 0; i < runs.length; i++) {
            assertTrue(String.valueOf(i), !runs[i].error);
            if (runs[i].interrupted)
                throw new InterruptedException(String.valueOf(i));
        }
        postRemoveTest(keys);
    }

    public void testIterate() {
        iterationTest(false);
    }

    private List iterationTest(boolean random) {
        Set keys = new TreeSet();
        List ordered = new ArrayList(200);
        for (int i = 0; i < _maps.length; i++) {
            Iterator itr = (random) ? _maps[i].randomEntryIterator()
                : _maps[i].entrySet().iterator();
            while (itr.hasNext()) {
                Map.Entry entry = (Map.Entry) itr.next();
                assertEquals("v" + entry.getKey(), entry.getValue());
                assertTrue(keys + ":: " + _maps[i].getClass() + "::"
                    + entry.getKey() + "::" + entry.getValue(),
                    keys.add(entry.getKey()));
                ordered.add(entry.getKey());
            }
        }
        assertEquals(keys.toString(), ENTRIES * _maps.length, keys.size());
        return ordered;
    }

    public void testRandomIterate() {
        iterationTest(true);
    }

    private static class RemoveRandomRunnable implements Runnable {

        public boolean error = false;
        public boolean interrupted = false;

        private final ConcurrentMap _map;
        private final Set _keys;

        public RemoveRandomRunnable(ConcurrentMap map, Set keys) {
            _map = map;
            _keys = keys;
        }

        public synchronized void run() {
            try {
                Thread.currentThread().sleep((long) (Math.random() * SLEEP));
            } catch (InterruptedException ie) {
                interrupted = true;
            }
            error = !removeRandom(_map, _keys);
        }
    }
}
