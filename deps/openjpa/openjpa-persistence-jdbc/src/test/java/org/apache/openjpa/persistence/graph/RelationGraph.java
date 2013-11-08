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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

/**
 * RelationGraph is a first-class persistent entity that express its persistent state as a set of
 * {@link Relation persistent relations}.
 *  
 * @author Pinaki Poddar
 *
 */

@SuppressWarnings("serial")
@Entity
public class RelationGraph<E> extends PersistentGraph<E>  {
    @OneToMany(cascade={CascadeType.PERSIST,CascadeType.MERGE, CascadeType.DETACH, CascadeType.REFRESH})
    private Set<PersistentRelation<E,E>> relations = new HashSet<PersistentRelation<E,E>>();
    
    /* 
     * Links the given vertices, unless they are already connected.
     * 
     * @param source non-null source vertex
     * @param target non-null target vertex
     * 
     * @see org.apache.openjpa.persistence.graph.Graph#link(V1, V2)
     */
    public <V1 extends E,V2 extends E> Relation<V1,V2> link(V1 source, V2 target) {
        if (source == null)
            throw new NullPointerException("Can not link from a null source vertex");
        if (target == null)
            throw new NullPointerException("Can not link to a null target vertex");
        
        Relation<V1,V2> r = getRelation(source, target);
        if (r == null) {
            r = new PersistentRelation<V1, V2>(source, target);
            relations.add((PersistentRelation<E, E>) r);
        }
        return r;
        
    }
    
    /* 
     * Delinks the given vertices, if they are currently connected.
     * 
     * 
     * @see org.apache.openjpa.persistence.graph.Graph#delink(V1, V2)
     */
    public <V1 extends E,V2 extends E> Relation<V1,V2> delink(V1 source, V2 target) {
        Relation<V1,V2> r = getRelation(source, target);
        if (r != null) {
            relations.remove(r);
        }
        return r;
        
    }
    
    /* 
     * Get the relation between the given vertex.
     *  
     * @see org.apache.openjpa.persistence.graph2.Graph#getRelation(V1, V2)
     */
    public <V1 extends E,V2 extends E> Relation<V1,V2> getRelation(V1 source, V2 target) {
        for (Relation<?,?> r : relations) {
            if (r.getSource().equals(source) && r.getTarget() != null && r.getTarget().equals(target)) {
                return (Relation<V1,V2>)r;
            }
        }
        return null;
    }
    
    /**
     * Iterator over the nodes of this graph.
     */
    public Iterator<E> iterator() {
        return getNodes().iterator();
    }
    
    public int size() {
        return 0;
    }

    public <V extends E> Set<Relation<V, E>> getRelationsFrom(V source) {
        Set<Relation<V,E>> rs = new HashSet<Relation<V,E>>();
        for (Relation<E,E> r : relations) {
            if (r.getSource().equals(source) && r.getTarget() != null)
                rs.add((Relation<V,E>)r);
        }
        return rs;
    }

    public <V extends E> Set<Relation<E, V>> getRelationsTo(V target) {
        Set<Relation<E, V>> rs = new HashSet<Relation<E, V>>();
        for (Relation<?,?> r : relations) {
            if (r.getTarget() != null && r.getTarget().equals(target))
                rs.add((Relation<E, V>)r);
        }
        return rs;
    }

    public Set<E> getSources(Object target) {
        Set<E> sources = new HashSet<E>();
        for (Relation<E,E> r : relations) {
            if (r.getTarget() != null && r.getTarget().equals(target))
                sources.add(r.getSource());
        }
        return sources;
    }

    public Set<E> getTargets(Object source) {
        Set<E> targets = new HashSet<E>();
        for (Relation<E,E> r : relations) {
            if (r.getSource().equals(source) && r.getTarget() != null)
                targets.add(r.getTarget());
        }
        return targets;
    }
    
    public Set<E> getNodes() {
        Set all = new HashSet();
        for (Relation<?,?> r : relations) {
            all.add(r.getSource());
            if (r.getTarget() != null)
                all.add(r.getTarget());
        }
        return all;
    }
    
    @Override
    public boolean add(E e) {
        if (contains(e))
            return false;
        relations.add(new PersistentRelation<E,E>(e,null));
        return true;
    }
}
