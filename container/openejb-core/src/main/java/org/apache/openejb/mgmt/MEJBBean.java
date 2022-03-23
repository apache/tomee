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

package org.apache.openejb.mgmt;

import java.util.List;
import java.util.Set;
import jakarta.ejb.Lock;
import jakarta.ejb.RemoteHome;
import jakarta.ejb.Singleton;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.QueryExp;
import javax.management.ReflectionException;
import javax.management.j2ee.ListenerRegistration;
import javax.management.j2ee.ManagementHome;

import static jakarta.ejb.LockType.READ;

@Singleton(name = "MEJB")
@Lock(READ)
@RemoteHome(ManagementHome.class)
public class MEJBBean {

    MBeanServer mbeanServer;

    public MEJBBean() {
        final List mbeanServers = MBeanServerFactory.findMBeanServer(null);
        if (mbeanServers.size() > 0) {
            mbeanServer = (MBeanServer) mbeanServers.get(0);
        } else {
            mbeanServer = MBeanServerFactory.createMBeanServer();
        }
    }

    public Object getAttribute(final ObjectName objectName, final String string) throws MBeanException, AttributeNotFoundException, InstanceNotFoundException, ReflectionException {
        return mbeanServer.getAttribute(objectName, string);
    }

    public AttributeList getAttributes(final ObjectName objectName, final String[] strings) throws InstanceNotFoundException, ReflectionException {
        return mbeanServer.getAttributes(objectName, strings);
    }

    public String getDefaultDomain() {
        return mbeanServer.getDefaultDomain();
    }

    public Integer getMBeanCount() {
        return mbeanServer.getMBeanCount();
    }

    public MBeanInfo getMBeanInfo(final ObjectName objectName) throws IntrospectionException, InstanceNotFoundException, ReflectionException {
        return mbeanServer.getMBeanInfo(objectName);
    }

    public Object invoke(final ObjectName objectName, final String string, final Object[] objects, final String[] strings) throws InstanceNotFoundException, MBeanException, ReflectionException {
        return mbeanServer.invoke(objectName, string, objects, strings);
    }

    public boolean isRegistered(final ObjectName objectName) {
        return mbeanServer.isRegistered(objectName);
    }

    public Set queryNames(final ObjectName objectName, final QueryExp queryExp) {
        return mbeanServer.queryNames(objectName, queryExp);
    }

    public void setAttribute(final ObjectName objectName, final Attribute attribute) throws InstanceNotFoundException, AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
        mbeanServer.setAttribute(objectName, attribute);
    }

    public AttributeList setAttributes(final ObjectName objectName, final AttributeList attributeList) throws InstanceNotFoundException, ReflectionException {
        return mbeanServer.setAttributes(objectName, attributeList);
    }

    public ListenerRegistration getListenerRegistry() {
        return null;
    }
}