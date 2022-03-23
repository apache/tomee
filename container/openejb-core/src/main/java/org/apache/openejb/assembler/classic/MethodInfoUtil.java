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
import org.apache.openejb.util.Classes;
import org.apache.openejb.util.Join;
import org.apache.openejb.util.SetAccessible;

import jakarta.ejb.EJBHome;
import jakarta.ejb.EJBLocalHome;
import jakarta.ejb.EJBLocalObject;
import jakarta.ejb.EJBObject;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.Arrays.asList;

/**
 * @version $Rev$ $Date$
 */
public class MethodInfoUtil {


    /**
     * Finds the nearest java.lang.reflect.Method with the given NamedMethodInfo
     * Callbacks can be private so class.getMethod() cannot be used.  Searching
     * starts by looking in the specified class, if the method is not found searching continues with
     * the immediate parent and continues recurssively until the method is found or java.lang.Object
     * is reached.  If the method is not found a IllegalStateException is thrown.
     *
     * @param clazz
     * @param info
     * @return Method
     * @throws IllegalStateException if the method is not found in this class or any of its parent classes
     */
    public static Method toMethod(Class clazz, final NamedMethodInfo info) {
        final List<Class> parameterTypes = new ArrayList<>();

        if (info.methodParams != null) {
            for (final String paramType : info.methodParams) {
                try {
                    parameterTypes.add(Classes.forName(paramType, clazz.getClassLoader()));
                } catch (final ClassNotFoundException cnfe) {
                    throw new IllegalStateException("Parameter class could not be loaded for type " + paramType, cnfe);
                }
            }
        }

        final Class[] parameters = parameterTypes.toArray(new Class[parameterTypes.size()]);

        IllegalStateException noSuchMethod = null;
        while (clazz != null) {
            try {
                final Method method = clazz.getDeclaredMethod(info.methodName, parameters);
                return SetAccessible.on(method);
            } catch (final NoSuchMethodException e) {
                if (noSuchMethod == null) {
                    noSuchMethod = new IllegalStateException("Callback method does not exist: " + clazz.getName() + "." + info.methodName, e);
                }
                clazz = clazz.getSuperclass();
            }
        }

        throw noSuchMethod;
    }

    public static List<Method> matchingMethods(final Method signature, final Class clazz) {
        final List<Method> list = new ArrayList<>();
        METHOD:
        for (final Method method : clazz.getMethods()) {
            if (!method.getName().equals(signature.getName())) {
                continue;
            }

            final Class<?>[] methodTypes = method.getParameterTypes();
            final Class<?>[] signatureTypes = signature.getParameterTypes();

            if (methodTypes.length != signatureTypes.length) {
                continue;
            }

            for (int i = 0; i < methodTypes.length; i++) {
                if (!methodTypes[i].equals(signatureTypes[i])) {
                    continue METHOD;
                }
            }
            list.add(method);
        }
        return list;
    }

    public static List<Method> matchingMethods(final MethodInfo mi, final Class clazz) {
        final Method[] methods = clazz.getMethods();

        return matchingMethods(mi, methods);
    }

    public static List<Method> matchingMethods(final MethodInfo mi, final Method[] methods) {

        List<Method> filtered = filterByLevel(mi, methods);

        filtered = filterByView(mi, filtered);

        return filtered;
    }

    private static List<Method> filterByView(final MethodInfo mi, final List<Method> filtered) {
        final View view = view(mi);
        switch (view) {
            case CLASS: {
                return filterByClass(mi, filtered);
            }
        }

        return filtered;
    }

    private static List<Method> filterByClass(final MethodInfo mi, final List<Method> methods) {
        final ArrayList<Method> list = new ArrayList<>();
        for (final Method method : methods) {
            final String className = method.getDeclaringClass().getName();
            if (mi.className.equals(className)) {
                list.add(method);
            }
        }
        return list;
    }

