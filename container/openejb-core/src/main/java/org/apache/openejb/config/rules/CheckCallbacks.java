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
package org.apache.openejb.config.rules;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.AfterBegin;
import javax.ejb.AfterCompletion;
import javax.ejb.BeforeCompletion;
import javax.ejb.Init;
import javax.ejb.PostActivate;
import javax.ejb.PrePassivate;
import javax.ejb.Remove;
import javax.ejb.SessionSynchronization;
import javax.interceptor.InvocationContext;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.jee.AroundInvoke;
import org.apache.openejb.jee.AroundTimeout;
import org.apache.openejb.jee.CallbackMethod;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.InitMethod;
import org.apache.openejb.jee.Interceptor;
import org.apache.openejb.jee.LifecycleCallback;
import org.apache.openejb.jee.NamedMethod;
import org.apache.openejb.jee.RemoveMethod;
import org.apache.openejb.jee.Session;
import org.apache.openejb.jee.SessionBean;
import org.apache.openejb.jee.SessionType;
import org.apache.openejb.jee.TimerConsumer;
import org.apache.xbean.finder.ClassFinder;

/**
 * @version $Rev$ $Date$
 */
public class CheckCallbacks extends ValidationBase {

    public void validate(EjbModule module) {
        for (EnterpriseBean bean : module.getEjbJar().getEnterpriseBeans()) {
            Class ejbClass = null;
            try {
                ejbClass = loadClass(bean.getEjbClass());
            } catch (OpenEJBException e) {
                continue;
            }

            for (AroundInvoke aroundInvoke : bean.getAroundInvoke()) {
                checkAroundInvoke(ejbClass, aroundInvoke, bean.getEjbName());
            }

            for (LifecycleCallback callback : bean.getPostConstruct()) {
                checkCallback(ejbClass, "PostConstruct", callback, bean);
            }

            for (LifecycleCallback callback : bean.getPreDestroy()) {
                checkCallback(ejbClass, "PreDestroy", callback, bean);
            }

            if (bean instanceof Session ) {
                SessionBean session = (SessionBean) bean;

                if (session.getSessionType() == SessionType.STATEFUL ) {

                    for (LifecycleCallback callback : session.getPrePassivate()) {
                        checkCallback(ejbClass, "PrePassivate", callback, bean);
                    }

                    for (LifecycleCallback callback : session.getPostActivate()) {
                        checkCallback(ejbClass, "PostActivate", callback, bean);
                    }

                    checkSessionSynchronization(ejbClass, session);

                    for (LifecycleCallback callback : session.getAfterBegin()) {
                        checkCallback(ejbClass, "AfterBegin", callback, bean);
                    }

                    for (LifecycleCallback callback : session.getBeforeCompletion()) {
                        checkCallback(ejbClass, "BeforeCompletion", callback, bean);
                    }

                    for (LifecycleCallback callback : session.getAfterCompletion()) {
                        checkCallback(ejbClass, "AfterCompletion", callback, bean, boolean.class);
                    }

                    for (AroundTimeout aroundTimeout : session.getAroundTimeout()) {
                        ignoredAnnotation("aroundTimeout", bean, bean.getEjbClass(), aroundTimeout.getMethodName(), SessionType.STATEFUL.getName());
                    }

                } else {

                    for (LifecycleCallback callback : session.getAfterBegin()) {
                        ignoredAnnotation("afterBegin", bean, bean.getEjbClass(), callback.getMethodName(), session.getSessionType().getName());
                    }

                    for (LifecycleCallback callback : session.getBeforeCompletion()) {
                        ignoredAnnotation("beforeCompletion", bean, bean.getEjbClass(), callback.getMethodName(), session.getSessionType().getName());
                    }

                    for (LifecycleCallback callback : session.getAfterCompletion()) {
                        ignoredAnnotation("afterCompletion", bean, bean.getEjbClass(), callback.getMethodName(), session.getSessionType().getName());
                    }

                    for (LifecycleCallback callback : session.getPrePassivate()) {
                        ignoredAnnotation("PrePassivate", bean, bean.getEjbClass(), callback.getMethodName(), session.getSessionType().getName());
                    }

                    for (LifecycleCallback callback : session.getPostActivate()) {
                        ignoredAnnotation("PostActivate", bean, bean.getEjbClass(), callback.getMethodName(), session.getSessionType().getName());
                    }

                    for (RemoveMethod method : session.getRemoveMethod()) {
                        ignoredAnnotation("Remove", bean, bean.getEjbClass(), method.getBeanMethod().getMethodName(), session.getSessionType().getName());
                    }

                    for (InitMethod method : session.getInitMethod()) {
                        ignoredAnnotation("Init", bean, bean.getEjbClass(), method.getBeanMethod().getMethodName(), session.getSessionType().getName());
                    }
                }
            } else {
                ClassFinder finder = new ClassFinder(ejbClass);

                for (Method method : finder.findAnnotatedMethods(PrePassivate.class)) {
                    ignoredAnnotation("PrePassivate", bean, bean.getEjbClass(), method.getName(), bean.getClass().getSimpleName());
                }

                for (Method method : finder.findAnnotatedMethods(PostActivate.class)) {
                    ignoredAnnotation("PostActivate", bean, bean.getEjbClass(), method.getName(), bean.getClass().getSimpleName());
                }

                for (Method method : finder.findAnnotatedMethods(Remove.class)) {
                    ignoredAnnotation("Remove", bean, bean.getEjbClass(), method.getName(), bean.getClass().getSimpleName());
                }

                for (Method method : finder.findAnnotatedMethods(Init.class)) {
                    ignoredAnnotation("Init", bean, bean.getEjbClass(), method.getName(), bean.getClass().getSimpleName());
                }

                for (Method method : finder.findAnnotatedMethods(AfterBegin.class)) {
                    ignoredAnnotation("afterBegin", bean, bean.getEjbClass(), method.getName(), bean.getClass().getSimpleName());
                }

                for (Method method : finder.findAnnotatedMethods(BeforeCompletion.class)) {
                    ignoredAnnotation("beforeCompletion", bean, bean.getEjbClass(), method.getName(), bean.getClass().getSimpleName());
                }

                for (Method method : finder.findAnnotatedMethods(AfterCompletion.class)) {
                    ignoredAnnotation("afterCompletion", bean, bean.getEjbClass(), method.getName(), bean.getClass().getSimpleName());
                }
            }

            if (bean instanceof TimerConsumer) {
                TimerConsumer timerConsumer = (TimerConsumer) bean;
                checkTimeOut(ejbClass, timerConsumer.getTimeoutMethod(), bean);

                for (AroundTimeout aroundTimeout : bean.getAroundTimeout()) {
                    checkAroundTimeout(ejbClass, aroundTimeout, bean.getEjbName());
                }
            }
        }

        for (Interceptor interceptor : module.getEjbJar().getInterceptors()) {
            Class interceptorClass = null;
            try {
                interceptorClass = loadClass(interceptor.getInterceptorClass());
            } catch (OpenEJBException e) {
                continue;
            }

            for (AroundInvoke aroundInvoke : interceptor.getAroundInvoke()) {
                checkAroundInvoke(interceptorClass, aroundInvoke, "Interceptor");
            }

            for (AroundTimeout aroundTimeout : interceptor.getAroundTimeout()) {
                checkAroundTimeout(interceptorClass, aroundTimeout, "Interceptor");
            }

            for (LifecycleCallback callback : interceptor.getPostConstruct()) {
                checkCallback(interceptorClass, "PostConstruct", callback, interceptor);
            }

            for (LifecycleCallback callback : interceptor.getPreDestroy()) {
                checkCallback(interceptorClass, "PreDestroy", callback, interceptor);
            }

            for (LifecycleCallback callback : interceptor.getPrePassivate()) {
                checkCallback(interceptorClass, "PrePassivate", callback, interceptor);
            }

            for (LifecycleCallback callback : interceptor.getPostActivate()) {
                checkCallback(interceptorClass, "PostActivate", callback, interceptor);
            }

            for (LifecycleCallback callback : interceptor.getAfterBegin()) {
                checkCallback(interceptorClass, "AfterBegin", callback, interceptor);
            }

            for (LifecycleCallback callback : interceptor.getBeforeCompletion()) {
                checkCallback(interceptorClass, "BeforeCompletion", callback, interceptor);
            }

            for (LifecycleCallback callback : interceptor.getAfterCompletion()) {
                checkCallback(interceptorClass, "AfterCompletion", callback, interceptor);
            }
        }
    }

