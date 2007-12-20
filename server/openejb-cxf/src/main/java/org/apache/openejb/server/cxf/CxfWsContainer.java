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
package org.apache.openejb.server.cxf;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.bus.extension.ExtensionManagerBus;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.DestinationFactoryManager;
import org.apache.openejb.core.webservices.PortData;
import org.apache.openejb.server.httpd.HttpRequest;
import org.apache.openejb.server.httpd.HttpResponse;
import org.apache.openejb.server.httpd.HttpListener;
import org.apache.openejb.server.webservices.saaj.SaajUniverse;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public abstract class CxfWsContainer implements HttpListener {
    protected final Bus bus;
    protected final PortData port;
    protected HttpDestination destination;
    protected CxfEndpoint endpoint;


    public CxfWsContainer(Bus bus, PortData port) {
        this.bus = bus;
        this.port = port;

        List<String> ids = new ArrayList<String>();
        ids.add("http://schemas.xmlsoap.org/wsdl/soap/");

        DestinationFactoryManager factoryManager = bus.getExtension(DestinationFactoryManager.class);
        HttpTransportFactory factory = new HttpTransportFactory(bus);
        factory.setTransportIds(ids);

        factoryManager.registerDestinationFactory("http://cxf.apache.org/transports/http/configuration", factory);
        factoryManager.registerDestinationFactory("http://cxf.apache.org/bindings/xformat", factory);
        factoryManager.registerDestinationFactory("http://www.w3.org/2003/05/soap/bindings/HTTP/", factory);
        factoryManager.registerDestinationFactory("http://schemas.xmlsoap.org/soap/http", factory);
        factoryManager.registerDestinationFactory("http://schemas.xmlsoap.org/wsdl/http/", factory);
        factoryManager.registerDestinationFactory("http://schemas.xmlsoap.org/wsdl/soap/http", factory);
    }

    public void start() {
        endpoint = createEndpoint();
        endpoint.publish("http://nopath");
        destination = (HttpDestination) endpoint.getServer().getDestination();
    }

    protected abstract CxfEndpoint createEndpoint();

    public void destroy() {
        if (endpoint != null) {
            endpoint.stop();
            endpoint = null;
        }
        // if (destination != null) {
        //    destination.shutdown();
        //    destination = null;
        // }
    }

    public void onMessage(HttpRequest request, HttpResponse response) throws Exception {
        if (request.getMethod() == HttpRequest.Method.GET) {
            processGET(request, response);
        } else {
            processPOST(request, response);
        }
    }

    protected void processGET(HttpRequest request, HttpResponse response) throws Exception {
        boolean wsdlRequest = request.getParameter("wsdl") != null ||
                request.getParameter("WSDL") != null ||
                request.getParameter("xsd") != null ||
                request.getParameter("XSD") != null;

        if (wsdlRequest) {
            getWsdl(request, response);
        } else if (endpoint.isSOAP11()) {
            EndpointInfo ei = this.destination.getEndpointInfo();
            response.setContentType("text/html");
            PrintWriter pw = new PrintWriter(response.getOutputStream());
            pw.write("<html><title>Web Service</title><body>");
            pw.write("Hi, this is '" + ei.getService().getName().getLocalPart() + "' web service.");
            pw.write("</body></html>");
            pw.flush();
        } else {
            processPOST(request, response);
        }
    }

    protected void processPOST(HttpRequest request, HttpResponse response) throws Exception {
        SaajUniverse universe = new SaajUniverse();
        universe.set(SaajUniverse.DEFAULT);
        try {
            destination.invoke(request, response);
        } finally {
            universe.unset();
        }
    }

    private void getWsdl(HttpRequest request, HttpResponse response) throws Exception {
        WsdlQueryHandler queryHandler = new WsdlQueryHandler(this.bus);
        String requestUri = request.getURI().toString();
        EndpointInfo ei = this.destination.getEndpointInfo();
        OutputStream out = response.getOutputStream();
        response.setContentType("text/xml");
        queryHandler.writeResponse(requestUri, null, ei, out);
    }

    /*
     * Ensure the bus created is unqiue and non-shared.
     * The very first bus created is set as a default bus which then can
     * be (re)used in other places.
     */
    public static Bus getBus() {
        getDefaultBus();
        return new ExtensionManagerBus();
    }

    /*
     * Ensure the Spring bus is initialized with the CXF module classloader
     * instead of the application classloader.
     */
    public static Bus getDefaultBus() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(CxfEndpoint.class.getClassLoader());
        try {
            return BusFactory.getDefaultBus();
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }
    }
}
