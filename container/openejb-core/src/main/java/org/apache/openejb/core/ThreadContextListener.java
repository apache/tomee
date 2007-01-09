/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.core;

public interface ThreadContextListener {
    /**
     * A new context has been entered.  The new context is already associated with the thread.
     * @param oldContext the old context that was associated with the thread
     * @param newContext the new context that is now associated with the thread
     */
    void contextEntered(ThreadContext oldContext, ThreadContext newContext);

    /**
     * A context has exited.  The reentered context is already associated with the thread.
     * @param exitedContext the context that was exited
     * @param reenteredContext the context that is not associated with the thread
     */
    void contextExited(ThreadContext exitedContext, ThreadContext reenteredContext);
}
