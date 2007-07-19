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

import org.apache.openejb.config.ValidationRule;
import org.apache.openejb.config.EjbSet;
import org.apache.openejb.config.ValidationError;
import org.apache.openejb.config.ValidationFailure;
import org.apache.openejb.config.ValidationWarning;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.util.SafeToolkit;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @version $Rev$ $Date$
 */
public abstract class ValidationBase implements ValidationRule {
    EjbSet set;

    public abstract void validate(EjbSet set);

    public void error(EnterpriseBean bean, String key, Object... details) {
        ValidationError error = new ValidationError(key);
        error.setDetails(details);
        error.setComponentName(bean.getEjbName());

        set.addError(error);
    }

    public void fail(EnterpriseBean bean, String key, Object... details) {
        ValidationFailure failure = new ValidationFailure(key);
        failure.setDetails(details);
        failure.setComponentName(bean.getEjbName());

        set.addFailure(failure);
    }

    public void fail(String component, String key, Object... details) {
        ValidationFailure failure = new ValidationFailure(key);
        failure.setDetails(details);
        failure.setComponentName(component);

        set.addFailure(failure);
    }

    public void warn(EnterpriseBean bean, String key, Object... details) {
        ValidationWarning warning = new ValidationWarning(key);
        warning.setDetails(details);
        warning.setComponentName(bean.getEjbName());

        set.addWarning(warning);
    }

    public void missingMethod(EnterpriseBean bean, String key, String methodName, Class returnType, Class... paramTypes){
        fail(bean, key, methodName, returnType.getName(), getParameters(paramTypes));
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

    public String getParameters(Method method) {
        Class[] params = method.getParameterTypes();
        return getParameters(params);
    }

    public String getParameters(Class... params) {
        StringBuffer paramString = new StringBuffer(512);

        if (params.length > 0) {
            paramString.append(params[0].getName());
        }

        for (int i = 1; i < params.length; i++) {
            paramString.append(", ");
            paramString.append(params[i]);
        }

        return paramString.toString();
    }

    protected Class loadClass(String clazz) throws OpenEJBException {
        ClassLoader cl = set.getClassLoader();
        try {
            return cl.loadClass(clazz);
        } catch (ClassNotFoundException cnfe) {
            throw new OpenEJBException(SafeToolkit.messages.format("cl0007", clazz, set.getJarPath()), cnfe);
        }
    }

    public String join(List list, String s) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < list.size(); i++) {
            Object object = list.get(i);
            sb.append(object.toString());
            sb.append(s);
        }
        if (sb.length() > 0) {
            sb.delete(sb.length() - s.length(), sb.length());
        }
        return sb.toString();
    }
}
