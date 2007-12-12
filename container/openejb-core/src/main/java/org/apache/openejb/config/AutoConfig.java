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
import org.apache.openejb.config.sys.Resource;
import org.apache.openejb.assembler.classic.ContainerInfo;
import org.apache.openejb.assembler.classic.ResourceInfo;
import org.apache.openejb.assembler.classic.LinkResolver;
import org.apache.openejb.assembler.classic.UniqueDefaultLinkResolver;
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
import org.apache.openejb.jee.JndiConsumer;
import org.apache.openejb.jee.Connector;
import org.apache.openejb.jee.ResourceAdapter;
import org.apache.openejb.jee.OutboundResourceAdapter;
import org.apache.openejb.jee.ConnectionDefinition;
import org.apache.openejb.jee.InboundResource;
import org.apache.openejb.jee.MessageListener;
import org.apache.openejb.jee.AdminObject;
import org.apache.openejb.jee.PersistenceContextRef;
import org.apache.openejb.jee.PersistenceRef;
import org.apache.openejb.jee.jpa.unit.Persistence;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.apache.openejb.jee.oejb3.EjbDeployment;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.openejb.jee.oejb3.ResourceLink;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.URISupport;
import static org.apache.openejb.util.Join.join;

import javax.sql.DataSource;
import javax.jms.Queue;
import javax.jms.Topic;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Collection;
import java.net.URI;

public class AutoConfig implements DynamicDeployer {

