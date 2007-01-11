/**
 *
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
package org.apache.openejb.alt.config;

import org.apache.openejb.assembler.classic.OpenEjbConfiguration;
import org.apache.openejb.assembler.classic.EnterpriseBeanInfo;
import org.apache.openejb.assembler.classic.MethodInfo;
import org.apache.openejb.assembler.classic.MethodTransactionInfo;
import org.apache.openejb.assembler.classic.MethodPermissionInfo;
import org.apache.openejb.assembler.classic.EnvEntryInfo;
import org.apache.openejb.assembler.classic.EjbReferenceInfo;
import org.apache.openejb.assembler.classic.ResourceReferenceInfo;
import org.apache.openejb.assembler.classic.JndiEncInfo;
import org.apache.openejb.assembler.classic.SecurityRoleInfo;
import org.apache.openejb.assembler.classic.ContainerInfo;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.EjbJarInfo;

import java.util.ListIterator;

/**
 * @version $Revision$ $Date$
 */
public class ConfigurationPrinter {

    public static String[] tabs = {"", " ", "    ", "      ", "        ", "          "};

    public static void printConf(OpenEjbConfiguration conf) {
        out(0, "CONFIGURATION");

        out(1, conf.containerSystem.containers.size());
        for (ContainerInfo container : conf.containerSystem.containers) {
            out(1, "className    ", container.className);
            out(1, "codebase     ", container.codebase);
            out(1, "containerName", container.id);
            out(1, "containerType", container.containerType);
            out(1, "description  ", container.description);
            out(1, "displayName  ", container.displayName);
            out(1, "properties   ");
            container.properties.list(System.out);
        }

        for (AppInfo app : conf.containerSystem.applications) {
            for (EjbJarInfo ejbJar : app.ejbJars) {
                out(1, "ejbeans      ", ejbJar.enterpriseBeans.size());
                for (EnterpriseBeanInfo bean : ejbJar.enterpriseBeans) {
                    out(2, "codebase       ", bean.codebase);
                    out(2, "description    ", bean.description);
                    out(2, "displayName    ", bean.displayName);
                    out(2, "ejbClass       ", bean.ejbClass);
                    out(2, "ejbDeploymentId", bean.ejbDeploymentId);
                    out(2, "ejbName        ", bean.ejbName);
                    out(2, "home           ", bean.home);
                    out(2, "largeIcon      ", bean.largeIcon);
                    out(2, "remote         ", bean.remote);
                    out(2, "smallIcon      ", bean.smallIcon);
                    out(2, "transactionType", bean.transactionType);
                    out(2, "type           ", bean.type);

                    JndiEncInfo jndiEnc = bean.jndiEnc;
                    out(2, "jndiEnc        ", jndiEnc);
                    out(2, "envEntries     ", jndiEnc.envEntries.size());
                    for (ListIterator<EnvEntryInfo> iterator = jndiEnc.envEntries.listIterator(); iterator.hasNext();) {
                        EnvEntryInfo envEntry = iterator.next();
                        out(3, "--[" + iterator.previousIndex() + "]----------------------");
                        out(3, "name  ", envEntry.name);
                        out(3, "type  ", envEntry.type);
                        out(3, "value ", envEntry.value);
                    }
                    out(2, "ejbReferences  ", jndiEnc.ejbReferences.size());
                    for (ListIterator<EjbReferenceInfo> iterator = jndiEnc.ejbReferences.listIterator(); iterator.hasNext();) {
                        EjbReferenceInfo ejbReference = iterator.next();
                        out(3, "--[" + iterator.previousIndex() + "]----------------------");
                        out(3, "homeType        ", ejbReference.homeType);
                        out(3, "referenceName   ", ejbReference.referenceName);
                        out(3, "location        ", ejbReference.location);
                        out(3, "ejbDeploymentId ", ejbReference.location.ejbDeploymentId);
                        out(3, "jndiContextId   ", ejbReference.location.jndiContextId);
                        out(3, "remote          ", ejbReference.location.remote);
                        out(3, "remoteRefName   ", ejbReference.location.remoteRefName);
                    }
                    out(2, "resourceRefs   ", jndiEnc.resourceRefs.size());
                    for (ListIterator<ResourceReferenceInfo> iterator = jndiEnc.resourceRefs.listIterator(); iterator.hasNext();) {
                        ResourceReferenceInfo resourceRef = iterator.next();
                        out(3, "--[" + iterator.previousIndex() + "]----------------------");
                        out(3, "referenceAuth   ", resourceRef.referenceAuth);
                        out(3, "referenceName   ", resourceRef.referenceName);
                        out(3, "referenceType   ", resourceRef.referenceType);
                        if (resourceRef.location != null) {
                            out(3, "location        ", resourceRef.location);
                            out(3, "jndiContextId   ", resourceRef.location.jndiContextId);
                            out(3, "remote          ", resourceRef.location.remote);
                            out(3, "remoteRefName   ", resourceRef.location.remoteRefName);
                        }
                    }
                }

                if (!ejbJar.securityRoles.isEmpty()) {
                    out(0, "--Security Roles------------");
                    for (ListIterator<SecurityRoleInfo> iterator = ejbJar.securityRoles.listIterator(); iterator.hasNext();) {
                        SecurityRoleInfo securityRole =  iterator.next();
                        out(1, "--[" + iterator.previousIndex() + "]----------------------");
                        out(1, "            ", securityRole);
                        out(1, "description ", securityRole.description);
                        out(1, "roleName    ", securityRole.roleName);
                    }
                }

                if (!ejbJar.methodPermissions.isEmpty()) {
                    out(0, "--Method Permissions--------");
                    for (ListIterator<MethodPermissionInfo> iterator = ejbJar.methodPermissions.listIterator(); iterator.hasNext();) {
                        MethodPermissionInfo methodPermission =  iterator.next();

                        out(1, "--[" + iterator.previousIndex() + "]----------------------");
                        out(1, "            ", methodPermission);
                        out(1, "description ", methodPermission.description);
                        out(1, "roleNames   ", methodPermission.roleNames);
                        if (methodPermission.roleNames != null) {
                            for (ListIterator<String> roleNameIterator = methodPermission.roleNames.listIterator(); roleNameIterator.hasNext();) {
                                String roleName = roleNameIterator.next();
                                out(1, "roleName[" + roleNameIterator.previousIndex() + "]   ", roleName);
                            }
                        }
                        out(1, "methods     ", methodPermission.methods);
                        if (methodPermission.methods != null) {
                            for (MethodInfo methodInfo : methodPermission.methods) {
                                out(2, "description    ", methodInfo.description);
                                out(2, "ejbDeploymentId", methodInfo.ejbDeploymentId);
                                out(2, "methodIntf     ", methodInfo.methodIntf);
                                out(2, "methodName     ", methodInfo.methodName);
                                if (methodInfo.methodParams != null) {
                                    out(2, "methodParams   ", methodInfo.methodParams);
                                    for (ListIterator<String> paramIterator = methodInfo.methodParams.listIterator(); paramIterator.hasNext();) {
                                        String methodParam = paramIterator.next();
                                        out(3, "param[" + paramIterator.previousIndex() + "]", methodParam);
                                    }
                                }
                            }
                        }
                    }
                }

                if (!ejbJar.methodTransactions.isEmpty()) {
                    out(0, "--Method Transactions-------");

                    for (ListIterator<MethodTransactionInfo> iterator = ejbJar.methodTransactions.listIterator(); iterator.hasNext();) {
                        MethodTransactionInfo methodTransaction = iterator.next();

                        out(1, "--[" + iterator.previousIndex() + "]----------------------");
                        out(1, "               ", methodTransaction);
                        out(1, "description    ", methodTransaction.description);
                        out(1, "transAttribute ", methodTransaction.transAttribute);
                        out(1, "methods        ", methodTransaction.methods);
                        for (MethodInfo methodInfo : methodTransaction.methods) {
                            out(2, "description    ", methodInfo.description);
                            out(2, "ejbDeploymentId", methodInfo.ejbDeploymentId);
                            out(2, "methodIntf     ", methodInfo.methodIntf);
                            out(2, "methodName     ", methodInfo.methodName);
                            if (methodInfo.methodParams != null) {
                                out(2, "methodParams   ", methodInfo.methodParams);
                                for (ListIterator<String> paramIterator = methodInfo.methodParams.listIterator(); paramIterator.hasNext();) {
                                    String methodParam = paramIterator.next();
                                    out(3, "param[" + paramIterator.previousIndex() + "]", methodParam);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static void out(int t, String m) {
        System.out.println(tabs[t] + m);
    }

    private static void out(int t, String m, String n) {
        System.out.println(tabs[t] + m + " = " + n);
    }

    private static void out(int t, String m, boolean n) {
        System.out.println(tabs[t] + m + " = " + n);
    }

    private static void out(int t, String m, int n) {
        System.out.println(tabs[t] + m + " = " + n);
    }

    private static void out(int t, String m, Object n) {
        System.out.println(tabs[t] + m + " = " + n);
    }

    private static void out(int t, int m) {
        System.out.println(ConfigurationPrinter.tabs[t] + m);
    }
}
