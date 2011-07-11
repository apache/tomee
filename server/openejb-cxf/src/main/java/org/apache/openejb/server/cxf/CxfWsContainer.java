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
import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.apache.cxf.transport.http.HTTPTransportFactory;
import org.apache.openejb.core.webservices.PortData;
import org.apache.openejb.server.cxf.transport.HttpTransportFactory;
import org.apache.openejb.server.httpd.HttpListener;
import org.apache.openejb.server.httpd.HttpRequest;
import org.apache.openejb.server.httpd.HttpResponse;

import java.util.ArrayList;
import java.util.List;

public abstract class CxfWsContainer implements HttpListener {
    protected final Bus bus;
    protected final PortData port;
    protected AbstractHTTPDestination destination;
    protected CxfEndpoint endpoint;
    protected HTTPTransportFactory httpTransportFactory;

    public CxfWsContainer(Bus bus, PortData port) {
        this.bus = bus;
        this.port = port;
        
        List<String> ids = new ArrayList<String>();
        ids.add("http://schemas.xmlsoap.org/wsdl/soap/");

        httpTransportFactory = new HttpTransportFactory(bus);
        httpTransportFactory.setTransportIds(ids);
    }

    public void start() {
        endpoint = createEndpoint();
        endpoint.publish("http://nopath");
        destination = (AbstractHTTPDestination) endpoint.getServer().getDestination();
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
        destination.invoke(null, request.getServletContext(), request, response);
    }
}
