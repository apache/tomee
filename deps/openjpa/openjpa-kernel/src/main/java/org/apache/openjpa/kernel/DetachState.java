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

/**
 * Constants for which fields to include in the detach graph.
 *
 * @author Abe White
 * @since 0.4.0
 */
public interface DetachState {

    /**
     * Mode to detach all fields in the current fetch groups.
     * 
     * @since 0.9.8
     */
    public static final int DETACH_FETCH_GROUPS = 0;

    /**
     * @deprecated
     */
    public static final int DETACH_FGS = 0;

    /**
     * Mode to detach all currently-loaded fields.
     */
    public static final int DETACH_LOADED = 1;

    /**
     * Mode to detach all fields.
     */
    public static final int DETACH_ALL = 2;
}
