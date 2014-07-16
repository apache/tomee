/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License") you may not use this file except in compliance with
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

package org.apache.tomee.webaccess.test.helpers

import org.apache.commons.io.FileUtils
import org.apache.http.Consts
import org.apache.http.NameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.message.BasicNameValuePair
import org.apache.http.util.EntityUtils

class Utilities {

    static void copyFile(String from, String to) {
        FileUtils.copyInputStreamToFile(
                Thread.currentThread().contextClassLoader.getResourceAsStream(from),
                new File("${System.getProperty("arquillian.tomee.path")}/${to}")
        )
    }

    static String getBody(CloseableHttpResponse response) {
        String body = null
        try {
            def entity = response.getEntity()
            body = EntityUtils.toString(entity)
            EntityUtils.consume(entity)
        } finally {
            response.close()
        }
        body
    }

    static String post(URL deploymentURL, CloseableHttpClient client, String path, NameValuePair... postParams) {
        def post = new HttpPost("${deploymentURL.toURI()}${path}")
        post.entity = new UrlEncodedFormEntity(new ArrayList<NameValuePair>(Arrays.asList(postParams)), Consts.UTF_8)
        getBody(client.execute(post))
    }

    static def withClient(URL deploymentURL, callback) {
        def client = HttpClients.custom().build()
        post(deploymentURL, client, 'rest/authentication',
                new BasicNameValuePair('user', 'admin'),
                new BasicNameValuePair('password', 'admin')
        )
        callback(client)
        client.close()
    }


}
