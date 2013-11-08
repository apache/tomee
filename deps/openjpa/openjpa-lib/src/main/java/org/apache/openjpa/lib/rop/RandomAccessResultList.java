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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * Random-access result list implementation. It maintains a map
 * of the items that we have already instantiated.
 *
 * @author Marc Prud'hommeaux
 * @author Abe White
 * @nojavadoc
 */
public class RandomAccessResultList extends AbstractNonSequentialResultList {

    private static final int OPEN = 0;
    private static final int FREED = 1;
    private static final int CLOSED = 2;

    // data provider
    private ResultObjectProvider _rop = null;

    // holds all the row values that have been instantiated so far
    private Map _rows = null;
    private Object[] _full = null;

    // bookkeeping
    private long _requests = 0;
    private int _state = OPEN;
    private int _size = -1;

    public RandomAccessResultList(ResultObjectProvider rop) {
        _rop = rop;
        _rows = newRowMap();

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

    /**
     * Override this method to control what kind of map is used for
     * the instantiated rows.
     */
    protected Map newRowMap() {
        return new HashMap();
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

    protected Object getInternal(int index) {
        if (_full != null) {
            if (index >= _full.length)
                return PAST_END;
            return _full[index];
        }

        Integer i = index;
        Object ret = _rows.get(i);
        if (ret != null) {
            if (ret instanceof Null)
                return null;
            return ret;
        }

        ret = instantiateRow(i);
        return (ret == null) ? PAST_END : ret;
    }

    /**
     * Instantiate the row object at the specified index.
     */
    private Object instantiateRow(Integer i) {
        _requests++;
        try {
            if (!_rop.absolute(i.intValue()))
                return PAST_END;

            Object ob = _rop.getResultObject();
            if (ob == null)
                ob = new Null();

            // cache the result
            _rows.put(i, ob);

            // check to see if our map is full
            checkComplete();
            return ob;
        } catch (RuntimeException re) {
            close();
            throw re;
        } catch (Exception e) {
            close();
            _rop.handleCheckedException(e);
            return null;
        }
    }

    /**
     * Check to see if the soft map is the same size as all the
     * rows in the Result: if so, we copy over the values to a
     * hard reference HashSet and close the Result object associated with
     * this endeavour.
     */
    private void checkComplete() {
        // only check if we've actually gotten the size for some reason already
        if (_size == -1 || _rows.size() != _size)
            return;

        Object[] full = new Object[_size];
        int count = 0;
        Integer key;
        for (Iterator itr = _rows.keySet().iterator(); itr.hasNext(); count++) {
            key = (Integer) itr.next();
            full[key.intValue()] = _rows.get(key);
        }

        // double-check, in case any of the soft references were
        // cleaned up between the time we checked the size and the
        // time we completed the copy to the hard reference map
        if (count == _size) {
            _full = full;
            free();
        }
    }

    public int size() {
        assertOpen();
        if (_size != -1)
            return _size;
        if (_full != null)
            return _full.length;
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

    private void free() {
        if (_state == OPEN) {
            try {
                _rop.close();
            } catch (Exception e) {
            }
            _rows = null;
            _state = FREED;
        }
    }

    public Object writeReplace() throws ObjectStreamException {
        if (_full != null)
            return new ListResultList(Arrays.asList(_full));
        ArrayList list = new ArrayList();
        for (Iterator itr = iterator(); itr.hasNext();)
            list.add(itr.next());
        return list;
    }

    public String toString() {
        return getClass().getName()
            + "; identity: " + System.identityHashCode(this)
            + "; cached: " + _rows.size()
            + "; requests: " + _requests;
    }

    public int hashCode() {
        // superclass tries to traverses entire list for hashcode
        return System.identityHashCode(this);
    }

    public boolean equals(Object other) {
        // superclass tries to traverse entire list for equality
        return other == this;
    }

    /**
     * Used to represent nulls in the result list. Can't use a singleton
     * pattern, because then there will always be a hard ref to all the
     * nulls, and they'll never get GC'd; this is bad in the unlikely
     * event of a huge result set with lots of nulls.
     */
    private static class Null {

    }
}

