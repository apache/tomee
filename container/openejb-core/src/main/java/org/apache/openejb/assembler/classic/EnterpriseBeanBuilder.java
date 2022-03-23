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
import org.apache.openejb.dyni.DynamicSubclass;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.persistence.JtaEntityManager;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.util.Duration;
import org.apache.openejb.util.Index;
import org.apache.openejb.util.Messages;

import jakarta.ejb.TimedObject;
import jakarta.ejb.Timer;
import javax.naming.Context;
import javax.naming.NamingException;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.SynchronizationType;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

class EnterpriseBeanBuilder {
    private final EnterpriseBeanInfo bean;
    private final BeanType ejbType;
    private final List<Exception> warnings = new ArrayList<>();
    private final ModuleContext moduleContext;
    private final List<Injection> moduleInjections;

    public EnterpriseBeanBuilder(final EnterpriseBeanInfo bean, final ModuleContext moduleContext, final List<Injection> moduleInjections) {
        this.moduleContext = moduleContext;
        this.bean = bean;
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
            final String persistenceType = ((EntityBeanInfo) bean).persistenceType;
            ejbType = persistenceType.equalsIgnoreCase("Container") ? BeanType.CMP_ENTITY : BeanType.BMP_ENTITY;
        } else {
            throw new UnsupportedOperationException("No building support for bean type: " + bean);
        }
    }

    public BeanContext build() throws OpenEJBException {
        Class ejbClass = loadClass(bean.ejbClass, "classNotFound.ejbClass");

        if (DynamicSubclass.isDynamic(ejbClass)) {
            ejbClass = DynamicSubclass.createSubclass(ejbClass, moduleContext.getClassLoader());
        }

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

        final List<Class> businessLocals = new ArrayList<>();
        for (final String businessLocal : bean.businessLocal) {
            businessLocals.add(loadClass(businessLocal, "classNotFound.businessLocal"));
        }

        final List<Class> businessRemotes = new ArrayList<>();
        for (final String businessRemote : bean.businessRemote) {
            businessRemotes.add(loadClass(businessRemote, "classNotFound.businessRemote"));
        }

        Class serviceEndpoint = null;
        if (BeanType.STATELESS == ejbType || BeanType.SINGLETON == ejbType) {
            if (bean.serviceEndpoint != null) {
                serviceEndpoint = loadClass(bean.serviceEndpoint, "classNotFound.sei");
            }
        }

        Class primaryKey = null;
        if (ejbType.isEntity() && ((EntityBeanInfo) bean).primKeyClass != null) {
            final String className = ((EntityBeanInfo) bean).primKeyClass;
            primaryKey = loadClass(className, "classNotFound.primaryKey");
        }

        final String transactionType = bean.transactionType;

        // determine the injections
        final InjectionBuilder injectionBuilder = new InjectionBuilder(moduleContext.getClassLoader());
        final List<Injection> injections = injectionBuilder.buildInjections(bean.jndiEnc);
        final Set<Class<?>> relevantClasses = new HashSet<>();
        Class c = ejbClass;
        do {
            relevantClasses.add(c);
            c = c.getSuperclass();
        } while (c != null && c != Object.class);

        for (final Injection injection : moduleInjections) {
            if (relevantClasses.contains(injection.getTarget())) {
                injections.add(injection);
            }
        }

        // build the enc
        final JndiEncBuilder jndiEncBuilder = new JndiEncBuilder(bean.jndiEnc, injections, transactionType, moduleContext.getId(), null,
                moduleContext.getUniqueId(), moduleContext.getClassLoader(), moduleContext.getAppContext() == null ? moduleContext.getProperties() : moduleContext.getAppContext().getProperties());
        final Context compJndiContext = jndiEncBuilder.build(JndiEncBuilder.JndiScope.comp);
        bind(compJndiContext, "module", moduleContext.getModuleJndiContext());
        bind(compJndiContext, "app", moduleContext.getAppContext().getAppJndiContext());
        bind(compJndiContext, "global", moduleContext.getAppContext().getGlobalJndiContext());

        final BeanContext deployment;
        if (BeanType.MESSAGE_DRIVEN != ejbType) {
            deployment = new BeanContext(bean.ejbDeploymentId, compJndiContext, moduleContext, ejbClass, home, remote, localhome, local, proxy, serviceEndpoint, businessLocals, businessRemotes, primaryKey, ejbType, bean.localbean && ejbType.isSession(), bean.passivable);
            if (bean instanceof ManagedBeanInfo) {
                deployment.setHidden(((ManagedBeanInfo) bean).hidden);
            }
        } else {
            final MessageDrivenBeanInfo messageDrivenBeanInfo = (MessageDrivenBeanInfo) bean;
            final Class mdbInterface = loadClass(messageDrivenBeanInfo.mdbInterface, "classNotFound.mdbInterface");
            deployment = new BeanContext(bean.ejbDeploymentId, compJndiContext, moduleContext, ejbClass, mdbInterface, messageDrivenBeanInfo.activationProperties);
            deployment.setDestinationId(messageDrivenBeanInfo.destinationId);
        }

        deployment.getProperties().putAll(bean.properties);

        deployment.setEjbName(bean.ejbName);

        deployment.setRunAs(bean.runAs);
        deployment.setRunAsUser(bean.runAsUser);

        deployment.getInjections().addAll(injections);

        // ejbTimeout
        deployment.setEjbTimeout(getTimeout(ejbClass, bean.timeoutMethod));

        if (bean.statefulTimeout != null) {
            deployment.setStatefulTimeout(new Duration(bean.statefulTimeout.time, TimeUnit.valueOf(bean.statefulTimeout.unit)));
        }

        if (bean instanceof StatefulBeanInfo) {
            final StatefulBeanInfo statefulBeanInfo = (StatefulBeanInfo) bean;

            for (final InitMethodInfo init : statefulBeanInfo.initMethods) {
                final Method beanMethod = MethodInfoUtil.toMethod(ejbClass, init.beanMethod);
                final List<Method> methods = new ArrayList<>();

                if (home != null) {
                    methods.addAll(Arrays.asList(home.getMethods()));
                }
                if (localhome != null) {
                    methods.addAll(Arrays.asList(localhome.getMethods()));
                }

                for (final Method homeMethod : methods) {
                    if (init.createMethod != null && !init.createMethod.methodName.equals(homeMethod.getName())) {
                        continue;
                    }

                    if (!homeMethod.getName().startsWith("create")) {
                        continue;
                    }

                    if (paramsMatch(beanMethod, homeMethod)) {
                        deployment.mapMethods(homeMethod, beanMethod);
                    }
                }
            }

            for (final RemoveMethodInfo removeMethod : statefulBeanInfo.removeMethods) {

                if (removeMethod.beanMethod.methodParams == null) {

                    final MethodInfo methodInfo = new MethodInfo();
                    methodInfo.methodName = removeMethod.beanMethod.methodName;
                    methodInfo.methodParams = removeMethod.beanMethod.methodParams;
                    methodInfo.className = removeMethod.beanMethod.className;
                    final List<Method> methods = MethodInfoUtil.matchingMethods(methodInfo, ejbClass);

                    for (final Method method : methods) {
                        deployment.getRemoveMethods().add(method);
                        deployment.setRetainIfExeption(method, removeMethod.retainIfException);
                    }

                } else {
                    final Method method = MethodInfoUtil.toMethod(ejbClass, removeMethod.beanMethod);
                    deployment.getRemoveMethods().add(method);
                    deployment.setRetainIfExeption(method, removeMethod.retainIfException);
                }

            }

            final Map<EntityManagerFactory, BeanContext.EntityManagerConfiguration> extendedEntityManagerFactories = new HashMap<>();
            for (final PersistenceContextReferenceInfo info : statefulBeanInfo.jndiEnc.persistenceContextRefs) {
                if (info.extended) {
                    try {
                        final ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
                        final Object o = containerSystem.getJNDIContext().lookup(PersistenceBuilder.getOpenEJBJndiName(info.unitId));
                        final EntityManagerFactory emf = EntityManagerFactory.class.cast(o);
                        extendedEntityManagerFactories.put(
                            emf,
                            new BeanContext.EntityManagerConfiguration(
                                info.properties,
                                JtaEntityManager.isJPA21(emf) && info.synchronizationType != null ? SynchronizationType.valueOf(info.synchronizationType) : null));
                    } catch (final NamingException e) {
                        throw new OpenEJBException("PersistenceUnit '" + info.unitId + "' not found for EXTENDED ref '" + info.referenceName + "'");
                    }

                }
            }
            deployment.setExtendedEntityManagerFactories(new Index<>(extendedEntityManagerFactories));
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
            final EntityBeanInfo entity = (EntityBeanInfo) bean;

            deployment.setCmp2(entity.cmpVersion == 2);
            deployment.setIsReentrant(entity.reentrant.equalsIgnoreCase("true"));

            if (ejbType == BeanType.CMP_ENTITY) {
                Class cmpImplClass = null;
                final String cmpImplClassName = CmpUtil.getCmpImplClassName(entity.abstractSchemaName, entity.ejbClass);
                cmpImplClass = loadClass(cmpImplClassName, "classNotFound.cmpImplClass");
                deployment.setCmpImplClass(cmpImplClass);
                deployment.setAbstractSchemaName(entity.abstractSchemaName);

                for (final QueryInfo query : entity.queries) {

                    if (query.remoteResultType) {
                        final StringBuilder methodSignature = new StringBuilder();
                        methodSignature.append(query.method.methodName);
                        if (query.method.methodParams != null && !query.method.methodParams.isEmpty()) {
                            methodSignature.append('(');
                            boolean first = true;
                            for (final String methodParam : query.method.methodParams) {
                                if (!first) {
                                    methodSignature.append(",");
                                }
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
            for (final NamedMethodInfo methodInfo : bean.asynchronous) {
                final Method method = MethodInfoUtil.toMethod(ejbClass, methodInfo);
                deployment.getMethodContext(deployment.getMatchingBeanMethod(method)).setAsynchronous(true);
            }
            for (final String className : bean.asynchronousClasses) {
                deployment.getAsynchronousClasses().add(loadClass(className, "classNotFound.ejbClass"));
            }
            deployment.createAsynchronousMethodSet();
        }

        for (final SecurityRoleReferenceInfo securityRoleReference : bean.securityRoleReferences) {
            deployment.addSecurityRoleReference(securityRoleReference.roleName, securityRoleReference.roleLink);
        }

        return deployment;
    }

    private void bind(final Context compJndiContext, final String s, final Context moduleJndiContext) throws OpenEJBException {
        final Context c;
        try {
            c = (Context) moduleJndiContext.lookup(s);
        } catch (final NamingException e) {
            //ok, nothing there....
            return;
        }
        try {
            compJndiContext.bind(s, c);
        } catch (final NamingException e) {
            throw new OpenEJBException("Could not bind context at " + s, e);
        }
    }

    public static boolean paramsMatch(final Method methodA, final Method methodB) {
        if (methodA.getParameterTypes().length != methodB.getParameterTypes().length) {
            return false;
        }

        for (int i = 0; i < methodA.getParameterTypes().length; i++) {
            final Class<?> a = methodA.getParameterTypes()[i];
            final Class<?> b = methodB.getParameterTypes()[i];
            if (!a.equals(b)) {
                return false;
            }
        }
        return true;
    }

    public List<Exception> getWarnings() {
        return warnings;
    }

    private Method getTimeout(final Class ejbClass, final NamedMethodInfo info) {
        Method timeout = null;
        try {
            if (TimedObject.class.isAssignableFrom(ejbClass)) {
                timeout = ejbClass.getMethod("ejbTimeout", Timer.class);
            } else if (info != null) {
                try {
                    timeout = MethodInfoUtil.toMethod(ejbClass, info);
                } catch (final IllegalStateException e) {
                    //Spec 18.2.5.3 [102] For the compatibility of timeout method signature, if method-params is  not set, it is also required to search the method signaure below :
                    //void <METHOD> (Timer timer)

                    // TODO Lets move this fallback searching into the config side so people do
                    // not get 'Callback method does not exist' runtime exceptions and instead
                    // get a validation failure.  Then we can explicitly add the (Timer) param
                    // if the fallback method does exist.
                    if (info.methodParams == null) {
                        final NamedMethodInfo candidateInfo = new NamedMethodInfo();
                        candidateInfo.className = info.className;
                        candidateInfo.id = info.id;
                        candidateInfo.methodName = info.methodName;
                        candidateInfo.methodParams = Collections.singletonList(Timer.class.getName());
                        timeout = MethodInfoUtil.toMethod(ejbClass, candidateInfo);
                    }
                }
            }
        } catch (final Exception e) {
            warnings.add(e);
        }

        return timeout;
    }


    private Class loadClass(final String className, final String messageCode) throws OpenEJBException {
        final Class clazz = load(className, messageCode);
        try {
//            clazz.getDeclaredMethods();
//            clazz.getDeclaredFields();
//            clazz.getDeclaredConstructors();
//            clazz.getInterfaces();
            return clazz;
        } catch (final NoClassDefFoundError e) {
            if (clazz.getClassLoader() != moduleContext.getClassLoader()) {
                final String message = messages().format("cl0008", className, clazz.getClassLoader(), moduleContext.getClassLoader(), e.getMessage());
                throw new OpenEJBException(messages().format(messageCode, className, bean.ejbDeploymentId, message), e);
            } else {
                final String message = messages().format("cl0009", className, clazz.getClassLoader(), e.getMessage());
                throw new OpenEJBException(messages().format(messageCode, className, bean.ejbDeploymentId, message), e);
            }
        }
    }

    private Messages messages() { // new is fine cause for errors only
        return new Messages("org.apache.openejb.util.resources");
    }

    private Class load(final String className, final String messageCode) throws OpenEJBException {
        try {
            return Class.forName(className, true, moduleContext.getClassLoader());
        } catch (final ClassNotFoundException e) {
            final String message = messages().format("cl0007", className, bean.codebase);
            throw new OpenEJBException(messages().format(messageCode, className, bean.ejbDeploymentId, message));
        }
    }
}
