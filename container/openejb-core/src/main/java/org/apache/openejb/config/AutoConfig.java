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
package org.apache.openejb.config;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.assembler.classic.ContainerInfo;
import org.apache.openejb.assembler.classic.ResourceInfo;
import org.apache.openejb.assembler.classic.LinkResolver;
import org.apache.openejb.config.sys.JaxbOpenejb;
import org.apache.openejb.config.sys.Resource;
import org.apache.openejb.jee.MessageDrivenBean;
import org.apache.openejb.jee.ActivationConfig;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.MessageDestination;
import org.apache.openejb.jee.AssemblyDescriptor;
import org.apache.openejb.jee.PersistenceType;
import org.apache.openejb.jee.SessionType;
import org.apache.openejb.jee.MessageDestinationRef;
import org.apache.openejb.jee.JndiReference;
import org.apache.openejb.jee.ResourceRef;
import org.apache.openejb.jee.jpa.unit.Persistence;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.apache.openejb.jee.oejb3.EjbDeployment;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.openejb.jee.oejb3.ResourceLink;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.Messages;

import javax.sql.DataSource;
import javax.jms.Queue;
import javax.jms.Topic;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.TreeMap;
import java.util.HashMap;
import java.net.URI;

public class AutoConfig implements DynamicDeployer {
    public static Messages messages = new Messages("org.apache.openejb.util.resources");
    public static Logger logger = Logger.getInstance("OpenEJB", "org.apache.openejb.util.resources");

    private static Map<String,String> defaultResourceIds = new TreeMap<String,String>();
    static {
        defaultResourceIds.put("javax.sql.DataSource", "Default Unmanaged JDBC Database");
        defaultResourceIds.put("javax.jms.ConnectionFactory", "Default JMS Connection Factory");
        defaultResourceIds.put("javax.jms.QueueConnectionFactory", "Default JMS Connection Factory");
        defaultResourceIds.put("javax.jms.TopicConnectionFactory", "Default JMS Connection Factory");
        defaultResourceIds.put("org.omg.CORBA.ORB", "Default ORB");
        defaultResourceIds.put("javax.mail.Session", "Default Mail Session");
    }

    private static Set<String> ignoredReferenceTypes = new TreeSet<String>();
    static{
        // Context objects are automatically handled
        ignoredReferenceTypes.add("javax.ejb.SessionContext");
        ignoredReferenceTypes.add("javax.ejb.EntityContext");
        ignoredReferenceTypes.add("javax.ejb.MessageDrivenContext");
        ignoredReferenceTypes.add("javax.xml.ws.WebServiceContext");
        // URLs are turned into env-refs
        ignoredReferenceTypes.add("java.net.URL");
        // User transaction is automatically handled
        ignoredReferenceTypes.add("javax.transaction.UserTransaction");
    }

    private final ConfigurationFactory configFactory;
    private boolean autoCreateContainers = true;
    private boolean autoCreateResources = true;

    public AutoConfig(ConfigurationFactory configFactory) {
        this.configFactory = configFactory;
    }

    public synchronized boolean autoCreateResources() {
        return autoCreateResources;
    }

    public synchronized void autoCreateResources(boolean autoCreateResources) {
        this.autoCreateResources = autoCreateResources;
    }

    public synchronized boolean autoCreateContainers() {
        return autoCreateContainers;
    }

    public synchronized void autoCreateContainers(boolean autoCreateContainers) {
        this.autoCreateContainers = autoCreateContainers;
    }

    public void init() throws OpenEJBException {
    }

    public synchronized AppModule deploy(AppModule appModule) throws OpenEJBException {
        for (EjbModule ejbModule : appModule.getEjbModules()) {
            processActivationConfig(ejbModule);
        }
        resolveDestinationLinks(appModule);

        for (EjbModule ejbModule : appModule.getEjbModules()) {
            deploy(ejbModule);
        }
        for (ClientModule clientModule : appModule.getClientModules()) {
            deploy(clientModule);
        }
        for (PersistenceModule persistenceModule : appModule.getPersistenceModules()) {
            deploy(persistenceModule);
        }
        return appModule;
    }

