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
import org.apache.openejb.BeanContext;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.assembler.classic.util.ServiceConfiguration;
import org.apache.openejb.core.webservices.HandlerChainData;
import org.apache.openejb.core.webservices.HandlerData;
import org.apache.openejb.core.webservices.PortData;
import org.apache.openejb.jee.JavaWsdlMapping;
import org.apache.openejb.jee.PortComponent;
import org.apache.openejb.server.axis.assembler.CommonsSchemaInfoBuilder;
import org.apache.openejb.server.axis.assembler.JaxRpcServiceInfo;
import org.apache.openejb.server.axis.assembler.JaxRpcServiceInfoBuilder;
import org.apache.openejb.server.axis.assembler.XmlSchemaInfo;
import org.apache.openejb.server.httpd.HttpListener;
import org.apache.openejb.server.webservices.WsService;

import javax.naming.Context;
import javax.wsdl.Port;
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

    private JaxRpcServiceInfo getJaxRpcServiceInfo(final ClassLoader classLoader) throws OpenEJBException {
        final JavaWsdlMapping mapping = null; // the java to wsdl mapping file
        final CommonsSchemaInfoBuilder xmlBeansSchemaInfoBuilder = new CommonsSchemaInfoBuilder(null, null); // the schema data from the wsdl file
        final PortComponent portComponent = null; // webservice.xml declaration of this service
        final Port port = null; // wsdl.xml declaration of this service
        final String wsdlFile = null;

        final XmlSchemaInfo schemaInfo = xmlBeansSchemaInfoBuilder.createSchemaInfo();

        final JaxRpcServiceInfoBuilder serviceInfoBuilder = new JaxRpcServiceInfoBuilder(mapping, schemaInfo, portComponent, port, wsdlFile, classLoader);
        final JaxRpcServiceInfo serviceInfo = serviceInfoBuilder.createServiceInfo();
        return serviceInfo;
    }

    @Override
    protected HttpListener createEjbWsContainer(final URL url, final PortData port, final BeanContext beanContext, final ServiceConfiguration serviceInfos) throws Exception {
        final ClassLoader classLoader = beanContext.getClassLoader();

        // todo build JaxRpcServiceInfo in assembler
        final JaxRpcServiceInfo serviceInfo = getJaxRpcServiceInfo(classLoader);

        // Build java service descriptor
        final JavaServiceDescBuilder javaServiceDescBuilder = new JavaServiceDescBuilder(serviceInfo, classLoader);
        final JavaServiceDesc serviceDesc = javaServiceDescBuilder.createServiceDesc();

        // Create service
        final RPCProvider provider = new EjbRpcProvider(beanContext, createHandlerInfos(port.getHandlerChains()));
        final SOAPService service = new SOAPService(null, provider, null);
        service.setServiceDescription(serviceDesc);

        // Set class name
        service.setOption("className", beanContext.getServiceEndpointInterface().getName());
        serviceDesc.setImplClass(beanContext.getServiceEndpointInterface());

        // Create container
        final AxisWsContainer container = new AxisWsContainer(port.getWsdlUrl(), service, null, classLoader);
        wsContainers.put(beanContext.getDeploymentID().toString(), container);
        return container;
    }


    protected void destroyEjbWsContainer(final String deploymentId) {
        final AxisWsContainer container = wsContainers.remove(deploymentId);
        if (container != null) {
            container.destroy();
        }
    }

    protected HttpListener createPojoWsContainer(final ClassLoader loader, final URL moduleBaseUrl, final PortData port, final String serviceId, final Class target, final Context context, final String contextRoot, final Map<String, Object> bdgs, final ServiceConfiguration serviceInfos) throws Exception {
        final ClassLoader classLoader = target.getClassLoader();

        // todo build JaxRpcServiceInfo in assembler
        final JaxRpcServiceInfo serviceInfo = getJaxRpcServiceInfo(classLoader);

        // Build java service descriptor
        final JavaServiceDescBuilder javaServiceDescBuilder = new JavaServiceDescBuilder(serviceInfo, classLoader);
        final JavaServiceDesc serviceDesc = javaServiceDescBuilder.createServiceDesc();

        // Create service
        final RPCProvider provider = new PojoProvider();
        final SOAPService service = new SOAPService(null, provider, null);
        service.setServiceDescription(serviceDesc);

        // Set class name
        service.setOption("className", target.getName());

        // Add Handler Chain
        final List<HandlerInfo> handlerInfos = createHandlerInfos(port.getHandlerChains());
        final HandlerInfoChainFactory handlerInfoChainFactory = new HandlerInfoChainFactory(handlerInfos);
        service.setOption(org.apache.axis.Constants.ATTR_HANDLERINFOCHAIN, handlerInfoChainFactory);

        // Create container
        final AxisWsContainer container = new AxisWsContainer(port.getWsdlUrl(), service, null, classLoader);
        wsContainers.put(serviceId, container);
        return container;
    }

    protected void destroyPojoWsContainer(final String serviceId) {
        final AxisWsContainer container = wsContainers.remove(serviceId);
        if (container != null) {
            container.destroy();
        }
    }

    @SuppressWarnings({"unchecked"})
    private List<HandlerInfo> createHandlerInfos(final List<HandlerChainData> handlerChains) throws ClassNotFoundException {
        if (handlerChains == null || handlerChains.isEmpty()) return null;
        final List<HandlerData> handlers = handlerChains.get(0).getHandlers();

        final List<HandlerInfo> handlerInfos = new ArrayList<HandlerInfo>(handlers.size());
        for (final HandlerData handler : handlers) {
            final Class<?> handlerClass = handler.getHandlerClass();
            final Map initParams = new HashMap(handler.getInitParams());
            final QName[] headers = handler.getSoapHeaders().toArray(new QName[handler.getSoapHeaders().size()]);
            final HandlerInfo handlerInfo = new HandlerInfo(handlerClass, initParams, headers);
            handlerInfos.add(handlerInfo);
        }

        return handlerInfos;
    }
}
