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

import org.apache.openjpa.meta.ClassMetaData;

/**
 * Interface for any non-operator in a query filter, including
 * constants, variables, and object fields.
 *
 * @author Abe White
 */
public interface Value
    extends Serializable {

    /**
     * Return the expected type for this value, or <code>Object</code> if
     * the type is unknown.
     */
    public Class getType();

    /**
     * Set the implicit type of the value, based on how it is used in the
     * filter. This method is only called on values who return
     * <code>Object</code> from {@link #getType}.
     */
    public void setImplicitType(Class type);

    /**
     * Return true if this value is a variable.
     */
    public boolean isVariable();

    /**
     * Return true if this value is an aggregate.
     */
    public boolean isAggregate();

    /**
     * Return true if this value is an XML Path.
     */
    public boolean isXPath();
    
    /**
     * Return any associated persistent type.
     */
    public ClassMetaData getMetaData();

    /**
     * Associate a persistent type with this value.
     */
    public void setMetaData(ClassMetaData meta);

    /**
     * Accept a visit from a tree visitor.
     */
    public void acceptVisit(ExpressionVisitor visitor);

    /**
     * Return select item alias
     */
    public String getAlias();

    /**
     * Set select item alias
     */
    public void setAlias(String alias);

    /**
     * Return 'this' concrete class if alias is set, otherwise null
     */
    public Value getSelectAs();

    public Path getPath();

    public String getName();
}