    /**
     * Set destination, destinationType, clientId and subscriptionName in the MDB activation config.
     */
    private void processActivationConfig(EjbModule ejbModule) throws OpenEJBException {
        OpenejbJar openejbJar;
        if (ejbModule.getOpenejbJar() != null) {
            openejbJar = ejbModule.getOpenejbJar();
        } else {
            openejbJar = new OpenejbJar();
            ejbModule.setOpenejbJar(openejbJar);
        }

        for (EnterpriseBean bean : ejbModule.getEjbJar().getEnterpriseBeans()) {
            if (bean instanceof MessageDrivenBean) {
                MessageDrivenBean mdb = (MessageDrivenBean) bean;

                EjbDeployment ejbDeployment = openejbJar.getDeploymentsByEjbName().get(bean.getEjbName());
                if (ejbDeployment == null) {
                    throw new OpenEJBException("No ejb deployment found for ejb " + bean.getEjbName());
                }

                if (mdb.getActivationConfig() == null) {
                    mdb.setActivationConfig(new ActivationConfig());
                }

                Properties properties = mdb.getActivationConfig().toProperties();

                // destination
                String destination = properties.getProperty("destination");
                if (destination == null) {
                    destination = ejbDeployment.getDeploymentId();
                    mdb.getActivationConfig().addProperty("destination", destination);
                }

                // destination identifier
                ResourceLink link = ejbDeployment.getResourceLink("openejb/destination");
                if (link == null && mdb.getMessageDestinationLink() == null) {
                    link = new ResourceLink();
                    link.setResId(destination);
                    link.setResRefName("openejb/destination");
                    ejbDeployment.addResourceLink(link);
                }
                                
                // destination type
                String destinationType = properties.getProperty("destinationType");
                if (destinationType == null && mdb.getMessageDestinationType() != null) {
                    destinationType = mdb.getMessageDestinationType();
                    mdb.getActivationConfig().addProperty("destinationType", destinationType);
                }
                if (mdb.getMessageDestinationType() == null) {
                    mdb.setMessageDestinationType(destinationType);
                }

                // topics need a clientId and subscriptionName
                if ("javax.jms.Topic".equals(destinationType)) {
                    if (!properties.containsKey("clientId")) {
                        mdb.getActivationConfig().addProperty("clientId", ejbDeployment.getDeploymentId());
                    }
                    if (!properties.containsKey("subscriptionName")) {
                        mdb.getActivationConfig().addProperty("subscriptionName", ejbDeployment.getDeploymentId() + "_subscription");
                    }
                }
            }
        }
    }

