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
package org.apache.openjpa.persistence.criteria;

import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;

import org.apache.openjpa.kernel.exps.Value;

/**
 * The context holds named variables for nodes of CriteriaQuery.
 * Can create unique alias for the nodes that has not been aliased explicitly.
 *  
 * @author Pinaki Poddar
 * @since 2.0.0
 *
 */
interface AliasContext {
    /**
     * Gets the alias for the given node.
     * If no alias is set on the given node then create an alias, assign it to
     * the node.
     */
    String getAlias(Selection<?> node);
    
    /**
     * Register the given variable of corresponding given value against the
     * given node.
     * 
     * @param node of query tree
     * @param variable must be a variable
     * @param value path value
     */
    void registerVariable(Selection<?> node, Value variable, Value path);
    
    /**
     * Affirms if the given node has been registered.
     */
    boolean isRegistered(Selection<?> node);   
    
    /**
     * Gets the registered variable for the given node. 
     * Return null if the node is not registered.     
     */
    Value getRegisteredVariable(Selection<?> node);
    
    /**
     * Gets the registered root variable for the given node. 
     * Return null if the node is not registered.     
     */
    Value getRegisteredRootVariable(Root<?> node);
    
    /**
     * Gets the registered path value for the given node. 
     * Return null if the node is not registered.     
     */
    Value getRegisteredValue(Selection<?> node);
}
