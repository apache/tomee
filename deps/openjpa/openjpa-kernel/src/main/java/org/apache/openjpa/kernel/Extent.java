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
package org.apache.openjpa.kernel;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Representation of all members of a persistent class.
 *
 * @author Abe White
 * @author Patrick Linskey
 */
public interface Extent<T> {

    /**
     * Return the (mutable) fetch configuration for this extent.
     */
    public FetchConfiguration getFetchConfiguration();

    /**
     * Whether this extent will ignore changes made in the current transaction.
     */
    public boolean getIgnoreChanges();

    /**
     * Whether this extent will ignore changes made in the current transaction.
     */
    public void setIgnoreChanges(boolean ignoreChanges);

    /**
     * Returns a list of all objects represented by this extent. This method
     * creates a {@link List} by traversing the entire iterator returned by a
     * call to {@link #iterator}. This means that {@link Collection#size} will
     * work correctly, but if the extent represents a large data set, this
     * method may be quite slow and may consume quite a bit of memory.
     */
    public List<T> list();

    /**
     * Return an iterator over the extent members.
     */
    public Iterator<T> iterator();

    /**
     * The broker that generated the extent.
     */
    public Broker getBroker();

    /**
     * The class of extent elements.
     */
    public Class<T> getElementType();

    /**
     * Whether the extent includes subclasses.
     */
    public boolean hasSubclasses();

    /**
     * Close all open iterators.
     */
    public void closeAll();

    /**
     * Synchronizes on an internal lock.
     */
    public void lock();

    /**
     * Release the internal lock.
     */
	public void unlock ();
}
