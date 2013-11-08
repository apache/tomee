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
package org.apache.openjpa.kernel.exps;

import java.io.Serializable;
import java.util.Collection;

import org.apache.openjpa.kernel.StoreContext;

/**
 * An aggregate listener aggregates data produced by an ExpressionQuery.
 *
 * @author Abe White
 * @author Patrick Linskey
 */
public interface AggregateListener
    extends Serializable {

    /**
     * Return the tag that this listener listens for.
     */
    public String getTag();

    /**
     * Return whether this function expects to act on arguments. Some
     * function may not need arguments; for example, a function to count the
     * number of objects that match a given filter might be of the form:
     * <code>count()</code>.
     */
    public boolean expectsArguments();

    /**
     * Return the value of this aggregate.
     *
     * @param args for each candidate, the value of the arguments to
     * the function; will be null if this aggregate does
     * not expect an argument; if this function has
     * multiple arguments, each element will be an array
     * @param argClasses the expected class of each argument element
     * @param candidates the candidate objects being evaluated
     * @param ctx the persistence context
     * @return the value of the aggregate
     * @throws org.apache.openjpa.util.UserException if this aggregate does not 
     * support in-memory operation
     */
    public Object evaluate(Collection args, Class[] argClasses,
        Collection candidates, StoreContext ctx);

    /**
     * Return the expected type of the result of this listener.
     *
     * @param argClasses the expected classes of the argument, or null if
     * no arguments
     */
    public Class getType(Class[] argClasses);
}
