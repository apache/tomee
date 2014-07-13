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

package org.apache.openejb;

import java.util.ArrayList;
import java.util.List;

public class ProxyInfo {

    protected BeanContext beanContext;
    protected Object primaryKey;
    protected List<Class> proxyInterfaces;
    protected RpcContainer beanContainer;
    protected InterfaceType interfaceType;
    protected Class mainInterface;

    protected ProxyInfo() {
    }

    public ProxyInfo(final BeanContext beanContext, final Object primaryKey, final List<Class> interfaces, final InterfaceType proxyType, final Class mainInterface) {
        this.beanContext = beanContext;
        this.primaryKey = primaryKey;
        this.proxyInterfaces = interfaces;
        this.interfaceType = proxyType;
        this.beanContainer = (RpcContainer) beanContext.getContainer();
        this.mainInterface = mainInterface;
    }

    /**
     * This is the constructor that containers should call.
     * Containers do not know the list of interfaces that should
     * be applied nor do they need to tell the proxy handling
     * code what kind of proxy it should create.
     *
     * @param depInfo
     * @param pk
     */
    public ProxyInfo(final BeanContext depInfo, final Object pk) {
        this(depInfo, pk, new ArrayList<Class>(), InterfaceType.UNKNOWN, null);
    }

    public InterfaceType getInterfaceType() {
        return interfaceType;
    }

    public BeanContext getBeanContext() {
        return beanContext;
    }

    public Object getPrimaryKey() {
        return primaryKey;
    }

    public Class getInterface() {
        return mainInterface == null && proxyInterfaces != null && !proxyInterfaces.isEmpty() ? proxyInterfaces.get(0) : mainInterface;
    }

    public List<Class> getInterfaces() {
        return proxyInterfaces;
    }

    public RpcContainer getBeanContainer() {
        return beanContainer;
    }

}
