/*
 * Copyright 2018 OmniFaces.
 * Copyright 2003-2011 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tomee.catalina.security;

import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Wrapper;
import org.apache.catalina.core.StandardContext;
import org.apache.openejb.assembler.classic.DelegatePermissionCollection;
import org.apache.openejb.assembler.classic.PolicyContext;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;

import jakarta.security.jacc.PolicyContextException;
import jakarta.security.jacc.WebResourcePermission;
import jakarta.security.jacc.WebRoleRefPermission;
import jakarta.security.jacc.WebUserDataPermission;
import jakarta.servlet.ServletContext;
import java.security.PermissionCollection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;

/**
 * @author Guillermo González de Agüero
 */
public class TomcatSecurityConstaintsToJaccPermissionsTransformer {

    // from context
    final StandardContext standardContext;
    private final List<SecurityConstraint> constraints;
    private final List<String> declaredRoles;
    private final boolean isDenyUncoveredHttpMethods;

    // final result
    private final PolicyContext policyContext;

    // computed in various methods
    private final Set<String> securityRoles = new HashSet<>();
    private final Map<String, URLPattern> uncheckedPatterns = new HashMap<>();
    private final Map<UncheckedItem, HTTPMethods> uncheckedResourcePatterns = new HashMap<>();
    private final Map<UncheckedItem, HTTPMethods> uncheckedUserPatterns = new HashMap<>();
    private final Map<String, URLPattern> excludedPatterns = new HashMap<>();
    private final Map<String, Map<String, URLPattern>> rolesPatterns = new HashMap<>();
    private final Set<URLPattern> allSet = new HashSet<>();
    private final Map<String, URLPattern> allMap = new HashMap<>(); //uncheckedPatterns union excludedPatterns union rolesPatterns.

    public TomcatSecurityConstaintsToJaccPermissionsTransformer(final StandardContext standardContext) {
        this.standardContext = standardContext;

        constraints = new ArrayList<>(asList(standardContext.findConstraints()));
        declaredRoles = asList(standardContext.findSecurityRoles());
        isDenyUncoveredHttpMethods = standardContext.getDenyUncoveredHttpMethods();

        // todo move all host context id crap into something consistent like in Tomcat JASPIC`
        // instead of only using host and context path - consistency and safe
        final ServletContext servletContext = standardContext.getServletContext();
        final String id = servletContext.getVirtualServerName() + " " + servletContext.getContextPath();
        policyContext = new PolicyContext(id);
    }

    public PolicyContext createResourceAndDataPermissions() {

            securityRoles.addAll(declaredRoles);

            // todo, improve this part so roles are extracted in the constructor and we don't need standard context
            // it should even receive in the constructor all required information from TomcatWebAppBuilder so it's more testable
            // find all role ref permission - probably too wide
            for (Container container : standardContext.findChildren()) {
                if (container instanceof Wrapper) {
                    processRoleRefPermissions((Wrapper) container);
                }
            }

            addUnmappedJSPPermissions();
            analyzeSecurityConstraints();
            removeExcludedDups();
            buildPermissions();

            // all populated now
            return policyContext;

    }

