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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tomee.catalina;

import org.apache.catalina.ContainerEvent;
import org.apache.catalina.ContainerListener;
import org.apache.catalina.Context;
import org.apache.catalina.core.StandardContext;
import org.apache.openejb.cdi.CdiAppContextsService;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.spi.ContextsService;

public class TomEEContainerListener implements ContainerListener {
    private static final ThreadLocal<StandardContext> context = new ThreadLocal<StandardContext>();

    @Override
    public void containerEvent(final ContainerEvent event) {
        if ("beforeContextInitialized".equals(event.getType())) {
            context.set((StandardContext) event.getContainer());
        } else if ("afterContextInitialized".equals(event.getType())) {
            context.remove();
        } else if (Context.CHANGE_SESSION_ID_EVENT.endsWith(event.getType())) {
            final String[] ids = (String[]) event.getData();

            final WebBeansContext wbc = WebBeansContext.currentInstance();
            final ContextsService cs = wbc.getContextsService();
            if (CdiAppContextsService.class.isInstance(cs) && ids.length > 0) {
                ((CdiAppContextsService) cs).updateSessionIdMapping(ids[0], ids[1]);
            }
        }
    }

    public static StandardContext get() {
        return context.get();
    }
}
