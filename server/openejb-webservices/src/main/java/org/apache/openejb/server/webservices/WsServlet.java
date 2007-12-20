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
package org.apache.openejb.server.webservices;

import org.apache.openejb.server.httpd.HttpListener;
import org.apache.openejb.server.httpd.HttpRequest;
import org.apache.openejb.server.httpd.HttpResponse;
import org.apache.openejb.server.httpd.ServletRequestAdapter;
import org.apache.openejb.server.httpd.ServletResponseAdapter;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.rpc.ServiceException;
import javax.xml.rpc.handler.MessageContext;
import javax.xml.rpc.server.ServiceLifecycle;
import javax.xml.rpc.server.ServletEndpointContext;
import java.io.IOException;
import java.security.Principal;

public class WsServlet implements Servlet {
    public static final String POJO_CLASS = WsServlet.class.getName() + "@pojoClassName";
    public static final String WEBSERVICE_CONTAINER = WsServlet.class.getName() + "@WebServiceContainer";
    
    private static final DefaultContext DEFAULT_CONTEXT = new DefaultContext();
    private static final ThreadLocal<ServletEndpointContext> endpointContext = new ThreadLocal<ServletEndpointContext>();

    private ServletConfig config;
    private Object pojo;
    private HttpListener service;

    public WsServlet() {
    }

    public WsServlet(HttpListener service) {
        this.service = service;
    }

    public void init(ServletConfig config) throws ServletException {
        this.config = config;

        // this is only used by JaxRPC pojo services
        pojo = createPojoInstance();
        if (pojo instanceof ServiceLifecycle) {
            try {
                ((ServiceLifecycle) pojo).init(new InstanceContext(config.getServletContext()));
            } catch (ServiceException e) {
                throw new ServletException("Unable to initialize ServiceEndpoint", e);
            }
        }
        getService();
    }

    public ServletConfig getServletConfig() {
        return config;
    }

    public String getServletInfo() {
        return "Webservice Servlet " + getService();
    }

    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        HttpListener service = getService();
        if (service == null) throw new ServletException("WebServiceContainer has not been set");
        
        ServletEndpointContext context = getContext();
        endpointContext.set(new InvocationContext((HttpServletRequest) req));
        try {
            res.setContentType("text/xml");
            HttpRequest httpRequest = new ServletRequestAdapter((HttpServletRequest) req, (HttpServletResponse) res, config.getServletContext());
            HttpResponse httpResponse = new ServletResponseAdapter((HttpServletResponse) res);

            if (pojo != null) {
                req.setAttribute(WsConstants.POJO_INSTANCE, pojo);
            }

            try {
                service.onMessage(httpRequest, httpResponse);
            } catch (IOException e) {
                throw e;
            } catch (ServletException e) {
                throw e;
            } catch (Exception e) {
                throw new ServletException("Error processing webservice request", e);
            }
        } finally {
            endpointContext.set(context);
        }
    }

    public void destroy() {
        if (pojo instanceof ServiceLifecycle) {
            ((ServiceLifecycle) pojo).destroy();
        }
    }

    private Object createPojoInstance() throws ServletException {
        ServletContext context = getServletConfig().getServletContext();

        String pojoClassId = context.getInitParameter(POJO_CLASS);
        if (pojoClassId == null) return null;

        Class pojoClass = (Class) context.getAttribute(pojoClassId);
        if (pojoClass == null) return null;

        try {
            Object instance = pojoClass.newInstance();
            return instance;
        } catch (Exception e) {
            throw new ServletException("Unable to instantiate POJO WebService class: " + pojoClass.getName(), e);
        }
    }

    private synchronized HttpListener getService() {
        if (service == null) {
            ServletConfig config = getServletConfig();
            String webServiceContainerId = config.getInitParameter(WEBSERVICE_CONTAINER);
            if (webServiceContainerId != null) {
                service = (HttpListener) config.getServletContext().getAttribute(webServiceContainerId);
            }
        }
        return service;
    }

    private static ServletEndpointContext getContext() {
        ServletEndpointContext context = endpointContext.get();
        return context != null ? context : DEFAULT_CONTEXT;
    }

    private static class InstanceContext implements ServletEndpointContext {
        private final ServletContext servletContext;

        public InstanceContext(ServletContext servletContext) {
            this.servletContext = servletContext;
        }

        public MessageContext getMessageContext() {
            return getContext().getMessageContext();
        }

        public Principal getUserPrincipal() {
            return getContext().getUserPrincipal();
        }

        public HttpSession getHttpSession() {
            return getContext().getHttpSession();
        }

        public ServletContext getServletContext() {
            return servletContext;
        }

        public boolean isUserInRole(String s) {
            return getContext().isUserInRole(s);
        }
    }

    private static class InvocationContext implements ServletEndpointContext {

        private final HttpServletRequest request;

        public InvocationContext(HttpServletRequest request) {
            this.request = request;
        }

        public MessageContext getMessageContext() {
            return (MessageContext) request.getAttribute(WsConstants.MESSAGE_CONTEXT);
        }

        public Principal getUserPrincipal() {
            return request.getUserPrincipal();
        }

        public HttpSession getHttpSession() {
            return request.getSession();
        }

        public ServletContext getServletContext() {
            throw new IllegalAccessError("InstanceContext should never delegate this method.");
        }

        public boolean isUserInRole(String s) {
            return request.isUserInRole(s);
        }
    }

    private static class DefaultContext implements ServletEndpointContext {

        public MessageContext getMessageContext() {
            throw new IllegalStateException("Method cannot be called outside a request context");
        }

        public Principal getUserPrincipal() {
            throw new IllegalStateException("Method cannot be called outside a request context");
        }

        public HttpSession getHttpSession() {
            throw new javax.xml.rpc.JAXRPCException("Method cannot be called outside an http request context");
        }

        public ServletContext getServletContext() {
            throw new IllegalAccessError("InstanceContext should never delegate this method.");
        }

        public boolean isUserInRole(String s) {
            throw new IllegalStateException("Method cannot be called outside a request context");
        }
    }
}
