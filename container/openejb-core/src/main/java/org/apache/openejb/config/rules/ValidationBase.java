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
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.ClientModule;
import org.apache.openejb.config.DeploymentModule;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.config.ValidationContext;
import org.apache.openejb.config.ValidationRule;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.EntityBean;
import org.apache.openejb.jee.MethodParams;
import org.apache.openejb.jee.PersistenceType;
import org.apache.openejb.util.Classes;
import org.apache.openejb.util.Join;
import org.apache.openejb.util.Messages;

import java.lang.reflect.Method;

/**
 * @version $Rev$ $Date$
 */
public abstract class ValidationBase implements ValidationRule {
    DeploymentModule module;

    public void validate(final AppModule appModule) {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try {
            for (final EjbModule ejbModule : appModule.getEjbModules()) {
                Thread.currentThread().setContextClassLoader(ejbModule.getClassLoader());

                module = ejbModule;
                validate(ejbModule);
            }
            for (final ClientModule clientModule : appModule.getClientModules()) {
                Thread.currentThread().setContextClassLoader(clientModule.getClassLoader());

                module = clientModule;
                validate(clientModule);
            }
        } finally {
            Thread.currentThread().setContextClassLoader(loader);
        }
    }

    public void validate(final ClientModule appModule) {
    }

    public void validate(final EjbModule appModule) {
    }

    public void error(final EnterpriseBean bean, final String key, final Object... details) {
        error(bean.getEjbName(), key, details);
    }

    private void error(final String componentName, final String key, final Object... details) {
        module.getValidation().error(componentName, key, details);
    }

    public void fail(final EnterpriseBean bean, final String key, final Object... details) {
        fail(bean.getEjbName(), key, details);
    }

    public void fail(final String component, final String key, final Object... details) {
        module.getValidation().fail(component, key, details);
    }

    public void warn(final EnterpriseBean bean, final String key, final Object... details) {
        warn(bean.getEjbName(), key, details);
    }

    protected void warn(final String componentName, final String key, final Object... details) {
        module.getValidation().warn(componentName, key, details);
    }

    public void missingMethod(final ValidationContext set, final EnterpriseBean bean, final String key, final String methodName, final Class returnType, final Class... paramTypes) {
        fail(bean, key, methodName, returnType.getName(), getParameters(paramTypes));
    }

    public void ignoredMethodAnnotation(final String annotationType, final EnterpriseBean bean, final String className, final String methodName, final String beanType) {
        warn(bean, "ignoredMethodAnnotation", annotationType, beanType, className, methodName);
    }

    public void ignoredClassAnnotation(final String annotationType, final EnterpriseBean bean, final String className, final String beanType) {
        warn(bean, "ignoredClassAnnotation", annotationType, beanType, className);
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

    public String getParameters(final Method method) {
        final Class[] params = method.getParameterTypes();
        return getParameters(params);
    }

    public String getParameters(final Class... params) {
        final StringBuilder paramString = new StringBuilder(512);

        if (params.length > 0) {
            paramString.append(params[0].getName());
        }

        for (int i = 1; i < params.length; i++) {
            paramString.append(", ");
            paramString.append(params[i]);
        }

        return paramString.toString();
    }

    public String getParameters(final MethodParams methodParams) {
        if (methodParams == null) {
            return "";
        } else {
            return Join.join(",", methodParams.getMethodParam());
        }
    }

    protected Class loadClass(final String clazz) throws OpenEJBException {
        final ClassLoader cl = module.getClassLoader();
        try {
            return Classes.forName(clazz, cl);
        } catch (final ClassNotFoundException cnfe) {
            throw new OpenEJBException(messages().format("cl0007", clazz, module.getJarLocation()), cnfe);
        }
    }

    private Messages messages() { // new is fine cause for errors only
        return new Messages("org.apache.openejb.util.resources");
    }

    public boolean isCmp(final EnterpriseBean b) {

        if (b instanceof EntityBean) {
            final EntityBean entityBean = (EntityBean) b;
            final PersistenceType persistenceType = entityBean.getPersistenceType();
            return persistenceType == PersistenceType.CONTAINER;
        }
        return false;
    }
}
