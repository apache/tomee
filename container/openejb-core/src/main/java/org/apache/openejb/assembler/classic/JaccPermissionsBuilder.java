/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.assembler.classic;

import org.apache.openejb.InterfaceType;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.assembler.classic.PolicyContext;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.EnterpriseBeanInfo;
import org.apache.openejb.assembler.classic.MethodPermissionInfo;
import org.apache.openejb.assembler.classic.MethodInfo;
import org.apache.openejb.assembler.classic.SecurityRoleReferenceInfo;

import javax.security.jacc.EJBMethodPermission;
import javax.security.jacc.EJBRoleRefPermission;
import javax.security.jacc.PolicyConfigurationFactory;
import javax.security.jacc.PolicyConfiguration;
import javax.security.auth.Subject;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @version $Rev$ $Date$
 */
public class JaccPermissionsBuilder {

    public static class RunAsBuilder {
        
    }
    public void install(PolicyContext policyContext) throws Exception {
        PolicyConfigurationFactory factory = PolicyConfigurationFactory.getPolicyConfigurationFactory();

        PolicyConfiguration policy = factory.getPolicyConfiguration(policyContext.getContextID(), false);

        policy.addToExcludedPolicy(policyContext.getExcludedPermissions());

        policy.addToUncheckedPolicy(policyContext.getUncheckedPermissions());

        for (Map.Entry<String, PermissionCollection> entry : policyContext.getRolePermissions().entrySet()) {
            policy.addToRole(entry.getKey(), entry.getValue());
        }

        policy.commit();
    }


    public PolicyContext build(EjbJarInfo ejbJar, HashMap<String, DeploymentInfo> deployments) throws OpenEJBException {

        PolicyContext policyContext = new PolicyContext(ejbJar.moduleId);

        for (EnterpriseBeanInfo enterpriseBean : ejbJar.enterpriseBeans) {
            CoreDeploymentInfo deployment = (CoreDeploymentInfo) deployments.get(enterpriseBean.ejbDeploymentId);

            Permissions permissions = new Permissions();

            String ejbName = enterpriseBean.ejbName;
            for (InterfaceType type : InterfaceType.values()) {
                addPossibleEjbMethodPermissions(permissions, ejbName, type.getName(), deployment.getInterface(type));
            }

            addDeclaredEjbPermissions(ejbJar, enterpriseBean, null, permissions, policyContext);

        }

        return policyContext;
    }

    private void addDeclaredEjbPermissions(EjbJarInfo ejbJar, EnterpriseBeanInfo beanInfo, String defaultRole, Permissions notAssigned, PolicyContext policyContext) throws OpenEJBException {

        PermissionCollection uncheckedPermissions = policyContext.getUncheckedPermissions();
        PermissionCollection excludedPermissions = policyContext.getExcludedPermissions();
        Map<String,PermissionCollection> rolePermissions = policyContext.getRolePermissions();

        String ejbName = beanInfo.ejbName;

        //this can occur in an ear when one ejb module has security and one doesn't.  In this case we still need
        //to make the non-secure one completely unchecked.
        /**
         * JACC v1.0 section 3.1.5.1
         */
        for (MethodPermissionInfo methodPermission : ejbJar.methodPermissions) {
            List<String> roleNames = methodPermission.roleNames;
            boolean unchecked = methodPermission.unchecked;

            for (MethodInfo method : methodPermission.methods) {

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
                String methodIntf = method.methodIntf;

                // method parameters
                String[] methodParams;
                if (method.methodParams != null) {
                    List<String> paramList = method.methodParams;
                    methodParams = paramList.toArray(new String[paramList.size()]);
                } else {
                    methodParams = null;
                }

                // create the permission object
                EJBMethodPermission permission = new EJBMethodPermission(ejbName, methodName, methodIntf, methodParams);
                notAssigned = cullPermissions(notAssigned, permission);

                // if this is unchecked, mark it as unchecked; otherwise assign the roles
                if (unchecked) {
                    uncheckedPermissions.add(permission);
                } else {
                    for (String roleName : roleNames) {
                        Permissions permissions = (Permissions) rolePermissions.get(roleName);
                        if (permissions == null) {
                            permissions = new Permissions();
                            rolePermissions.put(roleName, permissions);
                        }
                        permissions.add(permission);
                    }
                }
            }

        }

        /**
         * JACC v1.0 section 3.1.5.2
         */
        for (MethodInfo method : ejbJar.excludeList) {
            if (!ejbName.equals(method.ejbName)) {
                continue;
            }

            // method name
            String methodName = method.methodName;
            // method interface
            String methodIntf = method.methodIntf;

            // method parameters
            String[] methodParams;
            if (method.methodParams != null) {
                List<String> paramList = method.methodParams;
                methodParams = paramList.toArray(new String[paramList.size()]);
            } else {
                methodParams = null;
            }

            // create the permission object
            EJBMethodPermission permission = new EJBMethodPermission(ejbName, methodName, methodIntf, methodParams);

            excludedPermissions.add(permission);
            notAssigned = cullPermissions(notAssigned, permission);
        }

        /**
         * JACC v1.0 section 3.1.5.3
         */
        for (SecurityRoleReferenceInfo securityRoleRef : beanInfo.securityRoleReferences) {

            if (securityRoleRef.roleLink == null) {
                throw new OpenEJBException("Missing role-link");
            }

            String roleLink = securityRoleRef.roleLink;

            PermissionCollection roleLinks = (PermissionCollection) rolePermissions.get(roleLink);
            if (roleLinks == null) {
                roleLinks = new Permissions();
                rolePermissions.put(roleLink, roleLinks);

            }
            roleLinks.add(new EJBRoleRefPermission(ejbName, securityRoleRef.roleName));
        }

        /**
         * EJB v2.1 section 21.3.2
         * <p/>
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
            permissions = (PermissionCollection) rolePermissions.get(defaultRole);
            if (permissions == null) {
                permissions = new Permissions();
                rolePermissions.put(defaultRole, permissions);
            }
        }

        Enumeration e = notAssigned.elements();
        while (e.hasMoreElements()) {
            Permission p = (Permission) e.nextElement();
            permissions.add(p);
        }

    }

    /**
     * Generate all the possible permissions for a bean's interface.
     * <p/>
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
     * @throws org.apache.openejb.OpenEJBException
     *          in case a class could not be found
     */
    public void addPossibleEjbMethodPermissions(Permissions permissions, String ejbName, String methodInterface, Class clazz) throws OpenEJBException {
        if (clazz == null) return;
        for (java.lang.reflect.Method method : clazz.getMethods()) {
            permissions.add(new EJBMethodPermission(ejbName, methodInterface, method));
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
    private Permissions cullPermissions(Permissions toBeChecked, Permission permission) {
        Permissions result = new Permissions();

        for (Enumeration e = toBeChecked.elements(); e.hasMoreElements();) {
            Permission test = (Permission) e.nextElement();
            if (!permission.implies(test)) {
                result.add(test);
            }
        }

        return result;
    }
}
