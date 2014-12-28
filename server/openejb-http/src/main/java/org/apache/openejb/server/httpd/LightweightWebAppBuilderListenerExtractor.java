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
package org.apache.openejb.server.httpd;

import org.apache.openejb.AppContext;
import org.apache.openejb.assembler.classic.WebAppBuilder;
import org.apache.openejb.core.WebContext;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.web.LightweightWebAppBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

// could be optimized since we could bind to the request the listeners in org.apache.openejb.server.httpd.OpenEJBHttpRegistry.ClassLoaderHttpListener.onMessage()
// but ok for now since that's fully for the embedded mode
public final class LightweightWebAppBuilderListenerExtractor {
    public static  <T> Collection<T> findByTypeForContext(final String context, final Class<T> type) {
        final WebAppBuilder builder = SystemInstance.get().getComponent(WebAppBuilder.class);
        if (!LightweightWebAppBuilder.class.isInstance(builder)) {
            return Collections.emptyList();
        }

        for (final AppContext app : SystemInstance.get().getComponent(ContainerSystem.class).getAppContexts()) {
            for (final WebContext web : app.getWebContexts()) {
                if (web.getContextRoot().replace("/", "").equals(context.replace("/", ""))) {
                    final Collection<Object> potentials = LightweightWebAppBuilder.class.cast(builder).listenersFor(web.getContextRoot());
                    if (potentials == null) {
                        return Collections.emptyList();
                    }
                    final Collection<T> filtered = new ArrayList<>(potentials.size());
                    for (final Object o : potentials) {
                        if (type.isInstance(o)) {
                            filtered.add(type.cast(o));
                        }
                    }
                    return filtered;
                }
            }
        }
        return Collections.emptyList();
    }

    private LightweightWebAppBuilderListenerExtractor() {
        // no-op
    }
}
