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
package org.apache.openjpa.jdbc.kernel;

import org.apache.openjpa.jdbc.sql.Select;
import org.apache.openjpa.kernel.LockManager;
import org.apache.openjpa.kernel.OpenJPAStateManager;

/**
 * Extension of the {@link LockManager} interface with methods for
 * datastore locking during the select and load process. Implementors of
 * this interface can also take advantage of the fact that in the
 * {@link LockManager#lock} and {@link LockManager#lockAll} methods, the
 * given <code>conn</code> parameter, if any, will be an instance of
 * {@link ConnectionInfo}.
 *
 * @author Abe White
 */
public interface JDBCLockManager
    extends LockManager {

    /**
     * Return whether to issue the given select FOR UPDATE, depending on
     * the capabilities of the dictionary and the fetch configuration.
     */
    public boolean selectForUpdate(Select sel, int lockLevel);

    /**
     * Notification that the given instance was loaded via a result set
     * produced by a FOR UPDATE select.
     */
    public void loadedForUpdate(OpenJPAStateManager sm);

    /**
     * Return true if locking is not desired for relation fields.
     */
    public boolean skipRelationFieldLock();
}
