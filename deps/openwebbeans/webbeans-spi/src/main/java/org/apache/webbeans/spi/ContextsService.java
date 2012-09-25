/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.webbeans.spi;

import java.lang.annotation.Annotation;

import javax.enterprise.context.ContextException;
import javax.enterprise.context.spi.Context;

/**
 * Contexts services provides demarcation
 * methods for each context that is defined
 * in the specification. SPI providers implement
 * related method that it supports.
 * 
 * <p>
 * For example, web container supports request, session
 * conversation, application, singleton and dependent
 * contexts.
 * </p>
 * @version $Rev$ $Date$
 *
 */
public interface ContextsService
{
    /**
     * Initialize container contexts service.
     * @param initializeObject any initialize object
     */
    public void init(Object initializeObject);
    
    /**
     * Destroys container contexts service.
     * @param destroyObject any destroy parameter
     */
    public void destroy(Object destroyObject);
    
    /**
     * Gets current context with given scope type with
     * respect to the current thread of execution.
     * <p>
     * If there is not current context, it returns null. 
     * </p>
     * @param scopeType context scope type
     * @return current context with given scope type
     */
    public Context getCurrentContext(Class<? extends Annotation> scopeType);
    
    /**
     * If container supports the given scope type it returns
     * true, otherwise it return false.
     * @param scopeType scope type
     * @return true if container supports given scope type false otherwise
     */
    public boolean supportsContext(Class<? extends Annotation> scopeType);
    
    /**
     * Starts the context with the given scope type. If 
     * given scope type is not supported, there is no action.
     * @param scopeType scope type
     * @param startParameter any parameter
     * @throws ContextException if any exception thrown by starting context,
     *         it is wrapped inside {@link ContextException} and thrown.
     */
    public void startContext(Class<? extends Annotation> scopeType, Object startParameter) throws ContextException;
    
    /**
     * Ends the context with the given scope type. If 
     * given scope type is not supported, there is no action.
     * Any exception thrown by the operation is catched and 
     * logged by the container.
     * @param scopeType scope type
     * @param endParameters any end parameter
     */
    public void endContext(Class<? extends Annotation> scopeType, Object endParameters);
    
    /**
     * Activate the context with the given scope type. If 
     * given scope type is not supported, there is no action.
     * Any exception thrown by the operation is catched and 
     * logged by the container.
     * @param scopeType scope type
     */
    public void activateContext(Class<? extends Annotation> scopeType);
    
    /**
     * Deactivates the context with the given scope type. If 
     * given scope type is not supported, there is no action.
     * Any exception thrown by the operation is catched and 
     * logged by the container.
     * @param scopeType scope type
     */    
    public void deActivateContext(Class<? extends Annotation> scopeType);
}
