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
package org.apache.openejb.tck.cdi.tomee.embedded;

import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.context.AbstractContext;
import org.apache.webbeans.context.RequestContext;
import org.apache.webbeans.spi.ContextsService;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.RequestScoped;

/**
 * @version $Rev$ $Date$
 */
public class ContextsImpl implements org.jboss.jsr299.tck.spi.Contexts<AbstractContext> {

    public AbstractContext getRequestContext() {
        ContextsService contextFactory = WebBeansContext.currentInstance().getContextsService();
        RequestContext ctx = (RequestContext) contextFactory.getCurrentContext(RequestScoped.class);

        if (ctx == null) {
            contextFactory.startContext(RequestScoped.class, null);
        }

        return (AbstractContext) contextFactory.getCurrentContext(RequestScoped.class);
    }

    public void setActive(AbstractContext context) {
        context.setActive(true);

    }

    public void setInactive(AbstractContext context) {
        context.setActive(false);
    }

    public AbstractContext getDependentContext() {
        ContextsService contextFactory = WebBeansContext.currentInstance().getContextsService();
        return (AbstractContext) contextFactory.getCurrentContext(Dependent.class);
    }

    public void destroyContext(AbstractContext context) {
        context.destroy();
    }
}