    /**
     * Set resource id in all message-destination-refs and MDBs that are using message destination links.
     */
    private void resolveDestinationLinks(AppModule appModule) throws OpenEJBException {
        // build up a link resolver
        LinkResolver<MessageDestination> destinationResolver = new LinkResolver<MessageDestination>();
        for (EjbModule ejbModule : appModule.getEjbModules()) {
            AssemblyDescriptor assembly = ejbModule.getEjbJar().getAssemblyDescriptor();
            if (assembly != null) {
                String moduleId = ejbModule.getModuleId();
                for (MessageDestination destination : assembly.getMessageDestination()) {
                    destinationResolver.add(moduleId, destination.getMessageDestinationName(), destination);
                }
            }
        }
        for (ClientModule clientModule : appModule.getClientModules()) {
            String moduleId = appModule.getModuleId();
            for (MessageDestination destination : clientModule.getApplicationClient().getMessageDestination()) {
                destinationResolver.add(moduleId, destination.getMessageDestinationName(), destination);
            }
        }

        // remember the type of each destination so we can use it to fillin MDBs that don't declare destination type
        Map<MessageDestination,String> destinationTypes = new HashMap<MessageDestination,String>();

        // resolve all MDBs with destination links
        // if MessageDestination does not have a mapped name assigned, give it the destination from the MDB
        for (EjbModule ejbModule : appModule.getEjbModules()) {
            AssemblyDescriptor assembly = ejbModule.getEjbJar().getAssemblyDescriptor();
            if (assembly == null) {
                continue;
            }

            URI moduleUri = URI.create(appModule.getModuleId());
            OpenejbJar openejbJar = ejbModule.getOpenejbJar();

            for (EnterpriseBean bean : ejbModule.getEjbJar().getEnterpriseBeans()) {
                // MDB destination is deploymentId if none set
                if (bean instanceof MessageDrivenBean) {
                    MessageDrivenBean mdb = (MessageDrivenBean) bean;

                    EjbDeployment ejbDeployment = openejbJar.getDeploymentsByEjbName().get(bean.getEjbName());
                    if (ejbDeployment == null) {
                        throw new OpenEJBException("No ejb deployment found for ejb " + bean.getEjbName());
                    }

                    // skip destination refs without a destination link
                    String link = mdb.getMessageDestinationLink();
                    if (link == null || link.length() == 0) {
                        continue;
                    }

                    // resolve the destination... if we don't find one it is a configuration bug
                    MessageDestination destination = destinationResolver.resolveLink(link, moduleUri);
                    if (destination == null) {
                        throw new OpenEJBException("Message destination " + link + " for message driven bean " + mdb.getEjbName()  + " not found");
                    }

                    // get the destinationId is the mapped name
                    String destinationId = destination.getMappedName();
                    if (destinationId == null) {
                        // if we don't have a mapped name use the destination of the mdb
                        Properties properties = mdb.getActivationConfig().toProperties();
                        destinationId = properties.getProperty("destination");
                        destination.setMappedName(destinationId);
                    }

                    if (mdb.getMessageDestinationType() != null && !destinationTypes.containsKey(destination)) {
                        destinationTypes.put(destination, mdb.getMessageDestinationType());
                    }

                    // destination identifier
                    ResourceLink resourceLink = ejbDeployment.getResourceLink("openejb/destination");
                    if (resourceLink == null) {
                        resourceLink = new ResourceLink();
                        resourceLink.setResRefName("openejb/destination");
                        ejbDeployment.addResourceLink(resourceLink);
                    }
                    resourceLink.setResId(destinationId);
                }
            }
        }
        
        // resolve all message destination refs with links and assign a ref id to the reference
        for (EjbModule ejbModule : appModule.getEjbModules()) {
            AssemblyDescriptor assembly = ejbModule.getEjbJar().getAssemblyDescriptor();
            if (assembly == null) {
                continue;
            }

            URI moduleUri = URI.create(appModule.getModuleId());
            OpenejbJar openejbJar = ejbModule.getOpenejbJar();

            for (EnterpriseBean bean : ejbModule.getEjbJar().getEnterpriseBeans()) {
                EjbDeployment ejbDeployment = openejbJar.getDeploymentsByEjbName().get(bean.getEjbName());
                if (ejbDeployment == null) {
                    throw new OpenEJBException("No ejb deployment found for ejb " + bean.getEjbName());
                }

                for (MessageDestinationRef ref : bean.getMessageDestinationRef()) {
                    // skip destination refs with a resource link already assigned
                    if (ejbDeployment.getResourceLink(ref.getName()) == null) {
                        String destinationId = resolveDestinationId(ref, moduleUri, destinationResolver, destinationTypes);
                        if (destinationId != null) {
                            // build the link and add it
                            ResourceLink resourceLink = new ResourceLink();
                            resourceLink.setResId(destinationId);
                            resourceLink.setResRefName(ref.getName());
                            ejbDeployment.addResourceLink(resourceLink);
                        }

                    }
                }
            }
        }

        for (ClientModule clientModule : appModule.getClientModules()) {
            URI moduleUri = URI.create(appModule.getModuleId());
            for (MessageDestinationRef ref : clientModule.getApplicationClient().getMessageDestinationRef()) {
                String destinationId = resolveDestinationId(ref, moduleUri, destinationResolver, destinationTypes);
                if (destinationId != null) {
                    // for client modules we put the destinationId in the mapped name
                    ref.setMappedName(destinationId);
                }
            }
        }

        // Process MDBs one more time...
        // this time fill in the destination type (if not alreday specified) with
        // the info from the destination (which got filled in from the references)
        for (EjbModule ejbModule : appModule.getEjbModules()) {
            AssemblyDescriptor assembly = ejbModule.getEjbJar().getAssemblyDescriptor();
            if (assembly == null) {
                continue;
            }

            URI moduleUri = URI.create(appModule.getModuleId());
            OpenejbJar openejbJar = ejbModule.getOpenejbJar();

            for (EnterpriseBean bean : ejbModule.getEjbJar().getEnterpriseBeans()) {
                // MDB destination is deploymentId if none set
                if (bean instanceof MessageDrivenBean) {
                    MessageDrivenBean mdb = (MessageDrivenBean) bean;

                    EjbDeployment ejbDeployment = openejbJar.getDeploymentsByEjbName().get(bean.getEjbName());
                    if (ejbDeployment == null) {
                        throw new OpenEJBException("No ejb deployment found for ejb " + bean.getEjbName());
                    }

                    // if destination type is already set in, continue
                    String destinationType = mdb.getMessageDestinationType();
                    if (destinationType != null) {
                        continue;
                    }

                    String link = mdb.getMessageDestinationLink();
                    if (link != null && link.length() != 0) {
                        // resolve the destination... if we don't find one it is a configuration bug
                        MessageDestination destination = destinationResolver.resolveLink(link, moduleUri);
                        if (destination == null) {
                            throw new OpenEJBException("Message destination " + link + " for message driven bean " + mdb.getEjbName()  + " not found");
                        }
                        destinationType = destinationTypes.get(destination);
                    }

                    if (destinationType == null) {
                        // couldn't determine type... we'll have to guess

                        // if destination name contains the string "queue" or "topic" we use that
                        Properties properties = mdb.getActivationConfig().toProperties();
                        String destination = properties.getProperty("destination").toLowerCase();
                        if (destination.indexOf("queue") >= 0) {
                            destinationType = Queue.class.getName();
                        } else if (destination.indexOf("topic") >= 0) {
                            destinationType = Topic.class.getName();
                        } else {
                            // Queue is the default
                            destinationType = Queue.class.getName();
                        }
                        logger.warning("Auto-configuring a message driven bean " + ejbDeployment.getDeploymentId() + " destination " + properties.getProperty("destination") + " to be destinationType " + destinationType);
                    }

                    if (destinationType != null) {
                        mdb.getActivationConfig().addProperty("destinationType", destinationType);
                        mdb.setMessageDestinationType(destinationType);

                        // topics need a clientId and subscriptionName
                        if ("javax.jms.Topic".equals(destinationType)) {
                            Properties properties = mdb.getActivationConfig().toProperties();
                            if (!properties.containsKey("clientId")) {
                                mdb.getActivationConfig().addProperty("clientId", ejbDeployment.getDeploymentId());
                            }
                            if (!properties.containsKey("subscriptionName")) {
                                mdb.getActivationConfig().addProperty("subscriptionName", ejbDeployment.getDeploymentId() + "_subscription");
                            }
                        }
                    }
                }
            }
        }

    }

