/*
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

package org.apache.tomee.catalina;

import org.junit.Assert;
import org.junit.Test;

import jakarta.servlet.http.HttpSession;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class HttpSessionProxyTest {

    @Test
    public void test() throws Exception {
        final HttpSession session = HttpSessionProxy.get();

        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final ObjectOutputStream so = new ObjectOutputStream(outputStream);
        so.writeObject(session);
        so.flush();

        final byte[] bytes = outputStream.toByteArray();

        final ByteArrayInputStream is = new ByteArrayInputStream(bytes);
        final ObjectInputStream ois = new ObjectInputStream(is);
        final HttpSession readSession = (HttpSession) ois.readObject();

        Assert.assertNotNull(readSession);
    }
}
