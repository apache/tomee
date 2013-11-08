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
package org.apache.openjpa.kernel;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.apache.openjpa.lib.rop.ResultList;
import org.apache.openjpa.util.RuntimeExceptionTranslator;

/**
 * An immutable list that imposes uniqueness on its member. This implementation
 * traverses the entire result list on construction. So it is not suitable or
 * efficient for large results. All mutation operations (except clear()) throw
 * {@link UnsupportedOperationException}.
 * 
 * @author Pinaki Poddar
 * @since 2.0.0
 * 
 * @param <E>
 *            element type
 */
public class DistinctResultList<E> implements List<E>, Serializable {
    private static final long serialVersionUID = -6140119764940777922L;

    private final ArrayList<E> _del;
    private final RuntimeExceptionTranslator _trans;

    public DistinctResultList(ResultList<E> list, RuntimeExceptionTranslator trans) {
        _del = new ArrayList<E>();
        _trans = trans;
        for (E e : list) {
            if (!_del.contains(e))
                _del.add(e);
        }
    }

    public boolean add(E o) {
        throw new UnsupportedOperationException();
    }

    public void add(int index, E element) {
        throw new UnsupportedOperationException();
    }

    public boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    public boolean addAll(int index, Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    public void clear() {
        try {
            _del.clear();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public boolean contains(Object o) {
        try {
            return _del.contains(o);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public boolean containsAll(Collection<?> c) {
        try {
            return _del.containsAll(c);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public E get(int index) {
        try {
            return _del.get(index);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public int indexOf(Object o) {
        try {
            return _del.indexOf(o);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public boolean isEmpty() {
        try {
            return _del.isEmpty();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public Iterator<E> iterator() {
        try {
            return _del.iterator();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public int lastIndexOf(Object o) {
        try {
            return _del.indexOf(o);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public ListIterator<E> listIterator() {
        try {
            return _del.listIterator();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public ListIterator<E> listIterator(int index) {
        try {
            return _del.listIterator(index);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    public E remove(int index) {
        throw new UnsupportedOperationException();
    }

    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    public E set(int index, E element) {
        throw new UnsupportedOperationException();
    }

    public int size() {
        try {
            return _del.size();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public List<E> subList(int fromIndex, int toIndex) {
        try {
            return _del.subList(fromIndex, toIndex);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public Object[] toArray() {
        try {
            return _del.toArray();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public <T> T[] toArray(T[] a) {
        try {
            return _del.toArray(a);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    protected RuntimeException translate(RuntimeException re) {
        return (_trans == null) ? re : _trans.translate(re);
    }

    public Object writeReplace()
        throws ObjectStreamException {
        return _del;
    }

}