    private void analyzeSecurityConstraints() {
        for (SecurityConstraint securityConstraint : constraints) {
            Map<String, URLPattern> currentPatterns = null;
            Set<String> roleNames = null;
            if (securityConstraint.getAuthConstraint()) {
                if (securityConstraint.findAuthRoles().length == 0) {
                    currentPatterns = excludedPatterns;

                } else {
                    roleNames = new HashSet<String>(Arrays.asList(securityConstraint.findAuthRoles()));
                    if (roleNames.remove("*")) {
                        roleNames.addAll(securityRoles);
                    }
                }

            } else {
                currentPatterns = uncheckedPatterns;
            }
            String transport = securityConstraint.getUserConstraint() == null ? "NONE" : securityConstraint.getUserConstraint();

            boolean isRoleBasedPattern = (currentPatterns == null);

            if (securityConstraint.findCollections() != null) {
                for (SecurityCollection webResourceCollection : securityConstraint.findCollections()) {
                    //Calculate HTTP methods list
                    for (String urlPattern : webResourceCollection.findPatterns()) {

                        if (isRoleBasedPattern) {
                            for (String roleName : roleNames) {
                                Map<String, URLPattern> currentRolePatterns = rolesPatterns.get(roleName);
                                if (currentRolePatterns == null) {
                                    currentRolePatterns = new HashMap<>();
                                    rolesPatterns.put(roleName, currentRolePatterns);
                                }

                                boolean omission = false;
                                String[] httpMethods = webResourceCollection.findMethods();
                                if (httpMethods.length == 0) {
                                    omission = true;
                                    httpMethods = webResourceCollection.findOmittedMethods();
                                }

                                analyzeURLPattern(urlPattern, new HashSet<>(Arrays.asList(httpMethods)), omission, transport, currentRolePatterns);
                            }

                        } else {
                            boolean omission = false;
                            String[] httpMethods = webResourceCollection.findMethods();
                            if (httpMethods.length == 0) {
                                omission = true;
                                httpMethods = webResourceCollection.findOmittedMethods();
                            }

                            analyzeURLPattern(urlPattern, new HashSet<>(Arrays.asList(httpMethods)), omission, transport, currentPatterns);
                        }
                        URLPattern allPattern = allMap.get(urlPattern);

                        if (allPattern == null) {
                            boolean omission = false;
                            String[] httpMethods = webResourceCollection.findMethods();
                            if (httpMethods.length == 0) {
                                omission = true;
                                httpMethods = webResourceCollection.findOmittedMethods();
                            }

                            allPattern = new URLPattern(urlPattern, new HashSet<>(Arrays.asList(httpMethods)), omission);
                            allSet.add(allPattern);
                            allMap.put(urlPattern, allPattern);

                        } else {
                            boolean omission = false;
                            String[] httpMethods = webResourceCollection.findMethods();
                            if (httpMethods.length == 0) {
                                omission = true;
                                httpMethods = webResourceCollection.findOmittedMethods();
                            }

                            allPattern.addMethods(new HashSet<>(Arrays.asList(httpMethods)), omission);
                        }

                    }
                }
            }
        }
    }

    private void analyzeURLPattern(final String urlPattern,
                                final Set<String> httpMethods,
                                final boolean omission,
                                final String transport,
                                final Map<String, URLPattern> currentPatterns) {

        URLPattern pattern = currentPatterns.get(urlPattern);
        if (pattern == null) {
            pattern = new URLPattern(urlPattern, httpMethods, omission);
            currentPatterns.put(urlPattern, pattern);

        } else {
            pattern.addMethods(httpMethods, omission);
        }
        pattern.setTransport(transport);
    }

    private void removeExcludedDups() {
        for (Map.Entry<String, URLPattern> excluded : excludedPatterns.entrySet()) {
            String url = excluded.getKey();
            URLPattern pattern = excluded.getValue();
            removeExcluded(url, pattern, uncheckedPatterns);
            for (Map<String, URLPattern> rolePatterns : rolesPatterns.values()) {
                removeExcluded(url, pattern, rolePatterns);
            }
        }
    }

    private void removeExcluded(final String url, final URLPattern pattern, final Map<String, URLPattern> patterns) {
        URLPattern testPattern = patterns.get(url);
        if (testPattern != null) {
            if (!testPattern.removeMethods(pattern)) {
                patterns.remove(url);
            }
        }
    }

