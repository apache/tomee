/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.tomee.embedded.internal;

import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.webresources.StandardRoot;
import org.apache.openejb.config.WebModule;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.observer.Observes;
import org.apache.openejb.util.URLs;
import org.apache.tomee.catalina.TomcatWebAppBuilder;

import java.io.File;
import java.net.URL;

public class StandardContextCustomizer {
    private final WebModule module;

    public StandardContextCustomizer(final WebModule webModule) {
        module = webModule;
    }

    public void customize(final @Observes LifecycleEvent event) {
        final Object data = event.getSource();
        if (!StandardContext.class.isInstance(data)) {
            return;
        }

        final StandardContext context = StandardContext.class.cast(data);
        if (!module.getContextRoot().equals(context.getPath())) {
            return;
        }

        switch (event.getType()) {
            case Lifecycle.BEFORE_START_EVENT:
                context.setResources(new StandardRoot(context));

                final WebResourceRoot resources = context.getResources();
                for (final URL url : module.getScannableUrls()) {
                    final File file = URLs.toFile(url);
                    if (file.isDirectory()) {
                        resources.createWebResourceSet(WebResourceRoot.ResourceSetType.RESOURCE_JAR, "/WEB-INF/classes", file.getAbsolutePath(), "", "/");
                    } else {
                        resources.createWebResourceSet(WebResourceRoot.ResourceSetType.RESOURCE_JAR, "/WEB-INF/lib", file.getAbsolutePath(), "", "/");
                    }
                }
                break;
            case Lifecycle.CONFIGURE_START_EVENT:
                SystemInstance.get().getComponent(TomcatWebAppBuilder.class).setFinderOnContextConfig(StandardContext.class.cast(data), module.appModule());
                break;
            default:
        }
    }
}
