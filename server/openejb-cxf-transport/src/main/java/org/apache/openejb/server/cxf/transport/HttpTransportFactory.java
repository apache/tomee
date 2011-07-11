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
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.Destination;
import org.apache.cxf.transport.http.HTTPTransportFactory;

import java.io.IOException;

public class HttpTransportFactory extends HTTPTransportFactory {
    public HttpTransportFactory() {
    	// no-op
    }
  
    public HttpTransportFactory(Bus bus) {
        setBus(bus);

    }

    @Override public Destination getDestination(EndpointInfo endpointInfo) throws IOException {
        return new HttpDestination(getBus(), getRegistry(), endpointInfo, endpointInfo.getAddress());
    }
}
