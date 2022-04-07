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

import org.apache.catalina.Context;
import org.apache.catalina.Globals;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.Loader;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.webresources.StandardRoot;
import org.apache.openejb.config.WebModule;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.observer.Observes;
import org.apache.openejb.util.URLs;
import org.apache.openejb.util.reflection.Reflections;
import org.apache.tomee.catalina.TomcatWebAppBuilder;
import org.apache.tomee.embedded.Configuration;
import org.apache.tomee.embedded.SecurityConstaintBuilder;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StandardContextCustomizer {
    private final WebModule module;
    private final Configuration config;
    private final ClassLoader loader;

    public StandardContextCustomizer(final Configuration configuration, final WebModule webModule, final boolean keepClassloader) {
        module = webModule;
        config = configuration;
        loader = keepClassloader ? Thread.currentThread().getContextClassLoader() : null;
    }

    public void customize(@Observes final LifecycleEvent event) {
        final Object data = event.getSource();
        if (!StandardContext.class.isInstance(data)) {
            return;
        }

        final StandardContext context = StandardContext.class.cast(data);
        final String contextRoot = module.getContextRoot();
        final String path = context.getPath();
        final boolean rightPath = (path.isEmpty() && contextRoot.equals(path))
                || (contextRoot.startsWith("/") ? contextRoot : '/' + contextRoot).equals(path);
        if (!rightPath) {
            return;
        }

        switch (event.getType()) {
            case Lifecycle.BEFORE_START_EVENT:
                final StandardRoot resources = new StandardRoot(context);
                resources.setCachingAllowed(config.areWebResourcesCached());

                context.setResources(resources);
                if (!module.getProperties().containsKey("fakeJarLocation")) {
                    context.setDocBase(module.getJarLocation());
                }

                // move last fake folder, tomcat is broken without it so we can't remove it
                final List allResources = List.class.cast(Reflections.get(resources, "allResources"));
                final Object mainResources = allResources.remove(1);
                allResources.add(mainResources);

                for (final URL url : module.getScannableUrls()) {
                    final File file = URLs.toFile(url);
                    final String absolutePath = file.getAbsolutePath();
                    if (file.isDirectory()) {
                        resources.createWebResourceSet(WebResourceRoot.ResourceSetType.CLASSES_JAR, "/WEB-INF/classes", absolutePath, "", "/");
                        if (new File(file, "META-INF/resources").exists()) {
                            resources.createWebResourceSet(WebResourceRoot.ResourceSetType.RESOURCE_JAR, "/", absolutePath, "", "/META-INF/resources");
                        }
                    } else {
                        if (absolutePath.endsWith(".jar") || Boolean.getBoolean("tomee.embedded.resources.add-war-as-jar")) {
                            resources.createWebResourceSet(WebResourceRoot.ResourceSetType.CLASSES_JAR, "/WEB-INF/lib", absolutePath, null, "/");
                            resources.createWebResourceSet(WebResourceRoot.ResourceSetType.RESOURCE_JAR, "/", url, "/META-INF/resources");
                        } // else endsWith .war => ignore
                    }
                }
                if (config.getCustomWebResources() != null) {
                    for (final String web : config.getCustomWebResources()) {
                        final File file = new File(web);
                        if (file.isDirectory()) {
                            try {
                                resources.createWebResourceSet(WebResourceRoot.ResourceSetType.RESOURCE_JAR, "/", file.toURI().toURL(), "/");
                            } catch (final MalformedURLException e) {
                                throw new IllegalArgumentException(e);
                            }
                        } else {
                            Logger.getLogger(StandardContextCustomizer.class.getName())
                                    .log(Level.WARNING, "''{0}'' is not a directory, ignoring", web);
                        }
                    }
                }

                if (config.getLoginConfig() != null) {
                    context.setLoginConfig(config.getLoginConfig().build());
                }
                for (final SecurityConstaintBuilder sc : config.getSecurityConstraints()) {
                    context.addConstraint(sc.build());
                }
                if (config.getWebXml() != null) {
                    context.getServletContext().setAttribute(Globals.ALT_DD_ATTR, config.getWebXml());
                }

                if (loader != null) {
                    context.setLoader(new ProvidedLoader(loader));
                }
                break;
            case Lifecycle.CONFIGURE_START_EVENT:
                SystemInstance.get().getComponent(TomcatWebAppBuilder.class).setFinderOnContextConfig(context, module.appModule());
                break;
            default:
        }
    }

    private static final class ProvidedLoader implements Loader {
        private final ClassLoader delegate;
        private Context context;

        private ProvidedLoader(final ClassLoader loader) {
            this.delegate = loader;
        }

        @Override
        public void backgroundProcess() {
            // no-op
        }

        @Override
        public ClassLoader getClassLoader() {
            return delegate;
        }

        @Override
        public Context getContext() {
            return context;
        }

        @Override
        public void setContext(final Context context) {
            this.context = context;
        }

        @Override
        public boolean modified() {
            return false;
        }

        @Override
        public boolean getDelegate() {
            return false;
        }

        @Override
        public void setDelegate(final boolean delegate) {
            // ignore
        }

        @Override
        public void addPropertyChangeListener(final PropertyChangeListener listener) {
            // no-op
        }

        @Override
        public void removePropertyChangeListener(final PropertyChangeListener listener) {
            // no-op
        }
    }
}
