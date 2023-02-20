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

import jakarta.enterprise.concurrent.ContextServiceDefinition;
import jakarta.interceptor.AroundConstruct;
import org.apache.openejb.BeanContext;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.api.LocalClient;
import org.apache.openejb.api.Proxy;
import org.apache.openejb.api.RemoteClient;
import org.apache.openejb.cdi.CdiBeanInfo;
import org.apache.openejb.config.rules.CheckClasses;
import org.apache.openejb.core.EmptyResourcesClassLoader;
import org.apache.openejb.core.ParentClassLoaderFinder;
import org.apache.openejb.core.TempClassLoader;
import org.apache.openejb.core.webservices.JaxWsUtils;
import org.apache.openejb.dyni.DynamicSubclass;
import org.apache.openejb.jee.*;
import org.apache.openejb.jee.jba.JndiName;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.openejb.loader.JarLocation;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.Classes;
import org.apache.openejb.util.Join;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.SuperProperties;
import org.apache.openejb.util.URLs;
import org.apache.openejb.util.proxy.DynamicProxyImplFactory;
import org.apache.xbean.finder.Annotated;
import org.apache.xbean.finder.AnnotationFinder;
import org.apache.xbean.finder.IAnnotationFinder;
import org.apache.xbean.finder.MetaAnnotatedClass;
import org.apache.xbean.finder.archive.Archive;
import org.apache.xbean.finder.archive.ClassesArchive;

import jakarta.annotation.ManagedBean;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import jakarta.annotation.Resources;
import jakarta.annotation.security.DeclareRoles;
import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.annotation.security.RunAs;
import jakarta.annotation.sql.DataSourceDefinition;
import jakarta.annotation.sql.DataSourceDefinitions;
import jakarta.decorator.Decorator;
import jakarta.decorator.Delegate;
import jakarta.ejb.AccessTimeout;
import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.AfterBegin;
import jakarta.ejb.AfterCompletion;
import jakarta.ejb.ApplicationException;
import jakarta.ejb.Asynchronous;
import jakarta.ejb.BeforeCompletion;
import jakarta.ejb.ConcurrencyManagement;
import jakarta.ejb.DependsOn;
import jakarta.ejb.EJB;
import jakarta.ejb.EJBHome;
import jakarta.ejb.EJBLocalHome;
import jakarta.ejb.EJBLocalObject;
import jakarta.ejb.EJBObject;
import jakarta.ejb.EJBs;
import jakarta.ejb.Init;
import jakarta.ejb.Local;
import jakarta.ejb.LocalBean;
import jakarta.ejb.LocalHome;
import jakarta.ejb.Lock;
import jakarta.ejb.LockType;
import jakarta.ejb.MessageDriven;
import jakarta.ejb.PostActivate;
import jakarta.ejb.PrePassivate;
import jakarta.ejb.Remote;
import jakarta.ejb.RemoteHome;
import jakarta.ejb.Remove;
import jakarta.ejb.Schedule;
import jakarta.ejb.Schedules;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.ejb.Stateful;
import jakarta.ejb.StatefulTimeout;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.ConversationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.NormalScope;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.inject.Model;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.Stereotype;
import jakarta.enterprise.inject.spi.DefinitionException;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.inject.Inject;
import jakarta.interceptor.ExcludeClassInterceptors;
import jakarta.interceptor.ExcludeDefaultInterceptors;
import jakarta.interceptor.Interceptors;
import jakarta.jms.JMSConnectionFactoryDefinition;
import jakarta.jms.JMSConnectionFactoryDefinitions;
import jakarta.jms.JMSDestinationDefinition;
import jakarta.jms.JMSDestinationDefinitions;
import jakarta.jms.Queue;
import jakarta.jws.HandlerChain;
import jakarta.jws.WebService;
import jakarta.persistence.Converter;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceContexts;
import jakarta.persistence.PersistenceUnit;
import jakarta.persistence.PersistenceUnits;
import jakarta.resource.spi.Activation;
import jakarta.resource.spi.AdministeredObject;
import jakarta.resource.spi.ConnectionDefinition;
import jakarta.resource.spi.ConnectionDefinitions;
import jakarta.resource.spi.Connector;
import jakarta.resource.spi.SecurityPermission;
import jakarta.resource.spi.work.WorkContext;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.Provider;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.WebServiceProvider;
import jakarta.xml.ws.WebServiceRef;
import jakarta.xml.ws.WebServiceRefs;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import static java.util.Arrays.asList;

/**
 * @version $Rev$ $Date$
 */
