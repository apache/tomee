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

import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.meta.ValueMetaData;

/**
 * Perform an action when OpenJPA detects an orphaned key in the database.
 *
 * @author Abe White
 * @since 0.3.2.2
 */
public interface OrphanedKeyAction {

    /**
     * Callback received when OpenJPA discovers an orphaned key.
     *
     * @param oid the orphaned key
     * @param sm the instance representing the record in which the
     * key was discovered; may be null
     * @param vmd the value in which the key was discovered
     * @return the value to load into field <code>fmd</code>; typically
     * <code>null</code>
     */
    public Object orphan(Object oid, OpenJPAStateManager sm, ValueMetaData vmd);
}
