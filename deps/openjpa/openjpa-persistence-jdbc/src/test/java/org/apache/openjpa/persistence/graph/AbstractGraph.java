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
package org.apache.openjpa.persistence.graph;

import java.util.AbstractSet;
import java.util.Set;

/**
 * Abstract implementation of a {@linkplain Graph} borrows from {@link AbstractSet abstract} implementation of 
 * {@link Set}. The extended {@link Set#remove(Object) remove()} semantics accounts for 
 * {@link Graph#delink(Object, Object) removal} of all relationship to the removed element.
 * 
 * @author Pinaki Poddar
 *
 * @param <E> type of element of the graph.
 */
public abstract class AbstractGraph<E> extends AbstractSet<E> implements Graph<E> {
    /**
     * Removing an element from this graph has the side effect of removing all 
     * relations directed to the removed element.
     */
    @Override
    public boolean remove(Object e) {
        E node = (E)e;
        Set<Relation<E, E>> rs = getRelationsTo(node);
        for (Relation<E,E> r : rs) {
            delink(r.getSource(), node);
        }
        return super.remove(e);
    }
}
