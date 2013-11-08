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

import java.util.Comparator;

import org.apache.openjpa.lib.rop.MergedResultObjectProvider;
import org.apache.openjpa.lib.rop.ResultObjectProvider;

/**
 * Merged result object provider specialization that extracts ordering
 * values from results for comparison.
 *
 * @author Abe White
 * @nojavadoc
 */
public class OrderingMergedResultObjectProvider
    extends MergedResultObjectProvider {

    private final StoreQuery.Executor[] _execs;
    private final StoreQuery _query;
    private final Object[] _params;
    private final int _orderings;

    public OrderingMergedResultObjectProvider(ResultObjectProvider[] rops,
        boolean[] asc, StoreQuery.Executor exec, StoreQuery q,
        Object[] params) {
        this(rops, asc, new StoreQuery.Executor[]{ exec }, q, params);
    }

    public OrderingMergedResultObjectProvider(ResultObjectProvider[] rops,
        boolean[] asc, StoreQuery.Executor[] execs, StoreQuery q,
        Object[] params) {
        super(rops, new OrderingComparator(asc));
        _orderings = asc.length;
        _execs = execs;
        _query = q;
        _params = params;
    }

    protected Object getOrderingValue(Object val, int idx,
        ResultObjectProvider rop) {
        StoreQuery.Executor exec = (_execs.length == 1) ? _execs[0]
            : _execs[idx];
        if (_orderings == 1)
            return exec.getOrderingValue(_query, _params, val, 0);

        Object[] ret = new Object[_orderings];
        for (int i = 0; i < _orderings; i++)
            ret[i] = exec.getOrderingValue(_query, _params, val, i);
        return ret;
    }

    /**
     * Comparator that works on multiple ordering criteria given in an array.
     */
    private static class OrderingComparator
        implements Comparator {

        private final boolean[] _asc;

        public OrderingComparator(boolean[] asc) {
            _asc = asc;
        }

        public int compare(Object o1, Object o2) {
            if (_asc.length == 1)
                return cmp(o1, o2, _asc[0]);

            Object[] arr1 = (Object[]) o1;
            Object[] arr2 = (Object[]) o2;
            int cmp;
            for (int i = 0; i < _asc.length; i++) {
                cmp = cmp(arr1[i], arr2[i], _asc[i]);
                if (cmp != 0)
                    return cmp;
            }
            return 0;
        }

        private static int cmp(Object o1, Object o2, boolean asc) {
            if (o1 == null && o2 == null)
                return 0;
            if (o1 == null)
                return (asc) ? 1 : -1;
            if (o2 == null)
                return (asc) ? -1 : 1;
            int cmp = ((Comparable) o1).compareTo(o2);
            if (!asc)
                cmp *= -1;
            return cmp;
        }
    }
}

