/**
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

package org.apache.openejb.server.axis2.pojo;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.axis2.jaxws.server.endpoint.lifecycle.factory.EndpointLifecycleManagerFactory;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HTTPTransportUtils;
import org.apache.openejb.InjectionProcessor;
import org.apache.openejb.core.webservices.PortData;
import org.apache.openejb.server.axis2.Axis2WsContainer;
import org.apache.openejb.server.httpd.HttpRequest;
import org.apache.openejb.server.httpd.HttpResponse;

import javax.naming.Context;
import javax.xml.ws.WebServiceException;

public class PojoWsContainer extends Axis2WsContainer {
    private Object endpointInstance;
    private String contextRoot;
    private InjectionProcessor<Object> injectionProcessor;

    public PojoWsContainer(PortData port, Class<?> endpointClass, Context context, String contextRoot) {
        super(port, endpointClass, context);
        this.contextRoot = contextRoot;
    }
    
    public void start() throws Exception {
        super.start();
        
        /*
         * This replaces EndpointLifecycleManagerFactory for all web services.
         * This should be ok as we do our own endpoint instance management and injection.       
         */
        FactoryRegistry.setFactory(EndpointLifecycleManagerFactory.class, 
                                   new PojoEndpointLifecycleManagerFactory());
                                      
        String servicePath = trimContext(getServicePath(this.contextRoot));
        this.configurationContext.setServicePath(servicePath);
        //need to setContextRoot after servicePath as cachedServicePath is only built 
        //when setContextRoot is called.
        String rootContext = trimContext(this.contextRoot);
        this.configurationContext.setContextRoot(rootContext); 
        
        // instantiate and inject resources into service
        try {
            injectionProcessor = new InjectionProcessor<Object>(endpointClass, port.getInjections(), null, null, context);
            injectionProcessor.createInstance();
            injectionProcessor.postConstruct();
            endpointInstance = injectionProcessor.getInstance();
        } catch (Exception e) {
            throw new WebServiceException("Service resource injection failed", e);
        }
        
        // configure and inject handlers
        try {
            configureHandlers();
        } catch (Exception e) {
            throw new WebServiceException("Error configuring handlers", e);
        }
        
    }
    
    protected void processPOSTRequest(HttpRequest request, HttpResponse response, AxisService service, MessageContext msgContext) throws Exception {
        String contentType = request.getHeader(HTTPConstants.HEADER_CONTENT_TYPE);
        String soapAction = request.getHeader(HTTPConstants.HEADER_SOAP_ACTION);
        if (soapAction == null) {
            soapAction = "\"\"";
        }

        ConfigurationContext configurationContext = msgContext.getConfigurationContext();
        configurationContext.fillServiceContextAndServiceGroupContext(msgContext);
        
        setMsgContextProperties(request, response, service, msgContext);

        ServiceContext serviceContext = msgContext.getServiceContext();
        serviceContext.setProperty(ServiceContext.SERVICE_OBJECT, this.endpointInstance);

        try {
            HTTPTransportUtils.processHTTPPostRequest(msgContext,
                                                      request.getInputStream(),
                                                      response.getOutputStream(),
                                                      contentType,
                                                      soapAction,
                                                      request.getURI().getPath());
        } finally {                        
            // de-associate JAX-WS MessageContext with the thread
            // (association happens in POJOEndpointLifecycleManager.createService() call)
            PojoWsContext.clear();
        } 
    }
         
    public void destroy() {
        // call handler preDestroy
        destroyHandlers();
        
        // call service preDestroy
        if (injectionProcessor != null) {
            injectionProcessor.preDestroy();
        }

        super.destroy();
    }
}
