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

import junit.framework.TestCase;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import jakarta.xml.ws.Endpoint;
import jakarta.xml.ws.EndpointReference;
import jakarta.xml.ws.WebServiceFeature;
import jakarta.xml.ws.spi.Provider;
import jakarta.xml.ws.spi.ServiceDelegate;
import jakarta.xml.ws.wsaddressing.W3CEndpointReference;
import java.net.URL;
import java.util.List;

public class JaxWsProviderWrapperTest extends TestCase {

    public void test() throws Exception {
        System.setProperty(Provider.class.getName(), MockProvider.class.getName());
        Provider provider = Provider.provider();
        assertNotNull("provider is null", provider);
        assertFalse("provider should not be an instance of ProviderWrapper", provider instanceof JaxWsProviderWrapper);

        JaxWsProviderWrapper.beforeCreate(null);
        try {
            provider = Provider.provider();
            assertNotNull("provider is null", provider);
            assertTrue("provider should be an instance of ProviderWrapper", provider instanceof JaxWsProviderWrapper);
            final JaxWsProviderWrapper providerWrapper = (JaxWsProviderWrapper) provider;

            // check delegate
            final Provider delegate = providerWrapper.getDelegate();
            assertNotNull("providerWrapper delegate is null", delegate);
            assertFalse("providerWrapper delegate should not be an instance of ProviderWrapper", delegate instanceof JaxWsProviderWrapper);
        } finally {
            JaxWsProviderWrapper.afterCreate();
        }
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public static class MockProvider extends Provider {

        @Override
        public ServiceDelegate createServiceDelegate(final URL url, final QName qName, final Class aClass) {
            return null;
        }

        @Override
        public Endpoint createEndpoint(final String string, final Object object) {
            return null;
        }

        @Override
        public Endpoint createAndPublishEndpoint(final String string, final Object object) {
            return null;
        }

        @Override
        public W3CEndpointReference createW3CEndpointReference(final String address,
                                                               final QName serviceName,
                                                               final QName portName,
                                                               final List<Element> metadata,
                                                               final String wsdlDocumentLocation,
                                                               final List<Element> referenceParameters) {
            return null;
        }

        @Override
        public EndpointReference readEndpointReference(final Source source) {
            return null;
        }

        @Override
        public <T> T getPort(final EndpointReference endpointReference, final Class<T> serviceEndpointInterface, final WebServiceFeature... features) {
            return null;
        }
    }
}
