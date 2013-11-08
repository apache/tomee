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
package org.apache.openjpa.event;

import org.apache.openjpa.lib.util.Closeable;

/**
 * An entity that is responsible for communicating commit
 * notification to other {@link RemoteCommitEventManager}s. Each
 * event manager creates a remote commit provider, based on
 * the values of the <code>openjpa.RemoteCommitProvider</code>
 * configuration property.
 *  An adapter that implements {@link TransactionListener} is
 * registered with each {@link org.apache.openjpa.kernel.Broker}. This adapter
 * invokes <code>broadcast</code>, which is responsible for
 * notifying other remote commit provider objects of the commit changes.
 *  Upon receiving a notification from a different remote commit provider,
 * a provider must notify all local remote commit listeners via the
 * {@link RemoteCommitEventManager#fireEvent} method.
 *  A remote commit provider <b>must not</b> fire remote events for
 * commits that originated with a local broker.
 *
 * @author Patrick Linskey
 * @since 0.2.5.0
 */
public interface RemoteCommitProvider
    extends Closeable {

    /**
     * Set the "owning" remote event manager to notify when remote events
     * are received from remote sources.
     */
    public void setRemoteCommitEventManager(RemoteCommitEventManager mgr);

    /**
     * Notifies other remote event managers in this JVM and on other machines
     * of changes to the cache. This method must not notify the
     * event manager associated with the broker that originated this commit.
     */
    public void broadcast(RemoteCommitEvent event);

    /**
     * Free the resources used by this provider.
     */
    public void close();
}
