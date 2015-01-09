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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.arquillian.tests.realm;

import org.apache.openejb.arquillian.common.IO;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class Https {
    public static String connection(final URL webapp, final String path, final String username, final String password) throws IOException {
        final HttpURLConnection con = (HttpURLConnection) new URL(webapp.toExternalForm() + path).openConnection();
        String userCredentials = username + ":" + password;
        String basicAuth = "Basic " + DatatypeConverter.printBase64Binary(userCredentials.getBytes());
        con.setRequestProperty("Authorization", basicAuth);
        con.setUseCaches(false);
        try {
            return IO.slurp(con.getInputStream());
        } finally {
            IO.close(con.getInputStream());
            con.disconnect();
        }
    }

    private Https() {
        // no-op
    }
}
