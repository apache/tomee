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
package org.apache.openjpa.persistence.jdbc.common.apps;


/**
 * <p>Kodo 3.x callback.</p>
 *
 * @deprecated
 */
public interface PostDetachCallback {

    /**
     * Invoked on the detached copy of the persistent instance
     * after it has been detached. This method will only be called once
     * the entire graph of objects for a single detach operation has
     * been completed.
     *
     * @param managed the managed instance that was the
     * source of the detached instance
     */
    public void jdoPostDetach(Object managed);
}
