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
package org.apache.openejb.config;


import static java.lang.reflect.Modifier.isAbstract;
import static java.util.Arrays.asList;
import static org.apache.openejb.util.Join.join;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

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
import javax.ejb.AccessTimeout;
import javax.ejb.AfterBegin;
import javax.ejb.AfterCompletion;
import javax.ejb.ApplicationException;
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
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceProvider;
import javax.xml.ws.WebServiceRef;
import javax.xml.ws.WebServiceRefs;

import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.api.LocalClient;
import org.apache.openejb.api.RemoteClient;
import org.apache.openejb.core.webservices.JaxWsUtils;
import org.apache.openejb.jee.ActivationConfig;
import org.apache.openejb.jee.ApplicationClient;
import org.apache.openejb.jee.AroundInvoke;
import org.apache.openejb.jee.AroundTimeout;
import org.apache.openejb.jee.AssemblyDescriptor;
import org.apache.openejb.jee.ConcurrencyManagementType;
import org.apache.openejb.jee.ConcurrentLockType;
import org.apache.openejb.jee.ContainerConcurrency;
import org.apache.openejb.jee.ContainerTransaction;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.EjbLocalRef;
import org.apache.openejb.jee.EjbRef;
import org.apache.openejb.jee.EjbReference;
import org.apache.openejb.jee.Empty;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.ExcludeList;
import org.apache.openejb.jee.FacesConfig;
import org.apache.openejb.jee.FacesManagedBean;
import org.apache.openejb.jee.Filter;
import org.apache.openejb.jee.Handler;
import org.apache.openejb.jee.HandlerChains;
import org.apache.openejb.jee.InitMethod;
import org.apache.openejb.jee.InjectionTarget;
import org.apache.openejb.jee.Interceptor;
import org.apache.openejb.jee.InterceptorBinding;
import org.apache.openejb.jee.JndiConsumer;
import org.apache.openejb.jee.JndiReference;
import org.apache.openejb.jee.Lifecycle;
import org.apache.openejb.jee.LifecycleCallback;
import org.apache.openejb.jee.Listener;
import org.apache.openejb.jee.MessageDrivenBean;
import org.apache.openejb.jee.MethodAttribute;
import org.apache.openejb.jee.MethodParams;
import org.apache.openejb.jee.MethodPermission;
import org.apache.openejb.jee.MethodSchedule;
import org.apache.openejb.jee.NamedMethod;
import org.apache.openejb.jee.PersistenceContextRef;
import org.apache.openejb.jee.PersistenceContextType;
import org.apache.openejb.jee.PersistenceUnitRef;
import org.apache.openejb.jee.PortComponent;
import org.apache.openejb.jee.Property;
import org.apache.openejb.jee.RemoteBean;
import org.apache.openejb.jee.RemoveMethod;
import org.apache.openejb.jee.ResAuth;
import org.apache.openejb.jee.ResSharingScope;
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
import org.apache.openejb.jee.TimeUnitType;
import org.apache.openejb.jee.Timeout;
import org.apache.openejb.jee.TimerConsumer;
import org.apache.openejb.jee.TimerSchedule;
import org.apache.openejb.jee.TldTaglib;
import org.apache.openejb.jee.TransAttribute;
import org.apache.openejb.jee.TransactionType;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.jee.WebserviceDescription;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.xbean.finder.AbstractFinder;
import org.apache.xbean.finder.ClassFinder;

/**
 * @version $Rev$ $Date$
 */
