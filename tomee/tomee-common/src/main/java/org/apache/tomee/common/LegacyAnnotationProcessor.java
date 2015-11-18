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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.naming.NamingException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import javax.xml.ws.WebServiceRef;
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
     */
    public void postConstruct(final Object instance) throws IllegalAccessException, InvocationTargetException {

        Class<?> clazz = instance.getClass();

        while (clazz != null) {
            final Method[] methods = clazz.getDeclaredMethods();
            Method postConstruct = null;
            for (final Method method : methods) {
                if (method.isAnnotationPresent(PostConstruct.class)) {
                    if ((postConstruct != null)
                            || (method.getParameterTypes().length != 0)
                            || (Modifier.isStatic(method.getModifiers()))
                            || (method.getExceptionTypes().length > 0)
                            || (!method.getReturnType().getName().equals("void"))) {
                        throw new IllegalArgumentException("Invalid PostConstruct annotation");
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

            clazz = clazz.getSuperclass();
        }
    }


    /**
     * Call preDestroy method on the specified instance.
     */
    public void preDestroy(final Object instance) throws IllegalAccessException, InvocationTargetException {

        Class<?> clazz = instance.getClass();

        while (clazz != null) {
            final Method[] methods = clazz.getDeclaredMethods();
            Method preDestroy = null;
            for (final Method method : methods) {
                if (method.isAnnotationPresent(PreDestroy.class)) {
                    if ((preDestroy != null)
                            || (method.getParameterTypes().length != 0)
                            || (Modifier.isStatic(method.getModifiers()))
                            || (method.getExceptionTypes().length > 0)
                            || (!method.getReturnType().getName().equals("void"))) {
                        throw new IllegalArgumentException("Invalid PreDestroy annotation");
                    }
                    preDestroy = method;
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
            for (final Field field : fields) {
                if (field.isAnnotationPresent(Resource.class)) {
                    final Resource annotation =
                            field.getAnnotation(Resource.class);
                    lookupFieldResource(context, instance, field,
                            annotation.name(), clazz);
                }
                if (field.isAnnotationPresent(EJB.class)) {
                    final EJB annotation = field.getAnnotation(EJB.class);
                    lookupFieldResource(context, instance, field,
                            annotation.name(), clazz);
                }
                if (field.isAnnotationPresent(WebServiceRef.class)) {
                    final WebServiceRef annotation =
                            field.getAnnotation(WebServiceRef.class);
                    lookupFieldResource(context, instance, field,
                            annotation.name(), clazz);
                }
                if (field.isAnnotationPresent(PersistenceContext.class)) {
                    final PersistenceContext annotation =
                            field.getAnnotation(PersistenceContext.class);
                    lookupFieldResource(context, instance, field,
                            annotation.name(), clazz);
                }
                if (field.isAnnotationPresent(PersistenceUnit.class)) {
                    final PersistenceUnit annotation =
                            field.getAnnotation(PersistenceUnit.class);
                    lookupFieldResource(context, instance, field,
                            annotation.name(), clazz);
                }
            }

            // Initialize methods annotations
            final Method[] methods = clazz.getDeclaredMethods();

            for (final Method method : methods) {
                if (method.isAnnotationPresent(Resource.class)) {
                    final Resource annotation = method.getAnnotation(Resource.class);
                    lookupMethodResource(context, instance, method,
                            annotation.name(), clazz);
                }
                if (method.isAnnotationPresent(EJB.class)) {
                    final EJB annotation = method.getAnnotation(EJB.class);
                    lookupMethodResource(context, instance, method,
                            annotation.name(), clazz);
                }
                if (method.isAnnotationPresent(WebServiceRef.class)) {
                    final WebServiceRef annotation =
                            method.getAnnotation(WebServiceRef.class);
                    lookupMethodResource(context, instance, method,
                            annotation.name(), clazz);
                }
                if (method.isAnnotationPresent(PersistenceContext.class)) {
                    final PersistenceContext annotation =
                            method.getAnnotation(PersistenceContext.class);
                    lookupMethodResource(context, instance, method,
                            annotation.name(), clazz);
                }
                if (method.isAnnotationPresent(PersistenceUnit.class)) {
                    final PersistenceUnit annotation =
                            method.getAnnotation(PersistenceUnit.class);
                    lookupMethodResource(context, instance, method,
                            annotation.name(), clazz);
                }
            }

            clazz = clazz.getSuperclass();
        }
    }


    /**
     * Inject resources in specified field.
     */
    protected static void lookupFieldResource(final javax.naming.Context context,
                                              final Object instance, final Field field, final String name, final Class<?> clazz)
            throws NamingException, IllegalAccessException {

        final Object lookedupResource;

        if ((name != null) && (name.length() > 0)) {
            lookedupResource = context.lookup(name);
        } else {
            lookedupResource = context.lookup(clazz.getName() + "/" + field.getName());
        }

        final boolean accessibility = field.isAccessible();
        field.setAccessible(true);
        field.set(instance, lookedupResource);
        field.setAccessible(accessibility);
    }


    /**
     * Inject resources in specified method.
     */
    protected static void lookupMethodResource(final javax.naming.Context context,
                                               final Object instance, final Method method, final String name, final Class<?> clazz)
            throws NamingException, IllegalAccessException, InvocationTargetException {

        if (!method.getName().startsWith("set")
                || method.getParameterTypes().length != 1
                || !method.getReturnType().getName().equals("void")) {
            throw new IllegalArgumentException("Invalid method resource injection annotation");
        }

        final Object lookedupResource;

        if ((name != null) && (name.length() > 0)) {
            lookedupResource = context.lookup(name);
        } else {
            lookedupResource = context.lookup(clazz.getName() + "/" + method.getName().substring(3));
        }

        final boolean accessibility = method.isAccessible();
        method.setAccessible(true);
        method.invoke(instance, lookedupResource);
        method.setAccessible(accessibility);
    }
}