    private void checkAroundTypeInvoke(String aroundType, Class ejbClass, String declaringClassName, String declaringMethodName, String componentName) {
        try {
            Method method = getMethod(ejbClass, declaringMethodName, InvocationContext.class);

            Class<?> returnType = method.getReturnType();

            if (!returnType.equals(Object.class)) {
                fail(componentName, "aroundInvoke.badReturnType", aroundType, declaringMethodName, returnType.getName(), declaringClassName);
            }

            boolean throwsException = false;
            for (Class<?> exceptionType : method.getExceptionTypes()) {
                if (exceptionType.getName().equals(Exception.class.getName())) {
                    throwsException = true;
                }
            }

            if (!throwsException) {
                fail(componentName, "aroundInvoke.mustThrowException", aroundType, declaringMethodName, declaringClassName);
            }

        } catch (NoSuchMethodException e) {
            List<Method> possibleMethods = getMethods(ejbClass, declaringMethodName);

            if (possibleMethods.size() == 0) {
                fail(componentName, "aroundInvoke.missing", aroundType, declaringMethodName, declaringClassName);
            } else if (possibleMethods.size() == 1) {
                fail(componentName, "aroundInvoke.invalidArguments", aroundType, declaringMethodName, getParameters(possibleMethods.get(0)), declaringClassName);
            } else {
                fail(componentName, "aroundInvoke.missing.possibleTypo", aroundType, declaringMethodName, possibleMethods.size(), declaringClassName);
            }
        }
    }

