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

package org.apache.openejb.server.axis2;

import org.apache.axiom.om.util.UUIDGenerator;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.AddressingHelper;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.engine.Handler.InvocationResponse;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.impl.DescriptionUtils;
import org.apache.axis2.jaxws.handler.lifecycle.factory.HandlerLifecycleManagerFactory;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.axis2.transport.OutTransportInfo;
import org.apache.axis2.transport.RequestResponseTransport;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HTTPTransportReceiver;
import org.apache.axis2.transport.http.HTTPTransportUtils;
import org.apache.axis2.transport.http.TransportHeaders;
import org.apache.axis2.transport.http.util.RESTUtil;
import org.apache.axis2.util.MessageContextBuilder;
import org.apache.openejb.core.webservices.HandlerResolverImpl;
import org.apache.openejb.core.webservices.PortData;
import org.apache.openejb.server.axis2.client.Axis2Config;
import org.apache.openejb.server.httpd.HttpRequest;
import org.apache.openejb.server.httpd.HttpResponse;
import org.apache.openejb.server.httpd.HttpListener;
import org.apache.openejb.server.webservices.WsConstants;
import org.apache.openejb.server.webservices.saaj.SaajUniverse;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.LogCategory;

import javax.naming.Context;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.handler.Handler;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public abstract class Axis2WsContainer implements HttpListener {
    private static final Logger logger = Logger.getInstance(LogCategory.AXIS2, Axis2WsContainer.class);

    public static final String REQUEST = Axis2WsContainer.class.getName() + "@Request";
    public static final String RESPONSE = Axis2WsContainer.class.getName() + "@Response";

    protected String endpointClassName;
    protected PortData port;
    protected ConfigurationContext configurationContext;
    protected final Class<?> endpointClass;
    protected AxisService service;
    protected WsdlQueryHandler wsdlQueryHandler;
    protected Context context;
    private HandlerResolverImpl handlerResolver;

    public Axis2WsContainer(PortData port, Class<?> endpointClass, Context context) {
        this.endpointClass = endpointClass;
        this.port = port;
        this.context = context;
    }

    public void start() throws Exception {
        Axis2Config.initialize();

        configurationContext = ConfigurationContextFactory.createBasicConfigurationContext("META-INF/openejb-axis2.xml");

        AxisServiceGenerator serviceGen = createServiceGenerator();
        if (port.getWsdlUrl() != null) {
            // WSDL file has been provided
            service = serviceGen.getServiceFromWSDL(port, endpointClass);
        } else {
            // No WSDL, let Axis2 handle it.
            service = serviceGen.getServiceFromClass(endpointClass);
        }

        service.setScope(Constants.SCOPE_APPLICATION);
        configurationContext.getAxisConfiguration().addService(service);

        wsdlQueryHandler = new WsdlQueryHandler(service);

        /*
        * This replaces HandlerLifecycleManagerFactory for all web services.
        * This should be ok as we do our own handler instance managment and injection.
        * Also, this does not affect service-ref clients, as we install our own
        * HandlerResolver.
        */
        FactoryRegistry.setFactory(HandlerLifecycleManagerFactory.class, new HandlerLifecycleManagerFactoryImpl());
    }

    protected AxisServiceGenerator createServiceGenerator() {
        return new AxisServiceGenerator();
    }

    public void getWsdl(HttpRequest request, HttpResponse response) throws Exception {
        doService(request, response);
    }

    public void onMessage(HttpRequest request, HttpResponse response) throws Exception {
        SaajUniverse universe = new SaajUniverse();
        universe.set(SaajUniverse.AXIS2);
        try {
            doService(request, response);
        } finally {
            universe.unset();
        }
    }

    protected void doService(HttpRequest request, HttpResponse response) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug("Target URI: " + request.getURI());
        }

        MessageContext msgContext = new MessageContext();
        msgContext.setIncomingTransportName(Constants.TRANSPORT_HTTP);
        msgContext.setProperty(MessageContext.REMOTE_ADDR, request.getRemoteAddr());

        try {
            TransportOutDescription transportOut = this.configurationContext.getAxisConfiguration()
                    .getTransportOut(Constants.TRANSPORT_HTTP);
            TransportInDescription transportIn = this.configurationContext.getAxisConfiguration()
                    .getTransportIn(Constants.TRANSPORT_HTTP);

            msgContext.setConfigurationContext(this.configurationContext);

            //TODO: Port this segment for session support.
//            String sessionKey = (String) this.httpcontext.getAttribute(HTTPConstants.COOKIE_STRING);
//            if (this.configurationContext.getAxisConfiguration().isManageTransportSession()) {
//                SessionContext sessionContext = this.sessionManager.getSessionContext(sessionKey);
//                msgContext.setSessionContext(sessionContext);
//            }
            msgContext.setTransportIn(transportIn);
            msgContext.setTransportOut(transportOut);
            msgContext.setServiceGroupContextId(UUIDGenerator.getUUID());
            msgContext.setServerSide(true);
            msgContext.setAxisService(this.service);

            doService2(request, response, msgContext);
        } catch (Throwable e) {
            String msg = "Exception occurred while trying to invoke service method doService()";
            logger.error(msg, e);
            try {
                msgContext.setProperty(MessageContext.TRANSPORT_OUT, response.getOutputStream());
                msgContext.setProperty(Constants.OUT_TRANSPORT_INFO, new Axis2TransportInfo(response));

                MessageContext faultContext = MessageContextBuilder.createFaultMessageContext(msgContext, e);
                // If the fault is not going along the back channel we should be 202ing
                if (AddressingHelper.isFaultRedirected(msgContext)) {
                    response.setStatusCode(HttpURLConnection.HTTP_ACCEPTED);
                } else {
                    response.setStatusCode(HttpURLConnection.HTTP_INTERNAL_ERROR);
                }
                AxisEngine.sendFault(faultContext);
            } catch (Exception ex) {
                if (AddressingHelper.isFaultRedirected(msgContext)) {
                    response.setStatusCode(HttpURLConnection.HTTP_ACCEPTED);
                } else {
                    response.setStatusCode(HttpURLConnection.HTTP_INTERNAL_ERROR);
                    response.setHeader(HTTPConstants.HEADER_CONTENT_TYPE, "text/plain");
                    PrintWriter pw = new PrintWriter(response.getOutputStream());
                    ex.printStackTrace(pw);
                    pw.flush();
                    logger.error(msg, ex);
                }
            }
        }

    }

    protected String getServicePath(String contextRoot) {
        String location = port.getLocation();
        if (location != null && location.startsWith(contextRoot)) {
            return location.substring(contextRoot.length());
        }
        return null;
    }

    public static String trimContext(String contextPath) {
        if (contextPath != null) {
            if (contextPath.startsWith("/")) {
                contextPath = contextPath.substring(1);
            }
            if (contextPath.endsWith("/")) {
                contextPath = contextPath.substring(0, contextPath.length() - 1);
            }
        }
        return contextPath;
    }

    public void doService2(HttpRequest request, HttpResponse response, MessageContext msgContext) throws Exception {

        if (request.getMethod() == HttpRequest.Method.GET) {
            processGETRequest(request, response, this.service, msgContext);
        } else if (request.getMethod() == HttpRequest.Method.POST) {
            processPOSTRequest(request, response, this.service, msgContext);
        } else {
            throw new UnsupportedOperationException("[" + request.getMethod() + " ] method not supported");
        }

        // Finalize response
        OperationContext operationContext = msgContext.getOperationContext();
        Object contextWritten = null;
        Object isTwoChannel = null;
        if (operationContext != null) {
            contextWritten = operationContext.getProperty(Constants.RESPONSE_WRITTEN);
            isTwoChannel = operationContext.getProperty(Constants.DIFFERENT_EPR);
        }

        if ((contextWritten != null) && Constants.VALUE_TRUE.equals(contextWritten)) {
            if ((isTwoChannel != null) && Constants.VALUE_TRUE.equals(isTwoChannel)) {
                response.setStatusCode(HttpURLConnection.HTTP_ACCEPTED);
                return;
            }
            response.setStatusCode(HttpURLConnection.HTTP_OK);
        } else {
            response.setStatusCode(HttpURLConnection.HTTP_ACCEPTED);
        }
    }

    public void destroy() {
    }

    public static class Axis2TransportInfo implements OutTransportInfo {
        private HttpResponse response;

        public Axis2TransportInfo(HttpResponse response) {
            this.response = response;
        }

        public void setContentType(String contentType) {
            response.setHeader(HTTPConstants.HEADER_CONTENT_TYPE, contentType);
        }
    }

    protected void processGETRequest(HttpRequest request, HttpResponse response, AxisService service, MessageContext msgContext) throws Exception {
        if (request.getURI().getQuery() != null && (request.getURI().getQuery().startsWith("wsdl") || request.getURI().getQuery().startsWith("xsd"))) {
            // wsdl or xsd request

            if (port.getWsdlUrl() != null) {
                URL wsdlURL = port.getWsdlUrl();
                this.wsdlQueryHandler.writeResponse(request.getURI().toString(), wsdlURL.toString(), response.getOutputStream());
            } else {
                service.printWSDL(response.getOutputStream());
            }
        } else if (AxisServiceGenerator.isSOAP11(service)) {
            response.setContentType("text/html");
            PrintWriter pw = new PrintWriter(response.getOutputStream());
            pw.write("<html><title>Web Service</title><body>");
            pw.write("Hi, this is '" + service.getName() + "' web service.");
            pw.write("</body></html>");
            pw.flush();
        } else {
            // REST request
            setMsgContextProperties(request, response, service, msgContext);

            String contentType = request.getHeader(HTTPConstants.HEADER_CONTENT_TYPE);

            msgContext.setTo(new EndpointReference(request.getURI().toString()));

            msgContext.setProperty(MessageContext.TRANSPORT_OUT, response.getOutputStream());
            msgContext.setProperty(Constants.OUT_TRANSPORT_INFO, new Axis2TransportInfo(response));

            InvocationResponse processed = RESTUtil.processURLRequest(msgContext, response.getOutputStream(), contentType);

            if (!processed.equals(InvocationResponse.CONTINUE)) {
                response.setStatusCode(HttpURLConnection.HTTP_OK);
                String s = HTTPTransportReceiver.getServicesHTML(configurationContext);
                PrintWriter pw = new PrintWriter(response.getOutputStream());
                pw.write(s);
                pw.flush();
            }
        }
    }

    protected void setMsgContextProperties(HttpRequest request, HttpResponse response, AxisService service, MessageContext msgContext) {
        msgContext.setProperty(MessageContext.TRANSPORT_OUT, response.getOutputStream());
        msgContext.setProperty(Constants.OUT_TRANSPORT_INFO, new Axis2TransportInfo(response));
        msgContext.setProperty(RequestResponseTransport.TRANSPORT_CONTROL, new Axis2RequestResponseTransport(response));
        msgContext.setProperty(Constants.Configuration.TRANSPORT_IN_URL, request.getURI().toString());
        msgContext.setIncomingTransportName(Constants.TRANSPORT_HTTP);

        HttpServletRequest servletRequest = (HttpServletRequest) request.getAttribute(HttpRequest.SERVLET_REQUEST);
        msgContext.setProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST, servletRequest);

        HttpServletResponse servletResponse = (HttpServletResponse) request.getAttribute(HttpRequest.SERVLET_RESPONSE);
        msgContext.setProperty(HTTPConstants.MC_HTTP_SERVLETRESPONSE, servletResponse);

        ServletContext servletContext = (ServletContext) request.getAttribute(HttpRequest.SERVLET_CONTEXT);
        msgContext.setProperty(HTTPConstants.MC_HTTP_SERVLETCONTEXT, servletContext);

        if (servletRequest != null) {
            msgContext.setProperty(MessageContext.TRANSPORT_HEADERS, new TransportHeaders(servletRequest));
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

        HTTPTransportUtils.processHTTPPostRequest(msgContext, request.getInputStream(), response.getOutputStream(), contentType, soapAction, request.getURI().getPath());
    }

    /*
    * Gets the right handlers for the port/service/bindings and performs injection.
    */
    protected void configureHandlers() throws Exception {
        EndpointDescription desc = AxisServiceGenerator.getEndpointDescription(this.service);
        if (desc != null) {
            handlerResolver = new HandlerResolverImpl(port.getHandlerChains(), port.getInjections(), context);
            List<Handler> handlers = handlerResolver.getHandlerChain(port);

            DescriptionUtils.registerHandlerHeaders(desc.getAxisService(), handlers);
        }
    }

    protected void destroyHandlers() {
        if (this.handlerResolver != null) {
            handlerResolver.destroyHandlers();
            handlerResolver = null;
        }
    }
}
