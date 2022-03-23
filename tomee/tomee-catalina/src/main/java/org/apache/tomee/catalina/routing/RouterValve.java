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

package org.apache.tomee.catalina.routing;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.apache.openejb.config.DeploymentLoader;
import org.apache.openejb.loader.SystemInstance;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class RouterValve extends ValveBase {
    public static final String ROUTER_CONF = "tomee-router.conf";
    public static final String WEB_INF = "/WEB-INF/";

    private SimpleRouter router = new SimpleRouter();

    @Override
    public void invoke(final Request request, final Response response) throws IOException, ServletException {
        final String destination = router.route(request.getRequestURI());
        if (destination == null) {
            getNext().invoke(request, response);
            return;
        }

        if (router.hasPrefix()) {
            request.getRequestDispatcher(destination).forward(request, response);
        } else {
            response.sendRedirect(destination);
        }
    }

    public void setConfigurationPath(final URL configurationPath) {
        router.readConfiguration(configurationPath);
    }

    @Override
    protected synchronized void startInternal() throws LifecycleException {
        super.startInternal();
        router.JMXOn("Router Valve " + System.identityHashCode(this));
    }

    @Override
    protected synchronized void stopInternal() throws LifecycleException {
        router.cleanUp();
        super.stopInternal();
    }

    public static URL configurationURL(final ServletContext ctx) {
        try {
            return ctx.getResource(WEB_INF + routerConfigurationName());
        } catch (final MalformedURLException e) {
            // let return null
        }

        return null;
    }

    public static String routerConfigurationName() {
        final String conf = SystemInstance.get().getOptions().get(DeploymentLoader.OPENEJB_ALTDD_PREFIX, (String) null);
        if (conf == null) {
            return ROUTER_CONF;
        } else {
            return conf + "." + ROUTER_CONF;
        }
    }

    public static URL serverRouterConfigurationURL() {
        final File confDir = SystemInstance.get().getHome().getDirectory();
        final File configFile = new File(confDir, "conf/" + routerConfigurationName());

        if (configFile.exists()) {
            try {
                return configFile.toURI().toURL();
            } catch (final MalformedURLException e) {
                // let return null
            }
        }

        return null;
    }

    public void setPrefix(final String name) {
        if (name == null || "/".equals(name)) {
            router.setPrefix("");
        } else {
            router.setPrefix(name);
        }
    }
}