    private void checkAroundInvoke(Class<?> ejbClass, AroundInvoke aroundInvoke, String componentName) {
        checkAroundTypeInvoke("AroundInvoke", ejbClass, aroundInvoke.getClassName(), aroundInvoke.getMethodName(), componentName);
    }

    private void checkAroundTimeout(Class<?> ejbClass, AroundTimeout aroundTimeout, String componentName) {
        checkAroundTypeInvoke("AroundTimeout", ejbClass, aroundTimeout.getClassName(), aroundTimeout.getMethodName(), componentName);
    }

    private void ignoredAnnotation(String annotationType, EnterpriseBean bean, String className, String methodName, String beanType) {
        warn(bean, "ignoredAnnotation", annotationType, beanType, className, methodName);
    }

    private void checkCallback(Class<?> ejbClass, String type, CallbackMethod callback, EnterpriseBean bean, Class... parameterTypes) {
        try {
            Method method = getMethod(ejbClass, callback.getMethodName(), parameterTypes);

            Class<?> returnType = method.getReturnType();

            if (!returnType.equals(Void.TYPE)) {
                fail(bean, "callback.badReturnType", type, callback.getMethodName(), returnType.getName(), callback.getClassName());
            }

            int methodModifiers = method.getModifiers();
            if (Modifier.isFinal(methodModifiers) || Modifier.isStatic(methodModifiers)) {
                fail(bean, "callback.badModifier", type, callback.getMethodName(), callback.getClassName());
            }
        } catch (NoSuchMethodException e) {
            List<Method> possibleMethods = getMethods(ejbClass, callback.getMethodName());

            if (possibleMethods.size() == 0) {
                fail(bean, "callback.missing", type, callback.getMethodName(), callback.getClassName());
            } else if (possibleMethods.size() != parameterTypes.length) {
                fail(bean, "callback.invalidArguments", type, callback.getMethodName(), getParameters(possibleMethods.get(0)), callback.getClassName());
            } else {
                fail(bean, "callback.missing.possibleTypo", type, callback.getMethodName(), possibleMethods.size(), callback.getClassName());
            }
        }
    }

