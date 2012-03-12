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
package org.apache.openejb.assembler.classic;

import org.apache.openejb.BeanContext;
import org.apache.openejb.BeanType;
import org.apache.openejb.Injection;
import org.apache.openejb.ModuleContext;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.core.cmp.CmpUtil;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.util.Duration;
import org.apache.openejb.util.Index;
import org.apache.openejb.util.Messages;
import org.apache.openejb.util.SafeToolkit;

import javax.ejb.TimedObject;
import javax.ejb.Timer;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.persistence.EntityManagerFactory;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

class EnterpriseBeanBuilder {
    protected static final Messages messages = new Messages("org.apache.openejb.util.resources");
    private final EnterpriseBeanInfo bean;
    private final List<String> defaultInterceptors;
    private final BeanType ejbType;
    private final List<Exception> warnings = new ArrayList<Exception>();
    private final ModuleContext moduleContext;
    private final List<Injection> moduleInjections;

    public EnterpriseBeanBuilder(EnterpriseBeanInfo bean, List<String> defaultInterceptors, ModuleContext moduleContext, List<Injection> moduleInjections) {
        this.moduleContext = moduleContext;
        this.bean = bean;
        this.defaultInterceptors = defaultInterceptors;
        this.moduleInjections = moduleInjections;

        if (bean.type == EnterpriseBeanInfo.STATEFUL) {
            ejbType = BeanType.STATEFUL;
        } else if (bean.type == EnterpriseBeanInfo.STATELESS) {
            ejbType = BeanType.STATELESS;
        } else if (bean.type == EnterpriseBeanInfo.SINGLETON) {
            ejbType = BeanType.SINGLETON;
        } else if (bean.type == EnterpriseBeanInfo.MANAGED) {
            ejbType = BeanType.MANAGED;
        } else if (bean.type == EnterpriseBeanInfo.MESSAGE) {
            ejbType = BeanType.MESSAGE_DRIVEN;
        } else if (bean.type == EnterpriseBeanInfo.ENTITY) {
            String persistenceType = ((EntityBeanInfo) bean).persistenceType;
            ejbType = (persistenceType.equalsIgnoreCase("Container")) ? BeanType.CMP_ENTITY : BeanType.BMP_ENTITY;
        } else {
            throw new UnsupportedOperationException("No building support for bean type: " + bean);
        }
    }

