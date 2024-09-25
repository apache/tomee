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

import jakarta.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class HttpServletRequestProxyTest {

    @Test
    public void test() throws Exception {
        final HttpServletRequest request = HttpServletRequestProxy.get();

        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final ObjectOutputStream so = new ObjectOutputStream(outputStream);
        so.writeObject(request);
        so.flush();

        final byte[] bytes = outputStream.toByteArray();

        final ByteArrayInputStream is = new ByteArrayInputStream(bytes);
        final ObjectInputStream ois = new ObjectInputStream(is);
        final HttpServletRequest readRequest = (HttpServletRequest) ois.readObject();

        Assert.assertNotNull(readRequest);
    }

    @Test
    public void noRequestActive() {
        final HttpServletRequest request = HttpServletRequestProxy.get();

        RequestNotActiveException exception = Assert.assertThrows(RequestNotActiveException.class, () -> request.getRequestURI());
        Assert.assertEquals("Method 'getRequestURI' was invoked on HttpServletRequest, but no servlet request is active on the current thread", exception.getMessage());
    }
}
