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

package org.apache.openejb.core.mdb;

import org.apache.openejb.ApplicationException;
import org.apache.openejb.BeanContext;
import org.apache.openejb.ContainerType;
import org.apache.openejb.InterfaceType;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.SystemException;

import jakarta.resource.spi.ResourceAdapter;
import javax.transaction.xa.XAResource;
import java.lang.reflect.Method;
import java.util.Properties;

public interface BaseMdbContainer {
    BeanContext[] getBeanContexts();

    BeanContext getBeanContext(Object deploymentID);

    ContainerType getContainerType();

    Object getContainerID();

    ResourceAdapter getResourceAdapter();

    Class getMessageListenerInterface();

    Properties getProperties();

    void deploy(BeanContext beanContext) throws OpenEJBException;

    void start(BeanContext info) throws OpenEJBException;

    void stop(BeanContext info) throws OpenEJBException;

    void undeploy(BeanContext beanContext) throws OpenEJBException;

    Object invoke(Object deploymentId, InterfaceType type, Class callInterface, Method method, Object[] args, Object primKey) throws OpenEJBException;

    void beforeDelivery(BeanContext deployInfo, Object instance, Method method, XAResource xaResource) throws SystemException;

    Object invoke(Object instance, Method method, InterfaceType type, Object... args) throws SystemException, ApplicationException;

    void afterDelivery(Object instance) throws SystemException;

    void release(BeanContext deployInfo, Object instance);
}
