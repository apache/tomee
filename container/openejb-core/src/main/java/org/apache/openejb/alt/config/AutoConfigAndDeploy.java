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

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.alt.config.ejb.EjbDeployment;
import org.apache.openejb.alt.config.ejb.MethodParams;
import org.apache.openejb.alt.config.ejb.OpenejbJar;
import org.apache.openejb.alt.config.ejb.Query;
import org.apache.openejb.alt.config.ejb.QueryMethod;
import org.apache.openejb.alt.config.ejb.ResourceLink;
import org.apache.openejb.assembler.classic.ConnectorInfo;
import org.apache.openejb.assembler.classic.ContainerInfo;
import org.apache.openejb.jee.ResourceRef;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.Messages;
import org.apache.openejb.util.SafeToolkit;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class AutoConfigAndDeploy implements DynamicDeployer {
    public static Messages messages = new Messages("org.apache.openejb.util.resources");
    public static Logger logger = Logger.getInstance("OpenEJB", "org.apache.openejb.util.resources");

    private final ConfigurationFactory configFactory;

    public AutoConfigAndDeploy(ConfigurationFactory configFactory) {
        this.configFactory = configFactory;
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
        return clientModule;
    }

    public EjbModule deploy(EjbModule ejbModule) throws OpenEJBException {
        String jarLocation = ejbModule.getJarURI();
        ClassLoader classLoader = ejbModule.getClassLoader();

        OpenejbJar openejbJar;
        if (ejbModule.getOpenejbJar() != null) {
            openejbJar = ejbModule.getOpenejbJar();
        } else {
            openejbJar = new OpenejbJar();
            ejbModule.setOpenejbJar(openejbJar);
        }

        Bean[] beans = EjbJarUtils.getBeans(ejbModule.getEjbJar());

        for (int i = 0; i < beans.length; i++) {
            final Bean bean = beans[i];

            EjbDeployment ejbDeployment = openejbJar.getDeploymentsByEjbName().get(bean.getEjbName());
            if (ejbDeployment == null) {

                ejbDeployment = new EjbDeployment();

                ejbDeployment.setEjbName(bean.getEjbName());
                ejbDeployment.setDeploymentId(autoAssignDeploymentId(bean));

                Class<? extends ContainerInfo> containerInfoType = ConfigurationFactory.getContainerInfoType(bean.getType());

                String containerId = getUsableContainer(containerInfoType);

                if (containerId == null){
                    ContainerInfo containerInfo = configFactory.configureDefaultService(containerInfoType);
                    logger.warning("Auto-creating a container for bean " + ejbDeployment.getDeploymentId() + ": Container(type=" + bean.getType() + ", id=" + containerInfo.id + ")");
                    configFactory.install(containerInfo);
                    containerId = containerInfo.id;
                }

                ejbDeployment.setContainerId(containerId);

                logger.warning("Auto-deploying ejb " + bean.getEjbName() + ": EjbDeployment(deployment-id=" + ejbDeployment.getDeploymentId() + ", container-id=" + ejbDeployment.getContainerId() + ")");
                openejbJar.getEjbDeployment().add(ejbDeployment);
            }

            // create the container if it doesn't exist
            if (!configFactory.getContainerIds().contains(ejbDeployment.getContainerId())) {

                ContainerInfo containerInfo = configFactory.configureDefaultService(ConfigurationFactory.getContainerInfoType(bean.getType()));
                logger.warning("Auto-creating a container for bean " + ejbDeployment.getDeploymentId() + ": Container(type=" + bean.getType() + ", id=" + containerInfo.id + ")");
                configFactory.install(containerInfo);

            }

            // check the resource refs
            ResourceRef[] refs = bean.getResourceRef();
            for (int j = 0; j < refs.length; j++) {
                ResourceRef ref = refs[j];
                ResourceLink link = ejbDeployment.getResourceLink(ref.getResRefName());
                if (link == null) {
                    link = new ResourceLink();
                    List<String> connectorMap = configFactory.getConnectorIds();
                    String resRefName = ref.getResRefName();

                    String id = null;
                    if (!connectorMap.contains(resRefName)) {
                        String name = resRefName.replaceFirst(".*/", "");
                        if (!connectorMap.contains(name)) {
                            ConnectorInfo connectorInfo = configFactory.configureDefaultService(ConnectorInfo.class);
                            id = connectorInfo.id = name;
                            logger.warning("Auto-creating a connector for res-ref-name '" + resRefName + "' in bean '" + ejbDeployment.getDeploymentId() + "': Connector(id=" + id + ").  THERE IS LITTLE CHANCE THIS WILL WORK!");
                            configFactory.install(connectorInfo);
                        }
                    }
                    logger.warning("Auto-linking res-ref-name '" + resRefName + "' in bean " + ejbDeployment.getDeploymentId() + " to Connector(id=" + id + ")");
                    link.setResId(id);
                    link.setResRefName(resRefName);
                    ejbDeployment.addResourceLink(link);
                } else {

                    List<String> connectorMap = configFactory.getConnectorIds();
                    if (!connectorMap.contains(link.getResId())) {
                        logger.error("Bad resource-link in bean '" + ejbDeployment.getDeploymentId() + "': No such connector with specified res-id: ResourceLink(res-ref-name=" + link.getResRefName() + ", res-id" + link.getResId() + ")");

                        String id = null;
                        if (connectorMap.size() > 0) {
                            id = connectorMap.get(0);
                        } else {
                            ConnectorInfo connectorInfo = configFactory.configureDefaultService(ConnectorInfo.class);
                            id = connectorInfo.id;
                            logger.warning("Auto-creating a connector with res-id " + link.getResId() + " for bean '"+ejbDeployment.getDeploymentId()+"'.  THERE IS LITTLE CHANCE THIS WILL WORK!");
                            configFactory.install(connectorInfo);
                        }
                    }
                }
            }

            if (bean.getType().equals("CMP_ENTITY") && ((EntityBean) bean).getCmpVersion() == 1) {
                List<Query> queries = ejbDeployment.getQuery();
                if (bean.getHome() != null) {
                    Class interfce = loadClass(bean.getHome(), classLoader, jarLocation);
                    List finderMethods = getFinderMethods(interfce);
                    for (Query query : queries) {
                        finderMethods.remove(new Key(query));
                    }
                    if (finderMethods.size() != 0) {
                        throw new OpenEJBException("CMP 1.1 Beans with finder methods cannot be autodeployed; finder methods require OQL Select statements which cannot be generated accurately.");
                    }
                }
                if (bean.getLocalHome() != null) {
                    Class interfce = loadClass(bean.getLocalHome(), classLoader, jarLocation);
                    List finderMethods = getFinderMethods(interfce);
                    for (Query query : queries) {
                        finderMethods.remove(new Key(query));
                    }
                    if (finderMethods.size() != 0) {
                        throw new OpenEJBException("CMP 1.1 Beans with finder methods cannot be autodeployed; finder methods require OQL Select statements which cannot be generated accurately.");
                    }
                }
            }
        }

        return ejbModule;
    }

    private static class Key {
        private final Query query;

        public Key(Query query) {
            this.query = query;
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof Method)) {
                return false;
            }
            Method method = (Method) obj;
            QueryMethod qmethod = query.getQueryMethod();
            if (!method.getName().equals(qmethod.getMethodName())) {
                return false;
            }
            MethodParams mp = qmethod.getMethodParams();
            int length = method.getParameterTypes().length;
            if ((mp == null && length != 0) || mp == null || mp.getMethodParam().size() != length) {
                return false;
            }
            List<String> params = mp.getMethodParam();
            for (int i = 0; i < method.getParameterTypes().length; i++) {
                Class<?> type = method.getParameterTypes()[i];
                if (!type.getName().equals(params.get(i))) {
                    return false;
                }
            }
            return true;
        }
    }


    private Class loadClass(String className, ClassLoader classLoader, String jarLocation) throws OpenEJBException {
        try {
            return classLoader.loadClass(className);
        } catch (ClassNotFoundException cnfe) {
            throw new OpenEJBException(SafeToolkit.messages.format("cl0007", className, jarLocation));
        }
    }

    private List<Method> getFinderMethods(Class bean) {

        Method[] methods = bean.getMethods();
        List<Method> finderMethods = new ArrayList();
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getName().startsWith("find") && !methods[i].getName().equals("findByPrimaryKey")) {
                finderMethods.add(methods[i]);
            }
        }
        return finderMethods;
    }

    private String autoAssignDeploymentId(Bean bean) {
        return bean.getEjbName();
    }

    private String getUsableContainer(Class<? extends ContainerInfo> containerInfoType) {
        for (ContainerInfo containerInfo : configFactory.getContainerInfos()) {
            if (containerInfo.getClass().equals(containerInfoType)){
                return containerInfo.id;
            }
        }

        return null;
    }
}
