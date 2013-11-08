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

/**
 * ResultList implementation that uses a forward-scrolling window of results.
 *
 * @author Abe White
 * @nojavadoc
 */
public class WindowResultList extends AbstractNonSequentialResultList {

    private static final int OPEN = 0;
    private static final int FREED = 1;
    private static final int CLOSED = 2;

    private final Object[] _window;
    private int _pos = -1;
    private ResultObjectProvider _rop = null;
    private boolean _random = false;
    private int _state = OPEN;
    private int _size = -1;

    public WindowResultList(ResultObjectProvider rop) {
        this(rop, 10);
    }

    public WindowResultList(ResultObjectProvider rop, int windowSize) {
        _rop = rop;

        if (windowSize <= 0)
            windowSize = 10;
        _window = new Object[windowSize];

        try {
            _rop.open();
            _random = _rop.supportsRandomAccess();
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

    public int size() {
        assertOpen();
        if (_size != -1)
            return _size;
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

    public Object getInternal(int index) {
        // out of range?
        if (index < 0 || (_size != -1 && index >= _size))
            return PAST_END;

        try {
            // if this is before window range, move window back
            if (index < _pos) {
                if (!_random || index == 0)
                    _rop.reset();
                _pos = -1;
            }

            // if this is the first get or past window range, move window
            if (_pos == -1 || index >= _pos + _window.length) {
                // position result provider just before requested index
                if (_random && index != 0) {
                    if (!_rop.absolute(index - 1))
                        return PAST_END;
                } else {
                    int begin = (_pos == -1) ? 0 : _pos + _window.length;
                    for (int i = begin; i < index; i++)
                        if (!_rop.next())
                            return PAST_END;
                }

                // create window starting at requested index
                int end = -1;
                for (int i = 0; i < _window.length; i++) {
                    if (end == -1 && !_rop.next())
                        end = i;
                    _window[i] = (end == -1) ? _rop.getResultObject()
                        : PAST_END;
                }
                _pos = index;

                // if the window spans the entire result list, free
                if (end != -1 && _pos == 0) {
                    _size = end;
                    free();
                }
            }

            // grab result from window
            return _window[index - _pos];
        } catch (RuntimeException re) {
            close();
            throw re;
        } catch (Exception e) {
            close();
            _rop.handleCheckedException(e);
            return null;
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
        if (_state != OPEN)
            return this;

        // load results into list
        List list = new ArrayList();
        for (Iterator itr = iterator(); itr.hasNext();)
            list.add(itr.next());
        return list;
    }
}
