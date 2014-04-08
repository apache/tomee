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

public class ServiceImpl implements javax.xml.rpc.Service{
    private final Service delegate;
    private final Map seiClassNameToFactoryMap;
    private final Map portToImplementationMap;

    public ServiceImpl(Map portToImplementationMap, Map seiClassNameToFactoryMap) {
        this.portToImplementationMap = portToImplementationMap;
        this.seiClassNameToFactoryMap = seiClassNameToFactoryMap;

        TypeMappingRegistryImpl typeMappingRegistry = new TypeMappingRegistryImpl();
        typeMappingRegistry.doRegisterFromVersion("1.3");

        SimpleProvider engineConfiguration = new SimpleProvider(typeMappingRegistry);
        engineConfiguration.deployTransport("http", new SimpleTargetedChain(new HTTPSender()));

        AxisClientImpl engine = new AxisClientImpl(engineConfiguration, this.portToImplementationMap);

        delegate = new Service(engineConfiguration, engine);
    }

    public Remote getPort(QName qName, Class portClass) throws ServiceException {
        if (qName != null) {
            String portName = qName.getLocalPart();
            Remote port = internalGetPort(portName);
            return port;
        }
        return getPort(portClass);
    }

    public Remote getPort(Class portClass) throws ServiceException {
        String fqcn = portClass.getName();
        Remote port = internalGetPortFromClassName(fqcn);
        return port;
    }

    public Call[] getCalls(QName portName) throws ServiceException {

        if (portName == null) throw new ServiceException("Portname cannot be null");

        SeiFactory factory = (SeiFactory) portToImplementationMap.get(portName.getLocalPart());
        if (factory == null) throw new ServiceException("No port for portname: " + portName);

        OperationInfo[] operationInfos = factory.getOperationInfos();
        javax.xml.rpc.Call[] array = new javax.xml.rpc.Call[operationInfos.length];
        for (int i = 0; i < operationInfos.length; i++) {
            OperationInfo operation = operationInfos[i];
            array[i] = delegate.createCall(factory.getPortQName(), operation.getOperationName());
        }
        return array;
    }

    public Call createCall(QName qName) throws ServiceException {
        return delegate.createCall(qName);
    }

    public Call createCall(QName qName, QName qName1) throws ServiceException {
        return delegate.createCall(qName, qName1);
    }

    public Call createCall(QName qName, String s) throws ServiceException {
        return delegate.createCall(qName, s);
    }

    public Call createCall() throws ServiceException {
        return delegate.createCall();
    }

    public QName getServiceName() {
        Iterator iterator = portToImplementationMap.values().iterator();
        if (!iterator.hasNext()) return null;
        SeiFactory factory = (SeiFactory) iterator.next();
        return factory.getServiceName();
    }

    public Iterator getPorts() throws ServiceException {
        return portToImplementationMap.values().iterator();
    }

    public URL getWSDLDocumentLocation() {
        Iterator iterator = portToImplementationMap.values().iterator();
        if (!iterator.hasNext()) return null;
        SeiFactory factory = (SeiFactory) iterator.next();
        return factory.getWSDLDocumentLocation();
    }

    public TypeMappingRegistry getTypeMappingRegistry() {
        throw new UnsupportedOperationException();
        //return delegate.getTypeMappingRegistry();
    }

    public HandlerRegistry getHandlerRegistry() {
        throw new UnsupportedOperationException();
    }

    Remote internalGetPort(String portName) throws ServiceException {
        if (portToImplementationMap.containsKey(portName)) {
            SeiFactory seiFactory = (SeiFactory) portToImplementationMap.get(portName);
            Remote port = seiFactory.createServiceEndpoint();
            return port;
        }
        throw new ServiceException("No port for portname: " + portName);
    }

    Remote internalGetPortFromClassName(String className) throws ServiceException {
        if (seiClassNameToFactoryMap.containsKey(className)) {
            SeiFactory seiFactory = (SeiFactory) seiClassNameToFactoryMap.get(className);
            Remote port = seiFactory.createServiceEndpoint();
            return port;
        }
        throw new ServiceException("no port for class " + className);
    }

    Service getService() {
        return delegate;
    }
}
