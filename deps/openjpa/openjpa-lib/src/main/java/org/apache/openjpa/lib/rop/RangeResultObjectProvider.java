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

import java.util.NoSuchElementException;

import org.apache.openjpa.lib.util.Localizer;

/**
 * Prevents a view of a given range of indices from the delegate
 * result object provider.
 *
 * @author Abe White
 * @nojavadoc
 */
public class RangeResultObjectProvider implements ResultObjectProvider {

    private static final Localizer _loc = Localizer.forPackage
        (RangeResultObjectProvider.class);

    private final ResultObjectProvider _delegate;
    private final int _startIdx;
    private final int _endIdx;
    private int _idx = -1;

    /**
     * Constructor. Because this is a wrapper around some delegate,
     * and result object providers work with int indexes, neither the start
     * or end index can be greater than Integer.MAX_VALUE(with the exception
     * of Long.MAX_VALUE, which is used to indicate no limit).
     *
     * @param delegate the result object provider to delegate to
     * @param startIdx 0-based inclusive start index of the range
     * to present; must be &lt; Integer.MAX_VALUE
     * @param endIdx 0-based exclusive end index of the range to
     * present; must be &lt; Integer.MAX_VALUE, or Long.MAX_VALUE for no limit
     */
    public RangeResultObjectProvider(ResultObjectProvider delegate,
        long startIdx, long endIdx) {
        // use Integer.MAX_VALUE for no limit internally
        if (endIdx == Long.MAX_VALUE)
            endIdx = Integer.MAX_VALUE;

        _delegate = delegate;
        if (startIdx > Integer.MAX_VALUE || endIdx > Integer.MAX_VALUE)
            throw new IllegalArgumentException(_loc.get("range-too-high",
                String.valueOf(startIdx), String.valueOf(endIdx)).getMessage());

        _startIdx = (int) startIdx;
        _endIdx = (int) endIdx;
    }

    public boolean supportsRandomAccess() {
        return _delegate.supportsRandomAccess();
    }

    public void open() throws Exception {
        _delegate.open();
    }

    public Object getResultObject() throws Exception {
        if (_idx < _startIdx || _idx >= _endIdx)
            throw new NoSuchElementException(String.valueOf(_idx));
        return _delegate.getResultObject();
    }

    public boolean next() throws Exception {
        // advance up to just behind _startIdx if we haven't already
        while (_idx < _startIdx - 1) {
            if (_delegate.supportsRandomAccess()) {
                _idx = _startIdx - 1;
                if (!_delegate.absolute(_startIdx - 1))
                    return false;
            } else {
                _idx++;
                if (!_delegate.next())
                    return false;
            }
        }

        // make sure we're not falling off the end of the range
        if (_idx >= _endIdx - 1)
            return false;

        _idx++;
        return _delegate.next();
    }

    public boolean absolute(int pos) throws Exception {
        _idx = pos + _startIdx;
        if (_idx >= _endIdx)
            return false;
        return _delegate.absolute(_idx);
    }

    public int size() throws Exception {
        int size = _delegate.size();
        if (size == Integer.MAX_VALUE)
            return size;
        size = Math.min(_endIdx, size) - _startIdx;
        return (size < 0) ? 0 : size;
    }

    public void reset() throws Exception {
        _idx = -1;
        _delegate.reset();
    }

    public void close() throws Exception {
        _delegate.close();
    }

    public void handleCheckedException(Exception e) {
        _delegate.handleCheckedException(e);
    }
    
    public ResultObjectProvider getDelegate() {
        return _delegate;
    }
}

