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
 * An entity that wishes to be notified when Brokers
 * associated with remote BrokerFactories commit.
 *  A RemoteCommitListener <b>is not</b> notified of commits that originated
 * with a Broker created from the BrokerFactory that it is
 * registered with. (Of course, if a listener is registered with multiple
 * factories, this situation might be complicated a bit.)
 *  Usage:
 * <code><pre> import org.apache.openjpa.event.*;
 * import org.apache.openjpa.conf.*;
 * 
 * {@link org.apache.openjpa.conf.OpenJPAConfiguration} conf =
 *         factory.getConfiguraiton ();
 *  RemoteCommitListener l = new RemoteCommitListener () {
 * public void afterCommit ({@link RemoteCommitEvent} e) {
 * // update a Swing widget when remote brokers make
 * // changes to reference data }
 *  public void close () { } };
 *  conf.getRemoteCommitEventManager ().addListener (l);
 * </pre></code>
 * 
 *
 * @author Patrick Linskey
 * @since 0.2.5.0
 */
public interface RemoteCommitListener
    extends Closeable {

    /**
     * Notification that a transaction associated with a different
     * BrokerFactory has successfully committed.
     */
    public void afterCommit(RemoteCommitEvent event);

    /**
     * Free the resources used by this listener.
     */
    public void close();
}