    public BeanContext build() throws OpenEJBException {
        Class ejbClass = loadClass(bean.ejbClass, "classNotFound.ejbClass");

        Class home = null;
        Class remote = null;
        if (bean.home != null) {
            home = loadClass(bean.home, "classNotFound.home");
            remote = loadClass(bean.remote, "classNotFound.remote");
        }

        Class<?> proxy = null;
        if (bean.proxy != null) {
            proxy = loadClass(bean.proxy, "classNotFound.proxy");
        }

        Class<?> localhome = null;
        Class<?> local = null;
        if (bean.localHome != null) {
            localhome = loadClass(bean.localHome, "classNotFound.localHome");
            local = loadClass(bean.local, "classNotFound.local");
        }

        List<Class> businessLocals = new ArrayList<Class>();
        for (String businessLocal : bean.businessLocal) {
            businessLocals.add(loadClass(businessLocal, "classNotFound.businessLocal"));
        }

        List<Class> businessRemotes = new ArrayList<Class>();
        for (String businessRemote : bean.businessRemote) {
            businessRemotes.add(loadClass(businessRemote, "classNotFound.businessRemote"));
        }

        Class serviceEndpoint = null;
        if (BeanType.STATELESS == ejbType || BeanType.SINGLETON == ejbType ){
            if(bean.serviceEndpoint != null){
                serviceEndpoint = loadClass(bean.serviceEndpoint, "classNotFound.sei");
            }
        }

        Class primaryKey = null;
        if (ejbType.isEntity() && ((EntityBeanInfo) bean).primKeyClass != null) {
            String className = ((EntityBeanInfo) bean).primKeyClass;
            primaryKey = loadClass(className, "classNotFound.primaryKey");
        }

        final String transactionType = bean.transactionType;

        // determine the injections
        InjectionBuilder injectionBuilder = new InjectionBuilder(moduleContext.getClassLoader());
        List<Injection> injections = injectionBuilder.buildInjections(bean.jndiEnc);
        Set<Class<?>> relevantClasses = new HashSet<Class<?>>();
        Class c = ejbClass;
        do {
            relevantClasses.add(c);
            c = c.getSuperclass();
        } while (c != null && c != Object.class);

        for (Injection injection: moduleInjections) {
            if (relevantClasses.contains(injection.getTarget())) {
                injections.add(injection);
            }
        }

        // build the enc
        JndiEncBuilder jndiEncBuilder = new JndiEncBuilder(bean.jndiEnc, injections, transactionType, moduleContext.getId(), null, moduleContext.getUniqueId(), moduleContext.getClassLoader());
        Context compJndiContext = jndiEncBuilder.build(JndiEncBuilder.JndiScope.comp);
        bind(compJndiContext, "module", moduleContext.getModuleJndiContext());
        bind(compJndiContext, "app", moduleContext.getAppContext().getAppJndiContext());
        bind(compJndiContext, "global", moduleContext.getAppContext().getGlobalJndiContext());

        BeanContext deployment;
        if (BeanType.MESSAGE_DRIVEN != ejbType) {
            deployment = new BeanContext(bean.ejbDeploymentId, compJndiContext, moduleContext, ejbClass, home, remote, localhome, local, proxy, serviceEndpoint, businessLocals, businessRemotes, primaryKey, ejbType, bean.localbean && ejbType.isSession());
            if (bean instanceof ManagedBeanInfo) {
                deployment.setHidden(((ManagedBeanInfo) bean).hidden);
            }
        } else {
            MessageDrivenBeanInfo messageDrivenBeanInfo = (MessageDrivenBeanInfo) bean;
            Class mdbInterface = loadClass(messageDrivenBeanInfo.mdbInterface, "classNotFound.mdbInterface");
            deployment = new BeanContext(bean.ejbDeploymentId, compJndiContext, moduleContext, ejbClass, mdbInterface, messageDrivenBeanInfo.activationProperties);
            deployment.setDestinationId(messageDrivenBeanInfo.destinationId);
        }

        deployment.getProperties().putAll(bean.properties);

        deployment.setEjbName(bean.ejbName);

        deployment.setRunAs(bean.runAs);

        deployment.getInjections().addAll(injections);

        // ejbTimeout
        deployment.setEjbTimeout(getTimeout(ejbClass, bean.timeoutMethod));

        if (bean.statefulTimeout != null) {
            deployment.setStatefulTimeout(new Duration(bean.statefulTimeout.time, TimeUnit.valueOf(bean.statefulTimeout.unit)));
        }

        if (bean instanceof StatefulBeanInfo) {
            StatefulBeanInfo statefulBeanInfo = (StatefulBeanInfo) bean;

            for (InitMethodInfo init : statefulBeanInfo.initMethods) {
                Method beanMethod = MethodInfoUtil.toMethod(ejbClass, init.beanMethod);
                List<Method> methods = new ArrayList<Method>();

                if (home != null) methods.addAll(Arrays.asList(home.getMethods()));
                if (localhome != null) methods.addAll(Arrays.asList(localhome.getMethods()));

                for (Method homeMethod : methods) {
                    if (init.createMethod != null && !init.createMethod.methodName.equals(homeMethod.getName())) continue;

                    if (!homeMethod.getName().startsWith("create")) continue;

                    if (paramsMatch(beanMethod, homeMethod)){
                        deployment.mapMethods(homeMethod, beanMethod);
                    }
                }
            }

            for (RemoveMethodInfo removeMethod : statefulBeanInfo.removeMethods) {
                
                if (removeMethod.beanMethod.methodParams == null) {

                    MethodInfo methodInfo = new MethodInfo();
                    methodInfo.methodName = removeMethod.beanMethod.methodName;
                    methodInfo.methodParams = removeMethod.beanMethod.methodParams;
                    methodInfo.className = removeMethod.beanMethod.className;
                    List<Method> methods = MethodInfoUtil.matchingMethods(methodInfo, ejbClass);

                    for (Method method : methods) {
                        deployment.getRemoveMethods().add(method);
                        deployment.setRetainIfExeption(method, removeMethod.retainIfException);
                    }

                } else {
                    Method method = MethodInfoUtil.toMethod(ejbClass, removeMethod.beanMethod);
                    deployment.getRemoveMethods().add(method);
                    deployment.setRetainIfExeption(method, removeMethod.retainIfException);
                }
                
            }

            String moduleId = moduleContext.getId();
            Map<EntityManagerFactory, Map> extendedEntityManagerFactories = new HashMap<EntityManagerFactory, Map>();
            for (PersistenceContextReferenceInfo info : statefulBeanInfo.jndiEnc.persistenceContextRefs) {
                if (info.extended) {
//                    EntityManagerFactory entityManagerFactory = emfLinkResolver.resolveLink(info.persistenceUnitName, moduleId);
//                    extendedEntityManagerFactories.put(entityManagerFactory, info.properties);

                    try {
                        ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
                        Object o = containerSystem.getJNDIContext().lookup(PersistenceBuilder.getOpenEJBJndiName(info.unitId));
                        extendedEntityManagerFactories.put((EntityManagerFactory) o, info.properties);
                    } catch (NamingException e) {
                        throw new OpenEJBException("PersistenceUnit '" + info.unitId + "' not found for EXTENDED ref '" + info.referenceName + "'");
                    }

                }
            }
            deployment.setExtendedEntityManagerFactories(new Index<EntityManagerFactory, Map>(extendedEntityManagerFactories));
        }

        if (ejbType.isSession() || ejbType.isMessageDriven()) {
            deployment.setBeanManagedTransaction("Bean".equalsIgnoreCase(bean.transactionType));
        }

        if (ejbType.isSession()) {
            // Allow dependsOn to work for all session beans
            deployment.getDependsOn().addAll(bean.dependsOn);

        }

        if (ejbType == BeanType.SINGLETON) {
            deployment.setBeanManagedConcurrency("Bean".equalsIgnoreCase(bean.concurrencyType));
            deployment.setLoadOnStartup(bean.loadOnStartup);
        }

        if (ejbType.isEntity()) {
            EntityBeanInfo entity = (EntityBeanInfo) bean;

            deployment.setCmp2(entity.cmpVersion == 2);
            deployment.setIsReentrant(entity.reentrant.equalsIgnoreCase("true"));

            if (ejbType == BeanType.CMP_ENTITY) {
                Class cmpImplClass = null;
                String cmpImplClassName = CmpUtil.getCmpImplClassName(entity.abstractSchemaName, entity.ejbClass);
                cmpImplClass = loadClass(cmpImplClassName, "classNotFound.cmpImplClass");
                deployment.setCmpImplClass(cmpImplClass);
                deployment.setAbstractSchemaName(entity.abstractSchemaName);

                for (QueryInfo query : entity.queries) {

                    if (query.remoteResultType) {
                        StringBuilder methodSignature = new StringBuilder();
                        methodSignature.append(query.method.methodName);
                        if (query.method.methodParams != null && !query.method.methodParams.isEmpty()) {
                            methodSignature.append('(');
                            boolean first = true;
                            for (String methodParam : query.method.methodParams) {
                                if (!first) methodSignature.append(",");
                                methodSignature.append(methodParam);
                                first = false;
                            }
                            methodSignature.append(')');
                        }
                        deployment.setRemoteQueryResults(methodSignature.toString());
                    }

                }

                if (entity.primKeyField != null) {
                    deployment.setPrimaryKeyField(entity.primKeyField);
                }
            }
        }

        deployment.createMethodMap();

        //Configure asynchronous tag after the method map is created, so while we check whether the method is asynchronous,
        //we could directly check the matching bean method.
        if (ejbType == BeanType.STATELESS || ejbType == BeanType.SINGLETON || ejbType == BeanType.STATEFUL) {
            for (NamedMethodInfo methodInfo : bean.asynchronous) {
                Method method = MethodInfoUtil.toMethod(ejbClass, methodInfo);
                deployment.getMethodContext(deployment.getMatchingBeanMethod(method)).setAsynchronous(true);
            }
            for (String className : bean.asynchronousClasses) {
                deployment.getAsynchronousClasses().add(loadClass(className, "classNotFound.ejbClass"));
            }
            deployment.createAsynchronousMethodSet();
        }

        return deployment;
    }

