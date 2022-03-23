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

package org.apache.openejb.assembler.classic;

import org.apache.openejb.BeanContext;
import org.apache.openejb.InterfaceType;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.JavaSecurityManagers;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import jakarta.security.jacc.EJBMethodPermission;
import jakarta.security.jacc.EJBRoleRefPermission;
import jakarta.security.jacc.PolicyConfiguration;
import jakarta.security.jacc.PolicyConfigurationFactory;
import jakarta.security.jacc.PolicyContextException;
import java.lang.reflect.Method;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.openejb.assembler.classic.MethodInfoUtil.resolveAttributes;

/**
 * @version $Rev$ $Date$
 */
public class JaccPermissionsBuilder {

    static {
        JavaSecurityManagers.setSystemProperty("org.apache.security.jacc.EJBMethodPermission.methodInterfaces", "BusinessLocalHome,BusinessRemoteHome,BusinessRemote,BusinessLocal");
    }

    public void install(final PolicyContext policyContext) throws OpenEJBException {
        if (SystemInstance.get().hasProperty("openejb.geronimo")) {
            return;
        }

        final String contextID = policyContext.getContextID();
        try {
            final PolicyConfigurationFactory factory = PolicyConfigurationFactory.getPolicyConfigurationFactory();

            // final boolean needsCommit = factory.inService(contextID);
            final PolicyConfiguration policy = factory.getPolicyConfiguration(contextID, false);

            policy.addToExcludedPolicy(policyContext.getExcludedPermissions());

            policy.addToUncheckedPolicy(policyContext.getUncheckedPermissions());

            for (final Map.Entry<String, PermissionCollection> entry : policyContext.getRolePermissions().entrySet()) {
                policy.addToRole(entry.getKey(), entry.getValue());
            }

            // not sure if this is required or not
            // if (needsCommit) {
                policy.commit();
            // }
        } catch (final ClassNotFoundException e) {
            throw new OpenEJBException("PolicyConfigurationFactory class not found", e);

        } catch (final PolicyContextException e) {
            throw new OpenEJBException("JACC PolicyConfiguration failed: ContextId=" + contextID, e);
        }
    }

    private static final Logger log = Logger.getInstance(LogCategory.OPENEJB_STARTUP.createChild("attributes"), JaccPermissionsBuilder.class);

    public PolicyContext build(final EjbJarInfo ejbJar, final HashMap<String, BeanContext> deployments) throws OpenEJBException {

        final List<MethodPermissionInfo> normalized = new ArrayList<>();

        List<MethodPermissionInfo> perms = ejbJar.methodPermissions;

        for (final MethodInfo info : ejbJar.excludeList) {
            final MethodPermissionInfo perm = new MethodPermissionInfo();
            perm.excluded = true;
            perm.methods.add(info);
            perms.add(perm);
        }

        perms = MethodInfoUtil.normalizeMethodPermissionInfos(perms);

        for (final BeanContext beanContext : deployments.values()) {
            final Map<Method, MethodAttributeInfo> attributes = resolveAttributes(perms, beanContext);

            if (log.isDebugEnabled()) {
                for (final Map.Entry<Method, MethodAttributeInfo> entry : attributes.entrySet()) {
                    final Method method = entry.getKey();
                    final MethodPermissionInfo value = (MethodPermissionInfo) entry.getValue();
                    log.debug("Security Attribute: " + method + " -- " + MethodInfoUtil.toString(value));
                }
            }

            for (final Map.Entry<Method, MethodAttributeInfo> entry : attributes.entrySet()) {
                final Method method = entry.getKey();

                final MethodPermissionInfo a = (MethodPermissionInfo) entry.getValue();
                final MethodPermissionInfo b = new MethodPermissionInfo();
                b.excluded = a.excluded;
                b.unchecked = a.unchecked;
                b.roleNames.addAll(a.roleNames);

                final MethodInfo am = a.methods.get(0);
                final MethodInfo bm = new MethodInfo();

                bm.ejbName = beanContext.getEjbName();
                bm.ejbDeploymentId = String.valueOf(beanContext.getDeploymentID());
                bm.methodIntf = am.methodIntf;

                bm.className = method.getDeclaringClass().getName();
                bm.methodName = method.getName();
                bm.methodParams = new ArrayList<>();
                for (final Class<?> type : method.getParameterTypes()) {
                    bm.methodParams.add(type.getName());
                }
                b.methods.add(bm);

                normalized.add(b);
            }
        }

        ejbJar.methodPermissions.clear();
        ejbJar.methodPermissions.addAll(normalized);
        ejbJar.excludeList.clear();

        final PolicyContext policyContext = new PolicyContext(ejbJar.moduleUri.toString());

        for (final EnterpriseBeanInfo enterpriseBean : ejbJar.enterpriseBeans) {
            final BeanContext beanContext = deployments.get(enterpriseBean.ejbDeploymentId);

            final PermissionCollection permissions = DelegatePermissionCollection.getPermissionCollection();

            final String ejbName = enterpriseBean.ejbName;

            for (final InterfaceType type : InterfaceType.values()) {
                if (type == InterfaceType.UNKNOWN) {
                    continue;
                }

                for (final Class interfce : beanContext.getInterfaces(type)) {
                    addPossibleEjbMethodPermissions(permissions, ejbName, type.getSpecName(), interfce);
                }
            }
            addPossibleEjbMethodPermissions(permissions, ejbName, null, beanContext.getBeanClass());

            addDeclaredEjbPermissions(ejbJar, enterpriseBean, null, permissions, policyContext);

        }

        return policyContext;
    }

