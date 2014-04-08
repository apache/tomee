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
package org.apache.openejb.monitoring.remote;

import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.monitoring.LocalMBeanServer;
import org.apache.openejb.monitoring.ObjectNameBuilder;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

public class RemoteResourceMonitor implements DynamicMBean {
    private static final String PING = "ping";
    private static final AttributeList ATTRIBUTE_LIST = new AttributeList();
    private static final MBeanAttributeInfo[] EMPTY_ATTRIBUTES = new MBeanAttributeInfo[0];
    private static final MBeanNotificationInfo[] EMPTY_NOTIFICATIONS = new MBeanNotificationInfo[0];
    private static final MBeanParameterInfo[] EMPTY_PARAMETERS = new MBeanParameterInfo[0];
    private static final MBeanOperationInfo PING_INFO = new MBeanOperationInfo("ping", "ping the parameter host", new MBeanParameterInfo[] {
                                                                new MBeanParameterInfo("host", String.class.getName(), "the host to ping")
                                                            }, String.class.getName(), MBeanOperationInfo.INFO);

    private final Collection<String> hosts = new CopyOnWriteArraySet<String>();
    private ObjectName objectName = null;
    private MBeanInfo info = null;

    public synchronized void addHost(final String host) {
        hosts.add(host);
        buildMBeanInfo();
    }

    public synchronized void removeHost(final String host) {
        hosts.remove(host);
        buildMBeanInfo();
    }

    public void registerIfNot() { // do it lazily
        if (objectName != null) {
            return;
        }

        final ObjectNameBuilder jmxName = new ObjectNameBuilder("openejb.management");
        jmxName.set("ObjectType", "Related Hosts");
        objectName = jmxName.build();

        final MBeanServer server = LocalMBeanServer.get();
        try {
            if (server.isRegistered(objectName)) {
                server.unregisterMBean(objectName);
            }
            server.registerMBean(this, objectName);
        } catch (Exception e) {
            throw new OpenEJBRuntimeException(e);
        }
    }

    public void unregister() {
        try {
            LocalMBeanServer.get().unregisterMBean(objectName);
        } catch (Exception e) {
            throw new OpenEJBRuntimeException(e);
        }
    }

    @Override
    public Object invoke(final String actionName, final Object[] params, final String[] signature) throws MBeanException, ReflectionException {
        if (hosts.contains(actionName)) {
            return ping(actionName);
        } else if (PING.equals(actionName) && params != null && params.length == 1) {
            return ping((String) params[0]);
        }
        throw new MBeanException(new IllegalArgumentException(), actionName + " doesn't exist");
    }

    @Override
    public MBeanInfo getMBeanInfo() {
        if (info == null) {
            buildMBeanInfo();
        }
        return info;
    }

    @Override
    public Object getAttribute(final String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException {
        throw new AttributeNotFoundException();
    }

    @Override
    public void setAttribute(final Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
        throw new AttributeNotFoundException();
    }

    @Override
    public AttributeList getAttributes(String[] attributes) {
        return ATTRIBUTE_LIST;
    }

    @Override
    public AttributeList setAttributes(AttributeList attributes) {
        return ATTRIBUTE_LIST;
    }

    private void buildMBeanInfo() {
        final List<MBeanOperationInfo> operationInfos = new ArrayList<MBeanOperationInfo>();
        for (String host: hosts) {
            operationInfos.add(new MBeanOperationInfo(host, "ping host " + host, EMPTY_PARAMETERS, String.class.getName(), MBeanOperationInfo.INFO));
        }
        operationInfos.add(PING_INFO);
        info = new MBeanInfo(RemoteResourceMonitor.class.getName(),
                "Monitor remote resources",
                EMPTY_ATTRIBUTES,
                null,
                operationInfos.toArray(new MBeanOperationInfo[operationInfos.size()]),
                EMPTY_NOTIFICATIONS);
    }

    private static String ping(final String host) {
        try {
            final InetAddress address = InetAddress.getByName(host);
            boolean ok = address.isReachable(30000);
            if (ok) { // do it twice since the first one is generally longer
                final long start = System.nanoTime();
                ok = address.isReachable(30000);
                final long end = System.nanoTime();
                if (ok) {
                    final long duration = end - start;
                    final long ms = TimeUnit.NANOSECONDS.toMillis(duration);
                    return "Ping done in " + ms + "." + Long.toString(duration - 1000 * ms) + " ms";
                }
            }
            return "Can't ping host, timeout (30s)";
        } catch (UnknownHostException e) {
            return "Can't find host: " + e.getMessage();
        } catch (IOException e) {
            return "Can't ping host: " + e.getMessage();
        }
    }
}
