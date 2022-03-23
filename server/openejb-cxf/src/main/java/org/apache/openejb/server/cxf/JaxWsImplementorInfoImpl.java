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

import org.apache.cxf.jaxws.support.JaxWsImplementorInfo;

import jakarta.xml.ws.BindingType;
import jakarta.xml.ws.soap.SOAPBinding;

/**
 * Override the binding type uri.
 */
public class JaxWsImplementorInfoImpl extends JaxWsImplementorInfo {
    private final String bindingURI;

    public JaxWsImplementorInfoImpl(Class clazz, String bindingURI) {
        super(clazz);
        this.bindingURI = bindingURI;
    }

    @Override
    public String getBindingType() {
        final BindingType bType = getImplementorClass().getAnnotation(BindingType.class);
        if (bType != null) {
            return bType.value();
        }

        if (this.bindingURI != null) {
            return this.bindingURI;
        }

        return SOAPBinding.SOAP11HTTP_BINDING;
    }
}
