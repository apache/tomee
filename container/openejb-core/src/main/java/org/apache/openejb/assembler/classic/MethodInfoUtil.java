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
import java.util.HashMap;
import java.lang.reflect.Method;

/**
 * @version $Rev$ $Date$
 */
public class MethodInfoUtil {

    public static List<Method> matchingMethods(MethodInfo mi, Class clazz) {

        Level level = level(mi);

        switch(level){
            case BEAN:
            case PACKAGE: {
                return asList(clazz.getMethods());
            }
            case CLASS: {
                return filterByClass(clazz.getMethods(), mi.className);
            }
            case OVERLOADED_METHOD_BEAN:{
                return filterByName(clazz.getMethods(), mi.methodName);
            }
            case OVERLOADED_METHOD_CLASS:{
                return filterByNameAndClass(clazz.getMethods(), mi.className, mi.methodName);
            }
            case EXACT_METHOD:{
                Method method = getMethod(clazz, mi);
                if (method != null) return asList(method);
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

    private static List<Method> filterByClass(Method[] methods, String className) {
        List<Method> list = new ArrayList<Method>();
        for (Method method : methods) {
            if (method.getDeclaringClass().getName().equals(className)){
                list.add(method);
            }
        }
        return list;
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

    private static List<Method> filterByNameAndClass(Method[] methods, String className, String methodName) {
        List<Method> list = new ArrayList<Method>();
        for (Method method : methods) {
            if (method.getName().equals(methodName) && method.getDeclaringClass().getName().equals(className) ){
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

    public static Level level(MethodInfo methodInfo) {
        if (methodInfo.ejbName.equals("*")) return Level.PACKAGE;
        if (methodInfo.methodName.equals("*")) {
            if (methodInfo.className.equals("*")) return Level.BEAN;
            else return Level.CLASS;
        }
        if (methodInfo.methodParams == null){
            if (methodInfo.className.equals("*")) return Level.OVERLOADED_METHOD_BEAN;
            else return Level.OVERLOADED_METHOD_CLASS;
        }
        return Level.EXACT_METHOD;
    }


    private static Class getClassForParam(String className, ClassLoader cl) throws ClassNotFoundException {
        if (cl == null) {
            cl = ClassLoader.getSystemClassLoader();
        }

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
            return cl.loadClass(className);

    }

    public static Map<Method, MethodAttributeInfo> resolveAttributes(List<? extends MethodAttributeInfo> infos, CoreDeploymentInfo deploymentInfo) {
        Map<Method, MethodAttributeInfo> attributes = new HashMap<Method, MethodAttributeInfo>();

        for (MethodAttributeInfo attributeInfo : infos) {
            for (MethodInfo methodInfo : attributeInfo.methods) {

                if (methodInfo.ejbDeploymentId == null || methodInfo.ejbDeploymentId.equals(deploymentInfo.getDeploymentID())) {
                    if (!deploymentInfo.isBeanManagedTransaction()) {

                        List<Method> methods = new ArrayList<Method>();

                        if (methodInfo.methodIntf == null) {

                            methods.addAll(matchingMethods(methodInfo, deploymentInfo.getBeanClass()));

                            if (deploymentInfo.getRemoteInterface() != null) {
                                methods.addAll(matchingMethods(methodInfo, deploymentInfo.getRemoteInterface()));
                            }
                            if (deploymentInfo.getHomeInterface() != null) {
                                methods.addAll(matchingMethods(methodInfo, deploymentInfo.getHomeInterface()));
                            }
                            if (deploymentInfo.getLocalInterface() != null) {
                                methods.addAll(matchingMethods(methodInfo, deploymentInfo.getLocalInterface()));
                            }
                            if (deploymentInfo.getLocalHomeInterface() != null) {
                                methods.addAll(matchingMethods(methodInfo, deploymentInfo.getLocalHomeInterface()));
                            }
                            if(deploymentInfo.getMdbInterface() != null) {
                                methods.addAll(matchingMethods(methodInfo, deploymentInfo.getMdbInterface()));
                            }
                            if(deploymentInfo.getServiceEndpointInterface() != null) {
                                methods.addAll(matchingMethods(methodInfo, deploymentInfo.getServiceEndpointInterface()));
                            }
                            for (Class intf : deploymentInfo.getBusinessRemoteInterfaces()) {
                                methods.addAll(matchingMethods(methodInfo, intf));
                            }
                            for (Class intf : deploymentInfo.getBusinessLocalInterfaces()) {
                                methods.addAll(matchingMethods(methodInfo, intf));
                            }
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
        }
        return attributes;
    }

    public static enum Level {
        PACKAGE, CLASS, BEAN, OVERLOADED_METHOD_CLASS, OVERLOADED_METHOD_BEAN, EXACT_METHOD
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

            return levelA.ordinal() - levelB.ordinal();
        }
    }

}
