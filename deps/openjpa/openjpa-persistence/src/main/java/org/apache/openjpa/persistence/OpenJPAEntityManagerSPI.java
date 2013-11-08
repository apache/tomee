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
package org.apache.openjpa.persistence;

import java.util.EnumSet;

import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.ee.ManagedRuntime;

public interface OpenJPAEntityManagerSPI
    extends OpenJPAEntityManager {

    /**
     * Return the configuration associated with this entity manager.
     */
    public OpenJPAConfiguration getConfiguration();

    /**
     * Return the managed runtime in use.
     */
    public ManagedRuntime getManagedRuntime();

    //////////
    // Events
    //////////

    /**
     * Register a listener for transaction-related events.
     */
    public void addTransactionListener(Object listener);

    /**
     * Remove a listener for transaction-related events.
     */
    public void removeTransactionListener(Object listener);

    /**
     * The {@link CallbackMode} flags for handling transaction listener
     * exceptions.
     *
     * @since 1.1.0
     */
    public EnumSet<CallbackMode> getTransactionListenerCallbackModes();

    /**
     * The {@link CallbackMode} flag for handling transaction listener
     * exceptions. The flags provided here will entirely replace the
     * previous settings.
     */
    public void setTransactionListenerCallbackMode(CallbackMode mode);

    /**
     * The {@link CallbackMode} flags for handling transaction listener
     * exceptions. The flags provided here will entirely replace the
     * previous settings.
     */
    public void setTransactionListenerCallbackMode(EnumSet<CallbackMode> modes);

    /**
     * Register a listener for lifecycle-related events on the specified
     * classes. If the classes are null, all events will be propagated to
     * the listener.
     */
    public void addLifecycleListener(Object listener, Class... classes);

    /**
     * Remove a listener for lifecycle-related events.
     */
    public void removeLifecycleListener(Object listener);

    /**
     * The {@link CallbackMode} flags for handling lifecycle listener
     * exceptions.
     *
     * @since 1.1.0
     */
    public EnumSet<CallbackMode> getLifecycleListenerCallbackModes();

    /**
     * The {@link CallbackMode} flag for handling lifecycle listener
     * exceptions. The flags provided here will entirely replace the
     * previous settings.
     */
    public void setLifecycleListenerCallbackMode(CallbackMode mode);

    /**
     * The {@link CallbackMode} flags for handling lifecycle listener
     * exceptions. The flags provided here will entirely replace the
     * previous settings.
     */
    public void setLifecycleListenerCallbackMode(EnumSet<CallbackMode> modes);
    
    
    /**
     * Affirms if this receiver is caching database queries.
     *  
     * @since 2.0.0
     */
    public boolean getQuerySQLCache();
    
    /**
     * Sets whether this receiver will cache database queries during its 
     * lifetime. The cache configured at BrokerFactory level is not affected by 
     * setting it inactive for this receiver. 
     * 
     * @since 2.0.0
     */
    public void setQuerySQLCache(boolean flag);

}
