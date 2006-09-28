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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.dispatch;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.apache.openejb.util.ClassLoading;

/**
 *
 *
 * @version $Revision$ $Date$
 */
public final class InterfaceMethodSignature implements Comparable, Serializable {
    private static final long serialVersionUID = -3284902678375161698L;
    private static final String[] NOARGS = {};
    private final String methodName;
    private final String[] parameterTypes;
    private final boolean isHomeMethod;
    private final int hashCode;

    public InterfaceMethodSignature(Method method, boolean isHomeMethod) {
        this(method.getName(), convertParameterTypes(method.getParameterTypes()), isHomeMethod);
    }

    public InterfaceMethodSignature(String methodName, boolean isHomeMethod) {
        this(methodName, NOARGS, isHomeMethod);
    }

    public InterfaceMethodSignature(String methodName, Class[] params, boolean isHomeMethod) {
        this(methodName, convertParameterTypes(params), isHomeMethod);
    }

    public InterfaceMethodSignature(MethodSignature signature, boolean isHomeMethod) {
        this(signature.getMethodName(), signature.getParameterTypes(), isHomeMethod);
    }

    public InterfaceMethodSignature(String methodName, String[] parameterTypes, boolean isHomeMethod) {
        this.methodName = methodName;
        this.parameterTypes = parameterTypes != null ? parameterTypes : NOARGS;
        this.isHomeMethod = isHomeMethod;

        int result = 17;
        result = 37 * result + methodName.hashCode();
        for (int i = 0; i < parameterTypes.length; i++) {
            result = 37 * result + parameterTypes[i].hashCode();
        }
        hashCode = result;
    }

    public String getMethodName() {
        return methodName;
    }

    public String[] getParameterTypes() {
        return parameterTypes;
    }

    public boolean isHomeMethod() {
        return isHomeMethod;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(methodName).append('(');
        if (parameterTypes != null) {
            for (int i = 0; i < parameterTypes.length; i++) {
                String parameterType = parameterTypes[i];
                if (i > 0) {
                    buffer.append(',');
                }
                buffer.append(parameterType);
            }
        }
        buffer.append(')');
        return buffer.toString();
    }

    public boolean match(Method method) {
        if(!methodName.equals(method.getName())) {
            return false;
        }
        Class[] types = method.getParameterTypes();
        if (types.length != parameterTypes.length) {
            return false;
        }
        for (int i = 0; i < parameterTypes.length; i++) {
            if(!types[i].getName().equals(parameterTypes[i])) {
                return false;
            }
        }
        return true;
    }

    public Method getMethod(Class clazz) {
        if (clazz == null) {
            return null;
        }

        try {
            ClassLoader classLoader = clazz.getClassLoader();
            Class[] args = new Class[parameterTypes.length];
            for (int i = 0; i < parameterTypes.length; i++) {
                args[i] = ClassLoading.loadClass(parameterTypes[i], classLoader);
            }
            return clazz.getMethod(methodName, args);
        } catch (Exception e) {
            return null;
        }
    }

    public int hashCode() {
        return hashCode;
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof InterfaceMethodSignature)) {
            return false;
        }
        InterfaceMethodSignature other = (InterfaceMethodSignature) obj;
        return  hashCode == other.hashCode &&
                isHomeMethod == other.isHomeMethod &&
                methodName.equals(other.methodName) &&
                Arrays.equals(parameterTypes, other.parameterTypes);
    }

    public int compareTo(Object object) {
        InterfaceMethodSignature methodSignature = (InterfaceMethodSignature) object;

        // home methods come before remote methods
        if (isHomeMethod && !methodSignature.isHomeMethod) {
            return -1;
        }
        if(!isHomeMethod && methodSignature.isHomeMethod) {
            return 1;
        }

        // alphabetic compare of method names
        int value = methodName.compareTo(methodSignature.methodName);
        if (value != 0) {
            return value;
        }

        // shorter parameter list comes before longer parameter lists
        if (parameterTypes.length < methodSignature.parameterTypes.length) {
            return -1;
        }
        if (parameterTypes.length > methodSignature.parameterTypes.length) {
            return 1;
        }

        // alphabetic compare of each parameter type
        for (int i = 0; i < parameterTypes.length; i++) {
            value = parameterTypes[i].compareTo(methodSignature.parameterTypes[i]);
            if (value != 0) {
                return value;
            }
        }

        // they are the same
        return 0;
    }

    private static String[] convertParameterTypes(Class[] params) {
        if(params == null || params.length == 0) {
            return NOARGS;
        }

        String[] types = new String[params.length];
        for (int i = 0; i < params.length; i++) {
            types[i] = params[i].getName();
        }
        return types;
    }
}