    private String resolveDestinationId(MessageDestinationRef ref, URI moduleUri, LinkResolver<MessageDestination> destinationResolver, Map<MessageDestination,String> destinationTypes) throws OpenEJBException {
        // skip destination refs without a destination link
        String link = ref.getMessageDestinationLink();
        if (link == null || link.length() == 0) {
            return null;
        }

        // resolve the destination... if we don't find one it is a configuration bug
        MessageDestination destination = destinationResolver.resolveLink(link, moduleUri);
        if (destination == null) {
            throw new OpenEJBException("Message destination " + link + " for message-destination-ref " + ref.getMessageDestinationRefName()  + " not found");
        }

        // remember the type of each destination so we can use it to fillin MDBs that don't declare destination type
        if (ref.getMessageDestinationType() != null && !destinationTypes.containsKey(destination)) {
            destinationTypes.put(destination, ref.getMessageDestinationType());
        }

        // get the destinationId
        String destinationId = destination.getMappedName();
        if (destinationId == null) destination.getMessageDestinationName();
        return destinationId;
    }

    private void deploy(ClientModule clientModule) throws OpenEJBException {
        // Resource env reference
        for (ResourceRef ref : clientModule.getApplicationClient().getResourceRef()) {
            // skip destinations with a global jndi name
            String mappedName = ref.getMappedName();
            if (mappedName == null) mappedName = "";
            if (mappedName.startsWith("jndi:")){
                continue;
            }

            String destinationId = (mappedName.length() == 0) ? ref.getName() : mappedName;
            destinationId = getResourceId(clientModule.getModuleId(), destinationId, ref.getType());
            ref.setMappedName(destinationId);
        }

        // Resource env reference
        for (JndiReference ref : clientModule.getApplicationClient().getResourceEnvRef()) {
            // skip destinations with a global jndi name
            String mappedName = ref.getMappedName() + "";
            if (mappedName.startsWith("jndi:")){
                continue;
            }

            String destinationId = (mappedName.length() == 0) ? ref.getName() : mappedName;
            destinationId = getResourceEnvId(clientModule.getModuleId(), destinationId, ref.getType());
            ref.setMappedName(destinationId);
        }

        // Message destination reference
        for (MessageDestinationRef ref : clientModule.getApplicationClient().getMessageDestinationRef()) {
            // skip destinations with a global jndi name
            String mappedName = ref.getMappedName() + "";
            if (mappedName.startsWith("jndi:")){
                continue;
            }

            String destinationId = (mappedName.length() == 0) ? ref.getName() : mappedName;
            destinationId = getResourceEnvId(clientModule.getModuleId(), destinationId, ref.getType());
            ref.setMappedName(destinationId);
        }
    }

