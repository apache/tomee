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
package org.apache.openejb;

import java.util.TreeSet;
import java.lang.reflect.Method;

import javax.ejb.TimedObject;
import javax.ejb.Timer;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import org.apache.openejb.proxy.ProxyInfo;
import org.apache.openejb.dispatch.InterfaceMethodSignature;

/**
 * @version $Revision$ $Date$
 */
public class RpcSignatureIndexBuilder implements SignatureIndexBuilder {
    private final Class beanClass;
    private final Class homeInterface;
    private final Class remoteInterface;
    private final Class localHomeInterface;
    private final Class localInterface;
    private final Class serviceEndpointInterface;

    public RpcSignatureIndexBuilder(Class beanClass, ProxyInfo proxyInfo) {
        this(beanClass,
                proxyInfo.getHomeInterface(),
                proxyInfo.getRemoteInterface(),
                proxyInfo.getLocalHomeInterface(),
                proxyInfo.getLocalInterface(),
                proxyInfo.getServiceEndpointInterface());
    }

    public RpcSignatureIndexBuilder(Class beanClass, Class homeInterface, Class remoteInterface, Class localHomeInterface, Class localInterface, Class serviceEndpointInterface) {
        this.beanClass = beanClass;
        this.homeInterface = homeInterface;
        this.remoteInterface = remoteInterface;
        this.localHomeInterface = localHomeInterface;
        this.localInterface = localInterface;
        this.serviceEndpointInterface = serviceEndpointInterface;
    }

    public InterfaceMethodSignature[] createSignatureIndex() {
        TreeSet signatures = createSignatureSet();
        return (InterfaceMethodSignature[]) signatures.toArray(new InterfaceMethodSignature[signatures.size()]);
    }

    public TreeSet createSignatureSet() {
        TreeSet signatures = new TreeSet();
        if (homeInterface != null) {
            Method[] homeMethods = homeInterface.getMethods();
            for (int i = 0; i < homeMethods.length; i++) {
                Method homeMethod = homeMethods[i];
                if (shouldIndexedHomeMethod(homeMethod)) {
                    signatures.add(new InterfaceMethodSignature(homeMethod, true));
                }
            }
        }

        if (localHomeInterface != null) {
            Method[] localHomeMethods = localHomeInterface.getMethods();
            for (int i = 0; i < localHomeMethods.length; i++) {
                Method localHomeMethod = localHomeMethods[i];
                signatures.add(new InterfaceMethodSignature(localHomeMethod, true));
            }
        }

        if (remoteInterface != null) {
            Method[] remoteMethods = remoteInterface.getMethods();
            for (int i = 0; i < remoteMethods.length; i++) {
                Method remoteMethod = remoteMethods[i];
                if (shouldIndexedRemoteMethod(remoteMethod)) {
                    signatures.add(new InterfaceMethodSignature(remoteMethod, false));
                }
            }
        }

        if (localInterface != null) {
            Method[] localMethods = localInterface.getMethods();
            for (int i = 0; i < localMethods.length; i++) {
                Method localMethod = localMethods[i];
                if (shouldIndexedLocalMethod(localMethod)) {
                    signatures.add(new InterfaceMethodSignature(localMethod, false));
                }
            }
        }

        if (serviceEndpointInterface != null) {
            Method[] serviceMethods = serviceEndpointInterface.getMethods();
            for (int i = 0; i < serviceMethods.length; i++) {
                Method serviceEndpointInterface = serviceMethods[i];
                if (shouldIndexedLocalMethod(serviceEndpointInterface)) {
                    signatures.add(new InterfaceMethodSignature(serviceEndpointInterface, false));
                }
            }
        }

        if (TimedObject.class.isAssignableFrom(beanClass)) {
            signatures.add(new InterfaceMethodSignature("ejbTimeout", new Class[]{Timer.class}, false));
        }

        signatures.add(new InterfaceMethodSignature("ejbActivate", false));
        signatures.add(new InterfaceMethodSignature("ejbPassivate", false));

        if (EntityBean.class.isAssignableFrom(beanClass)) {
            signatures.add(new InterfaceMethodSignature("setEntityContext", new Class[] { EntityContext.class }, false));
            signatures.add(new InterfaceMethodSignature("unsetEntityContext", false));
            signatures.add(new InterfaceMethodSignature("ejbLoad", false));
            signatures.add(new InterfaceMethodSignature("ejbStore", false));
        } else if (SessionBean.class.isAssignableFrom(beanClass)) {
            signatures.add(new InterfaceMethodSignature("setSessionContext", new Class[] { SessionContext.class }, false));
        }
        return signatures;
    }

    protected boolean shouldIndexedHomeMethod(Method method) {
        String name = method.getName();
        return !name.equals("getEJBMetaData") &&
            !name.equals("getHomeHandle");
    }

    private static boolean shouldIndexedRemoteMethod(Method method) {
        String name = method.getName();
        return !name.equals("getEJBHome") &&
            !name.equals("getHandle") &&
            !name.equals("getPrimaryKey") &&
            !name.equals("isIdentical");
    }

    private static boolean shouldIndexedLocalMethod(Method method) {
        String name = method.getName();
        return !name.equals("getEJBLocalHome") &&
            !name.equals("getPrimaryKey") &&
            !name.equals("isIdentical");
    }
}
