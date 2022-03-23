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
package org.apache.openejb.server.cxf.transport;

import org.apache.cxf.Bus;
import org.apache.cxf.message.Message;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.apache.cxf.transport.http.DestinationRegistry;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

public class HttpDestination extends AbstractHTTPDestination {
    public HttpDestination(Bus bus, DestinationRegistry registry, EndpointInfo endpointInfo, String path) throws IOException {
        super(bus, registry, endpointInfo, path, true);
    }

    public DestinationRegistry getRegistry() {
        return registry;
    }

    @Override
    public Logger getLogger() {
        return Logger.getLogger(HttpDestination.class.getName());
    }

    @Override
    protected void setupMessage(final Message inMessage, final ServletConfig config, final ServletContext context, final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        super.setupMessage(inMessage, config, context, req, resp);

        /*
         * This parameter is needed to pass these tests of the Jakarta EE TCK
         *
         * com.sun.ts.tests.jaxrs.ee.rs.pathparam.locator
         */
//        inMessage.put("keep.subresource.candidates", true);
    }
}