    private void deploy(EjbModule ejbModule) throws OpenEJBException {
        OpenejbJar openejbJar;
        if (ejbModule.getOpenejbJar() != null) {
            openejbJar = ejbModule.getOpenejbJar();
        } else {
            openejbJar = new OpenejbJar();
            ejbModule.setOpenejbJar(openejbJar);
        }

        for (EnterpriseBean bean : ejbModule.getEjbJar().getEnterpriseBeans()) {
            EjbDeployment ejbDeployment = openejbJar.getDeploymentsByEjbName().get(bean.getEjbName());
            if (ejbDeployment == null) {
                throw new OpenEJBException("No ejb deployment found for ejb " + bean.getEjbName());
            }

            Class<? extends ContainerInfo> containerInfoType = ConfigurationFactory.getContainerInfoType(getType(bean));
            if (ejbDeployment.getContainerId() == null && !skipMdb(bean)) {
                String containerId = getUsableContainer(containerInfoType, bean);
                if (containerId == null){
                    containerId = createContainer(containerInfoType, ejbDeployment, bean);
                }
                ejbDeployment.setContainerId(containerId);
            }

            // create the container if it doesn't exist
            if (!configFactory.getContainerIds().contains(ejbDeployment.getContainerId()) && !skipMdb(bean)) {
                createContainer(containerInfoType, ejbDeployment, bean);
            }

            // Resource reference
            for (ResourceRef ref : bean.getResourceRef()) {
                processResourceRef(ref, ejbDeployment);
            }

            // Resource env reference
            for (JndiReference ref : bean.getResourceEnvRef()) {
                processResourceEnvRef(ref, ejbDeployment);
            }

            // Message destination reference
            for (MessageDestinationRef ref : bean.getMessageDestinationRef()) {
                processResourceEnvRef(ref, ejbDeployment);
            }


            // mdb message destination id
            if (autoCreateResources && bean instanceof MessageDrivenBean) {
                MessageDrivenBean mdb = (MessageDrivenBean) bean;

                ResourceLink resourceLink = ejbDeployment.getResourceLink("openejb/destination");
                if (resourceLink != null) {
                    String destinationId = getResourceEnvId(bean.getEjbName(), resourceLink.getResId(), mdb.getMessageDestinationType());
                    resourceLink.setResId(destinationId);
                }
            }

        }
    }

