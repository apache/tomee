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

/**
 * @version $Revision$ $Date$
 */
public class ConfigurationPrinter {

    public static String[] tabs = {"", " ", "    ", "      ", "        ", "          "};

    public static void printConf(OpenEjbConfiguration conf) {
        out(0, "CONFIGURATION");

        out(1, conf.containerSystem.containers.length);
        for (int i = 0; i < conf.containerSystem.containers.length; i++) {
            out(1, "className    ", conf.containerSystem.containers[i].className);
            out(1, "codebase     ", conf.containerSystem.containers[i].codebase);
            out(1, "containerName", conf.containerSystem.containers[i].containerName);
            out(1, "containerType", conf.containerSystem.containers[i].containerType);
            out(1, "description  ", conf.containerSystem.containers[i].description);
            out(1, "displayName  ", conf.containerSystem.containers[i].displayName);
            out(1, "properties   ");
            conf.containerSystem.containers[i].properties.list(System.out);
            out(1, "ejbeans      ", conf.containerSystem.containers[i].ejbeans.length);
            for (int j = 0; j < conf.containerSystem.containers[i].ejbeans.length; j++) {
                EnterpriseBeanInfo bean = conf.containerSystem.containers[i].ejbeans[j];
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
                out(2, "jndiEnc        ", bean.jndiEnc);
                out(2, "envEntries     ", bean.jndiEnc.envEntries.length);
                for (int n = 0; n < bean.jndiEnc.envEntries.length; n++) {
                    out(3, "--[" + n + "]----------------------");
                    out(3, "name  ", bean.jndiEnc.envEntries[n].name);
                    out(3, "type  ", bean.jndiEnc.envEntries[n].type);
                    out(3, "value ", bean.jndiEnc.envEntries[n].value);
                }
                out(2, "ejbReferences  ", bean.jndiEnc.ejbReferences.length);
                for (int n = 0; n < bean.jndiEnc.ejbReferences.length; n++) {
                    out(3, "--[" + n + "]----------------------");
                    out(3, "homeType        ", bean.jndiEnc.ejbReferences[n].homeType);
                    out(3, "referenceName   ", bean.jndiEnc.ejbReferences[n].referenceName);
                    out(3, "location        ", bean.jndiEnc.ejbReferences[n].location);
                    out(3, "ejbDeploymentId ", bean.jndiEnc.ejbReferences[n].location.ejbDeploymentId);
                    out(3, "jndiContextId   ", bean.jndiEnc.ejbReferences[n].location.jndiContextId);
                    out(3, "remote          ", bean.jndiEnc.ejbReferences[n].location.remote);
                    out(3, "remoteRefName   ", bean.jndiEnc.ejbReferences[n].location.remoteRefName);
                }
                out(2, "resourceRefs   ", bean.jndiEnc.resourceRefs.length);
                for (int n = 0; n < bean.jndiEnc.resourceRefs.length; n++) {
                    out(3, "--[" + n + "]----------------------");
                    out(3, "referenceAuth   ", bean.jndiEnc.resourceRefs[n].referenceAuth);
                    out(3, "referenceName   ", bean.jndiEnc.resourceRefs[n].referenceName);
                    out(3, "referenceType   ", bean.jndiEnc.resourceRefs[n].referenceType);
                    if (bean.jndiEnc.resourceRefs[n].location != null) {
                        out(3, "location        ", bean.jndiEnc.resourceRefs[n].location);
                        out(3, "jndiContextId   ", bean.jndiEnc.resourceRefs[n].location.jndiContextId);
                        out(3, "remote          ", bean.jndiEnc.resourceRefs[n].location.remote);
                        out(3, "remoteRefName   ", bean.jndiEnc.resourceRefs[n].location.remoteRefName);
                    }
                }
            }
        }

        if (conf.containerSystem.securityRoles != null) {
            out(0, "--Security Roles------------");
            for (int i = 0; i < conf.containerSystem.securityRoles.length; i++) {
                out(1, "--[" + i + "]----------------------");
                out(1, "            ", conf.containerSystem.securityRoles[i]);
                out(1, "description ", conf.containerSystem.securityRoles[i].description);
                out(1, "roleName    ", conf.containerSystem.securityRoles[i].roleName);
            }
        }

        if (conf.containerSystem.methodPermissions != null) {
            out(0, "--Method Permissions--------");
            for (int i = 0; i < conf.containerSystem.methodPermissions.length; i++) {
                out(1, "--[" + i + "]----------------------");
                out(1, "            ", conf.containerSystem.methodPermissions[i]);
                out(1, "description ", conf.containerSystem.methodPermissions[i].description);
                out(1, "roleNames   ", conf.containerSystem.methodPermissions[i].roleNames);
                if (conf.containerSystem.methodPermissions[i].roleNames != null) {
                    String[] roleNames = conf.containerSystem.methodPermissions[i].roleNames;
                    for (int r = 0; r < roleNames.length; r++) {
                        out(1, "roleName[" + r + "]   ", roleNames[r]);
                    }
                }
                out(1, "methods     ", conf.containerSystem.methodPermissions[i].methods);
                if (conf.containerSystem.methodPermissions[i].methods != null) {
                    MethodInfo[] mthds = conf.containerSystem.methodPermissions[i].methods;
                    for (int j = 0; j < mthds.length; j++) {
                        out(2, "description    ", mthds[j].description);
                        out(2, "ejbDeploymentId", mthds[j].ejbDeploymentId);
                        out(2, "methodIntf     ", mthds[j].methodIntf);
                        out(2, "methodName     ", mthds[j].methodName);
                        out(2, "methodParams   ", mthds[j].methodParams);
                        if (mthds[j].methodParams != null) {
                            for (int n = 0; n < mthds[j].methodParams.length; n++) {
                                out(3, "param[" + n + "]", mthds[j].methodParams[n]);
                            }
                        }

                    }
                }
            }
        }

        if (conf.containerSystem.methodTransactions != null) {
            out(0, "--Method Transactions-------");
            for (int i = 0; i < conf.containerSystem.methodTransactions.length; i++) {

                out(1, "--[" + i + "]----------------------");
                out(1, "               ", conf.containerSystem.methodTransactions[i]);
                out(1, "description    ", conf.containerSystem.methodTransactions[i].description);
                out(1, "transAttribute ", conf.containerSystem.methodTransactions[i].transAttribute);
                out(1, "methods        ", conf.containerSystem.methodTransactions[i].methods);
                if (conf.containerSystem.methodTransactions[i].methods != null) {
                    MethodInfo[] mthds = conf.containerSystem.methodTransactions[i].methods;
                    for (int j = 0; j < mthds.length; j++) {
                        out(2, "description    ", mthds[j].description);
                        out(2, "ejbDeploymentId", mthds[j].ejbDeploymentId);
                        out(2, "methodIntf     ", mthds[j].methodIntf);
                        out(2, "methodName     ", mthds[j].methodName);
                        out(2, "methodParams   ", mthds[j].methodParams);
                        if (mthds[j].methodParams != null) {
                            for (int n = 0; n < mthds[j].methodParams.length; n++) {
                                out(3, "param[" + n + "]", mthds[j].methodParams[n]);
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