    private static List<Method> filterByLevel(final MethodInfo mi, final Method[] methods) {
        final Level level = level(mi);

        switch (level) {
            case BEAN:
            case PACKAGE: {
                return asList(methods);
            }
            case OVERLOADED_METHOD: {
                return filterByName(methods, mi.methodName);
            }
            case EXACT_METHOD: {
                return filterByNameAndParams(methods, mi);
            }
        }

        return Collections.EMPTY_LIST;
    }

    public static Method getMethod(final Class clazz, final MethodInfo info) {
        final ClassLoader cl = clazz.getClassLoader();

        final List<Class> params = new ArrayList<>();
        for (final String methodParam : info.methodParams) {
            try {
                params.add(getClassForParam(methodParam, cl));
            } catch (final ClassNotFoundException cnfe) {
                // no-op
            }
        }
        Method method = null;
        try {
            method = clazz.getMethod(info.methodName, params.toArray(new Class[params.size()]));
        } catch (final NoSuchMethodException e) {
            return null;
        }

        if (!info.className.equals("*") && !method.getDeclaringClass().getName().equals(info.className)) {
            return null;
        }

        return method;
    }

    private static List<Method> filterByName(final Method[] methods, final String methodName) {
        final List<Method> list = new ArrayList<>();
        for (final Method method : methods) {
            if (method.getName().equals(methodName)) {
                list.add(method);
            }
        }
        return list;
    }

    private static List<Method> filterByNameAndParams(final Method[] methods, final MethodInfo mi) {
        final List<Method> list = new ArrayList<>();
        for (final Method method : methods) {
            if (matches(method, mi)) {
                list.add(method);
            }
        }
        return list;
    }

    /**
     * This method splits the MethodPermissionInfo objects so that there is
     * exactly one MethodInfo per MethodPermissionInfo.  A single MethodPermissionInfo
     * with three MethodInfos would be expanded into three MethodPermissionInfo with
     * one MethodInfo each.
     *
     * The MethodPermissionInfo list is then sorted from least to most specific.
     *
     * @param infos
     * @return a normalized list of new MethodPermissionInfo objects
     */
    public static List<MethodPermissionInfo> normalizeMethodPermissionInfos(final List<MethodPermissionInfo> infos) {
        final List<MethodPermissionInfo> normalized = new ArrayList<>();
        for (final MethodPermissionInfo oldInfo : infos) {
            for (final MethodInfo methodInfo : oldInfo.methods) {
                final MethodPermissionInfo newInfo = new MethodPermissionInfo();
                newInfo.description = oldInfo.description;
                newInfo.methods.add(methodInfo);
                newInfo.roleNames.addAll(oldInfo.roleNames);
                newInfo.unchecked = oldInfo.unchecked;
                newInfo.excluded = oldInfo.excluded;

                normalized.add(newInfo);
            }
        }

        normalized.sort(new MethodPermissionComparator());

        return normalized;
    }

    private static Class getClassForParam(final String className, final ClassLoader cl) throws ClassNotFoundException {

        switch (className) {
            case "int":
                return Integer.TYPE;
            case "double":
                return Double.TYPE;
            case "long":
                return Long.TYPE;
            case "boolean":
                return Boolean.TYPE;
            case "float":
                return Float.TYPE;
            case "char":
                return Character.TYPE;
            case "short":
                return Short.TYPE;
            case "byte":
                return Byte.TYPE;
            default:
                return Class.forName(className, false, cl);
        }

    }