    private String createContainer(Class<? extends ContainerInfo> containerInfoType, EjbDeployment ejbDeployment, EnterpriseBean bean) throws OpenEJBException {
        if (!autoCreateContainers) {
            throw new OpenEJBException("A container of type " + getType(bean) + " must be declared in the configuration file for bean: " + bean.getEjbName());
        }

        // get the container info (data used to build the container)
        ContainerInfo containerInfo = configFactory.configureService(containerInfoType);
        logger.warning("Auto-creating a container for bean " + ejbDeployment.getDeploymentId() + ": Container(type=" + getType(bean) + ", id=" + containerInfo.id + ")");

        // if the is an MDB container we need to resolve the resource adapter
        String resourceAdapterId = containerInfo.properties.getProperty("ResourceAdapter");
        if (resourceAdapterId != null) {
            String newResourceId = getResourceId(ejbDeployment.getDeploymentId(), resourceAdapterId, null);
            if (resourceAdapterId != newResourceId) {
                containerInfo.properties.setProperty("ResourceAdapter", newResourceId);
            }
        }

        // install the container
        configFactory.install(containerInfo);
        return containerInfo.id;
    }

    private void processResourceRef(ResourceRef ref, EjbDeployment ejbDeployment) throws OpenEJBException {
        // skip destinations with a global jndi name
        String mappedName = ref.getMappedName();
        if (mappedName == null) mappedName = "";
        if ((mappedName).startsWith("jndi:")){
            return;
        }

        String refName = ref.getName();
        String refType = ref.getType();

        // skip references such as URLs which are automatically handled by the server
        if (ignoredReferenceTypes.contains(refType)) {
            return;
        }

        ResourceLink link = ejbDeployment.getResourceLink(refName);
        if (link == null) {
            String id = (mappedName.length() == 0) ? ref.getName() : mappedName;
            id = getResourceId(ejbDeployment.getDeploymentId(), id, refType);
            logger.warning("Auto-linking resource reference '" + refName + "' in bean " + ejbDeployment.getDeploymentId() + " to Resource(id=" + id + ")");

            link = new ResourceLink();
            link.setResId(id);
            link.setResRefName(refName);
            ejbDeployment.addResourceLink(link);
        } else {
            String id = getResourceId(ejbDeployment.getDeploymentId(), link.getResId(), refType);
            link.setResId(id);
            link.setResRefName(refName);
        }
    }

    private void processResourceEnvRef(JndiReference ref, EjbDeployment ejbDeployment) throws OpenEJBException {
        // skip destinations with a global jndi name
        String mappedName = ref.getMappedName() + "";
        if (mappedName.startsWith("jndi:")){
            return;
        }

        String refName = ref.getName();
        String refType = ref.getType();

        // skip references such as SessionContext which are automatically handled by the server
        if (ignoredReferenceTypes.contains(refType)) {
            return;
        }

        ResourceLink link = ejbDeployment.getResourceLink(refName);
        if (link == null) {

            String id = (mappedName.length() == 0) ? refName : mappedName;
            id = getResourceEnvId(ejbDeployment.getDeploymentId(), id, refType);
            if (id == null) {
                // could be a session context ref
                return;
            }
            logger.warning("Auto-linking resource reference '" + refName + "' in bean " + ejbDeployment.getDeploymentId() + " to Resource(id=" + id + ")");

            link = new ResourceLink();
            link.setResId(id);
            link.setResRefName(refName);
            ejbDeployment.addResourceLink(link);
        } else {
            String id = getResourceEnvId(ejbDeployment.getDeploymentId(), link.getResId(), refType);
            link.setResId(id);
            link.setResRefName(refName);
        }
    }

    private static boolean skipMdb(Object bean) {
        return bean instanceof MessageDrivenBean && System.getProperty("duct tape") != null;
    }

