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

import org.apache.openejb.alt.config.sys.Openejb;
import org.apache.openejb.alt.config.sys.Container;
import org.apache.openejb.alt.config.sys.Connector;
import org.apache.openejb.alt.config.ejb.OpenejbJar;
import org.apache.openejb.alt.config.ejb.EjbDeployment;
import org.apache.openejb.alt.config.ejb.ResourceLink;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.util.SafeToolkit;
import org.apache.openejb.util.Messages;
import org.apache.openejb.util.Logger;
import org.apache.openejb.jee.ResourceRef;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.HashMap;

public class AutoConfigAndDeploy implements DynamicDeployer {
    public static Messages messages = new Messages("org.apache.openejb.util.resources");
    public static Logger logger = Logger.getInstance("OpenEJB", "org.apache.openejb.util.resources");

    private final Openejb config;
    private ClassLoader classLoader;
    private String jarLocation;

    public AutoConfigAndDeploy(Openejb config) {
        this.config = config;
    }

    public void init() throws OpenEJBException {
    }

    public EjbModule deploy(EjbModule ejbModule) throws OpenEJBException {
        this.jarLocation = ejbModule.getJarURI();
        this.classLoader = ejbModule.getClassLoader();

        OpenejbJar openejbJar;
        if (ejbModule.getOpenejbJar() != null) {
            openejbJar = ejbModule.getOpenejbJar();
        } else {
            openejbJar = new OpenejbJar();
        }

        Bean[] beans = EjbJarUtils.getBeans(ejbModule.getEjbJar());

        for (int i = 0; i < beans.length; i++) {
            final Bean bean = beans[i];

            EjbDeployment ejbDeployment = openejbJar.getDeploymentsByEjbName().get(bean.getEjbName());
            if (ejbDeployment == null){

                ejbDeployment = new EjbDeployment();

                ejbDeployment.setEjbName(bean.getEjbName());
                ejbDeployment.setDeploymentId(autoAssignDeploymentId(bean));
                ejbDeployment.setContainerId(autoAssignContainerId(bean));

                logger.warning("Auto-deploying ejb "+bean.getEjbName()+": EjbDeployment(deployment-id="+ejbDeployment.getDeploymentId()+", container-id="+ejbDeployment.getContainerId()+")");
                openejbJar.getEjbDeployment().add(ejbDeployment);
            }

            // create the container if it doesn't exist
            Map<String, Container> containerMap = getContainersById();
            if (!containerMap.containsKey(ejbDeployment.getContainerId())){
                Container container = new Container();
                container.setId(ejbDeployment.getContainerId());
                container.setCtype(bean.getType());
                logger.warning("Auto-creating a container for bean "+ejbDeployment.getDeploymentId()+": Container(type="+container.getCtype()+", id="+container.getId()+")");
                config.addContainer(container);
            }

            // check the resource refs
            ResourceRef[] refs = bean.getResourceRef();
            for (int j = 0; j < refs.length; j++) {
                ResourceRef ref = refs[j];
                ResourceLink link = ejbDeployment.getResourceLink(ref.getResRefName());
                if (link == null){
                    link = new ResourceLink();
                    Map<String, Connector> connectorMap = getConnectorsById();
                    String resRefName = ref.getResRefName();
                    Connector connector = connectorMap.get(resRefName);
                    if (connector == null){
                        String name = resRefName.replaceFirst(".*/","");
                        connector = connectorMap.get(name);
                        if (connector == null){
                            connector = new Connector();
                            connector.setId(name);
                            logger.warning("Auto-creating a connector for res-ref-name '"+resRefName+"' in bean "+ejbDeployment.getDeploymentId()+": Connector(id="+connector.getId()+").  THERE IS LITTLE CHANCE THIS WILL WORK!");
                            config.addConnector(connector);
                        }
                    }
                    logger.warning("Auto-linking res-ref-name '"+resRefName+"' in bean "+ejbDeployment.getDeploymentId()+" to Connector(id="+connector.getId()+")");
                    link.setResId(connector.getId());
                    link.setResRefName(resRefName);
                    ejbDeployment.addResourceLink(link);
                } else {
                    Map<String, Connector> connectorMap = getConnectorsById();
                    Connector connector = connectorMap.get(link.getResId());
                    if (connector == null) {
                        logger.error("Bad resource-link: No such connector with specified res-id: ResourceLink(res-ref-name="+link.getResRefName()+", res-id"+link.getResId()+")");
                        connector = new Connector();
                        connector.setId(link.getResId());
                        logger.warning("Auto-creating a connector with res-id "+link.getResId()+".  THERE IS LITTLE CHANCE THIS WILL WORK!");
                        config.addConnector(connector);
                    }
                }
            }

            if (bean.getType().equals("CMP_ENTITY")) {
                if (bean.getHome() != null) {
                    Class tempBean = loadClass(bean.getHome());
                    if (hasFinderMethods(tempBean)) {
                        throw new OpenEJBException("CMP 1.1 Beans with finder methods cannot be autodeployed; finder methods require OQL Select statements which cannot be generated accurately.");
                    }
                }
                if (bean.getLocalHome() != null) {
                    Class tempBean = loadClass(bean.getLocalHome());
                    if (hasFinderMethods(tempBean)) {
                        throw new OpenEJBException("CMP 1.1 Beans with finder methods cannot be autodeployed; finder methods require OQL Select statements which cannot be generated accurately.");
                    }
                }
            }
        }


        return new EjbModule(this.jarLocation, ejbModule.getEjbJar(), openejbJar);
    }

