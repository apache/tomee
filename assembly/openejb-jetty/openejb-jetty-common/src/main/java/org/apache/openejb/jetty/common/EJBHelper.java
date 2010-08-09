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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.jetty.common;

import org.apache.openejb.assembler.classic.*;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.jee.EjbRef;
import org.apache.openejb.jee.EjbReference;
import org.apache.openejb.jee.EjbLocalRef;

import java.util.Collection;
import java.util.List;

public class EJBHelper {
    public EnterpriseBeanInfo getEJBInfo(EjbReference ejbReference) {
        Collection<AppInfo> deployedApplications = getDeployedApps();
        for (AppInfo app : deployedApplications) {
            List<EjbJarInfo> ejbJars = app.ejbJars;
            for (EjbJarInfo ejbJar : ejbJars) {
                List<EnterpriseBeanInfo> enterpriseBeans = ejbJar.enterpriseBeans;
                for (EnterpriseBeanInfo enterpriseBean : enterpriseBeans) {
                    if (enterpriseBean.ejbDeploymentId.equals(ejbReference.getEjbLink())) {
                        return enterpriseBean;
                    }

                    if (ejbReference instanceof EjbRef) {
                        EjbRef ejbRef = (EjbRef) ejbReference;

                        if (enterpriseBean.remote != null && enterpriseBean.remote.equals(ejbRef.getRemote())) {
                            return enterpriseBean;
                        }

                        if (enterpriseBean.businessRemote != null && enterpriseBean.businessRemote.contains(ejbRef.getRemote())) {
                            return enterpriseBean;
                        }
                    }

                    if (ejbReference instanceof EjbLocalRef) {
                        EjbLocalRef ejbLocalRef = (EjbLocalRef) ejbReference;
                        if (enterpriseBean.local != null && enterpriseBean.local.equals(ejbLocalRef.getLocal())) {
                            return enterpriseBean;
                        }

                        if (enterpriseBean.businessLocal != null && enterpriseBean.businessLocal.contains(ejbLocalRef.getLocal())) {
                            return enterpriseBean;
                        }
                    }
                }
            }
        }

        return null;
    }

    public EnterpriseBeanInfo getBeanInfo(Class<?> fieldType) {
        Collection<AppInfo> deployedApplications = getDeployedApps();

        for (AppInfo deployedApplication : deployedApplications) {
            List<EjbJarInfo> ejbJars = deployedApplication.ejbJars;

            for (EjbJarInfo ejbJar : ejbJars) {
                List<EnterpriseBeanInfo> enterpriseBeans = ejbJar.enterpriseBeans;

                for (EnterpriseBeanInfo enterpriseBean : enterpriseBeans) {
                    List<String> remoteInterfaces = enterpriseBean.businessRemote;
                    List<String> localInterfaces = enterpriseBean.businessLocal;
                    if (remoteInterfaces.contains(fieldType.getCanonicalName()) || localInterfaces.contains(fieldType.getCanonicalName())) {
                        return enterpriseBean;
                    }
                }
            }
        }

        return null;
    }

    private Collection<AppInfo> getDeployedApps() {
        Assembler assembler = SystemInstance.get().getComponent(Assembler.class);
        return assembler.getDeployedApplications();
    }

    public String getJndiName(EjbReference ref, EnterpriseBeanInfo beanInfo) {
        for (JndiNameInfo jndiNameInfo : beanInfo.jndiNamess) {
            if (ref instanceof EjbRef) {
                EjbRef ejbRef = (EjbRef) ref;
                if (jndiNameInfo.intrface.equals(ejbRef.getRemote())) {
                    return "java:openejb/remote/" + jndiNameInfo.name;
                }
            }
            if (ref instanceof EjbLocalRef) {
                EjbLocalRef ejbRef = (EjbLocalRef) ref;
                if (jndiNameInfo.intrface.equals(ejbRef.getLocal())) {
                    return "java:openejb/local/" + jndiNameInfo.name;
                }
            }
        }

        return null;
    }

    public String getJndiName(EjbReference ref) {
        EnterpriseBeanInfo beanInfo = getEJBInfo(ref);
        if (beanInfo == null) return null;
        return getJndiName(ref, beanInfo);
    }

    public String getJndiName(Class<?> iface) {
        EnterpriseBeanInfo beanInfo = getBeanInfo(iface);
        if (beanInfo == null) return null;

        List<JndiNameInfo> jndiNameInfoList = beanInfo.jndiNamess;
        for (JndiNameInfo jndiNameInfo : jndiNameInfoList) {
            if (jndiNameInfo.intrface.equals(iface.getCanonicalName()) && beanInfo.businessRemote.contains(iface.getCanonicalName())) {
                return "java:openejb/remote/" + jndiNameInfo.name;
            }
            if (jndiNameInfo.intrface.equals(iface.getCanonicalName()) && beanInfo.businessLocal.contains(iface.getCanonicalName())) {
                return "java:openejb/local/" + jndiNameInfo.name;
            }
        }

        return null;
    }

    public String getJndiName(EjbReferenceInfo ref) {
        EnterpriseBeanInfo beanInfo = getEJBInfo(ref);

        if (beanInfo == null) return null;
        return getJndiName(ref, beanInfo);
    }

    private String getJndiName(EjbReferenceInfo ref, EnterpriseBeanInfo beanInfo) {
        for (JndiNameInfo jndiNameInfo : beanInfo.jndiNamess) {
            if (jndiNameInfo.intrface.equals(ref.interfaceClassName) && beanInfo.businessRemote.contains(ref.interfaceClassName)) {
                return "java:openejb/remote/" + jndiNameInfo.name;
            }
            if (jndiNameInfo.intrface.equals(ref.interfaceClassName) && beanInfo.businessLocal.contains(ref.interfaceClassName)) {
                return "java:openejb/local/" + jndiNameInfo.name;
            }
        }

        return null;
    }

    private EnterpriseBeanInfo getEJBInfo(EjbReferenceInfo ref) {
        Collection<AppInfo> deployedApplications = getDeployedApps();
        for (AppInfo app : deployedApplications) {
            List<EjbJarInfo> ejbJars = app.ejbJars;
            for (EjbJarInfo ejbJar : ejbJars) {
                List<EnterpriseBeanInfo> enterpriseBeans = ejbJar.enterpriseBeans;
                for (EnterpriseBeanInfo enterpriseBean : enterpriseBeans) {
                    if (enterpriseBean.ejbDeploymentId.equals(ref.ejbDeploymentId)) {
                        return enterpriseBean;
                    }

                    if (enterpriseBean.remote != null && enterpriseBean.remote.equals(ref.interfaceClassName)) {
                        return enterpriseBean;
                    }

                    if (enterpriseBean.businessRemote != null && enterpriseBean.businessRemote.contains(ref.interfaceClassName)) {
                        return enterpriseBean;
                    }

                    if (enterpriseBean.local != null && enterpriseBean.local.equals(ref.interfaceClassName)) {
                        return enterpriseBean;
                    }

                    if (enterpriseBean.businessLocal != null && enterpriseBean.businessLocal.contains(ref.interfaceClassName)) {
                        return enterpriseBean;
                    }
                }
            }
        }

        return null;
    }
}
