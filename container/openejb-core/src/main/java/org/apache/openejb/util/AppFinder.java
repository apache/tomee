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
            boolean found = false;
            if (appContextClassLoader.equals(cl) || (cl != null && cl.equals(appContextClassLoader))) { // CxfContainerLoader is not symmetric
                final T from = transformer.from(appContext);
                found = true;
                if (from != null) {
                    return from;
                }
            }
            for (final WebContext web : appContext.getWebContexts()) {
                final ClassLoader webClassLoader = web.getClassLoader();
                if (webClassLoader.equals(cl) || (cl != null && cl.equals(webClassLoader))) {
                    return transformer.from(web);
                }
            }
            if (found) { // for cases where app and webapp share the same classloader
                break;
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

    public static class AppContextTransformer implements Transformer<AppContext> {
        public static final Transformer<AppContext> INSTANCE = new AppContextTransformer();

        @Override
        public AppContext from(final AppContext appCtx) {
            return appCtx;
        }

        @Override
        public AppContext from(final WebContext webCtx) {
            return webCtx.getAppContext();
        }
    }

    public static class AppOrWebContextTransformer implements Transformer<Object> {
        public static final Transformer<AppContext> INSTANCE = new AppContextTransformer();

        @Override
        public Object from(final AppContext appCtx) {
            return appCtx;
        }

        @Override
        public Object from(final WebContext webCtx) {
            return webCtx;
        }
    }

    private AppFinder() {
        // no-op
    }
}
