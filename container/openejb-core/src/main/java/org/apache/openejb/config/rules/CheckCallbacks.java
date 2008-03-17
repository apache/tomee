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

import org.apache.openejb.config.EjbModule;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.AroundInvoke;
import org.apache.openejb.jee.CallbackMethod;
import org.apache.openejb.jee.LifecycleCallback;
import org.apache.openejb.jee.Session;
import org.apache.openejb.jee.Interceptor;
import org.apache.openejb.jee.TimerConsumer;
import org.apache.openejb.jee.NamedMethod;
import org.apache.openejb.OpenEJBException;

import javax.interceptor.InvocationContext;
import java.lang.reflect.Method;
import java.util.List;
import java.util.ArrayList;

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

            if (bean instanceof Session) {
                Session session = (Session) bean;

                for (LifecycleCallback callback : session.getPrePassivate()) {
                    checkCallback(ejbClass, "PrePassivate", callback, bean);
                }

                for (LifecycleCallback callback : session.getPostActivate()) {
                    checkCallback(ejbClass, "PostActivate", callback, bean);
                }

            }

            if (bean instanceof TimerConsumer) {
                TimerConsumer timerConsumer = (TimerConsumer) bean;
                checkTimeOut(ejbClass, timerConsumer.getTimeoutMethod(), bean);
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
        }
    }

    private void checkAroundInvoke(Class ejbClass, AroundInvoke aroundInvoke, String componentName) {
        try {
            Method method = getMethod(ejbClass, aroundInvoke.getMethodName(), InvocationContext.class);

            Class<?> returnType = method.getReturnType();

            if (!returnType.equals(Object.class)) {
                fail(componentName, "aroundInvoke.badReturnType", aroundInvoke.getMethodName(), returnType.getName(),aroundInvoke.getClassName());
            }

            boolean throwsException = false;
            for (Class<?> exceptionType : method.getExceptionTypes()) {
                if (exceptionType.getName().equals(Exception.class.getName())) {
                    throwsException = true;
                }
            }

            if (!throwsException) {
                fail(componentName, "aroundInvoke.mustThrowException", aroundInvoke.getMethodName(), aroundInvoke.getClassName());
            }

        } catch (NoSuchMethodException e) {
            List<Method> possibleMethods = getMethods(ejbClass, aroundInvoke.getMethodName());

            if (possibleMethods.size() == 0) {
                fail(componentName, "aroundInvoke.missing", aroundInvoke.getMethodName(), aroundInvoke.getClassName());
            } else if (possibleMethods.size() == 1) {
                fail(componentName, "aroundInvoke.invalidArguments", aroundInvoke.getMethodName(), getParameters(possibleMethods.get(0)), aroundInvoke.getClassName());
            } else {
                fail(componentName, "aroundInvoke.missing.possibleTypo", aroundInvoke.getMethodName(), possibleMethods.size(), aroundInvoke.getClassName());
            }
        }
    }

    private void checkCallback(Class ejbClass, String type, CallbackMethod callback, EnterpriseBean bean) {
        try {
            Method method = getMethod(ejbClass, callback.getMethodName());

            Class<?> returnType = method.getReturnType();

            if (!returnType.equals(Void.TYPE)) {
                fail(bean, "callback.badReturnType", type, callback.getMethodName(), returnType.getName(), callback.getClassName());
            }
        } catch (NoSuchMethodException e) {
            List<Method> possibleMethods = getMethods(ejbClass, callback.getMethodName());

            if (possibleMethods.size() == 0) {
                fail(bean, "callback.missing", type, callback.getMethodName(), callback.getClassName());
            } else if (possibleMethods.size() == 1) {
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
        }
        return methods;
    }

}
