/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tomee.embedded;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.AbstractHttp11Protocol;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ConnectorConfigTest {
    @Test
    public void createConnector() {
        final AtomicReference<Connector> connector = new AtomicReference<>();
        try {
            new Container(new Configuration()
                    .property("connector.xpoweredBy", "true")
                    .property("connector.attributes.compression", "on")
                    .property("connector.attributes.maxHeaderCount", "2016")) {

                @Override
                protected Connector createConnector() {
                    final Connector connector1 = super.createConnector();
                    connector.set(connector1);
                    throw new RuntimeException("end");
                }
            };
            fail("we throw an exception to prevent startup");
        } catch (final Exception re) {
            assertEquals(re.getMessage(), "java.lang.RuntimeException: end", re.getMessage());

            final Connector c = connector.get();
            assertNotNull(c);
            assertTrue(c.getXpoweredBy());
            assertEquals(2016, AbstractHttp11Protocol.class.cast(c.getProtocolHandler()).getMaxHeaderCount());
            assertEquals("on", AbstractHttp11Protocol.class.cast(c.getProtocolHandler()).getCompression());
        }
    }
}
