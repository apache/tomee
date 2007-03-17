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

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.jee.*;
import org.apache.openejb.jee.SessionBean;
import org.apache.openejb.util.Logger;
import org.apache.xbean.finder.ClassFinder;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.annotation.Resources;
import javax.ejb.EJB;
import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBs;
import javax.ejb.Local;
import javax.ejb.LocalHome;
import javax.ejb.MessageDriven;
import javax.ejb.PostActivate;
import javax.ejb.PrePassivate;
import javax.ejb.Remote;
import javax.ejb.RemoteHome;
import javax.ejb.Stateful;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.ejb.Init;
import javax.ejb.Remove;
import javax.ejb.EJBContext;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContexts;
import javax.persistence.PersistenceProperty;
import javax.persistence.PersistenceUnit;
import javax.persistence.PersistenceUnits;
import javax.interceptor.Interceptors;
import javax.interceptor.ExcludeDefaultInterceptors;
import javax.interceptor.ExcludeClassInterceptors;
import javax.xml.ws.WebServiceRef;
import javax.xml.ws.WebServiceRefs;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @version $Rev$ $Date$
 */
public class AnnotationDeployer implements DynamicDeployer {

    public static final Logger logger = Logger.getInstance("OpenEJB.startup", AnnotationDeployer.class.getPackage().getName());
    private final DiscoverBeansInClassLoader discoverBeansInClassLoader;
    private final ProcessAnnotatedBeans processAnnotatedBeans;

    public AnnotationDeployer() {
        discoverBeansInClassLoader = new DiscoverBeansInClassLoader();
        processAnnotatedBeans = new ProcessAnnotatedBeans();
    }

    public AppModule deploy(AppModule appModule) throws OpenEJBException {
        appModule = discoverBeansInClassLoader.deploy(appModule);
        appModule = processAnnotatedBeans.deploy(appModule);
        return appModule;
    }

    public static class DiscoverBeansInClassLoader implements DynamicDeployer {

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

//        @SuppressWarnings("unchecked")
        public EjbModule deploy(EjbModule ejbModule) throws OpenEJBException {
            ClassFinder finder;
            if (ejbModule.getJarURI() != null) {
                try {
                    String location = ejbModule.getJarURI();
                    File file = new File(location);

                    URL url;
                    if (file.exists()) {
                        url = file.toURL();
                    } else {
                        url = new URL(location);
                    }
                    finder = new ClassFinder(ejbModule.getClassLoader(), url);
                } catch (MalformedURLException e) {
                    DeploymentLoader.logger.warning("Unable to scrape for @Stateful, @Stateless or @MessageDriven annotations. EjbModule URL not valid: " + ejbModule.getJarURI(), e);
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
            // TODO: Anyone know how to avoid SuppressWarnings just for the below line
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
            }

            classes = finder.findAnnotatedClasses(MessageDriven.class);
            for (Class<?> beanClass : classes) {
                MessageDriven mdb = beanClass.getAnnotation(MessageDriven.class);
                String ejbName = mdb.name().length() == 0 ? beanClass.getSimpleName() : mdb.name();
                MessageDrivenBean messageBean = (MessageDrivenBean) ejbJar.getEnterpriseBean(ejbName);
                if (messageBean == null) {
                    messageBean = new MessageDrivenBean(ejbName);
                    Class<?> interfce = mdb.messageListenerInterface();
                    if (interfce != null && !interfce.equals(Object.class)) {
                        if (!interfce.isInterface()) {
                            throw new OpenEJBException("MessageListenerInterface property of @MessageDriven is not an interface");
                        }
                        messageBean.setMessagingType(interfce.getName());
                    }
                    ejbJar.addEnterpriseBean(messageBean);
                }
                if (messageBean.getEjbClass() == null) {
                    messageBean.setEjbClass(beanClass.getName());
                }
            }

            return ejbModule;
        }
    }

    public static class ProcessAnnotatedBeans implements DynamicDeployer {

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
            ClassLoader classLoader = clientModule.getClassLoader();
            Class<?> clazz = null;
            try {
                clazz = classLoader.loadClass(clientModule.getMainClass());
            } catch (ClassNotFoundException e) {
                throw new OpenEJBException("Unable to load Client main-class: " + clientModule.getMainClass(), e);
            }
            ApplicationClient client = clientModule.getApplicationClient();

