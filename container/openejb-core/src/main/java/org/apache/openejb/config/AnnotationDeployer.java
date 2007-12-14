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

import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.core.webservices.JaxWsUtils;
import org.apache.openejb.jee.ActivationConfig;
import org.apache.openejb.jee.ApplicationClient;
import org.apache.openejb.jee.AroundInvoke;
import org.apache.openejb.jee.AssemblyDescriptor;
import org.apache.openejb.jee.ContainerTransaction;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.EjbLocalRef;
import org.apache.openejb.jee.EjbRef;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.ExcludeList;
import org.apache.openejb.jee.Filter;
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
import org.apache.openejb.jee.MethodParams;
import org.apache.openejb.jee.MethodPermission;
import org.apache.openejb.jee.MethodTransaction;
import org.apache.openejb.jee.NamedMethod;
import org.apache.openejb.jee.PersistenceContextRef;
import org.apache.openejb.jee.PersistenceContextType;
import org.apache.openejb.jee.PersistenceUnitRef;
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
import org.apache.openejb.jee.StatefulBean;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.jee.Tag;
import org.apache.openejb.jee.TimerConsumer;
import org.apache.openejb.jee.TldTaglib;
import org.apache.openejb.jee.TransAttribute;
import org.apache.openejb.jee.TransactionType;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.jee.HandlerChains;
import org.apache.openejb.jee.Handler;
import org.apache.openejb.jee.WebserviceDescription;
import org.apache.openejb.jee.PortComponent;
import static org.apache.openejb.util.Join.join;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.xbean.finder.ClassFinder;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.annotation.Resources;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.annotation.security.RunAs;
import javax.ejb.ApplicationException;
import javax.ejb.EJB;
import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.EJBs;
import javax.ejb.Init;
import javax.ejb.Local;
import javax.ejb.LocalHome;
import javax.ejb.MessageDriven;
import javax.ejb.PostActivate;
import javax.ejb.PrePassivate;
import javax.ejb.Remote;
import javax.ejb.RemoteHome;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.ExcludeClassInterceptors;
import javax.interceptor.ExcludeDefaultInterceptors;
import javax.interceptor.Interceptors;
import javax.jws.WebService;
import javax.jws.HandlerChain;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContexts;
import javax.persistence.PersistenceUnit;
import javax.persistence.PersistenceUnits;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityManager;
import javax.xml.ws.WebServiceProvider;
import javax.xml.ws.WebServiceRef;
import javax.xml.ws.WebServiceRefs;
import javax.xml.ws.Service;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

/**
 * @version $Rev$ $Date$
 */
