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

import java.util.Iterator;
import java.util.Set;

import org.apache.openjpa.lib.util.concurrent.ConcurrentReferenceHashSet;

/**
 * Single-JVM-only implementation of {@link RemoteCommitProvider}
 * that listens for object modifications and propagates those changes
 * to other SingleJVMRemoteCommitProviders in the same JVM. This is
 * only useful for linking together multiple factories in the same
 * JVM that are all loaded in the same classloader, which is a rare
 * circumstance.
 *
 * @author Patrick Linskey
 * @since 0.2.5.0
 */
public class SingleJVMRemoteCommitProvider
    extends AbstractRemoteCommitProvider {

    private static Set s_providers = new ConcurrentReferenceHashSet(
        ConcurrentReferenceHashSet.HARD);

    public SingleJVMRemoteCommitProvider() {
        s_providers.add(this);
    }

    public void broadcast(RemoteCommitEvent event) {
        SingleJVMRemoteCommitProvider provider;
        for (Iterator iter = s_providers.iterator(); iter.hasNext();) {
            provider = (SingleJVMRemoteCommitProvider) iter.next();

            // don't notify this object -- this provider's factory
            // should not be notified of commits that originated
            // with one of its brokers
            if (provider == this)
                continue;

            provider.fireEvent(event);
        }
    }

    public void close() {
        s_providers.remove(this);
    }
}
