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
package org.apache.openejb.alt.config;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.jee.oejb3.EjbDeployment;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.openejb.jee.oejb3.ResourceLink;
import org.apache.openejb.assembler.classic.ContainerInfo;
import org.apache.openejb.jee.ApplicationClient;
import org.apache.openejb.jee.ResourceRef;
import org.apache.openejb.util.SafeToolkit;

import java.lang.reflect.Method;
import java.util.List;

public class AutoDeployer implements DynamicDeployer {
    private final ConfigurationFactory config;

    public AutoDeployer(ConfigurationFactory config) {
        /* Load container list */
        this.config = config;
    }

    public void init() throws OpenEJBException {
    }

    public AppModule deploy(AppModule appModule) throws OpenEJBException {
        for (EjbModule ejbModule : appModule.getEjbModules()) {
            deploy(ejbModule);
        }
        for (ClientModule clientModule : appModule.getClientModules()) {
            deploy(clientModule);
        }
        return appModule;
    }

    public ClientModule deploy(ClientModule clientModule) throws OpenEJBException {
        ApplicationClient applicationClient = clientModule.getApplicationClient();
        List<ResourceRef> resourceRefs = applicationClient.getResourceRef();
        for (ResourceRef resourceRef : resourceRefs) {
            autoAssingResourceRef(resourceRef);
        }
        return clientModule;
    }

    public EjbModule deploy(EjbModule ejbModule) throws OpenEJBException {
        if (ejbModule.getOpenejbJar() != null) {
            return ejbModule;
        }

        OpenejbJar openejbJar = new OpenejbJar();
        ejbModule.setOpenejbJar(openejbJar);

        Bean[] beans = EjbJarUtils.getBeans(ejbModule.getEjbJar());

        for (int i = 0; i < beans.length; i++) {
            openejbJar.getEjbDeployment().add(deployBean(ejbModule, beans[i]));
        }
//        return new EjbModule(ejbModule.getClassLoader(), ejbModule.getJarURI(), ejbModule.getEjbJar(), openejbJar);
        return ejbModule;
    }

    private EjbDeployment deployBean(EjbModule ejbModule, Bean bean) throws OpenEJBException {
        EjbDeployment deployment = new EjbDeployment();

        deployment.setEjbName(bean.getEjbName());

        deployment.setDeploymentId(autoAssignDeploymentId(ejbModule.getModuleId(), bean));

        deployment.setContainerId(autoAssignContainerId(bean));

        ResourceRef[] refs = bean.getResourceRef();

        if (refs.length > 1) {
            throw new OpenEJBException("Beans with more that one resource-ref cannot be autodeployed;  there is no accurate way to determine how the references should be mapped.");
        }

        for (ResourceRef ref : refs) {
            if ((ref.getMappedName() + "").startsWith("jndi:")) {
                continue;
            }
            deployment.getResourceLink().add(autoAssingResourceRef(ref));
        }

        if (bean.getType().equals("CMP_ENTITY") && ((EntityBean) bean).getCmpVersion() == 1) {
            if (bean.getHome() != null) {
                Class tempBean = loadClass(ejbModule, bean.getHome());
                if (hasFinderMethods(tempBean)) {
                    throw new OpenEJBException("CMP 1.1 Beans with finder methods cannot be autodeployed; finder methods require OQL Select statements which cannot be generated accurately.");
                }
            }
            if (bean.getLocalHome() != null) {
                Class tempBean = loadClass(ejbModule, bean.getLocalHome());
                if (hasFinderMethods(tempBean)) {
                    throw new OpenEJBException("CMP 1.1 Beans with finder methods cannot be autodeployed; finder methods require OQL Select statements which cannot be generated accurately.");
                }
            }
        }

        return deployment;
    }

    private Class loadClass(EjbModule ejbModule, String className) throws OpenEJBException {
        try {
            return ejbModule.getClassLoader().loadClass(className);
        } catch (ClassNotFoundException cnfe) {
            throw new OpenEJBException(SafeToolkit.messages.format("cl0007", className, ejbModule.getJarURI()));
        }
    }

    private boolean hasFinderMethods(Class bean) throws OpenEJBException {

        Method[] methods = bean.getMethods();

        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getName().startsWith("find") && !methods[i].getName().equals("findByPrimaryKey")) {
                return true;
            }
        }
        return false;
    }

    private String autoAssignDeploymentId(String moduleId, Bean bean) throws OpenEJBException {
        return moduleId + "/" + bean.getEjbName();
    }

    private String autoAssignContainerId(Bean bean) throws OpenEJBException {
        Class<? extends ContainerInfo> containerInfoType = ConfigurationFactory.getContainerInfoType(bean.getType());

        String containerId = getUsableContainer(containerInfoType);

        if (containerId == null) {
            throw new OpenEJBException("A container of type " + bean.getType() + " must be declared in the configuration file.");
        }
        return containerId;
    }

    private String getUsableContainer(Class<? extends ContainerInfo> containerInfoType) {
        for (ContainerInfo containerInfo : config.getContainerInfos()) {
            if (containerInfo.getClass().equals(containerInfoType)) {
                return containerInfo.id;
            }
        }

        return null;
    }

    private ResourceLink autoAssingResourceRef(ResourceRef ref) throws OpenEJBException {

        List<String> resources = config.getConnectorIds();
        if (resources.size() == 0) {
            throw new OpenEJBException("A Connector must be declared in the configuration file to satisfy the resource-ref " + ref.getResRefName());
        }

        String id = resources.get(0);
        ref.setResLink(id);
        ResourceLink link = new ResourceLink();
        link.setResRefName(ref.getResRefName());
        link.setResId(id);
        return link;
    }

}
