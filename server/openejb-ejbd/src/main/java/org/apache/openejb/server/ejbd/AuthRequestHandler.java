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
import org.apache.openejb.client.ProtocolMetaData;
import org.apache.openejb.client.Response;
import org.apache.openejb.client.ResponseCodes;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

class AuthRequestHandler extends RequestHandler {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB_SERVER_REMOTE.createChild("auth"), "org.apache.openejb.server.util.resources");
    private static final boolean DEBUG = LOGGER.isDebugEnabled();

    protected AuthRequestHandler(final EjbDaemon daemon) {
        super(daemon);
    }

    @Override
    public String getName() {
        return "Authentication";
    }

    @Override
    public Logger getLogger() {
        return LOGGER;
    }

    @Override
    public Response processRequest(final ObjectInputStream in, final ProtocolMetaData metaData) throws Exception {

        final AuthenticationRequest req = new AuthenticationRequest();
        req.setMetaData(metaData);

        final AuthenticationResponse res = new AuthenticationResponse();
        res.setMetaData(metaData);

        try {
            req.readExternal(in);

            final String securityRealm = req.getRealm();
            final String username = req.getUsername();
            final String password = req.getCredentials();

            final SecurityService securityService = SystemInstance.get().getComponent(SecurityService.class);
            final Object token = securityService.login(securityRealm, username, password);

            final ClientMetaData client = new ClientMetaData();
            client.setMetaData(metaData);
            client.setClientIdentity(token);

            res.setIdentity(client);
            res.setResponseCode(ResponseCodes.AUTH_GRANTED);
        } catch (Throwable t) {
            res.setResponseCode(ResponseCodes.AUTH_DENIED);
            res.setDeniedCause(t);
        } finally {
            if (DEBUG) {
                try {
                    LOGGER.debug("AUTH REQUEST: " + req + " -- RESPONSE: " + res);
                } catch (Exception e) {
                    //Ignore
                }
            }
        }

        return res;
    }

    @Override
    public void processResponse(final Response response, final ObjectOutputStream out, final ProtocolMetaData metaData) throws Exception {

        if (AuthenticationResponse.class.isInstance(response)) {

            final AuthenticationResponse res = (AuthenticationResponse) response;
            res.setMetaData(metaData);

            try {
                res.writeExternal(out);
            } catch (Exception e) {
                LOGGER.fatal("Could not write AuthenticationResponse to output stream", e);
            }
        } else {
            LOGGER.error("AuthRequestHandler cannot process an instance of: " + response.getClass().getName());
        }
    }
}