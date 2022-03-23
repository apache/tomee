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
package org.apache.tomee.common;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import javax.naming.NamingException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceUnit;
import jakarta.xml.ws.WebServiceRef;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class LegacyAnnotationProcessor {

    protected javax.naming.Context context;

    public LegacyAnnotationProcessor(final javax.naming.Context context) {
        this.context = context;
    }


    /**
     * Call postConstruct method on the specified instance.
     * @param instance
     * @throws java.lang.IllegalAccessException
     * @throws java.lang.reflect.InvocationTargetException
     */
    public void postConstruct(final Object instance) throws IllegalAccessException, InvocationTargetException {

        Class<?> clazz = instance.getClass();

        while (clazz != null) {
            final Method[] methods = clazz.getDeclaredMethods();
            Method postConstruct = null;
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].isAnnotationPresent(PostConstruct.class)) {
                    if ((postConstruct != null)
                            || (methods[i].getParameterTypes().length != 0)
                            || (Modifier.isStatic(methods[i].getModifiers()))
                            || (methods[i].getExceptionTypes().length > 0)
                            || (!methods[i].getReturnType().getName().equals("void"))) {
                        throw new IllegalArgumentException("Invalid PostConstruct annotation");
                    }
                    postConstruct = methods[i];
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

            clazz = clazz.getSuperclass();
        }
    }


    /**
     * Call preDestroy method on the specified instance.
     * @param instance
     * @throws java.lang.IllegalAccessException
     * @throws java.lang.reflect.InvocationTargetException
     */
    public void preDestroy(final Object instance) throws IllegalAccessException, InvocationTargetException {

        Class<?> clazz = instance.getClass();

        while (clazz != null) {
            final Method[] methods = clazz.getDeclaredMethods();
            Method preDestroy = null;
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].isAnnotationPresent(PreDestroy.class)) {
                    if ((preDestroy != null)
                            || (methods[i].getParameterTypes().length != 0)
                            || (Modifier.isStatic(methods[i].getModifiers()))
                            || (methods[i].getExceptionTypes().length > 0)
                            || (!methods[i].getReturnType().getName().equals("void"))) {
                        throw new IllegalArgumentException("Invalid PreDestroy annotation");
                    }
                    preDestroy = methods[i];
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

            clazz = clazz.getSuperclass();
        }
    }


    /**
     * Inject resources in specified instance.
     * @param instance
     * @throws java.lang.IllegalAccessException
     * @throws java.lang.reflect.InvocationTargetException
     * @throws javax.naming.NamingException
     */
    public void processAnnotations(final Object instance) throws IllegalAccessException, InvocationTargetException, NamingException {

        if (context == null) {
            // No resource injection
            return;
        }

        Class<?> clazz = instance.getClass();

        while (clazz != null) {
            // Initialize fields annotations
            final Field[] fields = clazz.getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                if (fields[i].isAnnotationPresent(Resource.class)) {
                    final Resource annotation =
                            fields[i].getAnnotation(Resource.class);
                    lookupFieldResource(context, instance, fields[i],
                            annotation.name(), clazz);
                }
                if (fields[i].isAnnotationPresent(EJB.class)) {
                    final EJB annotation = fields[i].getAnnotation(EJB.class);
                    lookupFieldResource(context, instance, fields[i],
                            annotation.name(), clazz);
                }
                if (fields[i].isAnnotationPresent(WebServiceRef.class)) {
                    final WebServiceRef annotation =
                            fields[i].getAnnotation(WebServiceRef.class);
                    lookupFieldResource(context, instance, fields[i],
                            annotation.name(), clazz);
                }
                if (fields[i].isAnnotationPresent(PersistenceContext.class)) {
                    final PersistenceContext annotation =
                            fields[i].getAnnotation(PersistenceContext.class);
                    lookupFieldResource(context, instance, fields[i],
                            annotation.name(), clazz);
                }
                if (fields[i].isAnnotationPresent(PersistenceUnit.class)) {
                    final PersistenceUnit annotation =
                            fields[i].getAnnotation(PersistenceUnit.class);
                    lookupFieldResource(context, instance, fields[i],
                            annotation.name(), clazz);
                }
            }

            // Initialize methods annotations
            final Method[] methods = clazz.getDeclaredMethods();

            for (int i = 0; i < methods.length; i++) {
                if (methods[i].isAnnotationPresent(Resource.class)) {
                    final Resource annotation = methods[i].getAnnotation(Resource.class);
                    lookupMethodResource(context, instance, methods[i],
                            annotation.name(), clazz);
                }
                if (methods[i].isAnnotationPresent(EJB.class)) {
                    final EJB annotation = methods[i].getAnnotation(EJB.class);
                    lookupMethodResource(context, instance, methods[i],
                            annotation.name(), clazz);
                }
                if (methods[i].isAnnotationPresent(WebServiceRef.class)) {
                    final WebServiceRef annotation =
                            methods[i].getAnnotation(WebServiceRef.class);
                    lookupMethodResource(context, instance, methods[i],
                            annotation.name(), clazz);
                }
                if (methods[i].isAnnotationPresent(PersistenceContext.class)) {
                    final PersistenceContext annotation =
                            methods[i].getAnnotation(PersistenceContext.class);
                    lookupMethodResource(context, instance, methods[i],
                            annotation.name(), clazz);
                }
                if (methods[i].isAnnotationPresent(PersistenceUnit.class)) {
                    final PersistenceUnit annotation =
                            methods[i].getAnnotation(PersistenceUnit.class);
                    lookupMethodResource(context, instance, methods[i],
                            annotation.name(), clazz);
                }
            }

            clazz = clazz.getSuperclass();
        }
    }


    /**
     * Inject resources in specified field.
     * @param context
     * @param instance
     * @param field
     * @param name
     * @param clazz
     * @throws javax.naming.NamingException
     * @throws java.lang.IllegalAccessException
     */
    protected static void lookupFieldResource(final javax.naming.Context context,
                                              final Object instance, final Field field, final String name, final Class<?> clazz)
            throws NamingException, IllegalAccessException {

        Object lookedupResource = null;
        boolean accessibility = false;

        if ((name != null) && (name.length() > 0)) {
            lookedupResource = context.lookup(name);
        } else {
            lookedupResource = context.lookup(clazz.getName() + "/" + field.getName());
        }

        accessibility = field.isAccessible();
        field.setAccessible(true);
        field.set(instance, lookedupResource);
        field.setAccessible(accessibility);
    }


    /**
     * Inject resources in specified method.
     * @param context
     * @param instance
     * @param method
     * @param name
     * @param clazz
     * @throws javax.naming.NamingException
     * @throws java.lang.IllegalAccessException
     * @throws java.lang.reflect.InvocationTargetException
     */
    protected static void lookupMethodResource(final javax.naming.Context context,
                                               final Object instance, final Method method, final String name, final Class<?> clazz)
            throws NamingException, IllegalAccessException, InvocationTargetException {

        if (!method.getName().startsWith("set")
                || method.getParameterTypes().length != 1
                || !method.getReturnType().getName().equals("void")) {
            throw new IllegalArgumentException("Invalid method resource injection annotation");
        }

        Object lookedupResource = null;
        boolean accessibility = false;

        if ((name != null) &&
                (name.length() > 0)) {
            lookedupResource = context.lookup(name);
        } else {
            lookedupResource = context.lookup(clazz.getName() + "/" + method.getName().substring(3));
        }

        accessibility = method.isAccessible();
        method.setAccessible(true);
        method.invoke(instance, lookedupResource);
        method.setAccessible(accessibility);
    }
}
