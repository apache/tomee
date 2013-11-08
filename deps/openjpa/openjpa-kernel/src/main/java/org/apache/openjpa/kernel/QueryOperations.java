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
 * Query operation constants.
 *
 * @since 0.4.0
 */
public interface QueryOperations {

    /**
     * Symbolic constant that indicates that this query will be
     * performing a select operation.
     *
     * @see QueryContext#getOperation
     */
    public static final int OP_SELECT = 1;

    /**
     * Symbolic constant that indicates that this query will be
     * performing a delete operation.
     *
     * @see QueryContext#getOperation
     */
    public static final int OP_DELETE = 2;

    /**
     * Symbolic constant that indicates that this query will be
     * performing a update operation.
     *
     * @see QueryContext#getOperation
     */
    public static final int OP_UPDATE = 3;
}
