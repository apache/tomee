package org.apache.openejb.server.ejbd;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.openejb.client.AuthenticationRequest;
import org.apache.openejb.client.AuthenticationResponse;
import org.apache.openejb.client.ClientMetaData;
import org.apache.openejb.client.RequestMethods;
import org.apache.openejb.client.ResponseCodes;

class AuthRequestHandler implements ResponseCodes, RequestMethods {
    private final EjbDaemon daemon;

    AuthRequestHandler(EjbDaemon daemon) {
        this.daemon = daemon;

    }

    public void processRequest(ObjectInputStream in, ObjectOutputStream out) {
        AuthenticationRequest req = new AuthenticationRequest();
        AuthenticationResponse res = new AuthenticationResponse();

        try {
            req.readExternal(in);

            ClientMetaData client = new ClientMetaData();

            client.setClientIdentity(new String((String) req.getPrinciple()));

            res.setIdentity(client);
            res.setResponseCode(AUTH_GRANTED);

            res.writeExternal(out);
        } catch (Throwable t) {

            return;
        }
    }
}