    public static Map<Method, MethodAttributeInfo> resolveAttributes(final List<? extends MethodAttributeInfo> infos, final BeanContext beanContext) {
        final Map<Method, MethodAttributeInfo> attributes = new LinkedHashMap<>();

        final Method[] wildCardView = getWildCardView(beanContext).toArray(new Method[]{});

        for (final MethodAttributeInfo attributeInfo : infos) {
            for (final MethodInfo methodInfo : attributeInfo.methods) {

                if (methodInfo.ejbName == null || methodInfo.ejbName.equals("*") || methodInfo.ejbName.equals(beanContext.getEjbName())) {

                    final List<Method> methods = new ArrayList<>();

                    if (methodInfo.methodIntf == null) {
                        methods.addAll(matchingMethods(methodInfo, wildCardView));
                    } else if (methodInfo.methodIntf.equals("Home")) {
                        methods.addAll(matchingMethods(methodInfo, beanContext.getHomeInterface()));
                    } else if (methodInfo.methodIntf.equals("Remote")) {
                        if (beanContext.getRemoteInterface() != null) {
                            methods.addAll(matchingMethods(methodInfo, beanContext.getRemoteInterface()));
                        }
                        for (final Class intf : beanContext.getBusinessRemoteInterfaces()) {
                            methods.addAll(matchingMethods(methodInfo, intf));
                        }
                    } else if (methodInfo.methodIntf.equals("LocalHome")) {
                        methods.addAll(matchingMethods(methodInfo, beanContext.getLocalHomeInterface()));
                    } else if (methodInfo.methodIntf.equals("Local")) {
                        if (beanContext.getLocalInterface() != null) {
                            methods.addAll(matchingMethods(methodInfo, beanContext.getLocalInterface()));
                        }
                        for (final Class intf : beanContext.getBusinessRemoteInterfaces()) {
                            methods.addAll(matchingMethods(methodInfo, intf));
                        }
                    } else if (methodInfo.methodIntf.equals("ServiceEndpoint")) {
                        methods.addAll(matchingMethods(methodInfo, beanContext.getServiceEndpointInterface()));
                    }

                    for (final Method method : methods) {
                        if (containerMethod(method)) {
                            continue;
                        }

                        attributes.put(method, attributeInfo);
                    }
                }
            }
        }
        return attributes;
    }

    public static Map<ViewMethod, MethodAttributeInfo> resolveViewAttributes(final List<? extends MethodAttributeInfo> infos, final BeanContext beanContext) {
        final Map<ViewMethod, MethodAttributeInfo> attributes = new LinkedHashMap<>();

        final Method[] wildCardView = getWildCardView(beanContext).toArray(new Method[]{});

        for (final MethodAttributeInfo attributeInfo : infos) {
            for (final MethodInfo methodInfo : attributeInfo.methods) {

                if (methodInfo.ejbName == null || methodInfo.ejbName.equals("*") || methodInfo.ejbName.equals(beanContext.getEjbName())) {

                    final List<Method> methods = new ArrayList<>();

                    if (methodInfo.methodIntf == null) {
                        methods.addAll(matchingMethods(methodInfo, wildCardView));
                    } else if (methodInfo.methodIntf.equals("Home")) {
                        methods.addAll(matchingMethods(methodInfo, beanContext.getHomeInterface()));
                    } else if (methodInfo.methodIntf.equals("Remote")) {
                        if (beanContext.getRemoteInterface() != null) {
                            methods.addAll(matchingMethods(methodInfo, beanContext.getRemoteInterface()));
                        }
                        for (final Class intf : beanContext.getBusinessRemoteInterfaces()) {
                            methods.addAll(matchingMethods(methodInfo, intf));
                        }
                    } else if (methodInfo.methodIntf.equals("LocalHome")) {
                        methods.addAll(matchingMethods(methodInfo, beanContext.getLocalHomeInterface()));
                    } else if (methodInfo.methodIntf.equals("Local")) {
                        if (beanContext.getLocalInterface() != null) {
                            methods.addAll(matchingMethods(methodInfo, beanContext.getLocalInterface()));
                        }
                        for (final Class intf : beanContext.getBusinessRemoteInterfaces()) {
                            methods.addAll(matchingMethods(methodInfo, intf));
                        }
                    } else if (methodInfo.methodIntf.equals("ServiceEndpoint")) {
                        methods.addAll(matchingMethods(methodInfo, beanContext.getServiceEndpointInterface()));
                    }

                    for (final Method method : methods) {
                        if (containerMethod(method)) {
                            continue;
                        }

                        final ViewMethod viewMethod = new ViewMethod(methodInfo.methodIntf, method);
                        attributes.put(viewMethod, attributeInfo);
//                        List<MethodAttributeInfo> methodAttributeInfos = attributes.get(method);
//                        if (methodAttributeInfos == null) {
//                            methodAttributeInfos = new ArrayList<MethodAttributeInfo>();
//                            attributes.put(method, methodAttributeInfos);
//                        }
//                        methodAttributeInfos.add(attributeInfo);
                    }
                }
            }
        }
        return attributes;
    }

