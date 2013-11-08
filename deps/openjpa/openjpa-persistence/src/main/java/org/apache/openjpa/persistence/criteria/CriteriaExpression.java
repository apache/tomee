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

/**
 * Interface to support Visitor pattern and CQL conversion of Criteria Query nodes.
 * 
 * @author Pinaki Poddar
 * @since 2.0.0
 */
interface CriteriaExpression  {
    /**
     * Accept visit from the given visitor. The receiver is responsible 
     * to propagate the visitor to the constituent sub-nodes if any.
     * 
     * @param visitor a processor to walk the nodes of a tree.
     */
    void acceptVisit(CriteriaExpressionVisitor visitor);
    
    /**
     * Get a string representation of this node as a value in the context of the given query.
     */
    StringBuilder asValue(AliasContext q);
    
    /**
     * Get a string representation of this node as a variable in the context of the given query.
     */
    StringBuilder asVariable(AliasContext q);
    
    /**
     * Get a string representation of this node as a projection term in the context of the given query.
     */
    StringBuilder asProjection(AliasContext q);
}
