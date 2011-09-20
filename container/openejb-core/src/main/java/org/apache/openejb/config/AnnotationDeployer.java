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

import org.apache.openejb.BeanContext;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.api.LocalClient;
import org.apache.openejb.api.RemoteClient;
import org.apache.openejb.cdi.CdiBeanInfo;
import org.apache.openejb.core.webservices.JaxWsUtils;
import org.apache.openejb.jee.ActivationConfig;
import org.apache.openejb.jee.ActivationSpec;
import org.apache.openejb.jee.AdminObject;
import org.apache.openejb.jee.ApplicationClient;
import org.apache.openejb.jee.AroundInvoke;
import org.apache.openejb.jee.AroundTimeout;
import org.apache.openejb.jee.AssemblyDescriptor;
import org.apache.openejb.jee.AsyncMethod;
import org.apache.openejb.jee.AuthenticationMechanism;
import org.apache.openejb.jee.Beans;
import org.apache.openejb.jee.ConcurrencyManagementType;
import org.apache.openejb.jee.ConcurrentLockType;
import org.apache.openejb.jee.ConcurrentMethod;
import org.apache.openejb.jee.ConfigProperty;
import org.apache.openejb.jee.ContainerConcurrency;
import org.apache.openejb.jee.ContainerTransaction;
import org.apache.openejb.jee.DataSource;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.EjbLocalRef;
import org.apache.openejb.jee.EjbRef;
import org.apache.openejb.jee.EjbReference;
import org.apache.openejb.jee.Empty;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.EnvEntry;
import org.apache.openejb.jee.ExcludeList;
import org.apache.openejb.jee.FacesConfig;
import org.apache.openejb.jee.FacesManagedBean;
import org.apache.openejb.jee.Filter;
import org.apache.openejb.jee.Handler;
import org.apache.openejb.jee.HandlerChains;
import org.apache.openejb.jee.Icon;
import org.apache.openejb.jee.InboundResourceadapter;
import org.apache.openejb.jee.InitMethod;
import org.apache.openejb.jee.Injectable;
import org.apache.openejb.jee.InjectionTarget;
import org.apache.openejb.jee.Interceptor;
import org.apache.openejb.jee.InterceptorBinding;
import org.apache.openejb.jee.Invokable;
import org.apache.openejb.jee.IsolationLevel;
import org.apache.openejb.jee.JndiConsumer;
import org.apache.openejb.jee.JndiReference;
import org.apache.openejb.jee.License;
import org.apache.openejb.jee.Lifecycle;
import org.apache.openejb.jee.LifecycleCallback;
import org.apache.openejb.jee.Listener;
import org.apache.openejb.jee.MessageAdapter;
import org.apache.openejb.jee.MessageDrivenBean;
import org.apache.openejb.jee.MessageListener;
import org.apache.openejb.jee.MethodAttribute;
import org.apache.openejb.jee.MethodParams;
import org.apache.openejb.jee.MethodPermission;
import org.apache.openejb.jee.NamedMethod;
import org.apache.openejb.jee.OutboundResourceAdapter;
import org.apache.openejb.jee.ParamValue;
import org.apache.openejb.jee.PersistenceContextRef;
import org.apache.openejb.jee.PersistenceContextType;
import org.apache.openejb.jee.PersistenceUnitRef;
import org.apache.openejb.jee.PortComponent;
import org.apache.openejb.jee.Property;
import org.apache.openejb.jee.RemoteBean;
import org.apache.openejb.jee.RemoveMethod;
import org.apache.openejb.jee.ResAuth;
import org.apache.openejb.jee.ResSharingScope;
import org.apache.openejb.jee.ResourceAdapter;
import org.apache.openejb.jee.ResourceEnvRef;
import org.apache.openejb.jee.ResourceRef;
import org.apache.openejb.jee.SecurityIdentity;
import org.apache.openejb.jee.SecurityRoleRef;
import org.apache.openejb.jee.ServiceRef;
import org.apache.openejb.jee.Servlet;
import org.apache.openejb.jee.SessionBean;
import org.apache.openejb.jee.SessionType;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.jee.StatefulBean;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.jee.Tag;
import org.apache.openejb.jee.Text;
import org.apache.openejb.jee.Timeout;
import org.apache.openejb.jee.Timer;
import org.apache.openejb.jee.TimerConsumer;
import org.apache.openejb.jee.TimerSchedule;
import org.apache.openejb.jee.TldTaglib;
import org.apache.openejb.jee.TransAttribute;
import org.apache.openejb.jee.TransactionSupportType;
import org.apache.openejb.jee.TransactionType;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.jee.WebserviceDescription;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.AnnotationUtil;
import org.apache.openejb.util.Join;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.xbean.finder.Annotated;
import org.apache.xbean.finder.AnnotationFinder;
import org.apache.xbean.finder.IAnnotationFinder;
import org.apache.xbean.finder.MetaAnnotatedClass;
import org.apache.xbean.finder.archive.Archive;
import org.apache.xbean.finder.archive.ClassesArchive;

import javax.annotation.ManagedBean;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.annotation.Resources;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.annotation.security.RunAs;
import javax.annotation.sql.DataSourceDefinition;
import javax.annotation.sql.DataSourceDefinitions;
import javax.ejb.AccessTimeout;
import javax.ejb.AfterBegin;
import javax.ejb.AfterCompletion;
import javax.ejb.ApplicationException;
import javax.ejb.Asynchronous;
import javax.ejb.BeforeCompletion;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.DependsOn;
import javax.ejb.EJB;
import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.EJBs;
import javax.ejb.Init;
import javax.ejb.Local;
import javax.ejb.LocalBean;
import javax.ejb.LocalHome;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.MessageDriven;
import javax.ejb.PostActivate;
import javax.ejb.PrePassivate;
import javax.ejb.Remote;
import javax.ejb.RemoteHome;
import javax.ejb.Remove;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Stateful;
import javax.ejb.StatefulTimeout;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.enterprise.inject.Specializes;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.interceptor.ExcludeClassInterceptors;
import javax.interceptor.ExcludeDefaultInterceptors;
import javax.interceptor.Interceptors;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContexts;
import javax.persistence.PersistenceUnit;
import javax.persistence.PersistenceUnits;
import javax.resource.spi.Activation;
import javax.resource.spi.AdministeredObject;
import javax.resource.spi.ConnectionDefinition;
import javax.resource.spi.ConnectionDefinitions;
import javax.resource.spi.Connector;
import javax.resource.spi.SecurityPermission;
import javax.resource.spi.work.WorkContext;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceProvider;
import javax.xml.ws.WebServiceRef;
import javax.xml.ws.WebServiceRefs;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
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

import static java.lang.reflect.Modifier.isAbstract;
import static java.util.Arrays.asList;
import static org.apache.openejb.util.Join.join;

/**
 * @version $Rev$ $Date$
 */
