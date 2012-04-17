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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.arquillian.tests.jaxrs;

import junit.framework.Assert;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.ziplock.IO;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * @version $Rev$ $Date$
 */
public class JaxrsTest {

    protected HttpClient client = new DefaultHttpClient();

    protected Map<String, String> headers(String... h) throws IOException {
        Map<String, String> map = new HashMap<String, String>();

        for (int i = 0; i < h.length - 1; ) {
            String key = h[i++];
            String value = h[i++];
            map.put(key, value);
        }

        return map;
    }

    protected String get(String path) throws IOException {
        return get(headers(), path);
    }

    protected String get(Map<String, String> headers, String path) throws IOException {

        final URI uri = uri(path);

        final HttpGet get = new HttpGet(uri);

        for (Map.Entry<String, String> header : headers.entrySet()) {
            get.setHeader(header.getKey(), header.getValue());
        }

        final HttpResponse execute = client.execute(get);
        if (execute.getStatusLine().getStatusCode() != 200) {
            throw new IOException(execute.getStatusLine().toString());
        }

        return asString(execute);
    }

    protected URI uri(String path) {
        if (path.startsWith("/")) path = path.substring(1);
        final String port = System.getProperty("tomee.httpPort", "11080");
        return URI.create(String.format("http://localhost:%s/%s/%s", port, this.getClass().getSimpleName(), path));
    }

    public static void assertStatusCode(int actual, HttpResponse response) {
        Assert.assertEquals(response.getStatusLine().getStatusCode(), actual);
    }

    public static String asString(HttpResponse execute) throws IOException {
        final InputStream in = execute.getEntity().getContent();
        try {
            return IO.slurp(in);
        } finally {
            in.close();
        }
    }

}