public class AnnotationDeployer implements DynamicDeployer {
    public static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP, AnnotationDeployer.class.getPackage().getName());
    public static final Logger startupLogger = Logger.getInstance(LogCategory.OPENEJB_STARTUP_CONFIG, "org.apache.openejb.util.resources");

    public static final String OPENEJB_JPA_AUTO_SCAN = "openejb.jpa.auto-scan";
    public static final String OPENEJB_JPA_AUTO_SCAN_PACKAGE = "openejb.jpa.auto-scan.package";

    private static final ThreadLocal<DeploymentModule> currentModule = new ThreadLocal<DeploymentModule>();
    private static final Set<String> lookupMissing = new HashSet<String>(2);
    private static final String[] JSF_CLASSES = new String[]{
        "jakarta.faces.application.ResourceDependencies",
        "jakarta.faces.application.ResourceDependency",
        "jakarta.faces.bean.ApplicationScoped",
        "jakarta.faces.bean.CustomScoped",
        "jakarta.faces.bean.ManagedBean",
        "jakarta.faces.bean.ManagedProperty",
        "jakarta.faces.bean.NoneScoped",
        "jakarta.faces.bean.ReferencedBean",
        "jakarta.faces.bean.RequestScoped",
        "jakarta.faces.bean.SessionScoped",
        "jakarta.faces.bean.ViewScoped",
        "jakarta.faces.component.FacesComponent",
        "jakarta.faces.component.UIComponent",
        "jakarta.faces.convert.Converter",
        "jakarta.faces.convert.FacesConverter",
        "jakarta.faces.event.ListenerFor",
        "jakarta.faces.event.ListenersFor",
        "jakarta.faces.event.NamedEvent",
        "jakarta.faces.render.FacesBehaviorRenderer",
        "jakarta.faces.render.FacesRenderer",
        "jakarta.faces.render.Renderer",
        "jakarta.faces.validator.FacesValidator",
        "jakarta.faces.validator.Validator"
    };

    private static final String[] WEB_CLASSES = new String[]{
        // Servlet 3.0
        "jakarta.servlet.annotation.WebServlet",
        "jakarta.servlet.annotation.WebFilter",
        "jakarta.servlet.annotation.WebListener",

        // WebSocket 1.0 (since Tomcat 7.0.47)
        "jakarta.websocket.server.ServerEndpoint",
        "jakarta.websocket.server.ServerApplicationConfig",
        "jakarta.websocket.Endpoint"
    };

    private static final Collection<String> API_CLASSES = new ArrayList<String>(WEB_CLASSES.length + JSF_CLASSES.length);

    static {
        API_CLASSES.addAll(Arrays.asList(JSF_CLASSES));
        API_CLASSES.addAll(Arrays.asList(WEB_CLASSES));
    }

    public static final Set<String> knownResourceEnvTypes = new TreeSet<String>(Arrays.asList(
        "jakarta.ejb.EJBContext",
        "jakarta.ejb.SessionContext",
        "jakarta.ejb.EntityContext",
        "jakarta.ejb.MessageDrivenContext",
        "jakarta.transaction.UserTransaction",
        "jakarta.jms.Queue",
        "jakarta.jms.Topic",
        "jakarta.xml.ws.WebServiceContext",
        "jakarta.ejb.TimerService",
        "jakarta.enterprise.inject.spi.BeanManager",
        "jakarta.validation.Validator",
        "jakarta.validation.ValidatorFactory"
    ));

    public static final Set<String> knownEnvironmentEntries = new TreeSet<String>(Arrays.asList(
        "boolean", "java.lang.Boolean",
        "char", "java.lang.Character",
        "byte", "java.lang.Byte",
        "short", "java.lang.Short",
        "int", "java.lang.Integer",
        "long", "java.lang.Long",
        "float", "java.lang.Float",
        "double", "java.lang.Double",
        "java.lang.String",
        "java.lang.Class"
    ));

    private final DiscoverAnnotatedBeans discoverAnnotatedBeans;
    private final ProcessAnnotatedBeans processAnnotatedBeans;
    private final EnvEntriesPropertiesDeployer envEntriesPropertiesDeployer;
    private final MBeanDeployer mBeanDeployer;
    private final BuiltInEnvironmentEntries builtInEnvironmentEntries;
    private final MergeWebappJndiContext mergeWebappJndiContext;

    public AnnotationDeployer() {
        discoverAnnotatedBeans = new DiscoverAnnotatedBeans();
        processAnnotatedBeans = new ProcessAnnotatedBeans(SystemInstance.get().getOptions().get("openejb.jaxws.add-remote", false));
        builtInEnvironmentEntries = new BuiltInEnvironmentEntries(SystemInstance.get().getOptions().get("openejb.environment.default", false));
        envEntriesPropertiesDeployer = new EnvEntriesPropertiesDeployer();
        mBeanDeployer = new MBeanDeployer();
        mergeWebappJndiContext = new MergeWebappJndiContext();
    }

    public AppModule deploy(AppModule appModule) throws OpenEJBException {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(appModule.getClassLoader());
        setModule(appModule);
        try {
            appModule = discoverAnnotatedBeans.deploy(appModule);
            appModule = envEntriesPropertiesDeployer.deploy(appModule);
            appModule = mergeWebappJndiContext.deploy(appModule);
            appModule = builtInEnvironmentEntries.deploy(appModule);
            appModule = processAnnotatedBeans.deploy(appModule);
            appModule = mergeWebappJndiContext.deploy(appModule);
            appModule = mBeanDeployer.deploy(appModule);
            return appModule;
        } finally {
            envEntriesPropertiesDeployer.resetAdditionalEnvEntries();
            Thread.currentThread().setContextClassLoader(classLoader);
            removeModule();
        }
    }

    // TODO Remove this section.  It's called by some code in the assembler.
    // The scanning portion should be completed prior to this point
    public void deploy(final CdiBeanInfo beanInfo) throws OpenEJBException {
        this.processAnnotatedBeans.deploy(beanInfo);
    }

    public WebModule deploy(WebModule webModule) throws OpenEJBException {
        setModule(webModule);
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(webModule.getClassLoader());
        try {
            webModule = discoverAnnotatedBeans.deploy(webModule);
            webModule = envEntriesPropertiesDeployer.deploy(webModule);
            webModule = processAnnotatedBeans.deploy(webModule);
            return webModule;
        } finally {
            envEntriesPropertiesDeployer.resetAdditionalEnvEntries();
            Thread.currentThread().setContextClassLoader(classLoader);
            removeModule();
        }
    }

    public static DeploymentModule getModule() {
        return currentModule.get();
    }

    private static void setModule(final DeploymentModule module) {
        currentModule.set(module);
    }

    private static void removeModule() {
        currentModule.remove();
    }

    private static ValidationContext getValidationContext() {
        return getModule().getValidation();
    }

    private static void mergeApplicationExceptionAnnotation(final AssemblyDescriptor assemblyDescriptor, final Class<?> exceptionClass, final ApplicationException annotation) {
        final org.apache.openejb.jee.ApplicationException applicationException = assemblyDescriptor.getApplicationException(exceptionClass);
        if (applicationException.getRollback() == null) {
            applicationException.setRollback(annotation.rollback());
        }
        if (applicationException.getInherited() == null) {
            applicationException.setInherited(annotation.inherited());
        }
    }

    public static boolean isKnownEnvironmentEntryType(final Class type) {
        return knownEnvironmentEntries.contains(type.getName()) || type.isEnum();
    }

    public static boolean isShareableJNDINamespace(final String jndiName) {
        return jndiName.startsWith("java:global/") || jndiName.startsWith("java:app/") || jndiName.startsWith("java:module/");
    }

    public static class DiscoverAnnotatedBeans implements DynamicDeployer {
        public AppModule deploy(AppModule appModule) throws OpenEJBException {
            if (!appModule.isWebapp() && !appModule.getWebModules().isEmpty()) { // need to scan for jsf stuff at least
                try {
                    appModule.setEarLibFinder(FinderFactory.createFinder(appModule));
                } catch (final Exception e) {
                    logger.error("Can't create a finder for ear libs", e);
                }
            }

            for (final EjbModule ejbModule : appModule.getEjbModules()) {
                ejbModule.initAppModule(appModule);
                setModule(ejbModule);
                try {
                    deploy(ejbModule);
                } finally {
                    removeModule();
                }
            }
            for (final ClientModule clientModule : appModule.getClientModules()) {
                clientModule.initAppModule(appModule);
                setModule(clientModule);
                try {
                    deploy(clientModule);
                } finally {
                    removeModule();
                }
            }
            for (final ConnectorModule connectorModule : appModule.getConnectorModules()) {
                connectorModule.initAppModule(appModule);
                setModule(connectorModule);
                try {
                    deploy(connectorModule);
                } finally {
                    removeModule();
                }
            }
            for (final WebModule webModule : appModule.getWebModules()) { // here we scan by inheritance so great to keep it last
                webModule.initAppModule(appModule);
                setModule(webModule);
                try {
                    deploy(webModule);
                } finally {
                    removeModule();
                }
            }
            final AdditionalBeanDiscoverer discoverer = SystemInstance.get().getComponent(AdditionalBeanDiscoverer.class);
            if (discoverer != null) {
                appModule = discoverer.discover(appModule);
            }
            return appModule;
        }

        public ClientModule deploy(final ClientModule clientModule) throws OpenEJBException {

            if (clientModule.getApplicationClient() == null) {
                clientModule.setApplicationClient(new ApplicationClient());
            }

            // Lots of jars have main classes so this might not even be an app client.
            // We're not going to scrape it for @LocalClient or @RemoteClient annotations
            // unless they flag us specifically by adding a META-INF/application-client.xml
            //
            // ClientModules that already have a AnnotationFinder have been generated automatically
            // from an EjbModule, so we don't skip those ever.
            if (clientModule.getFinder() == null && clientModule.getAltDDs().containsKey("application-client.xml")) {
                if (clientModule.getApplicationClient() != null && clientModule.getApplicationClient().isMetadataComplete()) {
                    return clientModule;
                }
            }


            IAnnotationFinder finder = clientModule.getFinder();

            if (finder == null) {
                try {
                    finder = FinderFactory.createFinder(clientModule);
                } catch (final MalformedURLException e) {
                    startupLogger.warning("startup.scrapeFailedForClientModule.url", clientModule.getJarLocation());
                    return clientModule;
                } catch (final Exception e) {
                    startupLogger.warning("startup.scrapeFailedForClientModule", e, clientModule.getJarLocation());
                    return clientModule;
                }
            }

            // This method is also called by the deploy(EjbModule) method to see if those
            // modules have any @LocalClient or @RemoteClient classes
            for (final Annotated<Class<?>> clazz : finder.findMetaAnnotatedClasses(LocalClient.class)) {
                clientModule.getLocalClients().add(clazz.get().getName());
            }

            for (final Annotated<Class<?>> clazz : finder.findMetaAnnotatedClasses(RemoteClient.class)) {
                clientModule.getRemoteClients().add(clazz.get().getName());
            }

            if (clientModule.getApplicationClient() == null) {
                if (clientModule.getRemoteClients().size() > 0 || clientModule.getLocalClients().size() > 0) {
                    clientModule.setApplicationClient(new ApplicationClient());
                }
            }

            return clientModule;
        }

        public ConnectorModule deploy(final ConnectorModule connectorModule) throws OpenEJBException {

            org.apache.openejb.jee.Connector connector = connectorModule.getConnector();
            if (connector == null) {
                connector = new org.apache.openejb.jee.Connector();
            }

            // JCA 1.6 - 18.3.1 do not look at annotations if the provided connector
            // deployment descriptor is "meta-data complete".

            float specVersion = 0;
            try {
                specVersion = Float.parseFloat(connector.getVersion());
            } catch (final Exception e) {
                // no-op
            }

            if (specVersion < 1.6 || Boolean.TRUE.equals(connector.isMetadataComplete())) {
                return connectorModule;
            }


            IAnnotationFinder finder = connectorModule.getFinder();
            if (finder == null) {
                try {
                    finder = FinderFactory.createFinder(connectorModule);
                    connectorModule.setFinder(finder);
                } catch (final Exception e) {
                    // TODO: some sort of error
                    return connectorModule;
                }
            }

            final List<Class<?>> connectorClasses = finder.findAnnotatedClasses(Connector.class);

            // are we allowed to have more than one connector class? Not without a deployment descriptor
            if (connector.getResourceAdapter() == null || connector.getResourceAdapter().getResourceAdapterClass() == null || connector.getResourceAdapter().getResourceAdapterClass().length() == 0) {
                if (connectorClasses.size() == 0) { //NOPMD
                    // TODO: fail some validation here too
                }

                if (connectorClasses.size() > 1) { //NOPMD
                    // too many connector classes, this is against the spec
                    // TODO: something like connectorModule.getValidation().fail(ejbName, "abstractAnnotatedAsBean", annotationClass.getSimpleName(), beanClass.get().getName());
                }
            }

            Class<?> connectorClass = null;
            if (connectorClasses.size() == 1) {
                connectorClass = connectorClasses.get(0);
            }

            if (connectorClasses.size() > 1) {
                for (final Class<?> cls : connectorClasses) {
                    if (cls.getName().equals(connector.getResourceAdapter().getResourceAdapterClass())) {
                        connectorClass = cls;
                        break;
                    }
                }
            }

            if (connectorClass != null) {
                if (connector.getResourceAdapter() == null) {
                    connector.setResourceAdapter(new ResourceAdapter());
                }

                if (connector.getResourceAdapter().getResourceAdapterClass() == null || connector.getResourceAdapter().getResourceAdapterClass().length() == 0) {
                    connector.getResourceAdapter().setResourceAdapterClass(connectorClass.getName());
                }

                final Connector connectorAnnotation = connectorClass.getAnnotation(Connector.class);

                connector.setDisplayNames(getTexts(connector.getDisplayNames(), connectorAnnotation.displayName()));
                connector.setDescriptions(getTexts(connector.getDescriptions(), connectorAnnotation.description()));

                connector.setEisType(getString(connector.getEisType(), connectorAnnotation.eisType()));
                connector.setVendorName(getString(connector.getVendorName(), connectorAnnotation.vendorName()));
                connector.setResourceAdapterVersion(getString(connector.getResourceAdapterVersion(), connectorAnnotation.version()));

                if (connector.getIcons().isEmpty()) {
                    final int smallIcons = connectorAnnotation.smallIcon().length;
                    final int largeIcons = connectorAnnotation.largeIcon().length;

                    for (int i = 0; i < smallIcons && i < largeIcons; i++) {
                        final Icon icon = new Icon();
                        // locale can't be specified in the annotation and it is en by default
                        // so on other systems it doesn't work because Icon return the default locale
                        icon.setLang(Locale.getDefault().getLanguage());
                        if (i < smallIcons) {
                            icon.setSmallIcon(connectorAnnotation.smallIcon()[i]);
                        }

                        if (i < largeIcons) {
                            icon.setLargeIcon(connectorAnnotation.largeIcon()[i]);
                        }

                        connector.getIcons().add(icon);
                    }
                }

                if (connector.getLicense() == null) {
                    final License license = new License();
                    connector.setLicense(license);
                    license.setLicenseRequired(connectorAnnotation.licenseRequired());
                }

                connector.getLicense().setDescriptions(getTexts(connector.getLicense().getDescriptions(), connectorAnnotation.licenseDescription()));


                final SecurityPermission[] annotationSecurityPermissions = connectorAnnotation.securityPermissions();
                final List<org.apache.openejb.jee.SecurityPermission> securityPermission = connector.getResourceAdapter().getSecurityPermission();
                if (securityPermission == null || securityPermission.size() == 0) {
                    for (final SecurityPermission sp : annotationSecurityPermissions) {
                        final org.apache.openejb.jee.SecurityPermission permission = new org.apache.openejb.jee.SecurityPermission();
                        permission.setSecurityPermissionSpec(sp.permissionSpec());
                        permission.setDescriptions(stringsToTexts(sp.description()));
                        securityPermission.add(permission);
                    }
                }

                final Class<? extends WorkContext>[] annotationRequiredWorkContexts = connectorAnnotation.requiredWorkContexts();
                final List<String> requiredWorkContext = connector.getRequiredWorkContext();
                if (requiredWorkContext.size() == 0) {
                    for (final Class<? extends WorkContext> cls : annotationRequiredWorkContexts) {
                        requiredWorkContext.add(cls.getName());
                    }
                }

                OutboundResourceAdapter outboundResourceAdapter = connector.getResourceAdapter().getOutboundResourceAdapter();
                if (outboundResourceAdapter == null) {
                    outboundResourceAdapter = new OutboundResourceAdapter();
                    connector.getResourceAdapter().setOutboundResourceAdapter(outboundResourceAdapter);
                }

                final List<AuthenticationMechanism> authenticationMechanisms = outboundResourceAdapter.getAuthenticationMechanism();
                final jakarta.resource.spi.AuthenticationMechanism[] authMechanisms = connectorAnnotation.authMechanisms();
                if (authenticationMechanisms.size() == 0) {
                    for (final jakarta.resource.spi.AuthenticationMechanism am : authMechanisms) {
                        final AuthenticationMechanism authMechanism = new AuthenticationMechanism();
                        authMechanism.setAuthenticationMechanismType(am.authMechanism());
                        authMechanism.setCredentialInterface(am.credentialInterface().toString());
                        authMechanism.setDescriptions(stringsToTexts(am.description()));

                        authenticationMechanisms.add(authMechanism);
                    }
                }

                if (outboundResourceAdapter.getTransactionSupport() == null) {
                    outboundResourceAdapter.setTransactionSupport(TransactionSupportType.fromValue(connectorAnnotation.transactionSupport().toString()));
                }

                if (outboundResourceAdapter.isReauthenticationSupport() == null) {
                    outboundResourceAdapter.setReauthenticationSupport(connectorAnnotation.reauthenticationSupport());
                }
            }

            // process @ConnectionDescription(s)
            List<Class<?>> classes = finder.findAnnotatedClasses(ConnectionDefinitions.class);
            for (final Class<?> cls : classes) {
                final ConnectionDefinitions connectionDefinitionsAnnotation = cls.getAnnotation(ConnectionDefinitions.class);
                final ConnectionDefinition[] definitions = connectionDefinitionsAnnotation.value();

                for (final ConnectionDefinition definition : definitions) {
                    processConnectionDescription(connector.getResourceAdapter(), definition, cls);
                }
            }

            classes = finder.findAnnotatedClasses(ConnectionDefinition.class);
            for (final Class<?> cls : classes) {
                final ConnectionDefinition connectionDefinitionAnnotation = cls.getAnnotation(ConnectionDefinition.class);
                processConnectionDescription(connector.getResourceAdapter(), connectionDefinitionAnnotation, cls);
            }


            InboundResourceadapter inboundResourceAdapter = connector.getResourceAdapter().getInboundResourceAdapter();
            if (inboundResourceAdapter == null) {
                inboundResourceAdapter = new InboundResourceadapter();
                connector.getResourceAdapter().setInboundResourceAdapter(inboundResourceAdapter);
            }

            MessageAdapter messageAdapter = inboundResourceAdapter.getMessageAdapter();
            if (messageAdapter == null) {
                messageAdapter = new MessageAdapter();
                inboundResourceAdapter.setMessageAdapter(messageAdapter);
            }

            classes = finder.findAnnotatedClasses(Activation.class);
            for (final Class<?> cls : classes) {
                MessageListener messageListener = null;
                final Activation activationAnnotation = cls.getAnnotation(Activation.class);

                final List<MessageListener> messageListeners = messageAdapter.getMessageListener();
                for (final MessageListener ml : messageListeners) {
                    if (cls.getName().equals(ml.getActivationSpec().getActivationSpecClass())) {
                        messageListener = ml;
                        break;
                    }
                }

                if (messageListener == null) {
                    final Class<?>[] listeners = activationAnnotation.messageListeners();
                    for (final Class<?> listener : listeners) {
                        messageAdapter.addMessageListener(new MessageListener(listener.getName(), cls.getName()));
                    }
                }
            }

            classes = finder.findAnnotatedClasses(AdministeredObject.class);
            final List<AdminObject> adminObjects = connector.getResourceAdapter().getAdminObject();
            for (final Class<?> cls : classes) {
                final AdministeredObject administeredObjectAnnotation = cls.getAnnotation(AdministeredObject.class);
                final Class[] adminObjectInterfaces = administeredObjectAnnotation.adminObjectInterfaces();

                AdminObject adminObject = null;
                for (final AdminObject admObj : adminObjects) {
                    if (admObj.getAdminObjectClass().equals(cls.getName())) {
                        adminObject = admObj;
                    }
                }

                if (adminObject == null) {
                    for (final Class iface : adminObjectInterfaces) {
                        final AdminObject newAdminObject = new AdminObject();
                        newAdminObject.setAdminObjectClass(cls.getName());
                        newAdminObject.setAdminObjectInterface(iface.getName());
                        adminObjects.add(newAdminObject);
                    }
                }
            }

            // need to make a list of classes to process for config properties

            // resource adapter
            final String raCls = connector.getResourceAdapter().getResourceAdapterClass();
            process(connectorModule.getClassLoader(), raCls, connector.getResourceAdapter());

            // managedconnectionfactory
            if (connector.getResourceAdapter() != null && connector.getResourceAdapter().getOutboundResourceAdapter() != null) {
                final List<org.apache.openejb.jee.ConnectionDefinition> connectionDefinitions = connector.getResourceAdapter().getOutboundResourceAdapter().getConnectionDefinition();
                for (final org.apache.openejb.jee.ConnectionDefinition connectionDefinition : connectionDefinitions) {
                    process(connectorModule.getClassLoader(), connectionDefinition.getManagedConnectionFactoryClass(), connectionDefinition);
                }
            }

            // administeredobject
            if (connector.getResourceAdapter() != null) {
                final List<AdminObject> raAdminObjects = connector.getResourceAdapter().getAdminObject();
                for (final AdminObject raAdminObject : raAdminObjects) {
                    process(connectorModule.getClassLoader(), raAdminObject.getAdminObjectClass(), raAdminObject);
                }
            }

            // activationspec
            if (connector.getResourceAdapter() != null && connector.getResourceAdapter().getInboundResourceAdapter() != null && connector.getResourceAdapter().getInboundResourceAdapter().getMessageAdapter() != null) {
                final List<MessageListener> messageListeners = connector.getResourceAdapter().getInboundResourceAdapter().getMessageAdapter().getMessageListener();
                for (final MessageListener messageListener : messageListeners) {
                    final ActivationSpec activationSpec = messageListener.getActivationSpec();
                    process(connectorModule.getClassLoader(), activationSpec.getActivationSpecClass(), activationSpec);
                }
            }

            return connectorModule;
        }

        void process(final ClassLoader cl, final String cls, final Object object) {

            List<ConfigProperty> configProperties = null;
            try {
                // grab a list of ConfigProperty objects
                configProperties = (List<ConfigProperty>) object.getClass().getDeclaredMethod("getConfigProperty").invoke(object);
            } catch (final Exception e) {
                // no-op
            }

            if (configProperties == null) {
                // can't get config properties
                return;
            }

            ClassLoader classLoader = cl;
            if (classLoader == null) {
                classLoader = Thread.currentThread().getContextClassLoader();
            }

            final List<String> allowedTypes = Arrays.asList(new String[]{Boolean.class.getName(), String.class.getName(), Integer.class.getName(), Double.class.getName(), Byte.class.getName(), Short.class.getName(), Long.class.getName(), Float.class.getName(), Character.class.getName()});

            try {
                final Class<?> clazz = classLoader.loadClass(realClassName(cls));
                final Object o = clazz.newInstance();

                // add any introspected properties
                final BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
                final PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();

                for (final PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                    final String name = propertyDescriptor.getName();
                    Class<?> type = propertyDescriptor.getPropertyType();
                    if (type == null) {
                        continue;
                    }
                    if (type.isPrimitive()) {
                        type = getWrapper(type.getName());
                    }

                    if (!allowedTypes.contains(type.getName())) {
                        continue;
                    }

                    if (!containsConfigProperty(configProperties, name)) {
                        final ConfigProperty configProperty = new ConfigProperty();
                        configProperties.add(configProperty);

                        Object value = null;
                        if (propertyDescriptor.getReadMethod() != null) {
                            try {
                                value = propertyDescriptor.getReadMethod().invoke(o);
                            } catch (final Exception e) {
                                // no-op
                            }
                        }

                        final Method write = propertyDescriptor.getWriteMethod();
                        jakarta.resource.spi.ConfigProperty annotation = null;
                        if (write != null) {
                            annotation = write.getAnnotation(jakarta.resource.spi.ConfigProperty.class);
                            if (annotation == null) {
                                try {
                                    // if there's no annotation on the setter, we'll try and scrape one off the field itself (assuming the same name)
                                    annotation = clazz.getDeclaredField(name).getAnnotation(jakarta.resource.spi.ConfigProperty.class);
                                } catch (final Exception ignored) {
                                    // no-op : getDeclaredField() throws exceptions and does not return null
                                }
                            }
                        }

                        configProperty.setConfigPropertyName(name);
                        configProperty.setConfigPropertyType(getConfigPropertyType(annotation, type));
                        if (value != null) {
                            configProperty.setConfigPropertyValue(value.toString());
                        }

                        if (annotation != null) {
                            if (annotation.defaultValue() != null && annotation.defaultValue().length() > 0) {
                                configProperty.setConfigPropertyValue(annotation.defaultValue());
                            }
                            configProperty.setConfigPropertyConfidential(annotation.confidential());
                            configProperty.setConfigPropertyIgnore(annotation.ignore());
                            configProperty.setConfigPropertySupportsDynamicUpdates(annotation.supportsDynamicUpdates());
                            configProperty.setDescriptions(stringsToTexts(annotation.description()));
                        }
                    }
                }

                // add any annotated fields we haven't already picked up
                final Field[] declaredFields = clazz.getDeclaredFields();
                for (final Field field : declaredFields) {
                    final jakarta.resource.spi.ConfigProperty annotation = field.getAnnotation(jakarta.resource.spi.ConfigProperty.class);

                    final String name = field.getName();
                    Object value = null;
                    try {
                        value = field.get(o);
                    } catch (final Exception e) {
                        // no-op
                    }

                    if (!containsConfigProperty(configProperties, name)) {
                        final String type = getConfigPropertyType(annotation, field.getType());

                        if (type != null) {
                            final ConfigProperty configProperty = new ConfigProperty();
                            configProperties.add(configProperty);

                            configProperty.setConfigPropertyName(name);
                            configProperty.setConfigPropertyType(type);
                            if (value != null) {
                                configProperty.setConfigPropertyValue(value.toString());
                            }

                            if (annotation != null) {
                                if (annotation.defaultValue() != null) {
                                    configProperty.setConfigPropertyValue(annotation.defaultValue());
                                }
                                configProperty.setConfigPropertyConfidential(annotation.confidential());
                                configProperty.setConfigPropertyIgnore(annotation.ignore());
                                configProperty.setConfigPropertySupportsDynamicUpdates(annotation.supportsDynamicUpdates());
                            }
                        }
                    }
                }
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }

        private String getConfigPropertyType(final jakarta.resource.spi.ConfigProperty annotation, final Class<?> type) {
            Class<?> t = annotation == null ? null : annotation.type();
            if (t == null && type != null) {
                return type.getName();
            } else if (t == null) {
                return null;
            }

            if (t.equals(Object.class)) {
                t = type;
            }
            if (t == null) { // t == null && type == null
                return null;
            }

            if (t.isPrimitive()) {
                t = getWrapper(t.getName());
            }

            return t.getName();
        }

        private boolean containsConfigProperty(final List<ConfigProperty> configProperties, final String name) {
            for (final ConfigProperty configProperty : configProperties) {
                if (configProperty.getConfigPropertyName().equals(name)) {
                    return true;
                }
            }

            return false;
        }

        private Class<?> getWrapper(final String primitiveType) {
            final Map<String, Class<?>> builtInMap = new HashMap<>();
            {
                builtInMap.put("int", Integer.class);
                builtInMap.put("long", Long.class);
                builtInMap.put("double", Double.class);
                builtInMap.put("float", Float.class);
                builtInMap.put("boolean", Boolean.class);
                builtInMap.put("char", Character.class);
                builtInMap.put("byte", Byte.class);
                builtInMap.put("void", Void.class);
                builtInMap.put("short", Short.class);
            }

            return builtInMap.get(primitiveType);
        }

        private void processConnectionDescription(final ResourceAdapter resourceAdapter, final ConnectionDefinition connectionDefinitionAnnotation, final Class<?> cls) {
            // try and find the managed connection factory

            OutboundResourceAdapter outboundResourceAdapter = resourceAdapter.getOutboundResourceAdapter();
            if (outboundResourceAdapter == null) {
                outboundResourceAdapter = new OutboundResourceAdapter();
                resourceAdapter.setOutboundResourceAdapter(outboundResourceAdapter);
            }

            final List<org.apache.openejb.jee.ConnectionDefinition> connectionDefinition = outboundResourceAdapter.getConnectionDefinition();

            org.apache.openejb.jee.ConnectionDefinition definition = null;
            for (final org.apache.openejb.jee.ConnectionDefinition cd : connectionDefinition) {
                if (cd.getManagedConnectionFactoryClass().equals(cls.getName())) {
                    definition = cd;
                    break;
                }
            }

            if (definition == null) {
                definition = new org.apache.openejb.jee.ConnectionDefinition();
                outboundResourceAdapter.getConnectionDefinition().add(definition);
            }

            if (definition.getManagedConnectionFactoryClass() == null) {
                definition.setManagedConnectionFactoryClass(cls.getName());
            }

            if (definition.getConnectionInterface() == null) {
                definition.setConnectionInterface(connectionDefinitionAnnotation.connection().getName());
            }

            if (definition.getConnectionImplClass() == null) {
                definition.setConnectionImplClass(connectionDefinitionAnnotation.connectionImpl().getName());
            }

            if (definition.getConnectionFactoryInterface() == null) {
                definition.setConnectionFactoryInterface(connectionDefinitionAnnotation.connectionFactory().getName());
            }

            if (definition.getConnectionFactoryImplClass() == null) {
                definition.setConnectionFactoryImplClass(connectionDefinitionAnnotation.connectionFactoryImpl().getName());
            }
        }

        private Text[] stringsToTexts(final String[] strings) {
            if (strings == null) {
                return null;
            }

            final Text[] result = new Text[strings.length];
            for (int i = 0; i < result.length; i++) {
                result[i] = new Text();
                result[i].setValue(strings[i]);
            }

            return result;
        }

        private String getString(final String descriptorString, final String annotationString) {
            if (descriptorString != null && descriptorString.length() > 0) {
                return descriptorString;
            }

            if (annotationString != null && annotationString.length() > 0) {
                return annotationString;
            }

            return null;
        }

        private Text[] getTexts(final Text[] originalTexts, final String[] newStrings) {
            if (newStrings != null && newStrings.length > 0 && (originalTexts == null || originalTexts.length == 0)) {
                final Text[] texts = new Text[newStrings.length];
                for (int i = 0; i < newStrings.length; i++) {
                    texts[i] = new Text(null, newStrings[i]);
                }

                return texts;
            } else {
                return originalTexts;
            }
        }

        public WebModule deploy(final WebModule webModule) throws OpenEJBException {
            WebApp webApp = webModule.getWebApp();
            if (webApp != null && webApp.isMetadataComplete()) {
                return webModule;
            }

            try {
                if (webModule.getFinder() == null) {
                    webModule.setFinder(FinderFactory.createFinder(webModule));
                }
            } catch (final Exception e) {
                startupLogger.warning("Unable to scrape for @WebService or @WebServiceProvider annotations. AnnotationFinder failed.", e);
                return webModule;
            }

            if (webApp == null) {
                webApp = new WebApp();
                webModule.setWebApp(webApp);
            }

            final List<String> existingServlets = new ArrayList<>();
            for (final Servlet servlet : webApp.getServlet()) {
                if (servlet.getServletClass() != null) {
                    existingServlets.add(servlet.getServletClass());
                }
            }

            final IAnnotationFinder finder = webModule.getFinder();
            final List<Class> classes = new ArrayList<>();
            classes.addAll(finder.findAnnotatedClasses(WebService.class));
            classes.addAll(finder.findAnnotatedClasses(WebServiceProvider.class));

            for (final Class<?> webServiceClass : classes) {
                // If this class is also annotated @Stateless or @Singleton, we should skip it
                if (webServiceClass.isAnnotationPresent(Singleton.class) || webServiceClass.isAnnotationPresent(Stateless.class)) {
                    webModule.getEjbWebServices().add(webServiceClass.getName());
                    continue;
                }

                final int modifiers = webServiceClass.getModifiers();
                if (!Modifier.isPublic(modifiers) || Modifier.isFinal(modifiers) || Modifier.isAbstract(modifiers)) {
                    continue;
                }

                if (existingServlets.contains(webServiceClass.getName())) {
                    continue;
                }

                // create webApp and webservices objects if they don't exist already

                // add new <servlet/> element
                final Servlet servlet = new Servlet();
                servlet.setServletName(webServiceClass.getName());
                servlet.setServletClass(webServiceClass.getName());
                final ParamValue param = new ParamValue();
                param.setParamName("openejb-internal");
                param.setParamValue("true");
                servlet.getInitParam().add(param);
                webApp.getServlet().add(servlet);
            }

           /*
            * REST
            */
            boolean restHandledByTheWebApp;
            try {
                restHandledByTheWebApp = webModule.getClassLoader().loadClass(Application.class.getName()) != Application.class;
            } catch (final Throwable e) { // ClassNotFoundException or NoClassDefFoundError
                restHandledByTheWebApp = false;
            }

            if (!restHandledByTheWebApp) {
                // get by annotations
                webModule.getRestClasses().addAll(findRestClasses(webModule, finder));
                addJaxRsProviders(finder, webModule.getJaxrsProviders(), Provider.class);

                // CXF actually does this in its own CDI setup - org.apache.cxf.cdi.JAXRSCdiResourceExtension#collect(jakarta.enterprise.inject.spi.ProcessBean<T>)
                //addJaxRsProviders(finder, webModule.getJaxrsProviders(), Path.class);

                // Applications with a default constructor
                // findSubclasses will not work by default to gain a lot of time
                // look FinderFactory for the flag to activate it or
                // use @ApplicationPath("/")
                final List<Class<? extends Application>> applications = finder.findSubclasses(Application.class);
                for (final Class<? extends Application> app : applications) {
                    addRestApplicationIfPossible(webModule, app);
                }

                // look for ApplicationPath, it will often return the same than the previous one
                // but without finder.link() invocation it still works
                // so it can save a lot of startup time
                final List<Annotated<Class<?>>> applicationsByAnnotation = finder.findMetaAnnotatedClasses(ApplicationPath.class);
                for (final Annotated<Class<?>> annotatedApp : applicationsByAnnotation) {
                    final Class<?> app = annotatedApp.get();
                    if (!Application.class.isAssignableFrom(app)) {
                        logger.error("class '" + app.getName() + "' is annotated with @ApplicationPath but doesn't implement " + Application.class.getName());
                        continue;
                    }

                    addRestApplicationIfPossible(webModule, (Class<? extends Application>) app);
                }
            }

            /*
             * JSF
             */

            // we need to look for JSF stuff in ear libs (converters...) so get back the finder for this part
            IAnnotationFinder parentFinder = null;
            final AppModule appModule = webModule.getAppModule();
            if (appModule != null) {
                parentFinder = appModule.getEarLibFinder();
            }

            final ClassLoader classLoader = webModule.getClassLoader();
            for (final String jsfClass : JSF_CLASSES) {
                final Class<? extends Annotation> clazz;
                try {
                    clazz = (Class<? extends Annotation>) classLoader.loadClass(jsfClass);
                } catch (final ClassNotFoundException e) {
                    continue;
                }

                final Set<String> convertedClasses = new HashSet<>();

                if (parentFinder != null) {
                    final List<Annotated<Class<?>>> foundParent = parentFinder.findMetaAnnotatedClasses(clazz);
                    for (final Annotated<Class<?>> annotated : foundParent) {
                        convertedClasses.add(annotated.get().getName());
                    }

                    for (final EjbModule module : appModule.getEjbModules()) {
                        // if we are deplying a webapp we don't need to (re)do it
                        // or if this module is another webapp we don't need to look it
                        // otherwise that's a common part of ear we want to scan
                        if (appModule.isWebapp() || module.isWebapp() && !module.getModuleId().equals(webModule.getModuleId())) {
                            continue;
                        }

                        final List<Annotated<Class<?>>> ejbFound = module.getFinder().findMetaAnnotatedClasses(clazz);
                        for (final Annotated<Class<?>> annotated : ejbFound) {
                            convertedClasses.add(annotated.get().getName());
                        }
                    }
                }

                final List<Annotated<Class<?>>> found = finder.findMetaAnnotatedClasses(clazz);
                for (final Annotated<Class<?>> annotated : found) {
                    convertedClasses.add(annotated.get().getName());
                }

                webModule.getJsfAnnotatedClasses().put(jsfClass, convertedClasses);
            }

            /*
             * Servlet, Filter, Listener...
             * here we can scan by inheritance so do it last
             */

            Map<String, String> urlByClasses = null;
            for (final String apiClassName : WEB_CLASSES) {
                final Class<? extends Annotation> clazz;
                try {
                    clazz = (Class<? extends Annotation>) classLoader.loadClass(apiClassName);
                } catch (final ClassNotFoundException e) {
                    continue;
                }

                if (urlByClasses == null) { // try to reuse scanning info, maybe some better indexing can be a nice idea
                    urlByClasses = FinderFactory.urlByClass(finder);
                }

                final List<Annotated<Class<?>>> found = finder.findMetaAnnotatedClasses(clazz);
                addWebAnnotatedClassInfo(urlByClasses, webModule.getWebAnnotatedClasses(), found);
            }

            if (urlByClasses != null) {
                urlByClasses.clear();
            }

            return webModule;
        }

        private void addJaxRsProviders(final IAnnotationFinder finder, final Collection<String> set, final Class<? extends Annotation> annotation) {
            for (final Annotated<Class<?>> provider : finder.findMetaAnnotatedClasses(annotation)) {
                set.add(provider.get().getName());
            }
        }

        private static void addRestApplicationIfPossible(final WebModule webModule, final Class<? extends Application> app) {
            if (!isInstantiable(app)) {
                return;
            }

            if (app.getConstructors().length == 0) {
                webModule.getRestApplications().add(app.getName());
            } else {
                for (final Constructor<?> ctr : app.getConstructors()) {
                    if (ctr.getParameterTypes().length == 0) {
                        webModule.getRestApplications().add(app.getName());
                        break;
                    }
                }
            }
        }

        public EjbModule deploy(final EjbModule ejbModule) throws OpenEJBException {
            if (ejbModule.getEjbJar() != null && ejbModule.getEjbJar().isMetadataComplete()) {
                return ejbModule;
            }

            try {
                if (ejbModule.getFinder() == null) {
                    ejbModule.setFinder(FinderFactory.createFinder(ejbModule));
                }
            } catch (final MalformedURLException e) {
                startupLogger.warning("startup.scrapeFailedForModule", ejbModule.getJarLocation());
                return ejbModule;
            } catch (final Exception e) {
                startupLogger.warning("Unable to scrape for @Stateful, @Stateless, @Singleton or @MessageDriven annotations. AnnotationFinder failed.", e);
                return ejbModule;
            }

            final IAnnotationFinder finder = ejbModule.getFinder();

            // Fill in default sessionType for xml declared EJBs
            for (final EnterpriseBean bean : ejbModule.getEjbJar().getEnterpriseBeans()) {
                if (!(bean instanceof SessionBean)) {
                    continue;
                }

                final SessionBean sessionBean = (SessionBean) bean;

                if (sessionBean.getSessionType() != null) {
                    continue;
                }

                try {
                    final Class<?> clazz = ejbModule.getClassLoader().loadClass(bean.getEjbClass());
                    sessionBean.setSessionType(getSessionType(clazz));
                } catch (final Throwable handledInValidation) {
                    // no-op
                }
            }

            // Fill in default ejbName for xml declared EJBs
            for (final EnterpriseBean bean : ejbModule.getEjbJar().getEnterpriseBeans()) {
                if (bean.getEjbClass() == null) {
                    continue;
                }
                if (bean.getEjbName() == null || bean.getEjbName().startsWith("@NULL@")) {
                    ejbModule.getEjbJar().removeEnterpriseBean(bean.getEjbName());
                    try {
                        final Class<?> clazz = ejbModule.getClassLoader().loadClass(bean.getEjbClass());
                        final String ejbName = getEjbName(bean, clazz);
                        bean.setEjbName(ejbName);
                    } catch (final Throwable handledInValidation) {
                        // no-op
                    }
                    ejbModule.getEjbJar().addEnterpriseBean(bean);
                }
            }
            /* 19.2:  ejb-name: Default is the unqualified name of the bean class */

            final EjbJar ejbJar = ejbModule.getEjbJar();
            for (final Annotated<Class<?>> beanClass : finder.findMetaAnnotatedClasses(Singleton.class)) {
                final Singleton singleton = beanClass.getAnnotation(Singleton.class);
                final String ejbName = getEjbName(singleton, beanClass.get());

                if (!isValidEjbAnnotationUsage(Singleton.class, beanClass, ejbName, ejbModule)) {
                    continue;
                }

                EnterpriseBean enterpriseBean = ejbJar.getEnterpriseBean(ejbName);
                if (enterpriseBean == null) {
                    enterpriseBean = new SingletonBean(ejbName, beanClass.get());
                    ejbJar.addEnterpriseBean(enterpriseBean);
                }
                if (enterpriseBean.getEjbClass() == null) {
                    enterpriseBean.setEjbClass(beanClass.get());
                }
                if (enterpriseBean instanceof SessionBean) {
                    final SessionBean sessionBean = (SessionBean) enterpriseBean;
                    sessionBean.setSessionType(SessionType.SINGLETON);

                    if (singleton.mappedName() != null) {
                        sessionBean.setMappedName(singleton.mappedName());
                    }
                }
                LegacyProcessor.process(beanClass.get(), enterpriseBean);
            }

            for (final Annotated<Class<?>> beanClass : finder.findMetaAnnotatedClasses(Stateless.class)) {
                final Stateless stateless = beanClass.getAnnotation(Stateless.class);
                final String ejbName = getEjbName(stateless, beanClass.get());

                if (!isValidEjbAnnotationUsage(Stateless.class, beanClass, ejbName, ejbModule)) {
                    continue;
                }

                EnterpriseBean enterpriseBean = ejbJar.getEnterpriseBean(ejbName);
                if (enterpriseBean == null) {
                    enterpriseBean = new StatelessBean(ejbName, beanClass.get());
                    ejbJar.addEnterpriseBean(enterpriseBean);
                }
                if (enterpriseBean.getEjbClass() == null) {
                    enterpriseBean.setEjbClass(beanClass.get());
                }
                if (enterpriseBean instanceof SessionBean) {
                    final SessionBean sessionBean = (SessionBean) enterpriseBean;
                    sessionBean.setSessionType(SessionType.STATELESS);

                    if (stateless.mappedName() != null) {
                        sessionBean.setMappedName(stateless.mappedName());
                    }
                }
                LegacyProcessor.process(beanClass.get(), enterpriseBean);
            }

            // The Specialization code is good, but it possibly needs to be moved to after the full processing of the bean
            // the plus is that it would get the required interfaces.  The minus is that it would get all the other items

            // Possibly study alternatives.  Alternatives might have different meta data completely while it seems Specializing beans inherit all meta-data

            // Anyway.. the qualifiers aren't getting inherited, so we need to fix that

            for (final Annotated<Class<?>> beanClass : finder.findMetaAnnotatedClasses(Stateful.class)) {
                final Stateful stateful = beanClass.getAnnotation(Stateful.class);
                final String ejbName = getEjbName(stateful, beanClass.get());

                if (!isValidEjbAnnotationUsage(Stateful.class, beanClass, ejbName, ejbModule)) {
                    continue;
                }

                EnterpriseBean enterpriseBean = ejbJar.getEnterpriseBean(ejbName);
                if (enterpriseBean == null) {
                    enterpriseBean = new StatefulBean(ejbName, beanClass.get());
                    ejbJar.addEnterpriseBean(enterpriseBean);
                }
                if (enterpriseBean.getEjbClass() == null) {
                    enterpriseBean.setEjbClass(beanClass.get());
                }
                if (enterpriseBean instanceof SessionBean) {
                    final SessionBean sessionBean = (SessionBean) enterpriseBean;
                    // TODO: We might be stepping on an xml override here
                    sessionBean.setSessionType(SessionType.STATEFUL);
                    if (stateful.mappedName() != null) {
                        sessionBean.setMappedName(stateful.mappedName());
                    }
                    if (sessionBean.getPassivationCapable() == null) {
                        sessionBean.setPassivationCapable(stateful.passivationCapable());
                    }
                }
                LegacyProcessor.process(beanClass.get(), enterpriseBean);
            }

            for (final Annotated<Class<?>> beanClass : finder.findMetaAnnotatedClasses(ManagedBean.class)) {
                final ManagedBean managed = beanClass.getAnnotation(ManagedBean.class);
                final String ejbName = getEjbName(managed, beanClass.get());

                // TODO: this is actually against the spec, but the requirement is rather silly
                // (allowing @Stateful and @ManagedBean on the same class)
                // If the TCK doesn't complain we should discourage it
                if (!isValidEjbAnnotationUsage(ManagedBean.class, beanClass, ejbName, ejbModule)) {
                    continue;
                }

                EnterpriseBean enterpriseBean = ejbJar.getEnterpriseBean(ejbName);
                if (enterpriseBean == null) {
                    enterpriseBean = new org.apache.openejb.jee.ManagedBean(ejbName, beanClass.get());
                    ejbJar.addEnterpriseBean(enterpriseBean);
                }
                if (enterpriseBean.getEjbClass() == null) {
                    enterpriseBean.setEjbClass(beanClass.get());
                }
                if (enterpriseBean instanceof SessionBean) {
                    final SessionBean sessionBean = (SessionBean) enterpriseBean;
                    sessionBean.setSessionType(SessionType.MANAGED);

                    final TransactionType transactionType = sessionBean.getTransactionType();
                    if (transactionType == null) {
                        sessionBean.setTransactionType(TransactionType.BEAN);
                    }
                }
            }

            for (final Annotated<Class<?>> beanClass : finder.findMetaAnnotatedClasses(MessageDriven.class)) {
                final MessageDriven mdb = beanClass.getAnnotation(MessageDriven.class);
                final String ejbName = getEjbName(mdb, beanClass.get());

                if (!isValidEjbAnnotationUsage(MessageDriven.class, beanClass, ejbName, ejbModule)) {
                    continue;
                }

                MessageDrivenBean messageBean = (MessageDrivenBean) ejbJar.getEnterpriseBean(ejbName);
                if (messageBean == null) {
                    messageBean = new MessageDrivenBean(ejbName);
                    ejbJar.addEnterpriseBean(messageBean);
                }
                if (messageBean.getEjbClass() == null) {
                    messageBean.setEjbClass(beanClass.get());
                }
                LegacyProcessor.process(beanClass.get(), messageBean);
            }

            AssemblyDescriptor assemblyDescriptor = ejbModule.getEjbJar().getAssemblyDescriptor();
            if (assemblyDescriptor == null) {
                assemblyDescriptor = new AssemblyDescriptor();
                ejbModule.getEjbJar().setAssemblyDescriptor(assemblyDescriptor);
            }

            startupLogger.debug("Searching for annotated application exceptions (see OPENEJB-980)");
            final List<Class<?>> appExceptions = finder.findAnnotatedClasses(ApplicationException.class);
            for (final Class<?> exceptionClass : appExceptions) {
                startupLogger.debug("...handling " + exceptionClass);
                final ApplicationException annotation = exceptionClass.getAnnotation(ApplicationException.class);
                if (assemblyDescriptor.getApplicationException(exceptionClass) == null) {
                    startupLogger.debug("...adding " + exceptionClass + " with rollback=" + annotation.rollback());
                    assemblyDescriptor.addApplicationException(exceptionClass, annotation.rollback(), annotation.inherited());
                } else {
                    mergeApplicationExceptionAnnotation(assemblyDescriptor, exceptionClass, annotation);
                }
            }

            { // after having found EJB for auto CDI activation
                final Map<URL, List<String>> managedClasses;
                Beans beans = ejbModule.getBeans();

                final boolean deployComp;
                if (beans == null && !ejbJar.getEnterpriseBeansByEjbName().isEmpty()
                        && isActivateCdiForEjbOnlyModules(ejbModule)) {
                    logger.info("Activating CDI in ACTIVATED mode in module '" + ejbModule.getModuleUri() + "' cause EJB were found\n" +
                            "  add openejb.cdi.activated=false in application.properties to switch it off or\n" +
                            "  openejb.cdi.activated-on-ejb=false in conf/system.properties" +
                            "  to switch it off");
                    beans = new Beans();
                    beans.setBeanDiscoveryMode("ANNOTATED");
                    beans.setVersion("1.1");
                    try {
                        ejbModule.getModuleUri().toURL();
                        beans.setUri(ejbModule.getModuleUri().toASCIIString());
                    } catch (final MalformedURLException | IllegalArgumentException iae) { // test? fake a URI
                        beans.setUri(URI.create("jar:file://!/" + ejbModule.getModuleUri().toASCIIString() + "/META-INF/beans.xml").toASCIIString());
                    }
                    ejbModule.setBeans(beans);
                    deployComp = false; // no need normally since mainly only EJB will be injectable
                } else {
                    deployComp = true;
                }

                if (beans != null) {
                    managedClasses = beans.getManagedClasses();
                    getBeanClasses(beans.getUri(), finder, managedClasses, beans.getNotManagedClasses(), ejbModule.getAltDDs());

                    if (deployComp) {
                        // passing jar location to be able to manage maven classes/test-classes which have the same moduleId
                        String id = ejbModule.getModuleId();
                        if (ejbModule.getJarLocation() != null &&
                                (ejbModule.getJarLocation().contains(ejbModule.getModuleId() + "/target/test-classes".replace("/", File.separator)) ||
                                        ejbModule.getJarLocation().contains(ejbModule.getModuleId() + "/build/classes/test".replace("/", File.separator)))) {
                            // with maven/gradle if both src/main/java and src/test/java are deployed
                            // moduleId.Comp exists twice so it fails
                            // here we simply modify the test comp bean name to avoid it
                            id += "_test";
                        }
                        final String name = BeanContext.Comp.openejbCompName(id);
                        final org.apache.openejb.jee.ManagedBean managedBean = new CompManagedBean(name, BeanContext.Comp.class);
                        managedBean.setTransactionType(TransactionType.BEAN);
                        ejbModule.getEjbJar().addEnterpriseBean(managedBean);

                        if ("true".equals(SystemInstance.get().getProperty("openejb.cdi.support.@Startup", "true"))) {
                            final List<Annotated<Class<?>>> forceStart = finder.findMetaAnnotatedClasses(Startup.class);
                            final List<String> startupBeans = beans.getStartupBeans();
                            for (final Annotated<Class<?>> clazz : forceStart) {
                                startupBeans.add(clazz.get().getName());
                            }
                        }
                    }
                }
            }

            // ejb can be rest bean and only then in standalone so scan providers here too
            // adding them to app since they should be in the app classloader
            if (ejbModule.getAppModule() != null) {
                addJaxRsProviders(finder, ejbModule.getAppModule().getJaxRsProviders(), Provider.class);
            }

            autoJpa(ejbModule);

            return ejbModule;
        }

        private boolean isActivateCdiForEjbOnlyModules(final EjbModule ejbModule) {
            final String activated = ejbModule.getProperties().getProperty("openejb.cdi.activated");
            final String globalConfig = SystemInstance.get().getProperty("openejb.cdi.activated-on-ejb"); // spec should be true but mem + bck compat
            return (globalConfig == null || Boolean.parseBoolean(globalConfig)) &&
                    ((activated == null && hasAtInject(ejbModule)) || (activated != null && Boolean.parseBoolean(activated)));
        }

        // quick heuristic to guess if cdi is needed, avoid to need more mem when useless
        private boolean hasAtInject(final EjbModule ejbModule) {
            final IAnnotationFinder finder = ejbModule.getFinder();
            return finder != null &&
                    (!finder.findAnnotatedFields(Inject.class).isEmpty()
                    || !finder.findAnnotatedConstructors(Inject.class).isEmpty()
                    || !finder.findAnnotatedMethods(Inject.class).isEmpty());
        }

        private SessionType getSessionType(final Class<?> clazz) {
            if (clazz.isAnnotationPresent(Stateful.class)) {
                return SessionType.STATEFUL;
            }
            if (clazz.isAnnotationPresent(Stateless.class)) {
                return SessionType.STATELESS;
            }
            if (clazz.isAnnotationPresent(Singleton.class)) {
                return SessionType.SINGLETON;
            }
            if (clazz.isAnnotationPresent(ManagedBean.class)) {
                return SessionType.MANAGED;
            }
            return null;
        }

        private String getEjbName(final EnterpriseBean bean, final Class<?> clazz) {

            if (bean instanceof SessionBean) {
                final SessionBean sessionBean = (SessionBean) bean;
                switch (sessionBean.getSessionType()) {
                    case STATEFUL: {
                        final Stateful annotation = clazz.getAnnotation(Stateful.class);
                        if (annotation != null && specified(annotation.name())) {
                            return annotation.name();
                        }
                    }
                    case STATELESS: {
                        final Stateless annotation = clazz.getAnnotation(Stateless.class);
                        if (annotation != null && specified(annotation.name())) {
                            return annotation.name();
                        }
                    }
                    case SINGLETON: {
                        final Singleton annotation = clazz.getAnnotation(Singleton.class);
                        if (annotation != null && specified(annotation.name())) {
                            return annotation.name();
                        }
                    }
                }
            }

            if (bean instanceof MessageDrivenBean) {
                final MessageDriven annotation = clazz.getAnnotation(MessageDriven.class);
                if (annotation != null && specified(annotation.name())) {
                    return annotation.name();
                }
            }

            return clazz.getSimpleName();
        }

        private static boolean specified(final String name) {
            return name != null && name.length() != 0;
        }

        private void getBeanClasses(final String uri, final IAnnotationFinder finder,
                                    final Map<URL, List<String>> classes,
                                    final Map<URL, List<String>> notManaged,
                                    final Map<String, Object> altDD) {

            //  What we're hoping in this method is to get lucky and find
            //  that our 'finder' instances is an AnnotationFinder that is
            //  holding an AggregatedArchive so we can get the classes that
            //  that pertain to each URL for CDI purposes.
            //
            //  If not we call finder.getAnnotatedClassNames() which may return
            //  more classes than actually apply to CDI.  This can "pollute"
            //  the CDI class space and break injection points

            // force cast otherwise we would be broken
            final IAnnotationFinder delegate = FinderFactory.ModuleLimitedFinder.class.isInstance(finder) ?
                    FinderFactory.ModuleLimitedFinder.class.cast(finder).getDelegate() : finder;
            if (!AnnotationFinder.class.isInstance(delegate)) {
                return; // only few tests
            }

            final AnnotationFinder annotationFinder = AnnotationFinder.class.cast(delegate);
            final Archive archive = annotationFinder.getArchive();

            if (!WebappAggregatedArchive.class.isInstance(archive)) {
                try {
                    final List<String> annotatedClassNames = annotationFinder.getAnnotatedClassNames();
                    if (!annotatedClassNames.isEmpty()) {
                        classes.put(uri == null ? null : new URL(uri), annotatedClassNames);
                    }
                } catch (final MalformedURLException e) {
                    throw new IllegalStateException(e);
                }
                return;
            }

            final WebappAggregatedArchive aggregatedArchive = (WebappAggregatedArchive) archive;
            final Map<URL, List<String>> map = aggregatedArchive.getClassesMap();

            Collection<Class<?>> discoveredBeans = null;
            List<Class<? extends Extension>> extensions = null;

            final FolderDDMapper ddMapper = SystemInstance.get().getComponent(FolderDDMapper.class);
            for (final Map.Entry<URL, List<String>> entry : map.entrySet()) {
                final URL key = entry.getKey();
                final URL beansXml = hasBeansXml(key, ddMapper);
                final List<String> value = entry.getValue();
                if (beansXml != null) {
                    classes.put(beansXml, value);
                } else if (!value.isEmpty()) {
                    final Set<String> fastValue = new HashSet<>(value);
                    if (discoveredBeans == null) { // lazy init for apps not using it, it slows down the app boot and that should be useless
                        discoveredBeans = new HashSet<>();

                        final Set<Class<? extends Annotation>> containerAnnot = new HashSet<>();
                        containerAnnot.add(Stereotype.class);
                        containerAnnot.add(NormalScope.class);
                        containerAnnot.add(Dependent.class);
                        containerAnnot.add(ApplicationScoped.class);
                        containerAnnot.add(ConversationScoped.class);
                        containerAnnot.add(RequestScoped.class);
                        containerAnnot.add(SessionScoped.class);
                        containerAnnot.add(Model.class);
                        containerAnnot.add(Singleton.class);
                        containerAnnot.add(Stateless.class);
                        containerAnnot.add(Stateful.class);
                        containerAnnot.add(MessageDriven.class);
                        containerAnnot.add(jakarta.interceptor.Interceptor.class);
                        containerAnnot.add(Decorator.class);
                        final ClassLoader classLoader = ParentClassLoaderFinder.Helper.get();
                        try {
                            for (final String name : asList("jakarta.faces.flow.FlowScoped", "jakarta.faces.view.ViewScoped")) {
                                containerAnnot.add((Class<? extends Annotation>) classLoader.loadClass(name));
                            }
                        } catch (final Throwable e) {
                            // no-op
                        }

                        final Set<Class<?>> newMarkers = new HashSet<>();
                        for (final Class<? extends Annotation> a : containerAnnot) {
                            newMarkers.addAll(finder.findAnnotatedClasses(a));
                        }

                        do {
                            final Set<Class<?>> loopMarkers = new HashSet<>(newMarkers);
                            newMarkers.clear();
                            for (final Class<?> marker : loopMarkers) {
                                discoveredBeans.add(marker);

                                final List<Class<?>> found = finder.findAnnotatedClasses(Class.class.cast(marker));
                                for (final Class<?> c : found) {
                                    if (c.isAnnotation()) {
                                        newMarkers.add(c);
                                    }
                                    discoveredBeans.add(c);
                                }
                            }
                        } while (!newMarkers.isEmpty());

                        for (final Field field : finder.findAnnotatedFields(Delegate.class)) { // should be done for constructors but too slow?
                            discoveredBeans.add(field.getDeclaringClass());
                        }

                        extensions = finder.findImplementations(Extension.class);
                    }

                    boolean skip = false;
                    for (final Class<?> c : extensions) {
                        if (fastValue.contains(c.getName())) {
                            // legacy mode, we should check META-INF/services/... but this mode + having an Extension should be enough
                            skip = true;
                            continue;
                        }
                    }
                    if (skip) {
                        continue;
                    }

                    final Set<String> beans = new HashSet<>();
                    for (final Class<?> c : discoveredBeans) {
                        final String name = c.getName();
                        if (fastValue.contains(name)) {
                            beans.add(name);
                        }
                    }

                    if (beans.isEmpty()) {
                        continue;
                    }

                    // just keep the potential ones to not load all classes during boot
                    notManaged.put(entry.getKey(), new ArrayList<>(beans));
                }
            }
        }

        public URL hasBeansXml(final URL url, final FolderDDMapper ddMapper) {
            final String urlPath = url.getPath();
            if (urlPath.endsWith("/WEB-INF/beans.xml")) {
                return url;
            }
            if (urlPath.endsWith("WEB-INF/classes/") || urlPath.endsWith("WEB-INF/classes")) {
                final File toFile = URLs.toFile(url);
                {
                    final File file = new File(toFile.getParent(), "beans.xml");
                    if (file.exists()) {
                        try {
                            return file.toURI().toURL();
                        } catch (final MalformedURLException e) {
                            return url;
                        }
                    }
                }
                {
                    final File file = new File(toFile, "META-INF/beans.xml");
                    if (file.exists()) {
                        try {
                            return file.toURI().toURL();
                        } catch (final MalformedURLException e) {
                            return url;
                        }
                    }
                }
                return null;
            }
            if (url.getPath().endsWith("!/META-INF/beans.xml")) {
                return url;
            }

            try (URLClassLoader loader = new URLClassLoader(new URL[]{url}, new EmptyResourcesClassLoader())) {
                final String[] paths = {
                        "META-INF/beans.xml",
                        "WEB-INF/beans.xml",
                        "/WEB-INF/beans.xml",
                        "/META-INF/beans.xml",
                };

                for (final String path : paths) {
                    final URL resource = loader.findResource(path);
                    if (resource != null) {
                        return resource;
                    }
                }
            } catch (final Exception e) {
                // no-op
            }
            // no-op
            if (ddMapper != null) {
                final File asFile = URLs.toFile(url);
                if (asFile.isDirectory()) {
                    final File ddFolder = ddMapper.getDDFolder(asFile);
                    final File file = new File(ddFolder, "beans.xml");
                    if (file.isFile()) {
                        try {
                            return file.toURI().toURL();
                        } catch (final MalformedURLException e) {
                            // no-op
                        }
                    }
                }
            }
            return null;
        }

        private String getEjbName(final MessageDriven mdb, final Class<?> beanClass) {
            return mdb.name().isEmpty() ? beanClass.getSimpleName() : mdb.name();
        }

        private String getEjbName(final Stateful stateful, final Class<?> beanClass) {
            return stateful.name().isEmpty() ? beanClass.getSimpleName() : stateful.name();
        }

        private String getEjbName(final Stateless stateless, final Class<?> beanClass) {
            return stateless.name().isEmpty() ? beanClass.getSimpleName() : stateless.name();
        }

        private String getEjbName(final Singleton singleton, final Class<?> beanClass) {
            return singleton.name().isEmpty() ? beanClass.getSimpleName() : singleton.name();
        }

        private String getEjbName(final ManagedBean managed, final Class<?> beanClass) {
            return managed.value().isEmpty() ? beanClass.getSimpleName() : managed.value();
        }

        private boolean isValidEjbAnnotationUsage(final Class annotationClass, final Annotated<Class<?>> beanClass, final String ejbName, final EjbModule ejbModule) {
            final List<Class<? extends Annotation>> annotations = new ArrayList(Arrays.asList(Singleton.class, Stateless.class, Stateful.class, MessageDriven.class));
            annotations.remove(annotationClass);

            final boolean b = true;
            for (final Class<? extends Annotation> secondAnnotation : annotations) {
                final Annotation annotation = beanClass.getAnnotation(secondAnnotation);

                if (annotation == null) {
                    continue;
                }

                String secondEjbName = null;
                if (annotation instanceof Stateful) {
                    secondEjbName = getEjbName((Stateful) annotation, beanClass.get());
                } else if (annotation instanceof Stateless) {
                    secondEjbName = getEjbName((Stateless) annotation, beanClass.get());
                } else if (annotation instanceof Singleton) {
                    secondEjbName = getEjbName((Singleton) annotation, beanClass.get());
                } else if (annotation instanceof MessageDriven) {
                    secondEjbName = getEjbName((MessageDriven) annotation, beanClass.get());
                }

                if (ejbName.equals(secondEjbName)) {
                    ejbModule.getValidation().fail(ejbName, "multiplyAnnotatedAsBean", annotationClass.getSimpleName(), secondAnnotation.getSimpleName(), ejbName, beanClass.get().getName());
                }
            }

            if (beanClass.get().isInterface()) {
                if (!CheckClasses.isAbstractAllowed(beanClass.get())) {
                    ejbModule.getValidation().fail(ejbName, "interfaceAnnotatedAsBean", annotationClass.getSimpleName(), beanClass.get().getName());
                    return false;
                }
            } else if (Modifier.isAbstract(beanClass.get().getModifiers())) {
                if (!CheckClasses.isAbstractAllowed(beanClass.get())) {
                    ejbModule.getValidation().fail(ejbName, "abstractAnnotatedAsBean", annotationClass.getSimpleName(), beanClass.get().getName());
                    return false;
                }
            }

            return b;
        }

    }

    public static void autoJpa(final EjbModule ejbModule) {
        final IAnnotationFinder finder = ejbModule.getFinder();
        if (ejbModule.getAppModule() != null) {
            for (final PersistenceModule pm : ejbModule.getAppModule().getPersistenceModules()) {
                for (final org.apache.openejb.jee.jpa.unit.PersistenceUnit pu : pm.getPersistence().getPersistenceUnit()) {
                    if ((pu.isExcludeUnlistedClasses() == null || !pu.isExcludeUnlistedClasses())
                        && "true".equalsIgnoreCase(pu.getProperties().getProperty(OPENEJB_JPA_AUTO_SCAN))) {
                        doAutoJpa(finder, pu);
                    }
                }
            }
        }
    }

    public static void doAutoJpa(final IAnnotationFinder finder, final org.apache.openejb.jee.jpa.unit.PersistenceUnit pu) {
        final String packageName = pu.getProperties().getProperty(OPENEJB_JPA_AUTO_SCAN_PACKAGE);
        String[] packageNames = null;
        if (packageName != null) {
            packageNames = packageName.split(",");
        }

        // no need of meta currently since JPA providers doesn't support it
        final List<Class<?>> classes = new ArrayList<>();
        classes.addAll(finder.findAnnotatedClasses(Entity.class));
        classes.addAll(finder.findAnnotatedClasses(Embeddable.class));
        classes.addAll(finder.findAnnotatedClasses(MappedSuperclass.class));
        classes.addAll(finder.findAnnotatedClasses(Converter.class));
        final List<String> existingClasses = pu.getClazz();
        for (final Class<?> clazz : classes) {
            final String name = clazz.getName();
            if (existingClasses.contains(name)) {
                continue;
            }

            if (packageNames == null) {
                pu.getClazz().add(name);
            } else {
                for (final String pack : packageNames) {
                    if (name.startsWith(pack)) {
                        pu.getClazz().add(name);
                    }
                }
            }
        }
        pu.setScanned(true);
    }

    public static class ProcessAnnotatedBeans implements DynamicDeployer {

        public static final String STRICT_INTERFACE_DECLARATION = "openejb.strict.interface.declaration";

        private final boolean webserviceAsRemote;

        public ProcessAnnotatedBeans(final boolean wsAsRemote) {
            webserviceAsRemote = wsAsRemote;
        }

        public void deploy(final CdiBeanInfo beanInfo) throws OpenEJBException {

            final AnnotationFinder annotationFinder = createFinder(beanInfo.getBeanClass());
            /*
             * @EJB
             * @Resource
             * @WebServiceRef
             * @PersistenceUnit
             * @PersistenceContext
             */
            buildAnnotatedRefs(beanInfo, annotationFinder, beanInfo.getClassLoader());

            processWebServiceClientHandlers(beanInfo, annotationFinder, beanInfo.getClassLoader());

        }

        public AppModule deploy(final AppModule appModule) throws OpenEJBException {
            for (final EjbModule ejbModule : appModule.getEjbModules()) {
                setModule(ejbModule);
                try {
                    deploy(ejbModule);
                } finally {
                    removeModule();
                }
            }
            for (final ClientModule clientModule : appModule.getClientModules()) {
                setModule(clientModule);
                try {
                    deploy(clientModule);
                } finally {
                    removeModule();
                }
            }
            for (final ConnectorModule connectorModule : appModule.getConnectorModules()) {
                setModule(connectorModule);
                try {
                    deploy(connectorModule);
                } finally {
                    removeModule();
                }
            }
            for (final WebModule webModule : appModule.getWebModules()) {
                setModule(webModule);
                try {
                    deploy(webModule);
                } finally {
                    removeModule();
                }
            }
            return appModule;
        }

        public ClientModule deploy(final ClientModule clientModule) throws OpenEJBException {
            if (clientModule.getApplicationClient() != null && clientModule.getApplicationClient().isMetadataComplete()) {
                return clientModule;
            }

            final ClassLoader classLoader = clientModule.getClassLoader();

            ApplicationClient client = clientModule.getApplicationClient();

            if (client == null) {
                client = new ApplicationClient();
            }

            final Set<Class> remoteClients = new HashSet<>();

            if (clientModule.getMainClass() != null) {
                final String className = realClassName(clientModule.getMainClass());

                // OPENEJB-1063: a Main-Class should use "." instead of "/"
                // it wasn't check before jdk 1.5 so we can get old module with
                // bad format http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4986512
                // replace all "/" by "."
                if (clientModule.getMainClass().contains("/")) { // className can't be null here
                    clientModule.setMainClass(className);
                }

                final Class clazz;
                try {
                    clazz = classLoader.loadClass(className);
                    remoteClients.add(clazz);

                    final AnnotationFinder annotationFinder = createFinder(clazz);

                    buildAnnotatedRefs(client, annotationFinder, classLoader);
                } catch (final ClassNotFoundException e) {
                    /**
                     * Some ClientModule are discovered only because the jar uses a Main-Class
                     * entry in the MANIFEST.MF file.  Lots of jars do this that are not used as
                     * java ee application clients, so lets not make this a failure unless it
                     * has a META-INF/application-client.xml which tells us it is in fact
                     * expected to be a ClientModule and not just some random jar.
                     */
                    if (clientModule.getAltDDs().containsKey("application-client.xml")) {
                        getValidationContext().fail("client", "client.missingMainClass", className);
                    } else {
                        getValidationContext().warn("client", "client.missingMainClass", className);
                    }
                }
            }

            for (final String rawClassName : clientModule.getRemoteClients()) {
                final String className = realClassName(rawClassName);
                final Class clazz;
                try {
                    clazz = classLoader.loadClass(className);
                    remoteClients.add(clazz);
                } catch (final ClassNotFoundException e) {
                    throw new OpenEJBException("Unable to load RemoteClient class: " + className, e);
                }

                final AnnotationFinder annotationFinder = createFinder(clazz);

                buildAnnotatedRefs(client, annotationFinder, classLoader);
            }

            for (final String rawClassName : clientModule.getLocalClients()) {
                final String className = realClassName(rawClassName);
                final Class clazz;
                try {
                    clazz = classLoader.loadClass(className);
                } catch (final ClassNotFoundException e) {
                    throw new OpenEJBException("Unable to load LocalClient class: " + className, e);
                }

                final AnnotationFinder annotationFinder = createFinder(clazz);

                buildAnnotatedRefs(client, annotationFinder, classLoader);
            }

            validateRemoteClientRefs(classLoader, client, remoteClients);

            final IAnnotationFinder finder = clientModule.getFinder();
            if (!AnnotationFinder.class.isInstance(finder) && finder != null) {
                final Class<?>[] loadedClasses = new Class<?>[finder.getAnnotatedClassNames().size()];
                int i = 0;
                for (final String s : finder.getAnnotatedClassNames()) {
                    try {
                        loadedClasses[i++] = classLoader.loadClass(s);
                    } catch (final ClassNotFoundException e) {
                        // no-op
                    }
                }
                clientModule.getFinderReference().set(new FinderFactory.OpenEJBAnnotationFinder(new ClassesArchive(loadedClasses)));
            }
            processWebServiceClientHandlers(client, AnnotationFinder.class.cast(clientModule.getFinder()), classLoader);

            return clientModule;
        }

        private void validateRemoteClientRefs(final ClassLoader classLoader, final ApplicationClient client, final Set<Class> remoteClients) {
            for (final EjbLocalRef ref : client.getEjbLocalRef()) {
                for (final InjectionTarget target : ref.getInjectionTarget()) {
                    try {
                        final Class<?> targetClass = classLoader.loadClass(realClassName(target.getInjectionTargetClass()));
                        for (final Class remoteClient : remoteClients) {
                            if (targetClass.isAssignableFrom(remoteClient)) {
                                fail(remoteClient.getName(), "remoteClient.ejbLocalRef", target.getInjectionTargetClass(), target.getInjectionTargetName());
                            }
                        }
                    } catch (final ClassNotFoundException ignore) {
                        // no-op
                    }
                }
            }

            for (final PersistenceContextRef ref : client.getPersistenceContextRef()) {
                for (final InjectionTarget target : ref.getInjectionTarget()) {
                    try {
                        final Class<?> targetClass = classLoader.loadClass(realClassName(target.getInjectionTargetClass()));
                        for (final Class remoteClient : remoteClients) {
                            if (targetClass.isAssignableFrom(remoteClient)) {
                                fail(remoteClient.getName(), "remoteClient.persistenceContextRef", target.getInjectionTargetClass(), target.getInjectionTargetName());
                            }
                        }
                    } catch (final ClassNotFoundException ignore) {
                        // no-op
                    }
                }
            }

            /* TODO: still useful?
            List<String> unusableTypes = new ArrayList<String>(knownResourceEnvTypes);
            unusableTypes.remove("jakarta.jms.Topic");
            unusableTypes.remove("jakarta.jms.Queue");

            for (ResourceEnvRef ref : client.getResourceEnvRef()) {

                if (!unusableTypes.contains(ref.getType())) continue;

                for (InjectionTarget target : ref.getInjectionTarget()) {
                    try {
                        Class<?> targetClass = classLoader.loadClass(target.getInjectionTargetClass());
                        for (Class remoteClient : remoteClients) {
                            if (targetClass.isAssignableFrom(remoteClient)) {
                                fail(remoteClient.getName(), "remoteClient.resourceEnvRef", target.getInjectionTargetClass(), target.getInjectionTargetName(), ref.getType());
                            }
                        }
                    } catch (ClassNotFoundException ignore) {
                    }
                }
            }
            */
        }

        public ConnectorModule deploy(final ConnectorModule connectorModule) throws OpenEJBException {
            // resource modules currently don't have any annotations
            return connectorModule;
        }

        /**
         * Collects a list of all webapp related classes that are eligible for
         * annotation processing then scans them and fills out the web.xml with
         * the xml version of the annotations.
         *
         * @param webModule
         * @return
         * @throws OpenEJBException
         */
        public WebModule deploy(final WebModule webModule) throws OpenEJBException {
            final WebApp webApp = webModule.getWebApp();
            if (webApp != null && webApp.isMetadataComplete()) {
                return webModule;
            }

            /*
             * Classes added to this set will be scanned for annotations
             */
            final Set<Class> classes = new HashSet<>();


            final ClassLoader classLoader = webModule.getClassLoader();

            final String webXmlApplication = webApp.contextParamsAsMap().get("jakarta.ws.rs.Application");
            if (webXmlApplication != null) {
                webModule.getRestApplications().clear();
                webModule.getRestApplications().add(webXmlApplication);
            }

            final Collection<String> restApp = webModule.getRestApplications();
            if (restApp.isEmpty()) {
                addRestClassesToScannedClasses(webModule, classes, classLoader);
            } else {
                for (final String rawClassName : restApp) {
                    final String application = realClassName(rawClassName);
                    if (application != null) {
                        final Class<?> clazz;
                        try {
                            clazz = classLoader.loadClass(application);
                            classes.add(clazz);
                        } catch (final ClassNotFoundException e) {
                            throw new OpenEJBException("Unable to load Application class: " + application, e);
                        }
                        if (Modifier.isAbstract(clazz.getModifiers())) {
                            continue;
                        }

                        try {
                            final Application app = Application.class.cast(clazz.newInstance());
                            try {
                                final Set<Class<?>> appClasses = app.getClasses();
                                if (!appClasses.isEmpty()) {
                                    classes.addAll(appClasses);
                                } else {
                                    addRestClassesToScannedClasses(webModule, classes, classLoader);
                                }
                            } catch (final RuntimeException npe) {
                                if (app == null) {
                                    throw npe;
                                }
                                // if app depends on cdi no need to do it
                            }
                        } catch (final InstantiationException e) {
                            throw new OpenEJBException("Unable to instantiate Application class: " + application, e);
                        } catch (final IllegalAccessException e) {
                            throw new OpenEJBException("Unable to access Application class: " + application, e);
                        }
                    }
                }
            }


            /*
             * Servlet classes are scanned
             */
            for (final Servlet servlet : webApp.getServlet()) {
                final String servletName = servlet.getServletName();
                if ("jakarta.ws.rs.core.Application".equals(servletName) || "jakarta.ws.rs.Application".equals(servletName)) {
                    // check first if there is a real application as init param
                    boolean done = false;
                    for (final ParamValue pv : servlet.getInitParam()) {
                        if ("jakarta.ws.rs.core.Application".equals(pv.getParamName()) || "jakarta.ws.rs.Application".equals(pv.getParamName())) {
                            webModule.getRestApplications().add(pv.getParamValue());
                            done = true;
                            break;
                        }
                    }
                    if (!done) {
                        servlet.setServletName(ProvidedJAXRSApplication.class.getName());
                        webModule.getRestApplications().add(ProvidedJAXRSApplication.class.getName());
                        for (final ServletMapping mapping : webApp.getServletMapping()) {
                            if (servletName.equals(mapping.getServletName())) {
                                mapping.setServletName(ProvidedJAXRSApplication.class.getName());
                            }
                        }
                    }
                    continue;
                }

                String servletClass = realClassName(servlet.getServletClass());
                if (servletClass == null) { // try with servlet name, @see org.apache.openejb.arquillian.tests.jaxrs.basicapp.BasicApplication
                    servletClass = realClassName(servletName);
                }

                if (servletClass != null && servlet.getJspFile() == null) { // jaxrs application doesn't have a jsp file
                    if (!"org.apache.openejb.server.rest.OpenEJBRestServlet".equals(servletClass)) {
                        try {
                            final Class clazz = classLoader.loadClass(servletClass);
                            classes.add(clazz);
                            if (servlet.getServletClass() == null) {
                                servlet.setServletClass(servletClass);
                            }
                        } catch (final ClassNotFoundException e) {
                            if (servlet.getServletClass() != null) {
                                throw new OpenEJBException("Unable to load servlet class: " + servletClass, e);
                            } else {
                                logger.error("servlet " + servletName + " has no servlet-class defined and is not a subclass of Application");
                            }
                        }
                    }

                    // if the servlet is a rest init servlet don't deploy rest classes automatically
                    for (final ParamValue param : servlet.getInitParam()) {
                        if (param.getParamName().equals(Application.class.getName()) || param.getParamName().equals("jakarta.ws.rs.Application")) {
                            webModule.getRestApplications().clear();
                            webModule.getRestApplications().add(param.getParamValue());
                            break;
                        }
                    }
                }
            }

            /*
             * Filter classes are scanned
             */
            for (final Filter filter : webApp.getFilter()) {
                final String filterClass = realClassName(filter.getFilterClass());
                if (filterClass != null) {
                    try {
                        final Class clazz = classLoader.loadClass(filterClass);
                        classes.add(clazz);
                    } catch (final ClassNotFoundException e) {
                        throw new OpenEJBException("Unable to load servlet filter class: " + filterClass, e);
                    }
                }
            }

            /*
             * Listener classes are scanned
             */
            for (final Listener listener : webApp.getListener()) {
                final String listenerClass = realClassName(listener.getListenerClass());
                if (listenerClass != null) {
                    try {
                        final Class clazz = classLoader.loadClass(listenerClass);
                        classes.add(clazz);
                    } catch (final ClassNotFoundException e) {
                        throw new OpenEJBException("Unable to load servlet listener class: " + listenerClass, e);
                    }
                }
            }

            for (final TldTaglib taglib : webModule.getTaglibs()) {
                /*
                 * TagLib Listener classes are scanned
                 */
                for (final Listener listener : taglib.getListener()) {
                    final String listenerClass = realClassName(listener.getListenerClass());
                    if (listenerClass != null) {
                        try {
                            final Class clazz = classLoader.loadClass(listenerClass);
                            classes.add(clazz);
                        } catch (final ClassNotFoundException e) {
                            logger.error("Unable to load tag library servlet listener class: " + listenerClass);
                        }
                    }
                }

                /*
                 * TagLib Tag classes are scanned
                 */
                for (final Tag tag : taglib.getTag()) {
                    final String tagClass = realClassName(tag.getTagClass());
                    if (tagClass != null) {
                        try {
                            final Class clazz = classLoader.loadClass(tagClass);
                            classes.add(clazz);
                        } catch (final ClassNotFoundException e) {
                            logger.error("Unable to load tag library tag class: " + tagClass);
                        }
                    }
                }
            }

            /*
             * WebService HandlerChain classes are scanned
             */
            if (webModule.getWebservices() != null) {
                for (final WebserviceDescription webservice : webModule.getWebservices().getWebserviceDescription()) {
                    for (final PortComponent port : webservice.getPortComponent()) {
                        // skip ejb port defs
                        if (port.getServiceImplBean().getEjbLink() != null) {
                            continue;
                        }

                        if (port.getHandlerChains() == null) {
                            continue;
                        }
                        for (final org.apache.openejb.jee.HandlerChain handlerChain : port.getHandlerChains().getHandlerChain()) {
                            for (final Handler handler : handlerChain.getHandler()) {
                                final String handlerClass = realClassName(handler.getHandlerClass());
                                if (handlerClass != null) {
                                    try {
                                        final Class clazz = classLoader.loadClass(handlerClass);
                                        classes.add(clazz);
                                    } catch (final ClassNotFoundException e) {
                                        throw new OpenEJBException("Unable to load webservice handler class: " + handlerClass, e);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            /*
             * JSF ManagedBean classes are scanned
             */
            for (final FacesConfig facesConfig : webModule.getFacesConfigs()) {
                for (final FacesManagedBean bean : facesConfig.getManagedBean()) {
                    final String managedBeanClass = realClassName(bean.getManagedBeanClass().trim());
                    if (managedBeanClass != null) {
                        try {
                            final Class clazz = classLoader.loadClass(managedBeanClass);
                            classes.add(clazz);
                        } catch (final ClassNotFoundException e) {
                            logger.error("Unable to load JSF managed bean class: " + managedBeanClass);
                        }
                    }
                }
            }

            final IAnnotationFinder finder = webModule.getFinder();

            if (finder != null) {
                for (final String apiClassName : API_CLASSES) {
                    final Class<? extends Annotation> clazz;
                    try {
                        clazz = (Class<? extends Annotation>) classLoader.loadClass(apiClassName);
                    } catch (final ClassNotFoundException e) {
                        continue;
                    }
                    if (clazz.isAnnotation()) {
                        classes.addAll(metaToClass(finder.findMetaAnnotatedClasses(clazz)));
                    } else if (Modifier.isAbstract(clazz.getModifiers())) {
                        classes.addAll(finder.findSubclasses(clazz));
                    } else {
                        classes.addAll(finder.findImplementations(clazz));
                    }
                }
            }

            final AnnotationFinder annotationFinder = createFinder(classes.toArray(new Class<?>[classes.size()]));

            /*
             * @EJB
             * @Resource
             * @WebServiceRef
             * @PersistenceUnit
             * @PersistenceContext
             */
            buildAnnotatedRefs(webApp, annotationFinder, classLoader);

            processWebServiceClientHandlers(webApp, annotationFinder, classLoader);

            return webModule;
        }

        public EjbModule deploy(final EjbModule ejbModule) throws OpenEJBException {
            if (ejbModule.getEjbJar() != null && ejbModule.getEjbJar().isMetadataComplete()) {
                return ejbModule;
            }

//            Map<String, EjbDeployment> deployments = ejbModule.getOpenejbJar().getDeploymentsByEjbName();
            final ClassLoader classLoader = ejbModule.getClassLoader();
            final EnterpriseBean[] enterpriseBeans = ejbModule.getEjbJar().getEnterpriseBeans();
            for (final EnterpriseBean bean : enterpriseBeans) {
                final String ejbName = bean.getEjbName();
                final String ejbClassName = realClassName(bean.getEjbClass());

                if (ejbClassName == null) {
                    final List<String> others = new ArrayList<>();
                    for (final EnterpriseBean otherBean : enterpriseBeans) {
                        others.add(otherBean.getEjbName());
                    }
                    fail(ejbName, "xml.noEjbClass", ejbName, Join.join(", ", others));
                }

                final Class<?> clazz;
                try {
                    clazz = classLoader.loadClass(ejbClassName);
                } catch (final ClassNotFoundException e) {
                    // Handled in CheckClasses.java along with other missing classes
                    continue;
                }

                final MetaAnnotatedClass<?> metaClass = new MetaAnnotatedClass(clazz);
                final boolean dynamicBean = DynamicProxyImplFactory.isKnownDynamicallyImplemented(metaClass, clazz);

                AnnotationFinder finder = null; // created lazily since not always needed
                final AnnotationFinder annotationFinder;
                if (ejbModule.getFinder() instanceof AnnotationFinder) {
                    AnnotationFinder af = (AnnotationFinder) ejbModule.getFinder();

                    final List<Class<?>> ancestors = Classes.ancestors(clazz);
                    ancestors.addAll(asList(clazz.getInterfaces()));
                    if (dynamicBean) {
                        final Proxy p = metaClass.getAnnotation(Proxy.class);
                        if (p != null) {
                            ancestors.add(p.value());
                        }
                    }

                    final String[] names = new String[ancestors.size()];
                    int i = 0;
                    for (final Class<?> ancestor : ancestors) {
                        names[i++] = ancestor.getName();
                    }
                    annotationFinder = af.select(names);
                } else { // shouldn't occur
                    if (!dynamicBean) {
                        annotationFinder = createFinder(clazz);
                    } else {
                        final Class<?>[] classes;
                        final Proxy proxy = metaClass.getAnnotation(Proxy.class);
                        if (proxy == null) {
                            classes = new Class<?>[]{clazz};
                        } else {
                            classes = new Class<?>[]{clazz, proxy.value()};
                        }
                        annotationFinder = createFinder(classes);
                    }
                }

                /*
                 * @AroundConstruct can't be on the bean itself per spec
                 * @PostConstruct
                 * @PreDestroy
                 * @AroundInvoke
                 * @Timeout
                 * @PostActivate
                 * @PrePassivate
                 * @Init
                 * @Remove
                 * @AroundTimeout
                 * @AfterBegin
                 * @BeforeCompletion
                 * @AfterCompletion
                 */
                processCallbacks(bean, annotationFinder);

                /*
                 * @TransactionManagement
                 */
                if (bean.getTransactionType() == null) {
                    final TransactionManagement tx = getInheritableAnnotation(clazz, TransactionManagement.class);
                    TransactionManagementType transactionType = TransactionManagementType.CONTAINER;
                    if (tx != null) {
                        transactionType = tx.value();
                    }
                    switch (transactionType) {
                        case BEAN:
                            bean.setTransactionType(TransactionType.BEAN);
                            break;
                        case CONTAINER:
                            bean.setTransactionType(TransactionType.CONTAINER);
                            break;
                    }
                }

                final AssemblyDescriptor assemblyDescriptor = ejbModule.getEjbJar().getAssemblyDescriptor();

                /*
                 * @ApplicationException
                 */
                processApplicationExceptions(clazz, assemblyDescriptor);

                /*
                 * TransactionAttribute
                 */
                if (bean.getTransactionType() == TransactionType.CONTAINER) {
                    processAttributes(new TransactionAttributeHandler(assemblyDescriptor, ejbName), clazz, annotationFinder);
                } else {
                    if (finder == null) {
                        finder = annotationFinder.select(clazz.getName());
                    }
                    checkAttributes(new TransactionAttributeHandler(assemblyDescriptor, ejbName), ejbName, ejbModule, finder, "invalidTransactionAttribute");
                }

                /*
                 * @RolesAllowed
                 * @PermitAll
                 * @DenyAll
                 * @RunAs
                 * @DeclareRoles
                 */
                processSecurityAnnotations(clazz, ejbName, ejbModule, annotationFinder, bean);

                /*
                 * @Schedule
                 * @Schedules
                 */
                processSchedules(bean, annotationFinder);

                /*
                 * Add any interceptors they may have referenced in xml but did not declare
                 */
                for (final InterceptorBinding binding : assemblyDescriptor.getInterceptorBinding()) {
                    final EjbJar ejbJar = ejbModule.getEjbJar();

                    final List<String> list = new ArrayList<>(binding.getInterceptorClass());

                    if (binding.getInterceptorOrder() != null) {
                        list.clear();
                        list.addAll(binding.getInterceptorOrder().getInterceptorClass());
                    }

                    for (final String interceptor : list) {
                        if (ejbJar.getInterceptor(interceptor) == null) {
                            logger.debug("Adding '<ejb-jar><interceptors><interceptor>' entry for undeclared interceptor " + interceptor);
                            ejbJar.addInterceptor(new Interceptor(interceptor));
                        }
                    }
                }

                /*
                 * @Interceptors
                 */
                final List<Annotated<Class<?>>> annotatedClasses = sortClasses(annotationFinder.findMetaAnnotatedClasses(Interceptors.class));
                for (final Annotated<Class<?>> interceptorsAnnotatedClass : annotatedClasses) {
                    final Interceptors interceptors = interceptorsAnnotatedClass.getAnnotation(Interceptors.class);
                    final EjbJar ejbJar = ejbModule.getEjbJar();
                    for (final Class interceptor : interceptors.value()) {
                        if (ejbJar.getInterceptor(interceptor.getName()) == null) {
                            ejbJar.addInterceptor(new Interceptor(interceptor.getName()));
                        }
                    }

                    final InterceptorBinding binding = new InterceptorBinding(bean);
                    assemblyDescriptor.getInterceptorBinding().add(0, binding);

                    for (final Class interceptor : interceptors.value()) {
                        binding.getInterceptorClass().add(interceptor.getName());
                    }
                }

                final List<Annotated<Method>> annotatedMethods = sortMethods(annotationFinder.findMetaAnnotatedMethods(Interceptors.class));
                for (final Annotated<Method> method : annotatedMethods) {
                    final Interceptors interceptors = method.getAnnotation(Interceptors.class);
                    if (interceptors != null) {
                        final EjbJar ejbJar = ejbModule.getEjbJar();
                        for (final Class interceptor : interceptors.value()) {
                            if (ejbJar.getInterceptor(interceptor.getName()) == null) {
                                ejbJar.addInterceptor(new Interceptor(interceptor.getName()));
                            }
                        }

                        final InterceptorBinding binding = new InterceptorBinding(bean);
                        assemblyDescriptor.getInterceptorBinding().add(0, binding);

                        for (final Class interceptor : interceptors.value()) {
                            binding.getInterceptorClass().add(interceptor.getName());
                        }

                        binding.setMethod(new NamedMethod(method.get()));
                    }
                }

                /*
                 * @ExcludeDefaultInterceptors
                 */
                final ExcludeDefaultInterceptors excludeDefaultInterceptors = clazz.getAnnotation(ExcludeDefaultInterceptors.class);
                if (excludeDefaultInterceptors != null) {
                    final InterceptorBinding binding = assemblyDescriptor.addInterceptorBinding(new InterceptorBinding(bean));
                    binding.setExcludeDefaultInterceptors(true);
                }

                for (final Annotated<Method> method : annotationFinder.findMetaAnnotatedMethods(ExcludeDefaultInterceptors.class)) {
                    final InterceptorBinding binding = assemblyDescriptor.addInterceptorBinding(new InterceptorBinding(bean));
                    binding.setExcludeDefaultInterceptors(true);
                    binding.setMethod(new NamedMethod(method.get()));
                }

                for (final Annotated<Method> method : sortMethods(annotationFinder.findMetaAnnotatedMethods(ExcludeClassInterceptors.class))) {
                    final InterceptorBinding binding = assemblyDescriptor.addInterceptorBinding(new InterceptorBinding(bean));
                    binding.setExcludeClassInterceptors(true);
                    binding.setMethod(new NamedMethod(method.get()));
                }

                /**
                 * All beans except MDBs have remoting capabilities (busines or legacy interfaces)
                 */
                if (bean instanceof RemoteBean) {
                    final RemoteBean remoteBean = (RemoteBean) bean;

                    /*
                     * @RemoteHome
                     */
                    if (remoteBean.getHome() == null) {
                        final RemoteHome remoteHome = getInheritableAnnotation(clazz, RemoteHome.class);
                        if (remoteHome != null) {
                            final Class<?> homeClass = remoteHome.value();
                            try {
                                Method create = null;
                                for (final Method method : homeClass.getMethods()) {
                                    if (method.getName().startsWith("create")) {
                                        create = method;
                                        break;
                                    }
                                }
                                if (create == null) {
                                    throw new NoSuchMethodException("create");
                                }

                                final Class<?> remoteClass = create.getReturnType();
                                remoteBean.setHome(homeClass.getName());
                                remoteBean.setRemote(remoteClass.getName());
                            } catch (final NoSuchMethodException e) {
                                logger.error("Class annotated as a RemoteHome has no 'create()' method.  Unable to determine remote interface type.  Bean class: " + clazz.getName() + ",  Home class: " + homeClass.getName());
                            }
                        }
                    }

                    /*
                     * @LocalHome
                     */
                    if (remoteBean.getLocalHome() == null) {
                        final LocalHome localHome = getInheritableAnnotation(clazz, LocalHome.class);
                        if (localHome != null) {
                            final Class<?> homeClass = localHome.value();
                            try {
                                Method create = null;
                                for (final Method method : homeClass.getMethods()) {
                                    if (method.getName().startsWith("create")) {
                                        create = method;
                                        break;
                                    }
                                }
                                if (create == null) {
                                    throw new NoSuchMethodException("create");
                                }

                                final Class<?> remoteClass = create.getReturnType();
                                remoteBean.setLocalHome(homeClass.getName());
                                remoteBean.setLocal(remoteClass.getName());
                            } catch (final NoSuchMethodException e) {
                                logger.error("Class annotated as a LocalHome has no 'create()' method.  Unable to determine remote interface type.  Bean class: " + clazz.getName() + ",  Home class: " + homeClass.getName());
                            }
                        }
                    }

                    /*
                     * Annotations specific to @Stateless, @Stateful and @Singleton beans
                     */
                    if (remoteBean instanceof SessionBean) {
                        final SessionBean sessionBean = (SessionBean) remoteBean;

                        // add parents
                        sessionBean.getParents().add(clazz.getName());
                        if (!clazz.isInterface()) {
                            for (Class<?> current = clazz.getSuperclass(); !current.equals(Object.class); current = current.getSuperclass()) {
                                sessionBean.getParents().add(current.getName());
                            }
                        }

                        /*
                        * @Remote
                        * @Local
                        * @WebService
                        * @WebServiceProvider
                        */
                        processSessionInterfaces(sessionBean, clazz, ejbModule);

                        /*
                         * @Asynchronous
                         */
                        processAsynchronous(bean, annotationFinder);

                        /*
                         * Allow for all session bean types
                         * @DependsOn
                         */
                        if (sessionBean.getDependsOn() == null) {
                            final DependsOn dependsOn = getInheritableAnnotation(clazz, DependsOn.class);
                            if (dependsOn != null) {
                                sessionBean.setDependsOn(dependsOn.value());
                            } else {
                                sessionBean.setDependsOn(Collections.EMPTY_LIST);
                            }
                        }

                        /**
                         * Annotations for singletons and stateless
                         */
                        if (sessionBean.getSessionType() != SessionType.STATEFUL) {
                            // REST can be fun
                            if (annotationFinder.isAnnotationPresent(Path.class)) {
                                sessionBean.setRestService(true);
                            }
                        }

                        /*
                         * Annotations specific to @Singleton beans
                         */
                        if (sessionBean.getSessionType() == SessionType.SINGLETON) {

                            /*
                             * @ConcurrencyManagement
                             */
                            if (sessionBean.getConcurrencyManagementType() == null) {
                                final ConcurrencyManagement tx = getInheritableAnnotation(clazz, ConcurrencyManagement.class);
                                jakarta.ejb.ConcurrencyManagementType concurrencyType = jakarta.ejb.ConcurrencyManagementType.CONTAINER;
                                if (tx != null) {
                                    concurrencyType = tx.value();
                                }
                                switch (concurrencyType) {
                                    case BEAN:
                                        sessionBean.setConcurrencyManagementType(ConcurrencyManagementType.BEAN);
                                        break;
                                    case CONTAINER:
                                        sessionBean.setConcurrencyManagementType(ConcurrencyManagementType.CONTAINER);
                                        break;
                                }
                            }

                            /*
                             * @Lock
                             */
                            final LockHandler lockHandler = new LockHandler(assemblyDescriptor, sessionBean);
                            if (sessionBean.getConcurrencyManagementType() == ConcurrencyManagementType.CONTAINER) {
                                processAttributes(lockHandler, clazz, annotationFinder);
                            } else {
                                checkAttributes(lockHandler, ejbName, ejbModule, annotationFinder, "invalidConcurrencyAttribute");
                            }

                            /*
                             * @AccessTimeout
                             */
                            final AccessTimeoutHandler accessTimeoutHandler =
                                new AccessTimeoutHandler(assemblyDescriptor, sessionBean, lockHandler.getContainerConcurrency());
                            processAttributes(accessTimeoutHandler, clazz, annotationFinder);

                            /*
                             * @Startup
                             */
                            if (!sessionBean.hasInitOnStartup()) {
                                final Startup startup = getInheritableAnnotation(clazz, Startup.class);
                                sessionBean.setInitOnStartup(startup != null);
                            }

                        } else if (sessionBean.getSessionType() == SessionType.STATEFUL) {
                            /*
                             * Annotations specific to @Stateful beans
                             */

                            /*
                             * @StatefulTimeout
                             */
                            if (sessionBean.getStatefulTimeout() == null) {
                                final StatefulTimeout annotation = getInheritableAnnotation(clazz, StatefulTimeout.class);
                                if (annotation != null) {
                                    final Timeout timeout = new Timeout();
                                    timeout.setTimeout(annotation.value());
                                    timeout.setUnit(annotation.unit());
                                    sessionBean.setStatefulTimeout(timeout);
                                }
                            }

                            /*
                             * @AccessTimeout
                             */
                            final AccessTimeoutHandler accessTimeoutHandler = new AccessTimeoutHandler(assemblyDescriptor, sessionBean);
                            processAttributes(accessTimeoutHandler, clazz, annotationFinder);

                        }
                    }
                }

                if (bean instanceof MessageDrivenBean) {
                    /*
                     * @ActivationConfigProperty
                     */
                    final MessageDrivenBean mdb = (MessageDrivenBean) bean;
                    final MessageDriven messageDriven = clazz.getAnnotation(MessageDriven.class);
                    if (messageDriven != null) {
                        ActivationConfig activationConfig = mdb.getActivationConfig();
                        if (activationConfig == null) {
                            activationConfig = new ActivationConfig();
                        }

                        if (!messageDriven.mappedName().isEmpty()) {
                            if (mdb.getActivationConfig() == null) {
                                mdb.setActivationConfig(activationConfig);
                            }
                            if (!activationConfig.toProperties().containsKey("destinationType")) {
                                activationConfig.addProperty("destinationType", Queue.class.getName());
                            }
                            activationConfig.addProperty("destination", messageDriven.mappedName());
                        }

                        final ActivationConfigProperty[] configProperties = messageDriven.activationConfig();
                        if (configProperties != null) {
                            if (mdb.getActivationConfig() == null) {
                                mdb.setActivationConfig(activationConfig);
                            }

                            final Properties properties = activationConfig.toProperties();
                            for (final ActivationConfigProperty property : configProperties) {
                                if (!properties.containsKey(property.propertyName())) {
                                    activationConfig.addProperty(property.propertyName(), property.propertyValue());
                                }
                            }
                        }

                        if (mdb.getMessagingType() == null) {
                            final Class<?> interfce = messageDriven.messageListenerInterface();
                            if (interfce != null && !interfce.equals(Object.class)) {
                                if (!interfce.isInterface()) {
                                    // TODO: Move this check to o.a.o.c.rules.CheckClasses and do it for all MDBs, annotated or not
                                    throw new OpenEJBException("MessageListenerInterface property of @MessageDriven is not an interface");
                                }
                                mdb.setMessagingType(interfce.getName());
                            }
                        }
                    }

                    /*
                     * Determine the MessageListener interface
                     */
                    if (mdb.getMessagingType() == null) {
                        final List<Class<?>> interfaces = new ArrayList<>();
                        for (final Class<?> intf : clazz.getInterfaces()) {
                            final String name = intf.getName();
                            if (!name.equals("java.io.Serializable") &&
                                !name.equals("java.io.Externalizable") &&
                                !name.startsWith("jakarta.ejb.") &&
                                !intf.isSynthetic()) {
                                interfaces.add(intf);
                            }
                        }

                        if (interfaces.size() != 1) {
                            StringBuilder msg = new StringBuilder("When annotating a bean class as @MessageDriven without" +
                                    " declaring messageListenerInterface, the bean must implement exactly one interface, no more and" +
                                    " no less. beanClass=" + clazz.getName() + " interfaces=");
                            for (final Class<?> intf : interfaces) {
                                msg.append(intf.getName()).append(", ");
                            }
                            // TODO: Make this a validation failure, not an exception
                            throw new IllegalStateException(msg.toString());
                        }
                        mdb.setMessagingType(interfaces.get(0).getName());
                    }
                }

                buildAnnotatedRefs(bean, annotationFinder, classLoader);

                processWebServiceHandlers(ejbModule, bean, annotationFinder);

                processWebServiceClientHandlers(bean, annotationFinder, classLoader);

                try {
                    if (BeanContext.Comp.class.getName().equals(bean.getEjbClass())) {
                        buildAnnotatedRefs(bean, ejbModule.getFinder(), classLoader);
                    }
                } catch (final OpenEJBException e) {
                    logger.error("Processing of @Resource, @EJB, and other references failed for CDI managed beans", e);
                }
            }

            for (final Interceptor interceptor : ejbModule.getEjbJar().getInterceptors()) {
                final Class<?> clazz;
                try {
                    clazz = classLoader.loadClass(realClassName(interceptor.getInterceptorClass()));
                } catch (final ClassNotFoundException e) {
                    throw new OpenEJBException("Unable to load interceptor class: " + interceptor.getInterceptorClass(), e);
                }

                final AnnotationFinder annotationFinder = createFinder(clazz);

                /*
                 * @PostConstruct
                 * @PreDestroy
                 * @AroundInvoke
                 * @Timeout
                 * @PostActivate
                 * @PrePassivate
                 * @Init
                 * @Remove
                 */
                processCallbacks(interceptor, annotationFinder);

                /*
                 * @PAroundConstruct can only be on the interceptor itself
                 */
                final boolean override = "true".equalsIgnoreCase(getProperty("openejb.callbacks.override", "false"));
                if (apply(override, interceptor.getAroundConstruct())) {
                    for (final Annotated<Method> method : sortMethods(annotationFinder.findMetaAnnotatedMethods(AroundConstruct.class))) {
                        interceptor.getAroundConstruct().add(new LifecycleCallback(method.get()));
                    }
                }

                /*
                 * @ApplicationException
                 */
                processApplicationExceptions(clazz, ejbModule.getEjbJar().getAssemblyDescriptor());

                /*
                 * @EJB
                 * @Resource
                 * @WebServiceRef
                 * @PersistenceUnit
                 * @PersistenceContext
                 */
                buildAnnotatedRefs(interceptor, annotationFinder, classLoader);

                processWebServiceClientHandlers(interceptor, annotationFinder, classLoader);

                /**
                 * Interceptors do not have their own section in ejb-jar.xml for resource references
                 * so we add them to the references of each ejb.  A bit backwards but more or less
                 * mandated by the design of the spec.
                 */
                for (final EnterpriseBean bean : enterpriseBeans) {
                    // Just simply merge the injection targets of the interceptors to enterprise beans
                    mergeJndiReferences(interceptor.getEnvEntryMap(), bean.getEnvEntryMap());
                    mergeJndiReferences(interceptor.getEjbRefMap(), bean.getEjbRefMap());
                    mergeJndiReferences(interceptor.getEjbLocalRefMap(), bean.getEjbLocalRefMap());
                    mergeJndiReferences(interceptor.getResourceRefMap(), bean.getResourceRefMap());
                    mergeJndiReferences(interceptor.getResourceEnvRefMap(), bean.getResourceEnvRefMap());
                    mergeJndiReferences(interceptor.getPersistenceContextRefMap(), bean.getPersistenceContextRefMap());
                    mergeJndiReferences(interceptor.getPersistenceUnitRefMap(), bean.getPersistenceUnitRefMap());
                    mergeJndiReferences(interceptor.getMessageDestinationRefMap(), bean.getMessageDestinationRefMap());
                    mergeJndiReferences(interceptor.getServiceRefMap(), bean.getServiceRefMap());
                }
            }

            return ejbModule;
        }

        private void processAsynchronous(final EnterpriseBean bean, final AnnotationFinder annotationFinder) {
            if (!(bean instanceof SessionBean)) {
                return;
            }

            final SessionBean sessionBean = (SessionBean) bean;

            for (final Annotated<Method> method : annotationFinder.findMetaAnnotatedMethods(Asynchronous.class)) {
                sessionBean.getAsyncMethod().add(new AsyncMethod(method.get()));
            }

            //Spec 4.5.1 @Asynchronous could be used at the class level of a bean-class ( or superclass ).
            //Seems that it should not be used on the any interface view

            for (final Annotated<Class<?>> clazz : annotationFinder.findMetaAnnotatedClasses(Asynchronous.class)) {
                if (!clazz.get().isInterface()) {
                    sessionBean.getAsynchronousClasses().add(clazz.get().getName());
                }
            }
        }

        private <T extends Injectable> void mergeJndiReferences(final Map<String, T> from, final Map<String, T> to) {
            for (final Map.Entry<String, T> entry : from.entrySet()) {
                final Injectable injectable = to.get(entry.getKey());
                if (injectable == null) {
                    to.put(entry.getKey(), entry.getValue());
                } else {
                    injectable.getInjectionTarget().addAll(entry.getValue().getInjectionTarget());
                }
            }
        }

        //TODO why is this necessary, we scan for exceptions with this annotation elsewhere.
        private void processApplicationExceptions(final Class<?> clazz, final AssemblyDescriptor assemblyDescriptor) {
            /*
             * @ApplicationException
             */
            for (final Method method : clazz.getMethods()) {
                for (final Class<?> exception : method.getExceptionTypes()) {
                    final ApplicationException annotation = exception.getAnnotation(ApplicationException.class);
                    if (annotation == null) {
                        continue;
                    }
                    if (assemblyDescriptor.getApplicationException(exception) != null) {
                        mergeApplicationExceptionAnnotation(assemblyDescriptor, exception, annotation);
                    } else {
                        logger.debug("Found previously undetected application exception {0} listed on a method {1} with annotation {2}", method, exception, annotation);
                        assemblyDescriptor.addApplicationException(exception, annotation.rollback(), annotation.inherited());
                    }
                }
            }
        }

        private void processSessionInterfaces(final SessionBean sessionBean, final Class<?> beanClass, final EjbModule ejbModule) {

            final ValidationContext validation = ejbModule.getValidation();
            final String ejbName = sessionBean.getEjbName();

            final boolean strict = getProperty(STRICT_INTERFACE_DECLARATION, "false").equalsIgnoreCase("true");

            /*
             * Collect all interfaces explicitly declared via xml.
             * We will subtract these from the interfaces implemented
             * by the bean and do annotation scanning on the remainder.
             */
            final List<String> descriptor = new ArrayList<>();
            descriptor.add(sessionBean.getHome());
            descriptor.add(sessionBean.getRemote());
            descriptor.add(sessionBean.getLocalHome());
            descriptor.add(sessionBean.getLocal());
            descriptor.addAll(sessionBean.getBusinessLocal());
            descriptor.addAll(sessionBean.getBusinessRemote());
            descriptor.add(sessionBean.getServiceEndpoint());

            final BusinessInterfaces xml = new BusinessInterfaces();
            xml.addLocals(sessionBean.getBusinessLocal(), ejbModule.getClassLoader());
            xml.addRemotes(sessionBean.getBusinessRemote(), ejbModule.getClassLoader());

            if (beanClass.getAnnotation(LocalBean.class) != null) {
                sessionBean.setLocalBean(new Empty());
            }

            /**
             * Anything declared as both <business-local> and <business-remote> is invalid in strict mode
             */
            if (strict) {
                for (final Class interfce : xml.local) {
                    if (xml.remote.contains(interfce)) {
                        validation.fail(ejbName, "xml.localRemote.conflict", interfce.getName());
                    }
                }
            }

            /**
             * Merge the xml declared business interfaces into the complete set
             */
            final BusinessInterfaces all = new BusinessInterfaces();
            all.local.addAll(xml.local);
            all.remote.addAll(xml.remote);

            final List<Class<?>> classes = strict ? new ArrayList(Collections.singletonList(beanClass)) : Classes.ancestors(beanClass);

            for (final Class<?> clazz : classes) {

                /*
                 * @WebService
                 * @WebServiceProvider
                 */
                Class<?> webServiceItf = null;
                if (sessionBean.getServiceEndpoint() == null) {
                    Class defaultEndpoint = BeanContext.ServiceEndpoint.class;

                    for (final Class interfce : clazz.getInterfaces()) {
                        if (interfce.isAnnotationPresent(WebService.class)) {
                            defaultEndpoint = interfce;
                            webServiceItf = interfce;
                        }
                    }

                    final WebService webService = clazz.getAnnotation(WebService.class);
                    if (webService != null) {

                        final String className = webService.endpointInterface();

                        if (!className.isEmpty()) {
                            sessionBean.setServiceEndpoint(className);
                        } else {
                            sessionBean.setServiceEndpoint(defaultEndpoint.getName());
                        }
                    } else if (clazz.isAnnotationPresent(WebServiceProvider.class)) {
                        sessionBean.setServiceEndpoint(defaultEndpoint.getName());
                    } else if (!defaultEndpoint.equals(BeanContext.ServiceEndpoint.class)) {
                        sessionBean.setServiceEndpoint(defaultEndpoint.getName());
                    }
                }

                /*
                 * These interface types are not eligable to be business interfaces.
                 * java.io.Serializable
                 * java.io.Externalizable
                 * jakarta.ejb.*
                 */
                final List<Class<?>> interfaces = new ArrayList<>();
                if (!clazz.isInterface()) { // dynamic proxy implementation
                    for (final Class<?> interfce : clazz.getInterfaces()) {
                        final String name = interfce.getName();
                        if (!name.equals("scala.ScalaObject") &&
                            !name.equals("groovy.lang.GroovyObject") &&
                            !name.equals("java.io.Serializable") &&
                            !name.equals("java.io.Externalizable") &&
                            !(name.equals(InvocationHandler.class.getName()) && DynamicSubclass.isDynamic(beanClass)) &&
                            !name.startsWith("jakarta.ejb.") &&
                            !descriptor.contains(interfce.getName()) &&
                            !interfce.isSynthetic() &&
                            !"net.sourceforge.cobertura.coveragedata.HasBeenInstrumented".equals(name) &&
                            !name.startsWith("org.scalatest.")) {
                            interfaces.add(interfce);
                        }
                    }
                }

                /**
                 * Anything discovered and delcared in a previous loop
                 * or at the beginning in the deployment descriptor is
                 * not eligable to be redefined.
                 */
                interfaces.removeAll(all.local);
                interfaces.removeAll(all.remote);

                /**
                 * OK, now start checking the class metadata
                 */
                final Local local = clazz.getAnnotation(Local.class);
                final Remote remote = clazz.getAnnotation(Remote.class);

                final boolean impliedLocal = local != null && local.value().length == 0;
                final boolean impliedRemote = remote != null && remote.value().length == 0;

                /**
                 * This set holds the values of @Local and @Remote
                 * when applied to the bean class itself
                 *
                 * These declarations override any similar declaration
                 * on the interface.
                 */
                final BusinessInterfaces bean = new BusinessInterfaces();
                if (local != null) {
                    bean.local.addAll(Arrays.asList(local.value()));
                }
                if (remote != null) {
                    bean.remote.addAll(Arrays.asList(remote.value()));
                }

                if (strict) {
                    for (final Class interfce : bean.local) {
                        if (bean.remote.contains(interfce)) {
                            validation.fail(ejbName, "ann.localRemote.conflict", interfce.getName());
                        }
                    }
                }

                /**
                 * Anything listed explicitly via @Local or @Remote
                 * on the bean class does not need to be investigated.
                 * We do not need to check these interfaces for @Local or @Remote
                 */
                interfaces.removeAll(bean.local);
                interfaces.removeAll(bean.remote);

                if (impliedLocal || impliedRemote) {
                    if (interfaces.size() != 1) {
                        /**
                         * Cannot imply either @Local or @Remote and list multiple interfaces
                         */
                        // Need to extract the class names and append .class to them to show proper validation level 3 message
                        final List<String> interfaceNames = new ArrayList<>();
                        for (final Class<?> intrfce : interfaces) {
                            interfaceNames.add(intrfce.getName() + ".class");
                        }

                        // just warn for @Local since Glassfish supports it even if it is weird
                        // still fail for @Remote!
                        if (impliedLocal && local.value().length == 0 && interfaces.size() == 0 && !strict) {
                            validation.warn(ejbName, "ann.local.forLocalBean", Join.join(", ", interfaceNames));
                            // we don't go out to let be deployed
                        } else if (impliedLocal) {
                            validation.fail(ejbName, "ann.local.noAttributes", Join.join(", ", interfaceNames));
                            /**
                             * This bean is invalid, so do not bother looking at the other interfaces or the superclass
                             */
                            return;
                        }
                        if (impliedRemote) {
                            validation.fail(ejbName, "ann.remote.noAttributes", Join.join(", ", interfaceNames));
                            /**
                             * This bean is invalid, so do not bother looking at the other interfaces or the superclass
                             */
                            return;
                        }
                    } else if (strict && impliedLocal && impliedRemote) {
                        final Class<?> interfce = interfaces.remove(0);
                        /**
                         * Cannot imply @Local and @Remote at the same time with strict mode on
                         */
                        validation.fail(ejbName, "ann.localRemote.ambiguous", interfce.getName());
                    } else {
                        if (impliedLocal) {
                            bean.local.addAll(interfaces);
                        }
                        if (impliedRemote) {
                            bean.remote.addAll(interfaces);
                        }

                        interfaces.clear();
                    }
                }

                /**
                 * OK, now start checking the metadata of the interfaces implemented by this class
                 */

                /**
                 * This set holds the list of interfaces that the bean implements
                 * that are annotated either as @Local or @Remote
                 *
                 * If the interface is annotated to the contrary in the bean class
                 * the bean class meta data wins, therefore we track these separately
                 *
                 * Ultimately, the deployment descriptor wins over all, so we have tracked
                 * those declarations separately as well.
                 */
                final BusinessInterfaces implemented = new BusinessInterfaces();

                for (final Class interfce : interfaces) {
                    final boolean isLocal = interfce.isAnnotationPresent(Local.class);
                    final boolean isRemote = interfce.isAnnotationPresent(Remote.class);

                    if (strict && isLocal && isRemote) {
                        validation.fail(ejbName, "ann.localRemote.conflict", interfce.getName());
                    } else {
                        final Class[] superInterface = interfce.getInterfaces();
                        if (isLocal) {
                            if (strict) {
                                for (final Class si : superInterface) {
                                    final boolean present = si.isAnnotationPresent(Remote.class);
                                    if (present) {
                                        validation.fail(ejbName, "ann.remoteOrLocal.converse.parent", interfce.getName(), "Local", si.getName(), "Remote");
                                    }
                                }
                            }
                            implemented.local.add(interfce);
                        }
                        if (isRemote) {
                            if (strict) {
                                for (final Class si : superInterface) {
                                    final boolean present = si.isAnnotationPresent(Local.class);
                                    if (present) {
                                        validation.fail(ejbName, "ann.remoteOrLocal.converse.parent", interfce.getName(), "Remote", si.getName(), "Local");
                                    }
                                }
                            }
                            implemented.remote.add(interfce);
                        }
                    }

                }

                interfaces.removeAll(implemented.local);
                interfaces.removeAll(implemented.remote);


                /**
                 * Merge in class-level metadata.
                 *
                 * We've already merged in the xml metadata, so that
                 * metadata will win over this metadata.
                 */

                // remove anything we've already seen
                bean.local.removeAll(all.local);
                bean.local.removeAll(all.remote);
                bean.remote.removeAll(all.remote);
                bean.remote.removeAll(all.local);

                // validate the things we are going to add
                for (final Class interfce : bean.local) {
                    validateLocalInterface(interfce, validation, ejbName);
                }
                for (final Class interfce : bean.remote) {
                    validateRemoteInterface(interfce, validation, ejbName);
                }

                // add finally, add them
                all.local.addAll(bean.local);
                all.remote.addAll(bean.remote);


                /**
                 * Merge in interface-level metadata
                 *
                 * We've already merged in the xml metadata *and* class metadata,
                 * so both of those will win over this metadata.
                 */

                // remove anything we've already seen
                implemented.local.removeAll(all.local);
                implemented.local.removeAll(all.remote);
                implemented.remote.removeAll(all.remote);
                implemented.remote.removeAll(all.local);

                // validate the things we are going to add
                for (final Class interfce : implemented.local) {
                    validateLocalInterface(interfce, validation, ejbName);
                }
                for (final Class interfce : implemented.remote) {
                    validateRemoteInterface(interfce, validation, ejbName);
                }

                // add the rest
                all.local.addAll(implemented.local);
                all.remote.addAll(implemented.remote);


                // We only consider the top-most class (the bean class itself) when evaluating
                // the case of absolutely no metadata at all and attempting to figure out the
                // default view which will be implied as either @LocalBean or @Local
                if (clazz == beanClass
                    && sessionBean.getLocalBean() == null
                    && sessionBean.getBusinessLocal().isEmpty()
                    && sessionBean.getBusinessRemote().isEmpty()
                    && sessionBean.getHome() == null
                    && sessionBean.getRemote() == null
                    && sessionBean.getLocalHome() == null
                    && sessionBean.getLocal() == null
                    && all.local.isEmpty()
                    && all.remote.isEmpty()
                    ) {

                    if (interfaces.size() == 0 || DynamicProxyImplFactory.isKnownDynamicallyImplemented(clazz)) {
                        // No interfaces?  Then @LocalBean

                        sessionBean.setLocalBean(new Empty());

                    } else if (interfaces.size() == 1) {
                        // One interface?  Then @Local

                        all.local.add(interfaces.remove(0));

                    } else {
                        // Multiple interfaces?  Illegal
                        validation.fail(ejbName, "too.many.interfaces", ejbName, interfaces.toString().replace("interface ", ""));
                        return;
                    }
                }

                // do it here to not loose the @Local handling (if (interfaces.size() == 1))
                if (webserviceAsRemote
                    && webServiceItf != null
                    && all.remote.isEmpty()) {
                    all.remote.add(webServiceItf);
                }

                //alway set Local View for ManagedBean
                if (beanClass.isAnnotationPresent(ManagedBean.class)) {
                    sessionBean.setLocalBean(new Empty());
                }
            }

            // Finally, add all the business interfaces we found
            for (final Class interfce : all.local) {
                sessionBean.addBusinessLocal(interfce);
            }
            for (final Class interfce : all.remote) {
                sessionBean.addBusinessRemote(interfce);
            }
        }

        private static class BusinessInterfaces {
            private final Set<Class> local = new LinkedHashSet<>();
            private final Set<Class> remote = new LinkedHashSet<>();

            public void addLocals(final Collection<String> names, final ClassLoader loader) {
                add(loader, names, local);
            }

            public void addRemotes(final Collection<String> names, final ClassLoader loader) {
                add(loader, names, remote);
            }

            private void add(final ClassLoader loader, final Collection<String> names, final Set<Class> classes) {
                for (final String className : names) {
                    try {
                        classes.add(loader.loadClass(realClassName(className)));
                    } catch (final Throwable t) {
                        // handled in validation
                    }
                }
            }
        }

        private String getProperty(final String key, final String defaultValue) {
            String value = SystemInstance.get().getOptions().get(key, defaultValue);
            final DeploymentModule module = getModule();

            if (module instanceof EjbModule) {
                final EjbModule ejbModule = (EjbModule) module;

                final OpenejbJar openejbJar = ejbModule.getOpenejbJar();
                if (openejbJar != null && openejbJar.getProperties() != null) {
                    value = openejbJar.getProperties().getProperty(key, value);
                }
            }
            return value;
        }

        private void processSecurityAnnotations(final Class<?> beanClass, final String ejbName, final EjbModule ejbModule, final AnnotationFinder annotationFinder, final EnterpriseBean bean) {
            final AssemblyDescriptor assemblyDescriptor = ejbModule.getEjbJar().getAssemblyDescriptor();

            final List<String> classPermissions = getDeclaredClassPermissions(assemblyDescriptor, ejbName);

            for (final Class<?> clazzz : Classes.ancestors(beanClass)) {
                final MetaAnnotatedClass<?> clazz = new MetaAnnotatedClass(clazzz);
                /*
                 * Process annotations at the class level
                 */
                if (!classPermissions.contains("*") || !classPermissions.contains(clazz.getName())) {

                    final RolesAllowed rolesAllowed = clazz.getAnnotation(RolesAllowed.class);
                    final PermitAll permitAll = clazz.getAnnotation(PermitAll.class);
                    final DenyAll denyAll = clazz.getAnnotation(DenyAll.class);

                    /*
                     * @RolesAllowed
                     */
                    if ((rolesAllowed != null && permitAll != null)
                        || (rolesAllowed != null && denyAll != null)
                        || (permitAll != null && denyAll != null)) {
                        ejbModule.getValidation().fail(ejbName, "permitAllAndRolesAllowedOnClass", clazz.getName());
                    }

                    if (rolesAllowed != null) {
                        final MethodPermission methodPermission = new MethodPermission();
                        methodPermission.getRoleName().addAll(Arrays.asList(rolesAllowed.value()));
                        methodPermission.getMethod().add(new org.apache.openejb.jee.Method(ejbName, clazz.getName(), "*"));
                        assemblyDescriptor.getMethodPermission().add(methodPermission);

                        // Automatically add a role ref for any role listed in RolesAllowed
                        final RemoteBean remoteBean = (RemoteBean) bean;
                        final List<SecurityRoleRef> securityRoleRefs = remoteBean.getSecurityRoleRef();
                        for (final String role : rolesAllowed.value()) {
                            securityRoleRefs.add(new SecurityRoleRef(role));
                        }
                    }

                    /*
                     * @PermitAll
                     */
                    if (permitAll != null) {
                        final MethodPermission methodPermission = new MethodPermission();
                        methodPermission.setUnchecked(true);
                        methodPermission.getMethod().add(new org.apache.openejb.jee.Method(ejbName, clazz.getName(), "*"));
                        assemblyDescriptor.getMethodPermission().add(methodPermission);
                    }

                    /**
                     * @DenyAll
                     */
                    if (denyAll != null) {
                        assemblyDescriptor.getExcludeList()
                            .addMethod(new org.apache.openejb.jee.Method(ejbName, clazz.getName(), "*"));
                    }
                }

                /*
                 * @RunAs
                 */
                final RunAs runAs = clazz.getAnnotation(RunAs.class);
                if (runAs != null && bean.getSecurityIdentity() == null) {
                    final SecurityIdentity securityIdentity = new SecurityIdentity();
                    securityIdentity.setRunAs(runAs.value());
                    bean.setSecurityIdentity(securityIdentity);
                }

                /*
                 * @DeclareRoles
                 */
                final DeclareRoles declareRoles = clazz.getAnnotation(DeclareRoles.class);
                if (declareRoles != null && bean instanceof RemoteBean) {
                    final RemoteBean remoteBean = (RemoteBean) bean;
                    final List<SecurityRoleRef> securityRoleRefs = remoteBean.getSecurityRoleRef();
                    for (final String role : declareRoles.value()) {
                        securityRoleRefs.add(new SecurityRoleRef(role));
                    }
                }
            }

            /*
             * Process annotations at the method level
             */
            final List<Method> seen = new ArrayList<>();

            /*
             * @RolesAllowed
             */
            for (final Annotated<Method> method : annotationFinder.findMetaAnnotatedMethods(RolesAllowed.class)) {
                checkConflictingSecurityAnnotations(method, ejbName, ejbModule, seen);
                final RolesAllowed rolesAllowed = method.getAnnotation(RolesAllowed.class);
                final MethodPermission methodPermission = new MethodPermission();
                methodPermission.getRoleName().addAll(Arrays.asList(rolesAllowed.value()));
                methodPermission.getMethod().add(new org.apache.openejb.jee.Method(ejbName, method.get()));
                assemblyDescriptor.getMethodPermission().add(methodPermission);

                // Automatically add a role ref for any role listed in RolesAllowed
                final RemoteBean remoteBean = (RemoteBean) bean;
                final List<SecurityRoleRef> securityRoleRefs = remoteBean.getSecurityRoleRef();
                for (final String role : rolesAllowed.value()) {
                    securityRoleRefs.add(new SecurityRoleRef(role));
                }
            }

            /*
             * @PermitAll
             */
            for (final Annotated<Method> method : annotationFinder.findMetaAnnotatedMethods(PermitAll.class)) {
                checkConflictingSecurityAnnotations(method, ejbName, ejbModule, seen);
                final MethodPermission methodPermission = new MethodPermission();
                methodPermission.setUnchecked(true);
                methodPermission.getMethod().add(new org.apache.openejb.jee.Method(ejbName, method.get()));
                assemblyDescriptor.getMethodPermission().add(methodPermission);
            }

            /*
             * @DenyAll
             */
            for (final Annotated<Method> method : annotationFinder.findMetaAnnotatedMethods(DenyAll.class)) {
                checkConflictingSecurityAnnotations(method, ejbName, ejbModule, seen);
                final ExcludeList excludeList = assemblyDescriptor.getExcludeList();
                excludeList.addMethod(new org.apache.openejb.jee.Method(ejbName, method.get()));
            }

        }

        /**
         * Validation
         *
         * Conflicting use of @RolesAllowed, @PermitAll, and @DenyAll
         *
         * @param method
         * @param ejbName
         * @param ejbModule
         * @param seen
         */
        private void checkConflictingSecurityAnnotations(final Annotated<Method> method, final String ejbName, final EjbModule ejbModule, final List<Method> seen) {
            if (seen.contains(method.get())) {
                return;
            } else {
                seen.add(method.get());
            }

            final List<String> annotations = new ArrayList<>();
            for (final Class<? extends Annotation> annotation : Arrays.asList(RolesAllowed.class, PermitAll.class, DenyAll.class)) {
                if (method.getAnnotation(annotation) != null) {
                    annotations.add("@" + annotation.getSimpleName());
                }
            }

            if (annotations.size() > 1) {
                ejbModule.getValidation().fail(ejbName, "conflictingSecurityAnnotations", method.get().getName(), Join.join(" and ", annotations), method.get().getDeclaringClass());
            }
        }

        private void processSchedules(final EnterpriseBean bean, final AnnotationFinder annotationFinder) {
            if (!(bean instanceof TimerConsumer)) {
                return;
            }
            final TimerConsumer timerConsumer = (TimerConsumer) bean;
            final Set<Annotated<Method>> scheduleMethods = new HashSet<>();
            scheduleMethods.addAll(annotationFinder.findMetaAnnotatedMethods(Schedules.class));
            scheduleMethods.addAll(annotationFinder.findMetaAnnotatedMethods(Schedule.class));

            final List<Timer> timers = timerConsumer.getTimer();

            // TODO : The NamedMethod object implements equals and hashCode, so we could rely on that rather than collecting strings
            final Set<String> methodsConfiguredInDeploymentXml = new HashSet<>();
            for (final Timer timer : timers) {
                final NamedMethod namedMethod = timer.getTimeoutMethod();
                methodsConfiguredInDeploymentXml.add(namedMethod.getMethodName() + (namedMethod.getMethodParams() == null ? "" : Join.join("", namedMethod.getMethodParams().getMethodParam())));
            }

            for (final Annotated<Method> method : scheduleMethods) {

                // Don't add the schedules from annotations if the schedules have been
                // supplied for this method via xml.  The xml is considered an override.
                if (methodsConfiguredInDeploymentXml.contains(method.get().getName() + Join.join("", (Object[]) asStrings(method.get().getParameterTypes())))) {
                    continue;
                }

                final List<Schedule> scheduleAnnotationList = new ArrayList<>();

                final Schedules schedulesAnnotation = method.getAnnotation(Schedules.class);
                if (schedulesAnnotation != null) {
                    scheduleAnnotationList.addAll(Arrays.asList(schedulesAnnotation.value()));
                }

                final Schedule scheduleAnnotation = method.getAnnotation(Schedule.class);
                if (scheduleAnnotation != null) {
                    scheduleAnnotationList.add(scheduleAnnotation);
                }

                for (final Schedule schedule : scheduleAnnotationList) {
                    final Timer timer = new Timer();
                    timer.setPersistent(schedule.persistent());
                    timer.setInfo(schedule.info() == null || schedule.info().isEmpty() ? null : schedule.info());
                    timer.setTimezone(schedule.timezone() == null || schedule.timezone().isEmpty() ? null : schedule.timezone());
                    //Copy TimerSchedule
                    final TimerSchedule timerSchedule = new TimerSchedule();
                    timerSchedule.setSecond(schedule.second());
                    timerSchedule.setMinute(schedule.minute());
                    timerSchedule.setHour(schedule.hour());
                    timerSchedule.setDayOfWeek(schedule.dayOfWeek());
                    timerSchedule.setDayOfMonth(schedule.dayOfMonth());
                    timerSchedule.setMonth(schedule.month());
                    timerSchedule.setYear(schedule.year());
                    timer.setSchedule(timerSchedule);
                    //Copy Method Signature
                    timer.setTimeoutMethod(new NamedMethod(method.get()));

                    timers.add(timer);
                }
            }
        }

        private void processCallbacks(final Lifecycle bean, final AnnotationFinder annotationFinder) {

            final boolean override = "true".equalsIgnoreCase(getProperty("openejb.callbacks.override", "false"));

            /*
             * @PostConstruct
             */
            if (apply(override, bean.getPostConstruct())) {
                for (final Annotated<Method> method : sortMethods(annotationFinder.findMetaAnnotatedMethods(PostConstruct.class))) {
                    bean.getPostConstruct().add(new LifecycleCallback(method.get()));
                }
            }

            /*
             * @PreDestroy
             */
            if (apply(override, bean.getPreDestroy())) {
                for (final Annotated<Method> method : sortMethods(annotationFinder.findMetaAnnotatedMethods(PreDestroy.class))) {
                    bean.getPreDestroy().add(new LifecycleCallback(method.get()));
                }
            }

            if (bean instanceof Invokable) {
                final Invokable invokable = (Invokable) bean;

                /*
                 * @AroundInvoke
                 */
                if (apply(override, invokable.getAroundInvoke())) {
                    for (final Annotated<Method> method : sortMethods(annotationFinder.findMetaAnnotatedMethods(jakarta.interceptor.AroundInvoke.class))) {
                        invokable.getAroundInvoke().add(new AroundInvoke(method.get()));
                    }
                }

                /*
                 *  @AroundTimeout
                 */
                if (apply(override, invokable.getAroundTimeout())) {
                    for (final Annotated<Method> method : sortMethods(annotationFinder.findMetaAnnotatedMethods(jakarta.interceptor.AroundTimeout.class))) {
                        invokable.getAroundTimeout().add(new AroundTimeout(method.get()));
                    }
                }
            }

            /*
             * @Timeout
             */
            if (bean instanceof TimerConsumer) {
                final TimerConsumer timerConsumer = (TimerConsumer) bean;
                if (timerConsumer.getTimeoutMethod() == null) {
                    final List<Annotated<Method>> timeoutMethods = sortMethods(annotationFinder.findMetaAnnotatedMethods(jakarta.ejb.Timeout.class));
                    //Validation Logic is moved to CheckCallback class.
                    if (timeoutMethods.size() >= 1) {
                        // Use the timeout method most near the child class because
                        // the timeout method in child class will override the timeout method in super classes
                        timerConsumer.setTimeoutMethod(new NamedMethod(timeoutMethods.get(timeoutMethods.size() - 1).get()));
                    }
                }
            }

            if (bean instanceof Session) {
                final Session session = (Session) bean;

                /*
                 * @AfterBegin
                 */
                final LifecycleCallback afterBegin = getFirst(session.getAfterBegin());
                if (afterBegin == null) {
                    for (final Annotated<Method> method : sortMethods(annotationFinder.findMetaAnnotatedMethods(AfterBegin.class))) {
                        session.getAfterBegin().add(new LifecycleCallback(method.get()));
                    }
                }

                /*
                 * @BeforeCompletion
                 */
                final LifecycleCallback beforeCompletion = getFirst(session.getBeforeCompletion());
                if (beforeCompletion == null) {
                    for (final Annotated<Method> method : sortMethods(annotationFinder.findMetaAnnotatedMethods(BeforeCompletion.class))) {
                        session.getBeforeCompletion().add(new LifecycleCallback(method.get()));
                    }
                }

                /*
                 * @AfterCompletion
                 */
                final LifecycleCallback afterCompletion = getFirst(session.getAfterCompletion());
                if (afterCompletion == null) {
                    for (final Annotated<Method> method : sortMethods(annotationFinder.findMetaAnnotatedMethods(AfterCompletion.class))) {
                        session.getAfterCompletion().add(new LifecycleCallback(method.get()));
                    }
                }

                /*
                 * @PostActivate
                 */
                if (apply(override, session.getPostActivate())) {
                    for (final Annotated<Method> method : sortMethods(annotationFinder.findMetaAnnotatedMethods(PostActivate.class))) {
                        session.getPostActivate().add(new LifecycleCallback(method.get()));
                    }
                }

                /*
                 * @PrePassivate
                 */
                if (apply(override, session.getPrePassivate())) {
                    for (final Annotated<Method> method : sortMethods(annotationFinder.findMetaAnnotatedMethods(PrePassivate.class))) {
                        session.getPrePassivate().add(new LifecycleCallback(method.get()));
                    }
                }
                /*
                 * @Init
                 */
                for (final Annotated<Method> method : sortMethods(annotationFinder.findMetaAnnotatedMethods(Init.class))) {
                    final InitMethod initMethod = new InitMethod(method.get());

                    final Init init = method.getAnnotation(Init.class);
                    if (init.value() != null && !init.value().isEmpty()) {
                        initMethod.setCreateMethod(init.value());
                    }

                    session.getInitMethod().add(initMethod);
                }

                /*
                 * @Remove
                 */
                final List<Annotated<Method>> removeMethods = sortMethods(annotationFinder.findMetaAnnotatedMethods(Remove.class));
                final Map<NamedMethod, RemoveMethod> declaredRemoveMethods = new HashMap<>();
                for (final RemoveMethod removeMethod : session.getRemoveMethod()) {
                    declaredRemoveMethods.put(removeMethod.getBeanMethod(), removeMethod);
                }
                for (final Annotated<Method> method : removeMethods) {
                    final Remove remove = method.getAnnotation(Remove.class);
                    final RemoveMethod removeMethod = new RemoveMethod(method.get(), remove.retainIfException());

                    final RemoveMethod declaredRemoveMethod = declaredRemoveMethods.get(removeMethod.getBeanMethod());

                    if (declaredRemoveMethod == null) {
                        session.getRemoveMethod().add(removeMethod);
                    } else if (!declaredRemoveMethod.isExplicitlySet()) {
                        declaredRemoveMethod.setRetainIfException(remove.retainIfException());
                    }
                }
            }
        }

        private boolean apply(final boolean override, final List<?> list) {
            // Compliant behavior is to always add the annotated callbacks
            // into the list of xml configured callbacks

            // Legacy behavior was to not apply the annotations to the list
            // if there were any of the related elements specified in the xml

            // if we are *not* using the legacy logic, always return true
            if (!override) {
                return true;
            }

            // if we are using that logic, then only return true if the list is empty
            // i.e. we will not augment the list if callbacks have been specified in xml
            return list.size() == 0;
        }

        public void buildAnnotatedRefs(final JndiConsumer consumer, final IAnnotationFinder annotationFinder, final ClassLoader classLoader) throws OpenEJBException {
            //
            // @EJB
            //

            final List<EJB> ejbList = new ArrayList<>();
            for (final Annotated<Class<?>> clazz : annotationFinder.findMetaAnnotatedClasses(EJBs.class)) {
                final EJBs ejbs = clazz.getAnnotation(EJBs.class);
                ejbList.addAll(Arrays.asList(ejbs.value()));
            }
            for (final Annotated<Class<?>> clazz : annotationFinder.findMetaAnnotatedClasses(EJB.class)) {
                final EJB e = clazz.getAnnotation(EJB.class);
                ejbList.add(e);
            }

            for (final EJB ejb : ejbList) {
                buildEjbRef(consumer, ejb, null);
            }

            for (final Annotated<Field> field : annotationFinder.findMetaAnnotatedFields(EJB.class)) {
                final EJB ejb = field.getAnnotation(EJB.class);

                final Member member = new FieldMember(field.get());

                buildEjbRef(consumer, ejb, member);
            }

            for (final Annotated<Method> method : annotationFinder.findMetaAnnotatedMethods(EJB.class)) {
                final EJB ejb = method.getAnnotation(EJB.class);

                final Member member = new MethodMember(method.get());

                buildEjbRef(consumer, ejb, member);
            }

            //
            // @Resource
            //

            final List<Resource> resourceList = new ArrayList<>();
            for (final Annotated<Class<?>> clazz : annotationFinder.findMetaAnnotatedClasses(Resources.class)) {
                final Resources resources = clazz.getAnnotation(Resources.class);
                resourceList.addAll(Arrays.asList(resources.value()));
            }
            for (final Annotated<Class<?>> clazz : annotationFinder.findMetaAnnotatedClasses(Resource.class)) {
                final Resource resource = clazz.getAnnotation(Resource.class);
                resourceList.add(resource);
            }

            for (final Resource resource : resourceList) {
                buildResource(consumer, resource, null);
            }

            for (final Annotated<Field> field : annotationFinder.findMetaAnnotatedFields(Resource.class)) {
                final Resource resource = field.getAnnotation(Resource.class);

                final Member member = new FieldMember(field.get());

                buildResource(consumer, resource, member);
            }

            for (final Annotated<Method> method : annotationFinder.findMetaAnnotatedMethods(Resource.class)) {
                final Resource resource = method.getAnnotation(Resource.class);

                final Member member = new MethodMember(method.get());

                buildResource(consumer, resource, member);
            }

            //
            // @Context (REST)
            //
            for (final Annotated<Field> field : annotationFinder.findMetaAnnotatedFields(Context.class)) {
                final Member member = new FieldMember(field.get());
                buildContext(consumer, member);
            }

            //
            // @WebServiceRef
            //

            final List<WebServiceRef> webservicerefList = new ArrayList<>();
            for (final Annotated<Class<?>> clazz : annotationFinder.findMetaAnnotatedClasses(WebServiceRefs.class)) {
                final WebServiceRefs webServiceRefs = clazz.getAnnotation(WebServiceRefs.class);
                webservicerefList.addAll(Arrays.asList(webServiceRefs.value()));
            }
            for (final Annotated<Class<?>> clazz : annotationFinder.findMetaAnnotatedClasses(WebServiceRef.class)) {
                final WebServiceRef webServiceRef = clazz.getAnnotation(WebServiceRef.class);
                webservicerefList.add(webServiceRef);
            }

            for (final WebServiceRef webserviceref : webservicerefList) {

                buildWebServiceRef(consumer, webserviceref, null, null, classLoader);
            }

            for (final Annotated<Field> field : annotationFinder.findMetaAnnotatedFields(WebServiceRef.class)) {
                final WebServiceRef webserviceref = field.getAnnotation(WebServiceRef.class);
                final HandlerChain handlerChain = field.getAnnotation(HandlerChain.class);

                final Member member = new FieldMember(field.get());

                buildWebServiceRef(consumer, webserviceref, handlerChain, member, classLoader);
            }

            for (final Annotated<Method> method : annotationFinder.findMetaAnnotatedMethods(WebServiceRef.class)) {
                final WebServiceRef webserviceref = method.getAnnotation(WebServiceRef.class);
                final HandlerChain handlerChain = method.getAnnotation(HandlerChain.class);

                final Member member = new MethodMember(method.get());

                buildWebServiceRef(consumer, webserviceref, handlerChain, member, classLoader);
            }

            //
            // @PersistenceUnit
            //

            final List<PersistenceUnit> persistenceUnitList = new ArrayList<>();
            for (final Annotated<Class<?>> clazz : annotationFinder.findMetaAnnotatedClasses(PersistenceUnits.class)) {
                final PersistenceUnits persistenceUnits = clazz.getAnnotation(PersistenceUnits.class);
                persistenceUnitList.addAll(Arrays.asList(persistenceUnits.value()));
            }
            for (final Annotated<Class<?>> clazz : annotationFinder.findMetaAnnotatedClasses(PersistenceUnit.class)) {
                final PersistenceUnit persistenceUnit = clazz.getAnnotation(PersistenceUnit.class);
                persistenceUnitList.add(persistenceUnit);
            }
            for (final PersistenceUnit pUnit : persistenceUnitList) {
                buildPersistenceUnit(consumer, pUnit, null);
            }
            for (final Annotated<Field> field : annotationFinder.findMetaAnnotatedFields(PersistenceUnit.class)) {
                final PersistenceUnit pUnit = field.getAnnotation(PersistenceUnit.class);
                final Member member = new FieldMember(field.get());
                buildPersistenceUnit(consumer, pUnit, member);
            }
            for (final Annotated<Method> method : annotationFinder.findMetaAnnotatedMethods(PersistenceUnit.class)) {
                final PersistenceUnit pUnit = method.getAnnotation(PersistenceUnit.class);
                final Member member = new MethodMember(method.get());
                buildPersistenceUnit(consumer, pUnit, member);
            }

            //
            // @PersistenceContext
            //

            final PersistenceContextAnnFactory pcFactory = new PersistenceContextAnnFactory();
            final List<PersistenceContext> persistenceContextList = new ArrayList<>();
            for (final Annotated<Class<?>> clazz : annotationFinder.findMetaAnnotatedClasses(PersistenceContexts.class)) {
                final PersistenceContexts persistenceContexts = clazz.getAnnotation(PersistenceContexts.class);
                persistenceContextList.addAll(Arrays.asList(persistenceContexts.value()));
                pcFactory.addAnnotations(clazz.get());
            }
            for (final Annotated<Class<?>> clazz : annotationFinder.findMetaAnnotatedClasses(PersistenceContext.class)) {
                final PersistenceContext persistenceContext = clazz.getAnnotation(PersistenceContext.class);
                persistenceContextList.add(persistenceContext);
                pcFactory.addAnnotations(clazz.get());

                // dynamic proxy implementation
                if (clazz.get().isInterface()) {
                    final Member member = new FilledMember("em", EntityManager.class, clazz.get());

                    buildPersistenceContext(consumer, pcFactory.create(persistenceContext, member), member);
                }
            }
            for (final PersistenceContext pCtx : persistenceContextList) {
                buildPersistenceContext(consumer, pcFactory.create(pCtx, null), null);
            }
            for (final Annotated<Field> field : annotationFinder.findMetaAnnotatedFields(PersistenceContext.class)) {
                final PersistenceContext pCtx = field.getAnnotation(PersistenceContext.class);
                final Member member = new FieldMember(field.get());
                buildPersistenceContext(consumer, pcFactory.create(pCtx, member), member);
            }
            for (final Annotated<Method> method : annotationFinder.findMetaAnnotatedMethods(PersistenceContext.class)) {
                final PersistenceContext pCtx = method.getAnnotation(PersistenceContext.class);
                final Member member = new MethodMember(method.get());
                buildPersistenceContext(consumer, pcFactory.create(pCtx, member), member);
            }

            //
            // @DataSourceDefinition
            //

            for (final Annotated<Class<?>> annotated : annotationFinder.findMetaAnnotatedClasses(DataSourceDefinitions.class)) {
                final DataSourceDefinitions defs = annotated.getAnnotation(DataSourceDefinitions.class);
                for (final DataSourceDefinition definition : defs.value()) {
                    buildDataSourceDefinition(consumer, definition);
                }
            }

            for (final Annotated<Class<?>> annotated : annotationFinder.findMetaAnnotatedClasses(DataSourceDefinition.class)) {
                final DataSourceDefinition definition = annotated.getAnnotation(DataSourceDefinition.class);
                buildDataSourceDefinition(consumer, definition);
            }

            //
            // @ContextServiceDefinition
            //

            for (final Annotated<Class<?>> annotated : annotationFinder.findMetaAnnotatedClasses(ContextServiceDefinition.List.class)) {
                final ContextServiceDefinition.List defs = annotated.getAnnotation(ContextServiceDefinition.List.class);
                for (final ContextServiceDefinition definition : defs.value()) {
                    buildContextServiceDefinition(consumer, definition);
                }
            }

            for (final Annotated<Class<?>> annotated : annotationFinder.findMetaAnnotatedClasses(ContextServiceDefinition.class)) {
                final ContextServiceDefinition definition = annotated.getAnnotation(ContextServiceDefinition.class);
                buildContextServiceDefinition(consumer, definition);
            }

            //
            // @JMSConnectionFactoryDefinition
            //

            for (final Annotated<Class<?>> annotated : annotationFinder.findMetaAnnotatedClasses(JMSConnectionFactoryDefinitions.class)) {
                final JMSConnectionFactoryDefinitions defs = annotated.getAnnotation(JMSConnectionFactoryDefinitions.class);
                for (final JMSConnectionFactoryDefinition definition : defs.value()) {
                    buildConnectionFactoryDefinition(consumer, definition);
                }
            }

            for (final Annotated<Class<?>> annotated : annotationFinder.findMetaAnnotatedClasses(JMSConnectionFactoryDefinition.class)) {
                final JMSConnectionFactoryDefinition definition = annotated.getAnnotation(JMSConnectionFactoryDefinition.class);
                buildConnectionFactoryDefinition(consumer, definition);
            }

            //
            // @JMSDestinationDefinition
            //
            for (final Annotated<Class<?>> annotated : annotationFinder.findMetaAnnotatedClasses(JMSDestinationDefinitions.class)) {
                final JMSDestinationDefinitions defs = annotated.getAnnotation(JMSDestinationDefinitions.class);
                for (final JMSDestinationDefinition definition : defs.value()) {
                    buildDestinationDefinition(consumer, definition);
                }
            }

            for (final Annotated<Class<?>> annotated : annotationFinder.findMetaAnnotatedClasses(JMSDestinationDefinition.class)) {
                buildDestinationDefinition(consumer, annotated.getAnnotation(JMSDestinationDefinition.class));
            }
        }

        private void buildContextServiceDefinition(final JndiConsumer consumer, final ContextServiceDefinition definition) {
            final ContextService existing = consumer.getContextServiceMap().get(definition.name());
            final ContextService contextService = (existing != null) ? existing : new ContextService();

            if (contextService.getName() == null) {
                final JndiName jndiName = new JndiName();
                jndiName.setvalue(definition.name());
                contextService.setName(jndiName);
            }

            if (contextService.getCleared().isEmpty()) {
                contextService.getCleared().addAll(Arrays.asList(definition.cleared()));
            }

            if (contextService.getPropagated().isEmpty()) {
                contextService.getPropagated().addAll(Arrays.asList(definition.propagated()));
            }

            if (contextService.getUnchanged().isEmpty()) {
                contextService.getUnchanged().addAll(Arrays.asList(definition.unchanged()));
            }

            consumer.getContextServiceMap().put(definition.name(), contextService);
        }

        private void buildContext(final JndiConsumer consumer, final Member member) {
            final ContextRef ref = new ContextRef();
            ref.setName(member.getDeclaringClass().getName() + "/" + member.getName());
            ref.setResType(member.getType().getName());

            final InjectionTarget target = new InjectionTarget();
            target.setInjectionTargetClass(member.getDeclaringClass().getName());
            target.setInjectionTargetName(member.getName());
            ref.getInjectionTarget().add(target);

            consumer.getResourceRef().add(ref);
        }

        /**
         * Process @EJB into <ejb-ref> or <ejb-local-ref> for the specified member (field or method)
         *
         * @param consumer
         * @param ejb
         * @param member
         */
        private void buildEjbRef(final JndiConsumer consumer, final EJB ejb, final Member member) {

            // TODO: Looks like we aren't looking for an existing ejb-ref or ejb-local-ref
            // we need to do this to support overriding.

            /**
             * Was @EJB used at a class level witout specifying the 'name' or 'beanInterface' attributes?
             */
            final String name = consumer.getJndiConsumerName();
            if (member == null) {
                boolean shouldReturn = false;
                if (ejb.name().isEmpty()) {
                    fail(name, "ejbAnnotation.onClassWithNoName");
                    shouldReturn = true;
                }
                if (ejb.beanInterface().equals(Object.class)) {
                    fail(name, "ejbAnnotation.onClassWithNoBeanInterface");
                    shouldReturn = true;
                }
                if (shouldReturn) {
                    return;
                }
            }

            final EjbRef ejbRef = new EjbRef();

            // This is how we deal with the fact that we don't know
            // whether to use an EjbLocalRef or EjbRef (remote).
            // We flag it uknown and let the linking code take care of
            // figuring out what to do with it.
            ejbRef.setRefType(EjbReference.Type.UNKNOWN);

            // Get the ejb-ref-name
            String refName = ejb.name();
            if (refName.length() == 0) {
                refName = member.getDeclaringClass().getName() + "/" + member.getName();
            }

            //TODO can refName actually be null?
            ejbRef.setEjbRefName(normalize(refName));

            if (member != null) {
                // Set the member name where this will be injected
                final InjectionTarget target = new InjectionTarget();
                target.setInjectionTargetClass(member.getDeclaringClass().getName());
                target.setInjectionTargetName(member.getName());
                ejbRef.getInjectionTarget().add(target);

            }

            Class<?> interfce = ejb.beanInterface();
            if (interfce.equals(Object.class)) {
                interfce = member == null ? null : member.getType();
            }

            final boolean localbean = isKnownLocalBean(interfce);
            final boolean dynamicallyImplemented = DynamicProxyImplFactory.isKnownDynamicallyImplemented(interfce);

            if (!localbean && interfce != null && !isValidEjbInterface(name, interfce, ejbRef.getName())) {
                return;
            }

            if (interfce != null && !interfce.equals(Object.class)) {
                if (EJBHome.class.isAssignableFrom(interfce)) {
                    ejbRef.setHome(interfce.getName());
                    final Method[] methods = interfce.getMethods();
                    for (final Method method : methods) {
                        if (method.getName().startsWith("create")) {
                            ejbRef.setRemote(method.getReturnType().getName());
                            break;
                        }
                    }
                    ejbRef.setRefType(EjbReference.Type.REMOTE);
                } else if (EJBLocalHome.class.isAssignableFrom(interfce)) {
                    ejbRef.setHome(interfce.getName());
                    final Method[] methods = interfce.getMethods();
                    for (final Method method : methods) {
                        if (method.getName().startsWith("create")) {
                            ejbRef.setRemote(method.getReturnType().getName());
                            break;
                        }
                    }
                    ejbRef.setRefType(EjbReference.Type.LOCAL);
                } else if (localbean) {
                    ejbRef.setRefType(EjbReference.Type.LOCAL);
                    ejbRef.setRemote(interfce.getName());
                } else if (dynamicallyImplemented) {
                    ejbRef.setRefType(EjbReference.Type.LOCAL);
                    ejbRef.setRemote(interfce.getName());
                } else {
                    ejbRef.setRemote(interfce.getName());
                    if (interfce.getAnnotation(Local.class) != null) {
                        ejbRef.setRefType(EjbReference.Type.LOCAL);
                    } else if (interfce.getAnnotation(Remote.class) != null) {
                        ejbRef.setRefType(EjbReference.Type.REMOTE);
                    }
                }
            }

            // Set the ejb-link, if any
            String ejbName = ejb.beanName();
            if (ejbName.isEmpty()) {
                ejbName = null;
            }
            ejbRef.setEjbLink(ejbName);

            // Set the mappedName, if any
            String mappedName = ejb.mappedName();
            if (mappedName.isEmpty()) {
                mappedName = null;
            }
            ejbRef.setMappedName(mappedName);

            // Set lookup name, if any
            String lookupName = getLookupName(ejb);
            if (lookupName.isEmpty()) {
                lookupName = null;
            }
            ejbRef.setLookupName(lookupName);

            final Map<String, EjbRef> remoteRefs = consumer.getEjbRefMap();
            if (remoteRefs.containsKey(ejbRef.getName())) {
                final EjbRef ref = remoteRefs.get(ejbRef.getName());
                if (ref.getRemote() == null) {
                    ref.setRemote(ejbRef.getRemote());
                }
                if (ref.getHome() == null) {
                    ref.setHome(ejbRef.getHome());
                }
                if (ref.getMappedName() == null) {
                    ref.setMappedName(ejbRef.getMappedName());
                }
                ref.getInjectionTarget().addAll(ejbRef.getInjectionTarget());
                return;
            }

            final Map<String, EjbLocalRef> localRefs = consumer.getEjbLocalRefMap();
            if (localRefs.containsKey(ejbRef.getName())) {
                final EjbLocalRef ejbLocalRef = new EjbLocalRef(ejbRef);
                final EjbLocalRef ref = localRefs.get(ejbLocalRef.getName());
                if (ref.getLocal() == null) {
                    ref.setLocal(ejbLocalRef.getLocal());
                }
                if (ref.getLocalHome() == null) {
                    ref.setLocalHome(ejbLocalRef.getLocalHome());
                }
                if (ref.getMappedName() == null) {
                    ref.setMappedName(ejbLocalRef.getMappedName());
                }
                if(ref.getEjbLink() == null){
                    ref.setEjbLink(ejbLocalRef.getEjbLink());
                }
                ref.getInjectionTarget().addAll(ejbLocalRef.getInjectionTarget());
                return;
            }

            switch (ejbRef.getRefType()) {
                case UNKNOWN:
                case REMOTE:
                    consumer.getEjbRef().add(ejbRef);
                    break;
                case LOCAL:
                    consumer.getEjbLocalRef().add(new EjbLocalRef(ejbRef));
                    break;
            }
        }


        private String normalize(final String refName) {
            if (refName.startsWith("java:")) {
                return refName.startsWith("/") ? refName.substring(1) : refName;
            }
            return "java:comp/env/" + refName;
        }

        private boolean isKnownLocalBean(final Class clazz) {
            if (clazz.isAnnotation()) {
                return false;
            }
            if (clazz.isArray()) {
                return false;
            }
            if (clazz.isEnum()) {
                return false;
            }
            if (clazz.isInterface()) {
                return false;
            }
            if (clazz.isPrimitive()) {
                return false;
            }
            if (Modifier.isAbstract(clazz.getModifiers())) {
                return false;
            }
            if (Modifier.isFinal(clazz.getModifiers())) {
                return false;
            }

            return true;
        }

        private boolean isValidEjbInterface(final String b, final Class clazz, final String refName) {
            if (!clazz.isInterface()) { //NOPMD
                //It is not an interface. No validation necessary.
            } else if (EJBObject.class.isAssignableFrom(clazz)) {
                fail(b, "ann.ejb.ejbObject", clazz.getName(), refName);
                return false;
            } else if (EJBLocalObject.class.isAssignableFrom(clazz)) {
                fail(b, "ann.ejb.ejbLocalObject", clazz.getName(), refName);
                return false;
            }
            return true;
        }

        private void fail(final String component, final String key, final Object... details) {
            getValidationContext().fail(component, key, details);
        }

        /**
         * Process @Resource into either <resource-ref> or <resource-env-ref> for the given member (field or method) or class
         *
         * @param consumer
         * @param resource
         * @param member
         */
        private void buildResource(final JndiConsumer consumer, final Resource resource, final Member member) {

            /**
             * Was @Resource used at a class level without specifying the 'name' or 'beanInterface' attributes?
             */
            if (member == null) {
                if (resource.name().length() == 0) {
                    fail(consumer.getJndiConsumerName(), "resourceAnnotation.onClassWithNoName");
                    return;
                }
            }

            // Get the ref-name
            String refName = resource.name();
            if (refName.isEmpty()) {
                refName = member.getDeclaringClass().getName() + "/" + member.getName();
            }

            refName = normalize(refName);

            JndiReference reference = consumer.getEnvEntryMap().get(refName);
            if (reference == null) {

                /**
                 * Was @Resource mistakenly used when either @PersistenceContext or @PersistenceUnit should have been used?
                 */
                if (member != null) { // Little quick validation for common mistake
                    final Class type = member.getType();
                    if (EntityManager.class.isAssignableFrom(type)) {
                        fail(consumer.getJndiConsumerName(), "resourceRef.onEntityManager", refName);
                        return;
                    } else if (EntityManagerFactory.class.isAssignableFrom(type)) {
                        fail(consumer.getJndiConsumerName(), "resourceRef.onEntityManagerFactory", refName);
                        return;
                    }
                }

                final Class type;
                if (member == null) {
                    type = resource.type();
                } else {
                    type = member.getType();
                }

                if (knownResourceEnvTypes.contains(type.getName())) {
                    /*
                     * @Resource <resource-env-ref>
                     */
                    ResourceEnvRef resourceEnvRef = consumer.getResourceEnvRefMap().get(refName);
                    if (resourceEnvRef == null) {
                        resourceEnvRef = new ResourceEnvRef();
                        resourceEnvRef.setName(refName);
                        consumer.getResourceEnvRef().add(resourceEnvRef);
                    }

                    if (resourceEnvRef.getResourceEnvRefType() == null || "".equals(resourceEnvRef.getResourceEnvRefType())) {
                        resourceEnvRef.setResourceEnvRefType(type.getName());
                    }
                    reference = resourceEnvRef;
                } else if (isKnownEnvironmentEntryType(type)) {
                    /*
                     * @Resource <env-entry>
                     *
                     * Add an env-entry via @Resource if 'lookup' attribute is set.
                     */
                    final String lookupName = getLookupName(resource);
                    if (!lookupName.isEmpty()) {
                        final EnvEntry envEntry = new EnvEntry();
                        envEntry.setName(refName);
                        consumer.getEnvEntry().add(envEntry);

                        envEntry.setLookupName(lookupName);

                        reference = envEntry;
                    } else if (isShareableJNDINamespace(refName)) {
                        final EnvEntry envEntry = new EnvEntry();
                        envEntry.setName(refName);
                        consumer.getEnvEntry().add(envEntry);
                        reference = envEntry;
                    } else {

                        final String shortName = normalize(member.getName());
                        reference = consumer.getEnvEntryMap().get(shortName);

                        if (reference == null) {
                            final EnvEntry envEntry = new EnvEntry();
                            envEntry.setName(refName);
                            consumer.getEnvEntry().add(envEntry);
                            reference = envEntry;
                        }

//                        /*
//                         * Can't add env-entry since @Resource.lookup is not set and it is NOT in a shareable JNDI name space
//                         */
//                        return;
                    }
                } else {
                    /*
                     * @Resource <resource-ref>
                     */
                    ResourceRef resourceRef = consumer.getResourceRefMap().get(refName);

                    if (resourceRef == null) {
                        resourceRef = new ResourceRef();
                        resourceRef.setName(refName);
                        consumer.getResourceRef().add(resourceRef);
                    }

                    if (member != null) {
                        resourceRef.setOrigin(member.getDeclaringClass() + "#" + member.getName());
                    } // TODO: else @Resource on a class

                    if (resourceRef.getResAuth() == null) {
                        if (resource.authenticationType() == Resource.AuthenticationType.APPLICATION) {
                            resourceRef.setResAuth(ResAuth.APPLICATION);
                        } else {
                            resourceRef.setResAuth(ResAuth.CONTAINER);
                        }
                    }

                    if (resourceRef.getResType() == null || "".equals(resourceRef.getResType())) {
                        resourceRef.setResType(type.getName());
                    }

                    if (resourceRef.getResSharingScope() == null) {
                        if (resource.shareable()) {
                            resourceRef.setResSharingScope(ResSharingScope.SHAREABLE);
                        } else {
                            resourceRef.setResSharingScope(ResSharingScope.UNSHAREABLE);
                        }
                    }
                    reference = resourceRef;
                }
            }

            /*
             * Fill in the injection information <injection-target>
             */
            if (member != null) {
                // Set the member name where this will be injected
                final InjectionTarget target = new InjectionTarget();
                target.setInjectionTargetClass(member.getDeclaringClass().getName());
                target.setInjectionTargetName(member.getName());
                reference.getInjectionTarget().add(target);
            }

            // Override the mapped name if not set
            if (reference.getMappedName() == null && !resource.mappedName().isEmpty()) {
                reference.setMappedName(resource.mappedName());
            }

            // Override the lookup name if not set
            if (reference.getLookupName() == null) {
                final String lookupName = getLookupName(resource);
                if (!lookupName.isEmpty()) {
                    reference.setLookupName(lookupName);
                }
            }
        }

        private static Method getLookupMethod(final Class cls) {
            final String name = cls.getName();
            if (!lookupMissing.contains(name)) {
                try {
                    return cls.getMethod("lookup", null);
                } catch (final NoSuchMethodException e) {
                    lookupMissing.add(name);
                    final String exists = getSourceIfExists(cls);
                    logger.warning("Method 'lookup' is not available for '" + name + "'"
                        + (null != exists ? ". The old API '" + exists + "' was found on the classpath." : ". Probably using an older Runtime."));
                }
            }

            return null;
        }

        private static String getSourceIfExists(final Class<?> cls) {
            if (cls.getProtectionDomain() != null && cls.getProtectionDomain().getCodeSource() != null
                && cls.getProtectionDomain().getCodeSource().getLocation() != null) {
                return cls.getProtectionDomain().getCodeSource().getLocation().toString();
            }
            return null;
        }

        private static String getLookupName(final Resource resource) {
            String value = "";
            final Method lookupMethod = getLookupMethod(Resource.class);
            if (lookupMethod != null) {
                try {
                    value = (String) lookupMethod.invoke(resource, null);
                } catch (final Exception e) {
                    // ignore
                }
            }
            return value;
        }


        private static String getLookupName(final EJB ejb) {
            String value = "";
            final Method lookupMethod = getLookupMethod(EJB.class);
            if (lookupMethod != null) {
                try {
                    value = (String) lookupMethod.invoke(ejb, null);
                } catch (final Exception e) {
                    // ignore
                }
            }
            return value;
        }


        /**
         * Process @PersistenceUnit into <persistence-unit> for the specified member (field or method)
         *
         * @param consumer
         * @param persistenceUnit
         * @param member
         * @throws OpenEJBException
         */
        private void buildPersistenceUnit(final JndiConsumer consumer, final PersistenceUnit persistenceUnit, final Member member) throws OpenEJBException {
            String refName = persistenceUnit.name();
            // Get the ref-name
            if (refName.length() == 0) {
                /**
                 * Was @PersistenceUnit used at a class level without specifying the 'name' attribute?
                 */
                if (member == null) {
                    fail(consumer.getJndiConsumerName(), "persistenceUnitAnnotation.onClassWithNoName", persistenceUnit.unitName());
                    return;
                }
                refName = member.getDeclaringClass().getName() + "/" + member.getName();
            }

            refName = normalize(refName);
            PersistenceUnitRef persistenceUnitRef = consumer.getPersistenceUnitRefMap().get(refName);
            if (persistenceUnitRef == null) {
                persistenceUnitRef = new PersistenceUnitRef();
                persistenceUnitRef.setPersistenceUnitName(persistenceUnit.unitName());
                persistenceUnitRef.setPersistenceUnitRefName(refName);
                consumer.getPersistenceUnitRef().add(persistenceUnitRef);
            }


            if (member != null) {
                final Class type = member.getType();
                if (EntityManager.class.isAssignableFrom(type)) {
                    failIfCdiProducer(member, "EntityManagerFactory");

                    /**
                     * Was @PersistenceUnit mistakenly used when @PersistenceContext should have been used?
                     */
                    final ValidationContext validationContext = getValidationContext();
                    final String jndiConsumerName = consumer.getJndiConsumerName();
                    final String name = persistenceUnitRef.getName();
                    validationContext.fail(jndiConsumerName, "persistenceUnitAnnotation.onEntityManager", name);
                } else if (!EntityManagerFactory.class.isAssignableFrom(type)) {
                    failIfCdiProducer(member, "EntityManagerFactory");


                    /**
                     * Was @PersistenceUnit mistakenly used for something that isn't an EntityManagerFactory?
                     */
                    fail(consumer.getJndiConsumerName(), "persistenceUnitAnnotation.onNonEntityManagerFactory", persistenceUnitRef.getName());
                } else {
                    // Set the member name where this will be injected
                    final InjectionTarget target = new InjectionTarget();
                    target.setInjectionTargetClass(member.getDeclaringClass().getName());
                    target.setInjectionTargetName(member.getName());
                    persistenceUnitRef.getInjectionTarget().add(target);
                }
            }

            if (persistenceUnitRef.getPersistenceUnitName() == null && !persistenceUnit.unitName().isEmpty()) {
                persistenceUnitRef.setPersistenceUnitName(persistenceUnit.unitName());
            }
        }

        /**
         * Process @PersistenceContext into <persistence-context> for the specified member (field or method)
         *
         * Refer 16.11.2.1 Overriding Rules of EJB Core Spec for overriding rules
         *
         * @param consumer
         * @param persistenceContext
         * @param member
         * @throws OpenEJBException
         */
        private void buildPersistenceContext(final JndiConsumer consumer, final PersistenceContextAnn persistenceContext, final Member member) throws OpenEJBException {
            AppModule module = null;
            if (currentModule.get() instanceof AppModule) {
                module = (AppModule) currentModule.get();
            } else if (currentModule.get() instanceof Module) {
                module = ((Module) currentModule.get()).getAppModule();
            }
            if (module != null
                && org.apache.openejb.jee.jpa.unit.TransactionType.RESOURCE_LOCAL.equals(module.getTransactionType(persistenceContext.unitName()))) {
                // should it be in warn level?
                // IMO no since with CDI it is tempting to do so
                String name = persistenceContext.unitName();
                if (name == null || name.isEmpty()) { // search for it
                    try { // get the first one
                        name = module.getPersistenceModules().iterator().next()
                            .getPersistence()
                            .getPersistenceUnit().iterator().next().getName();
                    } catch (final Exception e) {
                        name = "?";
                    }
                }
                logger.info("PersistenceUnit '" + name + "' is a RESOURCE_LOCAL one, " +
                    "you'll have to manage @PersistenceContext yourself.");
                return;
            }

            String refName = persistenceContext.name();

            if (refName.length() == 0) {
                /**
                 * Was @PersistenceContext used at a class level without specifying the 'name' attribute?
                 */
                if (member == null) {
                    fail(consumer.getJndiConsumerName(), "persistenceContextAnnotation.onClassWithNoName", persistenceContext.unitName());
                    return;
                }
                refName = member.getDeclaringClass().getName() + "/" + member.getName();
            }

            refName = normalize(refName);

            PersistenceContextRef persistenceContextRef = consumer.getPersistenceContextRefMap().get(refName);
            if (persistenceContextRef == null) {
                persistenceContextRef = new PersistenceContextRef();
                if (persistenceContext.synchronization() != null) { // should be the case in "normal" deployments
                    persistenceContextRef.setPersistenceContextSynchronization(PersistenceContextSynchronization.valueOf(persistenceContext.synchronization().toUpperCase(Locale.ENGLISH)));
                }
                persistenceContextRef.setPersistenceUnitName(persistenceContext.unitName());
                persistenceContextRef.setPersistenceContextRefName(refName);
                if ("EXTENDED".equalsIgnoreCase(persistenceContext.type())) {
                    persistenceContextRef.setPersistenceContextType(PersistenceContextType.EXTENDED);
                } else {
                    persistenceContextRef.setPersistenceContextType(PersistenceContextType.TRANSACTION);
                }
                consumer.getPersistenceContextRef().add(persistenceContextRef);
            } else {
                if (persistenceContextRef.getPersistenceUnitName() == null || "".equals(persistenceContextRef.getPersistenceUnitName())) {
                    persistenceContextRef.setPersistenceUnitName(persistenceContext.unitName());
                }
                if (persistenceContextRef.getPersistenceContextType() == null) {
                    if ("EXTENDED".equalsIgnoreCase(persistenceContext.type())) {
                        persistenceContextRef.setPersistenceContextType(PersistenceContextType.EXTENDED);
                    } else {
                        persistenceContextRef.setPersistenceContextType(PersistenceContextType.TRANSACTION);
                    }
                }
                if (persistenceContextRef.getPersistenceContextSynchronization() == null && persistenceContext.synchronization() != null) {
                    persistenceContextRef.setPersistenceContextSynchronization(PersistenceContextSynchronization.valueOf(persistenceContext.synchronization().toUpperCase(Locale.ENGLISH)));
                }
            }

            List<Property> persistenceProperties = persistenceContextRef.getPersistenceProperty();
            if (persistenceProperties == null) {
                persistenceProperties = new ArrayList<>();
                persistenceContextRef.setPersistenceProperty(persistenceProperties);
            }

            for (final Map.Entry<String, String> persistenceProperty : persistenceContext.properties().entrySet()) {
                boolean flag = true;
                for (final Property prpty : persistenceProperties) {
                    if (prpty.getName().equals(persistenceProperty.getKey())) {
                        flag = false;
                        break;
                    }
                }
                if (flag) {
                    final Property property = new Property();
                    property.setName(persistenceProperty.getKey());
                    property.setValue(persistenceProperty.getValue());
                    persistenceProperties.add(property);
                }
            }

            if (member != null) {
                final Class type = member.getType();
                if (EntityManagerFactory.class.isAssignableFrom(type)) {
                    failIfCdiProducer(member, "EntityManager");

                    /**
                     * Was @PersistenceContext mistakenly used when @PersistenceUnit should have been used?
                     */
                    fail(consumer.getJndiConsumerName(), "persistenceContextAnnotation.onEntityManagerFactory", persistenceContextRef.getName());
                } else if (!EntityManager.class.isAssignableFrom(type)) {
                    failIfCdiProducer(member, "EntityManager");

                    /**
                     * Was @PersistenceContext mistakenly used for something that isn't an EntityManager?
                     */
                    fail(consumer.getJndiConsumerName(), "persistenceContextAnnotation.onNonEntityManager", persistenceContextRef.getName());
                } else {
                    // Set the member name where this will be injected
                    final InjectionTarget target = new InjectionTarget();
                    target.setInjectionTargetClass(member.getDeclaringClass().getName());
                    target.setInjectionTargetName(member.getName());
                    persistenceContextRef.getInjectionTarget().add(target);
                }
            }
        }

        private void buildDestinationDefinition(final JndiConsumer consumer, final JMSDestinationDefinition definition) {
            final JMSDestination destination = new JMSDestination();
            destination.setName(definition.name());
            destination.setClassName(definition.className());
            destination.setInterfaceName(definition.interfaceName());
            destination.setResourceAdapter(definition.resourceAdapter());
            destination.setDestinationName(definition.destinationName());

            for (final String s : definition.properties()) {
                final int equal = s.indexOf('=');
                if (equal < s.length() - 1) {
                    final SuperProperties props = new SuperProperties();
                    try {
                        props.load(new ByteArrayInputStream(s.getBytes()));
                        for (final String key : props.stringPropertyNames()) {
                            if (!key.isEmpty()) {
                                destination.property(key, props.getProperty(key));
                            }
                        }
                    } catch (final IOException e) {
                        final String key = s.substring(0, equal).trim();
                        final String value = s.substring(equal + 1).trim();
                        destination.property(key, value);
                    }
                } else {
                    destination.property(s.trim(), "");
                }
            }

            consumer.getJMSDestination().add(destination);
        }

        private void buildConnectionFactoryDefinition(final JndiConsumer consumer, final JMSConnectionFactoryDefinition definition) {
            final JMSConnectionFactory connectionFactory = new JMSConnectionFactory();
            connectionFactory.setName(definition.name());
            connectionFactory.setMinPoolSize(definition.minPoolSize());
            connectionFactory.setMaxPoolSize(definition.maxPoolSize());
            connectionFactory.setClassName(definition.className());
            connectionFactory.setInterfaceName(definition.interfaceName());
            connectionFactory.setClientId(definition.clientId());
            connectionFactory.setUser(definition.user());
            connectionFactory.setPassword(definition.password());
            connectionFactory.setResourceAdapter(definition.resourceAdapter());
            connectionFactory.setTransactional(definition.transactional());

            for (final String s : definition.properties()) {
                final int equal = s.indexOf('=');
                if (equal < s.length() - 1) {
                    final SuperProperties props = new SuperProperties();
                    try {
                        props.load(new ByteArrayInputStream(s.getBytes()));
                        for (final String key : props.stringPropertyNames()) {
                            if (!key.isEmpty()) {
                                connectionFactory.property(key, props.getProperty(key));
                            }
                        }
                    } catch (final IOException e) {
                        final String key = s.substring(0, equal).trim();
                        final String value = s.substring(equal + 1).trim();
                        connectionFactory.property(key, value);
                    }
                } else {
                    connectionFactory.property(s.trim(), "");
                }
            }

            consumer.getJMSConnectionFactories().add(connectionFactory);
        }

        private void buildDataSourceDefinition(final JndiConsumer consumer, final DataSourceDefinition d) {
            final DataSource dataSource = new DataSource();

            dataSource.setName(d.name());
            dataSource.setClassName(d.className());
            dataSource.setTransactional(d.transactional());

            final DataSource existing = consumer.getDataSourceMap().get(dataSource.getKey());

            if (existing != null) {
                return;
            }

            // Optional properties
            if (!d.databaseName().isEmpty()) {
                dataSource.setDatabaseName(d.databaseName());
            }
            if (d.initialPoolSize() != -1) {
                dataSource.setInitialPoolSize(d.initialPoolSize());
            }
            if (d.isolationLevel() != -1) {
                dataSource.setIsolationLevel(IsolationLevel.fromFlag(d.isolationLevel()));
            }
            if (d.loginTimeout() != 0) {
                dataSource.setLoginTimeout(d.loginTimeout());
            }
            if (d.maxIdleTime() != -1) {
                dataSource.setMaxIdleTime(d.maxIdleTime());
            }
            if (d.maxPoolSize() != -1) {
                dataSource.setMaxPoolSize(d.maxPoolSize());
            }
            if (d.maxStatements() != -1) {
                dataSource.setMaxStatements(d.maxStatements());
            }
            if (d.minPoolSize() != -1) {
                dataSource.setMinPoolSize(d.minPoolSize());
            }
            if (!d.password().isEmpty()) {
                dataSource.setPassword(d.password());
            }
            if (d.portNumber() != -1) {
                dataSource.setPortNumber(d.portNumber());
            }
            if (!"localhost".equals(d.serverName())) {
                dataSource.setServerName(d.serverName());
            }
            if (!d.url().isEmpty()) {
                dataSource.setUrl(d.url());
            }
            if (!d.user().isEmpty()) {
                dataSource.setUser(d.user());
            }

            for (final String s : d.properties()) {
                final int equal = s.indexOf('=');
                if (equal < s.length() - 1) {
                    final SuperProperties props = new SuperProperties();
                    try {
                        props.load(new ByteArrayInputStream(s.getBytes()));
                        for (final String key : props.stringPropertyNames()) {
                            if (!key.isEmpty()) {
                                dataSource.property(key, props.getProperty(key));
                            }
                        }
                    } catch (final IOException e) {
                        final String key = s.substring(0, equal).trim();
                        final String value = s.substring(equal + 1).trim();
                        dataSource.property(key, value);
                    }
                } else {
                    dataSource.property(s.trim(), "");
                }
            }

            consumer.getDataSource().add(dataSource);
        }


        /**
         * Process @WebServiceRef and @HandlerChain for the given member (field or method)
         *
         * @param consumer
         * @param webService
         * @param handlerChain
         * @param member
         * @param classLoader
         * @throws OpenEJBException
         */
        private void buildWebServiceRef(final JndiConsumer consumer, final WebServiceRef webService, final HandlerChain handlerChain, final Member member, final ClassLoader classLoader) throws OpenEJBException {

            ServiceRef serviceRef;

            String refName = webService.name();
            if (refName.isEmpty()) {
                if (member == null) {
                    //TODO fail
                    return;
                }
                refName = member.getDeclaringClass().getName() + "/" + member.getName();
            }

            refName = normalize(refName);

            serviceRef = consumer.getServiceRefMap().get(refName);

            if (serviceRef == null) {
                serviceRef = new ServiceRef();
                serviceRef.setServiceRefName(refName);

                consumer.getServiceRef().add(serviceRef);
            }

            if (member != null) {
                // Set the member name where this will be injected
                final InjectionTarget target = new InjectionTarget();
                target.setInjectionTargetClass(member.getDeclaringClass().getName());
                target.setInjectionTargetName(member.getName());
                serviceRef.getInjectionTarget().add(target);
            }

            // Set service interface
            Class<?> serviceInterface = null;
            if (serviceRef.getServiceInterface() == null) {
                serviceInterface = webService.type();
                if (serviceInterface.equals(Object.class)) {
                    serviceInterface = webService.value();
                    if ((Service.class.equals(serviceInterface) || Object.class.equals(serviceInterface)) && member != null) {
                        serviceInterface = member.getType();
                    }
                }
            }
            if (serviceInterface == null || !Service.class.isAssignableFrom(serviceInterface)) {
                serviceInterface = Service.class;
            }
            serviceRef.setServiceInterface(serviceInterface.getName());

            // reference type
            if (serviceRef.getServiceRefType() == null || "".equals(serviceRef.getServiceRefType())) {
                if (webService.type() != Object.class) {
                    serviceRef.setServiceRefType(webService.type().getName());
                } else {
                    serviceRef.setServiceRefType(member.getType().getName());
                }
            }
            Class<?> refType = null;
            try {
                refType = classLoader.loadClass(realClassName(serviceRef.getType()));
            } catch (final ClassNotFoundException e) {
                // no-op
            }

            // Set the mappedName
            if (serviceRef.getMappedName() == null) {
                String mappedName = webService.mappedName();
                if (mappedName.isEmpty()) {
                    mappedName = null;
                }
                serviceRef.setMappedName(mappedName);
            }

            // wsdl file
            if (serviceRef.getWsdlFile() == null) {
                final String wsdlLocation = webService.wsdlLocation();
                if (!wsdlLocation.isEmpty()) {
                    serviceRef.setWsdlFile(wsdlLocation);
                }
            }

            if (SystemInstance.get().hasProperty("openejb.geronimo")) {
                return;
            }

            if (serviceRef.getWsdlFile() == null && refType != null) {
                serviceRef.setWsdlFile(JaxWsUtils.getServiceWsdlLocation(refType, classLoader));
            }
            if (serviceRef.getWsdlFile() == null) {
                serviceRef.setWsdlFile(JaxWsUtils.getServiceWsdlLocation(serviceInterface, classLoader));
            }

            // service qname
            if (serviceRef.getServiceQname() == null && refType != null) {
                try {
                    serviceRef.setServiceQname(JaxWsUtils.getServiceQName(refType));
                } catch (final IllegalArgumentException iae) {
                    if (FieldMember.class.isInstance(member) && FieldMember.class.cast(member).field.getAnnotation(Produces.class) != null) {
                        throw new DefinitionException(FieldMember.class.cast(member).field + " is not a webservice client");
                    }
                    throw iae;
                }
            }
            if (serviceRef.getServiceQname() == null) {
                serviceRef.setServiceQname(JaxWsUtils.getServiceQName(serviceInterface));
            }

            // handlers
            if (serviceRef.getHandlerChains() == null && handlerChain != null) {
                try {
                    final URL handlerFileURL = member.getDeclaringClass().getResource(handlerChain.file());
                    final HandlerChains handlerChains = ReadDescriptors.readHandlerChains(handlerFileURL);
                    serviceRef.setHandlerChains(handlerChains);
                } catch (final Throwable e) {
                    throw new OpenEJBException("Unable to load handler chain file: " + handlerChain.file(), e);
                }
            }
        }

        /**
         * Scan for @EJB, @Resource, @WebServiceRef, @PersistenceUnit, and @PersistenceContext on WebService HandlerChain classes
         */
        private void processWebServiceHandlers(final EjbModule ejbModule, final EnterpriseBean bean, final AnnotationFinder finder) throws OpenEJBException {
            // add webservice handler classes to the class finder used in annotation processing
            final Set<String> classes = new HashSet<>();
            if (ejbModule.getWebservices() != null) {
                for (final WebserviceDescription webservice : ejbModule.getWebservices().getWebserviceDescription()) {
                    for (final PortComponent port : webservice.getPortComponent()) {
                        // only process port definitions for this ejb
                        if (!bean.getEjbName().equals(port.getServiceImplBean().getEjbLink())) {
                            continue;
                        }

                        if (port.getHandlerChains() == null) {
                            continue;
                        }
                        for (final org.apache.openejb.jee.HandlerChain handlerChain : port.getHandlerChains().getHandlerChain()) {
                            for (final Handler handler : handlerChain.getHandler()) {
                                final String handlerClass = realClassName(handler.getHandlerClass());
                                if (handlerClass != null) {
                                    classes.add(handlerClass);
                                }
                            }
                        }
                    }
                }
            }
            // classes.add(bean.getEjbClass());
            final AnnotationFinder handlersFinder = finder.select(classes);
            buildAnnotatedRefs(bean, handlersFinder, ejbModule.getClassLoader());
        }

        /**
         * Scan for @EJB, @Resource, @WebServiceRef, @PersistenceUnit, and @PersistenceContext on WebService HandlerChain classes
         *
         * @param consumer
         * @param classLoader
         * @throws OpenEJBException
         */
        private void processWebServiceClientHandlers(final JndiConsumer consumer, final AnnotationFinder finder, final ClassLoader classLoader) throws OpenEJBException {
            if (SystemInstance.get().hasProperty("openejb.geronimo")) {
                return;
            }

            final Set<String> processedClasses = new HashSet<>();
            final Set<String> handlerClasses = new HashSet<>();
            do {
                // get unprocessed handler classes
                handlerClasses.clear();
                for (final ServiceRef serviceRef : consumer.getServiceRef()) {
                    final HandlerChains chains = serviceRef.getAllHandlers();
                    if (chains == null) {
                        continue;
                    }
                    for (final org.apache.openejb.jee.HandlerChain handlerChain : chains.getHandlerChain()) {
                        for (final Handler handler : handlerChain.getHandler()) {
                            if (handler.getHandlerClass() != null) {
                                handlerClasses.add(realClassName(handler.getHandlerClass()));
                            }
                        }
                    }
                }
                handlerClasses.removeAll(processedClasses);
                if (handlerClasses.isEmpty()) {
                    continue;
                }

                // process handler classes
                final AnnotationFinder handlerAnnotationFinder = finder != null ? finder.select(handlerClasses) :
                        new FinderFactory.OpenEJBAnnotationFinder(new FinderFactory.DoLoadClassesArchive(classLoader, handlerClasses));

                /*
                 * @EJB
                 * @Resource
                 * @WebServiceRef
                 * @PersistenceUnit
                 * @PersistenceContext
                 */
                buildAnnotatedRefs(consumer, handlerAnnotationFinder, classLoader);

                processedClasses.addAll(handlerClasses);
            } while (!handlerClasses.isEmpty());
        }

        // ----------------------------------------------------------------------
        //
        //  Utility methods and classes
        //
        // ----------------------------------------------------------------------


        private List<String> getDeclaredClassPermissions(final AssemblyDescriptor assemblyDescriptor, final String ejbName) {
            final List<MethodPermission> permissions = assemblyDescriptor.getMethodPermission();
            final List<String> classPermissions = new ArrayList<>();
            for (final MethodPermission permission : permissions) {
                for (final org.apache.openejb.jee.Method method : permission.getMethod()) {
                    if (!method.getEjbName().equals(ejbName)) {
                        continue;
                    }
                    if (!"*".equals(method.getMethodName())) {
                        continue;
                    }

                    String className = method.getClassName();
                    if (className == null) {
                        className = "*";
                    }
                    classPermissions.add(className);
                }
            }
            return classPermissions;
        }

        public interface AnnotationHandler<A extends Annotation> {
            Class<A> getAnnotationClass();

            Map<String, List<MethodAttribute>> getExistingDeclarations();

            void addClassLevelDeclaration(A annotation, Class clazz);

            void addMethodLevelDeclaration(A annotation, Method method);
        }

        public static class TransactionAttributeHandler implements AnnotationHandler<TransactionAttribute> {

            private final AssemblyDescriptor assemblyDescriptor;
            private final String ejbName;

            public TransactionAttributeHandler(final AssemblyDescriptor assemblyDescriptor, final String ejbName) {
                this.assemblyDescriptor = assemblyDescriptor;
                this.ejbName = ejbName;
            }

            public Map<String, List<MethodAttribute>> getExistingDeclarations() {
                return assemblyDescriptor.getMethodTransactionMap(ejbName);
            }

            public void addClassLevelDeclaration(final TransactionAttribute attribute, final Class type) {
                final ContainerTransaction ctx = new ContainerTransaction(cast(attribute.value()), type.getName(), ejbName, "*");
                assemblyDescriptor.getContainerTransaction().add(ctx);
            }

            public void addMethodLevelDeclaration(final TransactionAttribute attribute, final Method method) {
                final ContainerTransaction ctx = new ContainerTransaction(cast(attribute.value()), ejbName, method);
                assemblyDescriptor.getContainerTransaction().add(ctx);
            }

            public Class<TransactionAttribute> getAnnotationClass() {
                return TransactionAttribute.class;
            }

            private TransAttribute cast(final TransactionAttributeType transactionAttributeType) {
                return TransAttribute.valueOf(transactionAttributeType.toString());
            }
        }

        private static class ConcurrentMethodHandler {

            protected final AssemblyDescriptor assemblyDescriptor;
            protected final SessionBean bean;
            protected final Map<Object, ContainerConcurrency> methods;

            public ConcurrentMethodHandler(final AssemblyDescriptor assemblyDescriptor,
                                           final SessionBean bean,
                                           final Map<Object, ContainerConcurrency> methods) {
                this.assemblyDescriptor = assemblyDescriptor;
                this.bean = bean;
                this.methods = methods;
            }

            public Map<String, List<MethodAttribute>> getExistingDeclarations() {
                final Map<String, List<MethodAttribute>> declarations = new HashMap<>();
                final List<ConcurrentMethod> methods = bean.getConcurrentMethod();
                for (final ConcurrentMethod method : methods) {
                    List<MethodAttribute> list = declarations.computeIfAbsent(method.getMethod().getMethodName(), k -> new ArrayList<>());
                    list.add(new MethodAttribute(null, bean.getEjbName(), method.getMethod()));
                }
                return declarations;
            }

            public ContainerConcurrency getContainerConcurrency(final Method method) {
                ContainerConcurrency concurrency = methods.get(method);
                if (concurrency == null) {
                    concurrency = new ContainerConcurrency(null, bean.getEjbName(), method);
                    methods.put(method, concurrency);
                    assemblyDescriptor.getContainerConcurrency().add(concurrency);
                }
                return concurrency;
            }

            public ContainerConcurrency getContainerConcurrency(final Class clazz) {
                ContainerConcurrency concurrency = methods.get(clazz);
                if (concurrency == null) {
                    concurrency = new ContainerConcurrency(null, clazz.getName(), bean.getEjbName(), "*");
                    methods.put(clazz, concurrency);
                    assemblyDescriptor.getContainerConcurrency().add(concurrency);
                }
                return concurrency;
            }

            protected Map<Object, ContainerConcurrency> getContainerConcurrency() {
                return methods;
            }
        }

        public static class LockHandler extends ConcurrentMethodHandler implements AnnotationHandler<Lock> {

            public LockHandler(final AssemblyDescriptor assemblyDescriptor,
                               final SessionBean bean) {
                this(assemblyDescriptor, bean, new HashMap<Object, ContainerConcurrency>());
            }

            public LockHandler(final AssemblyDescriptor assemblyDescriptor,
                               final SessionBean bean,
                               final Map<Object, ContainerConcurrency> methods) {
                super(assemblyDescriptor, bean, methods);
            }

            public void addClassLevelDeclaration(final Lock attribute, final Class type) {
                final ContainerConcurrency concurrency = getContainerConcurrency(type);
                concurrency.setLock(toLock(attribute));
            }

            public void addMethodLevelDeclaration(final Lock attribute, final Method method) {
                final ContainerConcurrency concurrency = getContainerConcurrency(method);
                concurrency.setLock(toLock(attribute));
            }

            private ConcurrentLockType toLock(final Lock annotation) {
                if (LockType.READ.equals(annotation.value())) {
                    return ConcurrentLockType.READ;
                } else if (LockType.WRITE.equals(annotation.value())) {
                    return ConcurrentLockType.WRITE;
                } else {
                    throw new IllegalArgumentException("Unknown lock annotation: " + annotation.value());
                }
            }

            public Class<Lock> getAnnotationClass() {
                return Lock.class;
            }

        }

        public static class AccessTimeoutHandler extends ConcurrentMethodHandler implements AnnotationHandler<AccessTimeout> {

            public AccessTimeoutHandler(final AssemblyDescriptor assemblyDescriptor,
                                        final SessionBean bean) {
                this(assemblyDescriptor, bean, new HashMap<Object, ContainerConcurrency>());
            }

            public AccessTimeoutHandler(final AssemblyDescriptor assemblyDescriptor,
                                        final SessionBean bean,
                                        final Map<Object, ContainerConcurrency> methods) {
                super(assemblyDescriptor, bean, methods);
            }

            public void addClassLevelDeclaration(final AccessTimeout attribute, final Class type) {
                final ContainerConcurrency concurrency = getContainerConcurrency(type);
                concurrency.setAccessTimeout(toTimeout(attribute));
            }

            public void addMethodLevelDeclaration(final AccessTimeout attribute, final Method method) {
                final ContainerConcurrency concurrency = getContainerConcurrency(method);
                concurrency.setAccessTimeout(toTimeout(attribute));
            }

            private Timeout toTimeout(final AccessTimeout annotation) {
                final Timeout timeout = new Timeout();
                timeout.setTimeout(annotation.value());
                timeout.setUnit(annotation.unit());
                return timeout;
            }

            public Class<AccessTimeout> getAnnotationClass() {
                return AccessTimeout.class;
            }

        }

        private <A extends Annotation> void checkAttributes(final AnnotationHandler<A> handler, final String ejbName, final EjbModule ejbModule, final AnnotationFinder annotationFinder, final String messageKey) {
            final Map<String, List<MethodAttribute>> existingDeclarations = handler.getExistingDeclarations();

            int xml = 0;
            for (final List<MethodAttribute> methodAttributes : existingDeclarations.values()) {
                xml += methodAttributes.size();
            }

            if (xml > 0) {
                ejbModule.getValidation().warn(ejbName, "xml." + messageKey, xml);
            }

            int ann = annotationFinder.findAnnotatedClasses(handler.getAnnotationClass()).size();
            ann += annotationFinder.findAnnotatedMethods(handler.getAnnotationClass()).size();

            if (ann > 0) {
                ejbModule.getValidation().warn(ejbName, "ann." + messageKey, ann);
            }


        }

        private <A extends Annotation> void processAttributes(final AnnotationHandler<A> handler, final Class<?> clazz, final AnnotationFinder annotationFinder) {
            final Map<String, List<MethodAttribute>> existingDeclarations = handler.getExistingDeclarations();

            // SET THE DEFAULT
            final Class<A> annotationClass = handler.getAnnotationClass();

            final List<Annotated<Class<?>>> types = sortClasses(annotationFinder.findMetaAnnotatedClasses(annotationClass));
            if (!hasMethodAttribute("*", null, existingDeclarations)) {
                for (final Annotated<Class<?>> type : types) {
                    if (!type.get().isAssignableFrom(clazz)) {
                        continue;
                    }
                    if (!hasMethodAttribute("*", type.get(), existingDeclarations)) {
                        final A attribute = type.getAnnotation(annotationClass);
                        if (attribute != null) {
                            handler.addClassLevelDeclaration(attribute, type.get());
                        }
                    }
                }
            }

            final List<Annotated<Method>> methods = annotationFinder.findMetaAnnotatedMethods(annotationClass);
            for (final Annotated<Method> method : methods) {
                final A attribute = method.getAnnotation(annotationClass);
                if (!existingDeclarations.containsKey(method.get().getName())) {
                    // no method with this name in descriptor
                    handler.addMethodLevelDeclaration(attribute, method.get());
                } else {
                    // method name already declared
                    final List<MethodAttribute> list = existingDeclarations.get(method.get().getName());
                    for (final MethodAttribute mtx : list) {
                        final MethodParams methodParams = mtx.getMethodParams();
                        if (methodParams == null) {
                            // params not specified, so this is more specific
                            handler.addMethodLevelDeclaration(attribute, method.get());
                        } else {
                            final List<String> params1 = methodParams.getMethodParam();
                            final String[] params2 = asStrings(method.get().getParameterTypes());
                            if (params1.size() != params2.length) {
                                // params not the same
                                handler.addMethodLevelDeclaration(attribute, method.get());
                            } else {
                                for (int i = 0; i < params1.size(); i++) {
                                    final String a = params1.get(i);
                                    final String b = params2[i];
                                    if (!a.equals(b)) {
                                        // params not the same
                                        handler.addMethodLevelDeclaration(attribute, method.get());
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        private boolean hasMethodAttribute(final String methodName, final Class clazz, final Map<String, List<MethodAttribute>> map) {
            return getMethodAttribute(methodName, clazz, map) != null;
        }

        private MethodAttribute getMethodAttribute(final String methodName, final Class clazz, final Map<String, List<MethodAttribute>> map) {
            final List<MethodAttribute> methodAttributes = map.get(methodName);
            if (methodAttributes == null) {
                return null;
            }

            for (final MethodAttribute methodAttribute : methodAttributes) {
                final String className = clazz != null ? clazz.getName() : "null";

                if (className.equals(methodAttribute.getClassName())) {
                    return methodAttribute;
                }
            }
            return null;
        }

        /**
         * Searches for an annotation starting at the specified class and working backwards.
         * Searching stops when the annotation is found.
         *
         * @param clazz
         * @param annotationClass
         * @return
         */
        private <A extends Annotation> A getInheritableAnnotation(final Class clazz, final Class<A> annotationClass) {
            if (clazz == null || clazz.equals(Object.class)) {
                return null;
            }

            final MetaAnnotatedClass meta = new MetaAnnotatedClass(clazz);
            final Annotation annotation = meta.getAnnotation(annotationClass);
            if (annotation != null) {
                return (A) annotation;
            }

            return getInheritableAnnotation(clazz.getSuperclass(), annotationClass);
        }

        /**
         * Creates a list of the specified class and all its parent
         * classes then creates a AnnotationFinder from that list which
         * can be used for easy annotation scanning.
         *
         * @param classes
         * @return
         */
        private AnnotationFinder createFinder(final Class<?>... classes) {
            final Set<Class<?>> parents = new HashSet<>();
            for (final Class<?> clazz : classes) {
                parents.addAll(Classes.ancestors(clazz));
            }
            return new AnnotationFinder(new ClassesArchive(parents)).enableMetaAnnotations(); // no need to have subclasses/impl here
        }

        /**
         * Converts an array of classes to an array of class name strings
         *
         * @param types
         * @return
         */
        private String[] asStrings(final Class[] types) {
            final List<String> names = new ArrayList<>();
            for (final Class clazz : types) {
                names.add(clazz.getName());
            }
            return names.toArray(new String[names.size()]);
        }

        /**
         * Grabs the first element of a list if there is one.
         *
         * @param list
         * @return
         */
        private <T> T getFirst(final List<T> list) {
            if (list.size() > 0) {
                return list.get(0);
            }
            return null;
        }

        /**
         * Remote interface validation
         *
         * @param interfce
         * @param validation
         * @param ejbName
         * @return
         */
        private boolean validateRemoteInterface(final Class interfce, final ValidationContext validation, final String ejbName) {
            return isValidInterface(interfce, validation, ejbName, "Remote");
        }

        /**
         * Local interface validation
         *
         * @param interfce
         * @param validation
         * @param ejbName
         * @return
         */
        private boolean validateLocalInterface(final Class interfce, final ValidationContext validation, final String ejbName) {
            return isValidInterface(interfce, validation, ejbName, "Local");
        }

        /**
         * Checks that the values specified via @Local and @Remote are *not*:
         *
         * - classes
         * - derived from jakarta.ejb.EJBObject
         * - derived from jakarta.ejb.EJBHome
         * - derived from jakarta.ejb.EJBLocalObject
         * - derived from jakarta.ejb.EJBLocalHome
         *
         * @param interfce
         * @param validation
         * @param ejbName
         * @param annotationName
         * @return
         */
        private boolean isValidInterface(final Class interfce, final ValidationContext validation, final String ejbName, final String annotationName) {
            if (!interfce.isInterface()) {
                validation.fail(ejbName, "ann.notAnInterface", annotationName, interfce.getName());
                return false;
            } else if (EJBHome.class.isAssignableFrom(interfce)) {
                validation.fail(ejbName, "ann.remoteOrLocal.ejbHome", annotationName, interfce.getName());
                return false;
            } else if (EJBObject.class.isAssignableFrom(interfce)) {
                validation.fail(ejbName, "ann.remoteOrLocal.ejbObject", annotationName, interfce.getName());
                return false;
            } else if (EJBLocalHome.class.isAssignableFrom(interfce)) {
                validation.fail(ejbName, "ann.remoteOrLocal.ejbLocalHome", annotationName, interfce.getName());
                return false;
            } else if (EJBLocalObject.class.isAssignableFrom(interfce)) {
                validation.fail(ejbName, "ann.remoteOrLocal.ejbLocalObject", annotationName, interfce.getName());
                return false;
            }
            return true;
        }
    }

    private static void failIfCdiProducer(final Member member, final String type) {
        if (FieldMember.class.isInstance(member) && FieldMember.class.cast(member).field.getAnnotation(Produces.class) != null) {
            throw new DefinitionException(FieldMember.class.cast(member).field + " is not a " + type);
        }
    }

    private static void addRestClassesToScannedClasses(final WebModule webModule, final Set<Class> classes, final ClassLoader classLoader) throws OpenEJBException {
        for (final String rawClassName : webModule.getRestClasses()) {
            final String className = realClassName(rawClassName);
            if (className != null) {
                final Class<?> clazz;
                try {
                    clazz = classLoader.loadClass(className);
                    classes.add(clazz);
                } catch (final ClassNotFoundException e) {
                    throw new OpenEJBException("Unable to load REST class: " + className, e);
                }
            }
        }
    }

    /**
     * Small utility interface used to allow polymorphing
     * of java.lang.reflect.Method and java.lang.reflect.Field
     * so that each can be treated as injection targets using
     * the same code.
     */
    public interface Member {
        Class<?> getDeclaringClass();

        String getName();

        Class<?> getType();
    }

    public static class FilledMember implements Member {
        private final String name;
        private final Class<?> type;
        private final Class<?> declaringClass;

        public FilledMember(final String name, final Class<?> type, final Class<?> declaringClass) {
            this.name = name;
            this.type = type;
            this.declaringClass = declaringClass;
        }

        public Class getDeclaringClass() {
            return declaringClass;
        }

        public String getName() {
            return name;
        }

        public Class getType() {
            return type;
        }

        @Override
        public String toString() {
            return "FilledMember{" +
                "name='" + name + '\'' +
                ", type=" + type.getName() +
                ", declaringClass=" + declaringClass.getName() +
                '}';
        }
    }

    /**
     * Implementation of Member for java.lang.reflect.Method
     * Used for injection targets that are annotated methods
     */
    public static class MethodMember implements Member {
        private final Method setter;

        public MethodMember(final Method method) {
            this.setter = method;
        }

        public Class<?> getType() {
            return setter.getParameterTypes()[0];
        }

        public Class<?> getDeclaringClass() {
            return setter.getDeclaringClass();
        }

        /**
         * The method name needs to be changed from "getFoo" to "foo"
         *
         * @return
         */
        public String getName() {
            final StringBuilder name = new StringBuilder(setter.getName());

            // remove 'set'
            name.delete(0, 3);

            // lowercase first char
            name.setCharAt(0, Character.toLowerCase(name.charAt(0)));

            return name.toString();
        }

        public String toString() {
            return setter.toString();
        }
    }

    /**
     * Implementation of Member for java.lang.reflect.Field
     * Used for injection targets that are annotated fields
     */
    public static class FieldMember implements Member {
        private final Field field;

        public FieldMember(final Field field) {
            this.field = field;
        }

        public Class<?> getType() {
            return field.getType();
        }

        public String toString() {
            return field.toString();
        }

        public Class<?> getDeclaringClass() {
            return field.getDeclaringClass();
        }

        public String getName() {
            return field.getName();
        }
    }

    public static List<Annotated<Class<?>>> sortClasses(final List<Annotated<Class<?>>> list) {
        list.sort(new Comparator<Annotated<Class<?>>>() {
            @Override
            public int compare(final Annotated<Class<?>> o1, final Annotated<Class<?>> o2) {
                return compareClasses(o1.get(), o2.get());
            }
        });
        return list;
    }

    public static List<Class<?>> sortClassesParentFirst(final List<Class<?>> list) {
        list.sort(new Comparator<Class<?>>() {
            @Override
            public int compare(final Class<?> o1, final Class<?> o2) {
                return compareClasses(o2, o1);
            }
        });
        return list;
    }

    public static List<Annotated<Method>> sortMethods(final List<Annotated<Method>> list) {
        list.sort(new Comparator<Annotated<Method>>() {
            @Override
            public int compare(final Annotated<Method> o1, final Annotated<Method> o2) {
                return compareClasses(o1.get().getDeclaringClass(), o2.get().getDeclaringClass());
            }
        });
        return list;
    }

    private static int compareClasses(final Class<?> a, final Class<?> b) {
        if (a == b) {
            return 0;
        }
        if (a.isAssignableFrom(b)) {
            return 1;
        }
        if (b.isAssignableFrom(a)) {
            return -1;
        }

        return 0;
    }

    public static Collection<String> findRestClasses(final WebModule webModule, final IAnnotationFinder finder) {
        final Collection<String> classes = new HashSet<>();

        // annotations on classes
        final List<Annotated<Class<?>>> annotatedClasses = finder.findMetaAnnotatedClasses(Path.class);
        for (final Annotated<Class<?>> aClazz : annotatedClasses) {
            final Class<?> clazz = aClazz.get();
            if (isInstantiable(clazz)) {
                if (!isEJB(clazz)) {
                    classes.add(clazz.getName());
                } else {
                    webModule.getEjbRestServices().add(clazz.getName());
                }
            } else if (clazz.isInterface()) {
                final Class api = clazz;
                final List impl = finder.findImplementations((Class<?>)api);
                if (impl != null && impl.size() == 1) { // single impl so that's the service
                    final Class implClass = (Class) impl.iterator().next();
                    final String name = implClass.getName();
                    if (!isEJB(implClass)) {
                        classes.add(name);
                    } else {
                        webModule.getEjbRestServices().add(name);
                    }
                }
            } else if (isEJB(clazz) && DynamicSubclass.isDynamic(clazz)) {
                classes.add(clazz.getName());
            }
        }

        if ("true".equalsIgnoreCase(SystemInstance.get().getProperty("openejb.jaxrs.scanning.methods", "false"))) {
            final List<Annotated<Method>> methods = finder.findMetaAnnotatedMethods(Path.class);
            for (final Annotated<Method> aMethod : methods) {
                final Method method = aMethod.get();
                final Class<?> clazz = method.getDeclaringClass();

                if (isInstantiable(clazz)) {
                    if (!isEJB(clazz)) {
                        classes.add(clazz.getName());
                    } else {
                        webModule.getEjbRestServices().add(clazz.getName());
                    }
                } else if (isEJB(clazz) && DynamicSubclass.isDynamic(clazz)) {
                    classes.add(clazz.getName());
                }
            }
        }

        return classes;
    }

    public static boolean isInstantiable(final Class<?> clazz) {
        final int modifiers = clazz.getModifiers();
        return !Modifier.isAbstract(modifiers)
                && !(clazz.getEnclosingClass() != null
                && !Modifier.isStatic(modifiers))
                && Modifier.isPublic(modifiers) && !clazz.isEnum();
    }

    private static boolean isEJB(final Class<?> clazz) {
        return clazz.isAnnotationPresent(Stateless.class)
            || clazz.isAnnotationPresent(Singleton.class)
            || clazz.isAnnotationPresent(ManagedBean.class)  // what a weird idea!
            || clazz.isAnnotationPresent(Stateful.class); // what another weird idea!
    }

    private static String realClassName(final String rawClassName) {
        if (rawClassName == null) {
            return null;
        }
        if (rawClassName.contains("/")) {
            return rawClassName.replace("/", ".");
        }
        return rawClassName;
    }

    private static Collection<Class<?>> metaToClass(final List<Annotated<Class<?>>> found) {
        final Collection<Class<?>> classes = new ArrayList<>(found.size());
        for (final Annotated<Class<?>> clazz : found) {
            classes.add(clazz.get());
        }
        return classes;
    }

    private static Map<String, Set<String>> addWebAnnotatedClassInfo(final Map<String, String> urlByClasses, final Map<String, Set<String>> classes, final List<Annotated<Class<?>>> found) {
        for (final Annotated<Class<?>> clazz : found) {
            final Class<?> loadedClass = clazz.get();
            final String name = loadedClass.getName();

            // url of the jar/folder containing the class
            String url = null;
            if (urlByClasses != null) {
                url = urlByClasses.get(name);
            }

            if (url == null) {
                try {
                    url = JarLocation.jarLocation(loadedClass).toURI().toURL().toExternalForm();
                } catch (final MalformedURLException e) {
                    url = classLocation(loadedClass).toExternalForm();
                }
            }

            Set<String> list = classes.computeIfAbsent(url, k -> new HashSet<>());

            // saving class url
            // first try the file approach (if the same class is in several classloaders it avoids weird errors)
            try {
                final File dir = new File(new URL(url).toURI());
                if (dir.isDirectory()) {
                    final File fileClazz = new File(dir, name.replace('.', '/') + ".class");
                    if (fileClazz.exists()) {
                        list.add(fileClazz.toURI().toURL().toExternalForm());
                    } else {
                        list.add(classLocation(loadedClass).toExternalForm());
                    }
                } else if (url.endsWith(".jar") && url.startsWith("file:")) {
                    list.add("jar:" + url + "!/" + name.replace('.', '/') + ".class");
                } else {
                    list.add(classLocation(loadedClass).toExternalForm());
                }
            } catch (final Exception e) {
                list.add(classLocation(loadedClass).toExternalForm());
            }
        }
        return classes;
    }

    public static URL classLocation(final Class clazz) {
        try {
            final String classFileName = clazz.getName().replace(".", "/") + ".class";

            final ClassLoader loader = clazz.getClassLoader();
            final URL url;
            if (loader != null) {
                if (TempClassLoader.class.isInstance(loader)) {
                    url = TempClassLoader.class.cast(loader).getInternalResource(classFileName);
                } else { // shouldn't occur
                    url = loader.getResource(classFileName);
                }
            } else {
                url = clazz.getResource(classFileName);
            }

            if (url == null) {
                throw new IllegalStateException("classloader.getResource(classFileName) returned a null URL");
            }

            return url;
        } catch (final RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public EnvEntriesPropertiesDeployer getEnvEntriesPropertiesDeployer() {
        return envEntriesPropertiesDeployer;
    }


    public static class ProvidedJAXRSApplication extends Application {
        // no-method
    }

    public interface FolderDDMapper {
        File getDDFolder(final File dir);
    }
}