    private void addDeclaredEjbPermissions(final EjbJarInfo ejbJar,
                                           final EnterpriseBeanInfo beanInfo,
                                           final String defaultRole,
                                           PermissionCollection notAssigned,
                                           final PolicyContext policyContext) throws OpenEJBException {

        final PermissionCollection uncheckedPermissions = policyContext.getUncheckedPermissions();
        final PermissionCollection excludedPermissions = policyContext.getExcludedPermissions();
        final Map<String, PermissionCollection> rolePermissions = policyContext.getRolePermissions();

        final String ejbName = beanInfo.ejbName;

        //this can occur in an ear when one ejb module has security and one doesn't.  In this case we still need
        //to make the non-secure one completely unchecked.
        /**
         * JACC v1.0 section 3.1.5.1
         */
        for (final MethodPermissionInfo methodPermission : ejbJar.methodPermissions) {
            final List<String> roleNames = methodPermission.roleNames;
            final boolean unchecked = methodPermission.unchecked;
            final boolean excluded = methodPermission.excluded;

            for (final MethodInfo method : methodPermission.methods) {

                if (!ejbName.equals(method.ejbName)) {
                    continue;
                }

                // method name
                String methodName = method.methodName;
                if ("*".equals(methodName)) {
                    // jacc uses null instead of *
                    methodName = null;
                }

                // method interface
                final String methodIntf = method.methodIntf;

                // method parameters
                final String[] methodParams;
                if (method.methodParams != null) {
                    final List<String> paramList = method.methodParams;
                    methodParams = paramList.toArray(new String[paramList.size()]);
                } else {
                    methodParams = null;
                }

                // create the permission object
                final EJBMethodPermission permission = new EJBMethodPermission(ejbName, methodName, methodIntf, methodParams);
                notAssigned = cullPermissions(notAssigned, permission);

                // if this is unchecked, mark it as unchecked; otherwise assign the roles
                if (unchecked) {
                    uncheckedPermissions.add(permission);
                } else if (excluded) {
                    /**
                     * JACC v1.0 section 3.1.5.2
                     */
                    excludedPermissions.add(permission);
                } else {
                    for (final String roleName : roleNames) {
                        PermissionCollection permissions = rolePermissions.get(roleName);
                        if (permissions == null) {
                            permissions = DelegatePermissionCollection.getPermissionCollection();
                            rolePermissions.put(roleName, permissions);
                        }
                        permissions.add(permission);
                    }
                }
            }

        }

        /**
         * JACC v1.0 section 3.1.5.3
         */
        for (final SecurityRoleReferenceInfo securityRoleRef : beanInfo.securityRoleReferences) {

            if (securityRoleRef.roleLink == null) {
                throw new OpenEJBException("Missing role-link");
            }

            final String roleLink = securityRoleRef.roleLink;

            PermissionCollection roleLinks = rolePermissions.get(roleLink);
            if (roleLinks == null) {
                roleLinks = DelegatePermissionCollection.getPermissionCollection();
                rolePermissions.put(roleLink, roleLinks);

            }
            roleLinks.add(new EJBRoleRefPermission(ejbName, securityRoleRef.roleName));
        }

        /**
         * EJB v2.1 section 21.3.2
         *
         * It is possible that some methods are not assigned to any security
         * roles nor contained in the <code>exclude-list</code> element. In
         * this case, it is the responsibility of the Deployer to assign method
         * permissions for all of the unspecified methods, either by assigning
         * them to security roles, or by marking them as <code>unchecked</code>.
         */
        PermissionCollection permissions;
        if (defaultRole == null) {
            permissions = uncheckedPermissions;
        } else {
            permissions = rolePermissions.get(defaultRole);
            if (permissions == null) {
                permissions = DelegatePermissionCollection.getPermissionCollection();
                rolePermissions.put(defaultRole, permissions);
            }
        }

        final Enumeration e = notAssigned.elements();
        while (e.hasMoreElements()) {
            final Permission p = (Permission) e.nextElement();
            permissions.add(p);
        }

    }

    /**
     * Generate all the possible permissions for a bean's interface.
     *
     * Method permissions are defined in the deployment descriptor as a binary
     * relation from the set of security roles to the set of methods of the
     * home, component, and/or web service endpoint interfaces of session and
     * entity beans, including all their superinterfaces (including the methods
     * of the <code>EJBHome</code> and <code>EJBObject</code> interfaces and/or
     * <code>EJBLocalHome</code> and <code>EJBLocalObject</code> interfaces).
     *
     * @param permissions     the permission set to be extended
     * @param ejbName         the name of the EJB
     * @param methodInterface the EJB method interface
     * @param clazz           clazz
     * @throws OpenEJBException in case a class could not be found
     */
    public void addPossibleEjbMethodPermissions(final PermissionCollection permissions,
                                                final String ejbName,
                                                final String methodInterface,
                                                final Class clazz) throws OpenEJBException {
        if (clazz == null) {
            return;
        }
        for (final Method method : clazz.getMethods()) {
            final String methodIface = "LocalBean".equals(methodInterface) || "LocalBeanHome".equals(methodInterface) ? null : methodInterface;
            permissions.add(new EJBMethodPermission(ejbName, methodIface, method));
        }
    }

    /**
     * Removes permissions from <code>toBeChecked</code> that are implied by
     * <code>permission</code>.
     *
     * @param toBeChecked the permissions that are to be checked and possibly culled
     * @param permission  the permission that is to be used for culling
     * @return the culled set of permissions that are not implied by <code>permission</code>
     */
    private PermissionCollection cullPermissions(final PermissionCollection toBeChecked, final Permission permission) {
        final PermissionCollection result = DelegatePermissionCollection.getPermissionCollection();

        for (final Enumeration e = toBeChecked.elements(); e.hasMoreElements(); ) {
            final Permission test = (Permission) e.nextElement();
            if (!permission.implies(test)) {
                result.add(test);
            }
        }

        return result;
    }
}
