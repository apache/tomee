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

import org.apache.openjpa.kernel.StoreContext;

/**
 * A filter listener extends expression filters with custom functionality.
 *
 * @author Steve Kim
 * @author Abe White
 */
public interface FilterListener
    extends Serializable {

    /**
     * Return the tag that this extension listens for.
     */
    public String getTag();

    /**
     * Return true if this extension expects arguments to act on. Some
     * extensions may not need arguments; for example, an extension to
     * switch a string to upper case might be of the form:
     * <code>field.ext:toUpperCase ()</code>.
     */
    public boolean expectsArguments();

    /**
     * Return true if this extension expects a target to act on. Some
     * extensions act on a field or object value; others stand alone.
     * <code>field.ext:toUpperCase ()</code> acts on the target
     * <code>field</code> but has no arguments, while another possible form,
     * <code>ext:toUpperCase (field)</code> has no target but does have an
     * argument.
     */
    public boolean expectsTarget();

    /**
     * Evaluate the given expression. This method is used when
     * evaluating in-memory expressions. The method used when evaluating
     * data store expressions will change depending on the data store in use.
     *
     * @param target the target object / field value to act on; will be
     * null if this extension does not expect a target
     * @param targetClass the expected class of the target; given in case
     * the target evaluates to null and typing is needed
     * @param args the values of the arguments given in the filter;
     * will be null if this extension does not expect an argument
     * @param argClasses the expected classes of the arguments; given in case
     * an argument evaluates to null and typing is needed
     * @param candidate the candidate object being evaluated
     * @param ctx the persistence context
     * @return the value of the extension for this candidate; if
     * this extension is an expression, this method should
     * return {@link Boolean#TRUE} or {@link Boolean#FALSE}
     * @throws org.apache.openjpa.util.UserException if this extension does not 
     * support in-memory operation
     */
    public Object evaluate(Object target, Class targetClass, Object[] args,
        Class[] argClasses, Object candidate, StoreContext ctx);

    /**
     * Return the expected type of the result of this listener.
     *
     * @param targetClass the expected class of the target, or null if no target
     * @param argClasses the expected classes of the arguments, or null if
     * no arguments
     */
    public Class getType(Class targetClass, Class[] argClasses);
}