    private Map<String, Connector> getConnectorsById() {
        Connector[] connectorList = config.getConnector();
        Map<String,Connector> connectorMap = new HashMap();
        for (int k = 0; k < connectorList.length; k++) {
            Connector connector = connectorList[k];
            connectorMap.put(connector.getId(), connector);
        }
        return connectorMap;
    }

    private Map<String, Container> getContainersById() {
        Container[] containerList = config.getContainer();
        Map<String,Container> containerMap = new HashMap();
        for (int j = 0; j < containerList.length; j++) {
            Container container = containerList[j];
            containerMap.put(container.getId(), container);
        }
        return containerMap;
    }

    private Class loadClass(String className) throws OpenEJBException {
        try {
            return classLoader.loadClass(className);
        } catch (ClassNotFoundException cnfe) {
            throw new OpenEJBException(SafeToolkit.messages.format("cl0007", className, this.jarLocation));
        }
    }

    private boolean hasFinderMethods(Class bean)
            throws OpenEJBException {

        Method[] methods = bean.getMethods();

        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getName().startsWith("find")
                    && !methods[i].getName().equals("findByPrimaryKey")) {
                return true;
            }
        }
        return false;
    }

    private String autoAssignDeploymentId(Bean bean){
        return bean.getEjbName();
    }

    private String autoAssignContainerId(Bean bean) {
        Container[] usableContainers = EjbJarUtils.getUsableContainers(config.getContainer(), bean);

        if (usableContainers != null && usableContainers.length > 0){
            return usableContainers[0].getId();
        } else {
            String type = bean.getType();
            if (type.equals(Bean.BMP_ENTITY)){
                return ProviderDefaults.DEFAULT_BMP_CONTAINER;
            } else if (type.equals(Bean.CMP_ENTITY)){
                return ProviderDefaults.DEFAULT_CMP_CONTAINER;
            } else if (type.equals(Bean.STATEFUL)){
                return ProviderDefaults.DEFAULT_STATEFUL_CONTAINER;
            } else if (type.equals(Bean.STATELESS)){
                return ProviderDefaults.DEFAULT_STATELESS_CONTAINER;
            }

            throw new IllegalStateException("Unknown bean type "+type);
        }
    }
}
