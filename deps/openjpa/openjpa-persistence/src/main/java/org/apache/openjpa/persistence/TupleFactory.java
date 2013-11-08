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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.persistence.Tuple;
import javax.persistence.TupleElement;
import javax.persistence.criteria.Selection;

import org.apache.openjpa.kernel.ObjectFactory;


/**
 * A factory for tuples such that all instances created by a factory instance share the same TupleElements
 * to save memory consumption.
 * <BR>
 * All Tuple instances created by this factory access the TupleElememts contained in this factory. 
 * 
 * @author Pinaki Poddar
 * 
 * @since 2.0.0
 *
 */
public class TupleFactory implements ObjectFactory<Tuple> {
    private final List<TupleElement<?>> elements;
    
    /**
     * A factory of Tuple that shares the given TupleElements.
     * 
     */
    public TupleFactory(List<TupleElement<?>> elems) {
        elements = Collections.unmodifiableList(elems);
    }
    
    public TupleFactory(TupleElement<?>... elems) {
        this(Arrays.asList(elems));
    }
    
    public TupleFactory(Selection<?>... elems) {
        List<TupleElement<?>> list = new ArrayList<TupleElement<?>>();
        for (Selection<?> s : elems)
            list.add(s);
        elements = Collections.unmodifiableList(list);
    }
    
    public List<TupleElement<?>> getElements() {
        return elements;
    }
    
    public TupleImpl newInstance() {
        TupleImpl impl = new TupleImpl(this);
        return impl;
    }
    
    public int getIndex(TupleElement<?> e) {
        int i = elements.indexOf(e);
        if (i == -1)
            throw new IllegalArgumentException("Index " + i + " does not exist");
        return i;
    }
    
    public int getIndex(String alias) {
        if (alias == null)
            throw new IllegalArgumentException("null alias");
        for (int i = 0; i < elements.size(); i++) {
            TupleElement<?> e = elements.get(i);
            if (alias.equals(e.getAlias()))
                return i;
        }
        throw new IllegalArgumentException(alias + " not found");
    }
}
