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
package org.apache.openejb.assembler.classic;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.ejb.LockType;

import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.core.MethodContext;
import org.apache.openejb.util.Classes;
import org.apache.openejb.util.Duration;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.SetAccessible;

public class ConcurrentMethodBuilder {

    public static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP, ConcurrentMethodBuilder.class.getPackage().getName());

    public void build(CoreDeploymentInfo deploymentInfo, EnterpriseBeanInfo beanInfo) {
        Class<?> clazz = deploymentInfo.getBeanClass();

        for (ConcurrentMethodInfo info : beanInfo.concurrentMethodInfos) {
            Method method;
            try {
                method = getMethod(clazz, info.method.methodName, toClasses(info.method.methodParams, clazz.getClassLoader()));
            } catch (NoSuchMethodException e) {
                // method doesn't exist
                logger.warning("Concurrent method does not exist: "+info.method.methodName, e);
                continue;
            } catch (ClassNotFoundException e) {
                logger.warning("Concurrent method param cannot be loaded.", e);
                continue;
            }

            if (info.method.className == null || method.getDeclaringClass().getName().equals(info.method.className)) {
                MethodContext methodContext = deploymentInfo.getMethodContext(method);
                
                if (info.accessTimeout != null) {
                    Duration accessTimeout = new Duration(info.accessTimeout.time, TimeUnit.valueOf(info.accessTimeout.unit));
                    methodContext.setAccessTimeout(accessTimeout);
                }
                
                if (info.lockType != null) {
                    methodContext.setLockType(LockType.valueOf(info.lockType));
                }
            }
        }
    }

    private Class<?>[] toClasses(List<String> params, ClassLoader classLoader) throws ClassNotFoundException {
        if (params == null) {
            return null;
        }
        Class<?>[] paramsArray = new Class[params.size()];
        for (int j = 0; j < paramsArray.length; j++) {
            String methodParam = params.get(j);
            paramsArray[j] = Classes.forName(methodParam, classLoader);

        }
        return paramsArray;
    }

    /**
     * Finds the nearest java.lang.reflect.Method with the given
     * name and parameters.  Callbacks can be private so class.getMethod() cannot be used.  Searching
     * starts by looking in the specified class, if the method is not found searching continues with
     * the immediate parent and continues recurssively until the method is found or java.lang.Object
     * is reached.  If the method is not found a NoSuchMethodException is thrown.
     *
     * @param clazz
     * @param methodName
     * @param parameterTypes
     * @return
     * @throws NoSuchMethodException if the method is not found in this class or any of its parent classes
     */
    private Method getMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
        NoSuchMethodException original = null;
        while (clazz != null){
            try {
                Method method = clazz.getDeclaredMethod(methodName, parameterTypes);
                return SetAccessible.on(method);
            } catch (NoSuchMethodException e) {
                if (original == null) original = e;
            }
            clazz = clazz.getSuperclass();
        }
        throw original;
    }

}
