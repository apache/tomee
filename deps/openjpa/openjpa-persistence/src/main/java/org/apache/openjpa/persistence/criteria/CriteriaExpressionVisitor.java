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

import java.util.HashSet;
import java.util.Set;

/**
 * A visitor for Criteria Expression nodes.
 * 
 * @author Pinaki Poddar
 * @since 2.0.0
 *
 */
public interface CriteriaExpressionVisitor {
    // Enumerates order of traversal of nodes
    public static enum TraversalStyle {
        INFIX,    // operand1 operator operand2   e.g. a + b
        POSTFIX,  // operand1 operand2 operator   e.g. a b +
        PREFIX,   // operator operand1 operand2   e.g. + a b
        FUNCTION  // operator(operand1, operand2) e.g. f(a,b)
    }
    
    /**
     * Enter the given node.
     */
    void enter(CriteriaExpression node);
    
    /**
     * Exit the given node.
     */
    void exit(CriteriaExpression node);
    
    /**
     * Affirms if this node has been visited.
     */
    boolean isVisited(CriteriaExpression node);
    
    /**
     * Get the traversal style of the children of the given node.
     */
    TraversalStyle getTraversalStyle(CriteriaExpression node);
    
    /**
     * An abstract implementation that can detect cycles during traversal.
     *  
     */
    public static abstract class AbstractVisitor implements CriteriaExpressionVisitor {
        protected final Set<CriteriaExpression> _visited = new HashSet<CriteriaExpression>();
        
        /**
         * Remembers the node being visited.
         */
        public void exit(CriteriaExpression node) {
            _visited.add(node);
        }
        
        /**
         * Affirms if this node has been visited before.
         */
        public boolean isVisited(CriteriaExpression node) {
            return _visited.contains(node);
        }
        
        /**
         * Returns PREFIX as the default traversal style.
         */
        public TraversalStyle getTraversalStyle(CriteriaExpression node) {
            return TraversalStyle.PREFIX;
        }
    }
    
    /**
     * A visitor to register Parameter expression of a query.
     *
     */
    public static class ParameterVisitor extends AbstractVisitor {
        private final CriteriaQueryImpl<?> query;
        
        public ParameterVisitor(CriteriaQueryImpl<?> q) {
            query = q;
        }
        
        public void enter(CriteriaExpression expr) {
            if (expr instanceof ParameterExpressionImpl) {
                query.registerParameter((ParameterExpressionImpl<?>)expr);
            }
        }
    }
}
