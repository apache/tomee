/*
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

package org.apache.openejb.config;

import org.apache.openejb.JndiConstants;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.assembler.classic.ContainerInfo;
import org.apache.openejb.assembler.classic.MdbContainerInfo;
import org.apache.openejb.assembler.classic.ResourceInfo;
import org.apache.openejb.config.sys.Resource;
import org.apache.openejb.jee.ActivationConfig;
import org.apache.openejb.jee.ActivationConfigProperty;
import org.apache.openejb.jee.AdminObject;
import org.apache.openejb.jee.AssemblyDescriptor;
import org.apache.openejb.jee.ConnectionDefinition;
import org.apache.openejb.jee.Connector;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.EntityBean;
import org.apache.openejb.jee.InboundResourceadapter;
import org.apache.openejb.jee.InjectionTarget;
import org.apache.openejb.jee.JndiConsumer;
import org.apache.openejb.jee.JndiReference;
import org.apache.openejb.jee.MessageDestination;
import org.apache.openejb.jee.MessageDestinationRef;
import org.apache.openejb.jee.MessageDrivenBean;
import org.apache.openejb.jee.MessageListener;
import org.apache.openejb.jee.OutboundResourceAdapter;
import org.apache.openejb.jee.PersistenceContextRef;
import org.apache.openejb.jee.PersistenceRef;
import org.apache.openejb.jee.PersistenceType;
import org.apache.openejb.jee.ResourceAdapter;
import org.apache.openejb.jee.ResourceRef;
import org.apache.openejb.jee.SessionBean;
import org.apache.openejb.jee.SessionType;
import org.apache.openejb.jee.jpa.unit.Persistence;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.apache.openejb.jee.jpa.unit.TransactionType;
import org.apache.openejb.jee.oejb3.EjbDeployment;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.openejb.jee.oejb3.ResourceLink;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.resource.jdbc.DataSourceFactory;
import org.apache.openejb.util.IntrospectionSupport;
import org.apache.openejb.util.Join;
import org.apache.openejb.util.LinkResolver;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.PropertyPlaceHolderHelper;
import org.apache.openejb.util.SuperProperties;
import org.apache.openejb.util.URISupport;
import org.apache.openejb.util.URLs;

import jakarta.annotation.ManagedBean;
import jakarta.ejb.TimerService;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.jms.Queue;
import jakarta.jms.Topic;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.TransactionSynchronizationRegistry;
import jakarta.transaction.UserTransaction;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Providers;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import static java.util.Arrays.asList;

public class AutoConfig implements DynamicDeployer, JndiConstants {

    public static final String ORIGIN_ANNOTATION = "Annotation";
    public static final String ORIGIN_FLAG = "Origin";
    public static final String ORIGINAL_ID = "OriginalId";

    private static final AppResources EMPTY_APP_RESOURCES = new AppResources();

    public static Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP_CONFIG, AutoConfig.class);

    private static final int MAX_IMPLICIT_POOL_SIZE = 5;

    private static final Set<String> ignoredReferenceTypes = new TreeSet<String>();
    public static final String AUTOCREATE_JTA_DATASOURCE_FROM_NON_JTA_ONE_KEY = "openejb.autocreate.jta-datasource-from-non-jta-one";

    static {
        // Context objects are automatically handled
        ignoredReferenceTypes.add("jakarta.ejb.SessionContext");
        ignoredReferenceTypes.add("jakarta.ejb.EntityContext");
        ignoredReferenceTypes.add("jakarta.ejb.MessageDrivenContext");
        ignoredReferenceTypes.add("jakarta.ejb.EJBContext");
        ignoredReferenceTypes.add("jakarta.xml.ws.WebServiceContext");
        // URLs are automatically handled
        ignoredReferenceTypes.add("java.net.URL");
        // User transaction is automatically handled
        ignoredReferenceTypes.add(UserTransaction.class.getName());
        ignoredReferenceTypes.add(TransactionManager.class.getName());
        ignoredReferenceTypes.add(TransactionSynchronizationRegistry.class.getName());
        ignoredReferenceTypes.add(TimerService.class.getName());
        // Bean Validation is automatically handled
        ignoredReferenceTypes.add(Validator.class.getName());
        ignoredReferenceTypes.add(ValidatorFactory.class.getName());
        // CDI BeanManager is handled
        ignoredReferenceTypes.add(BeanManager.class.getName());
        // REST injections done via @Context and bound into a resource env...
        ignoredReferenceTypes.add(Request.class.getName());
        ignoredReferenceTypes.add(UriInfo.class.getName());
        ignoredReferenceTypes.add(HttpHeaders.class.getName());
        ignoredReferenceTypes.add(SecurityContext.class.getName());
        ignoredReferenceTypes.add(ContextResolver.class.getName());
        ignoredReferenceTypes.add(Application.class.getName());
        ignoredReferenceTypes.add(Providers.class.getName());
        ignoredReferenceTypes.add(ServletRequest.class.getName());
        ignoredReferenceTypes.add(HttpServletRequest.class.getName());
        ignoredReferenceTypes.add(ServletConfig.class.getName());
        ignoredReferenceTypes.add(ServletContext.class.getName());
        ignoredReferenceTypes.add(HttpServletResponse.class.getName());
    }

    private final ConfigurationFactory configFactory;
    private boolean autoCreateContainers = true;
    private boolean autoCreateResources = true;

    public AutoConfig(final ConfigurationFactory configFactory) {
        this.configFactory = configFactory;
    }

    public synchronized boolean autoCreateResources() {
        return autoCreateResources;
    }

    public synchronized void autoCreateResources(final boolean autoCreateResources) {
        this.autoCreateResources = autoCreateResources;
    }

    public synchronized boolean autoCreateContainers() {
        return autoCreateContainers;
    }

    public synchronized void autoCreateContainers(final boolean autoCreateContainers) {
        this.autoCreateContainers = autoCreateContainers;
    }

    public void init() throws OpenEJBException {
    }

    @Override
    public synchronized AppModule deploy(final AppModule appModule) throws OpenEJBException {
        final List<ContainerInfo> containerInfos = ContainerUtils.getContainerInfos(appModule, configFactory);
        final AppResources appResources = new AppResources(appModule, containerInfos);

        appResources.dump();

        processApplicationResources(appModule);
        for (final EjbModule ejbModule : appModule.getEjbModules()) {
            processActivationConfig(ejbModule);
        }
        resolveDestinationLinks(appModule);

        resolvePersistenceRefs(appModule);

        for (final EjbModule ejbModule : appModule.getEjbModules()) {
            deploy(ejbModule, appResources);
        }
        for (final ClientModule clientModule : appModule.getClientModules()) {
            deploy(clientModule, appResources);
        }
        for (final WebModule webModule : appModule.getWebModules()) {
            deploy(webModule, appResources);
        }
        for (final PersistenceModule persistenceModule : appModule.getPersistenceModules()) {
            deploy(appModule, persistenceModule);
        }
        // Note that there is nothing to process for resource modules.
        // We dont need to loop over "appModule.getConnectorModules()".
        return appModule;
    }

    private void resolvePersistenceRefs(final AppModule appModule) {
        final LinkResolver<PersistenceUnit> persistenceUnits = new PersistenceUnitLinkResolver(appModule);

        for (final PersistenceModule module : appModule.getPersistenceModules()) {
            final String rootUrl = module.getRootUrl();
            for (final PersistenceUnit unit : module.getPersistence().getPersistenceUnit()) {
                unit.setId(appModule.persistenceUnitId(rootUrl, unit.getName()));
                persistenceUnits.add(rootUrl, unit.getName(), unit);
            }
        }

        for (final EjbModule ejbModule : appModule.getEjbModules()) {
            final URI moduleURI = ejbModule.getModuleUri();

            for (final JndiConsumer component : ejbModule.getEjbJar().getEnterpriseBeans()) {
                processPersistenceRefs(component, ejbModule, persistenceUnits, moduleURI);
            }

        }

        for (final ClientModule clientModule : appModule.getClientModules()) {
            final URI moduleURI = URLs.uri(clientModule.getModuleId());
            processPersistenceRefs(clientModule.getApplicationClient(), clientModule, persistenceUnits, moduleURI);
        }

        for (final WebModule webModule : appModule.getWebModules()) {
            final URI moduleURI = URLs.uri(webModule.getModuleId());
            processPersistenceRefs(webModule.getWebApp(), webModule, persistenceUnits, moduleURI);
        }
    }

    private void processPersistenceRefs(final JndiConsumer component, final DeploymentModule module, final LinkResolver<PersistenceUnit> persistenceUnits, final URI moduleURI) {

        final String componentName = component.getJndiConsumerName();
        final ValidationContext validation = module.getValidation();
        for (final PersistenceRef ref : component.getPersistenceUnitRef()) {

            processPersistenceRef(persistenceUnits, ref, moduleURI, componentName, validation);
        }
        for (final PersistenceRef ref : component.getPersistenceContextRef()) {

            processPersistenceRef(persistenceUnits, ref, moduleURI, componentName, validation);
        }
    }

    private PersistenceUnit processPersistenceRef(final LinkResolver<PersistenceUnit> persistenceUnits,
                                                  final PersistenceRef ref,
                                                  final URI moduleURI,
                                                  final String componentName,
                                                  final ValidationContext validation) {

        if (ref.getMappedName() != null && ref.getMappedName().startsWith("jndi:")) {
            return null;
        }

        PersistenceUnit unit = persistenceUnits.resolveLink(ref.getPersistenceUnitName(), moduleURI);

        // Explicitly check if we messed up the "if there's only one,
        // that's what you get" rule by adding our "cmp" unit.
        final Collection<PersistenceUnit> cmpUnits = persistenceUnits.values("cmp");
        if (unit == null && cmpUnits.size() > 0 && persistenceUnits.values().size() - cmpUnits.size() == 1) {
            // We did, there is exactly one non-cmp unit.  Let's find it.
            for (final PersistenceUnit persistenceUnit : persistenceUnits.values()) {
                if (!persistenceUnit.getName().equals("cmp")) {
                    // Found it
                    unit = persistenceUnit;
                    break;
                }
            }
        }

        // try again using the ref name
        if (unit == null) {
            unit = persistenceUnits.resolveLink(ref.getName(), moduleURI);
        }

        // try again using the ref name with any prefix removed
        if (unit == null) {
            final String shortName = ref.getName().replaceFirst(".*/", "");
            unit = persistenceUnits.resolveLink(shortName, moduleURI);
        }

        if (unit != null) {
            ref.setPersistenceUnitName(unit.getName());
            ref.setMappedName(unit.getId());
        } else {

            // ----------------------------------------------
            //  Nothing was found.  Let's try and figure out
            //  what went wrong and log a validation message
            // ----------------------------------------------

            String refType = "persistence";
            if (ref instanceof PersistenceContextRef) {
                refType += "ContextRef";
            } else {
                refType += "UnitRef";
            }

            String refShortName = ref.getName();
            if (refShortName.matches(".*\\..*/.*")) {
                refShortName = refShortName.replaceFirst(".*/", "");
            }

            final List<String> availableUnits = new ArrayList<>();
            for (final PersistenceUnit persistenceUnit : persistenceUnits.values()) {
                availableUnits.add(persistenceUnit.getName());
            }

            Collections.sort(availableUnits);

            String unitName = ref.getPersistenceUnitName();

            if (availableUnits.size() == 0) {
                // Print a sample persistence.xml using their data
                if (unitName == null) {
                    unitName = refShortName;
                }
                validation.fail(componentName, refType + ".noPersistenceUnits", refShortName, unitName);
            } else if ((ref.getPersistenceUnitName() == null || ref.getPersistenceUnitName().length() == 0) && availableUnits.size() > 1) {
                // Print a correct example of unitName in a ref
                // DMB: Idea, the ability to set a default unit-name in openejb-jar.xml via a property
                final String sampleUnitName = availableUnits.get(0);
                validation.fail(componentName, refType + ".noUnitName", refShortName, Join.join(", ", availableUnits), sampleUnitName);
            } else {
                final Collection<PersistenceUnit> vagueMatches = persistenceUnits.values(ref.getPersistenceUnitName());
                if (vagueMatches.size() != 0) {
                    // Print the full rootUrls

                    final List<String> possibleUnits = new ArrayList<>();
                    for (final PersistenceUnit persistenceUnit : persistenceUnits.values()) {
                        try {
                            URI unitURI = URLs.uri(persistenceUnit.getId());
                            unitURI = URISupport.relativize(moduleURI, unitURI);
                            possibleUnits.add(unitURI.toString());
                        } catch (final Exception e) {
                            // id is typically not a valid URI
                            possibleUnits.add(persistenceUnit.getId());
                        }
                    }

                    Collections.sort(possibleUnits);

                    validation.fail(componentName, refType + ".vagueMatches", refShortName, unitName, possibleUnits.size(), Join.join("\n", possibleUnits));
                } else {
                    validation.fail(componentName, refType + ".noMatches", refShortName, unitName, Join.join(", ", availableUnits));
                }
            }
        }
        return unit;
    }

    /**
     * Set destination, destinationType, clientId and subscriptionName in the MDB activation config.
     */
    private void processActivationConfig(final EjbModule ejbModule) throws OpenEJBException {
        final OpenejbJar openejbJar;
        if (ejbModule.getOpenejbJar() != null) {
            openejbJar = ejbModule.getOpenejbJar();
        } else {
            openejbJar = new OpenejbJar();
            ejbModule.setOpenejbJar(openejbJar);
        }

        final Map<String, EjbDeployment> deployments = openejbJar.getDeploymentsByEjbName();

        for (final EnterpriseBean bean : ejbModule.getEjbJar().getEnterpriseBeans()) {
            if (bean instanceof MessageDrivenBean) {
                final MessageDrivenBean mdb = (MessageDrivenBean) bean;

                if (mdb.getActivationConfig() == null) {
                    mdb.setActivationConfig(new ActivationConfig());
                }

                if (!isJms(mdb)) {
                    continue;
                }

                final EjbDeployment ejbDeployment = deployments.get(bean.getEjbName());
                if (ejbDeployment == null) {
                    throw new OpenEJBException("No ejb deployment found for ejb " + bean.getEjbName());
                }

                final Properties properties = mdb.getActivationConfig().toProperties();

                String destination = properties.getProperty("destinationName", properties.getProperty("destinationLookup"));

                if (destination != null) {
                    if (destination.startsWith("openejb:Resource/")) {
                        destination = destination.substring("openejb:Resource/".length());
                    }
                    if (destination.startsWith("java:openejb/Resource/")) {
                        destination = destination.substring("java:openejb/Resource/".length());
                    }

                    mdb.getActivationConfig().addProperty("destination", destination);

                    // Remove destinationName as it is not in the standard ActivationSpec 
                    final List<ActivationConfigProperty> list = mdb.getActivationConfig().getActivationConfigProperty();
                    final Iterator<ActivationConfigProperty> iterator = list.iterator();
                    while (iterator.hasNext()) {
                        final ActivationConfigProperty configProperty = iterator.next();
                        final String activationConfigPropertyName = configProperty.getActivationConfigPropertyName();
                        if (activationConfigPropertyName.equals("destinationName")
                                || activationConfigPropertyName.equals("destinationLookup")) {
                            iterator.remove();
                            break; // we suppose we have only one of both we should be the case
                        }
                    }
                } else {
                    destination = properties.getProperty("destination");
                }

                if (destination == null) { // EE 7/EJB 3.2
                    destination = properties.getProperty("destinationLookup");
                }

                // destination
                //                String destination = properties.getProperty("destination", properties.getProperty("destinationName"));
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
                if ("jakarta.jms.Topic".equals(destinationType)) {
                    if (Boolean.parseBoolean(
                            SystemInstance.get().getProperty(
                                    "openejb.activemq.deploymentId-as-clientId",
                                    ejbModule.getProperties().getProperty("openejb.activemq.deploymentId-as-clientId", "true")))
                            && !properties.containsKey("clientId")) {
                        mdb.getActivationConfig().addProperty("clientId", ejbDeployment.getDeploymentId());
                    }
                    if (!properties.containsKey("subscriptionName")) {
                        mdb.getActivationConfig().addProperty("subscriptionName", ejbDeployment.getDeploymentId() + "_subscription");
                    }
                }

            }
        }
    }

    private boolean isJms(final MessageDrivenBean mdb) {
        final String messagingType = mdb.getMessagingType();
        return messagingType != null && messagingType.startsWith("jakarta.jms");
    }

    /**
     * Set resource id in all message-destination-refs and MDBs that are using message destination links.
     */
    private void resolveDestinationLinks(final AppModule appModule) throws OpenEJBException {
        // build up a link resolver
        final LinkResolver<MessageDestination> destinationResolver = new LinkResolver<>();
        for (final EjbModule ejbModule : appModule.getEjbModules()) {
            final AssemblyDescriptor assembly = ejbModule.getEjbJar().getAssemblyDescriptor();
            if (assembly != null) {
                for (final MessageDestination destination : assembly.getMessageDestination()) {
                    destinationResolver.add(ejbModule.getModuleUri(), destination.getMessageDestinationName(), destination);
                }
            }
        }
        for (final ClientModule clientModule : appModule.getClientModules()) {
            for (final MessageDestination destination : clientModule.getApplicationClient().getMessageDestination()) {
                destinationResolver.add(appModule.getModuleUri(), destination.getMessageDestinationName(), destination);
            }
        }
        for (final WebModule webModule : appModule.getWebModules()) {
            for (final MessageDestination destination : webModule.getWebApp().getMessageDestination()) {
                destinationResolver.add(appModule.getModuleUri(), destination.getMessageDestinationName(), destination);
            }
        }

        // remember the type of each destination so we can use it to fillin MDBs that don't declare destination type
        final Map<MessageDestination, String> destinationTypes = new HashMap<>();

        // resolve all MDBs with destination links
        // if MessageDestination does not have a mapped name assigned, give it the destination from the MDB
        for (final EjbModule ejbModule : appModule.getEjbModules()) {
            final AssemblyDescriptor assembly = ejbModule.getEjbJar().getAssemblyDescriptor();
            if (assembly == null) {
                continue;
            }

            final URI moduleUri = ejbModule.getModuleUri();
            final OpenejbJar openejbJar = ejbModule.getOpenejbJar();

            for (final EnterpriseBean bean : ejbModule.getEjbJar().getEnterpriseBeans()) {
                // MDB destination is deploymentId if none set
                if (bean instanceof MessageDrivenBean) {
                    final MessageDrivenBean mdb = (MessageDrivenBean) bean;

                    final EjbDeployment ejbDeployment = openejbJar.getDeploymentsByEjbName().get(bean.getEjbName());
                    if (ejbDeployment == null) {
                        throw new OpenEJBException("No ejb deployment found for ejb " + bean.getEjbName());
                    }

                    // skip destination refs without a destination link
                    final String link = mdb.getMessageDestinationLink();
                    if (link == null || link.length() == 0) {
                        continue;
                    }

                    // resolve the destination... if we don't find one it is a configuration bug
                    final MessageDestination destination = destinationResolver.resolveLink(link, moduleUri);
                    if (destination == null) {
                        throw new OpenEJBException("Message destination " + link + " for message driven bean " + mdb.getEjbName() + " not found");
                    }

                    // get the destinationId is the mapped name
                    String destinationId = destination.getMappedName();
                    if (destinationId == null) {
                        // if we don't have a mapped name use the destination of the mdb
                        final Properties properties = mdb.getActivationConfig().toProperties();
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
        for (final EjbModule ejbModule : appModule.getEjbModules()) {
            final AssemblyDescriptor assembly = ejbModule.getEjbJar().getAssemblyDescriptor();
            if (assembly == null) {
                continue;
            }

            final URI moduleUri = ejbModule.getModuleUri();
            final OpenejbJar openejbJar = ejbModule.getOpenejbJar();

            for (final EnterpriseBean bean : ejbModule.getEjbJar().getEnterpriseBeans()) {
                final EjbDeployment ejbDeployment = openejbJar.getDeploymentsByEjbName().get(bean.getEjbName());
                if (ejbDeployment == null) {
                    throw new OpenEJBException("No ejb deployment found for ejb " + bean.getEjbName());
                }

                for (final MessageDestinationRef ref : bean.getMessageDestinationRef()) {
                    // skip destination refs with a resource link already assigned
                    if (ref.getMappedName() == null && ejbDeployment.getResourceLink(ref.getName()) == null) {
                        final String destinationId = resolveDestinationId(ref, appModule, moduleUri, destinationResolver, destinationTypes);
                        if (destinationId != null) {
                            // build the link and add it
                            final ResourceLink resourceLink = new ResourceLink();
                            resourceLink.setResId(destinationId);
                            resourceLink.setResRefName(ref.getName());
                            ejbDeployment.addResourceLink(resourceLink);
                        }

                    }
                }
            }
        }

        for (final ClientModule clientModule : appModule.getClientModules()) {
            final URI moduleUri = clientModule.getModuleUri();
            for (final MessageDestinationRef ref : clientModule.getApplicationClient().getMessageDestinationRef()) {
                final String destinationId = resolveDestinationId(ref, appModule, moduleUri, destinationResolver, destinationTypes);
                if (destinationId != null) {
                    // for client modules we put the destinationId in the mapped name
                    ref.setMappedName(destinationId);
                }
            }
        }

        for (final WebModule webModule : appModule.getWebModules()) {
            final URI moduleUri = URLs.uri(webModule.getModuleId());
            for (final MessageDestinationRef ref : webModule.getWebApp().getMessageDestinationRef()) {
                final String destinationId = resolveDestinationId(ref, appModule, moduleUri, destinationResolver, destinationTypes);
                if (destinationId != null) {
                    // for web modules we put the destinationId in the mapped name
                    ref.setMappedName(destinationId);
                }
            }
        }

        // Process MDBs one more time...
        // this time fill in the destination type (if not alreday specified) with
        // the info from the destination (which got filled in from the references)
        for (final EjbModule ejbModule : appModule.getEjbModules()) {
            final AssemblyDescriptor assembly = ejbModule.getEjbJar().getAssemblyDescriptor();
            if (assembly == null) {
                continue;
            }

            final URI moduleUri = URLs.uri(ejbModule.getModuleId());
            final OpenejbJar openejbJar = ejbModule.getOpenejbJar();

            for (final EnterpriseBean bean : ejbModule.getEjbJar().getEnterpriseBeans()) {
                // MDB destination is deploymentId if none set
                if (bean instanceof MessageDrivenBean) {
                    final MessageDrivenBean mdb = (MessageDrivenBean) bean;

                    if (!isJms(mdb)) {
                        continue;
                    }

                    final EjbDeployment ejbDeployment = openejbJar.getDeploymentsByEjbName().get(bean.getEjbName());
                    if (ejbDeployment == null) {
                        throw new OpenEJBException("No ejb deployment found for ejb " + bean.getEjbName());
                    }

                    // if destination type is already set in, continue
                    String destinationType = mdb.getMessageDestinationType();
                    if (destinationType != null) {
                        continue;
                    }

                    final String link = mdb.getMessageDestinationLink();
                    if (link != null && link.length() != 0) {
                        // resolve the destination... if we don't find one it is a configuration bug
                        final MessageDestination destination = destinationResolver.resolveLink(link, moduleUri);
                        if (destination == null) {
                            throw new OpenEJBException("Message destination " + link + " for message driven bean " + mdb.getEjbName() + " not found");
                        }
                        destinationType = destinationTypes.get(destination);
                    }

                    if (destinationType == null) {
                        // couldn't determine type... we'll have to guess

                        // if destination name contains the string "queue" or "topic" we use that
                        final Properties properties = mdb.getActivationConfig().toProperties();
                        final String destination = properties.getProperty("destination").toLowerCase();
                        if (destination.contains("queue")) {
                            destinationType = Queue.class.getName();
                        } else if (destination.contains("topic")) {
                            destinationType = Topic.class.getName();
                        } else {
                            // Queue is the default
                            destinationType = Queue.class.getName();
                        }
                        logger.info("Auto-configuring a message driven bean " +
                            ejbDeployment.getDeploymentId() +
                            " destination " +
                            properties.getProperty("destination") +
                            " to be destinationType " +
                            destinationType);
                    }

                    if (destinationType != null) {
                        mdb.getActivationConfig().addProperty("destinationType", destinationType);
                        mdb.setMessageDestinationType(destinationType);

                        // topics need a clientId and subscriptionName
                        if ("jakarta.jms.Topic".equals(destinationType)) {
                            final Properties properties = mdb.getActivationConfig().toProperties();
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

    private String resolveDestinationId(final MessageDestinationRef ref,
                                        AppModule appModule, final URI moduleUri,
                                        final LinkResolver<MessageDestination> destinationResolver,
                                        final Map<MessageDestination, String> destinationTypes) throws OpenEJBException {
        // skip destination refs without a destination link
        final String link = ref.getMessageDestinationLink();
        if (link == null || link.length() == 0) {
            return null;
        }

        // resolve the destination... if we don't find one it is a configuration bug
        MessageDestination destination = destinationResolver.resolveLink(link, moduleUri);

        if (destination == null && link.contains("#")) {
            // try the app module URI + "/" + link instead

            final List<EjbModule> ejbModules = appModule.getEjbModules();
            for (final EjbModule ejbModule : ejbModules) {
                final String shortModuleName = link.substring(0, link.indexOf("#"));
                if (ejbModule.getModuleUri().toString().endsWith(shortModuleName)) {
                    final String appModuleLink = ejbModule.getModuleUri() + "#" + link.substring(link.indexOf("#") + 1);
                    destination = destinationResolver.resolveLink(appModuleLink, moduleUri);

                    if (destination != null) {
                        break;
                    }
                }
            }
        }

        if (destination == null) {
            throw new OpenEJBException("Message destination " + link + " for message-destination-ref " + ref.getMessageDestinationRefName() + " not found");
        }

        // remember the type of each destination so we can use it to fillin MDBs that don't declare destination type
        if (ref.getMessageDestinationType() != null && !destinationTypes.containsKey(destination)) {
            destinationTypes.put(destination, ref.getMessageDestinationType());
        }

        // get the destinationId
        final String destinationId = destination.getMappedName();
        if (destinationId == null) {
            destination.getMessageDestinationName();
        }
        return destinationId;
    }

    private void deploy(final ClientModule clientModule, final AppResources appResources) throws OpenEJBException {
        processJndiRefs(clientModule.getModuleId(), clientModule.getApplicationClient(), appResources, clientModule.getClassLoader());
    }

    private void deploy(final WebModule webModule, final AppResources appResources) throws OpenEJBException {
        processJndiRefs(webModule.getModuleId(), webModule.getWebApp(), appResources, webModule.getClassLoader());
    }

    private void processJndiRefs(final String moduleId, final JndiConsumer jndiConsumer, final AppResources appResources, final ClassLoader classLoader) throws OpenEJBException {
        // Resource reference
        for (final ResourceRef ref : jndiConsumer.getResourceRef()) {
            // skip destinations with lookup name
            if (ref.getLookupName() != null) {
                continue;
            }

            // skip destinations with a global jndi name
            final String mappedName = ref.getMappedName() == null ? "" : ref.getMappedName();
            if (mappedName.startsWith("jndi:")) {
                continue;
            }

            final String refType = getType(ref, classLoader);

            // skip references such as URLs which are automatically handled by the server
            if (isIgnoredReferenceType(refType, classLoader)) {
                continue;
            }

            String destinationId = mappedName.length() == 0 ? ref.getName() : mappedName;
            try {
                destinationId = getResourceId(moduleId, destinationId, refType, appResources);
            } catch (final OpenEJBException ex) {
                if (!(ref instanceof ContextRef)) {
                    throw ex;
                } else { // let jaxrs provider manage it
                    continue;
                }
            }
            ref.setMappedName(destinationId);
        }

        // Resource env reference
        for (final JndiReference ref : jndiConsumer.getResourceEnvRef()) {
            // skip destinations with lookup name
            if (ref.getLookupName() != null) {
                continue;
            }

            // skip destinations with a global jndi name
            final String mappedName = ref.getMappedName() == null ? "" : ref.getMappedName();
            if (mappedName.startsWith("jndi:")) {
                continue;
            }

            final String refType = getType(ref, classLoader);

            // skip references such as URLs which are automatically handled by the server
            if (isIgnoredReferenceType(refType, classLoader)) {
                continue;
            }

            String destinationId = mappedName.length() == 0 ? ref.getName() : mappedName;
            destinationId = getResourceEnvId(moduleId, destinationId, refType, appResources);
            ref.setMappedName(destinationId);
        }

        // Message destination reference
        for (final MessageDestinationRef ref : jndiConsumer.getMessageDestinationRef()) {
            // skip destinations with lookup name
            if (ref.getLookupName() != null) {
                continue;
            }

            // skip destinations with a global jndi name
            final String mappedName = ref.getMappedName() == null ? "" : ref.getMappedName();
            if (mappedName.startsWith("jndi:")) {
                continue;
            }

            String destinationId = mappedName.length() == 0 ? ref.getName() : mappedName;
            destinationId = getResourceEnvId(moduleId, destinationId, ref.getType(), appResources);
            ref.setMappedName(destinationId);
        }
    }

    private boolean isIgnoredReferenceType(final String typeName, final ClassLoader loader) {
        if (ignoredReferenceTypes.contains(typeName)) {
            return true;
        } else if (loader != null) {
            try {
                final Class<?> type = loader.loadClass(typeName);
                return type.isAnnotationPresent(ManagedBean.class);
            } catch (final ClassNotFoundException e) {
                // ignore
            }
        }
        return false;
    }

    private void deploy(final EjbModule ejbModule, final AppResources appResources) throws OpenEJBException {
        final OpenejbJar openejbJar;
        if (ejbModule.getOpenejbJar() != null) {
            openejbJar = ejbModule.getOpenejbJar();
        } else {
            openejbJar = new OpenejbJar();
            ejbModule.setOpenejbJar(openejbJar);
        }

        final Map<String, EjbDeployment> deployments = openejbJar.getDeploymentsByEjbName();

        for (final EnterpriseBean bean : ejbModule.getEjbJar().getEnterpriseBeans()) {
            final EjbDeployment ejbDeployment = deployments.get(bean.getEjbName());
            if (ejbDeployment == null) {
                throw new OpenEJBException("No ejb deployment found for ejb " + bean.getEjbName());
            }

            final String beanType = getType(bean);
            final Class<? extends ContainerInfo> containerInfoType = ConfigurationFactory.getContainerInfoType(beanType);
            logger.debug("Bean type of bean {0} is {1}", bean.getEjbName(), beanType);

            if (ejbDeployment.getContainerId() == null && !skipMdb(bean)) {
                logger.debug("Container for bean {0} is not set, looking for a suitable container", bean.getEjbName());

                String containerId = getUsableContainer(containerInfoType, bean, appResources);
                if (containerId == null) {
                    logger.debug("Suitable container for bean {0} not found, creating one", bean.getEjbName());
                    containerId = createContainer(containerInfoType, ejbDeployment, bean);
                }

                logger.debug("Setting container ID {0} for bean {1}", containerId, bean.getEjbName());
                ejbDeployment.setContainerId(containerId);
            }

            logger.debug("Container ID for bean {0} is {1}", bean.getEjbName(), ejbDeployment.getContainerId());

            // create the container if it doesn't exist
            final List<String> containerIds = configFactory.getContainerIds();

            final Collection<ContainerInfo> containerInfos = appResources.getContainerInfos();
            for (final ContainerInfo containerInfo : containerInfos) {
                containerIds.add(containerInfo.id);
            }

            if (!containerIds.contains(ejbDeployment.getContainerId()) && !skipMdb(bean)) {
                logger.debug("Desired container {0} not found. Containers available: {1}. Creating a new container.",
                        ejbDeployment.getContainerId(),
                        Join.join(", ", containerIds)
                );

                createContainer(containerInfoType, ejbDeployment, bean);
            }

            // Resource reference
            for (final ResourceRef ref : bean.getResourceRef()) {
                processResourceRef(ref, ejbDeployment, appResources, ejbModule);
            }

            // Resource env reference
            for (final JndiReference ref : bean.getResourceEnvRef()) {
                processResourceEnvRef(ref, ejbDeployment, appResources, ejbModule.getClassLoader());
            }

            // Message destination reference
            for (final MessageDestinationRef ref : bean.getMessageDestinationRef()) {
                processResourceEnvRef(ref, ejbDeployment, appResources, ejbModule.getClassLoader());
            }

            // mdb message destination id
            if (autoCreateResources && bean instanceof MessageDrivenBean) {
                final MessageDrivenBean mdb = (MessageDrivenBean) bean;

                final ResourceLink resourceLink = ejbDeployment.getResourceLink("openejb/destination");
                if (resourceLink != null) {
                    try {
                        final String destinationId = getResourceEnvId(bean.getEjbName(), resourceLink.getResId(), mdb.getMessageDestinationType(), appResources);
                        resourceLink.setResId(destinationId);
                    } catch (final OpenEJBException e) {
                        // The MDB doesn't need the auto configured "openejb/destination" env entry
                        ejbDeployment.removeResourceLink("openejb/destination");
                    }
                }
            }

        }
    }

    private void processApplicationResources(final AppModule module) throws OpenEJBException {
        final Collection<Resource> resources = module.getResources();

        if (resources.size() == 0) {
            return;
        }

        final List<JndiConsumer> jndiConsumers = new ArrayList<>();
        for (final WebModule webModule : module.getWebModules()) {
            final JndiConsumer consumer = webModule.getWebApp();
            jndiConsumers.add(consumer);
        }

        for (final EjbModule ejbModule : module.getEjbModules()) {
            Collections.addAll(jndiConsumers, ejbModule.getEjbJar().getEnterpriseBeans());
        }

        List<ResourceInfo> resourceInfos = new ArrayList<>();
        final Map<ResourceInfo, Resource> resourcesMap = new HashMap<>(resources.size());
        for (final Resource resource : resources) {
            final String originalId = PropertyPlaceHolderHelper.value(resource.getId());
            final String modulePrefix = module.getModuleId() + "/";

            if ("/".equals(modulePrefix) || originalId.startsWith("global") || originalId.startsWith("/global")) {
                resource.setId(replaceJavaAndSlash(originalId));
            } else {
                resource.getProperties().setProperty(ORIGINAL_ID, originalId);
                resource.setId(modulePrefix + replaceJavaAndSlash(originalId));
            }
            resource.setJndi(PropertyPlaceHolderHelper.value(resource.getJndi()));

            final Thread thread = Thread.currentThread();
            final ClassLoader oldCl = thread.getContextClassLoader();
            thread.setContextClassLoader(module.getClassLoader());
            try {
                resource.getProperties().putAll(PropertyPlaceHolderHelper.holds(resource.getProperties()));
            } finally {
                thread.setContextClassLoader(oldCl);
            }

            final Collection<String> aliases = resource.getAliases();
            if (!aliases.isEmpty()) {
                final Collection<String> newAliases = new ArrayList<>();
                for (final String s : aliases) {
                    newAliases.add(module.getModuleId() + "/" + s);
                }
                resource.getAliases().clear();
                resource.getAliases().addAll(newAliases);
            }

            final Properties properties = resource.getProperties();

            if (DataSource.class.getName().equals(resource.getType())
                || DataSource.class.getSimpleName().equals(resource.getType())) {
                DataSourceFactory.trimNotSupportedDataSourceProperties(properties);
            }

            final boolean shouldGenerateJdbcUrl = DataSource.class.getName().equals(resource.getType())
                && resource.getProperties().containsKey(ORIGIN_FLAG)
                && resource.getProperties().getProperty(ORIGIN_FLAG).equals(ORIGIN_ANNOTATION);

            if (shouldGenerateJdbcUrl && properties.get("JdbcUrl") == null) {
                final String url = getVendorUrl(properties);
                if (url != null) {
                    properties.put("JdbcUrl", url);
                }
            }

            final ResourceInfo resourceInfo = configFactory.configureService(resource, ResourceInfo.class);
            resourceInfo.originAppName = module.getModuleId();
            final ResourceRef resourceRef = new ResourceRef();
            resourceRef.setResType(chooseType(module.getClassLoader(), resourceInfo, resource.getType()));

            if (shouldGenerateJdbcUrl) {
                properties.remove(ORIGIN_FLAG);
                resourceRef.setResRefName(dataSourceLookupName(resource));
            } else {
                resourceRef.setResRefName(OPENEJB_RESOURCE_JNDI_PREFIX + resourceInfo.id);
            }

            resourceRef.setMappedName(resourceInfo.id);

            final ResourceRef strictRef = new ResourceRef(OPENEJB_RESOURCE_JNDI_PREFIX + originalId,
                resourceRef.getResType(),
                resourceRef.getResAuth(),
                resourceRef.getResSharingScope());
            strictRef.setMappedName(resourceInfo.id);

            for (final JndiConsumer consumer : jndiConsumers) {
                addResource(consumer, resourceRef); // for injections etc...
                if (!"/".equals(modulePrefix)) {
                    addResource(consumer, strictRef); // for lookups (without prefix)
                }
            }

            resourceInfos.add(resourceInfo);
            resourcesMap.put(resourceInfo, resource);
        }

        resourceInfos = ConfigurationFactory.sort(resourceInfos, module.getModuleId() + "/");
        for (final ResourceInfo resourceInfo : resourceInfos) {
            final int originalSize = resourceInfo.aliases.size();
            final String id = installResource(module.getModuleId(), resourceInfo);

            final Resource resource = resourcesMap.remove(resourceInfo);
            resource.setId(id);
            if (resourceInfo.aliases.size() > originalSize) { // an aliases is generally added to be able to bind in global jndi tree
                resource.getAliases().add(resourceInfo.aliases.get(resourceInfo.aliases.size() - 1));
            }
        }

        resourceInfos.clear();
        // resources.clear(); // don't clear it since we want to keep this to be able to undeploy resources with the app
    }

    private static void addResource(final JndiConsumer consumer, final ResourceRef resourceRef) {
        final ResourceRef existing = consumer.getResourceRefMap().get(resourceRef.getKey());
        if (existing != null) {
            existing.setMappedName(resourceRef.getMappedName());
        } else {
            consumer.getResourceRef().add(resourceRef);
        }
    }

    private static String chooseType(final ClassLoader classLoader, final ResourceInfo info, final String defaultType) {
        if (info.types != null) {
            for (final String type : info.types) {
                if (canLoad(classLoader, type)) {
                    return type;
                }
            }
        }
        return info.className != null ? ((canLoad(classLoader, info.className) ? info.className : defaultType)) : defaultType;
    }

    private static boolean canLoad(final ClassLoader classLoader, final String type) {
        try {
            classLoader.loadClass(type);
            return true;
        } catch (final ClassNotFoundException e) {
            return false;
        }
    }

    private String dataSourceLookupName(final Resource datasource) {
        final String jndi = datasource.getJndi();
        if (jndi.startsWith("java:")) {
            return jndi.startsWith("/") ? jndi.substring(1) : jndi;
        }
        if (jndi.startsWith("comp/env/")) {
            return "java:" + jndi;
        }
        if (jndi.startsWith("module/")) {
            return "java:" + jndi;
        }
        if (jndi.startsWith("global/")) {
            return "java:" + jndi;
        }
        if (jndi.startsWith("app/")) {
            return "java:" + jndi;
        }
        return "java:comp/env/" + jndi;
    }

    private static String getVendorUrl(final Properties properties) {

        final String driver = properties.getProperty("JdbcDriver");
        final String serverName = properties.getProperty("ServerName");
        final int port = getInt(properties.get("PortNumber"));
        final boolean remote = port != -1;
        final String databaseName = properties.getProperty("DatabaseName");

        if (driver == null || driver.equals("org.hsqldb.jdbcDriver")) {
            if (remote) {
                return String.format("jdbc:hsqldb:hsql://%s:%s/%s", serverName, port, databaseName);
            } else {
                return String.format("jdbc:hsqldb:mem:%s", databaseName);
            }
        }

        if (driver.startsWith("org.apache.derby.jdbc.Embedded")) { // Driver or DataSource
            return String.format("jdbc:derby:%s%s", databaseName, properties.getProperty("connectionAttributes", ";create=true"));
        }

        if (driver.equals("org.apache.derby.jdbc.ClientDriver")) {
            return String.format("jdbc:derby://%s:%s/%s%s", serverName, port, databaseName, properties.getProperty("connectionAttributes", ";create=true"));
        }

        if (driver.equals("com.mysql.jdbc.Driver")) {
            return String.format("jdbc:mysql://%s:%s/%s", serverName, port, databaseName);
        }

        if (driver.equals("com.postgresql.jdbc.Driver")) {
            return String.format("jdbc:postgresql://%s:%s/%s", serverName, port, databaseName);
        }

        if (driver.equals("oracle.jdbc.OracleDriver")) {
            return String.format("jdbc:oracle:thin:@//%s:%s/%s", serverName, port, databaseName);
        }

        return null;
    }

    private static int getInt(final Object number) {
        try {
            return (Integer) number;
        } catch (final Exception e) {
            try {
                return Integer.parseInt(String.valueOf(number));
            } catch (final NumberFormatException e1) {
                return -1;
            }
        }
    }

    private String createContainer(final Class<? extends ContainerInfo> containerInfoType, final EjbDeployment ejbDeployment, final EnterpriseBean bean) throws OpenEJBException {
        if (!autoCreateContainers) {
            throw new OpenEJBException("A container of type " + getType(bean) + " must be declared in the configuration file for bean: " + bean.getEjbName());
        }

        // get the container info (data used to build the container)
        final ContainerInfo containerInfo = configFactory.configureService(containerInfoType);
        logger.info("Auto-creating a container for bean " + ejbDeployment.getDeploymentId() + ": Container(type=" + getType(bean) + ", id=" + containerInfo.id + ")");

        // if the is an MDB container we need to resolve the resource adapter
        final String resourceAdapterId = containerInfo.properties.getProperty("ResourceAdapter");
        if (resourceAdapterId != null) {
            final String newResourceId = getResourceId(ejbDeployment.getDeploymentId(), resourceAdapterId, null, null);
            if (resourceAdapterId.equals(newResourceId)) {
                containerInfo.properties.setProperty("ResourceAdapter", newResourceId);
            }
        }

        // install the container
        configFactory.install(containerInfo);
        return containerInfo.id;
    }

    private void processResourceRef(final ResourceRef ref,
                                    final EjbDeployment ejbDeployment,
                                    final AppResources appResources,
                                    final EjbModule ejbModule) throws OpenEJBException {

        // skip destinations with lookup name
        if (ref.getLookupName() != null) {
            return;
        }
        // skip destinations with a global jndi name
        final String mappedName = ref.getMappedName() == null ? "" : ref.getMappedName();
        if (mappedName.startsWith("jndi:")) {
            return;
        }

        final String refName = ref.getName();
        final String refType = getType(ref, ejbModule.getClassLoader());

        // skip references such as URLs which are automatically handled by the server
        if (ignoredReferenceTypes.contains(refType)) {
            final ResourceInfo resourceInfo = configFactory.getResourceInfo(refName.replace("java:", "").replace("comp/env/", ""));
            if (resourceInfo != null) {
                ref.setMappedName("jndi:" + (resourceInfo.id.startsWith("java:") ? resourceInfo.id : "openejb:Resource/" + resourceInfo.id));
            }
            return;
        }

        try {
            final Class<?> clazz = ejbModule.getClassLoader().loadClass(refType);
            if (clazz.isAnnotationPresent(ManagedBean.class)) {
                return;
            }
        } catch (final Throwable t) {
            // no-op
        }

        try {
            ResourceLink link = ejbDeployment.getResourceLink(refName);
            if (link == null) {
                String id = mappedName.length() == 0 ? ref.getName() : mappedName;
                if (id.startsWith("java:")) {
                    id = id.substring("java:".length());
                }
                if (id.startsWith("/")) {
                    id = id.substring(1);
                }
                try {
                    final AppModule appModule = ejbModule.getAppModule();
                    if (appModule != null) {
                        final String newId = findResourceId(appModule.getModuleId() + '/' + id.replace("java:", "").replaceAll("^comp/env/", ""),
                            refType,
                            new Properties(),
                            appResources);
                        if (newId != null) { // app scoped resources, try to find it without creating it first
                            id = getResourceId(ejbModule.getModuleId(), newId, refType, appResources);
                        } else {
                            id = getResourceId(ejbDeployment.getDeploymentId(), id, refType, appResources);
                        }
                    } else {
                        id = getResourceId(ejbDeployment.getDeploymentId(), id, refType, appResources);
                    }
                } catch (final OpenEJBException e) { // changing the message to be explicit
                    throw new OpenEJBException("Can't find resource for " + ref.getOrigin() + ". (" + e.getMessage() + ")", e.getCause());
                }
                logger.info("Auto-linking resource-ref '" + refName + "' in bean " + ejbDeployment.getDeploymentId() + " to Resource(id=" + id + ")");

                link = new ResourceLink();
                link.setResId(id);
                link.setResRefName(refName);
                ejbDeployment.addResourceLink(link);
            } else {
                final String id = getResourceId(ejbDeployment.getDeploymentId(), link.getResId(), refType, appResources);
                link.setResId(id);
                link.setResRefName(refName);
            }
        } catch (final OpenEJBException ex) {
            if (!(ref instanceof ContextRef)) {
                throw ex;
            }
        }
    }

    private void processResourceEnvRef(final JndiReference ref,
                                       final EjbDeployment ejbDeployment,
                                       final AppResources appResources,
                                       final ClassLoader classLoader) throws OpenEJBException {
        // skip destinations with lookup name
        if (ref.getLookupName() != null) {
            return;
        }
        // skip destinations with a global jndi name
        final String mappedName = ref.getMappedName() == null ? "" : ref.getMappedName();
        if (mappedName.startsWith("jndi:")) {
            return;
        }

        final String refName = ref.getName();
        final String refType = getType(ref, classLoader);

        // skip references such as SessionContext which are automatically handled by the server
        if (isIgnoredReferenceType(refType, classLoader)) {
            return;
        }

        ResourceLink link = ejbDeployment.getResourceLink(refName);
        if (link == null) {

            String id = mappedName.length() == 0 ? refName : mappedName;
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
            final String id = getResourceEnvId(ejbDeployment.getDeploymentId(), link.getResId(), refType, appResources);
            link.setResId(id);
            link.setResRefName(refName);
        }
    }

    private String getType(final JndiReference ref, final ClassLoader classLoader) throws OpenEJBException {
        final String refType = ref.getType();
        if (refType != null) {
            return refType;
        }
        if (classLoader != null) {
            final Set<InjectionTarget> injections = ref.getInjectionTarget();
            for (final InjectionTarget injection : injections) {
                try {
                    final Class target = classLoader.loadClass(injection.getInjectionTargetClass().trim());
                    final Class type = IntrospectionSupport.getPropertyType(target, injection.getInjectionTargetName().trim());
                    return type.getName();
                } catch (final ClassNotFoundException | NoSuchFieldException e) {
                    // ignore
                }
            }
        }
        throw new OpenEJBException("Unable to infer type for " + ref.getKey());
    }

    private static boolean skipMdb(final Object bean) {
        return bean instanceof MessageDrivenBean && SystemInstance.get().hasProperty("openejb.geronimo");
    }

    private static String getType(final EnterpriseBean enterpriseBean) throws OpenEJBException {
        if (enterpriseBean instanceof EntityBean) {
            if (((EntityBean) enterpriseBean).getPersistenceType() == PersistenceType.CONTAINER) {
                return BeanTypes.CMP_ENTITY;
            } else {
                return BeanTypes.BMP_ENTITY;
            }
        } else if (enterpriseBean instanceof SessionBean) {
            if (((SessionBean) enterpriseBean).getSessionType() == SessionType.STATEFUL) {
                return BeanTypes.STATEFUL;
            } else if (((SessionBean) enterpriseBean).getSessionType() == SessionType.SINGLETON) {
                return BeanTypes.SINGLETON;
            } else if (((SessionBean) enterpriseBean).getSessionType() == SessionType.MANAGED) {
                return BeanTypes.MANAGED;
            } else {
                return BeanTypes.STATELESS;
            }
        } else if (enterpriseBean instanceof MessageDrivenBean) {
            return BeanTypes.MESSAGE;
        }
        throw new OpenEJBException("Unknown enterprise bean type " + enterpriseBean.getClass().getName());
    }

    private void deploy(final AppModule app, final PersistenceModule persistenceModule) throws OpenEJBException {
        if (!autoCreateResources) {
            return;
        }

        final Persistence persistence = persistenceModule.getPersistence();
        for (final PersistenceUnit unit : persistence.getPersistenceUnit()) {
            if (unit.getProvider() != null) {
                logger.info("Configuring PersistenceUnit(name=" + unit.getName() + ", provider=" + unit.getProvider() + ")");
            } else {
                logger.info("Configuring PersistenceUnit(name=" + unit.getName() + ")");
            }

            if (unit.getJtaDataSource() == null && unit.getNonJtaDataSource() == null
                    && "true".equalsIgnoreCase(SystemInstance.get().getProperty("openejb.force-unit-type", unit.getProperty("openejb.force-unit-type", "true")))) {
                unit.setTransactionType(TransactionType.JTA); // 8.2.1.5 of JPA 2.0 spec
            }

            // if jta datasource is specified it can be used as model fo rnon jta datasource
            final boolean resourceLocal = TransactionType.RESOURCE_LOCAL.equals(unit.getTransactionType()) && unit.getJtaDataSource() == null;
            if (resourceLocal && unit.getNonJtaDataSource() == null && isDataSourcePropertiesConfigured(unit.getProperties())) {
                continue;
            }

            final Properties required = new Properties();

            //            if (unit.getJtaDataSource() == null && unit.getNonJtaDataSource() == null){
            //                unit.setJtaDataSource("JtaDataSource");
            //                unit.setNonJtaDataSource("NonJtaDataSource");
            //            } else if (unit.getJtaDataSource() == null){
            //                unit.setJtaDataSource(unit.getNonJtaDataSource()+"Jta");
            //            } else if (unit.getNonJtaDataSource() == null){
            //                unit.setNonJtaDataSource(unit.getJtaDataSource()+"NonJta");
            //            }

            if ("org.apache.openjpa.persistence.PersistenceProviderImpl".equals(unit.getProvider())) {
                if (unit.getJtaDataSource() == null) {
                    unit.setJtaDataSource(unit.getProperty("openjpa.ConnectionFactoryName"));
                }
                if (unit.getNonJtaDataSource() == null) {
                    unit.setNonJtaDataSource(unit.getProperty("openjpa.ConnectionFactory2Name"));
                }
            }

            logger.debug("raw <jta-data-source>" + unit.getJtaDataSource() + "</jta-datasource>");
            logger.debug("raw <non-jta-data-source>" + unit.getNonJtaDataSource() + "</non-jta-datasource>");

            final String originalJtaDataSource = unit.getJtaDataSource(); // keep it can start with java:global for instance
            unit.setJtaDataSource(normalizeResourceId(originalJtaDataSource));
            final String originalNonJtaDataSource = unit.getNonJtaDataSource();
            unit.setNonJtaDataSource(normalizeResourceId(originalNonJtaDataSource));

            logger.debug("normalized <jta-data-source>" + unit.getJtaDataSource() + "</jta-datasource>");
            logger.debug("normalized <non-jta-data-source>" + unit.getNonJtaDataSource() + "</non-jta-datasource>");

            if (logger.isDebugEnabled()) {
                required.put("JtaManaged", "true");
                final List<String> managed = configFactory.getResourceIds("DataSource", required);

                required.put("JtaManaged", "false");
                final List<String> unmanaged = configFactory.getResourceIds("DataSource", required);

                required.clear();
                final List<String> unknown = configFactory.getResourceIds("DataSource", required);

                logger.debug("Available DataSources");
                for (final String name : managed) {
                    logger.debug("DataSource(name=" + name + ", JtaManaged=true)");
                }
                for (final String name : unmanaged) {
                    logger.debug("DataSource(name=" + name + ", JtaManaged=false)");
                }
                for (final String name : unknown) {
                    if (managed.contains(name)) {
                        continue;
                    }
                    if (unmanaged.contains(name)) {
                        continue;
                    }
                    logger.debug("DataSource(name=" + name + ", JtaManaged=<unknown>)");
                }
            }

            final String prefix = app.getModuleId() + "/";

            String jtaDataSourceId = null;
            String nonJtaDataSourceId = null;

            // first try exact matching without JtaManaged which is not mandatory actually (custom DS + JTADataSourceWrapperFactory)
            final String jtaWithJavaAndSlash = replaceJavaAndSlash(unit.getJtaDataSource());
            for (final String potentialName : asList(prefix + jtaWithJavaAndSlash, originalJtaDataSource, jtaWithJavaAndSlash)) {
                if(potentialName == null) {
                    // If unit.getJtaDataSource() is null, one of the potentialName is also null.
                    continue;
                }
                final ResourceInfo jtaInfo = configFactory.getResourceInfo(potentialName);
                if (jtaInfo != null) {
                    if (!"false".equalsIgnoreCase(jtaInfo.properties.getProperty("JtaManaged")) // don't test true since it can be missing
                            && (jtaInfo.types.contains("DataSource") || jtaInfo.types.contains(DataSource.class.getName()))) {
                        jtaDataSourceId = jtaInfo.id;
                        break;
                    } else {
                        logger.warning("Found matching datasource: " + jtaInfo.id + " but this one is not a JTA datasource");
                    }
                }
            }

            final String nonJtaWithJavaAndSlash = replaceJavaAndSlash(unit.getNonJtaDataSource());
            for (final String potentialName : asList(prefix + nonJtaWithJavaAndSlash, originalNonJtaDataSource, nonJtaWithJavaAndSlash)) {
                if(potentialName == null) {
                    // If unit.getNonJtaDataSource() is null, one of the potentialName is also null.
                    continue;
                }
                final ResourceInfo info = configFactory.getResourceInfo(potentialName);
                if (info != null) {
                    if (!"true".equalsIgnoreCase(info.properties.getProperty("JtaManaged"))
                            && (info.types.contains("DataSource") || info.types.contains(DataSource.class.getName()))) {
                        nonJtaDataSourceId = info.id;
                        break;
                    } else {
                        logger.warning("Found matching datasource: " + info.id + " but this one is a JTA datasource");
                    }
                }
            }

            // then that's ok to force configuration
            if (jtaDataSourceId == null && !resourceLocal) {
                required.put("JtaManaged", "true");
                jtaDataSourceId = findResourceId(prefix + jtaWithJavaAndSlash, "DataSource", required, null);
                if (jtaDataSourceId == null) { // test with javax.sql.DataSource before DataSource since RA can register resources without our shortcut
                    jtaDataSourceId = findResourceId(jtaWithJavaAndSlash, "javax.sql.DataSource", required, null);
                }
                /* this shouldn't be mandatory anymore since our DataSource has as alias javax.sql.DataSource
                if (jtaDataSourceId == null) {
                    jtaDataSourceId = findResourceId(replaceJavaAndSlash(unit.getJtaDataSource()), "DataSource", required, null);
                }
                */
            }

            if (nonJtaDataSourceId == null) {
                required.put("JtaManaged", "false");
                nonJtaDataSourceId = findResourceId(prefix + nonJtaWithJavaAndSlash, "DataSource", required, null);
                if (nonJtaDataSourceId == null) {
                    nonJtaDataSourceId = findResourceId(nonJtaWithJavaAndSlash, "DataSource", required, null);
                }
            }

            if ((jtaDataSourceId != null || resourceLocal) && nonJtaDataSourceId != null) {
                // Both DataSources were explicitly configured.
                if (jtaDataSourceId != null) {
                    setJtaDataSource(unit, jtaDataSourceId);
                }
                setNonJtaDataSource(unit, nonJtaDataSourceId);
                continue;
            }

            //
            //  If the jta-data-source or the non-jta-data-source link to
            //  third party resources, then we can't do any auto config
            //  for them.  We give them what they asked for and move on.
            //
            if (jtaDataSourceId == null && nonJtaDataSourceId == null) {
                required.put("JtaManaged", ServiceUtils.NONE);

                if (!resourceLocal) {
                    jtaDataSourceId = findResourceId(unit.getJtaDataSource(), "DataSource", required, null);
                }
                nonJtaDataSourceId = findResourceId(unit.getNonJtaDataSource(), "DataSource", required, null);

                if (jtaDataSourceId != null || nonJtaDataSourceId != null) {
                    if (jtaDataSourceId != null) {
                        setJtaDataSource(unit, jtaDataSourceId);
                    }
                    if (nonJtaDataSourceId != null) {
                        setNonJtaDataSource(unit, nonJtaDataSourceId);
                    }
                    continue;
                }
            }

            //  We are done with the most optimal configuration.
            //
            //  If both the jta-data-source and non-jta-data-source
            //  references were explicitly and correctly configured
            //  to existing datasource, we wouldn't get this far.
            //
            //  At this point we see if either we can't figure out
            //  if there's an issue with their configuration or
            //  if we can't intelligently complete their configuration.

            //
            //  Do both the jta-data-source and non-jta-data-source references
            //  point to the same datasource?
            //
            //  If so, then unlink the invalid one so defaulting rules can
            //  possibly fill in a good value.
            //

            required.put("JtaManaged", ServiceUtils.ANY);
            final String possibleJta = findResourceId(jtaWithJavaAndSlash, "DataSource", required, null);
            final String possibleNonJta = findResourceId(nonJtaWithJavaAndSlash, "DataSource", required, null);
            if (possibleJta != null && possibleJta.equals(possibleNonJta)) {
                final ResourceInfo dataSource = configFactory.getResourceInfo(possibleJta);

                final String jtaManaged = (String) dataSource.properties.get("JtaManaged");

                logger.warning("PeristenceUnit(name=" +
                    unit.getName() +
                    ") invalidly refers to Resource(id=" +
                    dataSource.id +
                    ") as both its <jta-data-source> and <non-jta-data-source>.");

                if ("true".equalsIgnoreCase(jtaManaged)) {
                    nonJtaDataSourceId = null;
                    unit.setNonJtaDataSource(null);

                } else if ("false".equalsIgnoreCase(jtaManaged)) {
                    jtaDataSourceId = null;
                    unit.setJtaDataSource(null);
                }
            }

            //
            //  Do the jta-data-source and non-jta-data-source references
            //  point to innapropriately configured Resources?
            //
            checkUnitDataSourceRefs(unit);

            //
            //  Do either the jta-data-source and non-jta-data-source
            //  references point to the explicit name of a ServiceProvider?
            //
            if (jtaDataSourceId == null && nonJtaDataSourceId == null) {
                jtaDataSourceId = findResourceProviderId(unit.getJtaDataSource());
                nonJtaDataSourceId = findResourceProviderId(unit.getNonJtaDataSource());

                // if one of them is not null we have a match on at least one
                // we can just create the second resource using the first as a template
                if (jtaDataSourceId != null || nonJtaDataSourceId != null) {
                    final Resource jtaResource = new Resource(jtaDataSourceId, "DataSource", jtaDataSourceId);
                    jtaResource.getProperties().setProperty("JtaManaged", "true");

                    final Resource nonJtaResource = new Resource(nonJtaDataSourceId, "DataSource", nonJtaDataSourceId);
                    nonJtaResource.getProperties().setProperty("JtaManaged", "false");

                    if (jtaDataSourceId == null) {
                        jtaResource.setId(nonJtaDataSourceId + "Jta");
                        jtaResource.setProvider(nonJtaDataSourceId);
                    } else if (nonJtaDataSourceId == null) {
                        nonJtaResource.setId(jtaDataSourceId + "NonJta");
                        nonJtaResource.setProvider(jtaDataSourceId);
                    }

                    final ResourceInfo jtaResourceInfo = configFactory.configureService(jtaResource, ResourceInfo.class);
                    final ResourceInfo nonJtaResourceInfo = configFactory.configureService(nonJtaResource, ResourceInfo.class);
                    if (jtaDataSourceId != null && nonJtaDataSourceId == null) {
                        nonJtaResourceInfo.originAppName = jtaResourceInfo.originAppName;
                    }

                    logAutoCreateResource(jtaResourceInfo, "DataSource", unit.getName());
                    jtaDataSourceId = installResource(unit.getName(), jtaResourceInfo);

                    logAutoCreateResource(nonJtaResourceInfo, "DataSource", unit.getName());
                    nonJtaDataSourceId = installResource(unit.getName(), nonJtaResourceInfo);

                    setJtaDataSource(unit, jtaDataSourceId);
                    setNonJtaDataSource(unit, nonJtaDataSourceId);
                    continue;
                }
            }

            // No data sources were specified: 
            // Look for defaults, see https://issues.apache.org/jira/browse/OPENEJB-1027
            if (jtaDataSourceId == null && nonJtaDataSourceId == null) {
                // We check for data sources matching the following names:
                // 1. The persistence unit id
                // 2. The web module id
                // 3. The web module context root
                // 4. The application module id
                final List<String> ids = new ArrayList<>();
                ids.add(unit.getName());
                for (final WebModule webModule : app.getWebModules()) {
                    ids.add(webModule.getModuleId());
                    ids.add(webModule.getContextRoot());
                }
                ids.add(app.getModuleId());

                // Search for a matching data source
                for (final String id : ids) {
                    //Try finding a jta managed data source
                    required.put("JtaManaged", "true");
                    jtaDataSourceId = findResourceId(id, "DataSource", required, null);

                    if (jtaDataSourceId == null) {
                        //No jta managed data source found. Try finding a non-jta managed
                        required.clear();
                        required.put("JtaManaged", "false");
                        nonJtaDataSourceId = findResourceId(id, "DataSource", required, null);
                    }

                    if (jtaDataSourceId == null && nonJtaDataSourceId == null) {
                        // Neither jta nor non-jta managed data sources were found. try to find one with it unset
                        required.clear();
                        required.put("JtaManaged", ServiceUtils.NONE);
                        jtaDataSourceId = findResourceId(id, "DataSource", required, null);
                    }

                    if (jtaDataSourceId != null || nonJtaDataSourceId != null) {
                        //We have found a default. Exit the loop
                        break;
                    }
                }
            }

            //
            //  If neither of the references are valid yet, then let's take
            //  the first valid datasource.
            //
            //  We won't fill in both jta-data-source and non-jta-data-source
            //  this way as the following code does a great job at determining
            //  if any of the existing data sources are a good match or if
            //  one needs to be generated.
            //
            if (jtaDataSourceId == null && nonJtaDataSourceId == null) {

                required.clear();
                required.put("JtaManaged", "true");
                jtaDataSourceId = firstMatching(prefix, "DataSource", required, null);

                if (jtaDataSourceId == null) {
                    required.clear();
                    required.put("JtaManaged", "false");
                    nonJtaDataSourceId = firstMatching(prefix, "DataSource", required, null);
                }
            }

            //
            //  Does the jta-data-source reference point an existing
            //  Resource in the system with JtaManaged=true?
            //
            //  If so, we can search for an existing datasource
            //  configured with identical properties and use it.
            //
            //  If that doesn't work, we can copy the jta-data-source
            //  and auto-create the missing non-jta-data-source
            //  using it as a template, applying the overrides,
            //  and finally setting JtaManaged=false
            //

            if (jtaDataSourceId != null && nonJtaDataSourceId == null) {

                final ResourceInfo jtaResourceInfo = configFactory.getResourceInfo(jtaDataSourceId);

                final Properties jtaProperties = jtaResourceInfo.properties;

                if (jtaProperties.containsKey("JtaManaged")) {

                    // Strategy 1: Best match search

                    required.clear();
                    required.put("JtaManaged", "false");

                    for (final String key : asList("JdbcDriver", "JdbcUrl")) {
                        if (jtaProperties.containsKey(key)) {
                            required.put(key, jtaProperties.get(key));
                        }
                    }

                    nonJtaDataSourceId = firstMatching(prefix, "DataSource", required, null);

                    // Strategy 2: Copy

                    if (nonJtaDataSourceId == null) {
                        final ResourceInfo nonJtaResourceInfo = copy(jtaResourceInfo);
                        nonJtaResourceInfo.id = jtaResourceInfo.id + "NonJta";
                        nonJtaResourceInfo.originAppName = jtaResourceInfo.originAppName;
                        suffixAliases(nonJtaResourceInfo, "NonJta");
                        configureImplicitDataSource(nonJtaResourceInfo);

                        final Properties overrides = ConfigurationFactory.getSystemProperties(nonJtaResourceInfo.id, nonJtaResourceInfo.service);
                        nonJtaResourceInfo.properties.putAll(overrides);
                        nonJtaResourceInfo.properties.setProperty("JtaManaged", "false");
                        nonJtaResourceInfo.properties.remove("Definition"); // if created from annotation we just want live config

                        logAutoCreateResource(nonJtaResourceInfo, "DataSource", unit.getName());
                        logger.info("configureService.configuring", nonJtaResourceInfo.id, nonJtaResourceInfo.service, jtaResourceInfo.id);

                        nonJtaDataSourceId = installResource(unit.getName(), nonJtaResourceInfo);
                    }
                }

            }

            //
            //  Does the jta-data-source reference point an existing
            //  Resource in the system with JtaManaged=false?
            //
            //  If so, we can search for an existing datasource
            //  configured with identical properties and use it.
            //
            //  If that doesn't work, we can copy the jta-data-source
            //  and auto-create the missing non-jta-data-source
            //  using it as a template, applying the overrides,
            //  and finally setting JtaManaged=false
            //

            final String deduceJtaFromNonJta = unit.getProperty(AUTOCREATE_JTA_DATASOURCE_FROM_NON_JTA_ONE_KEY,
                SystemInstance.get().getOptions().get(AUTOCREATE_JTA_DATASOURCE_FROM_NON_JTA_ONE_KEY, (String) null));
            if (nonJtaDataSourceId != null && jtaDataSourceId == null
                // hibernate uses the fact that this ds is missing to get a non jta em instead of a JTA one
                && (!resourceLocal || deduceJtaFromNonJta != null)
                && (deduceJtaFromNonJta == null || deduceJtaFromNonJta != null && Boolean.parseBoolean(deduceJtaFromNonJta))) {

                final ResourceInfo nonJtaResourceInfo = configFactory.getResourceInfo(nonJtaDataSourceId);

                final Properties nonJtaProperties = nonJtaResourceInfo.properties;

                if (nonJtaProperties.containsKey("JtaManaged")) {

                    // Strategy 1: Best match search

                    required.clear();
                    required.put("JtaManaged", "true");

                    for (final String key : asList("JdbcDriver", "JdbcUrl")) {
                        if (nonJtaProperties.containsKey(key)) {
                            required.put(key, nonJtaProperties.get(key));
                        }
                    }

                    jtaDataSourceId = firstMatching(prefix, "DataSource", required, null);

                    // Strategy 2: Copy

                    if (jtaDataSourceId == null) {
                        final ResourceInfo jtaResourceInfo = copy(nonJtaResourceInfo);
                        jtaResourceInfo.id = nonJtaResourceInfo.id + "Jta";
                        suffixAliases(jtaResourceInfo, "Jta");
                        configureImplicitDataSource(jtaResourceInfo);

                        final Properties overrides = ConfigurationFactory.getSystemProperties(jtaResourceInfo.id, jtaResourceInfo.service);
                        jtaResourceInfo.properties.putAll(overrides);
                        jtaResourceInfo.properties.setProperty("JtaManaged", "true");
                        jtaResourceInfo.properties.remove("Definition"); // if created from annotation we just want live config

                        logAutoCreateResource(jtaResourceInfo, "DataSource", unit.getName());
                        logger.info("configureService.configuring", jtaResourceInfo.id, jtaResourceInfo.service, nonJtaResourceInfo.id);

                        jtaDataSourceId = installResource(unit.getName(), jtaResourceInfo);
                    }
                }

            }

            //
            //  By this point if we've found anything at all, both
            //  jta-data-source and non-jta-data-source should be
            //  filled in (provided they aren't using a third party
            //  data source).
            //
            //  Should both references still be null
            //  we can just take a shot in the dark and auto-create
            //  them them both using the built-in templates for jta
            //  and non-jta default datasources.  These are supplied
            //  via the service-jar.xml file.
            //
            if (jtaDataSourceId == null && nonJtaDataSourceId == null) {
                if (!resourceLocal) {
                    required.put("JtaManaged", "true");
                    jtaDataSourceId = autoCreateResource("DataSource", required, unit.getName());
                }

                required.put("JtaManaged", "false");
                nonJtaDataSourceId = autoCreateResource("DataSource", required, unit.getName());
            }

            if (jtaDataSourceId != null) {
                setJtaDataSource(unit, jtaDataSourceId);
            }
            if (nonJtaDataSourceId != null) {
                setNonJtaDataSource(unit, nonJtaDataSourceId);
            }
        }
    }

    private boolean isDataSourcePropertiesConfigured(final Properties properties) {
        return "true".equals(SystemInstance.get().getProperty("openejb.guess.resource-local-datasource-properties-configured", "true")) &&
                (properties.containsKey("jakarta.persistence.jdbc.driver") || properties.containsKey("jakarta.persistence.jdbc.url"));
    }

    private static void suffixAliases(final ResourceInfo ri, final String suffix) {
        final Collection<String> aliases = ri.aliases;
        final List<String> newAliases = new ArrayList<>();
        for (final String alias : aliases) {
            newAliases.add(alias + suffix);
        }
        ri.aliases = newAliases;
    }

    private static void configureImplicitDataSource(final ResourceInfo copy) {
        if (copy != null && copy.properties != null) {
            for (final String key : copy.properties.stringPropertyNames()) {
                if ("InitialSize".equalsIgnoreCase(key)) {
                    try {
                        final int value = Integer.parseInt(copy.properties.getProperty("InitialSize"));
                        if (MAX_IMPLICIT_POOL_SIZE < value) {
                            copy.properties.setProperty(key, Integer.toString(MAX_IMPLICIT_POOL_SIZE));
                            logger.warning("Adjusting " + key + " to " + MAX_IMPLICIT_POOL_SIZE + " for " + copy.id
                                + " DataSource to avoid too much network bandwidth usage."
                                + " If you want to keep it please define the DataSource explicitely.");
                        }
                    } catch (final NumberFormatException nfe) {
                        // no-op
                    }
                }
            }
        }
    }

    private String replaceJavaAndSlash(final String name) {
        if (name == null) {
            return null;
        }

        if (name.startsWith("java:")) {
            return replaceJavaAndSlash(name.substring("java:".length()));
        }
        if (name.startsWith("/")) {
            return name.substring(1);
        }
        return name;
    }

    private void setNonJtaDataSource(final PersistenceUnit unit, final String current) {

        final String previous = unit.getNonJtaDataSource();

        if (!current.equals(previous)) {

            logger.info("Adjusting PersistenceUnit " + unit.getName() + " <non-jta-data-source> to Resource ID '" + current + "' from '" + previous + "'");

        }

        unit.setNonJtaDataSource(current);
    }

    private void setJtaDataSource(final PersistenceUnit unit, final String current) {

        final String previous = unit.getJtaDataSource();

        if (!current.equals(previous)) {

            logger.info("Adjusting PersistenceUnit " + unit.getName() + " <jta-data-source> to Resource ID '" + current + "' from '" + previous + "'");

        }

        unit.setJtaDataSource(current);
    }

    private ResourceInfo copy(final ResourceInfo a) {
        final ResourceInfo b = new ResourceInfo();

        b.id = a.id;
        b.service = a.service;
        b.className = a.className;
        b.codebase = a.codebase;
        b.displayName = a.displayName;
        b.description = a.description;
        b.factoryMethod = a.factoryMethod;
        b.constructorArgs.addAll(a.constructorArgs);
        b.originAppName = a.originAppName;
        b.types.addAll(a.types);
        b.properties = new SuperProperties();
        b.properties.putAll(a.properties);
        if (a.classpath != null) {
            b.classpath = new URI[a.classpath.length];
            System.arraycopy(a.classpath, 0, b.classpath, 0, a.classpath.length);
        }
        //b.aliases.addAll(a.aliases);

        return b;
    }

    private void checkUnitDataSourceRefs(final PersistenceUnit unit) throws OpenEJBException {
        final Properties required = new Properties();

        // check that non-jta-data-source does NOT point to a JtaManaged=true datasource

        required.put("JtaManaged", "true");

        final String invalidNonJta = findResourceId(unit.getNonJtaDataSource(), "DataSource", required, null);

        if (invalidNonJta != null) {
            throw new OpenEJBException("PeristenceUnit " +
                unit.getName() +
                " <non-jta-data-source> points to a jta managed Resource.  Update Resource \"" +
                invalidNonJta +
                "\" to \"JtaManaged=false\", use a different Resource, or delete the <non-jta-data-source> element and a default will be supplied if possible.");
        }

        // check that jta-data-source does NOT point to a JtaManaged=false datasource

        required.put("JtaManaged", "false");

        final String invalidJta = findResourceId(unit.getJtaDataSource(), "DataSource", required, null);

        if (invalidJta != null) {
            throw new OpenEJBException("PeristenceUnit " +
                unit.getName() +
                " <jta-data-source> points to a non jta managed Resource.  Update Resource \"" +
                invalidJta +
                "\" to \"JtaManaged=true\", use a different Resource, or delete the <jta-data-source> element and a default will be supplied if possible.");
        }
    }

    private String findResourceProviderId(String resourceId) throws OpenEJBException {
        if (resourceId == null) {
            return null;
        }

        if (ServiceUtils.hasServiceProvider(resourceId)) {
            return resourceId;
        }

        if (resourceId.startsWith("java:")) { // can be an absolute path
            String jndi = resourceId.substring("java:".length());
            if (jndi.startsWith("/")) {
                jndi = jndi.substring(1);
            }
            if (ServiceUtils.hasServiceProvider(jndi)) {
                return jndi;
            }
        }

        resourceId = toShortName(resourceId);
        if (ServiceUtils.hasServiceProvider(resourceId)) {
            return resourceId;
        }

        return null;
    }

    private String getResourceId(final String beanName, final String resourceId, final String type, final AppResources appResources) throws OpenEJBException {
        return getResourceId(beanName, resourceId, type, null, appResources);
    }

    private String getResourceId(final String beanName, String resourceId, final String type, final Properties required, AppResources appResources) throws OpenEJBException {
        resourceId = normalizeResourceId(resourceId);

        if (resourceId == null) {
            return null;
        }

        if (appResources == null) {
            appResources = EMPTY_APP_RESOURCES;
        }

        // skip references such as URL which are automatically handled by the server
        if (type != null && ignoredReferenceTypes.contains(type)) {
            return null;
        }

        // check for existing resource with specified resourceId and type and properties
        String id = findResourceId(beanName + '/' + resourceId, type, required, appResources); // check first in app namespace
        if (id != null) {
            return id;
        }

        id = findResourceId(resourceId, type, required, appResources);
        if (id != null) {
            return id;
        }

        // expand search to any type -- may be asking for a reference to a sub-type
        id = findResourceId(resourceId, null, required, appResources);
        if (id != null) {
            return id;
        }

        // app resources
        if (appResources.appId != null && !appResources.appId.isEmpty() && resourceId.startsWith(appResources.appId + '/')) {
            id = findResourceId(resourceId.substring(appResources.appId.length() + 1), type, required, appResources);
            if (id != null) {
                return id;
            }
        }

        // throw an exception or log an error
        final String shortName = toShortName(resourceId);
        final String message = "No existing resource found while attempting to Auto-link unmapped resource-ref '" +
            resourceId +
            "' of type '" +
            type +
            "' for '" +
            beanName +
            "'.  Looked for Resource(id=" +
            resourceId +
            ") and Resource(id=" +
            shortName +
            ")";
        if (!autoCreateResources) {
            throw new OpenEJBException(message);
        }
        logger.debug(message);

        // if there is a provider with the specified name. use it
        if (ServiceUtils.hasServiceProvider(resourceId)) {
            final ResourceInfo resourceInfo = configFactory.configureService(resourceId, ResourceInfo.class);
            return installResource(beanName, resourceInfo);
        } else if (ServiceUtils.hasServiceProvider(shortName)) {
            final ResourceInfo resourceInfo = configFactory.configureService(shortName, ResourceInfo.class);
            return installResource(beanName, resourceInfo);
        }

        // if there are any resources of the desired type, use the first one
        id = firstMatching(beanName, type, required, appResources);
        if (id != null) {
            return id;
        }

        // Auto create a resource using the first provider that can supply a resource of the desired type
        return autoCreateResource(type, required, beanName);
    }

    private String autoCreateResource(final String type, final Properties required, final String beanName) throws OpenEJBException {
        final String resourceId;
        resourceId = ServiceUtils.getServiceProviderId(type, required);
        if (resourceId == null) {
            throw new OpenEJBException("No provider available for resource-ref '" + resourceId + "' of type '" + type + "' for '" + beanName + "'.");
        }
        final ResourceInfo resourceInfo = configFactory.configureService(resourceId, ResourceInfo.class);

        logAutoCreateResource(resourceInfo, type, beanName);
        return installResource(beanName, resourceInfo);
    }

    private void logAutoCreateResource(final ResourceInfo resourceInfo, final String type, final String beanName) {
        logger.info("Auto-creating a Resource with id '" + resourceInfo.id + "' of type '" + type + "' for '" + beanName + "'.");
    }

    private String firstMatching(final String prefix, final String type, final Properties required, final AppResources appResources) {
        final List<String> resourceIds = getResourceIds(appResources, type, required);
        if(resourceIds.isEmpty()){
            return null;
        }

        return Collections.min(resourceIds, new Comparator<String>() { // sort from webapp to global resources
            @Override
            public int compare(final String o1, final String o2) { // don't change global order, just put app scoped resource before others
                if (o1.startsWith(prefix) && o2.startsWith(prefix)) {
                    return resourceIds.indexOf(o1) - resourceIds.indexOf(o2);
                } else if (o1.startsWith(prefix)) {
                    return -1;
                } else if (o2.startsWith(prefix)) {
                    return 1;
                }
                // make it stable with prefixed comparison + keep existing ordering (bck compat)
                return resourceIds.indexOf(o1) - resourceIds.indexOf(o2);
            }
        });
    }

    private String findResourceId(final String resourceId, final String type, final Properties required, final AppResources appResources) {
        if (resourceId == null) {
            return null;
        }
        return findResourceId(getResourceIds(appResources, type, required), resourceId);
    }

    public static String findResourceId(final Collection<String> resourceIds, final String inId) {
        if (inId == null) {
            return null;
        }

        final String resourceId = normalizeResourceId(inId);

        // check for existing resource with specified resourceId
        for (final String id : resourceIds) {
            if (id.equalsIgnoreCase(resourceId)) {
                return id;
            }
        }

        // check for existing resource with shortName
        final String shortName = toShortName(resourceId);
        for (final String id : resourceIds) {
            if (id.equalsIgnoreCase(shortName)) {
                return id;
            }
        }

        if (resourceId.startsWith("osgi:")) {
            return resourceId;
        }
        return null;
    }

    private List<String> getResourceIds(final AppResources appResources, final String type, final Properties required) {
        final List<String> resourceIds;
        resourceIds = new ArrayList<>();
        if (appResources != null) {
            resourceIds.addAll(appResources.getResourceIds(type));
        }
        resourceIds.addAll(configFactory.getResourceIds(type, required));
        return resourceIds;
    }

    private static String toShortName(final String resourceId) {
        // check for an existing resource using the short name (everything ever the final '/')
        return resourceId.replaceFirst(".*/", "");
    }

    private static String normalizeResourceId(String resourceId) {
        if (resourceId == null) {
            return null;
        }

        if (resourceId.startsWith("java:")) {
            resourceId = resourceId.substring("java:".length());
            if (resourceId.startsWith("/")) {
                resourceId = resourceId.substring(1);
            }
        }

        // strip off "java:comp/env"
        if (resourceId.startsWith("comp/env/")) {
            resourceId = resourceId.substring("comp/env/".length());
        }

        // strip off "java:openejb/Resource"
        if (resourceId.startsWith("openejb/Resource/")) {
            resourceId = resourceId.substring("openejb/Resource/".length());
        }

        // strip off "java:openejb/Connector"
        if (resourceId.startsWith("openejb/Connector/")) {
            resourceId = resourceId.substring("openejb/Connector/".length());
        }

        return resourceId;
    }

    private String installResource(final String beanName, final ResourceInfo resourceInfo) throws OpenEJBException {
        final String resourceAdapterId = resourceInfo.properties.getProperty("ResourceAdapter");
        if (resourceAdapterId != null) {
            final String newResourceId = getResourceId(beanName, resourceAdapterId, null, null);
            if (!resourceAdapterId.equals(newResourceId)) {
                resourceInfo.properties.setProperty("ResourceAdapter", newResourceId);
            }
        }
        final String dataSourceId = resourceInfo.properties.getProperty("DataSource");
        if (dataSourceId != null && dataSourceId.length() > 0) {
            final String newResourceId = getResourceId(beanName, dataSourceId, null, null);
            if (!dataSourceId.equals(newResourceId)) {
                resourceInfo.properties.setProperty("DataSource", newResourceId);
            }
        }

        configFactory.install(resourceInfo);
        return resourceInfo.id;
    }

    private String getResourceEnvId(final String beanName, String resourceId, final String type, AppResources appResources) throws OpenEJBException {
        if (resourceId == null) {
            return null;
        }
        if (appResources == null) {
            appResources = EMPTY_APP_RESOURCES;
        }

        // skip references such as URLs which are automatically handled by the server
        if (ignoredReferenceTypes.contains(type)) {
            return null;
        }

        resourceId = normalizeResourceId(resourceId);

        // check for existing resource with specified resourceId
        final List<String> resourceEnvIds = getResourceIds(appResources, type, null);
        for (final String id : resourceEnvIds) {
            if (id.equalsIgnoreCase(resourceId)) {
                return id;
            }
        }

        // throw an exception or log an error
        final String message = "No existing resource found while attempting to Auto-link unmapped resource-env-ref '" +
            resourceId +
            "' of type '" +
            type +
            "' for '" +
            beanName +
            "'.  Looked for Resource(id=" +
            resourceId +
            ")";
        if (!autoCreateResources) {
            throw new OpenEJBException(message);
        }
        logger.debug(message);

        // Auto create a resource using the first provider that can supply a resource of the desired type
        final String providerId = ServiceUtils.getServiceProviderId(type);
        if (providerId == null) {
            // if there are any existing resources of the desired type, use the first one
            if (resourceEnvIds.size() > 0) {
                return resourceEnvIds.get(0);
            }
            throw new OpenEJBException("No provider available for resource-env-ref '" + resourceId + "' of type '" + type + "' for '" + beanName + "'.");
        }

        final Resource resource = new Resource(resourceId, null, providerId);
        resource.getProperties().setProperty("destination", resourceId);

        final ResourceInfo resourceInfo = configFactory.configureService(resource, ResourceInfo.class);
        logAutoCreateResource(resourceInfo, type, beanName);
        return installResource(beanName, resourceInfo);
    }

    private String getUsableContainer(final Class<? extends ContainerInfo> containerInfoType, final EnterpriseBean bean, final AppResources appResources) {
        if (logger.isDebugEnabled()) {
            logger.debug("Searching for usable container for bean: {0}. Available application containers: {1}, available system containers {2}",
                    bean.getEjbName(),
                    getContainerIds(appResources.getContainerInfos()),
                    getContainerIds(configFactory.getContainerInfos())
            );
        }

        if (MessageDrivenBean.class.isInstance(bean)) {
            final MessageDrivenBean messageDrivenBean = (MessageDrivenBean) bean;
            final String messagingType = messageDrivenBean.getMessagingType();

            final List<String> containerIds = appResources.containerIdsByType.get(messagingType);
            if (logger.isDebugEnabled()) {
                logger.debug("Searching for usable container for bean: {0} by messaging type: {1}. Potential application containers: {2}",
                        bean.getEjbName(),
                        messagingType,
                        containerIds == null ? "" : Join.join(",", containerIds));
            }

            if (containerIds != null && !containerIds.isEmpty()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Returning first application container matching by type: {0} - {1}",
                            messagingType,
                            containerIds.get(0));
                }

                return containerIds.get(0);
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Attempting to find a matching container for bean: {0} from application containers {1}",
                    bean.getEjbName(),
                    getContainerIds(appResources.getContainerInfos()));
        }

        String containerInfo = matchContainer(containerInfoType, bean, appResources.getContainerInfos());
        if (containerInfo == null) { // avoid to build configFactory.getContainerInfos() if not needed

            if (logger.isDebugEnabled()) {
                logger.debug("Matching application container not found. Attempting to find a matching container for bean: {0} from system containers {1}",
                        bean.getEjbName(),
                        getContainerIds(appResources.getContainerInfos()));
            }

            containerInfo = matchContainer(containerInfoType, bean, configFactory.getContainerInfos());
        }

        if (containerInfo != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Using container {0} for bean {1}", containerInfo, bean.getEjbName());
            }
            return containerInfo;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("No suitable existing container found for bean {0}", bean.getEjbName());
        }

        return null;
    }

    private String getContainerIds(final Collection<ContainerInfo> containerInfos) {
        final Set<String> containerIds = new HashSet<String>();

        for (final ContainerInfo containerInfo : containerInfos) {
            containerIds.add(containerInfo.id);
        }

        return Join.join(", ", containerIds);
    }

    private String matchContainer(final Class<? extends ContainerInfo> containerInfoType, final EnterpriseBean bean, final Collection<ContainerInfo> list) {
        for (final ContainerInfo containerInfo : list) {
            if (containerInfo.getClass().equals(containerInfoType)) {
                // MDBs must match message listener interface type
                if (MessageDrivenBean.class.isInstance(bean)) {
                    final MessageDrivenBean messageDrivenBean = (MessageDrivenBean) bean;
                    final String messagingType = messageDrivenBean.getMessagingType();

                    if (containerInfo.properties.get("MessageListenerInterface").equals(messagingType)) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Container {0} matches container type {1} and MessageListenerInterface {2} for bean {3}, this container will be used.",
                                    containerInfo.id,
                                    containerInfoType.getName(),
                                    messagingType,
                                    bean.getEjbName());
                        }

                        return containerInfo.id;
                    } else {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Container {0} of type {1} does not have the matching MessageListenerInterface. Bean listener interface is {2}, " +
                                            "container listener interface is {3} for bean {4}. Skipping.",
                                    containerInfo.id,
                                    containerInfoType.getName(),
                                    messagingType,
                                    containerInfo.properties.get("MessageListenerInterface"),
                                    bean.getEjbName());
                        }

                    }
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Container {0} matches container type {1} for bean {2}, this container will be used.",
                                containerInfo.id,
                                containerInfoType.getName(),
                                bean.getEjbName());
                    }

                    return containerInfo.id;
                }
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Skipping container {0} of type {1}", containerInfo.id, containerInfoType.getName());
            }
        }

        return null;
    }

    /*private*/ static class AppResources {

        private String appId;

        @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection"})
        private final Set<String> resourceAdapterIds = new TreeSet<>();
        private final Map<String, List<String>> resourceIdsByType = new TreeMap<>();
        private final Map<String, List<String>> resourceEnvIdsByType = new TreeMap<>();
        private final Map<String, List<String>> containerIdsByType = new TreeMap<>();
        private final Collection<ContainerInfo> containerInfos = new HashSet<>();

        public void dump() {
            if (!logger.isDebugEnabled()) {
                return;
            }
            for (final String s : resourceAdapterIds) {
                logger.debug(appId + " module contains resource adapter id: " + s);
            }
            for (final Map.Entry<String, List<String>> stringListEntry : resourceIdsByType.entrySet()) {
                for (final String value : stringListEntry.getValue()) {
                    logger.debug(appId + " module contains resource type: " + stringListEntry.getKey() + " --> " + value);
                }
            }
            for (final Map.Entry<String, List<String>> stringListEntry : resourceEnvIdsByType.entrySet()) {
                for (final String value : stringListEntry.getValue()) {
                    logger.debug(appId + " module contains resource env type: " + stringListEntry.getKey() + " --> " + value);
                }
            }
            for (final Map.Entry<String, List<String>> stringListEntry : containerIdsByType.entrySet()) {
                for (final String value : stringListEntry.getValue()) {
                    logger.debug(appId + " module contains container type: " + stringListEntry.getKey() + " --> " + value);
                }
            }
        }

        public AppResources() {
        }

        public AppResources(final AppModule appModule, final List<ContainerInfo> containerInfos) {
            this.containerInfos.addAll(containerInfos);
            this.appId = appModule.getModuleId();

            //
            // DEVELOPERS NOTE:  if you change the id generation code here, you must change
            // the id generation code in ConfigurationFactory.configureApplication(AppModule appModule)
            //

            for (final ContainerInfo containerInfo : containerInfos) {
                if (!MdbContainerInfo.class.isInstance(containerInfo)) {
                    continue;
                }
                
                final MdbContainerInfo mdbContainerInfo = MdbContainerInfo.class.cast(containerInfo);
                final String messageListenerInterface = mdbContainerInfo.properties.getProperty("MessageListenerInterface");
                if (messageListenerInterface != null) {
                    List<String> containerIds = containerIdsByType.computeIfAbsent(messageListenerInterface, k -> new ArrayList<>());
                    containerIds.add(containerInfo.id);
                }
            }

            for (final ConnectorModule connectorModule : appModule.getConnectorModules()) {
                final Connector connector = connectorModule.getConnector();

                final ResourceAdapter resourceAdapter = connector.getResourceAdapter();
                if (resourceAdapter.getResourceAdapterClass() != null) {
                    final String resourceAdapterId;
                    if (resourceAdapter.getId() != null) {
                        resourceAdapterId = resourceAdapter.getId();
                    } else {
                        resourceAdapterId = connectorModule.getModuleId() + "RA";
                    }
                    resourceAdapterIds.add(resourceAdapterId);
                }

                final OutboundResourceAdapter outbound = resourceAdapter.getOutboundResourceAdapter();
                if (outbound != null) {
                    for (final ConnectionDefinition connection : outbound.getConnectionDefinition()) {
                        final String type = connection.getConnectionFactoryInterface();

                        final String resourceId;
                        if (connection.getId() != null) {
                            resourceId = connection.getId();
                        } else if (outbound.getConnectionDefinition().size() == 1) {
                            resourceId = connectorModule.getModuleId();
                        } else {
                            resourceId = connectorModule.getModuleId() + "-" + type;
                        }

                        List<String> resourceIds = resourceIdsByType.computeIfAbsent(type, k -> new ArrayList<>());
                        resourceIds.add(resourceId);
                    }
                }

                final InboundResourceadapter inbound = resourceAdapter.getInboundResourceAdapter();
                if (inbound != null) {
                    for (final MessageListener messageListener : inbound.getMessageAdapter().getMessageListener()) {
                        final String type = messageListener.getMessageListenerType();

                        final String containerId;
                        if (messageListener.getId() != null) {
                            containerId = messageListener.getId();
                        } else if (inbound.getMessageAdapter().getMessageListener().size() == 1) {
                            containerId = connectorModule.getModuleId();
                        } else {
                            containerId = connectorModule.getModuleId() + "-" + type;
                        }

                        List<String> containerIds = containerIdsByType.computeIfAbsent(type, k -> new ArrayList<>());
                        containerIds.add(containerId);
                    }
                }

                for (final AdminObject adminObject : resourceAdapter.getAdminObject()) {
                    final String type = adminObject.getAdminObjectInterface();

                    final String resourceEnvId;
                    if (adminObject.getId() != null) {
                        resourceEnvId = adminObject.getId();
                    } else if (resourceAdapter.getAdminObject().size() == 1) {
                        resourceEnvId = connectorModule.getModuleId();
                    } else {
                        resourceEnvId = connectorModule.getModuleId() + "-" + type;
                    }

                    List<String> resourceEnvIds = resourceEnvIdsByType.computeIfAbsent(type, k -> new ArrayList<>());
                    resourceEnvIds.add(resourceEnvId);
                }
            }

            for (final Resource r : appModule.getResources()) {
                final String type = r.getType();
                if (type != null) {
                    final String[] types = type.trim().split(",");
                    for (final String t : types) {
                        List<String> ids = resourceIdsByType.computeIfAbsent(t, k -> new ArrayList<>());
                        ids.add(r.getId());
                        if (r.getJndi() != null) {
                            ids.add(r.getJndi());
                        }
                    }
                }
            }

            //            for (EjbModule module : appModule.getEjbModules()) {
            //                EnterpriseBean[] enterpriseBeans = module.getEjbJar().getEnterpriseBeans();
            //                OpenejbJar openejbJar = module.getOpenejbJar();
            //                Map<String, EjbDeployment> deployments = openejbJar.getDeploymentsByEjbName();
            //
            //                for (DatasourceDefinition ds : module.getDatasources()) {
            //                    final String id = module.getUniqueId() + '/' + ds.getName().replace("java:", "");
            //                    if (resourceIdsByType.get("javax.sql.DataSource") == null) {
            //                        resourceIdsByType.put("javax.sql.DataSource", new ArrayList<String>());
            //                    }
            //                    resourceIdsByType.get("javax.sql.DataSource").add(id);
            //
            //                    for (EnterpriseBean bean : enterpriseBeans) {
            //                        EjbDeployment ejbDeployment = deployments.get(bean.getEjbName());
            //                        for (ResourceRef ref : bean.getResourceRef()) {
            //                            if (ds.getName().equals(ref.getName())) {
            //                                ResourceLink link = new ResourceLink();
            //                                link.setResId(id);
            //                                link.setResRefName(ds.getName());
            //                                ejbDeployment.addResourceLink(link);
            //                            }
            //                        }
            //                    }
            //                }
            //            }
        }

        public Collection<ContainerInfo> getContainerInfos() {
            return containerInfos;
        }

        // needs to be called after merge otherwise we get wrong/missing data
        private void addContainer(final ContainerInfo container) {
            containerInfos.add(container);
            // no need to enrich containerIdsByType here, TODO: see if we can remove containerIdsByType
        }

        public List<String> getResourceIds(final String type) {
            if (type == null) {
                final List<String> allResourceIds = new ArrayList<>();
                for (final List<String> resourceIds : resourceIdsByType.values()) {
                    allResourceIds.addAll(resourceIds);
                }
                return allResourceIds;
            }

            final List<String> resourceIds = resourceIdsByType.get(type);
            if (resourceIds != null) {
                return resourceIds;
            }
            return Collections.emptyList();
        }

        public List<String> getResourceEnvIds(final String type) {
            if (type != null) {
                final List<String> resourceIds = resourceEnvIdsByType.get(type);
                if (resourceIds != null) {
                    return resourceIds;
                }
            }
            return Collections.emptyList();
        }

        public List<String> getContainerIds() {
            final ArrayList<String> ids = new ArrayList<>();
            for (final List<String> list : containerIdsByType.values()) {
                ids.addAll(list);
            }
            return ids;
        }

    }
}
