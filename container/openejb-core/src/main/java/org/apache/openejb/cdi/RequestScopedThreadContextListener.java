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

import org.apache.openejb.BeanContext;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.ThreadContextListener;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.spi.ContextsService;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.spi.Context;

/**
 * @version $Rev$ $Date$
 */
public class RequestScopedThreadContextListener implements ThreadContextListener {
    @Override
    public void contextEntered(final ThreadContext oldContext, final ThreadContext newContext) {

        final BeanContext beanContext = newContext.getBeanContext();

        final WebBeansContext webBeansContext = beanContext.getModuleContext().getAppContext().getWebBeansContext();
        if (webBeansContext == null) {
            return;
        }

        final ContextsService contextsService = webBeansContext.getContextsService();

        final Context requestContext = CdiAppContextsService.class.cast(contextsService).getRequestContext(false);

        if (requestContext == null) {
            contextsService.startContext(RequestScoped.class, CdiAppContextsService.EJB_REQUEST_EVENT);
            newContext.set(DestroyContext.class, new DestroyContext(contextsService, newContext));
        }
    }

    @Override
    public void contextExited(final ThreadContext exitedContext, final ThreadContext reenteredContext) {
        if (exitedContext == null) {
            return;
        }

        final DestroyContext destroyContext = exitedContext.get(DestroyContext.class);

        if (destroyContext == null || destroyContext.threadContext != exitedContext) {
            return;
        }

        destroyContext.contextsService.endContext(RequestScoped.class, CdiAppContextsService.EJB_REQUEST_EVENT);
        destroyContext.contextsService.removeThreadLocals();
    }

    private static final class DestroyContext {
        private final ContextsService contextsService;
        private final ThreadContext threadContext;

        private DestroyContext(final ContextsService contextsService, final ThreadContext threadContext) {
            this.contextsService = contextsService;
            this.threadContext = threadContext;
        }
    }
}
