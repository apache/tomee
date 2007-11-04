/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.openejb.server.axis2.util;

import org.apache.openejb.util.Logger;
import org.apache.openejb.util.LogCategory;
import org.xml.sax.InputSource;

import javax.wsdl.xml.WSDLLocator;
import java.io.IOException;

public class SimpleWsdlLocator implements WSDLLocator {
    private static final Logger logger = Logger.getInstance(LogCategory.AXIS2, SimpleWsdlLocator.class);

    private String baseURI;
    private String lastImportLocation;
    private SimpleUriResolver resolver;

    public SimpleWsdlLocator(String baseURI) {
        this.baseURI = baseURI;
        this.resolver = new SimpleUriResolver();
    }

    public InputSource getBaseInputSource() {
        return resolve("", this.baseURI);
    }

    public String getBaseURI() {
        return this.baseURI;
    }

    public InputSource getImportInputSource(String parentLocation, String importLocation) {
        return resolve(parentLocation, importLocation);
    }

    protected InputSource resolve(String parentLocation, String importLocation) {
        if (logger.isDebugEnabled()) {
            logger.debug("Resolving '" + importLocation + "' relative to '" + parentLocation + "'");
        }
        try {
            this.resolver.resolve(parentLocation, importLocation);
            if (this.resolver.isResolved()) {
                this.lastImportLocation = this.resolver.getURI().toString();
                if (logger.isDebugEnabled()) {
                    logger.debug("Resolved location '" + this.lastImportLocation + "'");
                }
                return new InputSource(this.resolver.getInputStream());
            }
        } catch (IOException e) {
            // ignore
        }
        return null;
    }

    public String getLatestImportURI() {
        return this.lastImportLocation;
    }

    public void close() {
    }
}
