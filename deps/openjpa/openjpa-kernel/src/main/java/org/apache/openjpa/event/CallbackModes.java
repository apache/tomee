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

/**
 * Callback handling constants.
 *
 * @author Steve Kim
 * @since 0.4.0
 */
public interface CallbackModes {

    /**
     * Flag to stop executing callbacks on exception immediately.
     */
    public static final int CALLBACK_FAIL_FAST = 2 << 0;

    /**
     * Flag to ignore exceptions made during callback.
     */
    public static final int CALLBACK_IGNORE = 2 << 1;

    /**
     * Flag to log exceptions made during callback.
     */
    public static final int CALLBACK_LOG = 2 << 2;

    /**
     * Flag to re-throw exceptions made during callback.
     */
    public static final int CALLBACK_RETHROW = 2 << 3;

    /**
     * Flag to always rollback on a callback exception.
     */
    public static final int CALLBACK_ROLLBACK = 2 << 4;
}
