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

import java.util.Map;

import org.apache.openjpa.conf.OpenJPAConfiguration;

public interface OpenJPAEntityManagerFactorySPI
    extends OpenJPAEntityManagerFactory {

    /**
     * Register a listener for lifecycle-related events on the specified
     * classes. If the classes are null, all events will be propagated to
     * the listener. The listener will be passed on to all new entity
     * managers. See the <code>org.apache.openjpa.event</code> package for
     * listener types.
     *
     * @since 0.3.3
     */
    public void addLifecycleListener(Object listener, Class... classes);

    /**
     * Remove a listener for lifecycle-related events.
     *
     * @since 0.3.3
     */
    public void removeLifecycleListener (Object listener);

    /**
     * Register a listener for transaction-related events on the specified
     * classes. The listener will be passed on to all new entity
     * managers. See the <code>org.apache.openjpa.event</code> package for
     * listener types.
     *
     * @since 1.0.0
     */
    public void addTransactionListener(Object listener);

    /**
     * Remove a listener for transaction-related events.
     *
     * @since 1.0.0
     */
    public void removeTransactionListener (Object listener);

    /**
     * Return the configuration for this factory.
     */
    public OpenJPAConfiguration getConfiguration();

    public OpenJPAEntityManagerSPI createEntityManager();

    public OpenJPAEntityManagerSPI createEntityManager(Map props);
}