    private void bind(Context compJndiContext, String s, Context moduleJndiContext) throws OpenEJBException {
        Context c;
        try {
            c = (Context) moduleJndiContext.lookup(s);
        } catch (NamingException e) {
            //ok, nothing there....
            return;
        }
        try {
            compJndiContext.bind(s, c);
        } catch (NamingException e) {
            throw new OpenEJBException("Could not bind context at " + s, e);
        }
    }

    public static boolean paramsMatch(Method methodA, Method methodB) {
        if (methodA.getParameterTypes().length != methodB.getParameterTypes().length){
            return false;
        }

        for (int i = 0; i < methodA.getParameterTypes().length; i++) {
            Class<?> a = methodA.getParameterTypes()[i];
            Class<?> b = methodB.getParameterTypes()[i];
            if (!a.equals(b)) return false;
        }
        return true;
    }

    public List<Exception> getWarnings() {
        return warnings;
    }

    private Method getCallback(Class ejbClass, List<CallbackInfo> callbackInfos) {
        Method callback = null;
        for (CallbackInfo info : callbackInfos) {
            try {
                if (ejbClass.getName().equals(info.className)) {
                    if (callback != null) {
                        throw new IllegalStateException("Spec requirements only allow one callback method of a given type per class.  The following callback will be ignored: " + info.className + "." + info.method);
                    }
                    try {
                        callback = ejbClass.getMethod(info.method);
                    } catch (NoSuchMethodException e) {
                        throw (IllegalStateException) new IllegalStateException("Callback method does not exist: " + info.className + "." + info.method).initCause(e);
                    }

                } else {
                    throw new UnsupportedOperationException("Callback: " + info.className + "." + info.method + " -- We currently do not support callbacks where the callback class is not the bean class.  If you need this feature, please let us know and we will complete it asap.");
                }
            } catch (Exception e) {
                warnings.add(e);
            }
        }
        return callback;
    }

