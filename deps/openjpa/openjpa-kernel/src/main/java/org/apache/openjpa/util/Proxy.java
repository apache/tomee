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

import org.apache.openjpa.kernel.OpenJPAStateManager;

/**
 * Interface implemented by all proxy types to allow setting and nulling
 * of their owning instance.
 * All concrete proxy classes should be public and have publc no-args
 * constructors so that tools that work via reflection on persistent instances
 * can manipulate them.
 *
 * @author Abe White
 */
public interface Proxy {

    /**
     * Reset the state of the proxy, and set the owning instance of the
     * proxy and the name of the field it is assigned to. Set to null to
     * indicate that the proxy is no longer managed.
     */
    public void setOwner(OpenJPAStateManager sm, int field);

    /**
     * Return the owning object.
     */
    public OpenJPAStateManager getOwner();

    /**
     * Return the owning field index.
     */
    public int getOwnerField();

    /**
     * Return the change tracker for this proxy, or null if none.
     */
    public ChangeTracker getChangeTracker();

    /**
     * Return an unproxied copy of the given instance. This method is used
     * by proxy managers to create backup values for use in rollback.
     */
    public Object copy(Object orig);
}