    public static class ViewMethod {
        private final String view;
        private final Method method;

        public ViewMethod(final String view, final Method method) {
            this.view = view;
            this.method = method;
        }

        public String getView() {
            return view;
        }

        public Method getMethod() {
            return method;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final ViewMethod that = (ViewMethod) o;

            if (!method.equals(that.method)) {
                return false;
            }
            if (!Objects.equals(view, that.view)) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int result = view != null ? view.hashCode() : 0;
            result = 31 * result + method.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return String.format("%s : %s(%s)", view, method.getName(), Join.join(", ", Classes.getSimpleNames(method.getParameterTypes())));
        }
    }

    private static boolean containerMethod(final Method method) {
        return (method.getDeclaringClass() == EJBObject.class ||
            method.getDeclaringClass() == EJBHome.class ||
            method.getDeclaringClass() == EJBLocalObject.class ||
            method.getDeclaringClass() == EJBLocalHome.class) &&
            !method.getName().equals("remove");
    }

    private static List<Method> getWildCardView(final BeanContext info) {

        final List<Method> beanMethods = asList(info.getBeanClass().getMethods());
        final List<Method> methods = new ArrayList<>(beanMethods);

        if (info.getRemoteInterface() != null) {
            methods.addAll(exclude(beanMethods, info.getRemoteInterface().getMethods()));
        }

        if (info.getHomeInterface() != null) {
            methods.addAll(exclude(beanMethods, info.getHomeInterface().getMethods()));
        }

        if (info.getLocalInterface() != null) {
            methods.addAll(exclude(beanMethods, info.getLocalInterface().getMethods()));
        }

        if (info.getLocalHomeInterface() != null) {
            methods.addAll(exclude(beanMethods, info.getLocalHomeInterface().getMethods()));
        }
        if (info.getMdbInterface() != null) {
            methods.addAll(exclude(beanMethods, info.getMdbInterface().getMethods()));
        }

        if (info.getServiceEndpointInterface() != null) {
            methods.addAll(exclude(beanMethods, info.getServiceEndpointInterface().getMethods()));
        }

        for (final Class intf : info.getBusinessRemoteInterfaces()) {
            methods.addAll(exclude(beanMethods, intf.getMethods()));
        }

        for (final Class intf : info.getBusinessLocalInterfaces()) {
            methods.addAll(exclude(beanMethods, intf.getMethods()));
        }

        // Remove methods that cannot be controlled by the user
        methods.removeIf(MethodInfoUtil::containerMethod);

        return methods;
    }

    private static List<Method> exclude(final List<Method> excludes, final Method[] methods) {
        final ArrayList<Method> list = new ArrayList<>();

        for (final Method method : methods) {
            if (!matches(excludes, method)) {
                list.add(method);
            }
        }
        return list;
    }

    private static boolean matches(final List<Method> excludes, final Method method) {
        for (final Method excluded : excludes) {
            final boolean match = match(method, excluded);
            if (match) {
                return true;
            }
        }
        return false;
    }