public class AnnotationDeployer implements DynamicDeployer {
    public static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP, AnnotationDeployer.class.getPackage().getName());
    public static final Logger startupLogger = Logger.getInstance(LogCategory.OPENEJB_STARTUP_CONFIG, "org.apache.openejb.util.resources");
    private static final ThreadLocal<DeploymentModule> currentModule = new ThreadLocal<DeploymentModule>();

    private final DiscoverAnnotatedBeans discoverAnnotatedBeans;
    private final ProcessAnnotatedBeans processAnnotatedBeans;
    private final EnvEntriesPropertiesDeployer envEntriesPropertiesDeployer;

    public AnnotationDeployer() {
        discoverAnnotatedBeans = new DiscoverAnnotatedBeans();
        processAnnotatedBeans = new ProcessAnnotatedBeans();
        envEntriesPropertiesDeployer = new EnvEntriesPropertiesDeployer();
    }

    public AppModule deploy(AppModule appModule) throws OpenEJBException {
        setModule(appModule);
        try {
            appModule = discoverAnnotatedBeans.deploy(appModule);
            appModule = envEntriesPropertiesDeployer.deploy(appModule);
            appModule = processAnnotatedBeans.deploy(appModule);
            return appModule;
        } finally {
            removeModule();
        }
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

    public static class DiscoverAnnotatedBeans implements DynamicDeployer {

        public static final Set<String> knownResourceEnvTypes = new TreeSet<String>(asList(
                "javax.ejb.SessionContext",
                "javax.ejb.EntityContext",
                "javax.ejb.MessageDrivenContext",
                "javax.transaction.UserTransaction",
                "javax.jms.Queue",
                "javax.jms.Topic",
                "javax.xml.ws.WebServiceContext",
                "javax.ejb.TimerService"
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
                "java.lang.String"
        ));

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
            for (ConnectorModule connectorModule : appModule.getResourceModules()) {
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

            if (clientModule.getApplicationClient() == null){
                clientModule.setApplicationClient(new ApplicationClient());
            }

            // Lots of jars have main classes so this might not even be an app client.
            // We're not going to scrape it for @LocalClient or @RemoteClient annotations
            // unless they flag us specifically by adding a META-INF/application-client.xml
            //
            // ClientModules that already have a ClassFinder have been generated automatically
            // from an EjbModule, so we don't skip those ever.
            if (clientModule.getFinder() == null && clientModule.getAltDDs().containsKey("application-client.xml"))

            if (clientModule.getApplicationClient() != null && clientModule.getApplicationClient().isMetadataComplete()) return clientModule;


            AbstractFinder finder = clientModule.getFinder();

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
            for (Class<?> clazz : finder.findAnnotatedClasses(LocalClient.class)) {
                clientModule.getLocalClients().add(clazz.getName());
            }

            for (Class<?> clazz : finder.findAnnotatedClasses(RemoteClient.class)) {
                clientModule.getRemoteClients().add(clazz.getName());
            }

            if (clientModule.getApplicationClient() == null){
                if (clientModule.getRemoteClients().size() > 0 || clientModule.getLocalClients().size() > 0) {
                    clientModule.setApplicationClient(new ApplicationClient());
                }
            }

            return clientModule;
        }

        public ConnectorModule deploy(ConnectorModule connectorModule) throws OpenEJBException {
            return connectorModule;
        }

        public WebModule deploy(WebModule webModule) throws OpenEJBException {
            WebApp webApp = webModule.getWebApp();
            if (webApp != null && (webApp.isMetadataComplete())) return webModule;

            try {
                if (webModule.getFinder() == null) {
                    webModule.setFinder(FinderFactory.createFinder(webModule));
                }
            } catch (Exception e) {
                startupLogger.warning("Unable to scrape for @WebService or @WebServiceProvider annotations. ClassFinder failed.", e);
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

            AbstractFinder finder = webModule.getFinder();
            List<Class> classes = new ArrayList<Class>();
            classes.addAll(finder.findAnnotatedClasses(WebService.class));
            classes.addAll(finder.findAnnotatedClasses(WebServiceProvider.class));

            for (Class<?> webServiceClass : classes) {
                // If this class is also annotated @Stateless or @Singleton, we should skip it
                if (webServiceClass.isAnnotationPresent(Singleton.class)) continue;
                if (webServiceClass.isAnnotationPresent(Stateless.class)) continue;

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
                startupLogger.warning("Unable to scrape for @Stateful, @Stateless, @Singleton or @MessageDriven annotations. ClassFinder failed.", e);
                return ejbModule;
            }

            AbstractFinder finder = ejbModule.getFinder();

            /* 19.2:  ejb-name: Default is the unqualified name of the bean class */

            EjbJar ejbJar = ejbModule.getEjbJar();
            for (Class<?> beanClass : finder.findAnnotatedClasses(Singleton.class)) {
                Singleton singleton = beanClass.getAnnotation(Singleton.class);
                String ejbName = getEjbName(singleton, beanClass);

                if (!isValidEjbAnnotationUsage(Singleton.class, beanClass, ejbName, ejbModule)) continue;

                EnterpriseBean enterpriseBean = ejbJar.getEnterpriseBean(ejbName);
                if (enterpriseBean == null) {
                    enterpriseBean = new SingletonBean(ejbName, beanClass.getName());
                    ejbJar.addEnterpriseBean(enterpriseBean);
                    LegacyProcessor.process(beanClass, enterpriseBean);
                }
                if (enterpriseBean.getEjbClass() == null) {
                    enterpriseBean.setEjbClass(beanClass.getName());
                    LegacyProcessor.process(beanClass, enterpriseBean);
                }
                if (enterpriseBean instanceof SessionBean) {
                    SessionBean sessionBean = (SessionBean) enterpriseBean;
                    sessionBean.setSessionType(SessionType.SINGLETON);

                    if (singleton.mappedName() != null) {
                        sessionBean.setMappedName(singleton.mappedName());
                    }
                }
            }

            for (Class<?> beanClass : finder.findAnnotatedClasses(Stateless.class)) {
                Stateless stateless = beanClass.getAnnotation(Stateless.class);
                String ejbName = getEjbName(stateless, beanClass);

                if (!isValidEjbAnnotationUsage(Stateless.class, beanClass, ejbName, ejbModule)) continue;

                EnterpriseBean enterpriseBean = ejbJar.getEnterpriseBean(ejbName);
                if (enterpriseBean == null) {
                    enterpriseBean = new StatelessBean(ejbName, beanClass.getName());
                    ejbJar.addEnterpriseBean(enterpriseBean);
                    LegacyProcessor.process(beanClass, enterpriseBean);
                }
                if (enterpriseBean.getEjbClass() == null) {
                    enterpriseBean.setEjbClass(beanClass.getName());
                    LegacyProcessor.process(beanClass, enterpriseBean);
                }
                if (enterpriseBean instanceof SessionBean) {
                    SessionBean sessionBean = (SessionBean) enterpriseBean;
                    sessionBean.setSessionType(SessionType.STATELESS);

                    if (stateless.mappedName() != null) {
                        sessionBean.setMappedName(stateless.mappedName());
                    }
                }
            }

            for (Class<?> beanClass : finder.findAnnotatedClasses(Stateful.class)) {
                Stateful stateful = beanClass.getAnnotation(Stateful.class);
                String ejbName = getEjbName(stateful, beanClass);

                if (!isValidEjbAnnotationUsage(Stateful.class, beanClass, ejbName, ejbModule)) continue;

                EnterpriseBean enterpriseBean = ejbJar.getEnterpriseBean(ejbName);
                if (enterpriseBean == null) {
                    enterpriseBean = new StatefulBean(ejbName, beanClass.getName());
                    ejbJar.addEnterpriseBean(enterpriseBean);
                    LegacyProcessor.process(beanClass, enterpriseBean);
                }
                if (enterpriseBean.getEjbClass() == null) {
                    enterpriseBean.setEjbClass(beanClass.getName());
                    LegacyProcessor.process(beanClass, enterpriseBean);
                }
                if (enterpriseBean instanceof SessionBean) {
                    SessionBean sessionBean = (SessionBean) enterpriseBean;
                    // TODO: We might be stepping on an xml override here
                    sessionBean.setSessionType(SessionType.STATEFUL);
                    if (stateful.mappedName() != null) {
                        sessionBean.setMappedName(stateful.mappedName());
                    }
                }
            }

            for (Class<?> beanClass : finder.findAnnotatedClasses(ManagedBean.class)) {
                ManagedBean managed = beanClass.getAnnotation(ManagedBean.class);
                String ejbName = getEjbName(managed, beanClass);

                // TODO: this is actually against the spec, but the requirement is rather silly
                // (allowing @Stateful and @ManagedBean on the same class)
                // If the TCK doesn't complain we should discourage it
                if (!isValidEjbAnnotationUsage(ManagedBean.class, beanClass, ejbName, ejbModule)) continue;

                EnterpriseBean enterpriseBean = ejbJar.getEnterpriseBean(ejbName);
                if (enterpriseBean == null) {
                    enterpriseBean = new org.apache.openejb.jee.ManagedBean(ejbName, beanClass.getName());
                    ejbJar.addEnterpriseBean(enterpriseBean);
                }
                if (enterpriseBean.getEjbClass() == null) {
                    enterpriseBean.setEjbClass(beanClass.getName());
                }
                if (enterpriseBean instanceof SessionBean) {
                    SessionBean sessionBean = (SessionBean) enterpriseBean;
                    sessionBean.setSessionType(SessionType.MANAGED);
                }
            }

            List<Class> classes = finder.findAnnotatedClasses(MessageDriven.class);
            for (Class<?> beanClass : classes) {
                MessageDriven mdb = beanClass.getAnnotation(MessageDriven.class);
                String ejbName = getEjbName(mdb, beanClass);

                if (!isValidEjbAnnotationUsage(MessageDriven.class, beanClass, ejbName, ejbModule)) continue;

                MessageDrivenBean messageBean = (MessageDrivenBean) ejbJar.getEnterpriseBean(ejbName);
                if (messageBean == null) {
                    messageBean = new MessageDrivenBean(ejbName);
                    ejbJar.addEnterpriseBean(messageBean);
                    LegacyProcessor.process(beanClass, messageBean);
                }
                if (messageBean.getEjbClass() == null) {
                    messageBean.setEjbClass(beanClass.getName());
                    LegacyProcessor.process(beanClass, messageBean);
                }
            }

            AssemblyDescriptor assemblyDescriptor = ejbModule.getEjbJar().getAssemblyDescriptor();
            if (assemblyDescriptor == null) {
                assemblyDescriptor = new AssemblyDescriptor();
                ejbModule.getEjbJar().setAssemblyDescriptor(assemblyDescriptor);
            }

            // https://issues.apache.org/jira/browse/OPENEJB-980
            startupLogger.debug("Searching for inherited application exceptions (see OPENEJB-980) - it doesn't care whether inherited is true/false");
            List<Class> appExceptions;
            appExceptions = finder.findInheritedAnnotatedClasses(ApplicationException.class);
            for (Class<?> exceptionClass : appExceptions) {
                startupLogger.debug("...handling " + exceptionClass);
                if (assemblyDescriptor.getApplicationException(exceptionClass) == null) {
                    ApplicationException annotation = exceptionClass.getAnnotation(ApplicationException.class);
                    // OPENEJB-980
                    if (annotation == null) {
                        Class<?> parentExceptionClass = exceptionClass;
                        while (annotation == null) {
                            parentExceptionClass = parentExceptionClass.getSuperclass();
                            annotation = parentExceptionClass.getAnnotation(ApplicationException.class);
                        }
                    }
                    startupLogger.debug("...adding " + exceptionClass + " with rollback=" + annotation.rollback());
                    assemblyDescriptor.addApplicationException(exceptionClass, annotation.rollback());
                }
            }

            return ejbModule;
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

        private boolean isValidEjbAnnotationUsage(Class annotationClass, Class<?> beanClass, String ejbName, EjbModule ejbModule) {
            List<Class<? extends Annotation>> annotations = new ArrayList(asList(Singleton.class, Stateless.class, Stateful.class, MessageDriven.class));
            annotations.remove(annotationClass);

            Map<String, Class<? extends Annotation>> names = new HashMap<String, Class<? extends Annotation>>();

            boolean b = true;
            for (Class<? extends Annotation> secondAnnotation : annotations) {
                Annotation annotation = beanClass.getAnnotation(secondAnnotation);

                if (annotation == null) continue;

                String secondEjbName = null;
                if (annotation instanceof Stateful) {
                    secondEjbName = getEjbName((Stateful) annotation, beanClass);
                } else if (annotation instanceof Stateless) {
                    secondEjbName = getEjbName((Stateless) annotation, beanClass);
                } else if (annotation instanceof Singleton) {
                    secondEjbName = getEjbName((Singleton) annotation, beanClass);
                } else if (annotation instanceof MessageDriven) {
                    secondEjbName = getEjbName((MessageDriven) annotation, beanClass);
                }

                if (ejbName.equals(secondEjbName)) {
                    ejbModule.getValidation().fail(ejbName, "multiplyAnnotatedAsBean", annotationClass.getSimpleName(), secondAnnotation.getSimpleName(), ejbName, beanClass.getName());
                }
            }

            if (beanClass.isInterface()) {
                ejbModule.getValidation().fail(ejbName, "interfaceAnnotatedAsBean", annotationClass.getSimpleName(), beanClass.getName());
                return false;
            }

            if (isAbstract(beanClass.getModifiers())) {
                ejbModule.getValidation().fail(ejbName, "abstractAnnotatedAsBean", annotationClass.getSimpleName(), beanClass.getName());
                return false;
            }

            return b;
        }

    }

    public static class ProcessAnnotatedBeans implements DynamicDeployer {
        public static final Set<String> knownResourceEnvTypes = new TreeSet<String>(asList(
                "javax.ejb.SessionContext",
                "javax.ejb.EntityContext",
                "javax.ejb.MessageDrivenContext",
                "javax.transaction.UserTransaction",
                "javax.jms.Queue",
                "javax.jms.Topic",
                "javax.xml.ws.WebServiceContext",
                "javax.ejb.TimerService"
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
                "java.lang.String"
        ));
        private static final String STRICT_INTERFACE_DECLARATION = "openejb.strict.interface.declaration";

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
            for (ConnectorModule connectorModule : appModule.getResourceModules()) {
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

                    ClassFinder inheritedClassFinder = createInheritedClassFinder(clazz);

                    buildAnnotatedRefs(client, inheritedClassFinder, classLoader);
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

                ClassFinder inheritedClassFinder = createInheritedClassFinder(clazz);

                buildAnnotatedRefs(client, inheritedClassFinder, classLoader);
            }

            for (String className : clientModule.getLocalClients()) {
                Class clazz;
                try {
                    clazz = classLoader.loadClass(className);
                } catch (ClassNotFoundException e) {
                    throw new OpenEJBException("Unable to load LocalClient class: " + className, e);
                }

                ClassFinder inheritedClassFinder = createInheritedClassFinder(clazz);

                buildAnnotatedRefs(client, inheritedClassFinder, classLoader);
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

            /*
             * Servlet classes are scanned
             */
            for (Servlet servlet : webApp.getServlet()) {
                String servletClass = servlet.getServletClass();
                if (servletClass != null) {
                    try {
                        Class clazz = classLoader.loadClass(servletClass);
                        classes.add(clazz);
                    } catch (ClassNotFoundException e) {
                        throw new OpenEJBException("Unable to load servlet class: " + servletClass, e);
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
                            throw new OpenEJBException("Unable to load tag library servlet listener class: " + listenerClass, e);
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
                            throw new OpenEJBException("Unable to load tag library tag class: " + tagClass, e);
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
                            throw new OpenEJBException("Unable to load JSF managed bean class: " + managedBeanClass, e);
                        }
                    }
                }
            }

            AbstractFinder finder = webModule.getFinder();

            if (finder != null) {
                String[] webComponentAnnotations = {
                        "javax.faces.bean.ManagedBean",
                        "javax.servlet.annotation.WebServlet",
                        "javax.servlet.annotation.WebServletContextListener",
                        "javax.servlet.annotation.ServletFilter",
                };

                List<Class<? extends Annotation>> annotations = new ArrayList<Class<? extends Annotation>>();
                for (String componentAnnotationName : webComponentAnnotations) {
                    try {
                        Class<?> clazz = classLoader.loadClass(componentAnnotationName);
                        annotations.add(clazz.asSubclass(Annotation.class));
                    } catch (ClassNotFoundException e) {
                        logger.debug("Support not enabled: " + componentAnnotationName);
                    }
                }


                for (Class<? extends Annotation> annotation : annotations) {
                    logger.debug("Scanning for @" + annotation.getName());
                    List<Class> list = finder.findAnnotatedClasses(annotation);
                    if (logger.isDebugEnabled()) for (Class clazz : list) {
                        logger.debug("Found " + clazz.getName());
                    }

                    classes.addAll(list);
                }
            }

            ClassFinder inheritedClassFinder = createInheritedClassFinder(classes.toArray(new Class<?>[classes.size()]));

            /*
             * @EJB
             * @Resource
             * @WebServiceRef
             * @PersistenceUnit
             * @PersistenceContext
             */
            buildAnnotatedRefs(webApp, inheritedClassFinder, classLoader);

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

                Class<?> clazz;
                try {
                    clazz = classLoader.loadClass(bean.getEjbClass());
                } catch (ClassNotFoundException e) {
                    throw new OpenEJBException("Unable to load bean class: " + bean.getEjbClass(), e);
                }
                ClassFinder classFinder = new ClassFinder(clazz);

                ClassFinder inheritedClassFinder = createInheritedClassFinder(clazz);

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
                processCallbacks(bean, inheritedClassFinder);

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

                AssemblyDescriptor assemblyDescriptor = ejbModule.getEjbJar().getAssemblyDescriptor();

                /*
                 * @ApplicationException
                 */
                processApplicationExceptions(clazz, assemblyDescriptor);

                /*
                 * TransactionAttribute
                 */
                if (bean.getTransactionType() == TransactionType.CONTAINER) {
                    processAttributes(new TransactionAttributeHandler(assemblyDescriptor, ejbName), clazz, inheritedClassFinder);
                } else {
                    checkAttributes(new TransactionAttributeHandler(assemblyDescriptor, ejbName), ejbName, ejbModule, classFinder, "invalidTransactionAttribute");
                }

                /*
                 * @RolesAllowed
                 * @PermitAll
                 * @DenyAll
                 * @RunAs
                 * @DeclareRoles
                 */
                processSecurityAnnotations(clazz, ejbName, ejbModule, inheritedClassFinder, bean);

                if (bean instanceof SessionBean) {
                    SessionBean sessionBean = (SessionBean) bean;

                    // Merge AccessTimeout value from XML - XML value takes precedence
                    if(sessionBean.getAccessTimeout() == null) {
                        final AccessTimeout annotation = clazz.getAnnotation(AccessTimeout.class);
                        if(annotation != null) {
                            final Timeout timeout = new Timeout();
                            timeout.setTimeout(annotation.value());
                            timeout.setUnit(TimeUnitType.valueOf(annotation.unit().toString()));
                            sessionBean.setAccessTimeout(timeout);
                        }
                    }

                    // Merge StatefulTimeout value from XML - XML value takes precedence
                    if(sessionBean.getStatefulTimeout() == null) {
                        final StatefulTimeout annotation = clazz.getAnnotation(StatefulTimeout.class);
                        if(annotation != null) {
                            final Timeout timeout = new Timeout();
                            timeout.setTimeout(annotation.value());
                            timeout.setUnit(TimeUnitType.valueOf(annotation.unit().toString()));
                            sessionBean.setStatefulTimeout(timeout);
                        }
                    }
                }


                /*
                 * @Schedule
                 * @Schedules
                 */
                Set<Method> scheduleMethods = new HashSet<Method>();
                scheduleMethods.addAll(inheritedClassFinder.findAnnotatedMethods(javax.ejb.Schedules.class));
                scheduleMethods.addAll(inheritedClassFinder.findAnnotatedMethods(javax.ejb.Schedule.class));

                Map<String, List<MethodAttribute>> existingDeclarations = assemblyDescriptor.getMethodScheduleMap(ejbName);

                for (Method method : scheduleMethods) {

                    // Don't add the schedules from annotations if the schedules have been
                    // supplied for this method via xml.  The xml is considered an override.
                    if (hasMethodAttribute(existingDeclarations, method)) break;

                    List<javax.ejb.Schedule> list = new ArrayList<javax.ejb.Schedule>();

                    javax.ejb.Schedules schedulesAnnotation = method.getAnnotation(javax.ejb.Schedules.class);
                    if (schedulesAnnotation != null) {
                        list.addAll(asList(schedulesAnnotation.value()));
                    }

                    javax.ejb.Schedule scheduleAnnotation = method.getAnnotation(javax.ejb.Schedule.class);
                    if (scheduleAnnotation != null) {
                        list.add(scheduleAnnotation);
                    }

                    if (list.size() == 0) continue;

                    MethodSchedule methodSchedule = new MethodSchedule(ejbName, method);
                    for (javax.ejb.Schedule schedule : list) {
                        TimerSchedule s = new TimerSchedule();
                        s.setSecond(schedule.second());
                        s.setMinute(schedule.minute());
                        s.setHour(schedule.hour());
                        s.setDayOfWeek(schedule.dayOfWeek());
                        s.setDayOfMonth(schedule.dayOfMonth());
                        s.setMonth(schedule.month());
                        s.setYear(schedule.year());
                        s.setPersistent(schedule.persistent());
                        s.setInfo(schedule.info());
                        methodSchedule.getSchedule().add(s);
                    }
                    assemblyDescriptor.getMethodSchedule().add(methodSchedule);
                }

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
                for (Class<?> interceptorsAnnotatedClass : inheritedClassFinder.findAnnotatedClasses(Interceptors.class)) {
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

                for (Method method : inheritedClassFinder.findAnnotatedMethods(Interceptors.class)) {
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

                        binding.setMethod(new NamedMethod(method));
                    }
                }

                /*
                 * @ExcludeDefaultInterceptors
                 */
                ExcludeDefaultInterceptors excludeDefaultInterceptors = clazz.getAnnotation(ExcludeDefaultInterceptors.class);
                if (excludeDefaultInterceptors != null) {
                    InterceptorBinding binding = assemblyDescriptor.addInterceptorBinding(new InterceptorBinding(bean));
                    binding.setExcludeDefaultInterceptors(true);
                }

                for (Method method : classFinder.findAnnotatedMethods(ExcludeDefaultInterceptors.class)) {
                    InterceptorBinding binding = assemblyDescriptor.addInterceptorBinding(new InterceptorBinding(bean));
                    binding.setExcludeDefaultInterceptors(true);
                    binding.setMethod(new NamedMethod(method));
                }

                ExcludeClassInterceptors excludeClassInterceptors = clazz.getAnnotation(ExcludeClassInterceptors.class);
                if (excludeClassInterceptors != null) {
                    InterceptorBinding binding = assemblyDescriptor.addInterceptorBinding(new InterceptorBinding(bean));
                    binding.setExcludeClassInterceptors(true);
                }

                for (Method method : classFinder.findAnnotatedMethods(ExcludeClassInterceptors.class)) {
                    InterceptorBinding binding = assemblyDescriptor.addInterceptorBinding(new InterceptorBinding(bean));
                    binding.setExcludeClassInterceptors(true);
                    binding.setMethod(new NamedMethod(method));
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
                            if (sessionBean.getConcurrencyManagementType() == ConcurrencyManagementType.CONTAINER) {
                                processAttributes(new ConcurrencyAttributeHandler(assemblyDescriptor, ejbName), clazz, inheritedClassFinder);
                            } else {
                                checkAttributes(new ConcurrencyAttributeHandler(assemblyDescriptor, ejbName), ejbName, ejbModule, classFinder, "invalidConcurrencyAttribute");
                            }

                            /*
                             * @Startup
                             */
                            if (!sessionBean.hasInitOnStartup()) {
                                Startup startup = getInheritableAnnotation(clazz, Startup.class);
                                sessionBean.setInitOnStartup(startup != null);
                            }

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
                                    !name.startsWith("javax.ejb.")) {
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

                // add webservice handler classes to the class finder used in annotation processing
                Set<Class<?>> classes = new HashSet<Class<?>>();
                classes.add(clazz);
                if (ejbModule.getWebservices() != null) {
                    for (WebserviceDescription webservice : ejbModule.getWebservices().getWebserviceDescription()) {
                        for (PortComponent port : webservice.getPortComponent()) {
                            // only process port definitions for this ejb
                            if (!ejbName.equals(port.getServiceImplBean().getEjbLink())) continue;

                            if (port.getHandlerChains() == null) continue;
                            for (org.apache.openejb.jee.HandlerChain handlerChain : port.getHandlerChains().getHandlerChain()) {
                                for (Handler handler : handlerChain.getHandler()) {
                                    String handlerClass = handler.getHandlerClass();
                                    if (handlerClass != null) {
                                        try {
                                            Class handlerClazz = classLoader.loadClass(handlerClass);
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
                inheritedClassFinder = createInheritedClassFinder(classes.toArray(new Class<?>[classes.size()]));
            }

            for (EnterpriseBean bean : enterpriseBeans) {
                Class<?> clazz;
                try {
                    clazz = classLoader.loadClass(bean.getEjbClass());
                } catch (ClassNotFoundException e) {
                    throw new OpenEJBException("Unable to load bean class: " + bean.getEjbClass(), e);
                }

                ClassFinder inheritedClassFinder = createInheritedClassFinder(clazz);

                /*
                 * @EJB
                 * @Resource
                 * @WebServiceRef
                 * @PersistenceUnit
                 * @PersistenceContext
                 */
                buildAnnotatedRefs(bean, inheritedClassFinder, classLoader);
                processWebServiceClientHandlers(bean, classLoader);
            }


            for (Interceptor interceptor : ejbModule.getEjbJar().getInterceptors()) {
                Class<?> clazz;
                try {
                    clazz = classLoader.loadClass(interceptor.getInterceptorClass());
                } catch (ClassNotFoundException e) {
                    throw new OpenEJBException("Unable to load interceptor class: " + interceptor.getInterceptorClass(), e);
                }

                ClassFinder inheritedClassFinder = createInheritedClassFinder(clazz);

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
                processCallbacks(interceptor, inheritedClassFinder);

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
                buildAnnotatedRefs(interceptor, inheritedClassFinder, classLoader);

                processWebServiceClientHandlers(interceptor, classLoader);

                /**
                 * Interceptors do not have their own section in ejb-jar.xml for resource references
                 * so we add them to the references of each ejb.  A bit backwards but more or less
                 * mandated by the design of the spec.
                 */
                for (EnterpriseBean bean : enterpriseBeans) {
                    // DMB: TODO, we should actually check to see if the ref exists in the bean's enc.
                    bean.getEnvEntry().addAll(interceptor.getEnvEntry());
                    bean.getEjbRef().addAll(interceptor.getEjbRef());
                    bean.getEjbLocalRef().addAll(interceptor.getEjbLocalRef());
                    bean.getResourceRef().addAll(interceptor.getResourceRef());
                    bean.getResourceEnvRef().addAll(interceptor.getResourceEnvRef());
                    bean.getPersistenceContextRef().addAll(interceptor.getPersistenceContextRef());
                    bean.getPersistenceUnitRef().addAll(interceptor.getPersistenceUnitRef());
                    bean.getMessageDestinationRef().addAll(interceptor.getMessageDestinationRef());
                    bean.getServiceRef().addAll(interceptor.getServiceRef());
                }
            }

            return ejbModule;
        }

        private boolean hasMethodAttribute(Map<String, List<MethodAttribute>> existingDeclarations, Method method) {
            List<MethodAttribute> list = existingDeclarations.get(method.getName());
            if (list == null) return false;

            for (MethodAttribute attribute : list) {
                MethodParams methodParams = attribute.getMethodParams();
                if (methodParams == null) break;

                List<String> params1 = methodParams.getMethodParam();
                String[] params2 = asStrings(method.getParameterTypes());
                if (params1.size() != params2.length) break;

                for (int i = 0; i < params1.size(); i++) {
                    String a = params1.get(i);
                    String b = params2[i];
                    if (!a.equals(b)) break;
                }

                return true;
            }

            return false;
        }

        private void processApplicationExceptions(Class<?> clazz, AssemblyDescriptor assemblyDescriptor) {
            /*
             * @ApplicationException
             */
            for (Method method : clazz.getMethods()) {
                for (Class<?> exception : method.getExceptionTypes()) {
                    ApplicationException annotation = exception.getAnnotation(ApplicationException.class);
                    if (annotation == null) continue;
                    if (assemblyDescriptor.getApplicationException(exception) != null) continue;
                    assemblyDescriptor.addApplicationException(exception, annotation.rollback());
                }
            }
        }

        private void processSessionInterfaces(SessionBean sessionBean, Class<?> beanClass, EjbModule ejbModule) {

            ValidationContext validation = ejbModule.getValidation();
            String ejbName = sessionBean.getEjbName();

            boolean strict = getProperty(ejbModule, STRICT_INTERFACE_DECLARATION, false + "").equalsIgnoreCase("true");

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

            for (Class<?> clazz : ancestors(beanClass)) {
                /*
                 * @WebService
                 * @WebServiceProvider
                 */
                if (sessionBean.getServiceEndpoint() == null) {
                    Class defaultEndpoint = DeploymentInfo.ServiceEndpoint.class;

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
                    } else if (!defaultEndpoint.equals(DeploymentInfo.ServiceEndpoint.class)) {
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
                            !descriptor.contains(interfce.getName())) {
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
                    if (interfaces.size() == 1) {
                        Class<?> interfce = interfaces.remove(0);

                        if (strict && impliedLocal && impliedRemote) {
                            /**
                             * Cannot imply @Local and @Remote at the same time with strict mode on
                             */
                            validation.fail(ejbName, "ann.localRemote.ambiguous", interfce.getName());
                        } else {
                            if (impliedLocal) bean.local.add(interfce);
                            if (impliedRemote) bean.remote.add(interfce);
                        }

                    } else { // invalid
                        /**
                         * Cannot imply either @Local or @Remote and list multiple interfaces
                         */
                        if (impliedLocal) validation.fail(ejbName, "ann.local.noAttributes", join(", ", interfaces));
                        if (impliedRemote) validation.fail(ejbName, "ann.remote.noAttributes", join(", ", interfaces));

                        /**
                         * This bean is invalid, so do not bother looking at the other interfaces or the superclass
                         */
                        return;
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
                        if (isLocal) implemented.local.add(interfce);
                        if (isRemote) implemented.remote.add(interfce);
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
            for (Class interfce : all.unspecified) sessionBean.addBusinessLocal(interfce);

            if (beanClass.getAnnotation(LocalBean.class) != null || beanClass.getInterfaces().length == 0) {
                sessionBean.setLocalBean(new Empty());
            }

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

        private String getProperty(EjbModule ejbModule, String key, String defaultValue) {
            OpenejbJar openejbJar = ejbModule.getOpenejbJar();
            String value = SystemInstance.get().getProperty(key, defaultValue);
            if (openejbJar != null && openejbJar.getProperties() != null){
                value = openejbJar.getProperties().getProperty(key, value);
            }
            return value;
        }

        private void processSecurityAnnotations(Class<?> beanClass, String ejbName, EjbModule ejbModule, ClassFinder classFinder, EnterpriseBean bean) {
            AssemblyDescriptor assemblyDescriptor = ejbModule.getEjbJar().getAssemblyDescriptor();

            List<String> classPermissions = getDeclaredClassPermissions(assemblyDescriptor, ejbName);

            for (Class<?> clazz : ancestors(beanClass)) {
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
            for (Method method : classFinder.findAnnotatedMethods(RolesAllowed.class)) {
                checkConflictingSecurityAnnotations(method, ejbName, ejbModule, seen);
                RolesAllowed rolesAllowed = method.getAnnotation(RolesAllowed.class);
                MethodPermission methodPermission = new MethodPermission();
                methodPermission.getRoleName().addAll(asList(rolesAllowed.value()));
                methodPermission.getMethod().add(new org.apache.openejb.jee.Method(ejbName, method));
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
            for (Method method : classFinder.findAnnotatedMethods(PermitAll.class)) {
                checkConflictingSecurityAnnotations(method, ejbName, ejbModule, seen);
                MethodPermission methodPermission = new MethodPermission();
                methodPermission.setUnchecked(true);
                methodPermission.getMethod().add(new org.apache.openejb.jee.Method(ejbName, method));
                assemblyDescriptor.getMethodPermission().add(methodPermission);
            }

            /*
             * @DenyAll
             */
            for (Method method : classFinder.findAnnotatedMethods(DenyAll.class)) {
                checkConflictingSecurityAnnotations(method, ejbName, ejbModule, seen);
                ExcludeList excludeList = assemblyDescriptor.getExcludeList();
                excludeList.addMethod(new org.apache.openejb.jee.Method(ejbName, method));
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
        private void checkConflictingSecurityAnnotations(Method method, String ejbName, EjbModule ejbModule, List<Method> seen) {
            if (seen.contains(method)) return;
            seen.add(method);

            List<String> annotations = new ArrayList<String>();
            for (Class<? extends Annotation> annotation : asList(RolesAllowed.class, PermitAll.class, DenyAll.class)) {
                if (method.getAnnotation(annotation) != null) {
                    annotations.add("@" + annotation.getSimpleName());
                }
            }

            if (annotations.size() > 1) {
                ejbModule.getValidation().fail(ejbName, "conflictingSecurityAnnotations", method.getName(), join(" and ", annotations), method.getDeclaringClass());
            }
        }


        private void processCallbacks(Lifecycle bean, ClassFinder classFinder) {
            /*
             * @PostConstruct
             */
            LifecycleCallback postConstruct = getFirst(bean.getPostConstruct());
            if (postConstruct == null) {
                for (Method method : classFinder.findAnnotatedMethods(PostConstruct.class)) {
                    bean.getPostConstruct().add(new LifecycleCallback(method));
                }
            }

            /*
             * @PreDestroy
             */
            LifecycleCallback preDestroy = getFirst(bean.getPreDestroy());
            if (preDestroy == null) {
                for (Method method : classFinder.findAnnotatedMethods(PreDestroy.class)) {
                    bean.getPreDestroy().add(new LifecycleCallback(method));
                }
            }

            /*
             * @AroundInvoke
             */
            AroundInvoke aroundInvoke = getFirst(bean.getAroundInvoke());
            if (aroundInvoke == null) {
                for (Method method : classFinder.findAnnotatedMethods(javax.interceptor.AroundInvoke.class)) {
                    bean.getAroundInvoke().add(new AroundInvoke(method));
                }
            }

            /*
             *  @AroundTimeout
             */
            AroundTimeout aroundTimeout = getFirst(bean.getAroundTimeout());
            if (aroundTimeout == null) {
                for (Method method : classFinder.findAnnotatedMethods(javax.interceptor.AroundTimeout.class)) {
                    bean.getAroundTimeout().add(new AroundTimeout(method));
                }
            }

            /*
             * @Timeout
             */
            if (bean instanceof TimerConsumer) {
                TimerConsumer timerConsumer = (TimerConsumer) bean;
                if (timerConsumer.getTimeoutMethod() == null) {
                    List<Method> timeoutMethods = classFinder.findAnnotatedMethods(javax.ejb.Timeout.class);
                    for (Method method : timeoutMethods) {
                        timerConsumer.setTimeoutMethod(new NamedMethod(method));
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
                    for (Method method : classFinder.findAnnotatedMethods(AfterBegin.class)) {
                        session.getAfterBegin().add(new LifecycleCallback(method));
                    }
                }

                /*
                 * @BeforeCompletion
                 */
                LifecycleCallback beforeCompletion = getFirst(session.getBeforeCompletion());
                if (beforeCompletion == null) {
                    for (Method method : classFinder.findAnnotatedMethods(BeforeCompletion.class)) {
                        session.getBeforeCompletion().add(new LifecycleCallback(method));
                    }
                }

                /*
                 * @AfterCompletion
                 */
                LifecycleCallback afterCompletion = getFirst(session.getAfterCompletion());
                if (afterCompletion == null) {
                    for (Method method : classFinder.findAnnotatedMethods(AfterCompletion.class)) {
                        session.getAfterCompletion().add(new LifecycleCallback(method));
                    }
                }

                /*
                 * @PostActivate
                 */
                LifecycleCallback postActivate = getFirst(session.getPostActivate());
                if (postActivate == null) {
                    for (Method method : classFinder.findAnnotatedMethods(PostActivate.class)) {
                        session.getPostActivate().add(new LifecycleCallback(method));
                    }
                }

                /*
                 * @PrePassivate
                 */
                LifecycleCallback prePassivate = getFirst(session.getPrePassivate());
                if (prePassivate == null) {
                    for (Method method : classFinder.findAnnotatedMethods(PrePassivate.class)) {
                        session.getPrePassivate().add(new LifecycleCallback(method));
                    }
                }

                /*
                 * @Init
                 */
                List<Method> initMethods = classFinder.findAnnotatedMethods(Init.class);
                for (Method method : initMethods) {
                    InitMethod initMethod = new InitMethod(method);

                    Init init = method.getAnnotation(Init.class);
                    if (init.value() != null && !init.value().equals("")) {
                        initMethod.setCreateMethod(init.value());
                    }

                    session.getInitMethod().add(initMethod);
                }

                /*
                 * @Remove
                 */
                List<Method> removeMethods = classFinder.findAnnotatedMethods(Remove.class);
                Map<NamedMethod, RemoveMethod> declaredRemoveMethods = new HashMap<NamedMethod, RemoveMethod>();
                for (RemoveMethod removeMethod : session.getRemoveMethod()) {
                    declaredRemoveMethods.put(removeMethod.getBeanMethod(), removeMethod);
                }
                for (Method method : removeMethods) {
                    Remove remove = method.getAnnotation(Remove.class);
                    RemoveMethod removeMethod = new RemoveMethod(method, remove.retainIfException());

                    RemoveMethod declaredRemoveMethod = declaredRemoveMethods.get(removeMethod.getBeanMethod());

                    if (declaredRemoveMethod == null) {
                        session.getRemoveMethod().add(removeMethod);
                    } else if (!declaredRemoveMethod.isExplicitlySet()) {
                        declaredRemoveMethod.setRetainIfException(remove.retainIfException());
                    }
                }
            }
        }

        public void buildAnnotatedRefs(JndiConsumer consumer, ClassFinder classFinder, ClassLoader classLoader) throws OpenEJBException {
            //
            // @EJB
            //

            List<EJB> ejbList = new ArrayList<EJB>();
            for (Class<?> clazz : classFinder.findAnnotatedClasses(EJBs.class)) {
                EJBs ejbs = clazz.getAnnotation(EJBs.class);
                ejbList.addAll(asList(ejbs.value()));
            }
            for (Class<?> clazz : classFinder.findAnnotatedClasses(EJB.class)) {
                EJB e = clazz.getAnnotation(EJB.class);
                ejbList.add(e);
            }

            for (EJB ejb : ejbList) {
                buildEjbRef(consumer, ejb, null);
            }

            for (Field field : classFinder.findAnnotatedFields(EJB.class)) {
                EJB ejb = field.getAnnotation(EJB.class);

                Member member = new FieldMember(field);

                buildEjbRef(consumer, ejb, member);
            }

            for (Method method : classFinder.findAnnotatedMethods(EJB.class)) {
                EJB ejb = method.getAnnotation(EJB.class);

                Member member = new MethodMember(method);

                buildEjbRef(consumer, ejb, member);
            }

            //
            // @Resource
            //

            List<Resource> resourceList = new ArrayList<Resource>();
            for (Class<?> clazz : classFinder.findAnnotatedClasses(Resources.class)) {
                Resources resources = clazz.getAnnotation(Resources.class);
                resourceList.addAll(asList(resources.value()));
            }
            for (Class<?> clazz : classFinder.findAnnotatedClasses(Resource.class)) {
                Resource resource = clazz.getAnnotation(Resource.class);
                resourceList.add(resource);
            }

            for (Resource resource : resourceList) {
                buildResource(consumer, resource, null);
            }

            for (Field field : classFinder.findAnnotatedFields(Resource.class)) {
                Resource resource = field.getAnnotation(Resource.class);

                Member member = new FieldMember(field);

                buildResource(consumer, resource, member);
            }

            for (Method method : classFinder.findAnnotatedMethods(Resource.class)) {
                Resource resource = method.getAnnotation(Resource.class);

                Member member = new MethodMember(method);

                buildResource(consumer, resource, member);
            }

            //
            // @WebServiceRef
            //

            List<WebServiceRef> webservicerefList = new ArrayList<WebServiceRef>();
            for (Class<?> clazz : classFinder.findAnnotatedClasses(WebServiceRefs.class)) {
                WebServiceRefs webServiceRefs = clazz.getAnnotation(WebServiceRefs.class);
                webservicerefList.addAll(asList(webServiceRefs.value()));
            }
            for (Class<?> clazz : classFinder.findAnnotatedClasses(WebServiceRef.class)) {
                WebServiceRef webServiceRef = clazz.getAnnotation(WebServiceRef.class);
                webservicerefList.add(webServiceRef);
            }

            for (WebServiceRef webserviceref : webservicerefList) {

                buildWebServiceRef(consumer, webserviceref, null, null, classLoader);
            }

            for (Field field : classFinder.findAnnotatedFields(WebServiceRef.class)) {
                WebServiceRef webserviceref = field.getAnnotation(WebServiceRef.class);
                HandlerChain handlerChain = field.getAnnotation(HandlerChain.class);

                Member member = new FieldMember(field);

                buildWebServiceRef(consumer, webserviceref, handlerChain, member, classLoader);
            }

            for (Method method : classFinder.findAnnotatedMethods(WebServiceRef.class)) {
                WebServiceRef webserviceref = method.getAnnotation(WebServiceRef.class);
                HandlerChain handlerChain = method.getAnnotation(HandlerChain.class);

                Member member = new MethodMember(method);

                buildWebServiceRef(consumer, webserviceref, handlerChain, member, classLoader);
            }

            //
            // @PersistenceUnit
            //

            List<PersistenceUnit> persistenceUnitList = new ArrayList<PersistenceUnit>();
            for (Class<?> clazz : classFinder.findAnnotatedClasses(PersistenceUnits.class)) {
                PersistenceUnits persistenceUnits = clazz.getAnnotation(PersistenceUnits.class);
                persistenceUnitList.addAll(asList(persistenceUnits.value()));
            }
            for (Class<?> clazz : classFinder.findAnnotatedClasses(PersistenceUnit.class)) {
                PersistenceUnit persistenceUnit = clazz.getAnnotation(PersistenceUnit.class);
                persistenceUnitList.add(persistenceUnit);
            }
            for (PersistenceUnit pUnit : persistenceUnitList) {
                buildPersistenceUnit(consumer, pUnit, null);
            }
            for (Field field : classFinder.findAnnotatedFields(PersistenceUnit.class)) {
                PersistenceUnit pUnit = field.getAnnotation(PersistenceUnit.class);
                Member member = new FieldMember(field);
                buildPersistenceUnit(consumer, pUnit, member);
            }
            for (Method method : classFinder.findAnnotatedMethods(PersistenceUnit.class)) {
                PersistenceUnit pUnit = method.getAnnotation(PersistenceUnit.class);
                Member member = new MethodMember(method);
                buildPersistenceUnit(consumer, pUnit, member);
            }

            //
            // @PersistenceContext
            //

            PersistenceContextAnnFactory pcFactory = new PersistenceContextAnnFactory();
            List<PersistenceContext> persistenceContextList = new ArrayList<PersistenceContext>();
            for (Class<?> clazz : classFinder.findAnnotatedClasses(PersistenceContexts.class)) {
                PersistenceContexts persistenceContexts = clazz.getAnnotation(PersistenceContexts.class);
                persistenceContextList.addAll(asList(persistenceContexts.value()));
                pcFactory.addAnnotations(clazz);
            }
            for (Class<?> clazz : classFinder.findAnnotatedClasses(PersistenceContext.class)) {
                PersistenceContext persistenceContext = clazz.getAnnotation(PersistenceContext.class);
                persistenceContextList.add(persistenceContext);
                pcFactory.addAnnotations(clazz);
            }
            for (PersistenceContext pCtx : persistenceContextList) {
                buildPersistenceContext(consumer, pcFactory.create(pCtx, null), null);
            }
            for (Field field : classFinder.findAnnotatedFields(PersistenceContext.class)) {
                PersistenceContext pCtx = field.getAnnotation(PersistenceContext.class);
                Member member = new FieldMember(field);
                buildPersistenceContext(consumer, pcFactory.create(pCtx, member), member);
            }
            for (Method method : classFinder.findAnnotatedMethods(PersistenceContext.class)) {
                PersistenceContext pCtx = method.getAnnotation(PersistenceContext.class);
                Member member = new MethodMember(method);
                buildPersistenceContext(consumer, pcFactory.create(pCtx, member), member);
            }

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
            if (refName.equals("")) {
                refName = (member == null) ? null : member.getDeclaringClass().getName() + "/" + member.getName();
            }
            ejbRef.setEjbRefName(refName);

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

            boolean localbean = isLocalBean(interfce);

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

        private boolean isLocalBean(Class clazz) {
            DeploymentModule module = getModule();
            if (module instanceof EjbModule) {
                Set<String> localbeans = new HashSet<String>();
                EjbModule ejbModule = (EjbModule) module;
                for (EnterpriseBean bean : ejbModule.getEjbJar().getEnterpriseBeans()) {
                    if (bean instanceof SessionBean) {
                        if (((SessionBean) bean).getLocalBean() != null) {
                            localbeans.add(bean.getEjbClass());
                        }
                    }
                }

                if (localbeans.contains(clazz.getName())) {
                    return true;
                }
            }

            return false;
        }

        private boolean isValidEjbInterface(String b, Class clazz, String refName) {
            if (!clazz.isInterface()) {

                DeploymentModule module = getModule();
                if (module instanceof EjbModule) {
                    Set<String> beanClasses = new HashSet<String>();
                    EjbModule ejbModule = (EjbModule) module;
                    for (EnterpriseBean bean : ejbModule.getEjbJar().getEnterpriseBeans()) {
                        beanClasses.add(bean.getEjbClass());
                    }

                    if (beanClasses.contains(clazz.getName())) {
                        fail(b, "ann.ejb.beanClass", clazz.getName(), refName);
                    } else {
                        fail(b, "ann.ejb.notInterface", clazz.getName(), refName);
                    }
                } else {
                    fail(b, "ann.ejb.notInterface", clazz.getName(), refName);
                }

                return false;

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
            // Get the ref-name
            String refName = resource.name();
            if (refName.equals("")) {
                refName = (member == null) ? null : member.getDeclaringClass().getName() + "/" + member.getName();
            }

            /**
             * Was @Resource used at a class level witout specifying the 'name' or 'beanInterface' attributes?
             */
            if (member == null) {
                boolean shouldReturn = false;
                if (resource.name().equals("")) {
                    fail(consumer.getJndiConsumerName(), "resourceAnnotation.onClassWithNoName");
                    shouldReturn = true;
                }
                if (resource.type().equals(Object.class)) {
                    fail(consumer.getJndiConsumerName(), "resourceAnnotation.onClassWithNoType");
                    shouldReturn = true;
                }
                if (shouldReturn) return;
            }

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

                String type;
                if (resource.type() != java.lang.Object.class) {
                    type = resource.type().getName();
                } else {
                    type = member.getType().getName();
                }

                if (knownResourceEnvTypes.contains(type)) {
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
                        if (resource.type() != java.lang.Object.class) {
                            resourceEnvRef.setResourceEnvRefType(resource.type().getName());
                        } else {
                            resourceEnvRef.setResourceEnvRefType(member.getType().getName());
                        }
                    }
                    reference = resourceEnvRef;
                } else if (!knownEnvironmentEntries.contains(type)) {
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
                        if (resource.type() != java.lang.Object.class) {
                            resourceRef.setResType(resource.type().getName());
                        } else {
                            resourceRef.setResType(member.getType().getName());
                        }
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
             * @Resource <env-entry>
             *
             * Can't add an env-entry via @Resource as there needs to
             * be a corresponding value specified in the ejb-jar.xml.
             *
             * If we didn't find a reference (env-entry) in the ejb-jar.xml
             * and this is not a resource-env-ref or a resource-ref then
             * this is not a valid @Resource reference.
             */
            if (reference == null) {
                return;
            }

//            reference.setName(refName);

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
            // Get the ref-name
            String refName = persistenceUnit.name();
            if (refName.equals("")) {
                refName = (member == null) ? null : member.getDeclaringClass().getName() + "/" + member.getName();
            }

            /**
             * Was @PersistenceUnit used at a class level without specifying the 'name' attribute?
             */
            if (refName == null && member == null) {
                fail(consumer.getJndiConsumerName(), "presistenceUnitAnnotation.onClassWithNoName", persistenceUnit.unitName());
                return;
            }

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
                    validationContext.fail(jndiConsumerName, "presistenceUnitAnnotation.onEntityManager", name);
                } else if (!EntityManagerFactory.class.isAssignableFrom(type)) {
                    /**
                     * Was @PersistenceUnit mistakenly used for something that isn't an EntityManagerFactory?
                     */
                    fail(consumer.getJndiConsumerName(), "presistenceUnitAnnotation.onNonEntityManagerFactory", persistenceUnitRef.getName());
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

            if (refName.equals("")) {
                refName = (member == null) ? null : member.getDeclaringClass().getName() + "/" + member.getName();
            }

            /**
             * Was @PersistenceContext used at a class level without specifying the 'name' attribute?
             */
            if (refName == null && member == null) {
                fail(consumer.getJndiConsumerName(), "presistenceContextAnnotation.onClassWithNoName", persistenceContext.unitName());
                return;
            }

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
                    fail(consumer.getJndiConsumerName(), "presistenceContextAnnotation.onEntityManagerFactory", persistenceContextRef.getName());
                } else if (!EntityManager.class.isAssignableFrom(type)) {
                    /**
                     * Was @PersistenceContext mistakenly used for something that isn't an EntityManager?
                     */
                    fail(consumer.getJndiConsumerName(), "presistenceContextAnnotation.onNonEntityManager", persistenceContextRef.getName());
                } else {
                    // Set the member name where this will be injected
                    InjectionTarget target = new InjectionTarget();
                    target.setInjectionTargetClass(member.getDeclaringClass().getName());
                    target.setInjectionTargetName(member.getName());
                    persistenceContextRef.getInjectionTarget().add(target);
                }
            }
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
                refName = (member == null) ? null : member.getDeclaringClass().getName() + "/" + member.getName();
            }

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
         *
         * @param consumer
         * @param classLoader
         * @throws OpenEJBException
         */
        private void processWebServiceClientHandlers(JndiConsumer consumer, ClassLoader classLoader) throws OpenEJBException {
            if (SystemInstance.get().hasProperty("openejb.geronimo")) return;

            Set<Class<?>> processedClasses = new HashSet<Class<?>>();
            Set<Class<?>> handlerClasses = new HashSet<Class<?>>();
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
                ClassFinder handlerClassFinder = createInheritedClassFinder(handlerClasses.toArray(new Class<?>[handlerClasses.size()]));

                /*
                 * @EJB
                 * @Resource
                 * @WebServiceRef
                 * @PersistenceUnit
                 * @PersistenceContext
                 */
                buildAnnotatedRefs(consumer, handlerClassFinder, classLoader);

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

        public static class ConcurrencyAttributeHandler implements AnnotationHandler<javax.ejb.Lock> {

            private final AssemblyDescriptor assemblyDescriptor;
            private final String ejbName;

            public ConcurrencyAttributeHandler(AssemblyDescriptor assemblyDescriptor, String ejbName) {
                this.assemblyDescriptor = assemblyDescriptor;
                this.ejbName = ejbName;
            }

            public Map<String, List<MethodAttribute>> getExistingDeclarations() {
                return assemblyDescriptor.getMethodConcurrencyMap(ejbName);
            }

            //TODO uses non-existent ContainerConcurrency
            public void addClassLevelDeclaration(javax.ejb.Lock attribute, Class type) {
                ContainerConcurrency ctx = new ContainerConcurrency(cast(attribute.value()), type.getName(), ejbName, "*");
                assemblyDescriptor.getContainerConcurrency().add(ctx);
            }

            //TODO uses non-existent ContainerConcurrency
            public void addMethodLevelDeclaration(javax.ejb.Lock attribute, Method method) {
                ContainerConcurrency ctx = new ContainerConcurrency(cast(attribute.value()), ejbName, method);
                assemblyDescriptor.getContainerConcurrency().add(ctx);
            }

            public Class<javax.ejb.Lock> getAnnotationClass() {
                return javax.ejb.Lock.class;
            }

            private ConcurrentLockType cast(javax.ejb.LockType lockType) {
                return ConcurrentLockType.valueOf(lockType.toString());
            }
        }

        private <A extends Annotation> void checkAttributes(AnnotationHandler<A> handler, String ejbName, EjbModule ejbModule, ClassFinder classFinder, String messageKey) {
            Map<String, List<MethodAttribute>> existingDeclarations = handler.getExistingDeclarations();

            int xml = 0;
            for (List<MethodAttribute> methodAttributes : existingDeclarations.values()) {
                xml += methodAttributes.size();
            }

            if (xml > 0) {
                ejbModule.getValidation().warn(ejbName, "xml." + messageKey, xml);
            }

            int ann = classFinder.findAnnotatedClasses(handler.getAnnotationClass()).size();
            ann += classFinder.findAnnotatedMethods(handler.getAnnotationClass()).size();

            if (ann > 0) {
                ejbModule.getValidation().warn(ejbName, "ann." + messageKey, ann);
            }


        }

        private <A extends Annotation> void processAttributes(AnnotationHandler<A> handler, Class<?> clazz, ClassFinder classFinder) {
            Map<String, List<MethodAttribute>> existingDeclarations = handler.getExistingDeclarations();

            // SET THE DEFAULT
            final Class<A> annotationClass = handler.getAnnotationClass();

            if (!hasMethodAttribute("*", null, existingDeclarations)) {
                for (Class<?> type : ancestors(clazz)) {
                    if (!hasMethodAttribute("*", type, existingDeclarations)) {
                        A attribute = type.getAnnotation(annotationClass);
                        if (attribute != null) {
                            handler.addClassLevelDeclaration(attribute, type);
                        }
                    }
                }
            }


            List<Method> methods = classFinder.findAnnotatedMethods(annotationClass);
            for (Method method : methods) {
                A attribute = method.getAnnotation(annotationClass);
                if (!existingDeclarations.containsKey(method.getName())) {
                    // no method with this name in descriptor
                    handler.addMethodLevelDeclaration(attribute, method);
                } else {
                    // method name already declared
                    List<MethodAttribute> list = existingDeclarations.get(method.getName());
                    for (MethodAttribute mtx : list) {
                        MethodParams methodParams = mtx.getMethodParams();
                        if (methodParams == null) {
                            // params not specified, so this is more specific
                            handler.addMethodLevelDeclaration(attribute, method);
                        } else {
                            List<String> params1 = methodParams.getMethodParam();
                            String[] params2 = asStrings(method.getParameterTypes());
                            if (params1.size() != params2.length) {
                                // params not the same
                                handler.addMethodLevelDeclaration(attribute, method);
                            } else {
                                for (int i = 0; i < params1.size(); i++) {
                                    String a = params1.get(i);
                                    String b = params2[i];
                                    if (!a.equals(b)) {
                                        // params not the same
                                        handler.addMethodLevelDeclaration(attribute, method);
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
         * classes then creates a ClassFinder from that list which
         * can be used for easy annotation scanning.
         *
         * @param classes
         * @return
         */
        private ClassFinder createInheritedClassFinder(Class<?>... classes) {
            List<Class> parents = new ArrayList<Class>();
            for (Class<?> clazz : classes) {
                parents.addAll(ancestors(clazz));
            }

            return new ClassFinder(parents);
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
        Class getDeclaringClass();

        String getName();

        Class getType();
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

        public Class getType() {
            return setter.getParameterTypes()[0];
        }

        public Class getDeclaringClass() {
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

        public Class getType() {
            return field.getType();
        }

        public String toString() {
            return field.toString();
        }

        public Class getDeclaringClass() {
            return field.getDeclaringClass();
        }

        public String getName() {
            return field.getName();
        }
    }

}
