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

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Realm;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.util.RequestUtil;
import org.apache.catalina.valves.ValveBase;
import org.apache.openejb.config.DeploymentLoader;
import org.apache.openejb.loader.SystemInstance;
import org.apache.tomcat.util.buf.UDecoder;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Locale;

import static org.apache.tomcat.util.http.RequestUtil.normalize;

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
            final String normalized = normalizeDestination(request.getContext(), destination);
            if (normalized == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            if (!isDestinationAuthorized(request, normalized)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            request.getRequestDispatcher(destination).forward(request, response);
        } else {
            response.sendRedirect(destination);
        }
    }

    /**
     * Applies the same path processing as {@code ApplicationContext#getRequestDispatcher} and only
     * accepts destinations a client could request directly.
     *
     * @return the normalized destination path or {@code null} if it is not forwardable
     */
    static String normalizeDestination(final Context context, final String destination) {
        if (!destination.startsWith("/")) {
            return null;
        }

        String path = destination;
        final int query = path.indexOf('?');
        if (query >= 0) {
            path = path.substring(0, query);
        }

        path = RequestUtil.stripPathParams(path, null);

        if (context == null || context.getDispatchersUseEncodedPaths()) {
            try {
                path = context == null
                        ? UDecoder.URLDecode(path, StandardCharsets.UTF_8)
                        : UDecoder.URLDecode(path, StandardCharsets.UTF_8,
                            context.getEncodedSolidusHandlingEnum(), context.getEncodedReverseSolidusHandlingEnum());
            } catch (final IllegalArgumentException iae) {
                return null;
            }
        }

        path = normalize(path);
        if (path == null) {
            return null;
        }

        final String upper = path.toUpperCase(Locale.ENGLISH);
        if (upper.equals("/WEB-INF") || upper.startsWith("/WEB-INF/")
                || upper.equals("/META-INF") || upper.startsWith("/META-INF/")) {
            return null;
        }

        return path;
    }

    /**
     * Evaluates the context security constraints matching the forward destination against the current principal.
     */
    private boolean isDestinationAuthorized(final Request request, final String path) {
        final Context context = request.getContext();
        if (context == null) {
            return true;
        }

        final SecurityConstraint[] constraints = context.findConstraints();
        if (constraints == null) {
            return true;
        }

        for (final SecurityConstraint constraint : constraints) {
            if (!constraint.getAuthConstraint() || !constraintMatches(constraint, path)) {
                continue;
            }

            final Principal principal = request.getPrincipal();
            if (principal == null) {
                return false;
            }
            if (constraint.getAuthenticatedUsers()) { // "**"
                continue;
            }

            final String[] roles = constraint.getAllRoles() ? context.findSecurityRoles() : constraint.findAuthRoles();
            if (roles == null || roles.length == 0) { // auth-constraint without role: denies everybody
                return false;
            }

            final Realm realm = context.getRealm();
            if (realm == null) {
                return false;
            }

            boolean allowed = false;
            for (final String role : roles) {
                if (realm.hasRole(request.getWrapper(), principal, role)) {
                    allowed = true;
                    break;
                }
            }
            if (!allowed) {
                return false;
            }
        }

        return true;
    }

    private static boolean constraintMatches(final SecurityConstraint constraint, final String path) {
        for (final SecurityCollection collection : constraint.findCollections()) {
            for (final String pattern : collection.findPatterns()) {
                if (patternMatches(path, pattern)) {
                    return true;
                }
            }
        }
        return false;
    }

    // url-pattern matching as done for security constraints (servlet spec 13.8.3)
    static boolean patternMatches(final String path, final String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            return false;
        }
        if (pattern.equals(path) || "/".equals(pattern)) {
            return true;
        }
        if (pattern.startsWith("/") && pattern.endsWith("/*")) {
            final String prefix = pattern.substring(0, pattern.length() - 2);
            if (prefix.isEmpty()) {
                return true;
            }
            String current = path;
            while (true) {
                if (current.equals(prefix)) {
                    return true;
                }
                final int slash = current.lastIndexOf('/');
                if (slash <= 0) {
                    return false;
                }
                current = current.substring(0, slash);
            }
        }
        if (pattern.startsWith("*.")) {
            final int slash = path.lastIndexOf('/');
            final int period = path.lastIndexOf('.');
            return slash >= 0 && period > slash && path.length() > period + 1
                    && pattern.substring(2).equals(path.substring(period + 1));
        }
        return false;
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
