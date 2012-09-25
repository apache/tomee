/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.webbeans.web.tomcat7.test;


import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.StringBuilder;

/**
 * Simple requests to the tomcat installation
 */
public class OwbTomcatPluginIT
{
    @Test
    public void testTomcatRequest() throws Exception
    {
        DefaultHttpClient httpclient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet("http://localhost:9086/owbtomcat6it/test.test");

        HttpResponse response = httpclient.execute(httpGet);

        // Get the response
        BufferedReader rd = new BufferedReader
                (new InputStreamReader(response.getEntity().getContent()));

        StringBuilder builder = new StringBuilder();
        String line = "";
        while ((line = rd.readLine()) != null) {
            builder.append(line);
        }

        Assert.assertNotNull(response);
        Assert.assertEquals("Got " + builder.toString(), 200, response.getStatusLine().getStatusCode());
    }

}
