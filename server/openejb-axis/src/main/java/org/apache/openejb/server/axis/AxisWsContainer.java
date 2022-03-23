/**
 *   Licensed to the Apache Software Foundation (ASF) under one or more
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
package org.apache.openejb.server.axis;

import org.apache.axis.AxisEngine;
import org.apache.axis.AxisFault;
import org.apache.axis.Constants;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.SOAPPart;
import org.apache.axis.handlers.soap.SOAPService;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.axis.soap.SOAPConstants;
import org.apache.axis.transport.http.HTTPConstants;
import org.apache.axis.utils.Messages;
import org.apache.openejb.server.ServerRuntimeException;
import org.apache.openejb.server.httpd.HttpListener;
import org.apache.openejb.server.httpd.HttpRequest;
import org.apache.openejb.server.httpd.HttpResponse;
import org.apache.openejb.server.webservices.WsConstants;
import org.apache.openejb.server.webservices.saaj.SaajUniverse;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.w3c.dom.Element;

import jakarta.servlet.http.HttpServletResponse;
import javax.wsdl.OperationType;
import jakarta.xml.soap.MimeHeader;
import jakarta.xml.soap.MimeHeaders;
import jakarta.xml.soap.SOAPMessage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

public class AxisWsContainer implements HttpListener {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.AXIS, AxisWsContainer.class);
    public static final String REQUEST = AxisWsContainer.class.getName() + "@Request";
    public static final String RESPONSE = AxisWsContainer.class.getName() + "@Response";

    public static final String XSD_NS = "http://www.w3.org/2001/XMLSchema";

    private final URL wsdlLocation;
    private final SOAPService service;

    private final ClassLoader classLoader;
    private final Map wsdlMap;

    public AxisWsContainer(final URL wsdlURL, final SOAPService service, final Map wsdlMap, final ClassLoader classLoader) {
        this.wsdlLocation = wsdlURL;
        this.service = service;
        this.wsdlMap = wsdlMap;
        if (classLoader == null) {
            this.classLoader = Thread.currentThread().getContextClassLoader();
        } else {
            this.classLoader = classLoader;
        }
    }

    /**
     *
     * @param request HttpRequest
     * @param response HttpResponse
     * @throws Exception
     */
    @Override
    public void onMessage(final HttpRequest request, final HttpResponse response) throws Exception {
        final SaajUniverse universe = new SaajUniverse();
        universe.set(SaajUniverse.AXIS1);
        try {
            doService(request, response);
        } finally {
            universe.unset();
        }
    }

    protected void doService(final HttpRequest req, final HttpResponse res) throws Exception {
        final org.apache.axis.MessageContext messageContext = new org.apache.axis.MessageContext(null);
        req.setAttribute(WsConstants.MESSAGE_CONTEXT, messageContext);

        messageContext.setClassLoader(classLoader);

        Message responseMessage;

        String contentType = req.getHeader(HTTPConstants.HEADER_CONTENT_TYPE);
        final String contentLocation = req.getHeader(HTTPConstants.HEADER_CONTENT_LOCATION);
        final InputStream inputStream = req.getInputStream();
        final Message requestMessage = new Message(inputStream, false, contentType, contentLocation);

        messageContext.setRequestMessage(requestMessage);
        messageContext.setProperty(HTTPConstants.MC_HTTP_SERVLETPATHINFO, req.getURI().getPath());
        messageContext.setProperty(org.apache.axis.MessageContext.TRANS_URL, req.getURI().toString());
        messageContext.setService(service);
        messageContext.setProperty(REQUEST, req);
        messageContext.setProperty(RESPONSE, res);
        messageContext.setProperty(AxisEngine.PROP_DISABLE_PRETTY_XML, Boolean.TRUE);

        final ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            try {
                final String characterEncoding = (String) requestMessage.getProperty(SOAPMessage.CHARACTER_SET_ENCODING);
                if (characterEncoding != null) {
                    messageContext.setProperty(SOAPMessage.CHARACTER_SET_ENCODING, characterEncoding);
                } else {
                    messageContext.setProperty(SOAPMessage.CHARACTER_SET_ENCODING, "UTF-8");
                }


                final String soapAction = req.getHeader(HTTPConstants.HEADER_SOAP_ACTION);
                if (soapAction != null) {
                    messageContext.setUseSOAPAction(true);
                    messageContext.setSOAPActionURI(soapAction);
                }

                final SOAPEnvelope env = requestMessage.getSOAPEnvelope();
                if (env != null && env.getSOAPConstants() != null) {
                    messageContext.setSOAPConstants(env.getSOAPConstants());
                }
                final SOAPService service = messageContext.getService();

                Thread.currentThread().setContextClassLoader(classLoader);
                service.invoke(messageContext);

                responseMessage = messageContext.getResponseMessage();
            } catch (final AxisFault fault) {

                if (req.getMethod().equals(HttpRequest.Method.GET.name()) && req.getParameters().isEmpty()) {
                    String serviceName = req.getURI().getRawPath();
                    serviceName = serviceName.substring(serviceName.lastIndexOf("/") + 1);
                    printServiceInfo(res, serviceName);
                    return;
                } else {
                    responseMessage = handleFault(fault, res, messageContext);
                }

            } catch (final Exception e) {
                responseMessage = handleException(messageContext, res, e);
            }
            //TODO investigate and fix operation == null!
            if (messageContext.getOperation() != null) {
                if (messageContext.getOperation().getMep() == OperationType.ONE_WAY) {
                    // No content, so just indicate accepted
                    res.setStatus(HttpServletResponse.SC_ACCEPTED);
                    return;
                } else if (responseMessage == null) {
                    responseMessage = handleException(messageContext, null, new ServerRuntimeException("No response for non-one-way operation"));
                }
            } else if (responseMessage == null) {
                res.setStatus(HttpServletResponse.SC_ACCEPTED);
                return;
            }
            try {
                final SOAPConstants soapConstants = messageContext.getSOAPConstants();
                final String contentType1 = responseMessage.getContentType(soapConstants);
                res.setContentType(contentType1);
                // Transfer MIME headers to HTTP headers for response message.
                final MimeHeaders responseMimeHeaders = responseMessage.getMimeHeaders();
                for (final Iterator i = responseMimeHeaders.getAllHeaders(); i.hasNext(); ) {
                    final MimeHeader responseMimeHeader = (MimeHeader) i.next();
                    res.setHeader(responseMimeHeader.getName(),
                        responseMimeHeader.getValue());
                }
                //TODO discuss this with dims.
//                // synchronize the character encoding of request and response
//                String responseEncoding = (String) messageContext.getProperty(
//                        SOAPMessage.CHARACTER_SET_ENCODING);
//                if (responseEncoding != null) {
//                    try {
//                        responseMessage.setProperty(SOAPMessage.CHARACTER_SET_ENCODING,
//                                               responseEncoding);
//                    } catch (SOAPException e) {
//                        log.info(Messages.getMessage("exception00"), e);
//                    }
//                }
                //determine content type from message response
                contentType = responseMessage.getContentType(messageContext.
                    getSOAPConstants());
                responseMessage.writeTo(res.getOutputStream());
            } catch (final Exception e) {
                LOGGER.warning(Messages.getMessage("exception00"), e);
            }
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
    }

    private Message handleException(final MessageContext context, final HttpResponse res, final Exception e) {
        Message responseMessage;
        //other exceptions are internal trouble
        responseMessage = context.getResponseMessage();
        res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        Message responseMsg = responseMessage;
        LOGGER.warning(Messages.getMessage("exception00"), e);
        if (responseMsg == null) {
            final AxisFault fault = AxisFault.makeFault(e);
            //log the fault
            final Element runtimeException = fault.lookupFaultDetail(Constants.QNAME_FAULTDETAIL_RUNTIMEEXCEPTION);
            if (runtimeException != null) {
                LOGGER.debug(Messages.getMessage("axisFault00"), fault);
                //strip runtime details
                fault.removeFaultDetail(Constants.QNAME_FAULTDETAIL_RUNTIMEEXCEPTION);
            }
            responseMsg = new Message(fault);
        }
        responseMessage = responseMsg;
        final SOAPPart soapPart = (SOAPPart) responseMessage.getSOAPPart();
        soapPart.getMessage().setMessageContext(context);
        return responseMessage;
    }

    private Message handleFault(final AxisFault fault, final HttpResponse res, final MessageContext context) {
        Message responseMessage;
        final Element runtimeException = fault.lookupFaultDetail(Constants.QNAME_FAULTDETAIL_RUNTIMEEXCEPTION);

        LOGGER.warning(Messages.getMessage("axisFault00"), fault);
        if (runtimeException != null) {
            //strip runtime details
            fault.removeFaultDetail(Constants.QNAME_FAULTDETAIL_RUNTIMEEXCEPTION);
        }

        final int status = fault.getFaultCode().getLocalPart().startsWith("Server.Unauth")
            ? HttpServletResponse.SC_UNAUTHORIZED
            : HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        if (status == HttpServletResponse.SC_UNAUTHORIZED) {
            // unauth access results in authentication request
            // TODO: less generic realm choice?
            res.setHeader("WWW-Authenticate", "Basic realm=\"AXIS\"");
        }
        res.setStatus(status);
        responseMessage = context.getResponseMessage();
        if (responseMessage == null) {
            responseMessage = new Message(fault);
            final SOAPPart soapPart = (SOAPPart) responseMessage.getSOAPPart();
            soapPart.getMessage().setMessageContext(context);
        }
        return responseMessage;
    }

    public void getWsdl(final HttpRequest request, final HttpResponse response) throws Exception {
        final URI realLocation = request.getURI();
//        log.info("Request at " + realLocation);
        final String query = realLocation.getQuery();
        if (query == null || !query.toLowerCase().startsWith("wsdl")) {
            throw new IllegalStateException("request must contain a  wsdl or WSDL parameter: " + request.getParameters());
        }
        final String locationKey;
        if (query.length() > 4) {
            locationKey = query.substring(5);
        } else {
            locationKey = wsdlLocation.toString();
        }
        final Object wsdl = wsdlMap.get(locationKey);
        if (wsdl == null) {
            throw new IllegalStateException("No wsdl or schema known at location: " + locationKey);
        }
        final URI updated = new URI(realLocation.getScheme(),
            realLocation.getUserInfo(),
            realLocation.getHost(),
            realLocation.getPort(),
            null, //try null for no path
            null,
            null);
        final String replaced = ((String) wsdl).replaceAll(WsConstants.LOCATION_REPLACEMENT_TOKEN, updated.toString());
        response.getOutputStream().write(replaced.getBytes());
        response.getOutputStream().flush();
    }

    public void destroy() {
    }

    /**
     * print a snippet of service info.
     *
     * @param response    response
     * @param serviceName Name of the service
     */

    private void printServiceInfo(final HttpResponse response, final String serviceName) throws IOException {
        response.setContentType("text/html; charset=utf-8");
        final StringBuffer output = new StringBuffer("<h1>")
            .append(serviceName).append("</h1>\n");

        output.append("<p>").append(Messages.getMessage("axisService00"))
            .append("</p>\n");
        output.append(
            "<i>").append(
            Messages.getMessage("perhaps00")).append(
            "</i>\n");
        response.getOutputStream().write(output.toString().getBytes());
    }

}
