            buildAnnotatedRefs(clazz, client);

            return clientModule;
        }

        public EjbModule deploy(EjbModule ejbModule) throws OpenEJBException {

            ClassLoader classLoader = ejbModule.getClassLoader();
            EnterpriseBean[] enterpriseBeans = ejbModule.getEjbJar().getEnterpriseBeans();
            for (EnterpriseBean bean : enterpriseBeans) {
                final String ejbName = bean.getEjbName();

                Class<?> clazz = null;
                try {
                    clazz = classLoader.loadClass(bean.getEjbClass());
                } catch (ClassNotFoundException e) {
                    throw new OpenEJBException("Unable to load bean class: " + bean.getEjbClass());
                }
                ClassFinder classFinder = new ClassFinder(clazz);

                ClassFinder inheritedClassFinder = createInheritedClassFinder(clazz);

                inheritedClassFinder.getClass();
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

                processCallbacks(bean, inheritedClassFinder);

                Interceptors interceptors = clazz.getAnnotation(Interceptors.class);
                if (interceptors != null){
                    EjbJar ejbJar = ejbModule.getEjbJar();
                    for (Class interceptor : interceptors.value()) {
                        if (ejbJar.getInterceptor(interceptor.getName()) == null){
                            ejbJar.addInterceptor(new Interceptor(interceptor.getName()));
                        }
                    }

                    InterceptorBinding binding = assemblyDescriptor.addInterceptorBinding(new InterceptorBinding());
                    binding.setEjbName(bean.getEjbName());

                    for (Class interceptor : interceptors.value()) {
                        binding.getInterceptorClass().add(interceptor.getName());
                    }
                }

                for (Method method : classFinder.findAnnotatedMethods(Interceptors.class)) {
                    interceptors = method.getAnnotation(Interceptors.class);
                    if (interceptors != null){
                        EjbJar ejbJar = ejbModule.getEjbJar();
                        for (Class interceptor : interceptors.value()) {
                            if (ejbJar.getInterceptor(interceptor.getName()) == null){
                                ejbJar.addInterceptor(new Interceptor(interceptor.getName()));
                            }
                        }

                        InterceptorBinding binding = assemblyDescriptor.addInterceptorBinding(new InterceptorBinding());
                        binding.setEjbName(bean.getEjbName());

                        for (Class interceptor : interceptors.value()) {
                            binding.getInterceptorClass().add(interceptor.getName());
                        }

                        binding.setMethod(new NamedMethod(method));
                    }
                }

                ExcludeDefaultInterceptors excludeDefaultInterceptors = clazz.getAnnotation(ExcludeDefaultInterceptors.class);
                if (excludeDefaultInterceptors != null){
                    InterceptorBinding binding = assemblyDescriptor.addInterceptorBinding(new InterceptorBinding(bean));
                    binding.setExcludeDefaultInterceptors(true);
                }

                for (Method method : classFinder.findAnnotatedMethods(ExcludeDefaultInterceptors.class)) {
                    InterceptorBinding binding = assemblyDescriptor.addInterceptorBinding(new InterceptorBinding(bean));
                    binding.setExcludeDefaultInterceptors(true);
                    binding.setMethod(new NamedMethod(method));
                }

                ExcludeClassInterceptors excludeClassInterceptors = clazz.getAnnotation(ExcludeClassInterceptors.class);
                if (excludeClassInterceptors != null){
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
                                    if (method.getName().startsWith("create")){
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
                                    if (method.getName().startsWith("create")){
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
                        declared.add(sessionBean.getBusinessLocal());
                        declared.add(sessionBean.getBusinessRemote());
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
                                if (interfaces.size() != 1)
                                    throw new IllegalStateException("When annotating a bean class as @Remote with no annotation attributes, the bean must implement exactly one business interface, no more and no less.");
                                if (clazz.getAnnotation(Local.class) != null)
                                    throw new IllegalStateException("When annotating a bean class as @Remote with no annotation attributes you must not also annotate it with @Local.");
                                if (interfaces.get(0).getAnnotation(Local.class) != null)
                                    throw new IllegalStateException("When annotating a bean class as @Remote with no annotation attributes, the business interface itself must not be annotated as @Local.");

                                remotes.add(interfaces.get(0));
                                interfaces.remove(0);
                            }
                            for (Class interfce : remote.value()) {
                                remotes.add(interfce);
                                interfaces.remove(interfce);
                            }
                        }

                        List<Class> locals = new ArrayList<Class>();
                        Local local = clazz.getAnnotation(Local.class);
                        if (local != null) {
                            if (local.value().length == 0) {
                                if (interfaces.size() != 1)
                                    throw new IllegalStateException("When annotating a bean class as @Local with no annotation attributes, the bean must implement exactly one business interface, no more and no less.");
                                if (clazz.getAnnotation(Remote.class) != null)
                                    throw new IllegalStateException("When annotating a bean class as @Local with no annotation attributes you must not also annotate it with @Remote.");
                                if (interfaces.get(0).getAnnotation(Remote.class) != null)
                                    throw new IllegalStateException("When annotating a bean class as @Local with no annotation attributes, the business interface itself must not be annotated as @Remote.");

                                locals.add(interfaces.get(0));
                                interfaces.remove(0);
                            }
                            for (Class interfce : local.value()) {
                                locals.add(interfce);
                                interfaces.remove(interfce);
                            }
                        }

                        // todo This is not a real JEE annotation
                        // List<Class<?>> endpoints = new ArrayList<Class<?>>();
                        ServiceEndpoint endpoint = clazz.getAnnotation(ServiceEndpoint.class);
                        if (endpoint != null) {
                            for (Class<?> interfce : endpoint.value()) {
                                // endpoints.add(interfce);
                                interfaces.remove(interfce);
                            }
                        }

                        for (Class interfce : copy(interfaces)) {
                            if (interfce.isAnnotationPresent(Remote.class)) {
                                remotes.add(interfce);
                                interfaces.remove(interfce);
                            } else if (interfce.isAnnotationPresent(ServiceEndpoint.class)) {
                                // endpoints.add(interfce);
                                interfaces.remove(interfce);
                            } else {
                                locals.add(interfce);
                                interfaces.remove(interfce);
                            }
                        }

                        for (Class interfce : remotes) {
                            // TODO: This should be turned back into an array
                            sessionBean.setBusinessRemote(interfce.getName());
                        }

                        for (Class interfce : locals) {
                            // TODO: This should be turned back into an array
                            sessionBean.setBusinessLocal(interfce.getName());
                        }
                    }
                }

                if (bean instanceof MessageDrivenBean) {
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
                            for (javax.ejb.ActivationConfigProperty property : configProperties) {
                                ActivationConfigProperty configProperty = new ActivationConfigProperty(property.propertyName(), property.propertyValue());
                                activationConfig.getActivationConfigProperty().add(configProperty);
                            }
                        }

                        if (mdb.getMessagingType() == null) {
                            List<Class<?>> interfaces = new ArrayList<Class<?>>();
                            for (Class<?> interfce : clazz.getInterfaces()) {
                                String name = interfce.getName();
                                if (!name.equals("java.io.Serializable") &&
                                        !name.equals("java.io.Externalizable") &&
                                        !name.startsWith("javax.ejb.")) {
                                    interfaces.add(interfce);
                                }
                            }

                            if (interfaces.size() != 1) {
                                throw new IllegalStateException("When annotating a bean class as @MessageDriven without declaring messageListenerInterface, the bean must implement exactly one interface, no more and no less.");
                            }
                            mdb.setMessagingType(interfaces.get(0).getName());
                        }
                    }

                }
                buildAnnotatedRefs(clazz, bean);

            }

            Interceptor[] interceptors = ejbModule.getEjbJar().getInterceptors();
            if (interceptors != null) {
                for (Interceptor interceptor : interceptors) {
                    Class<?> clazz = null;
                    try {
                        clazz = classLoader.loadClass(interceptor.getInterceptorClass());
                    } catch (ClassNotFoundException e) {
                        throw new OpenEJBException("Unable to load interceptor class: " + interceptor.getInterceptorClass());
                    }

                    ClassFinder inheritedClassFinder = createInheritedClassFinder(clazz);

                    processCallbacks(interceptor, inheritedClassFinder);
                }
            }

            return ejbModule;
        }

        private ClassFinder createInheritedClassFinder(Class<?> clazz) {
            List<Class> parents = new ArrayList();
            parents.add(clazz);
            Class parent = clazz;
            while ((parent = parent.getSuperclass()) != null){
                parents.add(parent);
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
                    if (init.value() != null && !init.value().equals("")){
                        initMethod.setCreateMethod(init.value());
                    }

                    session.getInitMethod().add(initMethod);
                }

                List<Method> removeMethods = classFinder.findAnnotatedMethods(Remove.class);
                for (Method method : removeMethods) {
                    session.getRemoveMethod().add(new RemoveMethod(method));
                }
            }
        }

        private void buildAnnotatedRefs(Class<?> clazz, JndiConsumer consumer) throws OpenEJBException {
            List<EJB> ejbList = new ArrayList<EJB>();
            EJBs ejbs = clazz.getAnnotation(EJBs.class);
            if (ejbs != null) {
                ejbList.addAll(Arrays.asList(ejbs.value()));
            }
            EJB e = clazz.getAnnotation(EJB.class);
            if (e != null) {
                ejbList.add(e);
            }

            for (EJB ejb : ejbList) {
                buildEjbRef(consumer, ejb, null);
            }

            ClassFinder finder = new ClassFinder(clazz);

            for (Field field : finder.findAnnotatedFields(EJB.class)) {
                EJB ejb = field.getAnnotation(EJB.class);

                Member member = new FieldMember(field);

                buildEjbRef(consumer, ejb, member);
            }

            for (Method method : finder.findAnnotatedMethods(EJB.class)) {
                EJB ejb = method.getAnnotation(EJB.class);

                Member member = new MethodMember(method);

                buildEjbRef(consumer, ejb, member);
            }


            List<Resource> resourceList = new ArrayList<Resource>();
            Resources resources = clazz.getAnnotation(Resources.class);
            if (resources != null) {
                resourceList.addAll(Arrays.asList(resources.value()));
            }
            Resource r = clazz.getAnnotation(Resource.class);
            if (r != null) {
                resourceList.add(r);
            }

            for (Resource resource : resourceList) {

                buildResource(consumer, resource, null);
            }

            for (Field field : finder.findAnnotatedFields(Resource.class)) {
                Resource resource = field.getAnnotation(Resource.class);

                Member member = new FieldMember(field);

                buildResource(consumer, resource, member);
            }

            for (Method method : finder.findAnnotatedMethods(Resource.class)) {
                Resource resource = method.getAnnotation(Resource.class);

                Member member = new MethodMember(method);

                buildResource(consumer, resource, member);
            }

            List<WebServiceRef> webservicerefList = new ArrayList<WebServiceRef>();
            WebServiceRefs webservicerefs = clazz.getAnnotation(WebServiceRefs.class);
            if (webservicerefs != null) {
                webservicerefList.addAll(Arrays.asList(webservicerefs.value()));
            }
            WebServiceRef wsr = clazz.getAnnotation(WebServiceRef.class);
            if (r != null) {
                webservicerefList.add(wsr);
            }
            for (WebServiceRef webserviceref : webservicerefList) {

                buildWebServiceRef(consumer, webserviceref, null);
            }

            for (Field field : finder.findAnnotatedFields(WebServiceRef.class)) {
                WebServiceRef webserviceref = field.getAnnotation(WebServiceRef.class);

                Member member = new FieldMember(field);

                buildWebServiceRef(consumer, webserviceref, member);
            }

            for (Method method : finder.findAnnotatedMethods(WebServiceRef.class)) {
                WebServiceRef webserviceref = method.getAnnotation(WebServiceRef.class);

                Member member = new MethodMember(method);

                buildWebServiceRef(consumer, webserviceref, member);
            }

            List<PersistenceUnit> persistenceUnitList = new ArrayList<PersistenceUnit>();
            PersistenceUnits persistenceUnits = clazz.getAnnotation(PersistenceUnits.class);
            if (persistenceUnits != null) {
                persistenceUnitList.addAll(Arrays.asList(persistenceUnits.value()));
            }
            PersistenceUnit persistenceUnit = clazz.getAnnotation(PersistenceUnit.class);
            if (persistenceUnit != null) {
                persistenceUnitList.add(persistenceUnit);
            }
            for (PersistenceUnit pUnit : persistenceUnitList) {
                buildPersistenceUnit(consumer, pUnit, null);
            }
            for (Field field : finder.findAnnotatedFields(PersistenceUnit.class)) {
                PersistenceUnit pUnit = field.getAnnotation(PersistenceUnit.class);
                Member member = new FieldMember(field);
                buildPersistenceUnit(consumer, pUnit, member);
            }
            for (Method method : finder.findAnnotatedMethods(PersistenceUnit.class)) {
                PersistenceUnit pUnit = method.getAnnotation(PersistenceUnit.class);
                Member member = new MethodMember(method);
                buildPersistenceUnit(consumer, pUnit, member);
            }

            List<PersistenceContext> persistenceContextList = new ArrayList<PersistenceContext>();
            PersistenceContexts persistenceContexts = clazz.getAnnotation(PersistenceContexts.class);
            if(persistenceContexts != null){
                persistenceContextList.addAll(Arrays.asList(persistenceContexts.value()));
            }
            PersistenceContext persistenceContext = clazz.getAnnotation(PersistenceContext.class);
            if (persistenceContext != null){
                persistenceContextList.add(persistenceContext);
            }
            for (PersistenceContext pCtx : persistenceContextList) {
                buildPersistenceContext(consumer, pCtx, null);
            }
            for (Field field : finder.findAnnotatedFields(PersistenceContext.class)) {
                PersistenceContext pCtx = field.getAnnotation(PersistenceContext.class);
                Member member = new FieldMember(field);
                buildPersistenceContext(consumer, pCtx, member);
            }
            for (Method method : finder.findAnnotatedMethods(PersistenceContext.class)) {
                PersistenceContext pCtx = method.getAnnotation(PersistenceContext.class);
                Member member = new MethodMember(method);
                buildPersistenceContext(consumer, pCtx, member);
            }

        }

        private void buildPersistenceUnit(JndiConsumer consumer, PersistenceUnit persistenceUnit, Member member) throws OpenEJBException {
            // Get the ref-name
            String refName = persistenceUnit.name();
            if (refName.equals("")) {
                refName = (member == null) ? null : member.getDeclaringClass().getName() + "/" + member.getName();
            }
            if (refName == null) {
                throw new OpenEJBException("The name attribute is not specified for the class level annotation @PersistenceUnit with unitName=" + persistenceUnit.unitName()
                        + ". It is mandatory for all class level PersistenceUnit annotations.");
            }
            PersistenceUnitRef persistenceUnitRef = null;
            List<PersistenceUnitRef> persistenceUnitRefs = consumer.getPersistenceUnitRef();
            for (PersistenceUnitRef puRef : persistenceUnitRefs) {
                if (puRef.getPersistenceUnitRefName().equals(refName)) {
                    persistenceUnitRef = puRef;
                    break;
                }
            }


            if (persistenceUnitRef == null) {
                persistenceUnitRef = new PersistenceUnitRef();
                persistenceUnitRef.setPersistenceUnitName(persistenceUnit.unitName());
                persistenceUnitRef.setPersistenceUnitRefName(refName);
                persistenceUnitRefs.add(persistenceUnitRef);
            }
            if (member != null) {
                // Set the member name where this will be injected
                InjectionTarget target = new InjectionTarget();
                target.setInjectionTargetClass(member.getDeclaringClass().getName());
                target.setInjectionTargetName(member.getName());
                persistenceUnitRef.getInjectionTarget().add(target);
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

            JndiReference reference = null;

            List<EnvEntry> envEntries = consumer.getEnvEntry();
            for (EnvEntry envEntry : envEntries) {
                if (envEntry.getName().equals(refName)) {
                    reference = envEntry;
                    break;
                }
            }
            
            
            if(reference == null) {
                String type = null;
                if(resource.type() != java.lang.Object.class) {
                    type = resource.type().getName();
                } else {
                    type = member.getType().getName();                
                }
                /* TODO message-destination-refs have not been considered . Currently the types that are
                 *  not in the types in knownConnectionFactoryTypes array are considered resource-env-refs
                 *  This approach needs to be changed as it is just a hack to get the functionality working.
                 */
                
                 
                String[] knownConnectionFactoryTypes = {"javax.sql.DataSource","javax.jms.ConnectionFactory","javax.jms.QueueConnectionFactory","javax.jms.TopicConnectionFactory","javax.mail.Session","java.net.URL"};
                String[] knownEnvironmentEntries = {"java.lang.String", "java.lang.Character", "java.lang.Integer", "java.lang.Boolean", "java.lang.Double", 
                        "java.lang.Byte", "java.lang.Short", "java.lang.Long", "java.lang.Float","int","char","boolean","double","byte","short","long","float"};
                if (type.equals("javax.ejb.SessionContext")){
                    ResourceEnvRef ref = new ResourceEnvRef();
                    ref.setResourceEnvRefName(refName);
                    ref.setResourceEnvRefType(type);
                    consumer.getResourceEnvRef().add(ref);
                    reference = ref;
                } else if (contains(knownConnectionFactoryTypes,type)) {
                    ResourceRef resourceRef = null;
                    List<ResourceRef> resourceRefs = consumer.getResourceRef();
                    for (ResourceRef resRef : resourceRefs) {
                        if( resRef.getName().equals(refName)) {
                            resourceRef = resRef;
                            break;
                        }                
                    }
                            
                    if (resourceRef == null) {
                        resourceRef = new ResourceRef();
                        resourceRef.setName(refName);
                        resourceRefs.add(resourceRef);
                    }
                
                    if (resourceRef.getResAuth() == null) {
                        if (resource.authenticationType() == Resource.AuthenticationType.APPLICATION){
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
                
                    if (resourceRef.getResSharingScope() == null){
                        if (resource.shareable()) {
                            resourceRef.setResSharingScope(ResSharingScope.SHAREABLE);
                        } else {
                            resourceRef.setResSharingScope(ResSharingScope.UNSHAREABLE);
                        }
                    }
                    reference = resourceRef;
                } else if (!contains(knownEnvironmentEntries,type)){             
                                        
                    List<ResourceEnvRef> resourceEnvRefs = consumer.getResourceEnvRef();
                    ResourceEnvRef resourceEnvRef = null;
                    for (ResourceEnvRef resEnvRef : resourceEnvRefs) {
                        if(resEnvRef.getName().equals(refName)) {
                            resourceEnvRef = resEnvRef;
                            break;
                        }                    
                    }
                    if (resourceEnvRef == null) {
                        resourceEnvRef = new ResourceEnvRef();
                        resourceEnvRef.setName(refName);
                        resourceEnvRefs.add(resourceEnvRef);
                    }
                    if (resourceEnvRef.getResourceEnvRefType() == null || ("").equals(resourceEnvRef.getResourceEnvRefType())) {
                        if (resource.type() != java.lang.Object.class) {
                            resourceEnvRef.setResourceEnvRefType(resource.type().getName());
                        } else {
                            resourceEnvRef.setResourceEnvRefType(member.getType().getName());
                        }                
                    }  
                    reference = resourceEnvRef;
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

        private void buildWebServiceRef(JndiConsumer consumer, WebServiceRef webService, Member member) {

            ServiceRef serviceRef = new ServiceRef();

            String refName = webService.name();
            if (refName.equals("")) {
                refName = (member == null) ? null : member.getDeclaringClass().getName() + "/" + member.getName();
            }
            serviceRef.setServiceRefName(refName);


            if (member != null) {
                // Set the member name where this will be injected
                InjectionTarget target = new InjectionTarget();
                target.setInjectionTargetClass(member.getDeclaringClass().getName());
                target.setInjectionTargetName(member.getName());
                serviceRef.getInjectionTarget().add(target);
            }

            Class<?> interfce = webService.type();
            if (interfce.equals(Object.class)) {
                if (member != null) {
                    interfce = member.getType();
                } else {
                    interfce = webService.value();
                }
            }
            serviceRef.setServiceInterface(interfce.getName());

            // Set the mappedName, if any
            String mappedName = webService.mappedName();
            if (mappedName.equals("")) {
                mappedName = null;
            }
            serviceRef.setMappedName(mappedName);


            String wsdlLocation = webService.wsdlLocation();
            if (!wsdlLocation.equals("")) {
                serviceRef.setWsdlFile(wsdlLocation);
            }

            consumer.getServiceRef().add(serviceRef);

        }

        /**
         * Refer 16.11.2.1 Overriding Rules of EJB Core Spec for overriding rules
         * @param consumer
         * @param persistenceContext
         * @param member
         * @throws OpenEJBException
         */
        private void buildPersistenceContext(JndiConsumer consumer, PersistenceContext persistenceContext, Member member) throws OpenEJBException {
            String refName = persistenceContext.name();
            
            if(refName.equals("")) {
                refName = (member == null) ? null : member.getDeclaringClass().getName() + "/" + member.getName();
            }
            
            if(refName == null) {
                throw new OpenEJBException("The name attribute is not specified for the class level annotation @PersistenceContext with unitName="
                     + persistenceContext.unitName() + ". It is mandatory for all class level PersistenceContext annotations.");
            }
            PersistenceContextRef persistenceContextRef = null;
            
            List<PersistenceContextRef> persistenceContextRefs = consumer.getPersistenceContextRef();
            for (PersistenceContextRef pcRef : persistenceContextRefs) {
                if (pcRef.getPersistenceContextRefName().equals(refName)) {
                    persistenceContextRef = pcRef;
                    break;
                }
            }
            
            if (persistenceContextRef == null) {
                persistenceContextRef = new PersistenceContextRef();
                persistenceContextRef.setPersistenceUnitName(persistenceContext.unitName());
                persistenceContextRef.setPersistenceContextRefName(refName);
                if(persistenceContext.type() == javax.persistence.PersistenceContextType.EXTENDED) {
                    persistenceContextRef.setPersistenceContextType(PersistenceContextType.EXTENDED);
                } else {
                    persistenceContextRef.setPersistenceContextType(PersistenceContextType.TRANSACTION);
                }                
                persistenceContextRefs.add(persistenceContextRef);
            } else {
                if (persistenceContextRef.getPersistenceUnitName() == null || ("").equals(persistenceContextRef.getPersistenceUnitName())) {
                    persistenceContextRef.setPersistenceUnitName(persistenceContext.unitName());
                }
                if (persistenceContextRef.getPersistenceContextType() == null || ("").equals(persistenceContextRef.getPersistenceContextType())) {
                    if(persistenceContext.type() == javax.persistence.PersistenceContextType.EXTENDED) {
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
            
            Property property = null;
            for(PersistenceProperty persistenceProperty : persistenceContext.properties()) {
                boolean flag = true;
                for(Property prpty : persistenceProperties) {
                    if(prpty.getName().equals(persistenceProperty.name())) {
                        flag = false;
                        break;
                    } 
                }
                if(flag) {
                    property = new Property();
                    property.setName(persistenceProperty.name());
                    property.setValue(persistenceProperty.value());
                    persistenceProperties.add(property);
                }
            }
                        
            if (member != null) {
                // Set the member name where this will be injected
                InjectionTarget target = new InjectionTarget();
                target.setInjectionTargetClass(member.getDeclaringClass().getName());
                target.setInjectionTargetName(member.getName());
                persistenceContextRef.getInjectionTarget().add(target);
            }                        
        }
        
        private void buildEjbRef(JndiConsumer consumer, EJB ejb, Member member) {
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

        private boolean contains(Object[] types,Object type){
            int size = types.length;
            for (int i = 0; i < size; i++) {
                if (type.equals(types[i])) {
                    return true;
                }
            }
            return false;
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
            return names.toArray(new String[]{});
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
