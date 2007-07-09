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
package org.apache.openejb.server.ejbd;

import org.apache.openejb.client.AuthenticationRequest;
import org.apache.openejb.client.AuthenticationResponse;
import org.apache.openejb.client.ClientMetaData;
import org.apache.openejb.client.ResponseCodes;
import org.apache.openejb.client.RealmPrincipalInfo;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.util.Messages;
import org.apache.openejb.util.Logger;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;

import javax.security.auth.login.LoginException;

class AuthRequestHandler {

    Messages _messages = new Messages("org.apache.openejb.server.util.resources");
    Logger logger = Logger.getInstance("OpenEJB.server.remote", "org.apache.openejb.server.util.resources");

    AuthRequestHandler(EjbDaemon daemon) {
    }

    public void processRequest(ObjectInputStream in, ObjectOutputStream out) {
        AuthenticationRequest req = new AuthenticationRequest();
        AuthenticationResponse res = new AuthenticationResponse();

        try {
            req.readExternal(in);

            String securityRealm = null;
            String username;
            if (req.getPrincipal() instanceof String) {
                username = (String) req.getPrincipal();
            } else if (req.getPrincipal() instanceof RealmPrincipalInfo) {
                RealmPrincipalInfo info = (RealmPrincipalInfo)req.getPrincipal();
                securityRealm = info.getSecurityRealm();
                username = info.getPrincipalName();
            } else {
                throw new LoginException("Unkown message principal object: " + req.getPrincipal());
            }
            String password = (String) req.getCredentials();

            SecurityService securityService = SystemInstance.get().getComponent(SecurityService.class);
            Object token = securityService.login(securityRealm, username, password);

            ClientMetaData client = new ClientMetaData();
            client.setClientIdentity(token);

            res.setIdentity(client);
            res.setResponseCode(ResponseCodes.AUTH_GRANTED);

            res.writeExternal(out);
        } catch (Throwable t) {
            try {
                res.setResponseCode(ResponseCodes.AUTH_DENIED);
                res.writeExternal(out);
            } catch (IOException e) {
                logger.error("Failed to write to AuthenticationResponse", e);
            }
        }
    }


}