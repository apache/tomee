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
package org.apache.webbeans.cditest;


import java.lang.annotation.Annotation;
import javax.enterprise.inject.ResolutionException;
import javax.enterprise.inject.spi.BeanManager;


/**
 * A CdiTestContainer provides access to an underlying JSR-299 (CDI)
 * Container. It allows starting and stopping the container as to start
 * and stop single contexts.
 *
 */
public interface CdiTestContainer 
{
    /**
     * Booting the CdiTestContainer will scan the whole classpath
     * for Beans and extensions available.
     */
    public void bootContainer() throws Exception;
    
    /**
     * This will shutdown the underlying CDI container.
     */
    public void shutdownContainer() throws Exception;
    
    /**
     * This will start all Contexts
     */
    public void startContexts() throws Exception;
    
    /**
     * Stop all Contexts and destroy all beans properly
     */
    public void stopContexts() throws Exception;
    
    
    public void startApplicationScope() throws Exception;
    public void stopApplicationScope() throws Exception;
    
    public void startSessionScope() throws Exception;
    public void stopSessionScope() throws Exception;
    
    public void startRequestScope() throws Exception;
    public void stopRequestScope() throws Exception;

    public void startConversationScope() throws Exception;
    public void stopConversationScope() throws Exception;

    public void startCustomScope(Class<? extends Annotation> scopeClass) throws Exception;
    public void stopCustomScope(Class<? extends Annotation> scopeClass) throws Exception;

    /**
     * @return the {@link BeanManager} or <code>null</code> it not available
     */
    public  BeanManager getBeanManager();

    /**
     * Provide a contextual reference via it's type and optional qualifier annotations.
     * If no qualifier is given, &#064;Default is assumed.
     * @param <T> the Type of the contextual reference
     * @param type the Type of the contextual reference
     * @param qualifiers optional qualifiers to restrict the search
     * @return a contextual reference of the given bean if it is not ambiguous.
     * @throws ResolutionException if not exactly 1 {@code Bean<T>} got found
     */
    public <T> T getInstance(Class<T> type, Annotation... qualifiers) throws ResolutionException;

    /**
     * Provide a contextual reference via it's type and ExpressionLanguage name.
     * @param <T> the Type of the contextual reference
     * @param elName the EL name to search for.
     * @return a contextual reference of the given bean via it's name.
     * @throws ResolutionException if not exactly 1 {@code Bean<T>} got found
     */
    public Object getInstance(String elNname) throws ResolutionException;

}