    private static String getType(EnterpriseBean enterpriseBean) throws OpenEJBException {
        if (enterpriseBean instanceof org.apache.openejb.jee.EntityBean) {
            if (((org.apache.openejb.jee.EntityBean)enterpriseBean).getPersistenceType() == PersistenceType.CONTAINER) {
                return Bean.CMP_ENTITY;
            } else {
                return Bean.BMP_ENTITY;
            }
        } else if (enterpriseBean instanceof org.apache.openejb.jee.SessionBean) {
            if (((org.apache.openejb.jee.SessionBean) enterpriseBean).getSessionType() == SessionType.STATEFUL) {
                return Bean.STATEFUL;
            } else {
                return Bean.STATELESS;
            }
        } else if (enterpriseBean instanceof org.apache.openejb.jee.MessageDrivenBean) {
            return Bean.MESSAGE;
        }
        throw new OpenEJBException("Unknown enterprise bean type " + enterpriseBean.getClass().getName());
    }

    private void deploy(PersistenceModule persistenceModule) throws OpenEJBException {
        if (!autoCreateResources) {
            return;
        }

        Persistence persistence = persistenceModule.getPersistence();
        for (PersistenceUnit persistenceUnit : persistence.getPersistenceUnit()) {
            String jtaDataSourceId = getResourceId(persistenceUnit.getName(), persistenceUnit.getJtaDataSource(), DataSource.class.getName());
            if (jtaDataSourceId != null) {
                persistenceUnit.setJtaDataSource("java:openejb/Resource/" + jtaDataSourceId);
            }
            String nonJtaDataSourceId = getResourceId(persistenceUnit.getName(), persistenceUnit.getNonJtaDataSource(), DataSource.class.getName());
            if (nonJtaDataSourceId != null) {
                persistenceUnit.setNonJtaDataSource("java:openejb/Resource/" + nonJtaDataSourceId);
            }
        }
    }

    private String getResourceId(String beanName, String resourceId, String type) throws OpenEJBException {
        if(resourceId == null){
            return null;
        }

        // skip references such as URL which are automatically handled by the server
        if (type != null && ignoredReferenceTypes.contains(type)) {
            return null;
        }

        // strip off "java:comp/env"
        if (resourceId.startsWith("java:comp/env")) {
            resourceId = resourceId.substring("java:comp/env".length());
        }

        // check for existing resource with specified resourceId
        List<String> resourceIds = configFactory.getResourceIds(type);
        if (resourceIds.contains(resourceId)) {
            return resourceId;
        }

        // check for an existing resource using the short name (everything ever the final '/')
        String shortName = resourceId.replaceFirst(".*/", "");
        if (resourceIds.contains(shortName)) {
            return shortName;
        }

        // throw an exception or log an error
        String message = "No existing resource found while attempting to Auto-link unmapped reference '" + resourceId + "' of type '" + type  + "' for '" + beanName + "'.  Looked for Resource(id=" + resourceId + ") and Resource(id=" + shortName + ")";
        if (!autoCreateResources){
            throw new OpenEJBException(message);
        }
        logger.info(message);

        // if there is a provider with the specified name. use it
        if (ServiceUtils.hasServiceProvider(resourceId)) {
            ResourceInfo resourceInfo = configFactory.configureService(resourceId, ResourceInfo.class);
            return installResource(beanName, resourceInfo);
        } else if (ServiceUtils.hasServiceProvider(shortName)) {
            ResourceInfo resourceInfo = configFactory.configureService(shortName, ResourceInfo.class);
            return installResource(beanName, resourceInfo);
        }

        // if there is only one resource, use it
        if (resourceIds.size() > 0) {
            return resourceIds.get(0);
        }

        // look for a default resource based on the type
        Resource resource = getDefaultResource(type);
        if (resource == null) {
            // no default resource for this type... give up
            throw new OpenEJBException("No default resource defined for reference '" + resourceId + "' of type '" + type  + "' for '" + beanName + "'.");
        }
        ResourceInfo resourceInfo = configFactory.configureService(resource, ResourceInfo.class);
        logger.warning("Auto-creating a resource with id '" + resourceInfo.id +  "' of type '" + type  + " for '" + beanName + "'.  THERE IS LITTLE CHANCE THIS WILL WORK!");
        return installResource(beanName, resourceInfo);
    }

