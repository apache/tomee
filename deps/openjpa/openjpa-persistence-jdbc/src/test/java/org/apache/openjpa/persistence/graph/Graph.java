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

import java.util.Set;

/**
 * Graph is an extended {@link java.util.Set Set} that is aware of {@link Relation relationship} 
 * between its elements. The linkage between two elements is represented by a {@link Relation relation}.
 * <br>
 * The extended behavior of Set allows two member elements to be {@linkplain Graph#link(Object, Object) linked} 
 * or {@linkplain Graph#delink(Object, Object) delinked}. 
 * 
 * @param <E> Type of element of the graph
 * 
 * 
 * @author Pinaki Poddar
 *
 */
public interface Graph<E> extends Set<E> {
    /**
     * Links the pair of given vertices.
     * If the pair is already linked then the existing relation is returned.
     * If either of the vertices are currently non a member of this graph,
     * then they are added to the graph as a side-effect of linking.
     *  
     * @param source non-null source node 
     * @param target non-null target node
     * 
     * @param <V1> generic type of source node 
     * @param <V2> generic type of target node
     *
     * @return a relation 
     */
    public <V1 extends E, V2 extends E> Relation<V1, V2> link(V1 source, V2 target);

    /**
     * Breaks the relation between the given pair of nodes.
     * 
     * @param source non-null source node 
     * @param target non-null target node
     * 
     * @param <V1> generic type of source node 
     * @param <V2> generic type of target node
     * 
     * @return the existing relation, if any, that had been broken. null otherwise.
     */
    public <V1 extends E, V2 extends E> Relation<V1, V2> delink(V1 source, V2 target);

    /**
     * Gets the directed relation between the given pair of nodes, if exists.
     *  
     * @param source non-null source node 
     * @param target non-null target node
     * 
     * @param <V1> generic type of source node 
     * @param <V2> generic type of target node
     *
     * @return a relation between the nodes, if exists. null otherwise. 
     */
    public <V1 extends E, V2 extends E> Relation<V1, V2> getRelation(V1 source, V2 target);
    
    /**
     * Gets the nodes that are directly reachable from the given source node.
     * 
     * @return set of target nodes. Empty set if the given source node is not connected to any other nodes.
     */
    public Set<E> getTargets(E source);
    
    
    /**
     * Gets the source nodes that are directly connected to the given target node.
     * 
     * @return set of source nodes. Empty set if the given target node is not connected from any node.
     */
    public Set<E> getSources(E target);
    
    /**
     * Gets all the relations originating from the given source.
     * @param <V>
     * @param source
     * @return
     */
    public <V extends E> Set<Relation<V,E>> getRelationsFrom(V source);
    
    /**
     * Gets all the relations terminating on the given target.
     * @param <V>
     * @param target
     * @return
     */
    public <V extends E> Set<Relation<E,V>> getRelationsTo(V target);
}
