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
package org.apache.openejb.tck.cdi.embedded;

import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.context.AbstractContext;
import org.apache.webbeans.spi.ContextsService;
import org.jboss.cdi.tck.spi.Contexts;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.spi.Context;

/**
 * @version $Rev$ $Date$
 */
public class ContextsImpl implements Contexts<Context> {
    @Override
    public Context getRequestContext() {
        final ContextsService contextService = WebBeansContext.currentInstance().getContextsService();
        final Context ctx = contextService.getCurrentContext(RequestScoped.class);
        if (ctx == null || !ctx.isActive()) {
            contextService.startContext(RequestScoped.class, null);
        }
        return contextService.getCurrentContext(RequestScoped.class);
    }

    @Override
    public void setActive(final Context context) {
        if (AbstractContext.class.isInstance(context)) {
            AbstractContext.class.cast(context).setActive(true);
        }
    }

    @Override
    public void setInactive(final Context context) {
        if (AbstractContext.class.isInstance(context)) {
            AbstractContext.class.cast(context).setActive(false);
        }
    }

    @Override
    public Context getDependentContext() {
        final ContextsService contextService = WebBeansContext.currentInstance().getContextsService();
        return contextService.getCurrentContext(Dependent.class);
    }

    @Override
    public void destroyContext(final Context context) {
        if (AbstractContext.class.isInstance(context)) {
            AbstractContext.class.cast(context).destroy();
        }
    }
}
