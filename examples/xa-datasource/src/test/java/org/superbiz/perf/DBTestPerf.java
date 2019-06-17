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
package org.superbiz.perf;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.junit.Assert;
import org.tomitribe.util.IO;

import java.net.URI;

public class DBTestPerf extends Assert {

    private final CloseableHttpClient httpClient;
    private final URI webappUri;

    public DBTestPerf(String webappUrl) {
        System.out.println("Hello");
        webappUri = URI.create(webappUrl);
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(200);
        cm.setDefaultMaxPerRoute(200);
        httpClient = HttpClients.custom()
                .setConnectionManager(cm)
                .build();
    }

    public void get() throws Exception {
        {
            final HttpGet get = new HttpGet(webappUri.resolve("dbtest/list"));
            try (CloseableHttpResponse response = httpClient.execute(get)) {
                IO.slurp(response.getEntity().getContent());
                assertEquals(200, response.getStatusLine().getStatusCode());
            }

        }
    }

}
