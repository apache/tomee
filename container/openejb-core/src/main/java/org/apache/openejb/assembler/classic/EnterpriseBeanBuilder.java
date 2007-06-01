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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.assembler.classic;

import org.apache.openejb.BeanType;
import org.apache.openejb.Injection;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.core.DeploymentContext;
import org.apache.openejb.core.timer.EjbTimerServiceImpl;
import org.apache.openejb.core.timer.NullEjbTimerServiceImpl;
import org.apache.openejb.core.cmp.CmpUtil;
import org.apache.openejb.util.Index;
import org.apache.openejb.util.Messages;
import org.apache.openejb.util.SafeToolkit;
import org.apache.openejb.util.Classes;

import javax.naming.Context;
import javax.persistence.EntityManagerFactory;
import javax.ejb.TimedObject;
import javax.ejb.Timer;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

class EnterpriseBeanBuilder {
    protected static final Messages messages = new Messages("org.apache.openejb.util.resources");
    private final EnterpriseBeanInfo bean;
    private final String moduleId;
    private final List<String> defaultInterceptors;
    private final BeanType ejbType;
    private final ClassLoader cl;
    private final LinkResolver<EntityManagerFactory> emfLinkResolver;
    private List<Exception> warnings = new ArrayList<Exception>();

    public EnterpriseBeanBuilder(ClassLoader cl, EnterpriseBeanInfo bean, String moduleId, List<String> defaultInterceptors, LinkResolver<EntityManagerFactory> emfLinkResolver) {
        this.bean = bean;
        this.moduleId = moduleId;
        this.defaultInterceptors = defaultInterceptors;

        if (bean.type == EnterpriseBeanInfo.STATEFUL) {
            ejbType = BeanType.STATEFUL;
        } else if (bean.type == EnterpriseBeanInfo.STATELESS) {
            ejbType = BeanType.STATELESS;
        } else if (bean.type == EnterpriseBeanInfo.MESSAGE) {
            ejbType = BeanType.MESSAGE_DRIVEN;
        } else if (bean.type == EnterpriseBeanInfo.ENTITY) {
            String persistenceType = ((EntityBeanInfo) bean).persistenceType;
            ejbType = (persistenceType.equalsIgnoreCase("Container")) ? BeanType.CMP_ENTITY : BeanType.BMP_ENTITY;
        } else {
            throw new UnsupportedOperationException("No building support for bean type: " + bean);
        }
        this.cl = cl;
        this.emfLinkResolver = emfLinkResolver;
    }

