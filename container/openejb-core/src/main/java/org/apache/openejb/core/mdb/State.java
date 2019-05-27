/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.openejb.core.mdb;

enum State {
    /**
     * The handler has been initialized and is ready for invoation
     */
    NONE,

    /**
     * The beforeDelivery method has been called, and the next method called must be a message delivery method
     * or release.
     */
    BEFORE_CALLED,

    /**
     * The message delivery method has been called successfully, and the next method called must be
     * another message delivery method, afterDelivery, or release.
     */
    METHOD_CALLED,

    /**
     * The message delivery threw a system exception, and the next method called must be afterDelivery
     * or release.  This state notified the afterDelivery method that the instace must be replaced with a new
     * instance.
     */
    SYSTEM_EXCEPTION,

    /**
     * This message endpoint handler has been released and can no longer be used.
     */
    RELEASED
}
