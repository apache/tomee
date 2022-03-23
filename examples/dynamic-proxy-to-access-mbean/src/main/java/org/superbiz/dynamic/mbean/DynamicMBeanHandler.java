/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.superbiz.dynamic.mbean;

import jakarta.annotation.PreDestroy;
import javax.management.Attribute;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Need a @PreDestroy method to disconnect the remote host when used in remote mode.
 */
public class DynamicMBeanHandler implements InvocationHandler {

    private final Map<Method, ConnectionInfo> infos = new ConcurrentHashMap<Method, ConnectionInfo>();

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        final String methodName = method.getName();
        if (method.getDeclaringClass().equals(Object.class) && "toString".equals(methodName)) {
            return getClass().getSimpleName() + " Proxy";
        }
        if (method.getAnnotation(PreDestroy.class) != null) {
            return destroy();
        }

        final ConnectionInfo info = getConnectionInfo(method);
        final MBeanInfo infos = info.getMBeanInfo();
        if (methodName.startsWith("set") && methodName.length() > 3 && args != null && args.length == 1
                && (Void.TYPE.equals(method.getReturnType()) || Void.class.equals(method.getReturnType()))) {
            final String attributeName = attributeName(infos, methodName, method.getParameterTypes()[0]);
            info.setAttribute(new Attribute(attributeName, args[0]));
            return null;
        } else if (methodName.startsWith("get") && (args == null || args.length == 0) && methodName.length() > 3) {
            final String attributeName = attributeName(infos, methodName, method.getReturnType());
            return info.getAttribute(attributeName);
        }
        // operation
        return info.invoke(methodName, args, getSignature(method));
    }

    public Object destroy() {
        for (ConnectionInfo info : infos.values()) {
            info.clean();
        }
        infos.clear();
        return null;
    }

    private String[] getSignature(Method method) {
        String[] args = new String[method.getParameterTypes().length];
        for (int i = 0; i < method.getParameterTypes().length; i++) {
            args[i] = method.getParameterTypes()[i].getName();
        }
        return args; // note: null should often work...
    }

    private String attributeName(MBeanInfo infos, String methodName, Class<?> type) {
        String found = null;
        String foundBackUp = null; // without checking the type
        final String attributeName = methodName.substring(3, methodName.length());
        final String lowerName = Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4, methodName.length());

        for (MBeanAttributeInfo attribute : infos.getAttributes()) {
            final String name = attribute.getName();
            if (attributeName.equals(name)) {
                foundBackUp = attributeName;
                if (attribute.getType().equals(type.getName())) {
                    found = name;
                }
            } else if (found == null && ((lowerName.equals(name) && !attributeName.equals(name))
                    || lowerName.equalsIgnoreCase(name))) {
                foundBackUp = name;
                if (attribute.getType().equals(type.getName())) {
                    found = name;
                }
            }
        }

        if (found == null && foundBackUp == null) {
            throw new UnsupportedOperationException("cannot find attribute " + attributeName);
        }

        if (found != null) {
            return found;
        }
        return foundBackUp;
    }

    private synchronized ConnectionInfo getConnectionInfo(Method method) throws Exception {
        if (!infos.containsKey(method)) {
            synchronized (infos) {
                if (!infos.containsKey(method)) { // double check for synchro
                    org.superbiz.dynamic.mbean.ObjectName on = method.getAnnotation(org.superbiz.dynamic.mbean.ObjectName.class);
                    if (on == null) {
                        Class<?> current = method.getDeclaringClass();
                        do {
                            on = method.getDeclaringClass().getAnnotation(org.superbiz.dynamic.mbean.ObjectName.class);
                            current = current.getSuperclass();
                        } while (on == null && current != null);
                        if (on == null) {
                            throw new UnsupportedOperationException("class or method should define the objectName to use for invocation: " + method.toGenericString());
                        }
                    }
                    final ConnectionInfo info;
                    if (on.url().isEmpty()) {
                        info = new LocalConnectionInfo();
                        ((LocalConnectionInfo) info).server = ManagementFactory.getPlatformMBeanServer(); // could use an id...
                    } else {
                        info = new RemoteConnectionInfo();
                        final Map<String, String[]> environment = new HashMap<String, String[]>();
                        if (!on.user().isEmpty()) {
                            environment.put(JMXConnector.CREDENTIALS, new String[]{on.user(), on.password()});
                        }
                        // ((RemoteConnectionInfo) info).connector = JMXConnectorFactory.newJMXConnector(new JMXServiceURL(on.url()), environment);
                        ((RemoteConnectionInfo) info).connector = JMXConnectorFactory.connect(new JMXServiceURL(on.url()), environment);

                    }
                    info.objectName = new ObjectName(on.value());

                    infos.put(method, info);
                }
            }
        }
        return infos.get(method);
    }

    private abstract static class ConnectionInfo {

        protected ObjectName objectName;

        public abstract void setAttribute(Attribute attribute) throws Exception;

        public abstract Object getAttribute(String attribute) throws Exception;

        public abstract Object invoke(String operationName, Object params[], String signature[]) throws Exception;

        public abstract MBeanInfo getMBeanInfo() throws Exception;

        public abstract void clean();
    }

    private static class LocalConnectionInfo extends ConnectionInfo {

        private MBeanServer server;

        @Override
        public void setAttribute(Attribute attribute) throws Exception {
            server.setAttribute(objectName, attribute);
        }

        @Override
        public Object getAttribute(String attribute) throws Exception {
            return server.getAttribute(objectName, attribute);
        }

        @Override
        public Object invoke(String operationName, Object[] params, String[] signature) throws Exception {
            return server.invoke(objectName, operationName, params, signature);
        }

        @Override
        public MBeanInfo getMBeanInfo() throws Exception {
            return server.getMBeanInfo(objectName);
        }

        @Override
        public void clean() {
            // no-op
        }
    }

    private static class RemoteConnectionInfo extends ConnectionInfo {

        private JMXConnector connector;
        private MBeanServerConnection connection;

        private void before() throws IOException {
            connection = connector.getMBeanServerConnection();
        }

        private void after() throws IOException {
            // no-op
        }

        @Override
        public void setAttribute(Attribute attribute) throws Exception {
            before();
            connection.setAttribute(objectName, attribute);
            after();
        }

        @Override
        public Object getAttribute(String attribute) throws Exception {
            before();
            try {
                return connection.getAttribute(objectName, attribute);
            } finally {
                after();
            }
        }

        @Override
        public Object invoke(String operationName, Object[] params, String[] signature) throws Exception {
            before();
            try {
                return connection.invoke(objectName, operationName, params, signature);
            } finally {
                after();
            }
        }

        @Override
        public MBeanInfo getMBeanInfo() throws Exception {
            before();
            try {
                return connection.getMBeanInfo(objectName);
            } finally {
                after();
            }
        }

        @Override
        public void clean() {
            try {
                connector.close();
            } catch (IOException e) {
                // no-op
            }
        }
    }
}