    private void checkCallback(Class interceptorClass, String type, CallbackMethod callback, Interceptor interceptor) {
        try {
            Method method = getMethod(interceptorClass, callback.getMethodName(), InvocationContext.class);

            Class<?> returnType = method.getReturnType();

            if (!returnType.equals(Void.TYPE)) {
                fail("Interceptor", "interceptor.callback.badReturnType", interceptorClass, type, callback.getMethodName(), returnType.getName());
            }
        } catch (NoSuchMethodException e) {
            List<Method> possibleMethods = getMethods(interceptorClass, callback.getMethodName());

            if (possibleMethods.size() == 0) {
                fail("Interceptor", "interceptor.callback.missing", type, callback.getMethodName(), interceptorClass.getName());
            } else if (possibleMethods.size() == 1) {
                fail("Interceptor", "interceptor.callback.invalidArguments", type, callback.getMethodName(), getParameters(possibleMethods.get(0)), interceptorClass.getName());
            } else {
                fail("Interceptor", "interceptor.callback.missing.possibleTypo", type, callback.getMethodName(), possibleMethods.size(), interceptorClass.getName());
            }
        }
    }

    private void checkSessionSynchronization(Class ejbClass, SessionBean bean) {
        if (SessionSynchronization.class.isAssignableFrom(ejbClass)) {
            if (bean.getAfterBeginMethod() != null || bean.getBeforeCompletionMethod() != null || bean.getAfterCompletionMethod() != null) {
                fail(bean, "calllback.invalidSessionSynchronizationUse", ejbClass.getName());
            } else {
                ClassFinder classFinder = new ClassFinder(ejbClass);
                if (classFinder.findAnnotatedMethods(AfterBegin.class).size() > 0 || classFinder.findAnnotatedMethods(AfterBegin.class).size() > 0
                        || classFinder.findAnnotatedMethods(AfterBegin.class).size() > 0) {
                    fail(bean, "callback.sessionSynchronization.invalidUse", ejbClass.getName());
                }
            }
        }
    }

    private void checkTimeOut(Class ejbClass, NamedMethod timeout, EnterpriseBean bean) {
        if (timeout == null) return;
        try {
            Method method = getMethod(ejbClass, timeout.getMethodName(), javax.ejb.Timer.class);

            Class<?> returnType = method.getReturnType();

            if (!returnType.equals(Void.TYPE)) {
                fail(bean, "timeout.badReturnType", timeout.getMethodName(), returnType.getName());
            }
        } catch (NoSuchMethodException e) {
            List<Method> possibleMethods = getMethods(ejbClass, timeout.getMethodName());

            if (possibleMethods.size() == 0) {
                fail(bean, "timeout.missing", timeout.getMethodName());
            } else if (possibleMethods.size() == 1) {
                fail(bean, "timeout.invalidArguments", timeout.getMethodName(), getParameters(possibleMethods.get(0)));
            } else {
                fail(bean, "timeout.missing.possibleTypo", timeout.getMethodName(), possibleMethods.size());
            }
        }
    }

    private Method getMethod(Class clazz, String methodName, Class... parameterTypes) throws NoSuchMethodException {
        NoSuchMethodException original = null;
        while (clazz != null){
            try {
                return clazz.getDeclaredMethod(methodName, parameterTypes);
            } catch (NoSuchMethodException e) {
                if (original == null) original = e;
            }
            clazz = clazz.getSuperclass();
        }
        throw original;
    }

    private List<Method> getMethods(Class clazz, String methodName) {
        List<Method> methods = new ArrayList<Method>();
        while (clazz != null){
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.getName().equals(methodName)){
                    methods.add(method);
                }
            }
            clazz = clazz.getSuperclass();
        }
        return methods;
    }

}
