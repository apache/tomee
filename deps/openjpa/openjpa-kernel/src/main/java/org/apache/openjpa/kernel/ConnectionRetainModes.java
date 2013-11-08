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
 * Connection retain mode constants.
 *
 * @author Abe White
 * @since 0.4.0
 */
public interface ConnectionRetainModes {

    /**
     * Constant indicating that connections will be obtained as needed.
     */
    public static final int CONN_RETAIN_DEMAND = 0;

    /**
     * Constant indicating that connections will be retained for the life
     * of each transaction.
     */
    public static final int CONN_RETAIN_TRANS = 1;

    /**
     * Constant indicating that each broker will retain a single connection
     * that it will use for its lifespan.
     */
    public static final int CONN_RETAIN_ALWAYS = 2;
}