public class AnnotationDeployer implements DynamicDeployer {
    public static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP, AnnotationDeployer.class.getPackage().getName());

    private static final ThreadLocal<ValidationContext> validationContext = new ThreadLocal<ValidationContext>();

    private final DiscoverAnnotatedBeans discoverAnnotatedBeans;
    private final ProcessAnnotatedBeans processAnnotatedBeans;
    private final EnvEntriesPropertiesDeployer envEntriesPropertiesDeployer;

    public AnnotationDeployer() {
        discoverAnnotatedBeans = new DiscoverAnnotatedBeans();
        processAnnotatedBeans = new ProcessAnnotatedBeans();
        envEntriesPropertiesDeployer = new EnvEntriesPropertiesDeployer();
    }

    public AppModule deploy(AppModule appModule) throws OpenEJBException {
        validationContext.set(appModule.getValidation());
        try {
            appModule = discoverAnnotatedBeans.deploy(appModule);
            appModule = envEntriesPropertiesDeployer.deploy(appModule);
            appModule = processAnnotatedBeans.deploy(appModule);
            return appModule;
        } finally {
            validationContext.remove();
        }
    }

    public WebModule deploy(WebModule webModule) throws OpenEJBException {
        validationContext.set(webModule.getValidation());
        try {
            webModule = discoverAnnotatedBeans.deploy(webModule);
            webModule = envEntriesPropertiesDeployer.deploy(webModule);
            webModule = processAnnotatedBeans.deploy(webModule);
            return webModule;
        } finally {
            validationContext.remove();
        }
    }

    public static class DiscoverAnnotatedBeans implements DynamicDeployer {
        public static final Set<String> knownResourceEnvTypes = new TreeSet<String>(Arrays.asList(
                "javax.ejb.SessionContext",
                "javax.ejb.EntityContext",
                "javax.ejb.MessageDrivenContext",
                "javax.transaction.UserTransaction",
                "javax.jms.Queue",
                "javax.jms.Topic",
                "javax.xml.ws.WebServiceContext",
                "javax.ejb.TimerService"
        ));

        public static final Set<String> knownEnvironmentEntries = new TreeSet<String>(Arrays.asList(
                "boolean", "java.lang.Boolean",
                "char",    "java.lang.Character",
                "byte",    "java.lang.Byte",
                "short",   "java.lang.Short",
                "int",     "java.lang.Integer",
                "long",    "java.lang.Long",
                "float",   "java.lang.Float",
                "double",  "java.lang.Double",
                "java.lang.String"
        ));

        public AppModule deploy(AppModule appModule) throws OpenEJBException {
            for (EjbModule ejbModule : appModule.getEjbModules()) {
                validationContext.set(ejbModule.getValidation());
                try {
                    deploy(ejbModule);
                } finally {
                    validationContext.remove();
                }
            }
            for (ClientModule clientModule : appModule.getClientModules()) {
                validationContext.set(clientModule.getValidation());
                try {
                    deploy(clientModule);
                } finally {
                    validationContext.remove();
                }
            }
            for (ConnectorModule connectorModule : appModule.getResourceModules()) {
                validationContext.set(connectorModule.getValidation());
                try {
                    deploy(connectorModule);
                } finally {
                    validationContext.remove();
                }
            }
            for (WebModule webModule : appModule.getWebModules()) {
                validationContext.set(webModule.getValidation());
                try {
                    deploy(webModule);
                } finally {
                    validationContext.remove();
                }
            }
            return appModule;
        }

        public ClientModule deploy(ClientModule clientModule) throws OpenEJBException {
            return clientModule;
        }

        public ConnectorModule deploy(ConnectorModule connectorModule) throws OpenEJBException {
            return connectorModule;
        }

        public WebModule deploy(WebModule webModule) throws OpenEJBException {
            WebApp webApp = webModule.getWebApp();
            if (webApp != null && (webApp.isMetadataComplete() || !webApp.getServlet().isEmpty())) return webModule;

            ClassFinder finder;
            try {
                finder = new ClassFinder(webModule.getClassLoader());
            } catch (Exception e) {
                DeploymentLoader.logger.warning("Unable to scrape for @WebService or @WebServiceProvider annotations. ClassFinder failed.", e);
                return webModule;
            }

            List<Class> classes = new ArrayList<Class>();
            classes.addAll(finder.findAnnotatedClasses(WebService.class));
            classes.addAll(finder.findAnnotatedClasses(WebServiceProvider.class));
            for (Class<?> webServiceClass : classes) {
                int modifiers = webServiceClass.getModifiers();
                if (!Modifier.isPublic(modifiers) || Modifier.isFinal(modifiers) || Modifier.isAbstract(modifiers)) {
                    continue;
                }

                // create webApp and webservices objects if they don't exist already
                if (webApp == null) {
                    webApp = new WebApp();
                    webModule.setWebApp(webApp);
                }

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

            ClassFinder finder;
            if (ejbModule.getJarLocation() != null) {
                try {
                    String location = ejbModule.getJarLocation();
                    File file = new File(location);

                    URL url;
                    if (file.exists()) {
                        url = file.toURL();
                    } else {
                        url = new URL(location);
                    }
                    finder = new ClassFinder(ejbModule.getClassLoader(), url);
                } catch (MalformedURLException e) {
                    DeploymentLoader.logger.warning("Unable to scrape for @Stateful, @Stateless or @MessageDriven annotations. EjbModule URL not valid: " + ejbModule.getJarLocation());
                    return ejbModule;
                }
            } else {
                try {
                    finder = new ClassFinder(ejbModule.getClassLoader());
                } catch (Exception e) {
                    DeploymentLoader.logger.warning("Unable to scrape for @Stateful, @Stateless or @MessageDriven annotations. ClassFinder failed.", e);
                    return ejbModule;
                }
            }

            /* 19.2:  ejb-name: Default is the unqualified name of the bean class */

            EjbJar ejbJar = ejbModule.getEjbJar();
            List<Class> classes = finder.findAnnotatedClasses(Stateless.class);
            for (Class<?> beanClass : classes) {
                Stateless stateless = beanClass.getAnnotation(Stateless.class);
                String ejbName = stateless.name().length() == 0 ? beanClass.getSimpleName() : stateless.name();
                EnterpriseBean enterpriseBean = ejbJar.getEnterpriseBean(ejbName);
                if (enterpriseBean == null) {
                    enterpriseBean = new StatelessBean(ejbName, beanClass.getName());
                    ejbJar.addEnterpriseBean(enterpriseBean);
                }
                if (enterpriseBean.getEjbClass() == null) {
                    enterpriseBean.setEjbClass(beanClass.getName());
                }
                if (enterpriseBean instanceof SessionBean) {
                    SessionBean sessionBean = (SessionBean) enterpriseBean;
                    sessionBean.setSessionType(SessionType.STATELESS);
                }
            }

            classes = finder.findAnnotatedClasses(Stateful.class);
            for (Class<?> beanClass : classes) {
                Stateful stateful = beanClass.getAnnotation(Stateful.class);
                String ejbName = stateful.name().length() == 0 ? beanClass.getSimpleName() : stateful.name();
                EnterpriseBean enterpriseBean = ejbJar.getEnterpriseBean(ejbName);
                if (enterpriseBean == null) {
                    enterpriseBean = new StatefulBean(ejbName, beanClass.getName());
                    ejbJar.addEnterpriseBean(enterpriseBean);
                }
                if (enterpriseBean.getEjbClass() == null) {
                    enterpriseBean.setEjbClass(beanClass.getName());
                }
                if (enterpriseBean instanceof SessionBean) {
                    SessionBean sessionBean = (SessionBean) enterpriseBean;
                    sessionBean.setSessionType(SessionType.STATEFUL);
                }
            }

            classes = finder.findAnnotatedClasses(MessageDriven.class);
            for (Class<?> beanClass : classes) {
                MessageDriven mdb = beanClass.getAnnotation(MessageDriven.class);
                String ejbName = mdb.name().length() == 0 ? beanClass.getSimpleName() : mdb.name();
                MessageDrivenBean messageBean = (MessageDrivenBean) ejbJar.getEnterpriseBean(ejbName);
                if (messageBean == null) {
                    messageBean = new MessageDrivenBean(ejbName);
                    ejbJar.addEnterpriseBean(messageBean);
                }
                if (messageBean.getEjbClass() == null) {
                    messageBean.setEjbClass(beanClass.getName());
                }
            }

            classes = finder.findAnnotatedClasses(ApplicationException.class);
            if (!classes.isEmpty()) {
                if (ejbJar.getAssemblyDescriptor() == null) {
                    ejbJar.setAssemblyDescriptor(new AssemblyDescriptor());
                }
            }
            for (Class<?> exceptionClass : classes) {
                ApplicationException annotation = exceptionClass.getAnnotation(ApplicationException.class);
                org.apache.openejb.jee.ApplicationException exception = new org.apache.openejb.jee.ApplicationException(exceptionClass.getName(), annotation.rollback());
                ejbJar.getAssemblyDescriptor().getApplicationException().add(exception);
            }

            return ejbModule;
        }
    }

    public static class ProcessAnnotatedBeans implements DynamicDeployer {
        public static final Set<String> knownResourceEnvTypes = new TreeSet<String>(Arrays.asList(
                "javax.ejb.SessionContext",
                "javax.ejb.EntityContext",
                "javax.ejb.MessageDrivenContext",
                "javax.transaction.UserTransaction",
                "javax.jms.Queue",
                "javax.jms.Topic",
                "javax.xml.ws.WebServiceContext",
                "javax.ejb.TimerService"
        ));

        public static final Set<String> knownEnvironmentEntries = new TreeSet<String>(Arrays.asList(
                "boolean", "java.lang.Boolean",
                "char",    "java.lang.Character",
                "byte",    "java.lang.Byte",
                "short",   "java.lang.Short",
                "int",     "java.lang.Integer",
                "long",    "java.lang.Long",
                "float",   "java.lang.Float",
                "double",  "java.lang.Double",
                "java.lang.String"
        ));

        public AppModule deploy(AppModule appModule) throws OpenEJBException {
            for (EjbModule ejbModule : appModule.getEjbModules()) {
                deploy(ejbModule);
            }
            for (ClientModule clientModule : appModule.getClientModules()) {
                deploy(clientModule);
            }
            for (ConnectorModule connectorModule : appModule.getResourceModules()) {
                deploy(connectorModule);
            }
            for (WebModule webModule : appModule.getWebModules()) {
                deploy(webModule);
            }
            return appModule;
        }

        public ClientModule deploy(ClientModule clientModule) throws OpenEJBException {
            if (clientModule.getApplicationClient() != null && clientModule.getApplicationClient().isMetadataComplete()) return clientModule;

            ClassLoader classLoader = clientModule.getClassLoader();
            Class<?> clazz;
            try {
                clazz = classLoader.loadClass(clientModule.getMainClass());
            } catch (ClassNotFoundException e) {
                throw new OpenEJBException("Unable to load Client main-class: " + clientModule.getMainClass(), e);
            }
            ApplicationClient client = clientModule.getApplicationClient();
            ClassFinder inheritedClassFinder = createInheritedClassFinder(clazz);
            buildAnnotatedRefs(client, inheritedClassFinder, classLoader);
            processWebServiceClientHandlers(client, classLoader);

            return clientModule;
        }

        public ConnectorModule deploy(ConnectorModule connectorModule) throws OpenEJBException {
            // resource modules currently don't have any annotations
            return connectorModule;
        }

        public WebModule deploy(WebModule webModule) throws OpenEJBException {
            WebApp webApp = webModule.getWebApp();
            if (webApp != null && webApp.isMetadataComplete()) return webModule;

            Set<Class<?>> classes = new HashSet<Class<?>>();
            ClassLoader classLoader = webModule.getClassLoader();
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

            ClassFinder inheritedClassFinder = createInheritedClassFinder(classes.toArray(new Class<?>[classes.size()]));

            // Currently we only process the JNDI annotations for web applications
            buildAnnotatedRefs(webApp, inheritedClassFinder, classLoader);
            processWebServiceClientHandlers(webApp, classLoader);

            return webModule;
        }

        public EjbModule deploy(EjbModule ejbModule) throws OpenEJBException {
            if (ejbModule.getEjbJar() != null && ejbModule.getEjbJar().isMetadataComplete()) return ejbModule;

            ValidationContext validation = ejbModule.getValidation();

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

                processCallbacks(bean, inheritedClassFinder);

                if (bean.getTransactionType() == null) {
                    TransactionManagement tx = clazz.getAnnotation(TransactionManagement.class);
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
                if (assemblyDescriptor == null) {
                    assemblyDescriptor = new AssemblyDescriptor();
                    ejbModule.getEjbJar().setAssemblyDescriptor(assemblyDescriptor);
                }

                if (bean.getTransactionType() == TransactionType.CONTAINER) {
                    Map<String, List<MethodTransaction>> methodTransactions = assemblyDescriptor.getMethodTransactions(ejbName);

                    // SET THE DEFAULT
                    if (!methodTransactions.containsKey("*")) {
                        TransactionAttribute attribute = clazz.getAnnotation(TransactionAttribute.class);
                        if (attribute != null) {
                            ContainerTransaction ctx = new ContainerTransaction(cast(attribute.value()), ejbName, "*");
                            assemblyDescriptor.getContainerTransaction().add(ctx);
                        }
                    }

                    List<Method> methods = classFinder.findAnnotatedMethods(TransactionAttribute.class);
                    for (Method method : methods) {
                        TransactionAttribute attribute = method.getAnnotation(TransactionAttribute.class);
                        if (!methodTransactions.containsKey(method.getName())) {
                            // no method with this name in descriptor
                            addContainerTransaction(attribute, ejbName, method, assemblyDescriptor);
                        } else {
                            // method name already declared
                            List<MethodTransaction> list = methodTransactions.get(method.getName());
                            for (MethodTransaction mtx : list) {
                                MethodParams methodParams = mtx.getMethodParams();
                                if (methodParams == null) {
                                    // params not specified, so this is more specific
                                    addContainerTransaction(attribute, ejbName, method, assemblyDescriptor);
                                } else {
                                    List<String> params1 = methodParams.getMethodParam();
                                    String[] params2 = asStrings(method.getParameterTypes());
                                    if (params1.size() != params2.length) {
                                        // params not the same
                                        addContainerTransaction(attribute, ejbName, method, assemblyDescriptor);
                                    } else {
                                        for (int i = 0; i < params1.size(); i++) {
                                            String a = params1.get(i);
                                            String b = params2[i];
                                            if (!a.equals(b)) {
                                                // params not the same
                                                addContainerTransaction(attribute, ejbName, method, assemblyDescriptor);
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                RolesAllowed rolesAllowed = clazz.getAnnotation(RolesAllowed.class);
                if (rolesAllowed != null) {
                    MethodPermission methodPermission = new MethodPermission();
                    methodPermission.getRoleName().addAll(Arrays.asList(rolesAllowed.value()));
                    methodPermission.getMethod().add(new org.apache.openejb.jee.Method(ejbName, "*"));
                    assemblyDescriptor.getMethodPermission().add(methodPermission);
                }

                for (Method method : classFinder.findAnnotatedMethods(RolesAllowed.class)) {
                    rolesAllowed = method.getAnnotation(RolesAllowed.class);
                    MethodPermission methodPermission = new MethodPermission();
                    methodPermission.getRoleName().addAll(Arrays.asList(rolesAllowed.value()));
                    methodPermission.getMethod().add(new org.apache.openejb.jee.Method(ejbName, method));
                    assemblyDescriptor.getMethodPermission().add(methodPermission);
                }

                PermitAll permitAll = clazz.getAnnotation(PermitAll.class);
                if (permitAll != null) {
                    MethodPermission methodPermission = new MethodPermission();
                    methodPermission.setUnchecked(true);
                    methodPermission.getMethod().add(new org.apache.openejb.jee.Method(ejbName, "*"));
                    assemblyDescriptor.getMethodPermission().add(methodPermission);
                }

                for (Method method : classFinder.findAnnotatedMethods(PermitAll.class)) {
                    MethodPermission methodPermission = new MethodPermission();
                    methodPermission.setUnchecked(true);
                    methodPermission.getMethod().add(new org.apache.openejb.jee.Method(ejbName, method));
                    assemblyDescriptor.getMethodPermission().add(methodPermission);
                }

                for (Method method : classFinder.findAnnotatedMethods(DenyAll.class)) {
                    ExcludeList excludeList = assemblyDescriptor.getExcludeList();
                    excludeList.addMethod(new org.apache.openejb.jee.Method(ejbName, method));
                }

                RunAs runAs = clazz.getAnnotation(RunAs.class);
                if (runAs != null && bean.getSecurityIdentity() == null) {
                    SecurityIdentity securityIdentity = new SecurityIdentity();
                    securityIdentity.setRunAs(runAs.value());
                    bean.setSecurityIdentity(securityIdentity);
                }

                DeclareRoles declareRoles = clazz.getAnnotation(DeclareRoles.class);
                if (declareRoles != null && bean instanceof RemoteBean){
                    RemoteBean remoteBean = (RemoteBean) bean;
                    List<SecurityRoleRef> securityRoleRefs = remoteBean.getSecurityRoleRef();
                    for (String role : declareRoles.value()) {
                        securityRoleRefs.add(new SecurityRoleRef(role));
                    }
                }

                Interceptors interceptors = clazz.getAnnotation(Interceptors.class);
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
                }

                for (Method method : classFinder.findAnnotatedMethods(Interceptors.class)) {
                    interceptors = method.getAnnotation(Interceptors.class);
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

                if (bean instanceof RemoteBean) {
                    RemoteBean remoteBean = (RemoteBean) bean;

                    if (remoteBean.getHome() == null) {
                        RemoteHome remoteHome = clazz.getAnnotation(RemoteHome.class);
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

                    if (remoteBean.getLocalHome() == null) {
                        LocalHome localHome = clazz.getAnnotation(LocalHome.class);
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

                    if (remoteBean instanceof SessionBean) {
                        SessionBean sessionBean = (SessionBean) remoteBean;

                        // Anything declared in the xml is also not eligable
                        List<String> declared = new ArrayList<String>();
                        declared.addAll(sessionBean.getBusinessLocal());
                        declared.addAll(sessionBean.getBusinessRemote());
                        declared.add(sessionBean.getHome());
                        declared.add(sessionBean.getRemote());
                        declared.add(sessionBean.getLocalHome());
                        declared.add(sessionBean.getLocal());
                        declared.add(sessionBean.getServiceEndpoint());

                        List<Class<?>> interfaces = new ArrayList<Class<?>>();
                        for (Class<?> interfce : clazz.getInterfaces()) {
                            String name = interfce.getName();
                            if (!name.equals("java.io.Serializable") &&
                                    !name.equals("java.io.Externalizable") &&
                                    !name.startsWith("javax.ejb.") &&
                                    !declared.contains(interfce.getName())) {
                                interfaces.add(interfce);
                            }
                        }

                        List<Class> remotes = new ArrayList<Class>();
                        Remote remote = clazz.getAnnotation(Remote.class);
                        if (remote != null) {
                            if (remote.value().length == 0) {
                                if (interfaces.size() != 1) {
                                    validation.fail(ejbName, "ann.remote.noAttributes", join(", ", interfaces));
                                } else if (clazz.getAnnotation(Local.class) != null) {
                                    validation.fail(ejbName, "ann.remoteLocal.ambiguous", join(", ", interfaces));
                                } else if (interfaces.get(0).getAnnotation(Local.class) != null) {
                                    validation.fail(ejbName, "ann.remoteLocal.conflict", join(", ", interfaces));
                                } else {
                                    validateRemoteInterface(interfaces.get(0), validation, ejbName);
                                    remotes.add(interfaces.get(0));
                                    interfaces.remove(0);
                                }
                            } else for (Class interfce : remote.value()) {
                                validateRemoteInterface(interfce, validation, ejbName);
                                remotes.add(interfce);
                                interfaces.remove(interfce);
                            }
                        }

                        List<Class> locals = new ArrayList<Class>();
                        Local local = clazz.getAnnotation(Local.class);
                        if (local != null) {
                            if (local.value().length == 0) {
                                if (interfaces.size() != 1) {
                                    validation.fail(ejbName, "ann.local.noAttributes", join(", ", interfaces));
                                } else if (clazz.getAnnotation(Remote.class) != null) {
                                    validation.fail(ejbName, "ann.localRemote.ambiguous", join(", ", interfaces));
                                } else if (interfaces.get(0).getAnnotation(Remote.class) != null) {
                                    validation.fail(ejbName, "ann.localRemote.conflict", join(", ", interfaces));
                                } else {
                                    validateLocalInterface(interfaces.get(0), validation, ejbName);
                                    locals.add(interfaces.get(0));
                                    interfaces.remove(0);
                                }
                            } else for (Class interfce : local.value()) {
                                validateLocalInterface(interfce, validation, ejbName);
                                locals.add(interfce);
                                interfaces.remove(interfce);
                            }
                        }

                        if (sessionBean.getServiceEndpoint() == null) {
                            WebService webService = clazz.getAnnotation(WebService.class);
                            if (webService != null) {
                                String endpointInterfaceName = webService.endpointInterface();
                                if (!endpointInterfaceName.equals("")){
                                    try {
                                        sessionBean.setServiceEndpoint(endpointInterfaceName);
                                        Class endpointInterface = Class.forName(endpointInterfaceName, false, ejbModule.getClassLoader());
                                        interfaces.remove(endpointInterface);
                                    } catch (ClassNotFoundException e) {
                                        throw new IllegalStateException("Class not found @WebService.endpointInterface: "+endpointInterfaceName, e);
                                    }
                                } else {
                                    sessionBean.setServiceEndpoint(DeploymentInfo.ServiceEndpoint.class.getName());
                                }
                            } else if (clazz.isAnnotationPresent(WebServiceProvider.class)) {
                                sessionBean.setServiceEndpoint(DeploymentInfo.ServiceEndpoint.class.getName());
                            }
                        }

                        for (Class interfce : copy(interfaces)) {
                            if (interfce.isAnnotationPresent(WebService.class)) {
                                if (sessionBean.getServiceEndpoint().equals(DeploymentInfo.ServiceEndpoint.class.getName())) {
                                    sessionBean.setServiceEndpoint(interfce.getName());
                                }
                                interfaces.remove(interfce);
                            } else if (interfce.isAnnotationPresent(Remote.class)) {
                                remotes.add(interfce);
                                interfaces.remove(interfce);
                            } else {
                                locals.add(interfce);
                                interfaces.remove(interfce);
                            }
                        }

                        for (Class interfce : remotes) {
                            sessionBean.addBusinessRemote(interfce.getName());
                        }

                        for (Class interfce : locals) {
                            sessionBean.addBusinessLocal(interfce.getName());
                        }
                    }
                }

                if (bean instanceof MessageDrivenBean) {
                    MessageDrivenBean mdb = (MessageDrivenBean) bean;
                    MessageDriven messageDriven = clazz.getAnnotation(MessageDriven.class);
                    if (messageDriven != null) {
                        Class<?> interfce = messageDriven.messageListenerInterface();
                        if (interfce != null && !interfce.equals(Object.class)) {
                            if (!interfce.isInterface()) {
                                throw new OpenEJBException("MessageListenerInterface property of @MessageDriven is not an interface");
                            }
                            mdb.setMessagingType(interfce.getName());
                        }
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
                                throw new IllegalStateException(msg);
                            }
                            mdb.setMessagingType(interfaces.get(0).getName());
                        }
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

                processCallbacks(interceptor, inheritedClassFinder);

                buildAnnotatedRefs(interceptor, inheritedClassFinder, classLoader);
                processWebServiceClientHandlers(interceptor, classLoader);

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

        private ClassFinder createInheritedClassFinder(Class<?>... classes) {
            List<Class> parents = new ArrayList<Class>();
            for (Class<?> clazz : classes) {
                parents.add(clazz);
                Class parent = clazz;
                while ((parent = parent.getSuperclass()) != null) {
                    parents.add(parent);
                }
            }

            return new ClassFinder(parents);
        }

        private void processCallbacks(Lifecycle bean, ClassFinder classFinder) {
            LifecycleCallback postConstruct = getFirst(bean.getPostConstruct());
            if (postConstruct == null) {
                for (Method method : classFinder.findAnnotatedMethods(PostConstruct.class)) {
                    bean.getPostConstruct().add(new LifecycleCallback(method));
                }
            }

            LifecycleCallback preDestroy = getFirst(bean.getPreDestroy());
            if (preDestroy == null) {
                for (Method method : classFinder.findAnnotatedMethods(PreDestroy.class)) {
                    bean.getPreDestroy().add(new LifecycleCallback(method));
                }
            }

            AroundInvoke aroundInvoke = getFirst(bean.getAroundInvoke());
            if (aroundInvoke == null) {
                for (Method method : classFinder.findAnnotatedMethods(javax.interceptor.AroundInvoke.class)) {
                    bean.getAroundInvoke().add(new AroundInvoke(method));
                }
            }

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

                LifecycleCallback postActivate = getFirst(session.getPostActivate());
                if (postActivate == null) {
                    for (Method method : classFinder.findAnnotatedMethods(PostActivate.class)) {
                        session.getPostActivate().add(new LifecycleCallback(method));
                    }
                }

                LifecycleCallback prePassivate = getFirst(session.getPrePassivate());
                if (prePassivate == null) {
                    for (Method method : classFinder.findAnnotatedMethods(PrePassivate.class)) {
                        session.getPrePassivate().add(new LifecycleCallback(method));
                    }
                }

                List<Method> initMethods = classFinder.findAnnotatedMethods(Init.class);
                for (Method method : initMethods) {
                    InitMethod initMethod = new InitMethod(method);

                    Init init = method.getAnnotation(Init.class);
                    if (init.value() != null && !init.value().equals("")) {
                        initMethod.setCreateMethod(init.value());
                    }

                    session.getInitMethod().add(initMethod);
                }

                List<Method> removeMethods = classFinder.findAnnotatedMethods(Remove.class);
                Map<NamedMethod,RemoveMethod> declaredRemoveMethods = new HashMap<NamedMethod,RemoveMethod>();
                for (RemoveMethod removeMethod : session.getRemoveMethod()) {
                    declaredRemoveMethods.put(removeMethod.getBeanMethod(), removeMethod);
                }
                for (Method method : removeMethods) {
                    Remove remove = method.getAnnotation(Remove.class);
                    RemoveMethod removeMethod = new RemoveMethod(method, remove.retainIfException());

                    RemoveMethod declaredRemoveMethod = declaredRemoveMethods.get(removeMethod.getBeanMethod());

                    if (declaredRemoveMethod == null) {
                        session.getRemoveMethod().add(removeMethod);
                    } else if (!declaredRemoveMethod.isExplicitlySet()){
                        declaredRemoveMethod.setRetainIfException(remove.retainIfException());
                    }
                }
            }
        }

        private void buildAnnotatedRefs(JndiConsumer consumer, ClassFinder classFinder, ClassLoader classLoader) throws OpenEJBException {
            //
            // @EJB
            //

            List<EJB> ejbList = new ArrayList<EJB>();
            for (Class<?> clazz : classFinder.findAnnotatedClasses(EJBs.class)) {
                EJBs ejbs = clazz.getAnnotation(EJBs.class);
                ejbList.addAll(Arrays.asList(ejbs.value()));
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
                resourceList.addAll(Arrays.asList(resources.value()));
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
                webservicerefList.addAll(Arrays.asList(webServiceRefs.value()));
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
                persistenceUnitList.addAll(Arrays.asList(persistenceUnits.value()));
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
                persistenceContextList.addAll(Arrays.asList(persistenceContexts.value()));
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

        private void processWebServiceClientHandlers(JndiConsumer consumer, ClassLoader classLoader) throws OpenEJBException {
            if (System.getProperty("duct tape") != null) return;
            Set<Class<?>> processedClasses = new HashSet<Class<?>>();
            Set<Class<?>> handlerClasses = new HashSet<Class<?>>();
            do {
                // get unprocessed handler classes
                handlerClasses.clear();
                for (ServiceRef serviceRef : consumer.getServiceRef()) {
                    HandlerChains chains = serviceRef.getHandlerChains();
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
                buildAnnotatedRefs(consumer, handlerClassFinder, classLoader);
                processedClasses.addAll(handlerClasses);
            } while (!handlerClasses.isEmpty());
        }

        private void buildPersistenceUnit(JndiConsumer consumer, PersistenceUnit persistenceUnit, Member member) throws OpenEJBException {
            // Get the ref-name
            String refName = persistenceUnit.name();
            if (refName.equals("")) {
                refName = (member == null) ? null : member.getDeclaringClass().getName() + "/" + member.getName();
            }

            if (refName == null && member == null) {
                validationContext.get().fail(consumer.getJndiConsumerName(), "presistenceUnitAnnotation.onClassWithNoName", persistenceUnit.unitName());
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
                if (EntityManager.class.isAssignableFrom(type)){
                    validationContext.get().fail(consumer.getJndiConsumerName(), "presistenceUnitAnnotation.onEntityManager", persistenceUnitRef.getName());
                } else if (!EntityManagerFactory.class.isAssignableFrom(type)){
                    validationContext.get().fail(consumer.getJndiConsumerName(), "presistenceUnitAnnotation.onNonEntityManagerFactory", persistenceUnitRef.getName());
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

        private void buildResource(JndiConsumer consumer, Resource resource, Member member) {
            // Get the ref-name
            String refName = resource.name();
            if (refName.equals("")) {
                refName = (member == null) ? null : member.getDeclaringClass().getName() + "/" + member.getName();
            }

            if (member == null) {
                boolean shouldReturn = false;
                if (resource.name().equals("")){
                    validationContext.get().fail(consumer.getJndiConsumerName(), "resourceAnnotation.onClassWithNoName");
                    shouldReturn = true;
                }
                if (resource.type().equals(Object.class)){
                    validationContext.get().fail(consumer.getJndiConsumerName(), "resourceAnnotation.onClassWithNoType");
                    shouldReturn = true;
                }
                if (shouldReturn) return;
            }

            JndiReference reference = consumer.getEnvEntryMap().get(refName);
            if (reference == null) {

                if (member != null) { // Little quick validation for common mistake
                    Class type = member.getType();
                    boolean shouldReturn = false;
                    if (EntityManager.class.isAssignableFrom(type)){
                        validationContext.get().fail(consumer.getJndiConsumerName(), "resourceRef.onEntityManager", refName);
                        shouldReturn = true;
                    } else if (EntityManagerFactory.class.isAssignableFrom(type)){
                        validationContext.get().fail(consumer.getJndiConsumerName(), "resourceRef.onEntityManagerFactory", refName);
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
            if (reference == null) {
                return;
            }

//            reference.setName(refName);

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

            if (System.getProperty("duct tape") != null) return;

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

            if (refName == null && member == null) {
                validationContext.get().fail(consumer.getJndiConsumerName(), "presistenceContextAnnotation.onClassWithNoName", persistenceContext.unitName());
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
                if (EntityManagerFactory.class.isAssignableFrom(type)){
                    validationContext.get().fail(consumer.getJndiConsumerName(), "presistenceContextAnnotation.onEntityManagerFactory", persistenceContextRef.getName());
                } else if (!EntityManager.class.isAssignableFrom(type)){
                    validationContext.get().fail(consumer.getJndiConsumerName(), "presistenceContextAnnotation.onNonEntityManager", persistenceContextRef.getName());
                } else {
                    // Set the member name where this will be injected
                    InjectionTarget target = new InjectionTarget();
                    target.setInjectionTargetClass(member.getDeclaringClass().getName());
                    target.setInjectionTargetName(member.getName());
                    persistenceContextRef.getInjectionTarget().add(target);
                }
            }
        }

        private void buildEjbRef(JndiConsumer consumer, EJB ejb, Member member) {

            if (member == null) {
                boolean shouldReturn = false;
                if (ejb.name().equals("")){
                    validationContext.get().fail(consumer.getJndiConsumerName(), "ejbAnnotation.onClassWithNoName");
                    shouldReturn = true;
                }
                if (ejb.beanInterface().equals(Object.class)){
                    validationContext.get().fail(consumer.getJndiConsumerName(), "ejbAnnotation.onClassWithNoBeanInterface");
                    shouldReturn = true;
                }
                if (shouldReturn) return;
            }

            EjbRef ejbRef = new EjbRef();

            // This is how we deal with the fact that we don't know
            // whether to use an EjbLocalRef or EjbRef (remote).
            // We flag it uknown and let the linking code take care of
            // figuring out what to do with it.
            ejbRef.setRefType(EjbRef.Type.UNKNOWN);

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
                    ejbRef.setRefType(EjbRef.Type.REMOTE);
                } else if (EJBLocalHome.class.isAssignableFrom(interfce)) {
                    ejbRef.setHome(interfce.getName());
                    Method[] methods = interfce.getMethods();
                    for (Method method : methods) {
                        if (method.getName().startsWith("create")) {
                            ejbRef.setRemote(method.getReturnType().getName());
                            break;
                        }
                    }
                    ejbRef.setRefType(EjbRef.Type.LOCAL);
                } else {
                    ejbRef.setRemote(interfce.getName());
                    if (interfce.getAnnotation(Local.class) != null) {
                        ejbRef.setRefType(EjbRef.Type.LOCAL);
                    } else if (interfce.getAnnotation(Remote.class) != null) {
                        ejbRef.setRefType(EjbRef.Type.REMOTE);
                    }
                }
            }

            // Get the ejb-ref-name
            String refName = ejb.name();
            if (refName.equals("")) {
                refName = (member == null) ? null : member.getDeclaringClass().getName() + "/" + member.getName();
            }
            ejbRef.setEjbRefName(refName);

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
            if (remoteRefs.containsKey(ejbRef.getName())){
                EjbRef ref = remoteRefs.get(ejbRef.getName());
                if (ref.getRemote() == null) ref.setRemote(ejbRef.getRemote());
                if (ref.getHome() == null) ref.setHome(ejbRef.getHome());
                if (ref.getMappedName() == null) ref.setMappedName(ejbRef.getMappedName());
                ref.getInjectionTarget().addAll(ejbRef.getInjectionTarget());
                return;
            }

            Map<String, EjbLocalRef> localRefs = consumer.getEjbLocalRefMap();
            if (localRefs.containsKey(ejbRef.getName())){
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

        private List<Class<?>> copy(List<Class<?>> classes) {
            return new ArrayList<Class<?>>(classes);
        }

        private void addContainerTransaction(TransactionAttribute attribute, String ejbName, Method method, AssemblyDescriptor assemblyDescriptor) {
            ContainerTransaction ctx = new ContainerTransaction(cast(attribute.value()), ejbName, method.getName(), asStrings(method.getParameterTypes()));
            assemblyDescriptor.getContainerTransaction().add(ctx);
        }

        private String[] asStrings(Class[] types) {
            List<String> names = new ArrayList<String>();
            for (Class clazz : types) {
                names.add(clazz.getName());
            }
            return names.toArray(new String[names.size()]);
        }

        private TransAttribute cast(TransactionAttributeType transactionAttributeType) {
            return TransAttribute.valueOf(transactionAttributeType.toString());
        }

        private <T> T getFirst(List<T> list) {
            if (list.size() > 0) {
                return list.get(0);
            }
            return null;
        }


        private void validateRemoteInterface(Class interfce, ValidationContext validation, String ejbName) {
            validateInterface(interfce, validation, ejbName, "Remote");
        }

        private void validateLocalInterface(Class interfce, ValidationContext validation, String ejbName) {
            validateInterface(interfce, validation, ejbName, "Local");
        }

        private void validateInterface(Class interfce, ValidationContext validation, String ejbName, String annotationName) {
            if (EJBHome.class.isAssignableFrom(interfce)){
                validation.fail(ejbName, "ann.remoteOrLocal.ejbHome", annotationName, interfce.getName());
            } else if (EJBObject.class.isAssignableFrom(interfce)){
                validation.fail(ejbName, "ann.remoteOrLocal.ejbObject", annotationName, interfce.getName());
            } else if (EJBLocalHome.class.isAssignableFrom(interfce)) {
                validation.fail(ejbName, "ann.remoteOrLocal.ejbLocalHome", annotationName, interfce.getName());
            } else if (EJBLocalObject.class.isAssignableFrom(interfce)){
                validation.fail(ejbName, "ann.remoteOrLocal.ejbLocalObject", annotationName, interfce.getName());
            }
        }
    }

    public static interface Member {
        Class getDeclaringClass();

        String getName();

        Class getType();
    }

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
