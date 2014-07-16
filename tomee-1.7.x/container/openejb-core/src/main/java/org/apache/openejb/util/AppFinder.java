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
package org.apache.openejb.util;

import org.apache.openejb.AppContext;
import org.apache.openejb.core.WebContext;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.webbeans.config.WebBeansContext;

public final class AppFinder {
    public static <T> T findAppContextOrWeb(final ClassLoader cl, final Transformer<T> transformer) {
        final ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
        for (final AppContext appContext : containerSystem.getAppContexts()) {
            final ClassLoader appContextClassLoader = appContext.getClassLoader();
            if (appContextClassLoader.equals(cl) || (cl != null && cl.equals(appContextClassLoader))) { // CxfContainerLoader is not symmetric
                return transformer.from(appContext);
            }
            for (final WebContext web : appContext.getWebContexts()) {
                final ClassLoader webClassLoader = web.getClassLoader();
                if (webClassLoader.equals(cl) || (cl != null && cl.equals(webClassLoader))) {
                    return transformer.from(web);
                }
            }
        }
        return null;
    }

    public interface Transformer<T> {
        T from(AppContext appCtx);

        T from(WebContext webCtx);
    }

    public static class WebBeansContextTransformer implements Transformer<WebBeansContext> {
        public static final Transformer<WebBeansContext> INSTANCE = new WebBeansContextTransformer();

        @Override
        public WebBeansContext from(final AppContext appCtx) {
            return appCtx.getWebBeansContext();
        }

        @Override
        public WebBeansContext from(final WebContext webCtx) {
            final WebBeansContext webBeansContext = webCtx.getWebBeansContext();
            if (webBeansContext != null) { // ear
                return webBeansContext;
            }
            // war
            return from(webCtx.getAppContext());
        }
    }

    private AppFinder() {
        // no-op
    }
}