public class AnnotationDeployer implements DynamicDeployer {
    public static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP, AnnotationDeployer.class.getPackage().getName());
    public static final Logger startupLogger = Logger.getInstance(LogCategory.OPENEJB_STARTUP_CONFIG, "org.apache.openejb.util.resources");
    private static final ThreadLocal<DeploymentModule> currentModule = new ThreadLocal<DeploymentModule>();

    public static final Set<String> knownResourceEnvTypes = new TreeSet<String>(asList(
            "javax.ejb.EJBContext",
            "javax.ejb.SessionContext",
            "javax.ejb.EntityContext",
            "javax.ejb.MessageDrivenContext",
            "javax.transaction.UserTransaction",
            "javax.jms.Queue",
            "javax.jms.Topic",
            "javax.xml.ws.WebServiceContext",
            "javax.ejb.TimerService",
            "javax.enterprise.inject.spi.BeanManager",
            "javax.validation.Validator",
            "javax.validation.ValidatorFactory"
    ));

    public static final Set<String> knownEnvironmentEntries = new TreeSet<String>(asList(
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
        processAnnotatedBeans = new ProcessAnnotatedBeans();
        builtInEnvironmentEntries = new BuiltInEnvironmentEntries();
        envEntriesPropertiesDeployer = new EnvEntriesPropertiesDeployer();
        mBeanDeployer = new MBeanDeployer();
        mergeWebappJndiContext = new MergeWebappJndiContext();
    }

    public AppModule deploy(AppModule appModule) throws OpenEJBException {
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
            removeModule();
        }
    }

    // TODO Remove this section.  It's called by some code in the assembler.
    // The scanning portion should be completed prior to this point
    public void deploy(CdiBeanInfo beanInfo) throws OpenEJBException{
        this.processAnnotatedBeans.deploy(beanInfo);
    }

    public WebModule deploy(WebModule webModule) throws OpenEJBException {
        setModule(webModule);
        try {
            webModule = discoverAnnotatedBeans.deploy(webModule);
            webModule = envEntriesPropertiesDeployer.deploy(webModule);
            webModule = processAnnotatedBeans.deploy(webModule);
            return webModule;
        } finally {
            removeModule();
        }
    }

    public static DeploymentModule getModule() {
        return currentModule.get();
    }

    private static void setModule(DeploymentModule module) {
        currentModule.set(module);
    }

    private static void removeModule() {
        currentModule.remove();
    }

    private static ValidationContext getValidationContext() {
        return getModule().getValidation();
    }

    private static void mergeApplicationExceptionAnnotation(AssemblyDescriptor assemblyDescriptor, Class<?> exceptionClass, ApplicationException annotation) {
        org.apache.openejb.jee.ApplicationException applicationException = assemblyDescriptor.getApplicationException(exceptionClass);
        if (applicationException.getRollback() == null) {
            applicationException.setRollback(annotation.rollback());
        }
        if (applicationException.getInherited() == null) {
            applicationException.setInherited(annotation.inherited());
        }
    }

    public static boolean isKnownEnvironmentEntryType(Class type) {
        return knownEnvironmentEntries.contains(type.getName()) || type.isEnum();
    }

    public static boolean isShareableJNDINamespace(String jndiName) {
        return jndiName.startsWith("java:global/") || jndiName.startsWith("java:app/") || jndiName.startsWith("java:module/");
    }
    public static class DiscoverAnnotatedBeans implements DynamicDeployer {
        public AppModule deploy(AppModule appModule) throws OpenEJBException {
            for (EjbModule ejbModule : appModule.getEjbModules()) {
                ejbModule.setAppModule(appModule);
                setModule(ejbModule);
                try {
                    deploy(ejbModule);
                } finally {
                    removeModule();
                }
            }
            for (ClientModule clientModule : appModule.getClientModules()) {
                clientModule.setAppModule(appModule);
                setModule(clientModule);
                try {
                    deploy(clientModule);
                } finally {
                    removeModule();
                }
            }
            for (ConnectorModule connectorModule : appModule.getConnectorModules()) {
                connectorModule.setAppModule(appModule);
                setModule(connectorModule);
                try {
                    deploy(connectorModule);
                } finally {
                    removeModule();
                }
            }
            for (WebModule webModule : appModule.getWebModules()) {
                webModule.setAppModule(appModule);
                setModule(webModule);
                try {
                    deploy(webModule);
                } finally {
                    removeModule();
                }
            }
            return appModule;
        }

        public ClientModule deploy(ClientModule clientModule) throws OpenEJBException {

            if (clientModule.getApplicationClient() == null){
                clientModule.setApplicationClient(new ApplicationClient());
            }

            // Lots of jars have main classes so this might not even be an app client.
            // We're not going to scrape it for @LocalClient or @RemoteClient annotations
            // unless they flag us specifically by adding a META-INF/application-client.xml
            //
            // ClientModules that already have a AnnotationFinder have been generated automatically
            // from an EjbModule, so we don't skip those ever.
            if (clientModule.getFinder() == null && clientModule.getAltDDs().containsKey("application-client.xml"))

            if (clientModule.getApplicationClient() != null && clientModule.getApplicationClient().isMetadataComplete()) return clientModule;


            IAnnotationFinder finder = clientModule.getFinder();

            if (finder == null) {
                try {
                    finder = FinderFactory.createFinder(clientModule);
                } catch (MalformedURLException e) {
                    startupLogger.warning("startup.scrapeFailedForClientModule.url", clientModule.getJarLocation());
                    return clientModule;
                } catch (Exception e) {
                    startupLogger.warning("startup.scrapeFailedForClientModule", e, clientModule.getJarLocation());
                    return clientModule;
                }
            }

            // This method is also called by the deploy(EjbModule) method to see if those
            // modules have any @LocalClient or @RemoteClient classes
            for (Annotated<Class<?>> clazz : finder.findMetaAnnotatedClasses(LocalClient.class)) {
                clientModule.getLocalClients().add(clazz.get().getName());
            }

            for (Annotated<Class<?>> clazz : finder.findMetaAnnotatedClasses(RemoteClient.class)) {
                clientModule.getRemoteClients().add(clazz.get().getName());
            }

            if (clientModule.getApplicationClient() == null){
                if (clientModule.getRemoteClients().size() > 0 || clientModule.getLocalClients().size() > 0) {
                    clientModule.setApplicationClient(new ApplicationClient());
                }
            }

            return clientModule;
        }

        public ConnectorModule deploy(ConnectorModule connectorModule) throws OpenEJBException {
        	
        	org.apache.openejb.jee.Connector connector = connectorModule.getConnector();
        	if (connector == null) {
        		connector = new org.apache.openejb.jee.Connector();
        	}
        	
        	// JCA 1.6 - 18.3.1 do not look at annotations if the provided connector
        	// deployment descriptor is "meta-data complete".
        	
        	float specVersion = 0;
        	try {
        		specVersion = Float.parseFloat(connector.getVersion());
        	} catch (Exception e) {
        	}
        	
			if (specVersion < 1.6 || Boolean.TRUE.equals(connector.isMetadataComplete())) {
				return connectorModule;
			}
        	
        	
        	IAnnotationFinder finder = connectorModule.getFinder();
        	if (finder == null) {
        		try {
        			finder = FinderFactory.createFinder(connectorModule);
					connectorModule.setFinder(finder);
				} catch (Exception e) {
					// TODO: some sort of error
					return connectorModule;
				}
        	}
        	
        	List<Class<?>> connectorClasses = finder.findAnnotatedClasses(Connector.class);
        	
        	// are we allowed to have more than one connector class? Not without a deployment descriptor
        	if (connector.getResourceAdapter() == null || connector.getResourceAdapter().getResourceAdapterClass() == null ||  connector.getResourceAdapter().getResourceAdapterClass().length() == 0) {
        		if (connectorClasses.size() == 0) {
        			// fail some validation here too
        		}
        		
        		if (connectorClasses.size() > 1) {
        			// too many connector classes, this is against the spec
        			// something like connectorModule.getValidation().fail(ejbName, "abstractAnnotatedAsBean", annotationClass.getSimpleName(), beanClass.get().getName());
        		}
        	}
        	
        	Class<?> connectorClass = null;
        	if (connectorClasses.size() == 1) {
        		connectorClass = connectorClasses.get(0);
        	}
        	
        	if (connectorClasses.size() > 1) {
        		for (Class<?> cls : connectorClasses) {
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
	    			connector.getResourceAdapter().setResourceAdapterClass(connectorClass.getName().toString());
	    		}
	    		
	    		Connector connectorAnnotation = connectorClass.getAnnotation(Connector.class);
	    		
	    		connector.setDisplayNames(getTexts(connector.getDisplayNames(), connectorAnnotation.displayName()));
	    		connector.setDescriptions(getTexts(connector.getDescriptions(), connectorAnnotation.description()));
	    		
	    		connector.setEisType(getString(connector.getEisType(), connectorAnnotation.eisType()));
	    		connector.setVendorName(getString(connector.getVendorName(), connectorAnnotation.vendorName()));
	    		connector.setResourceAdapterVersion(getString(connector.getResourceAdapterVersion(), connectorAnnotation.version()));
	    		
	    		if (connector.getIcons().isEmpty()) {
	    			int smallIcons = connectorAnnotation.smallIcon().length;
	    			int largeIcons = connectorAnnotation.largeIcon().length;
	    			
	    			for (int i = 0; i < smallIcons && i < largeIcons; i++) {
	    				Icon icon = new Icon();
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
	    			License license = new License();
					connector.setLicense(license);
					license.setLicenseRequired(connectorAnnotation.licenseRequired());
	    		}
	    		
	    		connector.getLicense().setDescriptions(getTexts(connector.getLicense().getDescriptions(), connectorAnnotation.licenseDescription()));
	    		
	    		
	    		SecurityPermission[] annotationSecurityPermissions = connectorAnnotation.securityPermissions();
				List<org.apache.openejb.jee.SecurityPermission> securityPermission = connector.getResourceAdapter().getSecurityPermission();
				if (securityPermission == null || securityPermission.size() == 0) {
					for (SecurityPermission sp : annotationSecurityPermissions) {
						org.apache.openejb.jee.SecurityPermission permission = new org.apache.openejb.jee.SecurityPermission();
						permission.setSecurityPermissionSpec(sp.permissionSpec());
						permission.setDescriptions(stringsToTexts(sp.description()));
						securityPermission.add(permission);
					}
				}
				
				Class<? extends WorkContext>[] annotationRequiredWorkContexts = connectorAnnotation.requiredWorkContexts();
				List<String> requiredWorkContext = connector.getRequiredWorkContext();
				if (requiredWorkContext.size() == 0) {
					for (Class<? extends WorkContext> cls : annotationRequiredWorkContexts) {
						requiredWorkContext.add(cls.getName());
					}
				}
				
				OutboundResourceAdapter outboundResourceAdapter = connector.getResourceAdapter().getOutboundResourceAdapter();
				if (outboundResourceAdapter == null) {
					outboundResourceAdapter = new OutboundResourceAdapter();
					connector.getResourceAdapter().setOutboundResourceAdapter(outboundResourceAdapter);
				}
				
				List<AuthenticationMechanism> authenticationMechanisms = outboundResourceAdapter.getAuthenticationMechanism();
				javax.resource.spi.AuthenticationMechanism[] authMechanisms = connectorAnnotation.authMechanisms();
				if (authenticationMechanisms.size() == 0) {
					for (javax.resource.spi.AuthenticationMechanism am : authMechanisms) {
						AuthenticationMechanism authMechanism = new AuthenticationMechanism();
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
        	} else {
        		// we couldn't process a connector class - probably a validation issue which we should warn about.
        	}

        	// process @ConnectionDescription(s)
        	List<Class<?>> classes = finder.findAnnotatedClasses(ConnectionDefinitions.class);
        	for (Class<?> cls : classes) {
				ConnectionDefinitions connectionDefinitionsAnnotation = cls.getAnnotation(ConnectionDefinitions.class);
				ConnectionDefinition[] definitions = connectionDefinitionsAnnotation.value();
				
				for (ConnectionDefinition definition : definitions) {
					processConnectionDescription(connector.getResourceAdapter(), definition, cls);
				}
			}
        	
        	classes = finder.findAnnotatedClasses(ConnectionDefinition.class);
        	for (Class<?> cls : classes) {
				ConnectionDefinition connectionDefinitionAnnotation = cls.getAnnotation(ConnectionDefinition.class);
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
        	for (Class<?> cls : classes) {
        		MessageListener messageListener = null;
        		Activation activationAnnotation = cls.getAnnotation(Activation.class);

        		List<MessageListener> messageListeners = messageAdapter.getMessageListener();
				for (MessageListener ml : messageListeners) {
					if (cls.getName().equals(ml.getActivationSpec().getActivationSpecClass())) {
						messageListener = ml;
						break;
					}
				}
				
				if (messageListener == null) {
					Class<?>[] listeners = activationAnnotation.messageListeners();
					for (Class<?> listener : listeners) {
						messageAdapter.addMessageListener(new MessageListener(listener.getName(), cls.getName()));	
					}
				}
			}
        	
        	classes = finder.findAnnotatedClasses(AdministeredObject.class);
        	List<AdminObject> adminObjects = connector.getResourceAdapter().getAdminObject();
        	for (Class<?> cls : classes) {
				AdministeredObject administeredObjectAnnotation = cls.getAnnotation(AdministeredObject.class);
				Class[] adminObjectInterfaces = administeredObjectAnnotation.adminObjectInterfaces();
				
				AdminObject adminObject = null;
				for (AdminObject admObj : adminObjects) {
					if (admObj.getAdminObjectClass().equals(cls.getName())) {
						adminObject = admObj;
					}
				}
				
				if (adminObject == null) {
					for (Class iface : adminObjectInterfaces) {
						AdminObject newAdminObject = new AdminObject();
						newAdminObject.setAdminObjectClass(cls.getName());
						newAdminObject.setAdminObjectInterface(iface.getName());
						adminObjects.add(newAdminObject);	
					}
				}
			}
        	
        	// need to make a list of classes to process for config properties
        	
        	// resource adapter
        	String raCls = connector.getResourceAdapter().getResourceAdapterClass();
        	process(connectorModule.getClassLoader(), raCls, connector.getResourceAdapter());
        	
        	// managedconnectionfactory
        	if (connector.getResourceAdapter() != null && connector.getResourceAdapter().getOutboundResourceAdapter() != null) {
	        	List<org.apache.openejb.jee.ConnectionDefinition> connectionDefinitions = connector.getResourceAdapter().getOutboundResourceAdapter().getConnectionDefinition();
	        	for (org.apache.openejb.jee.ConnectionDefinition connectionDefinition : connectionDefinitions) {
	        		process(connectorModule.getClassLoader(), connectionDefinition.getManagedConnectionFactoryClass(), connectionDefinition);
				}
        	}
        	
        	// administeredobject
        	if (connector.getResourceAdapter() != null) {
	        	List<AdminObject> raAdminObjects = connector.getResourceAdapter().getAdminObject();
	        	for (AdminObject raAdminObject : raAdminObjects) {
					process(connectorModule.getClassLoader(), raAdminObject.getAdminObjectClass(), raAdminObject);
				}
        	}
        	
        	// activationspec
        	if (connector.getResourceAdapter() != null && connector.getResourceAdapter().getInboundResourceAdapter() != null && connector.getResourceAdapter().getInboundResourceAdapter().getMessageAdapter() != null) {
	        	List<MessageListener> messageListeners = connector.getResourceAdapter().getInboundResourceAdapter().getMessageAdapter().getMessageListener();
	        	for (MessageListener messageListener : messageListeners) {
					ActivationSpec activationSpec = messageListener.getActivationSpec();
					process(connectorModule.getClassLoader(), activationSpec.getActivationSpecClass(), activationSpec);
				}
        	}

            return connectorModule;
        }

		void process(ClassLoader cl, String cls, Object object) {

			List<ConfigProperty> configProperties = null;
			try {
			// grab a list of ConfigProperty objects
				configProperties = (List<ConfigProperty>) object.getClass().getDeclaredMethod("getConfigProperty").invoke(object);
			} catch (Exception e) {
			}
			
			if (configProperties == null) {
				// can't get config properties
				return;
			}
			
			ClassLoader classLoader = cl;
			if (classLoader == null) {
				classLoader = Thread.currentThread().getContextClassLoader();
			}
			
			final List<String> allowedTypes = Arrays.asList(new String[] { Boolean.class.getName(), String.class.getName(), Integer.class.getName(), Double.class.getName(), Byte.class.getName(), Short.class.getName(), Long.class.getName(), Float.class.getName(), Character.class.getName()});
			
			try {
				Class<?> clazz = classLoader.loadClass(cls);
				Object o = clazz.newInstance();
				
				// add any introspected properties
				BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
				PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
				
				for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
					String name = propertyDescriptor.getName();
					Class<?> type = propertyDescriptor.getPropertyType();
					if (type.isPrimitive()) {
						type = getWrapper(type.getName());
					}
					
					if (! allowedTypes.contains(type.getName())) {
						continue;
					}
					
					if (! containsConfigProperty(configProperties, name)) {
						if (type != null) {
							ConfigProperty configProperty = new ConfigProperty();
							configProperties.add(configProperty);
							
							Object value = null;
							try {
								value = propertyDescriptor.getReadMethod().invoke(o);
							} catch (Exception e) {
							}
							
							javax.resource.spi.ConfigProperty annotation = propertyDescriptor.getWriteMethod().getAnnotation(javax.resource.spi.ConfigProperty.class);
                            if (annotation == null) {
                                try {
                                    // if there's no annotation on the setter, we'll try and scrape one off the field itself (assuming the same name)
                                    annotation = clazz.getDeclaredField(name).getAnnotation(javax.resource.spi.ConfigProperty.class);
                                } catch (Exception ignored) {
                                    // no-op : getDeclaredField() throws exceptions and does not return null
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
				}
				
				// add any annotated fields we haven't already picked up
				Field[] declaredFields = clazz.getDeclaredFields();
				for (Field field : declaredFields) {
					javax.resource.spi.ConfigProperty annotation = field.getAnnotation(javax.resource.spi.ConfigProperty.class);
					
					String name = field.getName();
					Object value = null;
					try {
						value = field.get(o);
					} catch (Exception e) {
					}

					if (! containsConfigProperty(configProperties, name)) {
						String type = getConfigPropertyType(annotation, field.getType());
						
						if (type != null) {
							ConfigProperty configProperty = new ConfigProperty();
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
			} catch (Exception  e) {
				e.printStackTrace();
			}
		}

		private String getConfigPropertyType(javax.resource.spi.ConfigProperty annotation, Class<?> type) {
			Class<?> t = (annotation == null) ? null : annotation.type();
			if (t == null || t.equals(Object.class)) {
				t = type;
			}
			
			if (t.isPrimitive()) {
				t = getWrapper(t.getName());
			}
			
			return t.getName();
		}

		private boolean containsConfigProperty(List<ConfigProperty> configProperties, String name) {
			for (ConfigProperty configProperty : configProperties) {
				if (configProperty.getConfigPropertyName().equals(name)) {
					return true;
				}
			}
			
			return false;
		}

		private Class<?> getWrapper(String primitiveType) {
			final Map<String,Class<?>> builtInMap = new HashMap<String,Class<?>>();{
			       builtInMap.put("int", Integer.class);
			       builtInMap.put("long", Long.class);
			       builtInMap.put("double", Double.class);
			       builtInMap.put("float", Float.class);
			       builtInMap.put("bool", Boolean.class);
			       builtInMap.put("char", Character.class);
			       builtInMap.put("byte", Byte.class);
			       builtInMap.put("void", Void.class);
			       builtInMap.put("short", Short.class);
			}
			
			return builtInMap.get(primitiveType);
		}

		private void processConnectionDescription(ResourceAdapter resourceAdapter, ConnectionDefinition connectionDefinitionAnnotation, Class<?> cls) {
			// try and find the managed connection factory
			
			OutboundResourceAdapter outboundResourceAdapter = resourceAdapter.getOutboundResourceAdapter();
			if (outboundResourceAdapter == null) {
				outboundResourceAdapter = new OutboundResourceAdapter();
				resourceAdapter.setOutboundResourceAdapter(outboundResourceAdapter);
			}
			
			List<org.apache.openejb.jee.ConnectionDefinition> connectionDefinition = outboundResourceAdapter.getConnectionDefinition();
			
			org.apache.openejb.jee.ConnectionDefinition definition = null;
			for (org.apache.openejb.jee.ConnectionDefinition cd : connectionDefinition) {
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

		private Text[] stringsToTexts(String[] strings) {
			if (strings == null) {
				return null;
			}
			
			Text[] result = new Text[strings.length];
			for (int i = 0; i < result.length; i++) {
				result[i] = new Text();
				result[i].setValue(strings[i]);
			}
			
			return result;
		}

		private String getString(String descriptorString, String annotationString) {
			if (descriptorString != null && descriptorString.length() > 0) {
				return descriptorString;
			}
			
			if (annotationString != null && annotationString.length() > 0) {
				return annotationString;
			}
			
			return null;
		}

		private Text[] getTexts(Text[] originalTexts, String[] newStrings) {
			if (newStrings != null && newStrings.length > 0 && (originalTexts == null || originalTexts.length == 0)) {
				Text[] texts = new Text[newStrings.length];
				for (int i = 0; i < newStrings.length; i++) {
					texts[i] = new Text(null, newStrings[i]);
				}

				return texts;
			} else {
				return originalTexts;
			}
		}

        public WebModule deploy(WebModule webModule) throws OpenEJBException {
            WebApp webApp = webModule.getWebApp();
            if (webApp != null && (webApp.isMetadataComplete())) return webModule;

            try {
                if (webModule.getFinder() == null) {
                    webModule.setFinder(FinderFactory.createFinder(webModule));
                }
            } catch (Exception e) {
                startupLogger.warning("Unable to scrape for @WebService or @WebServiceProvider annotations. AnnotationFinder failed.", e);
                return webModule;
            }

            if (webApp == null) {
                webApp = new WebApp();
                webModule.setWebApp(webApp);
            }

            List<String> existingServlets = new ArrayList<String>();
            for (Servlet servlet : webApp.getServlet()) {
                existingServlets.add(servlet.getServletClass());
            }

            IAnnotationFinder finder = webModule.getFinder();
            List<Class> classes = new ArrayList<Class>();
            classes.addAll(finder.findAnnotatedClasses(WebService.class));
            classes.addAll(finder.findAnnotatedClasses(WebServiceProvider.class));

            for (Class<?> webServiceClass : classes) {
                // If this class is also annotated @Stateless or @Singleton, we should skip it
                if (webServiceClass.isAnnotationPresent(Singleton.class) || webServiceClass.isAnnotationPresent(Stateless.class)) {
                    webModule.getEjbWebServices().add(webServiceClass.getName());
                    continue;
                }

                int modifiers = webServiceClass.getModifiers();
                if (!Modifier.isPublic(modifiers) || Modifier.isFinal(modifiers) || isAbstract(modifiers)) {
                    continue;
                }

                if (existingServlets.contains(webServiceClass.getName())) continue;

                // create webApp and webservices objects if they don't exist already

                // add new <servlet/> element
                Servlet servlet = new Servlet();
                servlet.setServletName(webServiceClass.getName());
                servlet.setServletClass(webServiceClass.getName());
                webApp.getServlet().add(servlet);
            }

            /*
             * REST
             */
            // get by annotations
            webModule.getRestClasses().addAll(findRestClasses(webModule, finder));
            // Applications
            List<Class<? extends Application>> applications = finder.findSubclasses(Application.class);
            for (Class<? extends Application> app : applications) {
                webModule.getRestApplications().add(app.getName());
            }

            return webModule;
        }

        public EjbModule deploy(EjbModule ejbModule) throws OpenEJBException {
            if (ejbModule.getEjbJar() != null && ejbModule.getEjbJar().isMetadataComplete()) return ejbModule;


            try {
                if (ejbModule.getFinder() == null) {
                    ejbModule.setFinder(FinderFactory.createFinder(ejbModule));
                }
            } catch (MalformedURLException e) {
                startupLogger.warning("startup.scrapeFailedForModule", ejbModule.getJarLocation());
                return ejbModule;
            } catch (Exception e) {
                startupLogger.warning("Unable to scrape for @Stateful, @Stateless, @Singleton or @MessageDriven annotations. AnnotationFinder failed.", e);
                return ejbModule;
            }

            IAnnotationFinder finder = ejbModule.getFinder();


            final List<String> managedClasses;
            {
                final Beans beans = ejbModule.getBeans();

                if (beans != null) {
                    managedClasses = beans.getManagedClasses();
                    final List<String> classNames = getBeanClasses(finder);
                    for (String className : classNames) {
                        try {
                            final ClassLoader loader = ejbModule.getClassLoader();
                            final Class<?> clazz = loader.loadClass(className);

                            // The following can NOT be beans in CDI

                            // 1. Non-static inner classes
                            if (clazz.getEnclosingClass() != null && !Modifier.isStatic(clazz.getModifiers())) continue;
//
//                            // 2. Abstract classes (unless they are an @Decorator)
//                            if (Modifier.isAbstract(clazz.getModifiers()) && !clazz.isAnnotationPresent(javax.decorator.Decorator.class)) continue;
//
                            // 3. Implementations of Extension
                            if (Extension.class.isAssignableFrom(clazz)) continue;

                            managedClasses.add(className);
                        } catch (ClassNotFoundException e) {
                            // todo log debug warning
                        } catch (java.lang.NoClassDefFoundError e) {

                        }
                    }

                    final String name = ejbModule.getModuleId() + "." + BeanContext.Comp.class.getSimpleName();
                    final org.apache.openejb.jee.ManagedBean managedBean = new org.apache.openejb.jee.ManagedBean(name, BeanContext.Comp.class);
                    managedBean.setTransactionType(TransactionType.BEAN);
                    ejbModule.getEjbJar().addEnterpriseBean(managedBean);
                } else {
                    managedClasses = new ArrayList<String>();
                }
            }

            final Set<Class<?>> specializingClasses = new HashSet<Class<?>>();

            /* 19.2:  ejb-name: Default is the unqualified name of the bean class */

            EjbJar ejbJar = ejbModule.getEjbJar();
            for (Annotated<Class<?>> beanClass : finder.findMetaAnnotatedClasses(Singleton.class)) {

                if (beanClass.isAnnotationPresent(Specializes.class)) {
                    managedClasses.remove(beanClass.get().getName());
                    specializingClasses.add(beanClass.get());
                    continue;
                }

                Singleton singleton = beanClass.getAnnotation(Singleton.class);
                String ejbName = getEjbName(singleton, beanClass.get());

                if (!isValidEjbAnnotationUsage(Singleton.class, beanClass, ejbName, ejbModule)) continue;

                EnterpriseBean enterpriseBean = ejbJar.getEnterpriseBean(ejbName);
                if (enterpriseBean == null) {
                    enterpriseBean = new SingletonBean(ejbName, beanClass.get());
                    ejbJar.addEnterpriseBean(enterpriseBean);
                }
                if (enterpriseBean.getEjbClass() == null) {
                    enterpriseBean.setEjbClass(beanClass.get());
                }
                if (enterpriseBean instanceof SessionBean) {
                    SessionBean sessionBean = (SessionBean) enterpriseBean;
                    sessionBean.setSessionType(SessionType.SINGLETON);

                    if (singleton.mappedName() != null) {
                        sessionBean.setMappedName(singleton.mappedName());
                    }
                }
                LegacyProcessor.process(beanClass.get(), enterpriseBean);
            }

            for (Annotated<Class<?>> beanClass : finder.findMetaAnnotatedClasses(Stateless.class)) {

                if (beanClass.isAnnotationPresent(Specializes.class)) {
                    managedClasses.remove(beanClass.get().getName());
                    specializingClasses.add(beanClass.get());
                    continue;
                }

                Stateless stateless = beanClass.getAnnotation(Stateless.class);
                String ejbName = getEjbName(stateless, beanClass.get());

                if (!isValidEjbAnnotationUsage(Stateless.class, beanClass, ejbName, ejbModule)) continue;

                EnterpriseBean enterpriseBean = ejbJar.getEnterpriseBean(ejbName);
                if (enterpriseBean == null) {
                    enterpriseBean = new StatelessBean(ejbName, beanClass.get());
                    ejbJar.addEnterpriseBean(enterpriseBean);
                }
                if (enterpriseBean.getEjbClass() == null) {
                    enterpriseBean.setEjbClass(beanClass.get());
                }
                if (enterpriseBean instanceof SessionBean) {
                    SessionBean sessionBean = (SessionBean) enterpriseBean;
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

            for (Annotated<Class<?>> beanClass : finder.findMetaAnnotatedClasses(Stateful.class)) {

                if (beanClass.isAnnotationPresent(Specializes.class)) {
                    managedClasses.remove(beanClass.get().getName());
                    specializingClasses.add(beanClass.get());
                    continue;
                }

                Stateful stateful = beanClass.getAnnotation(Stateful.class);
                String ejbName = getEjbName(stateful, beanClass.get());

                if (!isValidEjbAnnotationUsage(Stateful.class, beanClass, ejbName, ejbModule)) continue;

                EnterpriseBean enterpriseBean = ejbJar.getEnterpriseBean(ejbName);
                if (enterpriseBean == null) {
                    enterpriseBean = new StatefulBean(ejbName, beanClass.get());
                    ejbJar.addEnterpriseBean(enterpriseBean);
                }
                if (enterpriseBean.getEjbClass() == null) {
                    enterpriseBean.setEjbClass(beanClass.get());
                }
                if (enterpriseBean instanceof SessionBean) {
                    SessionBean sessionBean = (SessionBean) enterpriseBean;
                    // TODO: We might be stepping on an xml override here
                    sessionBean.setSessionType(SessionType.STATEFUL);
                    if (stateful.mappedName() != null) {
                        sessionBean.setMappedName(stateful.mappedName());
                    }
                }
                LegacyProcessor.process(beanClass.get(), enterpriseBean);
            }

            for (Annotated<Class<?>> beanClass : finder.findMetaAnnotatedClasses(ManagedBean.class)) {

                if (beanClass.isAnnotationPresent(Specializes.class)) {
                    managedClasses.remove(beanClass.get().getName());
                    specializingClasses.add(beanClass.get());
                    continue;
                }

                ManagedBean managed = beanClass.getAnnotation(ManagedBean.class);
                String ejbName = getEjbName(managed, beanClass.get());

                // TODO: this is actually against the spec, but the requirement is rather silly
                // (allowing @Stateful and @ManagedBean on the same class)
                // If the TCK doesn't complain we should discourage it
                if (!isValidEjbAnnotationUsage(ManagedBean.class, beanClass, ejbName, ejbModule)) continue;

                EnterpriseBean enterpriseBean = ejbJar.getEnterpriseBean(ejbName);
                if (enterpriseBean == null) {
                    enterpriseBean = new org.apache.openejb.jee.ManagedBean(ejbName, beanClass.get());
                    ejbJar.addEnterpriseBean(enterpriseBean);
                }
                if (enterpriseBean.getEjbClass() == null) {
                    enterpriseBean.setEjbClass(beanClass.get());
                }
                if (enterpriseBean instanceof SessionBean) {
                    SessionBean sessionBean = (SessionBean) enterpriseBean;
                    sessionBean.setSessionType(SessionType.MANAGED);

                    final TransactionType transactionType = sessionBean.getTransactionType();
                    if (transactionType == null) sessionBean.setTransactionType(TransactionType.BEAN);
                }
            }

            for (Annotated<Class<?>> beanClass : finder.findMetaAnnotatedClasses(MessageDriven.class)) {

                if (beanClass.isAnnotationPresent(Specializes.class)) {
                    managedClasses.remove(beanClass.get().getName());
                    specializingClasses.add(beanClass.get());
                    continue;
                }

                MessageDriven mdb = beanClass.getAnnotation(MessageDriven.class);
                String ejbName = getEjbName(mdb, beanClass.get());

                if (!isValidEjbAnnotationUsage(MessageDriven.class, beanClass, ejbName, ejbModule)) continue;

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


            for (Class<?> specializingClass : sortClassesParentFirst(new ArrayList<Class<?>>(specializingClasses))) {

                final Class<?> parent = specializingClass.getSuperclass();

                if (parent == null || parent.equals(Object.class)) {
                    ejbModule.getValidation().fail(specializingClass.getSimpleName(), "specializes.extendsNothing", specializingClass.getName());
                }

                boolean found = false;

                for (EnterpriseBean enterpriseBean : ejbJar.getEnterpriseBeans()) {

                    final String ejbClass = enterpriseBean.getEjbClass();

                    if (ejbClass != null && ejbClass.equals(parent.getName())) {
                        managedClasses.remove(ejbClass);
                        enterpriseBean.setEjbClass(specializingClass.getName());
                        found = true;
                    }
                }

                if (!found) {
                    ejbModule.getValidation().fail(specializingClass.getSimpleName(), "specializes.extendsSimpleBean", specializingClass.getName());
                }
            }

            AssemblyDescriptor assemblyDescriptor = ejbModule.getEjbJar().getAssemblyDescriptor();
            if (assemblyDescriptor == null) {
                assemblyDescriptor = new AssemblyDescriptor();
                ejbModule.getEjbJar().setAssemblyDescriptor(assemblyDescriptor);
            }

            startupLogger.debug("Searching for annotated application exceptions (see OPENEJB-980)");
            List<Class<?>> appExceptions = finder.findAnnotatedClasses(ApplicationException.class);
            for (Class<?> exceptionClass : appExceptions) {
                startupLogger.debug("...handling " + exceptionClass);
                ApplicationException annotation = exceptionClass.getAnnotation(ApplicationException.class);
                if (assemblyDescriptor.getApplicationException(exceptionClass) == null) {
                    startupLogger.debug("...adding " + exceptionClass + " with rollback=" + annotation.rollback());
                    assemblyDescriptor.addApplicationException(exceptionClass, annotation.rollback(), annotation.inherited());
                } else {
                    mergeApplicationExceptionAnnotation(assemblyDescriptor, exceptionClass, annotation);
                }
            }

            return ejbModule;
        }

        private List<String> getBeanClasses(IAnnotationFinder finder) {

            //  What we're hoping in this method is to get lucky and find
            //  that our 'finder' instances is an AnnotationFinder that is
            //  holding an AggregatedArchive so we can get the classes that
            //  that pertain to each URL for CDI purposes.
            //
            //  If not we call finder.getAnnotatedClassNames() which may return
            //  more classes than actually apply to CDI.  This can "pollute"
            //  the CDI class space and break injection points

            if (!(finder instanceof AnnotationFinder)) return finder.getAnnotatedClassNames();

            final AnnotationFinder annotationFinder = (AnnotationFinder) finder;

            final Archive archive = annotationFinder.getArchive();

            if (!(archive instanceof AggregatedArchive)) return finder.getAnnotatedClassNames();

            final List<String> classes = new ArrayList<String>();

            final AggregatedArchive aggregatedArchive = (AggregatedArchive) archive;
            final Map<URL, List<String>> map = aggregatedArchive.getClassesMap();

            for (Map.Entry<URL, List<String>> entry : map.entrySet()) {

                if (hasBeansXml(entry.getKey())) classes.addAll(entry.getValue());
            }

            return classes;
        }

        private boolean hasBeansXml(URL url) {
            if (url.getPath().endsWith("WEB-INF/classes/")) return true;
            try {
                final URLClassLoader loader = new URLClassLoader(new URL[]{url});
                String[] paths = {
                        "META-INF/beans.xml",
                        "WEB-INF/beans.xml",
                        "/WEB-INF/beans.xml",
                        "/META-INF/beans.xml",
                };

                for (String path : paths) {
                    if (loader.findResource(path) != null) return true;
                }
            } catch (Exception e) {
            }
            return false;
        }

        private String getEjbName(MessageDriven mdb, Class<?> beanClass) {
            String ejbName = mdb.name().length() == 0 ? beanClass.getSimpleName() : mdb.name();
            return ejbName;
        }

        private String getEjbName(Stateful stateful, Class<?> beanClass) {
            String ejbName = stateful.name().length() == 0 ? beanClass.getSimpleName() : stateful.name();
            return ejbName;
        }

        private String getEjbName(Stateless stateless, Class<?> beanClass) {
            String ejbName = stateless.name().length() == 0 ? beanClass.getSimpleName() : stateless.name();
            return ejbName;
        }

        private String getEjbName(Singleton singleton, Class<?> beanClass) {
            String ejbName = singleton.name().length() == 0 ? beanClass.getSimpleName() : singleton.name();
            return ejbName;
        }

        private String getEjbName(ManagedBean managed, Class<?> beanClass) {
            String ejbName = managed.value().length() == 0 ? beanClass.getSimpleName() : managed.value();
            return ejbName;
        }

        private boolean isValidEjbAnnotationUsage(Class annotationClass, Annotated<Class<?>> beanClass, String ejbName, EjbModule ejbModule) {
            List<Class<? extends Annotation>> annotations = new ArrayList(asList(Singleton.class, Stateless.class, Stateful.class, MessageDriven.class));
            annotations.remove(annotationClass);

            boolean b = true;
            for (Class<? extends Annotation> secondAnnotation : annotations) {
                Annotation annotation = beanClass.getAnnotation(secondAnnotation);

                if (annotation == null) continue;

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

            if (beanClass.get().getAnnotation(PersistenceContext.class) == null // not a dynamic proxy implemented bean
                    && beanClass.get().isInterface()) {
                ejbModule.getValidation().fail(ejbName, "interfaceAnnotatedAsBean", annotationClass.getSimpleName(), beanClass.get().getName());
                return false;
            }

            if (!beanClass.get().isInterface() && isAbstract(beanClass.get().getModifiers())) {
                ejbModule.getValidation().fail(ejbName, "abstractAnnotatedAsBean", annotationClass.getSimpleName(), beanClass.get().getName());
                return false;
            }

            return b;
        }

    }

    public static class ProcessAnnotatedBeans implements DynamicDeployer {

        public static final String STRICT_INTERFACE_DECLARATION = "openejb.strict.interface.declaration";

        public void deploy(CdiBeanInfo beanInfo) throws OpenEJBException{

            AnnotationFinder annotationFinder = createFinder(beanInfo.getBeanClass());
            /*
             * @EJB
             * @Resource
             * @WebServiceRef
             * @PersistenceUnit
             * @PersistenceContext
             */
            buildAnnotatedRefs(beanInfo, annotationFinder, beanInfo.getClassLoader());

            processWebServiceClientHandlers(beanInfo, beanInfo.getClassLoader());

        }

        public AppModule deploy(AppModule appModule) throws OpenEJBException {
            for (EjbModule ejbModule : appModule.getEjbModules()) {
                setModule(ejbModule);
                try {
                    deploy(ejbModule);
                } finally {
                    removeModule();
                }
            }
            for (ClientModule clientModule : appModule.getClientModules()) {
                setModule(clientModule);
                try {
                    deploy(clientModule);
                } finally {
                    removeModule();
                }
            }
            for (ConnectorModule connectorModule : appModule.getConnectorModules()) {
                setModule(connectorModule);
                try {
                    deploy(connectorModule);
                } finally {
                    removeModule();
                }
            }
            for (WebModule webModule : appModule.getWebModules()) {
                setModule(webModule);
                try {
                    deploy(webModule);
                } finally {
                    removeModule();
                }
            }
            return appModule;
        }

        public ClientModule deploy(ClientModule clientModule) throws OpenEJBException {
            if (clientModule.getApplicationClient() != null && clientModule.getApplicationClient().isMetadataComplete())
                return clientModule;

            ClassLoader classLoader = clientModule.getClassLoader();

            ApplicationClient client = clientModule.getApplicationClient();

            if (client == null) client = new ApplicationClient();

            Set<Class> remoteClients = new HashSet<Class>();

            if (clientModule.getMainClass() != null){
                String className = clientModule.getMainClass();

                // OPENEJB-1063: a Main-Class should use "." instead of "/"
                // it wasn't check before jdk 1.5 so we can get old module with
                // bad format http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4986512
                // replace all "/" by "."
                if (className.indexOf("/") != -1) { // className can't be null here
                    className = className.replaceAll("/", ".");
                    clientModule.setMainClass(className);
		}

                Class clazz;
                try {
                    clazz = classLoader.loadClass(className);
                    remoteClients.add(clazz);

                    AnnotationFinder annotationFinder = createFinder(clazz);

                    buildAnnotatedRefs(client, annotationFinder, classLoader);
                } catch (ClassNotFoundException e) {
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

            for (String className : clientModule.getRemoteClients()) {
                Class clazz;
                try {
                    clazz = classLoader.loadClass(className);
                    remoteClients.add(clazz);
                } catch (ClassNotFoundException e) {
                    throw new OpenEJBException("Unable to load RemoteClient class: " + className, e);
                }

                AnnotationFinder annotationFinder = createFinder(clazz);

                buildAnnotatedRefs(client, annotationFinder, classLoader);
            }

            for (String className : clientModule.getLocalClients()) {
                Class clazz;
                try {
                    clazz = classLoader.loadClass(className);
                } catch (ClassNotFoundException e) {
                    throw new OpenEJBException("Unable to load LocalClient class: " + className, e);
                }

                AnnotationFinder annotationFinder = createFinder(clazz);

                buildAnnotatedRefs(client, annotationFinder, classLoader);
            }

            validateRemoteClientRefs(classLoader, client, remoteClients);

            processWebServiceClientHandlers(client, classLoader);

            return clientModule;
        }

        private void validateRemoteClientRefs(ClassLoader classLoader, ApplicationClient client, Set<Class> remoteClients) {
            for (EjbLocalRef ref : client.getEjbLocalRef()) {
                for (InjectionTarget target : ref.getInjectionTarget()) {
                    try {
                        Class<?> targetClass = classLoader.loadClass(target.getInjectionTargetClass());
                        for (Class remoteClient : remoteClients) {
                            if (targetClass.isAssignableFrom(remoteClient)) {
                                fail(remoteClient.getName(), "remoteClient.ejbLocalRef", target.getInjectionTargetClass(), target.getInjectionTargetName());
                            }
                        }
                    } catch (ClassNotFoundException ignore) {
                    }
                }
            }

            for (PersistenceContextRef ref : client.getPersistenceContextRef()) {
                for (InjectionTarget target : ref.getInjectionTarget()) {
                    try {
                        Class<?> targetClass = classLoader.loadClass(target.getInjectionTargetClass());
                        for (Class remoteClient : remoteClients) {
                            if (targetClass.isAssignableFrom(remoteClient)) {
                                fail(remoteClient.getName(), "remoteClient.persistenceContextRef", target.getInjectionTargetClass(), target.getInjectionTargetName());
                            }
                        }
                    } catch (ClassNotFoundException ignore) {
                    }
                }
            }

            /* TODO: still useful?
            List<String> unusableTypes = new ArrayList<String>(knownResourceEnvTypes);
            unusableTypes.remove("javax.jms.Topic");
            unusableTypes.remove("javax.jms.Queue");

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

        public ConnectorModule deploy(ConnectorModule connectorModule) throws OpenEJBException {
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
        public WebModule deploy(WebModule webModule) throws OpenEJBException {
            WebApp webApp = webModule.getWebApp();
            if (webApp != null && webApp.isMetadataComplete()) return webModule;

            /*
             * Classes added to this set will be scanned for annotations
             */
            Set<Class> classes = new HashSet<Class>();


            ClassLoader classLoader = webModule.getClassLoader();

            final String webXmlApplication = webApp.contextParamsAsMap().get("javax.ws.rs.Application");
            if (webXmlApplication != null) {
                webModule.getRestApplications().clear();
                webModule.getRestApplications().add(webXmlApplication);
            }

            Collection<String> restApp = webModule.getRestApplications();
            if (restApp.isEmpty()) {
                for (String className : webModule.getRestClasses()) {
                    if (className != null) {
                        Class<?> clazz;
                        try {
                            clazz = classLoader.loadClass(className);
                            classes.add(clazz);
                        } catch (ClassNotFoundException e) {
                            throw new OpenEJBException("Unable to load REST class: " + className, e);
                        }
                    }
                }
            } else {
                for (String application : restApp) {
                    if (application != null) {
                        Class<?> clazz;
                        try {
                            clazz = classLoader.loadClass(application);
                            classes.add(clazz);
                        } catch (ClassNotFoundException e) {
                            throw new OpenEJBException("Unable to load Application class: " + application, e);
                        }
                        try {
                            Application app = Application.class.cast(clazz.newInstance());
                            classes.addAll(app.getClasses());
                        } catch (InstantiationException e) {
                            throw new OpenEJBException("Unable to instantiate Application class: " + application, e);
                        } catch (IllegalAccessException e) {
                            throw new OpenEJBException("Unable to access Application class: " + application, e);
                        }
                    }
                }
            }


            /*
             * Servlet classes are scanned
             */
            for (Servlet servlet : webApp.getServlet()) {
                String servletClass = servlet.getServletClass();
                if (servletClass != null) {
                    if (!"org.apache.openejb.server.rest.OpenEJBRestServlet".equals(servletClass)) {
                        try {
                            Class clazz = classLoader.loadClass(servletClass);
                            classes.add(clazz);
                        } catch (ClassNotFoundException e) {
                            throw new OpenEJBException("Unable to load servlet class: " + servletClass, e);
                        }
                    }

                    // if the servlet is a rest init servlet don't deploy rest classes automatically
                    for (ParamValue param : servlet.getInitParam()) {
                        if (param.getParamName().equals(Application.class.getName())) {
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
            for (Filter filter : webApp.getFilter()) {
                String filterClass = filter.getFilterClass();
                if (filterClass != null) {
                    try {
                        Class clazz = classLoader.loadClass(filterClass);
                        classes.add(clazz);
                    } catch (ClassNotFoundException e) {
                        throw new OpenEJBException("Unable to load servlet filter class: " + filterClass, e);
                    }
                }
            }

            /*
             * Listener classes are scanned
             */
            for (Listener listener : webApp.getListener()) {
                String listenerClass = listener.getListenerClass();
                if (listenerClass != null) {
                    try {
                        Class clazz = classLoader.loadClass(listenerClass);
                        classes.add(clazz);
                    } catch (ClassNotFoundException e) {
                        throw new OpenEJBException("Unable to load servlet listener class: " + listenerClass, e);
                    }
                }
            }

            for (TldTaglib taglib : webModule.getTaglibs()) {
                /*
                 * TagLib Listener classes are scanned
                 */
                for (Listener listener : taglib.getListener()) {
                    String listenerClass = listener.getListenerClass();
                    if (listenerClass != null) {
                        try {
                            Class clazz = classLoader.loadClass(listenerClass);
                            classes.add(clazz);
                        } catch (ClassNotFoundException e) {
                            logger.error("Unable to load tag library servlet listener class: " + listenerClass);
                        }
                    }
                }

                /*
                 * TagLib Tag classes are scanned
                 */
                for (Tag tag : taglib.getTag()) {
                    String tagClass = tag.getTagClass();
                    if (tagClass != null) {
                        try {
                            Class clazz = classLoader.loadClass(tagClass);
                            classes.add(clazz);
                        } catch (ClassNotFoundException e) {
                            logger.error("Unable to load tag library tag class: " + tagClass);
                        }
                    }
                }
            }

            /*
             * WebService HandlerChain classes are scanned
             */
            if (webModule.getWebservices() != null) {
                for (WebserviceDescription webservice : webModule.getWebservices().getWebserviceDescription()) {
                    for (PortComponent port : webservice.getPortComponent()) {
                        // skip ejb port defs
                        if (port.getServiceImplBean().getEjbLink() != null) continue;

                        if (port.getHandlerChains() == null) continue;
                        for (org.apache.openejb.jee.HandlerChain handlerChain : port.getHandlerChains().getHandlerChain()) {
                            for (Handler handler : handlerChain.getHandler()) {
                                String handlerClass = handler.getHandlerClass();
                                if (handlerClass != null) {
                                    try {
                                        Class clazz = classLoader.loadClass(handlerClass);
                                        classes.add(clazz);
                                    } catch (ClassNotFoundException e) {
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
            for (FacesConfig facesConfig : webModule.getFacesConfigs()) {
                for (FacesManagedBean bean : facesConfig.getManagedBean()) {
                    String managedBeanClass = bean.getManagedBeanClass().trim();
                    if (managedBeanClass != null) {
                        try {
                            Class clazz = classLoader.loadClass(managedBeanClass);
                            classes.add(clazz);
                        } catch (ClassNotFoundException e) {
                            logger.error("Unable to load JSF managed bean class: " + managedBeanClass);
                        }
                    }
                }
            }

            IAnnotationFinder finder = webModule.getFinder();

            if (finder != null) {
                // Add all the classes of the previous finder
                // TODO this part can be optimized
                final List<String> classNames = finder.getAnnotatedClassNames();
                for (String className : classNames) {
                    try {
                        Class clazz = classLoader.loadClass(className);
                        classes.add(clazz);
                    } catch (Throwable e) {
                        logger.debug(String.format("%s: Unable to load class for scanning: %s", e.getClass().getName(), className));
                    }
                }
            }

            AnnotationFinder annotationFinder = createFinder(classes.toArray(new Class<?>[classes.size()]));

            /*
             * @EJB
             * @Resource
             * @WebServiceRef
             * @PersistenceUnit
             * @PersistenceContext
             */
            buildAnnotatedRefs(webApp, annotationFinder, classLoader);

            processWebServiceClientHandlers(webApp, classLoader);

            return webModule;
        }

        public EjbModule deploy(EjbModule ejbModule) throws OpenEJBException {
            if (ejbModule.getEjbJar() != null && ejbModule.getEjbJar().isMetadataComplete()) return ejbModule;

//            Map<String, EjbDeployment> deployments = ejbModule.getOpenejbJar().getDeploymentsByEjbName();
            ClassLoader classLoader = ejbModule.getClassLoader();
            EnterpriseBean[] enterpriseBeans = ejbModule.getEjbJar().getEnterpriseBeans();
            for (EnterpriseBean bean : enterpriseBeans) {
                final String ejbName = bean.getEjbName();
                final String ejbClassName = bean.getEjbClass();

                if (ejbClassName == null) {
                    List<String> others = new ArrayList<String>();
                    for (EnterpriseBean otherBean : enterpriseBeans) {
                        others.add(otherBean.getEjbName());
                    }
                    fail(ejbName, "xml.noEjbClass", ejbName, join(", ", others));
                }

                final Class<?> clazz;
                try {
                    clazz = classLoader.loadClass(ejbClassName);
                } catch (ClassNotFoundException e) {
                    // Handled in CheckClasses.java along with other missing classes
                    continue;
                }
                final AnnotationFinder finder = new AnnotationFinder(new ClassesArchive(clazz));

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
                    TransactionManagement tx = getInheritableAnnotation(clazz, TransactionManagement.class);
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
                for (InterceptorBinding binding : assemblyDescriptor.getInterceptorBinding()) {
                    EjbJar ejbJar = ejbModule.getEjbJar();

                    List<String> list = new ArrayList<String>(binding.getInterceptorClass());

                    if (binding.getInterceptorOrder() != null){
                        list.clear();
                        list.addAll(binding.getInterceptorOrder().getInterceptorClass());
                    }

                    for (String interceptor : list) {
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
                for (Annotated<Class<?>> interceptorsAnnotatedClass : annotatedClasses) {
                    Interceptors interceptors = interceptorsAnnotatedClass.getAnnotation(Interceptors.class);
                    EjbJar ejbJar = ejbModule.getEjbJar();
                    for (Class interceptor : interceptors.value()) {
                        if (ejbJar.getInterceptor(interceptor.getName()) == null) {
                            ejbJar.addInterceptor(new Interceptor(interceptor.getName()));
                        }
                    }

                    InterceptorBinding binding = new InterceptorBinding(bean);
                    assemblyDescriptor.getInterceptorBinding().add(0, binding);

                    for (Class interceptor : interceptors.value()) {
                        binding.getInterceptorClass().add(interceptor.getName());
                    }
                }

                final List<Annotated<Method>> annotatedMethods = sortMethods(annotationFinder.findMetaAnnotatedMethods(Interceptors.class));
                for (Annotated<Method> method : annotatedMethods) {
                    Interceptors interceptors = method.getAnnotation(Interceptors.class);
                    if (interceptors != null) {
                        EjbJar ejbJar = ejbModule.getEjbJar();
                        for (Class interceptor : interceptors.value()) {
                            if (ejbJar.getInterceptor(interceptor.getName()) == null) {
                                ejbJar.addInterceptor(new Interceptor(interceptor.getName()));
                            }
                        }

                        InterceptorBinding binding = new InterceptorBinding(bean);
                        assemblyDescriptor.getInterceptorBinding().add(0, binding);

                        for (Class interceptor : interceptors.value()) {
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
                    InterceptorBinding binding = assemblyDescriptor.addInterceptorBinding(new InterceptorBinding(bean));
                    binding.setExcludeDefaultInterceptors(true);
                }

                for (Annotated<Method> method : annotationFinder.findMetaAnnotatedMethods(ExcludeDefaultInterceptors.class)) {
                    InterceptorBinding binding = assemblyDescriptor.addInterceptorBinding(new InterceptorBinding(bean));
                    binding.setExcludeDefaultInterceptors(true);
                    binding.setMethod(new NamedMethod(method.get()));
                }

                for (Annotated<Method> method : sortMethods(annotationFinder.findMetaAnnotatedMethods(ExcludeClassInterceptors.class))) {
                    InterceptorBinding binding = assemblyDescriptor.addInterceptorBinding(new InterceptorBinding(bean));
                    binding.setExcludeClassInterceptors(true);
                    binding.setMethod(new NamedMethod(method.get()));
                }

                /**
                 * All beans except MDBs have remoting capabilities (busines or legacy interfaces)
                 */
                if (bean instanceof RemoteBean) {
                    RemoteBean remoteBean = (RemoteBean) bean;

                    /*
                     * @RemoteHome
                     */
                    if (remoteBean.getHome() == null) {
                        RemoteHome remoteHome = getInheritableAnnotation(clazz, RemoteHome.class);
                        if (remoteHome != null) {
                            Class<?> homeClass = remoteHome.value();
                            try {
                                Method create = null;
                                for (Method method : homeClass.getMethods()) {
                                    if (method.getName().startsWith("create")) {
                                        create = method;
                                        break;
                                    }
                                }
                                if (create == null) throw new NoSuchMethodException("create");

                                Class<?> remoteClass = create.getReturnType();
                                remoteBean.setHome(homeClass.getName());
                                remoteBean.setRemote(remoteClass.getName());
                            } catch (NoSuchMethodException e) {
                                logger.error("Class annotated as a RemoteHome has no 'create()' method.  Unable to determine remote interface type.  Bean class: " + clazz.getName() + ",  Home class: " + homeClass.getName());
                            }
                        }
                    }

                    /*
                     * @LocalHome
                     */
                    if (remoteBean.getLocalHome() == null) {
                        LocalHome localHome = getInheritableAnnotation(clazz, LocalHome.class);
                        if (localHome != null) {
                            Class<?> homeClass = localHome.value();
                            try {
                                Method create = null;
                                for (Method method : homeClass.getMethods()) {
                                    if (method.getName().startsWith("create")) {
                                        create = method;
                                        break;
                                    }
                                }
                                if (create == null) throw new NoSuchMethodException("create");

                                Class<?> remoteClass = create.getReturnType();
                                remoteBean.setLocalHome(homeClass.getName());
                                remoteBean.setLocal(remoteClass.getName());
                            } catch (NoSuchMethodException e) {
                                logger.error("Class annotated as a LocalHome has no 'create()' method.  Unable to determine remote interface type.  Bean class: " + clazz.getName() + ",  Home class: " + homeClass.getName());
                            }
                        }
                    }

                    /*
                     * Annotations specific to @Stateless, @Stateful and @Singleton beans
                     */
                    if (remoteBean instanceof SessionBean) {
                        SessionBean sessionBean = (SessionBean) remoteBean;

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
                            DependsOn dependsOn = getInheritableAnnotation(clazz, DependsOn.class);
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
                            if (annotationFinder.isAnnotationPresent(Path.class) || !annotationFinder.findAnnotatedMethods(Path.class).isEmpty()) {
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
                                ConcurrencyManagement tx = getInheritableAnnotation(clazz, ConcurrencyManagement.class);
                                javax.ejb.ConcurrencyManagementType concurrencyType = javax.ejb.ConcurrencyManagementType.CONTAINER;
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
                            LockHandler lockHandler = new LockHandler(assemblyDescriptor, sessionBean);
                            if (sessionBean.getConcurrencyManagementType() == ConcurrencyManagementType.CONTAINER) {
                                processAttributes(lockHandler, clazz, annotationFinder);
                            } else {
                                checkAttributes(lockHandler, ejbName, ejbModule, annotationFinder, "invalidConcurrencyAttribute");
                            }

                            /*
                             * @AccessTimeout
                             */
                            AccessTimeoutHandler accessTimeoutHandler =
                                new AccessTimeoutHandler(assemblyDescriptor, sessionBean, lockHandler.getContainerConcurrency());
                            processAttributes(accessTimeoutHandler, clazz, annotationFinder);

                            /*
                             * @Startup
                             */
                            if (!sessionBean.hasInitOnStartup()) {
                                Startup startup = getInheritableAnnotation(clazz, Startup.class);
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
                                if(annotation != null) {
                                    final Timeout timeout = new Timeout();
                                    timeout.setTimeout(annotation.value());
                                    timeout.setUnit(annotation.unit());
                                    sessionBean.setStatefulTimeout(timeout);
                                }
                            }

                            /*
                             * @AccessTimeout
                             */
                            AccessTimeoutHandler accessTimeoutHandler = new AccessTimeoutHandler(assemblyDescriptor, sessionBean);
                            processAttributes(accessTimeoutHandler, clazz, annotationFinder);

                        }
                    }
                }

                if (bean instanceof MessageDrivenBean) {
                    /*
                     * @ActivationConfigProperty
                     */
                    MessageDrivenBean mdb = (MessageDrivenBean) bean;
                    MessageDriven messageDriven = clazz.getAnnotation(MessageDriven.class);
                    if (messageDriven != null) {
                        javax.ejb.ActivationConfigProperty[] configProperties = messageDriven.activationConfig();
                        if (configProperties != null) {
                            ActivationConfig activationConfig = mdb.getActivationConfig();
                            if (activationConfig == null) {
                                activationConfig = new ActivationConfig();
                                mdb.setActivationConfig(activationConfig);
                            }
                            Properties properties = activationConfig.toProperties();
                            for (javax.ejb.ActivationConfigProperty property : configProperties) {
                                if (!properties.containsKey(property.propertyName())) {
                                    activationConfig.addProperty(property.propertyName(), property.propertyValue());
                                }
                            }
                        }

                        if (mdb.getMessagingType() == null) {
                            Class<?> interfce = messageDriven.messageListenerInterface();
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
                        List<Class<?>> interfaces = new ArrayList<Class<?>>();
                        for (Class<?> intf : clazz.getInterfaces()) {
                            String name = intf.getName();
                            if (!name.equals("java.io.Serializable") &&
                                    !name.equals("java.io.Externalizable") &&
                                    !name.startsWith("javax.ejb.") &&
                                    !intf.isSynthetic()) {
                                interfaces.add(intf);
                            }
                        }

                        if (interfaces.size() != 1) {
                            String msg = "When annotating a bean class as @MessageDriven without declaring messageListenerInterface, the bean must implement exactly one interface, no more and no less. beanClass=" + clazz.getName() + " interfaces=";
                            for (Class<?> intf : interfaces) {
                                msg += intf.getName() + ", ";
                            }
                            // TODO: Make this a validation failure, not an exception
                            throw new IllegalStateException(msg);
                        }
                        mdb.setMessagingType(interfaces.get(0).getName());
                    }
                }

                buildAnnotatedRefs(bean, annotationFinder, classLoader);

                processWebServiceHandlers(ejbModule, bean);

                processWebServiceClientHandlers(bean, classLoader);

                try {
                    if (BeanContext.Comp.class.getName().equals(bean.getEjbClass())) {
                        buildAnnotatedRefs(bean, ejbModule.getFinder(), classLoader);
                    }
                } catch (OpenEJBException e) {
                    logger.error("Processing of @Resource, @EJB, and other references failed for CDI managed beans", e);
                }
            }

            for (Interceptor interceptor : ejbModule.getEjbJar().getInterceptors()) {
                final Class<?> clazz;
                try {
                    clazz = classLoader.loadClass(interceptor.getInterceptorClass());
                } catch (ClassNotFoundException e) {
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

                processWebServiceClientHandlers(interceptor, classLoader);

                /**
                 * Interceptors do not have their own section in ejb-jar.xml for resource references
                 * so we add them to the references of each ejb.  A bit backwards but more or less
                 * mandated by the design of the spec.
                 */
                for (EnterpriseBean bean : enterpriseBeans) {
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

        private void processAsynchronous(EnterpriseBean bean, AnnotationFinder annotationFinder) {
            if (!(bean instanceof SessionBean)) {
                return;
            }

            SessionBean sessionBean = (SessionBean) bean;

            for (Annotated<Method> method : annotationFinder.findMetaAnnotatedMethods(Asynchronous.class)) {
                sessionBean.getAsyncMethod().add(new AsyncMethod(method.get()));
            }

            //Spec 4.5.1 @Asynchronous could be used at the class level of a bean-class ( or superclass ).
            //Seems that it should not be used on the any interface view

            for (Annotated<Class<?>> clazz : annotationFinder.findMetaAnnotatedClasses(Asynchronous.class)) {
                if (!clazz.get().isInterface()) {
                    sessionBean.getAsynchronousClasses().add(clazz.get().getName());
                }
            }
        }

        private <T extends Injectable> void mergeJndiReferences(Map<String, T> from, Map<String, T> to) {
            for (Map.Entry<String, T> entry : from.entrySet()) {
                Injectable injectable = to.get(entry.getKey());
                if (injectable == null) {
                    to.put(entry.getKey(), entry.getValue());
                } else {
                    injectable.getInjectionTarget().addAll(entry.getValue().getInjectionTarget());
                }
            }
        }

        //TODO why is this necessary, we scan for exceptions with this annotation elsewhere.
        private void processApplicationExceptions(Class<?> clazz, AssemblyDescriptor assemblyDescriptor) {
            /*
             * @ApplicationException
             */
            for (Method method : clazz.getMethods()) {
                for (Class<?> exception : method.getExceptionTypes()) {
                    ApplicationException annotation = exception.getAnnotation(ApplicationException.class);
                    if (annotation == null) continue;
                    if (assemblyDescriptor.getApplicationException(exception) != null) {
                        mergeApplicationExceptionAnnotation(assemblyDescriptor, exception, annotation);
                    } else {
                        logger.debug("Found previously undetected application exception {0} listed on a method {1} with annotation {2}", method, exception, annotation);
                        assemblyDescriptor.addApplicationException(exception, annotation.rollback(), annotation.inherited());
                    }
                }
            }
        }

        private void processSessionInterfaces(SessionBean sessionBean, Class<?> beanClass, EjbModule ejbModule) {

            ValidationContext validation = ejbModule.getValidation();
            String ejbName = sessionBean.getEjbName();

            boolean strict = getProperty(STRICT_INTERFACE_DECLARATION, false + "").equalsIgnoreCase("true");

            /*
             * Collect all interfaces explicitly declared via xml.
             * We will subtract these from the interfaces implemented
             * by the bean and do annotation scanning on the remainder.
             */
            List<String> descriptor = new ArrayList<String>();
            descriptor.add(sessionBean.getHome());
            descriptor.add(sessionBean.getRemote());
            descriptor.add(sessionBean.getLocalHome());
            descriptor.add(sessionBean.getLocal());
            descriptor.addAll(sessionBean.getBusinessLocal());
            descriptor.addAll(sessionBean.getBusinessRemote());
            descriptor.add(sessionBean.getServiceEndpoint());

            BusinessInterfaces xml = new BusinessInterfaces();
            xml.addLocals(sessionBean.getBusinessLocal(), ejbModule.getClassLoader());
            xml.addRemotes(sessionBean.getBusinessRemote(), ejbModule.getClassLoader());

            if (beanClass.getAnnotation(LocalBean.class) != null) {
                sessionBean.setLocalBean(new Empty());
            }

            /**
             * Anything declared as both <business-local> and <business-remote> is invalid in strict mode
             */
            if (strict) for (Class interfce : xml.local) {
                if (xml.remote.contains(interfce)) {
                    validation.fail(ejbName, "xml.localRemote.conflict", interfce.getName());
                }
            }

            /**
             * Merge the xml declared business interfaces into the complete set
             */
            BusinessInterfaces all = new BusinessInterfaces();
            all.local.addAll(xml.local);
            all.remote.addAll(xml.remote);

            final List<Class<?>> classes = strict ? new ArrayList(asList(beanClass)) : ancestors(beanClass);

            for (Class<?> clazz : classes) {

                /*
                 * @WebService
                 * @WebServiceProvider
                 */
                if (sessionBean.getServiceEndpoint() == null) {
                    Class defaultEndpoint = BeanContext.ServiceEndpoint.class;

                    for (Class interfce : clazz.getInterfaces()) {
                        if (interfce.isAnnotationPresent(WebService.class)) {
                            defaultEndpoint = interfce;
                        }
                    }

                    WebService webService = clazz.getAnnotation(WebService.class);
                    if (webService != null) {

                        String className = webService.endpointInterface();

                        if (!className.equals("")) {
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
                 * javax.ejb.*
                 */
                List<Class<?>> interfaces = new ArrayList<Class<?>>();
                for (Class<?> interfce : clazz.getInterfaces()) {
                    String name = interfce.getName();
                    if (!name.equals("java.io.Serializable") &&
                            !name.equals("java.io.Externalizable") &&
                            !name.startsWith("javax.ejb.") &&
                            !descriptor.contains(interfce.getName()) &&
                            !interfce.isSynthetic()) {
                        interfaces.add(interfce);
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
                Local local = clazz.getAnnotation(Local.class);
                Remote remote = clazz.getAnnotation(Remote.class);

                boolean impliedLocal = local != null && local.value().length == 0;
                boolean impliedRemote = remote != null && remote.value().length == 0;

                /**
                 * This set holds the values of @Local and @Remote
                 * when applied to the bean class itself
                 *
                 * These declarations override any similar declaration
                 * on the interface.
                 */
                BusinessInterfaces bean = new BusinessInterfaces();
                if (local != null) bean.local.addAll(asList(local.value()));
                if (remote != null) bean.remote.addAll(asList(remote.value()));

                if (strict) for (Class interfce : bean.local) {
                    if (bean.remote.contains(interfce)) {
                        validation.fail(ejbName, "ann.localRemote.conflict", interfce.getName());
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
                        List<String> interfaceNames = new ArrayList<String>();
                        for (Class<?> intrfce : interfaces) {
                            interfaceNames.add(intrfce.getName()+".class");
                        }
                        if (impliedLocal) validation.fail(ejbName, "ann.local.noAttributes", join(", ", interfaceNames));
                        if (impliedRemote) validation.fail(ejbName, "ann.remote.noAttributes", join(", ", interfaceNames));

                        /**
                         * This bean is invalid, so do not bother looking at the other interfaces or the superclass
                         */
                        return;
                    } else if (strict && impliedLocal && impliedRemote) {
                        Class<?> interfce = interfaces.remove(0);
                        /**
                         * Cannot imply @Local and @Remote at the same time with strict mode on
                         */
                        validation.fail(ejbName, "ann.localRemote.ambiguous", interfce.getName());
                    } else {
                        if (impliedLocal) bean.local.addAll(interfaces);
                        if (impliedRemote) bean.remote.addAll(interfaces);

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
                BusinessInterfaces implemented = new BusinessInterfaces();

                for (Class interfce : interfaces) {
                    boolean isLocal = interfce.isAnnotationPresent(Local.class);
                    boolean isRemote = interfce.isAnnotationPresent(Remote.class);

                    if (strict && isLocal && isRemote) {
                        validation.fail(ejbName, "ann.localRemote.conflict", interfce.getName());
                    } else {
                        Class[] superInterface = interfce.getInterfaces();
                        if (isLocal) {
                          if (strict) {
                            for (Class si : superInterface) {
                              boolean present = si.isAnnotationPresent(Remote.class);
                              if (present) {
                                validation.fail(ejbName, "ann.remoteOrLocal.converse.parent", interfce.getName(), "Local", si.getName(), "Remote");
                              }
                            }
                          }
                          implemented.local.add(interfce);
                        }
                        if (isRemote) {
                          if (strict) {
                            for (Class si : superInterface) {
                              boolean present = si.isAnnotationPresent(Local.class);
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
                for (Class interfce : bean.local) validateLocalInterface(interfce, validation, ejbName);
                for (Class interfce : bean.remote) validateRemoteInterface(interfce, validation, ejbName);

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
                for (Class interfce : implemented.local) validateLocalInterface(interfce, validation, ejbName);
                for (Class interfce : implemented.remote) validateRemoteInterface(interfce, validation, ejbName);

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
                        && sessionBean.getServiceEndpoint() == null
                        && all.local.isEmpty()
                        && all.remote.isEmpty()
                        ) {

                    if (interfaces.size() == 0 || AnnotationUtil.getAnnotation(PersistenceContext.class, clazz) != null) {
                        // No interfaces?  Then @LocalBean

                        sessionBean.setLocalBean(new Empty());

                    } else if (interfaces.size() == 1) {
                        // One interface?  Then @Local

                        all.local.add(interfaces.remove(0));

                    } else {
                        // Multiple interfaces?  Illegal
                        // TODO -- create message
                        validation.fail(ejbName, "TODO");
                        return;
                    }
                }
                
                //alway set Local View for ManagedBean
                if (beanClass.isAnnotationPresent(ManagedBean.class)){
                    sessionBean.setLocalBean(new Empty());
                }
                

                /**
                 * Track any interfaces we didn't use
                 */
                all.unspecified.addAll(interfaces);
            }

            // Finally, add all the business interfaces we found
            for (Class interfce : all.local) sessionBean.addBusinessLocal(interfce);
            for (Class interfce : all.remote) sessionBean.addBusinessRemote(interfce);

            // Anything unspecified?  Let's throw it in as local.
            //
            // This covers the required case where a bean is declared implementing
            // one interface and does not use @Local or @Remote anywhere nor does
            // it specify the business-local or business-remote elements in the ejb-jar.xml
            //
            // It goes a little beyond that, but no one has ever complained about having
            // more local interfaces.

            // TODO allow to be re-enabled with a flag
            //for (Class interfce : all.unspecified) sessionBean.addBusinessLocal(interfce);


        }

        private static class BusinessInterfaces {
            private Set<Class> local = new LinkedHashSet<Class>();
            private Set<Class> remote = new LinkedHashSet<Class>();
            private Set<Class> unspecified = new LinkedHashSet<Class>();

            public void addLocals(Collection<String> names, ClassLoader loader){
                add(loader, names, local);
            }

            public void addRemotes(Collection<String> names, ClassLoader loader){
                add(loader, names, remote);
            }

            private void add(ClassLoader loader, Collection<String> names, Set<Class> classes) {
                for (String className : names) {
                    try {
                        classes.add(loader.loadClass(className));
                    } catch (Throwable t) {
                        // handled in validation
                    }
                }
            }
        }

        private String getProperty(String key, String defaultValue) {
            String value = SystemInstance.get().getProperty(key, defaultValue);
            final DeploymentModule module = getModule();

            if (module instanceof EjbModule) {
                EjbModule ejbModule = (EjbModule) module;

                OpenejbJar openejbJar = ejbModule.getOpenejbJar();
                if (openejbJar != null && openejbJar.getProperties() != null){
                    value = openejbJar.getProperties().getProperty(key, value);
                }
            }
            return value;
        }

        private void processSecurityAnnotations(Class<?> beanClass, String ejbName, EjbModule ejbModule, AnnotationFinder annotationFinder, EnterpriseBean bean) {
            AssemblyDescriptor assemblyDescriptor = ejbModule.getEjbJar().getAssemblyDescriptor();

            List<String> classPermissions = getDeclaredClassPermissions(assemblyDescriptor, ejbName);

            for (Class<?> clazzz : ancestors(beanClass)) {
                final MetaAnnotatedClass<?> clazz = new MetaAnnotatedClass(clazzz);
                /*
                 * Process annotations at the class level
                 */
                if (!classPermissions.contains("*") || !classPermissions.contains(clazz.getName())) {

                    RolesAllowed rolesAllowed = clazz.getAnnotation(RolesAllowed.class);
                    PermitAll permitAll = clazz.getAnnotation(PermitAll.class);

                    /*
                     * @RolesAllowed
                     */
                    if (rolesAllowed != null && permitAll != null) {
                        ejbModule.getValidation().fail(ejbName, "permitAllAndRolesAllowedOnClass", clazz.getName());
                    }

                    if (rolesAllowed != null) {
                        MethodPermission methodPermission = new MethodPermission();
                        methodPermission.getRoleName().addAll(asList(rolesAllowed.value()));
                        methodPermission.getMethod().add(new org.apache.openejb.jee.Method(ejbName, clazz.getName(), "*"));
                        assemblyDescriptor.getMethodPermission().add(methodPermission);

                        // Automatically add a role ref for any role listed in RolesAllowed
                        RemoteBean remoteBean = (RemoteBean) bean;
                        List<SecurityRoleRef> securityRoleRefs = remoteBean.getSecurityRoleRef();
                        for (String role : rolesAllowed.value()) {
                            securityRoleRefs.add(new SecurityRoleRef(role));
                        }
                    }

                    /*
                     * @PermitAll
                     */
                    if (permitAll != null) {
                        MethodPermission methodPermission = new MethodPermission();
                        methodPermission.setUnchecked(true);
                        methodPermission.getMethod().add(new org.apache.openejb.jee.Method(ejbName, clazz.getName(), "*"));
                        assemblyDescriptor.getMethodPermission().add(methodPermission);
                    }
                }

                /*
                 * @RunAs
                 */
                RunAs runAs = clazz.getAnnotation(RunAs.class);
                if (runAs != null && bean.getSecurityIdentity() == null) {
                    SecurityIdentity securityIdentity = new SecurityIdentity();
                    securityIdentity.setRunAs(runAs.value());
                    bean.setSecurityIdentity(securityIdentity);
                }

                /*
                 * @DeclareRoles
                 */
                DeclareRoles declareRoles = clazz.getAnnotation(DeclareRoles.class);
                if (declareRoles != null && bean instanceof RemoteBean) {
                    RemoteBean remoteBean = (RemoteBean) bean;
                    List<SecurityRoleRef> securityRoleRefs = remoteBean.getSecurityRoleRef();
                    for (String role : declareRoles.value()) {
                        securityRoleRefs.add(new SecurityRoleRef(role));
                    }
                }
            }

            /*
             * Process annotations at the method level
             */
            List<Method> seen = new ArrayList<Method>();

            /*
             * @RolesAllowed
             */
            for (Annotated<Method> method : annotationFinder.findMetaAnnotatedMethods(RolesAllowed.class)) {
                checkConflictingSecurityAnnotations(method, ejbName, ejbModule, seen);
                RolesAllowed rolesAllowed = method.getAnnotation(RolesAllowed.class);
                MethodPermission methodPermission = new MethodPermission();
                methodPermission.getRoleName().addAll(asList(rolesAllowed.value()));
                methodPermission.getMethod().add(new org.apache.openejb.jee.Method(ejbName, method.get()));
                assemblyDescriptor.getMethodPermission().add(methodPermission);

                // Automatically add a role ref for any role listed in RolesAllowed
                RemoteBean remoteBean = (RemoteBean) bean;
                List<SecurityRoleRef> securityRoleRefs = remoteBean.getSecurityRoleRef();
                for (String role : rolesAllowed.value()) {
                    securityRoleRefs.add(new SecurityRoleRef(role));
                }
            }

            /*
             * @PermitAll
             */
            for (Annotated<Method> method : annotationFinder.findMetaAnnotatedMethods(PermitAll.class)) {
                checkConflictingSecurityAnnotations(method, ejbName, ejbModule, seen);
                MethodPermission methodPermission = new MethodPermission();
                methodPermission.setUnchecked(true);
                methodPermission.getMethod().add(new org.apache.openejb.jee.Method(ejbName, method.get()));
                assemblyDescriptor.getMethodPermission().add(methodPermission);
            }

            /*
             * @DenyAll
             */
            for (Annotated<Method> method : annotationFinder.findMetaAnnotatedMethods(DenyAll.class)) {
                checkConflictingSecurityAnnotations(method, ejbName, ejbModule, seen);
                ExcludeList excludeList = assemblyDescriptor.getExcludeList();
                excludeList.addMethod(new org.apache.openejb.jee.Method(ejbName, method.get()));
            }

        }

        /**
         * Validation
         * <p/>
         * Conflicting use of @RolesAllowed, @PermitAll, and @DenyAll
         *
         * @param method
         * @param ejbName
         * @param ejbModule
         * @param seen
         */
        private void checkConflictingSecurityAnnotations(Annotated<Method> method, String ejbName, EjbModule ejbModule, List<Method> seen) {
            if (seen.contains(method)) return;
            seen.add(method.get());

            List<String> annotations = new ArrayList<String>();
            for (Class<? extends Annotation> annotation : asList(RolesAllowed.class, PermitAll.class, DenyAll.class)) {
                if (method.getAnnotation(annotation) != null) {
                    annotations.add("@" + annotation.getSimpleName());
                }
            }

            if (annotations.size() > 1) {
                ejbModule.getValidation().fail(ejbName, "conflictingSecurityAnnotations", method.get().getName(), join(" and ", annotations), method.get().getDeclaringClass());
            }
        }

        private void processSchedules(EnterpriseBean bean, AnnotationFinder annotationFinder) {
            if(! (bean instanceof TimerConsumer)){
                return;
            }
            TimerConsumer timerConsumer = (TimerConsumer)bean;
            Set<Annotated<Method>> scheduleMethods = new HashSet<Annotated<Method>>();
            scheduleMethods.addAll(annotationFinder.findMetaAnnotatedMethods(javax.ejb.Schedules.class));
            scheduleMethods.addAll(annotationFinder.findMetaAnnotatedMethods(javax.ejb.Schedule.class));

            List<Timer> timers = timerConsumer.getTimer();

            // TODO : The NamedMethod object implements equals and hashCode, so we could rely on that rather than collecting strings
            Set<String> methodsConfiguredInDeploymentXml = new HashSet<String>();
            for (Timer timer : timers) {
                NamedMethod namedMethod = timer.getTimeoutMethod();
                methodsConfiguredInDeploymentXml.add(namedMethod.getMethodName() + (namedMethod.getMethodParams() == null ? "" : Join.join("", namedMethod.getMethodParams().getMethodParam())));
            }

            for (Annotated<Method> method : scheduleMethods) {

                // Don't add the schedules from annotations if the schedules have been
                // supplied for this method via xml.  The xml is considered an override.
                if (methodsConfiguredInDeploymentXml.contains(method.get().getName() + Join.join("", (Object[]) asStrings(method.get().getParameterTypes())))) {
                    continue;
                }

                List<javax.ejb.Schedule> scheduleAnnotationList = new ArrayList<javax.ejb.Schedule>();

                javax.ejb.Schedules schedulesAnnotation = method.getAnnotation(javax.ejb.Schedules.class);
                if (schedulesAnnotation != null) {
                    scheduleAnnotationList.addAll(asList(schedulesAnnotation.value()));
                }

                javax.ejb.Schedule scheduleAnnotation = method.getAnnotation(javax.ejb.Schedule.class);
                if (scheduleAnnotation != null) {
                    scheduleAnnotationList.add(scheduleAnnotation);
                }

                for (javax.ejb.Schedule schedule : scheduleAnnotationList) {
                    Timer timer = new Timer();
                    timer.setPersistent(schedule.persistent());
                    timer.setInfo((schedule.info() == null || schedule.info().isEmpty()) ? null : schedule.info());
                    timer.setTimezone((schedule.timezone() == null || schedule.timezone().isEmpty()) ? null : schedule.timezone());
                    //Copy TimerSchedule
                    TimerSchedule timerSchedule = new TimerSchedule();
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

        private void processCallbacks(Lifecycle bean, AnnotationFinder annotationFinder) {

            final boolean override = "true".equalsIgnoreCase(getProperty("openejb.callbacks.override", "false"));

            /*
             * @PostConstruct
             */
            if (apply(override, bean.getPostConstruct())) {
                for (Annotated<Method> method : sortMethods(annotationFinder.findMetaAnnotatedMethods(PostConstruct.class))) {
                    bean.getPostConstruct().add(new LifecycleCallback(method.get()));
                }
            }

            /*
             * @PreDestroy
             */
            if (apply(override, bean.getPreDestroy())) {
                for (Annotated<Method> method : sortMethods(annotationFinder.findMetaAnnotatedMethods(PreDestroy.class))) {
                    bean.getPreDestroy().add(new LifecycleCallback(method.get()));
                }
            }

            if (bean instanceof Invokable) {
                Invokable invokable = (Invokable) bean;

                /*
                 * @AroundInvoke
                 */
                if (apply(override, invokable.getAroundInvoke())) {
                    for (Annotated<Method> method : sortMethods(annotationFinder.findMetaAnnotatedMethods(javax.interceptor.AroundInvoke.class))) {
                        invokable.getAroundInvoke().add(new AroundInvoke(method.get()));
                    }
                }

                /*
                 *  @AroundTimeout
                 */
                if (apply(override, invokable.getAroundInvoke())) {
                    for (Annotated<Method> method : sortMethods((annotationFinder.findMetaAnnotatedMethods(javax.interceptor.AroundTimeout.class)))) {
                        invokable.getAroundTimeout().add(new AroundTimeout(method.get()));
                    }
                }
            }

            /*
             * @Timeout
             */
            if (bean instanceof TimerConsumer) {
                TimerConsumer timerConsumer = (TimerConsumer) bean;
                if (timerConsumer.getTimeoutMethod() == null) {
                    List<Annotated<Method>> timeoutMethods = sortMethods(annotationFinder.findMetaAnnotatedMethods(javax.ejb.Timeout.class));
                    //Validation Logic is moved to CheckCallback class.
                    if(timeoutMethods.size()  >=  1){
                        // Use the timeout method most near the child class because
                        // the timeout method in child class will override the timeout method in super classes
                        timerConsumer.setTimeoutMethod(new NamedMethod(timeoutMethods.get(timeoutMethods.size() - 1).get()));
                    }
                }
            }

            if (bean instanceof org.apache.openejb.jee.Session) {
                org.apache.openejb.jee.Session session = (org.apache.openejb.jee.Session) bean;

                /*
                 * @AfterBegin
                 */
                LifecycleCallback afterBegin = getFirst(session.getAfterBegin());
                if (afterBegin == null) {
                    for (Annotated<Method> method : sortMethods(annotationFinder.findMetaAnnotatedMethods(AfterBegin.class))) {
                        session.getAfterBegin().add(new LifecycleCallback(method.get()));
                    }
                }

                /*
                 * @BeforeCompletion
                 */
                LifecycleCallback beforeCompletion = getFirst(session.getBeforeCompletion());
                if (beforeCompletion == null) {
                    for (Annotated<Method> method : sortMethods(annotationFinder.findMetaAnnotatedMethods(BeforeCompletion.class))) {
                        session.getBeforeCompletion().add(new LifecycleCallback(method.get()));
                    }
                }

                /*
                 * @AfterCompletion
                 */
                LifecycleCallback afterCompletion = getFirst(session.getAfterCompletion());
                if (afterCompletion == null) {
                    for (Annotated<Method> method : sortMethods(annotationFinder.findMetaAnnotatedMethods(AfterCompletion.class))) {
                        session.getAfterCompletion().add(new LifecycleCallback(method.get()));
                    }
                }

                /*
                 * @PostActivate
                 */
                if (apply(override, session.getPostActivate())) {
                    for (Annotated<Method> method : sortMethods(annotationFinder.findMetaAnnotatedMethods(PostActivate.class))) {
                        session.getPostActivate().add(new LifecycleCallback(method.get()));
                    }
                }

                /*
                 * @PrePassivate
                 */
                if (apply(override, session.getPrePassivate())) {
                    for (Annotated<Method> method : sortMethods(annotationFinder.findMetaAnnotatedMethods(PrePassivate.class))) {
                        session.getPrePassivate().add(new LifecycleCallback(method.get()));
                    }
                }
                /*
                 * @Init
                 */
                for (Annotated<Method> method : sortMethods(annotationFinder.findMetaAnnotatedMethods(Init.class))) {
                    InitMethod initMethod = new InitMethod(method.get());

                    Init init = method.getAnnotation(Init.class);
                    if (init.value() != null && !init.value().equals("")) {
                        initMethod.setCreateMethod(init.value());
                    }

                    session.getInitMethod().add(initMethod);
                }

                /*
                 * @Remove
                 */
                List<Annotated<Method>> removeMethods = sortMethods(annotationFinder.findMetaAnnotatedMethods(Remove.class));
                Map<NamedMethod, RemoveMethod> declaredRemoveMethods = new HashMap<NamedMethod, RemoveMethod>();
                for (RemoveMethod removeMethod : session.getRemoveMethod()) {
                    declaredRemoveMethods.put(removeMethod.getBeanMethod(), removeMethod);
                }
                for (Annotated<Method> method : removeMethods) {
                    Remove remove = method.getAnnotation(Remove.class);
                    RemoveMethod removeMethod = new RemoveMethod(method.get(), remove.retainIfException());

                    RemoveMethod declaredRemoveMethod = declaredRemoveMethods.get(removeMethod.getBeanMethod());

                    if (declaredRemoveMethod == null) {
                        session.getRemoveMethod().add(removeMethod);
                    } else if (!declaredRemoveMethod.isExplicitlySet()) {
                        declaredRemoveMethod.setRetainIfException(remove.retainIfException());
                    }
                }
            }
        }

        private boolean apply(boolean override, List<?> list) {
            // Compliant behavior is to always add the annotated callbacks
            // into the list of xml configured callbacks

            // Legacy behavior was to not apply the annotations to the list
            // if there were any of the related elements specified in the xml

            // if we are *not* using the legacy logic, always return true
            if (!override) return true;

            // if we are using that logic, then only return true if the list is empty
            // i.e. we will not augment the list if callbacks have been specified in xml
            return list.size() == 0;
        }

        public void buildAnnotatedRefs(JndiConsumer consumer, IAnnotationFinder annotationFinder, ClassLoader classLoader) throws OpenEJBException {
            //
            // @EJB
            //

            List<EJB> ejbList = new ArrayList<EJB>();
            for (Annotated<Class<?>> clazz : annotationFinder.findMetaAnnotatedClasses(EJBs.class)) {
                EJBs ejbs = clazz.getAnnotation(EJBs.class);
                ejbList.addAll(asList(ejbs.value()));
            }
            for (Annotated<Class<?>> clazz : annotationFinder.findMetaAnnotatedClasses(EJB.class)) {
                EJB e = clazz.getAnnotation(EJB.class);
                ejbList.add(e);
            }

            for (EJB ejb : ejbList) {
                buildEjbRef(consumer, ejb, null);
            }

            for (Annotated<Field> field : annotationFinder.findMetaAnnotatedFields(EJB.class)) {
                EJB ejb = field.getAnnotation(EJB.class);

                Member member = new FieldMember(field.get());

                buildEjbRef(consumer, ejb, member);
            }

            for (Annotated<Method> method : annotationFinder.findMetaAnnotatedMethods(EJB.class)) {
                EJB ejb = method.getAnnotation(EJB.class);

                Member member = new MethodMember(method.get());

                buildEjbRef(consumer, ejb, member);
            }

            //
            // @Resource
            //

            List<Resource> resourceList = new ArrayList<Resource>();
            for (Annotated<Class<?>> clazz : annotationFinder.findMetaAnnotatedClasses(Resources.class)) {
                Resources resources = clazz.getAnnotation(Resources.class);
                resourceList.addAll(asList(resources.value()));
            }
            for (Annotated<Class<?>> clazz : annotationFinder.findMetaAnnotatedClasses(Resource.class)) {
                Resource resource = clazz.getAnnotation(Resource.class);
                resourceList.add(resource);
            }

            for (Resource resource : resourceList) {
                buildResource(consumer, resource, null);
            }

            for (Annotated<Field> field : annotationFinder.findMetaAnnotatedFields(Resource.class)) {
                Resource resource = field.getAnnotation(Resource.class);

                Member member = new FieldMember(field.get());

                buildResource(consumer, resource, member);
            }

            for (Annotated<Method> method : annotationFinder.findMetaAnnotatedMethods(Resource.class)) {
                Resource resource = method.getAnnotation(Resource.class);

                Member member = new MethodMember(method.get());

                buildResource(consumer, resource, member);
            }

            //
            // @Context (REST)
            //
            for (Annotated<Field> field : annotationFinder.findMetaAnnotatedFields(Context.class)) {
                Member member = new FieldMember(field.get());
                buildContext(consumer, member);
            }

            //
            // @WebServiceRef
            //

            List<WebServiceRef> webservicerefList = new ArrayList<WebServiceRef>();
            for (Annotated<Class<?>> clazz : annotationFinder.findMetaAnnotatedClasses(WebServiceRefs.class)) {
                WebServiceRefs webServiceRefs = clazz.getAnnotation(WebServiceRefs.class);
                webservicerefList.addAll(asList(webServiceRefs.value()));
            }
            for (Annotated<Class<?>> clazz : annotationFinder.findMetaAnnotatedClasses(WebServiceRef.class)) {
                WebServiceRef webServiceRef = clazz.getAnnotation(WebServiceRef.class);
                webservicerefList.add(webServiceRef);
            }

            for (WebServiceRef webserviceref : webservicerefList) {

                buildWebServiceRef(consumer, webserviceref, null, null, classLoader);
            }

            for (Annotated<Field> field : annotationFinder.findMetaAnnotatedFields(WebServiceRef.class)) {
                WebServiceRef webserviceref = field.getAnnotation(WebServiceRef.class);
                HandlerChain handlerChain = field.getAnnotation(HandlerChain.class);

                Member member = new FieldMember(field.get());

                buildWebServiceRef(consumer, webserviceref, handlerChain, member, classLoader);
            }

            for (Annotated<Method> method : annotationFinder.findMetaAnnotatedMethods(WebServiceRef.class)) {
                WebServiceRef webserviceref = method.getAnnotation(WebServiceRef.class);
                HandlerChain handlerChain = method.getAnnotation(HandlerChain.class);

                Member member = new MethodMember(method.get());

                buildWebServiceRef(consumer, webserviceref, handlerChain, member, classLoader);
            }

            //
            // @PersistenceUnit
            //

            List<PersistenceUnit> persistenceUnitList = new ArrayList<PersistenceUnit>();
            for (Annotated<Class<?>> clazz : annotationFinder.findMetaAnnotatedClasses(PersistenceUnits.class)) {
                PersistenceUnits persistenceUnits = clazz.getAnnotation(PersistenceUnits.class);
                persistenceUnitList.addAll(asList(persistenceUnits.value()));
            }
            for (Annotated<Class<?>> clazz : annotationFinder.findMetaAnnotatedClasses(PersistenceUnit.class)) {
                PersistenceUnit persistenceUnit = clazz.getAnnotation(PersistenceUnit.class);
                persistenceUnitList.add(persistenceUnit);
            }
            for (PersistenceUnit pUnit : persistenceUnitList) {
                buildPersistenceUnit(consumer, pUnit, null);
            }
            for (Annotated<Field> field : annotationFinder.findMetaAnnotatedFields(PersistenceUnit.class)) {
                PersistenceUnit pUnit = field.getAnnotation(PersistenceUnit.class);
                Member member = new FieldMember(field.get());
                buildPersistenceUnit(consumer, pUnit, member);
            }
            for (Annotated<Method> method : annotationFinder.findMetaAnnotatedMethods(PersistenceUnit.class)) {
                PersistenceUnit pUnit = method.getAnnotation(PersistenceUnit.class);
                Member member = new MethodMember(method.get());
                buildPersistenceUnit(consumer, pUnit, member);
            }

            //
            // @PersistenceContext
            //

            PersistenceContextAnnFactory pcFactory = new PersistenceContextAnnFactory();
            List<PersistenceContext> persistenceContextList = new ArrayList<PersistenceContext>();
            for (Annotated<Class<?>> clazz : annotationFinder.findMetaAnnotatedClasses(PersistenceContexts.class)) {
                PersistenceContexts persistenceContexts = clazz.getAnnotation(PersistenceContexts.class);
                persistenceContextList.addAll(asList(persistenceContexts.value()));
                pcFactory.addAnnotations(clazz.get());
            }
            for (Annotated<Class<?>> clazz : annotationFinder.findMetaAnnotatedClasses(PersistenceContext.class)) {
                PersistenceContext persistenceContext = clazz.getAnnotation(PersistenceContext.class);
                persistenceContextList.add(persistenceContext);
                pcFactory.addAnnotations(clazz.get());

                // dynamic proxy implementation
                if (clazz.get().isInterface()) {
                    Member member = new FilledMember("em", EntityManager.class, clazz.get());
                    buildPersistenceContext(consumer, pcFactory.create(persistenceContext, member), member);
                }
            }
            for (PersistenceContext pCtx : persistenceContextList) {
                buildPersistenceContext(consumer, pcFactory.create(pCtx, null), null);
            }
            for (Annotated<Field> field : annotationFinder.findMetaAnnotatedFields(PersistenceContext.class)) {
                PersistenceContext pCtx = field.getAnnotation(PersistenceContext.class);
                Member member = new FieldMember(field.get());
                buildPersistenceContext(consumer, pcFactory.create(pCtx, member), member);
            }
            for (Annotated<Method> method : annotationFinder.findMetaAnnotatedMethods(PersistenceContext.class)) {
                PersistenceContext pCtx = method.getAnnotation(PersistenceContext.class);
                Member member = new MethodMember(method.get());
                buildPersistenceContext(consumer, pcFactory.create(pCtx, member), member);
            }

            //
            // @DataSourceDefinition
            //

            for (Annotated<Class<?>> annotated : annotationFinder.findMetaAnnotatedClasses(DataSourceDefinitions.class)) {
                DataSourceDefinitions defs = annotated.getAnnotation(DataSourceDefinitions.class);
                for (DataSourceDefinition definition : defs.value()) {
                    buildDataSourceDefinition(consumer, definition);
                }
            }

            for (Annotated<Class<?>> annotated : annotationFinder.findMetaAnnotatedClasses(DataSourceDefinition.class)) {
                DataSourceDefinition definition = annotated.getAnnotation(DataSourceDefinition.class);
                buildDataSourceDefinition(consumer, definition);
            }
        }

        private void buildContext(JndiConsumer consumer, Member member) {
            ResourceRef ref = new ResourceRef();
            ref.setName(member.getDeclaringClass().getName() + "/" + member.getName());

            InjectionTarget target = new InjectionTarget();
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
        private void buildEjbRef(JndiConsumer consumer, EJB ejb, Member member) {

            // TODO: Looks like we aren't looking for an existing ejb-ref or ejb-local-ref
            // we need to do this to support overriding.

            /**
             * Was @EJB used at a class level witout specifying the 'name' or 'beanInterface' attributes?
             */
            String name = consumer.getJndiConsumerName();
            if (member == null) {
                boolean shouldReturn = false;
                if (ejb.name().equals("")) {
                    fail(name, "ejbAnnotation.onClassWithNoName");
                    shouldReturn = true;
                }
                if (ejb.beanInterface().equals(Object.class)) {
                    fail(name, "ejbAnnotation.onClassWithNoBeanInterface");
                    shouldReturn = true;
                }
                if (shouldReturn) return;
            }

            EjbRef ejbRef = new EjbRef();

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
                InjectionTarget target = new InjectionTarget();
                target.setInjectionTargetClass(member.getDeclaringClass().getName());
                target.setInjectionTargetName(member.getName());
                ejbRef.getInjectionTarget().add(target);

            }

            Class<?> interfce = ejb.beanInterface();
            if (interfce.equals(Object.class)) {
                interfce = (member == null) ? null : member.getType();
            }

            boolean localbean = isKnownLocalBean(interfce);
            boolean dynamicallyImplemented = isKnownDynamicallyImplemented(interfce);

            if ((!localbean) && interfce != null && !isValidEjbInterface(name, interfce, ejbRef.getName())) {
                return;
            }

            if (interfce != null && !interfce.equals(Object.class)) {
                if (EJBHome.class.isAssignableFrom(interfce)) {
                    ejbRef.setHome(interfce.getName());
                    Method[] methods = interfce.getMethods();
                    for (Method method : methods) {
                        if (method.getName().startsWith("create")) {
                            ejbRef.setRemote(method.getReturnType().getName());
                            break;
                        }
                    }
                    ejbRef.setRefType(EjbReference.Type.REMOTE);
                } else if (EJBLocalHome.class.isAssignableFrom(interfce)) {
                    ejbRef.setHome(interfce.getName());
                    Method[] methods = interfce.getMethods();
                    for (Method method : methods) {
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
            if (ejbName.equals("")) {
                ejbName = null;
            }
            ejbRef.setEjbLink(ejbName);

            // Set the mappedName, if any
            String mappedName = ejb.mappedName();
            if (mappedName.equals("")) {
                mappedName = null;
            }
            ejbRef.setMappedName(mappedName);

            // Set lookup name, if any
            String lookupName = getLookupName(ejb);
            if (lookupName.equals("")) {
                lookupName = null;
            }
            ejbRef.setLookupName(lookupName);

            Map<String, EjbRef> remoteRefs = consumer.getEjbRefMap();
            if (remoteRefs.containsKey(ejbRef.getName())) {
                EjbRef ref = remoteRefs.get(ejbRef.getName());
                if (ref.getRemote() == null) ref.setRemote(ejbRef.getRemote());
                if (ref.getHome() == null) ref.setHome(ejbRef.getHome());
                if (ref.getMappedName() == null) ref.setMappedName(ejbRef.getMappedName());
                ref.getInjectionTarget().addAll(ejbRef.getInjectionTarget());
                return;
            }

            Map<String, EjbLocalRef> localRefs = consumer.getEjbLocalRefMap();
            if (localRefs.containsKey(ejbRef.getName())) {
                EjbLocalRef ejbLocalRef = new EjbLocalRef(ejbRef);
                EjbLocalRef ref = localRefs.get(ejbLocalRef.getName());
                if (ref.getLocal() == null) ref.setLocal(ejbLocalRef.getLocal());
                if (ref.getLocalHome() == null) ref.setLocalHome(ejbLocalRef.getLocalHome());
                if (ref.getMappedName() == null) ref.setMappedName(ejbLocalRef.getMappedName());
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



        private String normalize(String refName) {
            if (refName.startsWith("java:")) {
                return refName;
            }
            return "java:comp/env/" + refName;
        }

        private boolean isKnownDynamicallyImplemented(Class<?> clazz) {
            return clazz.isInterface() && AnnotationUtil.getAnnotation(PersistenceContext.class, clazz) != null;
        }

        private boolean isKnownLocalBean(Class clazz) {
            if (clazz.isAnnotation()) return false;
            if (clazz.isArray()) return false;
            if (clazz.isEnum()) return false;
            if (clazz.isInterface()) return false;
            if (clazz.isPrimitive()) return false;
            if (Modifier.isAbstract(clazz.getModifiers())) return false;
            if (Modifier.isFinal(clazz.getModifiers())) return false;

            return true;
//            // This limits @LocalBean references to things in the same module
//            DeploymentModule module = getModule();
//            if (module instanceof EjbModule) {
//                Set<String> localbeans = new HashSet<String>();
//                EjbModule ejbModule = (EjbModule) module;
//                for (EnterpriseBean bean : ejbModule.getEjbJar().getEnterpriseBeans()) {
//                    if (bean instanceof SessionBean) {
//                        if (((SessionBean) bean).getLocalBean() != null) {
//                            localbeans.add(bean.getEjbClass());
//                        }
//                    }
//                }
//
//                if (localbeans.contains(clazz.getName())) {
//                    return true;
//                }
//            }
//
//            return false;
        }

        private boolean isValidEjbInterface(String b, Class clazz, String refName) {
            if (!clazz.isInterface()) {

//                DeploymentModule module = getModule();
//                if (module instanceof EjbModule) {
//                    Set<String> beanClasses = new HashSet<String>();
//                    EjbModule ejbModule = (EjbModule) module;
//                    for (EnterpriseBean bean : ejbModule.getEjbJar().getEnterpriseBeans()) {
//                        beanClasses.add(bean.getEjbClass());
//                    }
//
//                    if (beanClasses.contains(clazz.getName())) {
//                        fail(b, "ann.ejb.beanClass", clazz.getName(), refName);
//                    } else {
//                        fail(b, "ann.ejb.notInterface", clazz.getName(), refName);
//                    }
//                } else {
//                    fail(b, "ann.ejb.notInterface", clazz.getName(), refName);
//                }
//
//                return false;

            } else if (EJBObject.class.isAssignableFrom(clazz)) {

                fail(b, "ann.ejb.ejbObject", clazz.getName(), refName);

                return false;

            } else if (EJBLocalObject.class.isAssignableFrom(clazz)) {

                fail(b, "ann.ejb.ejbLocalObject", clazz.getName(), refName);

                return false;
            }

            return true;
        }

        private void fail(String component, String key, Object... details) {
            getValidationContext().fail(component, key, details);
        }

        /**
         * Process @Resource into either <resource-ref> or <resource-env-ref> for the given member (field or method) or class
         *
         * @param consumer
         * @param resource
         * @param member
         */
        private void buildResource(JndiConsumer consumer, Resource resource, Member member) {

            /**
             * Was @Resource used at a class level without specifying the 'name' or 'beanInterface' attributes?
             */
            if (member == null) {
                boolean shouldReturn = false;
                if (resource.name().length() == 0) {
                    fail(consumer.getJndiConsumerName(), "resourceAnnotation.onClassWithNoName");
                    shouldReturn = true;
                }

                if (shouldReturn) return;
            }

            // Get the ref-name
            String refName = resource.name();
            if (refName.equals("")) {
                refName = member.getDeclaringClass().getName() + "/" + member.getName();
            }

            refName = normalize(refName);

            JndiReference reference = consumer.getEnvEntryMap().get(refName);
            if (reference == null) {

                /**
                 * Was @Resource mistakenly used when either @PersistenceContext or @PersistenceUnit should have been used?
                 */
                if (member != null) { // Little quick validation for common mistake
                    Class type = member.getType();
                    boolean shouldReturn = false;
                    if (EntityManager.class.isAssignableFrom(type)) {
                        fail(consumer.getJndiConsumerName(), "resourceRef.onEntityManager", refName);
                        shouldReturn = true;
                    } else if (EntityManagerFactory.class.isAssignableFrom(type)) {
                        fail(consumer.getJndiConsumerName(), "resourceRef.onEntityManagerFactory", refName);
                        shouldReturn = true;
                    }
                    if (shouldReturn) return;
                }

                Class type;
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

                    if (resourceEnvRef.getResourceEnvRefType() == null || ("").equals(resourceEnvRef.getResourceEnvRefType())) {
                        resourceEnvRef.setResourceEnvRefType(type.getName());
                    }
                    reference = resourceEnvRef;
                } else if (isKnownEnvironmentEntryType(type)) {
                    /*
                     * @Resource <env-entry>
                     *
                     * Add an env-entry via @Resource if 'lookup' attribute is set.
                     */
                    String lookupName = getLookupName(resource);
                    if (!lookupName.equals("")) {
                        EnvEntry envEntry = new EnvEntry();
                        envEntry.setName(refName);
                        consumer.getEnvEntry().add(envEntry);

                        envEntry.setLookupName(lookupName);

                        reference = envEntry;
                    } else if(isShareableJNDINamespace(refName)){
                        EnvEntry envEntry = new EnvEntry();
                        envEntry.setName(refName);
                        consumer.getEnvEntry().add(envEntry);
                        reference = envEntry;
                    } else {

                        final String shortName = normalize(member.getName());
                        reference = consumer.getEnvEntryMap().get(shortName);

                        if (reference == null) {
                            EnvEntry envEntry = new EnvEntry();
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

                    if (resourceRef.getResAuth() == null) {
                        if (resource.authenticationType() == Resource.AuthenticationType.APPLICATION) {
                            resourceRef.setResAuth(ResAuth.APPLICATION);
                        } else {
                            resourceRef.setResAuth(ResAuth.CONTAINER);
                        }
                    }

                    if (resourceRef.getResType() == null || ("").equals(resourceRef.getResType())) {
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
                InjectionTarget target = new InjectionTarget();
                target.setInjectionTargetClass(member.getDeclaringClass().getName());
                target.setInjectionTargetName(member.getName());
                reference.getInjectionTarget().add(target);
            }

            // Override the mapped name if not set
            if (reference.getMappedName() == null && !resource.mappedName().equals("")) {
                reference.setMappedName(resource.mappedName());
            }

            // Override the lookup name if not set
            if (reference.getLookupName() == null) {
                String lookupName = getLookupName(resource);
                if (!lookupName.equals("")) {
                    reference.setLookupName(lookupName);
                }
            }
        }

        private static Method getLookupMethod(Class cls) {
            try {
                return cls.getMethod("lookup", null);
            } catch (NoSuchMethodException e) {
                logger.error("lookup method is not available for " + cls.getName()
                    + ". You probably have an old API -" + getSourceIfExists(cls) + "- in the classpath. ");
                return null;
            }
        }

        private static String getSourceIfExists(Class<?> cls) {
            if (cls.getProtectionDomain() != null && cls.getProtectionDomain().getCodeSource() != null
                && cls.getProtectionDomain().getCodeSource().getLocation() != null) {
                return cls.getProtectionDomain().getCodeSource().getLocation().toString();
            }
            return "";
        }

        private static String getLookupName(Resource resource) {
            String value = "";
            Method lookupMethod = getLookupMethod(Resource.class);
            if (lookupMethod != null) {
                try {
                    value = (String) lookupMethod.invoke(resource, null);
                } catch (Exception e) {
                    // ignore
                }
            }
            return value;
        }


        private static String getLookupName(EJB ejb) {
            String value = "";
            Method lookupMethod = getLookupMethod(EJB.class);
            if (lookupMethod != null) {
                try {
                    value = (String) lookupMethod.invoke(ejb, null);
                } catch (Exception e) {
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
        private void buildPersistenceUnit(JndiConsumer consumer, PersistenceUnit persistenceUnit, Member member) throws OpenEJBException {
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
                Class type = member.getType();
                if (EntityManager.class.isAssignableFrom(type)) {
                    /**
                     * Was @PersistenceUnit mistakenly used when @PersistenceContext should have been used?
                     */
                    ValidationContext validationContext = getValidationContext();
                    String jndiConsumerName = consumer.getJndiConsumerName();
                    String name = persistenceUnitRef.getName();
                    validationContext.fail(jndiConsumerName, "persistenceUnitAnnotation.onEntityManager", name);
                } else if (!EntityManagerFactory.class.isAssignableFrom(type)) {
                    /**
                     * Was @PersistenceUnit mistakenly used for something that isn't an EntityManagerFactory?
                     */
                    fail(consumer.getJndiConsumerName(), "persistenceUnitAnnotation.onNonEntityManagerFactory", persistenceUnitRef.getName());
                } else {
                    // Set the member name where this will be injected
                    InjectionTarget target = new InjectionTarget();
                    target.setInjectionTargetClass(member.getDeclaringClass().getName());
                    target.setInjectionTargetName(member.getName());
                    persistenceUnitRef.getInjectionTarget().add(target);
                }
            }

            if (persistenceUnitRef.getPersistenceUnitName() == null && !persistenceUnit.unitName().equals("")) {
                persistenceUnitRef.setPersistenceUnitName(persistenceUnit.unitName());
            }
        }

        /**
         * Process @PersistenceContext into <persistence-context> for the specified member (field or method)
         * <p/>
         * Refer 16.11.2.1 Overriding Rules of EJB Core Spec for overriding rules
         *
         * @param consumer
         * @param persistenceContext
         * @param member
         * @throws OpenEJBException
         */
        private void buildPersistenceContext(JndiConsumer consumer, PersistenceContextAnn persistenceContext, Member member) throws OpenEJBException {
            String refName = persistenceContext.name();

            if (refName.length() ==0) {
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
                persistenceContextRef.setPersistenceUnitName(persistenceContext.unitName());
                persistenceContextRef.setPersistenceContextRefName(refName);
                if ("EXTENDED".equalsIgnoreCase(persistenceContext.type())) {
                    persistenceContextRef.setPersistenceContextType(PersistenceContextType.EXTENDED);
                } else {
                    persistenceContextRef.setPersistenceContextType(PersistenceContextType.TRANSACTION);
                }
                consumer.getPersistenceContextRef().add(persistenceContextRef);
            } else {
                if (persistenceContextRef.getPersistenceUnitName() == null || ("").equals(persistenceContextRef.getPersistenceUnitName())) {
                    persistenceContextRef.setPersistenceUnitName(persistenceContext.unitName());
                }
                if (persistenceContextRef.getPersistenceContextType() == null || ("").equals(persistenceContextRef.getPersistenceContextType())) {
                    if ("EXTENDED".equalsIgnoreCase(persistenceContext.type())) {
                        persistenceContextRef.setPersistenceContextType(PersistenceContextType.EXTENDED);
                    } else {
                        persistenceContextRef.setPersistenceContextType(PersistenceContextType.TRANSACTION);
                    }
                }
            }

            List<Property> persistenceProperties = persistenceContextRef.getPersistenceProperty();
            if (persistenceProperties == null) {
                persistenceProperties = new ArrayList<Property>();
                persistenceContextRef.setPersistenceProperty(persistenceProperties);
            }

            for (Map.Entry<String, String> persistenceProperty : persistenceContext.properties().entrySet()) {
                boolean flag = true;
                for (Property prpty : persistenceProperties) {
                    if (prpty.getName().equals(persistenceProperty.getKey())) {
                        flag = false;
                        break;
                    }
                }
                if (flag) {
                    Property property = new Property();
                    property.setName(persistenceProperty.getKey());
                    property.setValue(persistenceProperty.getValue());
                    persistenceProperties.add(property);
                }
            }

            if (member != null) {
                Class type = member.getType();
                if (EntityManagerFactory.class.isAssignableFrom(type)) {
                    /**
                     * Was @PersistenceContext mistakenly used when @PersistenceUnit should have been used?
                     */
                    fail(consumer.getJndiConsumerName(), "persistenceContextAnnotation.onEntityManagerFactory", persistenceContextRef.getName());
                } else if (!EntityManager.class.isAssignableFrom(type)) {
                    /**
                     * Was @PersistenceContext mistakenly used for something that isn't an EntityManager?
                     */
                    fail(consumer.getJndiConsumerName(), "persistenceContextAnnotation.onNonEntityManager", persistenceContextRef.getName());
                } else {
                    // Set the member name where this will be injected
                    InjectionTarget target = new InjectionTarget();
                    target.setInjectionTargetClass(member.getDeclaringClass().getName());
                    target.setInjectionTargetName(member.getName());
                    persistenceContextRef.getInjectionTarget().add(target);
                }
            }
        }

        private void buildDataSourceDefinition(JndiConsumer consumer, DataSourceDefinition d) {
            final org.apache.openejb.jee.DataSource dataSource = new org.apache.openejb.jee.DataSource();

            dataSource.setName(d.name());
            dataSource.setClassName(d.className());
            dataSource.setTransactional(d.transactional());

            final DataSource existing = consumer.getDataSourceMap().get(dataSource.getKey());

            if (existing != null) return;

            // Optional properties
            if (!d.databaseName().isEmpty()) dataSource.setDatabaseName(d.databaseName());
            if (d.initialPoolSize() != -1) dataSource.setInitialPoolSize(d.initialPoolSize());
            if (d.isolationLevel() != -1) dataSource.setIsolationLevel(IsolationLevel.fromFlag(d.isolationLevel()));
            if (d.loginTimeout() != 0) dataSource.setLoginTimeout(d.loginTimeout());
            if (d.maxIdleTime() != -1) dataSource.setMaxIdleTime(d.maxIdleTime());
            if (d.maxPoolSize() != -1) dataSource.setMaxPoolSize(d.maxPoolSize());
            if (d.maxStatements() != -1) dataSource.setMaxStatements(d.maxStatements());
            if (d.minPoolSize() != -1) dataSource.setMinPoolSize(d.minPoolSize());
            if (!d.password().isEmpty()) dataSource.setPassword(d.password());
            if (d.portNumber() != -1) dataSource.setPortNumber(d.portNumber());
            if (!"localhost".equals(d.serverName())) dataSource.setServerName(d.serverName());
            if (!d.url().isEmpty()) dataSource.setUrl(d.url());
            if (!d.user().isEmpty()) dataSource.setUser(d.user());

            for (String s : d.properties()) {
                final String key = s.substring(0, s.indexOf('='));
                final String value = s.substring(s.indexOf('='));

                dataSource.property(key, value);
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
        private void buildWebServiceRef(JndiConsumer consumer, WebServiceRef webService, HandlerChain handlerChain, Member member, ClassLoader classLoader) throws OpenEJBException {

            ServiceRef serviceRef;

            String refName = webService.name();
            if (refName.equals("")) {
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
                InjectionTarget target = new InjectionTarget();
                target.setInjectionTargetClass(member.getDeclaringClass().getName());
                target.setInjectionTargetName(member.getName());
                serviceRef.getInjectionTarget().add(target);
            }

            // Set service interface
            Class<?> serviceInterface = null;
            if (serviceRef.getServiceInterface() == null) {
                serviceInterface = webService.type();
                if (serviceInterface.equals(Object.class)) {
                    if (member != null) {
                        serviceInterface = member.getType();
                    } else {
                        serviceInterface = webService.value();
                    }
                }
            }
            if (serviceInterface == null || !serviceInterface.isAssignableFrom(Service.class)) {
                serviceInterface = Service.class;
            }
            serviceRef.setServiceInterface(serviceInterface.getName());

            // reference type
            if (serviceRef.getServiceRefType() == null || ("").equals(serviceRef.getServiceRefType())) {
                if (webService.type() != java.lang.Object.class) {
                    serviceRef.setServiceRefType(webService.type().getName());
                } else {
                    serviceRef.setServiceRefType(member.getType().getName());
                }
            }
            Class<?> refType = null;
            try {
                refType = classLoader.loadClass(serviceRef.getType());
            } catch (ClassNotFoundException e) {
            }

            // Set the mappedName
            if (serviceRef.getMappedName() == null) {
                String mappedName = webService.mappedName();
                if (mappedName.equals("")) {
                    mappedName = null;
                }
                serviceRef.setMappedName(mappedName);
            }

            // wsdl file
            if (serviceRef.getWsdlFile() == null) {
                String wsdlLocation = webService.wsdlLocation();
                if (!wsdlLocation.equals("")) {
                    serviceRef.setWsdlFile(wsdlLocation);
                }
            }

            if (SystemInstance.get().hasProperty("openejb.geronimo")) return;

            if (serviceRef.getWsdlFile() == null && refType != null) {
                serviceRef.setWsdlFile(JaxWsUtils.getServiceWsdlLocation(refType, classLoader));
            }
            if (serviceRef.getWsdlFile() == null && serviceInterface != null) {
                serviceRef.setWsdlFile(JaxWsUtils.getServiceWsdlLocation(serviceInterface, classLoader));
            }

            // service qname
            if (serviceRef.getServiceQname() == null && refType != null) {
                serviceRef.setServiceQname(JaxWsUtils.getServiceQName(refType));
            }
            if (serviceRef.getServiceQname() == null && serviceInterface != null) {
                serviceRef.setServiceQname(JaxWsUtils.getServiceQName(serviceInterface));
            }

            // handlers
            if (serviceRef.getHandlerChains() == null && handlerChain != null) {
                try {
                    URL handlerFileURL = member.getDeclaringClass().getResource(handlerChain.file());
                    HandlerChains handlerChains = ReadDescriptors.readHandlerChains(handlerFileURL);
                    serviceRef.setHandlerChains(handlerChains);
                } catch (Throwable e) {
                    throw new OpenEJBException("Unable to load handler chain file: " + handlerChain.file(), e);
                }
            }
        }

        /**
         * Scan for @EJB, @Resource, @WebServiceRef, @PersistenceUnit, and @PersistenceContext on WebService HandlerChain classes
         */
        private void processWebServiceHandlers(EjbModule ejbModule, EnterpriseBean bean) throws OpenEJBException {
            // add webservice handler classes to the class finder used in annotation processing
            Set<Class<?>> classes = new HashSet<Class<?>>();
            if (ejbModule.getWebservices() != null) {
                for (WebserviceDescription webservice : ejbModule.getWebservices().getWebserviceDescription()) {
                    for (PortComponent port : webservice.getPortComponent()) {
                        // only process port definitions for this ejb
                        if (!bean.getEjbName().equals(port.getServiceImplBean().getEjbLink())) continue;

                        if (port.getHandlerChains() == null) continue;
                        for (org.apache.openejb.jee.HandlerChain handlerChain : port.getHandlerChains().getHandlerChain()) {
                            for (Handler handler : handlerChain.getHandler()) {
                                String handlerClass = handler.getHandlerClass();
                                if (handlerClass != null) {
                                    try {
                                        Class handlerClazz = ejbModule.getClassLoader().loadClass(handlerClass);
                                        classes.add(handlerClazz);
                                    } catch (ClassNotFoundException e) {
                                        throw new OpenEJBException("Unable to load webservice handler class: " + handlerClass, e);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            AnnotationFinder handlersFinder = createFinder(classes.toArray(new Class<?>[classes.size()]));
            buildAnnotatedRefs(bean, handlersFinder, ejbModule.getClassLoader());
        }

        /**
         * Scan for @EJB, @Resource, @WebServiceRef, @PersistenceUnit, and @PersistenceContext on WebService HandlerChain classes
         *
         * @param consumer
         * @param classLoader
         * @throws OpenEJBException
         */
        private void processWebServiceClientHandlers(JndiConsumer consumer, ClassLoader classLoader) throws OpenEJBException {
            if (SystemInstance.get().hasProperty("openejb.geronimo")) return;

            final Set<Class<?>> processedClasses = new HashSet<Class<?>>();
            final Set<Class<?>> handlerClasses = new HashSet<Class<?>>();
            do {
                // get unprocessed handler classes
                handlerClasses.clear();
                for (ServiceRef serviceRef : consumer.getServiceRef()) {
                    HandlerChains chains = serviceRef.getAllHandlers();
                    if (chains == null) continue;
                    for (org.apache.openejb.jee.HandlerChain handlerChain : chains.getHandlerChain()) {
                        for (Handler handler : handlerChain.getHandler()) {
                            if (handler.getHandlerClass() != null) {
                                try {
                                    Class clazz = classLoader.loadClass(handler.getHandlerClass());
                                    handlerClasses.add(clazz);
                                } catch (ClassNotFoundException e) {
                                    throw new OpenEJBException("Unable to load webservice handler class: " + handler.getHandlerClass(), e);
                                }
                            }
                        }
                    }
                }
                handlerClasses.removeAll(processedClasses);

                // process handler classes
                final AnnotationFinder handlerAnnotationFinder = createFinder(handlerClasses.toArray(new Class<?>[handlerClasses.size()]));

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


        private List<String> getDeclaredClassPermissions(AssemblyDescriptor assemblyDescriptor, String ejbName) {
            List<MethodPermission> permissions = assemblyDescriptor.getMethodPermission();
            List<String> classPermissions = new ArrayList<String>();
            for (MethodPermission permission : permissions) {
                for (org.apache.openejb.jee.Method method : permission.getMethod()) {
                    if (!method.getEjbName().equals(ejbName)) continue;
                    if (!"*".equals(method.getMethodName())) continue;

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
            public Class<A> getAnnotationClass();

            public Map<String, List<MethodAttribute>> getExistingDeclarations();

            public void addClassLevelDeclaration(A annotation, Class clazz);

            public void addMethodLevelDeclaration(A annotation, Method method);
        }

        public static class TransactionAttributeHandler implements AnnotationHandler<TransactionAttribute> {

            private final AssemblyDescriptor assemblyDescriptor;
            private final String ejbName;

            public TransactionAttributeHandler(AssemblyDescriptor assemblyDescriptor, String ejbName) {
                this.assemblyDescriptor = assemblyDescriptor;
                this.ejbName = ejbName;
            }

            public Map<String, List<MethodAttribute>> getExistingDeclarations() {
                return assemblyDescriptor.getMethodTransactionMap(ejbName);
            }

            public void addClassLevelDeclaration(TransactionAttribute attribute, Class type) {
                ContainerTransaction ctx = new ContainerTransaction(cast(attribute.value()), type.getName(), ejbName, "*");
                assemblyDescriptor.getContainerTransaction().add(ctx);
            }

            public void addMethodLevelDeclaration(TransactionAttribute attribute, Method method) {
                ContainerTransaction ctx = new ContainerTransaction(cast(attribute.value()), ejbName, method);
                assemblyDescriptor.getContainerTransaction().add(ctx);
            }

            public Class<TransactionAttribute> getAnnotationClass() {
                return TransactionAttribute.class;
            }

            private TransAttribute cast(TransactionAttributeType transactionAttributeType) {
                return TransAttribute.valueOf(transactionAttributeType.toString());
            }
        }

        private static class ConcurrentMethodHandler {

            protected final AssemblyDescriptor assemblyDescriptor;
            protected final SessionBean bean;
            protected final Map<Object, ContainerConcurrency> methods;

            public ConcurrentMethodHandler(AssemblyDescriptor assemblyDescriptor,
                                           SessionBean bean,
                                           Map<Object, ContainerConcurrency> methods) {
                this.assemblyDescriptor = assemblyDescriptor;
                this.bean = bean;
                this.methods = methods;
            }

            public Map<String, List<MethodAttribute>> getExistingDeclarations() {
                Map<String, List<MethodAttribute>> declarations = new HashMap<String, List<MethodAttribute>>();
                List<ConcurrentMethod> methods = bean.getConcurrentMethod();
                for (ConcurrentMethod method : methods) {
                    List<MethodAttribute> list = declarations.get(method.getMethod().getMethodName());
                    if (list == null) {
                        list = new ArrayList<MethodAttribute>();
                        declarations.put(method.getMethod().getMethodName(), list);
                    }
                    list.add(new MethodAttribute(null, bean.getEjbName(), method.getMethod()));
                }
                return declarations;
            }

            public ContainerConcurrency getContainerConcurrency(Method method) {
                ContainerConcurrency concurrency = methods.get(method);
                if (concurrency == null) {
                    concurrency = new ContainerConcurrency(null, bean.getEjbName(), method);
                    methods.put(method, concurrency);
                    assemblyDescriptor.getContainerConcurrency().add(concurrency);
                }
                return concurrency;
            }

            public ContainerConcurrency getContainerConcurrency(Class clazz) {
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

            public LockHandler(AssemblyDescriptor assemblyDescriptor,
                               SessionBean bean) {
                this(assemblyDescriptor, bean, new HashMap<Object, ContainerConcurrency>());
            }

            public LockHandler(AssemblyDescriptor assemblyDescriptor,
                               SessionBean bean,
                               Map<Object, ContainerConcurrency> methods) {
                super(assemblyDescriptor, bean, methods);
            }

            public void addClassLevelDeclaration(Lock attribute, Class type) {
                ContainerConcurrency concurrency = getContainerConcurrency(type);
                concurrency.setLock(toLock(attribute));
            }

            public void addMethodLevelDeclaration(Lock attribute, Method method) {
                ContainerConcurrency concurrency = getContainerConcurrency(method);
                concurrency.setLock(toLock(attribute));
            }

            private ConcurrentLockType toLock(Lock annotation) {
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

            public AccessTimeoutHandler(AssemblyDescriptor assemblyDescriptor,
                                        SessionBean bean) {
                this(assemblyDescriptor, bean, new HashMap<Object, ContainerConcurrency>());
            }

            public AccessTimeoutHandler(AssemblyDescriptor assemblyDescriptor,
                                        SessionBean bean,
                                        Map<Object, ContainerConcurrency> methods) {
                super(assemblyDescriptor, bean, methods);
            }

            public void addClassLevelDeclaration(AccessTimeout attribute, Class type) {
                ContainerConcurrency concurrency = getContainerConcurrency(type);
                concurrency.setAccessTimeout(toTimeout(attribute));
            }

            public void addMethodLevelDeclaration(AccessTimeout attribute, Method method) {
                ContainerConcurrency concurrency = getContainerConcurrency(method);
                concurrency.setAccessTimeout(toTimeout(attribute));
            }

            private Timeout toTimeout(AccessTimeout annotation) {
                Timeout timeout = new Timeout();
                timeout.setTimeout(annotation.value());
                timeout.setUnit(annotation.unit());
                return timeout;
            }

            public Class<AccessTimeout> getAnnotationClass() {
                return AccessTimeout.class;
            }

        }

        private <A extends Annotation> void checkAttributes(AnnotationHandler<A> handler, String ejbName, EjbModule ejbModule, AnnotationFinder annotationFinder, String messageKey) {
            Map<String, List<MethodAttribute>> existingDeclarations = handler.getExistingDeclarations();

            int xml = 0;
            for (List<MethodAttribute> methodAttributes : existingDeclarations.values()) {
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

        private <A extends Annotation> void processAttributes(AnnotationHandler<A> handler, Class<?> clazz, AnnotationFinder annotationFinder) {
            Map<String, List<MethodAttribute>> existingDeclarations = handler.getExistingDeclarations();

            // SET THE DEFAULT
            final Class<A> annotationClass = handler.getAnnotationClass();

            List<Annotated<Class<?>>> types = sortClasses(annotationFinder.findMetaAnnotatedClasses(annotationClass));
            if (!hasMethodAttribute("*", null, existingDeclarations)) {
                for (Annotated<Class<?>> type : types) {
                    if (!type.get().isAssignableFrom(clazz)) continue;
                    if (!hasMethodAttribute("*", type.get(), existingDeclarations)) {
                        A attribute = type.getAnnotation(annotationClass);
                        if (attribute != null) {
                            handler.addClassLevelDeclaration(attribute, type.get());
                        }
                    }
                }
            }

            List<Annotated<Method>> methods = annotationFinder.findMetaAnnotatedMethods(annotationClass);
            for (Annotated<Method> method : methods) {
                A attribute = method.getAnnotation(annotationClass);
                if (!existingDeclarations.containsKey(method.get().getName())) {
                    // no method with this name in descriptor
                    handler.addMethodLevelDeclaration(attribute, method.get());
                } else {
                    // method name already declared
                    List<MethodAttribute> list = existingDeclarations.get(method.get().getName());
                    for (MethodAttribute mtx : list) {
                        MethodParams methodParams = mtx.getMethodParams();
                        if (methodParams == null) {
                            // params not specified, so this is more specific
                            handler.addMethodLevelDeclaration(attribute, method.get());
                        } else {
                            List<String> params1 = methodParams.getMethodParam();
                            String[] params2 = asStrings(method.get().getParameterTypes());
                            if (params1.size() != params2.length) {
                                // params not the same
                                handler.addMethodLevelDeclaration(attribute, method.get());
                            } else {
                                for (int i = 0; i < params1.size(); i++) {
                                    String a = params1.get(i);
                                    String b = params2[i];
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

        private boolean hasMethodAttribute(String methodName, Class clazz, Map<String, List<MethodAttribute>> map) {
            return getMethodAttribute(methodName, clazz, map) != null;
        }

        private MethodAttribute getMethodAttribute(String methodName, Class clazz, Map<String, List<MethodAttribute>> map) {
            List<MethodAttribute> methodAttributes = map.get(methodName);
            if (methodAttributes == null) return null;

            for (MethodAttribute methodAttribute : methodAttributes) {
                String className = (clazz != null) ? clazz.getName() : null + "";

                if (className.equals(methodAttribute.getClassName() + "")) {
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
        private <A extends Annotation> A getInheritableAnnotation(Class clazz, Class<A> annotationClass) {
            if (clazz == null || clazz.equals(Object.class)) return null;

            Annotation annotation = clazz.getAnnotation(annotationClass);
            if (annotation != null) {
                return (A) annotation;
            }

            return getInheritableAnnotation(clazz.getSuperclass(), annotationClass);
        }

        /**
         * Creates a list of the specified class and all its parent classes
         *
         * @param clazz
         * @return
         */
        private List<Class<?>> ancestors(Class clazz) {
            ArrayList<Class<?>> ancestors = new ArrayList<Class<?>>();

            while (clazz != null && !clazz.equals(Object.class)) {
                ancestors.add(clazz);
                clazz = clazz.getSuperclass();
            }

            return ancestors;
        }

        /**
         * Creates a list of the specified class and all its parent
         * classes then creates a AnnotationFinder from that list which
         * can be used for easy annotation scanning.
         *
         * @param classes
         * @return
         */
        private AnnotationFinder createFinder(Class<?>... classes) {
            Set<Class<?>> parents = new HashSet<Class<?>>();
            for (Class<?> clazz : classes) {
                parents.addAll(ancestors(clazz));
            }

            return new AnnotationFinder(new ClassesArchive(parents)).link();
        }

        /**
         * Copy lists for iteration avoiding ConcurrentModificationException
         *
         * @param classes
         * @return
         */
        private List<Class<?>> copy(List<Class<?>> classes) {
            return new ArrayList<Class<?>>(classes);
        }

        /**
         * Converts an array of classes to an array of class name strings
         *
         * @param types
         * @return
         */
        private String[] asStrings(Class[] types) {
            List<String> names = new ArrayList<String>();
            for (Class clazz : types) {
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
        private <T> T getFirst(List<T> list) {
            if (list.size() > 0) {
                return list.get(0);
            }
            return null;
        }

        private boolean getFirstt(List<?> list) {
            return list.size() > 0;
        }


        /**
         * Remote interface validation
         *
         * @param interfce
         * @param validation
         * @param ejbName
         * @return
         */
        private boolean validateRemoteInterface(Class interfce, ValidationContext validation, String ejbName) {
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
        private boolean validateLocalInterface(Class interfce, ValidationContext validation, String ejbName) {
            return isValidInterface(interfce, validation, ejbName, "Local");
        }

        /**
         * Checks that the values specified via @Local and @Remote are *not*:
         * <p/>
         * - classes
         * - derived from javax.ejb.EJBObject
         * - derived from javax.ejb.EJBHome
         * - derived from javax.ejb.EJBLocalObject
         * - derived from javax.ejb.EJBLocalHome
         *
         * @param interfce
         * @param validation
         * @param ejbName
         * @param annotationName
         * @return
         */
        private boolean isValidInterface(Class interfce, ValidationContext validation, String ejbName, String annotationName) {
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

    /**
     * Small utility interface used to allow polymorphing
     * of java.lang.reflect.Method and java.lang.reflect.Field
     * so that each can be treated as injection targets using
     * the same code.
     */
    public static interface Member {
        Class<?> getDeclaringClass();

        String getName();

        Class<?> getType();
    }

    public static class FilledMember implements Member {
        private String name;
        private Class<?> type;
        private Class<?> declaringClass;

        public FilledMember(String name, Class<?> type, Class<?> declaringClass) {
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

        public MethodMember(Method method) {
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
            StringBuilder name = new StringBuilder(setter.getName());

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

        public FieldMember(Field field) {
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

    public static List<Annotated<Class<?>>> sortClasses(List<Annotated<Class<?>>> list) {
        Collections.sort(list, new Comparator<Annotated<Class<?>>>() {
            @Override
            public int compare(Annotated<Class<?>> o1, Annotated<Class<?>> o2) {
                return compareClasses(o1.get(), o2.get());
            }
        });
        return list;
    }

    public static List<Class<?>> sortClassesParentFirst(List<Class<?>> list) {
        Collections.sort(list, new Comparator<Class<?>>() {
            @Override
            public int compare(Class<?> o1, Class<?> o2) {
                return compareClasses(o2, o1);
            }
        });
        return list;
    }

    public static List<Annotated<Method>> sortMethods(List<Annotated<Method>> list) {
        Collections.sort(list, new Comparator<Annotated<Method>>() {
            @Override
            public int compare(Annotated<Method> o1, Annotated<Method> o2) {
                return compareClasses(o1.get().getDeclaringClass(), o2.get().getDeclaringClass());
            }
        });
        return list;
    }

    private static int compareClasses(Class<?> a, Class<?> b) {
        if (a == b) return 0;
        if (a.isAssignableFrom(b)) return 1;
        if (b.isAssignableFrom(a)) return -1;

        return 0;
    }

    private static Collection<String> findRestClasses(WebModule webModule, IAnnotationFinder finder) {
        Collection<String> classes = new HashSet<String>();

        // annotations on classes
        List<Annotated<Class<?>>> annotatedClasses = finder.findMetaAnnotatedClasses(Path.class);
        for (Annotated<Class<?>> aClazz : annotatedClasses) {
            Class<?> clazz = aClazz.get();
            int modifiers = clazz.getModifiers();
            if (!Modifier.isAbstract(modifiers)) {
                if (!isEJB(clazz)) {
                    classes.add(clazz.getName());
                } else {
                    webModule.getEjbRestServices().add(clazz.getName());
                }
            }
        }

        // methods annotations: inheritance is managed like it to be more efficient
        List<Annotated<Method>> methods = finder.findMetaAnnotatedMethods(Path.class);
        for (Annotated<Method> aMethod : methods) {
            Method method = aMethod.get();
            Class<?> clazz = method.getDeclaringClass();
            int modifiers = clazz.getModifiers();
            if (!Modifier.isAbstract(modifiers)) {
                if (!isEJB(clazz)) {
                    classes.add(clazz.getName());
                } else {
                    webModule.getEjbRestServices().add(clazz.getName());
                }
            }
        }

        return classes;
    }

    private static boolean isEJB(Class<?> clazz) {
        return clazz.isAnnotationPresent(Stateless.class)
            || clazz.isAnnotationPresent(Singleton.class)
            || clazz.isAnnotationPresent(Stateful.class); // what a weird idea!
    }
}
