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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * A basic {@link ResultList} implementation that wraps a normal list.
 *
 * @author Abe White
 * @nojavadoc
 */
public class ListResultList extends AbstractResultList {

    private final List _list;
    private boolean _closed = false;

    /**
     * Constructor. Supply delegate.
     */
    public ListResultList(List list) {
        _list = list;
    }

    /**
     * Return the wrapped list.
     */
    public List getDelegate() {
        return _list;
    }

    public boolean isProviderOpen() {
        return false;
    }

    public boolean isClosed() {
        return _closed;
    }

    public void close() {
        _closed = true;
    }

    public boolean contains(Object o) {
        assertOpen();
        return _list.contains(o);
    }

    public boolean containsAll(Collection c) {
        assertOpen();
        return _list.containsAll(c);
    }

    public Object get(int index) {
        assertOpen();
        return _list.get(index);
    }

    public int indexOf(Object o) {
        assertOpen();
        return _list.indexOf(o);
    }

    public int lastIndexOf(Object o) {
        assertOpen();
        return _list.lastIndexOf(o);
    }

    public int size() {
        assertOpen();
        return _list.size();
    }

    public boolean isEmpty() {
        assertOpen();
        return _list.isEmpty();
    }

    public Iterator iterator() {
        return listIterator();
    }

    public ListIterator listIterator() {
        return new ResultListIterator(_list.listIterator(), this);
    }

    public ListIterator listIterator(int index) {
        return new ResultListIterator(_list.listIterator(index), this);
    }

    public Object[] toArray() {
        assertOpen();
        return _list.toArray();
    }

    public Object[] toArray(Object[] a) {
        assertOpen();
        return _list.toArray(a);
    }

    public Object writeReplace() {
        return _list;
    }
    
    public String toString() {
    	return _list.toString();
    }

    public List subList(int fromIndex, int toIndex) {
        assertOpen();
        return _list.subList(fromIndex, toIndex);
    }
}
