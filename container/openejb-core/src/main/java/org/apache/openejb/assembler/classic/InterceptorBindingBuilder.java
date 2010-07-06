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

import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.core.interceptor.InterceptorData;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.SetAccessible;
import org.apache.openejb.util.Classes;
import org.apache.openejb.OpenEJBException;

import javax.interceptor.InvocationContext;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * @version $Rev$ $Date$
 */
public class InterceptorBindingBuilder {

    public static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP, InterceptorBindingBuilder.class.getPackage().getName());
    private final List<InterceptorBindingInfo> packageAndClassBindings;

    public static enum Level {
        PACKAGE, CLASS, OVERLOADED_METHOD, EXACT_METHOD
    }

    public static enum Type {
        ADDITION_OR_LOWER_EXCLUSION, SAME_LEVEL_EXCLUSION, SAME_AND_LOWER_EXCLUSION, EXPLICIT_ORDERING
    }

    private final EjbJarInfo ejbJarInfo;
    private final ArrayList<InterceptorBindingInfo> bindings;
    private final Map<String, InterceptorData> interceptors =  new HashMap<String, InterceptorData>();

    public InterceptorBindingBuilder(ClassLoader cl, EjbJarInfo ejbJarInfo) throws OpenEJBException {
        this.ejbJarInfo = ejbJarInfo;
        bindings = new ArrayList<InterceptorBindingInfo>(ejbJarInfo.interceptorBindings);
        Collections.sort(bindings, new IntercpetorBindingComparator());
        Collections.reverse(bindings);

        packageAndClassBindings = new ArrayList<InterceptorBindingInfo>();
        for (InterceptorBindingInfo binding : bindings) {
            Level level = level(binding);
            if (level == Level.PACKAGE || level == Level.CLASS){
                packageAndClassBindings.add(binding);
            }
        }

        for (InterceptorInfo info : ejbJarInfo.interceptors) {
            Class clazz = null;
            try {
                clazz = Class.forName(info.clazz, true, cl);
            } catch (ClassNotFoundException e) {
                throw new OpenEJBException("Interceptor class cannot be loaded: "+info.clazz);
            }
            InterceptorData interceptor = new InterceptorData(clazz);

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

    public void build(CoreDeploymentInfo deploymentInfo, EnterpriseBeanInfo beanInfo) {
        Class clazz = deploymentInfo.getBeanClass();

        InterceptorData beanAsInterceptor = new InterceptorData(clazz);

        toMethods(clazz, beanInfo.aroundInvoke, beanAsInterceptor.getAroundInvoke());
        toCallback(clazz, beanInfo.postConstruct, beanAsInterceptor.getPostConstruct());
        toCallback(clazz, beanInfo.preDestroy, beanAsInterceptor.getPreDestroy());

        if (beanInfo instanceof StatefulBeanInfo) {
            StatefulBeanInfo stateful = (StatefulBeanInfo) beanInfo;
            toCallback(clazz, stateful.postActivate, beanAsInterceptor.getPostActivate());
            toCallback(clazz, stateful.prePassivate, beanAsInterceptor.getPrePassivate());

            toCallback(clazz, stateful.afterBegin, beanAsInterceptor.getAfterBegin());
            toCallback(clazz, stateful.beforeCompletion, beanAsInterceptor.getBeforeCompletion());
            toCallback(clazz, stateful.afterCompletion, beanAsInterceptor.getAfterCompletion(), boolean.class);
        } else {
            toMethods(clazz, beanInfo.aroundTimeout, beanAsInterceptor.getAroundTimeout());
        }

        for (Method method : deploymentInfo.getBeanClass().getMethods()) {
            List<InterceptorData> methodInterceptors = createInterceptorDatas(method, beanInfo.ejbName, this.bindings);

            // The bean itself gets to intercept too and is always last.
            methodInterceptors.add(beanAsInterceptor);

            deploymentInfo.setMethodInterceptors(method, methodInterceptors);
        }

        List<InterceptorData> callbackInterceptorDatas = createInterceptorDatas(null, beanInfo.ejbName, this.packageAndClassBindings);

        // The bean itself gets to intercept too and is always last.
        callbackInterceptorDatas.add(beanAsInterceptor);

        deploymentInfo.setCallbackInterceptors(callbackInterceptorDatas);
    }

    private List<InterceptorData> createInterceptorDatas(Method method, String ejbName, List<InterceptorBindingInfo> bindings) {
        List<InterceptorBindingInfo> methodBindings = processBindings(method, ejbName, bindings);
        Collections.reverse(methodBindings);
        List<InterceptorData> methodInterceptors = new ArrayList<InterceptorData>();

        for (InterceptorBindingInfo info : methodBindings) {
            List<String> classes = (info.interceptorOrder.size() > 0) ? info.interceptorOrder : info.interceptors;
            for (String interceptorClassName : classes) {
                InterceptorData interceptorData = interceptors.get(interceptorClassName);
                if (interceptorData == null){
                    logger.warning("InterceptorBinding references non-existent (undeclared) interceptor: " + interceptorClassName);
                    continue;
                }
                methodInterceptors.add(interceptorData);
            }
        }
        return methodInterceptors;
    }


    private List<InterceptorBindingInfo> processBindings(Method method, String ejbName, List<InterceptorBindingInfo> bindings){
        List<InterceptorBindingInfo> methodBindings = new ArrayList<InterceptorBindingInfo>();

        // The only critical thing to understand in this loop is that
        // the bindings have already been sorted high to low (first to last)
        // in this order:
        //
        // Primary sort is "level":
        //   (highest/first)
        //    - Method-level bindings with params
        //    - Method-level with no params
        //    - Class-level
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
        Set<Level> excludes = new HashSet<Level>();
        for (InterceptorBindingInfo info : bindings) {
            Level level = level(info);

            if (!implies(method, ejbName, level, info)) continue;

            Type type = type(level, info);

            if (type == Type.EXPLICIT_ORDERING && !excludes.contains(level)){
                methodBindings.add(info);
                // An explicit ordering trumps all other bindings of the same level or below
                // (even other explicit order bindings).  Any bindings that were higher than
                // this one still apply (and will have already been applied).
                //
                // So we keep what we have, add this last binding and we're done with this method.
                return methodBindings;
            }

            if (type == Type.SAME_AND_LOWER_EXCLUSION){
                // We're done as the only things that will come after this will be
                // at the same or lower level and we've just been told to exclude them.
                // Nothing more to do for this method.
                return methodBindings;
            }

            if (type == Type.SAME_LEVEL_EXCLUSION){
                excludes.add(level);
            }

            if (!excludes.contains(level)) methodBindings.add(info);

            if (info.excludeClassInterceptors) excludes.add(Level.CLASS);
            if (info.excludeDefaultInterceptors) excludes.add(Level.PACKAGE);
        }
        return methodBindings;
    }

    private boolean implies(Method method, String ejbName, Level level, InterceptorBindingInfo info) {
        if (level == Level.PACKAGE) return true;
        if (!ejbName.equals(info.ejbName)) return false;
        if (level == Level.CLASS) return true;

        NamedMethodInfo methodInfo = info.method;

        if (!methodInfo.methodName.equals(method.getName())) return false;

        // do we have parameters?
        List<String> params = methodInfo.methodParams;
        if (params == null) return true;

        // do we have the same number of parameters?
        if (params.size() != method.getParameterTypes().length) return false;

        // match parameters names
        Class<?>[] parameterTypes = method.getParameterTypes();

        for (int i = 0; i < parameterTypes.length; i++) {

            Class<?> parameterType = parameterTypes[i];
            String methodParam = params.get(i);

            try {
                Class param = Classes.forName(methodParam, parameterType.getClassLoader());
                if (!param.equals(parameterType)) {
                    return false;
                }
            } catch (ClassNotFoundException e) {
                return false;
            }
        }

        return true;
    }


    /**
     * Used for getting the java.lang.reflect.Method objects for the following callbacks:
     *
     *  - @PostConstruct <any-scope> void <method-name>(InvocationContext)
     *  - @PreDestroy <any-scope> void <method-name>(InvocationContext)
     *  - @PrePassivate <any-scope> void <method-name>(InvocationContext)
     *  - @PostActivate <any-scope> void <method-name>(InvocationContext)
     *  - @AroundInvoke <any-scope> Object <method-name>(InvocationContext) throws Exception
     *  - @AroundTimeout <any-scope> Object <method-name>(InvocationContext) throws Exception
     *
     * @param clazz
     * @param callbackInfos the raw CallbackInfo objects
     * @param methods the collection where the created methods will be placed
     */
    private void toMethods(Class clazz, List<CallbackInfo> callbackInfos, List<Method> methods) {
        for (CallbackInfo callbackInfo : callbackInfos) {
            try {
                Method method = getMethod(clazz, callbackInfo.method, InvocationContext.class);
                if (callbackInfo.className == null && method.getDeclaringClass().equals(clazz)){
                    methods.add(method);
                }
                if (method.getDeclaringClass().getName().equals(callbackInfo.className)){
                    methods.add(method);
                }  else {
                    // check for a private method on the declared class

                    // find declared class
                    Class c = clazz;
                    while (c != null && !c.getName().equals(callbackInfo.className)) c = c.getSuperclass();

                    // get callback method
                    if (c != null) {
                        try {
                            method = getMethod(c, callbackInfo.method, InvocationContext.class);
                            // make sure it is private
                            if (Modifier.isPrivate(method.getModifiers())) {
                                SetAccessible.on(method);
                                methods.add(method);
                            }
                        } catch (NoSuchMethodException e) {
                        }
                    }
                }
            } catch (NoSuchMethodException e) {
                logger.warning("Interceptor method not found (skipping): public Object " + callbackInfo.method + "(InvocationContext); in class " + clazz.getName());
            }
        }
        Collections.sort(methods, new MethodCallbackComparator());
    }

    /**
     * Used for getting the java.lang.reflect.Method objects for the following callbacks:
     *
     *  - @PostConstruct <any-scope> void <method-name>()
     *  - @PreDestroy <any-scope> void <method-name>()
     *  - @PrePassivate <any-scope> void <method-name>()
     *  - @PostActivate <any-scope> void <method-name>()
     *  - @AfterBegin <any-scope> void <method-name>()
     *  - @BeforeCompletion <any-scope> void <method-name>()
     *  - @AfterCompletion <any-scope> void <method-name>(boolean)
     *
     * These apply to the bean class only, interceptor methods use InvocationContext as
     * a parameter.  The toMethods method is used for those.
     *
     * @param clazz
     * @param callbackInfos
     * @param methods
     */
    private void toCallback(Class clazz, List<CallbackInfo> callbackInfos, List<Method> methods, Class... parameterTypes) {
        for (CallbackInfo callbackInfo : callbackInfos) {
            try {
                Method method = getMethod(clazz, callbackInfo.method, parameterTypes);
                if (callbackInfo.className == null){
                    methods.add(method);
                } else if (method.getDeclaringClass().getName().equals(callbackInfo.className)){
                    methods.add(method);
                } else {
                    // check for a private method on the declared class

                    // find declared class
                    Class c = clazz;
                    while (c != null && !c.getName().equals(callbackInfo.className)) c = c.getSuperclass();

                    // get callback method
                    if (c != null) {
                        try {
                            method = c.getDeclaredMethod(callbackInfo.method);
                            // make sure it is private
                            if (Modifier.isPrivate(method.getModifiers())) {
                                SetAccessible.on(method);
                                methods.add(method);
                            }
                        } catch (NoSuchMethodException e) {
                        }
                    }
                }
            } catch (NoSuchMethodException e) {
                String message = "Bean Callback method not found (skipping): public void " + callbackInfo.method + "(); in class " + clazz.getName();
                logger.warning(message);
                throw new IllegalStateException(message, e);
            }
        }
        Collections.sort(methods, new MethodCallbackComparator());
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
    private Method getMethod(Class clazz, String methodName, Class... parameterTypes) throws NoSuchMethodException {
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

    // -------------------------------------------------------------------
    // Methods for sorting the bindings and callbacks
    // -------------------------------------------------------------------

    public static class IntercpetorBindingComparator implements Comparator<InterceptorBindingInfo> {
        public int compare(InterceptorBindingInfo a, InterceptorBindingInfo b) {
            Level levelA = level(a);
            Level levelB = level(b);

            if (levelA != levelB) return levelA.ordinal() - levelB.ordinal();

            // Now resort to secondary sorting.

            return type(levelA, a).ordinal() - type(levelB, b).ordinal();
        }
    }

    private static Level level(InterceptorBindingInfo info) {
        if (info.ejbName.equals("*")) {
            return Level.PACKAGE;
        }

        if (info.method == null) {
            return Level.CLASS;
        }

        if (info.method.methodParams == null) {
            return Level.OVERLOADED_METHOD;
        }

        return Level.EXACT_METHOD;
    }

    private static Type type(Level level, InterceptorBindingInfo info) {
        if (info.interceptorOrder.size() > 0) {
            return Type.EXPLICIT_ORDERING;
        }

        if (level == Level.CLASS && info.excludeClassInterceptors && info.excludeDefaultInterceptors) {
            return Type.SAME_AND_LOWER_EXCLUSION;
        }

        if (level == Level.CLASS && info.excludeClassInterceptors) {
            return Type.SAME_LEVEL_EXCLUSION;
        }

        return Type.ADDITION_OR_LOWER_EXCLUSION;
    }

    public static class MethodCallbackComparator implements Comparator<Method> {
        public int compare(Method m1, Method m2) {
            Class<?> c1 = m1.getDeclaringClass();
            Class<?> c2 = m2.getDeclaringClass();
            if (c1.equals(c2)) return 0;
            if (c1.isAssignableFrom(c2)) return -1;
            return 1;
        }
    }

}
