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
 * An almost stateless {@link ResultList} designed for use with result
 * object providers backed by efficient random-access data structures, such
 * as the {@link ListResultObjectProvider}. This result list does not
 * perform any caching.
 *
 * @author Abe White
 * @nojavadoc
 */
public class SimpleResultList extends AbstractNonSequentialResultList {

    private final transient ResultObjectProvider _rop;
    private boolean _closed = false;
    private int _size = -1;

    public SimpleResultList(ResultObjectProvider rop) {
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
        return !_closed;
    }

    public boolean isClosed() {
        return _closed;
    }

    public void close() {
        if (!_closed) {
            _closed = true;
            try {
                _rop.close();
            } catch (Exception e) {
            }
        }
    }

    public Object getInternal(int index) {
        try {
            if (!_rop.absolute(index))
                return PAST_END;
            return _rop.getResultObject();
        } catch (RuntimeException re) {
            close();
            throw re;
        } catch (Exception e) {
            close();
            _rop.handleCheckedException(e);
            return PAST_END;
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

    public Object writeReplace() throws ObjectStreamException {
        if (_closed)
            return this;

        // load results into list
        List list = new ArrayList();
        for (Iterator itr = iterator(); itr.hasNext();)
            list.add(itr.next());
        return list;
    }
}
