/*
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

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @version $Revision$ $Date$
 */
public class VersionedSet<E> {

    private Set<E> current;

    private ReadWriteLock sync = new ReentrantReadWriteLock();

    public boolean add(E o) {
        Lock lock = sync.writeLock();
        lock.lock();
        Set nextVersion = null;
        try {
            if (!current.set().contains(o)) {
                nextVersion = new Set(current);
                return nextVersion.set().add(o);
            } else {
                return false;
            }
        } finally {
            if (nextVersion != null) current = nextVersion;
            lock.unlock();
        }
    }

    public boolean remove(Object o) {
        Lock lock = sync.writeLock();
        lock.lock();
        Set nextVersion = null;
        try {
            if (current.set().contains(o)) {
                nextVersion = new Set(current);
                return nextVersion.set().remove(o);
            } else {
                return false;
            }
        } finally {
            if (nextVersion != null) current = nextVersion;
            lock.unlock();
        }
    }

    public Set<E> currentSet() {
        Lock lock = sync.readLock();
        lock.lock();
        try {
            return current;
        } finally {
            lock.unlock();
        }
    }

    public static class Set<E> implements java.util.Set<E> {
        private final int version;
        private final java.util.Set set;

        Set() {
            version = 0;
            set = new LinkedHashSet<E>();
        }

        Set(Set v) {
            this.version = v.version + 1;
            this.set = new LinkedHashSet<E>(v.set);
        }

        java.util.Set set() {
            return set;
        }

        public int getVersion() {
            return version;
        }

        public boolean add(Object o) {
            throw new UnsupportedOperationException();
        }

        public boolean addAll(Collection c) {
            throw new UnsupportedOperationException();
        }

        public void clear() {
            throw new UnsupportedOperationException();
        }

        public boolean contains(Object o) {
            return set.contains(o);
        }

        public boolean containsAll(Collection c) {
            return set.containsAll(c);
        }

        public boolean equals(Object o) {
            return set.equals(o);
        }

        public int hashCode() {
            return set.hashCode();
        }

        public boolean isEmpty() {
            return set.isEmpty();
        }

        public Iterator iterator() {
            return set.iterator();
        }

        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }

        public boolean removeAll(Collection c) {
            throw new UnsupportedOperationException();
        }

        public boolean retainAll(Collection c) {
            throw new UnsupportedOperationException();
        }

        public int size() {
            return set.size();
        }

        public Object[] toArray() {
            return set.toArray();
        }

        public Object[] toArray(Object[] a) {
            return set.toArray(a);
        }
    }
}
