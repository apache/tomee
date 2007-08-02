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

import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.DeploymentInfo;

import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.lang.reflect.Method;

/**
 * @version $Rev$ $Date$
 */
public class MethodTransactionBuilder {

    public void build(HashMap<String, DeploymentInfo> deployments, List<MethodTransactionInfo> methodTransactions) throws OpenEJBException {
        for (DeploymentInfo deploymentInfo : deployments.values()) {
            applyTransactionAttributes((CoreDeploymentInfo) deploymentInfo, methodTransactions);
        }
    }

    public static void applyTransactionAttributes(CoreDeploymentInfo deploymentInfo, List<MethodTransactionInfo> mtis) throws OpenEJBException {
        /*TODO: Add better exception handling.  This method doesn't throws any exceptions!!
        there is a lot of complex code here, I'm sure something could go wrong the user
        might want to know about.
        */

        mtis = normalize(mtis);

        for (MethodTransactionInfo transInfo : mtis) {
            for (MethodInfo methodInfo : transInfo.methods) {

                if (methodInfo.ejbDeploymentId == null || methodInfo.ejbDeploymentId.equals(deploymentInfo.getDeploymentID())) {
                    if (!deploymentInfo.isBeanManagedTransaction()) {

                        List<Method> methods = new ArrayList<Method>();

                        if (methodInfo.methodIntf == null) {
                            AssemblerTool.resolveMethods(methods, deploymentInfo.getBeanClass(), methodInfo);
                            if (deploymentInfo.getRemoteInterface() != null) {
                                AssemblerTool.resolveMethods(methods, deploymentInfo.getRemoteInterface(), methodInfo);
                            }
                            if (deploymentInfo.getHomeInterface() != null) {
                                AssemblerTool.resolveMethods(methods, deploymentInfo.getHomeInterface(), methodInfo);
                            }
                            if (deploymentInfo.getLocalInterface() != null) {
                                AssemblerTool.resolveMethods(methods, deploymentInfo.getLocalInterface(), methodInfo);
                            }
                            if (deploymentInfo.getLocalHomeInterface() != null) {
                                AssemblerTool.resolveMethods(methods, deploymentInfo.getLocalHomeInterface(), methodInfo);
                            }
                            if(deploymentInfo.getMdbInterface() != null) {
                                AssemblerTool.resolveMethods(methods, deploymentInfo.getMdbInterface(), methodInfo);
                            }
                            if(deploymentInfo.getServiceEndpointInterface() != null) {
                                AssemblerTool.resolveMethods(methods, deploymentInfo.getServiceEndpointInterface(), methodInfo);
                            }
                            for (Class intf : deploymentInfo.getBusinessRemoteInterfaces()) {
                                AssemblerTool.resolveMethods(methods, intf, methodInfo);
                            }
                            for (Class intf : deploymentInfo.getBusinessLocalInterfaces()) {
                                AssemblerTool.resolveMethods(methods, intf, methodInfo);
                            }
                        } else if (methodInfo.methodIntf.equals("Home")) {
                            AssemblerTool.resolveMethods(methods, deploymentInfo.getHomeInterface(), methodInfo);
                        } else if (methodInfo.methodIntf.equals("Remote")) {
                            AssemblerTool.resolveMethods(methods, deploymentInfo.getRemoteInterface(), methodInfo);
                            for (Class intf : deploymentInfo.getBusinessRemoteInterfaces()) {
                                AssemblerTool.resolveMethods(methods, intf, methodInfo);
                            }
                        } else if (methodInfo.methodIntf.equals("LocalHome")) {
                            AssemblerTool.resolveMethods(methods, deploymentInfo.getLocalHomeInterface(), methodInfo);
                        } else if (methodInfo.methodIntf.equals("Local")) {
                            AssemblerTool.resolveMethods(methods, deploymentInfo.getLocalInterface(), methodInfo);
                            for (Class intf : deploymentInfo.getBusinessRemoteInterfaces()) {
                                AssemblerTool.resolveMethods(methods, intf, methodInfo);
                            }
                        } else if (methodInfo.methodIntf.equals("ServiceEndpoint")) {
                            AssemblerTool.resolveMethods(methods, deploymentInfo.getServiceEndpointInterface(), methodInfo);
                        }

                        for (Method method : methods) {
                            if ((method.getDeclaringClass() == javax.ejb.EJBObject.class ||
                                    method.getDeclaringClass() == javax.ejb.EJBHome.class) &&
                                    !method.getName().equals("remove")) {
                                continue;
                            }
                            deploymentInfo.setMethodTransactionAttribute(method, transInfo.transAttribute);
                        }
                    }
                }
            }
        }

    }

    public static enum Level {
        PACKAGE, CLASS, OVERLOADED_METHOD, EXACT_METHOD
    }

    /**
     * This method splits the MethodTransactionInfo objects so that there is
     * exactly one MethodInfo per MethodTransactionInfo.  A single MethodTransactionInfo
     * with three MethodInfos would be expanded into three MethodTransactionInfo with
     * one MethodInfo each.
     *
     * The MethodTransactionInfo list is then sorted from least to most specific.
     *
     * @param infos
     * @return a normalized list of new MethodTransactionInfo objects
     */
    public static List<MethodTransactionInfo> normalize(List<MethodTransactionInfo> infos){
        List<MethodTransactionInfo> normalized = new ArrayList<MethodTransactionInfo>();
        for (MethodTransactionInfo oldInfo : infos) {
            for (MethodInfo methodInfo : oldInfo.methods) {
                MethodTransactionInfo newInfo = new MethodTransactionInfo();
                newInfo.description = oldInfo.description;
                newInfo.methods.add(methodInfo);
                newInfo.transAttribute = oldInfo.transAttribute;

                normalized.add(newInfo);
            }
        }

        Collections.sort(normalized, new MethodTransactionComparator());

        return normalized;
    }

    public static class MethodTransactionComparator implements Comparator<MethodTransactionInfo> {
        public int compare(MethodTransactionInfo a, MethodTransactionInfo b) {
            Level levelA = level(a);
            Level levelB = level(b);

            return levelA.ordinal() - levelB.ordinal();
        }
    }

    private static Level level(MethodTransactionInfo info) {
        MethodInfo methodInfo = info.methods.get(0);
        if (methodInfo.ejbName.equals("*")) return Level.PACKAGE;
        if (methodInfo.methodName == null || methodInfo.methodName.equals("*")) return Level.CLASS;
        if (methodInfo.methodParams == null) return Level.OVERLOADED_METHOD;
        return Level.EXACT_METHOD;
    }

}
