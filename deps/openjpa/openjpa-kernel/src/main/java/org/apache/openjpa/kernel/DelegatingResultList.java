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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.apache.openjpa.lib.rop.ResultList;
import org.apache.openjpa.util.RuntimeExceptionTranslator;

/**
 * Delegating result list that can also perform exception translation
 * for use in facades.
 *
 * @since 0.4.0
 * @author Marc Prud'hommeaux
 * @nojavadoc
 */
public class DelegatingResultList<T>
    implements ResultList<T> {

    private final ResultList<T> _del;
    private final RuntimeExceptionTranslator _trans;

    /**
     * Constructor; supply delegate.
     */
    public DelegatingResultList(ResultList<T> list) {
        this(list, null);
    }

    /**
     * Constructor; supply delegate and exception translator.
     */
    public DelegatingResultList(ResultList<T> list, RuntimeExceptionTranslator trans) {
        _del = list;
        _trans = trans;
    }

    /**
     * Return the direct delegate.
     */
    public ResultList<T> getDelegate() {
        return _del;
    }

    /**
     * Return the native delegate.
     */
    public ResultList<T> getInnermostDelegate() {
        return _del instanceof DelegatingResultList
            ? ((DelegatingResultList<T>) _del).getInnermostDelegate() : _del;
    }

    /**
     * Writes delegate, which may in turn write a normal list.
     */
    public Object writeReplace()
        throws ObjectStreamException {
        return _del;
    }

    public int hashCode() {
        try {
            return getInnermostDelegate().hashCode();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public boolean equals(Object other) {
        if (other == this)
            return true;
        if (other instanceof DelegatingResultList)
            other = ((DelegatingResultList) other).getInnermostDelegate();
        try {
            return getInnermostDelegate().equals(other);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    /**
     * Translate the OpenJPA exception.
     */
    protected RuntimeException translate(RuntimeException re) {
        return (_trans == null) ? re : _trans.translate(re);
    }

    public boolean isProviderOpen() {
        try {
            return _del.isProviderOpen();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public Object getUserObject() {
        try {
            return _del.getUserObject();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }
    
    public void setUserObject(Object opaque) {
        try {
            _del.setUserObject(opaque);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public void close() {
        try {
            _del.close();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public boolean isClosed() {
        try {
            return _del.isClosed();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public int size() {
        try {
            return _del.size();
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

    public boolean contains(Object o) {
        try {
            return _del.contains(o);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public Iterator<T> iterator() {
        return listIterator();
    }

    public Object[] toArray() {
        try {
            return _del.toArray();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public Object[] toArray(Object[] a) {
        try {
            return _del.toArray(a);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public boolean add(T o) {
        try {
            return _del.add(o);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public boolean remove(Object o) {
        try {
            return _del.remove(o);
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

    public boolean addAll(Collection<? extends T> c) {
        try {
            return _del.addAll(c);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public boolean addAll(int index, Collection<? extends T> c) {
        try {
            return _del.addAll(index, c);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public boolean removeAll(Collection<?> c) {
        try {
            return _del.removeAll(c);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public boolean retainAll(Collection<?> c) {
        try {
            return _del.retainAll(c);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public void clear() {
        try {
            _del.clear();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public T get(int index) {
        try {
            return _del.get(index);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public T set(int index, T element) {
        try {
            return _del.set(index, element);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public void add(int index, T element) {
        try {
            _del.add(index, element);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public T remove(int index) {
        try {
            return _del.remove(index);
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

    public int lastIndexOf(Object o) {
        try {
            return _del.lastIndexOf(o);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public ListIterator<T> listIterator() {
        try {
            return new DelegatingListIterator<T>(_del.listIterator());
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public ListIterator<T> listIterator(int index) {
        try {
            return new DelegatingListIterator<T>(_del.listIterator(index));
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public List<T> subList(int fromIndex, int toIndex) {
        try {
            return _del.subList(fromIndex, toIndex);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }
    
    public String toString() {
        try {
            return _del.toString();
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    /**
     * Delegating iterator that also performs exception translation.
     */
    public class DelegatingListIterator<T>
        implements ListIterator<T> {

        private final ListIterator<T> _del;

        /**
         * Constructor; supply delegate.
         */
        public DelegatingListIterator(ListIterator<T> it) {
            _del = it;
        }

        /**
         * Return the direct delegate.
         */
        public ListIterator<T> getDelegate() {
            return _del;
        }

        /**
         * Return the native delegate.
         */
        public ListIterator<T> getInnermostDelegate() {
            return _del instanceof DelegatingListIterator
                ? ((DelegatingListIterator<T>) _del).getInnermostDelegate() : _del;
        }

        public int hashCode() {
            try {
                return getInnermostDelegate().hashCode();
            } catch (RuntimeException re) {
                throw translate(re);
            }
        }

        public boolean equals(Object other) {
            if (other == this)
                return true;
            if (other instanceof DelegatingListIterator)
                other = ((DelegatingListIterator) other).getInnermostDelegate();
            try {
                return getInnermostDelegate().equals(other);
            } catch (RuntimeException re) {
                throw translate(re);
            }
        }

        public boolean hasNext() {
            try {
                return _del.hasNext();
            } catch (RuntimeException re) {
                throw translate(re);
            }
        }

        public T next() {
            try {
                return _del.next();
            } catch (RuntimeException re) {
                throw translate(re);
            }
        }

        public boolean hasPrevious() {
            try {
                return _del.hasPrevious();
            } catch (RuntimeException re) {
                throw translate(re);
            }
        }

        public T previous() {
            try {
                return _del.previous();
            } catch (RuntimeException re) {
                throw translate(re);
            }
        }

        public int nextIndex() {
            try {
                return _del.nextIndex();
            } catch (RuntimeException re) {
                throw translate(re);
            }
        }

        public int previousIndex() {
            try {
                return _del.previousIndex();
            } catch (RuntimeException re) {
                throw translate(re);
            }
        }

        public void remove() {
            try {
                _del.remove();
            } catch (RuntimeException re) {
                throw translate(re);
            }
        }

        public void set(T o) {
            try {
                _del.set(o);
            } catch (RuntimeException re) {
                throw translate(re);
            }
        }

        public void add(T o) {
            try {
                _del.add(o);
            } catch (RuntimeException re) {
                throw translate (re);
			}
		}
	}
}

