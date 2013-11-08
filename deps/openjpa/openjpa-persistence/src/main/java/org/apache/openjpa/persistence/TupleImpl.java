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

package org.apache.openjpa.persistence;

import java.lang.reflect.Method;
import java.util.List;

import javax.persistence.Tuple;
import javax.persistence.TupleElement;

import org.apache.openjpa.kernel.Filters;
import org.apache.openjpa.lib.util.Localizer;

/**
 * Tuple holds a set of values corresponding to a set of {@link TupleElement}.
 * This implementation prefers index-based access. 
 * A Tuple instance is constructed by a TupleFactory.
 * The TupleElemets are shared across all the tuple instances.
 * 
 * @author Pinaki Poddar
 *
 */
public class TupleImpl implements Tuple {
    private static final Localizer _loc = Localizer.forPackage(TupleImpl.class);
    private final TupleFactory factory;
    private final Object[] values;
    public static Method PUT;
    static {
        try {
            PUT = TupleImpl.class.getMethod("put", new Class[]{Integer.class, Object.class});
        } catch (Exception e) {
        }
    }
    
    /**
     * Supply the factory that creates prototypes and holds the elements.
     */
    TupleImpl(TupleFactory factory) {
        this.factory = factory;
        values = new Object[factory.getElements().size()];
    }
    
    public <X> X get(TupleElement<X> tupleElement) {
        int i = factory.getIndex(tupleElement);
        return assertAndConvertType(""+i, values[i], tupleElement.getJavaType());
    }

    public <X> X get(String alias, Class<X> type) {
        Object val = values[factory.getIndex(alias)];
        return assertAndConvertType(alias, val, type);
    }

    public Object get(String alias) {
        return get(alias, null);
    }

    public <X> X get(int i, Class<X> type) {
        if (i >= values.length || i < 0) {
            throw new IllegalArgumentException(_loc.get("tuple-exceeded-size", i, values.length).getMessage());
        }
        Object val = values[i];
        return assertAndConvertType(""+i, val, type);
    }

    public Object get(int i) {
        return get(i, null);
    }

    public Object[] toArray() {
        return values;
    }

    public List<TupleElement<?>> getElements() {
        return factory.getElements();
    }

    /**
     * Put the value at the given key index.
     * This is invoked by the kernel to populate a Tuple.
     */
    public void put(Integer key, Object value) {
        values[key] = value;
    }
    
    /**
     * Assert that the given value is convertible to the given type and convert.
     * null type implies no conversion and a pure cast.
     */
    <X> X assertAndConvertType(String id, Object value, Class<X> type) {
        try {
            if (type == null || value == null) {
                return (X) value;
            } else {
                return (X) Filters.convert(value, type);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(_loc.get("tuple-element-wrong-type", new Object[]{id, value, 
                value.getClass().getName(), type.getName()}).getMessage());
        }
    }
}
