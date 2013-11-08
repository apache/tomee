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

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import org.apache.commons.lang.ObjectUtils;

/**
 * Abstract base class for random-access result lists. Unlike the
 * {@link AbstractList}, this class doesn't rely on the
 * {@link Collection#size} method.
 *
 * @author Abe White
 * @nojavadoc
 */
public abstract class AbstractNonSequentialResultList
    extends AbstractResultList {

    protected static final Object PAST_END = new Object();

    /**
     * Implement this method and {@link #size}. Return {@link #PAST_END}
     * if the index is out of bounds.
     */
    protected abstract Object getInternal(int index);

    public boolean contains(Object o) {
        assertOpen();
        Object obj;
        for (int i = 0; true; i++) {
            obj = getInternal(i);
            if (obj == PAST_END)
                break;
            if (ObjectUtils.equals(o, obj))
                return true;
        }
        return false;
    }

    public boolean containsAll(Collection c) {
        assertOpen();
        for (Iterator itr = c.iterator(); itr.hasNext();)
            if (!contains(itr.next()))
                return false;
        return true;
    }

    public Object get(int index) {
        assertOpen();
        Object obj = getInternal(index);
        if (obj == PAST_END)
            throw new NoSuchElementException();
        return obj;
    }

    public int indexOf(Object o) {
        assertOpen();
        Object obj;
        for (int i = 0; true; i++) {
            obj = getInternal(i);
            if (obj == PAST_END)
                break;
            if (ObjectUtils.equals(o, obj))
                return i;
        }
        return -1;
    }

    public int lastIndexOf(Object o) {
        assertOpen();
        int index = -1;
        Object obj;
        for (int i = 0; true; i++) {
            obj = getInternal(i);
            if (obj == PAST_END)
                break;
            if (ObjectUtils.equals(o, obj))
                index = i;
        }
        return index;
    }

    public boolean isEmpty() {
        assertOpen();
        return getInternal(0) == PAST_END;
    }

    public Iterator iterator() {
        return listIterator();
    }

    public ListIterator listIterator() {
        return listIterator(0);
    }

    public ListIterator listIterator(int index) {
        return new ResultListIterator(new Itr(index), this);
    }

    public Object[] toArray() {
        assertOpen();
        ArrayList list = new ArrayList();
        Object obj;
        for (int i = 0; true; i++) {
            obj = getInternal(i);
            if (obj == PAST_END)
                break;
            list.add(obj);
        }
        return list.toArray();
    }

    public Object[] toArray(Object[] a) {
        assertOpen();
        ArrayList list = new ArrayList();
        Object obj;
        for (int i = 0; true; i++) {
            obj = getInternal(i);
            if (obj == PAST_END)
                break;
            list.add(obj);
        }
        return list.toArray(a);
    }

    public List subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException();
    }

    private class Itr extends AbstractListIterator {

        private int _idx = 0;
        private Object _next = PAST_END;

        public Itr(int index) {
            _idx = index;
        }

        public int nextIndex() {
            return _idx;
        }

        public int previousIndex() {
            return _idx - 1;
        }

        public boolean hasNext() {
            _next = getInternal(_idx);
            return _next != PAST_END;
        }

        public boolean hasPrevious() {
            return _idx > 0;
        }

        public Object previous() {
            if (_idx == 0)
                throw new NoSuchElementException();
            return getInternal(--_idx);
        }

        public Object next() {
            if (!hasNext())
                throw new NoSuchElementException();
            _idx++;
            return _next;
        }
    }
}
