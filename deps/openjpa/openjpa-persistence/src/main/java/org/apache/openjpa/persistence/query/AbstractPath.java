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
package org.apache.openjpa.persistence.query;

import java.util.LinkedList;

/**
 * An abstract path is formed by two parts : the first part is a parent path.
 * The second part can be an attribute or an operation (e.g. KEY() or VALUE())
 * or a join type operation. Based on the exact nature of the second part, 
 * concrete derivation of this class combines the two constituent parts to 
 * arrive at complete path name.
 * For example, a navigation path adds the two part with a navigation '.' 
 * operator, while a OperatorPath will combine the parts as KEY(parent).
 * 
 * The constituent parts are immutable and supplied at construction. Hence
 * concrete implementations know what exact type they are dealing with, but
 * this receiver maintains it state as more generic type to accommodate
 * concrete types to cast/interpret these state variables. 
 * 
 * @author Pinaki Poddar
 *
 */
abstract class AbstractPath extends ExpressionImpl implements
		PathExpression {
	protected final AbstractPath  _parent;
	protected final Object 		  _part2;
	protected final PathOperator  _operator;
	protected final QueryDefinitionImpl _owner;
	
	protected AbstractPath(QueryDefinitionImpl owner, AbstractPath parent, 
	    PathOperator op, Object part2) {
		_owner = owner;
		_parent = parent;
		_part2  = part2;
		_operator = op;
	}
	
    // ------------------------------------------------------------------------
    // Path related functions.
    // ------------------------------------------------------------------------
	
	final QueryDefinitionImpl getOwner() {
		return _owner;
	}
	/**
     * Gets the parent from which this receiver has been derived. Can be null
	 * for a root path.
	 */
	public AbstractPath getParent() {
		return _parent;
	}
	
	/**
	 * Gets operator that derived this receiver from its parent.
	 */
	public PathOperator getOperator() {
		return _operator;
	}

	/**
	 * Gets the last segment of this path. 
	 * Concrete implementation should return a covariant type.
	 */
	public Object getLastSegment() {
		return _part2;
	}

    // -----------------------------------------------------------------------
    // Implementation of PathExpression
    // -----------------------------------------------------------------------
	public Aggregate avg() {
		return new AverageExpression(this);
	}

	public Aggregate count() {
		return new CountExpression(this);
	}

	public Predicate isEmpty() {
		return new IsEmptyExpression(this);
	}

	public Aggregate max() {
		return new MaxExpression(this);
	}

	public Aggregate min() {
		return new MinExpression(this);
	}

	public Expression size() {
		return new SizeExpression(this);
	}

	public Aggregate sum() {
		return new SumExpression(this);
	}

	public Expression type() {
		return new TypeExpression(this);
	}
	
	LinkedList<AbstractPath> split() {
		return _split(this, new LinkedList<AbstractPath>());
	}
	
	private LinkedList<AbstractPath> _split(AbstractPath path, 
		LinkedList<AbstractPath> list) {
		if (path == null)
			return list;
		_split(path.getParent(), list);
		list.add(path);
		return list;
	}
}
