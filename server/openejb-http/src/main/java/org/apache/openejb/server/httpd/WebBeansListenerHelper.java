/**
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
package org.apache.openejb.server.httpd;

import org.apache.webbeans.spi.ContextsService;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.spi.Context;
import jakarta.servlet.ServletRequestListener;

/**
 * @deprecated this features is imo highly questionable. We should rather fix the root of the issue
 */
public final class WebBeansListenerHelper {
    private static final ThreadLocal<Boolean> FAKE_REQUEST = new ThreadLocal<Boolean>();

    public static void destroyFakedRequest(final ServletRequestListener listener) {
        final Boolean faked = FAKE_REQUEST.get();
        try {
            if (faked != null && faked) {
                listener.requestDestroyed(null);
            }
        } finally {
            FAKE_REQUEST.remove();
        }
    }

    public static void ensureRequestScope(final ContextsService cs, final ServletRequestListener listener) {
        final Context reqCtx = cs.getCurrentContext(RequestScoped.class);
        if (reqCtx == null || !cs.getCurrentContext(RequestScoped.class).isActive()) {
            listener.requestInitialized(null);
            FAKE_REQUEST.set(true);
        }
    }

    private WebBeansListenerHelper() {
        // no-op
    }
}
