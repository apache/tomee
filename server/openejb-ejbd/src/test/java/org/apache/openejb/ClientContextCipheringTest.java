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
package org.apache.openejb;

import org.apache.openejb.cipher.StaticDESPasswordCipher;
import org.apache.openejb.client.JNDIContext;
import org.junit.Assert;
import org.junit.Test;

import javax.naming.Context;
import javax.naming.NamingException;
import java.util.Hashtable;

public class ClientContextCipheringTest {
    @Test
    public void customCipher() throws NamingException {
        final JNDIContext jndiContext = new JNDIContext();
        final Hashtable<String, Object> env = new Hashtable<>();
        env.put(Context.PROVIDER_URL, "cipher:Static3DES:" + String.valueOf(new StaticDESPasswordCipher().encrypt("ejbd://localhost:1234")));
        jndiContext.getInitialContext(env);
        Assert.assertEquals("ejbd://localhost:1234", jndiContext.getEnvironment().get(Context.PROVIDER_URL).toString());
    }
}