    public static boolean match(final Method methodA, final Method methodB) {
        if (!methodA.getName().equals(methodB.getName())) {
            return false;
        }

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

    public static boolean matches(final Method method, final MethodInfo methodInfo) {
        return matches(method, methodInfo.methodName, methodInfo.methodParams);
    }

    public static boolean matches(final Method method, final NamedMethodInfo methodInfo) {
        return matches(method, methodInfo.methodName, methodInfo.methodParams);
    }

    public static boolean matches(final Method method, final String methodName, final List<String> methodParams) {

        if (!methodName.equals(method.getName())) {
            return false;
        }

        // do we have parameters?
        if (methodParams == null) {
            return true;
        }

        // do we have the same number of parameters?
        if (methodParams.size() != method.getParameterTypes().length) {
            return false;
        }

        // match parameters names
        final Class<?>[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            final Class<?> parameterType = parameterTypes[i];
            final String methodParam = methodParams.get(i);
            if (!methodParam.equals(getName(parameterType).replace('$', '.')) && !methodParam.equals(parameterType.getName())) {
                return false;
            }
        }

        return true;
    }

    private static String getName(final Class<?> type) {
        if (type.isArray()) {
            return getName(type.getComponentType()) + "[]"; // depend on JVM? type.getName() seems to work on Oracle one
        } else {
            return type.getName();
        }
    }


    public static enum Level {
        PACKAGE, BEAN, OVERLOADED_METHOD, EXACT_METHOD
    }

    public static enum View {
        CLASS, ANY, INTERFACE;
    }

    public static View view(final MethodInfo methodInfo) {
        if (methodInfo.className != null && !methodInfo.className.equals("*")) {
            return View.CLASS;
        }
        if (methodInfo.methodIntf != null && !methodInfo.methodIntf.equals("*")) {
            return View.INTERFACE;
        } else {
            return View.ANY;
        }
    }

    public static Level level(final MethodInfo methodInfo) {
        if (methodInfo.ejbName != null && methodInfo.ejbName.equals("*")) {
            return Level.PACKAGE;
        }
        if (methodInfo.methodName.equals("*")) {
            return Level.BEAN;
        }
        if (methodInfo.methodParams == null) {
            return Level.OVERLOADED_METHOD;
        }
        return Level.EXACT_METHOD;
    }


    public static class MethodPermissionComparator extends BaseComparator<MethodPermissionInfo> {
        public int compare(final MethodPermissionInfo a, final MethodPermissionInfo b) {
            return compare(a.methods.get(0), b.methods.get(0));
        }
    }

    public abstract static class BaseComparator<T> implements Comparator<T> {
        public int compare(final MethodInfo am, final MethodInfo bm) {
            final Level levelA = level(am);
            final Level levelB = level(bm);

            // Primary sort
            if (levelA != levelB) {
                return levelA.ordinal() - levelB.ordinal();
            }

            // Secondary sort
            return view(am).ordinal() - view(bm).ordinal();
        }
    }


    public static String toString(final MethodInfo i) {
        String s = i.ejbName;
        s += " : ";
        s += i.methodIntf == null ? "*" : i.methodIntf;
        s += " : ";
        s += i.className;
        s += " : ";
        s += i.methodName;
        s += "(";
        if (i.methodParams != null) {
            s += Join.join(", ", i.methodParams);
        } else {
            s += "*";
        }
        s += ")";
        return s;
    }

    public static String toString(final MethodPermissionInfo i) {
        String s = toString(i.methods.get(0));
        if (i.unchecked) {
            s += " Unchecked";
        } else if (i.excluded) {
            s += " Excluded";
        } else {
            s += " " + Join.join(", ", i.roleNames);
        }
        return s;
    }

    public static String toString(final MethodTransactionInfo i) {
        String s = toString(i.methods.get(0));
        s += " " + i.transAttribute;
        return s;
    }

    public static String toString(final MethodConcurrencyInfo i) {
        String s = toString(i.methods.get(0));
        s += " " + i.concurrencyAttribute;
        return s;
    }

}
