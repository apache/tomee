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

import java.util.Properties;

/**
 * Generic, directed, attributed Relation.
 * <br>
 * A relation is 
 * <ol>
 * <LI>generic because Relation type is parameterized with the type of vertices it links.
 * <LI>directed because it distinguishes the two vertices as source and target.
 * <LI>attributed because any arbitrary key-value pair can be associated with a relation.
 * </ol>
 * <br>
 * A relation is immutable in terms of its two vertices. The properties
 * associated to a relation, however, can change.
 * <br>
 * @param <V1> the type of <em>source</em> vertex linked by this relation.
 * @param <V2> the type of <em>target</em> vertex linked by this relation.
 *  
 * @author Pinaki Poddar
 *
 */
public interface Relation<V1,V2> {
    /**
     * Gets the immutable source vertex.
     * 
     * @return a non-null source vertex.
     */
    public V1 getSource();
    
    /**
     * Gets the immutable target vertex.
     * Unlike source, a target for a relation may be null.
     * 
     * @return a target vertex. May be null.
     */
    public V2 getTarget();
    
    
    /**
     * Adds the given key-value pair, overwriting any prior value associated to the same key.
     * 
     * @return the same relation for <em>fluent</em> method-chaining
     */
    public Relation<V1,V2> addAttribute(String key, Object value);
    
    /**
     * Affirms if an attribute value has been associated with the given key.
     * 
     */
    public boolean hasAttribute(String key);
    
    /**
     * Gets the value of the given attribute.
     * 
     * @return value of the given attribute. A null value does not distinguish whether
     * the attribute was set to a null value or the attribute was absent. 
     * 
     * @see #hasAttribute(String)
     */
    public Object getAttribute(String key);
    
    /**
     * Removes the given attribute.
     * 
     * @return the modified relation for <em>fluent</em> method chaining.
     */
    public Relation<V1,V2> removeAttribute(String key);
    
    /**
     * Gets the key-value pairs associated with this relation.
     */
    public Properties getAttributes();
}
