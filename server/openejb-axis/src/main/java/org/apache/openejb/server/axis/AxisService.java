/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.server.axis;

import org.apache.axis.description.JavaServiceDesc;
import org.apache.axis.handlers.HandlerInfoChainFactory;
import org.apache.axis.handlers.soap.SOAPService;
import org.apache.axis.providers.java.RPCProvider;
import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.core.webservices.HandlerChainData;
import org.apache.openejb.core.webservices.HandlerData;
import org.apache.openejb.core.webservices.PortData;
import org.apache.openejb.server.axis.assembler.JaxRpcServiceInfo;
import org.apache.openejb.server.axis.assembler.JaxRpcServiceInfoBuilder;
import org.apache.openejb.server.httpd.HttpListener;
import org.apache.openejb.server.webservices.WsService;

import javax.naming.Context;
import javax.xml.namespace.QName;
import javax.xml.rpc.handler.HandlerInfo;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class AxisService extends WsService {
    private final Map<String, AxisWsContainer> wsContainers = new TreeMap<String, AxisWsContainer>();

    public String getName() {
        return "axis";
    }

    protected HttpListener createEjbWsContainer(URL moduleBaseUrl, PortData port, DeploymentInfo deploymentInfo) throws Exception {
        ClassLoader classLoader = deploymentInfo.getClassLoader();

        // todo build JaxRpcServiceInfo in assembler
        JaxRpcServiceInfoBuilder serviceInfoBuilder = new JaxRpcServiceInfoBuilder(null, null, null, null, null, classLoader);
        JaxRpcServiceInfo serviceInfo = serviceInfoBuilder.createServiceDesc();

        // Build java service descriptor
        JavaServiceDescBuilder javaServiceDescBuilder = new JavaServiceDescBuilder(serviceInfo, classLoader);
        JavaServiceDesc serviceDesc = javaServiceDescBuilder.createServiceDesc();

        // Create service
        RPCProvider provider = new EjbContainerProvider(deploymentInfo, createHandlerInfos(port.getHandlerChains()));
        SOAPService service = new SOAPService(null, provider, null);
        service.setServiceDescription(serviceDesc);

        // Set class name
        service.setOption("className", deploymentInfo.getServiceEndpointInterface().getName());
        serviceDesc.setImplClass(deploymentInfo.getServiceEndpointInterface());

        // Create container
        AxisWsContainer container = new AxisWsContainer(port.getWsdlUrl(), service, null, classLoader);
        wsContainers.put(deploymentInfo.getDeploymentID().toString(), container);
        return container;
    }

    protected void destroyEjbWsContainer(String deploymentId) {
        AxisWsContainer container = wsContainers.remove(deploymentId);
        if (container != null) {
            container.destroy();
        }
    }

    protected HttpListener createPojoWsContainer(URL moduleBaseUrl, PortData port, String serviceId, Class target, Context context, String contextRoot) throws Exception {
        ClassLoader classLoader = target.getClassLoader();

        // todo build JaxRpcServiceInfo in assembler
        JaxRpcServiceInfoBuilder serviceInfoBuilder = new JaxRpcServiceInfoBuilder(null, null, null, null, null, classLoader);
        JaxRpcServiceInfo serviceInfo = serviceInfoBuilder.createServiceDesc();

        // Build java service descriptor
        JavaServiceDescBuilder javaServiceDescBuilder = new JavaServiceDescBuilder(serviceInfo, classLoader);
        JavaServiceDesc serviceDesc = javaServiceDescBuilder.createServiceDesc();

        // Create service
        RPCProvider provider = new PojoProvider();
        SOAPService service = new SOAPService(null, provider, null);
        service.setServiceDescription(serviceDesc);

        // Set class name
        service.setOption("className", target.getName());

        // Add Handler Chain
        List<HandlerInfo> handlerInfos = createHandlerInfos(port.getHandlerChains());
        HandlerInfoChainFactory handlerInfoChainFactory = new HandlerInfoChainFactory(handlerInfos);
        service.setOption(org.apache.axis.Constants.ATTR_HANDLERINFOCHAIN, handlerInfoChainFactory);

        // Create container
        AxisWsContainer container = new AxisWsContainer(port.getWsdlUrl(), service, null, classLoader);
        wsContainers.put(serviceId, container);
        return container;
    }

    protected void destroyPojoWsContainer(String serviceId) {
        AxisWsContainer container = wsContainers.remove(serviceId);
        if (container != null) {
            container.destroy();
        }
    }

    @SuppressWarnings({"unchecked"})
    private List<HandlerInfo> createHandlerInfos(List<HandlerChainData> handlerChains) throws ClassNotFoundException {
        if (handlerChains == null || handlerChains.isEmpty()) return null;
        List<HandlerData> handlers = handlerChains.get(0).getHandlers();

        List<HandlerInfo> handlerInfos = new ArrayList<HandlerInfo>(handlers.size());
        for (HandlerData handler : handlers) {
            Class<?> handlerClass = handler.getHandlerClass();
            Map initParams = new HashMap(handler.getInitParams());
            QName[] headers = handler.getSoapHeaders().toArray(new QName[handler.getSoapHeaders().size()]);
            HandlerInfo handlerInfo = new HandlerInfo(handlerClass, initParams, headers);
            handlerInfos.add(handlerInfo);
        }

        return handlerInfos;
    }
}
