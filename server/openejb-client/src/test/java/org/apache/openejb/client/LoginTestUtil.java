/**
 *
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
package org.apache.openejb.client;

import java.rmi.RemoteException;
import java.net.URL;
import java.net.URLDecoder;

public class LoginTestUtil {
    public static Request serverRequest;
    public static AuthenticationResponse serverResponse;

    public static void initialize() throws Exception {
        String path = System.getProperty("java.security.auth.login.config");
        if (path == null) {
            URL resource = ClientLoginTest.class.getClassLoader().getResource("client.login.conf");
            if (resource != null) {
                path = URLDecoder.decode(resource.getFile());
                System.setProperty("java.security.auth.login.config", path);
            }
        }

        Client.setClient(new Client() {
            protected Response processRequest(Request req, Response res, ServerMetaData server) throws RemoteException {
                serverRequest = req;
                return serverResponse;
            }
        });
    }

    static void setAuthGranted() {
        serverResponse = new AuthenticationResponse();
        serverResponse.setIdentity(new ClientMetaData("SecretIdentity"));
        serverResponse.setResponseCode(ResponseCodes.AUTH_GRANTED);
    }

    static void setAuthDenied() {
        // setup the server response
        serverResponse = new AuthenticationResponse();
        serverResponse.setResponseCode(ResponseCodes.AUTH_DENIED);
    }
}
