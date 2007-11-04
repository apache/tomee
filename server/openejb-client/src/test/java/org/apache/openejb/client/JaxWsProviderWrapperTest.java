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

import javax.xml.ws.spi.Provider;
import javax.xml.ws.spi.ServiceDelegate;
import javax.xml.ws.Endpoint;
import javax.xml.namespace.QName;
import java.net.URL;

public class JaxWsProviderWrapperTest extends TestCase {
    public void test() throws Exception {
        System.setProperty(Provider.JAXWSPROVIDER_PROPERTY, MockProvider.class.getName());
        Provider provider = Provider.provider();
        assertNotNull("provider is null", provider);
        assertFalse("provider should not be an instance of ProviderWrapper", provider instanceof JaxWsProviderWrapper);

        JaxWsProviderWrapper.beforeCreate(null);
        try {
            provider = Provider.provider();
            assertNotNull("provider is null", provider);
            assertTrue("provider should be an instance of ProviderWrapper", provider instanceof JaxWsProviderWrapper);
            JaxWsProviderWrapper providerWrapper = (JaxWsProviderWrapper)provider;

            // check delegate
            Provider delegate = providerWrapper.getDelegate();
            assertNotNull("providerWrapper delegate is null", delegate);
            assertFalse("providerWrapper delegate should not be an instance of ProviderWrapper", delegate instanceof JaxWsProviderWrapper);
        } finally {
            JaxWsProviderWrapper.afterCreate();
        }
    }

    public static class MockProvider extends Provider {
        public ServiceDelegate createServiceDelegate(URL url, QName qName, Class aClass) {
            return null;
        }

        public Endpoint createEndpoint(String string, Object object) {
            return null;
        }

        public Endpoint createAndPublishEndpoint(String string, Object object) {
            return null;
        }
    }
}