    public static Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP_CONFIG, AutoConfig.class);

    private static Set<String> ignoredReferenceTypes = new TreeSet<String>();
    static{
        // Context objects are automatically handled
        ignoredReferenceTypes.add("javax.ejb.SessionContext");
        ignoredReferenceTypes.add("javax.ejb.EntityContext");
        ignoredReferenceTypes.add("javax.ejb.MessageDrivenContext");
        ignoredReferenceTypes.add("javax.xml.ws.WebServiceContext");
        // URLs are automatically handled
        ignoredReferenceTypes.add("java.net.URL");
        // User transaction is automatically handled
        ignoredReferenceTypes.add("javax.transaction.UserTransaction");
        ignoredReferenceTypes.add("javax.ejb.TimerService");
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
        AppResources appResources = new AppResources(appModule);

        for (EjbModule ejbModule : appModule.getEjbModules()) {
            processActivationConfig(ejbModule);
        }
        resolveDestinationLinks(appModule);

        resolvePersistenceRefs(appModule);

        for (EjbModule ejbModule : appModule.getEjbModules()) {
            deploy(ejbModule, appResources);
        }
        for (ClientModule clientModule : appModule.getClientModules()) {
            deploy(clientModule, appResources);
        }
        for (ConnectorModule connectorModule : appModule.getResourceModules()) {
            deploy(connectorModule);
        }
        for (WebModule webModule : appModule.getWebModules()) {
            deploy(webModule, appResources);
        }
        for (PersistenceModule persistenceModule : appModule.getPersistenceModules()) {
            deploy(persistenceModule);
        }
        return appModule;
    }

    private void resolvePersistenceRefs(AppModule appModule) {
        LinkResolver<PersistenceUnit> persistenceUnits = new UniqueDefaultLinkResolver<PersistenceUnit>();

        for (PersistenceModule module : appModule.getPersistenceModules()) {
            String rootUrl = module.getRootUrl();
            for (PersistenceUnit unit : module.getPersistence().getPersistenceUnit()) {
                unit.setId(rootUrl + "#" + unit.getName());
                persistenceUnits.add(rootUrl, unit.getName(), unit);
            }
        }

        for (EjbModule ejbModule : appModule.getEjbModules()) {
            URI moduleURI = URI.create(ejbModule.getModuleId());

            for (JndiConsumer component : ejbModule.getEjbJar().getEnterpriseBeans()) {
                processPersistenceRefs(component, appModule, persistenceUnits, moduleURI);
            }

        }

        for (ClientModule clientModule : appModule.getClientModules()) {
            URI moduleURI = URI.create(clientModule.getModuleId());
            processPersistenceRefs(clientModule.getApplicationClient(), appModule, persistenceUnits, moduleURI);
        }

        for (WebModule webModule : appModule.getWebModules()) {
            URI moduleURI = URI.create(webModule.getModuleId());
            processPersistenceRefs(webModule.getWebApp(), appModule, persistenceUnits, moduleURI);
        }
    }

    private void processPersistenceRefs(JndiConsumer component, AppModule appModule, LinkResolver<PersistenceUnit> persistenceUnits, URI moduleURI) {

        String componentName = component.getJndiConsumerName();

        ValidationContext validation = appModule.getValidation();

        for (PersistenceRef ref : component.getPersistenceUnitRef()) {

            processPersistenceRef(persistenceUnits, ref, moduleURI, componentName, validation);
        }

        for (PersistenceRef ref : component.getPersistenceContextRef()) {

            processPersistenceRef(persistenceUnits, ref, moduleURI, componentName, validation);
        }
    }

    private PersistenceUnit processPersistenceRef(LinkResolver<PersistenceUnit> persistenceUnits, PersistenceRef ref, URI moduleURI, String componentName, ValidationContext validation) {

        if (ref.getMappedName() != null && ref.getMappedName().startsWith("jndi:")){
            return null;
        }

        PersistenceUnit unit = persistenceUnits.resolveLink(ref.getPersistenceUnitName(), moduleURI);

        // Explicitly check if we messed up the "if there's only one,
        // that's what you get" rule by adding our "cmp" unit.
        Collection<PersistenceUnit> cmpUnits = persistenceUnits.values("cmp");
        if (cmpUnits.size() > 0 && persistenceUnits.values().size() - cmpUnits.size() == 1) {
            // We did, there is exactly one non-cmp unit.  Let's find it.
            for (PersistenceUnit persistenceUnit : persistenceUnits.values()) {
                if (!persistenceUnit.getName().equals("cmp")){
                    // Found it
                    unit = persistenceUnit;
                    break;
                }
            }
        }


        // try again using the ref name
        if (unit == null){
            unit = persistenceUnits.resolveLink(ref.getName(), moduleURI);
        }

        // try again using the ref name with any prefix removed
        if (unit == null){
            String shortName = ref.getName().replaceFirst(".*/", "");
            unit = persistenceUnits.resolveLink(shortName, moduleURI);
        }

        if (unit != null){
            ref.setMappedName(unit.getId());
        } else {

            // ----------------------------------------------
            //  Nothing was found.  Let's try and figure out
            //  what went wrong and log a validation message
            // ----------------------------------------------

            String refType = "persistence";
            if (ref instanceof PersistenceContextRef){
                refType += "ContextRef";
            } else refType += "UnitRef";

            String refShortName = ref.getName();
            if (refShortName.matches(".*\\..*/.*")){
                refShortName = refShortName.replaceFirst(".*/", "");
            }

            List<String> availableUnits = new ArrayList<String>();
            for (PersistenceUnit persistenceUnit : persistenceUnits.values()) {
                availableUnits.add(persistenceUnit.getName());
            }

            Collections.sort(availableUnits);

            String unitName = ref.getPersistenceUnitName();

            if (availableUnits.size() == 0){
                // Print a sample persistence.xml using their data
                if (unitName == null){
                    unitName = refShortName;
                }
                validation.fail(componentName, refType + ".noPersistenceUnits", refShortName, unitName);
            } else if (ref.getPersistenceUnitName() == null && availableUnits.size() > 1) {
                // Print a correct example of unitName in a ref
                // DMB: Idea, the ability to set a default unit-name in openejb-jar.xml via a property
                String sampleUnitName = availableUnits.get(0);
                validation.fail(componentName, refType + ".noUnitName", refShortName, join(", ", availableUnits), sampleUnitName );
            } else {
                Collection<PersistenceUnit> vagueMatches = persistenceUnits.values(ref.getPersistenceUnitName());
                if (vagueMatches.size() != 0) {
                    // Print the full rootUrls

                    List<String> possibleUnits = new ArrayList<String>();
                    for (PersistenceUnit persistenceUnit : persistenceUnits.values()) {
                        URI unitURI = URI.create(persistenceUnit.getId());
                        unitURI = URISupport.relativize(moduleURI, unitURI);
                        possibleUnits.add(unitURI.toString());
                    }

                    Collections.sort(possibleUnits);

                    validation.fail(componentName, refType + ".vagueMatches", refShortName, unitName, possibleUnits.size(), join("\n", possibleUnits));
                } else {
                    validation.fail(componentName, refType + ".noMatches", refShortName, unitName, join(", ", availableUnits));
                }
            }
        }
        return unit;
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
                String destination = properties.getProperty("destination", properties.getProperty("destinationName"));
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
        for (WebModule webModule : appModule.getWebModules()) {
            String moduleId = appModule.getModuleId();
            for (MessageDestination destination : webModule.getWebApp().getMessageDestination()) {
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
                    if (ref.getMappedName() == null && ejbDeployment.getResourceLink(ref.getName()) == null) {
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

        for (WebModule webModule : appModule.getWebModules()) {
            URI moduleUri = URI.create(appModule.getModuleId());
            for (MessageDestinationRef ref : webModule.getWebApp().getMessageDestinationRef()) {
                String destinationId = resolveDestinationId(ref, moduleUri, destinationResolver, destinationTypes);
                if (destinationId != null) {
                    // for web modules we put the destinationId in the mapped name
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
                        logger.info("Auto-configuring a message driven bean " + ejbDeployment.getDeploymentId() + " destination " + properties.getProperty("destination") + " to be destinationType " + destinationType);
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

    private void deploy(ClientModule clientModule, AppResources appResources) throws OpenEJBException {
        processJndiRefs(clientModule.getModuleId(), clientModule.getApplicationClient(), appResources);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    private void deploy(ConnectorModule connectorModule) throws OpenEJBException {
        // Nothing to process for resource modules
    }

    private void deploy(WebModule webModule, AppResources appResources) throws OpenEJBException {
        processJndiRefs(webModule.getModuleId(), webModule.getWebApp(), appResources);
    }

    private void processJndiRefs(String moduleId, JndiConsumer jndiConsumer, AppResources appResources) throws OpenEJBException {
        // Resource reference
        for (ResourceRef ref : jndiConsumer.getResourceRef()) {
            // skip references such as URLs which are automatically handled by the server
            if (ignoredReferenceTypes.contains(ref.getType())) {
                continue;
            }

            // skip destinations with a global jndi name
            String mappedName = ref.getMappedName();
            if (mappedName == null) mappedName = "";
            if (mappedName.startsWith("jndi:")){
                continue;
            }

            String destinationId = (mappedName.length() == 0) ? ref.getName() : mappedName;
            destinationId = getResourceId(moduleId, destinationId, ref.getType(), appResources);
            ref.setMappedName(destinationId);
        }

        // Resource env reference
        for (JndiReference ref : jndiConsumer.getResourceEnvRef()) {
            // skip references such as URLs which are automatically handled by the server
            if (ignoredReferenceTypes.contains(ref.getType())) {
                continue;
            }

            // skip destinations with a global jndi name
            String mappedName = ref.getMappedName();
            if (mappedName == null) mappedName = "";
            if (mappedName.startsWith("jndi:")){
                continue;
            }

            String destinationId = (mappedName.length() == 0) ? ref.getName() : mappedName;
            destinationId = getResourceEnvId(moduleId, destinationId, ref.getType(), appResources);
            ref.setMappedName(destinationId);
        }

        // Message destination reference
        for (MessageDestinationRef ref : jndiConsumer.getMessageDestinationRef()) {
            // skip destinations with a global jndi name
            String mappedName = ref.getMappedName() + "";
            if (mappedName.startsWith("jndi:")){
                continue;
            }

            String destinationId = (mappedName.length() == 0) ? ref.getName() : mappedName;
            destinationId = getResourceEnvId(moduleId, destinationId, ref.getType(), appResources);
            ref.setMappedName(destinationId);
        }
    }

    private void deploy(EjbModule ejbModule, AppResources appResources) throws OpenEJBException {
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
                String containerId = getUsableContainer(containerInfoType, bean, appResources);
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
                processResourceRef(ref, ejbDeployment, appResources);
            }

            // Resource env reference
            for (JndiReference ref : bean.getResourceEnvRef()) {
                processResourceEnvRef(ref, ejbDeployment, appResources);
            }

            // Message destination reference
            for (MessageDestinationRef ref : bean.getMessageDestinationRef()) {
                processResourceEnvRef(ref, ejbDeployment, appResources);
            }


            // mdb message destination id
            if (autoCreateResources && bean instanceof MessageDrivenBean) {
                MessageDrivenBean mdb = (MessageDrivenBean) bean;

                ResourceLink resourceLink = ejbDeployment.getResourceLink("openejb/destination");
                if (resourceLink != null) {
                    try {
                        String destinationId = getResourceEnvId(bean.getEjbName(), resourceLink.getResId(), mdb.getMessageDestinationType(), appResources);
                        resourceLink.setResId(destinationId);
                    } catch (OpenEJBException e) {
                        // The MDB doesn't need the auto configured "openejb/destination" env entry
                        ejbDeployment.removeResourceLink("openejb/destination");
                    }
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
        logger.info("Auto-creating a container for bean " + ejbDeployment.getDeploymentId() + ": Container(type=" + getType(bean) + ", id=" + containerInfo.id + ")");

        // if the is an MDB container we need to resolve the resource adapter
        String resourceAdapterId = containerInfo.properties.getProperty("ResourceAdapter");
        if (resourceAdapterId != null) {
            String newResourceId = getResourceId(ejbDeployment.getDeploymentId(), resourceAdapterId, null, null);
            if (resourceAdapterId != newResourceId) {
                containerInfo.properties.setProperty("ResourceAdapter", newResourceId);
            }
        }

        // install the container
        configFactory.install(containerInfo);
        return containerInfo.id;
    }

    private void processResourceRef(ResourceRef ref, EjbDeployment ejbDeployment, AppResources appResources) throws OpenEJBException {
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
            id = getResourceId(ejbDeployment.getDeploymentId(), id, refType, appResources);
            logger.info("Auto-linking resource-ref '" + refName + "' in bean " + ejbDeployment.getDeploymentId() + " to Resource(id=" + id + ")");

            link = new ResourceLink();
            link.setResId(id);
            link.setResRefName(refName);
            ejbDeployment.addResourceLink(link);
        } else {
            String id = getResourceId(ejbDeployment.getDeploymentId(), link.getResId(), refType, appResources);
            link.setResId(id);
            link.setResRefName(refName);
        }
    }

    private void processResourceEnvRef(JndiReference ref, EjbDeployment ejbDeployment, AppResources appResources) throws OpenEJBException {
        // skip destinations with a global jndi name
        String mappedName = (ref.getMappedName() == null)? "": ref.getMappedName();
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
            id = getResourceEnvId(ejbDeployment.getDeploymentId(), id, refType, appResources);
            if (id == null) {
                // could be a session context ref
                return;
            }
            logger.info("Auto-linking resource-env-ref '" + refName + "' in bean " + ejbDeployment.getDeploymentId() + " to Resource(id=" + id + ")");

            link = new ResourceLink();
            link.setResId(id);
            link.setResRefName(refName);
            ejbDeployment.addResourceLink(link);
        } else {
            String id = getResourceEnvId(ejbDeployment.getDeploymentId(), link.getResId(), refType, appResources);
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
                return BeanTypes.CMP_ENTITY;
            } else {
                return BeanTypes.BMP_ENTITY;
            }
        } else if (enterpriseBean instanceof org.apache.openejb.jee.SessionBean) {
            if (((org.apache.openejb.jee.SessionBean) enterpriseBean).getSessionType() == SessionType.STATEFUL) {
                return BeanTypes.STATEFUL;
            } else {
                return BeanTypes.STATELESS;
            }
        } else if (enterpriseBean instanceof org.apache.openejb.jee.MessageDrivenBean) {
            return BeanTypes.MESSAGE;
        }
        throw new OpenEJBException("Unknown enterprise bean type " + enterpriseBean.getClass().getName());
    }

    private void deploy(PersistenceModule persistenceModule) throws OpenEJBException {
        if (!autoCreateResources) {
            return;
        }

        Persistence persistence = persistenceModule.getPersistence();
        for (PersistenceUnit persistenceUnit : persistence.getPersistenceUnit()) {
            String jtaDataSourceId = getResourceId(persistenceUnit.getName(), persistenceUnit.getJtaDataSource(), DataSource.class.getName(), null);
            if (jtaDataSourceId != null) {
                persistenceUnit.setJtaDataSource("java:openejb/Resource/" + jtaDataSourceId);
            }
            String nonJtaDataSourceId = getResourceId(persistenceUnit.getName(), persistenceUnit.getNonJtaDataSource(), DataSource.class.getName(), null);
            if (nonJtaDataSourceId != null) {
                persistenceUnit.setNonJtaDataSource("java:openejb/Resource/" + nonJtaDataSourceId);
            }
        }
    }

    private String getResourceId(String beanName, String resourceId, String type, AppResources appResources) throws OpenEJBException {
        if(resourceId == null){
            return null;
        }
        if (appResources == null) appResources = new AppResources();

        // skip references such as URL which are automatically handled by the server
        if (type != null && ignoredReferenceTypes.contains(type)) {
            return null;
        }

        // strip off "java:comp/env"
        if (resourceId.startsWith("java:comp/env")) {
            resourceId = resourceId.substring("java:comp/env".length());
        }

        // check for existing resource with specified resourceId
        List<String> resourceIds = new ArrayList<String>();
        resourceIds.addAll(appResources.getResourceIds(type));
        resourceIds.addAll(configFactory.getResourceIds(type));
        for (String id : resourceIds) {
            if (id.equalsIgnoreCase(resourceId)) return id;
        }

        // check for an existing resource using the short name (everything ever the final '/')
        String shortName = resourceId.replaceFirst(".*/", "");
        for (String id : resourceIds) {
            if (id.equalsIgnoreCase(shortName)) return id;
        }

        // expand search to any type -- may be asking for a reference to a sub-type
        List<String> allResourceIds = new ArrayList<String>();
        allResourceIds.addAll(appResources.getResourceIds(null));
        allResourceIds.addAll(configFactory.getResourceIds(null));
        for (String id : allResourceIds) {
            if (id.equalsIgnoreCase(resourceId)) return id;
        }
        for (String id : allResourceIds) {
            if (id.equalsIgnoreCase(shortName)) return id;
        }

        // throw an exception or log an error
        String message = "No existing resource found while attempting to Auto-link unmapped resource-ref '" + resourceId + "' of type '" + type  + "' for '" + beanName + "'.  Looked for Resource(id=" + resourceId + ") and Resource(id=" + shortName + ")";
        if (!autoCreateResources){
            throw new OpenEJBException(message);
        }
        logger.debug(message);

        // if there is a provider with the specified name. use it
        if (ServiceUtils.hasServiceProvider(resourceId)) {
            ResourceInfo resourceInfo = configFactory.configureService(resourceId, ResourceInfo.class);
            return installResource(beanName, resourceInfo);
        } else if (ServiceUtils.hasServiceProvider(shortName)) {
            ResourceInfo resourceInfo = configFactory.configureService(shortName, ResourceInfo.class);
            return installResource(beanName, resourceInfo);
        }

        // if there are any resources of the desired type, use the first one
        if (resourceIds.size() > 0) {
            return resourceIds.get(0);
        }

        // Auto create a resource using the first provider that can supply a resource of the desired type
        resourceId = ServiceUtils.getServiceProviderId(type);
        if (resourceId == null) {
            throw new OpenEJBException("No provider available for resource-ref '" + resourceId + "' of type '" + type + "' for '" + beanName + "'.");
        }
        ResourceInfo resourceInfo = configFactory.configureService(resourceId, ResourceInfo.class);

        logger.info("Auto-creating a resource with id '" + resourceInfo.id +  "' of type '" + type  + " for '" + beanName + "'.");
        return installResource(beanName, resourceInfo);
    }

    private String installResource(String beanName, ResourceInfo resourceInfo) throws OpenEJBException {
        String resourceAdapterId = resourceInfo.properties.getProperty("ResourceAdapter");
        if (resourceAdapterId != null) {
            String newResourceId = getResourceId(beanName, resourceAdapterId, null, null);
            if (resourceAdapterId != newResourceId) {
                resourceInfo.properties.setProperty("ResourceAdapter", newResourceId);
            }
        }

        configFactory.install(resourceInfo);
        return resourceInfo.id;
    }

    private String getResourceEnvId(String beanName, String resourceId, String type, AppResources appResources) throws OpenEJBException {
        if(resourceId == null){
            return null;
        }
        if (appResources == null) appResources = new AppResources();

        // skip references such as URLs which are automatically handled by the server
        if (ignoredReferenceTypes.contains(type)) {
            return null;
        }

        // strip off "java:comp/env"
        if (resourceId.startsWith("java:comp/env")) {
            resourceId = resourceId.substring("java:comp/env".length());
        }

        // check for existing resource with specified resourceId
        List<String> resourceEnvIds = new ArrayList<String>();
        resourceEnvIds.addAll(appResources.getResourceIds(type));
        resourceEnvIds.addAll(configFactory.getResourceIds(type));
        for (String id : resourceEnvIds) {
            if (id.equalsIgnoreCase(resourceId)) return id;
        }

        // throw an exception or log an error
        String message = "No existing resource found while attempting to Auto-link unmapped resource-env-ref '" + resourceId + "' of type '" + type  + "' for '" + beanName + "'.  Looked for Resource(id=" + resourceId + ")";
        if (!autoCreateResources){
            throw new OpenEJBException(message);
        }
        logger.debug(message);


        // Auto create a resource using the first provider that can supply a resource of the desired type
        String providerId = ServiceUtils.getServiceProviderId(type);
        if (providerId == null) {
            // if there are any existing resources of the desired type, use the first one
            if (resourceEnvIds.size() > 0) {
                return resourceEnvIds.get(0);
            }
            throw new OpenEJBException("No provider available for resource-env-ref '" + resourceId + "' of type '" + type + "' for '" + beanName + "'.");
        }

        Resource resource = new Resource(resourceId, providerId, null);
        resource.getProperties().setProperty("destination", resourceId);

        ResourceInfo resourceInfo = configFactory.configureService(resource, ResourceInfo.class);
        logger.info("Auto-creating a resource with id '" + resourceInfo.id +  "' of type '" + type  + " for '" + beanName + "'.");
        return installResource(beanName, resourceInfo);
    }

    private String getUsableContainer(Class<? extends ContainerInfo> containerInfoType, Object bean, AppResources appResources) {
        if (bean instanceof MessageDrivenBean) {
            MessageDrivenBean messageDrivenBean = (MessageDrivenBean) bean;
            String messagingType = messageDrivenBean.getMessagingType();
            List<String> containerIds = appResources.containerIdsByType.get(messagingType);
            if (containerIds != null && !containerIds.isEmpty()) {
                return containerIds.get(0);
            }
        }

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

    private static class AppResources {
        @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection"})
        private final Set<String> resourceAdapterIds = new TreeSet<String>();
        private final Map<String,List<String>> resourceIdsByType = new TreeMap<String,List<String>>();
        private final Map<String,List<String>> resourceEnvIdsByType = new TreeMap<String,List<String>>();
        private final Map<String,List<String>> containerIdsByType = new TreeMap<String,List<String>>();

        public AppResources() {
        }

        public AppResources(AppModule appModule) {

            //
            // DEVELOPERS NOTE:  if you change the id generation code here, you must change
            // the id generation code in ConfigurationFactory.configureApplication(AppModule appModule)
            //

            for (ConnectorModule connectorModule : appModule.getResourceModules()) {
                Connector connector = connectorModule.getConnector();

                ResourceAdapter resourceAdapter = connector.getResourceAdapter();
                if (resourceAdapter.getResourceAdapterClass() != null) {
                    String resourceAdapterId;
                    if (resourceAdapter.getId() != null) {
                        resourceAdapterId = resourceAdapter.getId();
                    } else {
                        resourceAdapterId = connectorModule.getModuleId() + "RA";
                    }
                    resourceAdapterIds.add(resourceAdapterId);
                }

                OutboundResourceAdapter outbound = resourceAdapter.getOutboundResourceAdapter();
                if (outbound != null) {
                    for (ConnectionDefinition connection : outbound.getConnectionDefinition()) {
                        String type = connection.getConnectionFactoryInterface();

                        String resourceId;
                        if (connection.getId() != null) {
                            resourceId = connection.getId();
                        } else if (outbound.getConnectionDefinition().size() == 1) {
                            resourceId = connectorModule.getModuleId();
                        } else {
                            resourceId = connectorModule.getModuleId() + "-" + type;
                        }

                        List<String> resourceIds = resourceIdsByType.get(type);
                        if (resourceIds == null) {
                            resourceIds = new ArrayList<String>();
                            resourceIdsByType.put(type, resourceIds);
                        }
                        resourceIds.add(resourceId);
                    }
                }

                InboundResource inbound = resourceAdapter.getInboundResourceAdapter();
                if (inbound != null) {
                    for (MessageListener messageListener : inbound.getMessageAdapter().getMessageListener()) {
                        String type = messageListener.getMessageListenerType();

                        String containerId;
                        if (messageListener.getId() != null) {
                            containerId = messageListener.getId();
                        } else if (inbound.getMessageAdapter().getMessageListener().size() == 1) {
                            containerId = connectorModule.getModuleId();
                        } else {
                            containerId = connectorModule.getModuleId() + "-" + type;
                        }

                        List<String> containerIds = containerIdsByType.get(type);
                        if (containerIds == null) {
                            containerIds = new ArrayList<String>();
                            containerIdsByType.put(type, containerIds);
                        }
                        containerIds.add(containerId);
                    }
                }

                for (AdminObject adminObject : resourceAdapter.getAdminObject()) {
                    String type = adminObject.getAdminObjectInterface();

                    String resourceEnvId;
                    if (adminObject.getId() != null) {
                        resourceEnvId = adminObject.getId();
                    } else if (resourceAdapter.getAdminObject().size() == 1) {
                        resourceEnvId = connectorModule.getModuleId();
                    } else {
                        resourceEnvId = connectorModule.getModuleId() + "-" + type;
                    }

                    List<String> resourceEnvIds = resourceEnvIdsByType.get(type);
                    if (resourceEnvIds == null) {
                        resourceEnvIds = new ArrayList<String>();
                        resourceEnvIdsByType.put(type, resourceEnvIds);
                    }
                    resourceEnvIds.add(resourceEnvId);
                }
            }

        }

        public List<String> getResourceIds(String type) {
            if (type == null) {
                List<String> allResourceIds = new ArrayList<String>();
                for (List<String> resourceIds : resourceIdsByType.values()) {
                    allResourceIds.addAll(resourceIds);
                }
                return allResourceIds;
            }

            List<String> resourceIds = resourceIdsByType.get(type);
            if (resourceIds != null) {
                return resourceIds;
            }
            return Collections.emptyList();
        }

        public List<String> getResourceEnvIds(String type) {
            if (type != null) {
                List<String> resourceIds = resourceEnvIdsByType.get(type);
                if (resourceIds != null) {
                    return resourceIds;
                }
            }
            return Collections.emptyList();
        }
    }
}
