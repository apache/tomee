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

package org.apache.openejb.assembler.classic;

import org.apache.openejb.BeanContext;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.core.interceptor.InterceptorData;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.SetAccessible;

import jakarta.interceptor.InvocationContext;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @version $Rev$ $Date$
 */
public class InterceptorBindingBuilder {

    public static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP, InterceptorBindingBuilder.class.getPackage().getName());
    private final List<InterceptorBindingInfo> packageAndClassBindings;

    public static enum Level {
        PACKAGE, ANNOTATION_CLASS, CLASS, ANNOTATION_METHOD, OVERLOADED_METHOD, EXACT_METHOD
    }

    public static enum Type {
        ADDITION_OR_LOWER_EXCLUSION, SAME_LEVEL_EXCLUSION, SAME_AND_LOWER_EXCLUSION, EXPLICIT_ORDERING
    }

    private final ArrayList<InterceptorBindingInfo> bindings;
    private final Map<String, InterceptorData> interceptors = new HashMap<>();

    public InterceptorBindingBuilder(final ClassLoader cl, final EjbJarInfo ejbJarInfo) throws OpenEJBException {
        bindings = new ArrayList<>(ejbJarInfo.interceptorBindings);
        bindings.sort(new IntercpetorBindingComparator());
        Collections.reverse(bindings);

        packageAndClassBindings = new ArrayList<>();
        for (final InterceptorBindingInfo binding : bindings) {
            final Level level = level(binding);
            if (level == Level.PACKAGE || level == Level.CLASS || level == Level.ANNOTATION_CLASS) {
                packageAndClassBindings.add(binding);
            }
        }

        for (final InterceptorInfo info : ejbJarInfo.interceptors) {
            Class<?> clazz = null;
            try {
                clazz = Class.forName(info.clazz, true, cl);
            } catch (final ClassNotFoundException e) {
                throw new OpenEJBException("Interceptor class cannot be loaded: " + info.clazz);
            }
            final InterceptorData interceptor = new InterceptorData(clazz);

            toMethods(clazz, info.aroundInvoke, interceptor.getAroundInvoke());
            toMethods(clazz, info.postActivate, interceptor.getPostActivate());
            toMethods(clazz, info.prePassivate, interceptor.getPrePassivate());
            toMethods(clazz, info.postConstruct, interceptor.getPostConstruct());
            toMethods(clazz, info.preDestroy, interceptor.getPreDestroy());
            toMethods(clazz, info.afterBegin, interceptor.getAfterBegin());
            toMethods(clazz, info.beforeCompletion, interceptor.getBeforeCompletion());
            toMethods(clazz, info.afterCompletion, interceptor.getAfterCompletion());
            toMethods(clazz, info.aroundTimeout, interceptor.getAroundTimeout());
            interceptors.put(info.clazz, interceptor);
        }
    }

    public void build(final BeanContext beanContext, final EnterpriseBeanInfo beanInfo) {
        Class<?> clazz = beanContext.getBeanClass();

        final InterceptorData beanAsInterceptor = new InterceptorData(clazz);


        if (beanInfo instanceof StatelessBeanInfo || beanInfo instanceof MessageDrivenBeanInfo) {
            /*
             * 4.3.10.2 and 4.5.8
             * If the stateless session bean or MDB instance has an ejbCreate method,
             * the container treats the ejbCreate method as the instance’s PostConstruct method,
             *  and, in this case, the PostConstruct annotation (or deployment descriptor metadata)
             *  can only be applied to the bean’s ejbCreate method.
             */
            final NamedMethodInfo info = new NamedMethodInfo();
            info.className = clazz.getName();
            final Method createMethod = beanContext.getCreateMethod();
            info.methodName = (createMethod != null) ? createMethod.getName(): "ejbCreate";
            info.methodParams = new ArrayList<>();

            try {
                final Method ejbcreate = MethodInfoUtil.toMethod(clazz, info);
                if (ejbcreate != null) {
                    final CallbackInfo ejbcreateAsPostConstruct = new CallbackInfo();
                    ejbcreateAsPostConstruct.className = ejbcreate.getDeclaringClass().getName();
                    ejbcreateAsPostConstruct.method = ejbcreate.getName();
                    beanInfo.postConstruct.add(ejbcreateAsPostConstruct);
                }
            } catch (final IllegalStateException e) {
                // there's no ejbCreate method in stateless bean.
            }

        }

        toMethods(clazz, beanInfo.aroundInvoke, beanAsInterceptor.getAroundInvoke());
        toCallback(clazz, beanInfo.postConstruct, beanAsInterceptor.getPostConstruct());
        toCallback(clazz, beanInfo.preDestroy, beanAsInterceptor.getPreDestroy());

        if (beanInfo instanceof StatefulBeanInfo) {
            final StatefulBeanInfo stateful = (StatefulBeanInfo) beanInfo;
            toCallback(clazz, stateful.postActivate, beanAsInterceptor.getPostActivate());
            toCallback(clazz, stateful.prePassivate, beanAsInterceptor.getPrePassivate());

            toCallback(clazz, stateful.afterBegin, beanAsInterceptor.getAfterBegin());
            toCallback(clazz, stateful.beforeCompletion, beanAsInterceptor.getBeforeCompletion());
            toCallback(clazz, stateful.afterCompletion, beanAsInterceptor.getAfterCompletion(), boolean.class);
        } else {
            toMethods(clazz, beanInfo.aroundTimeout, beanAsInterceptor.getAroundTimeout());
        }


        while (clazz != null && clazz != Object.class) {
            for (final Method method : clazz.getDeclaredMethods()) {
                final List<InterceptorData> methodInterceptors = createInterceptorDatas(method, beanInfo.ejbName, this.bindings);
                // The bean itself gets to intercept too and is always last.
                beanContext.setMethodInterceptors(method, methodInterceptors);
                beanContext.getMethodContext(method).setSelfInterception(beanAsInterceptor);
            }
            clazz = clazz.getSuperclass();
        }


        final List<InterceptorData> callbackInterceptorDatas = createInterceptorDatas(null, beanInfo.ejbName, this.packageAndClassBindings);

        // The bean itself gets to intercept too and is always last.
        callbackInterceptorDatas.add(beanAsInterceptor);

        beanContext.setCallbackInterceptors(callbackInterceptorDatas);
    }

    private List<InterceptorData> createInterceptorDatas(final Method method, final String ejbName, final List<InterceptorBindingInfo> bindings) {
        final List<InterceptorBindingInfo> methodBindings = processBindings(method, ejbName, bindings);
        Collections.reverse(methodBindings);
        final List<InterceptorData> methodInterceptors = new ArrayList<>();

        for (final InterceptorBindingInfo info : methodBindings) {
            final List<String> classes = info.interceptorOrder.size() > 0 ? info.interceptorOrder : info.interceptors;
            for (final String interceptorClassName : classes) {
                final InterceptorData interceptorData = interceptors.get(interceptorClassName);
                if (interceptorData == null) {
                    logger.warning("InterceptorBinding references non-existent (undeclared) interceptor: " + interceptorClassName);
                    continue;
                }
                methodInterceptors.add(interceptorData);
            }
        }
        return methodInterceptors;
    }


    private List<InterceptorBindingInfo> processBindings(final Method method, final String ejbName, final List<InterceptorBindingInfo> bindings) {
        final List<InterceptorBindingInfo> methodBindings = new ArrayList<>();

        // The only critical thing to understand in this loop is that
        // the bindings have already been sorted high to low (first to last)
        // in this order:
        //
        // Primary sort is "level":
        //   (highest/first)
        //    - Method-level bindings with params
        //    - Method-level with no params
        //    - Method-level bindings with params from annotations
        //    - Class-level
        //    - Class-level from annotation
        //    - Package-level (aka. default level)
        //   (lowest/last)
        //
        // They've been secondarily sorted *within* these levels by "type":
        //
        //   (highest)
        //    - Explicit order
        //    - Exclude applying to current and lower levels
        //    - Exclude applying to just current level
        //    - Any addition for current level and/or exclusion for a lower level
        //   (lowest)
        //
        final Set<Level> excludes = EnumSet.noneOf(Level.class);
        for (final InterceptorBindingInfo info : bindings) {
            final Level level = level(info);

            if (!implies(method, ejbName, level, info)) {
                continue;
            }

            final Type type = type(level, info);

            if (type == Type.EXPLICIT_ORDERING && !excludes.contains(level)) {

                methodBindings.add(info);
                // An explicit ordering trumps all other bindings of the same level or below
                // (even other explicit order bindings).  Any bindings that were higher than
                // this one still apply (and will have already been applied).
                //
                // So we keep what we have, add this last binding and we're done with this method.
                return methodBindings;
            }

            if (type == Type.SAME_AND_LOWER_EXCLUSION) {
                // We're done as the only things that will come after this will be
                // at the same or lower level and we've just been told to exclude them.
                // Nothing more to do for this method.
                return methodBindings;
            }

            if (type == Type.SAME_LEVEL_EXCLUSION) {
                excludes.add(level);
            }

            if (!excludes.contains(level)) {
                methodBindings.add(info);
            }

            if (info.excludeClassInterceptors) {
                excludes.add(Level.CLASS);
                excludes.add(Level.ANNOTATION_CLASS);
            }
            if (info.excludeDefaultInterceptors) {
                excludes.add(Level.PACKAGE);
            }
        }
        return methodBindings;
    }

    private boolean implies(final Method method, final String ejbName, final Level level, final InterceptorBindingInfo info) {
        if (level == Level.PACKAGE) {
            return true;
        }
        if (!ejbName.equals(info.ejbName)) {
            return false;
        }
        if (level == Level.CLASS || level == Level.ANNOTATION_CLASS) {
            return true;
        }

        final NamedMethodInfo methodInfo = info.method;
        return MethodInfoUtil.matches(method, methodInfo);
    }


    /**
     * Used for getting the java.lang.reflect.Method objects for the following callbacks:
     *
     * - @PostConstruct <any-scope> void <method-name>(InvocationContext)
     * - @PreDestroy <any-scope> void <method-name>(InvocationContext)
     * - @PrePassivate <any-scope> void <method-name>(InvocationContext)
     * - @PostActivate <any-scope> void <method-name>(InvocationContext)
     * - @AroundInvoke <any-scope> Object <method-name>(InvocationContext) throws Exception
     * - @AroundTimeout <any-scope> Object <method-name>(InvocationContext) throws Exception
     *
     * @param clazz
     * @param callbackInfos the raw CallbackInfo objects
     * @param callbacks     the collection where the created methods will be placed
     */
    private void toMethods(final Class<?> clazz, final List<CallbackInfo> callbackInfos, final Set<Method> callbacks) {
        final List<Method> methods = new ArrayList<>();

        for (final CallbackInfo callbackInfo : callbackInfos) {
            try {
                Method method = getMethod(clazz, callbackInfo.method, InvocationContext.class);
                if (callbackInfo.className == null && method.getDeclaringClass().equals(clazz) && !methods.contains(method)) {
                    methods.add(method);
                }
                if (method.getDeclaringClass().getName().equals(callbackInfo.className) && !methods.contains(method)) {
                    methods.add(method);
                } else {
                    // check for a private method on the declared class

                    // find declared class
                    Class<?> c = clazz;
                    while (c != null && !c.getName().equals(callbackInfo.className)) {
                        c = c.getSuperclass();
                    }

                    // get callback method
                    if (c != null) {
                        try {
                            method = getMethod(c, callbackInfo.method, InvocationContext.class);
                            // make sure it is private
                            if (Modifier.isPrivate(method.getModifiers()) && !methods.contains(method)) {
                                SetAccessible.on(method);
                                methods.add(method);
                            }
                        } catch (final NoSuchMethodException e) {
                            // no-op
                        }
                    }
                }
            } catch (final NoSuchMethodException e) {
                logger.warning("Interceptor method not found (skipping): public Object " + callbackInfo.method + "(InvocationContext); in class " + clazz.getName());
            }
        }
        methods.sort(new MethodCallbackComparator());

        callbacks.addAll(methods);
    }

    /**
     * Used for getting the java.lang.reflect.Method objects for the following callbacks:
     *
     * - @PostConstruct <any-scope> void <method-name>()
     * - @PreDestroy <any-scope> void <method-name>()
     * - @PrePassivate <any-scope> void <method-name>()
     * - @PostActivate <any-scope> void <method-name>()
     * - @AfterBegin <any-scope> void <method-name>()
     * - @BeforeCompletion <any-scope> void <method-name>()
     * - @AfterCompletion <any-scope> void <method-name>(boolean)
     *
     * These apply to the bean class only, interceptor methods use InvocationContext as
     * a parameter.  The toMethods method is used for those.
     *
     * @param clazz
     * @param callbackInfos
     * @param callbacks
     */
    private void toCallback(final Class<?> clazz, final List<CallbackInfo> callbackInfos, final Set<Method> callbacks, final Class<?>... parameterTypes) {
        final List<Method> methods = new ArrayList<>();

        for (final CallbackInfo callbackInfo : callbackInfos) {
            Class<?> usedClazz = clazz;
            if (clazz.isInterface() && !callbackInfo.className.equals(clazz.getName())) { // dynamic mbean for instance
                try {
                    usedClazz = clazz.getClassLoader().loadClass(callbackInfo.className);
                } catch (final ClassNotFoundException e) {
                    // ignored
                }
            }

            try {
                Method method = getMethod(usedClazz, callbackInfo.method, parameterTypes);
                if (callbackInfo.className == null && !methods.contains(method)) {
                    methods.add(method);
                } else if (method.getDeclaringClass().getName().equals(callbackInfo.className) && !methods.contains(method)) {
                    methods.add(method);
                } else {
                    // check for a private method on the declared class

                    // find declared class
                    Class<?> c = clazz;
                    while (c != null && !c.getName().equals(callbackInfo.className)) {
                        c = c.getSuperclass();
                    }

                    // get callback method
                    if (c != null) {
                        try {
                            method = c.getDeclaredMethod(callbackInfo.method);
                            // make sure it is private
                            if (Modifier.isPrivate(method.getModifiers()) && !methods.contains(method)) {
                                SetAccessible.on(method);
                                methods.add(method);
                            }
                        } catch (final NoSuchMethodException e) {
                            // no-op
                        }
                    }
                }
            } catch (final NoSuchMethodException e) {
                final String message = "Bean Callback method not found (skipping): public void " + callbackInfo.method + "(); in class " + clazz.getName();
                logger.warning(message);
                throw new IllegalStateException(message, e);
            }
        }
        methods.sort(new MethodCallbackComparator());
        callbacks.addAll(methods);
    }

    /**
     * Used by toMethods and toCallbacks to find the nearest java.lang.reflect.Method with the given
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
    private Method getMethod(Class<?> clazz, final String methodName, final Class<?>... parameterTypes) throws NoSuchMethodException {
        NoSuchMethodException original = null;
        while (clazz != null) {
            try {
                final Method method = clazz.getDeclaredMethod(methodName, parameterTypes);
                return SetAccessible.on(method);
            } catch (final NoSuchMethodException e) {
                if (original == null) {
                    original = e;
                }
            }
            clazz = clazz.getSuperclass();
        }
        throw original;
    }

    // -------------------------------------------------------------------
    // Methods for sorting the bindings and callbacks
    // -------------------------------------------------------------------

    public static class IntercpetorBindingComparator implements Comparator<InterceptorBindingInfo> {
        public int compare(final InterceptorBindingInfo a, final InterceptorBindingInfo b) {
            final Level levelA = level(a);
            final Level levelB = level(b);

            if (levelA != levelB) {
                return levelA.ordinal() - levelB.ordinal();
            }

            // Now resort to secondary sorting.

            return type(levelA, a).ordinal() - type(levelB, b).ordinal();
        }
    }

    private static Level level(final InterceptorBindingInfo info) {
        if (info.ejbName.equals("*")) {
            return Level.PACKAGE;
        }

        if (info.method == null) {
            return info.className == null ? Level.CLASS : Level.ANNOTATION_CLASS;
        }

        if (info.method.methodParams == null) {
            return Level.OVERLOADED_METHOD;
        }

        return info.className == null ? Level.EXACT_METHOD : Level.ANNOTATION_METHOD;
    }

    private static Type type(final Level level, final InterceptorBindingInfo info) {
        if (info.interceptorOrder.size() > 0) {
            return Type.EXPLICIT_ORDERING;
        }

        if ((level == Level.CLASS || level == Level.ANNOTATION_CLASS) && info.excludeClassInterceptors && info.excludeDefaultInterceptors) {
            return Type.SAME_AND_LOWER_EXCLUSION;
        }

        if ((level == Level.CLASS || level == Level.ANNOTATION_CLASS) && info.excludeClassInterceptors) {
            return Type.SAME_LEVEL_EXCLUSION;
        }

        return Type.ADDITION_OR_LOWER_EXCLUSION;
    }

    public static class MethodCallbackComparator implements Comparator<Method> {
        public int compare(final Method m1, final Method m2) {
            final Class<?> c1 = m1.getDeclaringClass();
            final Class<?> c2 = m2.getDeclaringClass();
            if (c1.equals(c2)) {
                return 0;
            }
            if (c1.isAssignableFrom(c2)) {
                return -1;
            }
            return 1;
        }
    }

}
