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

import java.util.Comparator;

import org.apache.commons.lang.exception.NestableRuntimeException;

/**
 * A result object provider that merges multiple result object provider
 * delegates. Support exists for maintaining ordering of the internally held
 * results, provided that each of the individual results is itself ordered.
 *
 * @author Abe White
 * @author Marc Prud'hommeaux
 */
public class MergedResultObjectProvider implements ResultObjectProvider {

    private static final byte UNOPENED = 0;
    private static final byte OPENED = 1;
    private static final byte VALUE = 2;
    private static final byte DONE = 3;

    private final ResultObjectProvider[] _rops;
    private final Comparator _comp;
    private final byte[] _status;
    private Object[] _values;
    private Object[] _orderValues;
    private Object _cur = null;
    private int _size = -1;

    /**
     * Constructor. Provide delegates.
     */
    public MergedResultObjectProvider(ResultObjectProvider[] rops) {
        this(rops, null);
    }

    /**
     * Constructor. Provide delegates and optional comparator.
     */
    public MergedResultObjectProvider(ResultObjectProvider[] rops,
        Comparator comp) {
        _rops = rops;
        _comp = comp;
        _status = new byte[rops.length];
        _values = (comp == null) ? null : new Object[rops.length];
        _orderValues = (comp == null) ? null : new Object[rops.length];
    }

    public boolean supportsRandomAccess() {
        return false;
    }

    public void open() throws Exception {
        // if we have a comparator, then open all; else open first
        int len = (_comp != null) ? _rops.length : 1;
        for (int i = 0; i < len; i++) {
            _rops[i].open();
            _status[i] = OPENED;
        }
    }

    public boolean absolute(int pos) throws Exception {
        throw new UnsupportedOperationException();
    }

    public int size() throws Exception {
        if (_size != -1)
            return _size;

        // have to open all to get sizes
        for (int i = 0; i < _status.length; i++) {
            if (_status[i] == UNOPENED) {
                _rops[i].open();
                _status[i] = OPENED;
            }
        }

        int total = 0;
        int size;
        for (int i = 0; i < _rops.length; i++) {
            size = _rops[i].size();
            if (size == Integer.MAX_VALUE) {
                total = size;
                break;
            }
            total += size;
        }
        _size = total;
        return _size;
    }

    public void reset() throws Exception {
        for (int i = 0; i < _rops.length; i++)
            if (_status[i] != UNOPENED)
                _rops[i].reset();
        clear();
    }

    public void close() throws Exception {
        Exception err = null;
        for (int i = 0; i < _rops.length; i++) {
            try {
                if (_status[i] != UNOPENED)
                    _rops[i].close();
            } catch (Exception e) {
                if (err == null)
                    err = e;
            }
        }

        clear();
        if (err != null)
            throw err;
    }

    private void clear() {
        _cur = null;
        for (int i = 0; i < _rops.length; i++) {
            _status[i] = OPENED;
            if (_values != null)
                _values[i] = null;
            if (_orderValues != null)
                _orderValues[i] = null;
        }
    }

    public void handleCheckedException(Exception e) {
        if (_rops.length == 0)
            throw new NestableRuntimeException(e);
        _rops[0].handleCheckedException(e);
    }

    public boolean next() throws Exception {
        // initialize all rops with the latest values
        boolean hasValue = false;
        for (int i = 0; i < _status.length; i++) {
            switch (_status[i]) {
                case UNOPENED:
                    // this will only ever be the case if we aren't ordering
                    _rops[i].open();
                    _status[i] = OPENED;
                    // no break
                case OPENED:
                    // if this rop has a value, cache it; if we're not ordering,
                    // then that's the value to return
                    if (_rops[i].next()) {
                        if (_comp == null) {
                            _cur = _rops[i].getResultObject();
                            return true;
                        } else {
                            hasValue = true;
                            _status[i] = VALUE;
                            _values[i] = _rops[i].getResultObject();
                            _orderValues[i] = getOrderingValue(_values[i],
                                i, _rops[i]);
                        }
                    } else
                        _status[i] = DONE;
                    break;
                case VALUE:
                    // we only use this state when ordering
                    hasValue = true;
                    break;
            }
        }

        // if we get to this point without a comparator, it means none
        // of our rops have any more values
        if (_comp == null || !hasValue)
            return false;

        // for all the rops with values, find the 'least' one according to
        // the comparator
        int least = -1;
        Object orderVal = null;
        for (int i = 0; i < _orderValues.length; i++) {
            if (_status[i] != VALUE)
                continue;
            if (least == -1 || _comp.compare(_orderValues[i], orderVal) < 0) {
                least = i;
                orderVal = _orderValues[i];
            }
        }

        // assign the least value to the current one, and clear the cached
        // value for that rop so that we know to get the next value for
        // the next comparison
        _cur = _values[least];
        _values[least] = null;
        _orderValues[least] = null;
        _status[least] = OPENED;
        return true;
    }

    public Object getResultObject() throws Exception {
        return _cur;
    }

    /**
     * Return the value to use for ordering on the given result value. Returns
     * the result value by default.
     *
     * @param val the result value
     * @param idx the index of the result object provider in the array
     * given on construction that produced the result value
     * @param rop the result object provider that produced the result value
     */
    protected Object getOrderingValue(Object val, int idx,
        ResultObjectProvider rop) {
        return val;
    }
}
