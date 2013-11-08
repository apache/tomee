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
package org.apache.openjpa.util;

import org.apache.openjpa.kernel.Broker;
import org.apache.openjpa.kernel.OpenJPAStateManager;

/**
 * Implemented by proxy classes which are delay-load capable. Delay-load
 * proxies are lazily loaded and provide some operations
 * which allow manipulation of proxy without necessarily needing to
 * load the proxied object. 
 */
public interface DelayedProxy {
    
    /**
     * Load the proxy if it was delay-loaded.
     */
    void load();

    /**
     * Returns whether the caller has direct-call access to the proxied
     * object.  Direct access allows calls to be made on the object
     * without triggering a load or proxy state tracking callbacks. 
     */
    boolean isDirectAccess();
    
    /**
     * Sets whether the caller has direct-call access to the proxied
     * object.  Direct access allows calls to be made on the object
     * without triggering a load or proxy state tracking callbacks. 
     */
    void setDirectAccess(boolean direct);
        
    /**
     * Get the broker that is used to service this proxy.
     */
    Broker getBroker();

    /**
     * Close the broker that is used to service this proxy.
     */
    void closeBroker();

    /**
     * Returns the state manager of the owning instance.
     */
    OpenJPAStateManager getOwnerStateManager();
    
    /**
     * Returns a state manager that can service this proxy even if
     * the collection was detached.
     */
    OpenJPAStateManager getDelayedOwner();
    
    /**
     * Returns the expected field index even if this collection
     * was detached.
     * @return
     */
    int getDelayedField();
    
    /**
     * Returns whether the proxy is detached.
     * @return
     */
    boolean isDetached();
}