    public Object build() throws OpenEJBException {
        Class ejbClass = loadClass(bean.ejbClass, "classNotFound.ejbClass");

        Class home = null;
        Class remote = null;
        if (bean.home != null) {
            home = loadClass(bean.home, "classNotFound.home");
            remote = loadClass(bean.remote, "classNotFound.remote");
        }

        Class localhome = null;
        Class local = null;
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
        if (BeanType.STATELESS == ejbType){
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

        JndiEncBuilder jndiEncBuilder = new JndiEncBuilder(bean.jndiEnc, transactionType, emfLinkResolver, moduleId);
        Context root = jndiEncBuilder.build();

        DeploymentContext deploymentContext = new DeploymentContext(bean.ejbDeploymentId, cl, root);
        CoreDeploymentInfo deployment;
        if (BeanType.MESSAGE_DRIVEN != ejbType) {
            deployment = new CoreDeploymentInfo(deploymentContext, ejbClass, home, remote, localhome, local, serviceEndpoint, businessLocals, businessRemotes, primaryKey, ejbType);
        } else {
            MessageDrivenBeanInfo messageDrivenBeanInfo = (MessageDrivenBeanInfo) bean;
            Class mdbInterface = loadClass(messageDrivenBeanInfo.mdbInterface, "classNotFound.mdbInterface");
            deployment = new CoreDeploymentInfo(deploymentContext, ejbClass, mdbInterface, messageDrivenBeanInfo.activationProperties);
            deployment.setDestinationId(messageDrivenBeanInfo.destinationId);
        }

        deployment.setEjbName(bean.ejbName);

        deployment.setModuleId(moduleId);

        deployment.setRunAs(bean.runAs);

        for (SecurityRoleReferenceInfo roleReferenceInfo : bean.securityRoleReferences) {
            String alias = roleReferenceInfo.roleName;
            String actualName = roleReferenceInfo.roleLink;

            // EJB 3.0 - 17.2.5.3
            // In the absence of this linking step, any security role name as used in the code will be assumed to
            // correspond to a security role of the same name.
            if (actualName == null){
                actualName = alias;
            }

            deployment.addSecurityRoleReference(alias, actualName);
        }

        for (EnvEntryInfo info : bean.jndiEnc.envEntries) {
            for (InjectionInfo target : info.targets) {
                Class targetClass = loadClass(target.className, "classNotFound.injectionTarget");
                Injection injection = new Injection(info.name, target.propertyName, targetClass);
                deployment.getInjections().add(injection);
            }
        }

        for (EjbReferenceInfo info : bean.jndiEnc.ejbReferences) {
            for (InjectionInfo target : info.targets) {
                Class targetClass = loadClass(target.className, "classNotFound.injectionTarget");
                Injection injection = new Injection(info.referenceName, target.propertyName, targetClass);
                deployment.getInjections().add(injection);
            }
        }

        for (EjbLocalReferenceInfo info : bean.jndiEnc.ejbLocalReferences) {
            for (InjectionInfo target : info.targets) {
                Class targetClass = loadClass(target.className, "classNotFound.injectionTarget");
                Injection injection = new Injection(info.referenceName, target.propertyName, targetClass);
                deployment.getInjections().add(injection);
            }
        }

        for (PersistenceUnitReferenceInfo info : bean.jndiEnc.persistenceUnitRefs) {
            for (InjectionInfo target : info.targets) {
                Class targetClass = loadClass(target.className, "classNotFound.injectionTarget");
                Injection injection = new Injection(info.referenceName, target.propertyName, targetClass);
                deployment.getInjections().add(injection);
            }
        }

        for (PersistenceContextReferenceInfo info : bean.jndiEnc.persistenceContextRefs) {
            for (InjectionInfo target : info.targets) {
                Class targetClass = loadClass(target.className, "classNotFound.injectionTarget");
                Injection injection = new Injection(info.referenceName, target.propertyName, targetClass);
                deployment.getInjections().add(injection);
            }
        }

        for (ResourceReferenceInfo info : bean.jndiEnc.resourceRefs) {
            for (InjectionInfo target : info.targets) {
                Class targetClass = loadClass(target.className, "classNotFound.injectionTarget");
                Injection injection = new Injection(info.referenceName, target.propertyName, targetClass);
                deployment.getInjections().add(injection);
            }
        }

        for (ResourceEnvReferenceInfo info : bean.jndiEnc.resourceEnvRefs) {
            for (InjectionInfo target : info.targets) {
                Class targetClass = loadClass(target.className, "classNotFound.injectionTarget");
                Injection injection = new Injection(info.resourceEnvRefName, target.propertyName, targetClass);
                deployment.getInjections().add(injection);
            }
        }

        for (ServiceReferenceInfo info : bean.jndiEnc.serviceRefs) {
            for (InjectionInfo target : info.targets) {
                Class targetClass = loadClass(target.className, "classNotFound.injectionTarget");
                Injection injection = new Injection(info.referenceName, target.propertyName, targetClass);
                deployment.getInjections().add(injection);
            }
        }

        // ejbTimeout
        deployment.setEjbTimeout(getTimeout(ejbClass, bean.timeoutMethod));

        if (bean instanceof StatefulBeanInfo) {
            StatefulBeanInfo statefulBeanInfo = (StatefulBeanInfo) bean;

            for (InitMethodInfo init : statefulBeanInfo.initMethods) {
                Method beanMethod = toMethod(ejbClass, init.beanMethod);
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
                Method method = toMethod(ejbClass, removeMethod.beanMethod);
                deployment.getRemoveMethods().add(method);
                deployment.setRetainIfExeption(method, removeMethod.retainIfException);
            }

            Map<EntityManagerFactory, Map> extendedEntityManagerFactories = new HashMap<EntityManagerFactory, Map>();
            for (PersistenceContextReferenceInfo info : statefulBeanInfo.jndiEnc.persistenceContextRefs) {
                if (info.extended) {
                    EntityManagerFactory entityManagerFactory = emfLinkResolver.resolveLink(info.persistenceUnitName, moduleId);
                    extendedEntityManagerFactories.put(entityManagerFactory, info.properties);
                }
            }
            deployment.setExtendedEntityManagerFactories(new Index<EntityManagerFactory, Map>(extendedEntityManagerFactories));
        }

        if (ejbType.isSession() || ejbType.isMessageDriven()) {
            deployment.setBeanManagedTransaction("Bean".equalsIgnoreCase(bean.transactionType));
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
                    List<Method> finderMethods = new ArrayList<Method>();

                    if (home != null) {
                        AssemblerTool.resolveMethods(finderMethods, home, query.method);
                    }
                    if (localhome != null) {
                        AssemblerTool.resolveMethods(finderMethods, localhome, query.method);
                    }

                    for (Method method : finderMethods) {
                        deployment.addQuery(method, query.queryStatement);
                    }

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
                deployment.setCmrFields(entity.cmpFieldNames.toArray(new String[]{}));

                if (entity.primKeyField != null) {
                    deployment.setPrimaryKeyField(entity.primKeyField);
                }
            }
        }

        deployment.createMethodMap();

        return deployment;
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
            } else if (info.methodParams != null) {
                timeout = toMethod(ejbClass, info);
            }
        } catch (Exception e) {
            warnings.add(e);
        }

        return timeout;
    }

    private Method toMethod(Class clazz, NamedMethodInfo info) {
        Method method = null;
        List<Class> parameterTypes = new ArrayList<Class>();

        if (info.methodParams != null){
            for (String paramType : info.methodParams) {
                try {
                    parameterTypes.add(Classes.forName(paramType, clazz.getClassLoader()));
                } catch (ClassNotFoundException cnfe) {
                    throw new IllegalStateException("Parameter class could not be loaded for type " + paramType, cnfe);
                }
            }
        }

        try {
            method = clazz.getDeclaredMethod(info.methodName, parameterTypes.toArray(new Class[parameterTypes.size()]));
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Callback method does not exist: " + clazz.getName() + "." + info.methodName, e);
        }
        return method;
    }


    private Class loadClass(String className, String messageCode) throws OpenEJBException {
        Class clazz = load(className, messageCode);
        try {
            clazz.getDeclaredMethods();
            clazz.getDeclaredFields();
            clazz.getDeclaredConstructors();
            clazz.getInterfaces();
            return clazz;
        } catch (NoClassDefFoundError e) {
            if (clazz.getClassLoader() != cl) {
                String message = SafeToolkit.messages.format("cl0008", className, clazz.getClassLoader(), cl, e.getMessage());
                throw new OpenEJBException(AssemblerTool.messages.format(messageCode, className, bean.ejbDeploymentId, message), e);
            } else {
                String message = SafeToolkit.messages.format("cl0009", className, clazz.getClassLoader(), e.getMessage());
                throw new OpenEJBException(AssemblerTool.messages.format(messageCode, className, bean.ejbDeploymentId, message), e);
            }
        }
    }

    private Class load(String className, String messageCode) throws OpenEJBException {
        try {
            return Class.forName(className, true, cl);
        } catch (ClassNotFoundException e) {
            String message = SafeToolkit.messages.format("cl0007", className, bean.codebase);
            throw new OpenEJBException(AssemblerTool.messages.format(messageCode, className, bean.ejbDeploymentId, message));
        }
    }
}
