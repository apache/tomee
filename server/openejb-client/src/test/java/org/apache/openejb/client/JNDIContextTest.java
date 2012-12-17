/**
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
package org.apache.openejb.client;

import org.junit.Assert;
import org.junit.Test;

import javax.naming.Context;
import java.util.Hashtable;

/**
 * @version $Rev$ $Date$
 */
@SuppressWarnings("UseOfObsoleteCollectionType")
public class JNDIContextTest {

    @Test
    public void testGetInitialContext() throws Exception {

        final int port = Integer.parseInt(System.getProperty("ejbd.port", "4201"));

        final JNDIContext jndiContext = new JNDIContext();
        final Hashtable<String, String> env = new Hashtable<String, String>();

        assertEquals(jndiContext, "ejbd://localhost:" + port);

        assertEquals(jndiContext, "http://localhost");

        assertEquals(jndiContext, "anything://localhost:" + port);

        assertEquals(jndiContext, "//localhost:" + port);

        assertEquals(jndiContext, "localhost", "ejbd://localhost:" + port);

        assertEquals(jndiContext, "localhost:" + port, "ejbd://localhost:" + port);

        assertEquals(jndiContext, "", "ejbd://localhost:" + port);

        assertEquals(jndiContext, "ejbd://127.0.0.1:" + port);

        assertEquals(jndiContext, "http://127.0.0.1");

        assertEquals(jndiContext, "anything://127.0.0.1:" + port);

        assertEquals(jndiContext, "//127.0.0.1:" + port);

        assertEquals(jndiContext, "127.0.0.1", "ejbd://127.0.0.1:" + port);

        assertEquals(jndiContext, "127.0.0.1:" + port, "ejbd://127.0.0.1:" + port);

    }

    private void assertEquals(final JNDIContext jndiContext, final String providerUrl) throws Exception {
        assertEquals(jndiContext, providerUrl, providerUrl);
    }

    private void assertEquals(final JNDIContext jndiContext, final String providerUrl, final String expectedProviderUrl) throws Exception {
        final Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.PROVIDER_URL, providerUrl);
        final JNDIContext ctx = (JNDIContext) jndiContext.getInitialContext(env);
        final String actualProviderUrl = ctx.addMissingParts(providerUrl);
        Assert.assertEquals("Expected " + expectedProviderUrl + " but was " + actualProviderUrl, expectedProviderUrl, actualProviderUrl);
    }
}
