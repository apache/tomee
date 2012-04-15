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
package org.apache.xbean.finder.util;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

public class SingleLinkedList<E> implements List<E> {

    private Entry<E> entry;
    private int size = 0;

    private class Entry<E> {

        private E value;
        private Entry next;

        private Entry(E value, Entry next) {
            this.value = value;
            this.next = next;
        }
    }


    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public boolean contains(Object o) {
        if (o == null) {
            for (E e : this) {
                if (null == e) return true;
            }
        } else {
            for (E e : this) {
                if (o.equals(e)) return true;
            }
        }

        return false;
    }

    public Iterator<E> iterator() {
        return values();
    }

    public Object[] toArray() {
        final Object[] array = new Object[size];
        return toArray(array);
    }

    public <T> T[] toArray(T[] a) {
        if (a.length < size) a = (T[]) Array.newInstance(a.getClass().getComponentType(), size);
        
        Object[] array = a;
        int i = 0;

        for (E e : this) {
            array[i++] = e;
        }
        
        return (T[]) array;
    }

    public boolean add(E e) {
        this.entry = new Entry(e, this.entry); 
        size++;
        return true;
    }

    public boolean remove(Object o) {
        throw new UnsupportedOperationException("remove");
    }

    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException("containsAll");
    }

    public boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException("addAll");
    }

    public boolean addAll(int index, Collection<? extends E> c) {
        throw new UnsupportedOperationException("addAll");
    }

    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("removeAll");
    }

    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("retainAll");
    }

    public void clear() {
        this.entry = null;
        this.size = 0; 
    }

    public E get(int index) {
        bounds(index);
        int i = size;
        for (E e : this) {
            if (--i == index) return e;
        }

        throw new IllegalStateException("statement should not be reachable");
    }

    public E set(int index, E element) {
        bounds(index);
        int i = size;

        for (Entry<E> entry : entries()) {
            if (--i == index) {
                final E old = entry.value;
                entry.value = element;
                return old;
            }
        }

        throw new IllegalStateException("statement should not be reachable");
    }

    public void add(int index, E element) {
        throw new UnsupportedOperationException("add");
    }

    public E remove(int index) {
        throw new UnsupportedOperationException("remove");
    }

    public int indexOf(Object o) {
        throw new UnsupportedOperationException("indexOf");
    }

    public int lastIndexOf(Object o) {
        throw new UnsupportedOperationException("lastIndexOf");
    }

    public ListIterator<E> listIterator() {
        throw new UnsupportedOperationException("listIterator");
    }

    public ListIterator<E> listIterator(int index) {
        throw new UnsupportedOperationException("listIterator");
    }

    public List<E> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException("subList");
    }

    private void bounds(int index) {
        if (index >= size) throw new IndexOutOfBoundsException(index + " [size " + size + "]");
        if (index < 0) throw new IndexOutOfBoundsException(index + " [size " + size + "]");
    }


    private Iterator<E> values() {
        return new Values<E>(this.entry);
    }

    private Entries entries() {
        return new Entries(this.entry);
    }
    

    private class Values<E> implements Iterator<E> {

        private Entry<E> current;

        private Values(Entry<E> current) {
            this.current = current;
        }

        public boolean hasNext() {
            return current != null;
        }

        public E next() {
            if (current == null) throw new NoSuchElementException();
            
            final E v = current.value;

            this.current = current.next;

            return v;
        }

        public void remove() {
            throw new UnsupportedOperationException("remove");
        }
    }

    private class Entries implements Iterator<Entry<E>>, Iterable<Entry<E>> {
        private Entry<E> current;

        private Entries(Entry<E> current) {
            this.current = current;
        }

        public Iterator<Entry<E>> iterator() {
            return this;
        }

        public boolean hasNext() {
            return current != null;
        }

        public Entry<E> next() {
            if (current == null) throw new NoSuchElementException();

            final Entry<E> value = this.current;

            this.current = current.next;

            return value;
        }

        public void remove() {
            throw new UnsupportedOperationException("remove");
        }
    }
}
