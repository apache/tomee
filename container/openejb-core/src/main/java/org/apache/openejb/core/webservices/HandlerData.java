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
package org.apache.openejb.core.webservices;

import javax.xml.namespace.QName;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class HandlerData {
    private final Class<?> handlerClass;
    private final Properties initParams = new Properties();
    private final List<QName> soapHeaders = new ArrayList<QName>();
    private final List<String> soapRoles = new ArrayList<String>();
    private final List<Method> postConstruct = new ArrayList<Method>();
    private final List<Method> preDestroy = new ArrayList<Method>();

    public HandlerData(Class<?> handlerClass) {
        if (handlerClass == null) throw new NullPointerException("handlerClass is null");
        this.handlerClass = handlerClass;
    }

    public Properties getInitParams() {
        return initParams;
    }

    public List<QName> getSoapHeaders() {
        return soapHeaders;
    }

    public List<String> getSoapRoles() {
        return soapRoles;
    }

    public Class<?> getHandlerClass() {
        return handlerClass;
    }

    public List<Method> getPostConstruct() {
        return postConstruct;
    }

    public List<Method> getPreDestroy() {
        return preDestroy;
    }
}
