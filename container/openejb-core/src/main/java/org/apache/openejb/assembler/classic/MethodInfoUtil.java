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

import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;

import static java.util.Arrays.asList;

import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Iterator;
import java.lang.reflect.Method;

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
     * @param methodName
     * @param parameterTypes
     * @return
     * @throws IllegalStateException if the method is not found in this class or any of its parent classes
     */
    public static Method toMethod(Class clazz, NamedMethodInfo info) {
        List<Class> parameterTypes = new ArrayList<Class>();

        if (info.methodParams != null){
            for (String paramType : info.methodParams) {
                try {
                    parameterTypes.add(Classes.forName(paramType, clazz.getClassLoader()));
                } catch (ClassNotFoundException cnfe) {
                    throw new IllegalStateException("Parameter class could not be loaded for type " + paramType, cnfe);
                }
            }
        }

        Class[] parameters = parameterTypes.toArray(new Class[parameterTypes.size()]);

        IllegalStateException noSuchMethod = null;
        while (clazz != null) {
            try {
                Method method = clazz.getDeclaredMethod(info.methodName, parameters);
                return SetAccessible.on(method);
            } catch (NoSuchMethodException e) {
                if (noSuchMethod == null) {
                    noSuchMethod = new IllegalStateException("Callback method does not exist: " + clazz.getName() + "." + info.methodName, e);
                }
                clazz = clazz.getSuperclass();
            }
        }

        throw noSuchMethod;
    }

    public static List<Method> matchingMethods(Method signature, Class clazz) {
        List<Method> list = new ArrayList<Method>();
        METHOD: for (Method method : clazz.getMethods()) {
            if (!method.getName().equals(signature.getName())) continue;

            Class<?>[] methodTypes = method.getParameterTypes();
            Class<?>[] signatureTypes = signature.getParameterTypes();

            if (methodTypes.length != signatureTypes.length) continue;

            for (int i = 0; i < methodTypes.length; i++) {
                if (!methodTypes[i].equals(signatureTypes[i])) continue METHOD;
            }
            list.add(method);
        }
        return list;
    }

    public static List<Method> matchingMethods(MethodInfo mi, Class clazz) {
        Method[] methods = clazz.getMethods();

        return matchingMethods(mi, methods);
    }

    public static List<Method> matchingMethods(MethodInfo mi, Method[] methods) {

        List<Method> filtered = filterByLevel(mi, methods);

        filtered = filterByView(mi, filtered);

        return filtered;
    }

    private static List<Method> filterByView(MethodInfo mi, List<Method> filtered) {
        View view = view(mi);
        switch(view){
            case CLASS:{
                return filterByClass(mi, filtered);
            }
        }

        return filtered;
    }

    private static List<Method> filterByClass(MethodInfo mi, List<Method> methods) {
        ArrayList<Method> list = new ArrayList<Method>();
        for (Method method : methods) {
            String className = method.getDeclaringClass().getName();
            if (mi.className.equals(className)){
                list.add(method);
            }
        }
        return list;
    }

    private static List<Method> filterByLevel(MethodInfo mi, Method[] methods) {
        Level level = level(mi);

        switch(level){
            case BEAN:
            case PACKAGE: {
                return asList(methods);
            }
            case OVERLOADED_METHOD:{
                return filterByName(methods, mi.methodName);
            }
            case EXACT_METHOD:{
                return filterByNameAndParams(methods, mi);
            }
        }

        return Collections.EMPTY_LIST;
    }

    public static Method getMethod(Class clazz, MethodInfo info) {
        ClassLoader cl = clazz.getClassLoader();

        List<Class> params = new ArrayList<Class>();
        for (String methodParam : info.methodParams) {
            try {
                params.add(getClassForParam(methodParam, cl));
            } catch (ClassNotFoundException cnfe) {

            }
        }
        Method method = null;
        try {
            method = clazz.getMethod(info.methodName, params.toArray(new Class[params.size()]));
        } catch (NoSuchMethodException e) {
            return null;
        }

        if (!info.className.equals("*") && !method.getDeclaringClass().getName().equals(info.className)){
            return null;
        }

        return method;
    }

    private static List<Method> filterByName(Method[] methods, String methodName) {
        List<Method> list = new ArrayList<Method>();
        for (Method method : methods) {
            if (method.getName().equals(methodName)){
                list.add(method);
            }
        }
        return list;
    }

    private static List<Method> filterByNameAndParams(Method[] methods, MethodInfo mi) {
        List<Method> list = new ArrayList<Method>();
        for (Method method : methods) {
            if (matches(method, mi)){
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
    public static List<MethodPermissionInfo> normalizeMethodPermissionInfos(List<MethodPermissionInfo> infos){
        List<MethodPermissionInfo> normalized = new ArrayList<MethodPermissionInfo>();
        for (MethodPermissionInfo oldInfo : infos) {
            for (MethodInfo methodInfo : oldInfo.methods) {
                MethodPermissionInfo newInfo = new MethodPermissionInfo();
                newInfo.description = oldInfo.description;
                newInfo.methods.add(methodInfo);
                newInfo.roleNames.addAll(oldInfo.roleNames);
                newInfo.unchecked = oldInfo.unchecked;
                newInfo.excluded = oldInfo.excluded;

                normalized.add(newInfo);
            }
        }

        Collections.sort(normalized, new MethodPermissionComparator());

        return normalized;
    }

    private static Class getClassForParam(String className, ClassLoader cl) throws ClassNotFoundException {

        if (className.equals("int")) {
            return Integer.TYPE;
        } else if (className.equals("double")) {
            return Double.TYPE;
        } else if (className.equals("long")) {
            return Long.TYPE;
        } else if (className.equals("boolean")) {
            return Boolean.TYPE;
        } else if (className.equals("float")) {
            return Float.TYPE;
        } else if (className.equals("char")) {
            return Character.TYPE;
        } else if (className.equals("short")) {
            return Short.TYPE;
        } else if (className.equals("byte")) {
            return Byte.TYPE;
        } else
            return Class.forName(className, false, cl);

    }

    public static Map<Method, MethodAttributeInfo> resolveAttributes(List<? extends MethodAttributeInfo> infos, BeanContext beanContext) {
        Map<Method, MethodAttributeInfo> attributes = new LinkedHashMap<Method, MethodAttributeInfo>();

        Method[] wildCardView = getWildCardView(beanContext).toArray(new Method[]{});

        for (MethodAttributeInfo attributeInfo : infos) {
            for (MethodInfo methodInfo : attributeInfo.methods) {

                if (methodInfo.ejbName == null || methodInfo.ejbName.equals("*") || methodInfo.ejbName.equals(beanContext.getEjbName())) {

                    List<Method> methods = new ArrayList<Method>();

                    if (methodInfo.methodIntf == null) {
                        methods.addAll(matchingMethods(methodInfo, wildCardView));
                    } else if (methodInfo.methodIntf.equals("Home")) {
                        methods.addAll(matchingMethods(methodInfo, beanContext.getHomeInterface()));
                    } else if (methodInfo.methodIntf.equals("Remote")) {
                        if (beanContext.getRemoteInterface() != null) {
                            methods.addAll(matchingMethods(methodInfo, beanContext.getRemoteInterface()));
                        }
                        for (Class intf : beanContext.getBusinessRemoteInterfaces()) {
                            methods.addAll(matchingMethods(methodInfo, intf));
                        }
                    } else if (methodInfo.methodIntf.equals("LocalHome")) {
                        methods.addAll(matchingMethods(methodInfo, beanContext.getLocalHomeInterface()));
                    } else if (methodInfo.methodIntf.equals("Local")) {
                        if (beanContext.getLocalInterface() != null) {
                            methods.addAll(matchingMethods(methodInfo, beanContext.getLocalInterface()));
                        }
                        for (Class intf : beanContext.getBusinessRemoteInterfaces()) {
                            methods.addAll(matchingMethods(methodInfo, intf));
                        }
                    } else if (methodInfo.methodIntf.equals("ServiceEndpoint")) {
                        methods.addAll(matchingMethods(methodInfo, beanContext.getServiceEndpointInterface()));
                    }

                    for (Method method : methods) {
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

    public static Map<ViewMethod, MethodAttributeInfo> resolveViewAttributes(List<? extends MethodAttributeInfo> infos, BeanContext beanContext) {
        Map<ViewMethod, MethodAttributeInfo> attributes = new LinkedHashMap<ViewMethod, MethodAttributeInfo>();

        Method[] wildCardView = getWildCardView(beanContext).toArray(new Method[]{});

        for (MethodAttributeInfo attributeInfo : infos) {
            for (MethodInfo methodInfo : attributeInfo.methods) {

                if (methodInfo.ejbName == null || methodInfo.ejbName.equals("*") || methodInfo.ejbName.equals(beanContext.getEjbName())) {

                    List<Method> methods = new ArrayList<Method>();

                    if (methodInfo.methodIntf == null) {
                        methods.addAll(matchingMethods(methodInfo, wildCardView));
                    } else if (methodInfo.methodIntf.equals("Home")) {
                        methods.addAll(matchingMethods(methodInfo, beanContext.getHomeInterface()));
                    } else if (methodInfo.methodIntf.equals("Remote")) {
                        if (beanContext.getRemoteInterface() != null) {
                            methods.addAll(matchingMethods(methodInfo, beanContext.getRemoteInterface()));
                        }
                        for (Class intf : beanContext.getBusinessRemoteInterfaces()) {
                            methods.addAll(matchingMethods(methodInfo, intf));
                        }
                    } else if (methodInfo.methodIntf.equals("LocalHome")) {
                        methods.addAll(matchingMethods(methodInfo, beanContext.getLocalHomeInterface()));
                    } else if (methodInfo.methodIntf.equals("Local")) {
                        if (beanContext.getLocalInterface() != null) {
                            methods.addAll(matchingMethods(methodInfo, beanContext.getLocalInterface()));
                        }
                        for (Class intf : beanContext.getBusinessRemoteInterfaces()) {
                            methods.addAll(matchingMethods(methodInfo, intf));
                        }
                    } else if (methodInfo.methodIntf.equals("ServiceEndpoint")) {
                        methods.addAll(matchingMethods(methodInfo, beanContext.getServiceEndpointInterface()));
                    }

                    for (Method method : methods) {
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

        public ViewMethod(String view, Method method) {
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
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ViewMethod that = (ViewMethod) o;

            if (!method.equals(that.method)) return false;
            if (view != null ? !view.equals(that.view) : that.view != null) return false;

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

    private static boolean containerMethod(Method method) {
        return (method.getDeclaringClass() == EJBObject.class ||
                method.getDeclaringClass() == EJBHome.class ||
                method.getDeclaringClass() == EJBLocalObject.class ||
                method.getDeclaringClass() == EJBLocalHome.class) &&
                !method.getName().equals("remove");
    }

    private static List<Method> getWildCardView(BeanContext info) {
        List<Method> methods = new ArrayList<Method>();

        List<Method> beanMethods = asList(info.getBeanClass().getMethods());
        methods.addAll(beanMethods);

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
        if(info.getMdbInterface() != null) {
            methods.addAll(exclude(beanMethods, info.getMdbInterface().getMethods()));
        }

        if(info.getServiceEndpointInterface() != null) {
            methods.addAll(exclude(beanMethods, info.getServiceEndpointInterface().getMethods()));
        }

        for (Class intf : info.getBusinessRemoteInterfaces()) {
            methods.addAll(exclude(beanMethods, intf.getMethods()));
        }

        for (Class intf : info.getBusinessLocalInterfaces()) {
            methods.addAll(exclude(beanMethods, intf.getMethods()));
        }

        // Remove methods that cannot be controlled by the user
        Iterator<Method> iterator = methods.iterator();
        while (iterator.hasNext()) {
            Method method = iterator.next();
            if (containerMethod(method)) iterator.remove();
        }

        return methods;
    }

    private static List<Method> exclude(List<Method> excludes, Method[] methods) {
        ArrayList<Method> list = new ArrayList<Method>();

        for (Method method : methods) {
            if (!matches(excludes, method)){
                list.add(method);
            }
        }
        return list;
    }

    private static boolean matches(List<Method> excludes, Method method) {
        for (Method excluded : excludes) {
            boolean match = match(method, excluded);
            if (match){
                return true;
            }
        }
        return false;
    }

    public static boolean match(Method methodA, Method methodB) {
        if (!methodA.getName().equals(methodB.getName())) return false;

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

    public static boolean matches(Method method, MethodInfo methodInfo) {
        return matches(method, methodInfo.methodName, methodInfo.methodParams);
    }

    public static boolean matches(Method method, NamedMethodInfo methodInfo) {
        return matches(method, methodInfo.methodName, methodInfo.methodParams);
    }

    public static boolean matches(Method method, String methodName, List<String> methodParams) {

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
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> parameterType = parameterTypes[i];
            String methodParam = methodParams.get(i);
            if (!methodParam.equals(getName(parameterType))) {
                return false;
            }
        }

        return true;
    }

    private static String getName(Class<?> type) {
        if (type.isArray()) {
            return getName(type.getComponentType()) + "[]";
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

    public static View view(MethodInfo methodInfo) {
        if (methodInfo.className != null && !methodInfo.className.equals("*")) return View.CLASS;
        if (methodInfo.methodIntf != null && !methodInfo.methodIntf.equals("*")) return View.INTERFACE;
        else return View.ANY;
    }

    public static Level level(MethodInfo methodInfo) {
        if (methodInfo.ejbName != null && methodInfo.ejbName.equals("*")) return Level.PACKAGE;
        if (methodInfo.methodName.equals("*")) return Level.BEAN;
        if (methodInfo.methodParams == null) return Level.OVERLOADED_METHOD;
        return Level.EXACT_METHOD;
    }


    public static class MethodPermissionComparator extends BaseComparator<MethodPermissionInfo> {
        public int compare(MethodPermissionInfo a, MethodPermissionInfo b) {
            return compare(a.methods.get(0), b.methods.get(0));
        }
    }

    public static abstract class BaseComparator<T> implements Comparator<T>{
        public int compare(MethodInfo am, MethodInfo bm) {
            Level levelA = level(am);
            Level levelB = level(bm);

            // Primary sort
            if (levelA != levelB) return levelA.ordinal() - levelB.ordinal();

            // Secondary sort
            return view(am).ordinal() - view(bm).ordinal();
        }
    }


    public static String toString(MethodInfo i) {
        String s = i.ejbName;
        s += " : ";
        s += (i.methodIntf == null) ? "*" : i.methodIntf;
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

    public static String toString(MethodPermissionInfo i) {
        String s = toString(i.methods.get(0));
        if (i.unchecked){
            s += " Unchecked";
        } else if (i.excluded){
            s += " Excluded";
        } else {
            s += " " + Join.join(", ", i.roleNames);
        }
        return s;
    }

    public static String toString(MethodTransactionInfo i) {
        String s = toString(i.methods.get(0));
        s += " " + i.transAttribute;
        return s;
    }

    public static String toString(MethodConcurrencyInfo i) {
        String s = toString(i.methods.get(0));
        s += " " + i.concurrencyAttribute;
        return s;
    }

}
