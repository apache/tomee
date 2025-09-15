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
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.spi.ContextsService;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.spi.Context;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @version $Rev$ $Date$
 */
public class RequestScopedThreadContextListener implements ThreadContextListener {

    private static Logger LOG = Logger.getInstance(LogCategory.OPENEJB.createChild("ThreadContext"), "org.apache.openejb.cdi");

    @Override
    public void contextEntered(final ThreadContext oldContext, final ThreadContext newContext) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("ThreadContextListener contextEntered: oldContext=" + oldContext + ", newContext=" + newContext + ", from=" + fromStackTrace(new Exception()));
        }
        final BeanContext beanContext = newContext.getBeanContext();

        final WebBeansContext webBeansContext = beanContext.getModuleContext().getAppContext().getWebBeansContext();
        if (webBeansContext == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("ThreadContextListener contextEntered: webBeansContext is null");
            }
            return;
        }

        final ContextsService contextsService = webBeansContext.getContextsService();

        final Context requestContext = CdiAppContextsService.class.cast(contextsService).getRequestContext(false);

        if (requestContext == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("ThreadContextListener contextEntered: requestContext is null, creating new request scope");
            }
            contextsService.startContext(RequestScoped.class, CdiAppContextsService.EJB_REQUEST_EVENT);
            newContext.set(DestroyContext.class, new DestroyContext(contextsService, newContext));
        }
    }

    @Override
    public void contextExited(final ThreadContext exitedContext, final ThreadContext reenteredContext) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("ThreadContextListener contextExited: exitedContext=" + exitedContext + ", reenteredContext=" + reenteredContext + ", from=" + fromStackTrace(new Exception()));
        }
        if (exitedContext == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("ThreadContextListener contextExited: exitedContext is null, not destroying RequestScope");
            }
            return;
        }

        final DestroyContext destroyContext = exitedContext.get(DestroyContext.class);

        if (destroyContext == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("ThreadContextListener contextExited: destroyContext is null, not destroying RequestScope");
            }
            return;
        }

        if (destroyContext.threadContext != exitedContext) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("ThreadContextListener contextExited: destroyContext does not match exited context, not destroying RequestScope");
            }
            return;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("ThreadContextListener contextExited: destroying RequestScope");
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

    private static String fromStackTrace(final Throwable throwable) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        pw.flush();

        return sw.toString();
    }
}
