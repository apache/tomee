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
package org.apache.openejb.test.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;
import javax.naming.InitialContext;

import org.apache.openejb.test.TestClient;
import org.apache.openejb.test.TestManager;

public abstract class ServletTestClient extends TestClient {
    protected URL serverUrl;
    private final String servletName;

    public ServletTestClient(String servletName) {
        super("Servlet." + servletName + ".");
        this.servletName = servletName;
        String serverUri = System.getProperty("openejb.server.uri", "http://127.0.0.1:8080/openejb/ejb");
        try {
            serverUrl = new URL(serverUri);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sets up the fixture, for example, open a network connection.
     * This method is called before a test is executed.
     */
    protected void setUp() throws Exception {

        Properties properties = TestManager.getServer().getContextEnvironment();
        //properties.put(Context.SECURITY_PRINCIPAL, "STATEFUL_test00_CLIENT");
        //properties.put(Context.SECURITY_CREDENTIALS, "STATEFUL_test00_CLIENT");

        initialContext = new InitialContext(properties);
    }

    protected Object invoke(String methodName) {
        InputStream in = null;
        try {
            URL url = new URL(serverUrl, "/itests/" + servletName + "?method=" + methodName);
            URLConnection connection = url.openConnection();
            connection.connect();
            in = connection.getInputStream();
            String response = readAll(in);
            if (response.startsWith("FAILED")) {
                response = response.substring("FAILED".length()).trim();
                fail(response);
            }
        } catch (Exception e) {
            fail("Received Exception " + e.getClass() + " : " + e.getMessage());
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception ignored) {
                }
            }
        }
        return null;
    }

    private static String readAll(InputStream in) throws IOException {
        // SwizzleStream block read methods are broken so read byte at a time
        StringBuilder sb = new StringBuilder();
        int i = in.read();
        while (i != -1) {
            sb.append((char) i);
            i = in.read();
        }
        return sb.toString();
    }

    @SuppressWarnings({"unchecked"})
    protected <T> T newServletProxy(Class<T> clazz) {
        Object proxy = Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{clazz}, new ServletInvocationHandler());
        return (T) proxy;
    }

    private class ServletInvocationHandler implements InvocationHandler {
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getParameterTypes().length != 0) throw new IllegalArgumentException("ServletProxy only supports no-argument methods: " + method);

            String methodName = method.getName();

            return ServletTestClient.this.invoke(methodName);
        }
    }
}