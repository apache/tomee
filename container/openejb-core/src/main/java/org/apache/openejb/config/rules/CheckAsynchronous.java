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

package org.apache.openejb.config.rules;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.jee.ApplicationException;
import org.apache.openejb.jee.AsyncMethod;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.MethodParams;
import org.apache.openejb.jee.SessionBean;
import org.apache.xbean.finder.ClassFinder;

import jakarta.ejb.Asynchronous;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Future;

/**
 * @version $Rev$ $Date$
 */
public class CheckAsynchronous extends ValidationBase {

    public void validate(final EjbModule module) {
        final Set<String> applicationExceptions = new HashSet<>();
        for (final ApplicationException applicationException : module.getEjbJar().getAssemblyDescriptor().getApplicationException()) {
            applicationExceptions.add(applicationException.getExceptionClass());
        }
        for (final EnterpriseBean bean : module.getEjbJar().getEnterpriseBeans()) {
            Class<?> ejbClass = null;
            try {
                ejbClass = loadClass(bean.getEjbClass());
            } catch (final OpenEJBException e) {
                continue;
            }
            if (bean instanceof SessionBean) {
                final SessionBean session = (SessionBean) bean;
                for (final AsyncMethod asyncMethod : session.getAsyncMethod()) {
                    final Method method = getMethod(ejbClass, asyncMethod);
                    if (method == null) {
                        fail(bean, "asynchronous.missing", asyncMethod.getMethodName(), ejbClass.getName(), getParameters(asyncMethod.getMethodParams()));
                    } else {
                        checkAsynchronousMethod(session, ejbClass, method, applicationExceptions);
                    }
                }

                for (final String className : session.getAsynchronousClasses()) {
                    try {
                        final Class<?> cls = loadClass(className);
                        for (final Method method : cls.getDeclaredMethods()) {
                            if (Modifier.isPublic(method.getModifiers()) && !method.isSynthetic()) {
                                checkAsynchronousMethod(session, ejbClass, method, applicationExceptions);
                            }
                        }
                    } catch (final OpenEJBException e) {
                        //ignore ?
                    }
                }
            } else {
                final ClassFinder classFinder = new ClassFinder(ejbClass);
                for (final Method method : classFinder.findAnnotatedMethods(Asynchronous.class)) {
                    ignoredMethodAnnotation("Asynchronous", bean, bean.getEjbClass(), method.getName(), bean.getClass().getSimpleName());
                }
                if (ejbClass.getAnnotation(Asynchronous.class) != null) {
                    ignoredClassAnnotation("Asynchronous", bean, bean.getEjbClass(), bean.getClass().getSimpleName());
                }
            }
        }
    }

    private void checkAsynchronousMethod(final SessionBean bean, final Class<?> ejbClass, final Method method, final Set<String> applicationExceptions) {
        final Class<?> retType = method.getReturnType();
        if (retType != void.class && retType != Future.class) {
            fail(bean, "asynchronous.badReturnType", method.getName(), retType.getName(), ejbClass.getName());
        }
        if (retType == void.class) {
            final String invalidThrowCauses = checkThrowCauses(method.getExceptionTypes(), applicationExceptions);
            if (invalidThrowCauses != null) {
                fail(bean, "asynchronous.badExceptionType", method.getName(), ejbClass.getName(), invalidThrowCauses);
            }
        }
    }

    /**
     * If the return value of the target method is void, it is not allowed to throw any application exception
     *
     * @param exceptionTypes
     * @param applicationExceptions
     * @return
     */
    private String checkThrowCauses(final Class<?>[] exceptionTypes, final Set<String> applicationExceptions) {
        StringBuilder buffer = null;
        for (final Class<?> exceptionType : exceptionTypes) {
            if (applicationExceptions.contains(exceptionType.getName()) || !Exception.class.isAssignableFrom(exceptionType) || RuntimeException.class.isAssignableFrom(exceptionType)) {
                continue;
            }
            if (buffer == null) {
                buffer = new StringBuilder(exceptionType.getName());
            } else {
                buffer.append(",").append(exceptionType.getName());
            }
        }
        return buffer == null ? null : buffer.toString();
    }

    private Method getMethod(final Class<?> clazz, final AsyncMethod asyncMethod) {
        try {
            final MethodParams methodParams = asyncMethod.getMethodParams();
            final Class<?>[] parameterTypes;
            if (methodParams != null) {
                parameterTypes = new Class[methodParams.getMethodParam().size()];
                int arrayIndex = 0;
                for (final String parameterType : methodParams.getMethodParam()) {
                    parameterTypes[arrayIndex++] = loadClass(parameterType);
                }
            } else {
                parameterTypes = new Class[0];
            }
            return clazz.getMethod(asyncMethod.getMethodName(), parameterTypes);
        } catch (final NoSuchMethodException e) {
            return null;
        } catch (final OpenEJBException e) {
            throw new OpenEJBRuntimeException(e);
        }
    }
}
