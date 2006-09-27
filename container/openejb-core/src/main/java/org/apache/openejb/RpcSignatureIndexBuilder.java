/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact info@openejb.org.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY THE OPENEJB GROUP AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE OPENEJB GROUP OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2005 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
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
