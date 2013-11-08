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

/**
 * Eager fetch mode constants.
 *
 * @author Abe White
 */
public interface EagerFetchModes {

    /**
     * Constant indicating not to try to load subclass data and related
     * objects when querying for an object.
     */
    public static final int EAGER_NONE = 0;

    /**
     * Constant indicating to load relations and subclass data if possible
     * without separate queries.
     */
    public static final int EAGER_JOIN = 1;

    /**
     * Constant indicating to load relations and subclass data if possible
     * using either joins or parallel queries.
     */
    public static final int EAGER_PARALLEL = 2;
}