    private void buildPermissions() {

        for (URLPattern pattern : excludedPatterns.values()) {
            String name = pattern.getQualifiedPattern(allSet);
            String actions = pattern.getMethods();
            policyContext.getExcludedPermissions().add(new WebResourcePermission(name, actions));
            policyContext.getExcludedPermissions().add(new WebUserDataPermission(name, actions));
        }

        for (Map.Entry<String, Map<String, URLPattern>> entry : rolesPatterns.entrySet()) {
            Set<URLPattern> currentRolePatterns = new HashSet<URLPattern>(entry.getValue().values());
            for (URLPattern pattern : entry.getValue().values()) {
                String name = pattern.getQualifiedPattern(currentRolePatterns);
                String actions = pattern.getMethods();
                WebResourcePermission permission = new WebResourcePermission(name, actions);
                policyContext.addRole(entry.getKey(), permission);
                HTTPMethods methods = pattern.getHTTPMethods();
                int transportType = pattern.getTransport();
                addOrUpdatePattern(uncheckedUserPatterns, name, methods, transportType);
            }
        }

        for (URLPattern pattern : uncheckedPatterns.values()) {
            String name = pattern.getQualifiedPattern(allSet);
            HTTPMethods methods = pattern.getHTTPMethods();
            addOrUpdatePattern(uncheckedResourcePatterns, name, methods, URLPattern.NA);
            int transportType = pattern.getTransport();
            addOrUpdatePattern(uncheckedUserPatterns, name, methods, transportType);
        }

        /*
         * A <code>WebResourcePermission</code> and a
         * <code>WebUserDataPermission</code> must be instantiated for each
         * <tt>url-pattern</tt> in the deployment descriptor and the default
         * pattern "/", that is not combined by the
         * <tt>web-resource-collection</tt> elements of the deployment
         * descriptor with ever HTTP method value. The permission objects must
         * be contructed using the qualified pattern as their name and with
         * actions defined by the subset of the HTTP methods that do not occur
         * in combination with the pattern. The resulting permissions that must
         * be added to the unchecked policy statements by calling the
         * <code>addToUncheckedPolcy</code> method on the
         * <code>PolicyConfiguration</code> object.
         */
        for (URLPattern pattern : allSet) {
            String name = pattern.getQualifiedPattern(allSet);
            HTTPMethods methods = pattern.getComplementedHTTPMethods();
            if (methods.isNone()) {
                continue;
            }
            addOrUpdatePattern(uncheckedResourcePatterns, name, methods, URLPattern.NA);
            addOrUpdatePattern(uncheckedUserPatterns, name, methods, URLPattern.NA);
        }

        if (!allMap.containsKey("/")) {
            URLPattern pattern = new URLPattern("/", Collections.<String>emptySet(), false);
            String name = pattern.getQualifiedPattern(allSet);
            HTTPMethods methods = pattern.getComplementedHTTPMethods();
            addOrUpdatePattern(uncheckedResourcePatterns, name, methods, URLPattern.NA);
            addOrUpdatePattern(uncheckedUserPatterns, name, methods, URLPattern.NA);
        }

        //Create the uncheckedPermissions for WebResourcePermissions
        for (UncheckedItem item : uncheckedResourcePatterns.keySet()) {
            HTTPMethods methods = uncheckedResourcePatterns.get(item);
            String actions = URLPattern.getMethodsWithTransport(methods, item.getTransportType());
            policyContext.getUncheckedPermissions().add(new WebResourcePermission(item.getName(), actions));
        }

        //Create the uncheckedPermissions for WebUserDataPermissions
        for (UncheckedItem item : uncheckedUserPatterns.keySet()) {
            HTTPMethods methods = uncheckedUserPatterns.get(item);
            String actions = URLPattern.getMethodsWithTransport(methods, item.getTransportType());
            policyContext.getUncheckedPermissions().add(new WebUserDataPermission(item.getName(), actions));
        }
    }

    private void addOrUpdatePattern(final Map<UncheckedItem, HTTPMethods> patternMap,
                            final String name,
                            final HTTPMethods actions,
                            final int transportType) {

        final UncheckedItem item = new UncheckedItem(name, transportType);
        final HTTPMethods existingActions = patternMap.get(item);
        if (existingActions != null) {
            patternMap.put(item, existingActions.add(actions));

        } else {
            patternMap.put(item, new HTTPMethods(actions, false));
        }
    }

    protected void processRoleRefPermissions(Wrapper servlet) {

        final String servletName = servlet.getName();

        //WebRoleRefPermissions
        Set<String> unmappedRoles = new HashSet<>(securityRoles);
        for (String securityRoleRef : servlet.findSecurityReferences()) {
            //jacc 3.1.3.2
            /*   The name of the WebRoleRefPermission must be the servlet-name in whose
             * context the security-role-ref is defined. The actions of the  WebRoleRefPermission
             * must be the value of the role-name (that is the  reference), appearing in the security-role-ref.
             * The deployment tools must  call the addToRole method on the PolicyConfiguration object to add the
             * WebRoleRefPermission object resulting from the translation to the role
             * identified in the role-link appearing in the security-role-ref.
             */
            policyContext.addRole(servlet.findSecurityReference(securityRoleRef),
                                          new WebRoleRefPermission(servletName, securityRoleRef));
            unmappedRoles.remove(securityRoleRef);
        }

        for (String roleName : unmappedRoles) {
            policyContext.addRole(roleName, new WebRoleRefPermission(servletName, roleName));
        }
    }

    protected void addUnmappedJSPPermissions() {
        for (String roleName : securityRoles) {
            policyContext.addRole(roleName, new WebRoleRefPermission("", roleName));
        }
    }

}
