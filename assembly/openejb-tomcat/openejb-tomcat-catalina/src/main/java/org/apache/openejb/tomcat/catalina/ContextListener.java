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
package org.apache.openejb.tomcat.catalina;

import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.core.StandardServer;

/**
 * Listener interface for web context.
 *
 * @version $Rev$ $Date$
 */
public interface ContextListener {

    /**
     * Web context is initialized.
     *
     * @param context web context
     */
    void init(StandardContext context);

    /**
     * Called before starting context.
     *
     * @param context web context
     */
    void beforeStart(StandardContext context);

    /**
     * Called when starting context.
     *
     * @param context web context
     */
    void start(StandardContext context);

    /**
     * Called after starting context.
     *
     * @param context web context
     */
    void afterStart(StandardContext context);

    /**
     * Called before stopping context.
     *
     * @param context web context
     */
    void beforeStop(StandardContext context);

    /**
     * Called when stopping context.
     *
     * @param context web context
     */
    void stop(StandardContext context);

    /**
     * Called after starting context.
     *
     * @param context web context
     */
    void afterStop(StandardContext context);

    /**
     * Called when destroying context.
     *
     * @param context web context
     */
    void destroy(StandardContext context);

    /**
     * Called after stopping server
     *
     * @param standardServer server instance
     */
    void afterStop(StandardServer standardServer);

    /**
     * Called on periodic events.
     *
     * @param standardHost host
     */
    void checkHost(StandardHost standardHost);
}
