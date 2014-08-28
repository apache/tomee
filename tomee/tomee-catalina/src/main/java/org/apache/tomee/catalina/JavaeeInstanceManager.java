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
package org.apache.tomee.catalina;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.core.WebContext;
import org.apache.tomcat.InstanceManager;
import org.apache.webbeans.exception.WebBeansConfigurationException;
import org.apache.webbeans.exception.WebBeansCreationException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.naming.NamingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * @version $Rev$ $Date$
 */
public class JavaeeInstanceManager implements InstanceManager {

    private final WebContext webContext;

    public JavaeeInstanceManager(final WebContext webContext) {
        this.webContext = webContext;
    }

    @Override
    public Object newInstance(final Class<?> clazz) throws IllegalAccessException, InvocationTargetException, NamingException, InstantiationException {
        try {
            final Object object = webContext.newInstance(clazz);
            postConstruct(object, clazz);
            return object;
        } catch (final OpenEJBException | WebBeansCreationException | WebBeansConfigurationException e) {
            throw (InstantiationException) new InstantiationException(e.getMessage()).initCause(e);
        }
    }

    @Override
    public Object newInstance(final String className) throws IllegalAccessException, InvocationTargetException, NamingException, InstantiationException, ClassNotFoundException {
        final ClassLoader classLoader = webContext.getClassLoader();
        return newInstance(className, classLoader);
    }

    @Override
    public Object newInstance(final String className, final ClassLoader classLoader) throws IllegalAccessException, InvocationTargetException, NamingException, InstantiationException, ClassNotFoundException {
        return newInstance(classLoader.loadClass(className));
    }

    @Override
    public void newInstance(final Object o) throws IllegalAccessException, InvocationTargetException, NamingException {
        try {
            webContext.inject(o);
            postConstruct(o, o.getClass());
        } catch (final OpenEJBException e) {
            throw new InjectionFailedException(e);
        }
    }

    @Override
    public void destroyInstance(final Object o) throws IllegalAccessException, InvocationTargetException {
        if (o == null) {
            return;
        }
        preDestroy(o, o.getClass());
        webContext.destroy(o);
    }

    public void inject(final Object o) {
        try {
            webContext.inject(o);
        } catch (final OpenEJBException e) {
            throw new InjectionFailedException(e);
        }
    }

    /**
     * Call postConstruct method on the specified instance recursively from deepest superclass to actual class.
     *
     * @param instance object to call postconstruct methods on
     * @param clazz    (super) class to examine for postConstruct annotation.
     * @throws IllegalAccessException if postConstruct method is inaccessible.
     * @throws java.lang.reflect.InvocationTargetException
     *                                if call fails
     */
    public void postConstruct(final Object instance, final Class<?> clazz)
            throws IllegalAccessException, InvocationTargetException {
        final Class<?> superClass = clazz.getSuperclass();
        if (superClass != Object.class) {
            postConstruct(instance, superClass);
        }

        final Method[] methods = clazz.getDeclaredMethods();

        Method postConstruct = null;
        for (final Method method : methods) {
            if (method.isAnnotationPresent(PostConstruct.class)) {
                if ((postConstruct != null)
                        || (method.getParameterTypes().length != 0)
                        || (Modifier.isStatic(method.getModifiers()))
                        || (method.getExceptionTypes().length > 0)
                        || (!method.getReturnType().getName().equals("void"))) {
                    throw new IllegalArgumentException("Invalid PostConstruct annotation. @PostConstruct methods "
                            + "should respect the following constraints:\n"
                            + "- no parameter (" + (method.getParameterTypes().length == 0) + ")\n"
                            + "- no exception should be declared (" + (method.getExceptionTypes().length == 0) + ")\n"
                            + "- should return void (" + method.getReturnType().getName().equals("void") + ")\n"
                            + "- should not be static (" + !Modifier.isStatic(method.getModifiers()) + ")\n");
                }
                postConstruct = method;
            }
        }

        // At the end the postconstruct annotated
        // method is invoked
        if (postConstruct != null) {
            final boolean accessibility = postConstruct.isAccessible();
            postConstruct.setAccessible(true);
            postConstruct.invoke(instance);
            postConstruct.setAccessible(accessibility);
        }

    }


    /**
     * Call preDestroy method on the specified instance recursively from deepest superclass to actual class.
     *
     * @param instance object to call preDestroy methods on
     * @param clazz    (super) class to examine for preDestroy annotation.
     * @throws IllegalAccessException if preDestroy method is inaccessible.
     * @throws java.lang.reflect.InvocationTargetException
     *                                if call fails
     */
    protected void preDestroy(final Object instance, final Class<?> clazz)
            throws IllegalAccessException, InvocationTargetException {
        final Class<?> superClass = clazz.getSuperclass();
        if (superClass != Object.class) {
            preDestroy(instance, superClass);
        }

        final Method[] methods = clazz.getDeclaredMethods();
        Method preDestroy = null;
        for (final Method method : methods) {
            if (method.isAnnotationPresent(PreDestroy.class)) {
                if ((method.getParameterTypes().length != 0)
                        || (Modifier.isStatic(method.getModifiers()))
                        || (method.getExceptionTypes().length > 0)
                        || (!method.getReturnType().getName().equals("void"))) {
                    throw new IllegalArgumentException("Invalid PreDestroy annotation");
                }
                preDestroy = method;
                break;
            }
        }

        // At the end the postconstruct annotated
        // method is invoked
        if (preDestroy != null) {
            final boolean accessibility = preDestroy.isAccessible();
            preDestroy.setAccessible(true);
            preDestroy.invoke(instance);
            preDestroy.setAccessible(accessibility);
        }
    }

}
