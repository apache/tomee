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
 * Copyright 2001 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.apache.openejb.proxy;

import org.apache.openejb.EJBComponentType;


public class ProxyInfo extends org.apache.openejb.ProxyInfo {
    private static final long serialVersionUID = 569021597222976175L;
    private final int componentType;
    private final String containerId;
    private final Object primaryKey;

    private final Class remoteInterface;
    private final Class homeInterface;
    private final Class localHomeInterface;
    private final Class localInterface;
    private final Class serviceEndpointInterface;
    private final Class primaryKeyClass;


    public ProxyInfo(ProxyInfo info, Object primaryKey) {
        this.componentType = info.componentType;
        this.containerId = info.containerId;
        this.homeInterface = info.homeInterface;
        this.remoteInterface = info.remoteInterface;
        this.localHomeInterface = info.localHomeInterface;
        this.localInterface = info.localInterface;
        this.serviceEndpointInterface = info.serviceEndpointInterface;
        this.primaryKeyClass = info.primaryKeyClass;
        this.primaryKey = primaryKey;
    }

    public ProxyInfo(
            int componentType,
            String containerId,
            Class homeInterface,
            Class remoteInterface,
            Class localHomeInterface,
            Class localInterface,
            Class serviceEndpointInterface,
            Class primaryKeyClass) {

        this.componentType = componentType;
        this.containerId = containerId;
        this.homeInterface = homeInterface;
        this.remoteInterface = remoteInterface;
        this.localHomeInterface = localHomeInterface;
        this.localInterface = localInterface;
        this.serviceEndpointInterface = serviceEndpointInterface;
        this.primaryKeyClass = primaryKeyClass;
        this.primaryKey = null;
    }

    public String getContainerID() {
        return containerId;
    }

    public boolean isSessionBean() {
        return componentType == EJBComponentType.STATELESS || componentType == EJBComponentType.STATEFUL;
    }

    public boolean isStatefulSessionBean() {
        return componentType == EJBComponentType.STATEFUL;
    }

    public boolean isStatelessSessionBean() {
        return componentType == EJBComponentType.STATELESS;
    }

    public boolean isBMPEntityBean() {
        return componentType == EJBComponentType.BMP_ENTITY;
    }

    public boolean isCMPEntityBean() {
        return componentType == EJBComponentType.CMP_ENTITY;
    }

    public boolean isMessageBean() {
        return componentType == EJBComponentType.MESSAGE_DRIVEN;
    }

    public int getComponentType() {
        return componentType;
    }

    public Class getHomeInterface() {
        return homeInterface;
    }

    public Class getRemoteInterface() {
        return remoteInterface;
    }

    public Class getLocalHomeInterface() {
        return localHomeInterface;
    }

    public Class getLocalInterface() {
        return localInterface;
    }

    public Class getServiceEndpointInterface() {
        return serviceEndpointInterface;
    }

    public Class getPrimaryKeyClass() {
        return primaryKeyClass;
    }

    // TODO: Kill this method
    public Object getPrimaryKey() {
        return primaryKey;
    }

}
