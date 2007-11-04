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

import org.apache.axis.handlers.HandlerInfoChainFactory;
import org.apache.axis.handlers.soap.SOAPService;
import org.apache.axis.providers.java.RPCProvider;
import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.core.webservices.HandlerChainData;
import org.apache.openejb.core.webservices.HandlerData;
import org.apache.openejb.core.webservices.PortData;
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
        RPCProvider provider = new EjbContainerProvider(deploymentInfo, createHandlerInfos(port.getHandlerChains()));
        SOAPService service = new SOAPService(null, provider, null);
        AxisWsContainer container = new AxisWsContainer(port.getWsdlUrl(), service, null, deploymentInfo.getClassLoader());
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
        RPCProvider provider = new PojoProvider();
        SOAPService service = new SOAPService(null, provider, null);

        // todo need to port this code
        // ServiceInfo serviceInfo = createServiceInfo(portInfo, classLoader);
        // JavaServiceDesc serviceDesc = serviceInfo.getServiceDesc();
        // service.setServiceDescription(serviceDesc);
        service.setOption("className", target.getName());

        List<HandlerInfo> handlerInfos = createHandlerInfos(port.getHandlerChains());
        HandlerInfoChainFactory handlerInfoChainFactory = new HandlerInfoChainFactory(handlerInfos);
        service.setOption(org.apache.axis.Constants.ATTR_HANDLERINFOCHAIN, handlerInfoChainFactory);


        AxisWsContainer container = new AxisWsContainer(port.getWsdlUrl(), service, null, target.getClassLoader());
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
