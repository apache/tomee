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
package org.apache.openjpa.slice.jdbc;

import java.util.Date;

import org.apache.openjpa.kernel.Filters;
import org.apache.openjpa.kernel.StoreQuery;
import org.apache.openjpa.kernel.exps.QueryExpressions;
import org.apache.openjpa.kernel.exps.Value;
import org.apache.openjpa.lib.rop.ResultObjectProvider;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.util.InternalException;

/**
 * Aggregates individual single query results from different databases.
 * 
 * @author Pinaki Poddar 
 *
 */
public class UniqueResultObjectProvider implements ResultObjectProvider {
    private final ResultObjectProvider[] _rops;
    private final StoreQuery _query;
    private final QueryExpressions[] _exps;
    private Object _single;
    private boolean _opened;
    
    private static final String COUNT = "Count";
    private static final String MAX   = "Max";
    private static final String MIN   = "Min";
    private static final String SUM   = "Sum";
    
    private static final Localizer _loc =
        Localizer.forPackage(UniqueResultObjectProvider.class);
    
    public UniqueResultObjectProvider(ResultObjectProvider[] rops, 
            StoreQuery q, QueryExpressions[] exps) {
        _rops = rops;
        _query = q;
        _exps = exps;
    }
    
    public boolean absolute(int pos) throws Exception {
        return false;
    }

    public void close() throws Exception {
        _opened = false;
        for (ResultObjectProvider rop:_rops)
            rop.close();
    }

    public Object getResultObject() throws Exception {
        if (!_opened)
            throw new InternalException(_loc.get("not-open"));
        return _single;
    }

    public void handleCheckedException(Exception e) {
        _rops[0].handleCheckedException(e);
    }

    public boolean next() throws Exception {
        if (!_opened) {
            open();
        }
            
        if (_single != null)
            return false;
        
        Value[] values = _exps[0].projections;
        Object[] single = new Object[values.length]; 
        for (int i=0; i<values.length; i++) {
            Value v = values[i];
            boolean isAggregate = v.isAggregate();
            
            String op = v.getClass().getSimpleName();
            for (ResultObjectProvider rop:_rops) {
                if (i == 0) 
                	rop.next();
                Object[] row = (Object[]) rop.getResultObject();
                if (isAggregate) {
                    if (COUNT.equals(op)) {
                        single[i] = count(single[i], row[i]);
                    } else if (MAX.equals(op)) {
                        single[i] = max(single[i], row[i]);
                    } else if (MIN.equals(op)) {
                        single[i] = min(single[i], row[i]);
                    } else if (SUM.equals(op)) {
                        single[i] = sum(single[i], row[i]);
                    } else {
                        throw new UnsupportedOperationException
                            (_loc.get("aggregate-unsupported", op).toString());
                    }
                } else {
                    single[i] = row[i];
                }
                single[i] = Filters.convert(single[i], v.getType());
            }
        }
        _single = single;
        return true;
    }
    
    Object count(Object current, Object other) {
        if (current == null)
            return other;
        if (other == null)
        	return current;
        return ((Number)current).longValue() + ((Number)other).longValue();
    }
    
    Object max(Object current, Object other) {
        if (current == null)
            return other;
        if (other == null)
        	return current;
        if (current instanceof Number) {
        	return Math.max(((Number)current).doubleValue(), 
                ((Number)other).doubleValue());
        }
        if (current instanceof String) {
        	return  ((String)current).compareTo((String)other) > 0 ? current : other; 
        }
        if (current instanceof Date) {
        	return ((Date)current).compareTo((Date)other) > 0 ? current : other;
        }
        if (current instanceof Character) {
        	return ((Character)current).compareTo((Character)other) > 0 ? current : other;
        }
        throw new UnsupportedOperationException(_loc.get("aggregate-unsupported-on-type", 
        		"MAX()", (current == null ? other : current).getClass().getName()).toString());
    }
    
    Object min(Object current, Object other) {
        if (current == null)
            return other;
        if (other == null)
        	return current;
        if (current instanceof Number) {
	        return Math.min(((Number)current).doubleValue(), 
	                ((Number)other).doubleValue());
        }
        if (current instanceof String) {
        	return ((String)current).compareTo((String)other) < 0 ? current : other; 
        }
        if (current instanceof Date) {
        	return ((Date)current).compareTo((Date)other) < 0 ? current : other;
        }
        if (current instanceof Character) {
        	return ((Character)current).compareTo((Character)other) < 0 ? current : other;
        }
        throw new UnsupportedOperationException(_loc.get("aggregate-unsupported-on-type", 
        		"MIN()", (current == null ? other : current).getClass().getName()).toString());
    }
    
    Object sum(Object current, Object other) {
        if (current == null)
            return other;
        if (other == null)
        	return current;
        if (current instanceof Number) {
        	return (((Number)current).doubleValue() +
                ((Number)other).doubleValue());
        }
        throw new UnsupportedOperationException(_loc.get("aggregate-unsupported-on-type", 
        		"SUM()", (current == null ? other : current).getClass().getName()).toString());
    }
    
    public void open() throws Exception {
        for (ResultObjectProvider rop:_rops)
            rop.open();
        _opened = true;
    }

    public void reset() throws Exception {
        _single = null;
        for (ResultObjectProvider rop : _rops) {
            rop.reset();
        }
    }

    public int size() throws Exception {
        return 1;
    }

    public boolean supportsRandomAccess() {
         return false;
    }
}
