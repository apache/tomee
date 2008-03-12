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

import java.util.Hashtable;

import javax.naming.Context;

import org.junit.Test;

/**
 * @version $Rev$ $Date$
 */
public class JNDIContextTest {

    @Test
    public void testGetInitialContext() throws Exception {
        JNDIContext jndiContext = new JNDIContext();
        Hashtable<String, String> env = new Hashtable<String, String>();

        assertEquals(jndiContext, "ejbd://localhost:4201");

        assertEquals(jndiContext, "http://localhost");

        assertEquals(jndiContext, "anything://localhost:4201");

        assertEquals(jndiContext, "//localhost:4201");

        assertEquals(jndiContext, "localhost", "ejbd://localhost:4201");

        assertEquals(jndiContext, "localhost:4201", "ejbd://localhost:4201");

        assertEquals(jndiContext, "", "ejbd://localhost:4201");

        assertEquals(jndiContext, "ejbd://127.0.0.1:4201");

        assertEquals(jndiContext, "http://127.0.0.1");

        assertEquals(jndiContext, "anything://127.0.0.1:4201");

        assertEquals(jndiContext, "//127.0.0.1:4201");

        assertEquals(jndiContext, "127.0.0.1", "ejbd://127.0.0.1:4201");

        assertEquals(jndiContext, "127.0.0.1:4201", "ejbd://127.0.0.1:4201");

    }

    private void assertEquals(JNDIContext jndiContext, String providerUrl) throws Exception {
        assertEquals(jndiContext, providerUrl, providerUrl);
    }

    private void assertEquals(JNDIContext jndiContext, String providerUrl, String expectedProviderUrl) throws Exception {
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.PROVIDER_URL, providerUrl);
        JNDIContext ctx = (JNDIContext) jndiContext.getInitialContext(env);
        String actualProviderUrl = ctx.addMissingParts(providerUrl);
        assert expectedProviderUrl.equals(actualProviderUrl) : "Expected " + expectedProviderUrl + " but was " + actualProviderUrl;
    }
}
