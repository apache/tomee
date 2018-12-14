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

import org.apache.openejb.client.LogoutRequest;
import org.apache.openejb.client.LogoutResponse;
import org.apache.openejb.client.ProtocolMetaData;
import org.apache.openejb.client.Response;
import org.apache.openejb.client.ResponseCodes;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

class LogoutRequestHandler extends RequestHandler {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB_SERVER_REMOTE.createChild("auth"), "org.apache.openejb.server.util.resources");
    private static final boolean DEBUG = LOGGER.isDebugEnabled();

    protected LogoutRequestHandler(final EjbDaemon daemon) {
        super(daemon);
    }

    @Override
    public String getName() {
        return "Logout";
    }

    @Override
    public Logger getLogger() {
        return LOGGER;
    }

    @Override
    public Response processRequest(final ObjectInputStream in, final ProtocolMetaData metaData) throws Exception {
        final LogoutRequest req = new LogoutRequest();
        req.setMetaData(metaData);

        final LogoutResponse res = new LogoutResponse();
        res.setMetaData(metaData);

        try {
            req.readExternal(in);

            SystemInstance.get().getComponent(SecurityService.class).logout(req.getSecurityIdentity());

            res.setResponseCode(ResponseCodes.LOGOUT_SUCCESS);
        } catch (final Throwable t) {
            res.setResponseCode(ResponseCodes.LOGOUT_FAILED);
            res.setDeniedCause(t);
        } finally {
            if (DEBUG) {
                try {
                    LOGGER.debug("LOGOUT REQUEST: " + req + " -- RESPONSE: " + res);
                } catch (Exception e) {
                    //Ignore
                }
            }
        }

        return res;
    }

    @Override
    public void processResponse(final Response response, final ObjectOutputStream out, final ProtocolMetaData metaData) throws Exception {
        if (LogoutResponse.class.isInstance(response)) {
            final LogoutResponse res = LogoutResponse.class.cast(response);
            res.setMetaData(metaData);
            try {
                res.writeExternal(out);
            } catch (final Exception e) {
                LOGGER.fatal("Could not write AuthenticationResponse to output stream", e);
            }
        } else {
            LOGGER.error("AuthRequestHandler cannot process an instance of: " + response.getClass().getName());
        }
    }
}