    private Method getTimeout(Class ejbClass, NamedMethodInfo info) {
        Method timeout = null;
        try {
            if (TimedObject.class.isAssignableFrom(ejbClass)) {
                timeout = ejbClass.getMethod("ejbTimeout", Timer.class);
            } else if (info != null){
                try {
                    timeout = MethodInfoUtil.toMethod(ejbClass, info);
                } catch (IllegalStateException e) {
                    //Spec 18.2.5.3 [102] For the compatibility of timeout method signature, if method-params is  not set, it is also required to search the method signaure below :
                    //void <METHOD> (Timer timer)

                    // TODO Lets move this fallback searching into the config side so people do
                    // not get 'Callback method does not exist' runtime exceptions and instead
                    // get a validation failure.  Then we can explicitly add the (Timer) param
                    // if the fallback method does exist.
                    if (info.methodParams == null) {
                        NamedMethodInfo candidateInfo = new NamedMethodInfo();
                        candidateInfo.className = info.className;
                        candidateInfo.id = info.id;
                        candidateInfo.methodName = info.methodName;
                        candidateInfo.methodParams = Arrays.asList(Timer.class.getName());
                        timeout = MethodInfoUtil.toMethod(ejbClass, candidateInfo);
                    }
                }
            }
        } catch (Exception e) {
            warnings.add(e);
        }

        return timeout;
    }


    private Class loadClass(String className, String messageCode) throws OpenEJBException {
        Class clazz = load(className, messageCode);
        try {
//            clazz.getDeclaredMethods();
//            clazz.getDeclaredFields();
//            clazz.getDeclaredConstructors();
//            clazz.getInterfaces();
            return clazz;
        } catch (NoClassDefFoundError e) {
            if (clazz.getClassLoader() != moduleContext.getClassLoader()) {
                String message = SafeToolkit.messages.format("cl0008", className, clazz.getClassLoader(), moduleContext.getClassLoader(), e.getMessage());
                throw new OpenEJBException(AssemblerTool.messages.format(messageCode, className, bean.ejbDeploymentId, message), e);
            } else {
                String message = SafeToolkit.messages.format("cl0009", className, clazz.getClassLoader(), e.getMessage());
                throw new OpenEJBException(AssemblerTool.messages.format(messageCode, className, bean.ejbDeploymentId, message), e);
            }
        }
    }

    private Class load(String className, String messageCode) throws OpenEJBException {
        try {
            return Class.forName(className, true, moduleContext.getClassLoader());
        } catch (ClassNotFoundException e) {
            String message = SafeToolkit.messages.format("cl0007", className, bean.codebase);
            throw new OpenEJBException(AssemblerTool.messages.format(messageCode, className, bean.ejbDeploymentId, message));
        }
    }
}
