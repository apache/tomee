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

import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * Lazy forward-only result list.
 *
 * @author Abe White
 * @nojavadoc
 */
public class LazyForwardResultList extends AbstractSequentialResultList
    implements ResultList {

    private static final int OPEN = 0;
    private static final int CLOSED = 1;
    private static final int FREED = 2;

    private ResultObjectProvider _rop = null;
    private final List _list = new ArrayList();
    private int _state = OPEN;
    private int _size = -1;

    public LazyForwardResultList(ResultObjectProvider rop) {
        _rop = rop;
        try {
            _rop.open();
        } catch (RuntimeException re) {
            close();
            throw re;
        } catch (Exception e) {
            close();
            _rop.handleCheckedException(e);
        }
    }

    public boolean isProviderOpen() {
        return _state == OPEN;
    }

    public boolean isClosed() {
        return _state == CLOSED;
    }

    public void close() {
        if (_state != CLOSED) {
            free();
            _state = CLOSED;
        }
    }

    public Object get(int index) {
        assertOpen();

        // optimization for getting sequntially
        if (index == _list.size())
            addNext();
        if (index < _list.size())
            return _list.get(index);

        return super.get(index);
    }

    protected ListIterator itr(int index) {
        return (_state != OPEN) ? _list.listIterator(index) : new Itr(index);
    }

    public int size() {
        assertOpen();
        if (_size != -1)
            return _size;
        if (_state != OPEN)
            return _list.size();
        try {
            _size = _rop.size();
            return _size;
        } catch (RuntimeException re) {
            close();
            throw re;
        } catch (Exception e) {
            close();
            _rop.handleCheckedException(e);
            return -1;
        }
    }

    private boolean addNext() {
        try {
            if (!_rop.next()) {
                free();
                return false;
            }
            _list.add(_rop.getResultObject());
            return true;
        } catch (RuntimeException re) {
            close();
            throw re;
        } catch (Exception e) {
            close();
            _rop.handleCheckedException(e);
            return false;
        }
    }

    private void free() {
        if (_state == OPEN) {
            try {
                _rop.close();
            } catch (Exception e) {
            }
            _state = FREED;
        }
    }

    public Object writeReplace() throws ObjectStreamException {
        // fully traverse results
        if (_state == OPEN)
            for (Iterator itr = itr(_list.size()); itr.hasNext();)
                itr.next();
        return _list;
    }

    public int hashCode() {
        // superclass tries to traverses entire list for hashcode
        return System.identityHashCode(this);
    }

    public boolean equals(Object other) {
        // superclass tries to traverse entire list for equality
        return other == this;
    }

    public List subList(int fromIndex, int toIndex) {
        assertOpen();
        return _list.subList(fromIndex, toIndex);
    }

    private class Itr extends AbstractListIterator {

        private int _idx = 0;

        public Itr(int index) {
            _idx = Math.min(index, _list.size());
            while (_idx < index)
                next();
        }

        public int nextIndex() {
            return _idx;
        }

        public int previousIndex() {
            return _idx - 1;
        }

        public boolean hasNext() {
            if (_list.size() > _idx)
                return true;
            if (_state != OPEN)
                return false;
            return addNext();
        }

        public boolean hasPrevious() {
            return _idx > 0;
        }

        public Object previous() {
            if (_idx == 0)
                throw new NoSuchElementException();
            return _list.get(--_idx);
        }

        public Object next() {
            if (!hasNext())
                throw new NoSuchElementException();
            return _list.get(_idx++);
        }
    }
}
