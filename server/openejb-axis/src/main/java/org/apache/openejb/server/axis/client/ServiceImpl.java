/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.server.axis.client;

import org.apache.axis.SimpleTargetedChain;
import org.apache.axis.client.Service;
import org.apache.axis.configuration.SimpleProvider;
import org.apache.axis.encoding.TypeMappingRegistryImpl;
import org.apache.axis.transport.http.HTTPSender;

import javax.xml.namespace.QName;
import javax.xml.rpc.Call;
import javax.xml.rpc.ServiceException;
import javax.xml.rpc.encoding.TypeMappingRegistry;
import javax.xml.rpc.handler.HandlerRegistry;
import java.net.URL;
import java.rmi.Remote;
import java.util.Iterator;
import java.util.Map;

public class ServiceImpl implements javax.xml.rpc.Service {
    private final Service delegate;
    private final Map seiClassNameToFactoryMap;
    private final Map portToImplementationMap;

    public ServiceImpl(final Map portToImplementationMap, final Map seiClassNameToFactoryMap) {
        this.portToImplementationMap = portToImplementationMap;
        this.seiClassNameToFactoryMap = seiClassNameToFactoryMap;

        final TypeMappingRegistryImpl typeMappingRegistry = new TypeMappingRegistryImpl();
        typeMappingRegistry.doRegisterFromVersion("1.3");

        final SimpleProvider engineConfiguration = new SimpleProvider(typeMappingRegistry);
        engineConfiguration.deployTransport("http", new SimpleTargetedChain(new HTTPSender()));

        final AxisClientImpl engine = new AxisClientImpl(engineConfiguration, this.portToImplementationMap);

        delegate = new Service(engineConfiguration, engine);
    }

    public Remote getPort(final QName qName, final Class portClass) throws ServiceException {
        if (qName != null) {
            final String portName = qName.getLocalPart();
            final Remote port = internalGetPort(portName);
            return port;
        }
        return getPort(portClass);
    }

    public Remote getPort(final Class portClass) throws ServiceException {
        final String fqcn = portClass.getName();
        final Remote port = internalGetPortFromClassName(fqcn);
        return port;
    }

    public Call[] getCalls(final QName portName) throws ServiceException {

        if (portName == null) throw new ServiceException("Portname cannot be null");

        final SeiFactory factory = (SeiFactory) portToImplementationMap.get(portName.getLocalPart());
        if (factory == null) throw new ServiceException("No port for portname: " + portName);

        final OperationInfo[] operationInfos = factory.getOperationInfos();
        final javax.xml.rpc.Call[] array = new javax.xml.rpc.Call[operationInfos.length];
        for (int i = 0; i < operationInfos.length; i++) {
            final OperationInfo operation = operationInfos[i];
            array[i] = delegate.createCall(factory.getPortQName(), operation.getOperationName());
        }
        return array;
    }

    public Call createCall(final QName qName) throws ServiceException {
        return delegate.createCall(qName);
    }

    public Call createCall(final QName qName, final QName qName1) throws ServiceException {
        return delegate.createCall(qName, qName1);
    }

    public Call createCall(final QName qName, final String s) throws ServiceException {
        return delegate.createCall(qName, s);
    }

    public Call createCall() throws ServiceException {
        return delegate.createCall();
    }

    public QName getServiceName() {
        final Iterator iterator = portToImplementationMap.values().iterator();
        if (!iterator.hasNext()) return null;
        final SeiFactory factory = (SeiFactory) iterator.next();
        return factory.getServiceName();
    }

    public Iterator getPorts() throws ServiceException {
        return portToImplementationMap.values().iterator();
    }

    public URL getWSDLDocumentLocation() {
        final Iterator iterator = portToImplementationMap.values().iterator();
        if (!iterator.hasNext()) return null;
        final SeiFactory factory = (SeiFactory) iterator.next();
        return factory.getWSDLDocumentLocation();
    }

    public TypeMappingRegistry getTypeMappingRegistry() {
        throw new UnsupportedOperationException();
        //return delegate.getTypeMappingRegistry();
    }

    public HandlerRegistry getHandlerRegistry() {
        throw new UnsupportedOperationException();
    }

    Remote internalGetPort(final String portName) throws ServiceException {
        if (portToImplementationMap.containsKey(portName)) {
            final SeiFactory seiFactory = (SeiFactory) portToImplementationMap.get(portName);
            final Remote port = seiFactory.createServiceEndpoint();
            return port;
        }
        throw new ServiceException("No port for portname: " + portName);
    }

    Remote internalGetPortFromClassName(final String className) throws ServiceException {
        if (seiClassNameToFactoryMap.containsKey(className)) {
            final SeiFactory seiFactory = (SeiFactory) seiClassNameToFactoryMap.get(className);
            final Remote port = seiFactory.createServiceEndpoint();
            return port;
        }
        throw new ServiceException("no port for class " + className);
    }

    Service getService() {
        return delegate;
    }
}
