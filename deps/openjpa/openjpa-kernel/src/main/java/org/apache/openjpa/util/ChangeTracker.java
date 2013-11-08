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

import java.util.Collection;

/**
 * Interface for components that track changes to containers at a
 * fine-grained level. Proxies that use change trackers might have better
 * update performance than non-tracking proxies.
 *
 * @author Abe White
 */
public interface ChangeTracker {

    /**
     * Return true if this tracker has an up-to-date view of all the changes
     * to the container it is managing.
     */
    public boolean isTracking();

    /**
     * Reset the state of the change tracker, and turn change tracking back
     * on if it has been disabled.
     */
    public void startTracking();

    /**
     * Tell the tracker to stop tracking changes for its container.
     */
    public void stopTracking();

    /**
     * Return the collection of values that need to be added to the managed
     * container.
     */
    public Collection getAdded();

    /**
     * Return the set of values that need to be removed from the managed
     * container.
     */
    public Collection getRemoved();

    /**
     * Return the set of elements that have changed. In maps, this marks a
     * possible change in value for a key. In collections, this marks an
     * element that has been removed and re-added.
     */
    public Collection getChanged();

    /**
     * The next element sequence value for this proxy at load time.
     * If the data store keeps this proxy's data in sequence order but allows
     * holes for removed objects, the implementation can set the next
     * sequence at load time, then retrieve it and start from there for
     * added objects at flush time. This value is set back to 0 if the
     * proxy stops tracking changes. For ordered proxies, it is set to the
     * proxy's size when the proxy starts tracking changes again.
     */
    public int getNextSequence();

    /**
     * The maximum element sequence value for this proxy at load time.
     * If the data store keeps this proxy's data in sequence order but allows
     * holes for removed objects, the implementation can set the next
     * sequence at load time, then retrieve it and start from there for
     * added objects at flush time. This value is set back to 0 if the
     * proxy stops tracking changes. For ordered proxies, it is set to the
     * proxy's size when the proxy starts tracking changes again.
     */
    public void setNextSequence (int seq);
}
