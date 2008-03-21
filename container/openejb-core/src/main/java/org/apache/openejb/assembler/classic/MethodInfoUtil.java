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

import static java.util.Arrays.asList;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.LinkedHashMap;
import java.lang.reflect.Method;

/**
 * @version $Rev$ $Date$
 */
public class MethodInfoUtil {

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

    public static Map<Method, MethodAttributeInfo> resolveAttributes(List<? extends MethodAttributeInfo> infos, CoreDeploymentInfo deploymentInfo) {
        Map<Method, MethodAttributeInfo> attributes = new LinkedHashMap<Method, MethodAttributeInfo>();

        Method[] wildCardView = getWildCardView(deploymentInfo).toArray(new Method[]{});

        for (MethodAttributeInfo attributeInfo : infos) {
            for (MethodInfo methodInfo : attributeInfo.methods) {

                if (methodInfo.ejbName == null || methodInfo.ejbName.equals("*") || methodInfo.ejbName.equals(deploymentInfo.getEjbName())) {

                    List<Method> methods = new ArrayList<Method>();

                    if (methodInfo.methodIntf == null) {
                        methods.addAll(matchingMethods(methodInfo, wildCardView));
                    } else if (methodInfo.methodIntf.equals("Home")) {
                        methods.addAll(matchingMethods(methodInfo, deploymentInfo.getHomeInterface()));
                    } else if (methodInfo.methodIntf.equals("Remote")) {
                        methods.addAll(matchingMethods(methodInfo, deploymentInfo.getRemoteInterface()));
                        for (Class intf : deploymentInfo.getBusinessRemoteInterfaces()) {
                            methods.addAll(matchingMethods(methodInfo, intf));
                        }
                    } else if (methodInfo.methodIntf.equals("LocalHome")) {
                        methods.addAll(matchingMethods(methodInfo, deploymentInfo.getLocalHomeInterface()));
                    } else if (methodInfo.methodIntf.equals("Local")) {
                        methods.addAll(matchingMethods(methodInfo, deploymentInfo.getLocalInterface()));
                        for (Class intf : deploymentInfo.getBusinessRemoteInterfaces()) {
                            methods.addAll(matchingMethods(methodInfo, intf));
                        }
                    } else if (methodInfo.methodIntf.equals("ServiceEndpoint")) {
                        methods.addAll(matchingMethods(methodInfo, deploymentInfo.getServiceEndpointInterface()));
                    }

                    for (Method method : methods) {
                        if ((method.getDeclaringClass() == javax.ejb.EJBObject.class ||
                                method.getDeclaringClass() == javax.ejb.EJBHome.class) &&
                                !method.getName().equals("remove")) {
                            continue;
                        }

                        attributes.put(method, attributeInfo);
                    }
                }
            }
        }
        return attributes;
    }

    private static List<Method> getWildCardView(CoreDeploymentInfo info) {
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

        if (!methodInfo.methodName.equals(method.getName())) {
            return false;
        }

        // do we have parameters?
        List<String> methodParams = methodInfo.methodParams;
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
        if (!methodInfo.className.equals("*")) return View.CLASS;
        if (methodInfo.methodIntf != null && !methodInfo.methodIntf.equals("*")) return View.INTERFACE;
        else return View.ANY;
    }

    public static Level level(MethodInfo methodInfo) {
        if (methodInfo.ejbName.equals("*")) return Level.PACKAGE;
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

}
