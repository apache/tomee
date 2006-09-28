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
package org.apache.openejb.proxy;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import net.sf.cglib.reflect.FastClass;
import org.apache.openejb.dispatch.InterfaceMethodSignature;
import org.apache.openejb.dispatch.MethodHelper;

/**
 * @version $Revision$ $Date$
 */
public class EJBProxyHelper {
    public static int[] getOperationMap(Class proxyType, InterfaceMethodSignature[] signatures, boolean isMessageDriven) {
        boolean isHomeInterface = isMessageDriven? false: isHomeInterface(proxyType);

        // get the map from method keys to the intercepted shadow index
        Map proxyToShadowIndex = buildProxyToShadowIndex(proxyType, isHomeInterface);

        // create the method lookup table and fill it with -1
        int[] shadowIndexToProxy = new int[FastClass.create(proxyType).getMaxIndex() + 1];
        Arrays.fill(shadowIndexToProxy, -1);

        // for each interface method, fill in it's id into the shadowIndex table
        for (int i = 0; i < signatures.length; i++) {
            if (signatures[i] != null) {
                Integer shadowIndex = (Integer) proxyToShadowIndex.get(signatures[i]);
                if (shadowIndex != null) {
                    shadowIndexToProxy[shadowIndex.intValue()] = i;
                }
            }
        }
        return shadowIndexToProxy;
    }

    private static boolean isHomeInterface(Class proxyType) {
        //
        // NOTE: We must load the ejb classes from the proxy's classloader because during deployment the
        // proxy's classloader is not a child of the classloader of this class
        //
        try {
            ClassLoader cl = proxyType.getClassLoader();
            Class ejbHomeClass = cl.loadClass("javax.ejb.EJBHome");
            Class ejbLocalHomeClass = cl.loadClass("javax.ejb.EJBLocalHome");
            if(ejbHomeClass.isAssignableFrom(proxyType) || ejbLocalHomeClass.isAssignableFrom(proxyType)) {
                return true;
            }

            Class ejbObjectClass = cl.loadClass("javax.ejb.EJBObject");
            Class ejbLocalObjectClass = cl.loadClass("javax.ejb.EJBLocalObject");
            if (ejbObjectClass.isAssignableFrom(proxyType) || ejbLocalObjectClass.isAssignableFrom(proxyType)) {
                return false;
            }
        } catch (ClassNotFoundException e) {
            // ignore... exception thrown below
        }

        throw new IllegalArgumentException("ProxyType must be an instance of EJBHome, EJBLocalHome, EJBObject, or EJBLocalObject");
    }

    /**
     * Builds a map from the MethodKeys for the real method to the index of
     * the shadow method, which is the same number returned from MethodProxy.getSuperIndex().
     * The map contains only the MethodKeys of methods that have shadow methods (i.e., only
     * the enhanced methods).
     * @param proxyType the generated proxy implementation class
     * @return a map from MethodKeys to the Integer for the shadow method
     */
    private static Map buildProxyToShadowIndex(Class proxyType, boolean isHome) {
        Map shadowMap = new HashMap();
        Method[] methods = proxyType.getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            int shadowIndex = MethodHelper.getSuperIndex(proxyType, methods[i]);
            if (shadowIndex >= 0) {
                shadowMap.put(new InterfaceMethodSignature(methods[i], isHome), new Integer(shadowIndex));
            }
        }
        return shadowMap;
    }
}
