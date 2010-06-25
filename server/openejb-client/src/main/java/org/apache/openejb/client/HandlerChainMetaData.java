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
package org.apache.openejb.client;

import javax.xml.namespace.QName;
import java.util.List;
import java.util.ArrayList;
import java.io.Serializable;

public class HandlerChainMetaData implements Serializable {
    private static final long serialVersionUID = -2861396042527297097L;
    private QName serviceNamePattern;
    private QName portNamePattern;
    private final List<String> protocolBindings = new ArrayList<String>();
    private final List<HandlerMetaData> handlers = new ArrayList<HandlerMetaData>();

    public QName getServiceNamePattern() {
        return serviceNamePattern;
    }

    public void setServiceNamePattern(QName serviceNamePattern) {
        this.serviceNamePattern = serviceNamePattern;
    }

    public QName getPortNamePattern() {
        return portNamePattern;
    }

    public void setPortNamePattern(QName portNamePattern) {
        this.portNamePattern = portNamePattern;
    }

    public List<String> getProtocolBindings() {
        return protocolBindings;
    }

    public List<HandlerMetaData> getHandlers() {
        return handlers;
    }
}