    private String installResource(String beanName, ResourceInfo resourceInfo) throws OpenEJBException {
        String resourceAdapterId = resourceInfo.properties.getProperty("ResourceAdapter");
        if (resourceAdapterId != null) {
            String newResourceId = getResourceId(beanName, resourceAdapterId, null);
            if (resourceAdapterId != newResourceId) {
                resourceInfo.properties.setProperty("ResourceAdapter", newResourceId);
            }
        }

        configFactory.install(resourceInfo);
        return resourceInfo.id;
    }

    private Resource getDefaultResource(String type) {
        String providerId = defaultResourceIds.get(type);
        if (providerId == null) {
            return null;
        }
        Resource resource = JaxbOpenejb.createResource();
        resource.setProvider(providerId);
        resource.setId(providerId);
        return resource;
    }

    private String getResourceEnvId(String beanName, String resourceId, String type) throws OpenEJBException {
        if(resourceId == null){
            return null;
        }

        // skip references such as URLs which are automatically handled by the server
        if (ignoredReferenceTypes.contains(type)) {
            return null;
        }

        // strip off "java:comp/env"
        if (resourceId.startsWith("java:comp/env")) {
            resourceId = resourceId.substring("java:comp/env".length());
        }

        // check for existing resource with specified resourceId
        List<String> resourceIds = configFactory.getResourceIds(type);
        if (resourceIds.contains(resourceId)) {
            return resourceId;
        }

        // throw an exception or log an error
        String message = "No existing resource found while attempting to Auto-link unmapped reference '" + resourceId + "' of type '" + type  + "' for '" + beanName + "'.  Looked for Resource(id=" + resourceId + ")";
        if (!autoCreateResources){
            throw new OpenEJBException(message);
        }
        logger.info(message);


        // if there isn't a type we can't create a default resource
        if (type == null) {
            throw new OpenEJBException("No provider available for resource reference '" + resourceId + "' of type '" + type + "' for '" + beanName + "'.");
        }

        // properties for new resource
        Properties properties = new Properties();
        properties.setProperty("destination", resourceId);

        // Search for a provider like "Default javax.jms.Queue"
        String longId = "Default " + type;
        if (ServiceUtils.hasServiceProvider(longId)) {
            ResourceInfo resourceInfo = configFactory.configureService(ResourceInfo.class, resourceId, properties, longId, null);
            return installResource(beanName, resourceInfo);
        }

        // Search for a provider like "Default Queue"
        String shortId = "Default " + type.replaceFirst(".*\\.", "");
        if (ServiceUtils.hasServiceProvider(shortId)) {
            ResourceInfo resourceInfo = configFactory.configureService(ResourceInfo.class, resourceId, properties, shortId, null);
            return installResource(beanName, resourceInfo);
        }

        throw new OpenEJBException("No provider available for resource reference '" + resourceId + "' of type '" + type + "' for '" + beanName + "'.  Looked for Resource(id=" + longId + ") and Resource(id=" + shortId + ")");
    }

    private String getUsableContainer(Class<? extends ContainerInfo> containerInfoType, Object bean) {
        for (ContainerInfo containerInfo : configFactory.getContainerInfos()) {
            if (containerInfo.getClass().equals(containerInfoType)){
                // MDBs must match message listener interface type
                if (bean instanceof MessageDrivenBean) {
                    MessageDrivenBean messageDrivenBean = (MessageDrivenBean) bean;
                    String messagingType = messageDrivenBean.getMessagingType();
                    if (containerInfo.properties.get("MessageListenerInterface").equals(messagingType)) {
                        return containerInfo.id;
                    }
                } else {
                    return containerInfo.id;
                }
            }
        }

        return null;
    }
}
