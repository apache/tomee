/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.apache.tomee.livereload;

import org.apache.catalina.Container;
import org.apache.catalina.Engine;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.Server;
import org.apache.catalina.Service;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.servlets.DefaultServlet;
import org.apache.tomcat.websocket.server.WsSci;
import org.apache.tomee.catalina.IgnoredStandardContext;
import org.apache.tomee.catalina.OpenEJBValve;
import org.apache.tomee.catalina.remote.ServerClassLoaderLoader;
import org.apache.tomee.loader.TomcatHelper;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;


public class LiveReloadInstaller {
    private LiveReloadInstaller() {
        // no-op
    }

    public static void install(String path, final int port, final String folder) {
        final Server server = TomcatHelper.getServer();
        if (server == null) {
            throw new IllegalStateException("tomcat not yet starting");
        }

        final Service service = server.findServices()[0]; // checking which one is localhost could be better but should be fine
        final Engine engine = Engine.class.cast(service.getContainer());
        final Container host = engine.findChild(engine.getDefaultHost() /* likelly localhost */);
        if (LifecycleState.STARTED != host.getState()) {
            throw new IllegalStateException("host not started, call LiveReloadInstaller.install() later.");
        }

        // add connector
        final Connector connector = new Connector();
        connector.setPort(port);
        connector.setProperty("connectionTimeout", "30000");
        service.addConnector(connector);

        // and the endpoint and start the watcher
        final Closeable watch = Instances.get().getWatcher().watch(folder);
        final LiveReloadWebapp liveReloadWebapp = new LiveReloadWebapp(path);
        liveReloadWebapp.addApplicationLifecycleListener(new ServletContextListener() {
            @Override
            public void contextInitialized(final ServletContextEvent servletContextEvent) {
                servletContextEvent.getServletContext().log("Started livereload server on port " + port);
            }

            @Override
            public void contextDestroyed(final ServletContextEvent servletContextEvent) {
                try {
                    watch.close();
                } catch (final IOException e) {
                    // no-op: not that important, we shutdown anyway
                }
            }
        });
        host.addChild(liveReloadWebapp);
    }

    private static class LiveReloadWebapp extends IgnoredStandardContext {
        private LiveReloadWebapp(final String path) {
            final boolean isRoot = path == null || "/".equals(path) || "ROOT".equals(path);

            setDocBase("");
            setParentClassLoader(LiveReloadInstaller.class.getClassLoader());
            setDelegate(true);
            setName(isRoot ? "" : (path.startsWith("/") ? path : ('/' + path)));
            setPath(getName());
            setLoader(new ServerClassLoaderLoader(this));
            addValve(new OpenEJBValve()); // Ensure security context is reset (ThreadLocal) for each request
        }

        @Override
        protected void initInternal() throws LifecycleException {
            super.initInternal();

            {// fake/not really desired "/*" servlet to ensure in context valve we match the request and got to WS filter
                final Wrapper servlet = createWrapper();
                servlet.setName(DefaultServlet.class.getSimpleName());
                servlet.setServletClass(DefaultServlet.class.getName());
                addChild(servlet);
                addServletMappingDecoded("/*", DefaultServlet.class.getSimpleName());
            }

            {// the js injected in the page
                final Wrapper servlet = createWrapper();
                servlet.setName(LiveReloadJs.class.getSimpleName());
                servlet.setServletClass(LiveReloadJs.class.getName());
                addChild(servlet);
                addServletMappingDecoded("/livereload.js", LiveReloadJs.class.getSimpleName());
            }
        }

        @Override
        protected void startInternal() throws LifecycleException {
            addServletContainerInitializer(new WsSci(), Collections.<Class<?>>singleton(LiveReloadEndpoint.class));
            super.startInternal();
        }
    }
}
