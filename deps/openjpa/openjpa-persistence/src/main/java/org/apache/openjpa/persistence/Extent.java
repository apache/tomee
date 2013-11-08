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

import java.util.List;

/**
 * An extent is a logical view of all instances of a class.
 *
 * @author Abe White
 * @since 0.4.0
 * @published
 */
public interface Extent<T>
    extends Iterable<T> {

    /**
     * The extent's element type.
     */
    public Class<T> getElementClass();

    /**
     * Whether the extent includes subclasses.
     */
    public boolean hasSubclasses();

    /**
     * The owning entity manager.
     */
    public OpenJPAEntityManager getEntityManager();

    /**
     * Fetch configuration for controlling how iterated objects are loaded.
     */
    public FetchPlan getFetchPlan();

    /**
     * Whether the extent sees inserts and deletes in the current transaction.
     */
    public boolean getIgnoreChanges();

    /**
     * Whether the extent sees inserts and deletes in the current transaction.
     */
    public void setIgnoreChanges(boolean ignoreChanges);

    /**
     * List the extent contents.
     */
    public List<T> list();

    /**
     * Close all open iterators that are consuming database resources.
     */
    public void closeAll();

    /**
     * @deprecated cast to {@link ExtentImpl} instead. This
     * method pierces the published-API boundary, as does the SPI cast.
     */
    public org.apache.openjpa.kernel.Extent getDelegate();
}
