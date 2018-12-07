/*
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

package org.apache.openejb.core.webservices;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

public class HandlerChainData {
    private final QName serviceNamePattern;
    private final QName portNamePattern;
    private final List<String> protocolBindings = new ArrayList<>();
    private final List<HandlerData> handlers = new ArrayList<>();

    public HandlerChainData(final QName serviceNamePattern, final QName portNamePattern, final List<String> protocolBindings, final List<HandlerData> handlers) {
        this.serviceNamePattern = serviceNamePattern;
        this.portNamePattern = portNamePattern;
        this.protocolBindings.addAll(protocolBindings);
        this.handlers.addAll(handlers);
    }

    public QName getServiceNamePattern() {
        return serviceNamePattern;
    }

    public QName getPortNamePattern() {
        return portNamePattern;
    }

    public List<String> getProtocolBindings() {
        return protocolBindings;
    }

    public List<HandlerData> getHandlers() {
        return handlers;
    }
}
