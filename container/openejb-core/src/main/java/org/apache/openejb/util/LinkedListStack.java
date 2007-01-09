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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.util;

public class LinkedListStack implements Stack {

    private LinkedEntry occupiedEntries;

    private LinkedEntry vacantEntries;

    private int size;

    public LinkedListStack(int initialSize) {
        for (int i = 0; i < initialSize; i++)
            vacantEntries = new LinkedEntry(null, vacantEntries);
    }

    public synchronized Object push(Object object) {
        /* Take an entry from the vacant list and move it to the occupied list. */

        if (vacantEntries == null)
            occupiedEntries = new LinkedEntry(object, occupiedEntries);
        else {

            LinkedEntry entry = vacantEntries;

            vacantEntries = vacantEntries.next;

            occupiedEntries = entry.set(object, occupiedEntries);
        }
        ++size;
        return object;
    }

    public synchronized Object pop() throws java.util.EmptyStackException {
        /* Take an entry from the occupied list and move it to the vacant list. */

        LinkedEntry entry = occupiedEntries;
        if (entry == null) return null;

        occupiedEntries = occupiedEntries.next;

        Object value = entry.value;
        vacantEntries = entry.set(null, vacantEntries);
        --size;
        return value;
    }

    public synchronized int size() {
        return size;
    }

    static class LinkedEntry {

        LinkedEntry next;
        Object value;

        LinkedEntry(Object value, LinkedEntry next) {
            set(value, next);
        }

        LinkedEntry set(Object value, LinkedEntry next) {
            this.next = next;
            this.value = value;
            return this;
        }
    }

}