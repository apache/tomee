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

package org.apache.openejb.cdi;

import org.apache.webbeans.spi.ContextsService;

import jakarta.enterprise.context.ConversationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.SessionScoped;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;

// helper for embedded case
public final class ScopeHelper {
    private ScopeHelper() {
        // no-op
    }

    public static void startContexts(final ContextsService contextsService, final ServletContext servletContext, final HttpSession session) throws Exception {
        contextsService.startContext(SessionScoped.class, session);
        contextsService.startContext(RequestScoped.class, null);
        contextsService.startContext(ConversationScoped.class, null);
    }

    public static void stopContexts(final ContextsService contextsService, final ServletContext servletContext, final HttpSession session) throws Exception {
        contextsService.endContext(SessionScoped.class, session);
        contextsService.endContext(RequestScoped.class, null);
        contextsService.endContext(ConversationScoped.class, null);
        if (CdiAppContextsService.class.isInstance(contextsService)) {
            CdiAppContextsService.class.cast(contextsService).removeThreadLocals();
        }
    }
}
