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
package org.apache.openejb.tck.cdi.tomee.embedded;

import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.context.AbstractContext;
import org.apache.webbeans.context.ContextFactory;
import org.apache.webbeans.context.RequestContext;
import org.apache.webbeans.context.type.ContextTypes;

import javax.enterprise.context.RequestScoped;

/**
 * @version $Rev$ $Date$
 */
public class ContextsImpl implements org.jboss.jsr299.tck.spi.Contexts<AbstractContext> {

    public AbstractContext getRequestContext() {
        ContextFactory contextFactory = WebBeansContext.getInstance().getContextFactory();
        RequestContext ctx = (RequestContext) contextFactory.getStandardContext(RequestScoped.class);

        if (ctx == null) {
            contextFactory.initRequestContext(null);
        }

        return (AbstractContext) contextFactory.getStandardContext(ContextTypes.REQUEST);
    }

    public void setActive(AbstractContext context) {
        context.setActive(true);

    }

    public void setInactive(AbstractContext context) {
        context.setActive(false);
    }

    public AbstractContext getDependentContext() {
        ContextFactory contextFactory = WebBeansContext.getInstance().getContextFactory();
        return (AbstractContext) contextFactory.getStandardContext(ContextTypes.DEPENDENT);
    }

    public void destroyContext(AbstractContext context) {
        context.destroy();
    }
}
