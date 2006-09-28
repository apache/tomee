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

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import net.sf.cglib.core.Signature;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.cglib.asm.Type;
import org.apache.openejb.EJBInterfaceType;

/**
 * Helper methods to deal with the whack handeling of indexes in cglib MethodProxy objects.
 *
 * @version $Revision$ $Date$
 */
public final class MethodHelper {
    private MethodHelper() {
    }

    public static int getSuperIndex(Class proxyImpl, MethodSignature signature) {
        try {
            //TODO look at asm Type and see if there is a more straightforward way to do this.
            // lookup the method object and get its index
            Method method = signature.getMethod(proxyImpl);
            return getSuperIndex(proxyImpl, method);
        } catch (Exception e) {
            // didn't find the method
            return -1;
        }
    }

    public static int getSuperIndex(Class proxyType, Method method) {
        Signature signature = new Signature(method.getName(), Type.getReturnType(method), Type.getArgumentTypes(method));
        MethodProxy methodProxy = MethodProxy.find(proxyType, signature);
        if (methodProxy != null) {
            return methodProxy.getSuperIndex();
        }
        return -1;
    }

    public static Map getMethodMap(EJBInterfaceType type, MethodSignature[] signatures, Class interfaceClass){
        if (type == EJBInterfaceType.HOME || type == EJBInterfaceType.LOCALHOME ){
            return getHomeMethodMap(signatures, interfaceClass);
        } else {
            return getObjectMethodMap(signatures, interfaceClass);
        }
    }
    
    public static Map getHomeMethodMap(MethodSignature[] signatures, Class homeClass) {
        return getMethodMap(homeClass, translateToHome(signatures));
    }

    public static Map getObjectMethodMap(MethodSignature[] signatures, Class objectClass) {
        return getMethodMap(objectClass, translateToObject(signatures));
    }

    private static Map getMethodMap(Class homeClass, MethodSignature[] signatures) {
        Method[] methods = homeClass.getMethods();
        Map methodMap = new HashMap(methods.length);
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            Integer index = findMethodIndex(signatures, method);
            if (index != null) {
                methodMap.put(method, index);
            }
        }
        return methodMap;
    }

    private static Integer findMethodIndex(MethodSignature[] signatures, Method method) {
        for (int i = 0; i < signatures.length; i++) {
            MethodSignature signature = signatures[i];
            if (signature != null && signature.match(method)) {
                return new Integer(i);
            }
        }
        return null;
    }

    public static MethodSignature[] translateToHome(MethodSignature[] signatures) {
        MethodSignature[] translated = new MethodSignature[signatures.length];
        for (int i = 0; i < signatures.length; i++) {
            MethodSignature signature = signatures[i];
            String name = signature.getMethodName();
            if (name.startsWith("ejbCreate")) {
                translated[i] = new MethodSignature("c" + name.substring(4), signature.getParameterTypes());
            } else if (name.startsWith("ejbFind")) {
                translated[i] = new MethodSignature("f" + name.substring(4), signature.getParameterTypes());
            } else if (name.startsWith("ejbHome")) {
                String translatedName = Character.toLowerCase(name.charAt(7)) + name.substring(8);
                translated[i] = new MethodSignature(translatedName, signature.getParameterTypes());
            } else if (name.startsWith("ejbRemove")) {
                translated[i] = new MethodSignature("remove", signature.getParameterTypes());
            }
        }
        return translated;
    }

    public static MethodSignature[] translateToObject(MethodSignature[] signatures) {
        MethodSignature[] translated = new MethodSignature[signatures.length];
        for (int i = 0; i < signatures.length; i++) {
            MethodSignature signature = signatures[i];
            String name = signature.getMethodName();
            if (name.startsWith("ejbRemove")) {
                translated[i] = new MethodSignature("remove", signature.getParameterTypes());
            } else {
                translated[i] = new MethodSignature(signature.getMethodName(), signature.getParameterTypes());
            }
        }
        return translated;
    }

    public static InterfaceMethodSignature translateToInterface(MethodSignature signature) {
        String name = signature.getMethodName();
        if (name.startsWith("ejbCreate")) {
            return new InterfaceMethodSignature("c" + name.substring(4), signature.getParameterTypes(), true);
        } else if (name.startsWith("ejbFind")) {
            return new InterfaceMethodSignature("f" + name.substring(4), signature.getParameterTypes(), true);
        } else if (name.startsWith("ejbHome")) {
            String translatedName = Character.toLowerCase(name.charAt(7)) + name.substring(8);
            return new InterfaceMethodSignature(translatedName, signature.getParameterTypes(), true);
        } else if (name.startsWith("ejbRemove")) {
            boolean isHome = signature.getParameterTypes().length == 1;
            return new InterfaceMethodSignature("remove", signature.getParameterTypes(), isHome);
        } else {
            return new InterfaceMethodSignature(signature.getMethodName(), signature.getParameterTypes(),false);
        }
    }

    public static String capitalize(String string) {
        if (string == null) throw new NullPointerException("string is null");
        if (string.length() == 0) throw new IllegalArgumentException("string is empty");

        if (string.length() == 1) {
            return "" + Character.toUpperCase(string.charAt(0));
        }
        return Character.toUpperCase(string.charAt(0)) + string.substring(1);
    }
}
