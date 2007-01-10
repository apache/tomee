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
import org.apache.openejb.core.interceptor.InterceptorData;
import org.apache.openejb.core.ivm.naming.IvmContext;
import org.apache.openejb.util.Messages;
import org.apache.openejb.util.SafeToolkit;

import javax.persistence.EntityManagerFactory;
import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class EnterpriseBeanBuilder {
    protected static final Messages messages = new Messages("org.apache.openejb.util.resources");
    private final EnterpriseBeanInfo bean;
    private final List<InterceptorInfo> defaultInterceptors;
    private final BeanType ejbType;
    private final ClassLoader cl;
    private final Map<String, Map<String, EntityManagerFactory>> factories;
    private List<Exception> warnings = new ArrayList<Exception>();

    public EnterpriseBeanBuilder(ClassLoader cl, EnterpriseBeanInfo bean, List<InterceptorInfo> defaultInterceptors, Map<String, Map<String, EntityManagerFactory>> factories) {
        this.bean = bean;
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
        this.factories = factories;
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

        Class businessLocal = null;
        if (bean.businessLocal != null) {
            businessLocal = loadClass(bean.businessLocal, "classNotFound.businessLocal");
        }

        Class businessRemote = null;
        if (bean.businessRemote != null) {
            businessRemote = loadClass(bean.businessRemote, "classNotFound.businessRemote");
        }

        Class primaryKey = null;
        if (ejbType.isEntity() && ((EntityBeanInfo) bean).primKeyClass != null) {
            String className = ((EntityBeanInfo) bean).primKeyClass;
            primaryKey = loadClass(className, "classNotFound.primaryKey");
        }

        final String transactionType = bean.transactionType;

        JndiEncBuilder jndiEncBuilder = new JndiEncBuilder(bean.jndiEnc, transactionType, ejbType, factories, new File(bean.codebase).getPath());
        IvmContext root = (IvmContext) jndiEncBuilder.build();

        DeploymentContext deploymentContext = new DeploymentContext(bean.ejbDeploymentId, cl, root);
        CoreDeploymentInfo deployment;
        if (BeanType.MESSAGE_DRIVEN != ejbType) {
            deployment = new CoreDeploymentInfo(deploymentContext, ejbClass, home, remote, localhome, local, businessLocal, businessRemote, primaryKey, ejbType);
        } else {
            MessageDrivenBeanInfo messageDrivenBeanInfo = (MessageDrivenBeanInfo) bean;
            Class mdbInterface = loadClass(messageDrivenBeanInfo.mdbInterface, "classNotFound.mdbInterface");
            deployment = new CoreDeploymentInfo(deploymentContext, ejbClass, mdbInterface, messageDrivenBeanInfo.activationProperties);
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

        for (PersistenceUnitInfo info : bean.jndiEnc.persistenceUnitRefs) {
            for (InjectionInfo target : info.targets) {
                Class targetClass = loadClass(target.className, "classNotFound.injectionTarget");
                Injection injection = new Injection(info.referenceName, target.propertyName, targetClass);
                deployment.getInjections().add(injection);
            }
        }

        for (PersistenceContextInfo info : bean.jndiEnc.persistenceContextRefs) {
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

        deployment.setPostConstruct(getCallback(ejbClass, bean.postConstruct));
        deployment.setPreDestroy(getCallback(ejbClass, bean.preDestroy));
        deployment.setEjbTimeout(getTimeout(ejbClass, bean.timeoutMethod));

        // interceptors
        InterceptorBuilder interceptorBuilder = new InterceptorBuilder(defaultInterceptors, bean);
        for (Method method : ejbClass.getMethods()) {
            List<InterceptorData> interceptorDatas = interceptorBuilder.build(method);
            deployment.setMethodInterceptors(method, interceptorDatas);
        }

        if (bean instanceof StatefulBeanInfo) {
            StatefulBeanInfo statefulBeanInfo = (StatefulBeanInfo) bean;
            deployment.setPrePassivate(getCallback(ejbClass, statefulBeanInfo.prePassivate));
            deployment.setPostActivate(getCallback(ejbClass, statefulBeanInfo.postActivate));
        }

        if (ejbType.isSession() || ejbType.isMessageDriven()) {
            deployment.setBeanManagedTransaction("Bean".equalsIgnoreCase(bean.transactionType));
        }

        if (ejbType.isEntity()) {
            EntityBeanInfo entity = (EntityBeanInfo) bean;

            deployment.setCmp2(entity.cmpVersion == 2);
            deployment.setIsReentrant(entity.reentrant.equalsIgnoreCase("true"));

            if (ejbType == BeanType.CMP_ENTITY) {
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
                }
                deployment.setCmrFields(entity.cmpFieldNames.toArray(new String[]{}));

                if (entity.primKeyField != null) {
                    deployment.setPrimaryKeyField(entity.primKeyField);
                }
            }
        }
        return deployment;
    }

    public List<Exception> getWarnings() {
        return warnings;
    }

    private Method getCallback(Class ejbClass, List<LifecycleCallbackInfo> callbackInfos) {
        Method callback = null;
        for (LifecycleCallbackInfo info : callbackInfos) {
            try {
                if (ejbClass.getName().equals(info.className)){
                    if (callback != null){
                        throw new IllegalStateException("Spec requirements only allow one callback method of a given type per class.  The following callback will be ignored: "+info.className+"."+info.method);
                    }
                    try {
                        callback = ejbClass.getMethod(info.method);
                    } catch (NoSuchMethodException e) {
                        throw (IllegalStateException) new IllegalStateException("Callback method does not exist: "+info.className+"."+info.method).initCause(e);
                    }

                } else {
                    throw new UnsupportedOperationException("Callback: "+info.className+"."+info.method+" -- We currently do not support callbacks where the callback class is not the bean class.  If you need this feature, please let us know and we will complete it asap.");
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
            if (info.methodParams != null) {
                List<Class> parameterTypes = new ArrayList<Class>();

                for (String paramType : info.methodParams) {
                    try {
                        parameterTypes.add(Class.forName(paramType));
                    } catch (ClassNotFoundException cnfe) {
                        throw (IllegalStateException) new IllegalStateException("Parameter class could not be loaded for type " + paramType).initCause(cnfe);
                    }
                }

                try {
                    timeout = ejbClass.getMethod(info.methodName, parameterTypes.toArray(new Class[parameterTypes.size()]));
                } catch (NoSuchMethodException e) {
                    throw (IllegalStateException) new IllegalStateException("Timeout Callback method does not exist: " + ejbClass.getName() + "." + info.methodName).initCause(e);
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
            clazz.getDeclaredMethods();
            clazz.getDeclaredFields();
            clazz.getDeclaredConstructors();
            clazz.getInterfaces();
            return clazz;
        } catch (NoClassDefFoundError e) {
            if (clazz.getClassLoader() != cl){
                String message = SafeToolkit.messages.format("cl0008", className, clazz.getClassLoader(), cl, e.getMessage());
                throw new OpenEJBException(AssemblerTool.messages.format(messageCode, className, bean.ejbDeploymentId, message),e);
            } else {
                String message = SafeToolkit.messages.format("cl0009", className, clazz.getClassLoader(), e.getMessage());
                throw new OpenEJBException(AssemblerTool.messages.format(messageCode, className, bean.ejbDeploymentId, message),e);
            }
        }
    }

    private Class load(String className, String messageCode) throws OpenEJBException {
        try {
            return cl.loadClass(className);
        } catch (ClassNotFoundException e) {
            String message = SafeToolkit.messages.format("cl0007", className, bean.codebase);
            throw new OpenEJBException(AssemblerTool.messages.format(messageCode, className, bean.ejbDeploymentId, message));
        }
    }

}
